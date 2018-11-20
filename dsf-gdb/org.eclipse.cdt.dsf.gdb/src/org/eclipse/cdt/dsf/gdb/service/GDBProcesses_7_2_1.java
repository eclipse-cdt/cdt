/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Version for GDB 7.2.1, which does not need a workaround
 * for a bug in GDB 7.2 (Bug 352998)
 *
 * @since 4.1
 */
public class GDBProcesses_7_2_1 extends GDBProcesses_7_2 {

	public GDBProcesses_7_2_1(DsfSession session) {
		super(session);
	}

	@Override
	protected boolean needFixForGDB72Bug352998() {
		return false;
	}
}
