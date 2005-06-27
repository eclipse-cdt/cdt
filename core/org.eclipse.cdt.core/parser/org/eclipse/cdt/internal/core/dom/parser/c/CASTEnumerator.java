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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTEnumerator extends CASTNode implements IASTEnumerator, IASTAmbiguityParent {

    private IASTName name;
    private IASTExpression value;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#setValue(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setValue(IASTExpression expression) {
        this.value = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#getValue()
     */
    public IASTExpression getValue() {
        return value;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitEnumerators ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        if( value != null ) if( !value.accept( action ) ) return false;
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		if( n == name )return r_definition;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == value)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            value  = (IASTExpression) other;
        }
    }

}
