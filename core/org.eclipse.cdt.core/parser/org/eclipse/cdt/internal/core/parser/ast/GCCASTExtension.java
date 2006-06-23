/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.ast;

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCDesignator;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTTypeId;
import org.eclipse.cdt.internal.core.parser.ast.complete.gcc.ASTGCCSimpleTypeSpecifier;
import org.eclipse.cdt.internal.core.parser.ast.complete.gcc.GCCASTCompleteExtension;
import org.eclipse.cdt.internal.core.parser.ast.gcc.ASTGCCDesignator;
import org.eclipse.cdt.internal.core.parser.ast.quick.GCCASTExpressionExtension;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfoProvider;

/**
 * @author jcamelon
 *
 */
public abstract class GCCASTExtension implements IASTFactoryExtension {
	protected final ParserMode mode;
	protected static final char[] EMPTY_STRING = "".toCharArray(); //$NON-NLS-1$
	/**
	 * @param mode
	 */
	public GCCASTExtension(ParserMode mode) {
		this.mode = mode;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#canHandleExpressionKind(org.eclipse.cdt.core.parser.ast.IASTExpression.Kind)
	 */
	public boolean canHandleExpressionKind(Kind kind) {
		if( kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID ||
			kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION ||
			kind == IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION ||
			kind == IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID || 
			kind == IASTGCCExpression.Kind.RELATIONAL_MAX || 
			kind == IASTGCCExpression.Kind.RELATIONAL_MIN ||
			kind == IASTGCCExpression.Kind.STATEMENT_EXPRESSION )
			return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#getExpressionResultType(org.eclipse.cdt.internal.core.parser.pst.TypeInfo, org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTTypeId, org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public ITypeInfo getExpressionResultType(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTTypeId typeId) {
		ITypeInfo info = null;
		if( kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID ||
			kind == IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION )
		{
			info = TypeInfoProvider.newTypeInfo( ITypeInfo.t_int );
			info.setBit(true, ITypeInfo.isUnsigned);
		}
		else if( kind == IASTGCCExpression.Kind.RELATIONAL_MAX || 
			kind == IASTGCCExpression.Kind.RELATIONAL_MIN )
		{
			if( lhs instanceof ASTExpression )
				info = TypeInfoProvider.newTypeInfo( ((ASTExpression)lhs).getResultType().getResult() );
		}
		else if( kind == IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID )
		{
			if( typeId instanceof ASTTypeId )
				info = TypeInfoProvider.newTypeInfo( ((ASTTypeId)typeId).getTypeSymbol().getTypeInfo() );
		}
		else if ( kind == IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION )
		{
			if( lhs instanceof ASTExpression ){
			    if( ((ASTExpression) lhs).getResultType() != null )
			        info = TypeInfoProvider.newTypeInfo( ((ASTExpression)lhs).getResultType().getResult() );
			    else {
			        info = TypeInfoProvider.newTypeInfo( ITypeInfo.t_void );
			    }
			}
		}
		
		if( info != null )
			return info;
		return TypeInfoProvider.newTypeInfo();
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
	public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(ParserSymbolTable pst, IASTScope scope, Type kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, boolean isComplex, boolean isImaginary, boolean isGlobal, Map extensionParms) {
		if( kind == IASTGCCSimpleTypeSpecifier.Type.TYPEOF )
		{
			ASTExpression typeOfExpression = (ASTExpression) extensionParms.get( IASTGCCSimpleTypeSpecifier.TYPEOF_EXRESSION );
			ISymbol s = pst.newSymbol( EMPTY_STRING );
			s.setTypeInfo( typeOfExpression.getResultType().getResult() );
			return new ASTGCCSimpleTypeSpecifier( s, isTypename, ( typeName == null ? EMPTY_STRING : typeName.toCharArray()), Collections.EMPTY_LIST, typeOfExpression );
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#overrideCreateDesignatorMethod(org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind)
	 */
	public boolean overrideCreateDesignatorMethod(DesignatorKind kind) {
		if( kind == IASTGCCDesignator.DesignatorKind.SUBSCRIPT_RANGE )
			return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IASTFactoryExtension#createDesignator(org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.IToken, java.util.Map)
	 */
	public IASTDesignator createDesignator(DesignatorKind kind, IASTExpression constantExpression, IToken fieldIdentifier, Map extensionParms) {
		IASTExpression secondExpression = (IASTExpression) extensionParms.get( IASTGCCDesignator.SECOND_EXRESSION );
		return new ASTGCCDesignator( kind, constantExpression, EMPTY_STRING, -1, secondExpression );
	}

	/**
	 * @param mode2
	 * @return
	 */
	public static IASTFactoryExtension createExtension(ParserMode parseMode) {
		if( parseMode == ParserMode.QUICK_PARSE )
			return new GCCASTExpressionExtension( parseMode );
		
		return new GCCASTCompleteExtension( parseMode );
	}
}
