/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.core.parser.extension;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;

/**
 * @author jcamelon
 *
 */
public interface IASTFactoryExtension {
	
	public boolean overrideCreateExpressionMethod();
    public IASTExpression createExpression(
            IASTScope scope,
            IASTExpression.Kind kind,
            IASTExpression lhs,
            IASTExpression rhs,
            IASTExpression thirdExpression,
            IASTTypeId typeId,
            ITokenDuple idExpression, 
			String literal, 
			IASTNewExpressionDescriptor newDescriptor, 
			List references);
    
    public boolean canHandleExpressionKind( IASTExpression.Kind kind );
	/**
	 * @param kind
	 * @param lhs
	 * @param rhs
	 * @param typeId
	 * @return TODO
	 */
	public ITypeInfo getExpressionResultType(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTTypeId typeId);
    
	public boolean overrideCreateSimpleTypeSpecifierMethod(Type type);
	
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
        ParserSymbolTable pst,
        IASTScope scope,
        IASTSimpleTypeSpecifier.Type kind,
        ITokenDuple typeName,
        boolean isShort,
        boolean isLong,
        boolean isSigned, 
		boolean isUnsigned, boolean isTypename, boolean isComplex, boolean isImaginary, boolean isGlobal, Map extensionParms );
    
    public boolean overrideCreateDesignatorMethod( IASTDesignator.DesignatorKind kind );
    public IASTDesignator createDesignator( IASTDesignator.DesignatorKind kind, IASTExpression constantExpression, IToken fieldIdentifier, Map extensionParms );

}
