/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    IBM Corporation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPField extends PDOMCPPVariable implements ICPPField {
	
	public PDOMCPPField(PDOMLinkage linkage, PDOMNode parent, ICPPField field)
			throws CoreException {
		super(linkage, parent, field);
	}		

	public PDOMCPPField(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	// @Override
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	// @Override
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPFIELD;
	}
	
	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATIONS));
	}

	// @Override
	@Override
	public boolean isMutable() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCPPAnnotation.MUTABLE_OFFSET);
	}

	// @Override
	@Override
	public boolean isAuto() {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	// @Override
	@Override
	public boolean isExtern() {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	// @Override
	@Override
	public boolean isExternC() {
		return false; 
	}

	// @Override
	@Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}
}
