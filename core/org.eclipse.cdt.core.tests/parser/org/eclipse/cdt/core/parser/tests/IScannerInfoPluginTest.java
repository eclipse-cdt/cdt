/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest.Scope;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;

/**
 * @author jcamelon
 *
 */
public class IScannerInfoPluginTest extends FileBasePluginTest {

	private static final String [] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * @param name
	 * @param className
	 */
	public IScannerInfoPluginTest(String name) {
		super(name, IScannerInfoPluginTest.class);
	}

    public static Test suite() {
        TestSuite suite = new TestSuite( IScannerInfoPluginTest.class );
        suite.addTest( new CompleteParsePluginTest("cleanupProject") );    //$NON-NLS-1$
	    return suite;
    }

    protected Iterator getDeclarations(IASTScope scope)
    {
    	Scope s = c.lookup( scope ); 
    	if( s != null )
    		return s.getDeclarations();
    	return null;
    }
    
    CompleteParseBaseTest.FullParseCallback c;
    
    protected IASTScope parse(IFile code, ParserLanguage language, IScannerInfo scannerInfo ) throws Exception
    {
    	c = new CompleteParseBaseTest.FullParseCallback();
    	InputStream stream = code.getContents();
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new CodeReader( code.getLocation().toOSString(), stream ), scannerInfo, //$NON-NLS-1$
    			ParserMode.COMPLETE_PARSE, language, c, new NullLogService(), null ), c, ParserMode.COMPLETE_PARSE, language, null 	
    		);
    	stream.close();
    	boolean parseResult = parser.parse();
    	// throw exception if there are generated IProblems
		if( !parseResult ) throw new ParserException( "FAILURE"); //$NON-NLS-1$
		assertTrue( ((Parser)parser).validateCaches());
        return c.getCompilationUnit();
    }

    
    public void testMacroFileLoading() throws Exception
	{
    	String imacroContent = "#define ONE 1\n"; //$NON-NLS-1$
    	IFile imacroFile = importFile( "imacros.h", imacroContent ); //$NON-NLS-1$
    	String code = "int x = ONE;\n"; //$NON-NLS-1$
    	IFile sourceCode = importFile( "source.cpp", code ); //$NON-NLS-1$
    	String [] imacroz = new String[1];
    	imacroz[0] = imacroFile.getFullPath().toOSString();
    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, imacroz, EMPTY_STRING_ARRAY );
    	Iterator i = parse( sourceCode, ParserLanguage.C, scannerInfo ).getDeclarations();
    	assertTrue( i.hasNext() );
    	IASTVariable x = (IASTVariable) i.next();
    	assertFalse( i.hasNext() );
    	assertEquals( x.getName(), "x" ); //$NON-NLS-1$
    	assertNotNull( x.getInitializerClause() );
    	assertNotNull( x.getInitializerClause().getAssigmentExpression() );
    	assertEquals( x.getInitializerClause().getAssigmentExpression().toString(), "1"); //$NON-NLS-1$
	}
    
    public void testIncludeFileLoading() throws Exception
	{
    	String inclContent = "int x = 4;\n\n"; //$NON-NLS-1$
    	IFile inclFile = importFile( "includeMe.h", inclContent ); //$NON-NLS-1$
    	String code = "int y = x;\n"; //$NON-NLS-1$
    	IFile sourceCode = importFile( "source.cpp", code ); //$NON-NLS-1$
    	String [] includez = new String[1];
    	includez[0] = inclFile.getFullPath().toOSString();
    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY, includez );
    	Iterator i = parse( sourceCode, ParserLanguage.C, scannerInfo ).getDeclarations();
    	assertTrue( i.hasNext() );
    	assertTrue(i.next() instanceof IASTVariable );
    	IASTVariable y = (IASTVariable) i.next();
    	assertFalse( i.hasNext() );
    	assertEquals( y.getName(), "y" ); //$NON-NLS-1$
    	assertNotNull( y.getInitializerClause() );
    	assertNotNull( y.getInitializerClause().getAssigmentExpression() );
    	assertEquals( y.getInitializerClause().getAssigmentExpression().toString(), "x"); //$NON-NLS-1$
	}

}
