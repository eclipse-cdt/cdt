/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;
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

	public CPPASTBaseSpecifier copy() {
		CPPASTBaseSpecifier copy = new CPPASTBaseSpecifier(name == null ? null : name.copy());
		copy.isVirtual = isVirtual;
		copy.visibility = visibility;
		copy.fIsPackExpansion= fIsPackExpansion;
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean value) {
        assertNotFrozen();
        isVirtual = value;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        assertNotFrozen();
        this.visibility = visibility;
    }

    public IASTName getName() {
        return name;
    }

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
        if (action.shouldVisitBaseSpecifiers && action instanceof ICPPASTVisitor) {
		    switch (((ICPPASTVisitor)action).visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

        if (!name.accept(action)) return false;

        if (action.shouldVisitBaseSpecifiers && action instanceof ICPPASTVisitor) {
    		    switch (((ICPPASTVisitor)action).leave(this)) {
    	            case ASTVisitor.PROCESS_ABORT : return false;
    	            case ASTVisitor.PROCESS_SKIP  : return true;
    	            default : break;
    	        }
    		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if (name == n) return r_reference;
		return r_unclear;
	}

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
				try {
					int key = base.getKey();
					if (key == ICPPClassType.k_class &&
							(classType == null || !base.isSameType(classType))) {
						filtered.add(base);
					}
				} catch (DOMException e) {
				}
			}
		}

		return filtered.toArray(new IBinding[filtered.size()]);
	}

	public boolean isPackExpansion() {
		return fIsPackExpansion;
	}

	public void setIsPackExpansion(boolean val) {
		assertNotFrozen();
		fIsPackExpansion= val;
	}

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
}
