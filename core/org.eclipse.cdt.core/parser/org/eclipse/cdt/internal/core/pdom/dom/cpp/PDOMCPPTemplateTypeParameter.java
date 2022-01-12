/*******************************************************************************
 * Copyright (c) 2007, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for template type parameters in the index.
 */
class PDOMCPPTemplateTypeParameter extends PDOMCPPBinding
		implements IPDOMMemberOwner, ICPPTemplateTypeParameter, ICPPUnknownType, IIndexType, IPDOMCPPTemplateParameter {
	private static final int PACK_BIT = 1 << 31;

	private static final int DEFAULT_TYPE = PDOMCPPBinding.RECORD_SIZE;
	private static final int MEMBERLIST = DEFAULT_TYPE + Database.TYPE_SIZE;
	private static final int PARAMETERID = MEMBERLIST + Database.PTR_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PARAMETERID + 4;

	private PDOMCPPUnknownScope fUnknownScope; // No need for volatile, PDOMCPPUnknownScope protects its fields.
	private int fCachedParamID = -1;

	public PDOMCPPTemplateTypeParameter(PDOMLinkage linkage, PDOMNode parent, ICPPTemplateTypeParameter param)
			throws CoreException {
		super(linkage, parent, param.getNameCharArray());

		final Database db = getDB();
		int id = param.getParameterID();
		if (param.isParameterPack()) {
			id |= PACK_BIT;
		}
		db.putInt(record + PARAMETERID, id);
	}

	public PDOMCPPTemplateTypeParameter(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_TYPE_PARAMETER;
	}

	@Override
	public short getParameterPosition() {
		return (short) getParameterID();
	}

	@Override
	public short getTemplateNestingLevel() {
		readParamID();
		return (short) (getParameterID() >> 16);
	}

	@Override
	public boolean isParameterPack() {
		readParamID();
		return (fCachedParamID & PACK_BIT) != 0;
	}

	@Override
	public int getParameterID() {
		readParamID();
		return fCachedParamID & ~PACK_BIT;
	}

	private void readParamID() {
		if (fCachedParamID == -1) {
			try {
				final Database db = getDB();
				fCachedParamID = db.getInt(record + PARAMETERID);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fCachedParamID = Integer.MAX_VALUE;
			}
		}
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.accept(visitor);
	}

	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}

		if (!(type instanceof ICPPTemplateTypeParameter))
			return false;

		return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	@Override
	public IType getDefault() {
		try {
			return getLinkage().loadType(record + DEFAULT_TYPE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	@Override
	public ICPPTemplateArgument getDefaultValue() {
		IType d = getDefault();
		if (d == null)
			return null;

		return new CPPTemplateTypeArgument(d);
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ICPPScope asScope() {
		if (fUnknownScope == null) {
			fUnknownScope = new PDOMCPPUnknownScope(this, new CPPASTName(getNameCharArray()));
		}
		return fUnknownScope;
	}

	@Override
	public void configure(ICPPTemplateParameter param) {
		try {
			ICPPTemplateArgument val = param.getDefaultValue();
			if (val != null) {
				IType dflt = val.getTypeValue();
				if (dflt != null) {
					getLinkage().storeType(record + DEFAULT_TYPE, dflt);
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPTemplateTypeParameter) {
			ICPPTemplateTypeParameter ttp = (ICPPTemplateTypeParameter) newBinding;
			updateName(newBinding.getNameCharArray());
			IType newDefault = null;
			try {
				newDefault = ttp.getDefault();
			} catch (DOMException e) {
				// ignore
			}
			if (newDefault != null) {
				getLinkage().storeType(record + DEFAULT_TYPE, newDefault);
			}
		}
	}

	@Override
	public void forceDelete(PDOMLinkage linkage) throws CoreException {
		getDBName().delete();
		getLinkage().storeType(record + DEFAULT_TYPE, null);
	}
}
