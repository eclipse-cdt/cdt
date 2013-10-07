/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.PasswordAuthentication;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.internal.remote.jsch.core.JSchConnectionManager;
import org.eclipse.internal.remote.jsch.ui.messages.Messages;
import org.eclipse.internal.remote.jsch.ui.wizards.JSchConnectionWizard;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jsch.ui.UserInfoPrompter;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IUserAuthenticator;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class JSchUIConnectionManager extends AbstractRemoteUIConnectionManager {
	private class RemoteAuthenticator implements IUserAuthenticator {
		UserInfoPrompter prompter;

		public RemoteAuthenticator(IRemoteConnection conn) {
			try {
				prompter = new UserInfoPrompter(new JSch().getSession(conn.getUsername(), conn.getAddress()));
			} catch (JSchException e) {
				// Not allowed
			}
		}

		public PasswordAuthentication prompt(String username, String message) {
			if (prompter.promptPassword(message)) {
				PasswordAuthentication auth = new PasswordAuthentication(username, prompter.getPassword().toCharArray());
				return auth;
			}
			return null;
		}

		public String[] prompt(String destination, String name, String message, String[] prompt, boolean[] echo) {
			return prompter.promptKeyboardInteractive(destination, name, message, prompt, echo);
		}

		public int prompt(final int promptType, final String title, final String message, final int[] promptResponses,
				final int defaultResponseIndex) {
			final Display display = getDisplay();
			final int[] retval = new int[1];
			final String[] buttons = new String[promptResponses.length];
			for (int i = 0; i < promptResponses.length; i++) {
				int prompt = promptResponses[i];
				switch (prompt) {
				case IDialogConstants.OK_ID:
					buttons[i] = IDialogConstants.OK_LABEL;
					break;
				case IDialogConstants.CANCEL_ID:
					buttons[i] = IDialogConstants.CANCEL_LABEL;
					break;
				case IDialogConstants.NO_ID:
					buttons[i] = IDialogConstants.NO_LABEL;
					break;
				case IDialogConstants.YES_ID:
					buttons[i] = IDialogConstants.YES_LABEL;
					break;
				}
			}

			display.syncExec(new Runnable() {
				public void run() {
					final MessageDialog dialog = new MessageDialog(new Shell(display), title, null /* title image */, message,
							promptType, buttons, defaultResponseIndex);
					retval[0] = dialog.open();
				}
			});
			return retval[0];
		}
	}

	private Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	private final JSchConnectionManager fConnMgr;

	public JSchUIConnectionManager(IRemoteServices services) {
		fConnMgr = (JSchConnectionManager) services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager#getConnectionWizard(org.eclipse.swt.widgets.Shell)
	 */
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		return new JSchConnectionWizard(shell, fConnMgr);
	}

	@Override
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		if (!connection.isOpen()) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						connection.open(new RemoteAuthenticator(connection), monitor);
					} catch (RemoteConnectionException e) {
						throw new InvocationTargetException(e);
					}
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			try {
				if (context != null) {
					context.run(true, true, op);
				} else {
					new ProgressMonitorDialog(shell).run(true, true, op);
				}
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(shell, Messages.JSchUIConnectionManager_Connection_Error, Messages.JSchUIConnectionManager_Could_not_open_connection,
						new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getCause().getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(shell, Messages.JSchUIConnectionManager_Connection_Error, Messages.JSchUIConnectionManager_Could_not_open_connection,
						new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
			}
		}
	}
}
