/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract C language. Derived classes need only implement
 * {@link getScannerExtensionConfiguration()} and 
 * {@link getParserExtensionConfiguration()}.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @see AbstractScannerExtensionConfiguration
 * @see AbstractCParserExtensionConfiguration
 * 
 * @since 4.0
 */
public abstract class AbstractCLanguage extends AbstractLanguage implements ICLanguageKeywords {

	protected static class NameCollector extends CASTVisitor {
		{
			shouldVisitNames= true;
		}

		private List nameList= new ArrayList();

		public int visit(IASTName name) {
			nameList.add(name);
			return PROCESS_CONTINUE;
		}

		public IASTName[] getNames() {
			return (IASTName[]) nameList.toArray(new IASTName[nameList.size()]);
		}
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class) {
			return new PDOMCLinkageFactory();
		}
		return super.getAdapter(adapter);
	}

	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory codeReaderFactory, IIndex index, IParserLogService log) throws CoreException {

		IScanner scanner= createScanner(reader, scanInfo, codeReaderFactory, log);
		ISourceCodeParser parser= createParser(scanner, log, index, false);

		// Parse
		IASTTranslationUnit ast= parser.parse();
		return ast;
	}

	public IASTCompletionNode getCompletionNode(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, IParserLogService log, int offset) throws CoreException {

		IScanner scanner= createScanner(reader, scanInfo, fileCreator, log);
		scanner.setContentAssistMode(offset);

		ISourceCodeParser parser= createParser(scanner, log, index, true);

		// Run the parse and return the completion node
		parser.parse();
		IASTCompletionNode node= parser.getCompletionNode();
		return node;
	}


	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		IASTNode selectedNode= ast.selectNodeForLocation(ast.getFilePath(), start, length);

		if (selectedNode == null)
			return new IASTName[0];

		if (selectedNode instanceof IASTName)
			return new IASTName[] { (IASTName) selectedNode };

		NameCollector collector= new NameCollector();
		selectedNode.accept(collector);
		return collector.getNames();
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// Use the default CDT model builder
		return null;
	}

	/**
	 * Create the scanner to be used with the parser.
	 * 
	 * @param reader  the code reader for the main file
	 * @param scanInfo  the scanner information (macros, include pathes)
	 * @param fileCreator  the code reader factory for inclusions
	 * @param log  the log for debugging
	 * @return an instance of IScanner
	 */
	protected IScanner createScanner(CodeReader reader, IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IParserLogService log) {
		return new DOMScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, ParserLanguage.C,
				log, getScannerExtensionConfiguration(), fileCreator);
	}

	/**
	 * Create the parser.
	 * 
	 * @param scanner  the IScanner to get tokens from
	 * @param log  the parser log service
	 * @param index  the index to help resolve bindings
	 * @param forCompletion  whether the parser is used for code completion
	 * @return  an instance of ISourceCodeParser
	 */
	protected ISourceCodeParser createParser(IScanner scanner, IParserLogService log, IIndex index, boolean forCompletion) {
		ParserMode mode= forCompletion ? ParserMode.COMPLETION_PARSE : ParserMode.COMPLETE_PARSE;
		return new GNUCSourceParser(scanner, mode, log, getParserExtensionConfiguration(), index);
	}

	/**
	 * @return the scanner extension configuration for this language, may not
	 *         return <code>null</code>
	 */
	protected abstract IScannerExtensionConfiguration getScannerExtensionConfiguration();

	/**
	 * @return the parser extension configuration for this language, may not
	 *         return <code>null</code>
	 */
	protected abstract ICParserExtensionConfiguration getParserExtensionConfiguration();

	/*
	 * @see org.eclipse.cdt.core.model.ICLanguageKeywords#getKeywords()
	 */
	public String[] getKeywords() {
		Set keywords= KeywordSets.getKeywords(KeywordSetKey.KEYWORDS, ParserLanguage.C);
		keywords= new HashSet(keywords);
		List additionalKeywords= getScannerExtensionConfiguration().getAdditionalKeywords().toList();
		for (Iterator iterator = additionalKeywords.iterator(); iterator.hasNext(); ) {
			char[] name = (char[]) iterator.next();
			keywords.add(new String(name));
		}
		return (String[]) keywords.toArray(new String[keywords.size()]);
	}

	/*
	 * @see org.eclipse.cdt.core.model.ICLanguageKeywords#getBuiltinTypes()
	 */
	public String[] getBuiltinTypes() {
		Set types= KeywordSets.getKeywords(KeywordSetKey.TYPES, ParserLanguage.C);
		return (String[]) types.toArray(new String[types.size()]);
	}

	/*
	 * @see org.eclipse.cdt.core.model.ICLanguageKeywords#getPreprocessorKeywords()
	 */
	public String[] getPreprocessorKeywords() {
		Set keywords= KeywordSets.getKeywords(KeywordSetKey.PP_DIRECTIVE, ParserLanguage.C);
		keywords= new HashSet(keywords);
		List additionalKeywords= getScannerExtensionConfiguration().getAdditionalPreprocessorKeywords().toList();
		for (Iterator iterator = additionalKeywords.iterator(); iterator.hasNext(); ) {
			char[] name = (char[]) iterator.next();
			keywords.add(new String(name));
		}
		return (String[]) keywords.toArray(new String[keywords.size()]);
	}
}
