/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Base class for all specialization scopes
 * For safe usage in index bindings, all fields need to be final or volatile.
 */
public class AbstractCPPClassSpecializationScope implements ICPPClassSpecializationScope {
	final private ICPPClassSpecialization specialClass;
	private volatile ICPPBase[] fBases; // Used by the pdom bindings, needs to be volatile.

	public AbstractCPPClassSpecializationScope(ICPPClassSpecialization specialization) {
		this.specialClass= specialization;
	}

	@Override
	public ICPPClassType getOriginalClassType() {
		return specialClass.getSpecializedBinding();
	}
		
	@Override
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(new ScopeLookupData(name, resolve, prefix));
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		char[] c = name.getLookupKey();
		
		if (CharArrayUtils.equals(c, specialClass.getNameCharArray())
				&& !CPPClassScope.shallReturnConstructors(name, false)) {
			return specialClass;
		}

		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ? classScope.getBindings(new ScopeLookupData(name, resolve, false)) : null;
		
		if (bindings == null)
			return null;
    	
		IBinding[] specs = new IBinding[0];
		for (IBinding binding : bindings) {
			specs = ArrayUtil.append(IBinding.class, specs, specialClass.specializeMember(binding, name));
		}
		specs = ArrayUtil.trim(IBinding.class, specs);
    	return CPPSemantics.resolveAmbiguities(name, specs);
	}

	@Deprecated
	@Override
	final public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
			IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	final public IBinding[] getBindings(ScopeLookupData lookup) {
		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		if (classScope == null)
			return IBinding.EMPTY_BINDING_ARRAY;
		
		IBinding[] bindings= classScope.getBindings(lookup);
		IBinding[] result= null;
		for (IBinding binding : bindings) {
			if (binding == specialized) {
				binding= specialClass;
			} else {
				binding= specialClass.specializeMember(binding, lookup.getLookupPoint());
			}
			result = ArrayUtil.append(IBinding.class, result, binding);
		}
		return ArrayUtil.trim(IBinding.class, result);
	}
	
	@Override
	public ICPPClassSpecialization getClassType() {
		return specialClass;
	}
	
	@Override
	public ICPPBase[] getBases(IASTNode point) {
		if (fBases == null) {
			ICPPBase[] result = null;
			ICPPBase[] bases = ClassTypeHelper.getBases(specialClass.getSpecializedBinding(), point);
			if (bases.length == 0) {
				fBases= bases;
			} else {
				final ICPPTemplateParameterMap tpmap = specialClass.getTemplateParameterMap();
				for (ICPPBase base : bases) {
					IBinding origClass = base.getBaseClass();
					if (origClass instanceof ICPPTemplateParameter && ((ICPPTemplateParameter) origClass).isParameterPack()) {
						IType[] specClasses= CPPTemplates.instantiateTypes(new IType[] { new CPPParameterPackType((IType) origClass) },
								tpmap, -1, specialClass, point);
						if (specClasses.length == 1 && specClasses[0] instanceof ICPPParameterPackType) {
							result= ArrayUtil.append(ICPPBase.class, result, base);
						} else {
							for (IType specClass : specClasses) {
								ICPPBase specBase = base.clone();
								specClass = SemanticUtil.getUltimateType(specClass, false);
								if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
									specBase.setBaseClass((IBinding) specClass);
									result = ArrayUtil.append(ICPPBase.class, result, specBase);
								}
							}
						}
						continue;
					}
					if (origClass instanceof IType) {
						ICPPBase specBase = base.clone();
						IType specClass= CPPTemplates.instantiateType((IType) origClass, tpmap, -1, specialClass, point);
						specClass = SemanticUtil.getUltimateType(specClass, false);
						if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
							specBase.setBaseClass((IBinding) specClass);
						}
						result = ArrayUtil.append(ICPPBase.class, result, specBase);
					}
				}
				result= ArrayUtil.trim(ICPPBase.class, result);
				fBases= result;
				return result;
			}
		}
		return fBases;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends IBinding> T[] specializeMembers(T[] array, IASTNode point) {
		if (array == null || array.length == 0) 
			return array;

		T[] newArray= array.clone();
		for (int i = 0; i < newArray.length; i++) {
			IBinding specializedMember = specialClass.specializeMember(array[i], point);
			newArray[i]= (T) specializedMember;
		}
		return newArray;
	}

	@Override
	public ICPPField[] getDeclaredFields(IASTNode point) {
		ICPPField[] fields= ClassTypeHelper.getDeclaredFields(specialClass.getSpecializedBinding(), point);
		return specializeMembers(fields, point);
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getImplicitMethods(null);
	}

	@Override
	public ICPPMethod[] getImplicitMethods(IASTNode point) {
		ICPPClassScope origClassScope= (ICPPClassScope) specialClass.getSpecializedBinding().getCompositeScope();
		if (origClassScope == null) {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
		ICPPMethod[] methods= origClassScope.getImplicitMethods();
		return specializeMembers(methods, point);
	}

	@Override
	public IName getScopeName() {
		if (specialClass instanceof ICPPInternalBinding)
			return (IASTName) ((ICPPInternalBinding) specialClass).getDefinition();
		return null;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getConstructors(null);
	}
		
	@Override
	public ICPPConstructor[] getConstructors(IASTNode point) {
		ICPPConstructor[] ctors= ClassTypeHelper.getConstructors(specialClass.getSpecializedBinding(), point);
		return specializeMembers(ctors, point);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		ICPPMethod[] bindings = ClassTypeHelper.getDeclaredMethods(specialClass.getSpecializedBinding(), point);
		return specializeMembers(bindings, point);
	}

	@Override
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		ICPPClassType[] bindings = ClassTypeHelper.getNestedClasses(specialClass.getSpecializedBinding(), point);
		return specializeMembers(bindings, point);
	}

	@Override
	public IBinding[] getFriends(IASTNode point) {
		IBinding[] friends = ClassTypeHelper.getFriends(specialClass.getSpecializedBinding(), point);
		return specializeMembers(friends, point);
	}

	@Override
	public IScope getParent() throws DOMException {
		IBinding binding= specialClass.getOwner();
		if (binding instanceof ICPPClassType) {
			return ((ICPPClassType) binding).getCompositeScope();
		}
		if (binding instanceof ICPPNamespace) {
			return ((ICPPNamespace) binding).getNamespaceScope();
		}
		return getOriginalClassType().getScope();
	}

	@Override
	public IBinding[] find(String name) {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	public String toString() {
		IName name = getScopeName();
		return name != null ? name.toString() : String.valueOf(specialClass);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}
}
