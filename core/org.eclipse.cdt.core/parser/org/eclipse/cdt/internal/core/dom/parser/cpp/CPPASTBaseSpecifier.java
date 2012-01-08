/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Base class specifier
 */
public class CPPASTBaseSpecifier extends ASTNode implements ICPPASTBaseSpecifier, ICPPASTCompletionContext {

    private boolean isVirtual;
    private int visibility;
    private IASTName name;
	private boolean fIsPackExpansion;

    
    public CPPASTBaseSpecifier() {
	}
    
    public CPPASTBaseSpecifier(IASTName name) {
		setName(name);
	}

	public CPPASTBaseSpecifier(IASTName name, int visibility, boolean isVirtual) {
		this.isVirtual = isVirtual;
		this.visibility = visibility;
		setName(name);
	}

	@Override
	public CPPASTBaseSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTBaseSpecifier copy(CopyStyle style) {
		CPPASTBaseSpecifier copy = new CPPASTBaseSpecifier(name == null ? null : name.copy(style));
		copy.isVirtual = isVirtual;
		copy.visibility = visibility;
		copy.fIsPackExpansion= fIsPackExpansion;
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
	@Override
	public boolean isVirtual() {
        return isVirtual;
    }

    @Override
	public void setVirtual(boolean value) {
        assertNotFrozen();
        isVirtual = value;
    }

    @Override
	public int getVisibility() {
        return visibility;
    }

    @Override
	public void setVisibility(int visibility) {
        assertNotFrozen();
        this.visibility = visibility;
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
        if (action.shouldVisitBaseSpecifiers) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

		if (name != null && !name.accept(action))
			return false;

		if (action.shouldVisitBaseSpecifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		if (name == n) return r_reference;
		return r_unclear;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
		List<IBinding> filtered = new ArrayList<IBinding>();

		ICPPClassType classType = null;
		if (getParent() instanceof CPPASTCompositeTypeSpecifier) {
			IASTName className = ((CPPASTCompositeTypeSpecifier) getParent()).getName();
			IBinding binding = className.resolveBinding();
			if (binding instanceof ICPPClassType) {
				classType = (ICPPClassType) binding;
			}
		}

		for (IBinding binding : bindings) {
			if (binding instanceof ICPPClassType) {
				ICPPClassType base = (ICPPClassType) binding;
				int key = base.getKey();
				if (key == ICPPClassType.k_class &&
						(classType == null || !base.isSameType(classType))) {
					filtered.add(base);
				}
			}
		}

		return filtered.toArray(new IBinding[filtered.size()]);
	}

	@Override
	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	@Override
	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion= val;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
