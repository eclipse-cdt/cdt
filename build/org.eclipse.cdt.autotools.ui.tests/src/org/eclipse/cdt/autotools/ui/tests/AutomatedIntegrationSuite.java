/*******************************************************************************
 * Copyright (c) 2010, 2015 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Marc Khouzam (Ericsson) - Skip tests if autotools binaries are not available
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.tests;

import org.eclipse.cdt.autotools.ui.tests.autoconf.AutoconfTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		/*
		 *
		 * 	TestToolActions.class,
		 *  TestEnvironmentVars.class,
		 *  TestMakeTargets.class,
		 *  SetConfigurationParameterTest.class,
		 */
		AutoconfTestSuite.class })

public class AutomatedIntegrationSuite {
}
