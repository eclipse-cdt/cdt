/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IParserSettings;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParseError.ParseErrorKind;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.util.ICancelable;
import org.eclipse.cdt.internal.core.util.ICanceler;
import org.eclipse.core.runtime.CoreException;

/**
 * This class provides a skeletal implementation of the ILanguage interface
 * for the DOM parser framework.
 *
 * This class uses the template method pattern, derived classes need only implement
 * {@link AbstractCLikeLanguage#getScannerExtensionConfiguration(IScannerInfo info)},
 * {@link AbstractCLikeLanguage#getParserLanguage()} and
 * {@link AbstractCLikeLanguage#createParser(IScanner scanner, ParserMode parserMode,
 *                                           IParserLogService logService, IIndex index)}.
 *
 * @see AbstractScannerExtensionConfiguration
 *
 * @since 5.0
 */
public abstract class AbstractCLikeLanguage extends AbstractLanguage implements ICLanguageKeywords {
	private static final AbstractScannerExtensionConfiguration DUMMY_SCANNER_EXTENSION_CONFIGURATION = new AbstractScannerExtensionConfiguration() {
	};

	static class NameCollector extends ASTVisitor {
		{
			shouldVisitNames = true;
		}

		private List<IASTName> nameList = new ArrayList<>();

		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			return PROCESS_CONTINUE;
		}

		public IASTName[] getNames() {
			return nameList.toArray(new IASTName[nameList.size()]);
		}
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @deprecated Do not override this method.
	 *     Override {@link #getScannerExtensionConfiguration(IScannerInfo)} instead.
	 */
	@Deprecated
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return DUMMY_SCANNER_EXTENSION_CONFIGURATION;
	}

	/**
	 * @return the scanner extension configuration for this language. May not return {@code null}.
	 * @since 5.4
	 */
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration(IScannerInfo info) {
		return getScannerExtensionConfiguration();
	}

	/**
	 * @returns the actual parser object.
	 */
	protected abstract ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode,
			IParserLogService logService, IIndex index);

	/**
	 * @returns the actual parser object, configured with additional settings.
	 * @since 5.6
	 */
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index, int options, IParserSettings settings) {
		return createParser(scanner, parserMode, logService, index);
	}

	/**
	 * @return The ParserLanguage value corresponding to the language supported.
	 */
	protected abstract ParserLanguage getParserLanguage();

	@Deprecated
	@Override
	public IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader,
			IScannerInfo scanInfo, org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IIndex index,
			IParserLogService log) throws CoreException {
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}

	@Deprecated
	@Override
	public IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader,
			IScannerInfo scanInfo, org.eclipse.cdt.core.dom.ICodeReaderFactory codeReaderFactory, IIndex index,
			int options, IParserLogService log) throws CoreException {
		return getASTTranslationUnit(FileContent.adapt(reader), scanInfo,
				IncludeFileContentProvider.adapt(codeReaderFactory), index, options, log);
	}

	@Override
	public IASTTranslationUnit getASTTranslationUnit(FileContent reader, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, int options, IParserLogService log)
			throws CoreException {
		final IScanner scanner = createScanner(reader, scanInfo, fileCreator, log);
		scanner.setComputeImageLocations((options & OPTION_NO_IMAGE_LOCATIONS) == 0);
		scanner.setProcessInactiveCode((options & OPTION_PARSE_INACTIVE_CODE) != 0);

		IParserSettings parserSettings = null;
		if (scanInfo instanceof ExtendedScannerInfo) {
			ExtendedScannerInfo extendedScannerInfo = (ExtendedScannerInfo) scanInfo;
			parserSettings = extendedScannerInfo.getParserSettings();
		}
		final ISourceCodeParser parser = createParser(scanner, log, index, false, options, parserSettings);

		// Make it possible to cancel parser by reconciler - http://bugs.eclipse.org/226682
		ICanceler canceler = null;
		if (log instanceof ICanceler) {
			canceler = (ICanceler) log;
			canceler.setCancelable(new ICancelable() {
				@Override
				public void cancel() {
					scanner.cancel();
					parser.cancel();
				}
			});
		}

		try {
			// Parse
			return parser.parse();
		} catch (ParseError e) {
			// Only the TOO_MANY_TOKENS error can be handled here.
			if (e.getErrorKind() != ParseErrorKind.TOO_MANY_TOKENS)
				throw e;

			// Otherwise generate a log because parsing was stopped because of a user preference.
			if (log != null) {
				String tuName = null;
				if (scanner.getLocationResolver() != null)
					tuName = scanner.getLocationResolver().getTranslationUnitPath();

				log.traceLog(e.getMessage() + (tuName == null ? "" : (" while parsing " + tuName))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		} finally {
			if (canceler != null) {
				canceler.setCancelable(null);
			}
		}
	}

	@Deprecated
	@Override
	public IASTCompletionNode getCompletionNode(org.eclipse.cdt.core.parser.CodeReader reader, IScannerInfo scanInfo,
			org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset)
			throws CoreException {
		return getCompletionNode(FileContent.adapt(reader), scanInfo, IncludeFileContentProvider.adapt(fileCreator),
				index, log, offset);
	}

	@Override
	public IASTCompletionNode getCompletionNode(FileContent reader, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, IParserLogService log, int offset)
			throws CoreException {
		IScanner scanner = createScanner(reader, scanInfo, fileCreator, log);
		scanner.setContentAssistMode(offset);

		ISourceCodeParser parser = createParser(scanner, log, index, true, 0);

		// Run the parse and return the completion node
		parser.parse();
		IASTCompletionNode node = parser.getCompletionNode();
		return node;
	}

	/**
	 * Creates the parser.
	 *
	 * @param scanner  the IScanner to get tokens from
	 * @param log  the parser log service
	 * @param index  the index to help resolve bindings
	 * @param forCompletion  whether the parser is used for code completion
	 * @param options for valid options see
	 *     {@link AbstractLanguage#getASTTranslationUnit(FileContent, IScannerInfo, IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * @return  an instance of ISourceCodeParser
	 */
	protected ISourceCodeParser createParser(IScanner scanner, IParserLogService log, IIndex index,
			boolean forCompletion, int options) {
		ParserMode mode = createParserMode(forCompletion, options);
		return createParser(scanner, mode, log, index);
	}

	/**
	 * Create the parser with additional settings.
	 *
	 * @param scanner  the IScanner to get tokens from
	 * @param log  the parser log service
	 * @param index  the index to help resolve bindings
	 * @param forCompletion  whether the parser is used for code completion
	 * @param options for valid options see
	 *     {@link AbstractLanguage#getASTTranslationUnit(FileContent, IScannerInfo, IncludeFileContentProvider, IIndex, int, IParserLogService)}
	 * @param settings for the parser
	 * @return  an instance of ISourceCodeParser
	 * @since 5.6
	 */
	protected ISourceCodeParser createParser(IScanner scanner, IParserLogService log, IIndex index,
			boolean forCompletion, int options, IParserSettings settings) {
		ParserMode mode = createParserMode(forCompletion, options);
		return createParser(scanner, mode, log, index, options, settings);
	}

	private ParserMode createParserMode(boolean forCompletion, int options) {
		if (forCompletion) {
			return ParserMode.COMPLETION_PARSE;
		} else if ((options & OPTION_SKIP_FUNCTION_BODIES) != 0) {
			return ParserMode.STRUCTURAL_PARSE;
		} else {
			return ParserMode.COMPLETE_PARSE;
		}
	}

	/**
	 * @deprecated Replaced by
	 *             {@link #createScanner(FileContent, IScannerInfo, IncludeFileContentProvider, IParserLogService)}
	 */
	@Deprecated
	protected IScanner createScanner(org.eclipse.cdt.core.parser.CodeReader reader, IScannerInfo scanInfo,
			org.eclipse.cdt.core.dom.ICodeReaderFactory fileCreator, IParserLogService log) {
		return createScanner(FileContent.adapt(reader), scanInfo, IncludeFileContentProvider.adapt(fileCreator), log);
	}

	/**
	 * Create the scanner to be used with the parser.
	 * @since 5.2
	 */
	protected IScanner createScanner(FileContent content, IScannerInfo scanInfo, IncludeFileContentProvider fcp,
			IParserLogService log) {
		return new CPreprocessor(content, scanInfo, getParserLanguage(), log,
				getScannerExtensionConfiguration(scanInfo), fcp);
	}

	@Override
	@Deprecated
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		IASTNode selectedNode = ast.getNodeSelector(null).findNode(start, length);

		if (selectedNode == null)
			return new IASTName[0];

		if (selectedNode instanceof IASTName)
			return new IASTName[] { (IASTName) selectedNode };

		if (selectedNode instanceof IASTPreprocessorMacroExpansion) {
			return new IASTName[] { ((IASTPreprocessorMacroExpansion) selectedNode).getMacroReference() };
		}

		NameCollector collector = new NameCollector();
		selectedNode.accept(collector);
		return collector.getNames();
	}

	@Override
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// Use default model builder.
		return null;
	}

	private ICLanguageKeywords cLanguageKeywords;

	private synchronized ICLanguageKeywords getCLanguageKeywords() {
		if (cLanguageKeywords == null) {
			cLanguageKeywords = new CLanguageKeywords(getParserLanguage(),
					getScannerExtensionConfiguration(new ExtendedScannerInfo()));
		}
		return cLanguageKeywords;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(ICLanguageKeywords.class))
			return (T) getCLanguageKeywords();

		return super.getAdapter(adapter);
	}

	// For backwards compatibility
	@Override
	public String[] getBuiltinTypes() {
		return getCLanguageKeywords().getBuiltinTypes();
	}

	@Override
	public String[] getKeywords() {
		return getCLanguageKeywords().getKeywords();
	}

	@Override
	public String[] getPreprocessorKeywords() {
		return getCLanguageKeywords().getPreprocessorKeywords();
	}
}
