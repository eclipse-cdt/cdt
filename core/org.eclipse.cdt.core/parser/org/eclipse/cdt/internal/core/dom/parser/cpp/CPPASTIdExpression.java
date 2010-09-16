/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPFunctionSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTIdExpression extends ASTNode implements IASTIdExpression, ICPPASTCompletionContext {

	private static final ICPPASTFieldReference NOT_INITIALIZED = new CPPASTFieldReference();
	
	private IASTName name;
	private ICPPASTFieldReference fTransformedExpression= NOT_INITIALIZED;

    public CPPASTIdExpression() {
	}

	public CPPASTIdExpression(IASTName name) {
		setName(name);
	}

	public CPPASTIdExpression copy() {
		CPPASTIdExpression copy = new CPPASTIdExpression(name == null ? null : name.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ID_NAME);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default : break;
	        }
		}

        if (name != null && !name.accept(action)) return false;

        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default : break;
	        }
		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if (name == n) return r_reference;
		return r_unclear;
	}

	public IType getExpressionType() {
        IBinding binding = name.resolvePreBinding();
        if (binding instanceof CPPFunctionSet)
        	binding= name.resolveBinding();
        
        if (checkForTransformation(binding)) {
        	return fTransformedExpression.getExpressionType();
        }
        try {
			if (binding instanceof IProblemBinding) {
				return (IProblemBinding) binding;
			}
			if (binding instanceof IType || binding instanceof ICPPConstructor) {
				// mstodo return problem type
				return null;
			} 
			if (binding instanceof IEnumerator) {
				IType type= ((IEnumerator) binding).getType();
				if (type instanceof ICPPEnumeration) {
					ICPPEnumeration enumType= (ICPPEnumeration) type;
					if (enumType.asScope() == CPPVisitor.getContainingScope(this)) {
						// C++0x: 7.2-5
						IType fixedType= enumType.getFixedType();
						if (fixedType != null)
							return fixedType;
						// This is a simplification, the actual type is determined
						// - in an implementation dependent manner - by the value
						// of the enumerator.
						return CPPSemantics.INT_TYPE;
					}
				}
				return type;
			} 
			if (binding instanceof IVariable) {
				final IType t = glvalueType(((IVariable) binding).getType());
				return SemanticUtil.mapToAST(t, this);
			}
			if (binding instanceof IFunction) {
				return SemanticUtil.mapToAST(((IFunction) binding).getType(), this);
			} 
			if (binding instanceof ICPPTemplateNonTypeParameter) {
				return prvalueType(((ICPPTemplateNonTypeParameter) binding).getType());
			} 
			if (binding instanceof ICPPUnknownBinding) {
				// mstodo typeof unknown binding
				return CPPUnknownClass.createUnnamedInstance();
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		// mstodo return problem type
		return null;
	}

	/**
	 * 9.3.1-3 Transformation to class member access within the definition of a non-static 
	 * member function. 
	 */
	public boolean checkForTransformation(IBinding binding) {
		if (fTransformedExpression == NOT_INITIALIZED) {
			fTransformedExpression= null;
			if (name instanceof ICPPASTQualifiedName) {
				IASTNode parent= name.getParent();
				if (parent instanceof ICPPASTUnaryExpression) {
					if (((ICPPASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_amper) {
						return false;
					}
				}
			}
			if (binding instanceof ICPPMember && !(binding instanceof IType) && !(binding instanceof ICPPConstructor)
					&&!((ICPPMember) binding).isStatic()) {
				IASTNode parent= getParent();
				while (parent != null && !(parent instanceof ICPPASTFunctionDefinition)) {
					parent= parent.getParent();
				}
				if (parent instanceof ICPPASTFunctionDefinition) {
					ICPPASTFunctionDefinition fdef= (ICPPASTFunctionDefinition) parent;
					final IBinding methodBinding = fdef.getDeclarator().getName().resolvePreBinding();
					if (methodBinding instanceof ICPPMethod && !((ICPPMethod) methodBinding).isStatic()) {
						IASTName nameDummy= new CPPASTName();
						nameDummy.setBinding(binding);
						IASTExpression owner= new CPPASTLiteralExpression(IASTLiteralExpression.lk_this, CharArrayUtils.EMPTY);
						owner= new CPPASTUnaryExpression(IASTUnaryExpression.op_star, owner);
						fTransformedExpression= new CPPASTFieldReference(nameDummy, owner);
						fTransformedExpression.setParent(getParent());
						fTransformedExpression.setPropertyInParent(getPropertyInParent());
					}
				}
			}
		}
		
		return fTransformedExpression != null;
	}

	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
	
	public ValueCategory getValueCategory() {
        IBinding binding = name.resolvePreBinding();
        if (checkForTransformation(binding)) {
			return fTransformedExpression.getValueCategory();
		}
        if (binding instanceof ICPPTemplateNonTypeParameter)
        	return ValueCategory.PRVALUE;
        
		if (binding instanceof IVariable || binding instanceof IFunction) {
			return ValueCategory.LVALUE;
		}
		return ValueCategory.PRVALUE;
	}

	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		return CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
	}

	@Override
	public String toString() {
		return name != null ? name.toString() : "<unnamed>"; //$NON-NLS-1$
	}
	
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
