/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author dsteffle
 */
public class CQualifiedPointerType implements ICPointerType, ITypeContainer {
	IType nextType = null;
	IASTArrayModifier mod = null;
	
	public CQualifiedPointerType(IType next, IASTArrayModifier mod) {
		this.nextType = next;
		if (mod instanceof ICASTArrayModifier) this.mod = mod;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICPointerType#isRestrict()
	 */
	public boolean isRestrict() {
		if (mod == null || !(mod instanceof ICASTArrayModifier)) return false;
		return ((ICASTArrayModifier)mod).isRestrict();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#getType()
	 */
	public IType getType() {
		return nextType;
	}
	
	public void setType(IType type) {
		nextType = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isConst()
	 */
	public boolean isConst() {
		if (mod == null || !(mod instanceof ICASTArrayModifier)) return false;
		return ((ICASTArrayModifier)mod).isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IPointerType#isVolatile()
	 */
	public boolean isVolatile() {
		if (mod == null || !(mod instanceof ICASTArrayModifier)) return false;
		return ((ICASTArrayModifier)mod).isVolatile();
	}
}
