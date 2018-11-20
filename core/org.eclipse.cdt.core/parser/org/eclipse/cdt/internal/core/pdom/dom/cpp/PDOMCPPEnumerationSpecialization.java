/*******************************************************************************
 * Copyright (c) 2013, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Enumeration specialization in the index.
 */
class PDOMCPPEnumerationSpecialization extends PDOMCPPSpecialization
		implements IPDOMCPPEnumType, IPDOMMemberOwner, ICPPEnumerationSpecialization {
	private static final int OFFSET_ENUMERATOR_LIST = PDOMCPPSpecialization.RECORD_SIZE;
	private static final int OFFSET_MIN_VALUE = OFFSET_ENUMERATOR_LIST + Database.PTR_SIZE;
	private static final int OFFSET_MAX_VALUE = OFFSET_MIN_VALUE + 8;
	private static final int OFFSET_FIXED_TYPE = OFFSET_MAX_VALUE + 8;
	private static final int OFFSET_FLAGS = OFFSET_FIXED_TYPE + Database.TYPE_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = OFFSET_FLAGS + 1;

	private Long fMinValue; // No need for volatile, all fields of Long are final.
	private Long fMaxValue; // No need for volatile, all fields of Long are final.
	private volatile IType fFixedType = ProblemBinding.NOT_INITIALIZED;
	private PDOMCPPEnumScope fScope; // No need for volatile, all fields of PDOMCPPEnumScope are final.

	public PDOMCPPEnumerationSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPEnumeration enumeration,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) enumeration, specialized);
		storeProperties(enumeration);
	}

	public PDOMCPPEnumerationSpecialization(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public ICPPEnumeration getSpecializedBinding() {
		return (ICPPEnumeration) super.getSpecializedBinding();
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		storeProperties((ICPPEnumeration) newBinding);
	}

	private void storeProperties(ICPPEnumeration enumeration) throws CoreException {
		final Database db = getDB();
		db.putByte(record + OFFSET_FLAGS, enumeration.isScoped() ? (byte) 1 : (byte) 0);

		getLinkage().storeType(record + OFFSET_FIXED_TYPE, enumeration.getFixedType());

		if (enumeration instanceof ICPPInternalBinding) {
			if (((ICPPInternalBinding) enumeration).getDefinition() != null) {
				final long minValue = enumeration.getMinValue();
				final long maxValue = enumeration.getMaxValue();
				db.putLong(record + OFFSET_MIN_VALUE, minValue);
				db.putLong(record + OFFSET_MAX_VALUE, maxValue);
				fMinValue = minValue;
				fMaxValue = maxValue;
			}
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_ENUMERATION_SPECIALIZATION;
	}

	@Override
	public IEnumerator[] getEnumerators() {
		return PDOMCPPEnumScope.getEnumerators(this);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPEnumScope.acceptViaCache(this, visitor);
	}

	@Override
	public void addChild(PDOMNode node) throws CoreException {
		if (node instanceof IPDOMCPPEnumerator) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + OFFSET_ENUMERATOR_LIST);
			list.addMember(node);
			PDOMCPPEnumScope.updateCache(this, (IPDOMCPPEnumerator) node);
		}
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}

		if (type instanceof PDOMNode) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}

		if (type instanceof IEnumeration) {
			IEnumeration etype = (IEnumeration) type;
			char[] nchars = etype.getNameCharArray();
			if (nchars.length == 0) {
				nchars = ASTTypeUtil.createNameForAnonymous(etype);
			}
			if (nchars == null || !CharArrayUtils.equals(nchars, getNameCharArray()))
				return false;

			return SemanticUtil.haveSameOwner(this, etype);
		}
		return false;
	}

	@Override
	public long getMinValue() {
		if (fMinValue != null) {
			return fMinValue.longValue();
		}
		long minValue = 0;
		try {
			minValue = getDB().getLong(record + OFFSET_MIN_VALUE);
		} catch (CoreException e) {
		}
		fMinValue = minValue;
		return minValue;
	}

	@Override
	public long getMaxValue() {
		if (fMaxValue != null) {
			return fMaxValue.longValue();
		}
		long maxValue = 0;
		try {
			maxValue = getDB().getLong(record + OFFSET_MAX_VALUE);
		} catch (CoreException e) {
		}
		fMaxValue = maxValue;
		return maxValue;
	}

	@Override
	public Object clone() {
		throw new IllegalArgumentException("Enums must not be cloned"); //$NON-NLS-1$
	}

	@Override
	public boolean isScoped() {
		try {
			return getDB().getByte(record + OFFSET_FLAGS) != 0;
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public IType getFixedType() {
		if (fFixedType == ProblemBinding.NOT_INITIALIZED) {
			fFixedType = loadFixedType();
		}
		return fFixedType;
	}

	private IType loadFixedType() {
		try {
			return getLinkage().loadType(record + OFFSET_FIXED_TYPE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public ICPPScope asScope() {
		if (fScope == null) {
			fScope = new PDOMCPPEnumScope(this);
		}
		return fScope;
	}

	@Override
	public void loadEnumerators(final List<IPDOMCPPEnumerator> enumerators) {
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + OFFSET_ENUMERATOR_LIST);
			list.accept(new IPDOMVisitor() {
				@Override
				public boolean visit(IPDOMNode node) throws CoreException {
					if (node instanceof IPDOMCPPEnumerator) {
						enumerators.add((IPDOMCPPEnumerator) node);
					}
					return true;
				}

				@Override
				public void leave(IPDOMNode node) {
				}
			});
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public IEnumerator specializeEnumerator(IEnumerator enumerator) {
		if (enumerator instanceof ICPPSpecialization && ((ICPPSpecialization) enumerator).getOwner() == this) {
			return enumerator;
		}

		// The specialized enumerators are already computed, just need to look up the right one.
		IEnumerator[] unspecializedEnumerators = getSpecializedBinding().getEnumerators();
		for (int i = 0; i < unspecializedEnumerators.length; ++i) {
			if (enumerator.equals(unspecializedEnumerators[i])) {
				IEnumerator[] enumerators = getEnumerators();
				return i < enumerators.length ? enumerators[i] : enumerator;
			}
		}
		return enumerator;
	}
}
