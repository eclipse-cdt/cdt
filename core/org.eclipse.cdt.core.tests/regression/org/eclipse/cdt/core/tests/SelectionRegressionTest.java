/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 1, 2004
 */
package org.eclipse.cdt.core.tests;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.IParser.ISelectionParseResult;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest.FullParseCallback;
import org.eclipse.core.resources.IFile;

/**
 * @author aniefer
 */
public class SelectionRegressionTest extends BaseTestFramework {

    public SelectionRegressionTest()
    {
        super();
    }
    /**
     * @param name
     */
    public SelectionRegressionTest(String name)
    {
        super(name);
    }
    
    public static Test suite(){
        return suite( true );
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("SelectionRegressionTests"); //$NON-NLS-1$
        suite.addTest( new SelectionRegressionTest("testSimpleOpenDeclaration") ); //$NON-NLS-1$
                
        if( cleanup )
            suite.addTest( new SelectionRegressionTest( "cleanupProject" ) ); //$NON-NLS-1$
        
	    return suite;
    }
    
    protected IASTNode getSelection(IFile code, int startOffset, int endOffset) throws Exception {
		return getSelection( code, startOffset, endOffset, ParserLanguage.CPP );
	}

	/**
	 * @param code
	 * @param offset1
	 * @param offset2
	 * @param b
	 * @return
	 */
	protected IASTNode getSelection(IFile file, int startOffset, int endOffset, ParserLanguage language ) throws Exception {
	    FullParseCallback callback = new FullParseCallback();
		IParser parser = ParserFactory.createParser(
							ParserFactory.createScanner(
									new CodeReader( file.getLocation().toOSString(), file.getContents() ),
									new ScannerInfo(),
									ParserMode.SELECTION_PARSE,
									ParserLanguage.CPP,
									callback,
									new NullLogService(), null),
							callback,
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							ParserFactory.createDefaultLogService()
						);
		
		ISelectionParseResult result = parser.parse( startOffset, endOffset );

		return (IASTNode) ( (result != null) ? result.getOffsetableNamedElement() : null );
	}
	
	protected void assertNodeLocation( IASTNode result, IFile file, int offset ) throws Exception {
	    if( result != null && result instanceof IASTOffsetableNamedElement ){
	        IASTOffsetableNamedElement el = (IASTOffsetableNamedElement) result;
	        assertEquals( new String( el.getFilename() ), file.getLocation().toOSString() );
	        assertEquals( el.getNameOffset(), offset );
	        return;
	    }
	    fail("Node not found at " + file.getLocation().toOSString() + " line " + offset );  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public void testSimpleOpenDeclaration() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("class A{};           \n"); //$NON-NLS-1$
	    String h = writer.toString();
	    IFile header = importFile( "a.h", h ); //$NON-NLS-1$
	    
	    writer = new StringWriter();
	    writer.write( "#include \"a.h\"       \n"); //$NON-NLS-1$
	    writer.write( "void f(){              \n"); //$NON-NLS-1$
	    writer.write( "   A a;                \n"); //$NON-NLS-1$
	    writer.write( "}                      \n"); //$NON-NLS-1$
	    String source = writer.toString();
	    IFile cpp = importFile( "a.cpp", source ); //$NON-NLS-1$
	    
	    int start = source.indexOf( "A" ); //$NON-NLS-1$
	    IASTNode node = getSelection( cpp, start, start + 1 );
	    assertNodeLocation( node, header, h.indexOf("A") ); //$NON-NLS-1$
	}
}
