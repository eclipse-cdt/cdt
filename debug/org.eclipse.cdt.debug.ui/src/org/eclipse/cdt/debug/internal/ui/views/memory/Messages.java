/*******************************************************************************
 * Copyright (c) 2005, 2010 Freescale, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String AddMemBlockDlg_enterAddrAndMemSpace;
	public static String AddMemBlockDlg_enterExpression;
	public static String AddMemBlockDlg_or;
	public static String AddMemBlockDlg_MonitorMemory;

	public static String AddMemBlocks_title;
	public static String AddMemBlocks_noMemoryBlock;
	public static String AddMemBlocks_failed;
	public static String AddMemBlocks_input_invalid;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
