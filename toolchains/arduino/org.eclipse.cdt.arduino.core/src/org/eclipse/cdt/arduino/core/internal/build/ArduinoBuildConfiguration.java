/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.cdt.arduino.core.internal.board.ArduinoTool;
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
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ArduinoBuildConfiguration extends CBuildConfiguration
		implements TemplateLoader, IRemoteConnectionChangeListener {

	private static final ArduinoManager manager = Activator.getService(ArduinoManager.class);
	private static final boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);

	private final ArduinoRemoteConnection target;
	private final String launchMode;
	private ArduinoBoard defaultBoard;
	private Properties boardProperties;

	// for Makefile generation
	private Configuration templateConfig;

	/**
	 * Default configuration.
	 *
	 * @param config
	 */
	ArduinoBuildConfiguration(IBuildConfiguration config, String name, String launchMode, ArduinoBoard defaultBoard,
			IToolChain toolChain) throws CoreException {
		super(config, ".default", toolChain); //$NON-NLS-1$
		this.target = null;
		this.launchMode = launchMode;
		this.defaultBoard = defaultBoard;
	}

	ArduinoBuildConfiguration(IBuildConfiguration config, String name, String launchMode,
			ArduinoRemoteConnection target, IToolChain toolChain) throws CoreException {
		super(config, name, toolChain);
		this.target = target;
		this.launchMode = launchMode;
		IRemoteServicesManager remoteManager = Activator.getService(IRemoteServicesManager.class);
		remoteManager.addRemoteConnectionChangeListener(this);
	}

	@Override
	public synchronized void connectionChanged(RemoteConnectionChangeEvent event) {
		if (event.getConnection().equals(target.getRemoteConnection())) {
			boardProperties = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(ArduinoBuildConfiguration.class)) {
			return (T) this;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getLaunchMode() {
		return launchMode;
	}

	public ArduinoRemoteConnection getTarget() {
		return target;
	}

	public ArduinoBoard getBoard() throws CoreException {
		if (target != null) {
			return target.getBoard();
		} else {
			return defaultBoard;
		}
	}

	private synchronized Properties getBoardProperties() throws CoreException {
		if (boardProperties == null) {
			ArduinoBoard board = getBoard();
			ArduinoPlatform platform = board.getPlatform();

			// IDE generated properties
			boardProperties = new Properties();
			boardProperties.put("runtime.platform.path", platform.getInstallPath().toString()); //$NON-NLS-1$
			boardProperties.put("runtime.ide.version", "10608"); //$NON-NLS-1$ //$NON-NLS-2$
			boardProperties.put("software", "ARDUINO"); //$NON-NLS-1$ //$NON-NLS-2$
			boardProperties.put("build.arch", platform.getArchitecture().toUpperCase()); //$NON-NLS-1$
			boardProperties.put("build.path", "."); //$NON-NLS-1$ //$NON-NLS-2$
			boardProperties.put("build.core.path", //$NON-NLS-1$
					platform.getInstallPath().resolve("cores").resolve("{build.core}").toString()); //$NON-NLS-1$ //$NON-NLS-2$
			boardProperties.put("build.system.path", platform.getInstallPath().resolve("system").toString()); //$NON-NLS-1$ //$NON-NLS-2$
			boardProperties.put("build.variant.path", //$NON-NLS-1$
					platform.getInstallPath().resolve("variants").resolve("{build.variant}").toString()); //$NON-NLS-1$ //$NON-NLS-2$

			// Everyone seems to want to use arduino package tools
			ArduinoPackage arduinoPackage = manager.getPackage("arduino"); //$NON-NLS-1$
			if (arduinoPackage != null) {
				for (ArduinoTool tool : arduinoPackage.getLatestTools()) {
					boardProperties.put("runtime.tools." + tool.getName() + ".path", tool.getInstallPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				for (ArduinoTool tool : arduinoPackage.getTools()) {
					boardProperties.put("runtime.tools." + tool.getName() + '-' + tool.getVersion() + ".path", //$NON-NLS-1$ //$NON-NLS-2$
							tool.getInstallPath().toString());
				}
			}

			// Super Platform
			String core = board.getBoardProperties().getProperty("build.core"); //$NON-NLS-1$
			if (core.contains(":")) { //$NON-NLS-1$
				String[] segments = core.split(":"); //$NON-NLS-1$
				if (segments.length == 2) {
					ArduinoPlatform superPlatform = manager.getInstalledPlatform(segments[0],
							platform.getArchitecture());
					if (superPlatform != null) {
						boardProperties.putAll(superPlatform.getPlatformProperties().flatten());
					}
				}
			}

			// Platform
			boardProperties.putAll(platform.getPlatformProperties().flatten());

			// Tools
			for (ToolDependency toolDep : platform.getToolsDependencies()) {
				boardProperties.putAll(toolDep.getTool().getToolProperties());
			}

			// Board
			boardProperties.putAll(board.getBoardProperties());

			// Menus
			HierarchicalProperties menus = board.getMenus();
			if (menus != null && target != null) {
				for (Entry<String, HierarchicalProperties> menuEntry : menus.getChildren().entrySet()) {
					String key = menuEntry.getKey();
					String value = target.getMenuValue(key);
					if (value == null || value.isEmpty()) {
						Iterator<HierarchicalProperties> i = menuEntry.getValue().getChildren().values().iterator();
						if (i.hasNext()) {
							HierarchicalProperties first = i.next();
							value = first.getValue();
						}
					}
					if (value != null && !value.isEmpty()) {
						boardProperties.putAll(board.getMenuProperties(key, value));
					}
				}
			}
		}

		// always do this in case the project changes names
		boardProperties.put("build.project_name", getProject().getName()); //$NON-NLS-1$
		return boardProperties;
	}

	public Map<String, Object> getBuildModel() throws CoreException {
		IProject project = getProject();
		ArduinoBoard board = getBoard();
		ArduinoPlatform platform = board.getPlatform();

		Properties properties = new Properties();
		Map<String, Object> buildModel = new HashMap<>();
		buildModel.put("boardId", board.getId()); //$NON-NLS-1$
		properties.put("object_file", "$@"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("source_file", "$<"); //$NON-NLS-1$ //$NON-NLS-2$

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
		properties.putAll(getBoardProperties());
		buildModel.put("build_path", properties.get("build.path")); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("project_name", project.getName()); //$NON-NLS-1$

		String includes = null;
		for (Path include : getIncludePath(platform, properties)) {
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

		// Magic recipes for platform builds with platform includes
		properties.put("includes", includes); //$NON-NLS-1$
		buildModel.put("recipe_cpp_o_pattern_plat", resolveProperty("recipe.cpp.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_c_o_pattern_plat", resolveProperty("recipe.c.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_S_o_pattern_plat", resolveProperty("recipe.S.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$

		ArduinoPlatform corePlatform = platform;
		String core = properties.getProperty("build.core"); //$NON-NLS-1$
		if (core.contains(":")) { //$NON-NLS-1$
			String[] segments = core.split(":"); //$NON-NLS-1$
			if (segments.length == 2) {
				corePlatform = manager.getInstalledPlatform(segments[0], platform.getArchitecture());
				core = segments[1];
			}
		}
		buildModel.put("platform_path", pathString(corePlatform.getInstallPath())); //$NON-NLS-1$
		Path corePath = corePlatform.getInstallPath().resolve("cores").resolve(core); //$NON-NLS-1$
		buildModel.put("platform_core_path", pathString(corePath)); //$NON-NLS-1$
		List<String> coreSources = new ArrayList<>();
		getSources(coreSources, corePath, true);
		buildModel.put("platform_core_srcs", coreSources); //$NON-NLS-1$

		List<String> variantSources = new ArrayList<>();
		String variant = properties.getProperty("build.variant"); //$NON-NLS-1$
		if (variant != null) {
			ArduinoPlatform variantPlatform = platform;
			if (variant.contains(":")) { //$NON-NLS-1$
				String[] segments = variant.split(":"); //$NON-NLS-1$
				if (segments.length == 2) {
					variantPlatform = manager.getInstalledPlatform(segments[0], platform.getArchitecture());
					variant = segments[1];
				}
			}
			Path variantPath = variantPlatform.getInstallPath().resolve("variants").resolve(variant); //$NON-NLS-1$
			buildModel.put("platform_variant_path", pathString(variantPath)); //$NON-NLS-1$
			getSources(variantSources, variantPath, true);
		}
		buildModel.put("platform_variant_srcs", variantSources); //$NON-NLS-1$

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

	private static void getSources(Collection<String> sources, Path dir, boolean recurse) {
		for (File file : dir.toFile().listFiles()) {
			if (file.isDirectory()) {
				if (recurse) {
					getSources(sources, file.toPath(), recurse);
				}
			} else {
				if (ArduinoBuildConfiguration.isSource(file.getName())) {
					sources.add(ArduinoBuildConfiguration.pathString(file.toPath()));
				}
			}
		}
	}

	public IFile generateMakeFile(IProgressMonitor monitor) throws CoreException {
		IFolder buildFolder = (IFolder) getBuildContainer();
		if (!buildFolder.exists()) {
			buildFolder.create(true, true, monitor);
		}

		IFile makefile = buildFolder.getFile("Makefile"); //$NON-NLS-1$

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

	private String resolvePropertyValue(String value, Properties dict) throws CoreException {
		String last;
		do {
			last = value;
			for (int i = value.indexOf('{'); i >= 0; i = value.indexOf('{', i)) {
				i++;
				if (value.charAt(i) == '{') {
					i++;
					continue;
				}

				int n = value.indexOf('}', i);
				if (n >= 0) {
					String p2 = value.substring(i, n);
					String r2 = dict.getProperty(p2);
					if (r2 != null) {
						value = value.replace('{' + p2 + '}', r2);
					} else {
						throw Activator.coreException(String.format("Undefined key %s", p2), null);
					}
				}
				i = n;
			}
		} while (!value.equals(last));

		return value.replace("}}", "}").replace("{{", "{"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	private String resolveProperty(String property, Properties dict) throws CoreException {
		String value = dict.getProperty(property);
		return value != null ? resolvePropertyValue(value, dict) : null;
	}

	public String getMakeCommand() {
		return isWindows ? ArduinoPreferences.getArduinoHome().resolve("make").toString() : "make"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String[] getBuildCommand() throws CoreException {
		return new String[] { getMakeCommand() };
	}

	public String[] getCleanCommand() throws CoreException {
		return new String[] { getMakeCommand(), "clean" }; //$NON-NLS-1$
	}

	public String[] getSizeCommand() throws CoreException {
		// TODO this shouldn't be in the makefile
		// should be like the upload command
		return new String[] { getMakeCommand(), "size" }; //$NON-NLS-1$
	}

	public String getCodeSizeRegex() throws CoreException {
		return getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex"); //$NON-NLS-1$
	}

	public int getMaxCodeSize() throws CoreException {
		String sizeStr = getBoardProperties().getProperty("upload.maximum_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String getDataSizeRegex() throws CoreException {
		return getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex.data"); //$NON-NLS-1$
	}

	public int getMaxDataSize() throws CoreException {
		String sizeStr = getBoardProperties().getProperty("upload.maximum_data_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String[] getUploadCommand(String serialPort) throws CoreException {
		Properties properties = new Properties();
		properties.putAll(getBoardProperties());

		String toolName = properties.getProperty("upload.tool"); //$NON-NLS-1$
		ArduinoPlatform platform = getBoard().getPlatform();
		if (toolName.contains(":")) { //$NON-NLS-1$
			String[] segments = toolName.split(":"); //$NON-NLS-1$
			if (segments.length == 2) {
				platform = manager.getInstalledPlatform(segments[0], platform.getArchitecture());
				toolName = segments[1];
			}
		}

		ArduinoTool uploadTool = platform.getTool(toolName);
		if (uploadTool != null) {
			properties.putAll(uploadTool.getToolProperties());
		}

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
		HierarchicalProperties toolsProps = platform.getPlatformProperties().getChild("tools"); //$NON-NLS-1$
		if (toolsProps != null) {
			HierarchicalProperties toolProps = toolsProps.getChild(toolName);
			if (toolProps != null) {
				properties.putAll(toolProps.flatten());
			}
		}

		String command;
		if (properties.get("upload.protocol") != null) { //$NON-NLS-1$
			// TODO make this a preference
			properties.put("upload.verbose", properties.getProperty("upload.params.verbose", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			properties.put("upload.verify", properties.getProperty("upload.params.verify", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			command = resolveProperty("upload.pattern", properties); //$NON-NLS-1$
		} else {
			// use the bootloader
			String programmer = target.getProgrammer();
			if (programmer != null) {
				HierarchicalProperties programmers = getBoard().getPlatform().getProgrammers();
				if (programmers != null) {
					HierarchicalProperties programmerProps = programmers.getChild(programmer);
					if (programmerProps != null) {
						properties.putAll(programmerProps.flatten());
					}
				}
			}

			// TODO preference
			properties.put("program.verbose", properties.getProperty("program.params.verbose", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			properties.put("program.verify", properties.getProperty("program.params.verify", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			command = resolveProperty("program.pattern", properties); //$NON-NLS-1$
		}

		if (command == null) {
			throw Activator.coreException("Upload command not specified", null);
		}
		if (isWindows) {
			List<String> args = splitCommand(command);
			return args.toArray(new String[args.size()]);
		} else {
			return new String[] { "sh", "-c", command }; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private Collection<Path> getIncludePath(ArduinoPlatform platform, Properties properties) throws CoreException {
		ArduinoPlatform corePlatform = platform;
		String core = properties.getProperty("build.core"); //$NON-NLS-1$
		if (core.contains(":")) { //$NON-NLS-1$
			String[] segments = core.split(":"); //$NON-NLS-1$
			if (segments.length == 2) {
				corePlatform = manager.getInstalledPlatform(segments[0], platform.getArchitecture());
				core = segments[1];
			}
		}

		ArduinoPlatform variantPlatform = platform;
		String variant = properties.getProperty("build.variant"); //$NON-NLS-1$
		if (variant != null) {
			if (variant.contains(":")) { //$NON-NLS-1$
				String[] segments = variant.split(":"); //$NON-NLS-1$
				if (segments.length == 2) {
					variantPlatform = manager.getInstalledPlatform(segments[0], platform.getArchitecture());
					variant = segments[1];
				}
			}
		} else {
			return Arrays.asList(corePlatform.getInstallPath().resolve("cores").resolve(core)); //$NON-NLS-1$
		}

		return Arrays.asList(corePlatform.getInstallPath().resolve("cores").resolve(core), //$NON-NLS-1$
				variantPlatform.getInstallPath().resolve("variants").resolve(variant)); //$NON-NLS-1$
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
			properties.putAll(getBoardProperties());

			// Overrides for scanner discovery
			properties.put("source_file", ""); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("object_file", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			// the base scanner info does includes
			properties.put("includes", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String commandString = resolveProperty(recipe, properties);

			List<Path> includePath = new ArrayList<>();
			includePath.addAll(getIncludePath(platform, properties));
			Collection<ArduinoLibrary> libs = manager.getLibraries(getProject());
			for (ArduinoLibrary lib : libs) {
				includePath.addAll(lib.getIncludePath());
			}
			String[] includes = null;
			try {
				includes = includePath.stream().map(path -> {
					try {
						return resolvePropertyValue(path.toString(), properties);
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList()).toArray(new String[includePath.size()]);
			} catch (RuntimeException e) {
				if (e.getCause() != null && e.getCause() instanceof CoreException) {
					throw (CoreException) e.getCause();
				} else {
					throw e;
				}
			}

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
			List<String> command = splitCommand(commandString);
			IScannerInfo info = getToolChain().getScannerInfo(getBuildConfiguration(), command, baseInfo, resource,
					getBuildDirectoryURI());

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

	private List<String> splitCommand(String command) {
		boolean inQuotes = false;
		boolean inDouble = false;

		List<String> args = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			switch (c) {
			case ' ':
				if (inQuotes || inDouble) {
					builder.append(c);
				} else if (builder.length() > 0) {
					args.add(builder.toString());
					builder = new StringBuilder();
				}
				break;
			case '\'':
				if (inDouble) {
					builder.append(c);
				} else {
					inQuotes = !inQuotes;
				}
				break;
			case '"':
				if (inQuotes) {
					builder.append(c);
				} else {
					inDouble = !inDouble;
				}
				break;
			default:
				builder.append(c);
			}
		}

		if (builder.length() > 0) {
			args.add(builder.toString());
		}

		return args;
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
				epm.setOutputStream(console.getOutputStream());
				ProcessBuilder processBuilder = new ProcessBuilder().command(getBuildCommand())
						.directory(getBuildDirectory().toFile());
				setBuildEnvironment(processBuilder.environment());
				Process process = processBuilder.start();
				if (watchProcess(process, new IConsoleParser[] { epm }) == 0) {
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

			watchProcess(process, console);

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
