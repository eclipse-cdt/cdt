/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class CArrayType implements ICArrayType, ITypeContainer {

	IType type = null;
	ICASTArrayModifier mod = null;
	
	public CArrayType(IType type) {
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IArrayType#getType()
	 */
	public IType getType() {
		return type;
	}
	
	public void setType( IType t ){
	    this.type = t;
	}
	
	public void setModifiedArrayModifier(ICASTArrayModifier mod) {
		this.mod = mod;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isConst()
	 */
	public boolean isConst() {
		if (mod==null) return false;
		return mod.isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isRestrict()
	 */
	public boolean isRestrict() {
		if (mod==null) return false;
		return mod.isRestrict();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isVolatile()
	 */
	public boolean isVolatile() {
		if (mod==null) return false;
		return mod.isVolatile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isStatic()
	 */
	public boolean isStatic() {
		if (mod==null) return false;
		return mod.isStatic();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICArrayType#isVariableLength()
	 */
	public boolean isVariableLength() {
		// TODO Auto-generated method stub
		return false;
	}

    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }
}
