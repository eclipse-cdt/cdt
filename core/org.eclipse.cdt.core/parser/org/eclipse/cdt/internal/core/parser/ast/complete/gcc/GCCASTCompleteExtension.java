/**********************************************************************
 * Copyright (c) 2004 IBM - Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

/*
 * Created on Jun 8, 2004
 */
package org.eclipse.cdt.internal.core.parser.ast.complete.gcc;

import java.util.List;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.internal.core.parser.ast.GCCASTExtension;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTBinaryExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTypeIdExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTUnaryExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ExpressionFactory;

/**
 * @author aniefer
 */
public class GCCASTCompleteExtension extends GCCASTExtension {

	/**
	 * @param mode
	 */
	public GCCASTCompleteExtension(ParserMode mode) {
		super(mode);
	}

	public boolean overrideCreateExpressionMethod() {
		if( mode == ParserMode.STRUCTURAL_PARSE || mode == ParserMode.COMPLETE_PARSE )
			return true;
		return false;
	}
	
	protected IASTExpression createExpression(IASTExpression.Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor, List references) {			
		if( lhs != null && rhs != null && 
		   (kind == IASTGCCExpression.Kind.RELATIONAL_MAX || 
			kind == IASTGCCExpression.Kind.RELATIONAL_MIN ) )
		{
			return new ASTBinaryExpression( kind, references, lhs, rhs ){
				public String toString(){
					IASTExpression.Kind k = getExpressionKind();
					StringBuffer buffer = new StringBuffer();
					buffer.append( ASTUtil.getExpressionString( getLHSExpression() ) );
					if( k == IASTGCCExpression.Kind.RELATIONAL_MAX )
						buffer.append( " >? " ); //$NON-NLS-1$
					else 
						buffer.append( " <? " ); //$NON-NLS-1$
					buffer.append( ASTUtil.getExpressionString( getRHSExpression() ) );
					return buffer.toString();
				}
			};
		}
		else if( lhs != null &&
				(kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION ||
				 kind == IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION) )
		{
			return new ASTUnaryExpression( kind, references, lhs ){
				public String toString(){
					IASTExpression.Kind k = getExpressionKind();
					StringBuffer buffer = new StringBuffer();
					if( k == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION )
						buffer.append( GCCKeywords.__ALIGNOF__ );
					else
						buffer.append( GCCKeywords.TYPEOF );
					buffer.append( ' ' );
					buffer.append( ASTUtil.getExpressionString( getLHSExpression() ) );
					return buffer.toString();
				}
			};
		}
		else if( typeId != null && lhs == null &&
				(kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID ||
				 kind == IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID) )
		{
			return new ASTTypeIdExpression( kind, references, typeId ){
				public String toString(){
					IASTExpression.Kind k = getExpressionKind();
					StringBuffer buffer = new StringBuffer();
					if( k == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID )
						buffer.append( GCCKeywords.__ALIGNOF__ );
					else
						buffer.append( GCCKeywords.TYPEOF );
					buffer.append( '(' );
					buffer.append( ASTUtil.getTypeId( getTypeId() ) );
					buffer.append( ')' );
					return buffer.toString();
				}
			};
		}
		
		return ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#createExpression(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTTypeId, org.eclipse.cdt.core.parser.ITokenDuple, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor, java.util.List)
	 */
	public IASTExpression createExpression(IASTScope scope, Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor, List references) {
		if( canHandleExpressionKind( kind ) )
			return createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
		
		return ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
	}
}
