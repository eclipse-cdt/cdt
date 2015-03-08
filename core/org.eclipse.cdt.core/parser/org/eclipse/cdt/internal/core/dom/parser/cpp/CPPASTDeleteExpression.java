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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;


public class CPPASTDeleteExpression extends ASTNode implements ICPPASTDeleteExpression, IASTAmbiguityParent {
    private static final ICPPEvaluation EVALUATION = new EvalFixed(CPPSemantics.VOID_TYPE, PRVALUE, Value.UNKNOWN);
	
    private IASTExpression operand;
    private boolean isGlobal;
    private boolean isVectored;

    private IASTImplicitName[] implicitNames;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

    public CPPASTDeleteExpression() {
	}

	public CPPASTDeleteExpression(IASTExpression operand) {
		setOperand(operand);
	}
	
	public CPPASTDeleteExpression(CPPASTDeleteExpression from) {
		setOperand(from.operand);
	}
	
	@Override
	public CPPASTDeleteExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTDeleteExpression copy(CopyStyle style) {
		CPPASTDeleteExpression copy =
				new CPPASTDeleteExpression(operand == null ? null : operand.copy(style));
		copy.isGlobal = isGlobal;
		copy.isVectored = isVectored;
		return copy(copy, style);
	}

	@Override
	public IASTExpression getOperand() {
        return operand;
    }

    @Override
	public void setOperand(IASTExpression expression) {
        assertNotFrozen();
        operand = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
    }

    @Override
	public void setIsGlobal(boolean global) {
        assertNotFrozen();
        isGlobal = global;
    }

    @Override
	public boolean isGlobal() {
        return isGlobal;
    }

    @Override
	public void setIsVectored(boolean vectored) {
        assertNotFrozen();
        isVectored = vectored;
    }

    @Override
	public boolean isVectored() {
        return isVectored;
    }

    /**
     * Tries to resolve both the destructor and operator delete.
     */
    @Override
	public IASTImplicitName[] getImplicitNames() {
    	if (implicitNames == null) {
	    	List<IASTImplicitName> names = new ArrayList<>();
	    	
	    	if (!isVectored) {
		    	ICPPFunction destructor = CPPSemantics.findImplicitlyCalledDestructor(this);
		    	if (destructor != null) {
		    		CPPASTImplicitName destructorName = new CPPASTImplicitName(destructor.getNameCharArray(), this);
		    		destructorName.setBinding(destructor);
		    		destructorName.computeOperatorOffsets(operand, false);
		    		names.add(destructorName);
		    	}
	    	}
	    	
	    	if (!isGlobal) {
		    	ICPPFunction deleteOperator = CPPSemantics.findOverloadedOperator(this);
		    	if (deleteOperator != null && !(deleteOperator instanceof CPPImplicitFunction)) {
		    		CPPASTImplicitName deleteName = new CPPASTImplicitName(deleteOperator.getNameCharArray(), this);
		    		deleteName.setOperator(true);
		    		deleteName.setBinding(deleteOperator);
		    		deleteName.computeOperatorOffsets(operand, false);
		    		names.add(deleteName);
		    	}
	    	}
	    	
	    	if (names.isEmpty()) {
	    		implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
	    	} else {
	    		implicitNames = names.toArray(new IASTImplicitName[names.size()]);
	    	}
    	}
    	
    	return implicitNames;    	
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

        if (action.shouldVisitImplicitNames) { 
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }

        if (operand != null && !operand.accept(action))
        	return false;

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
	public ICPPEvaluation getEvaluation() {
		return EVALUATION;
	}

    @Override
	public IType getExpressionType() {
    	return CPPSemantics.VOID_TYPE;
    }

	@Override
	public boolean isLValue() {
		return false;
	}

	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}
	
	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == operand) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand = (IASTExpression) other;
		}
	}
}
