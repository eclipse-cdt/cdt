/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public abstract class CPPUnknownBinding extends PlatformObject
		implements ICPPInternalUnknown, Cloneable {
    private ICPPScope unknownScope;
    protected ICPPInternalUnknown scopeBinding;
    protected IASTName name;

    public CPPUnknownBinding(ICPPInternalUnknown scopeBinding, IASTName name) {
        super();
        this.name = name;
        this.scopeBinding = scopeBinding;
    }

    public IASTNode[] getDeclarations() {
        return null;
    }

    public IASTNode getDefinition() {
        return null;
    }

    public void addDefinition(IASTNode node) {
    }

    public void addDeclaration(IASTNode node) {
    }

    public void removeDeclaration(IASTNode node) {
    }

    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    public char[][] getQualifiedNameCharArray() {
    	return CPPVisitor.getQualifiedNameCharArray(this);
    }

    public boolean isGloballyQualified() {
        return false;
    }

    public String getName() {
        return name.toString();
    }

    public char[] getNameCharArray() {
        return name.toCharArray();
    }

    public IScope getScope() {
        return scopeBinding.getUnknownScope();
    }

    public ICPPScope getUnknownScope() {
        if (unknownScope == null) {
            unknownScope = new CPPUnknownScope(this, name);
        }
        return unknownScope;
    }

    public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
        IBinding result = this;
        IType t = null;
		if (scopeBinding instanceof ICPPTemplateTypeParameter) {
			t = CPPTemplates.instantiateType((ICPPTemplateTypeParameter) scopeBinding, argMap);
		} else if (scopeBinding instanceof ICPPInternalUnknownClassType) {
        	IBinding binding = ((ICPPInternalUnknownClassType) scopeBinding).resolveUnknown(argMap);
        	if (binding instanceof IType) {
                t = (IType) binding;
            }
        }
        if (t != null) {
            t = SemanticUtil.getUltimateType(t, false);
	        if (t instanceof ICPPClassType) {
	            IScope s = ((ICPPClassType) t).getCompositeScope();
	            if (s != null && ASTInternal.isFullyCached(s)) {
	            	// If name did not come from an AST but was created just to encapsulate
	            	// a simple identifier, we should not use getBinding method since it may
	            	// lead to a NullPointerException.
	            	if (name.getParent() != null) {
	            		result = s.getBinding(name, true);
	            	} else {
		            	IBinding[] bindings = s.find(name.toString());
		            	if (bindings != null && bindings.length > 0) {
		            		result = bindings[0];
		            	}
	            	}
	            }
	        } else if (t instanceof ICPPInternalUnknown) {
	            result = resolvePartially((ICPPInternalUnknown) t, argMap);
	        }
        }
        return result;
    }


    protected abstract IBinding resolvePartially(ICPPInternalUnknown parentBinding, ObjectMap argMap);
    
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	@Override
	public CPPUnknownBinding clone() {
		try {
			return (CPPUnknownBinding) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;  // Never happens
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
