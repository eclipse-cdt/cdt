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

import java.util.List;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 */
public interface IASTInitializerClause {

	public class Kind extends Enum  
	{
		public static final Kind ASSIGNMENT_EXPRESSION = new Kind( 1 );
		public static final Kind INITIALIZER_LIST      = new Kind( 2 );
		public static final Kind EMPTY                 = new Kind( 3 );

		/**
		 * @param enumValue
		 */
		protected Kind(int enumValue) {
			super(enumValue);
		}
	}
	
	public Kind getKind(); 
	public List getInitializerList(); 
	public IASTExpression getAssigmentExpression(); 
	
}
