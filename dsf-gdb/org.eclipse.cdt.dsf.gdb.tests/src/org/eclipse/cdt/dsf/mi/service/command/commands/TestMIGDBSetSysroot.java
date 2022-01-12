/*******************************************************************************
 * Copyright (c) 2016 Ingenico.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ingenico	- Sysroot with spaces (Bug 497693)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControlDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.junit.Test;

/**
 * Verifies that the set sysroot MI command don't add double quotes if path contains space.
 *
 */
public class TestMIGDBSetSysroot {

	@Test
	public void pathWithSpaceShouldNotBe() {
		MIGDBSetSysroot setSysrootCommand = new MIGDBSetSysroot(new TestContext(), "/tmp/test with space/");
		assertEquals("Wrong syntax for command", "-gdb-set sysroot /tmp/test with space/\n",
				setSysrootCommand.constructCommand());
	}

	@Test
	public void pathWithDoubleQuotesShouldNotBe() {
		MIGDBSetSysroot setSysrootCommand = new MIGDBSetSysroot(new TestContext(), "/tmp/test with\"double quotes/");
		assertEquals("Wrong syntax for command", "-gdb-set sysroot /tmp/test with\"double quotes/\n",
				setSysrootCommand.constructCommand());

	}

	private class TestContext implements ICommandControlDMContext {
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

		@Override
		public String getCommandControlId() {
			return null;
		}
	}

}
