/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTBaseDeclSpecifier extends CPPASTNode implements
        ICPPASTDeclSpecifier {

    private boolean friend;
    private boolean inline;
    private boolean volatil;
    private boolean isConst;
    private int sc;
    private boolean virtual;
    private boolean explicit;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier#isFriend()
     */
    public boolean isFriend() {
        return friend;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getStorageClass()
     */
    public int getStorageClass() {
        return sc;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setStorageClass(int)
     */
    public void setStorageClass(int storageClass) {
        sc = storageClass;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#isConst()
     */
    public boolean isConst() {
        return isConst;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setConst(boolean)
     */
    public void setConst(boolean value) {
        isConst = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#isVolatile()
     */
    public boolean isVolatile() {
        return volatil;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setVolatile(boolean)
     */
    public void setVolatile(boolean value) {
        volatil = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#isInline()
     */
    public boolean isInline() {
        return inline;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setInline(boolean)
     */
    public void setInline(boolean value) {
        this.inline = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier#setIsFriend(boolean)
     */
    public void setFriend(boolean value) {
        friend = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier#isVirtual()
     */
    public boolean isVirtual() {
        return virtual;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier#setVirtual(boolean)
     */
    public void setVirtual(boolean value) {
        virtual = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier#isExplicit()
     */
    public boolean isExplicit() {
        return explicit;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier#setExplicit(boolean)
     */
    public void setExplicit(boolean value) {
        this.explicit = value;
    }

}
