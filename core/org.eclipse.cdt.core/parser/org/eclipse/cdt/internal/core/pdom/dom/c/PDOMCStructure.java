/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCStructure extends PDOMBinding implements ICompositeType, ICCompositeTypeScope, IPDOMMemberOwner, IIndexType, IIndexScope {
	private static final int MEMBERLIST = PDOMBinding.RECORD_SIZE;
	private static final int KEY = MEMBERLIST + 4; // byte
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 8;
	
	public PDOMCStructure(PDOM pdom, PDOMNode parent, ICompositeType compType) throws CoreException {
		super(pdom, parent, compType.getNameCharArray());		
		setKind(compType);
		// linked list is initialized by malloc zeroing allocated storage
	}

	public PDOMCStructure(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICompositeType) {
			ICompositeType ct= (ICompositeType) newBinding;
			setKind(ct);
			super.update(linkage, newBinding);
		}
	}

	private void setKind(ICompositeType ct) throws CoreException {
		try {
			pdom.getDB().putByte(record + KEY, (byte) ct.getKey());
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		new PDOMNodeLinkedList(pdom, record+MEMBERLIST, getLinkageImpl()).accept(visitor);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		new PDOMNodeLinkedList(pdom, record+MEMBERLIST, getLinkageImpl()).addMember(member);
	}
	
	public int getNodeType() {
		return IIndexCBindingConstants.CSTRUCTURE;
	}
	
	public Object clone() {
		throw new PDOMNotImplementedError();
	}
	
	public int getKey() throws DOMException {
		try {
			return pdom.getDB().getByte(record + KEY);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICompositeType.k_struct; // or something
		}
	}

	private static class GetFields implements IPDOMVisitor {
		private List fields = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof IField) {
				IField field= (IField) node;
				if (IndexFilter.ALL_DECLARED_OR_IMPLICIT.acceptBinding(field)) {
					fields.add(node);
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IField[] getFields() {
			return (IField[])fields.toArray(new IField[fields.size()]);
		}
	}
	public IField[] getFields() throws DOMException {
		try {
			GetFields fields = new GetFields();
			accept(fields);
			return fields.getFields();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IField[0];
		}
	}

	public static class FindField implements IPDOMVisitor {
		private IField field;
		private final String name;
		public FindField(String name) {
			this.name = name;
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof IField) {
				IField tField = (IField)node;
				if (IndexFilter.ALL_DECLARED_OR_IMPLICIT.acceptBinding(tField)) {
					if (name.equals(tField.getName())) {
						field = tField;
						throw new CoreException(Status.OK_STATUS);
					}
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IField getField() { return field; }
	}
	
	public IField findField(String name) throws DOMException {
		final String key= pdom.createKeyForCache(record, name.toCharArray());
		IField result= (IField) pdom.getCachedResult(key);
		if (result != null) {
			return result;
		}

		FindField visitor = new FindField(name);
		try {
			accept(visitor);
			// returned => not found
			return null;
		} catch (CoreException e) {
			if (e.getStatus().equals(Status.OK_STATUS)) {
				result= visitor.getField();
			}
			else {
				CCorePlugin.log(e);
				return null;
			}
		}
		if (result != null) {
			pdom.putCachedResult(key, result);
		}
		return result;
	}

	public IScope getCompositeScope() throws DOMException {
		throw new PDOMNotImplementedError();
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
		
		if (type instanceof ICompositeType) {
			ICompositeType etype= (ICompositeType) type;
			etype= (ICompositeType) PDOMASTAdapter.getAdapterIfAnonymous(etype);
			try {
				return getDBName().equals(etype.getNameCharArray());
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public void addChild(PDOMNode member) throws CoreException {
		addMember(member);
	}
	
	public boolean mayHaveChildren() {
		return true;
	}

	public ICompositeType getCompositeType() {
		return this;
	}

	public IBinding getBinding(char[] name) throws DOMException {
		fail(); return null;
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		fail(); return null;
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) throws DOMException {
		fail(); return null;
	}
	
	public IBinding[] find(String name) throws DOMException {
		fail(); return null;
	}
	
	public IIndexBinding getScopeBinding() {
		return this;
	}
}
