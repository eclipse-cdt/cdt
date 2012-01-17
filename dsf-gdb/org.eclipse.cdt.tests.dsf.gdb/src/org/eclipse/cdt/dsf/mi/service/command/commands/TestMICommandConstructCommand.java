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
import org.eclipse.cdt.dsf.mi.service.command.commands.MICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
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
		MICommand<MIInfo> target = new MICommand<MIInfo>(new TestContext(),
				"-test-operation");
		target.setOptions(new String[] { "-a a_test\\with slashes",
				"-b \"hello\"", "-c c_test" });
		target.setParameters(new String[] { "-param1 param", "param2",
				"-param3" });

		// Act
		String result = target.constructCommand();

		// Assert
		assertEquals(
				"Wrong syntax for command",
				"-test-operation \"-a a_test\\\\with slashes\" \"-b \\\"hello\\\"\" \"-c c_test\" -- \"-param1 param\" param2 -param3\n",
				result);
	}

	private class TestContext implements IDMContext {

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
