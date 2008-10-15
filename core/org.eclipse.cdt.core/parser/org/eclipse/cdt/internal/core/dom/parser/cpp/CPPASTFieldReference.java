/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author jcamelon
 */
public class CPPASTFieldReference extends ASTNode implements
        ICPPASTFieldReference, IASTAmbiguityParent, IASTCompletionContext {

    private boolean isTemplate;
    private IASTExpression owner;
    private IASTName name;
    private boolean isDeref;

    
    public CPPASTFieldReference() {
	}

	public CPPASTFieldReference(IASTName name, IASTExpression owner, boolean isDeref, boolean isTemplate) {
		setFieldName(name);
		setFieldOwner(owner);
		this.isTemplate = isTemplate;
		this.isDeref = isDeref;
	}
	
	public CPPASTFieldReference(IASTName name, IASTExpression owner) {
		this(name, owner, false, false);
	}

	public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean value) {
        isTemplate = value;
    }

    public IASTExpression getFieldOwner() {
        return owner;
    }

    public void setFieldOwner(IASTExpression expression) {
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
        isDeref = value;
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
    	return CPPVisitor.getExpressionType(this);
    }

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix);
		List<IBinding> filtered = new ArrayList<IBinding>();
		
		for (int i = 0; i < bindings.length; i++) {
			if (bindings[i] instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) bindings[i];
				if (method.isImplicit()) {
					continue;
				}
			}
			filtered.add(bindings[i]);
		}
		
		return filtered.toArray(new IBinding[filtered.size()]);
	}
}
