/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
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
	private final ICPPClassSpecialization specialClass;
	// The following fields are used by the PDOM bindings and need to be volatile.
	private volatile ICPPBase[] fBases;
	private volatile ICPPMethod[] ownInheritedConstructors;
	private final ThreadLocal<Boolean> fComputingBases = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	public AbstractCPPClassSpecializationScope(ICPPClassSpecialization specialization) {
		this.specialClass = specialization;
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
		IBinding[] bindings = classScope != null ? classScope.getBindings(new ScopeLookupData(name, resolve, false))
				: null;

		if (bindings == null)
			return null;

		IBinding[] specs = IBinding.EMPTY_BINDING_ARRAY;
		CPPSemantics.pushLookupPoint(name);
		try {
			for (IBinding binding : bindings) {
				specs = ArrayUtil.append(specs, specialClass.specializeMember(binding));
			}
		} finally {
			CPPSemantics.popLookupPoint();
		}
		specs = ArrayUtil.trim(specs);
		return CPPSemantics.resolveAmbiguities(name, specs);
	}

	@Deprecated
	@Override
	final public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	final public IBinding[] getBindings(ScopeLookupData lookup) {
		ICPPClassType specialized = specialClass.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		if (classScope == null)
			return IBinding.EMPTY_BINDING_ARRAY;

		IBinding[] bindings = classScope.getBindings(lookup);
		IBinding[] result = IBinding.EMPTY_BINDING_ARRAY;
		if (bindings == null) {
			return result;
		}
		int n = 0;
		for (IBinding binding : bindings) {
			if (binding == specialized || (binding instanceof ICPPClassType
					&& areSameTypesModuloPartialSpecialization(specialized, (IType) binding))) {
				binding = specialClass;
			} else {
				binding = specialClass.specializeMember(binding);
			}
			if (binding != null)
				result = ArrayUtil.appendAt(result, n++, binding);
		}
		return ArrayUtil.trim(result, n);
	}

	private static boolean areSameTypesModuloPartialSpecialization(IType type1, IType type2) {
		while (type1 instanceof ICPPClassTemplatePartialSpecialization) {
			type1 = ((ICPPClassTemplatePartialSpecialization) type1).getPrimaryClassTemplate();
		}
		while (type2 instanceof ICPPClassTemplatePartialSpecialization) {
			type2 = ((ICPPClassTemplatePartialSpecialization) type2).getPrimaryClassTemplate();
		}
		return type1.isSameType(type2);
	}

	@Override
	public ICPPClassSpecialization getClassType() {
		return specialClass;
	}

	@Override
	public ICPPBase[] getBases() {
		if (fBases == null) {
			if (fComputingBases.get()) {
				return ICPPBase.EMPTY_BASE_ARRAY; // avoid recursion
			}
			fComputingBases.set(true);
			try {
				ICPPBase[] result = ICPPBase.EMPTY_BASE_ARRAY;
				ICPPBase[] bases = specialClass.getSpecializedBinding().getBases();
				if (bases.length == 0) {
					fBases = bases;
				} else {
					final ICPPTemplateParameterMap tpmap = specialClass.getTemplateParameterMap();
					for (ICPPBase base : bases) {
						IType baseType = base.getBaseClassType();
						if (baseType instanceof ICPPParameterPackType) {
							IType[] specClasses = CPPTemplates.instantiateTypes(new IType[] { baseType },
									new InstantiationContext(tpmap, specialClass));
							if (specClasses.length == 1 && specClasses[0] instanceof ICPPParameterPackType) {
								result = ArrayUtil.append(result, base);
							} else {
								for (IType specClass : specClasses) {
									ICPPBase specBase = base.clone();
									specClass = SemanticUtil.getUltimateType(specClass, false);
									if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
										specBase.setBaseClass((IBinding) specClass);
										result = ArrayUtil.append(result, specBase);
									}
								}
							}
						} else if (baseType != null) {
							ICPPBase specBase = base.clone();
							ICPPClassSpecialization specializationContext = specialClass;
							IBinding owner = specialClass.getOwner();
							if (owner instanceof ICPPClassSpecialization) {
								specializationContext = (ICPPClassSpecialization) owner;
							}
							IType specClass = CPPTemplates.instantiateType(baseType,
									new InstantiationContext(tpmap, specializationContext));
							specClass = SemanticUtil.getUltimateType(specClass, false);
							if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
								specBase.setBaseClass((IBinding) specClass);
							}
							result = ArrayUtil.append(result, specBase);
						}
					}
					result = ArrayUtil.trim(result);
					fBases = result;
					return result;
				}
			} finally {
				fComputingBases.set(false);
			}
		}
		return fBases;
	}

	@SuppressWarnings("unchecked")
	private <T extends IBinding> T[] specializeMembers(T[] array) {
		if (array == null || array.length == 0)
			return array;

		T[] newArray = array.clone();
		for (int i = 0; i < newArray.length; i++) {
			IBinding specializedMember = specialClass.specializeMember(array[i]);
			newArray[i] = (T) specializedMember;
		}
		return newArray;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		ICPPField[] fields = specialClass.getSpecializedBinding().getDeclaredFields();
		return specializeMembers(fields);
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		ICPPClassType origClass = specialClass.getSpecializedBinding();
		ICPPMethod[] methods = ClassTypeHelper.getImplicitMethods(origClass);
		ICPPMethod[] specializedMembers = specializeMembers(methods);
		// Add inherited constructors.
		ICPPMethod[] inheritedConstructors = getOwnInheritedConstructors();
		return ArrayUtil.addAll(specializedMembers, inheritedConstructors);
	}

	@Override
	public IName getScopeName() {
		if (specialClass instanceof ICPPInternalBinding)
			return (IASTName) ((ICPPInternalBinding) specialClass).getDefinition();
		return null;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		ICPPConstructor[] ctors = specialClass.getSpecializedBinding().getConstructors();
		ICPPConstructor[] specializedCtors = specializeMembers(ctors);
		// Add inherited constructors.
		ICPPMethod[] inheritedConstructors = getOwnInheritedConstructors(specializedCtors);
		return ArrayUtil.addAll(specializedCtors, inheritedConstructors);
	}

	/**
	 * Returns the inherited constructors that are not specializations of the inherited constructors
	 * of the specialized class.
	 */
	private ICPPMethod[] getOwnInheritedConstructors(ICPPConstructor[] existingConstructors) {
		if (ownInheritedConstructors == null) {
			if (!hasInheritedConstructorsSources())
				return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

			IType[][] existingConstructorParamTypes = new IType[existingConstructors.length][];
			for (int i = 0; i < existingConstructors.length; i++) {
				ICPPParameter[] params = existingConstructors[i].getParameters();
				IType[] types = new IType[params.length];
				for (int j = 0; j < params.length; j++) {
					types[j] = params[j].getType();
				}
				existingConstructorParamTypes[i] = types;
			}
			ICPPMethod[] constructors = ClassTypeHelper.getInheritedConstructors(this, getBases(),
					existingConstructorParamTypes);
			ownInheritedConstructors = constructors;
		}
		return ownInheritedConstructors;
	}

	private ICPPMethod[] getOwnInheritedConstructors() {
		if (ownInheritedConstructors != null)
			return ownInheritedConstructors;
		ICPPConstructor[] ctors = specialClass.getSpecializedBinding().getConstructors();
		ICPPConstructor[] specializedCtors = specializeMembers(ctors);
		return getOwnInheritedConstructors(specializedCtors);
	}

	private boolean hasInheritedConstructorsSources() {
		for (ICPPBase base : getBases()) {
			if (base.isInheritedConstructorsSource())
				return true;
		}
		return false;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		ICPPMethod[] bindings = specialClass.getSpecializedBinding().getDeclaredMethods();
		return specializeMembers(bindings);
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		ICPPClassType[] bindings = specialClass.getSpecializedBinding().getNestedClasses();
		return specializeMembers(bindings);
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		ICPPUsingDeclaration[] bindings = specialClass.getSpecializedBinding().getUsingDeclarations();
		return specializeMembers(bindings);
	}

	@Override
	public IBinding[] getFriends() {
		IBinding[] friends = specialClass.getSpecializedBinding().getFriends();
		return specializeMembers(friends);
	}

	@Override
	public IScope getParent() throws DOMException {
		IBinding binding = specialClass.getOwner();
		if (binding instanceof ICPPClassType) {
			return ((ICPPClassType) binding).getCompositeScope();
		}
		if (binding instanceof ICPPNamespace) {
			return ((ICPPNamespace) binding).getNamespaceScope();
		}
		return getOriginalClassType().getScope();
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		return find(name);
	}

	@Override
	public IBinding[] find(String name) {
		return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	@Override
	public String toString() {
		IName name = getScopeName();
		return name != null ? name.toString() : String.valueOf(specialClass);
	}

	// Note: equals() and hashCode() are overridden because multiple instances
	//       of this class representing the same class specialization scope
	//       may be created, but scopes are sometimes stored in hash maps
	//       under the assumption that two objects representing the same
	//       scope will compare equal().

	@Override
	public boolean equals(Object other) {
		if (other instanceof ICPPClassSpecializationScope) {
			return getClassType().equals(((ICPPClassSpecializationScope) other).getClassType());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return specialClass.hashCode();
	}
}
