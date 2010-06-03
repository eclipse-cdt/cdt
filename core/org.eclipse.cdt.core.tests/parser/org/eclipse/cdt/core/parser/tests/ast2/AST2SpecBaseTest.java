/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author dsteffle
 */
public class AST2SpecBaseTest extends AST2BaseTest {
	public AST2SpecBaseTest() {
		super();
	}

	public AST2SpecBaseTest(String name) {
		super(name);
	}

	/**
	 * checkSemantics is used to specify whether the example should have semantics checked
	 * since several spec examples have syntactically correct code ONLY this flag was added
	 * so that future tests can ensure that examples are checked against syntax/semantics where necessary
	 * @param code
	 * @param expectedProblemBindings the number of problem bindings you expect to encounter
	 * @throws ParserException
	 */
	protected void parseCandCPP(String code, boolean checkBindings, int expectedProblemBindings) throws ParserException {
		parse(code, ParserLanguage.C, false, true, checkBindings, expectedProblemBindings, null);
		parse(code, ParserLanguage.CPP, false, true, checkBindings, expectedProblemBindings, null);
	}

	protected IASTTranslationUnit parseWithErrors(String code, ParserLanguage lang) throws ParserException {
    	return parse(code, lang, false, false, false, 0, null);
    }

	protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean checkBindings, int expectedProblemBindings) throws ParserException {
    	return parse(code, lang, false, true, checkBindings, expectedProblemBindings, null);
    }
	
	protected IASTTranslationUnit parse(String code, ParserLanguage lang, String[] problems) throws ParserException {
    	return parse(code, lang, false, true, true, problems.length, problems);
	}
    
    private IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, 
    		boolean checkBindings, int expectedProblemBindings, String[] problems) throws ParserException {
		// TODO beef this up with tests... i.e. run once with \n, and then run again with \r\n replacing \n ... etc
		// TODO another example might be to replace all characters with corresponding trigraph/digraph tests...
		
        FileContent codeReader = FileContent.create("<test-code>", code.toCharArray());
		return parse(codeReader, lang, useGNUExtensions, expectNoProblems, checkBindings, expectedProblemBindings, problems);
    }
	
//	private IASTTranslationUnit parse(IFile filename, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems) throws ParserException {
//		CodeReader codeReader=null;
//		try {
//			codeReader = new CodeReader(filename.getName(), filename.getContents());
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//		
//		return parse(codeReader, lang, useGNUExtensions, expectNoProblems);
//    }
	
	private IASTTranslationUnit parse(FileContent codeReader, ParserLanguage lang,
			boolean useGNUExtensions, boolean expectNoProblems, boolean checkBindings,
			int expectedProblemBindings, String[] problems) throws ParserException {
        ScannerInfo scannerInfo = new ScannerInfo();
        IScanner scanner= AST2BaseTest.createScanner(codeReader, lang, ParserMode.COMPLETE_PARSE, scannerInfo);
        
        ISourceCodeParser parser2 = null;
        if (lang == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration config = null;
            if (useGNUExtensions)
            	config = new GPPParserExtensionConfiguration();
            else
            	config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
        } else {
            ICParserExtensionConfiguration config = null;

            if (useGNUExtensions)
            	config = new GCCParserExtensionConfiguration();
            else
            	config = new ANSICParserExtensionConfiguration();
            
            parser2 = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, 
            		NULL_LOG, config);
        }
        
        if (expectedProblemBindings > 0)
    		CPPASTNameBase.sAllowNameComputation= true;

        IASTTranslationUnit tu = parser2.parse();
		
		// resolve all bindings
		if (checkBindings) {
			NameResolver res = new NameResolver();
	        tu.accept(res);
			if (res.problemBindings.size() != expectedProblemBindings)
				throw new ParserException("Expected " + expectedProblemBindings +
						" problems, encountered " + res.problemBindings.size()); 
			if (problems != null) {
				for (int i = 0; i < problems.length; i++) {
					assertEquals(problems[i], res.problemBindings.get(i));
				}
			}
		}

        if (parser2.encounteredError() && expectNoProblems)
            throw new ParserException("FAILURE"); 
         
        if (lang == ParserLanguage.C && expectNoProblems) {
			if (CVisitor.getProblems(tu).length != 0) {
				throw new ParserException("CVisitor has AST Problems"); 
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new ParserException("C TranslationUnit has Preprocessor Problems"); 
			}
        } else if (lang == ParserLanguage.CPP && expectNoProblems) {
			if (CPPVisitor.getProblems(tu).length != 0) {
				throw new ParserException("CPPVisitor has AST Problems"); 
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new ParserException("CPP TranslationUnit has Preprocessor Problems"); 
			}
        }
        
        return tu;
	}
	
	static protected class NameResolver extends ASTVisitor {
		{
			shouldVisitNames = true;
		}
		
		public List<IASTName> nameList = new ArrayList<IASTName>();
		public List<String> problemBindings = new ArrayList<String>();
		public int numNullBindings = 0;
		
		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				problemBindings.add(name.toString());
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		
		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}
		
		public int size() { 
			return nameList.size(); 
		}
	}
}
