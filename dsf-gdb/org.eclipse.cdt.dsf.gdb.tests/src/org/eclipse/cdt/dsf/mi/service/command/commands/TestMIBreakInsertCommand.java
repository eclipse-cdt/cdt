/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Test;

/**
 * Verifies that the break insert MI command have the correct path substitution.
 * 
 * @author qtobsod
 * 
 */
public class TestMIBreakInsertCommand {

	@Test
	public void pathWithSlashesShouldNotBeSubstituted() {
		MIBreakInsert target = new MIBreakInsert(new TestContext(), false,
				false, null, 1, "/test/this/path:14", 4, false);

		assertEquals("Wrong syntax for command",
				"-break-insert -i 1 -p 4 /test/this/path:14\n", target
						.constructCommand());
	}

	private class TestContext implements IBreakpointsTargetDMContext {
		private DsfSession session = null;

		public TestContext() {
			session = DsfSession.startSession(new DefaultDsfExecutor(GdbPlugin.PLUGIN_ID), GdbPlugin.PLUGIN_ID);
		}
		
		@Override
		public IDMContext[] getParents() {
			return new IDMContext[] {new GDBControlDMContext(getSessionId(), "1")};
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
