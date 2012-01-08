/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;


/**
 * @author jcamelon
 */
public abstract class CASTBaseDeclSpecifier extends ASTNode implements ICASTDeclSpecifier {

    protected int storageClass;
    protected boolean isConst;
    protected boolean isVolatile;
    protected boolean isRestrict;
    protected boolean isInline;

    
	@Override
	public boolean isRestrict() {
        return isRestrict;
    }

    @Override
	public int getStorageClass() {
        return storageClass;
    }

    @Override
	public boolean isConst() {
        return isConst;
    }

    @Override
	public boolean isVolatile() {
        return isVolatile;
    }

    @Override
	public boolean isInline() {
        return isInline;
    }
    
    @Override
	public void setStorageClass(int storageClass) {
        assertNotFrozen();
        this.storageClass = storageClass;
    }

    @Override
	public void setConst(boolean value) {
        assertNotFrozen();
        this.isConst = value;
    }
    
    @Override
	public void setVolatile(boolean value) {
        assertNotFrozen();
        this.isVolatile = value;
    }
    
    @Override
	public void setRestrict(boolean value) {
        assertNotFrozen();
        this.isRestrict = value;
    }
    
    @Override
	public void setInline(boolean value) {
        assertNotFrozen();
        this.isInline = value;
    }
    
    protected void copyBaseDeclSpec(CASTBaseDeclSpecifier copy) {
    	copy.storageClass = storageClass;
    	copy.isConst = isConst;
    	copy.isVolatile = isVolatile;
    	copy.isRestrict = isRestrict;
    	copy.isInline = isInline;
    	copy.setOffsetAndLength(this);
    }
}
