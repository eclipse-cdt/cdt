/*******************************************************************************
 * Copyright (c) 2002, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [190231] initial API and implementation
 * Martin Oberhuber (Wind River) - brought in methods from SubSystem (c) IBM
 * Martin Oberhuber (Wind River) - [236355] [api] Add an IRSEInteractionProvider#eventExec() method
 *******************************************************************************/
package org.eclipse.rse.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rse.core.IRSEInteractionProvider;
import org.eclipse.rse.core.IRSERunnableWithProgress;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A Default Interaction Provider that runs in the Eclipse / SWT UI. Meant to
 * provide the same functionality as it was there before UI / Non-UI Splitting.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/tm/">Target Management</a>
 * team.
 * </p>
 *
 * @since org.eclipse.rse.ui 3.0
 */
public class DefaultUIInteractionProvider implements IRSEInteractionProvider {

	private Shell shell = null;

	private class NullRunnableContext implements IRunnableContext {
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			IProgressMonitor monitor = new NullProgressMonitor();
			runnable.run(monitor);
		}
	}

	/**
	 * Get the progress monitor dialog for this operation. We try to use one for
	 * all phases of a single operation, such as connecting and resolving.
	 *
	 * @deprecated this is scheduled to be removed since we want to avoid UI
	 *             components in SubSystem.
	 */
	protected IRunnableContext getRunnableContext(/* Shell rshell */) {
		if (Display.getCurrent() == null) {
			return new NullRunnableContext();
		}
		// for wizards and dialogs use the specified context that was placed in
		// the registry
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistryUI().getRunnableContext();
		if (irc != null) {
			SystemBasePlugin.logInfo("Got runnable context from system registry"); //$NON-NLS-1$
			return irc;
		} else {
			// for other cases, use statusbar
			IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
			if (win != null) {
				Shell winShell = getActiveWorkbenchShell();
				if (winShell != null && !winShell.isDisposed() && winShell.isVisible()) {
					SystemBasePlugin.logInfo("Using active workbench window as runnable context"); //$NON-NLS-1$
					shell = winShell;
					return win;
					// dwd } else {
					// dwd win = null;
				}
			}
			// dwd if (shell == null || shell.isDisposed() ||
			// !shell.isVisible()) {
			// dwd SystemBasePlugin.logInfo("Using progress monitor dialog with
			// given shell as parent");
			// dwd shell = rshell;
			// dwd }
			// dwd IRunnableContext dlg = new ProgressMonitorDialog(rshell);
			IRunnableContext dlg = new ProgressMonitorDialog(shell);
			return dlg;
		}
	}

	/**
	 * Helper/convenience method. Return shell of active window.
	 */
	public static Shell getActiveWorkbenchShell() {
		Shell result = null;
		if (PlatformUI.isWorkbenchRunning()) {
			try {
				IWorkbenchWindow window = getActiveWorkbenchWindow();
				if (window != null) {
					result = window.getShell();
				}
			} catch (Exception e) {
				return null;
			}
		} else // workbench has not been loaded yet!
		{
			return null;
		}
		return result;
	}

	/**
	 * Helper/convenience method. Return active window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return RSEUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public void runInDefaultContext(boolean fork, boolean cancellable, IRSERunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub
	}

	private Display getDefaultDisplay() {
		Display d = Display.getCurrent();
		if (d == null) {
			d = SystemBasePlugin.getActiveWorkbenchShell().getDisplay();
			if (d == null) {
				d = Display.getDefault();
			}
		}
		return d;
	}

	public void asyncExec(final Runnable runnable) {
		getDefaultDisplay().asyncExec(runnable);
	}

	public void eventExec(final Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			asyncExec(runnable);
		}
	}

	public void flushRunnableQueue() {
		Display d = Display.getCurrent();
		if (d == null) {
			getDefaultDisplay().syncExec(new Runnable() {
				public void run() {
					flushRunnableQueue();
				}
			});
		} else {
			while (d.readAndDispatch()) {
				// flush the event queue
			}
		}
	}

	public IProgressMonitor getDefaultProgressMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void showMessage(SystemMessage msg) {
		// TODO Auto-generated method stub

	}

}
