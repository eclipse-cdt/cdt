/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Markus Schorn - initial API and implementation
 *      IBM Corporation
 *      Andrew Ferguson (Symbian)
 *      Sergey Prigogin (Google)
 *      Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.osgi.util.NLS;

class DialogsMessages extends NLS {
	public static String AbstractIndexerPage_fileSizeLimit;
	public static String AbstractIndexerPage_includedFileSizeLimit;
	public static String AbstractIndexerPage_heuristicIncludes;
	public static String AbstractIndexerPage_indexAllFiles;
	public static String AbstractIndexerPage_indexAllHeaders;
	public static String AbstractIndexerPage_indexAllHeadersC;
	public static String AbstractIndexerPage_indexAllHeadersCpp;
	public static String AbstractIndexerPage_indexOpenedFiles;
	public static String AbstractIndexerPage_skipAllReferences;
	public static String AbstractIndexerPage_skipImplicitReferences;
	public static String AbstractIndexerPage_skipTypeAndMacroReferences;
	public static String AbstractIndexerPage_skipTypeReferences;
	public static String AbstractIndexerPage_skipMacroReferences;
	public static String AbstractIndexerPage_indexAllHeaderVersions;
	public static String AbstractIndexerPage_indexAllVersionsSpecificHeaders;
	public static String Megabyte;
	public static String IndexerBlock_fixedBuildConfig;
	public static String IndexerBlock_indexerOptions;
	public static String IndexerBlock_buildConfigGroup;
	public static String IndexerBlock_activeBuildConfig;
	public static String PreferenceScopeBlock_enableProjectSettings;
	public static String PreferenceScopeBlock_preferenceLink;
	public static String PreferenceScopeBlock_storeWithProject;
	public static String CacheSizeBlock_absoluteLimit;
	public static String CacheSizeBlock_cacheLimitGroup;
	public static String CacheSizeBlock_headerFileCache;
	public static String CacheSizeBlock_indexDatabaseCache;
	public static String CacheSizeBlock_limitRelativeToMaxHeapSize;

	public static String DocCommentOwnerBlock_DocToolLabel;
	public static String DocCommentOwnerBlock_EnableProjectSpecificSettings;
	public static String DocCommentOwnerBlock_SelectDocToolDescription;
	public static String DocCommentOwnerCombo_None;
	public static String DocCommentOwnerComposite_DocumentationToolGroupTitle;
	public static String RegexErrorParserOptionPage_ConsumeNo;
	public static String RegexErrorParserOptionPage_ConsumeYes;
	public static String RegexErrorParserOptionPage_DescriptionColumn;
	public static String RegexErrorParserOptionPage_EatColumn;
	public static String RegexErrorParserOptionPage_FileColumn;
	public static String RegexErrorParserOptionPage_LineColumn;
	public static String RegexErrorParserOptionPage_LinkToPreferencesMessage;
	public static String RegexErrorParserOptionPage_Pattern_Column;
	public static String RegexErrorParserOptionPage_SeverityColumn;
	public static String RegexErrorParserOptionPage_SeverityError;
	public static String RegexErrorParserOptionPage_SeverityIgnore;
	public static String RegexErrorParserOptionPage_SeverityInfo;
	public static String RegexErrorParserOptionPage_SeverityWarning;
	public static String RegexErrorParserOptionPage_Title;
	public static String RegexErrorParserOptionPage_TooltipConsume;
	public static String RegexErrorParserOptionPage_TooltipDescription;
	public static String RegexErrorParserOptionPage_TooltipFile;
	public static String RegexErrorParserOptionPage_TooltipLine;
	public static String RegexErrorParserOptionPage_TooltipPattern;
	public static String RegexErrorParserOptionPage_TooltipSeverity;
	public static String RegularExpression_EmptyPattern;
	public static String RegularExpression_Validate;
	public static String RegularExpression_Enter;

	static {
		// initialize resource bundle
		NLS.initializeMessages(DialogsMessages.class.getName(), DialogsMessages.class);
	}

	private DialogsMessages() {
	}
}
