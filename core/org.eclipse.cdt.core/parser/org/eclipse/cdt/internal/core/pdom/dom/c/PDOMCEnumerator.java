/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for c enumerator in the index.
 */
class PDOMCEnumerator extends PDOMBinding implements IEnumerator {
	private static final int VALUE= PDOMBinding.RECORD_SIZE + 0;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = VALUE + 4;
	
	public PDOMCEnumerator(PDOMLinkage linkage, PDOMNode parent, IEnumerator enumerator)
			throws CoreException {
		super(linkage, parent, enumerator.getNameCharArray());
		
		final Database db = getDB();
		storeValue(db, enumerator);
	}

	public PDOMCEnumerator(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CENUMERATOR;
	}

	private void storeValue(final Database db, IEnumerator enumerator) throws CoreException {
		IValue value= enumerator.getValue();
		if (value != null) {
			Long val= value.numericalValue();
			db.putInt(record + VALUE, val == null ? -1 : val.intValue());
		}
	}
	
	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IEnumerator)
			storeValue(getDB(), (IEnumerator) newBinding);
	}

	@Override
	public IType getType() throws DOMException {
		IIndexFragmentBinding owner = getOwner();
		if (owner instanceof IType)
			return (IType) owner;
		return null;
	}
	
	@Override
	public IValue getValue() {
		try {
			int val= getDB().getInt(record + VALUE);
			return Value.create(val);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return Value.UNKNOWN;
	}
}
