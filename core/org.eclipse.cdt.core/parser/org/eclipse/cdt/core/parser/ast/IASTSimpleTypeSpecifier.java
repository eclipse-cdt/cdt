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
	public static class SimpleType extends Enum 
	{
		public static final SimpleType UNSPECIFIED  = new SimpleType( 1 );
		public static final SimpleType CHAR         = new SimpleType( 1 );
		public static final SimpleType WCHAR_T      = new SimpleType( 2 );
		public static final SimpleType BOOL         = new SimpleType( 3 );
		public static final SimpleType INT          = new SimpleType( 4 );
		public static final SimpleType FLOAT        = new SimpleType( 5 );
		public static final SimpleType DOUBLE       = new SimpleType( 6 );
		public static final SimpleType VOID         = new SimpleType( 7 );
		public static final SimpleType TYPENAME     = new SimpleType( 8 );
		public static final SimpleType TEMPLATE     = new SimpleType( 9 );
		
        /**
         * @param enumValue
         */
        protected SimpleType(int enumValue)
        {
            super(enumValue);
        }
		
	
	}
	
	public SimpleType getType(); 
	public String     getTypename(); 
	public boolean    isLong(); 
	public boolean    isShort(); 
	public boolean    isSigned(); 
	public boolean    isUnsigned();
	public boolean    isTypename();  
}
