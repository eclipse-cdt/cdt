/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPConstructorSpecialization extends
		PDOMCPPMethodSpecialization implements ICPPConstructor {

	/**
	 * The size in bytes of a PDOMCPPConstructorSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPMethodSpecialization.RECORD_SIZE + 0;

	public PDOMCPPConstructorSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPConstructor constructor, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, constructor, specialized);
	}

	public PDOMCPPConstructorSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CONSTRUCTOR_SPECIALIZATION;
	}
}
