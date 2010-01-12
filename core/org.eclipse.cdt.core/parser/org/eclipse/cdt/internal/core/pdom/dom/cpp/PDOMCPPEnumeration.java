/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * Enumerations in the index.
 */
class PDOMCPPEnumeration extends PDOMCPPBinding implements IEnumeration, IIndexType {

	private static final int FIRST_ENUMERATOR = PDOMBinding.RECORD_SIZE;
	private static final int OFFSET_MIN_VALUE= FIRST_ENUMERATOR + Database.PTR_SIZE;
	private static final int OFFSET_MAX_VALUE= OFFSET_MIN_VALUE + 8;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = OFFSET_MAX_VALUE + 8;
	
	private Long fMinValue;
	private Long fMaxValue;

	public PDOMCPPEnumeration(PDOMLinkage linkage, PDOMNode parent, IEnumeration enumeration)
			throws CoreException {
		super(linkage, parent, enumeration.getNameCharArray());
		storeValueBounds(enumeration);
	}

	public PDOMCPPEnumeration(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		storeValueBounds((IEnumeration) newBinding);
	}

	private void storeValueBounds(IEnumeration enumeration) throws CoreException {
		final Database db= getDB();
		final long minValue = enumeration.getMinValue();
		final long maxValue = enumeration.getMaxValue();
		db.putLong(record+ OFFSET_MIN_VALUE, minValue);
		db.putLong(record+ OFFSET_MAX_VALUE, maxValue);
		fMinValue= minValue;
		fMaxValue= maxValue;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPENUMERATION;
	}

	public IEnumerator[] getEnumerators() throws DOMException {
		try {
			ArrayList<PDOMCPPEnumerator> enums = new ArrayList<PDOMCPPEnumerator>();
			for (PDOMCPPEnumerator enumerator = getFirstEnumerator();
					enumerator != null;
					enumerator = enumerator.getNextEnumerator()) {
				enums.add(enumerator);
			}
			
			IEnumerator[] enumerators = enums.toArray(new IEnumerator[enums.size()]);
			
			// Reverse the list since they are last in first out
			int n = enumerators.length;
			for (int i = 0; i < n / 2; ++i) {
				IEnumerator tmp = enumerators[i];
				enumerators[i] = enumerators[n - 1 - i];
				enumerators[n - 1 - i] = tmp;
			}
				
			return enumerators;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IEnumerator[0];
		}
	}

	private PDOMCPPEnumerator getFirstEnumerator() throws CoreException {
		long value = getDB().getRecPtr(record + FIRST_ENUMERATOR);
		return value != 0 ? new PDOMCPPEnumerator(getLinkage(), value) : null;
	}
	
	public void addEnumerator(PDOMCPPEnumerator enumerator) throws CoreException {
		PDOMCPPEnumerator first = getFirstEnumerator();
		enumerator.setNextEnumerator(first);
		getDB().putRecPtr(record + FIRST_ENUMERATOR, enumerator.getRecord());
	}
	
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
		
		try {
			if (type instanceof IEnumeration) {
				IEnumeration etype= (IEnumeration) type;
				char[] nchars = etype.getNameCharArray();
				if (nchars.length == 0) {
					nchars= ASTTypeUtil.createNameForAnonymous(etype);
				}
				if (nchars == null || !CharArrayUtils.equals(nchars, getNameCharArray()))
					return false;

				return SemanticUtil.isSameOwner(getOwner(), etype.getOwner());
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return false;
	}

	public long getMinValue() {
		if (fMinValue != null) {
			return fMinValue.longValue();
		}
		long minValue= 0;
		try {
			minValue= getDB().getLong(record + OFFSET_MIN_VALUE);
		} catch (CoreException e) {
		}
		fMinValue= minValue;
		return minValue;
	}

	public long getMaxValue() {
		if (fMaxValue != null) {
			return fMaxValue.longValue();
		}
		long maxValue= 0;
		try {
			maxValue= getDB().getLong(record + OFFSET_MAX_VALUE);
		} catch (CoreException e) {
		}
		fMaxValue= maxValue;
		return maxValue;
	}

	@Override
	public Object clone() {
		throw new PDOMNotImplementedError();
	}
}
