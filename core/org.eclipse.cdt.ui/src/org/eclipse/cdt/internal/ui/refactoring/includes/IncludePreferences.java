/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;

/**
 * Preferences for managing of includes.
 */
public class IncludePreferences {
	private static final String DEFAULT_PARTNER_FILE_SUFFIXES = "test,unittest"; //$NON-NLS-1$

	public static enum UnusedStatementDisposition { REMOVE, COMMENT_OUT, KEEP }

	public final Map<IncludeKind, IncludeGroupStyle> includeStyles;
	public final boolean allowReordering;
	public final boolean heuristicHeaderSubstitution;
	public final boolean forwardDeclareCompositeTypes;
	public final boolean forwardDeclareEnums;
	public final boolean forwardDeclareFunctions;
	public final boolean forwardDeclareNamespaceElements;
	public final UnusedStatementDisposition unusedStatementsDisposition;
	public final String[] partnerFileSuffixes;

	public IncludePreferences(ICProject project) {
		includeStyles = new HashMap<IncludeKind, IncludeGroupStyle>();
		loadStyle(IncludeKind.RELATED, PREF_INCLUDE_STYLE_RELATED, project);
		loadStyle(IncludeKind.PARTNER, PREF_INCLUDE_STYLE_PARTNER, project);
		loadStyle(IncludeKind.IN_SAME_FOLDER, PREF_INCLUDE_STYLE_SAME_FOLDER, project);
		loadStyle(IncludeKind.IN_SUBFOLDER, PREF_INCLUDE_STYLE_SUBFOLDER, project);
		loadStyle(IncludeKind.SYSTEM, PREF_INCLUDE_STYLE_SYSTEM, project);
		loadStyle(IncludeKind.SYSTEM_WITH_EXTENSION, PREF_INCLUDE_STYLE_SYSTEM_WITH_EXTENSION, project);
		loadStyle(IncludeKind.SYSTEM_WITHOUT_EXTENSION, PREF_INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION, project);
		loadStyle(IncludeKind.OTHER, PREF_INCLUDE_STYLE_OTHER, project);
		loadStyle(IncludeKind.IN_SAME_PROJECT, PREF_INCLUDE_STYLE_SAME_PROJECT, project);
		loadStyle(IncludeKind.IN_OTHER_PROJECT, PREF_INCLUDE_STYLE_OTHER_PROJECT, project);
		loadStyle(IncludeKind.EXTERNAL, PREF_INCLUDE_STYLE_EXTERNAL, project);
		// Normalize order property of the styles to make sure that the numbers are sequential.
		List<IncludeGroupStyle> styles = new ArrayList<IncludeGroupStyle>(includeStyles.values());
		Collections.sort(styles);
		for (int i = 0; i < styles.size(); i++) {
			styles.get(i).setOrder(i);
		}
		// TODO(sprigogin): Load styles for headers matching patterns.

		forwardDeclareCompositeTypes = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_COMPOSITE_TYPES, project, true);
		forwardDeclareEnums = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_ENUMS, project, false);
		forwardDeclareFunctions = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_FUNCTIONS, project, false);
		forwardDeclareNamespaceElements = PreferenceConstants.getPreference(
				PREF_FORWARD_DECLARE_NAMESPACE_ELEMENTS, project, true);

		String value = PreferenceConstants.getPreference(
				PREF_PARTNER_FILE_SUFFIXES, project, DEFAULT_PARTNER_FILE_SUFFIXES);
		partnerFileSuffixes = value.split(","); //$NON-NLS-1$
		
		heuristicHeaderSubstitution = PreferenceConstants.getPreference(
				PREF_HEURISTIC_HEADER_SUBSTITUTION, project, true);

		allowReordering = PreferenceConstants.getPreference(
				PREF_INCLUDES_REORDERING, project, true);

		// Unused include handling preferences
		value = PreferenceConstants.getPreference(PREF_UNUSED_STATEMENTS_DISPOSITION, project, null);
		UnusedStatementDisposition disposition = null;
		if (value != null)
			disposition = UnusedStatementDisposition.valueOf(value);
		if (disposition == null)
			disposition = UnusedStatementDisposition.COMMENT_OUT;
		unusedStatementsDisposition = disposition;
	}

	private void loadStyle(IncludeKind includeKind, String preferenceKey, ICProject project) {
		String value = PreferenceConstants.getPreference(preferenceKey, project, null);
		IncludeGroupStyle style = null;
		if (value != null)
			style = IncludeGroupStyle.fromString(value, includeKind);
		if (style == null)
			style = new IncludeGroupStyle(includeKind);
		includeStyles.put(includeKind, style);
	}

	// TODO(sprigogin): Move the constants and defaults to PreferenceConstants.

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
	 * Defines a list of file name suffixes. A header file and the including file are considered
	 * partners if their file names without extensions are either identical or differ by one of
	 * these suffixes.  
	 */
	public static final String PREF_PARTNER_FILE_SUFFIXES = "include_partner_file_suffixes"; //$NON-NLS-1$

	/**
	 * Whether a heuristic approach should be used to decide which C++ header files to include.
	 * The heuristic prefers headers which have no file extension and / or are named like the symbol
	 * which should be defined. This often works out nicely since it's a commonly used naming
	 * convention for C++ library headers.
	 */
	public static final String PREF_HEURISTIC_HEADER_SUBSTITUTION = "organize_includes_heuristic_header_substitution"; //$NON-NLS-1$

	/**
	 * Whether it's allowed to reorder existing include directives. If this preference is set to
	 * false, the original order is kept as far as possible. This may be necessary to avoid breaking
	 * code which makes assumptions about the order of the include directives. If this is set to
	 * true, a different sort order can be applied. Groups of includes are ordered according to
	 * the values returned by
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle#getOrder()} method.
	 * Includes within each group are ordered alphabetically.
	 */
	public static final String PREF_INCLUDES_REORDERING = "organize_includes_allow_reordering"; //$NON-NLS-1$

	/**
	 * Determines what should be done with any unused include directives and forward declarations.
	 * This preference may have one of the three values defined by
	 * {@link UnusedStatementDisposition} enumeration ("REMOVE", "COMMENT_OUT", "KEEP").
	 */
	public static final String PREF_UNUSED_STATEMENTS_DISPOSITION = "organize_includes_unused_statements"; //$NON-NLS-1$

	/**
	 * Include style for headers closely related to the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_RELATED = "include_style_related"; //$NON-NLS-1$
	/**
	 * Include style for the header with the same name as the including file. 
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_PARTNER = "include_style_partner"; //$NON-NLS-1$
	/**
	 * Include style for headers in the same folder as the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_SAME_FOLDER = "include_style_same_folder"; //$NON-NLS-1$
	/**
	 * Include style for headers in subfolders of the folder containing the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_SUBFOLDER = "include_style_subfolder"; //$NON-NLS-1$
	/**
	 * Include style for system headers.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_SYSTEM = "include_style_system"; //$NON-NLS-1$
	/**
	 * Include style for C-style system headers with a file name extension.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_SYSTEM_WITH_EXTENSION = "include_style_system_with_extension"; //$NON-NLS-1$
	/**
	 * Include style for C++-style system headers without a file name extension.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION = "include_style_system_without_extension"; //$NON-NLS-1$
	/**
	 * Include style for headers not closely related to the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_OTHER = "include_style_other"; //$NON-NLS-1$
	/**
	 * Include style for headers in the same project as the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_SAME_PROJECT = "include_style_in_same_project"; //$NON-NLS-1$
	/**
	 * Include style for headers in a different project than the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_OTHER_PROJECT = "include_style_in_other_project"; //$NON-NLS-1$
	/**
	 * Include style for headers outside Eclipse workspace.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 */
	public static final String PREF_INCLUDE_STYLE_EXTERNAL = "include_style_external"; //$NON-NLS-1$
	/**
	 * Include styles for headers matching user-defined patterns.
	 * The value of the preference is an XML representation of one or more
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}s.
	 */
	public static final String PREF_INCLUDE_STYLE_MATCHING_PATTERN = "include_style_matching_pattern"; //$NON-NLS-1$

	/**
	 * Initializes the given preference store with the default values.
	 *
	 * @param store the preference store to be initialized
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {
		IncludeGroupStyle style = new IncludeGroupStyle(IncludeKind.RELATED);
		store.setDefault(PREF_INCLUDE_STYLE_RELATED, style.toString());
		style = new IncludeGroupStyle(IncludeKind.PARTNER);
		style.setKeepTogether(true);
		style.setBlankLineBefore(true);
		style.setOrder(0);
		store.setDefault(PREF_INCLUDE_STYLE_PARTNER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_SAME_FOLDER);
		store.setDefault(PREF_INCLUDE_STYLE_SAME_FOLDER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_SUBFOLDER);
		store.setDefault(PREF_INCLUDE_STYLE_SUBFOLDER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.SYSTEM);
		style.setKeepTogether(true);
		style.setBlankLineBefore(true);
		store.setDefault(PREF_INCLUDE_STYLE_SYSTEM, style.toString());
		style = new IncludeGroupStyle(IncludeKind.SYSTEM_WITH_EXTENSION);
		style.setKeepTogether(true);
		style.setAngleBrackets(true);
		style.setOrder(1);
		store.setDefault(PREF_INCLUDE_STYLE_SYSTEM_WITH_EXTENSION, style.toString());
		style = new IncludeGroupStyle(IncludeKind.SYSTEM_WITHOUT_EXTENSION);
		style.setKeepTogether(true);
		style.setAngleBrackets(true);
		style.setOrder(2);
		store.setDefault(PREF_INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION, style.toString());
		style = new IncludeGroupStyle(IncludeKind.OTHER);
		style.setKeepTogether(true);
		style.setBlankLineBefore(true);
		style.setOrder(3);
		store.setDefault(PREF_INCLUDE_STYLE_OTHER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_SAME_PROJECT);
		store.setDefault(PREF_INCLUDE_STYLE_SAME_PROJECT, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_OTHER_PROJECT);
		store.setDefault(PREF_INCLUDE_STYLE_OTHER_PROJECT, style.toString());
		style = new IncludeGroupStyle(IncludeKind.EXTERNAL);
		store.setDefault(PREF_INCLUDE_STYLE_EXTERNAL, style.toString());
		store.setDefault(PREF_INCLUDE_STYLE_MATCHING_PATTERN, ""); //$NON-NLS-1$

		store.setDefault(PREF_PARTNER_FILE_SUFFIXES, DEFAULT_PARTNER_FILE_SUFFIXES);
		store.setDefault(PREF_HEURISTIC_HEADER_SUBSTITUTION, true);
		store.setDefault(PREF_INCLUDES_REORDERING, true);
		store.setDefault(PREF_FORWARD_DECLARE_COMPOSITE_TYPES, true);
		store.setDefault(PREF_FORWARD_DECLARE_ENUMS, false);
		store.setDefault(PREF_FORWARD_DECLARE_FUNCTIONS, false);
		store.setDefault(PREF_FORWARD_DECLARE_NAMESPACE_ELEMENTS, true);
		store.setDefault(PREF_UNUSED_STATEMENTS_DISPOSITION, UnusedStatementDisposition.COMMENT_OUT.toString());
	}
}
