/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 */
class PDOMCPPClassType extends PDOMCPPBinding implements ICPPClassType,
		ICPPClassScope, IPDOMMemberOwner {

	private static final int FIRSTBASE = PDOMCPPBinding.RECORD_SIZE + 0;
	private static final int KEY = PDOMCPPBinding.RECORD_SIZE + 4; // byte
	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 8;
	
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 12;

	public PDOMCPPClassType(PDOM pdom, PDOMNode parent, IASTName name)
			throws CoreException {
		super(pdom, parent, name);

		IBinding binding = name.resolveBinding();
		try {
			int key = 0;
			if (binding instanceof ICPPClassType) // not sure why it wouldn't
				key = ((ICPPClassType) binding).getKey();
			pdom.getDB().putByte(record + KEY, (byte) key);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		// linked list is initialized by storage being zero'd by malloc
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}
	
	public PDOMCPPClassType(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPCLASSTYPE;
	}

	public PDOMCPPBase getFirstBase() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRSTBASE);
		return rec != 0 ? new PDOMCPPBase(pdom, rec) : null;
	}

	private void setFirstBase(PDOMCPPBase base) throws CoreException {
		int rec = base != null ? base.getRecord() : 0;
		pdom.getDB().putInt(record + FIRSTBASE, rec);
	}
	
	public void addBase(PDOMCPPBase base) throws CoreException {
		PDOMCPPBase firstBase = getFirstBase();
		base.setNextBase(firstBase);
		setFirstBase(base);
	}
	
	public boolean isSameType(IType type) {
		if (type instanceof PDOMBinding)
			return record == ((PDOMBinding)type).getRecord();
		else
			// TODO - should we check for real?
			return false;
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public IField findField(String name) throws DOMException {
		throw new PDOMNotImplementedError();
	}
	
	public ICPPBase[] getBases() throws DOMException {
		try {
			List list = new ArrayList();
			for (PDOMCPPBase base = getFirstBase(); base != null; base = base.getNextBase())
				list.add(base);
			ICPPBase[] bases = (ICPPBase[])list.toArray(new ICPPBase[list.size()]);
			ArrayUtil.reverse(bases);
			return bases;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPBase[0];
		}
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}
	
	public ICPPConstructor[] getConstructors() throws DOMException {
		// TODO
		return new ICPPConstructor[0];
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	private static class GetMethods implements IPDOMVisitor {
		private final List methods;
		public GetMethods(List methods) {
			this.methods = methods;
		}
		public GetMethods() {
			this.methods = new ArrayList();
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPMethod)
				methods.add(node);
			return false; // don't visit the method
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPMethod[] getMethods() {
			return (ICPPMethod[])methods.toArray(new ICPPMethod[methods.size()]); 
		}
	}
	
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		try {
			GetMethods methods = new GetMethods();
			accept(methods);
			return methods.getMethods();
		} catch (CoreException e) {
			return new ICPPMethod[0];
		}
	}

	private void visitAllDeclaredMethods(Set visited, List methods) throws CoreException {
		if (visited.contains(this))
			return;
		visited.add(this);
		
		// Get my members
		GetMethods myMethods = new GetMethods(methods);
		accept(myMethods);
		
		// Visit my base classes
		for (PDOMCPPBase base = getFirstBase(); base != null; base = base.getNextBase()) {
			try {
				IBinding baseClass = base.getBaseClass();
				if (baseClass != null && baseClass instanceof PDOMCPPClassType)
					((PDOMCPPClassType)baseClass).visitAllDeclaredMethods(visited, methods);
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}
	
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		List methods = new ArrayList();
		Set visited = new HashSet();
		try {
			visitAllDeclaredMethods(visited, methods);
			return (ICPPMethod[])methods.toArray(new ICPPMethod[methods.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPMethod[0];
		}
	}

	public ICPPMethod[] getMethods() throws DOMException {
		// TODO Should really include implicit methods too
		return getDeclaredMethods();
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
			GetFields visitor = new GetFields();
			accept(visitor);
			return visitor.getFields();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IField[0];
		}
	}

	public IBinding[] getFriends() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	private static class GetNestedClasses implements IPDOMVisitor {
		private List nestedClasses = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPClassType)
				nestedClasses.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPClassType[] getNestedClasses() {
			return (ICPPClassType[])nestedClasses.toArray(new ICPPClassType[nestedClasses.size()]);
		}
	}
	
	public ICPPClassType[] getNestedClasses() throws DOMException {
		try {
			GetNestedClasses visitor = new GetNestedClasses();
			accept(visitor);
			return visitor.getNestedClasses();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPClassType[0];
		}
	}

	public IScope getCompositeScope() throws DOMException {
		return this;
	}

	public int getKey() throws DOMException {
		try {
			return pdom.getDB().getByte(record + KEY);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPClassType.k_class; // or something
		}
	}
	
	public boolean isGloballyQualified() throws DOMException {
		try {
			return getParentNode() instanceof PDOMLinkage;
		} catch (CoreException e) {
			return true;
		}
	}

	public ICPPClassType getClassType() {
		return this;
	}

	public ICPPMethod[] getImplicitMethods() {
		throw new PDOMNotImplementedError();
	}

	public void addBinding(IBinding binding) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public void addName(IASTName name) throws DOMException {
		// TODO - this might be a better way of adding names to scopes
		// but for now do nothing.
	}

	public IBinding[] find(String name) throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return null;
	}

	public IScope getParent() throws DOMException {
		return null;
	}

	public IName getScopeName() throws DOMException {
		try {
			PDOMName name = getFirstDefinition();
			if (name == null)
				name = getFirstDefinition();
			return name;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public void removeBinding(IBinding binding) throws DOMException {
		throw new PDOMNotImplementedError();
	}
	
	public boolean mayHaveChildren() {
		return true;
	}
}
