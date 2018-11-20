/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleManager;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.internal.console.ConsolePageParticipantExtension;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A singleton Debugger Console manager which handles keeping track of all
 * active debugger consoles.
 */
public class DebuggerConsoleManager implements IDebuggerConsoleManager {

	/** A list of all known consoles */
	private ArrayList<IDebuggerConsole> fConsoleList = new ArrayList<>();

	/** A list of listeners registered for notifications of changes to consoles */
	private ListenerList<IConsoleListener> fConsoleListeners = new ListenerList<>();

	/** A list of registered console page participants */
	private List<ConsolePageParticipantExtension> fPageParticipants;

	private OpenDebuggerConsoleViewJob fOpenDebuggerConsoleViewJob = new OpenDebuggerConsoleViewJob();
	private ShowDebuggerConsoleViewJob fShowDebuggerConsoleViewJob = new ShowDebuggerConsoleViewJob();

	@Override
	public void addConsoleListener(IConsoleListener listener) {
		fConsoleListeners.add(listener);
	}

	@Override
	public void removeConsoleListener(IConsoleListener listener) {
		fConsoleListeners.remove(listener);
	}

	@Override
	public IDebuggerConsole[] getConsoles() {
		synchronized (fConsoleList) {
			return fConsoleList.toArray(new IDebuggerConsole[fConsoleList.size()]);
		}
	}

	@Override
	public void addConsole(IDebuggerConsole console) {
		synchronized (fConsoleList) {
			fConsoleList.add(console);
		}
		for (IConsoleListener listener : fConsoleListeners) {
			listener.consolesAdded(new IConsole[] { console });
		}
	}

	@Override
	public void removeConsole(IDebuggerConsole console) {
		synchronized (fConsoleList) {
			fConsoleList.remove(console);
		}
		for (IConsoleListener listener : fConsoleListeners) {
			listener.consolesRemoved(new IConsole[] { console });
		}
	}

	@Override
	public void showConsoleView() {
		fShowDebuggerConsoleViewJob.schedule(100);
	}

	@Override
	public void openConsoleView() {
		fOpenDebuggerConsoleViewJob.schedule(100);
	}

	// Code for page participants borrowed from
	// org.eclipse.ui.internal.console.ConsoleManager
	public IConsolePageParticipant[] getPageParticipants(IDebuggerConsole console) {
		if (fPageParticipants == null) {
			fPageParticipants = new ArrayList<>();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
					ConsolePlugin.getUniqueIdentifier(), IConsoleConstants.EXTENSION_POINT_CONSOLE_PAGE_PARTICIPANTS);
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement config = elements[i];
				ConsolePageParticipantExtension extension = new ConsolePageParticipantExtension(config) {
					@Override
					public boolean isEnabledFor(IConsole console) throws CoreException {
						// Override to provide more information to the evaluation context
						// than what the base class provides.  This allows richer enablement
						// conditions to be used.  For example, org.eclipse.cdt.examples.dsf.gdb
						// limits the enablement of its GdbExtendedConsolePageParticipant to
						// when the plugin has been activated.
						// Without this richer EvaluationContext, the information about
						// plugin activation is not available and all that can be checked is
						// the type of console.
						IEvaluationContext context = DebugUIPlugin.createEvaluationContext(console);
						Expression expression = getEnablementExpression();
						if (expression != null) {
							EvaluationResult evaluationResult = expression.evaluate(context);
							return evaluationResult == EvaluationResult.TRUE;
						}
						return true;
					}
				};
				fPageParticipants.add(extension);
			}
		}
		ArrayList<IConsolePageParticipant> list = new ArrayList<>();
		for (Iterator<ConsolePageParticipantExtension> i = fPageParticipants.iterator(); i.hasNext();) {
			ConsolePageParticipantExtension extension = i.next();
			try {
				if (extension.isEnabledFor(console)) {
					list.add(extension.createDelegate());
				}
			} catch (CoreException e) {
				CDebugUIPlugin.log(e);
			}
		}
		return list.toArray(new IConsolePageParticipant[0]);
	}

	private class ShowDebuggerConsoleViewJob extends WorkbenchJob {
		ShowDebuggerConsoleViewJob() {
			super("Show GDB Console View"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.SHORT);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					boolean consoleFound = false;
					IViewPart view = page.findView(DebuggerConsoleView.DEBUGGER_CONSOLE_VIEW_ID);
					if (view != null) {
						DebuggerConsoleView consoleView = (DebuggerConsoleView) view;
						boolean consoleVisible = page.isPartVisible(consoleView);
						if (consoleVisible) {
							consoleFound = true;
							page.bringToTop(consoleView);
						}
					}

					if (!consoleFound) {
						try {
							DebuggerConsoleView consoleView = (DebuggerConsoleView) page.showView(
									DebuggerConsoleView.DEBUGGER_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
							page.bringToTop(consoleView);
						} catch (PartInitException e) {
							CDebugUIPlugin.log(e);
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	private class OpenDebuggerConsoleViewJob extends WorkbenchJob {
		OpenDebuggerConsoleViewJob() {
			super("Open GDB Console View"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.SHORT);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart view = page.findView(DebuggerConsoleView.DEBUGGER_CONSOLE_VIEW_ID);
					if (view == null || !page.isPartVisible(view)) {
						try {
							page.showView(DebuggerConsoleView.DEBUGGER_CONSOLE_VIEW_ID, null,
									IWorkbenchPage.VIEW_CREATE);
						} catch (PartInitException e) {
							CDebugUIPlugin.log(e);
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	}
}
