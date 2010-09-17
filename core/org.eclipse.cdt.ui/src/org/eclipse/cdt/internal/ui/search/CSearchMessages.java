/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrey Eremchenko (LEDAS)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.osgi.util.NLS;

public final class CSearchMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.search.CSearchMessages";//$NON-NLS-1$

	private CSearchMessages() {
		// Do not instantiate
	}

	public static String group_declarations;
	public static String group_references;
	public static String CSearchResultCollector_matches;
	public static String CSearchResultCollector_line;
	public static String CSearchResultCollector_location;
	public static String CSearchPage_searchFor_label;
	public static String CSearchPage_searchFor_namespace;
	public static String CSearchPage_searchFor_method;
	public static String CSearchPage_searchFor_function;
	public static String CSearchPage_searchFor_field;
	public static String CSearchPage_searchFor_variable;
	public static String CSearchPage_searchFor_union;
	public static String CSearchPage_searchFor_enum;
	public static String CSearchPage_searchFor_enumr;
	public static String CSearchPage_searchFor_typedef;
	public static String CSearchPage_searchFor_macro;
	public static String CSearchPage_searchFor_any;
	public static String CSearchPage_searchFor_classStruct;
	public static String CSearchPage_limitTo_label;
	public static String CSearchPage_limitTo_declarations;
	public static String CSearchPage_limitTo_definitions;
	public static String CSearchPage_limitTo_references;
	public static String CSearchPage_limitTo_allOccurrences;
	public static String CSearchPage_expression_label;
	public static String CSearchPage_expression_caseSensitive;
	public static String CSearch_FindDeclarationAction_label;
	public static String CSearch_FindDeclarationAction_tooltip;
	public static String CSearch_FindDeclarationsProjectAction_label;
	public static String CSearch_FindDeclarationsProjectAction_tooltip;
	public static String CSearch_FindReferencesAction_label;
	public static String CSearch_FindReferencesAction_tooltip;
	public static String CSearch_FindReferencesProjectAction_label;
	public static String CSearch_FindReferencesProjectAction_tooltip;
	public static String CSearch_FindReferencesInWorkingSetAction_label;
	public static String CSearch_FindReferencesInWorkingSetAction_tooltip;
	public static String CSearchOperation_operationUnavailable_message;
	public static String WorkspaceScope;
	public static String WorkingSetScope;
	public static String SelectionScope;
	public static String ProjectScope;
	public static String PDOMSearchQuery_refs_label;
	public static String PDOMSearchQuery_defs_label;
	public static String PDOMSearchQuery_decls_label;
	public static String PDOMSearchQuery_refs_result_label;
	public static String PDOMSearchQuery_defs_result_label;
	public static String PDOMSearchQuery_decls_result_label;
	public static String PDOMSearchQuery_decldefs_result_label;
	public static String PDOMSearchQuery_occurrences_result_label;
	public static String PDOMSearchElementQuery_something;
	public static String PDOMSearchPatternQuery_PatternQuery_labelPatternInScope;
	public static String PDOMSearch_query_pattern_error;
	public static String SelectionParseAction_FileOpenFailure_format;
	public static String SelectionParseAction_SelectedTextNotSymbol_message;
	public static String SelectionParseAction_SymbolNotFoundInIndex_format;
	public static String SelectionParseAction_IncludeNotFound_format;

	public static String OccurrencesFinder_no_element;
	public static String OccurrencesFinder_no_binding;
	public static String OccurrencesFinder_searchfor;
	public static String OccurrencesFinder_label_singular;
	public static String OccurrencesFinder_label_plural;
	public static String OccurrencesFinder_occurrence_description;
	public static String OccurrencesFinder_occurrence_write_description;

	public static String PDOMSearchListContentProvider_IndexerNotEnabledMessageFormat;
	public static String PDOMSearchListContentProvider_ProjectClosedMessageFormat;
	public static String CSearchMessages_IndexRunningIncompleteWarning;
	public static String HidePolymorphicCalls_actionLabel;
	public static String HidePolymorphicCalls_description;
	public static String HidePolymorphicCalls_name;
	public static String PDOMSearchViewPage_ShowEnclosingDefinitions_actionLabel;
	public static String PDOMSearchViewPage_ShowEnclosingDefinitions_description;
	public static String PDOMSearchViewPageLocationColumn_label;
	public static String PDOMSearchViewPageDefinitionColumn_label;
	public static String PDOMSearchViewPageMatchColumn_label;
	public static String PDOMSearchTreeContentProvider_IndexerNotEnabledWarning;
	public static String PDOMSearchTreeContentProvider_ProjectClosedWarning;
	public static String PDOMSearchUnresolvedIncludesQuery_title;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CSearchMessages.class);
	}
}