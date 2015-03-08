/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;

public class CPPASTSimpleTypeConstructorExpression extends ASTNode
		implements ICPPASTSimpleTypeConstructorExpression {
	private ICPPASTDeclSpecifier fDeclSpec;
	private IASTInitializer fInitializer;
	private ICPPEvaluation fEvaluation;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

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
		return copy(copy, style);
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
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			final IType type = CPPVisitor.createType(fDeclSpec);
			ICPPEvaluation[] args= null;
			if (fInitializer instanceof ICPPASTConstructorInitializer) {
				IASTInitializerClause[] a = ((ICPPASTConstructorInitializer) fInitializer).getArguments();
				args= new ICPPEvaluation[a.length];
				for (int i = 0; i < a.length; i++) {
					args[i]= ((ICPPASTInitializerClause) a[i]).getEvaluation();
				}
				fEvaluation= new EvalTypeId(type, this, args);
			} else if (fInitializer instanceof ICPPASTInitializerList) {
				fEvaluation= new EvalTypeId(type, this,
						((ICPPASTInitializerList) fInitializer).getEvaluation());
			} else {
				fEvaluation= EvalFixed.INCOMPLETE;
			}
		}
		return fEvaluation;
	}

    @Override
	public IType getExpressionType() {
    	return getEvaluation().getTypeOrFunctionSet(this);
    }
    
	@Override
	public ValueCategory getValueCategory() {
    	return getEvaluation().getValueCategory(this);
	}

	@Override
	public boolean isLValue() {
		return false;
	}
	
	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = CPPVisitor.getTemporariesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
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
        
        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(fImplicitDestructorNames, action))
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

	@Deprecated
    @Override
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
    
	@Deprecated
    @Override
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
    
	@Deprecated
    @Override
    public IASTExpression getInitialValue() {
    	if (fInitializer instanceof ICPPASTConstructorInitializer) {
    		return ((ICPPASTConstructorInitializer) fInitializer).getExpression();
    	}
    	return null;
    }
    
	@Deprecated
    @Override
    public void setInitialValue(IASTExpression expression) {
    	ICPPASTConstructorInitializer init= new CPPASTConstructorInitializer();
    	init.setExpression(expression);
    	setInitializer(init);
    }
}
