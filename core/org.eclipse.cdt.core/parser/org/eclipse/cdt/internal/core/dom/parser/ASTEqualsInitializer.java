/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Initializer with equals sign (copy initialization)
 */
public abstract class ASTEqualsInitializer extends ASTNode implements IASTEqualsInitializer,
		IASTAmbiguityParent {

    private IASTInitializerClause fArgument;
    
    public ASTEqualsInitializer() {
	}

	public ASTEqualsInitializer(IASTInitializerClause arg) {
		setInitializerClause(arg);
	}

	@Override
	public IASTInitializerClause getInitializerClause() {
        return fArgument;
    }

    @Override
	public void setInitializerClause(IASTInitializerClause clause) {
        assertNotFrozen();
        fArgument = clause;
        if (clause != null) {
        	clause.setParent(this);
        	clause.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitInitializers) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
		if (fArgument != null && !fArgument.accept(action))
			return false;
        
		if (action.shouldVisitInitializers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fArgument) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fArgument = (IASTInitializerClause) other;
		}
	}

	@Deprecated
	public IASTExpression getExpression() {
		if (fArgument instanceof IASTExpression)
			return (IASTExpression) fArgument;
		
		return null;
	}

	@Deprecated
	public void setExpression(IASTExpression expression) {
		setInitializerClause(expression);
	}
}
