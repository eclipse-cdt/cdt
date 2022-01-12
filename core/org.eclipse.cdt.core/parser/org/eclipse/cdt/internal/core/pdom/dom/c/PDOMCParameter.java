/*******************************************************************************
 * Copyright (c) 2006, 2015 QNX Software Systems and others.
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
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a function parameter in the index.
 */
final class PDOMCParameter extends PDOMNamedNode implements IParameter, IPDOMBinding {
	private static final int NEXT_PARAM = PDOMNamedNode.RECORD_SIZE;
	private static final int FLAG_OFFSET = NEXT_PARAM + Database.PTR_SIZE;
	@SuppressWarnings("hiding")
	public static final int RECORD_SIZE = FLAG_OFFSET + 1;
	static {
		assert RECORD_SIZE <= 22; // 23 would yield a 32-byte block
	}

	private final IType fType;

	public PDOMCParameter(PDOMLinkage linkage, long record, IType type) {
		super(linkage, record);
		fType = type;
	}

	public PDOMCParameter(PDOMLinkage linkage, PDOMNode parent, IParameter param, PDOMCParameter next)
			throws CoreException {
		super(linkage, parent, param.getNameCharArray());
		fType = null; // this constructor is used for adding parameters to the database, only.

		Database db = getDB();

		db.putRecPtr(record + NEXT_PARAM, 0);
		db.putRecPtr(record + NEXT_PARAM, next == null ? 0 : next.getRecord());
		db.putByte(record + FLAG_OFFSET, PDOMCAnnotations.encodeVariableAnnotations(param));
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CPARAMETER;
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	public boolean isAuto() {
		return true;
	}

	@Override
	public boolean isRegister() {
		return PDOMCAnnotations.isRegister(getFlags());
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public IIndexScope getScope() {
		return null;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[0];
		}
	}

	@Override
	public IIndexFragment getFragment() {
		return getPDOM();
	}

	@Override
	public boolean hasDefinition() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	@Override
	public boolean hasDeclaration() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	@Override
	public String[] getQualifiedName() {
		return new String[] { getName() };
	}

	@Override
	public int getBindingConstant() {
		return getNodeType();
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		PDOMCParameter p = this;
		for (;;) {
			long rec = p.getNextPtr();
			p.flatDelete(linkage);
			if (rec == 0)
				return;
			p = new PDOMCParameter(linkage, rec, null);
		}
	}

	private void flatDelete(PDOMLinkage linkage) throws CoreException {
		super.delete(linkage);
	}

	public long getNextPtr() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_PARAM);
		return rec;
	}

	@Override
	public boolean isFileLocal() throws CoreException {
		return false;
	}

	@Override
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	private byte getFlags() {
		try {
			return getDB().getByte(record + FLAG_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return 0;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return false;
	}
}
