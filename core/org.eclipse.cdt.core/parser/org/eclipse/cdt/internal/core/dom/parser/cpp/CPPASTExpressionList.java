/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM) - implicit names
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;


public class CPPASTExpressionList extends ASTNode implements ICPPASTExpressionList, IASTAmbiguityParent {


	/**
	 * Caution: may contain nulls. 
	 * @see CPPASTExpressionList#computeImplicitNames
	 */
	private IASTImplicitName[] implicitNames;

	
	public CPPASTExpressionList copy() {
		CPPASTExpressionList copy = new CPPASTExpressionList();
		for(IASTExpression expr : getExpressions())
			copy.addExpression(expr == null ? null : expr.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression [] getExpressions() {
        if( expressions == null ) return IASTExpression.EMPTY_EXPRESSION_ARRAY;
        return (IASTExpression[]) ArrayUtil.trim( IASTExpression.class, expressions );
    }

    public void addExpression(IASTExpression expression) {
        assertNotFrozen();
        expressions = (IASTExpression [])ArrayUtil.append( IASTExpression.class, expressions, expression );
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NESTED_EXPRESSION);
		}
    }

    private IASTExpression [] expressions = new IASTExpression[2];
    
    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        IASTExpression[] exps = getExpressions();
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? computeImplicitNames() : null;

        for(int i = 0, n = exps.length; i < n; i++) {
        	if(!exps[i].accept(action)) { 
        		return false;
        	}
        	if(i < n-1 && implicits != null && implicits[i] != null) {
        		if(!implicits[i].accept(action)) {
        			return false;
        		}
        	}
        }
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		} 
        return true;
    }
    
    
    /**
     * Returns an array of implicit names where each element of the array
     * represents a comma between the expression in the same index and the
     * next expression. This array contains null elements as placeholders
     * for commas that do not resolve to overloaded operators.
     */
    private IASTImplicitName[] computeImplicitNames() {
		if(implicitNames == null) {
			IASTExpression[] exprs = getExpressions(); // has to be at least two
			if(exprs.length < 2)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			implicitNames = new IASTImplicitName[exprs.length-1];
			
			if(getPropertyInParent() == IASTFunctionCallExpression.PARAMETERS)
				return implicitNames;
			
			for(int i = 0, n = exprs.length-1; i < n; i++) {
				ICPPFunction overload = getOverload(i);
				if(overload != null) {
					CPPASTImplicitName operatorName = new CPPASTImplicitName(OverloadableOperator.COMMA, this);
					operatorName.setBinding(overload);
					operatorName.computeOperatorOffsets(exprs[i], true);
					implicitNames[i] = operatorName;
				}
			}
		}
		
		return implicitNames;
	}
    
    
    public IASTImplicitName[] getImplicitNames() {
    	return (IASTImplicitName[])ArrayUtil.removeNulls(IASTImplicitName.class, computeImplicitNames());
    }
    
    
    /**
     * @param index the index of the first argument
     */
    private ICPPFunction getOverload(int index) {
    	// try to find a method
    	IASTExpression[] exprs = getExpressions();
    	
    	IType type1 = exprs[index].getExpressionType();
    	IType ultimateType1 = SemanticUtil.getUltimateTypeUptoPointers(type1);
		if (ultimateType1 instanceof IProblemBinding) {
			return null;
		}
		if (ultimateType1 instanceof ICPPClassType) {
			ICPPFunction operator = CPPSemantics.findOperatorComma(this, index, (ICPPClassType) ultimateType1);
			if (operator != null)
				return operator;
		}
		
		// try to find a function
		IType type2 = exprs[index+1].getExpressionType();
		IType ultimateType2 = SemanticUtil.getUltimateTypeUptoPointers(type2);
		if (ultimateType2 instanceof IProblemBinding)
			return null;
		if (isUserDefined(ultimateType1) || isUserDefined(ultimateType2))
			return CPPSemantics.findOverloadedOperator(this, index);
		
    	return null;
    }
    
    private static boolean isUserDefined(IType type) {
    	return type instanceof ICPPClassType || type instanceof IEnumeration;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( expressions == null ) return;
        for (int i = 0; i < expressions.length; ++i) {
            if (child == expressions[i]) {
                other.setPropertyInParent(child.getPropertyInParent());
                other.setParent(child.getParent());
                expressions[i] = (IASTExpression) other;
            }
        }
    }
    
    public IType getExpressionType() {
    	for (int i = expressions.length-1; i >= 0 ; i--) {
			IASTExpression expr= expressions[i];
			if (expr != null)
				return expr.getExpressionType();
		}
    	return null;
    }
}
