/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTArrayModifier extends ASTNode implements IASTArrayModifier, IASTAmbiguityParent {
    private IASTExpression exp;

    public CPPASTArrayModifier() {
	}

	public CPPASTArrayModifier(IASTExpression exp) {
		setConstantExpression(exp);
	}

	@Override
	public IASTExpression getConstantExpression() {
        return exp;
    }

	@Override
	public CPPASTArrayModifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTArrayModifier copy(CopyStyle style) {
		CPPASTArrayModifier copy = new CPPASTArrayModifier(exp == null ? null : exp.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

    @Override
	public void setConstantExpression(IASTExpression expression) {
        assertNotFrozen();
        exp = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(CONSTANT_EXPRESSION);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitArrayModifiers) {
			switch (action.visit(this)) {
    		case ASTVisitor.PROCESS_ABORT: return false;
    		case ASTVisitor.PROCESS_SKIP: return true;
    		default: break;
    		}
    	}
        if (exp != null && !exp.accept(action))
        	return false;
        
		if (action.shouldVisitArrayModifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == exp) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            exp = (IASTExpression) other;
        }
    }
}
