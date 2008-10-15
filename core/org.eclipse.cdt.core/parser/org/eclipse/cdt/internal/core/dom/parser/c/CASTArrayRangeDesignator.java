/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTArrayRangeDesignator extends ASTNode implements
        IGCCASTArrayRangeDesignator, IASTAmbiguityParent {

    private IASTExpression floor, ceiling;

    public CASTArrayRangeDesignator() {
	}

	public CASTArrayRangeDesignator(IASTExpression floor, IASTExpression ceiling) {
		setRangeFloor(floor);
		setRangeCeiling(ceiling);
	}

	public IASTExpression getRangeFloor() {
        return this.floor;
    }

    public void setRangeFloor(IASTExpression expression) {
        floor = expression;
        if(expression != null) {
        	expression.setParent(this);
        	expression.setPropertyInParent(SUBSCRIPT_FLOOR_EXPRESSION);
        }
    }

    public IASTExpression getRangeCeiling() {
        return ceiling;        
    }

    public void setRangeCeiling(IASTExpression expression) {
        ceiling = expression;
        if(expression != null) {
        	expression.setParent(this);
        	expression.setPropertyInParent(SUBSCRIPT_CEILING_EXPRESSION);
        }
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if (action.shouldVisitDesignators && action instanceof ICASTVisitor) {
		    switch( ((ICASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( floor != null ) if( !floor.accept( action ) ) return false;
        if( ceiling != null ) if( !ceiling.accept( action ) ) return false;

        if (action.shouldVisitDesignators && action instanceof ICASTVisitor) {
		    switch( ((ICASTVisitor)action).leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( child == floor )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            floor = (IASTExpression) other;
        }
        if( child == ceiling)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            ceiling = (IASTExpression) other;
        }
    }

}
