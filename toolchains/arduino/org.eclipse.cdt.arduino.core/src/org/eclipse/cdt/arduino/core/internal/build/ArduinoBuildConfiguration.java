package org.eclipse.cdt.arduino.core.internal.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoardManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoTool;
import org.eclipse.cdt.arduino.core.internal.board.ToolDependency;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class ArduinoBuildConfiguration {

	private static final String PACKAGE_NAME = "packageName"; //$NON-NLS-1$
	private static final String PLATFORM_NAME = "platformName"; //$NON-NLS-1$
	private static final String BOARD_NAME = "boardName"; //$NON-NLS-1$

	private final IBuildConfiguration config;

	private ArduinoBoard board;
	private Properties properties;

	// Cache for scanner info
	private IScannerInfo cScannerInfo;
	private IScannerInfo cppScannerInfo;

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

	public static ArduinoBuildConfiguration getConfig(IProject project, ArduinoBoard board, IProgressMonitor monitor)
			throws CoreException {
		// return it if it exists already
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			ArduinoBuildConfiguration arduinoConfig = config.getAdapter(ArduinoBuildConfiguration.class);
			if (board.equals(arduinoConfig.getBoard())) {
				return arduinoConfig;
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
		arduinoConfig.setBoard(board);

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
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Saving preferences", e));
		}
	}

	public ArduinoBoard getBoard() throws CoreException {
		if (board == null) {
			IEclipsePreferences settings = getSettings();
			String packageName = settings.get(PACKAGE_NAME, ""); //$NON-NLS-1$
			String platformName = settings.get(PLATFORM_NAME, ""); //$NON-NLS-1$
			String boardName = settings.get(BOARD_NAME, ""); //$NON-NLS-1$
			board = ArduinoBoardManager.instance.getBoard(boardName, platformName, packageName);
		}
		return board;
	}

	private Properties getProperties() throws CoreException {
		if (properties == null) {
			ArduinoBoard board = getBoard();
			ArduinoPlatform platform = board.getPlatform();
			properties = board.getBoardProperties();
			properties.putAll(board.getPlatform().getPlatformProperties());
			for (ToolDependency toolDep : platform.getToolsDependencies()) {
				properties.putAll(toolDep.getTool().getToolProperties());
			}
			properties.put("runtime.ide.version", "1.6.7"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("build.arch", platform.getArchitecture().toUpperCase()); //$NON-NLS-1$
			properties.put("build.path", config.getName()); //$NON-NLS-1$
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
						if (CoreModel.isValidSourceUnitName(project, proxy.getName())) {
							Path sourcePath = new File(proxy.requestResource().getLocationURI()).toPath();
							sourceFiles.add(projectPath.relativize(sourcePath).toString());
						}
					}
					return true;
				}
			}, 0);
		}
		buildModel.put("project_srcs", sourceFiles); //$NON-NLS-1$

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
			includes += '"' + include.toString() + '"';
		}
		properties.put("includes", includes); //$NON-NLS-1$

		Path platformPath = platform.getInstallPath();
		buildModel.put("platform_path", platformPath.toString()); //$NON-NLS-1$

		Path corePath = platformPath.resolve("cores").resolve((String) properties.get("build.core")); //$NON-NLS-1$ //$NON-NLS-2$
		File[] platformFiles = corePath.toFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cpp") || name.endsWith(".c"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		String[] platformSource = new String[platformFiles.length];
		for (int i = 0; i < platformSource.length; ++i)

		{
			platformSource[i] = platformFiles[i].getAbsolutePath();
		}
		buildModel.put("platform_srcs", platformSource); //$NON-NLS-1$

		properties.put("object_file", "$@"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("source_file", "$<"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("archive_file", "libc.a"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("object_files", "$(PROJECT_OBJS)"); //$NON-NLS-1$ //$NON-NLS-2$

		buildModel.put("recipe_cpp_o_pattern", resolveProperty("recipe.cpp.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_c_o_pattern", resolveProperty("recipe.c.o.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_ar_pattern", resolveProperty("recipe.ar.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_c_combine_pattern", resolveProperty("recipe.c.combine.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_objcopy_eep_pattern", resolveProperty("recipe.objcopy.eep.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_objcopy_hex_pattern", resolveProperty("recipe.objcopy.hex.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$
		buildModel.put("recipe_size_pattern", resolveProperty("recipe.size.pattern", properties)); //$NON-NLS-1$ //$NON-NLS-2$

		ArduinoTemplateGenerator templateGen = new ArduinoTemplateGenerator();
		templateGen.generateFile(buildModel, "board.mk", makeFile, monitor); //$NON-NLS-1$
		return makeFile;
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
		// Arduino home to find platforms and libraries
		env.put("ARDUINO_HOME", ArduinoPreferences.getArduinoHome().toString()); //$NON-NLS-1$

		// Add tools to the path
		String pathKey = null;
		String path = null;
		for (Map.Entry<String, String> entry : env.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("PATH")) { //$NON-NLS-1$
				pathKey = entry.getKey();
				path = entry.getValue();
				break;
			}
		}

		List<String> toolPaths = new ArrayList<>();
		ArduinoBoard board = getBoard();
		ArduinoPlatform platform = board.getPlatform();
		for (ToolDependency dep : platform.getToolsDependencies()) {
			ArduinoTool tool = dep.getTool();
			Path installPath = tool.getInstallPath();
			Path binPath = installPath.resolve("bin"); //$NON-NLS-1$
			if (binPath.toFile().exists()) {
				toolPaths.add(binPath.toString());
			} else {
				// use the install dir by default
				toolPaths.add(installPath.toString());
			}
		}
		for (String toolPath : toolPaths) {
			if (path != null) {
				path = toolPath + File.pathSeparatorChar + path;
			} else {
				path = toolPath;
			}
		}
		if (pathKey == null) {
			pathKey = "PATH"; //$NON-NLS-1$
		}
		env.put(pathKey, path);
	}

	public String[] getBuildCommand() throws CoreException {
		return new String[] { "make", "-f", getMakeFile().getName() }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String[] getCleanCommand() throws CoreException {
		return new String[] { "make", "-f", getMakeFile().getName(), "clean" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public String[] getSizeCommand() throws CoreException {
		// TODO this shouldn't be in the makefile
		// should be like the upload command
		return new String[] { "make", "-f", getMakeFile().getName(), "size" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public String getCodeSizeRegex() throws CoreException {
		return (String) getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex"); //$NON-NLS-1$
	}

	public int getMaxCodeSize() throws CoreException {
		String sizeStr = (String) getBoard().getBoardProperties().getProperty("upload.maximum_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String getDataSizeRegex() throws CoreException {
		return (String) getBoard().getPlatform().getPlatformProperties().getProperty("recipe.size.regex.data"); //$NON-NLS-1$
	}

	public int getMaxDataSize() throws CoreException {
		String sizeStr = (String) getBoard().getBoardProperties().getProperty("upload.maximum_data_size"); //$NON-NLS-1$
		return sizeStr != null ? Integer.parseInt(sizeStr) : -1;
	}

	public String[] getUploadCommand(String serialPort) throws CoreException {
		String toolName = getProperties().getProperty("upload.tool"); //$NON-NLS-1$
		ArduinoTool tool = board.getPlatform().getTool(toolName);

		Properties properties = getProperties();
		properties.put("runtime.tools." + toolName + ".path", tool.getInstallPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("serial.port", serialPort); //$NON-NLS-1$
		// to make up for some cheating in the platform.txt file
		properties.put("path", "{tools." + toolName + ".path}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.put("cmd.path", "{tools." + toolName + ".cmd.path}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		properties.put("config.path", "{tools." + toolName + ".config.path}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		properties.put("upload.verbose", "{tools." + toolName + ".upload.params.quiet}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		String command = resolveProperty("tools." + toolName + ".upload.pattern", properties); //$NON-NLS-1$ //$NON-NLS-2$
		// TODO Windows
		return new String[] { "sh", "-c", command }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public IScannerInfo getScannerInfo(IResource resource) throws CoreException {
		// what language is this resource and pick the right path;
		switch (CCorePlugin.getContentType(resource.getProject(), resource.getName()).getId()) {
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

	private IScannerInfo calculateScannerInfo(String recipe, IResource resource) throws CoreException {
		try {
			ArduinoPlatform platform = getBoard().getPlatform();
			Properties properties = new Properties();
			properties.putAll(getProperties());

			Path tmpFile = Files.createTempFile("cdt", ".cpp"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("source_file", tmpFile.toString()); //$NON-NLS-1$
			properties.put("object_file", "-"); //$NON-NLS-1$ //$NON-NLS-2$

			String includes = "-E -P -v -dD"; //$NON-NLS-1$
			for (Path include : platform.getIncludePath()) {
				includes += " -I\"" + include.toString() + '"'; //$NON-NLS-1$
			}
			properties.put("includes", includes); //$NON-NLS-1$

			// TODO Windows
			String[] command = new String[] { "sh", "-c", resolveProperty(recipe, properties) }; //$NON-NLS-1$ //$NON-NLS-2$
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
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Compiler built-ins", e));
		}
	}

}
