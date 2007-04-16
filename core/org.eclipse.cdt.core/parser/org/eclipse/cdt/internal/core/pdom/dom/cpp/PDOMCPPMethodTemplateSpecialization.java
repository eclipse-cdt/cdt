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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPMethodTemplateSpecialization extends
		PDOMCPPFunctionTemplateSpecialization implements ICPPMethod {

	/**
	 * The size in bytes of a PDOMCPPMethodTemplateSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPFunctionTemplateSpecialization.RECORD_SIZE + 0;
	
	public PDOMCPPMethodTemplateSpecialization(PDOM pdom, PDOMNode parent, ICPPMethod method, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, (ICPPFunctionTemplate) method, specialized);
	}

	public PDOMCPPMethodTemplateSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_METHOD_TEMPLATE_SPECIALIZATION;
	}
	
	public boolean isDestructor() throws DOMException {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isDestructor();
		}
		return false;
	}

	public boolean isImplicit() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isImplicit();
		}
		return false;
	}

	public boolean isVirtual() throws DOMException {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isVirtual();
		}
		return false;
	}

	public ICPPClassType getClassOwner() throws DOMException {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).getClassOwner();
		}
		return null;
	}

	public int getVisibility() throws DOMException {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).getVisibility();
		}
		return 0;
	}
}
