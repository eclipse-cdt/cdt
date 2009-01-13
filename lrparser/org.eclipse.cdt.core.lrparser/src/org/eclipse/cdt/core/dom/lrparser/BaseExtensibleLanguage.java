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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, 
 * provides the ability to add LPG based languages to CDT.
 *
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public abstract class BaseExtensibleLanguage extends AbstractLanguage {
			
	
	private static final boolean DEBUG_PRINT_GCC_AST = false;
	private static final boolean DEBUG_PRINT_AST     = false;
	
	
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
	protected abstract IDOMTokenMap getTokenMap();
	
	
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
	protected abstract ParserLanguage getParserLanguage();
	
	
	/**
	 * Returns the scanner extension configuration for this language, may not return null
	 */
	protected abstract IScannerExtensionConfiguration getScannerExtensionConfiguration();
	
	
	
	@SuppressWarnings("nls")
	@Override
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) throws CoreException {
		
		IASTTranslationUnit gtu = null;
		if(DEBUG_PRINT_GCC_AST) {
			System.out.println();
			System.out.println("********************************************************");
			System.out.println("Parsing");
			System.out.println("Options: " + options);
			
			ILanguage gppLanguage = getParserLanguage() == ParserLanguage.CPP ? GPPLanguage.getDefault() : GCCLanguage.getDefault();
			gtu = gppLanguage.getASTTranslationUnit(reader, scanInfo, fileCreator, index, options, log);
			
			System.out.println("GPP AST:");
			ASTPrinter.print(gtu);
			System.out.println();
		}

		IScannerExtensionConfiguration config = getScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguage();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setScanComments((options & OPTION_ADD_COMMENTS) != 0);
		preprocessor.setComputeImageLocations((options & ILanguage.OPTION_NO_IMAGE_LOCATIONS) == 0);
		
		// The translation unit has to be created here so that the preprocessor
		// can fill in the preprocessor AST nodes.
		IASTTranslationUnit tu = getASTTranslationUnit(index, preprocessor);
		IParser parser = getParser();
		CPreprocessorAdapter.runCPreprocessor(preprocessor, parser, getTokenMap(), tu);
		
		Set<IParser.Options> parserOptions = new HashSet<IParser.Options>();
		//if((options & OPTION_SKIP_FUNCTION_BODIES) != 0)
		//	parserOptions.add(IParser.Options.OPTION_SKIP_FUNCTION_BODIES);
		if((options & OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0)
			parserOptions.add(IParser.Options.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS);
		
		if(!parserOptions.isEmpty())
			parserOptions = EnumSet.copyOf(parserOptions);
		
		parser.parse(tu, parserOptions); // The parser will fill in the rest of the AST
		
		// the TU is marked as either a source file or a header file
		tu.setIsHeaderUnit((options & OPTION_IS_SOURCE_UNIT) == 0);
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			ASTPrinter.print(tu);
		}
		
		return tu;
	}
	
	
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log) throws CoreException {
		
		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}

	
	@SuppressWarnings("nls")
	public IASTCompletionNode getCompletionNode(CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
			IIndex index, IParserLogService log, int offset) throws CoreException {
		
		
		IASTCompletionNode cn;
		if(DEBUG_PRINT_GCC_AST) {
			ILanguage gppLanguage = GCCLanguage.getDefault();
			cn = gppLanguage.getCompletionNode(reader, scanInfo, fileCreator, index, log, offset);
			
			System.out.println();
			System.out.println("********************************************************");
			System.out.println("GPP AST:");
			printCompletionNode(cn);
		}
		
		IScannerExtensionConfiguration config = getScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguage();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setContentAssistMode(offset);
		
		IParser parser = getParser();
		IASTTranslationUnit tu = getASTTranslationUnit(index, preprocessor);
		
		CPreprocessorAdapter.runCPreprocessor(preprocessor, parser, getTokenMap(), tu);
		
		// the parser will fill in the rest of the AST
		Set<IParser.Options> parserOptions = EnumSet.of(IParser.Options.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS);
		IASTCompletionNode completionNode = parser.parse(tu, parserOptions);
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			printCompletionNode(completionNode);
		}
		
		return completionNode;
	}
	
	
	private IASTTranslationUnit getASTTranslationUnit(IIndex index, IScanner preprocessor) {
		IASTTranslationUnit tu = createASTTranslationUnit();
		tu.setIndex(index);
		if(tu instanceof ASTTranslationUnit) {
			((ASTTranslationUnit)tu).setLocationResolver(preprocessor.getLocationResolver());
		}
		return tu;
	}
	
	/*
	 * For debugging.
	 */
	@SuppressWarnings("nls")
	private static void printCompletionNode(IASTCompletionNode cn) {
		if(cn == null) {
			System.out.println("Completion node is null");
			return;
		}
			
		ASTPrinter.print(cn.getTranslationUnit());
		for(IASTName name : cn.getNames()) {
			ASTNode context = (ASTNode)name.getCompletionContext();
			System.out.printf("Name: %s, Context: %s, At: %d", 
					name, DebugUtil.safeClassName(context), context == null ? null : context.getOffset());
			if(name.getTranslationUnit() == null) // some name nodes are not hooked up to the AST
				System.out.print(", not hooked up");
			System.out.println();
		}
		System.out.println();
	}
	
	
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		return GCCLanguage.getDefault().getSelectedNames(ast, start, length);
	}
	
	private ICLanguageKeywords cLanguageKeywords = new CLanguageKeywords(getParserLanguage(), getScannerExtensionConfiguration());
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if(ICLanguageKeywords.class.equals(adapter))
			return cLanguageKeywords;
		if(IPDOMLinkageFactory.class.equals(adapter)) {
			if(getParserLanguage().isCPP())
				return new PDOMCPPLinkageFactory();
			return new PDOMCLinkageFactory();
		}
		
		return super.getAdapter(adapter);
	}
	
}
