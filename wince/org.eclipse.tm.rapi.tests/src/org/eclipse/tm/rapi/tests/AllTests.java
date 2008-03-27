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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the Java wrappers of RAPI2.
 * <b>
 * Establishing an ActiveSync connection with a device or emulator is needed 
 * before running these tests.
 * </b>
 * @author Radoslav Gerganov
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.eclipse.tm.rapi");
    //$JUnit-BEGIN$
    suite.addTestSuite(RapiDesktopTest.class);
    suite.addTestSuite(RapiEnumDevicesTest.class);
    suite.addTestSuite(RapiDeviceTest.class);
    suite.addTestSuite(RapiSessionTest.class);
    //$JUnit-END$
    return suite;
  }

}
