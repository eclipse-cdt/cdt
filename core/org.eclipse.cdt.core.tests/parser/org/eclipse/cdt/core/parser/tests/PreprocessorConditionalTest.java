/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.scanner2.BaseScanner2Test;

/**
 * @author jcamelon
 *
 */
public class PreprocessorConditionalTest extends BaseScanner2Test
{

	private ISourceElementRequestor nullSourceElementRequestor = new NullSourceElementRequestor();


    protected void initializeScanner(String input, Map definitions ) throws Exception
	{
		scanner= ParserFactory.createScanner( new CodeReader(input.toCharArray()), new ScannerInfo( definitions ), ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, nullSourceElementRequestor, null, null );
	}

	protected void evaluateConditionalsPositive( String conditional, Map definitions ) throws Exception
	{
		 
		StringBuffer buff = new StringBuffer(); 
		buff.append( "#if " ); //$NON-NLS-1$
		buff.append( conditional ); 
		buff.append( "\n int x;\n#else\n#error NEVER\n#endif\n"); //$NON-NLS-1$
		initializeScanner( buff.toString(), definitions );
		evaluate(); 
	}

	protected void evaluateConditionalsNegative( String conditional, Map definitions )throws Exception
	{
		
		StringBuffer buff = new StringBuffer(); 
		buff.append( "#if " ); //$NON-NLS-1$
		buff.append( conditional ); 
		buff.append( "\n#error NEVER\n#else\n int x;\n#endif\n"); //$NON-NLS-1$
		initializeScanner( buff.toString(), definitions ); 
		evaluate(); 
	}

    /**
     * 
     */
    private void evaluate() throws Exception
    {
        try
        {
        	validateToken( IToken.t_int );
        	validateIdentifier( "x"); //$NON-NLS-1$
        	validateToken( IToken.tSEMI );
        	scanner.nextToken();
        	fail( "Should have hit EOF by now"); 	 //$NON-NLS-1$
        }
        catch( EndOfFileException eof )
        {
        	// expected 
        }
    }


    /**
     * @param x
     */
    public PreprocessorConditionalTest(String x)
    {
        super(x);
    }
    
    public void testConditionals()throws Exception
    {
    	Map definitions = new HashMap();
    	definitions.put( "DEFED", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    	definitions.put( "VALUE", "30 "); //$NON-NLS-1$ //$NON-NLS-2$
    	
    	evaluateConditionalsPositive( "defined( DEFED )", definitions ); //$NON-NLS-1$
    	evaluateConditionalsNegative( "defined( NOTDEFED )", definitions ); //$NON-NLS-1$

		evaluateConditionalsNegative( "! defined( DEFED )", definitions ); //$NON-NLS-1$
		evaluateConditionalsPositive( "! defined( NOTDEFED )", definitions ); //$NON-NLS-1$
    	 
    	evaluateConditionalsPositive( "defined( VALUE ) && VALUE == 30", definitions ); //$NON-NLS-1$
		evaluateConditionalsNegative( "defined( VALUE ) && VALUE == 40", definitions ); //$NON-NLS-1$
		
		
    }
}
