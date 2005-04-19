/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner2;

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
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest;
import org.eclipse.cdt.core.parser.tests.CompleteParsePluginTest;
import org.eclipse.cdt.core.parser.tests.FileBasePluginTest;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

/**
 * @author jcamelon
 *
 */
public class IncludeTest extends FileBasePluginTest {

	private static final String [] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * @param name
	 * @param className
	 */
	public IncludeTest(String name) {
		super(name, IncludeTest.class);
	}

    public static Test suite() {
        TestSuite suite = new TestSuite( IncludeTest.class );
        suite.addTest( new CompleteParsePluginTest("cleanupProject") );    //$NON-NLS-1$
	    return suite;
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

    
    public void testIncludeNext() throws Exception
	{    	
    	String baseFile = "int zero; \n#include \"foo.h\""; //$NON-NLS-1$
    	String i1Next = "int one; \n#include_next <foo.h>"; //$NON-NLS-1$
    	String i2Next = "int two; \n#include_next \"foo.h\""; //$NON-NLS-1$
    	String i3Next = "int three; \n"; //$NON-NLS-1$
    	
    	
    	IFile base = importFile( "base.cpp", baseFile ); //$NON-NLS-1$
    	importFile( "foo.h", i1Next ); //$NON-NLS-1$
    	IFolder twof = importFolder("two"); //$NON-NLS-1$
    	IFolder threef = importFolder("three"); //$NON-NLS-1$
    	importFile( "two/foo.h", i2Next ); //$NON-NLS-1$
    	importFile( "three/foo.h", i3Next ); //$NON-NLS-1$
    	
    	String [] path = new String[2];
    	path[0] = twof.getFullPath().toOSString();
    	path[1] = threef.getFullPath().toOSString();
    	
    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, EMPTY_STRING_ARRAY, path );
    	Iterator i = parse( base, ParserLanguage.C, scannerInfo ).getDeclarations();
    	IASTVariable v;
    	
    	assertTrue( i.hasNext() );
    	v = (IASTVariable) i.next(); 	
    	assertEquals( v.getName(), "zero" ); //$NON-NLS-1$
    
    	assertTrue( i.hasNext() );
    	v = (IASTVariable) i.next(); 	
    	assertEquals( v.getName(), "one" ); //$NON-NLS-1$
    	
    	assertTrue( i.hasNext() );
    	v = (IASTVariable) i.next(); 	
    	assertEquals( v.getName(), "two" ); //$NON-NLS-1$
    	
    	assertTrue( i.hasNext() );
    	v = (IASTVariable) i.next(); 	
    	assertEquals( v.getName(), "three" ); //$NON-NLS-1$
    	
    	assertFalse( i.hasNext() );  	
	}
    
    public void testIncludePathOrdering() throws Exception
	{    	
    	// create directory structure:
    	//  project/base.cpp
    	//  project/foo.h
    	//  project/two/foo.h
    	//  project/three/foo.h
    	
    	// this test sets the include path to be two;three and include foo.h (we should see the contents of two/foo.h
    	// then we change to three;two and we should see the contents of three/foo.h.
    	
    	String baseFile = "#include <foo.h>"; //$NON-NLS-1$
    	String i1Next = "int one;\n"; //$NON-NLS-1$
    	String i2Next = "int two;\n"; //$NON-NLS-1$
    	String i3Next = "int three;\n"; //$NON-NLS-1$   	
    	
    	IFile base = importFile( "base.cpp", baseFile ); //$NON-NLS-1$
    	importFile( "foo.h", i1Next ); //$NON-NLS-1$
    	IFolder twof = importFolder("two"); //$NON-NLS-1$
    	IFolder threef = importFolder("three"); //$NON-NLS-1$
    	importFile( "two/foo.h", i2Next ); //$NON-NLS-1$
    	importFile( "three/foo.h", i3Next ); //$NON-NLS-1$
    	
    	String [] path = new String[2];
    	path[0] = twof.getFullPath().toOSString();
       	path[1] = threef.getFullPath().toOSString();
   	
    	IScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, EMPTY_STRING_ARRAY, path );
    	Iterator i = parse( base, ParserLanguage.C, scannerInfo ).getDeclarations();
    	IASTVariable v;
    	
    	assertTrue( i.hasNext() );
    	v = (IASTVariable) i.next(); 	
    	assertEquals( v.getName(), "two" ); //$NON-NLS-1$
    	assertFalse( i.hasNext() ); 
    	 	
    	path[0] = threef.getFullPath().toOSString();
       	path[1] = twof.getFullPath().toOSString();

    	scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, path, EMPTY_STRING_ARRAY, path );
    	i = parse( base, ParserLanguage.C, scannerInfo ).getDeclarations();
    	
    	assertTrue( i.hasNext() );
    	v = (IASTVariable) i.next(); 	
    	assertEquals( v.getName(), "three" ); //$NON-NLS-1$
    	assertFalse( i.hasNext() ); 
	}
    
    public void testBug91086() throws Exception {
        IFile inclusion = importFile( "file.h", "#define FOUND 666\n" ); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuffer buffer = new StringBuffer( "#include \"" ); //$NON-NLS-1$
        buffer.append( inclusion.getLocation().toOSString() );
        buffer.append( "\"\n"); //$NON-NLS-1$
        buffer.append( "int var = FOUND;\n"); //$NON-NLS-1$
        IFile code = importFile( "code.c", buffer.toString() ); //$NON-NLS-1$
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            Iterator i = parse( code, p, new ScannerInfo() ).getDeclarations();
            IASTVariable var = (IASTVariable) i.next();
            assertEquals( var.getInitializerClause().getAssigmentExpression().getLiteralString(), "666" ); //$NON-NLS-1$
            assertFalse( i.hasNext() );
        }
        
        
    }
}
