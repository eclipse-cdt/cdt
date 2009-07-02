/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMGPPBasicType extends PDOMCPPBasicType implements IGPPBasicType {

	public PDOMGPPBasicType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMGPPBasicType(PDOMLinkage linkage, PDOMNode parent, IGPPBasicType type) throws CoreException {
		super(linkage, parent, type, encodeGPPFlags(type));
	}
		
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.GPPBASICTYPE;
	}

	protected static short encodeGPPFlags(IGPPBasicType type) {
		short flags = encodeFlags(type);
		try {
			if (type.isComplex())
				flags |= IS_COMPLEX;
			if (type.isImaginary())
				flags |= IS_IMAGINARY;
			if (type.isLongLong())
				flags |= IS_LONG_LONG;
		} catch (DOMException e) {
		}
		return flags;
	}

	public IType getTypeofType() throws DOMException {
		return null;
	}

	public boolean isComplex() {
		return (getQualifierBits() & IS_COMPLEX) != 0;
	}

	public boolean isImaginary() {
		return (getQualifierBits() & IS_IMAGINARY) != 0;
	}

	public boolean isLongLong() throws DOMException {
		return (getQualifierBits() & IS_LONG_LONG) != 0;
	}
}
