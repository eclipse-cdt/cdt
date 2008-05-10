/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - derived from SshConnectorService
 *******************************************************************************/
package org.eclipse.rse.internal.connectorservice.wince;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.BasicConnectorService;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.internal.services.wince.IRapiSessionProvider;
import org.eclipse.tm.rapi.IRapiDesktop;
import org.eclipse.tm.rapi.IRapiDevice;
import org.eclipse.tm.rapi.IRapiEnumDevices;
import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.Rapi;
import org.eclipse.tm.rapi.RapiException;


/**
 * Creates ActiveSync/RAPI2 connections to WinCE-based device.
 */
public class WinCEConnectorService extends BasicConnectorService implements IRapiSessionProvider {

  IRapiDesktop desktop = null;
  IRapiEnumDevices enumDevices = null;
  IRapiDevice device = null;
  IRapiSession session = null;
  
  public WinCEConnectorService(IHost host) {
    super(Messages.WinCEConnectorService_0, Messages.WinCEConnectorService_1, host, 0);
  }
  
  protected void internalConnect(IProgressMonitor monitor) throws Exception {
    fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);
    Rapi.initialize(Rapi.COINIT_MULTITHREADED);
    try {
      desktop = IRapiDesktop.getInstance();
      enumDevices = desktop.enumDevices();
      device = enumDevices.next();
      session = device.createSession();
      session.init();
    } catch (RapiException re) {
      //TODO externalize the error message
      throw new Exception("Cannot connect to the remote device (" + re.getMessage() + ")", re); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  protected void internalDisconnect(IProgressMonitor monitor) throws Exception {
    fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
    if (session != null) {
      session.uninit();
      session.release();
      session = null;
    }
    if (device != null) {
      device.release();
      device = null;
    }
    if (enumDevices != null) {
      enumDevices.release();
      enumDevices = null;
    }
    if (desktop != null) {
      desktop.release();
      desktop = null;
    }
  }

  public boolean isConnected() {
    if (device != null) {
      try {
        return device.isConnected();
      } catch (RapiException e) {
        //ignore
      }
    }
    return false;
  }

  public IRapiSession getSession() {
    return session;
  }

}
