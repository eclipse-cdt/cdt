package org.eclipse.cdt.docker.launcher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.build.ICBuildCommandLauncher;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.ProcessClosure;
import org.eclipse.cdt.internal.docker.launcher.Messages;
import org.eclipse.cdt.internal.docker.launcher.PreferenceConstants;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.eclipse.linuxtools.internal.docker.ui.launch.ContainerCommandProcess;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.Preferences;

@SuppressWarnings("restriction")
public class ContainerCommandLauncher implements ICommandLauncher, ICBuildCommandLauncher, IErrorMessageHolder {

	public final static String CONTAINER_BUILD_ENABLED = DockerLaunchUIPlugin.PLUGIN_ID
			+ ".containerbuild.property.enablement"; //$NON-NLS-1$
	public final static String CONNECTION_ID = DockerLaunchUIPlugin.PLUGIN_ID + ".containerbuild.property.connection"; //$NON-NLS-1$
	public final static String IMAGE_ID = DockerLaunchUIPlugin.PLUGIN_ID + ".containerbuild.property.image"; //$NON-NLS-1$
	public final static String VOLUMES_ID = DockerLaunchUIPlugin.PLUGIN_ID + ".containerbuild.property.volumes"; //$NON-NLS-1$
	public final static String SELECTED_VOLUMES_ID = DockerLaunchUIPlugin.PLUGIN_ID
			+ ".containerbuild.property.selectedvolumes"; //$NON-NLS-1$

	public final static String VOLUME_SEPARATOR_REGEX = "[|]"; //$NON-NLS-1$

	private IProject fProject;
	private Process fProcess;
	private boolean fShowCommand;
	private String fErrorMessage;
	private Properties fEnvironment;
	private ICBuildConfiguration fBuildConfig;

	private String[] commandArgs;
	private String fImageName = ""; //$NON-NLS-1$

	public final static int COMMAND_CANCELED = ICommandLauncher.COMMAND_CANCELED;
	public final static int ILLEGAL_COMMAND = ICommandLauncher.ILLEGAL_COMMAND;
	public final static int OK = ICommandLauncher.OK;

	private static final String NEWLINE = System.getProperty("line.separator", //$NON-NLS-1$
			"\n"); //$NON-NLS-1$

	/**
	 * The number of milliseconds to pause between polling.
	 */
	protected static final long DELAY = 50L;

	@Override
	public void setProject(IProject project) {
		this.fProject = project;
	}

	@Override
	public IProject getProject() {
		return fProject;
	}

	@Override
	public void setBuildConfiguration(ICBuildConfiguration config) {
		this.fBuildConfig = config;
		if (fProject == null) {
			try {
				fProject = config.getBuildConfiguration().getProject();
			} catch (CoreException e) {
				// ignore
			}
		}
	}

	@Override
	public ICBuildConfiguration getBuildConfiguration() {
		return fBuildConfig;
	}

	@SuppressWarnings("unused")
	private String getImageName() {
		return fImageName;
	}

	private void setImageName(String imageName) {
		fImageName = imageName;
	}

	@Override
	public void showCommand(boolean show) {
		this.fShowCommand = show;
	}

	@Override
	public String getErrorMessage() {
		return fErrorMessage;
	}

	@Override
	public void setErrorMessage(String error) {
		fErrorMessage = error;
	}

	@Override
	public String[] getCommandArgs() {
		return commandArgs;
	}

	@Override
	public Properties getEnvironment() {
		return fEnvironment;
	}

	@Override
	public String getCommandLine() {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isSystemDir(IPath p) {
		String firstSegment = p.segment(0);
		if (firstSegment.equals("usr") || firstSegment.equals("opt") //$NON-NLS-1$ //$NON-NLS-2$
				|| firstSegment.equals("bin") || firstSegment.equals("etc") //$NON-NLS-1$ //$NON-NLS-2$
				|| firstSegment.equals("sbin")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	@Override
	public Process execute(IPath commandPath, String[] args, String[] env, IPath workingDirectory,
			IProgressMonitor monitor) throws CoreException {

		HashMap<String, String> labels = new HashMap<>();
		labels.put("org.eclipse.cdt.container-command", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String projectName = fProject.getName();
		labels.put("org.eclipse.cdt.project-name", projectName); //$NON-NLS-1$

		List<String> additionalDirs = new ArrayList<>();

		//
		IPath projectLocation = fProject.getLocation();
		String projectPath = projectLocation.toPortableString();
		if (projectLocation.getDevice() != null) {
			projectPath = "/" + projectPath.replace(':', '/'); //$NON-NLS-1$
		}
		additionalDirs.add(projectPath);

		ArrayList<String> commandSegments = new ArrayList<>();

		List<String> cmdList = new ArrayList<>();

		String commandString = commandPath.toPortableString();
		if (commandPath.getDevice() != null) {
			commandString = "/" + commandString.replace(':', '/'); //$NON-NLS-1$
		}
		cmdList.add(commandString);
		commandSegments.add(commandString);
		for (String arg : args) {
			String realArg = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(arg);
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				// check if file exists and if so, add an additional directory
				IPath p = new Path(realArg);
				if (p.isValidPath(realArg) && p.getDevice() != null && !isSystemDir(p)) {
					File f = p.toFile();
					String modifiedArg = realArg;
					// if the directory of the arg as a file exists, we mount it
					// and modify the argument to be unix-style
					if (f.isFile() || f.isDirectory()) {
						f = f.getParentFile();
						modifiedArg = "/" //$NON-NLS-1$
								+ p.toPortableString().replace(':', '/');
						p = p.removeLastSegments(1);
					}
					if (f != null && f.exists()) {
						additionalDirs.add("/" + p.toPortableString().replace(':', '/')); //$NON-NLS-1$
						realArg = modifiedArg;
					}
				}
			} else if (realArg.startsWith("/")) { //$NON-NLS-1$
				// check if file directory exists and if so, add an additional
				// directory
				IPath p = new Path(realArg);
				if (p.isValidPath(realArg) && !isSystemDir(p)) {
					File f = p.toFile();
					if (f.isFile()) {
						f = f.getParentFile();
					}
					if (f != null && f.exists()) {
						additionalDirs.add(f.getAbsolutePath());
					}
				}
			}
			cmdList.add(realArg);
			commandSegments.add(realArg);
		}

		commandArgs = commandSegments.toArray(new String[0]);

		IProject[] referencedProjects = fProject.getReferencedProjects();
		for (IProject referencedProject : referencedProjects) {
			String referencedProjectPath = referencedProject.getLocation().toPortableString();
			if (referencedProject.getLocation().getDevice() != null) {
				referencedProjectPath = "/" //$NON-NLS-1$
						+ referencedProjectPath.replace(':', '/');
			}
			additionalDirs.add(referencedProjectPath);
		}

		String workingDir = workingDirectory.makeAbsolute().toPortableString();
		if (workingDirectory.toPortableString().equals(".")) { //$NON-NLS-1$
			workingDir = "/tmp"; //$NON-NLS-1$
		} else if (workingDirectory.getDevice() != null) {
			workingDir = "/" + workingDir.replace(':', '/'); //$NON-NLS-1$
		}
		parseEnvironment(env);
		Map<String, String> origEnv = null;

		boolean supportStdin = false;

		boolean privilegedMode = false;

		ContainerLauncher launcher = new ContainerLauncher();

		Preferences prefs = InstanceScope.INSTANCE.getNode(DockerLaunchUIPlugin.PLUGIN_ID);

		boolean keepContainer = prefs.getBoolean(PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);

		ICBuildConfiguration buildCfg = getBuildConfiguration();
		String selectedVolumeString = null;
		String connectionName = null;
		String imageName = null;
		if (buildCfg != null) {
			IToolChain toolChain = buildCfg.getToolChain();
			selectedVolumeString = toolChain.getProperty(SELECTED_VOLUMES_ID);
			connectionName = toolChain.getProperty(IContainerLaunchTarget.ATTR_CONNECTION_URI);
			imageName = toolChain.getProperty(IContainerLaunchTarget.ATTR_IMAGE_ID);
		} else {
			ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(fProject)
					.getActiveConfiguration();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
			if (cfg == null) {
				return null;
			}
			IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
			selectedVolumeString = props.getProperty(SELECTED_VOLUMES_ID);
			connectionName = props.getProperty(ContainerCommandLauncher.CONNECTION_ID);
			imageName = props.getProperty(ContainerCommandLauncher.IMAGE_ID);
		}

		// Add any specified volumes to additional dir list
		if (selectedVolumeString != null && !selectedVolumeString.isEmpty()) {
			String[] selectedVolumes = selectedVolumeString.split(VOLUME_SEPARATOR_REGEX);
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				for (String selectedVolume : selectedVolumes) {
					IPath path = new Path(selectedVolume);
					String selectedPath = path.toPortableString();
					if (path.getDevice() != null) {
						selectedPath = "/" + selectedPath.replace(':', '/'); //$NON-NLS-1$
					}
					additionalDirs.add(selectedPath);
				}
			} else {
				additionalDirs.addAll(Arrays.asList(selectedVolumes));
			}
		}

		if (connectionName == null) {
			return null;
		}
		if (imageName == null) {
			return null;
		}
		setImageName(imageName);

		fProcess = launcher.runCommand(connectionName, imageName, fProject, this, cmdList, workingDir, additionalDirs,
				origEnv, fEnvironment, supportStdin, privilegedMode, labels, keepContainer);

		return fProcess;
	}

	private String calculateImageName() {
		ICBuildConfiguration buildCfg = getBuildConfiguration();
		String imageName = ""; //$NON-NLS-1$

		if (buildCfg != null) {
			IToolChain toolChain;
			try {
				toolChain = buildCfg.getToolChain();
			} catch (CoreException e) {
				return imageName;
			}
			imageName = toolChain.getProperty(IContainerLaunchTarget.ATTR_IMAGE_ID);
		} else {
			ICConfigurationDescription cfgd = CoreModel.getDefault().getProjectDescription(fProject)
					.getActiveConfiguration();
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
			if (cfg == null) {
				return imageName;
			}
			IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
			imageName = props.getProperty(ContainerCommandLauncher.IMAGE_ID);
		}
		return imageName;
	}

	/**
	 * Parse array of "ENV=value" pairs to Properties.
	 */
	private void parseEnvironment(String[] env) {
		fEnvironment = null;
		if (env != null) {
			fEnvironment = new Properties();
			for (String envStr : env) {
				// Split "ENV=value" and put in Properties
				int pos = envStr.indexOf('='); // $NON-NLS-1$
				if (pos < 0)
					pos = envStr.length();
				String key = envStr.substring(0, pos);
				String value = envStr.substring(pos + 1);
				fEnvironment.put(key, value);
			}
		}
	}

	@Override
	public int waitAndRead(OutputStream out, OutputStream err) {
		printImageHeader(out);

		if (fShowCommand) {
			printCommandLine(out);
		}

		if (fProcess == null) {
			return ILLEGAL_COMMAND;
		}
		ProcessClosure closure = new ProcessClosure(fProcess, out, err);
		closure.runBlocking(); // a blocking call
		return OK;
	}

	@Override
	public int waitAndRead(OutputStream output, OutputStream err, IProgressMonitor monitor) {
		printImageHeader(output);

		if (fShowCommand) {
			printCommandLine(output);
		}

		if (fProcess == null) {
			return ILLEGAL_COMMAND;
		}

		ProcessClosure closure = new ProcessClosure(fProcess, output, err);
		closure.runNonBlocking();
		Runnable watchProcess = () -> {
			try {
				fProcess.waitFor();
			} catch (InterruptedException e) {
				// ignore
			}
			closure.terminate();
		};
		Thread t = new Thread(watchProcess);
		t.start();
		while (!monitor.isCanceled() && closure.isAlive()) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException ie) {
				break;
			}
		}
		try {
			t.join(500);
		} catch (InterruptedException e1) {
			// ignore
		}
		int state = OK;

		// Operation canceled by the user, terminate abnormally.
		if (monitor.isCanceled()) {
			closure.terminate();
			state = COMMAND_CANCELED;
			setErrorMessage(Messages.CommandLauncher_CommandCancelled);
		}
		try {
			fProcess.waitFor();
		} catch (InterruptedException e) {
			// ignore
		}

		monitor.done();
		return state;
	}

	protected void printImageHeader(OutputStream os) {
		if (os != null) {
			try {
				os.write(NLS.bind(Messages.ContainerCommandLauncher_image_msg,
						((ContainerCommandProcess) fProcess).getImage()).getBytes());
				os.write(NEWLINE.getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	@Override
	public String getConsoleHeader() {
		return NLS.bind(Messages.ContainerCommandLauncher_image_msg, calculateImageName()) + NEWLINE;
	}

	protected void printCommandLine(OutputStream os) {
		if (os != null) {
			try {
				os.write(getCommandLineQuoted(getCommandArgs(), true).getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore;
			}
		}
	}

	@SuppressWarnings("nls")
	private String getCommandLineQuoted(String[] commandArgs, boolean quote) {
		StringBuilder buf = new StringBuilder();
		if (commandArgs != null) {
			for (String commandArg : commandArgs) {
				if (quote && (commandArg.contains(" ") || commandArg.contains("\"") || commandArg.contains("\\"))) {
					commandArg = '"' + commandArg.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + '"';
				}
				buf.append(commandArg);
				buf.append(' ');
			}
			buf.append(NEWLINE);
		}
		return buf.toString();
	}

	protected String getCommandLine(String[] commandArgs) {
		return getCommandLineQuoted(commandArgs, false);
	}

}
