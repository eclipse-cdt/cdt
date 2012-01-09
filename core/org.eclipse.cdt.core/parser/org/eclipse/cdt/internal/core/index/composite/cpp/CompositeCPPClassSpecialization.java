/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
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
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassSpecialization extends CompositeCPPClassType implements ICPPClassSpecialization {
	
	private ObjectMap specializationMap= null;

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
		if (specializationMap == null) {
			final Object key= CPPCompositesFactory.createSpecializationKey(cf, rbinding);
			final IIndexFragment frag= rbinding.getFragment();
			Object cached= frag.getCachedResult(key);
			if (cached != null) { 
				specializationMap= (ObjectMap) cached;
			} else {
				final ObjectMap newMap= new ObjectMap(2);
				// in any fragment explicit specializations may be defined.
				IIndexFragmentBinding[] frags= cf.findEquivalentBindings(rbinding);
				for (IIndexFragmentBinding fb : frags) {
					if (fb instanceof ICPPClassType) {
						final ICPPClassType[] nested = ((ICPPClassType)fb).getNestedClasses();
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
		IBinding newSpec= CPPTemplates.createSpecialization(this, original);
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
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getBases();
		}
		return super.getBases();
	}
	
	@Override
	public final ICPPConstructor[] getConstructors() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getConstructors();
		}
		return super.getConstructors();
	}

	@Override
	public final ICPPMethod[] getDeclaredMethods() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods();
		}
		return super.getDeclaredMethods();
	}

	@Override
	public final ICPPField[] getDeclaredFields() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredFields();
		}
		return super.getDeclaredFields();
	}

	@Override
	public final IBinding[] getFriends() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getFriends();
		}
		return super.getFriends();
	}

	@Override
	public final ICPPClassType[] getNestedClasses() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getNestedClasses();
		}
		return super.getNestedClasses();
	}
	
	@Override
	@Deprecated
	public ObjectMap getArgumentMap() {
		return TemplateInstanceUtil.getArgumentMap(cf, rbinding);
	}
}
