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
package org.eclipse.cdt.core.lrparser.tests.c99;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility methods for parsing test code using the C99 LPG parser.
 * 
 * @author Mike Kucera
 */
public class ParseHelper {
	
	static int testsRun = 0;
	
	private static class C99NameResolver extends ASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		
		public int visit( IASTName name ){
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
	}
	
	

	public static IASTTranslationUnit parse(char[] code, BaseExtensibleLanguage lang, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		CodeReader codeReader = new CodeReader(code);
		return parse(codeReader, lang, new ScannerInfo(), null, expectNoProblems, checkBindings, expectedProblemBindings);
	}
	
	public static IASTTranslationUnit parse(String code, BaseExtensibleLanguage lang, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		return parse(code.toCharArray(), lang, expectNoProblems, checkBindings, expectedProblemBindings);
	}
	
	
	public static IASTTranslationUnit parse(String code, BaseExtensibleLanguage lang, boolean expectNoProblems) {
		return parse(code, lang, expectNoProblems, false, 0);
	}



	public static IASTTranslationUnit parse(CodeReader codeReader, BaseExtensibleLanguage language, IScannerInfo scanInfo, 
			                                ICodeReaderFactory fileCreator, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) {
		testsRun++;
		
		IASTTranslationUnit tu;
		try {
			tu = language.getASTTranslationUnit(codeReader, scanInfo, fileCreator, null, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}

		// should parse correctly first before we look at the bindings
        if(expectNoProblems )
        {
			if (CVisitor.getProblems(tu).length != 0) {
				throw new AssertionFailedError(" CVisitor has AST Problems " ); //$NON-NLS-1$
			}
			
			// TODO: actually collect preprocessor problems
			if (tu.getPreprocessorProblems().length != 0) {
				throw new AssertionFailedError(" C TranslationUnit has Preprocessor Problems " ); //$NON-NLS-1$
			}
        }

        // resolve all bindings
		if (checkBindings) {

			C99NameResolver res = new C99NameResolver();
	        tu.accept( res );
			if (res.numProblemBindings != expectedProblemBindings )
				throw new AssertionFailedError("Expected " + expectedProblemBindings + " problem(s), encountered " + res.numProblemBindings ); //$NON-NLS-1$ //$NON-NLS-2$
			
		}
		
		return tu;
	}

	
	public static IASTTranslationUnit commentParse(String code, BaseExtensibleLanguage language) {
		CodeReader codeReader = new CodeReader(code.toCharArray());
		IASTTranslationUnit tu;
		try {
			tu = language.getASTTranslationUnit(codeReader, new ScannerInfo(), null, null, AbstractLanguage.OPTION_ADD_COMMENTS, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}
		return tu;
	}
	
	public static IASTCompletionNode getCompletionNode(String code, BaseExtensibleLanguage lang) {
		return getCompletionNode(code, lang, code.length());
	}
	
	
	public static IASTCompletionNode getCompletionNode(String code, BaseExtensibleLanguage language, int offset) {
		CodeReader reader = new CodeReader(code.toCharArray());
		return language.getCompletionNode(reader, new ScannerInfo(), null, null, ParserUtil.getParserLogService(), offset);
	}

}
