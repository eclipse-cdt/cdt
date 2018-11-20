/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software System and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - Base API and implementation
 *     John Dallaway - GDB JTAG implementation (bug 538282)
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.tests.launch;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@SuppressWarnings("restriction")
@RunWith(Suite.class)
@Suite.SuiteClasses({ GDBJtagLaunchTest.class })

public class SuiteGDBJtag {

	@BeforeClass
	public static void before() {
		BaseParametrizedTestCase.resetGlobalState();
	}

}
