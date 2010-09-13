/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.XVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTConditionalExpression extends ASTNode implements IASTConditionalExpression,
		IASTAmbiguityParent {
	
    private IASTExpression fCondition;
    private IASTExpression fPositive;
    private IASTExpression fNegative;
    private IType fType;
    private ValueCategory fValueCategory;

    
    public CPPASTConditionalExpression() {
	}

	public CPPASTConditionalExpression(IASTExpression condition, IASTExpression postive, IASTExpression negative) {
    	setLogicalConditionExpression(condition);
    	setPositiveResultExpression(postive);
    	setNegativeResultExpression(negative);
	}

	
	public CPPASTConditionalExpression copy() {
		CPPASTConditionalExpression copy = new CPPASTConditionalExpression();
		copy.setLogicalConditionExpression(fCondition == null ? null : fCondition.copy());
		copy.setPositiveResultExpression(fPositive == null ? null : fPositive.copy());
		copy.setNegativeResultExpression(fNegative == null ? null : fNegative.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression getLogicalConditionExpression() {
        return fCondition;
    }

    public void setLogicalConditionExpression(IASTExpression expression) {
        assertNotFrozen();
        fCondition = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(LOGICAL_CONDITION);
		}
    }

    public IASTExpression getPositiveResultExpression() {
        return fPositive;
    }

    public void setPositiveResultExpression(IASTExpression expression) {
        assertNotFrozen();
        this.fPositive = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(POSITIVE_RESULT);
		}
    }

    public IASTExpression getNegativeResultExpression() {
        return fNegative;
    }

    public void setNegativeResultExpression(IASTExpression expression) {
        assertNotFrozen();
        this.fNegative = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEGATIVE_RESULT);
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
        
		if (fCondition != null && !fCondition.accept(action))
			return false;
		if (fPositive != null && !fPositive.accept(action))
			return false;
		if (fNegative != null && !fNegative.accept(action))
			return false;
        
		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

	public void replace(IASTNode child, IASTNode other) {
		if (child == fCondition) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fCondition = (IASTExpression) other;
		}
		if (child == fPositive) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fPositive = (IASTExpression) other;
		}
		if (child == fNegative) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fNegative = (IASTExpression) other;
		}
	}
    
    public IType getExpressionType() {
    	evaluate();
    	return fType;
    }
    
    public ValueCategory getValueCategory() {
    	evaluate();
    	return fValueCategory;
    }
    
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

    private void evaluate() {
    	if (fValueCategory != null)
    		return;
    	
    	fValueCategory= PRVALUE;
    	
    	// Gnu-extension: Empty positive expression is replaced by condition.
		IASTExpression expr2 = getPositiveResultExpression();
		final IASTExpression expr3 = getNegativeResultExpression();
		if (expr2 == null) {
			expr2= getLogicalConditionExpression();
		}
		
		IType t2 = expr2.getExpressionType();
		IType t3 = expr3.getExpressionType();
		if (t2 == null || t3 == null)
			return;
		
		final IType uqt2= getNestedType(t2, TDEF | REF | CVTYPE);
		final IType uqt3= getNestedType(t3, TDEF | REF | CVTYPE);
		if (uqt2 instanceof IProblemBinding || uqt2 instanceof ICPPUnknownType) {
			fType= uqt2;
			return;
		}
		if (uqt3 instanceof IProblemBinding || uqt3 instanceof ICPPUnknownType) {
			fType= uqt3;
			return;
		}
		
		final boolean void2= isVoidType(uqt2);
		final boolean void3= isVoidType(uqt3);

		// Void types: Either both are void or one is a throw expression.
		if (void2 || void3) {
			if (isThrowExpression(expr2)) {
				fType= Conversions.lvalue_to_rvalue(t3);
			} else if (isThrowExpression(expr3)) {
				fType= Conversions.lvalue_to_rvalue(t2);
			} else if (void2 && void3) {
				fType= uqt2;
			} else {
				createProblem();
			}
			return;
		}
		
		final ValueCategory vcat2= expr2.getValueCategory();
		final ValueCategory vcat3= expr3.getValueCategory();
		final boolean isClassType2 = uqt2 instanceof ICPPClassType;
		final boolean isClassType3 = uqt3 instanceof ICPPClassType;

		// Same type and same value category
		if (t2.isSameType(t3)) {
			if (vcat2 == vcat3) {
				fType= t2;
				fValueCategory= vcat2;
				return;
			} 
		} else {
			// Different types with at least one class type
			if (isClassType2 || isClassType3) {
				final Cost cost2= convertToMatch(t2, vcat2, uqt2, t3, vcat3, uqt3); // sets fType and fValueCategory
				final Cost cost3= convertToMatch(t3, vcat3, uqt3, t2, vcat2, uqt2); // sets fType and fValueCategory
				if (cost2.converts() || cost3.converts()) {
					if (cost2.converts()) {
						if (cost3.converts() || cost2.isAmbiguousUDC()) {
							fType= createProblem();
						}
					} else if (cost3.isAmbiguousUDC()) {
						fType= createProblem();
					}
					return;
				}
			} else if (vcat2 == vcat3 && vcat2.isGLValue() && uqt2.isSameType(uqt3)) {
				// Two lvalues or two xvalues with same type up to qualification.
				final CVQualifier cv2 = SemanticUtil.getCVQualifier(t2);
				final CVQualifier cv3 = SemanticUtil.getCVQualifier(t3);
				if (cv2.isAtLeastAsQualifiedAs(cv3)) {
					fType= t2;
					fValueCategory= vcat2;
				} else if (cv3.isAtLeastAsQualifiedAs(cv2)) {
					fType= t3;
					fValueCategory= vcat3;
				} else {
					createProblem();
				}
				return;
			}
		}
		
		// 5.16-5: At least one class type but no conversion
		if (isClassType2 || isClassType3) {
			ICPPFunction builtin = CPPSemantics.findOverloadedConditionalOperator(expr2, expr3);
			if (builtin != null) {
				fType= ExpressionTypes.typeFromFunctionCall(builtin);
			}
			createProblem();
			return;
		}

		// 5.16-6
		t2= Conversions.lvalue_to_rvalue(t2);
		t3= Conversions.lvalue_to_rvalue(t3);
		if (t2.isSameType(t3)) {
			fType= t2;
		} else {
	    	fType= CPPArithmeticConversion.convertCppOperandTypes(IASTBinaryExpression.op_plus, t2, t3);
	    	if (fType == null) {
	    		fType= Conversions.compositePointerType(t2, t3);
		    	if (fType == null) {
		    		createProblem();
		    	}
	    	}
		}
    }

	private boolean isThrowExpression(IASTExpression expr) {
		while (expr instanceof IASTUnaryExpression) {
			final IASTUnaryExpression unaryExpr = (IASTUnaryExpression) expr;
			final int op = unaryExpr.getOperator();
			if (op == IASTUnaryExpression.op_throw) {
				return true;
			} else if (op == IASTUnaryExpression.op_bracketedPrimary) {
				expr= unaryExpr.getOperand();
			} else {
				return false;
			}
		}
		return false;
	}

	private Cost convertToMatch(IType t1, ValueCategory vcat1, IType uqt1, IType t2, ValueCategory vcat2, IType uqt2) {
		// E2 is an lvalue or E2 is an xvalue
		try {
			if (vcat2.isGLValue()) {
				IType target= new CPPReferenceType(t2, vcat2 == XVALUE);
				Cost c= Conversions.checkImplicitConversionSequence(target, t1, vcat1, UDCMode.ALLOWED, Context.REQUIRE_DIRECT_BINDING);
				if (c.converts()) {
					fType= t2;
					fValueCategory= vcat2;
					return c;
				}
			}
			// Both are class types and one derives from the other
			if (uqt1 instanceof ICPPClassType && uqt2 instanceof ICPPClassType) {
				int dist= SemanticUtil.calculateInheritanceDepth(uqt1, uqt2);
				if (dist >= 0) {
					CVQualifier cv1 = SemanticUtil.getCVQualifier(t1);
					CVQualifier cv2 = SemanticUtil.getCVQualifier(t2);
					if (cv2.isAtLeastAsQualifiedAs(cv1)) {
						fType= t2;
						fValueCategory= PRVALUE;
						return new Cost(t1, t2, Rank.IDENTITY);
					}
					return Cost.NO_CONVERSION;
				}
				if (SemanticUtil.calculateInheritanceDepth(uqt2, uqt1) >= 0)
					return Cost.NO_CONVERSION;
			}
			// Unrelated class types or just one class:
			if (vcat2 != PRVALUE) {
				t2= Conversions.lvalue_to_rvalue(t2);
			}
			Cost c= Conversions.checkImplicitConversionSequence(t2, t1, vcat1, UDCMode.ALLOWED, Context.ORDINARY);
			if (c.converts()) {
				fType= t2;
				fValueCategory= PRVALUE;
				return c;
			}
		} catch (DOMException e) {
		}
		return Cost.NO_CONVERSION;
	}

	private ProblemBinding createProblem() {
		return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE, this.getRawSignature().toCharArray());
	}

	private boolean isVoidType(IType t) {
		return t instanceof ICPPBasicType && ((ICPPBasicType) t).getKind() == Kind.eVoid;
	}
}
