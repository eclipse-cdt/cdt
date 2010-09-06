/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
 
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTArraySubscriptExpression extends ASTNode implements ICPPASTArraySubscriptExpression, IASTAmbiguityParent {

    private IASTExpression arrayExpression;
    private IASTInitializerClause subscriptExp;
    private ICPPFunction overload= UNINITIALIZED_FUNCTION;

    private IASTImplicitName[] implicitNames = null;
    
    public CPPASTArraySubscriptExpression() {
	}

	public CPPASTArraySubscriptExpression(IASTExpression arrayExpression, IASTInitializerClause operand) {
		setArrayExpression(arrayExpression);
		setArgument(operand);
	}
	
	public CPPASTArraySubscriptExpression copy() {
		CPPASTArraySubscriptExpression copy = new CPPASTArraySubscriptExpression();
		copy.setArrayExpression(arrayExpression == null ? null : arrayExpression.copy());
		copy.setArgument(subscriptExp == null ? null : subscriptExp.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	

	public IASTExpression getArrayExpression() {
        return arrayExpression;
    }

    public void setArrayExpression(IASTExpression expression) {
        assertNotFrozen();
        arrayExpression = expression;        
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(ARRAY);
		}
    }

    public IASTInitializerClause getArgument() {
        return subscriptExp;
    }

    public void setArgument(IASTInitializerClause arg) {
        assertNotFrozen();
        subscriptExp = arg;
        if (arg != null) {
        	arg.setParent(this);
        	arg.setPropertyInParent(SUBSCRIPT);
		}
    }

    @Deprecated
    public IASTExpression getSubscriptExpression() {
    	if (subscriptExp instanceof IASTExpression)
    		return (IASTExpression) subscriptExp;
    	return null;
    }

    @Deprecated
    public void setSubscriptExpression(IASTExpression expression) {
    	setArgument(expression);
    }
    
    public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null || overload instanceof CPPImplicitFunction)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			// create separate implicit names for the two brackets
			CPPASTImplicitName n1 = new CPPASTImplicitName(OverloadableOperator.BRACKET, this);
			n1.setBinding(overload);
			n1.computeOperatorOffsets(arrayExpression, true);

			CPPASTImplicitName n2 = new CPPASTImplicitName(OverloadableOperator.BRACKET, this);
			n2.setBinding(overload);
			n2.computeOperatorOffsets(subscriptExp, true);
			n2.setAlternate(true);
			
			implicitNames = new IASTImplicitName[] { n1, n2 };
		}
		
		return implicitNames;
	}
    
    
    public ICPPFunction getOverload() {
    	if (overload == UNINITIALIZED_FUNCTION) {
    		overload= null;
    		IType t = getArrayExpression().getExpressionType();
    		t= SemanticUtil.getNestedType(t, TDEF | REF | CVTYPE);
    		if (t instanceof ICPPClassType) {
    			overload= CPPSemantics.findOverloadedOperator(this);
    		}
    	}
    	return overload;
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
        if (arrayExpression != null && !arrayExpression.accept(action))
        	return false;
        
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;
        
        if (implicits != null && implicits.length > 0 && !implicits[0].accept(action))
        	return false;
        
        if (subscriptExp != null && !subscriptExp.accept(action))
        	return false;
        
        if (implicits != null && implicits.length > 0 && !implicits[1].accept(action))
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

    public void replace(IASTNode child, IASTNode other) {
        if (child == subscriptExp) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            subscriptExp  = (IASTExpression) other;
        }
        if (child == arrayExpression) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            arrayExpression  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
		ICPPFunction op = getOverload();
		if (op != null) {
			return ExpressionTypes.typeFromFunctionCall(op);
		}
		IType t1 = getArrayExpression().getExpressionType();
		t1= Conversions.lvalue_to_rvalue(t1);
		if (t1 instanceof IPointerType) {
			t1= ((IPointerType) t1).getType();
			return glvalueType(t1);
		}
		
		IType t2= null;
		IASTInitializerClause arg = getArgument();
		if (arg instanceof IASTExpression) {
			t2= Conversions.lvalue_to_rvalue(t2);
			if (t2 instanceof IPointerType) {
				t2= ((IPointerType) t2).getType();
				return glvalueType(t2);
			}
		}
		if (t1 instanceof ICPPUnknownType || t2 instanceof ICPPUnknownType) {
			// mstodo type of unknown
			return CPPUnknownClass.createUnnamedInstance();
		}
		
		// mstodo return problem type
		return null;
    }

	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	public ValueCategory getValueCategory() {
		ICPPFunction op = getOverload();
		if (op != null) {
			return ExpressionTypes.valueCategoryFromFunctionCall(op);
		}
		return ValueCategory.LVALUE;
	}
}
