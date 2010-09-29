/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google) 
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CCorePreferenceConstants {

	/**
     * <pre>
     * RECOGNIZED OPTIONS:
     * Define the Automatic Task Tags
     *    When the tag list is not empty, indexer will issue a task marker whenever it encounters
     *    one of the corresponding tags inside any comment in C/C++ source code.
     *    Generated task messages will include the tag, and range until the next line separator or comment ending.
     *    Note that tasks messages are trimmed. If a tag is starting with a letter or digit, then it cannot be leaded by
     *    another letter or digit to be recognized ("fooToDo" will not be recognized as a task for tag "ToDo", but "foo#ToDo"
     *    will be detected for either tag "ToDo" or "#ToDo"). Respectively, a tag ending with a letter or digit cannot be followed
     *    by a letter or digit to be recognized ("ToDofoo" will not be recognized as a task for tag "ToDo", but "ToDo:foo" will
     *    be detected either for tag "ToDo" or "ToDo:").
     *     - option id:         "org.eclipse.cdt.core.taskTags"
     *     - possible values:   { "<tag>[,<tag>]*" } where <tag> is a String without any wild-card or leading/trailing spaces 
     *     - default:           ""
     * 
     * Define the Automatic Task Priorities
     *    In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
     *    of the task markers issued by the translation.
     *    If the default is specified, the priority of each task marker is "normal".
     *     - option id:         "org.eclipse.cdt.core.taskPriorities"
     *     - possible values:   { "<priority>[,<priority>]*" } where <priority> is one of "high", "normal" or "low"
     *     - default:           ""
     */

	/**
	 * Task tags used in code comments.
	 */
	public static final String TODO_TASK_TAGS = CCorePlugin.PLUGIN_ID + ".taskTags"; //$NON-NLS-1$

	/**
	 * Default task tag
	 */
	public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
	
	/**
	 * List of tags provided by default
	 * @since 5.1
	 */
	public static final String DEFAULT_TASK_TAGS = DEFAULT_TASK_TAG + ",FIXME,XXX"; //$NON-NLS-1$
	
	/**
	 * Possible configurable option value for TODO_TASK_PRIORITIES.
	 */
	public static final String TASK_PRIORITY_NORMAL = "normal"; //$NON-NLS-1$	    
    /**
     * Possible configurable option value for TODO_TASK_PRIORITIES.
     */
    public static final String TASK_PRIORITY_HIGH = "high"; //$NON-NLS-1$
    /**
     * Possible configurable option value for TODO_TASK_PRIORITIES.
     */
    public static final String TASK_PRIORITY_LOW = "low"; //$NON-NLS-1$
	/**
	 * Default task priority
	 */
	public static final String DEFAULT_TASK_PRIORITY = TASK_PRIORITY_NORMAL;

	/**
	 * Priorities associated with task tags.
	 */
	public static final String TODO_TASK_PRIORITIES = CCorePlugin.PLUGIN_ID + ".taskPriorities"; //$NON-NLS-1$

	/**
	 * Case sensitivity of task tags.
	 */
	public static final String TODO_TASK_CASE_SENSITIVE = CCorePlugin.PLUGIN_ID + ".taskCaseSensitive"; //$NON-NLS-1$

	/**
	 * Default case sensitivity of task tags.
	 */
	public static final String DEFAULT_TASK_CASE_SENSITIVE = "false"; //$NON-NLS-1$

	/**
	 * Active code formatter ID.
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
	 * Boolean preference controlling whether paths of non-workspace files are stored in index in canonical
	 * form or not. Canonicalization is performed by calling {@link java.io.File#getCanonicalPath()}.
	 * @since 5.2
	 */
	public static final String FILE_PATH_CANONICALIZATION = CCorePlugin.PLUGIN_ID + ".path_canonicalization"; //$NON-NLS-1$

	/**
	 * Workspace-wide language mappings. 
	 */
	public static final String WORKSPACE_LANGUAGE_MAPPINGS = CCorePlugin.PLUGIN_ID + ".workspaceLanguageMappings"; //$NON-NLS-1$

	/**
	 * Default workspace-wide language mappings.
	 */
	public static final String DEFAULT_WORKSPACE_LANGUAGE_MAPPINGS = ""; //$NON-NLS-1$

	/**
	 * Attempt to show source files for executable binaries.
	 */
	public static final String SHOW_SOURCE_FILES_IN_BINARIES = CCorePlugin.PLUGIN_ID + ".showSourceFilesInBinaries"; //$NON-NLS-1$
	
	/**
	 * Show source roots at the top level of projects.
	 * @since 5.2
	 */
	public static final String SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT = CCorePlugin.PLUGIN_ID + ".showSourceRootsAtTopLevelOfProject"; //$NON-NLS-1$

	/**
	 * "Build All Configurations" preference key.
	 * 
	 * @since 5.3
	 */
	public static final String PREF_BUILD_ALL_CONFIGS = "build.all.configs.enabled"; //$NON-NLS-1$

	/**
	 * Preference key for "build only if resources in (related) projects are modified".
	 * 
	 * @since 5.3
	 */
	public static final String PREF_BUILD_CONFIGS_RESOURCE_CHANGES = "build.proj.ref.configs.enabled"; //$NON-NLS-1$
}
