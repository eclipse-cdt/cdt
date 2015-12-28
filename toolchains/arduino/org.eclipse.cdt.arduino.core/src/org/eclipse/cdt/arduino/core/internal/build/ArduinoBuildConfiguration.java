package org.eclipse.cdt.arduino.core.internal.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.ArduinoTemplateGenerator;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.board.ToolDependency;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoConsoleParser;
import org.eclipse.cdt.arduino.core.internal.console.ArduinoErrorParser;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class ArduinoBuildConfiguration {

	private static final String PACKAGE_NAME = "packageName"; //$NON-NLS-1$
	private static final String PLATFORM_NAME = "platformName"; //$NON-NLS-1$
	private static final String BOARD_NAME = "boardName"; //$NON-NLS-1$

	private final IBuildConfiguration config;

	private static ArduinoManager manager = Activator.getService(ArduinoManager.class);

	private ArduinoBoard board;
	private Properties properties;

	// Cache for scanner info
	private IScannerInfo cScannerInfo;
	private IScannerInfo cppScannerInfo;

	private final static boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);

	private ArduinoBuildConfiguration(IBuildConfiguration config) {
		this.config = config;
	}

	private static Map<IBuildConfiguration, ArduinoBuildConfiguration> cache = new HashMap<>();

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(ArduinoBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
				IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
				ArduinoBuildConfiguration arduinoConfig = cache.get(config);
				if (arduinoConfig == null) {
					arduinoConfig = new ArduinoBuildConfiguration(config);
					cache.put(config, arduinoConfig);
				}
				return (T) arduinoConfig;
			}
			return null;
		}

		@Override
		public Class<?>[] getAdapterList() {
			return new Class<?>[] { ArduinoBuildConfiguration.class };
		}
	}

	public static ArduinoBuildConfiguration getConfig(IProject project, ArduinoRemoteConnection target,
			IProgressMonitor monitor) throws CoreException {
		ArduinoBoard board = target.getBoard();

		// return it if it exists already
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			if (!config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
				ArduinoBuildConfiguration arduinoConfig = config.getAdapter(ArduinoBuildConfiguration.class);
				if (arduinoConfig.matches(target)) {
					return arduinoConfig;
				}
			}
		}

		// Not found, need to create one
		Set<String> configNames = new HashSet<>();
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			configNames.add(config.getName());
		}
		String newName = board.getId();
		int n = 0;
		while (configNames.contains(newName)) {
			newName = board.getId() + (++n);
		}
		configNames.add(newName);
		IProjectDescription projectDesc = project.getDescription();
		projectDesc.setBuildConfigs(configNames.toArray(new String[configNames.size()]));
		project.setDescription(projectDesc, monitor);

		// set it up for the board
		IBuildConfiguration config = project.getBuildConfig(newName);
		ArduinoBuildConfiguration arduinoConfig = config.getAdapter(ArduinoBuildConfiguration.class);
		arduinoConfig.setBoard(target);

		return arduinoConfig;
	}

	public void setActive(IProgressMonitor monitor) throws CoreException {
		IProject project = config.getProject();
		if (config.equals(project.getActiveBuildConfig())) {
			// already set
			return;
		}

		IProjectDescription projectDesc = project.getDescription();
		projectDesc.setActiveBuildConfig(config.getName());
		project.setDescription(projectDesc, monitor);

		// Reindex - assuming for now each config has different compiler
		// settings
		CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));
	}

	public IEclipsePreferences getSettings() {
		return (IEclipsePreferences) new ProjectScope(config.getProject()).getNode(Activator.getId()).node("config") //$NON-NLS-1$
				.node(config.getName());
	}

	public void setBoard(ArduinoBoard board) throws CoreException {
		this.board = board;

		ArduinoPlatform platform = board.getPlatform();
		ArduinoPackage pkg = platform.getPackage();

		IEclipsePreferences settings = getSettings();
		settings.put(PACKAGE_NAME, pkg.getName());
		settings.put(PLATFORM_NAME, platform.getName());
		settings.put(BOARD_NAME, board.getName());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Saving preferences", e)); //$NON-NLS-1$
		}
	}

	public void setBoard(ArduinoRemoteConnection target) throws CoreException {
		this.board = target.getBoard();

		ArduinoPlatform platform = board.getPlatform();
		ArduinoPackage pkg = platform.getPackage();

		IEclipsePreferences settings = getSettings();
		settings.put(PACKAGE_NAME, pkg.getName());
		settings.put(PLATFORM_NAME, platform.getName());
		settings.put(BOARD_NAME, board.getName());

		HierarchicalProperties menus = board.getMenus();
		if (menus != null) {
			for (String id : menus.getChildren().keySet()) {
				String key = ArduinoBoard.MENU_QUALIFIER + id;
				String value = target.getRemoteConnection().getAttribute(key);
				if (value != null) {
					settings.put(key, value);
				}
			}
		}

		try {
			settings.flush();
		} catch (BackingStoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Saving preferences", e)); //$NON-NLS-1$
		}
	}

	public boolean matches(ArduinoRemoteConnection target) throws CoreException {
		ArduinoBoard otherBoard = target.getBoard();
		if (!getBoard().equals(otherBoard)) {
			return false;
		}

		IEclipsePreferences settings = getSettings();
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
		if (board == null) {
			IEclipsePreferences settings = getSettings();
			String packageName = settings.get(PACKAGE_NAME, ""); //$NON-NLS-1$
			String platformName = settings.get(PLATFORM_NAME, ""); //$NON-NLS-1$
			String boardName = settings.get(BOARD_NAME, ""); //$NON-NLS-1$
			board = manager.getBoard(boardName, platformName, packageName);

			if (board == null) {
				// Default to Uno or first one we find
				board = manager.getBoard("Arduino/Genuino Uno", "Arduino AVR Boards", "arduino"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (board == null) {
					List<ArduinoBoard> boards = manager.getInstalledBoards();
					if (!boards.isEmpty()) {
						board = boards.get(0);
					}
				}
			}
		}
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
			String configName = config.getName();
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
			IEclipsePreferences settings = getSettings();
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
		properties.put("build.project_name", config.getProject().getName()); //$NON-NLS-1$
		return properties;
	}

	public IFolder getBuildFolder() throws CoreException {
		IProject project = config.getProject();
		return project.getFolder("build"); //$NON-NLS-1$
	}

	public File getBuildDirectory() throws CoreException {
		return new File(getBuildFolder().getLocationURI());
	}

	public IFile getMakeFile() throws CoreException {
		IFolder buildFolder = getBuildFolder();
		ArduinoBoard board = getBoard();
		String makeFileName = board.getId() + ".mk"; //$NON-NLS-1$
		return buildFolder.getFile(makeFileName);
	}

	public IFile generateMakeFile(IProgressMonitor monitor) throws CoreException {
		final IProject project = config.getProject();

		IFolder buildFolder = getBuildFolder();
		if (!buildFolder.exists()) {
			buildFolder.create(true, true, monitor);
			buildFolder.setDerived(true, monitor);
			ICProject cproject = CoreModel.getDefault().create(project);
			IOutputEntry output = CoreModel.newOutputEntry(buildFolder.getFullPath());
			IPathEntry[] oldEntries = cproject.getRawPathEntries();
			IPathEntry[] newEntries = new IPathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = output;
			cproject.setRawPathEntries(newEntries, monitor);
		}

		ArduinoBoard board = getBoard();
		ArduinoPlatform platform = board.getPlatform();

		IFile makeFile = getMakeFile();

		// The board id
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

		ArduinoTemplateGenerator templateGen = new ArduinoTemplateGenerator();
		templateGen.generateFile(buildModel, "board.mk", makeFile, monitor); //$NON-NLS-1$
		return makeFile;
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

	private String resolveProperty(String property, Properties dict) {
		String res = dict.getProperty(property);
		if (res == null) {
			return null;
		}

		String last;
		do {
			last = res;
			for (int i = res.indexOf('{'); i >= 0; i = res.indexOf('{', i)) {
				i++;
				int n = res.indexOf('}', i);
				if (n >= 0) {
					String p2 = res.substring(i, n);
					String r2 = dict.getProperty(p2);
					if (r2 != null) {
						res = res.replace('{' + p2 + '}', r2);
					}
				}
				i = n;
			}
		} while (!res.equals(last));

		return res;
	}

	public void setEnvironment(Map<String, String> env) throws CoreException {
		// Everything is specified with full path, do not need to add anything
		// to the environment.
	}

	public String getMakeCommand() {
		if (isWindows) {
			Path makePath = ArduinoPreferences.getArduinoHome().resolve("tools/make/make.exe"); //$NON-NLS-1$
			if (!Files.exists(makePath)) {
				makePath = ArduinoPreferences.getArduinoHome().resolve("make.exe"); //$NON-NLS-1$
			}
			return makePath.toString();
		} else {
			return "make"; //$NON-NLS-1$
		}
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
		return (String) getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex"); //$NON-NLS-1$
	}

	public int getMaxCodeSize() throws CoreException {
		String sizeStr = (String) getProperties().getProperty("upload.maximum_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String getDataSizeRegex() throws CoreException {
		return (String) getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex.data"); //$NON-NLS-1$
	}

	public int getMaxDataSize() throws CoreException {
		String sizeStr = (String) getProperties().getProperty("upload.maximum_data_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String[] getUploadCommand(String serialPort) throws CoreException {
		String toolName = getProperties().getProperty("upload.tool"); //$NON-NLS-1$

		Properties properties = getProperties();

		properties.put("serial.port", serialPort); //$NON-NLS-1$
		if (serialPort.startsWith("/dev/")) {
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

	public IScannerInfo getScannerInfo(IResource resource) throws CoreException {
		IContentType contentType = CCorePlugin.getContentType(resource.getProject(), resource.getName());
		if (contentType != null) {
			// what language is this resource and pick the right path;
			switch (contentType.getId()) {
			case CCorePlugin.CONTENT_TYPE_CXXSOURCE:
			case CCorePlugin.CONTENT_TYPE_CXXHEADER:
				if (cppScannerInfo == null) {
					cppScannerInfo = calculateScannerInfo("recipe.cpp.o.pattern", resource); //$NON-NLS-1$
				}
				return cppScannerInfo;
			default:
				if (cScannerInfo == null) {
					cScannerInfo = calculateScannerInfo("recipe.c.o.pattern", resource); //$NON-NLS-1$
				}
				return cScannerInfo;
			}
		}
		// use the cpp scanner info if all else fails
		return cppScannerInfo;
	}

	public void clearScannerInfoCache() {
		cppScannerInfo = null;
		cScannerInfo = null;
	}

	public static String pathString(Path path) {
		String str = path.toString();
		if (isWindows) {
			str = str.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return str;
	}

	private IScannerInfo calculateScannerInfo(String recipe, IResource resource) throws CoreException {
		try {
			ArduinoPlatform platform = getBoard().getPlatform();
			Properties properties = new Properties();
			properties.putAll(getProperties());

			Path tmpFile = Files.createTempFile("cdt", ".cpp"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("source_file", pathString(tmpFile)); //$NON-NLS-1$
			properties.put("object_file", "-"); //$NON-NLS-1$ //$NON-NLS-2$

			String includes = "-E -P -v -dD"; //$NON-NLS-1$
			for (Path include : platform.getIncludePath()) {
				includes += " -I\"" + pathString(include) + '"'; //$NON-NLS-1$
			}
			Collection<ArduinoLibrary> libs = manager.getLibraries(config.getProject());
			for (ArduinoLibrary lib : libs) {
				for (Path path : lib.getIncludePath()) {
					includes += " -I\"" + pathString(path) + '"'; //$NON-NLS-1$
				}
			}
			properties.put("includes", includes); //$NON-NLS-1$

			String[] command;
			if (isWindows) {
				command = splitCommand(resolveProperty(recipe, properties));
			} else {
				command = new String[] { "sh", "-c", resolveProperty(recipe, properties) }; //$NON-NLS-1$ //$NON-NLS-2$
			}
			ProcessBuilder processBuilder = new ProcessBuilder(command).directory(tmpFile.getParent().toFile())
					.redirectErrorStream(true);
			setEnvironment(processBuilder.environment());
			Process process = processBuilder.start();

			Map<String, String> symbols = new HashMap<>();
			List<String> includePath = new ArrayList<>();
			Pattern definePattern = Pattern.compile("#define (.*)\\s(.*)"); //$NON-NLS-1$
			boolean inIncludePaths = false;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					if (inIncludePaths) {
						if (line.equals("End of search list.")) { //$NON-NLS-1$
							inIncludePaths = false;
						} else {
							includePath.add(line.trim());
						}
					} else if (line.startsWith("#define ")) { //$NON-NLS-1$
						Matcher matcher = definePattern.matcher(line);
						if (matcher.matches()) {
							symbols.put(matcher.group(1), matcher.group(2));
						}
					} else if (line.equals("#include <...> search starts here:")) { //$NON-NLS-1$
						inIncludePaths = true;
					}
				}
			}
			Files.delete(tmpFile);
			ExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(symbols,
					includePath.toArray(new String[includePath.size()]));
			return scannerInfo;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Compiler built-ins", e)); //$NON-NLS-1$
		}
	}

	private String[] splitCommand(String command) {
		// TODO deal with quotes properly, for now just strip
		return command.replaceAll("\"", "").split("\\s+"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public ArduinoConsoleParser[] getBuildConsoleParsers() {
		// ../src/Test.cpp:4:1: error: 'x' was not declared in this scope

		return new ArduinoConsoleParser[] { new ArduinoErrorParser("(.*?):(\\d+):(\\d+:)? (fatal )?error: (.*)") { //$NON-NLS-1$
			@Override
			protected int getSeverity(Matcher matcher) {
				return IMarker.SEVERITY_ERROR;
			}

			@Override
			protected String getMessage(Matcher matcher) {
				return matcher.group(matcher.groupCount());
			}

			@Override
			protected int getLineNumber(Matcher matcher) {
				return Integer.parseInt(matcher.group(2));
			}

			@Override
			protected String getFileName(Matcher matcher) {
				return matcher.group(1);
			}

			@Override
			protected int getLinkOffset(Matcher matcher) {
				return 0;
			}

			@Override
			protected int getLinkLength(Matcher matcher) {
				return matcher.group(1).length() + 1 + matcher.group(2).length() + 1 + matcher.group(3).length();
			}
		} };
	}

}
