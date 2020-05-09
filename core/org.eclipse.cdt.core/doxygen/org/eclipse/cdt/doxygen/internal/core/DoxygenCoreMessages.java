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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.doxygen.internal.core;

import org.eclipse.osgi.util.NLS;

public class DoxygenCoreMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.doxygen.internal.core.DoxygenCoreMessages"; //$NON-NLS-1$
	public static String DoxygenMetadataDefaults_new_line_after_brief_description;
	public static String DoxygenMetadataDefaults_new_line_after_brief_name;
	public static String DoxygenMetadataDefaults_use_brief_tag_description;
	public static String DoxygenMetadataDefaults_use_brief_tag_name;
	public static String DoxygenMetadataDefaults_use_javadoc_style_description;
	public static String DoxygenMetadataDefaults_use_javadoc_style_name;
	public static String DoxygenMetadataDefaults_use_pre_post_tags_description;
	public static String DoxygenMetadataDefaults_use_pre_post_tags_name;
	public static String DoxygenMetadataDefaults_use_structured_commands_description;
	public static String DoxygenMetadataDefaults_use_structured_commands_name;
	public static String DoxygenPreferenceAccess_e_get_preferences;
	public static String DoxygenPreferenceAccess_e_null_project;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DoxygenCoreMessages.class);
	}

	private DoxygenCoreMessages() {
	}
}
