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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.PTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTIdExpression extends ASTNode implements IASTIdExpression, ICPPASTCompletionContext {

	private IASTName name;

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
        try {
			if (binding instanceof IVariable) {
				final IVariable var = (IVariable) binding;
				IType type= SemanticUtil.mapToAST(var.getType(), this);
				if (var instanceof ICPPField && !var.isStatic()) {
					IScope scope= CPPVisitor.getContainingScope(name);
					if (scope != null) {
						IType thisType= CPPVisitor.getThisType(scope);
						if (thisType != null) {
							thisType= SemanticUtil.getNestedType(thisType, TDEF|REF|PTR);
							type= CPPASTFieldReference.addQualifiersForAccess((ICPPField) var, type, thisType);
						}
					}
				}
				return type;
			} else if (binding instanceof IEnumerator) {
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
			} else if (binding instanceof IProblemBinding) {
				return (IType) binding;
			} else if (binding instanceof IFunction) {
				return SemanticUtil.mapToAST(((IFunction) binding).getType(), this);
			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
				return ((ICPPTemplateNonTypeParameter) binding).getType();
			} else if (binding instanceof ICPPClassType) {
				return ((ICPPClassType) binding);
			} else if (binding instanceof ICPPUnknownBinding) {
				return CPPUnknownClass.createUnnamedInstance();
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		return null;
	}

	public boolean isLValue() {
		IBinding b= getName().resolveBinding();
		if (b instanceof IVariable || b instanceof IFunction) {
			return true;
		}
		return false;
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
