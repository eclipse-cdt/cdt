/*******************************************************************************
 * Copyright (c) 2008, 2015 Red Hat Inc..
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
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import org.eclipse.cdt.autotools.tests.editors.EditorTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** On Windows requires either Cygwin or MinGW to be in PATH */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AutotoolsProject0Test.class, AutotoolsProjectNatureTest.class, AutotoolsProject1Test.class,
		AutotoolsProject2Test.class, AutotoolsVirtualFolderTest.class, AutotoolsEnvironmentVarTest.class,
		LibtoolGCCBuildCommandParserTest.class, UpdateConfigureTest.class, EditorTestSuite.class })
public class AutomatedIntegrationSuite {
}
