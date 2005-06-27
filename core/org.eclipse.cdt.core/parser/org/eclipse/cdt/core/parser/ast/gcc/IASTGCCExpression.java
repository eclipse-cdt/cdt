/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.ast.gcc;

import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public interface IASTGCCExpression extends IASTExpression {
	
	public static class Kind extends IASTExpression.Kind
	{
		public static final Kind UNARY_ALIGNOF_UNARYEXPRESSION = new Kind( LAST_KIND + 1 );
		public static final Kind UNARY_ALIGNOF_TYPEID          = new Kind( LAST_KIND + 2 );
		public static final Kind UNARY_TYPEOF_UNARYEXPRESSION  = new Kind( LAST_KIND + 3 );
		public static final Kind UNARY_TYPEOF_TYPEID           = new Kind( LAST_KIND + 4 );
		public static final Kind RELATIONAL_MAX 			   = new Kind( LAST_KIND + 5 );
		public static final Kind RELATIONAL_MIN				   = new Kind( LAST_KIND + 6 );
		public static final Kind STATEMENT_EXPRESSION 		   = new Kind( LAST_KIND + 7 );

		protected Kind( int kind )
		{
			super( kind );
		}
	}
	
}
