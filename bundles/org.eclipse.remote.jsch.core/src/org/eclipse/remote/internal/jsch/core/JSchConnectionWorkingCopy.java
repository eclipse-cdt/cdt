/*******************************************************************************
 * Copyright (c) 2007, 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *    Markus Schorn - Bug 449306: Unnecessary access to secure storage.
 *******************************************************************************/
package org.eclipse.remote.internal.jsch.core;

import java.util.Collections;
import java.util.Map;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

/**
 * @since 5.0
 */
public class JSchConnectionWorkingCopy extends JSchConnection implements IRemoteConnectionWorkingCopy {
	private final JSchConnectionAttributes fWorkingAttributes;
	private final JSchConnection fOriginal;
	private boolean fIsDirty;

	public JSchConnectionWorkingCopy(JSchConnection connection) {
		super(connection.getName(), connection.getRemoteServices());
		fWorkingAttributes = connection.getInfo().copy();
		fOriginal = connection;
		fIsDirty = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#getAddress()
	 */
	@Override
	public String getAddress() {
		return fWorkingAttributes.getAttribute(JSchConnectionAttributes.ADDRESS_ATTR, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#getAttributes()
	 */
	@Override
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(fWorkingAttributes.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#getName()
	 */
	@Override
	public String getName() {
		return fWorkingAttributes.getName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#getOriginal()
	 */
	@Override
	public IRemoteConnection getOriginal() {
		return fOriginal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.internal.jsch.core.JSchConnection#getPassphrase()
	 */
	@Override
	public String getPassphrase() {
		return fWorkingAttributes.getSecureAttribute(JSchConnectionAttributes.PASSPHRASE_ATTR, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.internal.jsch.core.JSchConnection#getPassword()
	 */
	@Override
	public String getPassword() {
		return fWorkingAttributes.getSecureAttribute(JSchConnectionAttributes.PASSWORD_ATTR, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#getPort()
	 */
	@Override
	public int getPort() {
		return fWorkingAttributes.getInt(JSchConnectionAttributes.PORT_ATTR, DEFAULT_PORT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.internal.jsch.core.JSchConnection#getProxyCommand()
	 */
	@Override
	public String getProxyCommand() {
		return fWorkingAttributes.getAttribute(JSchConnectionAttributes.PROXYCOMMAND_ATTR, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.internal.jsch.core.JSchConnection#getProxyConnectionName()
	 */
	@Override
	public String getProxyConnectionName() {
		return fWorkingAttributes.getAttribute(JSchConnectionAttributes.PROXYCONNECTION_ATTR, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.internal.jsch.core.JSchConnection#getTimeout()
	 */
	@Override
	public int getTimeout() {
		return fWorkingAttributes.getInt(JSchConnectionAttributes.TIMEOUT_ATTR, DEFAULT_TIMEOUT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#getUsername()
	 */
	@Override
	public String getUsername() {
		return fWorkingAttributes.getAttribute(JSchConnectionAttributes.USERNAME_ATTR, JSchConnection.EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnection#getWorkingCopy()
	 */
	@Override
	public IRemoteConnectionWorkingCopy getWorkingCopy() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return fIsDirty;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.internal.jsch.core.JSchConnection#isPasswordAuth()
	 */
	@Override
	public boolean isPasswordAuth() {
		return fWorkingAttributes.getBoolean(JSchConnectionAttributes.IS_PASSWORD_ATTR, DEFAULT_IS_PASSWORD);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#save()
	 */
	@Override
	public IRemoteConnection save() {
		JSchConnectionAttributes info = fOriginal.getInfo();
		info.getAttributes().clear();
		info.getAttributes().putAll(fWorkingAttributes.getAttributes());
		info.setSecureAttributes(fWorkingAttributes.getSecureAttributes());
		if (!getName().equals(info.getName())) {
			info.setName(getName());
			getManager().remove(fOriginal);
			fOriginal.fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_RENAMED);
		}
		info.save();
		getManager().add(fOriginal);
		fIsDirty = false;
		return fOriginal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAddress(java.lang.String)
	 */
	@Override
	public void setAddress(String address) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.ADDRESS_ATTR, address);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String key, String value) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(key, value);
	}

	public void setIsPasswordAuth(boolean flag) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.IS_PASSWORD_ATTR, Boolean.toString(flag));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setName(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		fIsDirty = true;
		fWorkingAttributes.setName(name);
	}

	public void setPassphrase(String passphrase) {
		fIsDirty = true;
		fWorkingAttributes.setSecureAttribute(JSchConnectionAttributes.PASSPHRASE_ATTR, passphrase);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPassword(java.lang.String)
	 */
	@Override
	public void setPassword(String password) {
		fIsDirty = true;
		fWorkingAttributes.setSecureAttribute(JSchConnectionAttributes.PASSWORD_ATTR, password);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPort(int)
	 */
	@Override
	public void setPort(int port) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.PORT_ATTR, Integer.toString(port));
	}

	/**
	 * Sets the proxy command. Set to empty string for no command
	 * 
	 * @param proxyCommand
	 *            proxy command
	 */
	public void setProxyCommand(String proxyCommand) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.PROXYCOMMAND_ATTR, proxyCommand);
	}

	/**
	 * Sets the proxy connection name. Set to empty string for no proxy connection
	 * 
	 * @param proxyCommand
	 *            proxy connection name
	 */
	public void setProxyConnectionName(String proxyConnectionName) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.PROXYCONNECTION_ATTR, proxyConnectionName);
	}

	public void setTimeout(int timeout) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.TIMEOUT_ATTR, Integer.toString(timeout));
	}

	public void setUseLoginShell(boolean flag) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.USE_LOGIN_SHELL_ATTR, Boolean.toString(flag));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setUsername(java.lang.String)
	 */
	@Override
	public void setUsername(String userName) {
		fIsDirty = true;
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.USERNAME_ATTR, userName);
	}
}
