/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.CommandDescriptorBuilder.CommandDescriptor;
import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.ICMakePropertiesController;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;
import org.eclipse.cdt.cmake.is.core.CompileCommandsJsonParser;
import org.eclipse.cdt.cmake.is.core.IIndexerInfoConsumer;
import org.eclipse.cdt.cmake.is.core.ParseRequest;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public static final String CMAKE_GENERATOR = "cmake.generator"; //$NON-NLS-1$
	public static final String CMAKE_ARGUMENTS = "cmake.arguments"; //$NON-NLS-1$
	public static final String CMAKE_ENV = "cmake.environment"; //$NON-NLS-1$
	public static final String BUILD_COMMAND = "cmake.command.build"; //$NON-NLS-1$
	public static final String CLEAN_COMMAND = "cmake.command.clean"; //$NON-NLS-1$

	private ICMakeToolChainFile toolChainFile;

	// lazily instantiated..
	private CMakePropertiesController pc;

	private Map<IResource, IScannerInfo> infoPerResource;
	/**
	 * whether one of the CMakeLists.txt files in the project has been modified and saved by the
	 * user since the last build.<br>
	 * Cmake-generated build scripts re-run cmake if one of the CMakeLists.txt files was modified,
	 * but that output goes through ErrorParserManager and is impossible to parse because cmake
	 * outputs to both stderr and stdout and ErrorParserManager intermixes these streams making it
	 * impossible to parse for errors.<br>
	 * To work around that, we run cmake in advance with its dedicated working error parser.
	 */
	private boolean cmakeListsModified;
	/**
	 * whether we have to delete file CMakeCache.txt to avoid complaints by cmake
	 */
	private boolean deleteCMakeCache;

	public CMakeBuildConfiguration(IBuildConfiguration config, String name) throws CoreException {
		super(config, name);

		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		toolChainFile = manager.getToolChainFileFor(getToolChain());
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain) {
		this(config, name, toolChain, null, "run"); //$NON-NLS-1$
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, String name, IToolChain toolChain,
			ICMakeToolChainFile toolChainFile, String launchMode) {
		super(config, name, toolChain, launchMode);
		this.toolChainFile = toolChainFile;
	}

	/**
	 * Gets the tool-chain description file to pass to the cmake command-line.
	 *
	 * @return the tool-chain file or <code>null</code> if cmake should take the native (i.e. the
	 *         tools first found on the executable search path aka $path)
	 */
	public ICMakeToolChainFile getToolChainFile() {
		return toolChainFile;
	}

	@SuppressWarnings("unused") // kept for reference of the property names
	private boolean isLocal() throws CoreException {
		IToolChain toolchain = getToolChain();
		return (Platform.getOS().equals(toolchain.getProperty(IToolChain.ATTR_OS))
				|| "linux-container".equals(toolchain.getProperty(IToolChain.ATTR_OS))) //$NON-NLS-1$
				&& (Platform.getOSArch().equals(toolchain.getProperty(IToolChain.ATTR_ARCH)));
	}

	@Override
	public IProject[] build(int kind, Map<String, String> args, IConsole console, IProgressMonitor monitor)
			throws CoreException {
		final IProject project = getProject();

		project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		infoPerResource = new HashMap<>();

		try {
			ConsoleOutputStream infoStream = console.getInfoStream();

			Path buildDir = getBuildDirectory();
			org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDir.toString());

			ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
			launcher.setProject(project);
			launcher.showCommand(true);

			boolean runCMake = cmakeListsModified;
			if (deleteCMakeCache) {
				Files.deleteIfExists(buildDir.resolve("CMakeCache.txt")); //$NON-NLS-1$
				deleteCMakeCache = false;
				runCMake = true;
			}

			ICMakeProperties cmakeProperties = getPropertiesController().load();
			runCMake |= !Files.exists(buildDir.resolve("CMakeCache.txt")); //$NON-NLS-1$

			final SimpleOsOverridesSelector overridesSelector = new SimpleOsOverridesSelector();
			if (!runCMake) {
				CMakeGenerator generator = overridesSelector.getOsOverrides(cmakeProperties).getGenerator();
				runCMake |= !Files.exists(buildDir.resolve(generator.getMakefileName()));
			}
			CommandDescriptorBuilder cmdBuilder = new CommandDescriptorBuilder(cmakeProperties, overridesSelector);

			boolean buildscriptsOK = true;
			if (runCMake) {
				CMakeBuildConfiguration.deleteCMakeErrorMarkers(project);

				infoStream.write(String.format(Messages.CMakeBuildConfiguration_Configuring, buildDir));
				// tell cmake where its script is located..
				IContainer srcFolder = project;

				buildscriptsOK = generateBuildscripts(launcher, cmdBuilder, srcFolder, workingDir, console, monitor);
				cmakeListsModified = false;
			}

			if (buildscriptsOK) {
				// run the build tool...
				infoStream.write(String.format(Messages.CMakeBuildConfiguration_BuildingIn, buildDir.toString()));
				try (ErrorParserManager epm = new ErrorParserManager(project, getBuildDirectoryURI(), this,
						getToolChain().getErrorParserIds())) {
					epm.setOutputStream(console.getOutputStream());

					CommandDescriptor commandDescr = cmdBuilder.makeCMakeBuildCommandline("all"); //$NON-NLS-1$
					List<String> command = commandDescr.getArguments();
					// extract name of executable
					final String arg0 = command.remove(0);

					List<String> envList = new ArrayList<>();
					// pass envvars from CommandDescriptor
					envList.addAll(commandDescr.getEnvironment());

					String envStr = getProperty(CMAKE_ENV);
					if (envStr != null) {
						envList.addAll(CMakeUtils.stripEnvVars(envStr));
					}

					launcher.setErrorMessage(""); //$NON-NLS-1$
					final Process p = launcher.execute(new org.eclipse.core.runtime.Path(arg0),
							command.toArray(new String[command.size()]), envList.toArray(new String[envList.size()]),
							new org.eclipse.core.runtime.Path(workingDir.toString()), monitor);
					if (p == null) {
						// process start failed
						String msg = String.format(Messages.CMakeBuildConfiguration_Failure,
								launcher.getErrorMessage());
						addMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
					} else {
						try {
							// Close the input of the process since we will never write to it
							p.getOutputStream().close();
						} catch (IOException e) {
						}

						int state = launcher.waitAndRead(console.getOutputStream(), console.getErrorStream(), monitor);
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
						if (state == ICommandLauncher.COMMAND_CANCELED) {
							throw new OperationCanceledException(launcher.getErrorMessage());
						}
						String msg = String.format(Messages.CMakeBuildConfiguration_BuildingComplete,
								epm.getErrorCount(), epm.getWarningCount(), buildDir.toString());
						infoStream.write(msg);
						// check exit status
						final int exitValue = p.exitValue();
						if (exitValue != 0) {
							// had errors...
							String msg2 = String.format(Messages.CMakeBuildConfiguration_ExitFailure, arg0, exitValue);
							addMarker(null, -1, msg2, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
						}
					}
				}

				// parse compile_commands.json file
				processCompileCommandsFile(console, monitor);
			}
			return new IProject[] { project };
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Building, project.getName()), e));
		}
	}

	/** Runs cmake to generate the build-scripts.
	 *
	 * @return whether build-script generation was successful
	 *
	 * @throws CoreException
	 * @throws IOException  if {@code console} failed to get an output stream
	 */
	private boolean generateBuildscripts(ICommandLauncher launcher, CommandDescriptorBuilder cmdBuilder,
			IContainer srcFolder, IPath workingDir, IConsole console, IProgressMonitor monitor)
			throws CoreException, IOException {
		CommandDescriptor commandDescr = cmdBuilder
				.makeCMakeCommandline(toolChainFile != null ? toolChainFile.getPath() : null);

		// hook in cmake error parsing
		CMakeExecutionMarkerFactory markerFactory = new CMakeExecutionMarkerFactory(srcFolder);
		// NOTE: we need one parser for each stream, since the output streams are not synchronized
		// when the process is started via o.e.c.core.CommandLauncher, causing loss of the internal parser state
		// We parse stderr only, since cmake currently does not write anything of interest to stdout
		try (OutputStream errStream = new ParsingOutputStream(console.getErrorStream(),
				new CMakeErrorParser(markerFactory))) {

			List<String> arguments = commandDescr.getArguments();
			// tell cmake where its script is located..
			arguments.add(new File(srcFolder.getLocationURI()).getAbsolutePath());
			// extract name of executable
			final String arg0 = arguments.remove(0);
			final Process p = launcher.execute(new org.eclipse.core.runtime.Path(arg0),
					arguments.toArray(new String[arguments.size()]), null, workingDir, monitor);
			if (p == null) {
				// process start failed
				String msg = String.format(Messages.CMakeBuildConfiguration_Failure, launcher.getErrorMessage());
				addMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
				return false; // failure
			} else {
				try {
					// Close the input of the process since we will never write to it
					p.getOutputStream().close();
				} catch (IOException e) {
				}

				int state = launcher.waitAndRead(console.getOutputStream(), errStream, monitor);
				if (state == ICommandLauncher.COMMAND_CANCELED) {
					throw new OperationCanceledException(launcher.getErrorMessage());
				}
				// check cmake exit status
				final int exitValue = p.exitValue();
				if (exitValue != 0) {
					// cmake had errors...
					String msg = String.format(Messages.CMakeBuildConfiguration_ExitFailure, arg0, exitValue);
					addMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
					return false; // failure
				}
			}
		}
		return true; // success
	}

	@Override
	public void clean(IConsole console, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		try {

			project.deleteMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);

			console.getInfoStream()
					.write(String.format(Messages.CMakeBuildConfiguration_Cleaning + "%n", project.getName())); //$NON-NLS-1$

			Path buildDir = getBuildDirectory();
			if (!Files.exists(buildDir.resolve("CMakeFiles"))) { //$NON-NLS-1$
				console.getOutputStream().write(Messages.CMakeBuildConfiguration_NotFound);
				return;
			}

			ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
			launcher.setProject(project);
			launcher.showCommand(true);

			ICMakeProperties cmakeProperties = getPropertiesController().load();
			CommandDescriptorBuilder cmdBuilder = new CommandDescriptorBuilder(cmakeProperties,
					new SimpleOsOverridesSelector());
			CommandDescriptor commandDescr = cmdBuilder.makeCMakeBuildCommandline("clean"); //$NON-NLS-1$
			List<String> command = commandDescr.getArguments();
			// extract name of executable
			final String arg0 = command.remove(0);

			org.eclipse.core.runtime.Path workingDir = new org.eclipse.core.runtime.Path(buildDir.toString());
			final Process p = launcher.execute(new org.eclipse.core.runtime.Path(arg0),
					command.toArray(new String[command.size()]), null, workingDir, monitor);
			if (p == null) {
				// process start failed
				String msg = String.format(Messages.CMakeBuildConfiguration_Failure, launcher.getErrorMessage());
				addMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
			} else {
				try {
					// Close the input of the process since we will never write to it
					p.getOutputStream().close();
				} catch (IOException e) {
				}

				int state = launcher.waitAndRead(console.getOutputStream(), console.getErrorStream(), monitor);
				if (state == ICommandLauncher.COMMAND_CANCELED) {
					throw new OperationCanceledException(launcher.getErrorMessage());
				}
				// check exit status
				final int exitValue = p.exitValue();
				if (exitValue != 0) {
					// had errors...
					String msg = String.format(Messages.CMakeBuildConfiguration_ExitFailure, arg0, exitValue);
					addMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
				}
			}

			console.getInfoStream().write(Messages.CMakeBuildConfiguration_BuildComplete);

			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (IOException e) {
			throw new CoreException(Activator
					.errorStatus(String.format(Messages.CMakeBuildConfiguration_Cleaning, project.getName()), e));
		}
	}

	/**
	 * @param console the console to print the compiler output during built-ins detection to or
	 *                <code>null</code> if no separate console is to be allocated. Ignored if
	 *                workspace preferences indicate that no console output is wanted.
	 * @param monitor the job's progress monitor
	 */
	private void processCompileCommandsFile(IConsole console, IProgressMonitor monitor) throws CoreException {
		IFile file = getBuildContainer().getFile(new org.eclipse.core.runtime.Path("compile_commands.json")); //$NON-NLS-1$
		CompileCommandsJsonParser parser = new CompileCommandsJsonParser(
				new ParseRequest(file, new CMakeIndexerInfoConsumer(this::setScannerInformation),
						CommandLauncherManager.getInstance().getCommandLauncher(this), console));
		parser.parse(monitor);
	}

	/**
	 * Recursively removes any files and directories found below the specified Path.
	 * Kept for refernece since it is proven to not run out of file handles.
	 */
	private static void cleanDirectory(java.nio.file.Path dir) throws IOException {
		SimpleFileVisitor<java.nio.file.Path> deltor = new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
				super.postVisitDirectory(dir, exc);
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		};
		java.nio.file.Path[] files = Files.list(dir).toArray(java.nio.file.Path[]::new);
		for (java.nio.file.Path file : files) {
			Files.walkFileTree(file, deltor);
		}
	}

	/** Lazily creates the CMakePropertiesController for the project.
	 */
	private CMakePropertiesController getPropertiesController() {
		if (pc == null) {
			final Path filePath = Path.of(getProject().getFile(".settings/CDT-cmake.yaml").getLocationURI()); //$NON-NLS-1$
			pc = new CMakePropertiesController(filePath, () -> {
				deleteCMakeCache = true;
				// TODO delete cache file here for the case a user restarts the workbench
				// prior to running a new build
			});
		}
		return pc;
	}

	// interface IAdaptable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (ICMakePropertiesController.class.equals(adapter)) {
			return (T) pc;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Overridden since the ScannerInfoCache mechanism does not satisfy our needs.
	 */
	// interface IScannerInfoProvider
	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		if (infoPerResource == null) {
			// no build was run yet, nothing detected
			infoPerResource = new HashMap<>();
			try {
				processCompileCommandsFile(null, new NullProgressMonitor());
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return infoPerResource.get(resource);
	}

	private void setScannerInformation(Map<IResource, IScannerInfo> infoPerResource) {
		this.infoPerResource = infoPerResource;
	}

	/**
	 * Overwritten to detect whether one of the CMakeLists.txt files in the project was modified
	 * since the last build.
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
		super.elementChanged(event);
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;
		if (!cmakeListsModified) {
			processElementDelta(event.getDelta());
		}
	}

	/**
	 * Processes the delta in order to detect whether one of the CMakeLists.txt files in the project
	 * has been modified and saved by the user since the last build.
	 *
	 * @return <code>true</code> to continue with delta processing, otherwise <code>false</code>
	 */
	private boolean processElementDelta(ICElementDelta delta) {
		if (delta == null) {
			return true;
		}

		if (delta.getKind() == ICElementDelta.CHANGED) {
			// check for modified CMakeLists.txt file
			if (0 != (delta.getFlags() & ICElementDelta.F_CONTENT)) {
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				if (resourceDeltas != null) {
					for (IResourceDelta resourceDelta : resourceDeltas) {
						IResource resource = resourceDelta.getResource();
						if (resource.getType() == IResource.FILE) {
							String name = resource.getName();
							if (!resource.isDerived(IResource.CHECK_ANCESTORS)
									&& (name.equals("CMakeLists.txt") || name.endsWith(".cmake"))) { //$NON-NLS-1$ //$NON-NLS-2$
								cmakeListsModified = true;
								return false; // stop processing
							}
						}
					}
				}
			}
		}

		// recurse...
		for (ICElementDelta child : delta.getAffectedChildren()) {
			if (!processElementDelta(child)) {
				return false; // stop processing
			}
		}
		return true;
	}

	/**
	 * Overwritten since we do not parse console output to get scanner information.
	 */
	// interface IConsoleParser2
	@Override
	public boolean processLine(String line) {
		return true;
	}

	/**
	 * Overwritten since we do not parse console output to get scanner information.
	 */
	// interface IConsoleParser2
	@Override
	public boolean processLine(String line, List<Job> jobsArray) {
		return true;
	}

	/**
	 * Overwritten since we do not parse console output to get scanner information.
	 */
	// interface IConsoleParser2
	@Override
	public void shutdown() {
	}

	/**
	 * Deletes all CMake error markers on the specified project.
	 *
	 * @param project
	 *          the project where to remove the error markers.
	 * @throws CoreException
	 */
	private static void deleteCMakeErrorMarkers(IProject project) throws CoreException {
		project.deleteMarkers(ICMakeExecutionMarkerFactory.CMAKE_PROBLEM_MARKER_ID, false, IResource.DEPTH_INFINITE);
	}

	private static class CMakeIndexerInfoConsumer implements IIndexerInfoConsumer {
		/**
		 * gathered IScannerInfo objects or <code>null</code> if no new IScannerInfo was
		 * received
		 */
		private Map<IResource, IScannerInfo> infoPerResource = new HashMap<>();
		private boolean haveUpdates;
		private final Consumer<Map<IResource, IScannerInfo>> resultSetter;

		/**
		 * @param resultSetter receives the all scanner information when processing is
		 *                     finished
		 */
		public CMakeIndexerInfoConsumer(Consumer<Map<IResource, IScannerInfo>> resultSetter) {
			this.resultSetter = Objects.requireNonNull(resultSetter);
		}

		@Override
		public void acceptSourceFileInfo(String sourceFileName, List<String> systemIncludePaths,
				Map<String, String> definedSymbols, List<String> includePaths, List<String> macroFiles,
				List<String> includeFiles) {
			IFile file = getFileForCMakePath(sourceFileName);
			if (file != null) {
				ExtendedScannerInfo info = new ExtendedScannerInfo(definedSymbols,
						systemIncludePaths.stream().toArray(String[]::new), macroFiles.stream().toArray(String[]::new),
						includeFiles.stream().toArray(String[]::new), includePaths.stream().toArray(String[]::new));
				infoPerResource.put(file, info);
				haveUpdates = true;
			}
		}

		/**
		 * Gets an IFile object that corresponds to the source file name given in CMake
		 * notation.
		 *
		 * @param sourceFileName the name of the source file, in CMake notation. Note
		 *                       that on windows, CMake writes filenames with forward
		 *                       slashes (/) such as {@code H://path//to//source.c}.
		 * @return a IFile object or <code>null</code>
		 */
		private IFile getFileForCMakePath(String sourceFileName) {
			org.eclipse.core.runtime.Path path = new org.eclipse.core.runtime.Path(sourceFileName);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
			// TODO maybe we need to introduce a strategy here to get the workbench resource
			// Possible build scenarios:
			// 1) linux native: should be OK as is
			// 2) linux host, building in container: should be OK as is
			// 3) windows native: Path.fromOSString()?
			// 4) windows host, building in linux container: ??? needs testing on windows
			return file;
		}

		@Override
		public void shutdown() {
			if (haveUpdates) {
				// we received updates
				resultSetter.accept(infoPerResource);
				infoPerResource = null;
				haveUpdates = false;
			}
		}
	} // CMakeIndexerInfoConsumer

	private static class SimpleOsOverridesSelector implements IOsOverridesSelector {

		@Override
		public IOsOverrides getOsOverrides(ICMakeProperties cmakeProperties) {
			IOsOverrides overrides;
			// get overrides. Simplistic approach ATM, probably a strategy might fit better.
			// see comment in CMakeIndexerInfoConsumer#getFileForCMakePath()
			final String os = Platform.getOS();
			if (Platform.OS_WIN32.equals(os)) {
				overrides = cmakeProperties.getWindowsOverrides();
			} else {
				// fall back to linux, if OS is unknown
				overrides = cmakeProperties.getLinuxOverrides();
			}
			return overrides;
		}
	} // SimpleOsOverridesSelector
}
