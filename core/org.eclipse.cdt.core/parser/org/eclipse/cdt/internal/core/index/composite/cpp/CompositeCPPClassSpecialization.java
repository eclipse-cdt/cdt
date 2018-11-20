/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization.RecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassSpecialization extends CompositeCPPClassType implements ICPPClassSpecialization {
	private ObjectMap specializationMap;
	private final ThreadLocal<Set<IBinding>> fInProgress = new ThreadLocal<Set<IBinding>>() {
		@Override
		protected Set<IBinding> initialValue() {
			return new HashSet<>();
		}
	};

	public CompositeCPPClassSpecialization(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		return (ICPPClassScope) cf.getCompositeScope((IIndexScope) ((ICPPClassType) rbinding).getCompositeScope());
	}

	@Override
	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		IBinding owner = getOwner();
		if (owner instanceof ICPPSpecialization) {
			return ((ICPPSpecialization) owner).getTemplateParameterMap();
		}
		return CPPTemplateParameterMap.EMPTY;
	}

	@Override
	public IBinding specializeMember(IBinding original) {
		if (specializationMap == null) {
			final Object key = CPPCompositesFactory.createSpecializationKey(cf, rbinding);
			final IIndexFragment frag = rbinding.getFragment();
			Object cached = frag.getCachedResult(key);
			if (cached != null) {
				specializationMap = (ObjectMap) cached;
			} else {
				final ObjectMap newMap = new ObjectMap(2);
				// In any fragment explicit specializations may be defined.
				IIndexFragmentBinding[] frags = cf.findEquivalentBindings(rbinding);
				for (IIndexFragmentBinding fb : frags) {
					if (fb instanceof ICPPClassType) {
						final ICPPClassType[] nested = ((ICPPClassType) fb).getNestedClasses();
						if (nested.length > 0) {
							for (ICPPClassType ct : nested) {
								if (ct instanceof ICPPClassSpecialization
										&& !(ct.getCompositeScope() instanceof ICPPClassSpecializationScope)) {
									ICPPClassSpecialization cspec = (ICPPClassSpecialization) cf
											.getCompositeBinding((IIndexFragmentBinding) ct);
									newMap.put(cspec.getSpecializedBinding(), cspec);
								}
							}
							if (!newMap.isEmpty())
								break;
						}
					}
				}
				specializationMap = (ObjectMap) frag.putCachedResult(key, newMap, false);
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
	public final ICPPBase[] getBases() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getBases();
		}
		ICPPBase[] bases = ((ICPPClassType) rbinding).getBases();
		return wrapBases(bases);
	}

	@Override
	@Deprecated
	public final ICPPBase[] getBases(IASTNode point) {
		return getBases();
	}

	@Override
	public final ICPPConstructor[] getConstructors() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassScope) {
			return ((ICPPClassScope) scope).getConstructors();
		}
		ICPPConstructor[] result = ((ICPPClassType) rbinding).getConstructors();
		return wrapBindings(result);
	}

	@Override
	@Deprecated
	public final ICPPConstructor[] getConstructors(IASTNode point) {
		return getConstructors();
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
	public final ICPPMethod[] getDeclaredMethods() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods();
		}
		ICPPMethod[] result = ((ICPPClassType) rbinding).getDeclaredMethods();
		return wrapBindings(result);
	}

	@Override
	@Deprecated
	public final ICPPMethod[] getDeclaredMethods(IASTNode point) {
		return getDeclaredMethods();
	}

	@Override
	public final ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	@Override
	@Deprecated
	public final ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
		return getAllDeclaredMethods();
	}

	@Override
	public final ICPPField[] getDeclaredFields() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredFields();
		}
		ICPPField[] result = ((ICPPClassType) rbinding).getDeclaredFields();
		return wrapBindings(result);
	}

	@Override
	@Deprecated
	public final ICPPField[] getDeclaredFields(IASTNode point) {
		return getDeclaredFields();
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
	@Deprecated
	public final IField[] getFields(IASTNode point) {
		return getFields();
	}

	@Override
	public final IBinding[] getFriends() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getFriends();
		}
		IBinding[] result = ((ICPPClassType) rbinding).getFriends();
		return wrapBindings(result);
	}

	@Override
	@Deprecated
	public final IBinding[] getFriends(IASTNode point) {
		return getFriends();
	}

	@Override
	public final ICPPClassType[] getNestedClasses() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getNestedClasses();
		}
		ICPPClassType[] result = ((ICPPClassType) rbinding).getNestedClasses();
		return wrapBindings(result);
	}

	@Override
	@Deprecated
	public final ICPPClassType[] getNestedClasses(IASTNode point) {
		return getNestedClasses();
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		IScope scope = getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getUsingDeclarations();
		}
		ICPPUsingDeclaration[] result = ((ICPPClassType) rbinding).getUsingDeclarations();
		return wrapBindings(result);
	}

	@Override
	@Deprecated
	public ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point) {
		return getUsingDeclarations();
	}
}
