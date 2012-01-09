/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * c-specific modifier for array specifiers.
 */
public class CASTArrayModifier extends ASTNode implements ICASTArrayModifier, IASTAmbiguityParent {

    private IASTExpression exp;
    private boolean isVolatile;
    private boolean isRestrict;
    private boolean isStatic;
    private boolean isConst;
    private boolean isVarSized;
    
    public CASTArrayModifier() {
	}

	public CASTArrayModifier(IASTExpression exp) {
		setConstantExpression(exp);
	}

	@Override
	public CASTArrayModifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTArrayModifier copy(CopyStyle style) {
		CASTArrayModifier copy = new CASTArrayModifier(exp == null ? null : exp.copy(style));
		copy.setOffsetAndLength(this);
		copy.isVolatile = isVolatile;
		copy.isRestrict = isRestrict;
		copy.isStatic = isStatic;
		copy.isConst = isConst;
		copy.isVarSized = isVarSized;
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
    @Override
	public IASTExpression getConstantExpression() {
        return exp;
    }

    @Override
	public void setConstantExpression(IASTExpression expression) {
        assertNotFrozen();
        this.exp = expression;
        if(expression != null) {
        	expression.setParent(this);
        	expression.setPropertyInParent(CONSTANT_EXPRESSION);
        }
    }
	
	@Override
	public boolean isConst() {
        return isConst;
    }

    @Override
	public boolean isStatic() {
        return isStatic;
    }

    @Override
	public boolean isRestrict() {
        return isRestrict;
    }

    @Override
	public boolean isVolatile() {
        return isVolatile;
    }

    @Override
	public void setConst(boolean value) {
        assertNotFrozen();
        this.isConst = value;
    }

    @Override
	public void setVolatile(boolean value) {
        assertNotFrozen();
        this.isVolatile = value;
    }

    @Override
	public void setRestrict(boolean value) {
        assertNotFrozen();
        this.isRestrict = value;
    }

    @Override
	public void setStatic(boolean value) {
        assertNotFrozen();
        this.isStatic = value;
    }

    @Override
	public boolean isVariableSized() {
        return isVarSized;
    }

    @Override
	public void setVariableSized(boolean value) {
        assertNotFrozen();
        isVarSized = value;
    }

    @Override
	public boolean accept(ASTVisitor action) {
    	if( action.shouldVisitArrayModifiers ){
    		switch( action.visit( this ) ){
    		case ASTVisitor.PROCESS_ABORT : return false;
    		case ASTVisitor.PROCESS_SKIP  : return true;
    		default : break;
    		}
    	}
        if (exp != null && !exp.accept(action))
        	return false;
        
        if (action.shouldVisitArrayModifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT) {
        	return false;
        }
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
