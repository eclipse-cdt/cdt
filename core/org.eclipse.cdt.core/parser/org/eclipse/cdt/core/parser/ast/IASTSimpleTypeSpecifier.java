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
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public interface IASTSimpleTypeSpecifier extends IASTTypeSpecifier
{
	public static class Type extends Enum 
	{
        public static final Type UNSPECIFIED  = new Type( 1 );
		public static final Type CHAR         = new Type( 1 );
		public static final Type WCHAR_T      = new Type( 2 );
		public static final Type BOOL         = new Type( 3 );
		public static final Type INT          = new Type( 4 );
		public static final Type FLOAT        = new Type( 5 );
		public static final Type DOUBLE       = new Type( 6 );
		public static final Type VOID         = new Type( 7 );
		public static final Type CLASS_OR_TYPENAME     = new Type( 8 );
		public static final Type _BOOL        = new Type( 10 );	
		
		protected static final int LAST_TYPE = 10;
        /**
         * @param enumValue
         */
        protected Type(int enumValue)
        {
            super(enumValue);
        }
		
	
	}
	
	public Type getType(); 
	public String     getTypename(); 
	public boolean    isLong(); 
	public boolean    isShort(); 
	public boolean    isSigned(); 
	public boolean    isUnsigned();
	public boolean    isTypename();
	public boolean    isComplex(); 
	public boolean    isImaginary();
	
	public IASTTypeSpecifier getTypeSpecifier() throws ASTNotImplementedException;
	/**
	 * @param referenceManager
	 */
	public void releaseReferences();   
}
