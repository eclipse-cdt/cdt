/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.core.parser.ast.gcc;

import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public interface IASTGCCSimpleTypeSpecifier extends IASTSimpleTypeSpecifier {
	
	public static class Type extends IASTSimpleTypeSpecifier.Type
	{
		public static final Type TYPEOF = new Type( LAST_TYPE + 1 );
		
		/**
		 * @param enumValue
		 */
		protected Type(int enumValue) {
			super(enumValue);
		}
		
	}
	
	public IASTExpression getTypeOfExpression();
}
