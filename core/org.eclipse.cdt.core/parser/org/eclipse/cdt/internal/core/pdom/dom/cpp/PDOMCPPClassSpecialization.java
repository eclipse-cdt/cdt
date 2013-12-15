/*******************************************************************************
 * Copyright (c) 2007, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization.RecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
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
class PDOMCPPClassSpecialization extends PDOMCPPSpecialization implements
		ICPPClassSpecialization, IPDOMMemberOwner, IPDOMCPPClassType {
	private static final int FIRST_BASE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int MEMBERLIST = FIRST_BASE + 4;
	private static final int FINAL = MEMBERLIST + PDOMCPPMemberBlock.RECORD_SIZE; // byte

	/**
	 * The size in bytes of a PDOMCPPClassSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FINAL + 1;

	private volatile ICPPClassScope fScope;
	private ObjectMap specializationMap; // Obtained from the synchronized PDOM cache
	private final ThreadLocal<Set<IBinding>> fInProgress= new ThreadLocal<Set<IBinding>>() {
		@Override
		protected Set<IBinding> initialValue() {
			return new HashSet<IBinding>();
		}
	};

	public PDOMCPPClassSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPClassType classType,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) classType, specialized);
		setFinal(classType);
	}

	public PDOMCPPClassSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPClassType) {
			ICPPClassType ct= (ICPPClassType) newBinding;
			setFinal(ct);
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
		return specializeMember(original, null);
	}

	@Override
	public IBinding specializeMember(IBinding original, IASTNode point) {
		if (specializationMap == null) {
			final Long key= record + PDOMCPPLinkage.CACHE_INSTANCE_SCOPE;
			Object cached= getPDOM().getCachedResult(key);
			if (cached != null) {
				specializationMap= (ObjectMap) cached;
			} else {
				final ObjectMap newMap= new ObjectMap(2);
				try {
					PDOMClassUtil.NestedClassCollector visitor = new PDOMClassUtil.NestedClassCollector();
					PDOMCPPClassScope.acceptViaCache(this, visitor, false);
					final ICPPClassType[] nested= visitor.getNestedClasses();
					for (ICPPClassType classType : nested) {
						if (classType instanceof ICPPSpecialization) {
							newMap.put(((ICPPSpecialization) classType).getSpecializedBinding(), classType);
						}
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
				specializationMap= (ObjectMap) getPDOM().putCachedResult(key, newMap, false);
			}
		}
		synchronized (specializationMap) {
			IBinding result= (IBinding) specializationMap.get(original);
			if (result != null)
				return result;
		}

		IBinding newSpec;
		Set<IBinding> recursionProtectionSet= fInProgress.get();
		if (!recursionProtectionSet.add(original))
			return RecursionResolvingBinding.createFor(original, point);

		try {
			newSpec= CPPTemplates.createSpecialization(this, original, point);
		} finally {
			recursionProtectionSet.remove(original);
		}

		synchronized (specializationMap) {
			IBinding oldSpec= (IBinding) specializationMap.put(original, newSpec);
			if (oldSpec != null) {
				specializationMap.put(original, oldSpec);
				return oldSpec;
			}
		}
		return newSpec;
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		if (fScope == null) {
			try {
				if (hasOwnScope()) {
					fScope= new PDOMCPPClassScope(this);
					return fScope;
				}
			} catch (CoreException e) {
			}
			fScope= new PDOMCPPClassSpecializationScope(this);
		}
		return fScope;
	}

	protected boolean hasOwnScope() throws CoreException {
		return hasDefinition();
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
		getPDOM().removeCachedResult(record+PDOMCPPLinkage.CACHE_BASES);
		final PDOMLinkage linkage = getLinkage();
		PDOMCPPBase firstBase = getFirstBase();
		for (ICPPBase base : bases) {
			PDOMCPPBase nextBase= new PDOMCPPBase(linkage, base, classDefName);
			nextBase.setNextBase(firstBase);
			firstBase= nextBase;
		}
		setFirstBase(firstBase);
	}

	public void removeBases(PDOMName classDefName) throws CoreException {
		getPDOM().removeCachedResult(record+PDOMCPPLinkage.CACHE_BASES);
		PDOMCPPBase base= getFirstBase();
		PDOMCPPBase predecessor= null;
		long nameRec= classDefName.getRecord();
		boolean deleted= false;
		while (base != null) {
			PDOMCPPBase nextBase = base.getNextBase();
			long classDefRec= getDB().getRecPtr(base.getRecord() + PDOMCPPBase.CLASS_DEFINITION);
			if (classDefRec == nameRec) {
				deleted= true;
				base.delete();
			} else if (deleted) {
				deleted= false;
				if (predecessor == null) {
					setFirstBase(base);
				} else {
					predecessor.setNextBase(base);
				}
				predecessor= base;
			}
			base= nextBase;
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
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getBases(null);
	}

	@Override
	public ICPPBase[] getBases(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getBases(point);
		}

		// This is an explicit specialization.
		Long key= record + PDOMCPPLinkage.CACHE_BASES;
		ICPPBase[] bases= (ICPPBase[]) getPDOM().getCachedResult(key);
		if (bases != null)
			return bases;

		try {
			List<PDOMCPPBase> list = new ArrayList<PDOMCPPBase>();
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
	public ICPPConstructor[] getConstructors() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getConstructors(null);
	}

	@Override
	public ICPPConstructor[] getConstructors(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getConstructors(point);
		}
		try {
			PDOMClassUtil.ConstructorCollector visitor= new PDOMClassUtil.ConstructorCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getConstructors();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getDeclaredMethods(null);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods(point);
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
	public ICPPField[] getDeclaredFields() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getDeclaredFields(null);
	}

	@Override
	public ICPPField[] getDeclaredFields(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredFields(point);
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
	public ICPPClassType[] getNestedClasses() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getNestedClasses(null);
	}

	@Override
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getNestedClasses(point);
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
	public IBinding[] getFriends() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getFriends(null);
	}

	@Override
	public IBinding[] getFriends(IASTNode point) {
		ICPPClassScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope)
			return ((ICPPClassSpecializationScope) scope).getFriends(point);
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getMethods(null);
	}

	@Override
	public ICPPMethod[] getMethods(IASTNode point) {
		return ClassTypeHelper.getMethods(this, point);
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getAllDeclaredMethods(null);
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
		return ClassTypeHelper.getAllDeclaredMethods(this, point);
	}

	@Override
	public IField[] getFields() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getFields(null);
	}

	@Override
	public IField[] getFields(IASTNode point) {
		return ClassTypeHelper.getFields(this, point);
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
			PDOMNode node= (PDOMNode) type;
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
			return getDB().getByte(record + FINAL) != 0;
		} catch (CoreException e){
			CCorePlugin.log(e);
			return false;
		}
	}

	private void setFinal(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + FINAL, (byte) (ct.isFinal() ? 1 : 0));
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
}
