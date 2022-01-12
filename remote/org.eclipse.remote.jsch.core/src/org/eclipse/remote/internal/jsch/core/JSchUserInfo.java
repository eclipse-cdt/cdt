/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Patrick Tasse - [462418] use stored password on non-preferred password based authentication
 * Martin Oberhuber - [468889] Support Eclipse older than Mars
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.net.PasswordAuthentication;

import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IUserAuthenticatorService;
import org.eclipse.remote.internal.jsch.core.messages.Messages;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * Class to supply credentials from connection attributes without user interaction.
 */
public class JSchUserInfo implements UserInfo, UIKeyboardInteractive {
	private boolean logging = false;
	private boolean firstTryPassphrase = true;

	private final IRemoteConnectionHostService hostService;
	private IUserAuthenticatorService userAuthenticatorService;

	public JSchUserInfo(IRemoteConnectionHostService hostService) {
		this.hostService = hostService;
	}

	public JSchUserInfo(IRemoteConnectionHostService hostService, IUserAuthenticatorService userAuthenticatorService) {
		this(hostService);
		this.userAuthenticatorService = userAuthenticatorService;
	}

	@Override
	public String getPassphrase() {
		if (logging) {
			System.out.println("getPassphrase"); //$NON-NLS-1$
		}
		return hostService.getPassphrase();
	}

	@Override
	public String getPassword() {
		if (logging) {
			System.out.println("getPassword"); //$NON-NLS-1$
		}
		return hostService.getPassword();
	}

	@Override
	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
			boolean[] echo) {
		if (logging) {
			System.out.println("promptKeyboardInteractive:" + destination + ":" + name + ":" + instruction); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			for (String p : prompt) {
				System.out.println(" " + p); //$NON-NLS-1$
			}
		}

		if (userAuthenticatorService != null) {
			String[] result = userAuthenticatorService.prompt(destination, name, instruction, prompt, echo);
			if (result != null) {
				if (prompt.length == 1 && prompt[0].trim().equalsIgnoreCase("password:")) { //$NON-NLS-1$
					hostService.setPassword(result[0]);
				}
			}
			return result;
		}
		return null;
	}

	@Override
	public boolean promptPassphrase(String message) {
		if (logging) {
			System.out.println("promptPassphrase:" + message); //$NON-NLS-1$
		}
		if (firstTryPassphrase && !getPassphrase().equals("")) { //$NON-NLS-1$
			firstTryPassphrase = false;
			return true;
		}
		if (userAuthenticatorService != null) {
			PasswordAuthentication auth = userAuthenticatorService.prompt(hostService.getUsername(), message);
			if (auth == null) {
				return false;
			}
			hostService.setUsername(auth.getUserName());
			hostService.setPassphrase(new String(auth.getPassword()));
			return true;
		}
		return false;
	}

	@Override
	public boolean promptPassword(String message) {
		if (logging) {
			System.out.println("promptPassword:" + message); //$NON-NLS-1$
		}
		if (userAuthenticatorService != null) {
			PasswordAuthentication auth = userAuthenticatorService.prompt(hostService.getUsername(), message);
			if (auth == null) {
				return false;
			}
			hostService.setUsername(auth.getUserName());
			hostService.setPassword(new String(auth.getPassword()));
			return true;
		}
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		if (logging) {
			System.out.println("promptYesNo:" + message); //$NON-NLS-1$
		}
		if (userAuthenticatorService != null) {
			int prompt = userAuthenticatorService.prompt(IUserAuthenticatorService.QUESTION,
					Messages.AuthInfo_Authentication_message, message,
					new int[] { IUserAuthenticatorService.YES, IUserAuthenticatorService.NO },
					IUserAuthenticatorService.YES);
			return prompt == IUserAuthenticatorService.YES;
		}
		return true;
	}

	@Override
	public void showMessage(String message) {
		if (logging) {
			System.out.println("showMessage:" + message); //$NON-NLS-1$
		}
		if (userAuthenticatorService != null) {
			userAuthenticatorService.prompt(IUserAuthenticatorService.INFORMATION,
					Messages.AuthInfo_Authentication_message, message, new int[] { IUserAuthenticatorService.OK },
					IUserAuthenticatorService.OK);
		}
	}
}
