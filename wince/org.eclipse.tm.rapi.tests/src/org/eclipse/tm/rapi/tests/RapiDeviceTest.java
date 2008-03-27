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
package org.eclipse.tm.rapi.tests;

import org.eclipse.tm.rapi.IRapiDesktop;
import org.eclipse.tm.rapi.IRapiDevice;
import org.eclipse.tm.rapi.RapiConnectionInfo;
import org.eclipse.tm.rapi.RapiDeviceInfo;
import org.eclipse.tm.rapi.RapiException;

public class RapiDeviceTest extends RapiTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  IRapiDevice getDevice() throws RapiException {
    desktop = IRapiDesktop.getInstance();
    enumDevices = desktop.enumDevices();
    return enumDevices.next();
  }
  
  public void testCreateSession() throws RapiException {
    device = getDevice();
    session = device.createSession();
    assertNotNull("IRapiDevice.createSession() returned null", session);
  }

  public void testGetConnectionInfo() throws RapiException {
    device = getDevice();
    RapiConnectionInfo connectionInfo = device.getConnectionInfo();
    int connectionType = connectionInfo.connectionType;
    assertTrue("Unknown connectionType: " + connectionType, 
        connectionType >= 0 && connectionType <= 3);
  }

  public void testGetDeviceInfo() throws RapiException {
    device = getDevice();
    RapiDeviceInfo deviceInfo = device.getDeviceInfo();
    assertNotNull("deviceInfo.id is null", deviceInfo.id);
    assertNotNull("deviceInfo.name is null", deviceInfo.name);
    assertNotNull("deviceInfo.platform is null", deviceInfo.platform);
    //TODO: make some reasonable checks for the version numbers
  }

  public void testIsConnected() throws RapiException {
    device = getDevice();
    boolean connected = device.isConnected();
    assertTrue("device should be connected", connected);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
}
