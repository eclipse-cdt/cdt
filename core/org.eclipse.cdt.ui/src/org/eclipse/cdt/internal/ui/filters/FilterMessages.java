/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.filters;

import org.eclipse.osgi.util.NLS;

public final class FilterMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.filters.FilterMessages";//$NON-NLS-1$

	private FilterMessages() {
		// Do not instantiate
	}

	public static String CustomFiltersDialog_title;
	public static String CustomFiltersDialog_patternInfo;
	public static String CustomFiltersDialog_enableUserDefinedPattern;
	public static String CustomFiltersDialog_filterList_label;
	public static String CustomFiltersDialog_description_label;
	public static String CustomFiltersDialog_SelectAllButton_label;
	public static String CustomFiltersDialog_DeselectAllButton_label;
	public static String OpenCustomFiltersDialogAction_text;
	public static String FilterDescriptor_filterDescriptionCreationError_message;
	public static String FilterDescriptor_filterCreationError_message;

	static {
		NLS.initializeMessages(BUNDLE_NAME, FilterMessages.class);
	}
}