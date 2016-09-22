/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import java.util.ArrayList;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
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
	public void showConsoleView(IDebuggerConsole console) {
		fShowDebuggerConsoleViewJob.setConsole(console);
		fShowDebuggerConsoleViewJob.schedule(100);
	}

	private class ShowDebuggerConsoleViewJob extends WorkbenchJob {
		private IConsole fConsole;

		ShowDebuggerConsoleViewJob() {
			super("Show GDB Console View"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.SHORT);
		}

		void setConsole(IConsole console) {
			fConsole = console;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null && fConsole != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
					IViewPart view = page.findView(DebuggerConsoleView.DEBUGGER_CONSOLE_VIEW_ID);
					if (view == null) {
						try {
							page.showView(DebuggerConsoleView.DEBUGGER_CONSOLE_VIEW_ID, null,
									IWorkbenchPage.VIEW_CREATE);

						} catch (PartInitException e) {
							CDebugUIPlugin.log(e);
						}
					}
                }
            }
            fConsole = null;
			return Status.OK_STATUS;
		}
	}
}
