/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

/*
 * Created on Jun 8, 2004
 */
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.List;

import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.internal.core.parser.ast.GCCASTExtension;

/**
 * @author aniefer
 */
public class GCCASTExpressionExtension extends GCCASTExtension {
	/**
	 * @param mode
	 */
	public GCCASTExpressionExtension(ParserMode mode) {
		super(mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#overrideExpressionFactory()
	 */
	public boolean overrideCreateExpressionMethod() {
		return false;
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
	private static IASTExpression createExpression(IASTExpression.Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, char[] idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor) {			
		if( idExpression.length != 0 && literal.length == 0 )
			return new ASTIdExpression( kind, idExpression )
			{
				public long evaluateExpression() throws ASTExpressionEvaluationException {
					if( getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION )
						return 0;
					return super.evaluateExpression();
				}
			};
		else if( lhs != null && rhs != null && 
				(kind == IASTGCCExpression.Kind.RELATIONAL_MAX || 
				 kind == IASTGCCExpression.Kind.RELATIONAL_MIN ) )
		{
			return new ASTBinaryExpression( kind, lhs, rhs ){
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
			return new ASTUnaryExpression( kind, lhs ){
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
			return new ASTTypeIdExpression( kind, typeId ){
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
		
		return ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#createExpression(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTTypeId, org.eclipse.cdt.core.parser.ITokenDuple, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor, java.util.List)
	 */
	public IASTExpression createExpression(IASTScope scope, Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor, List references) {
		return createExpression( kind, lhs, rhs, thirdExpression, typeId, (idExpression == null ) ? EMPTY_STRING : idExpression.toCharArray(), literal, newDescriptor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#initialize(org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable)
	 */
	public void initialize(IASTFactory factory, IASTCompilationUnit compilationUnit) {
		// TODO Auto-generated method stub
		
	}
}
