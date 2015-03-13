/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

/**
 * Base for all c++ declaration specifiers.
 */
public abstract class CPPASTBaseDeclSpecifier extends ASTAttributeOwner implements ICPPASTDeclSpecifier,
		IASTAmbiguityParent {
    private boolean explicit;
    private boolean friend;
    private boolean inline;
    private boolean isConst;
    private boolean isConstexpr;
    private boolean isRestrict;
    private boolean isThreadLocal;
    private boolean isVolatile;
    private int sc;
    private boolean virtual;
    private IASTAlignmentSpecifier[] alignmentSpecifiers = 
    		IASTAlignmentSpecifier.EMPTY_ALIGNMENT_SPECIFIER_ARRAY;
    
    @Override
	public boolean isFriend() {
        return friend;
    }

    @Override
	public int getStorageClass() {
        return sc;
    }

    @Override
	public void setStorageClass(int storageClass) {
        assertNotFrozen();
        sc = storageClass;
    }

    @Override
	public boolean isThreadLocal() {
        return isThreadLocal;
    }

    @Override
	public void setThreadLocal(boolean value) {
        assertNotFrozen();
        isThreadLocal = value;
    }

    @Override
	public boolean isConst() {
        return isConst;
    }

    @Override
	public void setConst(boolean value) {
        assertNotFrozen();
        isConst = value;
    }

    @Override
	public boolean isConstexpr() {
        return isConstexpr;
    }

    @Override
	public void setConstexpr(boolean value) {
        assertNotFrozen();
        isConstexpr = value;
    }

    @Override
	public boolean isVolatile() {
        return isVolatile;
    }

    @Override
	public void setVolatile(boolean value) {
        assertNotFrozen();
        isVolatile = value;
    }

    @Override
	public boolean isRestrict() {
        return isRestrict;
    }

    @Override
	public void setRestrict(boolean value) {
        assertNotFrozen();
        isRestrict = value;
    }

    @Override
	public boolean isInline() {
        return inline;
    }

    @Override
	public void setInline(boolean value) {
        assertNotFrozen();
        this.inline = value;
    }

    @Override
	public void setFriend(boolean value) {
        assertNotFrozen();
        friend = value;
    }

    @Override
	public boolean isVirtual() {
        return virtual;
    }

    @Override
	public void setVirtual(boolean value) {
        assertNotFrozen();
        virtual = value;
    }

    @Override
	public boolean isExplicit() {
        return explicit;
    }

    @Override
	public void setExplicit(boolean value) {
        assertNotFrozen();
        this.explicit = value;
    }
    
    @Override
    public IASTAlignmentSpecifier[] getAlignmentSpecifiers() {
    	return alignmentSpecifiers;
    }
    
    @Override
    public void setAlignmentSpecifiers(IASTAlignmentSpecifier[] alignmentSpecifiers) {
    	assertNotFrozen();
    	for (IASTAlignmentSpecifier specifier : alignmentSpecifiers) {
    		specifier.setParent(this);
    		specifier.setPropertyInParent(ALIGNMENT_SPECIFIER);
    	}
    	this.alignmentSpecifiers = alignmentSpecifiers;
    }
    
	protected <T extends CPPASTBaseDeclSpecifier> T copy(T copy, CopyStyle style) {
		CPPASTBaseDeclSpecifier target = copy;
    	target.explicit = explicit;
    	target.friend = friend;
    	target.inline = inline;
    	target.isConst = isConst;
    	target.isConstexpr = isConstexpr;
    	target.isRestrict= isRestrict;
    	target.isThreadLocal = isThreadLocal;
    	target.isVolatile = isVolatile;
    	target.sc = sc;
    	target.virtual = virtual;
    	target.alignmentSpecifiers = new IASTAlignmentSpecifier[alignmentSpecifiers.length];
    	for (int i = 0; i < alignmentSpecifiers.length; ++i) {
    		target.alignmentSpecifiers[i] = alignmentSpecifiers[i].copy(style);
    		target.alignmentSpecifiers[i].setParent(target);
    	}
		return super.copy(copy, style);
	}

	/**
	 * Provided for debugging purposes, only.
	 */
	@Override
	public String toString() {
    	return ASTStringUtil.getSignatureString(this, null);
    }
	
    protected boolean visitAlignmentSpecifiers(ASTVisitor visitor) {
    	for (IASTAlignmentSpecifier specifier : alignmentSpecifiers) {
    		if (!specifier.accept(visitor)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    @Override
    public void replace(IASTNode child, IASTNode other) {
    	if (child instanceof IASTAlignmentSpecifier && other instanceof IASTAlignmentSpecifier) {
    		for (int i = 0; i < alignmentSpecifiers.length; ++i) {
    			if (alignmentSpecifiers[i] == child) {
    				alignmentSpecifiers[i] = (IASTAlignmentSpecifier) other;
    				other.setParent(child.getParent());
    				other.setPropertyInParent(child.getPropertyInParent());
    				return;
    			}
    		}
    	}
    }
}
