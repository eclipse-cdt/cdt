/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.board.ToolDependency;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ArduinoBuildConfiguration extends CBuildConfiguration implements TemplateLoader {

	private static final String PACKAGE_NAME = "packageName"; //$NON-NLS-1$
	private static final String PLATFORM_NAME = "platformName"; //$NON-NLS-1$
	private static final String BOARD_NAME = "boardName"; //$NON-NLS-1$

	private static ArduinoManager manager = Activator.getService(ArduinoManager.class);

	private final ArduinoBoard board;
	private final String launchMode;
	private Properties properties;

	// for Makefile generation
	private Configuration templateConfig;

	private final static boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);

	public ArduinoBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		Preferences settings = getSettings();
		String packageName = settings.get(PACKAGE_NAME, ""); //$NON-NLS-1$
		String platformName = settings.get(PLATFORM_NAME, ""); //$NON-NLS-1$
		String boardName = settings.get(BOARD_NAME, ""); //$NON-NLS-1$
		ArduinoBoard b = manager.getBoard(packageName, platformName, boardName);

		if (b == null) {
			// Default to Uno or first one we find
			b = manager.getBoard("Arduino/Genuino Uno", "Arduino AVR Boards", "arduino"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (b == null) {
				Collection<ArduinoBoard> boards = manager.getInstalledBoards();
				if (!boards.isEmpty()) {
					b = boards.iterator().next();
				}
			}
		}
		board = b;

		int i = name.lastIndexOf('.');
		this.launchMode = name.substring(i + 1);
	}

	ArduinoBuildConfiguration(IBuildConfiguration config, String name, ArduinoBoard board, String launchMode,
			IToolChain toolChain)
			throws CoreException {
		super(config, name, toolChain);
		this.board = board;
		this.launchMode = launchMode;

		// Store the board identifer
		ArduinoPlatform platform = board.getPlatform();
		ArduinoPackage pkg = platform.getPackage();

		Preferences settings = getSettings();
		settings.put(PACKAGE_NAME, pkg.getName());
		settings.put(PLATFORM_NAME, platform.getName());
		settings.put(BOARD_NAME, board.getName());

		try {
			settings.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Saving preferences", e)); //$NON-NLS-1$
		}
	}

	ArduinoBuildConfiguration(IBuildConfiguration config, String name, ArduinoRemoteConnection target,
			String launchMode, IToolChain toolChain) throws CoreException {
		this(config, name, target.getBoard(), launchMode, toolChain);

		// Store the menu settings
		HierarchicalProperties menus = board.getMenus();
		if (menus != null) {
			Preferences settings = getSettings();
			for (String id : menus.getChildren().keySet()) {
				String key = ArduinoBoard.MENU_QUALIFIER + id;
				String value = target.getRemoteConnection().getAttribute(key);
				if (value != null) {
					settings.put(key, value);
				}
			}

			try {
				settings.flush();
			} catch (BackingStoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Saving preferences", e)); //$NON-NLS-1$
			}
		}
	}

	static String generateName(ArduinoBoard board, String launchMode) {
		return "arduino." + board.getId() + '.' + launchMode; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(ArduinoBuildConfiguration.class)) {
			return (T) this;
		}
		return super.getAdapter(adapter);
	}

	public String getLaunchMode() {
		return launchMode;
	}

	public boolean matches(ArduinoRemoteConnection target) throws CoreException {
		ArduinoBoard otherBoard = target.getBoard();
		if (!getBoard().equals(otherBoard)) {
			return false;
		}

		Preferences settings = getSettings();
		HierarchicalProperties menus = board.getMenus();
		if (menus != null) {
			for (String id : menus.getChildren().keySet()) {
				String key = ArduinoBoard.MENU_QUALIFIER + id;
				if (!settings.get(key, "").equals(target.getRemoteConnection().getAttribute(key))) { //$NON-NLS-1$
					return false;
				}
			}
		}

		return true;
	}

	public ArduinoBoard getBoard() throws CoreException {
		return board;
	}

	private synchronized Properties getProperties() throws CoreException {
		if (properties == null) {
			ArduinoPlatform platform = board.getPlatform();

			// IDE generated properties
			properties = new Properties();
			properties.put("runtime.platform.path", platform.getInstallPath().toString()); //$NON-NLS-1$
			properties.put("runtime.ide.version", "10607"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("software", "ARDUINO"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("build.arch", platform.getArchitecture().toUpperCase()); //$NON-NLS-1$
			String configName = getBuildConfiguration().getName();
			if (configName.equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
				configName = "default"; //$NON-NLS-1$
			}
			properties.put("build.path", configName); //$NON-NLS-1$
			properties.put("build.variant.path", //$NON-NLS-1$
					platform.getInstallPath().resolve("variants").resolve("{build.variant}").toString()); //$NON-NLS-1$ //$NON-NLS-2$

			// Platform
			properties.putAll(board.getPlatform().getPlatformProperties());

			// Tools
			for (ToolDependency toolDep : platform.getToolsDependencies()) {
				properties.putAll(toolDep.getTool().getToolProperties());
			}

			// Board
			ArduinoBoard board = getBoard();
			properties.putAll(board.getBoardProperties());

			// Menus
			Preferences settings = getSettings();
			HierarchicalProperties menus = board.getMenus();
			if (menus != null) {
				for (String menuId : menus.getChildren().keySet()) {
					String value = settings.get(ArduinoBoard.MENU_QUALIFIER + menuId, ""); //$NON-NLS-1$
					if (!value.isEmpty()) {
						properties.putAll(board.getMenuProperties(menuId, value));
					}
				}
			}
		}

		// always do this in case the project changes names
		properties.put("build.project_name", getProject().getName()); //$NON-NLS-1$
		return properties;
	}

	public IFile getMakeFile() throws CoreException {
		IFolder buildFolder = (IFolder) getBuildContainer();
		ArduinoBoard board = getBoard();
		String makeFileName = board.getId() + ".mk"; //$NON-NLS-1$
		return buildFolder.getFile(makeFileName);
	}

	public Map<String, Object> getBuildModel() throws CoreException {
		IProject project = getProject();
		ArduinoBoard board = getBoard();
		ArduinoPlatform platform = board.getPlatform();

		Map<String, Object> buildModel = new HashMap<>();
		buildModel.put("boardId", board.getId()); //$NON-NLS-1$

		// The list of source files in the project
		final Path projectPath = new File(project.getLocationURI()).toPath();
		final List<String> sourceFiles = new ArrayList<>();
		for (ISourceRoot sourceRoot : CCorePlugin.getDefault().getCoreModel().create(project).getSourceRoots()) {
			sourceRoot.getResource().accept(new IResourceProxyVisitor() {
				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						if (isSource(proxy.getName())) {
							Path sourcePath = new File(proxy.requestResource().getLocationURI()).toPath();
							sourceFiles.add(pathString(projectPath.relativize(sourcePath)));
						}
					}
					return true;
				}
			}, 0);
		}
		buildModel.put("project_srcs", sourceFiles); //$NON-NLS-1$

		// The list of library sources
		List<String> librarySources = new ArrayList<>();
		for (ArduinoLibrary lib : manager.getLibraries(project)) {
			librarySources.addAll(lib.getSources());
		}
		buildModel.put("libraries_srcs", librarySources); //$NON-NLS-1$
		buildModel.put("libraries_path", pathString(ArduinoPreferences.getArduinoHome().resolve("libraries"))); //$NON-NLS-1$ //$NON-NLS-2$

		// the recipes
		Properties properties = new Properties();
		properties.putAll(getProperties());
		buildModel.put("build_path", properties.get("build.path")); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("project_name", project.getName()); //$NON-NLS-1$

		String includes = null;
		for (Path include : platform.getIncludePath()) {
			if (includes == null) {
				includes = "-I"; //$NON-NLS-1$
			} else {
				includes += " -I"; //$NON-NLS-1$
			}
			includes += '"' + pathString(include) + '"';
		}
		for (ArduinoLibrary lib : manager.getLibraries(project)) {
			for (Path include : lib.getIncludePath()) {
				includes += " -I\"" + pathString(include) + '"'; //$NON-NLS-1$
			}
		}
		properties.put("includes", includes); //$NON-NLS-1$

		Path platformPath = platform.getInstallPath();
		buildModel.put("platform_path", pathString(platformPath).replace("+", "\\+")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buildModel.put("platform_srcs", //$NON-NLS-1$
				platform.getSources(properties.getProperty("build.core"), properties.getProperty("build.variant"))); //$NON-NLS-1$ //$NON-NLS-2$

		properties.put("object_file", "$@"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("source_file", "$<"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("archive_file", "core.a"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("archive_file_path", "{build.path}/{archive_file}"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("object_files", "$(PROJECT_OBJS) $(LIBRARIES_OBJS)"); //$NON-NLS-1$ //$NON-NLS-2$

		buildModel.put("recipe_cpp_o_pattern", resolveProperty("recipe.cpp.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_c_o_pattern", resolveProperty("recipe.c.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_S_o_pattern", resolveProperty("recipe.S.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_ar_pattern", resolveProperty("recipe.ar.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_c_combine_pattern", resolveProperty("recipe.c.combine.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_objcopy_eep_pattern", resolveProperty("recipe.objcopy.eep.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_objcopy_hex_pattern", resolveProperty("recipe.objcopy.hex.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_objcopy_bin_pattern", resolveProperty("recipe.objcopy.bin.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_size_pattern", resolveProperty("recipe.size.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$

		return buildModel;
	}

	public IFile generateMakeFile(IProgressMonitor monitor) throws CoreException {
		IFolder buildFolder = (IFolder) getBuildContainer();
		if (!buildFolder.exists()) {
			buildFolder.create(true, true, monitor);
		}

		IFile makefile = getMakeFile();

		Map<String, Object> buildModel = getBuildModel();

		// Generate the Makefile
		try (StringWriter writer = new StringWriter()) {
			if (templateConfig == null) {
				templateConfig = new Configuration(Configuration.VERSION_2_3_22);
				templateConfig.setTemplateLoader(this);
			}

			Template template = templateConfig.getTemplate("templates/Makefile"); //$NON-NLS-1$
			template.process(buildModel, writer);
			try (ByteArrayInputStream in = new ByteArrayInputStream(
					writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8))) {
				createParent(makefile, monitor);
				if (makefile.exists()) {
					makefile.setContents(in, true, true, monitor);
				} else {
					makefile.create(in, true, monitor);
				}
			}
		} catch (IOException | TemplateException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Error generating makefile", e));
		}

		return makefile;
	}

	protected static void createParent(IResource child, IProgressMonitor monitor) throws CoreException {
		if (child == null)
			return;

		IContainer container = child.getParent();
		if (container.exists()) {
			return;
		}

		IFolder parent = container.getAdapter(IFolder.class);
		createParent(parent, monitor);
		parent.create(true, true, monitor);
	}

	public static boolean isSource(String filename) {
		int i = filename.lastIndexOf('.');
		String ext = filename.substring(i + 1);
		switch (ext) {
		case "cpp": //$NON-NLS-1$
		case "c": //$NON-NLS-1$
		case "S": //$NON-NLS-1$
			return true;
		default:
			return false;
		}
	}

	private String resolvePropertyValue(String value, Properties dict) {
		String last;
		do {
			last = value;
			for (int i = value.indexOf('{'); i >= 0; i = value.indexOf('{', i)) {
				i++;
				int n = value.indexOf('}', i);
				if (n >= 0) {
					String p2 = value.substring(i, n);
					String r2 = dict.getProperty(p2);
					if (r2 != null) {
						value = value.replace('{' + p2 + '}', r2);
					}
				}
				i = n;
			}
		} while (!value.equals(last));

		return value;
	}

	private String resolveProperty(String property, Properties dict) {
		String value = dict.getProperty(property);
		return value != null ? resolvePropertyValue(value, dict) : null;
	}

	public String getMakeCommand() {
		return isWindows ? ArduinoPreferences.getArduinoHome().resolve("make").toString() : "make"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String[] getBuildCommand() throws CoreException {
		return new String[] { getMakeCommand(), "-f", getMakeFile().getName() }; //$NON-NLS-1$
	}

	public String[] getCleanCommand() throws CoreException {
		return new String[] { getMakeCommand(), "-f", getMakeFile().getName(), "clean" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String[] getSizeCommand() throws CoreException {
		// TODO this shouldn't be in the makefile
		// should be like the upload command
		return new String[] { getMakeCommand(), "-f", getMakeFile().getName(), "size" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getCodeSizeRegex() throws CoreException {
		return getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex"); //$NON-NLS-1$
	}

	public int getMaxCodeSize() throws CoreException {
		String sizeStr = getProperties().getProperty("upload.maximum_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String getDataSizeRegex() throws CoreException {
		return getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex.data"); //$NON-NLS-1$
	}

	public int getMaxDataSize() throws CoreException {
		String sizeStr = getProperties().getProperty("upload.maximum_data_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String[] getUploadCommand(String serialPort) throws CoreException {
		String toolName = getProperties().getProperty("upload.tool"); //$NON-NLS-1$

		Properties properties = getProperties();

		properties.put("serial.port", serialPort); //$NON-NLS-1$
		// Little bit of weirdness needed for the bossac tool
		if (serialPort.startsWith("/dev/")) { //$NON-NLS-1$
			properties.put("serial.port.file", serialPort.substring(5)); //$NON-NLS-1$
		} else {
			properties.put("serial.port.file", serialPort); //$NON-NLS-1$
		}
		// to make up for some cheating in the platform.txt file
		properties.put("path", "{tools." + toolName + ".path}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.put("cmd.path", "{tools." + toolName + ".cmd.path}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.put("config.path", "{tools." + toolName + ".config.path}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// properties for the tool flattened
		HierarchicalProperties toolsProps = new HierarchicalProperties(getBoard().getPlatform().getPlatformProperties())
				.getChild("tools"); //$NON-NLS-1$
		if (toolsProps != null) {
			HierarchicalProperties toolProps = toolsProps.getChild(toolName);
			if (toolProps != null) {
				properties.putAll(toolProps.flatten());
			}
		}

		// TODO make this a preference
		properties.put("upload.verbose", properties.getProperty("upload.params.verbose", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// TODO needed this for esptool
		properties.put("upload.resetmethod", "ck"); //$NON-NLS-1$ //$NON-NLS-2$

		String command = resolveProperty("upload.pattern", properties); //$NON-NLS-1$
		if (command == null) {
			return new String[] { "command not specified" }; //$NON-NLS-1$
		}
		if (isWindows) {
			return splitCommand(command);
		} else {
			return new String[] { "sh", "-c", command }; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	// Scanner Info Cache
	private String[] cachedIncludePath;
	private String cachedInfoCommand;
	private IScannerInfo cachedScannerInfo;

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		try {
			IContentType contentType = CCorePlugin.getContentType(resource.getProject(), resource.getName());
			String recipe;
			if (contentType != null && (contentType.getId() == CCorePlugin.CONTENT_TYPE_CSOURCE
					|| contentType.getId() == CCorePlugin.CONTENT_TYPE_CSOURCE)) {
				recipe = "recipe.c.o.pattern"; //$NON-NLS-1$
			} else {
				recipe = "recipe.cpp.o.pattern"; //$NON-NLS-1$
			}

			ArduinoPlatform platform = getBoard().getPlatform();
			Properties properties = new Properties();
			properties.putAll(getProperties());

			// Overrides for scanner discovery
			properties.put("source_file", ""); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("object_file", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			// the base scanner info does includes
			properties.put("includes", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String commandString = resolveProperty(recipe, properties);

			List<Path> includePath = new ArrayList<>();
			includePath.addAll(platform.getIncludePath());
			Collection<ArduinoLibrary> libs = manager.getLibraries(getProject());
			for (ArduinoLibrary lib : libs) {
				includePath.addAll(lib.getIncludePath());
			}
			String[] includes = includePath.stream()
					.map(path -> resolvePropertyValue(path.toString(), properties)).collect(Collectors.toList())
					.toArray(new String[includePath.size()]);

			// Use cache if we can
			if (cachedScannerInfo != null && cachedInfoCommand.equals(commandString)
					&& cachedIncludePath.length == includes.length) {
				boolean matches = true;
				for (int i = 0; i < includes.length; ++i) {
					if (!includes[i].equals(cachedIncludePath[i])) {
						matches = false;
						break;
					}
				}

				if (matches) {
					return cachedScannerInfo;
				}
			}

			ExtendedScannerInfo baseInfo = new ExtendedScannerInfo(null, includes);
			String[] command = splitCommand(commandString);
			IScannerInfo info = getToolChain().getScannerInfo(getBuildConfiguration(), Paths.get(command[0]),
					Arrays.copyOfRange(command, 1, command.length), baseInfo, resource, getBuildDirectoryURI());

			// cache the results
			cachedScannerInfo = info;
			cachedInfoCommand = commandString;
			cachedIncludePath = includes;

			return info;
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}
	}

	public static String pathString(Path path) {
		String str = path.toString();
		if (isWindows) {
			str = str.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return str;
	}

	private String[] splitCommand(String command) {
		// TODO deal with quotes properly, for now just strip
		return command.replaceAll("\"", "").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		IProject project = getProject();
		try {
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

			ConsoleOutputStream consoleOut = console.getOutputStream();
			consoleOut.write(String.format("\nBuilding %s\n", project.getName()));

			generateMakeFile(monitor);

			try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
					getToolChain().getErrorParserIds())) {
				ProcessBuilder processBuilder = new ProcessBuilder().command(getBuildCommand())
						.directory(getBuildDirectory().toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				if (watchProcess(process, new IConsoleParser[] { epm }, console) == 0) {
					showSizes(console);
				}
			}

			getBuildContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}

		// TODO if there are references we want to watch, return them here
		return new IProject[] { project };
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		try {
			IProject project = getProject();
			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

			ConsoleOutputStream consoleOut = console.getOutputStream();
			consoleOut.write(String.format("\nCleaning %s\n", project.getName()));

			ProcessBuilder processBuilder = new ProcessBuilder().command(getCleanCommand())
					.directory(getBuildDirectory().toFile());
			setBuildEnvironment(processBuilder.environment());
			Process process = processBuilder.start();

			watchProcess(process, new IConsoleParser[0], console);

			getBuildContainer().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Build error", e));
		}
	}

	private void showSizes(IConsole console) throws CoreException {
		try {
			int codeSize = -1;
			int dataSize = -1;

			String codeSizeRegex = getCodeSizeRegex();
			Pattern codeSizePattern = codeSizeRegex != null ? Pattern.compile(codeSizeRegex) : null;
			String dataSizeRegex = getDataSizeRegex();
			Pattern dataSizePattern = dataSizeRegex != null ? Pattern.compile(dataSizeRegex) : null;

			if (codeSizePattern == null && dataSizePattern == null) {
				return;
			}

			int maxCodeSize = getMaxCodeSize();
			int maxDataSize = getMaxDataSize();

			ProcessBuilder processBuilder = new ProcessBuilder().command(getSizeCommand())
					.directory(getBuildDirectory().toFile()).redirectErrorStream(true);
			setBuildEnvironment(processBuilder.environment());
			Process process = processBuilder.start();
			try (BufferedReader processOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				for (String line = processOut.readLine(); line != null; line = processOut.readLine()) {
					if (codeSizePattern != null) {
						Matcher matcher = codeSizePattern.matcher(line);
						if (matcher.matches()) {
							codeSize += Integer.parseInt(matcher.group(1));
						}
					}
					if (dataSizePattern != null) {
						Matcher matcher = dataSizePattern.matcher(line);
						if (matcher.matches()) {
							dataSize += Integer.parseInt(matcher.group(1));
						}
					}
				}
			}

			ConsoleOutputStream consoleOut = console.getOutputStream();
			consoleOut.write("Program store usage: " + codeSize);
			if (maxCodeSize > 0) {
				consoleOut.write(" of maximum " + maxCodeSize);
			}
			consoleOut.write(" bytes\n");

			if (maxDataSize >= 0) {
				consoleOut.write("Initial RAM usage: " + dataSize);
				if (maxCodeSize > 0) {
					consoleOut.write(" of maximum " + maxDataSize);
				}
				consoleOut.write(" bytes\n");
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Checking sizes", e));
		}
	}

	@Override
	public Object findTemplateSource(String name) throws IOException {
		return FileLocator.find(Activator.getPlugin().getBundle(), new org.eclipse.core.runtime.Path(name), null);
	}

	@Override
	public long getLastModified(Object source) {
		try {
			URL url = (URL) source;
			if (url.getProtocol().equals("file")) { //$NON-NLS-1$
				File file = new File(url.toURI());
				return file.lastModified();
			} else {
				return 0;
			}
		} catch (URISyntaxException e) {
			return 0;
		}
	}

	@Override
	public Reader getReader(Object source, String encoding) throws IOException {
		URL url = (URL) source;
		return new InputStreamReader(url.openStream(), encoding);
	}

	@Override
	public void closeTemplateSource(Object arg0) throws IOException {
		// Nothing
	}

}
