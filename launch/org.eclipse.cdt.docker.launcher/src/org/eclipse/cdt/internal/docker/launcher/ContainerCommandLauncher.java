package org.eclipse.cdt.internal.docker.launcher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.internal.core.ProcessClosure;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IErrorMessageHolder;
import org.eclipse.linuxtools.internal.docker.ui.launch.ContainerCommandProcess;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.Preferences;

@SuppressWarnings("restriction")
public class ContainerCommandLauncher
		implements ICommandLauncher, IErrorMessageHolder {

	public final static String CONTAINER_BUILD_ENABLED = "org.eclipse.cdt.docker.launcher.containerbuild.property.enablement"; // $NON-NLS-0$
	public final static String CONNECTION_ID = "org.eclipse.cdt.docker.launcher.containerbuild.property.connection"; // $NON-NLS-0$
	public final static String IMAGE_ID = "org.eclipse.cdt.docker.launcher.containerbuild.property.image"; // $NON-NLS-0$

	private IProject fProject;
	private Process fProcess;
	private boolean fShowCommand;
	private String fErrorMessage;
	private Properties fEnvironment;

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

	@Override
	public Process execute(IPath commandPath, String[] args, String[] env,
			IPath workingDirectory, IProgressMonitor monitor)
			throws CoreException {

		HashMap<String, String> labels = new HashMap<>();
		labels.put("org.eclipse.cdt.container-command", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String projectName = fProject.getName();
		labels.put("org.eclipse.cdt.project-name", projectName); //$NON-NLS-1$

		List<String> additionalDirs = new ArrayList<>();

		//
		additionalDirs.add(fProject.getLocation().toPortableString());

		ArrayList<String> commandSegments = new ArrayList<>();

		StringBuilder b = new StringBuilder();
		b.append(commandPath.toString().trim());
		commandSegments.add(commandPath.toString().trim());
		for (String arg : args) {
			b.append(" "); //$NON-NLS-1$
			String realArg = VariablesPlugin.getDefault()
					.getStringVariableManager().performStringSubstitution(arg);
			b.append(realArg);
			if (realArg.startsWith("/")) { //$NON-NLS-1$
				// check if file exists and if so, add an additional directory
				IPath p = new Path(realArg);
				if (p.isValidPath(realArg)) {
					p = p.makeAbsolute();
					File f = p.toFile();
					if (f.exists()) {
						if (f.isFile()) {
							p = p.removeLastSegments(1);
						}
						additionalDirs.add(p.toPortableString());
					}
				}
			}
			commandSegments.add(realArg);
		}
		
		commandArgs = commandSegments.toArray(new String[0]);

		String commandDir = commandPath.removeLastSegments(1).toString();
		if (commandDir.isEmpty()) {
			commandDir = null;
		}

		IProject[] referencedProjects = fProject.getReferencedProjects();
		for (IProject referencedProject : referencedProjects) {
			additionalDirs
					.add(referencedProject.getLocation().toPortableString());
		}

		String command = b.toString();

		String workingDir = workingDirectory.toPortableString();
		parseEnvironment(env);
		Map<String, String> origEnv = null;

		boolean supportStdin = false;

		boolean privilegedMode = false;

		ContainerLauncher launcher = new ContainerLauncher();

		Preferences prefs = InstanceScope.INSTANCE
				.getNode(DockerLaunchUIPlugin.PLUGIN_ID);

		boolean keepContainer = prefs.getBoolean(
				PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);

		ICConfigurationDescription cfgd = CoreModel.getDefault()
				.getProjectDescription(fProject).getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager
				.getConfigurationForDescription(cfgd);
		if (cfg == null) {
			return null;
		}
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
		String connectionName = props
				.getProperty(ContainerCommandLauncher.CONNECTION_ID);
		if (connectionName == null) {
			return null;
		}
		String imageName = props
				.getProperty(ContainerCommandLauncher.IMAGE_ID);
		if (imageName == null) {
			return null;
		}
		setImageName(imageName);

		fProcess = launcher.runCommand(connectionName, imageName, fProject,
				this,
				command,
				commandDir,
				workingDir,
				additionalDirs,
				origEnv, fEnvironment, supportStdin, privilegedMode,
				labels, keepContainer);

		return fProcess;
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
	public int waitAndRead(OutputStream output, OutputStream err,
			IProgressMonitor monitor) {
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
				os.write(NLS
						.bind(Messages.ContainerCommandLauncher_image_msg,
								((ContainerCommandProcess) fProcess).getImage())
						.getBytes());
				os.write(NEWLINE.getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	protected void printCommandLine(OutputStream os) {
		if (os != null) {
			try {
				os.write(getCommandLineQuoted(getCommandArgs(), true)
						.getBytes());
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
				if (quote && (commandArg.contains(" ")
						|| commandArg.contains("\"")
						|| commandArg.contains("\\"))) {
					commandArg = '"' + commandArg.replaceAll("\\\\", "\\\\\\\\")
							.replaceAll("\"", "\\\\\"") + '"';
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
