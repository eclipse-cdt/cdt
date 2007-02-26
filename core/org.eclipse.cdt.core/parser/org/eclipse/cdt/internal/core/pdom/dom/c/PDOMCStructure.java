/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
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
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
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
		// linked list is initialized by malloc zeroing allocated storage
		try {
			pdom.getDB().putByte(record + KEY, (byte) compType.getKey());
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCStructure(PDOM pdom, int record) {
		super(pdom, record);
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		new PDOMNodeLinkedList(pdom, record+MEMBERLIST, getLinkageImpl()).accept(visitor);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		new PDOMNodeLinkedList(pdom, record+MEMBERLIST, getLinkageImpl()).addMember(member);
	}
	
	public int getNodeType() {
		return PDOMCLinkage.CSTRUCTURE;
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
			if (node instanceof IField)
				fields.add(node);
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
				if (name.equals(tField.getName())) {
					field = tField;
					throw new CoreException(Status.OK_STATUS);
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IField getField() { return field; }
	}
	
	public IField findField(String name) throws DOMException {
		FindField field = new FindField(name);
		try {
			accept(field);
			// returned => not found
			return null;
		} catch (CoreException e) {
			if (e.getStatus().equals(Status.OK_STATUS))
				return field.getField();
			else {
				CCorePlugin.log(e);
				return null;
			}
		}
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
	
	public IBinding[] find(String name) throws DOMException {
		return find(name, false);
	}
	
	public IBinding[] find(String name, boolean prefixLookup) throws DOMException {
		try {
			BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray(), null, prefixLookup, !prefixLookup);
			accept(visitor);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public IIndexBinding getScopeBinding() {
		return this;
	}
}
