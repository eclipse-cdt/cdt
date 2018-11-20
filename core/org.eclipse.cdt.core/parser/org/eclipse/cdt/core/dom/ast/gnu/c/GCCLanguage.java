/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Anton Leherbauer (Wind River Systems)
 *    Mike Kucera - IBM
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IParserSettings;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;

/**
 * Concrete ILanguage implementation for the DOM C parser.
 */
public class GCCLanguage extends AbstractCLikeLanguage {
	protected static final GCCScannerExtensionConfiguration C_GNU_SCANNER_EXTENSION = GCCScannerExtensionConfiguration
			.getInstance();
	protected static final GCCParserExtensionConfiguration C_GNU_PARSER_EXTENSION = GCCParserExtensionConfiguration
			.getInstance();
	// Must match the id in the extension
	public static final String ID = CCorePlugin.PLUGIN_ID + ".gcc"; //$NON-NLS-1$

	private static final GCCLanguage DEFAULT_INSTANCE = new GCCLanguage();

	public static GCCLanguage getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IPDOMLinkageFactory.class)) {
			return (T) new PDOMCLinkageFactory();
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int getLinkageID() {
		return ILinkage.C_LINKAGE_ID;
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @deprecated Since 5.4 not called by the framework. Override
	 *     {@link #getScannerExtensionConfiguration(IScannerInfo)} instead.
	 */
	@Deprecated
	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return C_GNU_SCANNER_EXTENSION;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration(IScannerInfo info) {
		return GCCScannerExtensionConfiguration.getInstance(info);
	}

	/**
	 * Returns the extension configuration used for creating the parser.
	 * @since 5.1
	 */
	protected ICParserExtensionConfiguration getParserExtensionConfiguration() {
		return C_GNU_PARSER_EXTENSION;
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index) {
		return new GNUCSourceParser(scanner, parserMode, logService, getParserExtensionConfiguration(), index);
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index, int options, IParserSettings settings) {
		GNUCSourceParser parser = new GNUCSourceParser(scanner, parserMode, logService,
				getParserExtensionConfiguration(), index);
		if (settings != null) {
			int maximumTrivialExpressions = settings.getMaximumTrivialExpressionsInAggregateInitializers();
			if (maximumTrivialExpressions >= 0
					&& (options & OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0) {
				parser.setMaximumTrivialExpressionsInAggregateInitializers(maximumTrivialExpressions);
			}
		}
		return parser;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.C;
	}
}
