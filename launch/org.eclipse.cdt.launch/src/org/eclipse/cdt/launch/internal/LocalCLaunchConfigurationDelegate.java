package org.eclipse.cdt.launch.internal;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.ICDebuggerManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

/**
 * Insert the type's description here.
 * @see ILaunchConfigurationDelegate
 */
public class LocalCLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	protected String renderDebugTarget(ICDISession session) {
//		String format= "{0} at localhost {1}";
//		return MessageFormat.format(format, new String[] { classToRun, String.valueOf(host) });
		return "session -- TODO";
	}

	public static String renderProcessLabel(String[] commandLine) {
		String format= "{0} ({1})";
		String timestamp= DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[] { commandLine[0], timestamp });
	}
	
	protected static String renderCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; 
		StringBuffer buf= new StringBuffer(commandLine[0]);
		for (int i= 1; i < commandLine.length; i++) {
			buf.append(' ');
			buf.append(commandLine[i]);
		}	
		return buf.toString();
	}
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
		throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Launching Local C Application", IProgressMonitor.UNKNOWN);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		ICProject cproject = getCProject(configuration);
		IPath projectPath = Platform.getLocation().append(cproject.getResource().getFullPath());
		projectPath = projectPath.append(getProgramName(configuration));
		String arguments[] = getProgramArgumentsArray(configuration);		
		ArrayList command = new ArrayList(1+arguments.length);
		command.add(projectPath.toOSString());
		command.addAll(Arrays.asList(arguments));

		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			ICDebuggerManager dbgmanager = CDebugCorePlugin.getDefault().getDebuggerManager();
			ICDebugger cdebugger = dbgmanager.createDebugger(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_CDT_DEBUGGER_ID, ""));
			IFile exe = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(projectPath);
			ICDISession dsession = cdebugger.createLaunchSession(configuration, exe);			
			ICDIRuntimeOptions opt = dsession.getRuntimeOptions();
			opt.setArguments(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""));
			File wd = getWorkingDir(configuration);
			if ( wd != null ) {
				opt.setWorkingDirectory(wd.toString());
			}
			opt.setEnvironment(getEnvironmentProperty(configuration));
			ICDITarget dtarget = dsession.getTargets()[0];
//			Process process = dtarget.getProcess();
//			IProcess iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel((String [])command.toArray(new String[command.size()])));
//			CDebugModel.newDebugTarget(launch, dsession.getTargets()[0], renderDebugTarget(dsession), iprocess, true, false, false );
		}
		else {
			Process process = exec((String [])command.toArray(new String[command.size()]), getEnvironmentArray(configuration), getWorkingDir(configuration));
			DebugPlugin.getDefault().newProcess(launch, process, "label");
		}
		monitor.done();
	}

	private String[] getEnvironmentArray(ILaunchConfiguration configuration) {
//		Map env = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) null);
// TODO create env array;
		return null;
	}

	private Properties getEnvironmentProperty(ILaunchConfiguration configuration) {
//		Map env = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) null);
		return new Properties();
	}


	protected File getWorkingDir(ILaunchConfiguration config) throws CoreException {
		String path = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		if (path == null) {
			return null;
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			abort("Specified working directory does not exist or is not a directory", null, ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
		}
		return dir;
	}

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), code, message, exception));
	}

	/**
	 * Performs a runtime exec on the given command line in the context
	 * of the specified working directory, and returns
	 * the resulting process. If the current runtime does not support the
	 * specification of a working directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *  cancelled
	 * @see Runtime
	 */
	protected Process exec(String[] cmdLine, String[] envp, File workingDirectory) throws CoreException {
		Process p = null;
		try {
			if (workingDirectory == null) {
				p = Runtime.getRuntime().exec(cmdLine, envp);
			}
			else {
				p = Runtime.getRuntime().exec(cmdLine, envp, workingDirectory);
			}
		}
		catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort("Exception starting process", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory

			IStatus status = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED, "Eclipse runtime does not support working directory", e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
					p = exec(cmdLine, envp, null);
				}
			}
		}
		return p;
	}
	
	public ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getCProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null && cProject.exists()) {
					return cProject;
				}
			}
		}
		return null;
	}

	public String getCProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}

	public String getProgramName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
	}
	
	/**
	 * Returns the program arguments as an array of individual arguments.
	 *
	 * @return the program arguments as an array of individual arguments
	 */
	public String[] getProgramArgumentsArray(ILaunchConfiguration configuration) throws CoreException {
		String args = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		return parseArguments(args);
	}	
			
	private static class ArgumentParser {
		private String fArgs;
		private int fIndex= 0;
		private int ch= -1;
		
		public ArgumentParser(String args) {
			fArgs= args;
		}
		
		public String[] parseArguments() {
			List v= new ArrayList();
			
			ch= getNext();
			while (ch > 0) {
				while (Character.isWhitespace((char)ch))
					ch= getNext();	
				
				if (ch == '"') {
					v.add(parseString());
				} else {
					v.add(parseToken());
				}
			}
	
			String[] result= new String[v.size()];
			v.toArray(result);
			return result;
		}
		
		private int getNext() {
			if (fIndex < fArgs.length())
				return fArgs.charAt(fIndex++);
			return -1;
		}
		
		private String parseString() {
			StringBuffer buf= new StringBuffer();
			ch= getNext();
			while (ch > 0 && ch != '"') {
				if (ch == '\\') {
					ch= getNext();
					if (ch != '"') {           // Only escape double quotes
						buf.append('\\'); 
					}
				}
				if (ch > 0) {
					buf.append((char)ch);
					ch= getNext();
				}
			}
	
			ch= getNext();
				
			return buf.toString();
		}
		
		private String parseToken() {
			StringBuffer buf= new StringBuffer();
			
			while (ch > 0 && !Character.isWhitespace((char)ch)) {
				if (ch == '\\') {
					ch= getNext();
					if (ch > 0) {
						if (ch != '"') {        // Only escape double quotes
							buf.append('\\'); 
						}
						buf.append((char)ch);
						ch= getNext();
					} else if (ch == -1) {     // Don't lose a trailing backslash
						buf.append('\\');
					}
				} else if (ch == '"') {
					buf.append(parseString());
				} else {
					buf.append((char)ch);
					ch= getNext();
				}
			}
			return buf.toString();
		}
	}
	
	private static String[] parseArguments(String args) {
		if (args == null)
			return new String[0];
		ArgumentParser parser= new ArgumentParser(args);
		String[] res= parser.parseArguments();
		
		return res;
	}


}
