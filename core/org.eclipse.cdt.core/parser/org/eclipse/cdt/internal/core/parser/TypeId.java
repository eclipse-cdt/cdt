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
import java.util.Collections;
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
	private static final int DEFAULT_ARRAYLIST_SIZE = 4;
    private ITokenDuple name;
    private List arrayModifiers;
    private List pointerOperators;
	private IASTScope scope;

    /**
	 * @param scope2
	 */
	public void reset(IASTScope s) {
		this.scope = s;
	    arrayModifiers = Collections.EMPTY_LIST;
	    pointerOperators = Collections.EMPTY_LIST;
		name = null;
	}
	/**
     * 
     */
    public TypeId()
    {
    	reset( null );
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
    	if( pointerOperators == Collections.EMPTY_LIST )
    		pointerOperators = new ArrayList( DEFAULT_ARRAYLIST_SIZE );
        pointerOperators.add( ptrOp );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclarator#addArrayModifier(org.eclipse.cdt.core.parser.ast.IASTArrayModifier)
     */
    public void addArrayModifier(IASTArrayModifier arrayMod)
    {
       	if( arrayModifiers == Collections.EMPTY_LIST )
       		arrayModifiers = new ArrayList( DEFAULT_ARRAYLIST_SIZE );
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
