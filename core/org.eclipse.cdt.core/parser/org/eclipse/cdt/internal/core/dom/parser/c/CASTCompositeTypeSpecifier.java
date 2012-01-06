/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Implementation for C composite specifiers.
 */
public class CASTCompositeTypeSpecifier extends CASTBaseDeclSpecifier implements
        ICASTCompositeTypeSpecifier, IASTAmbiguityParent {
    private int fKey;
    private IASTName fName;
    private IASTDeclaration[] fActiveDeclarations;
    private IASTDeclaration[] fAllDeclarations;
    private int fDeclarationsPos = -1;
    private IScope fScope;
    
    public CASTCompositeTypeSpecifier() {
	}

	public CASTCompositeTypeSpecifier(int key, IASTName name) {
		this.fKey = key;
		setName(name);
	}
    
	@Override
	public CASTCompositeTypeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CASTCompositeTypeSpecifier copy(CopyStyle style) {
		CASTCompositeTypeSpecifier copy = new CASTCompositeTypeSpecifier();
		copyCompositeTypeSpecifier(copy, style);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	protected void copyCompositeTypeSpecifier(CASTCompositeTypeSpecifier copy, CopyStyle style) {
		copyBaseDeclSpec(copy);
		copy.setKey(fKey);
		copy.setName(fName == null ? null : fName.copy(style));
		for (IASTDeclaration member : getMembers())
			copy.addMemberDeclaration(member == null ? null : member.copy(style));
	}
	
    @Override
	public int getKey() {
        return fKey;
    }

    @Override
	public void setKey(int key) {
        assertNotFrozen();
        this.fKey = key;
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
			name.setPropertyInParent(TYPE_NAME);
		}
    }

	@Override
	public IASTDeclaration[] getMembers() {
		IASTDeclaration[] active= fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fDeclarationsPos + 1);
			fActiveDeclarations= active;
		}
		return active;
	}

	@Override
	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations= ArrayUtil.trimAt(IASTDeclaration.class, fAllDeclarations,
					fDeclarationsPos);
			return fAllDeclarations;
		}
		return getMembers();
	}

    @Override
	public void addMemberDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
    	if (declaration != null) {
    		declaration.setParent(this);
    		declaration.setPropertyInParent(MEMBER_DECLARATION);
			fAllDeclarations = ArrayUtil.appendAt(IASTDeclaration.class, fAllDeclarations,
					++fDeclarationsPos, declaration);
			fActiveDeclarations= null;
    	}
    }
    
    @Override
	public void addDeclaration(IASTDeclaration declaration) {
    	addMemberDeclaration(declaration);
    }

    @Override
	public IScope getScope() {
        if (fScope == null)
            fScope = new CCompositeTypeScope(this);
        return fScope;
    }

    @Override
	public boolean accept(ASTVisitor action){
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
		if (fName != null && !fName.accept(action))
			return false;
           
		IASTDeclaration[] decls= getDeclarations(action.includeInactiveNodes);
		for (int i = 0; i < decls.length; i++) {
			if (!decls[i].accept(action))
				return false;
		}
        
		if (action.shouldVisitDeclSpecifiers) {
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
		if (n == this.fName)
			return r_definition;
		return r_unclear;
	}

    @Override
	public void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fDeclarationsPos; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations= null;
				return;
			}
		}
    }
}
