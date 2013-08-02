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
	public final boolean allowPartnerIndirectInclusion;
	public final boolean allowIndirectInclusion;
	public final boolean forwardDeclareCompositeTypes;
	public final boolean forwardDeclareEnums;
	public final boolean forwardDeclareFunctions;
	// TODO(sprigogin): Create a preference for this.
	public final boolean forwardDeclareExternalVariables = false;
	public final boolean forwardDeclareTemplates;
	public final boolean forwardDeclareNamespaceElements;
	public final UnusedStatementDisposition unusedStatementsDisposition;
	public final String[] partnerFileSuffixes;

	public IncludePreferences(ICProject project) {
		includeStyles = new HashMap<IncludeKind, IncludeGroupStyle>();
		loadStyle(IncludeKind.RELATED, PreferenceConstants.INCLUDE_STYLE_RELATED, project);
		loadStyle(IncludeKind.PARTNER, PreferenceConstants.INCLUDE_STYLE_PARTNER, project);
		loadStyle(IncludeKind.IN_SAME_FOLDER, PreferenceConstants.INCLUDE_STYLE_SAME_FOLDER, project);
		loadStyle(IncludeKind.IN_SUBFOLDER, PreferenceConstants.INCLUDE_STYLE_SUBFOLDER, project);
		loadStyle(IncludeKind.SYSTEM, PreferenceConstants.INCLUDE_STYLE_SYSTEM, project);
		loadStyle(IncludeKind.SYSTEM_WITH_EXTENSION, PreferenceConstants.INCLUDE_STYLE_SYSTEM_WITH_EXTENSION, project);
		loadStyle(IncludeKind.SYSTEM_WITHOUT_EXTENSION, PreferenceConstants.INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION, project);
		loadStyle(IncludeKind.OTHER, PreferenceConstants.INCLUDE_STYLE_OTHER, project);
		loadStyle(IncludeKind.IN_SAME_PROJECT, PreferenceConstants.INCLUDE_STYLE_SAME_PROJECT, project);
		loadStyle(IncludeKind.IN_OTHER_PROJECT, PreferenceConstants.INCLUDE_STYLE_OTHER_PROJECT, project);
		loadStyle(IncludeKind.EXTERNAL, PreferenceConstants.INCLUDE_STYLE_EXTERNAL, project);
		// Unclassified includes are always kept together.
		includeStyles.get(IncludeKind.OTHER).setKeepTogether(true);
		// Normalize order property of the styles to make sure that the numbers are sequential.
		List<IncludeGroupStyle> styles = new ArrayList<IncludeGroupStyle>(includeStyles.values());
		Collections.sort(styles);
		for (int i = 0; i < styles.size(); i++) {
			styles.get(i).setOrder(i);
		}
		// TODO(sprigogin): Load styles for headers matching patterns.

		forwardDeclareCompositeTypes = PreferenceConstants.getPreference(
				PreferenceConstants.FORWARD_DECLARE_COMPOSITE_TYPES, project, true);
		forwardDeclareEnums = PreferenceConstants.getPreference(
				PreferenceConstants.FORWARD_DECLARE_ENUMS, project, false);
		forwardDeclareFunctions = PreferenceConstants.getPreference(
				PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, project, false);
		forwardDeclareTemplates = PreferenceConstants.getPreference(
				PreferenceConstants.FORWARD_DECLARE_TEMPLATES, project, false);
		forwardDeclareNamespaceElements = PreferenceConstants.getPreference(
				PreferenceConstants.FORWARD_DECLARE_NAMESPACE_ELEMENTS, project, true);

		String value = PreferenceConstants.getPreference(
				PreferenceConstants.INCLUDES_PARTNER_FILE_SUFFIXES, project, DEFAULT_PARTNER_FILE_SUFFIXES);
		partnerFileSuffixes = value.split(","); //$NON-NLS-1$
		
		heuristicHeaderSubstitution = PreferenceConstants.getPreference(
				PreferenceConstants.INCLUDES_HEURISTIC_HEADER_SUBSTITUTION, project, true);

		allowReordering = PreferenceConstants.getPreference(
				PreferenceConstants.INCLUDES_ALLOW_REORDERING, project, true);

		// TODO(sprigogin): Create a preference for this.
		allowIndirectInclusion = false;

		allowPartnerIndirectInclusion = PreferenceConstants.getPreference(
				PreferenceConstants.INCLUDES_ALLOW_PARTNER_INDIRECT_INCLUSION, project, true);

		// Unused include handling preferences
		value = PreferenceConstants.getPreference(PreferenceConstants.INCLUDES_UNUSED_STATEMENTS_DISPOSITION, project, null);
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

	/**
	 * Initializes the given preference store with the default values.
	 *
	 * @param store the preference store to be initialized
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {
		IncludeGroupStyle style = new IncludeGroupStyle(IncludeKind.RELATED);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_RELATED, style.toString());
		style = new IncludeGroupStyle(IncludeKind.PARTNER);
		style.setKeepTogether(true);
		style.setBlankLineBefore(true);
		style.setOrder(0);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_PARTNER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_SAME_FOLDER);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_SAME_FOLDER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_SUBFOLDER);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_SUBFOLDER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.SYSTEM);
		style.setKeepTogether(true);
		style.setBlankLineBefore(true);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_SYSTEM, style.toString());
		style = new IncludeGroupStyle(IncludeKind.SYSTEM_WITH_EXTENSION);
		style.setKeepTogether(true);
		style.setAngleBrackets(true);
		style.setOrder(1);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_SYSTEM_WITH_EXTENSION, style.toString());
		style = new IncludeGroupStyle(IncludeKind.SYSTEM_WITHOUT_EXTENSION);
		style.setKeepTogether(true);
		style.setAngleBrackets(true);
		style.setOrder(2);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION, style.toString());
		style = new IncludeGroupStyle(IncludeKind.OTHER);
		style.setKeepTogether(true);
		style.setBlankLineBefore(true);
		style.setOrder(3);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_OTHER, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_SAME_PROJECT);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_SAME_PROJECT, style.toString());
		style = new IncludeGroupStyle(IncludeKind.IN_OTHER_PROJECT);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_OTHER_PROJECT, style.toString());
		style = new IncludeGroupStyle(IncludeKind.EXTERNAL);
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_EXTERNAL, style.toString());
		store.setDefault(PreferenceConstants.INCLUDE_STYLE_MATCHING_PATTERN, ""); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.INCLUDES_PARTNER_FILE_SUFFIXES, DEFAULT_PARTNER_FILE_SUFFIXES);
		store.setDefault(PreferenceConstants.INCLUDES_HEURISTIC_HEADER_SUBSTITUTION, true);
		store.setDefault(PreferenceConstants.INCLUDES_ALLOW_REORDERING, true);
		store.setDefault(PreferenceConstants.INCLUDES_ALLOW_PARTNER_INDIRECT_INCLUSION, false);
		store.setDefault(PreferenceConstants.FORWARD_DECLARE_COMPOSITE_TYPES, true);
		store.setDefault(PreferenceConstants.FORWARD_DECLARE_ENUMS, false);
		store.setDefault(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, false);
		store.setDefault(PreferenceConstants.FORWARD_DECLARE_TEMPLATES, false);
		store.setDefault(PreferenceConstants.FORWARD_DECLARE_NAMESPACE_ELEMENTS, true);
		store.setDefault(PreferenceConstants.INCLUDES_UNUSED_STATEMENTS_DISPOSITION,
				UnusedStatementDisposition.COMMENT_OUT.toString());

		store.setDefault(PreferenceConstants.INCLUDES_HEADER_SUBSTITUTION,
				HeaderSubstitutionMap.serializeMaps(GCCHeaderSubstitutionMaps.getDefaultMaps()));
		store.setDefault(PreferenceConstants.INCLUDES_SYMBOL_EXPORTING_HEADERS,
				SymbolExportMap.serializeMaps(Collections.singletonList(GCCHeaderSubstitutionMaps.getSymbolExportMap())));
	}
}
