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
package org.eclipse.cdt.internal.core.parser.ast.quick;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class ASTParameterDeclaration implements IASTParameterDeclaration
{
	private final boolean isConst; 
	private final IASTTypeSpecifier typeSpecifier; 
	private final List pointerOperators;
	private final List arrayModifiers;
	private final String parameterName; 
	private final IASTInitializerClause initializerClause;
    /**
     * @param isConst
     * @param typeSpecifier
     * @param pointerOperators
     * @param arrayModifiers
     * @param parameterName
     * @param initializerClause
     */
    public ASTParameterDeclaration(boolean isConst, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, String parameterName, IASTInitializerClause initializerClause)
    {
		this.isConst = isConst; 
		this.typeSpecifier = typeSpecifier;
		this.pointerOperators = pointerOperators;
		this.arrayModifiers = arrayModifiers;
		this.parameterName = parameterName;
		this.initializerClause = initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration#getName()
     */
    public String getName()
    {
        return parameterName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration#getDefaultValue()
     */
    public IASTInitializerClause getDefaultValue()
    {
        return initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#isConst()
     */
    public boolean isConst()
    {
        return isConst;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getTypeSpecifier()
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return typeSpecifier;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getPointerOperators()
     */
    public Iterator getPointerOperators()
    {
        return pointerOperators.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getArrayModifiers()
     */
    public Iterator getArrayModifiers()
    {
        return arrayModifiers.iterator();
    }
}
