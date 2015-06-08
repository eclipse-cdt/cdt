/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope.createInheritedConsructors;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
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
		IBinding[] bindings = classScope != null ?
				classScope.getBindings(new ScopeLookupData(name, resolve, false)) : null;
		
		if (bindings == null)
			return null;
    	
		IBinding[] specs = IBinding.EMPTY_BINDING_ARRAY;
		for (IBinding binding : bindings) {
			specs = ArrayUtil.append(specs, specialClass.specializeMember(binding, name));
		}
		specs = ArrayUtil.trim(specs);
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
		IBinding[] result= IBinding.EMPTY_BINDING_ARRAY;
		int n = 0;
		for (IBinding binding : bindings) {
			if (binding == specialized ||
					(binding instanceof ICPPClassType && areSameTypesModuloPartialSpecialization(specialized, (IType) binding))) {
				binding= specialClass;
			} else {
				binding= specialClass.specializeMember(binding, lookup.getLookupPoint());
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
	public ICPPBase[] getBases(IASTNode point) {
		if (fBases == null) {
			if (fComputingBases.get()) {
				return ICPPBase.EMPTY_BASE_ARRAY;  // avoid recursion
			}
			fComputingBases.set(true);
			try {
				ICPPBase[] result = ICPPBase.EMPTY_BASE_ARRAY;
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
								result= ArrayUtil.append(result, base);
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
							continue;
						}
						if (origClass instanceof IType) {
							ICPPBase specBase = base.clone();
							ICPPClassSpecialization specializationContext = specialClass;
							if (specialClass.getOwner() instanceof ICPPClassSpecialization) {
								specializationContext = (ICPPClassSpecialization) specialClass.getOwner();
							}
							IType specClass= CPPTemplates.instantiateType((IType) origClass, tpmap, -1, specializationContext, point);
							specClass = SemanticUtil.getUltimateType(specClass, false);
							if (specClass instanceof IBinding && !(specClass instanceof IProblemBinding)) {
								specBase.setBaseClass((IBinding) specClass);
							}
							result = ArrayUtil.append(result, specBase);
						}
					}
					result= ArrayUtil.trim(result);
					fBases= result;
					return result;
				}
			} finally {
				fComputingBases.set(false);
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
		ICPPClassType origClass = specialClass.getSpecializedBinding();
		ICPPMethod[] methods= ClassTypeHelper.getImplicitMethods(origClass, point);
		ICPPMethod[] specializedMembers = specializeMembers(methods, point);
		// Add inherited constructors.
		ICPPMethod[] inheritedConstructors = getOwnInheritedConstructors(point);
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
		CCorePlugin.log(new Exception("Unsafe method call. Instantiation of dependent expressions may not work.")); //$NON-NLS-1$
		return getConstructors(null);
	}
		
	@Override
	public ICPPConstructor[] getConstructors(IASTNode point) {
		ICPPConstructor[] ctors= ClassTypeHelper.getConstructors(specialClass.getSpecializedBinding(), point);
		ICPPConstructor[] specializedCtors = specializeMembers(ctors, point);
		// Add inherited constructors.
		ICPPMethod[] inheritedConstructors = getOwnInheritedConstructors(specializedCtors, point);
		return ArrayUtil.addAll(specializedCtors, inheritedConstructors);
	}

	/**
	 * Returns the inherited constructors that are not specializations of the inherited constructors
	 * of the specialized class.
	 */
	private ICPPMethod[] getOwnInheritedConstructors(ICPPConstructor[] existingConstructors,
			IASTNode point) {
		if (ownInheritedConstructors == null) {
	        if (!hasInheritedConstructorsSources(point))
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
			ICPPMethod[] constructors = createInheritedConsructors(this, specialClass.getNameCharArray(),
					getBases(point), existingConstructorParamTypes, point);
			ownInheritedConstructors = constructors;
		}
		return ownInheritedConstructors;
	}

	private ICPPMethod[] getOwnInheritedConstructors(IASTNode point) {
		if (ownInheritedConstructors != null)
			return ownInheritedConstructors;
		ICPPConstructor[] ctors= ClassTypeHelper.getConstructors(specialClass.getSpecializedBinding(), point);
		ICPPConstructor[] specializedCtors = specializeMembers(ctors, point);
		return getOwnInheritedConstructors(specializedCtors, point);
	}

	private boolean hasInheritedConstructorsSources(IASTNode point) {
		for (ICPPBase base : getBases(point)) {
			if (base.isInheritedConstructorsSource())
				return true;
		}
		return false;
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
