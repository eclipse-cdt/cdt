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
package org.eclipse.cdt.internal.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public class TypeId implements IDeclarator
{
    private ITokenDuple name;
    private List arrayModifiers = new ArrayList();
    private List pointerOperators = new ArrayList();
	private final IASTScope scope;
    /**
     * 
     */
    public TypeId(IASTScope scope )
    {
       this.scope = scope;  
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#getPointerOperators()
     */
    public List getPointerOperators()
    {
        return pointerOperators;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#addPointerOperator(org.eclipse.cdt.core.parser.ast.ASTPointerOperator)
     */
    public void addPointerOperator(ASTPointerOperator ptrOp)
    {
        pointerOperators.add( ptrOp );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#addArrayModifier(org.eclipse.cdt.core.parser.ast.IASTArrayModifier)
     */
    public void addArrayModifier(IASTArrayModifier arrayMod)
    {
        arrayModifiers.add( arrayMod );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#getArrayModifiers()
     */
    public List getArrayModifiers()
    {
        return arrayModifiers;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#setPointerOperatorName(org.eclipse.cdt.core.parser.ITokenDuple)
     */
    public void setPointerOperatorName(ITokenDuple nameDuple)
    {
        name = nameDuple;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#getPointerOperatorNameDuple()
     */
    public ITokenDuple getPointerOperatorNameDuple()
    {
        return name;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IDeclarator#getScope()
	 */
	public IASTScope getScope() {
		return scope;
	}
}
