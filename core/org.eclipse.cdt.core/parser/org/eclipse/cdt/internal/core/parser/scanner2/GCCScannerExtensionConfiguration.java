/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner.ExpressionEvaluator;

/**
 * @author jcamelon
 */
public class GCCScannerExtensionConfiguration extends GNUScannerExtensionConfiguration implements IScannerExtensionConfiguration {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#supportMinAndMaxOperators()
     */
    public boolean supportMinAndMaxOperators() {
        return false;
    }
    
	private static final FunctionStyleMacro _Pragma = new FunctionStyleMacro( 
			"_Pragma".toCharArray(),  //$NON-NLS-1$
			emptyCharArray, 
			new char[][] { "arg".toCharArray() } ); //$NON-NLS-1$

	private final DynamicFunctionStyleMacro __builtin_choose_expr = 
		new DynamicFunctionStyleMacro( "__builtin_choose_expr".toCharArray(),  //$NON-NLS-1$
		        					   new char[][] { "const_exp".toCharArray(), "exp1".toCharArray(), "exp2".toCharArray() } ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
	    	public char [] execute( CharArrayObjectMap argmap ){
                ExpressionEvaluator evaluator = new ExpressionEvaluator();
                char[] const_exp = (char[]) argmap.get( arglist[0] );
                long exp = 0;
                if( const_exp != null)
                    exp = evaluator.evaluate( const_exp, 0, const_exp.length, CharArrayObjectMap.EMPTY_MAP );
                
                if( exp != 0 )
                    return (char[])argmap.get( arglist[1] );
                return (char[])argmap.get( arglist[2] );
            } 
		};


    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalMacros()
     */
    public CharArrayObjectMap getAdditionalMacros() {
        CharArrayObjectMap result = super.getAdditionalMacros();
        result.put(_Pragma.name, _Pragma );
        result.put( __builtin_choose_expr.name, __builtin_choose_expr );
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerConfiguration#getAdditionalKeywords()
     */
    public CharArrayIntMap getAdditionalKeywords() {
        CharArrayIntMap result = new CharArrayIntMap( 4, -1 );
		result.put( GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		result.put( GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );
        return result;
    }

}
