/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassSpecialization extends CompositeCPPClassType implements ICPPClassSpecialization {
	
	private ObjectMap specializationMap= null;

	public CompositeCPPClassSpecialization(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	public ObjectMap getArgumentMap() {
		return TemplateInstanceUtil.getArgumentMap(cf, rbinding);
	}

	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) TemplateInstanceUtil.getSpecializedBinding(cf, rbinding);
	}

	public IBinding specializeMember(IBinding original) {
		if (specializationMap == null) {
			final Object key= CPPCompositesFactory.createSpecializationKey(cf, rbinding);
			final IIndexFragment frag= rbinding.getFragment();
			Object cached= frag.getCachedResult(key);
			if (cached instanceof ObjectMap) { 
				specializationMap= (ObjectMap) cached;
			} else {
				final ObjectMap newMap= new ObjectMap(2);
				frag.putCachedResult(key, newMap);
				specializationMap= newMap;
			}
		}
		IBinding result= (IBinding) specializationMap.get(original);
		if (result == null) {
			result= CPPTemplates.createSpecialization(this, original, getArgumentMap());
			specializationMap.put(original, result);
		}
		return result;
	}

	@Override
	public ICPPBase[] getBases() throws DOMException {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			// this is an implicit specialization
			final ICPPBase[] pdomBases = (getSpecializedBinding()).getBases();
			if (pdomBases != null) {
				ICPPBase[] result = null;
				for (ICPPBase origBase : pdomBases) {
					ICPPBase specBase = (ICPPBase) ((ICPPInternalBase)origBase).clone();
					IBinding origClass = origBase.getBaseClass();
					if (origClass instanceof IType) {
						IType specClass = CPPTemplates.instantiateType((IType) origClass, getArgumentMap(), this);
						specClass = SemanticUtil.getUltimateType(specClass, true);
						if (specClass instanceof IBinding) {
							((ICPPInternalBase)specBase).setBaseClass((IBinding) specClass);
						}
						result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBase);
					}
				}

				return (ICPPBase[]) ArrayUtil.trim(ICPPBase.class, result);
			}
			return ICPPBase.EMPTY_BASE_ARRAY;
		}
		return super.getBases();
	}
	
	@Override
	public ICPPConstructor[] getConstructors() throws DOMException {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getConstructors();
		}
		return super.getConstructors();
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods();
		}
		return super.getDeclaredMethods();
	}
}
