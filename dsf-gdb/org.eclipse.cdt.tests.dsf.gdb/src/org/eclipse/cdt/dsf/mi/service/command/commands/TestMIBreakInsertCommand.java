/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.junit.Test;

/**
 * Verifies that the break insert MI command have the correct path substitution.
 * 
 * @author qtobsod
 * 
 */
public class TestMIBreakInsertCommand {

	@Test
	public void pathShouldNotContainDoubleBackSlashes() {
		MIBreakInsert target = new MIBreakInsert(new TestContext(), false,
				false, null, 1, "c:\\test\\this\\path:14", 4, false);

		assertEquals("Wrong syntax for command",
				"-break-insert -i 1 -p 4 c:\\test\\this\\path:14\n", target
						.constructCommand());
	}

	@Test
	public void pathWithSlashesShouldNotBeSubstituted() {
		MIBreakInsert target = new MIBreakInsert(new TestContext(), false,
				false, null, 1, "/test/this/path:14", 4, false);

		assertEquals("Wrong syntax for command",
				"-break-insert -i 1 -p 4 /test/this/path:14\n", target
						.constructCommand());
	}

	private class TestContext implements IBreakpointsTargetDMContext {

		@Override
		public IDMContext[] getParents() {
			return null;
		}

		@Override
		public String getSessionId() {
			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return null;
		}
	}

}
