/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
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
	private final ThreadLocal<Set<IBinding>> fInProgress= new ThreadLocal<Set<IBinding>>() {
		@Override
		protected Set<IBinding> initialValue() {
			return new HashSet<IBinding>();
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
		IBinding owner= getOwner();
		if (owner instanceof ICPPSpecialization) {
			return ((ICPPSpecialization) owner).getTemplateParameterMap();
		}
		return CPPTemplateParameterMap.EMPTY;
	}

	@Override
	public IBinding specializeMember(IBinding original) {
		return specializeMember(original, null);
	}
	
	@Override
	public IBinding specializeMember(IBinding original, IASTNode point) {
		if (specializationMap == null) {
			final Object key= CPPCompositesFactory.createSpecializationKey(cf, rbinding);
			final IIndexFragment frag= rbinding.getFragment();
			Object cached= frag.getCachedResult(key);
			if (cached != null) { 
				specializationMap= (ObjectMap) cached;
			} else {
				final ObjectMap newMap= new ObjectMap(2);
				// In any fragment explicit specializations may be defined.
				IIndexFragmentBinding[] frags= cf.findEquivalentBindings(rbinding);
				for (IIndexFragmentBinding fb : frags) {
					if (fb instanceof ICPPClassType) {
						final ICPPClassType[] nested = ClassTypeHelper.getNestedClasses((ICPPClassType) fb, point);
						if (nested.length > 0) {
							for (ICPPClassType ct : nested) {
								if (ct instanceof ICPPClassSpecialization && 
										!(ct.getCompositeScope() instanceof ICPPClassSpecializationScope)) {
									ICPPClassSpecialization cspec= (ICPPClassSpecialization) cf.getCompositeBinding((IIndexFragmentBinding) ct);
									newMap.put(cspec.getSpecializedBinding(), cspec);
								}
							}
							if (!newMap.isEmpty())
								break;
						}
					}
				}
				specializationMap= (ObjectMap) frag.putCachedResult(key, newMap, false);
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
	public final ICPPBase[] getBases() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getBases(null);
	}

	@Override
	public final ICPPBase[] getBases(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getBases(point);
		}
		ICPPBase[] bases = ClassTypeHelper.getBases((ICPPClassType) rbinding, point);
		return wrapBases(bases);
	}
	
	@Override
	public final ICPPConstructor[] getConstructors() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getConstructors(null);
	}

	@Override
	public final ICPPConstructor[] getConstructors(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getConstructors(point);
		}
		ICPPConstructor[] result = ClassTypeHelper.getConstructors((ICPPClassType) rbinding, point);
		return wrapBindings(result);
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
	public final ICPPMethod[] getDeclaredMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getDeclaredMethods(null);
	}

	@Override
	public final ICPPMethod[] getDeclaredMethods(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods(point);
		}
		ICPPMethod[] result = ClassTypeHelper.getDeclaredMethods((ICPPClassType) rbinding, point);
		return wrapBindings(result);
	}

	@Override
	public final ICPPMethod[] getAllDeclaredMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getAllDeclaredMethods(null);
	}

	@Override
	public final ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
		return ClassTypeHelper.getAllDeclaredMethods(this, point);
	}

	@Override
	public final ICPPField[] getDeclaredFields() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getDeclaredFields(null);
	}

	@Override
	public final ICPPField[] getDeclaredFields(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredFields(point);
		}
		ICPPField[] result = ClassTypeHelper.getDeclaredFields((ICPPClassType) rbinding, point);
		return wrapBindings(result);
	}

	@Override
	public IField[] getFields() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getFields(null);
	}

	@Override
	public final IField[] getFields(IASTNode point) {
		return ClassTypeHelper.getFields(this, point);
	}

	@Override
	public final IBinding[] getFriends() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getFriends(null);
	}

	@Override
	public final IBinding[] getFriends(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getFriends(point);
		}
		IBinding[] result = ClassTypeHelper.getFriends((ICPPClassType) rbinding, point);
		return wrapBindings(result);
	}

	@Override
	public final ICPPClassType[] getNestedClasses() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getNestedClasses(null);
	}

	@Override
	public final ICPPClassType[] getNestedClasses(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getNestedClasses(point);
		}
		ICPPClassType[] result = ClassTypeHelper.getNestedClasses((ICPPClassType) rbinding, point);
		return wrapBindings(result);
	}
	
	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getUsingDeclarations(null);
	}
	
	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point) {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getUsingDeclarations(point);
		}
		ICPPUsingDeclaration[] result = ClassTypeHelper.getUsingDeclarations((ICPPClassType) rbinding, point);
		return wrapBindings(result);
	}
}
