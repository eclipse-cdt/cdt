/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTypeId;
import org.eclipse.cdt.internal.core.parser.ast.complete.gcc.ASTGCCSimpleTypeSpecifier;
import org.eclipse.cdt.internal.core.parser.ast.expression.gcc.ASTGCCExpression;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author jcamelon
 *
 */
public class GCCASTExtension implements IASTFactoryExtension {
	private final ParserMode mode;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final List EMPTY_LIST = new ArrayList( 0 );
	/**
	 * @param mode
	 */
	public GCCASTExtension(ParserMode mode) {
		this.mode = mode;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#overrideExpressionFactory()
	 */
	public boolean overrideCreateExpressionMethod() {
		if( mode == ParserMode.EXPRESSION_PARSE )
			return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#createExpression(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTTypeId, org.eclipse.cdt.core.parser.ITokenDuple, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor)
	 */
	public IASTExpression createExpression(IASTScope scope, Kind kind,
			IASTExpression lhs, IASTExpression rhs,
			IASTExpression thirdExpression, IASTTypeId typeId,
			ITokenDuple idExpression, String literal,
			IASTNewExpressionDescriptor newDescriptor)
	{
		return new ASTGCCExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression == null ? "" : idExpression.toString(), literal, newDescriptor );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#canHandleExpressionKind(org.eclipse.cdt.core.parser.ast.IASTExpression.Kind)
	 */
	public boolean canHandleExpressionKind(Kind kind) {
		if( kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID ||
			kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION ||
			kind == IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION ||
			kind == IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID )
			return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#getExpressionResultType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo, org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTTypeId, org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public TypeInfo getExpressionResultType(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTTypeId typeId) {
		TypeInfo info = null;
		if( kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID ||
				kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION )
		{
			info = new TypeInfo();
			info.setType(TypeInfo.t_int);
			info.setBit(true, TypeInfo.isUnsigned);
		}
		else if( kind == IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID )
		{
			if( typeId instanceof ASTTypeId )
				info = new TypeInfo( ((ASTTypeId)typeId).getTypeSymbol().getTypeInfo() );
		}
		else if ( kind == IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION )
		{
			if( lhs instanceof ASTExpression )
				info = new TypeInfo( ((ASTExpression)lhs).getResultType().getResult() );
		}
		
		if( info != null )
			return info;
		return new TypeInfo();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#overrideCreateSimpleTypeSpecifierMethod()
	 */
	public boolean overrideCreateSimpleTypeSpecifierMethod(Type type) {
		if( type == IASTGCCSimpleTypeSpecifier.Type.TYPEOF )
			return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean)
	 */
	public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(ParserSymbolTable pst, IASTScope scope, Type kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, boolean isComplex, boolean isImaginary, boolean isGlobal, Hashtable extensionParms) {
		if( kind == IASTGCCSimpleTypeSpecifier.Type.TYPEOF )
		{
			ASTExpression typeOfExpression = (ASTExpression) extensionParms.get( ASTGCCSimpleTypeSpecifier.TYPEOF_EXRESSION );
			ISymbol s = pst.newSymbol( EMPTY_STRING );
			s.setTypeInfo( typeOfExpression.getResultType().getResult() );
			return new ASTGCCSimpleTypeSpecifier( s, isTypename, ( typeName == null ? EMPTY_STRING : typeName.toString()), EMPTY_LIST, typeOfExpression );
		}
		return null;
	}
	
	
}
