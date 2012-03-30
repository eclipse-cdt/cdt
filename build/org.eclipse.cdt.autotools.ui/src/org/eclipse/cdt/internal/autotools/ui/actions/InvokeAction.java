/*******************************************************************************
 * Copyright (c) 2006, 2007, 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.autotools.core.AutotoolsNewMakeGenerator;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

public abstract class InvokeAction extends AbstractTargetAction {

	public final String SHELL_COMMAND = "sh"; //$NON-NLS-1$

	protected void showInformation(String title, String content) {

		MessageDialog.openInformation(new Shell(), title, content);
	}

	protected void showError(String title, String content) {

		MessageDialog.openError(new Shell(), title, content);
	}

	protected void showSuccess(String title) {
		MessageDialog.openInformation(new Shell(), title, 
				InvokeMessages.getString("InvokeAction.success")); //$NON-NLS-1$
	}
	
	protected String showInput(String title, String content, String defaultTxt) {
		InputDialog getOptionDialog = new InputDialog(new Shell(), title,
				content, defaultTxt, null);

		getOptionDialog.open();

		return getOptionDialog.getValue();
	}

	/**
	 * Separate targets to array from a string.
	 * 
	 * @param rawArgList
	 * @return targets in string[] array. if targets are not formatted properly,
	 *         returns null
	 */
	protected String[] separateTargets(String rawArgList) {

		StringTokenizer st = new StringTokenizer(rawArgList, " "); //$NON-NLS-1$
		ArrayList<String> targetList = new ArrayList<String>();

		while (st.hasMoreTokens()) {
			String currentWord = st.nextToken().trim();

			if (currentWord.startsWith("'")) { //$NON-NLS-1$
				StringBuffer tmpTarget = new StringBuffer();
				while (!currentWord.endsWith("'")) { //$NON-NLS-1$
					tmpTarget.append(currentWord + " "); //$NON-NLS-1$
					if (!st.hasMoreTokens()) {
						// quote not closed properly, so return null
						return null;
					}
					currentWord = st.nextToken().trim();
				}

				tmpTarget.append(currentWord);
				targetList.add(tmpTarget.toString());
				continue;
			}

			if (currentWord.startsWith("\"")) { //$NON-NLS-1$
				StringBuffer tmpTarget = new StringBuffer();
				while (!currentWord.endsWith("\"")) { //$NON-NLS-1$
					tmpTarget.append(currentWord + " "); //$NON-NLS-1$
					if (!st.hasMoreTokens()) {
						// double quote not closed properly, so return null
						return null;
					}
					currentWord = st.nextToken().trim();
				}

				tmpTarget.append(currentWord);
				targetList.add(tmpTarget.toString());
				continue;
			}

			// for targets without quote/double quotes.
			targetList.add(currentWord);

		}

		return (String[])targetList.toArray(new String[targetList.size()]);
	}

	protected String[] separateOptions(String rawArgList) {
		ArrayList<String> argList = new ArrayList<String>();
		// May be multiple user-specified options in which case we
		// need to split them up into individual options
		rawArgList = rawArgList.trim();
		boolean finished = false;
		int lastIndex = rawArgList.indexOf("--"); //$NON-NLS-1$
		if (lastIndex != -1) {
			while (!finished) {
				int index = rawArgList.indexOf("--", lastIndex + 2); //$NON-NLS-1$
				if (index != -1) {
					String previous = rawArgList.substring(lastIndex, index)
							.trim();
					argList.add(previous);
					rawArgList = rawArgList.substring(index);
				} else {
					argList.add(rawArgList);
					finished = true;
				}
			}
		}

		return (String[])argList.toArray(new String[argList.size()]);

	}

	protected String[] simpleParseOptions(String rawArgList) {
		ArrayList<String> argList = new ArrayList<String>();
		int lastArgIndex = -1;
		int i = 0;
		while (i < rawArgList.length()) {
			char ch = rawArgList.charAt(i);
			// Skip white-space
			while (Character.isWhitespace(ch)) {
				++i;
				if (i < rawArgList.length())
					ch = rawArgList.charAt(i);
				else // Otherwise we are done
					return argList.toArray(new String[argList.size()]);
			}

			// Simplistic parser.  We break up into strings delimited
			// by blanks.  If quotes are used, we ignore blanks within.
			// If a backslash is used, we ignore the next character and
			// pass it through.
			lastArgIndex = i;
			boolean inString = false;
			while (i < rawArgList.length()) {
				ch = rawArgList.charAt(i);
				if (ch == '\\') // escape character
					++i; // skip over the next character
				else if (ch == '\"') { // double quotes
					inString = !inString;
				} else if (Character.isWhitespace(ch)) {
					if (!inString) {
						argList.add(rawArgList.substring(lastArgIndex, i));
						break;
					}
				}
				++i;
			}
			// Look for the case where we ran out of chars for the last
			// token.
			if (i >= rawArgList.length())
				argList.add(rawArgList.substring(lastArgIndex));
			++i;
		}
		return argList.toArray(new String[argList.size()]);
	}

	protected IPath getExecDir(IContainer container) {
		int type = container.getType();
		IPath execDir = null;
		if (type == IContainer.FILE) {
			execDir = container.getLocation().removeLastSegments(1);
		} else {
			execDir = container.getLocation();
		}
		return execDir;
	}
	
	protected IPath getCWD(IContainer container) {
		int type = container.getType();
		IPath cwd = null;
		if (type == IContainer.FILE) {
			cwd = container.getFullPath().removeLastSegments(1);
		} else {
			cwd = container.getFullPath();
		}
		return cwd;
	}
	
	private static class ExecuteProgressDialog implements IRunnableWithProgress {
		private IPath command;
		private String[] argumentList;
		private String[] envList;
		private IPath execDir;
		private int status;
		private HashMap<String, String> outputs = null;
		
		public ExecuteProgressDialog(IPath command, String[] argumentList,
				String[] envList, IPath execDir) {
			this.command = command;
			this.argumentList = argumentList;
			this.envList = envList;
			this.execDir = execDir;
		}

		public void run(IProgressMonitor monitor)
		throws InvocationTargetException, InterruptedException {
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			ByteArrayOutputStream stderr = new ByteArrayOutputStream();
			CommandLauncher cmdL = new CommandLauncher();
			outputs = null;

			// invoke command
			try {
				monitor.beginTask(
						InvokeMessages.getFormattedString("InvokeAction.progress.message", // $NON-NLS-1$
								new String[]{command.toOSString()}), IProgressMonitor.UNKNOWN);
				monitor.worked(1);
				Process process = cmdL.execute(command, argumentList, envList,
						execDir, new NullProgressMonitor());

				if (cmdL.waitAndRead(stdout, stderr, new NullProgressMonitor()) == CommandLauncher.OK) {
					try {
						status = 0;
						monitor.done();
						process.getOutputStream().close();
					} catch (IOException e) {
						// ignore
					}
				} else {
					// failed to execute command
					status = -1;
					monitor.done();
					return;
				}
			} catch (CoreException e) {
				monitor.done();
				throw new InvocationTargetException(e);
			}

			outputs = new HashMap<String, String>();

			outputs.put("stdout", stdout.toString()); //$NON-NLS-1$
			outputs.put("stderr", stderr.toString()); //$NON-NLS-1$

			try {
				stdout.close();
				stderr.close();
			} catch (IOException e) {
				// ignore
			}
		}
			
		public HashMap<String, String> getOutputs() {
			return outputs;
		}
		
		public int getStatus() {
			return status;
		}
	}
	
	
	protected HashMap<String, String> executeCommand(IPath command,
			String[] argumentList, String[] envList, IPath execDir) {
		try {
			ExecuteProgressDialog d = new ExecuteProgressDialog(command,
					argumentList, envList, execDir);
			new ProgressMonitorDialog(new Shell()).run(false, false, d);
			if (d.getStatus() == -1)
				showError(InvokeMessages
					.getString("InvokeAction.execute.windowTitle.error"), InvokeMessages //$NON-NLS-1$
					.getString("InvokeAction.execute.message") //$NON-NLS-1$
					+ command.toOSString()); //$NON-NLS-1$
			return d.getOutputs();
		} catch (InvocationTargetException e) {
			showError(InvokeMessages
					.getString("InvokeAction.execute.windowTitle.error"), InvokeMessages //$NON-NLS-1$
					.getString("InvokeAction.execute.message") //$NON-NLS-1$
					+ command.toOSString()); //$NON-NLS-1$
			AutotoolsUIPlugin.logException(e);
			return null;
		} catch (InterruptedException e) {
		    return null;
		}
	}

	protected void executeConsoleCommand(final String actionName, final String command,
			final String[] argumentList, final IPath execDir) {
		// We need to use a workspace root scheduling rule because adding MakeTargets
		// may end up saving the project description which runs under a workspace root rule.
		final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();

		Job backgroundJob = new Job(actionName) {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

						public void run(IProgressMonitor monitor) throws CoreException {
							try {
								String errMsg = null;
								IProject project = getSelectedContainer().getProject();
								// Get a build console for the project
								IConsole console = CCorePlugin.getDefault().getConsole("org.eclipse.cdt.autotools.ui.autotoolsConsole"); //$NON-NLS-1$
								console.start(project);
								CUIPlugin.getDefault().startGlobalConsole();
								ConsoleOutputStream consoleOutStream = console.getOutputStream();
								// FIXME: we want to remove need for ManagedBuilderManager, but how do we
								// get environment variables.
								IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
								IConfiguration cfg = info.getDefaultConfiguration();

								StringBuffer buf = new StringBuffer();
								String[] consoleHeader = new String[3];

								consoleHeader[0] = actionName;
								consoleHeader[1] = cfg.getName();
								consoleHeader[2] = project.getName();
								buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
								String invokeMsg = InvokeMessages.getFormattedString("InvokeAction.console.message", //$NON-NLS-1$
										new String[]{actionName, execDir.toString()}); //$NON-NLS-1$
								buf.append(invokeMsg);
								buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
								buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
								consoleOutStream.write(buf.toString().getBytes());
								consoleOutStream.flush();
								
								ArrayList<String> additionalEnvs = new ArrayList<String>();
								String strippedCommand = AutotoolsNewMakeGenerator.stripEnvVars(command, additionalEnvs);
								// Get a launcher for the config command
								CommandLauncher launcher = new CommandLauncher();
								// Set the environment
								IEnvironmentVariable variables[] = ManagedBuildManager
										.getEnvironmentVariableProvider().getVariables(cfg, true);
								String[] env = null;
								ArrayList<String> envList = new ArrayList<String>();
								if (variables != null) {
									for (int i = 0; i < variables.length; i++) {
										envList.add(variables[i].getName()
												+ "=" + variables[i].getValue()); //$NON-NLS-1$
									}
									if (additionalEnvs.size() > 0)
										envList.addAll(additionalEnvs); // add any additional environment variables specified ahead of script
									env = (String[]) envList.toArray(new String[envList.size()]);
								}
								
								String[] newArgumentList;
								
								// Fix for bug #343905 and bug #371277
                                // For Windows and Mac, we cannot run a script directly (in this case,
                                // autotools are scripts).  We need to run "sh -c command args where command
								// plus args is represented in a single string.  The same applies for
								// some Linux shells such as dash.  Using sh -c will work on all Linux
								// POSIX-compliant shells.
								StringBuffer command = new StringBuffer(strippedCommand);
								for (String arg : argumentList) {
									command.append(" " + arg);
								}
								newArgumentList = new String[] { "-c", command.toString() };
   
						        OutputStream stdout = consoleOutStream;
								OutputStream stderr = consoleOutStream;

								launcher.showCommand(true);
								// Run the shell script via shell command.
								Process proc = launcher.execute(new Path(SHELL_COMMAND), newArgumentList, env,
										execDir, new NullProgressMonitor());
								if (proc != null) {
									try {
										// Close the input of the process since we will never write to
										// it
										proc.getOutputStream().close();
									} catch (IOException e) {
									}

									if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
											monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
										errMsg = launcher.getErrorMessage();
									}

									// Force a resync of the projects without allowing the user to
									// cancel.
									// This is probably unkind, but short of this there is no way to
									// ensure
									// the UI is up-to-date with the build results
									// monitor.subTask(ManagedMakeMessages
									// .getResourceString(REFRESH));
									monitor.subTask(AutotoolsUIPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
									try {
										project.refreshLocal(IResource.DEPTH_INFINITE, null);
									} catch (CoreException e) {
										monitor.subTask(AutotoolsUIPlugin
												.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
									}
								} else {
									errMsg = launcher.getErrorMessage();
								}
								
								if (errMsg != null)
									AutotoolsUIPlugin.logErrorMessage(errMsg);
								
							} catch (IOException e) {
								AutotoolsUIPlugin.log(e);
							}
						}
					}, rule, IWorkspace.AVOID_UPDATE, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				IStatus returnStatus = Status.OK_STATUS;
				return returnStatus;
			}
		};

		backgroundJob.setRule(rule);
		backgroundJob.schedule();
	}

}
