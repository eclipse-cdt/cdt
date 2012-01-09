/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
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

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class CPPASTSimpleTypeConstructorExpression extends ASTNode implements
        ICPPASTSimpleTypeConstructorExpression {
	private ICPPASTDeclSpecifier fDeclSpec;
	private IASTInitializer fInitializer;
	private IType fType;

    public CPPASTSimpleTypeConstructorExpression() {
	}

	public CPPASTSimpleTypeConstructorExpression(ICPPASTDeclSpecifier declSpec, IASTInitializer init) {
		setDeclSpecifier(declSpec);
		setInitializer(init);
	}

	@Override
	public CPPASTSimpleTypeConstructorExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTSimpleTypeConstructorExpression copy(CopyStyle style) {
		CPPASTSimpleTypeConstructorExpression copy = new CPPASTSimpleTypeConstructorExpression();
		copy.setDeclSpecifier(fDeclSpec == null ? null : fDeclSpec.copy(style));
		copy.setInitializer(fInitializer == null ? null : fInitializer.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public ICPPASTDeclSpecifier getDeclSpecifier() {
		return fDeclSpec;
	}

	@Override
	public IASTInitializer getInitializer() {
		return fInitializer;
	}

	@Override
	public void setDeclSpecifier(ICPPASTDeclSpecifier declSpec) {
    	assertNotFrozen();
    	fDeclSpec = declSpec;
    	if (declSpec != null) {
    		declSpec.setParent(this);
    		declSpec.setPropertyInParent(TYPE_SPECIFIER);
    	}
    }

	@Override
	public void setInitializer(IASTInitializer initializer) {
    	assertNotFrozen();
    	fInitializer = initializer;
    	if (initializer != null) {
    		initializer.setParent(this);
    		initializer.setPropertyInParent(INITIALIZER);
    	}
    }

    @Override
	public IType getExpressionType() {
		if (fType == null) {
			fType= prvalueType(CPPVisitor.createType(fDeclSpec));
		}
		return fType;
	}

	@Override
	public boolean isLValue() {
		return false;
	}
	
	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
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

		if (fDeclSpec != null && !fDeclSpec.accept(action))
			return false;

		if (fInitializer != null && !fInitializer.accept(action))
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

    @Override
	@Deprecated
    public int getSimpleType() {
    	IType type= getExpressionType();
    	if (type instanceof ICPPBasicType) {
    		ICPPBasicType bt= (ICPPBasicType) type;
    		Kind kind = bt.getKind();
    		switch(kind) {
			case eBoolean:
				return t_bool;
			case eChar:
				return t_char;
			case eDouble:
				return t_double;
			case eFloat:
				return t_float;
			case eInt:
				if (bt.isShort())
					return t_short;
				if (bt.isLong())
					return t_long;
				if (bt.isSigned())
					return t_signed;
				if (bt.isUnsigned())
					return t_unsigned;
				return t_int;
			case eVoid:
				return t_void;
			case eWChar:
				return t_wchar_t;
			default:
				break;
    		}
    	}
		return t_unspecified;
    }
    
    @Override
	@Deprecated
    public void setSimpleType(int value) {
		CPPASTSimpleDeclSpecifier declspec = new CPPASTSimpleDeclSpecifier();
    	switch(value) {
    	case t_bool:
    		declspec.setType(Kind.eBoolean);
    		break;
    	case t_char:
    		declspec.setType(Kind.eChar);
    		break;
    	case t_double:
    		declspec.setType(Kind.eDouble);
    		break;
    	case t_float:
    		declspec.setType(Kind.eFloat);
    		break;
    	case t_int:
    		declspec.setType(Kind.eInt);
    		break;
    	case t_long:
    		declspec.setType(Kind.eInt);
    		declspec.setLong(true);
    		break;
    	case t_short:
    		declspec.setType(Kind.eInt);
    		declspec.setShort(true);
    		break;
    	case t_signed:
    		declspec.setType(Kind.eInt);
    		declspec.setSigned(true);
    		break;
    	case t_unsigned:
    		declspec.setType(Kind.eInt);
    		declspec.setUnsigned(true);
    		break;
    	case t_void:
    		declspec.setType(Kind.eVoid);
    		break;
    	case t_wchar_t:
    		declspec.setType(Kind.eWChar);
    		break;
    	default:
    		declspec.setType(Kind.eUnspecified);
    		break;
    	}
    	setDeclSpecifier(declspec);
    }
    
    @Override
	@Deprecated
    public IASTExpression getInitialValue() {
    	if (fInitializer instanceof ICPPASTConstructorInitializer) {
    		return ((ICPPASTConstructorInitializer) fInitializer).getExpression();
    	}
    	return null;
    }
    
    @Override
	@Deprecated
    public void setInitialValue(IASTExpression expression) {
    	ICPPASTConstructorInitializer init= new CPPASTConstructorInitializer();
    	init.setExpression(expression);
    	setInitializer(init);
    }
}
