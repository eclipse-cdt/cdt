/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

@SuppressWarnings("restriction")
public class C99QualifierType implements ICQualifierType, ITypeContainer {

	private boolean isRestrict;
	private boolean isConst;
	private boolean isVolatile;
	
	private IType type;
	

	public C99QualifierType() {
	}

	public C99QualifierType(IType type) {
		this.type = type;
	}

	public C99QualifierType(IType type, boolean isConst, boolean isVolatile, boolean isRestrict) {
		this.isConst = isConst;
		this.isRestrict = isRestrict;
		this.isVolatile = isVolatile;
		this.type = type;
	}

	public boolean isRestrict() {
		return isRestrict;
	}

	public void setRestrict(boolean isRestrict) {
		this.isRestrict = isRestrict;
	}

	public boolean isConst() {
		return isConst;
	}

	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	public boolean isVolatile() {
		return isVolatile;
	}

	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public boolean isSameType(IType t) {
		if(t == this)
			return true;
		
		if (t instanceof ICQualifierType) {

			ICQualifierType pointerType = (ICQualifierType) t;
			if(pointerType.isConst() == isConst &&
			   pointerType.isRestrict() == isRestrict &&
			   pointerType.isVolatile() == isVolatile) {
				return type.isSameType(pointerType.getType());
			}

		}
		return false;
	}
	
	
	@Override
	public C99QualifierType clone() {
		try {
			C99QualifierType clone = (C99QualifierType) super.clone();
			clone.type = (IType) type.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}
	}

}
