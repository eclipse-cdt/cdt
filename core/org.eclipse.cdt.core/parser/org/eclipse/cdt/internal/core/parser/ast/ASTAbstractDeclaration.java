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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class ASTAbstractDeclaration  implements IASTAbstractDeclaration
{
	private final boolean isVolatile;
    private final List parms;
    private final boolean isConst; 
	private final IASTTypeSpecifier typeSpecifier; 
	private final List pointerOperators; 
	private final List arrayModifiers;
	private final ASTPointerOperator pointerOperator; 
    /**
     * @param isConst
     * @param typeSpecifier
     * @param pointerOperators
     * @param arrayModifiers
     */
    public ASTAbstractDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp )
    {
       this.isConst = isConst;
       this.typeSpecifier = typeSpecifier;
       this.pointerOperators = pointerOperators; 
       this.arrayModifiers = arrayModifiers;
       this.parms = parameters;
       this.pointerOperator = pointerOp;
       this.isVolatile = isVolatile;
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
    	if( pointerOperators == null ) return EmptyIterator.EMPTY_ITERATOR;
        return pointerOperators.iterator();
    }
    public List getPointerOperatorsList(){
        if( pointerOperators == null ) return Collections.EMPTY_LIST;
        return pointerOperators;
    }
    
    public int getNumPointerOperators(){
    	if( pointerOperators == null )
    		return 0;
    	return pointerOperators.size();	
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getArrayModifiers()
     */
    public Iterator getArrayModifiers()
    {
		if( arrayModifiers == null ) return EmptyIterator.EMPTY_ITERATOR;
        return arrayModifiers.iterator();
    }
    
    public List getArrayModifiersList(){
        if( arrayModifiers == null ) return Collections.EMPTY_LIST;
        return arrayModifiers;
    }
    
    public int getNumArrayModifiers(){
    	if( arrayModifiers == null )
    		return 0;
    	return arrayModifiers.size();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getParameters()
     */
    public Iterator getParameters()
    {
		if( parms == null ) return EmptyIterator.EMPTY_ITERATOR;
        return parms.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getPointerToFunctionOperator()
     */
    public ASTPointerOperator getPointerToFunctionOperator()
    {
        return pointerOperator;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#isVolatile()
     */
    public boolean isVolatile()
    {
        return isVolatile;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	List arrayMods = getArrayModifiersList();
    	int size = arrayMods.size();
    	for( int i = 0; i< size; i++ )
    		((IASTArrayModifier)arrayMods.get(i)).acceptElement(requestor);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
}
