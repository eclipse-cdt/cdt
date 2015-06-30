/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Bernd Hufmann - Implement IRemoteConnection.Service.Factory
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.ui;

import java.net.PasswordAuthentication;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jsch.ui.UserInfoPrompter;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IUserAuthenticatorService;
import org.eclipse.remote.internal.jsch.core.JSchConnection;
import org.eclipse.swt.widgets.Display;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class JSchUserAuthenticator implements IUserAuthenticatorService {
	
	private final IRemoteConnection remoteConnection;
	private UserInfoPrompter prompter;

	public JSchUserAuthenticator(IRemoteConnection conn) {
		this.remoteConnection = conn;
		try {
			String username = conn.getAttribute(JSchConnection.USERNAME_ATTR);
			String address = conn.getAttribute(JSchConnection.ADDRESS_ATTR);
			prompter = new UserInfoPrompter(new JSch().getSession(username, address));
		} catch (JSchException e) {
			// Not allowed
		}
	}

	@Override
	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}
	
	@Override
	public PasswordAuthentication prompt(String username, String message) {
		if (prompter.promptPassword(message)) {
			String sessionUserName = prompter.getSession().getUserName();
			if (sessionUserName != null) {
				username = sessionUserName;
			}
			PasswordAuthentication auth = new PasswordAuthentication(username, prompter.getPassword().toCharArray());
			return auth;
		}
		return null;
	}

	@Override
	public String[] prompt(String destination, String name, String message, String[] prompt, boolean[] echo) {
		return prompter.promptKeyboardInteractive(destination, name, message, prompt, echo);
	}

	@Override
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
			@Override
			public void run() {
				final MessageDialog dialog = new MessageDialog(display.getActiveShell(), title, null /* title image */, message,
						promptType, buttons, defaultResponseIndex);
				retval[0] = dialog.open();
			}
		});
		return promptResponses[retval[0]];
	}

	private Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}
	
	public static class Factory implements IRemoteConnection.Service.Factory {
		@Override
		@SuppressWarnings("unchecked")
		public <T extends IRemoteConnection.Service> T getService(IRemoteConnection connection, Class<T> service) {
			if (IUserAuthenticatorService.class.equals(service)) {
				return (T) new JSchUserAuthenticator(connection);
			} else {
				return null;
			}
		}
	}
}
