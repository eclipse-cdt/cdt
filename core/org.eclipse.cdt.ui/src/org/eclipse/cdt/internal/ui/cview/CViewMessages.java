/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.osgi.util.NLS;

public final class CViewMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.cview.CViewMessages";//$NON-NLS-1$

	private CViewMessages() {
		// Do not instantiate
	}

	public static String OpenWithMenu_label;
	public static String BuildAction_label;
	public static String RebuildAction_label;
	public static String CleanAction_label;
	public static String CopyAction_title;
	public static String CopyAction_toolTip;
	public static String PasteAction_title;
	public static String PasteAction_toolTip;
	public static String NewWizardsActionGroup_new;
	public static String DefaultAction_WIP;
	public static String DefaultAction_workInProgress;
	public static String CView_binaries;
	public static String CView_archives;
	public static String LibraryRefContainer_Libraries;
	public static String IncludeRefContainer_Includes;
	public static String CView_statusLine;
	public static String CopyToClipboardProblemDialog_title;
	public static String CopyToClipboardProblemDialog_message;
	public static String SelectionTransferDropAdapter_error_title;
	public static String SelectionTransferDropAdapter_error_message;
	public static String SelectionTransferDropAdapter_error_exception;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CViewMessages.class);
	}
}