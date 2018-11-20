/*******************************************************************************
 * Copyright (c) 2015 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

// Add additional test case classes below
@SuiteClasses({ PersistentSettingsManagerTest.class, VisualizerVirtualBoundsGraphicObjectTest.class, })
public class AutomatedIntegrationSuite {
	// Often overriding BeforeClass method here
}
