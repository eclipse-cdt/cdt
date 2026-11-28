/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.junit.Test;

public class TestMIExecInterrupt {
	private static final IContainerDMContext CONTAINER = new IContainerDMContext() {
		@Override
		public String getSessionId() {
			return "test"; //$NON-NLS-1$
		}

		@Override
		public IDMContext[] getParents() {
			return new IDMContext[0];
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}
	};

	/**
	 * GDB does not accept --thread or --thread-group together with --all.
	 */
	@Test
	public void allThreadsHasNoThreadOrGroupOption() {
		MIExecInterrupt command = new MIExecInterrupt(CONTAINER, true);
		assertEquals("-exec-interrupt --all\n", command.constructCommand("i1", null, -1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("-exec-interrupt --all\n", command.constructCommand("i1", "1", -1)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void oneThreadGroup() {
		MIExecInterrupt command = new MIExecInterrupt(CONTAINER, false);
		assertEquals("-exec-interrupt --thread-group i1\n", command.constructCommand("i1", null, -1)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
