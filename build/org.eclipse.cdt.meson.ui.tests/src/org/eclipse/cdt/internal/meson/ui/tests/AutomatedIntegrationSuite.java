/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for use in Meson testing
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ NewMesonProjectTest.class, NewMesonConfigureTest.class, NewManualNinjaTest.class })
public class AutomatedIntegrationSuite {

}
