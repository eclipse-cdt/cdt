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
package org.eclipse.cdt.internal.core.parser.ast;

import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind;


/**
 * @author jcamelon
 *
 */
public class BaseASTFactory  {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createMacro(java.lang.String, int, int, int)
	 */
	public IASTMacro createMacro(String name, int startingOffset, int nameOffset, int nameEndOffset, int endingOffset) {
		IASTMacro m = new ASTMacro( name, startingOffset, endingOffset, nameOffset, nameEndOffset );
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createInclusion(java.lang.String, java.lang.String, boolean)
	 */
	public IASTInclusion createInclusion(String name, String fileName, boolean local, int startingOffset, int nameOffset, int nameEndOffset, int endingOffset) {
		IASTInclusion inclusion = new ASTInclusion( name, fileName, local, startingOffset, nameOffset, nameEndOffset, endingOffset );
		return inclusion;
	}

    public IASTAbstractDeclaration createAbstractDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOperator)
    {
        return new ASTAbstractDeclaration( isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOperator );
    }

    public IASTArrayModifier createArrayModifier(IASTExpression exp)
    {
        return new ASTArrayModifier( exp );
    }

    public IASTDesignator createDesignator(DesignatorKind kind, IASTExpression constantExpression, IToken fieldIdentifier)
    {
        return new ASTDesignator( kind, constantExpression, 
        		fieldIdentifier == null ? "" : fieldIdentifier.getImage(), 
        		fieldIdentifier == null ? -1 : fieldIdentifier.getOffset() );
    }


}
