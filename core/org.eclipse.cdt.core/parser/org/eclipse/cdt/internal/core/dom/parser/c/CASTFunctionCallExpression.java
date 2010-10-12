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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Function call expression in C.
 */
public class CASTFunctionCallExpression extends ASTNode implements
        IASTFunctionCallExpression, IASTAmbiguityParent {

    private IASTExpression functionName;
    private IASTInitializerClause[] fArguments;

    
    public CASTFunctionCallExpression() {
    	setArguments(null);
	}

	public CASTFunctionCallExpression(IASTExpression functionName, IASTInitializerClause[] args) {
		setFunctionNameExpression(functionName);
		setArguments(args);
	}

	public CASTFunctionCallExpression copy() {
		IASTInitializerClause[] args = null;
		if (fArguments.length > 0) {
			args= new IASTInitializerClause[fArguments.length];
			for (int i=0; i<fArguments.length; i++) {
				args[i]= fArguments[i].copy();
			}
		}

		CASTFunctionCallExpression copy = new CASTFunctionCallExpression(null, args);
		copy.setFunctionNameExpression(functionName == null ? null : functionName.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public void setFunctionNameExpression(IASTExpression expression) {
        assertNotFrozen();
        this.functionName = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FUNCTION_NAME);
		}
    }

    public IASTExpression getFunctionNameExpression() {
        return functionName;
    }

	public IASTInitializerClause[] getArguments() {
        return fArguments;
    }

    public void setArguments(IASTInitializerClause[] arguments) {
        assertNotFrozen();
        if (arguments == null) {
        	fArguments= IASTExpression.EMPTY_EXPRESSION_ARRAY;
        } else {
            fArguments= arguments;
        	for (IASTInitializerClause arg : arguments) {
				arg.setParent(this);
				arg.setPropertyInParent(ARGUMENT);
			}
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
      
		if (functionName != null && !functionName.accept(action))
			return false;        

		for (IASTInitializerClause arg : fArguments) {
			if (!arg.accept(action))
				return false;
		}

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
    }

    public void replace(IASTNode child, IASTNode other) {
		if (child == functionName) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			functionName = (IASTExpression) other;
		}
		for (int i = 0; i < fArguments.length; ++i) {
			if (child == fArguments[i]) {
				other.setPropertyInParent(child.getPropertyInParent());
				other.setParent(child.getParent());
				fArguments[i] = (IASTInitializerClause) other;
			}
		}
	}

	public IType getExpressionType() {
		IType type = getFunctionNameExpression().getExpressionType();
		while (type instanceof ITypeContainer)
			type = ((ITypeContainer) type).getType();
		if (type instanceof IFunctionType)
			return ((IFunctionType) type).getReturnType();
		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	}

	public boolean isLValue() {
		return false;
	}
	
	public final ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
	
	@Deprecated
    public IASTExpression getParameterExpression() {
    	if (fArguments.length == 0)
    		return null;
    	if (fArguments.length == 1) {
    		IASTInitializerClause arg = fArguments[0];
    		if (arg instanceof IASTExpression)
    			return (IASTExpression) arg;
    		return null;
    	}
    		
    	CASTExpressionList result= new CASTExpressionList();
    	for (IASTInitializerClause arg : fArguments) {
    		if (arg instanceof IASTExpression) {
    			result.addExpression(((IASTExpression) arg).copy());
    		}
    	}
    	result.setParent(this);
    	result.setPropertyInParent(ARGUMENT);
        return result;
    }

	@Deprecated
    public void setParameterExpression(IASTExpression expression) {
        assertNotFrozen();
        if (expression == null) {
        	setArguments(null);
        } else if (expression instanceof IASTExpressionList) {
        	setArguments(((IASTExpressionList) expression).getExpressions());
        } else {
        	setArguments(new IASTExpression[] {expression});
        }
    }
}
