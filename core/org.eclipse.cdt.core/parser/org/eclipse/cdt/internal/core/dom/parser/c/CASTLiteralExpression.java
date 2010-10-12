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
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Represents a literal
 */
public class CASTLiteralExpression extends ASTNode implements IASTLiteralExpression {

    private int kind;
    private char[] value = CharArrayUtils.EMPTY;

    public CASTLiteralExpression() {
	}

	public CASTLiteralExpression(int kind, char[] value) {
		this.kind = kind;
		this.value = value;
	}
	
	public CASTLiteralExpression copy() {
		CASTLiteralExpression copy = new CASTLiteralExpression(kind, value == null ? null : value.clone());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public int getKind() {
        return kind;
    }

    public void setKind(int value) {
        assertNotFrozen();
        kind = value;
    }

    public char[] getValue() {
    	return value;
    }

    public void setValue(char[] value) {
        assertNotFrozen();
    	this.value= value;
    }
    
    @Override
	public String toString() {
        return new String(value);
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
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}      
        return true;
    }
    
	public IType getExpressionType() {
		switch (getKind()) {
		case IASTLiteralExpression.lk_char_constant:
			return new CBasicType(IBasicType.Kind.eChar, 0, this);
		case IASTLiteralExpression.lk_float_constant:
			return classifyTypeOfFloatLiteral();
		case IASTLiteralExpression.lk_integer_constant:
			return classifyTypeOfIntLiteral();
		case IASTLiteralExpression.lk_string_literal:
			IType type = new CBasicType(IBasicType.Kind.eChar, 0, this);
			type = new CQualifierType(type, true, false, false);
			return new CPointerType(type, 0);
		}
		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	}
	
	public boolean isLValue() {
		return getKind() == IASTLiteralExpression.lk_string_literal;
	}

	public final ValueCategory getValueCategory() {
		return isLValue() ? ValueCategory.LVALUE : ValueCategory.PRVALUE;
	}

	private IType classifyTypeOfFloatLiteral() {
		final char[] lit= getValue();
		final int len= lit.length;
		IBasicType.Kind kind= IBasicType.Kind.eDouble;
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
		
		return new CBasicType(kind, flags, this);
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
		return new CBasicType(IBasicType.Kind.eInt, flags, this);
	}
	
	
    /**
     * @deprecated, use {@link #setValue(char[])}, instead.
     */
    @Deprecated
	public void setValue(String value) {
        assertNotFrozen();
        this.value = value.toCharArray();
    }
    
    /**
     * @deprecated use {@link #CASTLiteralExpression(int, char[])}, instead.
     */
	@Deprecated
	public CASTLiteralExpression(int kind, String value) {
		this(kind, value.toCharArray());
	}
}
