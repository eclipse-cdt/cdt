/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.expression;

import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;

/**
 * @author jcamelon
 *
 */
public class ExpressionFactory {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	/**
	 * 
	 */
	public ExpressionFactory() {
		super();
	}

	/**
	 * @param kind
	 * @param lhs
	 * @param rhs
	 * @param thirdExpression
	 * @param typeId
	 * @param string
	 * @param literal
	 * @param newDescriptor
	 * @return
	 */
	public static IASTExpression createExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, String idExpression, String literal, IASTNewExpressionDescriptor newDescriptor) {
		if( !literal.equals( EMPTY_STRING ) && idExpression.equals( EMPTY_STRING )) //$NON-NLS-1$
			return new ASTLiteralExpression( kind, literal );
		
		if( !idExpression.equals( EMPTY_STRING ) && lhs == null )
			return new ASTIdExpression( kind, idExpression );
		
		if( thirdExpression != null )
			return new ASTConditionalExpression( kind, lhs, rhs, thirdExpression );
		
		if( newDescriptor != null  )
			return new ASTNewExpression( kind, newDescriptor, typeId );
		
		if( lhs != null && rhs != null )
			return new ASTBinaryExpression( kind, lhs, rhs );
		
		if( lhs != null && typeId != null )
			return new ASTUnaryTypeIdExpression( kind, lhs, typeId );
		
		if( lhs != null && !idExpression.equals( EMPTY_STRING ) )
			return new ASTUnaryIdExpression( kind, lhs, idExpression );
		
		if( lhs != null )
			return new ASTUnaryExpression( kind, lhs );
		
		if( typeId != null )
			return new ASTTypeIdExpression( kind, typeId );
		
		return new ASTEmptyExpression( kind );

	}
}
