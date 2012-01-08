/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;


public class CPPASTUsingDeclaration extends ASTNode
		implements ICPPASTUsingDeclaration, ICPPASTCompletionContext {

    private boolean typeName;
    private IASTName name;

    public CPPASTUsingDeclaration() {
	}

	public CPPASTUsingDeclaration(IASTName name) {
		setName(name);	
	}

	@Override
	public CPPASTUsingDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTUsingDeclaration copy(CopyStyle style) {
		CPPASTUsingDeclaration copy = new CPPASTUsingDeclaration(name == null ? null
				: name.copy(style));
		copy.typeName = typeName;
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public void setIsTypename(boolean value) {
        assertNotFrozen();
        this.typeName = value;
    }

    @Override
	public boolean isTypename() {
        return typeName;
    }

    @Override
	public IASTName getName() {
        return name;
    }

    @Override
	public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAME);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclarations) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if (name != null) if (!name.accept(action)) return false;
        
        if (action.shouldVisitDeclarations) {
		    switch(action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_declaration;
		return r_unclear;
	}
	
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
		List<IBinding> filtered = new ArrayList<IBinding>();
		
		for (IBinding binding : bindings) {
			if (binding instanceof ICPPNamespace) {
				filtered.add(binding);
			}
		}
		
		return filtered.toArray(new IBinding[filtered.size()]);
	}

	@Override
	public String toString() {
		return name.toString();
	}
	
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
