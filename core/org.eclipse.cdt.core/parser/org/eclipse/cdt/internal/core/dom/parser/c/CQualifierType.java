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

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;

/**
 * @author dsteffle
 */
public class CQualifierType implements ICQualifierType {

	ICASTDeclSpecifier declSpec = null;

	/**
	 * CQualifierType has an IBasicType to keep track of the basic type information.
	 * 
	 * @param type the CQualifierType's IBasicType
	 */
	public CQualifierType(ICASTDeclSpecifier declSpec) {
		this.declSpec = declSpec;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isConst()
	 */
	public boolean isConst() {
		if (declSpec == null) return false;
		return declSpec.isConst();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#isVolatile()
	 */
	public boolean isVolatile() {
		if (declSpec == null) return false;
		return declSpec.isVolatile();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICQualifierType#isRestrict()
	 */
	public boolean isRestrict() {
		if (declSpec == null) return false;

		return declSpec.isRestrict(); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IQualifierType#getType()
	 */
	public IType getType() {
		return new CBasicType((ICASTSimpleDeclSpecifier)declSpec);
	}
}
