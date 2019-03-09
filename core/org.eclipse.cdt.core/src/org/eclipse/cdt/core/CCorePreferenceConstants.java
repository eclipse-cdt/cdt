/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     IBM Corporation
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

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
	public static final String DEFAULT_MAX_INDEX_DB_CACHE_SIZE_MB = "256"; //$NON-NLS-1$

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
	 * Attempt to (not) show c source not found editor in debug. String value,
	 * one of {@link #SHOW_SOURCE_NOT_FOUND_EDITOR_ALL_THE_TIME},
	 * {@link #SHOW_SOURCE_NOT_FOUND_EDITOR_SOMETIMES},
	 * {@link #SHOW_SOURCE_NOT_FOUND_EDITOR_NEVER}
	 *
	 * @since 6.3
	 */
	public static final String SHOW_SOURCE_NOT_FOUND_EDITOR = CCorePlugin.PLUGIN_ID + ".showSourceNotFoundEditor"; //$NON-NLS-1$

	/**
	 * Use to display all the time the source not found editor
	 * @since 6.3
	 */
	public static final String SHOW_SOURCE_NOT_FOUND_EDITOR_ALL_THE_TIME = "all_time"; //$NON-NLS-1$

	/**
	 * Use to display sometimes the source not found editor
	 * @since 6.3
	 */
	public static final String SHOW_SOURCE_NOT_FOUND_EDITOR_SOMETIMES = "sometimes"; //$NON-NLS-1$

	/**
	 * Use to don't display the source not found editor
	 * @since 6.3
	 */
	public static final String SHOW_SOURCE_NOT_FOUND_EDITOR_NEVER = "never"; //$NON-NLS-1$

	/**
	 * Use to display by default the source not found editor
	 * @since 6.3
	 */
	public static final String SHOW_SOURCE_NOT_FOUND_EDITOR_DEFAULT = SHOW_SOURCE_NOT_FOUND_EDITOR_ALL_THE_TIME;

	/**
	 * Show source roots at the top level of projects.
	 * @since 5.2
	 */
	public static final String SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT = CCorePlugin.PLUGIN_ID
			+ ".showSourceRootsAtTopLevelOfProject"; //$NON-NLS-1$

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

	/**
	 * Default value for {@link #INCLUDE_EXPORT_PATTERN}.
	 * @since 5.5
	 */
	public static final String DEFAULT_INCLUDE_EXPORT_PATTERN = "IWYU\\s+(pragma:?\\s+)?export"; //$NON-NLS-1$

	/**
	 * Preference key for the regular expression pattern that, when appears in a comment on the same
	 * line as include statement, indicates that the included header file is exported.
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 *
	 * @since 5.5
	 */
	public static final String INCLUDE_EXPORT_PATTERN = "includes.exportPattern"; //$NON-NLS-1$

	/**
	 * Default value for {@link #INCLUDE_BEGIN_EXPORTS_PATTERN}.
	 * @since 5.5
	 */
	public static final String DEFAULT_INCLUDE_BEGIN_EXPORTS_PATTERN = "IWYU\\s+(pragma:?\\s+)?begin_exports?"; //$NON-NLS-1$

	/**
	 * Preference key for the regular expression pattern that, when appears in a comment, marks
	 * the beginning of a sequence of include statements that export the included header files.
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 *
	 * @since 5.5
	 */
	public static final String INCLUDE_BEGIN_EXPORTS_PATTERN = "includes.beginExportsPattern"; //$NON-NLS-1$

	/**
	 * Default value for {@link #INCLUDE_END_EXPORTS_PATTERN}.
	 * @since 5.5
	 */
	public static final String DEFAULT_INCLUDE_END_EXPORTS_PATTERN = "IWYU\\s+(pragma:?\\s+)?end_exports?"; //$NON-NLS-1$

	/**
	 * Preference key for the regular expression pattern that, when appears in a comment, marks
	 * the end of a sequence of include statements that export the included header files.
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 *
	 * @since 5.5
	 */
	public static final String INCLUDE_END_EXPORTS_PATTERN = "includes.endExportsPattern"; //$NON-NLS-1$

	/**
	 * Default value for {@link #INCLUDE_PRIVATE_PATTERN}.
	 * @since 5.7
	 */
	public static final String DEFAULT_INCLUDE_PRIVATE_PATTERN = "IWYU\\s+(pragma:?\\s+)?private(,\\s+include\\s+(?<header>\\S+))?"; //$NON-NLS-1$

	/**
	 * Preference key for the regular expression pattern that, when appears in a comment on the same
	 * line as include statement, indicates that the included header file is private and that
	 * another header file should be included instead.
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 *
	 * @since 5.7
	 */
	public static final String INCLUDE_PRIVATE_PATTERN = "includes.privatePattern"; //$NON-NLS-1$

	/**
	 * Default value for {@link #INCLUDE_KEEP_PATTERN}.
	 * @since 5.9
	 */
	public static final String DEFAULT_INCLUDE_KEEP_PATTERN = "IWYU\\s+(pragma:?\\s+)?keep"; //$NON-NLS-1$

	/**
	 * Preference key for the regular expression pattern that, when appears in a comment on the same
	 * line as include statement, indicates that the include statement should be preserved when
	 * organizing includes.
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 *
	 * @since 5.9
	 */
	public static final String INCLUDE_KEEP_PATTERN = "includes.keepPattern"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the parser should skip trivial expressions in initializer lists.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 *
	 * @since 5.6
	 */
	public static String SCALABILITY_SKIP_TRIVIAL_EXPRESSIONS = "scalability.skipTrivialExpressions"; //$NON-NLS-1$

	/**
	 * Default value for {@link #SCALABILITY_SKIP_TRIVIAL_EXPRESSIONS}.
	 * @since 5.6
	 */
	public static final boolean DEFAULT_SCALABILITY_SKIP_TRIVIAL_EXPRESSIONS = true;

	/**
	 * The maximum number of trivial expressions that are parsed in initializer lists. This preference
	 * is considered only if <code>SCALABILITY_SKIP_TRIVIAL_EXPRESSIONS</code> is set to true.
	 * <p>
	 * Value is of type <code>int</code>.
	 * </p>
	 *
	 * @since 5.6
	 */
	public static final String SCALABILITY_MAXIMUM_TRIVIAL_EXPRESSIONS = "scalability.maximumTrivialExpressions"; //$NON-NLS-1$

	/**
	 * Default value for {@link #SCALABILITY_MAXIMUM_TRIVIAL_EXPRESSIONS}.
	 * @since 5.6
	 */
	public static final int DEFAULT_SCALABILITY_MAXIMUM_TRIVIAL_EXPRESSIONS = 1000;

	/**
	 * A named preference that specifies whether the parser should abort when too many Tokens are created
	 * during parse of a single TU.  This is a heuristic that is used to detect translation units that
	 * are too complex to be handled the by the CDT parser.
	 *
	 * @since 5.7
	 */
	public static final String SCALABILITY_LIMIT_TOKENS_PER_TU = "scalability.limitTokensPerTU"; //$NON-NLS-1$

	/**
	 * Default value for {@link #SCALABILITY_LIMIT_TOKENS_PER_TU}.
	 * @since 5.7
	 */
	public static final boolean DEFAULT_SCALABILITY_LIMIT_TOKENS_PER_TU = false;

	/**
	 * A named preference that specifies the parser's token limit.  Parsing will be aborted when a single
	 * translation unit has produced a maximum number of tokens.  This is a heuristic that is used to
	 * detect translation units that are too complex to be handled the by the CDT parser.
	 *
	 * @since 5.7
	 */
	public static final String SCALABILITY_MAXIMUM_TOKENS = "scalability.maximumTokens"; //$NON-NLS-1$

	/**
	 * Default value for {@link #SCALABILITY_MAXIMUM_TOKENS}.
	 *
	 * @since 5.7
	 */
	public static final int DEFAULT_SCALABILITY_MAXIMUM_TOKENS = 25 * 1000 * 1000;
	// NOTE: This default came from measurements using a 1Gb heap on a 64-bit VM.  The test project was
	//       boost-1.55.0.  This default will index all but 9 files without running out of memory.

	/**
	 * A named preference that specifies whether the const qualifier is written to the right (or left) of
	 * the type in a declaration specifier.
	 *
	 * @since 6.3
	 */
	public static final String PLACE_CONST_RIGHT_OF_TYPE = "astwriter.placeConstRightOfType"; //$NON-NLS-1$

	/**
	 * Default value for {@link #PLACE_CONST_RIGHT_OF_TYPE}.
	 *
	 * @since 6.3
	 */
	public static final boolean DEFAULT_PLACE_CONST_RIGHT_OF_TYPE = false;

	/**
	 * A named preference that specifies whether the override keyword should be added
	 * to method signature.
	 *
	 * @since 6.8
	 */
	public static final String ADD_OVERRIDE_KEYWORD = "astwriter.addOverride"; //$NON-NLS-1$

	/**
	 * A named preference that specifies whether the virtual keyword should be added
	 * to method signature.
	 *
	 * @since 6.8
	 */
	public static final String PRESERVE_VIRTUAL_KEYWORD = "astwriter.preserveVirtual"; //$NON-NLS-1$

	/**
	 * Default value for {@link #ADD_OVERRIDE_KEYWORD}.
	 *
	 * @since 6.8
	 */
	public static final boolean DEFAULT_ADD_OVERRIDE_KEYWORD = false;

	/**
	 * Default value for {@link #PRESERVE_VIRTUAL_KEYWORD}.
	 *
	 * @since 6.8
	 */
	public static final boolean DEFAULT_PRESERVE_VIRTUAL_KEYWORD = true;

	/**
	 * Returns the node in the preference in the given context.
	 *
	 * @param key The preference key.
	 * @param cProject The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should
	 *     be avoided.
	 * @return Returns the node matching the given context.
	 */
	private static IEclipsePreferences getPreferenceNode(String key, ICProject cProject) {
		IProject project = cProject == null ? null : cProject.getProject();
		return getPreferenceNode(key, project);
	}

	/**
	 * Returns the node in the preference in the given context.
	 *
	 * @param key The preference key.
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should
	 *     be avoided.
	 * @return Returns the node matching the given context.
	 */
	private static IEclipsePreferences getPreferenceNode(String key, IProject project) {
		IEclipsePreferences node = null;
		if (project != null) {
			node = new ProjectScope(project).getNode(CCorePlugin.PLUGIN_ID);
			if (node.get(key, null) != null) {
				return node;
			}
		}
		node = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		if (node.get(key, null) != null) {
			return node;
		}

		node = ConfigurationScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		if (node.get(key, null) != null) {
			return node;
		}

		return DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @return Returns the current value for the string.
	 * @since 5.5
	 */
	public static String getPreference(String key, ICProject project) {
		return getPreference(key, project, null);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @return Returns the current value for the string.
	 * @since 5.9
	 */
	public static String getPreference(String key, IProject project) {
		return getPreference(key, project, null);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.5
	 */
	public static String getPreference(String key, ICProject project, String defaultValue) {
		return getPreferenceNode(key, project).get(key, defaultValue);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.9
	 */
	public static String getPreference(String key, IProject project, String defaultValue) {
		return getPreferenceNode(key, project).get(key, defaultValue);
	}

	/**
	 * Returns the integer value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.5
	 */
	public static int getPreference(String key, ICProject project, int defaultValue) {
		return getPreferenceNode(key, project).getInt(key, defaultValue);
	}

	/**
	 * Returns the integer value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.9
	 */
	public static int getPreference(String key, IProject project, int defaultValue) {
		return getPreferenceNode(key, project).getInt(key, defaultValue);
	}

	/**
	 * Returns the boolean value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.5
	 */
	public static boolean getPreference(String key, ICProject project, boolean defaultValue) {
		return getPreferenceNode(key, project).getBoolean(key, defaultValue);
	}

	/**
	 * Returns the boolean value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.9
	 */
	public static boolean getPreference(String key, IProject project, boolean defaultValue) {
		return getPreferenceNode(key, project).getBoolean(key, defaultValue);
	}

	/**
	 * Returns the scopes for preference lookup.
	 *
	 * @param project a project or {@code null}
	 * @return the scopes for preference lookup.
	 * @since 5.5
	 */
	public static IScopeContext[] getPreferenceScopes(IProject project) {
		return project != null
				? new IScopeContext[] { new ProjectScope(project), InstanceScope.INSTANCE, ConfigurationScope.INSTANCE,
						DefaultScope.INSTANCE }
				: new IScopeContext[] { InstanceScope.INSTANCE, ConfigurationScope.INSTANCE, DefaultScope.INSTANCE };
	}
}
