/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
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
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Enumerations in the database.
 */
class PDOMCEnumeration extends PDOMBinding implements IEnumeration, IIndexType, IPDOMMemberOwner {
	private static final int OFFSET_ENUMERATOR_LIST = PDOMBinding.RECORD_SIZE;
	private static final int OFFSET_MIN_VALUE= OFFSET_ENUMERATOR_LIST + Database.PTR_SIZE;
	private static final int OFFSET_MAX_VALUE= OFFSET_MIN_VALUE + 8;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = OFFSET_MAX_VALUE + 8;

	private Long fMinValue;  // No need for volatile, all fields of Long are final.
	private Long fMaxValue;  // No need for volatile, all fields of Long are final.
	
	public PDOMCEnumeration(PDOMLinkage linkage, PDOMNode parent, IEnumeration enumeration)
			throws CoreException {
		super(linkage, parent, enumeration.getNameCharArray());
		storeValueBounds(enumeration);
	}

	public PDOMCEnumeration(PDOMLinkage linkage, long record) {
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
		return IIndexCBindingConstants.CENUMERATION;
	}

	@Override
	public IEnumerator[] getEnumerators() {
		List<PDOMCEnumerator> result = getCachedEnumerators(true);
		return result.toArray(new IEnumerator[result.size()]);
	}

	private List<PDOMCEnumerator> getCachedEnumerators(boolean create) {
		final Long key= record;
		final PDOM pdom = getPDOM();
		@SuppressWarnings("unchecked")
		Reference<List<PDOMCEnumerator>> cached= (Reference<List<PDOMCEnumerator>>) pdom.getCachedResult(key);
		List<PDOMCEnumerator> result= cached == null ? null : cached.get();

		if (result == null && create) {
			// there is no cache, build it:
			result= loadEnumerators();
			pdom.putCachedResult(key, new SoftReference<List<PDOMCEnumerator>>(result));
		}
		return result;
	}

	private List<PDOMCEnumerator> loadEnumerators() {
		final ArrayList<PDOMCEnumerator> result= new ArrayList<PDOMCEnumerator>();
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + OFFSET_ENUMERATOR_LIST);
			list.accept(new IPDOMVisitor() {
				@Override
				public boolean visit(IPDOMNode node) throws CoreException {
					if (node instanceof PDOMCEnumerator) {
						result.add((PDOMCEnumerator) node);
					}
					return true;
				}
				@Override
				public void leave(IPDOMNode node) {}
			});
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		result.trimToSize();
		return result;
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		for (PDOMCEnumerator enumerator : getCachedEnumerators(true)) {
			visitor.visit(enumerator);
			visitor.leave(enumerator);
		}
	}

	@Override
	public void addChild(PDOMNode node) throws CoreException {
		if (node instanceof PDOMCEnumerator) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + OFFSET_ENUMERATOR_LIST);
			list.addMember(node);
			List<PDOMCEnumerator> cache = getCachedEnumerators(false);
			if (cache != null)
				cache.add((PDOMCEnumerator) node);
		}
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
	}


	@Override
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

	@Override
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
		return false;
	}

	@Override
	public Object clone() {
		throw new IllegalArgumentException("Enums must not be cloned"); //$NON-NLS-1$
	}
}
