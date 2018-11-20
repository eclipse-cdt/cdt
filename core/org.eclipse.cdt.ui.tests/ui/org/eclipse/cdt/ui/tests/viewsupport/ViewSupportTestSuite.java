/*******************************************************************************
 * Copyright (c) 2006, 2017 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.viewsupport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

		AsyncViewerTest.class, })
public class ViewSupportTestSuite extends TestSuite {

}
