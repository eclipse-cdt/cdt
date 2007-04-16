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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPConstructorTemplateSpecialization extends
		PDOMCPPMethodTemplateSpecialization implements ICPPConstructor {

	/**
	 * The size in bytes of a PDOMCPPConstructorTemplateSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPMethodTemplateSpecialization.RECORD_SIZE + 0;
	
	public PDOMCPPConstructorTemplateSpecialization(PDOM pdom, PDOMNode parent, ICPPConstructor constructor, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, constructor, specialized);
	}

	public PDOMCPPConstructorTemplateSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_CONSTRUCTOR_TEMPLATE_SPECIALIZATION;
	}
	
	public boolean isExplicit() throws DOMException {
		return ((ICPPConstructor)getSpecializedBinding()).isExplicit();
	}
}
