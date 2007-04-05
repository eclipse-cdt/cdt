/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core;


public class CCorePreferenceConstants {

	/**
	 * Possible configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String TRANSLATION_TASK_TAGS = CCorePlugin.PLUGIN_ID + ".translation.taskTags"; //$NON-NLS-1$

	/**
	 * Default task tag
	 */
	public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
	
	/**
	 * Default task priority
	 */
	public static final String DEFAULT_TASK_PRIORITY = CCorePlugin.TRANSLATION_TASK_PRIORITY_NORMAL;
	/**
	 * Possible configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String TRANSLATION_TASK_PRIORITIES = CCorePlugin.PLUGIN_ID + ".translation.taskPriorities"; //$NON-NLS-1$

	/**
	 * Active code formatter ID.
	 * @see #getDefaultOptions
	 */
	public static final String CODE_FORMATTER = CCorePlugin.PLUGIN_ID + ".code_formatter"; //$NON-NLS-1$
	
	/**
	 * Default code formatter
	 */
	public static final String DEFAULT_CODE_FORMATTER = CCorePlugin.PLUGIN_ID + ".defaultCodeFormatter"; //$NON-NLS-1$

	/**
	 * Cache size for the index in percentage of max memory.
	 */
	public static final String INDEX_DB_CACHE_SIZE_PCT = CCorePlugin.PLUGIN_ID + ".indexDBCacheSizePct"; //$NON-NLS-1$

	/**
	 * Default cache size of the index-db in percentage of max memory.
	 */
	public static final String DEFAULT_INDEX_DB_CACHE_SIZE_PCT = "10"; //$NON-NLS-1$

	/**
	 * Absolute maximum size of the index-db in megabytes.
	 */
	public static final String MAX_INDEX_DB_CACHE_SIZE_MB = CCorePlugin.PLUGIN_ID + ".maxIndexDBCacheSizeMB"; //$NON-NLS-1$
	
	/**
	 * Default absolute maximum size of the index-db in megabytes.
	 */
	public static final String DEFAULT_MAX_INDEX_DB_CACHE_SIZE_MB = "64"; //$NON-NLS-1$
	
	/**
	 * Workspace-wide language mappings. 
	 */
	public static final String WORKSPACE_LANGUAGE_MAPPINGS = CCorePlugin.PLUGIN_ID + ".workspaceLanguageMappings"; //$NON-NLS-1$

	/**
	 * Default workspace-wide language mappings.
	 */
	public static final String DEFAULT_WORKSPACE_LANGUAGE_MAPPINGS = ""; //$NON-NLS-1$
}
