/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTSimpleDeclSpecifier extends CPPASTBaseDeclSpecifier implements ICPPASTSimpleDeclSpecifier,
		IASTAmbiguityParent {
    private int type;
    private boolean isSigned;
    private boolean isUnsigned;
    private boolean isShort;
    private boolean isLong;
    private boolean isLonglong;
    private boolean isComplex=false;
    private boolean isImaginary=false;
	private IASTExpression fDeclTypeExpression;

    @Override
	public CPPASTSimpleDeclSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
    
	@Override
	public CPPASTSimpleDeclSpecifier copy(CopyStyle style) {
		CPPASTSimpleDeclSpecifier copy = new CPPASTSimpleDeclSpecifier();
		copySimpleDeclSpec(copy, style);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	protected void copySimpleDeclSpec(CPPASTSimpleDeclSpecifier other, CopyStyle style) {
    	copyBaseDeclSpec(other);
    	other.type = type;
    	other.isSigned = isSigned;
    	other.isUnsigned = isUnsigned;
    	other.isShort = isShort;
    	other.isLong = isLong;
    	other.isLonglong= isLonglong;
    	other.isComplex= isComplex;
    	other.isImaginary= isImaginary;
    	if (fDeclTypeExpression != null) {
			other.setDeclTypeExpression(fDeclTypeExpression.copy(style));
    	}
    }

	/**
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier
     */
    @Override
	public int getType() {
        return type;
    }

    @Override
	public void setType(int type) {
        assertNotFrozen();
        this.type = type;
    }

    @Override
	public void setType(Kind kind) {
    	setType(getType(kind));
    }
    
    private int getType(Kind kind) {
    	switch(kind) {
    	case eBoolean:
    		return t_bool;
		case eChar:
			return t_char;
		case eWChar:
			return t_wchar_t;
		case eChar16:
			return t_char16_t;
		case eChar32:
			return t_char32_t;
		case eDouble:
			return t_double;
		case eFloat:
			return t_float;
		case eInt:
			return t_int;
		case eUnspecified:
			return t_unspecified;
		case eVoid:
			return t_void;
    	}
    	return t_unspecified;
    }
    
    @Override
	public boolean isSigned() {
        return isSigned;
    }

    @Override
	public boolean isUnsigned() {
        return isUnsigned;
    }

    @Override
	public boolean isShort() {
        return isShort;
    }

    @Override
	public boolean isLong() {
        return isLong;
    }

    @Override
	public boolean isLongLong() {
		return isLonglong;
	}

	@Override
	public boolean isComplex() {
		return isComplex;
	}

	@Override
	public boolean isImaginary() {
		return isImaginary;
	}

	@Override
	public IASTExpression getDeclTypeExpression() {
		return fDeclTypeExpression;
	}

	@Override
	public void setSigned(boolean value) {
        assertNotFrozen();
        isSigned = value;
    }

    @Override
	public void setUnsigned(boolean value) {
        assertNotFrozen();
        isUnsigned = value;
    }

    @Override
	public void setLong(boolean value) {
        assertNotFrozen();
        isLong = value;
    }

    @Override
	public void setShort(boolean value) {
        assertNotFrozen();
        isShort = value;
    }

    @Override
	public void setLongLong(boolean value) {
        assertNotFrozen();
        isLonglong = value;
	}

	@Override
	public void setComplex(boolean value) {
        assertNotFrozen();
        isComplex = value;
	}

	@Override
	public void setImaginary(boolean value) {
        assertNotFrozen();
        isImaginary = value;
	}

	@Override
	public void setDeclTypeExpression(IASTExpression expression) {
        assertNotFrozen();
        fDeclTypeExpression = expression;
        if (expression != null) {
        	expression.setPropertyInParent(DECLTYPE_EXPRESSION);
        	expression.setParent(this);
        }
	}

	@Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

		if (fDeclTypeExpression != null && !fDeclTypeExpression.accept(action))
			return false;

        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fDeclTypeExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fDeclTypeExpression= (IASTExpression) other;
		}
	}
}
