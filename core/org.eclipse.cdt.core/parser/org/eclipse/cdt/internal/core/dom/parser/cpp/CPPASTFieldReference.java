/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPFunctionSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalID;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalMemberAccess;

public class CPPASTFieldReference extends ASTNode
		implements ICPPASTFieldReference, IASTAmbiguityParent, ICPPASTCompletionContext {
    private boolean fIsTemplate;
    private boolean fIsDeref;
    private ICPPASTExpression fOwner;
    private IASTName fName;
    private IASTImplicitName[] fImplicitNames;
	private ICPPEvaluation fEvaluation;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

    public CPPASTFieldReference() {
	}
	
	public CPPASTFieldReference(IASTName name, IASTExpression owner) {
		setFieldName(name);
		setFieldOwner(owner);
	}

	@Override
	public CPPASTFieldReference copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFieldReference copy(CopyStyle style) {
		CPPASTFieldReference copy = new CPPASTFieldReference();
		copy.setFieldName(fName == null ? null : fName.copy(style));
		copy.setFieldOwner(fOwner == null ? null : fOwner.copy(style));
		copy.fIsTemplate = fIsTemplate;
		copy.fIsDeref = fIsDeref;
		return copy(copy, style);
	}

	@Override
	public boolean isTemplate() {
        return fIsTemplate;
    }

    @Override
	public void setIsTemplate(boolean value) {
        assertNotFrozen();
        fIsTemplate = value;
    }

    @Override
	public ICPPASTExpression getFieldOwner() {
        return fOwner;
    }

    @Override
	public void setFieldOwner(IASTExpression expression) {
        assertNotFrozen();
        fOwner = (ICPPASTExpression) expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FIELD_OWNER);
		}
    }

    @Override
	public IASTName getFieldName() {
        return fName;
    }

    @Override
	public void setFieldName(IASTName name) {
        assertNotFrozen();
        this.fName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
    }

    @Override
	public boolean isPointerDereference() {
        return fIsDeref;
    }

    @Override
	public void setIsPointerDereference(boolean value) {
        assertNotFrozen();
        fIsDeref = value;
    }
    
    @Override
	public IASTImplicitName[] getImplicitNames() {
    	if (fImplicitNames == null) {
    		if (!fIsDeref)
    			return fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
    		// Collect the function bindings
			List<ICPPFunction> functionBindings = new ArrayList<ICPPFunction>();
			EvalMemberAccess.getFieldOwnerType(fOwner.getExpressionType(), fIsDeref, this, functionBindings, false);
			if (functionBindings.isEmpty())
				return fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			// Create a name to wrap each binding
			fImplicitNames = new IASTImplicitName[functionBindings.size()];
			int i= -1;
			for (ICPPFunction op : functionBindings) {
				if (op != null && !(op instanceof CPPImplicitFunction)) {
					CPPASTImplicitName operatorName = new CPPASTImplicitName(OverloadableOperator.ARROW, this);
					operatorName.setBinding(op);
					operatorName.computeOperatorOffsets(fOwner, true);
					fImplicitNames[++i] = operatorName;
				}
			}
			fImplicitNames= ArrayUtil.trimAt(IASTImplicitName.class, fImplicitNames, i);
		}
		
		return fImplicitNames;
	}
        
	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getTemporariesDestructorCalls(this);
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
      
        if (fOwner != null && !fOwner.accept(action))
        	return false;
        
        if (action.shouldVisitImplicitNames) { 
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }
        
        if (fName != null && !fName.accept(action))
        	return false;

        if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
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
	public int getRoleForName(IASTName n) {
		if (n == fName)
			return r_reference;
		return r_unclear;
	}

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == fOwner) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            fOwner  = (ICPPASTExpression) other;
        }
    }

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
		List<IBinding> filtered = new ArrayList<IBinding>();

		for (IBinding binding : bindings) {
			if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				if (method.isImplicit()) {
					continue;
				}
			}
			filtered.add(binding);
		}
		
		return filtered.toArray(new IBinding[filtered.size()]);
	}
	
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
	
    /**
     * For a pointer dereference expression e1->e2, return the type that e1 ultimately evaluates to
     * after chaining overloaded class member access operators <code>operator->()</code> calls.
     */
    @Override
	public IType getFieldOwnerType() {
    	return EvalMemberAccess.getFieldOwnerType(fOwner.getExpressionType(), fIsDeref, this, null, true);
    }
    
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			fEvaluation= createEvaluation();
		}
		return fEvaluation;
	}
	
	private ICPPEvaluation createEvaluation() {
		ICPPEvaluation ownerEval = fOwner.getEvaluation();
		if (!ownerEval.isTypeDependent()) {
			IType ownerType= EvalMemberAccess.getFieldOwnerType(ownerEval.getTypeOrFunctionSet(this), fIsDeref, this, null, false);
			if (ownerType != null) {
				IBinding binding = fName.resolvePreBinding();
				if (binding instanceof CPPFunctionSet)
					binding= fName.resolveBinding();

				if (binding instanceof IProblemBinding || binding instanceof IType || binding instanceof ICPPConstructor) 
					return EvalFixed.INCOMPLETE;

				return new EvalMemberAccess(ownerType, ownerEval.getValueCategory(this), binding, fIsDeref, this);
			}
		}

		IBinding qualifier= null;
		ICPPTemplateArgument[] args= null;
		IASTName n= fName;
		if (n instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qn= (ICPPASTQualifiedName) n;
			ICPPASTNameSpecifier[] ns= qn.getQualifier();
			if (ns.length < 1)
				return EvalFixed.INCOMPLETE;
			qualifier= ns[ns.length - 1].resolveBinding();
			if (qualifier instanceof IProblemBinding)
				return EvalFixed.INCOMPLETE;
			n= qn.getLastName();
		}
		if (n instanceof ICPPASTTemplateId) {
			try {
				args= CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) n);
			} catch (DOMException e) {
				return EvalFixed.INCOMPLETE;
			}
		}		
		return new EvalID(ownerEval, qualifier, fName.getSimpleID(), false, true, args, this);
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
		return getEvaluation().getValueCategory(this);
	}
}
