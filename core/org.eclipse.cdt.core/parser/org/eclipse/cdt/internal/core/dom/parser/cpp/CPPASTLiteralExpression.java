/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;

/**
 * Represents a C++ literal.
 */
public class CPPASTLiteralExpression extends ASTNode implements ICPPASTLiteralExpression {
	private static final EvalFixed EVAL_TRUE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE, Value.create(1));
	private static final EvalFixed EVAL_FALSE = new EvalFixed(CPPBasicType.BOOLEAN, PRVALUE, Value.create(0));
	private static final EvalFixed EVAL_NULL_PTR = new EvalFixed(CPPBasicType.NULL_PTR, PRVALUE, Value.create(0));

	public static final CPPASTLiteralExpression INT_ZERO =
			new CPPASTLiteralExpression(lk_integer_constant, new char[] {'0'});
	
    private int kind;
    private char[] value = CharArrayUtils.EMPTY;
	private ICPPEvaluation fEvaluation;

    public CPPASTLiteralExpression() {
	}

	public CPPASTLiteralExpression(int kind, char[] value) {
		this.kind = kind;
		this.value = value;
	}

	@Override
	public CPPASTLiteralExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTLiteralExpression copy(CopyStyle style) {
		CPPASTLiteralExpression copy =
				new CPPASTLiteralExpression(kind, value == null ? null : value.clone());
		return copy(copy, style);
	}

	@Override
	public int getKind() {
        return kind;
    }

    @Override
	public void setKind(int value) {
        assertNotFrozen();
        kind = value;
    }

    @Override
	public char[] getValue() {
    	return value;
    }

    @Override
	public void setValue(char[] value) {
        assertNotFrozen();
    	this.value= value;
    }
    
    @Override
	public String toString() {
        return new String(value);
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
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}  
        return true;
    }
    
	private IValue getStringLiteralSize() {
		char[] value= getValue();
		int length= value.length-1;
		boolean isRaw= false;
		for (int i = 0; i < length; i++) {
			final char c = value[i];
			if (c == '"') {
				if (isRaw) {
					for (int j = i + 1; j < length; j++) {
						final char d= value[j];
						if (d == '(') {
							length -= 2*(j-i);
							break;
						}
					}
				}
				length -= i;
				if (length < 0)
					length = 0;
				break;
			} else if (c == 'R') {
				isRaw = true;
			}
		}
		return Value.create(length);
	}

	private Kind getCharType() {
    	switch (getValue()[0]) {
    	case 'L':
    		return Kind.eWChar;
    	case 'u':
    		return Kind.eChar16;
    	case 'U':
    		return Kind.eChar32;
    	default:
    		return Kind.eChar;
    	}
    }
    
	private IType classifyTypeOfFloatLiteral() {
		final char[] lit= getValue();
		final int len= lit.length;
		Kind kind= Kind.eDouble;
		int flags= 0;
		if (len > 0) {
			switch (lit[len - 1]) {
			case 'f': case 'F':
				kind= Kind.eFloat;
				break;
			case 'l': case 'L':
				flags |= IBasicType.IS_LONG;
				break;
			}
		}
		return new CPPBasicType(kind, flags, this);
	}

	private IType classifyTypeOfIntLiteral() {
		int makelong= 0;
		boolean unsigned= false;
	
		final char[] lit= getValue();
		for (int i= lit.length - 1; i >= 0; i--) {
			final char c= lit[i];
			if (!(c > 'f' && c <= 'z') && !(c > 'F' && c <= 'Z')) {
				break;
			}
			switch (c) {
			case 'u':
			case 'U':
				unsigned = true;
				break;
			case 'l':
			case 'L':
				makelong++;
				break;
			}
		}

		int flags= 0;
		if (unsigned) {
			flags |= IBasicType.IS_UNSIGNED;
		}
		
		if (makelong > 1) {
			flags |= IBasicType.IS_LONG_LONG;
		} else if (makelong == 1) {
			flags |= IBasicType.IS_LONG;
		} 
		return new CPPBasicType(Kind.eInt, flags, this);
	}

    /**
     * @deprecated, use {@link #setValue(char[])}, instead.
     */
    @Override
	@Deprecated
	public void setValue(String value) {
        assertNotFrozen();
        this.value = value.toCharArray();
    }

    /**
     * @deprecated use {@link #CPPASTLiteralExpression(int, char[])}, instead.
     */
	@Deprecated
	public CPPASTLiteralExpression(int kind, String value) {
		this(kind, value.toCharArray());
	}
	
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null)
			fEvaluation= createEvaluation();
		return fEvaluation;
	}
	
	private ICPPEvaluation createEvaluation() {
    	switch (kind) {
    		case lk_this: {
    			IScope scope = CPPVisitor.getContainingScope(this);
    			IType type= CPPVisitor.getImpliedObjectType(scope);
    			if (type == null) 
    				return EvalFixed.INCOMPLETE;
    			return new EvalFixed(new CPPPointerType(type), PRVALUE, Value.UNKNOWN);
    		}
    		case lk_true:
    			return EVAL_TRUE;
    		case lk_false:
    			return EVAL_FALSE;
    		case lk_char_constant:
    			return new EvalFixed(new CPPBasicType(getCharType(), 0, this), PRVALUE, createCharValue());
    		case lk_float_constant: 
    			return new EvalFixed(classifyTypeOfFloatLiteral(), PRVALUE, Value.UNKNOWN);
    		case lk_integer_constant: 
    			return new EvalFixed(classifyTypeOfIntLiteral(), PRVALUE, createIntValue());
    		case lk_string_literal:
    			IType type = new CPPBasicType(getCharType(), 0, this);
    			type = new CPPQualifierType(type, true, false);
    			return new EvalFixed(new CPPArrayType(type, getStringLiteralSize()), LVALUE, Value.UNKNOWN);
    		case lk_nullptr:
    			return EVAL_NULL_PTR;
    	}
		return EvalFixed.INCOMPLETE;
	}

	private IValue createCharValue() {
		try {
			final char[] image= getValue();
			if (image.length > 1 && image[0] == 'L') 
				return Value.create(ExpressionEvaluator.getChar(image, 2));
			return Value.create(ExpressionEvaluator.getChar(image, 1));
		} catch (EvalException e1) {
			return Value.UNKNOWN;
		}
	}
	
	private IValue createIntValue() {
		try {
			return Value.create(ExpressionEvaluator.getNumber(getValue()));
		} catch (EvalException e1) {
			return Value.UNKNOWN;
		}
	}

	@Override
	public IType getExpressionType() {
		return getEvaluation().getTypeOrFunctionSet(this);
	}
	
	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	@Override
	public ValueCategory getValueCategory() {
		return getKind() == lk_string_literal ? LVALUE : PRVALUE;
	}
}
