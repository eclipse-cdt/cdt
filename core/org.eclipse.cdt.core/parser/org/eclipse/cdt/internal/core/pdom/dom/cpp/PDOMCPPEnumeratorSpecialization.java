/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a specialization of an enumerator in the index.
 */
class PDOMCPPEnumeratorSpecialization extends PDOMCPPSpecialization implements IPDOMCPPEnumerator {
	private static final int VALUE= PDOMCPPSpecialization.RECORD_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = VALUE + Database.VALUE_SIZE;

	public PDOMCPPEnumeratorSpecialization(PDOMLinkage linkage, PDOMNode parent,
			IEnumerator enumerator, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) enumerator, specialized);
		storeValue(enumerator);
	}

	public PDOMCPPEnumeratorSpecialization(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_ENUMERATOR_SPECIALIZATION;
	}

	private void storeValue(IEnumerator enumerator) throws CoreException {
		IValue value= enumerator.getValue();
		if (value != null) {
			getLinkage().storeValue(record + VALUE, value);
		}
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IEnumerator)
			storeValue((IEnumerator) newBinding);
	}

	@Override
	public IType getType() {
		IIndexFragmentBinding owner = getOwner();
		if (owner instanceof IType)
			return (IType) owner;
		return null;
	}

	@Override
	public IValue getValue() {
		try {
			return getLinkage().loadValue(record + VALUE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return Value.UNKNOWN;
	}
}
