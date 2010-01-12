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

import java.util.HashMap;
import java.util.Map;

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
    	Map<String, String> versions = new HashMap<String, String>(10);

    	versions.put("GNU gdb 6.8.50.20080730", "6.8.50.20080730");
    	versions.put("GNU gdb (GDB) 6.8.50.20080730-cvs", "6.8.50.20080730");
    	versions.put("GNU gdb (Ericsson GDB 1.0-10) 6.8.50.20080730-cvs", "6.8.50.20080730");
    	versions.put("GNU gdb (GDB) Fedora (7.0-3.fc12)", "7.0");
    	versions.put("GNU gdb 6.8.0.20080328-cvs (cygwin-special)", "6.8");  // Special for cygwin
    	versions.put("GNU gdb 7.0", "7.0");
    	versions.put("GNU gdb Fedora (6.8-27.el5)", "6.8");
    	versions.put("GNU gdb Red Hat Linux (6.3.0.0-1.162.el4rh)", "6.3.0.0");

    	for (String key : versions.keySet()) {
    		assertEquals("From \"" + key + "\"", versions.get(key), LaunchUtils.getGDBVersionFromText(key));
    	}
    }
}
