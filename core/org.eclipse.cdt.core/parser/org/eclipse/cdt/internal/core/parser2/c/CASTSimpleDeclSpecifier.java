/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public class CASTSimpleDeclSpecifier extends CASTBaseDeclSpecifier implements ICASTSimpleDeclSpecifier {
    
    private int simpleType;
    private boolean isSigned;
    private boolean isUnsigned;
    private boolean isShort;
    private boolean isLong;


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#getType()
     */
    public int getType() {
        return simpleType;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#isSigned()
     */
    public boolean isSigned() {
        return isSigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#isUnsigned()
     */
    public boolean isUnsigned() {
        return isUnsigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#isShort()
     */
    public boolean isShort() {
        return isShort;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#isLong()
     */
    public boolean isLong() {
        return isLong;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#setType(int)
     */
    public void setType(int type) {
        simpleType = type;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#setShort(boolean)
     */
    public void setShort(boolean value) {
        isShort = value;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#setLong(boolean)
     */
    public void setLong(boolean value) {
        isLong = value;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#setUnsigned(boolean)
     */
    public void setUnsigned(boolean value) {
        isUnsigned = value;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier#setSigned(boolean)
     */
    public void setSigned(boolean value) {
        isSigned = value;
    }

}
