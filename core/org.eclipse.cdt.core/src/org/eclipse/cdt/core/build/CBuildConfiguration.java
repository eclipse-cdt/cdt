/*******************************************************************************
 * Copyright (c) 2015, 2023 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IConsoleParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IIncludeFileEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IMacroFileEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.build.CBuildConfigurationManager;
import org.eclipse.cdt.internal.core.build.Messages;
import org.eclipse.cdt.internal.core.model.BinaryRunner;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.scannerinfo.ExtendedScannerInfoSerializer;
import org.eclipse.cdt.internal.core.scannerinfo.IExtendedScannerInfoDeserializer;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Root class for CDT build configurations. Provides access to the build
 * settings for subclasses.
 *
 * Each Eclipse project has one or more build configurations ({@link IBuildConfiguration}).
 * A CDT Core Build project pairs each build configuration with a Core Build configuration
 * ({@link ICBuildConfiguration}). A Core Build configuration has variable config pointing to
 * the IBuildConfiguration. The link from IBuildConfiguration to ICBuildConfiguration
 * goes via getAdapter(ICBuildConfiguration.class) which gets the ICBuildConfiguration
 * from Map configs in the {@link CBuildConfigurationManager}.
 *
 * In a new project the initial Core Build configurations creation is triggered by the
 * {@link CoreBuildLaunchBarTracker}. The initial configuration for Debug will only be
 * created if the user selects the Debug launch mode. Restoration of Core Build configurations,
 * after an Eclipse restart or close and open of the project, uses the settings which are
 * persistently stored in the backing store. @see org.osgi.service.prefs.Preferences
 *
 * @since 6.0
 */
public abstract class CBuildConfiguration extends PlatformObject implements ICBuildConfiguration, ICBuildConfiguration2,
		IMarkerGenerator, IConsoleParser2, IElementChangedListener {

	private static final String LAUNCH_MODE = "cdt.launchMode"; //$NON-NLS-1$
	/**
	 * Property name used to store the {@link ILaunchTarget#getTypeId()} in the ICBuildConfiguration properties.
	 */
	private static final String LAUNCH_TARGET_TYPE_ID = "cdt.launchTarget.typeId"; //$NON-NLS-1$
	/**
	 * Property name used to store the {@link ILaunchTarget#getId()} in the ICBuildConfiguration properties.
	 */
	private static final String LAUNCH_TARGET_ID = "cdt.launchTarget.id"; //$NON-NLS-1$

	private static final String NEED_REFRESH = "cdt.needScannerRefresh"; //$NON-NLS-1$

	private static final List<String> DEFAULT_COMMAND = new ArrayList<>(0);

	private final Map<IResource, IToolChain> tcMap = new HashMap<>();

	private final String name;
	private final IBuildConfiguration config;
	private final IToolChain toolChain;
	private final String launchMode;
	private final ILaunchTarget launchTarget;

	private Object scannerInfoLock = new Object();

	private final Map<IResource, List<IScannerInfoChangeListener>> scannerInfoListeners = new HashMap<>();
	private ScannerInfoCache scannerInfoCache;

	private ICommandLauncher launcher;

	/**
	 * Use when a Core Build configuration already exists (eg was created using
	 * {@link #CBuildConfiguration(IBuildConfiguration, String, IToolChain, String, ILaunchTarget)}.
	 * The toolchain, launch mode and launch target are loaded from preferences.
	 *
	 * @param buildConfig Platform Build Configuration. Must not be null.
	 * @param cBuildConfigName Name to give the CMakeBuildConfiguration. Must not be null.
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> Toolchain is missing,</li>
	 * <li> Launch mode missing,</li>
	 * <li> Launch Target is missing.</li>
	 * </ul>
	 */
	protected CBuildConfiguration(IBuildConfiguration buildConfig, String cBuildConfigName) throws CoreException {
		this.config = Objects.requireNonNull(buildConfig, "buildConfig must not be null"); //$NON-NLS-1$
		this.name = Objects.requireNonNull(cBuildConfigName, "cBuildConfigName must not be null"); //$NON-NLS-1$

		/*
		 * Retrieve previously saved toolchain, launch mode and launch target from preferences.
		 * The STATUS_BUILD_CONFIG_NOT_VALID is used which causes CBuildConfigurationManager to quarantine this Build Configuration.
		 */
		Preferences settings = getSettings();
		String typeId = settings.get(TOOLCHAIN_TYPE, null);
		String id = settings.get(TOOLCHAIN_ID, null);
		IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
		IToolChain tc = toolChainManager.getToolChain(typeId, id);
		if (tc == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_BUILD_CONFIG_NOT_VALID,
							String.format(Messages.CBuildConfiguration_CBuildConfiguration_ToolchainMissing,
									buildConfig.getName(), typeId, id),
							null));
		}
		this.toolChain = tc;

		String launchMode = settings.get(LAUNCH_MODE, null);
		if (launchMode == null || launchMode.isBlank()) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_BUILD_CONFIG_NOT_VALID,
							String.format(Messages.CBuildConfiguration_CBuildConfiguration_LaunchModeMissing,
									buildConfig.getName(), launchMode),
							null));
		}
		this.launchMode = launchMode;

		String launchTargetTypeId = settings.get(LAUNCH_TARGET_TYPE_ID, null);
		String launchTargetId = settings.get(LAUNCH_TARGET_ID, null);
		ILaunchTargetManager launchTargetManager = CCorePlugin.getService(ILaunchTargetManager.class);
		ILaunchTarget target = launchTargetManager.getLaunchTarget(launchTargetTypeId, launchTargetId);
		if (target == null) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_BUILD_CONFIG_NOT_VALID,
							String.format(Messages.CBuildConfiguration_CBuildConfiguration_LaunchTargetMissing,
									buildConfig.getName(), launchTargetTypeId, launchTargetId),
							null));
		}
		this.launchTarget = target;

		CoreModel.getDefault().addElementChangedListener(this);
	}

	/**
	 * Use when a Core Build configuration is created for the first time. The toolchain, launch mode and launch target
	 * are saved to preferences. This allows the Core Build configuration to retrieved later (maybe another workbench
	 * session), using {@link #CBuildConfiguration(IBuildConfiguration, String)}
	 *
	 * @param buildConfig Platform Build Configuration. Must not be null.
	 * @param cBuildConfigName  Name to give the CMakeBuildConfiguration. Must not be null.
	 * @param toolChain Toolchain to associate with this CMakeBuildConfiguration. Must not be null.
	 * @param launchMode Launch mode (eg "debug") to associate with this CMakeBuildConfiguration. Must not be null.
	 * @param launchTarget Launch target to associate with this CMakeBuildConfiguration. Must not be null.
	 * @since 9.0
	 */
	protected CBuildConfiguration(IBuildConfiguration buildConfig, String cBuildConfigName, IToolChain toolChain,
			String launchMode, ILaunchTarget launchTarget) {
		this.config = Objects.requireNonNull(buildConfig, "buildConfig must not be null"); //$NON-NLS-1$
		this.name = Objects.requireNonNull(cBuildConfigName, "cBuildConfigName must not be null"); //$NON-NLS-1$
		this.toolChain = Objects.requireNonNull(toolChain, "toolChain must not be null"); //$NON-NLS-1$
		this.launchMode = Objects.requireNonNull(launchMode, "launchMode must not be null"); //$NON-NLS-1$
		this.launchTarget = Objects.requireNonNull(launchTarget, "launchTarget must not be null"); //$NON-NLS-1$

		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_TYPE, toolChain.getTypeId());
		settings.put(TOOLCHAIN_ID, toolChain.getId());
		settings.put(LAUNCH_MODE, launchMode);
		settings.put(LAUNCH_TARGET_TYPE_ID, launchTarget.getTypeId());
		settings.put(LAUNCH_TARGET_ID, launchTarget.getId());

		try {
			settings.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}

		CoreModel.getDefault().addElementChangedListener(this);
	}

	@Override
	public IBuildConfiguration getBuildConfiguration() {
		return config;
	}

	public String getName() {
		return name;
	}

	/**
	 * @since 6.2
	 */
	@Override
	public String getLaunchMode() {
		return launchMode;
	}

	@Override
	public ILaunchTarget getLaunchTarget() {
		return launchTarget;
	}

	public IProject getProject() {
		return config.getProject();
	}

	@Override
	public List<String> getBinaryParserIds() throws CoreException {
		return toolChain != null ? toolChain.getBinaryParserIds() : List.of(CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID);
	}

	/**
	 * Create build container if it doesn't exist yet.
	 *
	 * For a {@link StandardBuildConfiguration} the container can be the project or
	 * a folder. For all other Core Build configurations it is a folder.
	 *
	 * @param container Container where build is executed.
	 * @param monitor
	 * @throws CoreException
	 */
	/*
	 * Container creation is done at the start of the build process. Folder creation
	 * during the creation of a Core Build Configuration can lead to a loop back to
	 * CBuildConfigurationManager.getBuildConfiguration() that makes Eclipse freeze.
	 * This was seen with StandardBuildConfiguration when it created the build
	 * container. Folder.create got triggered via applyProperties() and
	 * getBuildContainer(). See https://github.com/eclipse-cdt/cdt/issues/424
	 */
	private void createBuildContainer(IContainer container, IProgressMonitor monitor) throws CoreException {
		if (!(container instanceof IProject) && !container.exists()) {
			IContainer parent = container.getParent();
			if (!(parent instanceof IProject) && !parent.exists()) {
				createBuildContainer(parent, monitor);
			}

			if (container instanceof IFolder) {
				((IFolder) container).create(IResource.FORCE | IResource.DERIVED, true, monitor);
			}
		}
	}

	/*
	 * Only the StandardBuildConfiguration has an option in the launch configuration
	 * to change the build container to the project root. For as long as the
	 * StandardBuildConfiguration is the only one there is no need to make the
	 * folder name a project property for all types. StandardBuildConfiguration has
	 * its own getBuildContainer() override.
	 */
	public IContainer getBuildContainer() throws CoreException {
		IProject project = getProject();
		IFolder buildRootFolder = project.getFolder("build"); //$NON-NLS-1$
		IFolder buildFolder = buildRootFolder.getFolder(name);

		return buildFolder;
	}

	@Override
	public URI getBuildDirectoryURI() throws CoreException {
		return getBuildContainer().getLocationURI();
	}

	/**
	 * Convenience method to convert getBuildDirectoryURI() to a Path
	 * <p>
	 * @apiNote This method used to be overridable, but it leads to unclear semantics for extenders.
	 * Extenders looking to change the build directory should only override {@link #getBuildDirectoryURI()}
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	public Path getBuildDirectory() throws CoreException {
		return Paths.get(getBuildDirectoryURI());
	}

	@Override
	public void setBuildEnvironment(Map<String, String> env) {
		CCorePlugin.getDefault().getBuildEnvironmentManager().setEnvironment(env, config, true);
	}

	/**
	 * @since 6.1
	 */
	@Override
	public IBinary[] getBuildOutput() throws CoreException {
		ICProject cproject = CoreModel.getDefault().create(config.getProject());
		IBinaryContainer binaries = cproject.getBinaryContainer();
		IPath outputPath = getBuildContainer().getFullPath();
		final IBinary[] outputs = getBuildOutput(binaries, outputPath);
		if (outputs.length > 0) {
			return outputs;
		}

		// Give the binary runner a kick and try again.
		BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(cproject);
		runner.start();
		runner.waitIfRunning();
		return getBuildOutput(binaries, outputPath);
	}

	private IBinary[] getBuildOutput(final IBinaryContainer binaries, final IPath outputPath) throws CoreException {
		return Arrays.stream(binaries.getBinaries()).filter(b -> b.isExecutable() && outputPath.isPrefixOf(b.getPath()))
				.toArray(IBinary[]::new);
	}

	public void setActive(IProgressMonitor monitor) throws CoreException {
		IProject project = config.getProject();
		if (config.equals(project.getActiveBuildConfig())) {
			// already set
			return;
		}

		CoreModel m = CoreModel.getDefault();
		synchronized (m) {
			IProjectDescription projectDesc = project.getDescription();
			IBuildConfiguration[] bconfigs = project.getBuildConfigs();
			Set<String> names = new LinkedHashSet<>();
			for (IBuildConfiguration bconfig : bconfigs) {
				names.add(bconfig.getName());
			}
			// must add default config name as it may not be in build config list
			names.add(IBuildConfiguration.DEFAULT_CONFIG_NAME);
			// ensure active config is last in list so clean build will clean
			// active config last and this will be left in build console for user to see
			names.remove(config.getName());
			names.add(config.getName());

			projectDesc.setBuildConfigs(names.toArray(new String[0]));
			projectDesc.setActiveBuildConfig(config.getName());
			project.setDescription(projectDesc, monitor);
		}
	}

	protected Preferences getSettings() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config") //$NON-NLS-1$
				.node(getProject().getName()).node(config.getName());
	}

	@Override
	public IToolChain getToolChain() throws CoreException {
		return toolChain;
	}

	/**
	 * Get the toolchain based on the compiler found in the command line.
	 * Sub-classes can override this method to set their own method for
	 * getting the toolchain.
	 * @since 8.0
	 *
	 */
	protected IToolChain getToolChain(List<String> command) throws CoreException {
		return getToolChain();
	}

	/**
	 * Get the toolchain that was used to compile this resource.
	 *
	 * @param resource
	 * @return
	 * @throws CoreException
	 */
	private IToolChain getToolChain(IResource resource) throws CoreException {
		IToolChain tc = tcMap.get(resource);
		if (tc == null)
			return getToolChain();
		else
			return tc;
	}

	@Override
	public IEnvironmentVariable getVariable(String name) {
		IEnvironmentVariable[] vars = getVariables();
		if (vars != null) {
			for (IEnvironmentVariable var : vars) {
				if (var.getName().equals(name)) {
					return var;
				}
			}
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables() {
		// by default, none
		return null;
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		addMarker(new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar, null));
	}

	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		try {
			IProject project = config.getProject();
			IResource markerResource = problemMarkerInfo.file;
			if (markerResource == null) {
				markerResource = project;
			}
			String externalLocation = null;
			if (problemMarkerInfo.externalPath != null && !problemMarkerInfo.externalPath.isEmpty()) {
				externalLocation = problemMarkerInfo.externalPath.toOSString();
			}

			// Try to find matching markers and don't put in duplicates
			IMarker[] markers = markerResource.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true,
					IResource.DEPTH_ONE);
			for (IMarker m : markers) {
				int line = m.getAttribute(IMarker.LINE_NUMBER, -1);
				int sev = m.getAttribute(IMarker.SEVERITY, -1);
				String msg = (String) m.getAttribute(IMarker.MESSAGE);
				if (line == problemMarkerInfo.lineNumber && sev == mapMarkerSeverity(problemMarkerInfo.severity)
						&& msg.equals(problemMarkerInfo.description)) {
					String extloc = (String) m.getAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION);
					if (extloc == externalLocation || (extloc != null && extloc.equals(externalLocation))) {
						if (project == null || project.equals(markerResource.getProject())) {
							return;
						}
						String source = (String) m.getAttribute(IMarker.SOURCE_ID);
						if (project.getName().equals(source)) {
							return;
						}
					}
				}
			}

			String type = problemMarkerInfo.getType();
			if (type == null) {
				type = ICModelMarker.C_MODEL_PROBLEM_MARKER;
			}

			IMarker marker = markerResource.createMarker(type);
			marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
			marker.setAttribute(IMarker.SEVERITY, mapMarkerSeverity(problemMarkerInfo.severity));
			marker.setAttribute(IMarker.LINE_NUMBER, problemMarkerInfo.lineNumber);
			marker.setAttribute(IMarker.CHAR_START, problemMarkerInfo.startChar);
			marker.setAttribute(IMarker.CHAR_END, problemMarkerInfo.endChar);
			if (problemMarkerInfo.variableName != null) {
				marker.setAttribute(ICModelMarker.C_MODEL_MARKER_VARIABLE, problemMarkerInfo.variableName);
			}
			if (externalLocation != null) {
				URI uri = URIUtil.toURI(externalLocation);
				if (uri.getScheme() != null) {
					marker.setAttribute(ICModelMarker.C_MODEL_MARKER_EXTERNAL_LOCATION, externalLocation);
					String locationText = String.format(Messages.CBuildConfiguration_Location,
							problemMarkerInfo.lineNumber, externalLocation);
					marker.setAttribute(IMarker.LOCATION, locationText);
				}
			} else if (problemMarkerInfo.lineNumber == 0) {
				marker.setAttribute(IMarker.LOCATION, " "); //$NON-NLS-1$
			}
			// Set source attribute only if the marker is being set to a file
			// from different project
			if (project != null && !project.equals(markerResource.getProject())) {
				marker.setAttribute(IMarker.SOURCE_ID, project.getName());
			}

			// Add all other client defined attributes.
			Map<String, String> attributes = problemMarkerInfo.getAttributes();
			if (attributes != null) {
				for (Entry<String, String> entry : attributes.entrySet()) {
					marker.setAttribute(entry.getKey(), entry.getValue());
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}

	private int mapMarkerSeverity(int severity) {
		switch (severity) {
		case SEVERITY_ERROR_BUILD:
		case SEVERITY_ERROR_RESOURCE:
			return IMarker.SEVERITY_ERROR;
		case SEVERITY_INFO:
			return IMarker.SEVERITY_INFO;
		case SEVERITY_WARNING:
			return IMarker.SEVERITY_WARNING;
		}
		return IMarker.SEVERITY_ERROR;
	}

	protected Path findCommand(String command) {
		try {
			Path cmdPath = Paths.get(command);
			if (cmdPath.isAbsolute()) {
				return cmdPath;
			}
			Properties environmentVariables = EnvironmentReader.getEnvVars();
			Map<String, String> env = new HashMap<>();
			for (String key : environmentVariables.stringPropertyNames()) {
				String value = environmentVariables.getProperty(key);
				env.put(key, value);
			}

			setBuildEnvironment(env);

			String pathStr = env.get("PATH"); //$NON-NLS-1$
			if (pathStr == null) {
				return null; // no idea
			}
			String[] path = pathStr.split(File.pathSeparator);
			for (String dir : path) {
				Path commandPath = Paths.get(dir, command);
				if (Files.exists(commandPath) && Files.isRegularFile(commandPath)) {
					return commandPath;
				} else {
					if (Platform.getOS().equals(Platform.OS_WIN32)
							&& !(command.endsWith(".exe") || command.endsWith(".bat"))) { //$NON-NLS-1$ //$NON-NLS-2$
						commandPath = Paths.get(dir, command + ".exe"); //$NON-NLS-1$
						if (Files.exists(commandPath)) {
							return commandPath;
						}
					}
				}
			}
			IToolChain tc = getToolChain();
			if (tc instanceof IToolChain2) {
				// we may have a Container build...default to Path based on command
				return Paths.get(command);
			}
		} catch (InvalidPathException e) {
			// ignore
		} catch (CoreException e) {
			// ignore
		}
		return null;
	}

	/**
	 * @since 6.5
	 */
	public Process startBuildProcess(List<String> commands, IEnvironmentVariable[] envVars, IPath buildDirectory,
			IConsole console, IProgressMonitor monitor) throws IOException, CoreException {
		Process process = null;
		IToolChain tc = getToolChain();

		createBuildContainer(getBuildContainer(), monitor);

		if (tc instanceof IToolChain2) {
			process = ((IToolChain2) tc).startBuildProcess(this, commands, buildDirectory.toString(), envVars, console,
					monitor);
		} else {
			// verify command can be found locally on path
			Path commandPath = findCommand(commands.get(0));
			if (commandPath == null) {
				console.getErrorStream()
						.write(String.format(Messages.CBuildConfiguration_CommandNotFound, commands.get(0)));
				return null;
			}
			IPath cmd = new org.eclipse.core.runtime.Path(commandPath.toString());
			List<String> args = commands.subList(1, commands.size());

			// check if includes have been removed/refreshed and scanner info refresh is needed
			boolean needRefresh = CommandLauncherManager.getInstance().checkIfIncludesChanged(this);
			IToolChain t = getToolChain();
			if (t != null) {
				t.setProperty(NEED_REFRESH, Boolean.valueOf(needRefresh).toString());
			}

			// Generate environment block before launching process
			launcher = CommandLauncherManager.getInstance().getCommandLauncher(this);
			Properties envProps = launcher.getEnvironment();
			HashMap<String, String> environment = envProps.entrySet().stream()
					.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue()),
							(prev, next) -> next, HashMap::new));
			for (IEnvironmentVariable envVar : envVars) {
				environment.put(envVar.getName(), envVar.getValue());
			}
			setBuildEnvironment(environment);
			launcher.setProject(getProject());
			process = launcher.execute(cmd, args.toArray(new String[0]), BuildRunnerHelper.envMapToEnvp(environment),
					buildDirectory, monitor);
		}
		return process;
	}

	/**
	 * @return The exit code of the build process.
	 * @since 8.0
	 */
	protected int watchProcess(IConsole console, IProgressMonitor monitor) throws CoreException {
		assertLauncherNotNull(launcher);
		return launcher.waitAndRead(console.getOutputStream(), console.getErrorStream(), monitor);
	}

	/**
	 * @return The exit code of the build process.
	 * @since 8.0
	 */
	protected int watchProcess(IConsoleParser[] consoleParsers, IProgressMonitor monitor) throws CoreException {
		assertLauncherNotNull(launcher);
		ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(consoleParsers);
		return launcher.waitAndRead(sniffer.getOutputStream(), sniffer.getErrorStream(), monitor);
	}

	private void assertLauncherNotNull(ICommandLauncher launcher) {
		Assert.isNotNull(launcher, "Only processes launched with startBuildProcess can be watched."); //$NON-NLS-1$
	}

	private File getScannerInfoCacheFile() {
		return CCorePlugin.getDefault().getStateLocation().append("infoCache") //$NON-NLS-1$
				.append(getProject().getName()).append(name + ".json").toFile(); //$NON-NLS-1$
	}

	/**
	 * @since 6.1
	 */
	protected void loadScannerInfoCache() {
		synchronized (scannerInfoLock) {
			if (scannerInfoCache == null) {
				File cacheFile = getScannerInfoCacheFile();
				if (cacheFile.exists()) {
					try (FileReader reader = new FileReader(cacheFile)) {
						Gson gson = createGson();
						scannerInfoCache = gson.fromJson(reader, ScannerInfoCache.class);
					} catch (IOException e) {
						CCorePlugin.log(e);
					}
				}

				if (scannerInfoCache == null) {
					scannerInfoCache = new ScannerInfoCache();
				}
				scannerInfoCache.initCache();
			}
		}
	}

	private Gson createGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(IExtendedScannerInfo.class, new IExtendedScannerInfoDeserializer());
		gsonBuilder.registerTypeAdapter(ExtendedScannerInfo.class, new ExtendedScannerInfoSerializer());
		Gson gson = gsonBuilder.create();
		return gson;
	}

	/**
	 * @since 6.1
	 */
	protected synchronized void saveScannerInfoCache() {
		File cacheFile = getScannerInfoCacheFile();
		if (!cacheFile.getParentFile().exists()) {
			try {
				Files.createDirectories(cacheFile.getParentFile().toPath());
			} catch (IOException e) {
				CCorePlugin.log(e);
				return;
			}
		}

		try (FileWriter writer = new FileWriter(getScannerInfoCacheFile())) {
			Gson gson = createGson();
			synchronized (scannerInfoLock) {
				gson.toJson(scannerInfoCache, writer);
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	}

	/**
	 * @since 6.1
	 * @deprecated This method was never used in CDT.
	 */
	@Deprecated(forRemoval = true)
	protected ScannerInfoCache getScannerInfoCache() {
		return scannerInfoCache;
	}

	private IExtendedScannerInfo getBaseScannerInfo(IResource resource) throws CoreException {
		IPath resPath = resource.getFullPath();
		IIncludeEntry[] includeEntries = CoreModel.getIncludeEntries(resPath);
		String[] includes = new String[includeEntries.length];
		for (int i = 0; i < includeEntries.length; ++i) {
			includes[i] = includeEntries[i].getFullIncludePath().toOSString();
		}

		IIncludeFileEntry[] includeFileEntries = CoreModel.getIncludeFileEntries(resPath);
		String[] includeFiles = new String[includeFileEntries.length];
		for (int i = 0; i < includeFiles.length; ++i) {
			includeFiles[i] = includeFileEntries[i].getFullIncludeFilePath().toOSString();
		}

		IMacroEntry[] macros = CoreModel.getMacroEntries(resPath);
		Map<String, String> symbolMap = new HashMap<>();
		for (int i = 0; i < macros.length; ++i) {
			symbolMap.put(macros[i].getMacroName(), macros[i].getMacroValue());
		}

		IMacroFileEntry[] macroFileEntries = CoreModel.getMacroFileEntries(resPath);
		String[] macroFiles = new String[macroFileEntries.length];
		for (int i = 0; i < macroFiles.length; ++i) {
			macroFiles[i] = macroFileEntries[i].getFullMacroFilePath().toOSString();
		}
		return new ExtendedScannerInfo(symbolMap, includes, macroFiles, includeFiles);
	}

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		loadScannerInfoCache();
		IExtendedScannerInfo info = null;
		synchronized (scannerInfoLock) {
			info = scannerInfoCache.getScannerInfo(resource);
		}
		// Following is a kludge to fix Bug 579668 whereby sometimes a timing
		// bug occurs and scanner info for a project that specifies a container target
		// has not initialized the include paths correctly to point to copied includes
		// from the image target.  We check to see if org.eclipse.cdt.docker.launcher is
		// found in the include paths which is the .plugin directory where we copy headers
		// to in the .metadata folder.
		boolean needsFixing = false;
		if (info != null && info.getIncludePaths().length > 0 && toolChain != null
				&& toolChain.getId().startsWith("gcc-img-sha")) { //$NON-NLS-1$
			needsFixing = true;
			for (String includePath : info.getIncludePaths()) {
				if (includePath.contains("org.eclipse.cdt.docker.launcher")) { //$NON-NLS-1$
					needsFixing = false;
					break;
				}
			}
		}
		if (info == null || info.getIncludePaths().length == 0 || needsFixing) {
			ICElement celement = CCorePlugin.getDefault().getCoreModel().create(resource);
			if (celement instanceof ITranslationUnit) {
				try {
					ITranslationUnit tu = (ITranslationUnit) celement;
					IToolChain tc = getToolChain(resource);
					info = tc.getDefaultScannerInfo(getBuildConfiguration(), getBaseScannerInfo(resource),
							tu.getLanguage(), getBuildDirectoryURI());
					synchronized (scannerInfoLock) {
						scannerInfoCache.addScannerInfo(DEFAULT_COMMAND, info, resource);
					}
					saveScannerInfoCache();
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
				}
			}
		}
		return info;
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		// check if the path entries changed in the project and clear the cache if so
		processElementDelta(event.getDelta());
	}

	private void processElementDelta(ICElementDelta delta) {
		if (delta == null) {
			return;
		}

		int flags = delta.getFlags();
		int kind = delta.getKind();
		if (kind == ICElementDelta.CHANGED) {
			if ((flags
					& (ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE | ICElementDelta.F_CHANGED_PATHENTRY_MACRO)) != 0) {
				IResource resource = delta.getElement().getResource();
				if (resource.getProject().equals(getProject())) {
					loadScannerInfoCache();
					synchronized (scannerInfoLock) {
						if (scannerInfoCache.hasResource(DEFAULT_COMMAND, resource)) {
							scannerInfoCache.removeResource(resource);
						} else {
							// Clear the whole command and exit the delta
							scannerInfoCache.removeCommand(DEFAULT_COMMAND);
							return;
						}
					}
				}
			}
		}

		ICElementDelta[] affectedChildren = delta.getAffectedChildren();
		for (int i = 0; i < affectedChildren.length; i++) {
			processElementDelta(affectedChildren[i]);
		}
	}

	/**
	 * Parse a string containing compile options into individual argument strings.
	 *
	 * @param argString - String to parse
	 * @return List of arg Strings
	 */
	private List<String> stripArgs(String argString) {
		String[] args = CommandLineUtil.argumentsToArray(argString);
		return new ArrayList<>(Arrays.asList(args));
	}

	private boolean infoChanged = false;

	@Override
	public boolean processLine(String line) {
		// Split line into args, taking into account quotes
		List<String> command = stripArgs(line);

		// Make sure it's a compile command
		String[] compileCommands;
		IToolChain tc;
		try {
			tc = getToolChain(command);
			compileCommands = tc.getCompileCommands();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
		boolean found = false;
		loop: for (String arg : command) {
			// TODO we should really ask the toolchain, not all args start with '-'
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// option found, missed our command
				return false;
			}

			for (String cc : compileCommands) {
				if (arg.endsWith(cc) && (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
					found = true;
					break loop;
				}
			}

			if (Platform.getOS().equals(Platform.OS_WIN32) && !arg.endsWith(".exe")) { //$NON-NLS-1$
				// Try with exe
				arg = arg + ".exe"; //$NON-NLS-1$
				for (String cc : compileCommands) {
					if (arg.endsWith(cc) && (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
						found = true;
						break loop;
					}
				}
			}
		}

		if (!found) {
			return false;
		}

		try {
			IResource[] resources = tc.getResourcesFromCommand(command, getBuildDirectoryURI());
			if (resources != null && resources.length > 0) {
				List<String> commandStrings = tc.stripCommand(command, resources);

				boolean needScannerRefresh = false;

				String needRefresh = tc.getProperty(NEED_REFRESH);
				if ("true".equals(needRefresh)) { //$NON-NLS-1$
					needScannerRefresh = true;
				}

				for (IResource resource : resources) {
					tcMap.put(resource, tc);
					loadScannerInfoCache();
					boolean hasCommand = true;
					synchronized (scannerInfoLock) {
						if (scannerInfoCache.hasCommand(commandStrings)) {
							IExtendedScannerInfo info = scannerInfoCache.getScannerInfo(commandStrings);
							if (info.getIncludePaths().length == 0) {
								needScannerRefresh = true;
							}
							if (!scannerInfoCache.hasResource(commandStrings, resource)) {
								scannerInfoCache.addResource(commandStrings, resource);
								infoChanged = true;
							}
						} else {
							hasCommand = false;
						}
					}
					if (!hasCommand || needScannerRefresh) {
						Path commandPath = findCommand(command.get(0));
						if (commandPath != null) {
							command.set(0, commandPath.toString());
							IExtendedScannerInfo info = tc.getScannerInfo(getBuildConfiguration(), command, null,
									resource, getBuildDirectoryURI());
							synchronized (scannerInfoLock) {
								scannerInfoCache.addScannerInfo(commandStrings, info, resource);
								infoChanged = true;
							}
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	private class ScannerInfoJob extends Job {
		private IToolChain toolchain;
		private List<String> command;
		private List<String> commandStrings;
		private IResource resource;
		private URI buildDirectoryURI;

		public ScannerInfoJob(String msg, IToolChain toolchain, List<String> command, IResource resource,
				URI buildDirectoryURI, List<String> commandStrings) {
			super(msg);
			this.toolchain = toolchain;
			this.command = command;
			this.commandStrings = commandStrings;
			this.resource = resource;
			this.buildDirectoryURI = buildDirectoryURI;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IExtendedScannerInfo info = toolchain.getScannerInfo(getBuildConfiguration(), command, null, resource,
					buildDirectoryURI);
			synchronized (scannerInfoLock) {
				scannerInfoCache.addScannerInfo(commandStrings, info, resource);
				infoChanged = true;
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * Process a compile line for Scanner info in a separate job
	 *
	 * @param line - line to process
	 * @param jobsArray - array of Jobs to keep track of open scanner info jobs
	 * @return - true if line processed, false otherwise
	 *
	 * @since 6.5
	 */
	@Override
	public boolean processLine(String line, List<Job> jobsArray) {
		// Split line into args, taking into account quotes
		List<String> command = stripArgs(line);

		// Make sure it's a compile command
		String[] compileCommands;
		IToolChain tc;
		try {
			tc = getToolChain(command);
			compileCommands = tc.getCompileCommands();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
		boolean found = false;
		loop: for (String arg : command) {
			// TODO we should really ask the toolchain, not all args start with '-'
			if (arg.startsWith("-")) { //$NON-NLS-1$
				// option found, missed our command
				return false;
			}

			for (String cc : compileCommands) {
				if (arg.endsWith(cc) && (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
					found = true;
					break loop;
				}
			}

			if (Platform.getOS().equals(Platform.OS_WIN32) && !arg.endsWith(".exe")) { //$NON-NLS-1$
				// Try with exe
				arg = arg + ".exe"; //$NON-NLS-1$
				for (String cc : compileCommands) {
					if (arg.endsWith(cc) && (arg.equals(cc) || arg.endsWith("/" + cc) || arg.endsWith("\\" + cc))) { //$NON-NLS-1$ //$NON-NLS-2$
						found = true;
						break loop;
					}
				}
			}
		}

		if (!found) {
			return false;
		}

		try {
			IResource[] resources = tc.getResourcesFromCommand(command, getBuildDirectoryURI());
			if (resources != null && resources.length > 0) {
				List<String> commandStrings = tc.stripCommand(command, resources);

				boolean needScannerRefresh = false;

				String needRefresh = tc.getProperty(NEED_REFRESH);
				if ("true".equals(needRefresh)) { //$NON-NLS-1$
					needScannerRefresh = true;
				}

				for (IResource resource : resources) {
					tcMap.put(resource, tc);
					loadScannerInfoCache();
					boolean hasCommand = true;
					synchronized (scannerInfoLock) {
						if (scannerInfoCache.hasCommand(commandStrings)) {
							IExtendedScannerInfo info = scannerInfoCache.getScannerInfo(commandStrings);
							if (info.getIncludePaths().length == 0) {
								needScannerRefresh = true;
							}
							if (!scannerInfoCache.hasResource(commandStrings, resource)) {
								scannerInfoCache.addResource(commandStrings, resource);
								infoChanged = true;
							}
						} else {
							hasCommand = false;
						}
					}
					if (!hasCommand || needScannerRefresh) {
						Path commandPath = findCommand(command.get(0));
						if (commandPath != null) {
							command.set(0, commandPath.toString());
							Job job = new ScannerInfoJob(
									String.format(Messages.CBuildConfiguration_RunningScannerInfo, resource), tc,
									command, resource, getBuildDirectoryURI(), commandStrings);
							job.schedule();
							jobsArray.add(job);
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	/**
	 * @since 6.5
	 */
	@Override
	public void setActive() {
		try {
			refreshScannerInfo();
		} catch (CoreException e) {
			// do nothing
		}
	}

	/**
	 * @since 8.3
	 * @param launcher - launcher to set
	 */
	public void setLauncher(ICommandLauncher launcher) {
		this.launcher = launcher;
	}

	/**
	 * @since 6.5
	 * @throws CoreException
	 */
	protected void refreshScannerInfo() throws CoreException {
		CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(getProject()));
		infoChanged = false;
	}

	@Override
	public void shutdown() {
		// TODO persist changes

		// Trigger a reindex if anything changed
		// TODO be more surgical
		if (infoChanged) {
			saveScannerInfoCache();
			CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(getProject()));
			infoChanged = false;
		}
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		List<IScannerInfoChangeListener> listeners = scannerInfoListeners.get(resource);
		if (listeners == null) {
			listeners = new ArrayList<>();
			scannerInfoListeners.put(resource, listeners);
		}
		listeners.add(listener);
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		List<IScannerInfoChangeListener> listeners = scannerInfoListeners.get(resource);
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty()) {
				scannerInfoListeners.remove(resource);
			}
		}
	}

	/**
	 * Takes a command path and returns either the command path itself if it is
	 * absolute or the path to the command as it appears in the PATH environment
	 * variable. Also adjusts the command for Windows's .exe extension.
	 *
	 * @since 6.1
	 */
	public static Path getCommandFromPath(Path command) {
		if (command.isAbsolute()) {
			return command;
		}

		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			if (!command.toString().endsWith(".exe")) { //$NON-NLS-1$
				command = Paths.get(command.toString() + ".exe"); //$NON-NLS-1$
			}
		}

		// Look for it in the path environment var
		String path = System.getenv("PATH"); //$NON-NLS-1$
		for (String entry : path.split(File.pathSeparator)) {
			Path entryPath = Paths.get(entry);
			Path cmdPath = entryPath.resolve(command);
			if (Files.isExecutable(cmdPath)) {
				return cmdPath;
			}
		}

		return null;
	}

	/**
	 * @since 6.2
	 */
	@Override
	public boolean setProperties(Map<String, String> properties) {
		Preferences settings = getSettings();
		for (Entry<String, String> entry : properties.entrySet()) {
			settings.put(entry.getKey(), entry.getValue());
		}
		return true;
	}

	/**
	 * @since 6.2
	 */
	@Override
	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<>();
		Preferences settings = getSettings();
		try {
			for (String key : settings.keys()) {
				String value = settings.get(key, null);
				if (value != null) {
					properties.put(key, value);
				}
			}
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
		return properties;
	}

	/**
	 * @since 6.4
	 */
	@Override
	public String getProperty(String name) {
		return getSettings().get(name, null);
	}

	/**
	 * @since 6.4
	 */
	@Override
	public void setProperty(String name, String value) {
		Preferences settings = getSettings();
		settings.put(name, value);
	}

	@Override
	public void removeProperty(String name) {
		Preferences settings = getSettings();
		settings.remove(name);
	}

	/**
	 * @since 6.2
	 */
	@Override
	public Map<String, String> getDefaultProperties() {
		return new HashMap<>();
	}

}
