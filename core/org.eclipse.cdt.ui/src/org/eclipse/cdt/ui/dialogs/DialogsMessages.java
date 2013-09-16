/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Markus Schorn - initial API and implementation
 *      IBM Corporation
 *      Andrew Ferguson (Symbian)
 *      Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.osgi.util.NLS;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DialogsMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.dialogs.DialogsMessages"; //$NON-NLS-1$
	/** @since 5.2 */
	public static String AbstractIndexerPage_fileSizeLimit;
	/** @since 5.1 */
	public static String AbstractIndexerPage_heuristicIncludes;
	public static String AbstractIndexerPage_indexAllFiles;
	/** @since 5.1 */
	public static String AbstractIndexerPage_indexAllHeaders;
	/** @since 5.1 */
	public static String AbstractIndexerPage_indexAllHeadersC;
	/** @since 5.1 */
	public static String AbstractIndexerPage_indexAllHeadersCpp;
	/** @since 5.3 */
	public static String AbstractIndexerPage_indexOpenedFiles;
	public static String AbstractIndexerPage_indexUpFront;
	public static String AbstractIndexerPage_skipAllReferences;
	/** @since 5.1 */
	public static String AbstractIndexerPage_skipImplicitReferences;
	/** @since 5.2 */
	public static String AbstractIndexerPage_skipTypeAndMacroReferences;
	public static String AbstractIndexerPage_skipTypeReferences;
	public static String AbstractIndexerPage_skipMacroReferences;
	public static String CacheSizeBlock_MB;
	public static String IndexerBlock_fixedBuildConfig;
	/** @since 5.3 */
	public static String IndexerBlock_indexerOptions;
	public static String IndexerStrategyBlock_activeBuildConfig;
	public static String IndexerStrategyBlock_autoUpdate;
	public static String IndexerStrategyBlock_buildConfigGroup;
	public static String IndexerStrategyBlock_immediateUpdate;
	public static String IndexerStrategyBlock_specificBuildConfig;
	public static String IndexerStrategyBlock_strategyGroup;
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
	/** @since 5.7 */
	public static String AbstractIndexerPage_skipDescriptions;

	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_ConsumeNo;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_ConsumeYes;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_DescriptionColumn;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_EatColumn;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_FileColumn;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_LineColumn;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_LinkToPreferencesMessage;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_Pattern_Column;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_SeverityColumn;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_SeverityError;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_SeverityIgnore;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_SeverityInfo;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_SeverityWarning;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_Title;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_TooltipConsume;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_TooltipDescription;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_TooltipFile;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_TooltipLine;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_TooltipPattern;
	/** @since 5.2 */
	public static String RegexErrorParserOptionPage_TooltipSeverity;
	/** @since 5.2 */
	public static String RegularExpression_EmptyPattern;
	/** @since 5.2 */
	public static String RegularExpression_Validate;
	/** @since 5.2 */
	public static String RegularExpression_Enter;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DialogsMessages.class);
	}

	private DialogsMessages() {
	}
}
