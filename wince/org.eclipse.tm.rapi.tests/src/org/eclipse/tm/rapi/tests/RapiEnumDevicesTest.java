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
import org.eclipse.tm.rapi.RapiException;

public class RapiEnumDevicesTest extends RapiTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testNext() throws RapiException {
    desktop = IRapiDesktop.getInstance();
    enumDevices = desktop.enumDevices();
    device = enumDevices.next();
    assertNotNull("IRapiEnumDevices.next() returned null", device);
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
