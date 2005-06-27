/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;

/**
 * @author jcamelon
 *
 */
public class ExpressionFactory {
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
	public static IASTExpression createExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, char[] idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor) {
		if( literal.length != 0 && idExpression.length == 0 ) //$NON-NLS-1$
			return new ASTLiteralExpression( kind, literal );
		
		if( idExpression.length != 0 && lhs == null )
			return new ASTIdExpression( kind, idExpression );
		
		if( thirdExpression != null )
			return new ASTConditionalExpression( kind, lhs, rhs, thirdExpression );
		
		if( newDescriptor != null  )
			return new ASTNewExpression( kind, newDescriptor, typeId );
		
		if( lhs != null && rhs != null )
			return new ASTBinaryExpression( kind, lhs, rhs );
		
		if( lhs != null && typeId != null )
			return new ASTUnaryTypeIdExpression( kind, lhs, typeId );
		
		if( lhs != null && idExpression.length != 0 )
			return new ASTUnaryIdExpression( kind, lhs, idExpression );
		
		if( lhs != null )
			return new ASTUnaryExpression( kind, lhs );
		
		if( typeId != null )
			return new ASTTypeIdExpression( kind, typeId );
		
		return new ASTEmptyExpression( kind );

	}
}
