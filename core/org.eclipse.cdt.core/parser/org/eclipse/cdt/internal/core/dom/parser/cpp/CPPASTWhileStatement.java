/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * While statement in C++.
 */
public class CPPASTWhileStatement extends ASTNode
		implements ICPPASTWhileStatement, IASTAmbiguityParent {
    private IASTExpression condition;
    private IASTStatement body;
    private IASTDeclaration condition2;
    private IScope scope;

    public CPPASTWhileStatement() {
	}

	public CPPASTWhileStatement(IASTDeclaration condition, IASTStatement body) {
    	setConditionDeclaration(condition);
		setBody(body);
	}
    
    public CPPASTWhileStatement(IASTExpression condition, IASTStatement body) {
		setCondition(condition);
		setBody(body);
	}

    public CPPASTWhileStatement copy() {
		CPPASTWhileStatement copy = new CPPASTWhileStatement();
		copy.setConditionDeclaration(condition2 == null ? null : condition2.copy());
		copy.setCondition(condition == null ? null : condition.copy());
		copy.setBody(body == null ? null : body.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
    
	public IASTExpression getCondition() {
        return condition;
    }

    public void setCondition(IASTExpression condition) {
        assertNotFrozen();
        this.condition = condition;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITIONEXPRESSION);
			condition2= null;
		}
    }

    public IASTStatement getBody() {
        return body;
    }

    public void setBody(IASTStatement body) {
        assertNotFrozen();
        this.body = body;
        if (body != null) {
			body.setParent(this);
			body.setPropertyInParent(BODY);
		}
    }

	public IASTDeclaration getConditionDeclaration() {
		return condition2;
	}

	public void setConditionDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
		condition2 = declaration;
		if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(CONDITIONDECLARATION);
			condition= null;
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        if (condition != null && !condition.accept(action)) return false;
        if (condition2 != null && !condition2.accept(action)) return false;
        if (body != null && !body.accept(action)) return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
    
	public void replace(IASTNode child, IASTNode other) {
		if (body == child) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			body = (IASTStatement) other;
		}
		if (child == condition || child == condition2) {
			if (other instanceof IASTExpression) {
				setCondition((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setConditionDeclaration((IASTDeclaration) other);
			}
		}
	}

	public IScope getScope() {
		if (scope == null)
            scope = new CPPBlockScope(this);
        return scope;	
    }
}
