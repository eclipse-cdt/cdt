/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPField extends PDOMCPPVariable implements ICPPField, ICPPDelegateCreator {
	
	public PDOMCPPField(PDOM pdom, PDOMNode parent, ICPPField field)
			throws CoreException {
		super(pdom, parent, field);
	}		

	public PDOMCPPField(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	// @Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	// @Override
	public int getNodeType() {
		return PDOMCPPLinkage.CPPFIELD;
	}
	
	public ICPPClassType getClassOwner() {
		try {
			return (ICPPClassType)getParentNode();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATIONS));
	}

	// @Override
	public boolean isMutable() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCPPAnnotation.MUTABLE_OFFSET);
	}

	// @Override
	public boolean isAuto() {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	// @Override
	public boolean isExtern() {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	// @Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPField.CPPFieldDelegate(name, this);
	}
	
}
