/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPMethodInstance extends PDOMCPPFunctionInstance implements
		ICPPMethod {

	/**
	 * The size in bytes of a PDOMCPPMethodInstance record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPFunctionInstance.RECORD_SIZE + 0;
	
	public PDOMCPPMethodInstance(PDOM pdom, PDOMNode parent, ICPPMethod method, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, method, instantiated);
	}
	
	public PDOMCPPMethodInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_METHOD_INSTANCE;
	}
	
	public boolean isDestructor() throws DOMException {
		return ((ICPPMethod)getTemplateDefinition()).isDestructor();
	}

	public boolean isImplicit() {
		return ((ICPPMethod)getTemplateDefinition()).isImplicit();
	}

	public boolean isVirtual() throws DOMException {
		return ((ICPPMethod)getTemplateDefinition()).isVirtual();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return ((ICPPMethod)getTemplateDefinition()).getClassOwner();
	}

	public int getVisibility() throws DOMException {
		return ((ICPPMethod)getTemplateDefinition()).getVisibility();
	}
}
