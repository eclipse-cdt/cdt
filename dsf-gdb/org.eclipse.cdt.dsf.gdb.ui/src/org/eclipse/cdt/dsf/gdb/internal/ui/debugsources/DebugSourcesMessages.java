/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class DebugSourcesMessages extends NLS {

	public static String DebugSourcesMessages_name_column;
	public static String DebugSourcesMessages_path_column;
	public static String DebugSourcesMessages_unknown;

	public static String DebugSourcesMessages_filter_search_tooltip;
	public static String DebugSourcesMessages_sort_name_column_tooltip;
	public static String DebugSourcesMessages_sort_path_column_tooltip;

	public static String DebugSourcesExpandAction_name;
	public static String DebugSourcesExpandAction_description;
	public static String DebugSourcesCollapseAction_name;
	public static String DebugSourcesCollapseAction_description;
	public static String DebugSourcesFlattendedTree_name;
	public static String DebugSourcesFlattendedTree_description;
	public static String DebugSourcesNormalTree_description;
	public static String DebugSourcesNormalTree_name;
	public static String DebugSourcesShowExistingFilesOnly_description;
	public static String DebugSourcesShowExistingFilesOnly_name;
	public static String DebugSourcesView_unrooted;

	public static String GdbDebugSourcesPreferences_name;

	static {
		// initialize resource bundle
		NLS.initializeMessages(DebugSourcesMessages.class.getName(), DebugSourcesMessages.class);
	}

	private DebugSourcesMessages() {
	}
}
