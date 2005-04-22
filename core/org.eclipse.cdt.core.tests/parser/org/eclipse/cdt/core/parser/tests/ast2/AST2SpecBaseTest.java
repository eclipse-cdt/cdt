/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;

import junit.framework.TestCase;

/**
 * @author dsteffle
 */
public class AST2SpecBaseTest extends TestCase {
    private static final IParserLogService NULL_LOG = new NullLogService();

	/**
	 * checkSemantics is used to specify whether the example should have semantics checked
	 * since several spec examples have syntactically correct code ONLY this flag was added
	 * so that future tests can ensure that examples are checked against syntax/semantics where necessary
	 * @param code
	 * @param expectedProblemBindings the number of problem bindings you expect to encounter
	 * @throws ParserException
	 */
	protected void parseCandCPP( String code, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		parse( code, ParserLanguage.C, false, true, checkBindings, expectedProblemBindings);
		parse( code, ParserLanguage.CPP, false, true, checkBindings, expectedProblemBindings );
	}
		
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
    	return parse(code, lang, false, true, checkBindings, expectedProblemBindings );
    }
    
    private IASTTranslationUnit parse( String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		// TODO beef this up with tests... i.e. run once with \n, and then run again with \r\n replacing \n ... etc
		// TODO another example might be to replace all characters with corresponding trigraph/digraph tests...
		
		CodeReader codeReader = new CodeReader(code.toCharArray());
		return parse(codeReader, lang, useGNUExtensions, expectNoProblems, checkBindings, expectedProblemBindings);
    }
	
//	private IASTTranslationUnit parse( IFile filename, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) throws ParserException {
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
	
	private IASTTranslationUnit parse(CodeReader codeReader, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, boolean checkBindings, int expectedProblemBindings) throws ParserException {
        ScannerInfo scannerInfo = new ScannerInfo();
        IScannerExtensionConfiguration configuration = null;
        if( lang == ParserLanguage.C )
            configuration = new GCCScannerExtensionConfiguration();
        else
            configuration = new GPPScannerExtensionConfiguration();
        IScanner scanner = new DOMScanner( codeReader, scannerInfo, ParserMode.COMPLETE_PARSE, lang, NULL_LOG, configuration, FileCodeReaderFactory.getInstance() );
        
        ISourceCodeParser parser2 = null;
        if( lang == ParserLanguage.CPP )
        {
            ICPPParserExtensionConfiguration config = null;
            if (useGNUExtensions)
            	config = new GPPParserExtensionConfiguration();
            else
            	config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE,
                NULL_LOG,
                config );
        }
        else
        {
            ICParserExtensionConfiguration config = null;

            if (useGNUExtensions)
            	config = new GCCParserExtensionConfiguration();
            else
            	config = new ANSICParserExtensionConfiguration();
            
            parser2 = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, 
                NULL_LOG, config );
        }
        
        IASTTranslationUnit tu = parser2.parse();
		
		// resolve all bindings
		if (checkBindings) {
			if ( lang == ParserLanguage.CPP ) {
				CPPNameResolver res = new CPPNameResolver();
		        tu.accept( res );
				if (res.numProblemBindings != expectedProblemBindings )
					throw new ParserException("Expected " + expectedProblemBindings + " problems, encountered " + res.numProblemBindings ); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (lang == ParserLanguage.C ) {
				CNameResolver res = new CNameResolver();
		        tu.accept( res );
				if (res.numProblemBindings != expectedProblemBindings )
					throw new ParserException("Expected " + expectedProblemBindings + " problems, encountered " + res.numProblemBindings ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

        if( parser2.encounteredError() && expectNoProblems )
            throw new ParserException( "FAILURE"); //$NON-NLS-1$
         
        if( lang == ParserLanguage.C && expectNoProblems )
        {
			if (CVisitor.getProblems(tu).length != 0) {
				throw new ParserException (" CVisitor has AST Problems " ); //$NON-NLS-1$
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new ParserException (" C TranslationUnit has Preprocessor Problems " ); //$NON-NLS-1$
			}
        }
        else if ( lang == ParserLanguage.CPP && expectNoProblems )
        {
			if (CPPVisitor.getProblems(tu).length != 0) {
				throw new ParserException (" CPPVisitor has AST Problems " ); //$NON-NLS-1$
			}
			if (tu.getPreprocessorProblems().length != 0) {
				throw new ParserException (" CPP TranslationUnit has Preprocessor Problems " ); //$NON-NLS-1$
			}
        }
        
        return tu;
	}
	
	static protected class CNameResolver extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public List nameList = new ArrayList();
		public int visit( IASTName name ){
			nameList.add( name );
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName( int idx ){
			if( idx < 0 || idx >= nameList.size() )
				return null;
			return (IASTName) nameList.get( idx );
		}
		public int size() { return nameList.size(); } 
	}
	
	static protected class CPPNameResolver extends CPPASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public List nameList = new ArrayList();
		public int visit( IASTName name ){
			nameList.add( name );
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName( int idx ){
			if( idx < 0 || idx >= nameList.size() )
				return null;
			return (IASTName) nameList.get( idx );
		}
		public int size() { return nameList.size(); } 
	}
	
}
