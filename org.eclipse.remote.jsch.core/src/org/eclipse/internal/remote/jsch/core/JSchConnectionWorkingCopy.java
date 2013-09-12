/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.jsch.core;

import java.util.Collections;
import java.util.Map;

import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

/**
 * @since 5.0
 */
public class JSchConnectionWorkingCopy extends JSchConnection implements IRemoteConnectionWorkingCopy {
	private final JSchConnectionAttributes fWorkingAttributes;
	private final JSchConnection fOriginal;

	public JSchConnectionWorkingCopy(JSchConnection connection) {
		super(connection.getName(), connection.getRemoteServices());
		fWorkingAttributes = connection.getInfo().copy();
		fOriginal = connection;
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

	@Override
	public String getKeyFile() {
		return fWorkingAttributes.getAttribute(JSchConnectionAttributes.KEYFILE_ATTR, EMPTY_STRING);
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
	 * @see org.eclipse.internal.remote.jsch.core.JSchConnection#getPassphrase()
	 */
	@Override
	public String getPassphrase() {
		return fWorkingAttributes.getSecureAttribute(JSchConnectionAttributes.PASSPHRASE_ATTR, EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.internal.remote.jsch.core.JSchConnection#getPassword()
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
	 * @see org.eclipse.internal.remote.jsch.core.JSchConnection#getTimeout()
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.internal.remote.jsch.core.JSchConnection#isPasswordAuth()
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
	public void save() {
		JSchConnectionAttributes info = fOriginal.getInfo();
		info.getAttributes().clear();
		info.getAttributes().putAll(fWorkingAttributes.getAttributes());
		info.getSecureAttributes().clear();
		info.getSecureAttributes().putAll(fWorkingAttributes.getSecureAttributes());
		if (!getName().equals(info.getName())) {
			info.setName(getName());
			getManager().remove(fOriginal);
			fOriginal.fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_RENAMED);
		}
		info.save();
		getManager().add(fOriginal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.ADDRESS_ATTR, address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		fWorkingAttributes.setAttribute(key, value);
	}

	public void setIsPasswordAuth(boolean flag) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.IS_PASSWORD_ATTR, Boolean.toString(flag));
	}

	public void setKeyFile(String keyFile) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.KEYFILE_ATTR, keyFile);
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
	public void setName(String name) {
		fWorkingAttributes.setName(name);
	}

	public void setPassphrase(String passphrase) {
		fWorkingAttributes.setSecureAttribute(JSchConnectionAttributes.PASSPHRASE_ATTR, passphrase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		fWorkingAttributes.setSecureAttribute(JSchConnectionAttributes.PASSWORD_ATTR, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setPort(int)
	 */
	public void setPort(int port) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.PORT_ATTR, Integer.toString(port));
	}

	public void setTimeout(int timeout) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.TIMEOUT_ATTR, Integer.toString(timeout));
	}

	public void setUseLoginShell(boolean flag) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.USE_LOGIN_SHELL_ATTR, Boolean.toString(flag));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionWorkingCopy#setUsername(java.lang.String)
	 */
	public void setUsername(String userName) {
		fWorkingAttributes.setAttribute(JSchConnectionAttributes.USERNAME_ATTR, userName);
	}
}
