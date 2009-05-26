/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, 
 * provides the ability to add LPG based languages to CDT.
 */
@SuppressWarnings({ "restriction", "nls" })
public abstract class BaseExtensibleLanguage extends AbstractLanguage {
			
	
	private static final boolean DEBUG_PRINT_GCC_AST = false;
	private static final boolean DEBUG_PRINT_AST     = false;

	
	
	/**
	 * Retrieve the parser (runs after the preprocessor runs).
	 * 
	 * Can be overridden in subclasses to provide a different parser
	 * for a language extension.
	 */
	protected abstract IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String,String> properties);
	
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
	
	
	@Override
	public IASTTranslationUnit getASTTranslationUnit(CodeReader reader, IScannerInfo scanInfo,
			ICodeReaderFactory fileCreator, IIndex index, int options, IParserLogService log) throws CoreException {
		
		IASTTranslationUnit gtu = null;
		if(DEBUG_PRINT_GCC_AST) {
			System.out.println("\n********************************************************\nParsing\nOptions: " + options);
			
			ILanguage gppLanguage = getParserLanguage() == ParserLanguage.CPP ? GPPLanguage.getDefault() : GCCLanguage.getDefault();
			gtu = gppLanguage.getASTTranslationUnit(reader, scanInfo, fileCreator, index, options, log);
			
			System.out.println(gppLanguage.getName() + " AST:");
			ASTPrinter.print(gtu);
			System.out.println();
		}

		IScannerExtensionConfiguration config = getScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguage();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setComputeImageLocations((options & ILanguage.OPTION_NO_IMAGE_LOCATIONS) == 0);
		
		Map<String,String> parserProperties = new HashMap<String,String>();
		parserProperties.put(LRParserProperties.TRANSLATION_UNIT_PATH, reader.getPath());
		if((options & OPTION_SKIP_FUNCTION_BODIES) != 0)
			parserProperties.put(LRParserProperties.SKIP_FUNCTION_BODIES, "true");
		if((options & OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0)
			parserProperties.put(LRParserProperties.SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS, "true");
		
		IParser<IASTTranslationUnit> parser = getParser(preprocessor, index, parserProperties);
		IASTTranslationUnit tu = parser.parse();
		tu.setIsHeaderUnit((options & OPTION_IS_SOURCE_UNIT) == 0); // the TU is marked as either a source file or a header file
		
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
		
		
		Map<String,String> parserProperties = new HashMap<String,String>();
		parserProperties.put(LRParserProperties.TRANSLATION_UNIT_PATH, reader.getPath());
		parserProperties.put(LRParserProperties.SKIP_FUNCTION_BODIES, "true");
		parserProperties.put(LRParserProperties.SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS, "true");
		
		IParser<IASTTranslationUnit> parser = getParser(preprocessor, index, parserProperties);
		parser.parse();
		
		IASTCompletionNode completionNode = parser.getCompletionNode();
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			printCompletionNode(completionNode);
		}
		
		return completionNode;
	}
	
	
	/*
	 * For debugging.
	 */
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
	
	public IContributedModelBuilder createModelBuilder(@SuppressWarnings("unused") ITranslationUnit tu) {
		return null;
	}
	
}
