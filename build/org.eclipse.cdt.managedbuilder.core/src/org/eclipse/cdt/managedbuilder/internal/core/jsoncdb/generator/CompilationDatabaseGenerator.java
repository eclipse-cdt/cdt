/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core.jsoncdb.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.jsoncdb.CompilationDatabaseInformation;
import org.eclipse.cdt.managedbuilder.core.jsoncdb.ICompilationDatabaseContributor;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class CompilationDatabaseGenerator {

	private static final String CDB_FILENAME = "compile_commands.json"; //$NON-NLS-1$
	private static final String ERROR_MESSAGE = "Can not set contents to compile_commands.json file"; //$NON-NLS-1$
	/**
	 * Checked on each build
	 * Used before we look up the environment
	 * Map of compiler name (as calculated by {@link #getCompilerName(String)}) to the absolute path of the compiler.
	 */
	private Map<String, String> toolMap = new HashMap<>();
	private IProject project;
	private IConfiguration configuration;
	private ICSourceEntry[] srcEntries;
	private Collection<IResource> fileList;

	public CompilationDatabaseGenerator(IProject proj, IConfiguration config) {
		project = proj;
		configuration = config;
		srcEntries = config.getSourceEntries();
	}

	/**
	 * @param proj
	 *            Creates and populates compilation database file
	 * @param config
	 */
	public void generate() {
		IPath buildDirectory = ManagedBuildManager.getBuildFullPath(configuration, configuration.getBuilder());
		IPath compilationDatabasePath = buildDirectory
				.append(IPath.SEPARATOR + CompilationDatabaseGenerator.CDB_FILENAME);
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (!workspace.getRoot().exists(buildDirectory)) {
				createDirectory(workspace.getRoot().getFolder(buildDirectory).getProjectRelativePath().toString());
			}
			IFile compileCommandsFile = createFile(compilationDatabasePath);
			addToCompilationdatabase(project, compileCommandsFile, configuration);

		} catch (CoreException e) {
			Platform.getLog(getClass())
					.log(Status.error("Unable to initialize the creation of compile_commands.json file", e)); //$NON-NLS-1$
		}

	}

	private IFile createFile(IPath compilationDatabasePath) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile newFile = workspace.getRoot().getFile(compilationDatabasePath);
		try {
			if (!newFile.exists()) {
				try (InputStream inputStream = new ByteArrayInputStream("".getBytes())) { //$NON-NLS-1$
					newFile.create(inputStream, true, null);
				}
			} else {
				try (InputStream inputStream = new ByteArrayInputStream("".getBytes())) { //$NON-NLS-1$
					newFile.setContents(inputStream, true, false, null);
				}
			}

		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, getClass(), CompilationDatabaseGenerator.ERROR_MESSAGE, e);
			Platform.getLog(getClass()).log(status);
		}

		return newFile;
	}

	private void addToCompilationdatabase(IProject project, IFile compileCommandsFile, @NonNull IConfiguration config)
			throws CoreException {
		List<CompilationDatabaseInformation> objList = new ArrayList<>();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		ResourceProxyVisitor resourceVisitor = new ResourceProxyVisitor(this, config);
		project.accept(resourceVisitor, IResource.NONE);
		try {
			objList.addAll(populateObjList(project, config));
			objList.addAll(getRunnerForToolchain(config).getAdditionalFiles(config));
			CompilationDatabaseGenerator.save(gson.toJson(objList), compileCommandsFile);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, getClass(), CompilationDatabaseGenerator.ERROR_MESSAGE, e);
			Platform.getLog(getClass()).log(status);
		}
	}

	private static void save(String buffer, IFile file) throws CoreException {
		byte[] bytes = buffer.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		boolean force = true;
		file.setContents(stream, force, false, null); // Don't record history
	}

	@NonNull
	private ICompilationDatabaseContributor getRunnerForToolchain(@NonNull IConfiguration config) throws CoreException {
		return CompilationDatabaseContributionManager.getInstance().getCompilationDatabaseContributor(config);
	}

	private List<CompilationDatabaseInformation> populateObjList(IProject project, IConfiguration config)
			throws CoreException, BuildException {
		List<CompilationDatabaseInformation> objList = new ArrayList<>();
		for (IResource resource : getFileList()) {

			IPath moduleRelativePath = resource.getParent().getProjectRelativePath();
			String relativePath = moduleRelativePath.toString();
			IFolder folder = project.getFolder(config.getName());
			IPath sourceLocation = getPathForResource(resource);
			String ext = sourceLocation.getFileExtension();
			IResourceInfo rcInfo = config.getResourceInfo(resource.getProjectRelativePath(), false);
			ITool tool = null;
			if (rcInfo instanceof IFileInfo fi) {
				ITool[] tools = fi.getToolsToInvoke();
				if (tools != null && tools.length > 0) {
					tool = tools[0];
					for (ITool tool2 : tools) {
						if (!tool2.getCustomBuildStep()) {
							tool = tool2;
							break;
						}
					}
				}
			} else if (rcInfo instanceof IFolderInfo foInfo) {
				tool = foInfo.getToolFromInputExtension(ext);
			}

			if (tool != null) {

				ArrayList<IPath> enumeratedPrimaryOutputs = new ArrayList<>();
				calculateOutputsForSource(tool, relativePath, sourceLocation, enumeratedPrimaryOutputs);
				ArrayList<String> inputs = new ArrayList<>();
				inputs.add(resource.getLocation().toString());

				IPath[] addlInputPaths = tool.getAdditionalDependencies();
				for (IPath addlInputPath : addlInputPaths) {

					IPath addlPath = addlInputPath;
					if (!(addlPath.toString().startsWith("$(")) && !addlPath.isAbsolute()) { //$NON-NLS-1$
						IPath tempPath = getPathForResource(project).append(addlPath);
						if (tempPath != null) {
							addlPath = ManagedBuildManager.calculateRelativePath(folder.getLocation(), tempPath);
						}
					}
					inputs.add(addlPath.toString());
				}
				String[] inputStrings = inputs.toArray(new String[inputs.size()]);
				String primaryOutputName = null;
				String resourceName = sourceLocation.removeFileExtension().lastSegment();
				if (!enumeratedPrimaryOutputs.isEmpty()) {
					if (enumeratedPrimaryOutputs.size() > 1) {
						StringBuilder resultBuilder = new StringBuilder();
						for (IPath outputPath : enumeratedPrimaryOutputs) {
							resultBuilder.append(outputPath.toString()).append(" "); //$NON-NLS-1$
						}
						primaryOutputName = resultBuilder.toString().trim();
					} else {
						primaryOutputName = enumeratedPrimaryOutputs.get(0).toString();
					}
				} else {
					primaryOutputName = relativePath + IPath.SEPARATOR + resourceName + ".o"; //$NON-NLS-1$
				}
				IPath outputLocation = Path.fromOSString(primaryOutputName);
				if (!outputLocation.isAbsolute()) {
					outputLocation = getPathForResource(project).append(folder.getName()).append(primaryOutputName);
				}
				String outflag = tool.getOutputFlag();
				String outputPrefix = tool.getOutputPrefix();
				String[] flags = tool.getToolCommandFlags(sourceLocation, folder.getFullPath());
				IManagedCommandLineInfo cmdLInfo = generateToolCommandLineInfo(tool, flags, outflag, outputPrefix,
						outputLocation + "", inputStrings, sourceLocation, outputLocation); //$NON-NLS-1$

				IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
				String commandLine = cmdLInfo.getCommandLine();
				String compilerName = CompilationDatabaseGenerator.getCompilerName(commandLine);
				String compilerArguments = getCompilerArgs(commandLine);
				String compilerPath = findCompilerInPath(config, compilerName);
				String resolvedOptionFileContents;
				if (compilerPath != null && !compilerPath.isEmpty()) {
					resolvedOptionFileContents = provider.resolveValueToMakefileFormat(
							compilerPath + " " + compilerArguments, //$NON-NLS-1$
							"", " ", //$NON-NLS-1$//$NON-NLS-2$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(sourceLocation, outputLocation, null, tool));
				} else {
					resolvedOptionFileContents = provider.resolveValueToMakefileFormat(compilerArguments, "", " ", //$NON-NLS-1$//$NON-NLS-2$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(sourceLocation, outputLocation, null, tool));

				}
				objList.add(new CompilationDatabaseInformation(project.getLocation().toString(),
						resolvedOptionFileContents, resource.getLocation().toString()));
			}

		}
		return objList;

	}

	private IManagedCommandLineInfo generateToolCommandLineInfo(ITool tool, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources, IPath inputLocation,
			IPath outputLocation) {

		String cmd = tool.getToolCommand();
		try {
			String resolvedCommand = null;

			if ((inputLocation != null && inputLocation.toString().indexOf(" ") != -1) || //$NON-NLS-1$
					(outputLocation != null && outputLocation.toString().indexOf(" ") != -1)) { //$NON-NLS-1$
				resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValue(cmd, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(inputLocation, outputLocation, null, tool));
			} else {
				resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(cmd, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(inputLocation, outputLocation, null, tool));
			}
			if ((resolvedCommand = resolvedCommand.trim()).length() > 0) {
				cmd = resolvedCommand;
			}

		} catch (BuildMacroException e) {
			Platform.getLog(getClass()).log(Status.error(CompilationDatabaseGenerator.ERROR_MESSAGE, e));
		}

		IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
		return gen.generateCommandLineInfo(tool, cmd, flags, outputFlag, outputPrefix, outputName, inputResources,
				tool.getCommandLinePattern());

	}

	private void calculateOutputsForSource(ITool tool, String relativePath, IPath sourceLocation,
			ArrayList<IPath> enumeratedPrimaryOutputs) {

		IOutputType[] outTypes = tool.getOutputTypes();
		if (outTypes != null && outTypes.length > 0) {
			IOutputType type = tool.getPrimaryOutputType();
			boolean primaryOutput = (type == tool.getPrimaryOutputType());
			String[] extensions = type.getOutputExtensions(tool);
			if (primaryOutput) {
				for (String extension : extensions) {
					String prefix = type.getOutputPrefix();
					String fileName = sourceLocation.removeFileExtension().lastSegment();
					IPath outPath = Path.fromOSString(relativePath + IPath.SEPARATOR + prefix + fileName);
					outPath = outPath.addFileExtension(extension);
					enumeratedPrimaryOutputs.add(0, outPath);

				}
			}
		}
	}

	private IPath getPathForResource(IResource resource) {
		return new Path(resource.getLocationURI().getPath());
	}

	private boolean isSource(IPath path) {
		return !CDataUtil.isExcluded(path, srcEntries);
	}

	private void appendFileList(IResource resource) {
		getFileList().add(resource);
	}

	private Collection<IResource> getFileList() {
		if (fileList == null) {
			fileList = new LinkedHashSet<>();
		}
		return fileList;
	}

	private boolean isGeneratedResource(IResource resource) {
		// Is this a generated directory ...
		IPath path = resource.getProjectRelativePath();
		String[] configNames = ManagedBuildManager.getBuildInfo(project).getConfigurationNames();
		for (String name : configNames) {
			IPath root = new Path(name);
			// It is if it is a root of the resource pathname
			if (root.isPrefixOf(path)) {
				return true;
			}
		}

		return false;
	}

	private IPath createDirectory(String dirName) throws CoreException {
		// Create or get the handle for the build directory
		IFolder folder = project.getFolder(dirName);
		if (!folder.exists()) {
			// Make sure that parent folders exist
			IPath parentPath = (new Path(dirName)).removeLastSegments(1);
			// Assume that the parent exists if the path is empty
			if (!parentPath.isEmpty()) {
				IFolder parent = project.getFolder(parentPath);
				if (!parent.exists()) {
					createDirectory(parentPath.toString());
				}
			}

			// Now make the requested folder
			try {
				folder.create(true, true, null);
			} catch (CoreException e) {
				if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED) {
					folder.refreshLocal(IResource.DEPTH_ZERO, null);
				} else {
					throw e;
				}
			}

			// Make sure the folder is marked as derived so it is not added to CM
			if (!folder.isDerived()) {
				folder.setDerived(true, null);
			}
		}

		return folder.getFullPath();
	}

	/**
	 * This class is used to recursively walk the project and determine which
	 * modules contribute buildable source files.
	 */
	private class ResourceProxyVisitor implements IResourceProxyVisitor {
		private final CompilationDatabaseGenerator generator;
		private final IConfiguration config;

		public ResourceProxyVisitor(CompilationDatabaseGenerator generator, IConfiguration cfg) {
			this.generator = generator;
			this.config = cfg;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.
		 * resources.IResourceProxy)
		 */
		@Override
		public boolean visit(IResourceProxy proxy) throws CoreException {
			// No point in proceeding, is there
			if (generator == null) {
				return false;
			}

			IResource resource = proxy.requestResource();
			boolean isSource = isSource(resource.getProjectRelativePath());

			// Is this a resource we should even consider
			if (proxy.getType() == IResource.FILE) {
				// If this resource has a Resource Configuration and is not excluded or
				// if it has a file extension that one of the tools builds, add the sudirectory
				// to the list
				IResourceInfo rcInfo = config.getResourceInfo(resource.getProjectRelativePath(), false);
				if (isSource/* && !rcInfo.isExcluded() */) {
					if (rcInfo instanceof IFolderInfo) {
						String ext = resource.getFileExtension();
						boolean buildFile = ((IFolderInfo) rcInfo).buildsFileType(ext);
						if (buildFile &&
						// If this file resource is a generated resource, then it is uninteresting
								!generator.isGeneratedResource(resource)) {

							generator.appendFileList(resource);
						}
					} else {
						generator.appendFileList(resource);
					}
				}
			} else if (proxy.getType() == IResource.FOLDER) {

				if (!isSource || generator.isGeneratedResource(resource)) {
					return false;
				}
				return true;
			}
			return true;
		}

	}

	private String findCompilerInPath(IConfiguration config, String compilerName) {
		String cacheKey = compilerName;
		if (toolMap.containsKey(cacheKey)) {
			return "\"" + toolMap.get(cacheKey) + "\""; //$NON-NLS-1$//$NON-NLS-2$
		}
		File pathToCompiler = new File(compilerName);

		if (pathToCompiler.isAbsolute()) {
			toolMap.put(cacheKey, compilerName);
			return "\"" + compilerName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		ICConfigurationDescription cfg = ManagedBuildManager.getDescriptionForConfiguration(config);
		IEnvironmentVariable[] variables = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfg,
				true);

		for (IEnvironmentVariable variable : variables) {
			if ("PATH".equalsIgnoreCase(variable.getName())) { //$NON-NLS-1$
				IPath resolvedPath = PathUtil.findProgramLocation(compilerName, variable.getValue());
				if (resolvedPath != null) {
					String path = resolvedPath.toString();
					toolMap.put(cacheKey, path);
					return "\"" + path + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					return null; // Only one PATH so can exit early
				}
			}
		}

		return null;
	}

	private static String getCompilerName(String commandLine) {
		String[] arguments = CommandLineUtil.argumentsToArray(commandLine);
		if (arguments.length == 0) {
			return ""; //$NON-NLS-1$
		}
		return arguments[0];
	}

	private static String getCompilerArgs(String commandLine) {
		String[] arguments = CommandLineUtil.argumentsToArray(commandLine);
		if (arguments.length <= 1) {
			return ""; //$NON-NLS-1$
		}
		List<String> argsList = Arrays.asList(arguments).subList(1, arguments.length);
		return escArgsForCompileCommand(argsList);
	}

	private static String escArgsForCompileCommand(final List<String> args) {
		return args.stream().map(arg -> {
			if (arg.contains(" ") || arg.contains("\"") || arg.contains("\\")) { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				String escaped = arg.replace("\\", "\\\\").replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
				return "\"" + escaped + "\""; //$NON-NLS-1$//$NON-NLS-2$
			} else {
				return arg;
			}
		}).collect(Collectors.joining(" ")); //$NON-NLS-1$
	}

}
