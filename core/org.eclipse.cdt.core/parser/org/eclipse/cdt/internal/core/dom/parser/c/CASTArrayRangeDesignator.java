/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;

/**
 * @author jcamelon
 */
public class CASTArrayRangeDesignator extends CASTNode implements
        IGCCASTArrayRangeDesignator {

    private IASTExpression floor, ceiling;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTArrayRangeDesignator#getRangeFloor()
     */
    public IASTExpression getRangeFloor() {
        return this.floor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTArrayRangeDesignator#setRangeFloor(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setRangeFloor(IASTExpression expression) {
        floor =expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTArrayRangeDesignator#getRangeCeiling()
     */
    public IASTExpression getRangeCeiling() {
        return ceiling;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTArrayRangeDesignator#setRangeCeiling(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setRangeCeiling(IASTExpression expression) {
        ceiling = expression;
    }

    public boolean accept( ASTVisitor action ){
        if( action instanceof CASTVisitor && ((CASTVisitor)action).shouldVisitDesignators ){
		    switch( ((CASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( floor != null ) if( !floor.accept( action ) ) return false;
        if( ceiling != null ) if( !ceiling.accept( action ) ) return false;
        return true;
    }
}
