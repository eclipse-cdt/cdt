/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders Canada and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.tests;

import org.eclipse.launchbar.core.PerTargetLaunchConfigProviderTest;
import org.eclipse.launchbar.core.internal.LaunchBarManager2Test;
import org.eclipse.launchbar.core.internal.LaunchBarManagerTest;
import org.eclipse.launchbar.core.internal.target.LaunchTargetTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TargetAttributesTest.class, LaunchTargetTest.class, LaunchBarManager2Test.class,
		LaunchBarManagerTest.class, PerTargetLaunchConfigProviderTest.class })
public class AutomatedIntegrationSuite {

}
