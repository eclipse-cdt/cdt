/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 */
class PDOMCPPClassType extends PDOMCPPBinding implements IPDOMCPPClassType, IPDOMMemberOwner {

	private static final int FIRSTBASE = PDOMCPPBinding.RECORD_SIZE + 0;

	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 4;

	private static final int FIRSTFRIEND = PDOMCPPBinding.RECORD_SIZE + 8;
	
	private static final int KEY = PDOMCPPBinding.RECORD_SIZE + 12; // byte
	private static final int ANONYMOUS= PDOMCPPBinding.RECORD_SIZE + 13; // byte
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 14;

	private ICPPClassScope fScope;

	public PDOMCPPClassType(PDOMLinkage linkage, PDOMNode parent, ICPPClassType classType) throws CoreException {
		super(linkage, parent, classType.getNameCharArray());

		setKind(classType);
		setAnonymous(classType);
		// linked list is initialized by storage being zero'd by malloc
	}

	public PDOMCPPClassType(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPCLASSTYPE;
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPClassType) {
			ICPPClassType ct= (ICPPClassType) newBinding;
			setKind(ct);
			setAnonymous(ct);
			super.update(linkage, newBinding);
		}
	}

	private void setKind(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + KEY, (byte) ct.getKey());
	}

	private void setAnonymous(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + ANONYMOUS, (byte) (ct.isAnonymous() ? 1 : 0));
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
	}
	
	@Override
	public final void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.addMember(member);
		PDOMCPPClassScope.updateCache(this, member);
	}
	
	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPClassScope.acceptViaCache(this, visitor, false);
	}

	/**
	 * Called to populate the cache for the bindings in the class scope.
	 */
	public void acceptUncached(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.accept(visitor);
	}

	private PDOMCPPBase getFirstBase() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRSTBASE);
		return rec != 0 ? new PDOMCPPBase(getLinkage(), rec) : null;
	}

	private void setFirstBase(PDOMCPPBase base) throws CoreException {
		long rec = base != null ? base.getRecord() : 0;
		getDB().putRecPtr(record + FIRSTBASE, rec);
	}

	public void addBase(PDOMCPPBase base) throws CoreException {
		getPDOM().removeCachedResult(record+PDOMCPPLinkage.CACHE_BASES);
		PDOMCPPBase firstBase = getFirstBase();
		base.setNextBase(firstBase);
		setFirstBase(base);
	}

	public void removeBase(PDOMName pdomName) throws CoreException {
		getPDOM().removeCachedResult(record+PDOMCPPLinkage.CACHE_BASES);

		PDOMCPPBase base= getFirstBase();
		PDOMCPPBase predecessor= null;
		long nameRec= pdomName.getRecord();
		while (base != null) {
			PDOMName name = base.getBaseClassSpecifierName();
			if (name != null && name.getRecord() == nameRec) {
				break;
			}
			predecessor= base;
			base= base.getNextBase();
		}
		if (base != null) {
			if (predecessor != null) {
				predecessor.setNextBase(base.getNextBase());
			}
			else {
				setFirstBase(base.getNextBase());
			}
			base.delete();
		}
	}
	
	public void addFriend(PDOMCPPFriend friend) throws CoreException {
		PDOMCPPFriend firstFriend = getFirstFriend();
		friend.setNextFriend(firstFriend);
		setFirstFriend(friend);
	}

	private PDOMCPPFriend getFirstFriend() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRSTFRIEND);
		return rec != 0 ? new PDOMCPPFriend(getLinkage(), rec) : null;
	}

	private void setFirstFriend(PDOMCPPFriend friend) throws CoreException {
		long rec = friend != null ? friend.getRecord() : 0;
		getDB().putRecPtr(record + FIRSTFRIEND, rec);
	}

	public void removeFriend(PDOMName pdomName) throws CoreException {
		PDOMCPPFriend friend = getFirstFriend();
		PDOMCPPFriend predecessor= null;
		long nameRec= pdomName.getRecord();
		while (friend != null) {
			PDOMName name = friend.getSpecifierName();
			if (name != null && name.getRecord() == nameRec) {
				break;
			}
			predecessor= friend;
			friend= friend.getNextFriend();
		}
		if (friend != null) {
			if (predecessor != null) {
				predecessor.setNextFriend(friend.getNextFriend());
			}
			else {
				setFirstFriend(friend.getNextFriend());
			}
			friend.delete();
		}
	}

	public ICPPClassScope getCompositeScope() {
		if (fScope == null) {
			fScope= new PDOMCPPClassScope(this);
		}
		return fScope;
	}

	public int getKey() {
		try {
			return getDB().getByte(record + KEY);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPClassType.k_class; // or something
		}
	}

	public boolean isAnonymous() {
		try {
			return getDB().getByte(record + ANONYMOUS) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false; 
		}
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
		
		if (type instanceof ICPPClassType && !(type instanceof ProblemBinding)) {
			ICPPClassType ctype= (ICPPClassType) type;
			if (ctype.getKey() != getKey())
				return false;
			char[] nchars = ctype.getNameCharArray();
			if (nchars.length == 0) {
				nchars= ASTTypeUtil.createNameForAnonymous(ctype);
			}
			if (nchars == null || !CharArrayUtils.equals(nchars, getNameCharArray()))
				return false;

			return SemanticUtil.isSameOwner(getOwner(), ctype.getOwner());
		}
		return false;
	}

	public ICPPBase[] getBases() {
		Long key= record + PDOMCPPLinkage.CACHE_BASES;
		ICPPBase[] bases= (ICPPBase[]) getPDOM().getCachedResult(key);
		if (bases != null) 
			return bases;
		
		try {
			List<PDOMCPPBase> list = new ArrayList<PDOMCPPBase>();
			for (PDOMCPPBase base = getFirstBase(); base != null; base = base.getNextBase())
				list.add(base);
			Collections.reverse(list);
			bases = list.toArray(new ICPPBase[list.size()]);
			getPDOM().putCachedResult(key, bases);
			return bases;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPBase[0];
		}
	}

	public ICPPConstructor[] getConstructors() {
		PDOMClassUtil.ConstructorCollector visitor= new PDOMClassUtil.ConstructorCollector();
		try {
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getConstructors();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
	}

	public ICPPMethod[] getDeclaredMethods() {
		try {
			PDOMClassUtil.MethodCollector methods = new PDOMClassUtil.MethodCollector(false);
			PDOMCPPClassScope.acceptViaCache(this, methods, false);
			return methods.getMethods();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
	}

	public ICPPField[] getDeclaredFields() {
		try {
			PDOMClassUtil.FieldCollector visitor = new PDOMClassUtil.FieldCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getFields();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPField.EMPTY_CPPFIELD_ARRAY;
		}
	}

	public ICPPClassType[] getNestedClasses() {
		try {
			PDOMClassUtil.NestedClassCollector visitor = new PDOMClassUtil.NestedClassCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getNestedClasses();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPClassType.EMPTY_CLASS_ARRAY;
		}
	}

	public IBinding[] getFriends() {
		try {
			final List<IBinding> list = new ArrayList<IBinding>();
			for (PDOMCPPFriend friend = getFirstFriend();
					friend != null; friend = friend.getNextFriend()) {
				list.add(0, friend.getFriendSpecifier());
			}
			return list.toArray(new IBinding[list.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IBinding[0];
		}
	}

	public ICPPMethod[] getMethods() { 
		return ClassTypeHelper.getMethods(this);
	}

	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}
	
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}
	
	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}

	@Override
	public Object clone() {		
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
}
