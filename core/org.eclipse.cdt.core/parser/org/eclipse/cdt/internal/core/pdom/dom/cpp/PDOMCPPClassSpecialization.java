/*******************************************************************************
 * Copyright (c) 2007, 2015 QNX Software Systems and others.
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
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization.RecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPClassSpecialization extends PDOMCPPSpecialization
		implements ICPPClassSpecialization, IPDOMMemberOwner, IPDOMCPPClassType {
	private static final int FIRST_BASE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int MEMBERLIST = FIRST_BASE + 4;
	private static final int FLAGS = MEMBERLIST + PDOMCPPMemberBlock.RECORD_SIZE; // byte

	/**
	 * The size in bytes of a PDOMCPPClassSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FLAGS + 1;

	private static final byte FLAGS_FINAL = 0x01;
	private static final byte FLAGS_HAS_OWN_SCOPE = 0x02;

	private volatile ICPPClassScope fScope;
	private ObjectMap specializationMap; // Obtained from the synchronized PDOM cache.
	private final ThreadLocal<Set<IBinding>> fInProgress = new ThreadLocal<Set<IBinding>>() {
		@Override
		protected Set<IBinding> initialValue() {
			return new HashSet<>();
		}
	};

	public PDOMCPPClassSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPClassType classType,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) classType, specialized);
		setFlags(classType);
	}

	public PDOMCPPClassSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) newBinding;
			setFlags(classType);
			super.update(linkage, newBinding);
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_SPECIALIZATION;
	}

	@Override
	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) super.getSpecializedBinding();
	}

	@Override
	public IBinding specializeMember(IBinding original) {
		if (specializationMap == null) {
			final Long key = record + PDOMCPPLinkage.CACHE_INSTANCE_SCOPE;
			Object cached = getPDOM().getCachedResult(key);
			if (cached != null) {
				specializationMap = (ObjectMap) cached;
			} else {
				final ObjectMap newMap = new ObjectMap(2);
				try {
					PDOMClassUtil.NestedClassCollector visitor = new PDOMClassUtil.NestedClassCollector();
					PDOMCPPClassScope.acceptViaCache(this, visitor, false);
					final ICPPClassType[] nested = visitor.getNestedClasses();
					for (ICPPClassType classType : nested) {
						if (classType instanceof ICPPSpecialization) {
							newMap.put(((ICPPSpecialization) classType).getSpecializedBinding(), classType);
						}
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
				specializationMap = (ObjectMap) getPDOM().putCachedResult(key, newMap, false);
			}
		}
		synchronized (specializationMap) {
			IBinding result = (IBinding) specializationMap.get(original);
			if (result != null)
				return result;
		}

		IBinding newSpec;
		Set<IBinding> recursionProtectionSet = fInProgress.get();
		if (!recursionProtectionSet.add(original))
			return RecursionResolvingBinding.createFor(original);

		try {
			newSpec = CPPTemplates.createSpecialization(this, original);
		} finally {
			recursionProtectionSet.remove(original);
		}

		synchronized (specializationMap) {
			IBinding oldSpec = (IBinding) specializationMap.put(original, newSpec);
			if (oldSpec != null) {
				specializationMap.put(original, oldSpec);
				return oldSpec;
			}
		}
		return newSpec;
	}

	@Override
	@Deprecated
	public IBinding specializeMember(IBinding original, IASTNode point) {
		return specializeMember(original);
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		if (fScope == null) {
			try {
				if (hasOwnScope()) {
					fScope = new PDOMCPPClassScope(this);
					return fScope;
				}
			} catch (CoreException e) {
			}
			fScope = new PDOMCPPClassSpecializationScope(this);
		}
		return fScope;
	}

	public PDOMCPPBase getFirstBase() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRST_BASE);
		return rec != 0 ? new PDOMCPPBase(getLinkage(), rec) : null;
	}

	private void setFirstBase(PDOMCPPBase base) throws CoreException {
		long rec = base != null ? base.getRecord() : 0;
		getDB().putRecPtr(record + FIRST_BASE, rec);
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
		getPDOM().removeCachedResult(record + PDOMCPPLinkage.CACHE_BASES);
		PDOMCPPBase base = getFirstBase();
		PDOMCPPBase predecessor = null;
		long nameRec = classDefName.getRecord();
		boolean deleted = false;
		while (base != null) {
			PDOMCPPBase nextBase = base.getNextBase();
			long classDefRec = getDB().getRecPtr(base.getRecord() + PDOMCPPBase.CLASS_DEFINITION);
			if (classDefRec == nameRec) {
				deleted = true;
				base.delete();
			} else if (deleted) {
				deleted = false;
				if (predecessor == null) {
					setFirstBase(base);
				} else {
					predecessor.setNextBase(base);
				}
				predecessor = base;
			}
			base = nextBase;
		}
		if (deleted) {
			if (predecessor == null) {
				setFirstBase(null);
			} else {
				predecessor.setNextBase(null);
			}
		}
	}

	@Override
	public ICPPBase[] getBases() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getBases();
		}

		// This is an explicit specialization.
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
		}
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	@Deprecated
	public ICPPBase[] getBases(IASTNode point) {
		return getBases();
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			ICPPConstructor[] constructors = ((ICPPClassSpecializationScope) scope).getConstructors();
			return ClassTypeHelper.getAllConstructors(this, constructors);
		}

		try {
			PDOMClassUtil.ConstructorCollector visitor = new PDOMClassUtil.ConstructorCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			ICPPConstructor[] constructors = visitor.getConstructors();
			return ClassTypeHelper.getAllConstructors(this, constructors);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
	}

	@Override
	@Deprecated
	public ICPPConstructor[] getConstructors(IASTNode point) {
		return getConstructors();
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods();
		}
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
	@Deprecated
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		return getDeclaredMethods();
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredFields();
		}
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
	@Deprecated
	public ICPPField[] getDeclaredFields(IASTNode point) {
		return getDeclaredFields();
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getNestedClasses();
		}
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
	@Deprecated
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		return getNestedClasses();
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getUsingDeclarations();
		}
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
	@Deprecated
	public ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point) {
		return getUsingDeclarations();
	}

	@Override
	public IBinding[] getFriends() {
		ICPPClassScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope)
			return ((ICPPClassSpecializationScope) scope).getFriends();
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	@Deprecated
	public IBinding[] getFriends(IASTNode point) {
		return getFriends();
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this);
	}

	@Override
	@Deprecated
	public ICPPMethod[] getMethods(IASTNode point) {
		return getMethods();
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	@Override
	@Deprecated
	public ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
		return getAllDeclaredMethods();
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
	@Deprecated
	public IField[] getFields(IASTNode point) {
		return getFields();
	}

	@Override
	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}

	@Override
	public int getKey() {
		return getSpecializedBinding().getKey();
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (type instanceof PDOMNode) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}

		// require a class specialization
		if (!(type instanceof ICPPClassSpecialization))
			return false;

		return CPPClassSpecialization.isSameClassSpecialization(this, (ICPPClassSpecialization) type);
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
	public void addChild(PDOMNode member) throws CoreException {
		throw new UnsupportedOperationException("addMember method should be called instead."); //$NON-NLS-1$
	}

	@Override
	public void acceptUncached(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPMemberBlock members = new PDOMCPPMemberBlock(getLinkage(), record + MEMBERLIST);
		members.accept(visitor);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPClassScope.acceptViaCache(this, visitor, false);
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public boolean isFinal() {
		try {
			return (getFlags() & FLAGS_FINAL) != 0;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	private boolean hasOwnScope() throws CoreException {
		return (getFlags() & FLAGS_HAS_OWN_SCOPE) != 0;
	}

	private byte getFlags() throws CoreException {
		return getDB().getByte(record + FLAGS);
	}

	private void setFlags(ICPPClassType classType) throws CoreException {
		byte flags = (byte) ((classType.isFinal() ? FLAGS_FINAL : 0)
				| (hasOwnScope(classType) ? FLAGS_HAS_OWN_SCOPE : 0));
		getDB().putByte(record + FLAGS, flags);
	}

	/**
	 * Returns true if the given class is an explicit template specialization that has its own definition.
	 */
	private static boolean hasOwnScope(ICPPClassType classType) {
		if (!(classType instanceof ICPPInternalBinding))
			return false;

		ICPPInternalBinding binding = (ICPPInternalBinding) classType;
		if (binding.getDefinition() != null)
			return true;
		return false;
	}

	@Override
	public void addMember(PDOMNode member, int visibility) {
		try {
			PDOMCPPMemberBlock members = new PDOMCPPMemberBlock(getLinkage(), record + MEMBERLIST);
			members.addMember(member, visibility);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public int getVisibility(IBinding member) {
		try {
			PDOMCPPMemberBlock members = new PDOMCPPMemberBlock(getLinkage(), record + MEMBERLIST);
			int visibility = members.getVisibility(member);
			if (visibility < 0) {
				if (member instanceof ICPPSpecialization) {
					return getSpecializedBinding().getVisibility(((ICPPSpecialization) member).getSpecializedBinding());
				}
				throw new IllegalArgumentException(member.getName() + " is not a member of " + getName()); //$NON-NLS-1$
			}
			return visibility;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return v_private; // Fallback visibility
		}
	}

	// Class specializations do not need to be marked "visible to ADL only"
	// independent of their specialized class types, so they do not need
	// to implement these methods.
	@Override
	public boolean isVisibleToAdlOnly() {
		return false;
	}

	@Override
	public void setVisibleToAdlOnly(boolean visibleToAdlOnly) throws CoreException {
	}
}
