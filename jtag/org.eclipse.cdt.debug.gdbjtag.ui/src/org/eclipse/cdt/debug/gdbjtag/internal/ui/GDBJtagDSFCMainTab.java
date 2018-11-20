/*******************************************************************************
 * Copyright (c) 2007 - 2015 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.internal.ui;

import org.eclipse.cdt.dsf.gdb.internal.ui.launching.CMainTab;

/**
 * @since 7.0
 */
@SuppressWarnings("deprecation")
public class GDBJtagDSFCMainTab extends CMainTab {

	public GDBJtagDSFCMainTab() {
		super(CMainTab.INCLUDE_BUILD_SETTINGS);
	}
}
