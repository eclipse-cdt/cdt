/**********************************************************************
 * Copyright (c) 2002,2004 IBM Rational Software and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.extension.IScannerExtension;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;

/**
 * @author jcamelon
 */
public class GCCScannerExtension implements IScannerExtension {
	
	private static final String [] simpleIdentifiersDeclSpec;
	private static final String [] simpleIdentifiersAttribute;
	static
	{
		simpleIdentifiersDeclSpec = new String[ 1 ];
		simpleIdentifiersDeclSpec[0]= "x"; //$NON-NLS-1$
		
		simpleIdentifiersAttribute = new String[ 1 ];
		simpleIdentifiersAttribute[0] = "xyz"; //$NON-NLS-1$
	}

		
	private static final char [] emptyCharArray = "".toCharArray(); //$NON-NLS-1$
	// gcc built-ins
	private static final ObjectStyleMacro __inline__
		= new ObjectStyleMacro("__inline__".toCharArray(), "inline".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __extension__
		= new ObjectStyleMacro("__extension__".toCharArray(), emptyCharArray); //$NON-NLS-1$
	private static final ObjectStyleMacro __asm__
		= new ObjectStyleMacro("__asm__".toCharArray(), "asm".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __restrict__
		= new ObjectStyleMacro("__restrict__".toCharArray(), "restrict".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __restrict
		= new ObjectStyleMacro("__restrict".toCharArray(), "restrict".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __volatile__
		= new ObjectStyleMacro("__volatile__".toCharArray(), "volatile".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __const__
	= new ObjectStyleMacro("__const__".toCharArray(), "const".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __const
	= new ObjectStyleMacro("__const".toCharArray(), "const".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __signed__
	= new ObjectStyleMacro("__signed__".toCharArray(), "signed".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __cdecl = new
		ObjectStyleMacro( "__cdecl".toCharArray(), emptyCharArray ); //$NON-NLS-1$
	private static final ObjectStyleMacro __complex__ = 
		new ObjectStyleMacro( "__complex__".toCharArray(), "_Complex".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __real__ = 
		new ObjectStyleMacro( "__real__".toCharArray(), "(int)".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	private static final ObjectStyleMacro __imag__ = 
		new ObjectStyleMacro( "__imag__".toCharArray(), "(int)".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
	
	
	private static final FunctionStyleMacro __attribute__
		= new FunctionStyleMacro(
				"__attribute__".toCharArray(), //$NON-NLS-1$
				emptyCharArray,
				new char[][] { "arg".toCharArray() }); //$NON-NLS-1$
	private static final FunctionStyleMacro __declspec
	= new FunctionStyleMacro(
			"__declspec".toCharArray(), //$NON-NLS-1$
			emptyCharArray,
			new char[][] { "arg".toCharArray() }); //$NON-NLS-1$
	
	private static final FunctionStyleMacro _Pragma = new FunctionStyleMacro( 
			"_Pragma".toCharArray(),  //$NON-NLS-1$
			emptyCharArray, 
			new char[][] { "arg".toCharArray() } ); //$NON-NLS-1$

	private static final FunctionStyleMacro __builtin_va_arg 
		= new FunctionStyleMacro( 
		        "__builtin_va_arg".toCharArray(), //$NON-NLS-1$
		        "*(type *)ap".toCharArray(), //$NON-NLS-1$
		        new char[][] { "ap".toCharArray(), "type".toCharArray() } );  //$NON-NLS-1$//$NON-NLS-2$

	private static final FunctionStyleMacro __builtin_constant_p
		= new FunctionStyleMacro(
	        "__builtin_constant_p".toCharArray(), //$NON-NLS-1$
	        "0".toCharArray(), //$NON-NLS-1$
	        new char[][] {"exp".toCharArray()} );  //$NON-NLS-1$//$NON-NLS-2$
	
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
	 * @see org.eclipse.cdt.core.parser.IScannerExtension#setupBuiltInMacros()
	 */
	public void setupBuiltInMacros(IScannerData scannerData) {
		// gcc extensions
		CharArrayObjectMap realDefinitions = scannerData.getRealDefinitions();
        realDefinitions.put(__inline__.name, __inline__);
		realDefinitions.put(__cdecl.name, __cdecl );
		realDefinitions.put( __const__.name, __const__ );
		realDefinitions.put( __const.name, __const );
		realDefinitions.put(__extension__.name, __extension__);
		realDefinitions.put(__attribute__.name, __attribute__);
		realDefinitions.put( __declspec.name, __declspec );
		realDefinitions.put(__restrict__.name, __restrict__);
		realDefinitions.put(__restrict.name, __restrict);
		realDefinitions.put(__volatile__.name, __volatile__);
		realDefinitions.put(__signed__.name, __signed__ );
		realDefinitions.put(__complex__.name, __complex__ );
		realDefinitions.put(__imag__.name, __imag__ );
		realDefinitions.put(__real__.name, __real__ );
		realDefinitions.put( __builtin_va_arg.name, __builtin_va_arg );
		realDefinitions.put( __builtin_constant_p.name, __builtin_constant_p );
		if( scannerData.getLanguage() == ParserLanguage.CPP )
			realDefinitions.put(__asm__.name, __asm__);
		else{
			realDefinitions.put(_Pragma.name, _Pragma );
			realDefinitions.put( __builtin_choose_expr.name, __builtin_choose_expr );
		}		
	}
	
	private static final CharArrayIntMap additionalCPPKeywords;
	private static final CharArrayIntMap additionalCKeywords;
	private static final CharArrayIntMap additionalCPPOperators;
	private static final CharArrayIntMap additionalCOperators;
	private static final char [] MAX_OPERATOR = ">?".toCharArray(); //$NON-NLS-1$
	private static final char [] MIN_OPERATOR = "<?".toCharArray(); //$NON-NLS-1$
	
	static
	{
		additionalCKeywords = new CharArrayIntMap( 2, -1 );
		additionalCKeywords.put( GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		additionalCKeywords.put( GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );
		additionalCPPKeywords = new CharArrayIntMap( 4, -1 );
		additionalCPPKeywords.put( GCCKeywords.cp__ALIGNOF__, IGCCToken.t___alignof__ );
		additionalCPPKeywords.put( GCCKeywords.cpTYPEOF, IGCCToken.t_typeof );		
		additionalCPPKeywords.put( Keywords.cRESTRICT, IToken.t_restrict );
		additionalCPPKeywords.put( Keywords.c_COMPLEX, IToken.t__Complex );
		additionalCPPKeywords.put( Keywords.c_IMAGINARY, IToken.t__Imaginary );
		
		additionalCOperators = new CharArrayIntMap(2, -1);
		additionalCPPOperators = new CharArrayIntMap( 2, -1);
		additionalCPPOperators.put( MAX_OPERATOR, IGCCToken.tMAX );
		additionalCPPOperators.put( MIN_OPERATOR, IGCCToken.tMIN );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#isExtensionKeyword()
	 */
	public boolean isExtensionKeyword(ParserLanguage language, char[] tokenImage) {
		if( language == ParserLanguage.CPP )
			return ( additionalCPPKeywords.containsKey( tokenImage ) );
		else if( language == ParserLanguage.C )
			return ( additionalCKeywords.containsKey( tokenImage  ) );
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#createExtensionToken()
	 */
	public IToken createExtensionToken(IScannerData scannerData, char[] image) {
		int get = -1;
		if( scannerData.getLanguage() == ParserLanguage.CPP )
		{
			get = additionalCPPKeywords.get( image );
			if( get == -1 )
				get = additionalCPPOperators.get( image );
		}
		else if( scannerData.getLanguage()  == ParserLanguage.C )
		{
			get = additionalCKeywords.get( image );
			if( get == -1 )
				get = additionalCOperators.get( image );
		}
		if( get == -1 ) return null;
		int o = scannerData.getCurrentOffset() + 1;
		IToken i = new ImagedToken(get, image, o, scannerData.getCurrentFilename(), scannerData.getLineNumber( o ));
		return i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IScannerExtension#isExtensionOperator(java.lang.String)
	 */
	public boolean isExtensionOperator(ParserLanguage language, char[] query) {
		if( language == ParserLanguage.CPP )
			return ( additionalCPPOperators.containsKey( query ) );
		else if (language == ParserLanguage.C )
			return ( additionalCOperators.containsKey( query ));
		return false;
	}

}
