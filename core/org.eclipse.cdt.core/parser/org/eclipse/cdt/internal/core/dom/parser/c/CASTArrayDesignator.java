/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation of array designators
 */
public class CASTArrayDesignator extends ASTNode implements
        ICASTArrayDesignator, IASTAmbiguityParent {

    private IASTExpression exp;

    
    public CASTArrayDesignator() {
	}

	public CASTArrayDesignator(IASTExpression exp) {
		setSubscriptExpression(exp);
	}

	@Override
	public CASTArrayDesignator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTArrayDesignator copy(CopyStyle style) {
		CASTArrayDesignator copy = new CASTArrayDesignator(exp == null ? null : exp.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
    @Override
	public IASTExpression getSubscriptExpression() {
        return exp;
    }

    @Override
	public void setSubscriptExpression(IASTExpression value) {
        assertNotFrozen();
        exp = value;
        if(value != null) {
        	value.setParent(this);
        	value.setPropertyInParent(SUBSCRIPT_EXPRESSION);
        }
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if (action.shouldVisitDesignators) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if (exp != null && !exp.accept(action))
			return false;

		if (action.shouldVisitDesignators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if( child == exp )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            exp = (IASTExpression) other;
        }
    }
}
