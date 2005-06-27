/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;


/**
 * @author jcamelon
 */
public abstract class CASTBaseDeclSpecifier extends CASTNode implements ICASTDeclSpecifier {

    protected int storageClass;
    protected boolean isConst;
    protected boolean isVolatile;
    protected boolean isRestrict;
    protected boolean isInline;

    public boolean isRestrict() {
        return isRestrict;
    }

    public int getStorageClass() {
        return storageClass;
    }

    public boolean isConst() {
        return isConst;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public boolean isInline() {
        return isInline;
    }
    
    /**
     * @param storageClass The storageClass to set.
     */
    public void setStorageClass(int storageClass) {
        this.storageClass = storageClass;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setConst(boolean)
     */
    public void setConst(boolean value) {
        this.isConst = value;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setVolatile(boolean)
     */
    public void setVolatile(boolean value) {
        this.isVolatile = value;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        this.isRestrict = value;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#setInline(boolean)
     */
    public void setInline(boolean value) {
        this.isInline = value;
    }
}
