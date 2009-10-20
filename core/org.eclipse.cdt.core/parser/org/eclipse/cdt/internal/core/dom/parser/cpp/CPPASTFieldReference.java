/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * @author jcamelon
 */
public class CPPASTFieldReference extends ASTNode implements
        ICPPASTFieldReference, IASTAmbiguityParent, IASTCompletionContext, IASTImplicitNameOwner {

    private boolean isTemplate;
    private IASTExpression owner;
    private IASTName name;
    private boolean isDeref;
    
    private IASTImplicitName[] implicitNames = null;
    
    public CPPASTFieldReference() {
	}
	
	public CPPASTFieldReference(IASTName name, IASTExpression owner) {
		setFieldName(name);
		setFieldOwner(owner);
	}
	
	public CPPASTFieldReference copy() {
		CPPASTFieldReference copy = new CPPASTFieldReference();
		copy.setFieldName(name == null ? null : name.copy());
		copy.setFieldOwner(owner == null ? null : owner.copy());
		copy.isTemplate = isTemplate;
		copy.isDeref = isDeref;
		copy.setOffsetAndLength(this);
		return copy;
	}

	public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean value) {
        assertNotFrozen();
        isTemplate = value;
    }

    public IASTExpression getFieldOwner() {
        return owner;
    }

    public void setFieldOwner(IASTExpression expression) {
        assertNotFrozen();
        owner = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FIELD_OWNER);
		}
    }

    public IASTName getFieldName() {
        return name;
    }

    public void setFieldName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
    }

    public boolean isPointerDereference() {
        return isDeref;
    }

    public void setIsPointerDereference(boolean value) {
        assertNotFrozen();
        isDeref = value;
    }
    
    public IASTImplicitName[] getImplicitNames() {
    	if(implicitNames == null) {
    		if(!isDeref)
    			return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
    		// collect the function bindings
			List<ICPPFunction> functionBindings = new ArrayList<ICPPFunction>();
			try {
				CPPSemantics.getChainedMemberAccessOperatorReturnType(this, functionBindings);
			} catch (DOMException e) {
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			}
			if(functionBindings.isEmpty())
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			// create a name to wrap each binding
			implicitNames = new IASTImplicitName[functionBindings.size()];
			for(int i = 0, n = functionBindings.size(); i < n; i++) {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(OverloadableOperator.ARROW, this);
				operatorName.setBinding(functionBindings.get(i));
				operatorName.computeOperatorOffsets(owner, true);
				implicitNames[i] = operatorName;
			}
		}
		
		return implicitNames;
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
      
        if (owner != null && !owner.accept(action)) return false;
        
        if(action.shouldVisitImplicitNames) { 
        	for(IASTImplicitName name : getImplicitNames()) {
        		if(!name.accept(action)) return false;
        	}
        }
        
        if (name != null && !name.accept(action)) return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_reference;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if (child == owner) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            owner  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
		IASTName name= getFieldName();
		IBinding binding = name.resolvePreBinding();
		try {
			if (binding instanceof IVariable) {
                return SemanticUtil.mapToAST(((IVariable) binding).getType(), this);
			} else if (binding instanceof IEnumerator) {
				return ((IEnumerator) binding).getType();
			} else if (binding instanceof IFunction) {
				return SemanticUtil.mapToAST(((IFunction) binding).getType(), this);
			}  else if (binding instanceof ICPPUnknownBinding) {
				return CPPUnknownClass.createUnnamedInstance();
			} else if (binding instanceof IProblemBinding) {
				return (IType) binding;
			} 
	    } catch (DOMException e) {
	        return e.getProblem();
        }
	    return null;
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
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
}
