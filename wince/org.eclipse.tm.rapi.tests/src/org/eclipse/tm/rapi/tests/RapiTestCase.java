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
import org.eclipse.tm.rapi.IRapiEnumDevices;
import org.eclipse.tm.rapi.IRapiSession;
import org.eclipse.tm.rapi.Rapi;

import junit.framework.TestCase;

public class RapiTestCase extends TestCase {

  protected IRapiDesktop desktop = null;
  protected IRapiEnumDevices enumDevices = null;
  protected IRapiDevice device = null;
  protected IRapiSession session = null;
  
  protected void setUp() throws Exception {
    super.setUp();
    desktop = null;
    enumDevices = null;
    device = null;
    session = null;
    Rapi.initialize(Rapi.COINIT_MULTITHREADED);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    if (desktop != null) {
      desktop.release();
    }
    if (enumDevices != null) {
      enumDevices.release();
    }
    if (device != null) {
      device.release();
    }
    if (session != null) {
      session.release();
    }
    Rapi.uninitialize();
  }

}
