/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Array subscript expression for c
 */
public class CASTArraySubscriptExpression extends ASTNode implements
        IASTArraySubscriptExpression, IASTAmbiguityParent {

    private IASTExpression array;
    private IASTExpression subscript;

    public CASTArraySubscriptExpression() {
	}

	public CASTArraySubscriptExpression(IASTExpression array, IASTExpression subscript) {
		setArrayExpression(array);
		setSubscriptExpression(subscript);
	}

	public CASTArraySubscriptExpression copy() {
		CASTArraySubscriptExpression copy = new CASTArraySubscriptExpression();
		copy.setArrayExpression(array == null ? null : array.copy());
		copy.setSubscriptExpression(subscript == null ? null : subscript.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression getArrayExpression() {
        return array;
    }

    public void setArrayExpression(IASTExpression expression) {
        assertNotFrozen();
        array = expression;
        if(expression != null) {
        	expression.setParent(this);
        	expression.setPropertyInParent(ARRAY);
        }
    }

    public IASTExpression getSubscriptExpression() {
        return subscript;
    }

    public void setSubscriptExpression(IASTExpression expression) {
        assertNotFrozen();
        this.subscript = expression;
        if(expression != null) {
        	expression.setParent(this);
        	expression.setPropertyInParent(SUBSCRIPT);
        }
    }

    public IASTInitializerClause getArgument() {
		return getSubscriptExpression();
	}

	public void setArgument(IASTInitializerClause expression) {
		if (expression instanceof IASTExpression) {
			setSubscriptExpression((IASTExpression) expression);
		} else {
			setSubscriptExpression(null);
		}
	}

	@Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( array != null ) if( !array.accept( action ) ) return false;
        if( subscript != null ) if( !subscript.accept( action ) ) return false;

        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( child == array )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            array = (IASTExpression) other;
        }
        if( child == subscript)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            subscript = (IASTExpression) other;
        }
    }
    
    public IType getExpressionType() {
		IType t = getArrayExpression().getExpressionType();
		t = CVisitor.unwrapTypedefs(t);
		if (t instanceof IPointerType)
			return ((IPointerType)t).getType();
		else if (t instanceof IArrayType)
			return ((IArrayType)t).getType();
		return t;
    }

	public boolean isLValue() {
		return true;
	}
	
	public final ValueCategory getValueCategory() {
		return ValueCategory.LVALUE;
	}
}
