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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.internal.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;

/**
 * @author jcamelon
 *
 */
public class PreprocessorConditionalTest extends BaseScannerTest
{

	private ISourceElementRequestor nullSourceElementRequestor = new NullSourceElementRequestor();


    protected void initializeScanner(String input, Map definitions )
	{
		scanner= ParserFactory.createScanner( new StringReader(input),"TEXT", new ScannerInfo( definitions, null), ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, nullSourceElementRequestor );
	}


	protected void evaluateConditionalsPositive( String conditional, Map definitions )
	{
		 
		StringBuffer buff = new StringBuffer(); 
		buff.append( "#if " );
		buff.append( conditional ); 
		buff.append( "\n int x;\n#else\n#error NEVER\n#endif\n");
		initializeScanner( buff.toString(), definitions );
		evaluate(); 
	}

	protected void evaluateConditionalsNegative( String conditional, Map definitions )
	{
		
		StringBuffer buff = new StringBuffer(); 
		buff.append( "#if " );
		buff.append( conditional ); 
		buff.append( "\n#error NEVER\n#else\n int x;\n#endif\n");
		initializeScanner( buff.toString(), definitions ); 
		evaluate(); 
	}

    /**
     * 
     */
    private void evaluate()
    {
        try
        {
        	validateToken( IToken.t_int );
        	validateIdentifier( "x");
        	validateToken( IToken.tSEMI );
        	scanner.nextToken();
        	fail( "Should have hit EOF by now"); 	
        }
        catch( ScannerException se )
        {
        	fail( "Got #error, should not have gotten that.");
        }
        catch( EndOfFile eof )
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
    
    public void testConditionals()
    {
    	Map definitions = new HashMap();
    	definitions.put( "DEFED", "" );
    	definitions.put( "VALUE", "30 ");
    	
    	evaluateConditionalsPositive( "defined( DEFED )", definitions );
    	evaluateConditionalsNegative( "defined( NOTDEFED )", definitions );

		evaluateConditionalsNegative( "! defined( DEFED )", definitions );
		evaluateConditionalsPositive( "! defined( NOTDEFED )", definitions );
    	 
    	evaluateConditionalsPositive( "defined( VALUE ) && VALUE == 30", definitions );
		evaluateConditionalsNegative( "defined( VALUE ) && VALUE == 40", definitions );
		
		
    }
}
