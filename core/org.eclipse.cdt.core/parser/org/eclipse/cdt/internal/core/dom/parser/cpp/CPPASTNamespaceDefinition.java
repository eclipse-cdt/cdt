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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Definition of a namespace.
 */
public class CPPASTNamespaceDefinition extends ASTNode
		implements ICPPASTNamespaceDefinition, IASTAmbiguityParent {
    private IASTName fName;
	private IASTDeclaration[] fAllDeclarations;
	private IASTDeclaration[] fActiveDeclarations;
    private int fLastDeclaration= -1;
    private boolean fIsInline;

    public CPPASTNamespaceDefinition() {
	}

	public CPPASTNamespaceDefinition(IASTName name) {
		setName(name);
	}

	@Override
	public CPPASTNamespaceDefinition copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTNamespaceDefinition copy(CopyStyle style) {
		CPPASTNamespaceDefinition copy =
				new CPPASTNamespaceDefinition(fName == null ? null : fName.copy(style));
		copy.fIsInline = fIsInline;
		for (IASTDeclaration declaration : getDeclarations()) {
			copy.addDeclaration(declaration == null ? null : declaration.copy(style));
		}
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTName getName() {
        return fName;
    }

    @Override
	public void setName(IASTName name) {
        assertNotFrozen();
        this.fName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(NAMESPACE_NAME);
		}
    }
    
	@Override
	public void setIsInline(boolean isInline) {
		assertNotFrozen();
		fIsInline= isInline;
	}

	@Override
	public boolean isInline() {
		return fIsInline;
	}

	@Override
	public final void addDeclaration(IASTDeclaration decl) {
		if (decl != null) {
			decl.setParent(this);
			decl.setPropertyInParent(OWNED_DECLARATION);
			fAllDeclarations = ArrayUtil.appendAt(IASTDeclaration.class, fAllDeclarations, ++fLastDeclaration, decl);
			fActiveDeclarations= null;
		}
	}

	@Override
	public final IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] active= fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fLastDeclaration+1);
			fActiveDeclarations= active;
		}
		return active;
	}

	@Override
	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations= ArrayUtil.trimAt(IASTDeclaration.class, fAllDeclarations, fLastDeclaration);
			return fAllDeclarations;
		}
		return getDeclarations();
	}

    @Override
	public IScope getScope() {
	    return ((ICPPNamespace) fName.resolveBinding()).getNamespaceScope();
	}

    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitNamespaces) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
		if (fName != null && !fName.accept(action))
			return false;
		
        IASTDeclaration [] decls = getDeclarations(action.includeInactiveNodes);
		for (IASTDeclaration decl : decls) {
			if (!decl.accept(action))
				return false;
		}

		if (action.shouldVisitNamespaces && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		if (fName == n)
			return r_definition;
		return r_unclear;
	}

    @Override
	public void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fLastDeclaration; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations= null;
				break;
			}
		}
    }
}
