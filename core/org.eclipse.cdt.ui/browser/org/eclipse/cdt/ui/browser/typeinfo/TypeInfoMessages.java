/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class TypeInfoMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.browser.typeinfo.TypeInfoMessages";//$NON-NLS-1$

	private TypeInfoMessages() {
		// Do not instantiate
	}

	public static String TypeSelectionDialog_lowerLabel;
	public static String TypeSelectionDialog_upperLabel;
	public static String TypeSelectionDialog_filterLabel;
	public static String TypeSelectionDialog_filterNamespaces;
	public static String TypeSelectionDialog_filterClasses;
	public static String TypeSelectionDialog_filterStructs;
	public static String TypeSelectionDialog_filterTypedefs;
	public static String TypeSelectionDialog_filterEnums;
	public static String TypeSelectionDialog_filterUnions;
	public static String TypeSelectionDialog_filterFunctions;
	public static String TypeSelectionDialog_filterVariables;
	public static String TypeSelectionDialog_filterMacros;
	public static String TypeSelectionDialog_filterLowLevelTypes;
	public static String TypeInfoLabelProvider_globalScope;
	public static String TypeInfoLabelProvider_dash;
	public static String TypeInfoLabelProvider_colon;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TypeInfoMessages.class);
	}
}