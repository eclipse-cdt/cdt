/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.rapi;

/**
 * This class represents a connected WinCE-based remote device.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @author Radoslav Gerganov
 */
public abstract class IRapiDevice extends IUnknown {

  public IRapiDevice(int addr) {
    super(addr);
  }

  /**
   * Returns an instance of <code>IRapiSession</code> for this remote device.
   * Use {@link IRapiSession#release()} to release this instance when it is
   * no longer needed.
   * @return an instance of <code>IRapiSession</code>
   * @throws RapiException if an error occurs.
   */
  public abstract IRapiSession createSession() throws RapiException;

  /**
   * Returns information about the connection between this remote device and the desktop.
   * @return <code>RapiConnectionInfo</code> object containing information about
   * the connection between this remote device and the desktop.
   * @throws RapiException if an error occurs.
   */
  public abstract RapiConnectionInfo getConnectionInfo() throws RapiException;

  /**
   * Returns information about this remote device.
   * @return <code>RapiDeviceInfo</code> object containing information about this remote
   * device.
   * @throws RapiException if an error occurs.
   */
  public abstract RapiDeviceInfo getDeviceInfo() throws RapiException;

  /**
   * Tests whether this device is connected.
   * @return <code>true</code> if this device is connected;<code>false</code> otherwise.
   * @throws RapiException if an error occurs.
   */
  public abstract boolean isConnected() throws RapiException;

}