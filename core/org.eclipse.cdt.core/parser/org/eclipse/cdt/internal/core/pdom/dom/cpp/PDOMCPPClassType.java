/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPClassType extends PDOMCPPBinding implements IPDOMCPPClassType, IPDOMMemberOwner {
	private static final int FIRSTBASE = PDOMCPPBinding.RECORD_SIZE;
	private static final int MEMBERLIST = FIRSTBASE + 4;
	private static final int FIRSTFRIEND = MEMBERLIST + PDOMCPPMemberBlock.RECORD_SIZE;
	private static final int KEY = FIRSTFRIEND + 4; // byte
	private static final int ANONYMOUS = KEY + 1; // byte
	private static final int FINAL = ANONYMOUS + 1; // byte
	private static final int VISIBLE_TO_ADL_ONLY = FINAL + 1; // byte
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = VISIBLE_TO_ADL_ONLY + 1;

	private PDOMCPPClassScope fScope; // No need for volatile, all fields of PDOMCPPClassScope are final.

	public PDOMCPPClassType(PDOMLinkage linkage, PDOMNode parent, ICPPClassType classType, boolean visibleToAdlOnly)
			throws CoreException {
		super(linkage, parent, classType.getNameCharArray());

		setKind(classType);
		setAnonymous(classType);
		setFinal(classType);
		setVisibleToAdlOnly(visibleToAdlOnly);
		// Linked list is initialized by storage being zero'd by malloc.
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
			ICPPClassType ct = (ICPPClassType) newBinding;
			setKind(ct);
			setAnonymous(ct);
			setFinal(ct);
			super.update(linkage, newBinding);
		}
	}

	private void setKind(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + KEY, (byte) ct.getKey());
	}

	private void setAnonymous(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + ANONYMOUS, (byte) (ct.isAnonymous() ? 1 : 0));
	}

	private void setFinal(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + FINAL, (byte) (ct.isFinal() ? 1 : 0));
	}

	@Override
	public void setVisibleToAdlOnly(boolean visibleToAdlOnly) throws CoreException {
		getDB().putByte(record + VISIBLE_TO_ADL_ONLY, (byte) (visibleToAdlOnly ? 1 : 0));
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
	}

	@Override
	public final void addChild(PDOMNode member) throws CoreException {
		throw new UnsupportedOperationException("addMember should be called instead to add " + //$NON-NLS-1$
				(member instanceof IBinding ? ((IBinding) member).getName() : member.toString()) + " to " + getName()); //$NON-NLS-1$
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPClassScope.acceptViaCache(this, visitor, false);
	}

	/**
	 * Called to populate the cache for the bindings in the class scope.
	 */
	@Override
	public void acceptUncached(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMCPPMemberBlock list = new PDOMCPPMemberBlock(getLinkage(), record + MEMBERLIST);
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

	public void addBases(PDOMName classDefName, ICPPBase[] bases) throws CoreException {
		getPDOM().removeCachedResult(record + PDOMCPPLinkage.CACHE_BASES);
		final PDOMLinkage linkage = getLinkage();
		PDOMCPPBase firstBase = getFirstBase();
		for (ICPPBase base : bases) {
			PDOMCPPBase nextBase = new PDOMCPPBase(linkage, base, classDefName);
			nextBase.setNextBase(firstBase);
			firstBase = nextBase;
		}
		setFirstBase(firstBase);
	}

	public void removeBases(PDOMName classDefName) throws CoreException {
		final PDOM pdom = getPDOM();
		final Database db = getDB();
		pdom.removeCachedResult(record + PDOMCPPLinkage.CACHE_BASES);

		PDOMCPPBase base = getFirstBase();
		PDOMCPPBase prevBase = null;
		long nameRec = classDefName.getRecord();
		boolean deleted = false;
		while (base != null) {
			PDOMCPPBase nextBase = base.getNextBase();
			long classDefRec = db.getRecPtr(base.getRecord() + PDOMCPPBase.CLASS_DEFINITION);
			if (classDefRec == nameRec) {
				deleted = true;
				base.delete();
			} else {
				if (deleted) {
					deleted = false;
					if (prevBase == null) {
						setFirstBase(base);
					} else {
						prevBase.setNextBase(base);
					}
				}
				prevBase = base;
			}
			base = nextBase;
		}
		if (deleted) {
			if (prevBase == null) {
				setFirstBase(null);
			} else {
				prevBase.setNextBase(null);
			}
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
		PDOMCPPFriend predecessor = null;
		long nameRec = pdomName.getRecord();
		while (friend != null) {
			PDOMName name = friend.getSpecifierName();
			if (name != null && name.getRecord() == nameRec) {
				break;
			}
			predecessor = friend;
			friend = friend.getNextFriend();
		}
		if (friend != null) {
			if (predecessor != null) {
				predecessor.setNextFriend(friend.getNextFriend());
			} else {
				setFirstFriend(friend.getNextFriend());
			}
			friend.delete();
		}
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		if (fScope == null) {
			fScope = new PDOMCPPClassScope(this);
		}
		return fScope;
	}

	@Override
	public int getKey() {
		try {
			return getDB().getByte(record + KEY);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPClassType.k_class; // or something
		}
	}

	@Override
	public boolean isAnonymous() {
		try {
			return getDB().getByte(record + ANONYMOUS) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public boolean isFinal() {
		try {
			return getDB().getByte(record + FINAL) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	@Override
	public boolean isVisibleToAdlOnly() {
		try {
			return getDB().getByte(record + VISIBLE_TO_ADL_ONLY) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
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

		if (type instanceof ICPPClassType && !(type instanceof ProblemBinding)) {
			ICPPClassType ctype = (ICPPClassType) type;
			if (getEquivalentKind(ctype) != getEquivalentKind(this))
				return false;
			char[] nchars = ctype.getNameCharArray();
			if (nchars.length == 0) {
				nchars = ASTTypeUtil.createNameForAnonymous(ctype);
			}
			if (nchars == null || !CharArrayUtils.equals(nchars, getNameCharArray()))
				return false;

			return SemanticUtil.haveSameOwner(this, ctype);
		}
		return false;
	}

	private static int getEquivalentKind(ICPPClassType classType) {
		int key = classType.getKey();
		return key == k_class ? k_struct : key;
	}

	@Override
	public ICPPBase[] getBases() {
		Long key = record + PDOMCPPLinkage.CACHE_BASES;
		ICPPBase[] bases = (ICPPBase[]) getPDOM().getCachedResult(key);
		if (bases != null)
			return bases;

		try {
			List<PDOMCPPBase> list = new ArrayList<>();
			for (PDOMCPPBase base = getFirstBase(); base != null; base = base.getNextBase()) {
				list.add(base);
			}
			Collections.reverse(list);
			bases = list.toArray(new ICPPBase[list.size()]);
			getPDOM().putCachedResult(key, bases);
			return bases;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPBase.EMPTY_BASE_ARRAY;
		}
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		PDOMClassUtil.ConstructorCollector visitor = new PDOMClassUtil.ConstructorCollector();
		try {
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			ICPPConstructor[] constructors = visitor.getConstructors();
			return ClassTypeHelper.getAllConstructors(this, constructors);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		try {
			PDOMClassUtil.UsingDeclarationCollector visitor = new PDOMClassUtil.UsingDeclarationCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getUsingDeclarations();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
		}
	}

	@Override
	public IBinding[] getFriends() {
		try {
			final List<IBinding> list = new ArrayList<>();
			for (PDOMCPPFriend friend = getFirstFriend(); friend != null; friend = friend.getNextFriend()) {
				list.add(0, friend.getFriendSpecifier());
			}
			return list.toArray(new IBinding[list.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return IBinding.EMPTY_BINDING_ARRAY;
		}
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this);
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
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

	@Override
	public void addMember(PDOMNode member, int visibility) {
		try {
			PDOMCPPMemberBlock members = new PDOMCPPMemberBlock(getLinkage(), record + MEMBERLIST);
			members.addMember(member, visibility);
			PDOMCPPClassScope.updateCache(this, member);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public int getVisibility(IBinding member) {
		try {
			PDOMCPPMemberBlock members = new PDOMCPPMemberBlock(getLinkage(), record + MEMBERLIST);
			int visibility = members.getVisibility(member);
			if (visibility < 0)
				throw new IllegalArgumentException(member.getName() + " is not a member of " + getName()); //$NON-NLS-1$
			return visibility;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return v_private; // Fallback visibility
		}
	}
}
