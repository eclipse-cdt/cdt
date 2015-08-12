package org.eclipse.cdt.arduino.core.internal.build;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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

	private static final String PACKAGE_NAME = "packageId"; //$NON-NLS-1$
	private static final String PLATFORM_NAME = "platformName"; //$NON-NLS-1$
	private static final String BOARD_NAME = "boardName"; //$NON-NLS-1$

	private final IBuildConfiguration config;

	private ArduinoBuildConfiguration(IBuildConfiguration config) {
		this.config = config;
	}

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(ArduinoBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
				return (T) new ArduinoBuildConfiguration((IBuildConfiguration) adaptableObject);
			}
			return null;
		}

		@Override
		public Class<?>[] getAdapterList() {
			return new Class<?>[] { ArduinoBuildConfiguration.class };
		}
	}

	public IEclipsePreferences getSettings() {
		return (IEclipsePreferences) new ProjectScope(config.getProject()).getNode(Activator.getId()).node("config") //$NON-NLS-1$
				.node(config.getName());
	}

	public void setBoard(ArduinoBoard board) throws CoreException {
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
		IEclipsePreferences settings = getSettings();
		String packageName = settings.get(PACKAGE_NAME, ""); //$NON-NLS-1$
		String platformName = settings.get(PLATFORM_NAME, ""); //$NON-NLS-1$
		String boardName = settings.get(BOARD_NAME, ""); //$NON-NLS-1$
		return ArduinoBoardManager.instance.getBoard(boardName, platformName, packageName);
	}

	public IFolder getBuildFolder() throws CoreException {
		IProject project = config.getProject();
		return project.getFolder("build"); //$NON-NLS-1$
	}

	public IFile getMakeFile() throws CoreException {
		IFolder buildFolder = getBuildFolder();
		ArduinoBoard board = getBoard();
		String makeFileName = board.getId() + ".mk"; //$NON-NLS-1$
		return buildFolder.getFile(makeFileName);
	}

	public IFile generateMakeFile(IProgressMonitor monitor) throws CoreException {
		final IProject project = config.getProject();

		// Make sure build folder exists
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
		Properties properties = board.getBoardProperties();
		properties.putAll(board.getPlatform().getPlatformProperties());
		for (ToolDependency toolDep : platform.getToolsDependencies()) {
			properties.putAll(toolDep.getTool().getToolProperties());
		}
		properties.put("runtime.ide.version", "1.6.7"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("build.arch", platform.getArchitecture().toUpperCase()); //$NON-NLS-1$
		properties.put("build.path", "$(OUTPUT_DIR)"); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("build.project_name", project.getName()); //$NON-NLS-1$
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
				return name.endsWith(".cpp") || name.endsWith(".c");
			}
		});
		String[] platformSource = new String[platformFiles.length];
		for (int i = 0; i < platformSource.length; ++i) {
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

	public String[] getBuildCommand() throws CoreException {
		return new String[] { "make", "-f", getMakeFile().getName() }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String[] getCleanCommand() throws CoreException {
		return new String[] { "make", "-f", getMakeFile().getName(), "clean" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public String[] getEnvironment() throws CoreException {
		Map<String, String> env = new HashMap<>(System.getenv());

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

		// Reformat as a proper env.
		List<String> strEnv = new ArrayList<>(env.size());
		for (Map.Entry<String, String> entry : env.entrySet()) {
			strEnv.add(entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
		}
		return strEnv.toArray(new String[strEnv.size()]);
	}

}
