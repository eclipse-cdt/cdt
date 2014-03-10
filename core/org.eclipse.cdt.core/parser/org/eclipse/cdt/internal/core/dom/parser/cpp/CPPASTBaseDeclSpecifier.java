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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

/**
 * Base for all c++ declaration specifiers.
 */
public abstract class CPPASTBaseDeclSpecifier extends ASTAttributeOwner implements ICPPASTDeclSpecifier {
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
		return super.copy(copy, style);
	}

	/**
	 * Provided for debugging purposes, only.
	 */
	@Override
	public String toString() {
    	return ASTStringUtil.getSignatureString(this, null);
    }
}
