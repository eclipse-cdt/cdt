/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner2;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author jcamelon
 *
 */
public class GCCScannerExtensionsTest extends BaseScanner2Test {

	/**
	 * @param x
	 */
	public GCCScannerExtensionsTest(String x) {
		super(x);
	}
	
    public void testBug39698() throws Exception
	{
    	initializeScanner( "<? >?"); //$NON-NLS-1$
    	validateToken( IGCCToken.tMIN );
    	validateToken( IGCCToken.tMAX );
    	validateEOF();
	}

    public void test__attribute__() throws Exception {
    	initializeScanner(
    			"#define __cdecl __attribute__((cdecl))\n" + //$NON-NLS-1$
				"__cdecl;"); //$NON-NLS-1$
    	validateToken(IGCCToken.t__attribute__);
    	validateToken(IToken.tLPAREN);
    	validateToken(IToken.tLPAREN);
    	validateToken(IToken.tIDENTIFIER);
    	validateToken(IToken.tRPAREN);
    	validateToken(IToken.tRPAREN);
    	validateToken(IToken.tSEMI);
    	validateEOF();
	}
    
    public void testBug73954B() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "#define foo(x)                                            \\\n"); //$NON-NLS-1$
        writer.write( "  __builtin_choose_expr(                                  \\\n"); //$NON-NLS-1$
        writer.write( "     __builtin_types_compatible_p( typeof(x), double ),   \\\n"); //$NON-NLS-1$
        writer.write( "     foo_double( x ), (void)0 )                             \n"); //$NON-NLS-1$
        writer.write( "__builtin_constant_p(1)                                     \n"); //$NON-NLS-1$
        writer.write( "foo( 1 )                                                    \n"); //$NON-NLS-1$
        
        initializeScanner( writer.toString(), ParserLanguage.C );
        validateInteger( "0" ); //$NON-NLS-1$
        validateToken( IToken.tLPAREN );
        validateToken( IToken.t_void );
        validateToken( IToken.tRPAREN );
        validateInteger( "0" ); //$NON-NLS-1$
    }
    
    public void testImaginary() throws Exception
    {
        initializeScanner( "3i", ParserLanguage.C ); //$NON-NLS-1$
        validateInteger( "3i" ); //$NON-NLS-1$
        validateEOF();
    }
}
