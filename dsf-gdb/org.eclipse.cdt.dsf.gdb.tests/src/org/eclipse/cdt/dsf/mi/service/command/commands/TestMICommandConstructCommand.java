/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Marc Khouzam (Ericsson) - Fix NPE (bug 369583)
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Test;

/**
 * Test verifying that the construct command method handles separators and
 * escaping correctly
 *
 * @author qtobsod
 *
 */
public class TestMICommandConstructCommand {

	@Test
	public void multipleParametersShouldHaveCorrectSeparators() {
		// Setup
		MICommand<MIInfo> target = new MICommand<>(new TestContext(), "-test-operation");
		target.setOptions(new String[] { "-a a_test\\with slashes", "-b \"hello\"", "-c c_test" });
		target.setParameters(new String[] { "-param1 param", "param2", "-param3" });

		// Act
		String result = target.constructCommand();

		// Assert
		assertEquals("Wrong syntax for command",
				"-test-operation \"-a a_test\\\\with slashes\" \"-b \\\"hello\\\"\" \"-c c_test\" -- \"-param1 param\" param2 -param3\n",
				result);
	}

	private class TestContext implements IBreakpointsTargetDMContext {
		private DsfSession session = null;

		public TestContext() {
			session = DsfSession.startSession(new DefaultDsfExecutor(GdbPlugin.PLUGIN_ID), GdbPlugin.PLUGIN_ID);
		}

		@Override
		public IDMContext[] getParents() {
			return new IDMContext[] { new GDBControlDMContext(getSessionId(), "1") };
		}

		@Override
		public String getSessionId() {
			return session.getId();
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}
	}

}
