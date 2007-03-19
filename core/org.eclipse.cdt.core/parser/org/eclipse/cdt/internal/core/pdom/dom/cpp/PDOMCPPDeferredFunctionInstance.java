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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPDeferredFunctionInstance extends PDOMCPPFunctionInstance
		implements ICPPDeferredTemplateInstance {

	/**
	 * The size in bytes of a PDOMCPPDeferredFunctionInstance record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPFunctionInstance.RECORD_SIZE + 0;
	
	public PDOMCPPDeferredFunctionInstance(PDOM pdom, PDOMNode parent, ICPPFunction function, PDOMCPPFunctionTemplate instantiated)
			throws CoreException {
		super(pdom, parent, function, instantiated);
	}

	public PDOMCPPDeferredFunctionInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_DEFERRED_FUNCTION_INSTANCE;
	}
}
