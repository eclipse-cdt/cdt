/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui.test;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui.test.PersistentSettingsManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)

// Add additional test case classes below
@SuiteClasses({
    PersistentSettingsManagerTest.class,
})	
public class AllTests {
	// Often overriding BeforeClass method here
}
