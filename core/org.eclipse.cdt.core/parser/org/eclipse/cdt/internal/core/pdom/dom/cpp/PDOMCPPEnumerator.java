/*******************************************************************************
 * Copyright (c) 2006, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalEnumerator;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a c++ enumerator in the index.
 */
class PDOMCPPEnumerator extends PDOMCPPBinding implements IPDOMCPPEnumerator {
	private static final int VALUE = PDOMCPPBinding.RECORD_SIZE;
	private static final int INTERNAL_TYPE = VALUE + Database.VALUE_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = INTERNAL_TYPE + Database.TYPE_SIZE;

	public PDOMCPPEnumerator(PDOMLinkage linkage, PDOMNode parent, ICPPInternalEnumerator enumerator)
			throws CoreException {
		super(linkage, parent, enumerator.getNameCharArray());
		IValue value = enumerator.getValue();
		if (value != null) {
			linkage.storeValue(record + VALUE, value);
		}
		IType internalType = enumerator.getInternalType();
		if (internalType != null) {
			linkage.storeType(record + INTERNAL_TYPE, internalType);
		}
	}

	public PDOMCPPEnumerator(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPENUMERATOR;
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IEnumerator) {
			IValue value = ((IEnumerator) newBinding).getValue();
			if (value != null) {
				getLinkage().storeValue(record + VALUE, value);
			}
		}
	}

	@Override
	public IType getType() {
		IIndexFragmentBinding owner = getOwner();
		if (owner instanceof IType)
			return (IType) owner;
		return null;
	}

	@Override
	public IType getInternalType() {
		try {
			return getLinkage().loadType(record + INTERNAL_TYPE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue() {
		try {
			return getLinkage().loadValue(record + VALUE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return IntegralValue.UNKNOWN;
	}
}
