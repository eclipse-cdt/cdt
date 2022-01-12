/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.core.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.lsp.internal.core.LspCoreMessages;
import org.eclipse.cdt.lsp.internal.core.ShowStatus;
import org.eclipse.osgi.util.NLS;
import org.junit.Test;

public class ShowStatusTest {

	@Test
	public void busy() {
		StringBuilder sb = new StringBuilder();
		ShowStatus show = new ShowStatus(() -> "Busy", sb::append);
		show.accept(() -> 100500);
		assertEquals(NLS.bind(LspCoreMessages.ShowStatus_busy, "Busy", 100500), sb.toString());
	}

	@Test
	public void idle() {
		StringBuilder sb = new StringBuilder();
		ShowStatus show = new ShowStatus(() -> "Idle", sb::append);
		show.accept(() -> 0);
		assertEquals(NLS.bind(LspCoreMessages.ShowStatus_idle, "Idle"), sb.toString());
	}

}
