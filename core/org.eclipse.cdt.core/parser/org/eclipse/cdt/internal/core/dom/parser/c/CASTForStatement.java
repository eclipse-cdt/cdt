/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTForStatement extends ASTAttributeOwner implements IASTForStatement, IASTAmbiguityParent {
    private IScope scope;
    private IASTExpression condition;
    private IASTExpression iterationExpression;
    private IASTStatement body;
    private IASTStatement init;

    public CASTForStatement() {
	}

	public CASTForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body) {
    	setInitializerStatement(init);
    	setConditionExpression(condition);
    	setIterationExpression(iterationExpression);
    	setBody(body);
	}

	@Override
	public CASTForStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTForStatement copy(CopyStyle style) {
		CASTForStatement copy = new CASTForStatement();
		return copy(copy, style);
	}
	
	protected <T extends CASTForStatement> T copy(T copy, CopyStyle style) {
		copy.setInitializerStatement(init == null ? null : init.copy(style));
		copy.setConditionExpression(condition == null ? null : condition.copy(style));
		copy.setIterationExpression(iterationExpression == null ?
				null : iterationExpression.copy(style));
		copy.setBody(body == null ? null : body.copy(style));
		return super.copy(copy, style);
	}
	
	@Override
	public IASTExpression getConditionExpression() {
        return condition;
    }

    @Override
	public void setConditionExpression(IASTExpression condition) {
        assertNotFrozen();
        this.condition = condition;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
		}
    }

    @Override
	public IASTExpression getIterationExpression() {
        return iterationExpression;
    }

    @Override
	public void setIterationExpression(IASTExpression iterator) {
        assertNotFrozen();
        this.iterationExpression = iterator;
        if (iterator != null) {
			iterator.setParent(this);
			iterator.setPropertyInParent(ITERATION);
		}
    }
    
    @Override
	public IASTStatement getInitializerStatement() {
        return init;
    }

    @Override
	public void setInitializerStatement(IASTStatement statement) {
        assertNotFrozen();
        init = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(INITIALIZER);
		}
    }
    @Override
	public IASTStatement getBody() {
        return body;
    }

    @Override
	public void setBody(IASTStatement statement) {
        assertNotFrozen();
        body = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(BODY);
		}
    }

    @Override
	public IScope getScope() {
        if (scope == null)
            scope = new CScope(this, EScopeKind.eLocal);
        return scope;
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

        if (!acceptByAttributes(action)) return false;
        if (init != null && !init.accept(action)) return false;
        if (condition != null && !condition.accept(action)) return false;
        if (iterationExpression != null && !iterationExpression.accept(action)) return false;
        if (body != null && !body.accept(action)) return false;

        if (action.shouldVisitStatements) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (body == child) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            body = (IASTStatement) other;
        }
        if (child == init) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            init  = (IASTStatement) other;
        }
        if (child == iterationExpression) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            iterationExpression = (IASTExpression) other;
        }
        if (child == condition) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            condition = (IASTExpression) other;
        }
    }
}