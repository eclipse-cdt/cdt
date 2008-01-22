/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.util.DebugUtil;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, 
 * provides the ability to add LPG based languages to CDT.
 *
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public abstract class BaseExtensibleLanguage extends AbstractLanguage implements ILanguage, ICLanguageKeywords {
			
	
	/**
	 * Retrieve the parser (runs after the preprocessor runs).
	 * 
	 * Can be overridden in subclasses to provide a different parser
	 * for a language extension.
	 */
	protected abstract IParser getParser();
	
	
	/**
	 * A token map is used to map tokens from the DOM preprocessor
	 * to the tokens defined by an LPG parser.
	 */
	protected abstract ITokenMap getTokenMap();
	
	
	/**
	 * Normally all the AST nodes are created by the parser, but we
	 * need the root node ahead of time.
	 * 
	 * The preprocessor is responsible for creating preprocessor AST nodes,
     * so the preprocessor needs access to the translation unit so that it can
     * set the parent pointers on the AST nodes it creates.
     * 
	 * @return an IASTTranslationUnit object thats empty and will be filled in by the parser
	 */
	protected abstract IASTTranslationUnit createASTTranslationUnit();
	
	
	/**
	 * Returns the ParserLanguage value that is to be used when creating
	 * an instance of CPreprocessor.
	 * 
	 */
	protected abstract ParserLanguage getParserLanguageForPreprocessor();
	
	
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class)
			return new PDOMCLinkageFactory();
		
		return super.getAdapter(adapter);
	}
	
	
	
	
	@SuppressWarnings("nls")
	@Override
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) throws CoreException {
		
		ILanguage gccLanguage = GCCLanguage.getDefault();
		IASTTranslationUnit gtu = gccLanguage.getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
		
		System.out.println();
		System.out.println("********************************************************");
		System.out.println("GCC AST:");
		DebugUtil.printAST(gtu);
		System.out.println();
		
		
		
		//IParseResult parseResult = parse(reader, scanInfo, fileCreator, index, null, null);
		//IASTTranslationUnit tu = parseResult.getTranslationUnit();
		
		// TODO temporary
		IScannerExtensionConfiguration config = new GCCScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguageForPreprocessor();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setScanComments((options & OPTION_ADD_COMMENTS) != 0);
		preprocessor.setComputeImageLocations((options & AbstractLanguage.OPTION_NO_IMAGE_LOCATIONS) == 0);
		
		IParser parser = getParser();
		IASTTranslationUnit tu = createTranslationUnit(index, preprocessor);
		
		CPreprocessorAdapter.runCPreprocessor(preprocessor, parser, getTokenMap(), tu);
		
		parser.parse(tu); // the parser will fill in the rest of the AST
		
		
		System.out.println("Base Extensible Language AST:");
		//DebugUtil.printAST(tu);
		return tu;
	}
	
	
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException {
		
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}

	
	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset) {
		
		// TODO temporary
		IScannerExtensionConfiguration config = new GCCScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguageForPreprocessor();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setContentAssistMode(offset);
		
		IParser parser = getParser();
		IASTTranslationUnit tu = createTranslationUnit(index, preprocessor);
		
		CPreprocessorAdapter.runCPreprocessor(preprocessor, parser, getTokenMap(), tu);
		
		// the parser will fill in the rest of the AST
		IASTCompletionNode completionNode = parser.parse(tu);
		return completionNode;
	}
	
	
	/**
	 * Gets the translation unit object and sets the index and the location resolver. 
	 */
	private IASTTranslationUnit createTranslationUnit(IIndex index, IScanner preprocessor) {
		IASTTranslationUnit tu = createASTTranslationUnit();
		tu.setIndex(index);
		if(tu instanceof CASTTranslationUnit) {
			((CASTTranslationUnit)tu).setLocationResolver(preprocessor.getLocationResolver());
		}
		return tu;
	}
	
}
