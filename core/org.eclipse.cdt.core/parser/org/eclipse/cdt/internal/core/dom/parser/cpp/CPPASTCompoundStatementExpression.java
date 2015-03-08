/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalCompound;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

/**
 * Gnu-extension: ({ ... })
 */
public class CPPASTCompoundStatementExpression extends ASTNode implements IGNUASTCompoundStatementExpression, ICPPASTExpression {
    private IASTCompoundStatement fStatement;
    private ICPPEvaluation fEval;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;
    
    public CPPASTCompoundStatementExpression() {
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEval == null) {
			IASTCompoundStatement compound = getCompoundStatement();
			IASTStatement[] statements = compound.getStatements();
			if (statements.length > 0) {
				IASTStatement st = statements[statements.length - 1];
				if (st instanceof IASTExpressionStatement) {
					fEval= new EvalCompound(((ICPPASTExpression) ((IASTExpressionStatement) st).getExpression()).getEvaluation(), this);
				}
			}
			if (fEval == null)
				fEval= EvalFixed.INCOMPLETE;
		}
		return fEval;
	}

	public CPPASTCompoundStatementExpression(IASTCompoundStatement statement) {
		setCompoundStatement(statement);
	}

	@Override
	public CPPASTCompoundStatementExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTCompoundStatementExpression copy(CopyStyle style) {
		CPPASTCompoundStatementExpression copy = new CPPASTCompoundStatementExpression();
		copy.setCompoundStatement(fStatement == null ? null : fStatement.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTCompoundStatement getCompoundStatement() {
        return fStatement;
    }

    @Override
	public void setCompoundStatement(IASTCompoundStatement statement) {
        assertNotFrozen();
        this.fStatement = statement;
        if (statement != null) {
			statement.setParent(this);
			statement.setPropertyInParent(STATEMENT);
		}
    }

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = CPPVisitor.getTemporariesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
	}

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
        if (fStatement != null && !fStatement.accept(action)) return false;
   
        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(fImplicitDestructorNames, action))
        	return false;

        if (action.shouldVisitExpressions) {
        	switch (action.leave(this)) {
        		case ASTVisitor.PROCESS_ABORT: return false;
        		case ASTVisitor.PROCESS_SKIP: return true;
        		default: break;
        	}
        }
        return true;
    }
    
    @Override
	public IType getExpressionType() {
    	return getEvaluation().getTypeOrFunctionSet(this);
	}
    
	@Override
	public boolean isLValue() {
		return false;
	}

	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}
}
