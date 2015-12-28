/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.build;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.ArduinoTemplateGenerator;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoTool;
import org.eclipse.cdt.arduino.core.internal.board.ToolDependency;
import org.eclipse.cdt.build.core.CBuildConfiguration;
import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ArduinoBuildConfiguration extends CBuildConfiguration {

	private static final String PACKAGE_NAME = "packageName"; //$NON-NLS-1$
	private static final String PLATFORM_NAME = "platformName"; //$NON-NLS-1$
	private static final String BOARD_NAME = "boardName"; //$NON-NLS-1$

	private ArduinoBoard board;
	private Properties properties;

	private final static boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);

	private ArduinoBuildConfiguration(IBuildConfiguration config) {
		super(config);
	}

	private static Map<IBuildConfiguration, ArduinoBuildConfiguration> cache = new HashMap<>();

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(ArduinoBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
				synchronized (cache) {
					IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
					ArduinoBuildConfiguration arduinoConfig = cache.get(config);
					if (arduinoConfig == null) {
						arduinoConfig = new ArduinoBuildConfiguration(config);
						cache.put(config, arduinoConfig);
					}
					return (T) arduinoConfig;
				}
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

	public void setBoard(ArduinoBoard board) throws CoreException {
		this.board = board;

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

	public ArduinoBoard getBoard() throws CoreException {
		if (board == null) {
			Preferences settings = getSettings();
			String packageName = settings.get(PACKAGE_NAME, ""); //$NON-NLS-1$
			String platformName = settings.get(PLATFORM_NAME, ""); //$NON-NLS-1$
			String boardName = settings.get(BOARD_NAME, ""); //$NON-NLS-1$
			board = ArduinoManager.instance.getBoard(boardName, platformName, packageName);
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
			properties.put("runtime.ide.version", "10607"); //$NON-NLS-1$ //$NON-NLS-2$
			properties.put("build.arch", platform.getArchitecture().toUpperCase()); //$NON-NLS-1$
			properties.put("build.path", getName()); //$NON-NLS-1$
		}
		// always do this in case the project changes names
		properties.put("build.project_name", getProject().getName()); //$NON-NLS-1$
		return properties;
	}

	public IFolder getBuildFolder() throws CoreException {
		IProject project = getProject();
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
		final IProject project = getProject();

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
		for (ArduinoLibrary lib : ArduinoManager.instance.getLibraries(project)) {
			for (Path path : lib.getSources(project)) {
				librarySources.add(pathString(path));
			}
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
		for (ArduinoLibrary lib : ArduinoManager.instance.getLibraries(project)) {
			for (Path include : lib.getIncludePath()) {
				includes += " -I\"" + pathString(include) + '"'; //$NON-NLS-1$
			}
		}
		properties.put("includes", includes); //$NON-NLS-1$

		Path platformPath = platform.getInstallPath();
		buildModel.put("platform_path", pathString(platformPath)); //$NON-NLS-1$

		Path corePath = platformPath.resolve("cores").resolve((String) properties.get("build.core")); //$NON-NLS-1$ //$NON-NLS-2$
		File[] platformFiles = corePath.toFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cpp") || name.endsWith(".c"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		String[] platformSource = new String[platformFiles.length];
		for (int i = 0; i < platformSource.length; ++i) {
			platformSource[i] = pathString(platformFiles[i].toPath());
		}
		buildModel.put("platform_srcs", platformSource); //$NON-NLS-1$

		properties.put("object_file", "$@"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("source_file", "$<"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("archive_file", "core.a"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("object_files", "$(PROJECT_OBJS) $(LIBRARIES_OBJS)"); //$NON-NLS-1$ //$NON-NLS-2$

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
		env.put("ARDUINO_HOME", pathString(ArduinoPreferences.getArduinoHome())); //$NON-NLS-1$

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

		List<Path> toolPaths = new ArrayList<>();
		if (isWindows) {
			// Add in the tools/make directory to pick up make
			toolPaths.add(ArduinoPreferences.getArduinoHome().resolve("tools/make")); //$NON-NLS-1$
		}
		ArduinoBoard board = getBoard();
		ArduinoPlatform platform = board.getPlatform();
		for (ToolDependency dep : platform.getToolsDependencies()) {
			ArduinoTool tool = dep.getTool();
			Path installPath = tool.getInstallPath();
			Path binPath = installPath.resolve("bin"); //$NON-NLS-1$
			if (binPath.toFile().exists()) {
				toolPaths.add(binPath);
			} else {
				// use the install dir by default
				toolPaths.add(installPath);
			}
		}
		for (Path toolPath : toolPaths) {
			if (path != null) {
				path = pathString(toolPath) + File.pathSeparatorChar + path;
			} else {
				path = pathString(toolPath);
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
		properties.put("runtime.tools." + toolName + ".path", pathString(tool.getInstallPath())); //$NON-NLS-1$ //$NON-NLS-2$
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

		properties.put("upload.verbose", "{tools." + toolName + ".upload.params.quiet}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		String command = resolveProperty("tools." + toolName + ".upload.pattern", properties); //$NON-NLS-1$ //$NON-NLS-2$
		if (isWindows) {
			return command.split(" "); //$NON-NLS-1$
		} else {
			return new String[] { "sh", "-c", command }; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public IToolChain getToolChainx() {
		try {
			IToolChain toolChain = super.getToolChain();
			if (toolChain == null) {
				// figure out which one it is
				IToolChainManager toolChainManager = Activator.getService(IToolChainManager.class);
				ArduinoPlatform platform = board.getPlatform();
				String compilerPath = resolveProperty("compiler.path", platform.getPlatformProperties()); //$NON-NLS-1$
				if (compilerPath != null) {
					// TODO what if it is null?
					Path path = Paths.get(compilerPath);
					for (ToolDependency toolDep : platform.getToolsDependencies()) {
						ArduinoTool tool = toolDep.getTool();
						if (path.startsWith(tool.getInstallPath())) {
						}
					}
				}
			}
			return toolChain;
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}
	}

	@Override
	public IScannerInfo getScannerInfo(IResource resource) throws IOException {
		IScannerInfo info = super.getScannerInfo(resource);
		if (info == null) {
			// what language is this resource and pick the right recipe
			String recipe;
			switch (CCorePlugin.getContentType(resource.getProject(), resource.getName()).getId()) {
			case CCorePlugin.CONTENT_TYPE_CXXSOURCE:
			case CCorePlugin.CONTENT_TYPE_CXXHEADER:
				recipe = "recipe.cpp.o.pattern"; //$NON-NLS-1$
				break;
			default:
				recipe = "recipe.c.o.pattern"; //$NON-NLS-1$
			}

			try {
				ArduinoPlatform platform = getBoard().getPlatform();
				Properties properties = new Properties();
				properties.putAll(getProperties());

				Path resourcePath = new File(resource.getLocationURI()).toPath();
				Path sourcePath = getBuildDirectory().toPath().relativize(resourcePath);
				properties.put("source_file", pathString(sourcePath)); //$NON-NLS-1$
				properties.put("object_file", "-"); //$NON-NLS-1$ //$NON-NLS-2$

				String includes = ""; //$NON-NLS-1$
				for (Path include : platform.getIncludePath()) {
					includes += " -I\"" + pathString(include) + '"'; //$NON-NLS-1$
				}
				Collection<ArduinoLibrary> libs = ArduinoManager.instance.getLibraries(getProject());
				for (ArduinoLibrary lib : libs) {
					for (Path path : lib.getIncludePath()) {
						includes += " -I\"" + pathString(path) + '"'; //$NON-NLS-1$
					}
				}
				properties.put("includes", includes); //$NON-NLS-1$

				List<String> cmd = Arrays.asList(resolveProperty(recipe, properties).split(" ")); //$NON-NLS-1$
				// TODO for reals
				info = getToolChain().getScannerInfo(cmd.get(0), cmd.subList(1, cmd.size()), null, resource, null);
			} catch (CoreException e) {
				throw new IOException(e);
			}
		}
		return info;
	}

	public static String pathString(Path path) {
		String str = path.toString();
		if (isWindows) {
			str = str.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return str;
	}

}
