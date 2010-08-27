/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
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
 */
public class AbstractCPPClassSpecializationScope implements ICPPClassSpecializationScope {
	final private ICPPClassSpecialization specialClass;
	private ICPPBase[] fBases;

	public AbstractCPPClassSpecializationScope(ICPPClassSpecialization specialization) {
		this.specialClass= specialization;
	}

	public ICPPClassType getOriginalClassType() {
		return specialClass.getSpecializedBinding();
	}
		
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

	public IBinding getBinding(IASTName name, boolean forceResolve, IIndexFileSet fileSet) {
		char[] c = name.getLookupKey();
		
		if (CharArrayUtils.equals(c, specialClass.getNameCharArray())
				&& !CPPClassScope.shallReturnConstructors(name, false)) {
			return specialClass;
		}

		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ? classScope.getBindings(name, forceResolve, false) : null;
		
		if (bindings == null)
			return null;
    	
		IBinding[] specs = new IBinding[0];
		for (IBinding binding : bindings) {
			specs = (IBinding[]) ArrayUtil.append(IBinding.class, specs, specialClass.specializeMember(binding));
		}
		specs = (IBinding[]) ArrayUtil.trim(IBinding.class, specs);
    	return CPPSemantics.resolveAmbiguities(name, specs);
	}

	final public IBinding[] getBindings(IASTName name, boolean forceResolve, boolean prefixLookup,
			IIndexFileSet fileSet) {
		return getBindings(name, forceResolve, prefixLookup, fileSet, true);
	}

	public IBinding[] getBindings(IASTName name, boolean forceResolve, boolean prefixLookup,
			IIndexFileSet fileSet, boolean checkPointOfDecl) {
		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		if (classScope == null)
			return IBinding.EMPTY_BINDING_ARRAY;
		
		IBinding[] bindings;
		if (classScope instanceof ICPPASTInternalScope) {
			bindings= ((ICPPASTInternalScope) classScope).getBindings(name, forceResolve, prefixLookup, fileSet, checkPointOfDecl);
		} else {
			bindings= classScope.getBindings(name, forceResolve, prefixLookup, fileSet);
		}
		IBinding[] result= null;
		for (IBinding binding : bindings) {
			if (binding == specialized) {
				binding= specialClass;
			} else {
				binding= specialClass.specializeMember(binding);
			}
			result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
		}
		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}
	
	public ICPPClassSpecialization getClassType() {
		return specialClass;
	}
	
	public ICPPBase[] getBases() {
		if (fBases == null) {
			ICPPBase[] result = null;
			ICPPBase[] bases = specialClass.getSpecializedBinding().getBases();
			if (bases.length == 0) {
				fBases= bases;
			} else {
				final ICPPTemplateParameterMap tpmap = specialClass.getTemplateParameterMap();
				for (ICPPBase base : bases) {
					IBinding origClass = base.getBaseClass();
					if (origClass instanceof ICPPTemplateParameter && ((ICPPTemplateParameter) origClass).isParameterPack()) {
						IType[] specClasses= CPPTemplates.instantiateTypes(new IType[]{new CPPParameterPackType((IType) origClass)}, tpmap, -1, specialClass);
						if (specClasses.length == 1 && specClasses[0] instanceof ICPPParameterPackType) {
							result= (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, base);
						} else {
							for (IType specClass : specClasses) {
								ICPPBase specBase = base.clone();
								specClass = SemanticUtil.getUltimateType(specClass, false);
								if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
									specBase.setBaseClass((IBinding) specClass);
									result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBase);
								}
							}
						}
						continue;
					}
					if (origClass instanceof IType) {
						ICPPBase specBase = base.clone();
						IType specClass= CPPTemplates.instantiateType((IType) origClass, tpmap, -1, specialClass);
						specClass = SemanticUtil.getUltimateType(specClass, false);
						if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
							specBase.setBaseClass((IBinding) specClass);
						}
						result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBase);
					}
				}
				result= (ICPPBase[]) ArrayUtil.trim(ICPPBase.class, result);
				fBases= result;
				return result;
			}
		}
		return fBases;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends IBinding> T[] specializeMembers(T[] array) {
		if (array == null || array.length == 0) 
			return array;

		T[] newArray= array.clone();
		for (int i = 0; i < newArray.length; i++) {
			newArray[i]= (T) specialClass.specializeMember(array[i]);
		}
		return newArray;
	}

	public ICPPField[] getDeclaredFields() {
		ICPPField[] fields= specialClass.getSpecializedBinding().getDeclaredFields();
		return specializeMembers(fields);
	}
	
	public ICPPMethod[] getImplicitMethods() {
		ICPPClassScope origClassScope= (ICPPClassScope) specialClass.getSpecializedBinding().getCompositeScope();
		if (origClassScope == null) {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
		ICPPMethod[] methods= origClassScope.getImplicitMethods();
		return specializeMembers(methods);
	}

	public IName getScopeName() {
		if (specialClass instanceof ICPPInternalBinding)
			return (IASTName) ((ICPPInternalBinding) specialClass).getDefinition();
		return null;
	}

	public ICPPConstructor[] getConstructors() {
		ICPPConstructor[] ctors= specialClass.getSpecializedBinding().getConstructors();
		return specializeMembers(ctors);
	}
		
	public ICPPMethod[] getDeclaredMethods() {
		ICPPMethod[] bindings = specialClass.getSpecializedBinding().getDeclaredMethods();
		return specializeMembers(bindings);
	}

	public ICPPClassType[] getNestedClasses() {
		ICPPClassType[] bindings = specialClass.getSpecializedBinding().getNestedClasses();
		return specializeMembers(bindings);
	}

	public IBinding[] getFriends() {
		// not yet supported
		return IBinding.EMPTY_BINDING_ARRAY;
	}

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

	public IBinding[] find(String name) {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	public String toString() {
		IName name = getScopeName();
		return name != null ? name.toString() : String.valueOf(specialClass);
	}

	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}
}
