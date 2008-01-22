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

import java.util.Collections;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2SelectionParseTest;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

public class C99SelectionParseTest extends AST2SelectionParseTest {
	
	public C99SelectionParseTest() {}
	public C99SelectionParseTest(String name) { super(name); }

	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length) throws ParserException {
		if(lang == ParserLanguage.C)
			return parse(code, lang, false, false, offset, length);
		else
			return super.parse(code, lang, offset, length);
	}
	
	protected IASTNode parse(IFile file, ParserLanguage lang, int offset, int length) throws ParserException {
		if(lang == ParserLanguage.C) {
			IASTTranslationUnit tu = parse(file, lang, false, false);
			return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
		}
		else
			return super.parse(file, lang, offset, length);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, int offset, int length, boolean expectedToPass) throws ParserException {
		if(lang == ParserLanguage.C)
			return parse(code, lang, false, expectedToPass, offset, length);
		else
			return super.parse(code, lang, offset, length, expectedToPass);
	}
	
	protected IASTNode parse(String code, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems, int offset, int length) throws ParserException {
		if(lang == ParserLanguage.C) {
			IASTTranslationUnit tu = ParseHelper.parse(code, getLanguage(), useGNUExtensions, expectNoProblems, 0);
			return tu.selectNodeForLocation(tu.getFilePath(), offset, length);
		}
		else
			return super.parse(code, lang, useGNUExtensions, expectNoProblems, offset, length);
	}	
	
	protected IASTTranslationUnit parse( IFile file, ParserLanguage lang, IScannerInfo scanInfo, boolean useGNUExtensions, boolean expectNoProblems ) 
	    throws ParserException {
		
		if(lang != ParserLanguage.C)
			return super.parse(file, lang, useGNUExtensions, expectNoProblems);
		
		String fileName = file.getLocation().toOSString();
		ICodeReaderFactory fileCreator = SavedCodeReaderFactory.getInstance();
		CodeReader reader = fileCreator.createCodeReaderForTranslationUnit(fileName);
		return ParseHelper.parse(reader, getLanguage(), scanInfo, fileCreator, expectNoProblems, true, 0);
	}

	protected IASTTranslationUnit parse( IFile file, ParserLanguage lang, boolean useGNUExtensions, boolean expectNoProblems ) 
	    throws ParserException {
		return parse(file, lang, new ScannerInfo(), useGNUExtensions, expectNoProblems);
	}
	
	protected BaseExtensibleLanguage getLanguage() {
		return C99Language.getDefault();
	}
	
	
//	public void testBug193185_IncludeNext() throws Exception
//	{    	
//    	String baseFile = "int zero; \n#include \"foo.h\""; //$NON-NLS-1$
//    	String i1Next = "int one; \n#include_next <foo.h>"; //$NON-NLS-1$
//    	String i2Next = "int two; \n#include_next \"foo.h\""; //$NON-NLS-1$
//    	String i3Next = "int three; \n"; //$NON-NLS-1$
//    	
//    	
//    	IFile base = importFile( "base.c", baseFile ); //$NON-NLS-1$
//    	importFile( "foo.h", i1Next ); //$NON-NLS-1$
//    	IFolder twof = importFolder("two"); //$NON-NLS-1$
//    	IFolder threef = importFolder("three"); //$NON-NLS-1$
//    	importFile( "two/foo.h", i2Next ); //$NON-NLS-1$
//    	importFile( "three/foo.h", i3Next ); //$NON-NLS-1$
//    	
//    	String[] path = new String[] {
//    		twof.getRawLocation().toOSString(),
//    		threef.getRawLocation().toOSString()
//    	};
//    	
//    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, new String[0], path );
//    	
//    	IASTTranslationUnit tu = parse(base, ParserLanguage.C, scannerInfo, false, true);
//    	
//    	IASTDeclaration[] decls = tu.getDeclarations();
//    	assertEquals(4, decls.length);
//    	
//    	IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)decls[0];
//    	assertEquals("zero", declaration.getDeclarators()[0].getName().toString()); //$NON-NLS-1$
//    	
//    	declaration = (IASTSimpleDeclaration)decls[1];
//    	assertEquals("one", declaration.getDeclarators()[0].getName().toString()); //$NON-NLS-1$
//    	
//    	declaration = (IASTSimpleDeclaration)decls[2];
//    	assertEquals("two", declaration.getDeclarators()[0].getName().toString()); //$NON-NLS-1$
//    	
//    	declaration = (IASTSimpleDeclaration)decls[3];
//    	assertEquals("three", declaration.getDeclarators()[0].getName().toString()); //$NON-NLS-1$
//	}
//	
//	
//	public void testBug193366() throws Exception
//	{    	
//    	String baseFile = 
//    		"#define FOOH <foo.h> \n" +       //$NON-NLS-1$
//    		"#define bar blahblahblah \n" +   //$NON-NLS-1$
//    		"#include FOOH \n" +              //$NON-NLS-1$
//    		"#include <bar.h> \n";            //$NON-NLS-1$
//    	
//    	String fooFile = "int x; \n"; //$NON-NLS-1$
//    	String barFile = "int y; \n"; //$NON-NLS-1$
//    	
//    	
//    	IFile base = importFile( "base.c", baseFile ); //$NON-NLS-1$
//    	IFolder include = importFolder("inc"); //$NON-NLS-1$
//    	importFile( "inc/foo.h", fooFile ); //$NON-NLS-1$
//    	importFile( "inc/bar.h", barFile ); //$NON-NLS-1$
//    	
//    	String[] path = new String[] { include.getRawLocation().toOSString() };
//    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, new String[0], path );
//    	
//    	IASTTranslationUnit tu = parse(base, ParserLanguage.C, scannerInfo, false, true);
//    	
//    	IASTDeclaration[] decls = tu.getDeclarations();
//    	assertEquals(2, decls.length);
//    	
//    	IASTSimpleDeclaration declaration = (IASTSimpleDeclaration)decls[0];
//    	assertEquals("x", declaration.getDeclarators()[0].getName().toString()); //$NON-NLS-1$
//    	
//    	declaration = (IASTSimpleDeclaration)decls[1];
//    	assertEquals("y", declaration.getDeclarators()[0].getName().toString()); //$NON-NLS-1$
//	}
	
}
