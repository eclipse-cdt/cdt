/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;


public class CPPASTBinaryExpression extends ASTNode implements ICPPASTBinaryExpression, IASTAmbiguityParent {
	private int op;
    private IASTExpression operand1;
    private IASTExpression operand2;
    private IType type;
    private ICPPFunction overload= UNINITIALIZED_FUNCTION;
    private IASTImplicitName[] implicitNames = null;
    
    
    public CPPASTBinaryExpression() {
	}

	public CPPASTBinaryExpression(int op, IASTExpression operand1, IASTExpression operand2) {
		this.op = op;
		setOperand1(operand1);
		setOperand2(operand2);
	}

	public CPPASTBinaryExpression copy() {
		CPPASTBinaryExpression copy = new CPPASTBinaryExpression();
		copy.op = op;
		copy.setOperand1(operand1 == null ? null : operand1.copy());
		copy.setOperand2(operand2 == null ? null : operand2.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public int getOperator() {
        return op;
    }

    public IASTExpression getOperand1() {
        return operand1;
    }

    public IASTExpression getOperand2() {
        return operand2;
    }

    public void setOperator(int op) {
        assertNotFrozen();
        this.op = op;
    }

    public void setOperand1(IASTExpression expression) {
        assertNotFrozen();
        operand1 = expression;   
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
    }

    public void setOperand2(IASTExpression expression) {
        assertNotFrozen();
        operand2 = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_TWO);
		}
    }

	public IASTImplicitName[] getImplicitNames() {
		if(implicitNames == null) {
			ICPPFunction overload = getOverload();
			if(overload == null)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				
			CPPASTImplicitName operatorName = new CPPASTImplicitName(overload.getNameCharArray(), this);
			operatorName.setBinding(overload);
			operatorName.setOperator(true);
			operatorName.computeOperatorOffsets(operand1, true);
			implicitNames = new IASTImplicitName[] { operatorName };
		}
		
		return implicitNames;
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
        
        if( operand1 != null ) if( !operand1.accept( action ) ) return false;
        
        if(action.shouldVisitImplicitNames) { 
        	for(IASTImplicitName name : getImplicitNames()) {
        		if(!name.accept(action)) return false;
        	}
        }
        
        if( operand2 != null ) if( !operand2.accept( action ) ) return false;
        
        if(action.shouldVisitExpressions ){
        	switch( action.leave( this ) ){
        		case ASTVisitor.PROCESS_ABORT : return false;
        		case ASTVisitor.PROCESS_SKIP  : return true;
        		default : break;
        	}
        }
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == operand1 )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand1  = (IASTExpression) other;
        }
        if( child == operand2 )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            operand2  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
    	if (type == null) {
    		type= createExpressionType();
    	}
    	return type;
    }

    public ICPPFunction getOverload() {
    	if (overload != UNINITIALIZED_FUNCTION)
    		return overload;
    	
    	return overload = CPPSemantics.findOverloadedOperator(this);
    }
    
	private IType createExpressionType() {
		// Check for overloaded operator.
		ICPPFunction o= getOverload();
		if (o != null) {
			try {
				return o.getType().getReturnType();
			} catch (DOMException e) {
				e.getProblem();
			}
		}
		
		IType type1 = getOperand1().getExpressionType();
		IType ultimateType1 = SemanticUtil.getUltimateTypeUptoPointers(type1);
		if (ultimateType1 instanceof IProblemBinding) {
			return type1;
		}
		
		IType type2 = getOperand2().getExpressionType();
		IType ultimateType2 = SemanticUtil.getUltimateTypeUptoPointers(type2);
		if (ultimateType2 instanceof IProblemBinding) {
			return type2;
		}
		
        final int op = getOperator();
        switch (op) {
        case IASTBinaryExpression.op_lessEqual:
        case IASTBinaryExpression.op_lessThan:
        case IASTBinaryExpression.op_greaterEqual:
        case IASTBinaryExpression.op_greaterThan:
        case IASTBinaryExpression.op_logicalAnd:
        case IASTBinaryExpression.op_logicalOr:
        case IASTBinaryExpression.op_equals:
        case IASTBinaryExpression.op_notequals:
        	return new CPPBasicType(Kind.eBoolean, 0, this);
        case IASTBinaryExpression.op_plus:
        	if (ultimateType2 instanceof IPointerType) {
        		return ultimateType2;
        	}
        	break;
        case IASTBinaryExpression.op_minus:
        	if (ultimateType2 instanceof IPointerType) {
        		if (ultimateType1 instanceof IPointerType) {
        			return CPPVisitor.getPointerDiffType(this);
        		}
        		return ultimateType1;
        	}
        	break;
        case ICPPASTBinaryExpression.op_pmarrow:
        case ICPPASTBinaryExpression.op_pmdot:
        	if (type2 instanceof ICPPPointerToMemberType) {
        		return ((ICPPPointerToMemberType) type2).getType();
        	} 
        	return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE, getRawSignature().toCharArray()); 
        }
		return type1;
	}

}
