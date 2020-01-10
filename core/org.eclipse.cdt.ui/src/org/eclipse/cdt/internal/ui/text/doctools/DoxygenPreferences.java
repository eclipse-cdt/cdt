/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Stornelli - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

public class DoxygenPreferences {
	/**
	 * Use always brief tag in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final String DOXYGEN_USE_BRIEF_TAG = "doxygen_use_brief_tag"; //$NON-NLS-1$
	/**
	 * Use always structured commands in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final String DOXYGEN_USE_STRUCTURAL_COMMANDS = "doxygen_use_structural_commands"; //$NON-NLS-1$
	/**
	 * Use always javadoc tag style in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final String DOXYGEN_USE_JAVADOC_TAGS = "doxygen_use_javadoc_tags"; //$NON-NLS-1$
	/**
	 * Use always a new line after brief tag in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final String DOXYGEN_NEW_LINE_AFTER_BRIEF = "doxygen_new_line_after_brief"; //$NON-NLS-1$
	/**
	 * Use always a pre tag in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final String DOXYGEN_USE_PRE_POST_TAGS = "doxygen_use_pre_tag"; //$NON-NLS-1$

	/**
	 * Default use always brief tag in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final boolean DEF_DOXYGEN_USE_BRIEF_TAG = false;
	/**
	 * Default use always structured commands in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final boolean DEF_DOXYGEN_USE_STRUCTURED_COMMANDS = false;
	/**
	 * Default use always javadoc tag style in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final boolean DEF_DOXYGEN_USE_JAVADOC_TAGS = true;
	/**
	 * Default use always a new line after brief tag in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final boolean DEF_DOXYGEN_NEW_LINE_AFTER_BRIEF = true;
	/**
	 * Default use always a pre tag in auto-generation of doxygen comment
	 * @since 6.7
	 */
	public static final boolean DEF_DOXYGEN_USE_PRE_POST_TAGS = false;

	private static final String QUALIFIER = CCorePlugin.PLUGIN_ID;
	private static final String WORKSPACE_DOXYGEN_NODE = "doxygen"; //$NON-NLS-1$

	private Optional<IProject> project;

	public DoxygenPreferences(Optional<IProject> project) {
		this.project = project;
	}

	/**
	 * Get boolean preferences
	 * @param key A preference key
	 * @param defaultValue A default value
	 * @return The preference value
	 */
	public boolean getBooleanPref(String key, boolean defaultValue) {
		Preferences prefs = project.isPresent()
				? new ProjectScope(project.get()).getNode(QUALIFIER).node(WORKSPACE_DOXYGEN_NODE)
				: InstanceScope.INSTANCE.getNode(QUALIFIER).node(WORKSPACE_DOXYGEN_NODE);
		return prefs.getBoolean(key, defaultValue);
	}

	/**
	 * Put boolean preferences
	 * @param key A preference key
	 * @param defaultValue A default value
	 */
	public void putBooleanPref(String key, boolean value) {
		Preferences prefs = project.isPresent()
				? new ProjectScope(project.get()).getNode(QUALIFIER).node(WORKSPACE_DOXYGEN_NODE)
				: InstanceScope.INSTANCE.getNode(QUALIFIER).node(WORKSPACE_DOXYGEN_NODE);
		prefs.putBoolean(key, value);
	}
}
