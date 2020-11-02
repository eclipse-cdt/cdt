/*******************************************************************************
 * Copyright (c) 2007 - 2020 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *     John Dallaway - Extend CMainTab2, bug 568454
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.internal.ui;

import org.eclipse.cdt.launch.ui.CMainTab2;

public class GDBJtagDSFCMainTab extends CMainTab2 {

	public GDBJtagDSFCMainTab() {
		super(CMainTab2.INCLUDE_BUILD_SETTINGS);
	}

	@Override
	public String getId() {
		// return historic ID for compatibility with existing consumers
		return "org.eclipse.cdt.dsf.gdb.launch.mainTab"; //$NON-NLS-1$
	}

}
