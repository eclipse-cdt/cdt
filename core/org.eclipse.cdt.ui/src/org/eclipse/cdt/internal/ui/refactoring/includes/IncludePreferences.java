/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.PreferenceConstants;

/**
 * Preferences for managing of includes.
 */
public class IncludePreferences {
	public final boolean allowReordering;
	public final boolean forwardDeclareCompositeTypes;
	public final boolean forwardDeclareEnums;
	public final boolean forwardDeclareFunctions;
	public final boolean forwardDeclareNamespaceElements;
	public final boolean relativeHeaderInSameDir;
	public final boolean relativeHeaderInSubdir;
	public final boolean relativeHeaderInParentDir;
	public final boolean heuristicHeaderSubstitution;
	public final boolean separateIncludeBlocks;
	public final boolean sortAlphabetically;
	public final boolean removeUnusedIncludes;
	public final boolean commentOutUnusedIncludes;
	public final boolean sortByHeaderLocation;
	public final List<IncludeType> groupOrder;  // TODO(sprigogin): Change to IncludeGroupOrder

	public static enum IncludeGroupType {
		SIBLING_HEADER, HEADERS_IN_SAME_FOLDER, HEADERS_IN_SUBFOLDERS,
		SYSTEM_HEADERS, SYSTEM_C_HEADERS, SYSTEM_CPP_HEADERS,
		PROJECT_HEADERS, EXTERNAL_HEADERS, SPECIAL_HEADERS,
		TYPE_FORWARD_DECLARATIONS, FUNCTION_FORWARD_DECLARATIONS, USING_DECLARATIONS
	}

	public static class GroupStyle {
		public boolean useRelativePath;
		public boolean useAngleBrackets;
		public boolean separateByBlankLine;
	}

	public IncludePreferences(ICProject project) {
		forwardDeclareCompositeTypes = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_COMPOSITE_TYPES, project, true);
		forwardDeclareEnums = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_ENUMS, project, false);
		forwardDeclareFunctions = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_FUNCTIONS, project, false);
		forwardDeclareNamespaceElements = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_NAMESPACE_ELEMENTS, project, true);

		// Relative headers preferences
		relativeHeaderInSameDir = PreferenceConstants.getPreference(
				PREF_RELATIVE_HEADER_IN_SAME_DIR, project, false);
		relativeHeaderInSubdir = PreferenceConstants.getPreference(
				PREF_RELATIVE_HEADER_IN_SUB_DIR, project, false);
		relativeHeaderInParentDir = PreferenceConstants.getPreference(
				PREF_RELATIVE_HEADER_IN_PARENT_DIR, project, false);

		// Header resolution preferences
		heuristicHeaderSubstitution = PreferenceConstants.getPreference(
				PREF_HEURISTIC_HEADER_SUBSTITUTION, project, true);

		// Header sort order preferences
		allowReordering = PreferenceConstants.getPreference(
				PREF_ALLOW_TO_REORDER_INCLUDES, project, true);
		sortByHeaderLocation = PreferenceConstants.getPreference(
				PREF_SORT_BY_HEADER_LOCATION, project, true);
		String order = PreferenceConstants.getPreference(
				PREF_HEADER_LOCATION_SORT_ORDER, project,
				IncludeType.RELATIVE_HEADER.toString() + ',' +
				IncludeType.LIBRARY_HEADER.toString() + ',' +
				IncludeType.PROJECT_HEADER.toString() + ',' +
				IncludeType.FORWARD_DECLARATION.toString() + ',' +
				IncludeType.FUNCTION_FORWARD_DECLARATION.toString());
		String[] textSortOrder = order.split(","); //$NON-NLS-1$
		List<IncludeType> list = new ArrayList<IncludeType>(textSortOrder.length);
		for (String type : textSortOrder) {
			list.add(IncludeType.valueOf(type));
		}
		groupOrder = Collections.unmodifiableList(list);

		separateIncludeBlocks = PreferenceConstants.getPreference(
				PREF_SEPARATE_INCLUDE_BLOCKS, project, true);
		sortAlphabetically = PreferenceConstants.getPreference(
				PREF_SORT_ALPHABETICALLY, project, true);

		// Unused include handling preferences
		removeUnusedIncludes = PreferenceConstants.getPreference(
				PREF_REMOVE_UNUSED_INCLUDES, project, false);
		commentOutUnusedIncludes = PreferenceConstants.getPreference(
				PREF_COMMENT_OUT_UNUSED_INCLUDES, project, true);
	}

	// TODO(sprigogin): Move the constants and defaults to PreferenceConstants.

	/**
	 * Enumerates the different types of code constructs which the organize includes action can
	 * generate.
	 */
	public enum IncludeType {
		/**
		 * A header which is located within the current file's directory.
		 */
		RELATIVE_HEADER,

		/**
		 * A header which is located within the current file's project directory.
		 */
		PROJECT_HEADER,

		/**
		 * A (library) header which is located outside of the current file's project directory.
		 */
		LIBRARY_HEADER,

		/**
		 * A forward declaration.
		 */
		FORWARD_DECLARATION,

		/**
		 * A forward declaration of a function.
		 */
		FUNCTION_FORWARD_DECLARATION,

		/**
		 * A problem like e.g. an unresolved symbol.
		 */
		FOUND_PROBLEM
	}

	/**
	 * Enumerates the different options for having a protection against multiple header file
	 * inclusion.
	 */
	public enum MultipleInclusionProtectionType {
		/**
		 * No protection against multiple header file inclusion.
		 */
		NONE,

		/**
		 * Use include guards to avoid multiple header file inclusion.
		 */
		INCLUDE_GUARDS,

		/**
		 * Use pragma once to avoid multiple header file inclusion.
		 */
		PRAGMA_ONCE
	}

	/**
	 * Whether composite types should be forward declared if possible.
	 *
	 * Examples:
	 *  class X;
	 *  struct Y;
	 *  union Z;
	 */
	public static final String PREF_FORWARD_DECLARE_COMPOSITE_TYPES = "forward_declare_composite_types"; //$NON-NLS-1$

	/**
	 * Whether C++11-style enums should be forward declared if possible.
	 *
	 * Example:
	 *  enum class X;
	 */
	public static final String PREF_FORWARD_DECLARE_ENUMS = "forward_declare_enums"; //$NON-NLS-1$

	/**
	 * Whether C-style functions should be forward declared if possible.
	 *
	 * Example:
	 *  void foo();
	 */
	public static final String PREF_FORWARD_DECLARE_FUNCTIONS = "forward_declare_functions"; //$NON-NLS-1$

	/**
	 * Whether elements nested within namespaces should be forward declared if possible.
	 *
	 * Examples:
	 *  namespace N { class X; }
	 */
	public static final String PREF_FORWARD_DECLARE_NAMESPACE_ELEMENTS = "forward_declare_namespace_elements"; //$NON-NLS-1$

	/**
	 * Whether headers located within the same directory as the source file should always
	 * be included relative to the source file.
	 */
	public static final String PREF_RELATIVE_HEADER_IN_SAME_DIR = "relative_header_in_same_dir"; //$NON-NLS-1$

	/**
	 * Whether headers located within a subdirectory of the source file's directory should always
	 * be included relative to the source file.
	 */
	public static final String PREF_RELATIVE_HEADER_IN_SUB_DIR = "relative_header_in_sub_dir"; //$NON-NLS-1$

	/**
	 * Whether headers located within a parent directory of the source file's directory should
	 * always be included relative to the source file.
	 */
	public static final String PREF_RELATIVE_HEADER_IN_PARENT_DIR = "relative_header_in_parent_dir"; //$NON-NLS-1$

	/**
	 * Whether a heuristic approach should be used to resolve C++ header files. The heuristic
	 * prefers headers which have no file extension and / or are named like the symbol which should
	 * be defined. This often works out nicely since it's a commonly used naming convention for C++
	 * (library) headers.
	 */
	public static final String PREF_HEURISTIC_HEADER_SUBSTITUTION = "heuristic_header_resolution"; //$NON-NLS-1$

	/**
	 * Whether it's allowed to reorder existing include directives. If this is set to false,
	 * the original order is kept as far as possible. This may be necessary to avoid breaking code
	 * which makes assumptions about the order of the include directives. If this is set to true,
	 * a different sort order can be applied. Also see the other sort order preferences for further
	 * details.
	 */
	public static final String PREF_ALLOW_TO_REORDER_INCLUDES = "allow_to_reorder_includes"; //$NON-NLS-1$

	/**
	 * Whether the include directives should be sorted by header file location. Ignored if
	 * PREF_ALLOW_TO_REORDER_INCLUDES is false.
	 */
	public static final String PREF_SORT_BY_HEADER_LOCATION = "sort_by_header_location"; //$NON-NLS-1$

	/**
	 * Defines the header location sort order. Ignored if either PREF_ALLOW_TO_REORDER_INCLUDES or
	 * PREF_SORT_BY_HEADER_LOCATION is false. An example location sort order would be:
	 * Relative headers > Project headers > Library headers > Forward declarations
	 */
	public static final String PREF_HEADER_LOCATION_SORT_ORDER = "header_location_sort_order"; //$NON-NLS-1$

	/**
	 * Whether the different blocks of include directives should be separated by a blank line.
	 * Ignored if either PREF_ALLOW_TO_REORDER_INCLUDES or PREF_SORT_BY_HEADER_LOCATION is false.
	 *
	 * Example:
	 *  // Relative headers
	 *  #include "..."
	 *
	 *  // Project headers
	 *  #include "..."
	 *
	 *  // Library headers
	 *  #include <...>
	 *
	 *  // Forward declarations
	 *  class ...;
	 */
	public static final String PREF_SEPARATE_INCLUDE_BLOCKS = "separate_include_blocks"; //$NON-NLS-1$

	/**
	 * Whether the include directives should be sorted alphabetically. Ignored if
	 * PREF_ALLOW_TO_REORDER_INCLUDES is false.
	 */
	public static final String PREF_SORT_ALPHABETICALLY = "sort_alphabetically"; //$NON-NLS-1$

	/**
	 * Whether any unused include directives and forward declarations should be removed. It might be
	 * helpful to disable this if some include directives tend to become removed incorrectly, as it
	 * might happen when using e.g. conditional compilations.
	 */
	public static final String PREF_REMOVE_UNUSED_INCLUDES = "remove_unused_includes"; //$NON-NLS-1$

	public static final String PREF_COMMENT_OUT_UNUSED_INCLUDES = "comment_out_unused_includes"; //$NON-NLS-1$

	/**
     * Initializes the given preference store with the default values.
     *
     * @param store the preference store to be initialized
     */
    public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(PREF_FORWARD_DECLARE_COMPOSITE_TYPES, true);
		store.setDefault(PREF_FORWARD_DECLARE_ENUMS, false);
		store.setDefault(PREF_FORWARD_DECLARE_FUNCTIONS, false);
		store.setDefault(PREF_FORWARD_DECLARE_NAMESPACE_ELEMENTS, true);

		// Relative headers preferences
		store.setDefault(PREF_RELATIVE_HEADER_IN_SAME_DIR, false);
		store.setDefault(PREF_RELATIVE_HEADER_IN_SUB_DIR, false);
		store.setDefault(PREF_RELATIVE_HEADER_IN_PARENT_DIR, false);

		// Header resolution preferences
		store.setDefault(PREF_HEURISTIC_HEADER_SUBSTITUTION, true);

		// Header sort order preferences
		store.setDefault(PREF_ALLOW_TO_REORDER_INCLUDES, true);
		store.setDefault(PREF_SORT_BY_HEADER_LOCATION, true);
		store.setDefault(PREF_HEADER_LOCATION_SORT_ORDER,
				IncludeType.RELATIVE_HEADER.toString() + ' ' +
				IncludeType.LIBRARY_HEADER.toString() + ' ' +
				IncludeType.PROJECT_HEADER.toString() + ' ' +
				IncludeType.FORWARD_DECLARATION.toString() + ' ' +
				IncludeType.FUNCTION_FORWARD_DECLARATION.toString() + ' ' +
				IncludeType.FOUND_PROBLEM.toString());
		store.setDefault(PREF_SEPARATE_INCLUDE_BLOCKS, true);
		store.setDefault(PREF_SORT_ALPHABETICALLY, true);

		// Unused include handling preferences
		store.setDefault(PREF_REMOVE_UNUSED_INCLUDES, true);
		store.setDefault(PREF_COMMENT_OUT_UNUSED_INCLUDES, true);
    }
}
