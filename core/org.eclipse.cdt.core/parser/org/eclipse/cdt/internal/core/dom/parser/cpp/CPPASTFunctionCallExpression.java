/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * @author jcamelon
 */
public class CPPASTFunctionCallExpression extends ASTNode implements
        IASTFunctionCallExpression, IASTAmbiguityParent {
	
    private IASTExpression functionName;
    private IASTExpression parameter;

    
    public CPPASTFunctionCallExpression() {
	}

	public CPPASTFunctionCallExpression(IASTExpression functionName, IASTExpression parameter) {
		setFunctionNameExpression(functionName);
		setParameterExpression(parameter);
	}

	public CPPASTFunctionCallExpression copy() {
		CPPASTFunctionCallExpression copy = new CPPASTFunctionCallExpression();
		copy.setFunctionNameExpression(functionName == null ? null : functionName.copy());
		copy.setParameterExpression(parameter == null ? null : parameter.copy());
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

    public void setParameterExpression(IASTExpression expression) {
        assertNotFrozen();
        this.parameter = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(PARAMETERS);
		}
    }

    public IASTExpression getParameterExpression() {
        return parameter;
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
      
        if( functionName != null ) if( !functionName.accept( action ) ) return false;
        if( parameter != null )  if( !parameter.accept( action ) ) return false;

        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == functionName )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            functionName  = (IASTExpression) other;
        }    
        if( child == parameter )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            parameter  = (IASTExpression) other;
        }    
    }
    
    public IType getExpressionType() {
    	try {
    		IType t= null;
    		if (functionName instanceof IASTIdExpression) {
    			final IBinding binding= ((IASTIdExpression) functionName).getName().resolvePreBinding();
    			if (binding instanceof ICPPConstructor) {
    				IBinding owner= binding.getOwner();
    				if (owner instanceof ICPPClassType) {
    					return (ICPPClassType) owner;
    				}
    				return new ProblemBinding(this, IProblemBinding.SEMANTIC_BAD_SCOPE, binding.getName().toCharArray());
    			} else if (binding instanceof IFunction) {
    				t = ((IFunction) binding).getType();
    			} else if (binding instanceof IVariable) {
    				t = ((IVariable) binding).getType();
    			} else if (binding instanceof IType) {
    				return (IType) binding;  // constructor or simple type initializer
    			} else if (binding instanceof IProblemBinding) {
    				return (IProblemBinding) binding;
    			}
    		} else {
    			t= functionName.getExpressionType();
    		}

    		t= SemanticUtil.getUltimateTypeUptoPointers(t);
    		if (t instanceof IFunctionType) {
    			return ((IFunctionType) t).getReturnType();
    		} else if (t instanceof ICPPClassType) {
    			ICPPFunction op = CPPSemantics.findOperator(this, (ICPPClassType) t);
    			if (op != null) {
    				return op.getType().getReturnType();
    			}
    		} else if (t instanceof IPointerType) {
    			t= SemanticUtil.getUltimateTypeUptoPointers(((IPointerType) t).getType());
    			if (t instanceof IFunctionType) {
    				return ((IFunctionType) t).getReturnType();
    			}
    		}
		} catch (DOMException e) {
			return e.getProblem();
		} 
		return null;
    }
}
