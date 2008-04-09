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
package org.eclipse.tm.internal.rapi;

import org.eclipse.tm.rapi.IRapiDevice;
import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.RapiConnectionInfo;
import org.eclipse.tm.rapi.RapiDeviceInfo;
import org.eclipse.tm.rapi.RapiException;

/**
 * Implementation of <code>IRapiDevice</code>.
 * 
 * @author Radoslav Gerganov
 */
public class RapiDevice extends IRapiDevice {

  public RapiDevice(int addr) {
    super(addr);
  }
  
  public IRapiSession createSession() throws RapiException {
    int[] ppISession = new int[1];
    int rc = CreateSession(addr, ppISession);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("CreateSession failed", rc); //$NON-NLS-1$
    }
    return new RapiSession(ppISession[0]);
  }
  
  public RapiConnectionInfo getConnectionInfo() throws RapiException {
    RapiConnectionInfo connInfo = new RapiConnectionInfo();
    int rc = GetConnectionInfo(addr, connInfo);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("GetConnectionInfo failed", rc);       //$NON-NLS-1$
    }
    return connInfo;
  }

  public RapiDeviceInfo getDeviceInfo() throws RapiException {
    RapiDeviceInfo devInfo = new RapiDeviceInfo();
    int rc = GetDeviceInfo(addr, devInfo);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("GetDeviceInfo failed", rc); //$NON-NLS-1$
    }
    return devInfo;
  }
  
  public boolean isConnected() throws RapiException {
    int[] status = new int[1];
    int rc = GetConnectStat(addr, status);
    if (rc != Rapi.NOERROR) {
      throw new RapiException("GetConnectStat failed", rc); //$NON-NLS-1$
    }
    return status[0] == 1;
  }
  
  public String toString() {
    return "[RapiDevice] addr: " + Integer.toHexString(addr); //$NON-NLS-1$
  }

  private final native int CreateSession(int addr, int[] ppISession);
  private final native int GetConnectionInfo(int addr, RapiConnectionInfo pConnInfo);
  private final native int GetDeviceInfo(int addr, RapiDeviceInfo pDevInfo);
  private final native int GetConnectStat(int addr, int[] status);
}
