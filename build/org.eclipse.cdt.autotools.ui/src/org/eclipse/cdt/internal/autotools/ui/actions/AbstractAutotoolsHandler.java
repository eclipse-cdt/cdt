/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.autotools.core.AutotoolsNewMakeGenerator;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.remote.core.RemoteCommandLauncher;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractAutotoolsHandler extends AbstractHandler {

	private IContainer fContainer;

	protected abstract void run(Shell activeShell);

	protected Object execute1(ExecutionEvent event) {
		ISelection k = HandlerUtil.getCurrentSelection(event);
		if (!k.isEmpty() && k instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) k).getFirstElement();
			IContainer container = getContainer(obj);
			if (container != null) {
				setSelectedContainer(container);
				run(HandlerUtil.getActiveShell(event));
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected IContainer getContainer(Object obj) {
		IContainer fContainer = null;

		if (obj instanceof Collection) {
			Collection<Object> c = (Collection<Object>) obj;
			Object[] objArray = c.toArray();
			if (objArray.length > 0)
				obj = objArray[0];
		}
		if (obj instanceof ICElement) {
			if (obj instanceof ICContainer || obj instanceof ICProject) {
				fContainer = (IContainer) ((ICElement) obj).getUnderlyingResource();
			} else {
				obj = ((ICElement) obj).getResource();
				if (obj != null) {
					fContainer = ((IResource) obj).getParent();
				}
			}
		} else if (obj instanceof IResource) {
			if (obj instanceof IContainer) {
				fContainer = (IContainer) obj;
			} else {
				fContainer = ((IResource) obj).getParent();
			}
		} else {
			fContainer = null;
		}
		return fContainer;
	}

	public final String SHELL_COMMAND = "sh"; //$NON-NLS-1$

	protected void showError(String title, String content) {
		MessageDialog.openError(new Shell(), title, content);
	}

	/**
	 * Separate targets to array from a string.
	 *
	 * @param rawArgList
	 * @return targets in string[] array. if targets are not formatted properly,
	 *         returns null
	 */
	protected List<String> separateTargets(String rawArgList) {

		StringTokenizer st = new StringTokenizer(rawArgList, " "); //$NON-NLS-1$
		List<String> targetList = new ArrayList<>();

		while (st.hasMoreTokens()) {
			String currentWord = st.nextToken().trim();

			if (currentWord.startsWith("'")) { //$NON-NLS-1$
				StringBuilder tmpTarget = new StringBuilder();
				while (!currentWord.endsWith("'")) { //$NON-NLS-1$
					tmpTarget.append(currentWord).append(' ');
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
				StringBuilder tmpTarget = new StringBuilder();
				while (!currentWord.endsWith("\"")) { //$NON-NLS-1$
					tmpTarget.append(currentWord).append(' ');
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

		return targetList;
	}

	protected List<String> separateOptions(String rawArgList) {
		List<String> argList = new ArrayList<>();
		// May be multiple user-specified options in which case we
		// need to split them up into individual options
		rawArgList = rawArgList.trim();
		boolean finished = false;
		int lastIndex = rawArgList.indexOf("--"); //$NON-NLS-1$
		if (lastIndex != -1) {
			while (!finished) {
				int index = rawArgList.indexOf("--", lastIndex + 2); //$NON-NLS-1$
				if (index != -1) {
					String previous = rawArgList.substring(lastIndex, index).trim();
					argList.add(previous);
					rawArgList = rawArgList.substring(index);
				} else {
					argList.add(rawArgList);
					finished = true;
				}
			}
		}

		return argList;

	}

	protected List<String> simpleParseOptions(String rawArgList) {
		List<String> argList = new ArrayList<>();
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
					return argList;
			}

			// Simplistic parser. We break up into strings delimited
			// by blanks. If quotes are used, we ignore blanks within.
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
		return argList;
	}

	protected IPath getExecDir(IContainer container) {
		int type = container.getType();
		IPath execDir = null;
		if (type == IResource.FILE) {
			execDir = container.getLocation().removeLastSegments(1);
		} else {
			execDir = container.getLocation();
		}
		return execDir;
	}

	protected IPath getCWD(IContainer container) {
		int type = container.getType();
		IPath cwd = null;
		if (type == IResource.FILE) {
			cwd = container.getFullPath().removeLastSegments(1);
		} else {
			cwd = container.getFullPath();
		}
		return cwd;
	}

	protected void executeConsoleCommand(final String actionName, final String command, final List<String> argumentList,
			final IPath execDir) {
		// We need to use a workspace root scheduling rule because adding
		// MakeTargets
		// may end up saving the project description which runs under a
		// workspace root rule.
		final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();

		Job backgroundJob = Job.create(actionName, monitor -> {
			try {
				ResourcesPlugin.getWorkspace().run((IWorkspaceRunnable) monitor1 -> {
					try {
						String errMsg = null;
						IProject project = getSelectedContainer().getProject();
						// Get a build console for the project
						IConsole console = CCorePlugin.getDefault()
								.getConsole("org.eclipse.cdt.autotools.ui.autotoolsConsole"); //$NON-NLS-1$
						console.start(project);
						CUIPlugin.getDefault().startGlobalConsole();
						ConsoleOutputStream consoleOutStream = console.getOutputStream();
						// FIXME: we want to remove need for
						// ManagedBuilderManager, but how do we
						// get environment variables.
						IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
						IConfiguration cfg = info.getDefaultConfiguration();

						StringBuilder buf = new StringBuilder();
						String[] consoleHeader = new String[3];

						consoleHeader[0] = actionName;
						consoleHeader[1] = cfg.getName();
						consoleHeader[2] = project.getName();
						buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
						String invokeMsg = InvokeMessages.getFormattedString("InvokeAction.console.message", //$NON-NLS-1$
								new String[] { actionName, execDir.toString() }); // $NON-NLS-1$
						buf.append(invokeMsg);
						buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
						consoleOutStream.write(buf.toString().getBytes());
						consoleOutStream.flush();

						ArrayList<String> additionalEnvs = new ArrayList<>();
						String strippedCommand = AutotoolsNewMakeGenerator.stripEnvVars(command, additionalEnvs);
						// Get a launcher for the config command...default for non-remote is a local
						// launcher, but user can override to perform all Autotool commands in a
						// Container when build in Container is enabled so check optional build
						// properties
						IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
						boolean runInContainer = false;
						if (props != null) {
							String runInContainerProperty = props
									.getProperty(AutotoolsNewMakeGenerator.RUN_IN_CONFIGURE_LAUNCHER);
							if (runInContainerProperty != null) {
								runInContainer = Boolean.parseBoolean(runInContainerProperty);
							}
						}
						ICommandLauncher fallbackLauncher = runInContainer
								? CommandLauncherManager.getInstance().getCommandLauncher(project)
								: new CommandLauncher();
						RemoteCommandLauncher launcher = new RemoteCommandLauncher(fallbackLauncher);
						launcher.setProject(project);
						// Set the environment
						IEnvironmentVariable variables[] = ManagedBuildManager.getEnvironmentVariableProvider()
								.getVariables(cfg, true);
						String[] env = null;
						ArrayList<String> envList = new ArrayList<>();
						if (variables != null) {
							for (int i = 0; i < variables.length; i++) {
								envList.add(variables[i].getName() + "=" + variables[i].getValue()); //$NON-NLS-1$
							}
							if (additionalEnvs.size() > 0)
								envList.addAll(additionalEnvs); // add any
																// additional
																// environment
																// variables
																// specified
																// ahead of
																// script
							env = envList.toArray(new String[envList.size()]);
						}

						String[] newArgumentList;

						// Fix for bug #343905 and bug #371277
						// For Windows and Mac, we cannot run a script
						// directly (in this case,
						// autotools are scripts). We need to run "sh -c
						// command args where command
						// plus args is represented in a single string. The
						// same applies for
						// some Linux shells such as dash. Using sh -c will
						// work on all Linux
						// POSIX-compliant shells.
						StringBuilder command1 = new StringBuilder(strippedCommand);
						for (String arg : argumentList) {
							command1.append(' ').append(arg);
						}
						newArgumentList = new String[] { "-c", command1.toString() };

						OutputStream stdout = consoleOutStream;
						OutputStream stderr = consoleOutStream;

						launcher.showCommand(true);
						// Run the shell script via shell command.
						Process proc = launcher.execute(new Path(SHELL_COMMAND), newArgumentList, env, execDir,
								new NullProgressMonitor());
						if (proc != null) {
							try {
								// Close the input of the process since we
								// will never write to
								// it
								proc.getOutputStream().close();
							} catch (IOException e1) {
							}

							if (launcher.waitAndRead(stdout, stderr,
									SubMonitor.convert(monitor1)) != ICommandLauncher.OK) {
								errMsg = launcher.getErrorMessage();
							}

							// Force a resync of the projects without
							// allowing the user to
							// cancel.
							// This is probably unkind, but short of this
							// there is no way to
							// ensure
							// the UI is up-to-date with the build results
							// monitor.subTask(ManagedMakeMessages
							// .getResourceString(REFRESH));
							monitor1.subTask(AutotoolsUIPlugin.getResourceString("MakeGenerator.refresh")); //$NON-NLS-1$
							try {
								project.refreshLocal(IResource.DEPTH_INFINITE, null);
							} catch (CoreException e2) {
								monitor1.subTask(AutotoolsUIPlugin.getResourceString("MakeGenerator.refresh.error")); //$NON-NLS-1$
							}
						} else {
							errMsg = launcher.getErrorMessage();
						}

						if (errMsg != null)
							AutotoolsUIPlugin.logErrorMessage(errMsg);

					} catch (IOException e3) {
						AutotoolsUIPlugin.log(e3);
					}
				}, rule, IWorkspace.AVOID_UPDATE, monitor);
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		});

		backgroundJob.setRule(rule);
		backgroundJob.schedule();
	}

	protected IContainer getSelectedContainer() {
		return fContainer;
	}

	public void setSelectedContainer(IContainer container) {
		fContainer = container;
	}

}
