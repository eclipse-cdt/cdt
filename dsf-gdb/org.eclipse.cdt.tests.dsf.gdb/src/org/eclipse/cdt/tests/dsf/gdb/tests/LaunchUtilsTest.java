/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc-Andre Laperle - Added test for the gdb version string converter
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchUtilsTest extends TestCase {
	
    @Before
    public void init() {
    }

    @After
    public void shutdown() {
    }
	
    @Test
	public void testGetGDBVersionFromText(){
		String test1 = "GNU gdb 6.8.50.20080730";
		String test2 = "GNU gdb (GDB) 6.8.50.20080730-cvs";
		String test3 = "GNU gdb (Ericsson GDB 1.0-10) 6.8.50.20080730-cvs";
        String test4 = "GNU gdb (GDB) Fedora (7.0-3.fc12)";
        String test5 = "GNU gdb 6.8.0.20080328-cvs (cygwin-special)";
		String test6 = "GNU gdb 7.0";

        assertEquals("6.8.50.20080730", LaunchUtils.getGDBVersionFromText(test1));
        assertEquals("6.8.50.20080730", LaunchUtils.getGDBVersionFromText(test2));
        assertEquals("6.8.50.20080730", LaunchUtils.getGDBVersionFromText(test3));
        assertEquals("7.0", LaunchUtils.getGDBVersionFromText(test4));
        assertEquals("6.8", LaunchUtils.getGDBVersionFromText(test5));
        assertEquals("7.0", LaunchUtils.getGDBVersionFromText(test6));

        String appleTest1 = "GNU gdb 6.3.50-20050815 (Apple version gdb-696) (Sat Oct 20 18:20:28 GMT 2007)";
        String appleTest2 = "GNU gdb 6.3.50-20050815 (Apple version gdb-966) (Tue Mar 10 02:43:13 UTC 2009)";
        String appleTest3 = "GNU gdb 6.3.50-20050815 (Apple version gdb-1346) (Fri Sep 18 20:40:51 UTC 2009)";
        String appleTest4 = "GNU gdb 7.0 (Apple version gdb-1) (Fri Sep 18 20:40:51 UTC 2009)";
        String appleTest5 = "GNU gdb 7.0-20050815 (Apple version gdb-01) (Fri Sep 18 20:40:51 UTC 2009)";

        assertEquals("6.3.50"+LaunchUtils.MACOS_GDB_MARKER+"696", LaunchUtils.getGDBVersionFromText(appleTest1));
        assertEquals("6.3.50"+LaunchUtils.MACOS_GDB_MARKER+"966", LaunchUtils.getGDBVersionFromText(appleTest2));
        assertEquals("6.3.50"+LaunchUtils.MACOS_GDB_MARKER+"1346", LaunchUtils.getGDBVersionFromText(appleTest3));
        assertEquals("7.0"+LaunchUtils.MACOS_GDB_MARKER+"1", LaunchUtils.getGDBVersionFromText(appleTest4));
        assertEquals("7.0"+LaunchUtils.MACOS_GDB_MARKER+"01", LaunchUtils.getGDBVersionFromText(appleTest5));
	}
}
