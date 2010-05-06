/*******************************************************************************
 * Copyright (c) 2008 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Bruce Griffith, Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (allow
 *                connections via serial ports and pipes).
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.jtagdevice;

import org.eclipse.cdt.debug.gdbjtag.core.Activator;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;

public class GDBJtagDeviceContribution {

	private String deviceId;
	private String deviceName;
	private String deviceClassName;
	private IGDBJtagDevice device;
	private String deviceClassBundleName;
	private String deviceDefaultConnection;

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return this.deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the deviceName
	 */
	public String getDeviceName() {
		return this.deviceName;
	}

	/**
	 * @param deviceName the deviceName to set
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return the deviceClassName
	 */
	public String getDeviceClassName() {
		return this.deviceClassName;
	}

	/**
	 * @param deviceClassName the deviceClassName to set
	 */
	public void setDeviceClassName(String deviceClassName) {
		this.deviceClassName = deviceClassName;
	}

	public String getDeviceClassBundleName() {
		return deviceClassBundleName;
	}

	public void setDeviceClassBundleName(String deviceClassBundleName) {
		this.deviceClassBundleName = deviceClassBundleName;
	}

	/**
	 * @since 7.0
	 */
	public void setDeviceDefaultConnection(String connection) {
		this.deviceDefaultConnection = connection;
	}

	public IGDBJtagDevice getDevice() throws NullPointerException {
		if (device != null) return device;
		Object o = null;
		try {
			o = Platform.getBundle(deviceClassBundleName).loadClass(deviceClassName).newInstance();
			if (o instanceof IGDBJtagConnection) {
				((IGDBJtagConnection) o).setDefaultDeviceConnection(deviceDefaultConnection);
			}
			device = (IGDBJtagDevice) o;
			return device;
		} catch (Exception e) {
			Activator.log(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR,
					"Error instantiating " + getDeviceClassName() + " class", e)); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

	}

}
