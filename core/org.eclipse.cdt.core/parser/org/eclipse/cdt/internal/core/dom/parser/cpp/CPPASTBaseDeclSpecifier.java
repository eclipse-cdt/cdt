/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

/**
 * Base for all c++ declaration specifiers
 */
public abstract class CPPASTBaseDeclSpecifier extends ASTNode implements ICPPASTDeclSpecifier {

    private boolean friend;
    private boolean inline;
    private boolean isConst;
    private boolean isVolatile;
    private boolean isRestrict;
    private int sc;
    private boolean virtual;
    private boolean explicit;
    
    public boolean isFriend() {
        return friend;
    }

    public int getStorageClass() {
        return sc;
    }

    public void setStorageClass(int storageClass) {
        assertNotFrozen();
        sc = storageClass;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean value) {
        assertNotFrozen();
        isConst = value;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setVolatile(boolean value) {
        assertNotFrozen();
        isVolatile = value;
    }

    public boolean isRestrict() {
        return isRestrict;
    }

    public void setRestrict(boolean value) {
        assertNotFrozen();
        isRestrict = value;
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean value) {
        assertNotFrozen();
        this.inline = value;
    }

    public void setFriend(boolean value) {
        assertNotFrozen();
        friend = value;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean value) {
        assertNotFrozen();
        virtual = value;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean value) {
        assertNotFrozen();
        this.explicit = value;
    }

    protected void copyBaseDeclSpec(CPPASTBaseDeclSpecifier other) {
    	other.friend = friend;
    	other.inline = inline;
    	other.isConst = isConst;
    	other.isVolatile = isVolatile;
    	other.isRestrict= isRestrict;
    	other.virtual = virtual;
    	other.explicit = explicit;
    	other.sc = sc;
    	other.setOffsetAndLength(this);
    }
    
	/**
	 * Provided for debugging purposes, only.
	 */
	@Override
	public String toString() {
    	return ASTStringUtil.getSignatureString(this, null);
    }
}
