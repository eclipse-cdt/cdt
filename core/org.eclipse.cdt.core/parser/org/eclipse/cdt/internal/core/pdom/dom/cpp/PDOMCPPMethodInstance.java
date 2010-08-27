/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPMethodInstance extends PDOMCPPFunctionInstance implements ICPPMethod {

	/**
	 * The size in bytes of a PDOMCPPMethodInstance record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionInstance.RECORD_SIZE + 0;
	
	public PDOMCPPMethodInstance(PDOMLinkage linkage, PDOMNode parent, ICPPMethod method, PDOMBinding instantiated)
			throws CoreException {
		super(linkage, parent, method, instantiated);
	}
	
	public PDOMCPPMethodInstance(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_METHOD_INSTANCE;
	}
	
	public boolean isDestructor() {
		return ((ICPPMethod)getTemplateDefinition()).isDestructor();
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	public boolean isImplicit() {
		return ((ICPPMethod)getTemplateDefinition()).isImplicit();
	}

	public boolean isVirtual() {
		return ((ICPPMethod)getTemplateDefinition()).isVirtual();
	}

	public boolean isPureVirtual() {
		return ((ICPPMethod)getTemplateDefinition()).isPureVirtual();		
	}

	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	public int getVisibility() {
		return ((ICPPMethod)getTemplateDefinition()).getVisibility();
	}
}
