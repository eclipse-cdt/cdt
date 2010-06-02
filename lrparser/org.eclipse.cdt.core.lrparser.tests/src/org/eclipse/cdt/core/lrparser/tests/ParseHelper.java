/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2BaseTest;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Utility methods for parsing test code using the C99 LPG parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings({"restriction", "nls"})
public class ParseHelper {
	
	static int testsRun = 0;
	
	
	
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
			if(idx < 0 || idx >= nameList.size())
				return null;
			return nameList.get(idx);
		}
		
		public int size() { 
			return nameList.size(); 
		}
	}
	
	
	
	public static class Options {
		
		boolean checkSyntaxProblems = true;
		boolean checkPreprocessorProblems = true;
		boolean checkBindings = false;
		
		int expectedProblemBindings;
		String[] problems;
		boolean skipTrivialInitializers;
		
		public Options setCheckSyntaxProblems(boolean checkSyntaxProblems) {
			this.checkSyntaxProblems = checkSyntaxProblems;
			return this;
		}
		public Options setCheckBindings(boolean checkBindings) {
			this.checkBindings = checkBindings;
			return this;
		}
		public Options setCheckPreprocessorProblems(boolean checkPreprocessorProblems) {
			this.checkPreprocessorProblems = checkPreprocessorProblems;
			return this;
		}
		public Options setExpectedProblemBindings(int expectedProblemBindings) {
			this.expectedProblemBindings = expectedProblemBindings;
			return this;
		}
		public Options setProblems(String[] problems) {
			this.problems = problems;
			setExpectedProblemBindings(problems.length);
			setCheckBindings(true);
			return this;
		}
		public Options setSkipTrivialInitializers(boolean skipTrivialInitializers) {
			this.skipTrivialInitializers = skipTrivialInitializers;
			return this;
		}
		
	}
	
	
	public static IASTTranslationUnit parse(String code, ILanguage lang, boolean expectNoProblems) {
		Options options = new Options().setCheckSyntaxProblems(expectNoProblems).setCheckPreprocessorProblems(expectNoProblems);
		return parse(code.toCharArray(), lang, options);
	}

	public static IASTTranslationUnit parse(String code, ILanguage lang, Options options) {
		return parse(code.toCharArray(), lang, options);
	}
	
	
	public static IASTTranslationUnit parse(char[] code, ILanguage lang, Options options) {
			
		return parse(FileContent.create(AST2BaseTest.TEST_CODE, code), lang, new ScannerInfo(), null, options);
	}
	

	/**
	 * TODO thats WAY too many parameters, need to use a parameter object, need to refactor the
	 * DOM parser test suite so that its a lot cleaner.
	 * @Deprecated
	 */
	public static IASTTranslationUnit parse(CodeReader codeReader, ILanguage language, IScannerInfo scanInfo, 
			                                ICodeReaderFactory fileCreator, Options options) {
		testsRun++;
		
		IASTTranslationUnit tu;
		try {
			int languageOptions = 0;
			if(options.skipTrivialInitializers)
				languageOptions |= ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
			
			tu = language.getASTTranslationUnit(codeReader, scanInfo, fileCreator, null, languageOptions, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}

		// should parse correctly first before we look at the bindings
        if(options.checkSyntaxProblems) {
        	
        	// this should work for C++ also, CVisitor.getProblems() and CPPVisitor.getProblems() are exactly the same code!
			if (CVisitor.getProblems(tu).length != 0) { 
				throw new AssertionFailedError(" CVisitor has AST Problems " ); 
			}
        }
        
        if(options.checkPreprocessorProblems) {
			if (tu.getPreprocessorProblems().length != 0) {
				throw new AssertionFailedError(language.getName() + " TranslationUnit has Preprocessor Problems " );
			}
        }

        // resolve all bindings
		if (options.checkBindings) {
			NameResolver res = new NameResolver();
	        tu.accept( res );
			if(res.problemBindings.size() != options.expectedProblemBindings)
				throw new AssertionFailedError("Expected " + options.expectedProblemBindings + " problem(s), encountered " + res.problemBindings.size());
			
			if(options.problems != null) {
				for(int i = 0; i < options.problems.length; i++) {
					String expected = options.problems[i];
					String actual = res.problemBindings.get(i);
					if(!expected.equals(actual))
						throw new AssertionFailedError(String.format("Problem binding not equal, expected: %s, got: %s", expected, actual));
				}
			}
		}
		
		return tu;
	}
	
	/**
	 * TODO thats WAY too many parameters, need to use a parameter object, need to refactor the
	 * DOM parser test suite so that its a lot cleaner.
	 */
	public static IASTTranslationUnit parse(FileContent fileContent, ILanguage language, IScannerInfo scanInfo, 
			IncludeFileContentProvider fileContentProvider, Options options) {
		testsRun++;
		
		IASTTranslationUnit tu;
		try {
			int languageOptions = 0;
			if(options.skipTrivialInitializers)
				languageOptions |= ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
			
			tu = language.getASTTranslationUnit(fileContent, scanInfo, fileContentProvider, null, languageOptions, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}

		// should parse correctly first before we look at the bindings
        if(options.checkSyntaxProblems) {
        	
        	// this should work for C++ also, CVisitor.getProblems() and CPPVisitor.getProblems() are exactly the same code!
			if (CVisitor.getProblems(tu).length != 0) { 
				throw new AssertionFailedError(" CVisitor has AST Problems " ); 
			}
        }
        
        if(options.checkPreprocessorProblems) {
			if (tu.getPreprocessorProblems().length != 0) {
				throw new AssertionFailedError(language.getName() + " TranslationUnit has Preprocessor Problems " );
			}
        }

        // resolve all bindings
		if (options.checkBindings) {
			NameResolver res = new NameResolver();
	        tu.accept( res );
			if(res.problemBindings.size() != options.expectedProblemBindings)
				throw new AssertionFailedError("Expected " + options.expectedProblemBindings + " problem(s), encountered " + res.problemBindings.size());
			
			if(options.problems != null) {
				for(int i = 0; i < options.problems.length; i++) {
					String expected = options.problems[i];
					String actual = res.problemBindings.get(i);
					if(!expected.equals(actual))
						throw new AssertionFailedError(String.format("Problem binding not equal, expected: %s, got: %s", expected, actual));
				}
			}
		}
		
		return tu;
	}

	
	public static IASTTranslationUnit commentParse(String code, ILanguage language) {
		
		IASTTranslationUnit tu;
		try {
			tu = language.getASTTranslationUnit(FileContent.create(AST2BaseTest.TEST_CODE, code.toCharArray()), new ScannerInfo(), null, null, ILanguage.OPTION_ADD_COMMENTS, ParserUtil.getParserLogService());
		} catch (CoreException e) {
			throw new AssertionFailedError(e.toString());
		}
		return tu;
	}
	
	public static IASTCompletionNode getCompletionNode(String code, ILanguage lang) {
		return getCompletionNode(code, lang, code.length());
	}
	
	
	public static IASTCompletionNode getCompletionNode(String code, ILanguage language, int offset) {
		
		try {
			return language.getCompletionNode(FileContent.create(AST2BaseTest.TEST_CODE, code.toCharArray()), new ScannerInfo(), null, null, ParserUtil.getParserLogService(), offset);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

}
