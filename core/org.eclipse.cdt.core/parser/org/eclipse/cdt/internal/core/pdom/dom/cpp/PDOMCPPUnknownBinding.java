/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Models unknown bindings. The class is directly used for objects (variables, functions, ...) and
 * serves as a base for unknown types.
 */
class PDOMCPPUnknownBinding extends PDOMCPPBinding implements ICPPUnknownBinding {

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE;
	
	public PDOMCPPUnknownBinding(PDOMLinkage linkage, PDOMNode parent, ICPPUnknownBinding binding) throws CoreException {
		super(linkage, parent, binding.getNameCharArray());
	}

	public PDOMCPPUnknownBinding(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_UNKNOWN_BINDING;
	}
	
    @Override
	public ICPPScope asScope() {
    	return null;
    }
    
	@Override
	public boolean mayHaveChildren() {
		return false;
	}

	@Override
	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}
}
