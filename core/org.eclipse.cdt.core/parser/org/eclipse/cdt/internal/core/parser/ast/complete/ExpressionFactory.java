/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;

/**
 * @author jcamelon
 *
 */
public class ExpressionFactory {
	
	public static ASTExpression createExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor, List references )
	{
		if( !literal.equals( "") && idExpression == null ) //$NON-NLS-1$
			return new ASTLiteralExpression( kind, references, literal );
		
		if( idExpression != null && lhs == null )
			return new ASTIdExpression( kind, references, idExpression );
		
		if( thirdExpression != null )
			return new ASTConditionalExpression( kind, references, lhs, rhs, thirdExpression );
		
		if( newDescriptor != null  )
			return new ASTNewExpression( kind, references, newDescriptor, typeId );
		
		if( lhs != null && rhs != null )
			return new ASTBinaryExpression( kind, references, lhs, rhs );
		
		if( lhs != null && typeId != null )
			return new ASTUnaryTypeIdExpression( kind, references, lhs, typeId );
		
		if( lhs != null && idExpression != null )
			return new ASTUnaryIdExpression( kind, references, lhs, idExpression );
		
		if( lhs != null )
			return new ASTUnaryExpression( kind, references, lhs );
		
		if( typeId != null )
			return new ASTTypeIdExpression( kind, references, typeId );
		
		return new ASTEmptyExpression( kind, references );
	}
}
