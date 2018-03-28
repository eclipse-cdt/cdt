/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
