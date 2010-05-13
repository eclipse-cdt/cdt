/*******************************************************************************
 * Copyright (c) 2005, 2010 Freescale, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String AddMemBlockDlg_enterAddrAndMemSpace;
	public static String AddMemBlockDlg_enterExpression;
	public static String AddMemBlockDlg_or;
	public static String AddMemBlockDlg_MonitorMemory;
	
	public static String AddMemBlocks_title;
	public static String AddMemBlocks_noMemoryBlock;
	public static String AddMemBlocks_failed;
	public static String AddMemBlocks_input_invalid;

}

