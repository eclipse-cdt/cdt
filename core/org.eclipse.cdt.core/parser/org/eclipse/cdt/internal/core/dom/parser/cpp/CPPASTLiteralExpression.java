/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Represents a c++ literal.
 */
public class CPPASTLiteralExpression extends ASTNode implements ICPPASTLiteralExpression {

    private int kind;
    private char[] value = CharArrayUtils.EMPTY;

    public CPPASTLiteralExpression() {
	}

	public CPPASTLiteralExpression(int kind, char[] value) {
		this.kind = kind;
		this.value = value;
	}

	public CPPASTLiteralExpression copy() {
		CPPASTLiteralExpression copy = new CPPASTLiteralExpression(kind, value == null ? null : value.clone());
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
    		case lk_this: {
    			IScope scope = CPPVisitor.getContainingScope(this);
    			return CPPVisitor.getThisType(scope);
    		}
    		case lk_true:
    		case lk_false:
    			return new CPPBasicType(ICPPBasicType.t_bool, 0, this);
    		case lk_char_constant:
    			return new CPPBasicType(IBasicType.t_char, 0, this);
    		case lk_float_constant: 
    			return classifyTypeOfFloatLiteral();
    		case lk_integer_constant: 
    			return classifyTypeOfIntLiteral();
    		case lk_string_literal:
    			IType type = new CPPBasicType(IBasicType.t_char, 0, this);
    			type = new CPPQualifierType(type, true, false);
    			return new CPPArrayType(type);
    	}
    	return null;
    }
    
	private IType classifyTypeOfFloatLiteral() {
		final char[] lit= getValue();
		final int len= lit.length;
		int kind= IBasicType.t_double;
		int flags= 0;
		if (len > 0) {
			switch (lit[len - 1]) {
			case 'f': case 'F':
				kind= IBasicType.t_float;
				break;
			case 'l': case 'L':
				flags |= ICPPBasicType.IS_LONG;
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
			flags |= ICPPBasicType.IS_UNSIGNED;
		}
		
		if (makelong > 1) {
			flags |= ICPPBasicType.IS_LONG_LONG;
			GPPBasicType result = new GPPBasicType(IBasicType.t_int, flags, null);
			result.setFromExpression(this);
			return result;
		} 
		
		if (makelong == 1) {
			flags |= ICPPBasicType.IS_LONG;
		} 
		return new CPPBasicType(IBasicType.t_int, flags, this);
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
     * @deprecated use {@link #CPPASTLiteralExpression(int, char[])}, instead.
     */
	@Deprecated
	public CPPASTLiteralExpression(int kind, String value) {
		this(kind, value.toCharArray());
	}
}
