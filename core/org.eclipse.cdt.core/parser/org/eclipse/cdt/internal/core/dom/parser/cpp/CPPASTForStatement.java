/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Emanuel Graf IFS - Bug 198269
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;

/**
 * For statement in C++
 */
public class CPPASTForStatement extends ASTAttributeOwner
		implements ICPPASTForStatement, IASTAmbiguityParent {
    private IScope fScope;
    
    private IASTStatement fInit;
    private IASTExpression fCondition;
    private IASTDeclaration fCondDeclaration;
    private IASTExpression fIterationExpression;
    private IASTStatement fBody;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;
    
    public CPPASTForStatement() {
	}

	public CPPASTForStatement(IASTStatement init,  IASTDeclaration condDeclaration,
			IASTExpression iterationExpression, IASTStatement body) {
    	setInitializerStatement(init);
    	setConditionDeclaration(condDeclaration);
    	setIterationExpression(iterationExpression);
    	setBody(body);
	}

    public CPPASTForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body) {
    	setInitializerStatement(init);
    	setConditionExpression(condition);
    	setIterationExpression(iterationExpression);
    	setBody(body);
	}

    @Override
	public CPPASTForStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}
    
	@Override
	public CPPASTForStatement copy(CopyStyle style) {
		CPPASTForStatement copy = new CPPASTForStatement();
		copy.setInitializerStatement(fInit == null ? null : fInit.copy(style));
		copy.setConditionDeclaration(fCondDeclaration == null ? null : fCondDeclaration.copy(style));
		copy.setConditionExpression(fCondition == null ? null : fCondition.copy(style));
		copy.setIterationExpression(fIterationExpression == null ?
				null : fIterationExpression.copy(style));
		copy.setBody(fBody == null ? null : fBody.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getConditionExpression() {
        return fCondition;
    }

    @Override
	public void setConditionExpression(IASTExpression condition) {
        assertNotFrozen();
        this.fCondition = condition;
        if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
			fCondDeclaration= null;
		}
    }

    @Override
	public IASTExpression getIterationExpression() {
        return fIterationExpression;
    }

    @Override
	public void setIterationExpression(IASTExpression iterator) {
        assertNotFrozen();
        this.fIterationExpression = iterator;
        if (iterator != null) {
			iterator.setParent(this);
			iterator.setPropertyInParent(ITERATION);
		}
    }

    @Override
	public IASTStatement getBody() {
        return fBody;
    }

    @Override
	public void setBody(IASTStatement statement) {
        assertNotFrozen();
        fBody = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(BODY);
		}
    }

    @Override
	public IScope getScope() {
        if (fScope == null)
            fScope = new CPPBlockScope(this);
        return fScope;
    }

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getLocalVariablesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
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

        if (!acceptByAttributeSpecifiers(action)) return false;
        if (fInit != null && !fInit.accept(action)) return false;
        if (fCondition != null && !fCondition.accept(action)) return false;
        if (fCondDeclaration != null && !fCondDeclaration.accept(action)) return false;
        if (fIterationExpression != null && !fIterationExpression.accept(action)) return false;
        if (fBody != null && !fBody.accept(action)) return false;

        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
        	return false;

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
		if (fBody == child) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fBody = (IASTStatement) other;
		} else if (child == fCondition || child == fCondDeclaration) {
			if (other instanceof IASTExpression) {
				setConditionExpression((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setConditionDeclaration((IASTDeclaration) other);
			}
		} else if (child == fIterationExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fIterationExpression = (IASTExpression) other;
		} else if (child == fInit) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fInit = (IASTStatement) other;
		}
	}

    @Override
	public IASTStatement getInitializerStatement() {
        return fInit;
    }

    @Override
	public void setInitializerStatement(IASTStatement statement) {
        assertNotFrozen();
        fInit = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public void setConditionDeclaration(IASTDeclaration d) {
        assertNotFrozen();
        fCondDeclaration = d;
        if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONDITION_DECLARATION);
			fCondition= null;
		}
    }

    @Override
	public IASTDeclaration getConditionDeclaration() {
        return fCondDeclaration;
    }
}
