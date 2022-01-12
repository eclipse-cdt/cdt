/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson)	- Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This suite runs all suites that are part of the tests automatically run with
 * each CDT build.
 *
 *
 * This suite runs tests for gdb versions specified by java system variable "cdt.tests.dsf.gdb.versions", i.e.
 * -Dcdt.tests.dsf.gdb.versions=gdb.7.7,gdbserver.7.7,gdb.7.11
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ SuiteGdb.class, })
public class AutomatedSuite {
}