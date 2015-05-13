/*******************************************************************************
 * Copyright (c) 2009, 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *	   Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope;

/**
 * The context that determines access to private and protected class members.
 */
public class AccessContext {
	private static final int v_private = ICPPMember.v_private;
	private static final int v_protected = ICPPMember.v_protected;
	public static final int v_public = ICPPMember.v_public;

	/**
	 * Checks if a binding is accessible from a given name.
	 *
	 * @param binding  A binding to check access for.
	 * @param from A name corresponding to the binding.
	 * @return {@code true} if the binding is accessible.
	 */
	public static boolean isAccessible(IBinding binding, IASTName from) {
		return new AccessContext(from).isAccessible(binding);
	}

	/**
	 * Checks if a binding is accessible from a given name.
	 *
	 * @param binding  A binding to check access for.
	 * @param bindingVisibility visibility of the binding in the containing composite type.
	 *     Used instead of calling {@link ICPPMember#getVisibility()}.
	 * @param from A name corresponding to the binding.
	 * @return {@code true} if the binding is accessible.
	 */
	public static boolean isAccessible(IBinding binding, int bindingVisibility, IASTName from) {
		return new AccessContext(from).isAccessible(binding, bindingVisibility);
	}

	private final IASTName name;
	/**
	 * A chain of nested classes or/and a function that determine accessibility of private/protected
	 * members by participating in friendship or class inheritance relationships. If both, classes
	 * and a function are present in the context, the outermost class has to be local to
	 * the function.
	 * {@link "http://www.open-std.org/JTC1/SC22/WG21/docs/cwg_defects.html#45"}
	 */
	private IBinding[] context;
	/**
	 * A class through which the bindings are accessed (11.2.4).
	 */
	private boolean isUnqualifiedLookup;
	private ICPPClassType namingClass;  // Depends on the binding for which we check the access.
	// The first candidate is independent of the binding for which we do the access-check.
	private ICPPClassType firstCandidateForNamingClass;
	private DOMException initializationException;

	public AccessContext(IASTName name) {
		this.name = name;
	}

	/**
	 * Checks if a binding is accessible in a given context.
	 *
	 * @param binding A binding to check access for.
	 * @return {@code true} if the binding is accessible.
	 */
	public boolean isAccessible(IBinding binding) {
		if (binding instanceof ICPPTemplateParameter)
			return true;

		int bindingVisibility;
		if (binding instanceof ICPPMember) {
			bindingVisibility = ((ICPPMember) binding).getVisibility();
		} else {
	        while (binding instanceof ICPPSpecialization) {
	            binding = ((ICPPSpecialization) binding).getSpecializedBinding();
	        }
	        if (binding instanceof ICPPClassTemplatePartialSpecialization) {
	        	// A class template partial specialization inherits the visibility of its primary
	        	// class template. 
	        	binding = ((ICPPClassTemplatePartialSpecialization) binding).getPrimaryClassTemplate();
	        }
	        if (binding instanceof ICPPAliasTemplateInstance) {
	        	binding = ((ICPPAliasTemplateInstance) binding).getTemplateDefinition();
	        }
			IBinding owner = binding.getOwner();
			if (owner instanceof ICPPClassType) {
				bindingVisibility = ((ICPPClassType) owner).getVisibility(binding);
			} else {
				bindingVisibility = v_public;
			}
		}
		return isAccessible(binding, bindingVisibility);
	}

	/**
	 * Checks if a binding is accessible in a given context.
	 *
	 * @param binding A binding to check access for.
	 * @param bindingVisibility visibility of the binding in the containing composite type.
	 *     Used instead of calling {@link ICPPMember#getVisibility()}.
	 * @return {@code true} if the binding is accessible.
	 */
	public boolean isAccessible(IBinding binding, int bindingVisibility) {
		IBinding owner;
		while ((owner = binding.getOwner()) instanceof ICompositeType &&
				((ICompositeType) owner).isAnonymous()) {
			binding = owner;
		}
		if (!(owner instanceof ICPPClassType)) {
			return true; // The binding is not a class member.
		}
		if (!initialize()) {
			return true; // Assume visibility if anything goes wrong.
		}
		ICPPClassType accessOwner= (ICPPClassType) owner;
		namingClass = getNamingClass(accessOwner);
		if (namingClass == null) {
			return true;
		}
		return isAccessible(binding, bindingVisibility, accessOwner, namingClass,
				v_public, 0);
	}

	/**
	 * @return {@code true} if initialization succeeded.
	 */
	private boolean initialize() {
		if (context == null) {
			if (initializationException != null) {
				return false;
			}
			try {
				context = getContext(name);
				firstCandidateForNamingClass= getFirstCandidateForNamingClass(name);
			} catch (DOMException e) {
				CCorePlugin.log(e);
				initializationException = e;
				return false;
			}
		}
		return true;
	}

	// Return true if 'c' is the same type as 'target', or a specialization of 'target'.
	private static boolean isSameTypeOrSpecialization(ICPPClassType c, ICPPClassType target) {
		if (!(c instanceof ICPPSpecialization)) {
			while (target instanceof ICPPSpecialization) {
				IBinding specialized = ((ICPPSpecialization) target).getSpecializedBinding();
				if (specialized instanceof ICPPClassType) {
					target = (ICPPClassType) specialized;
				}
			}
		}
		return c.isSameType(target);
	}
	
	private boolean isAccessible(IBinding binding, int bindingVisibility, ICPPClassType owner,
			ICPPClassType derivedClass, int accessLevel, int depth) {
		if (depth > CPPSemantics.MAX_INHERITANCE_DEPTH)
			return false;

		accessLevel = getMemberAccessLevel(derivedClass, accessLevel);
	
		if (isSameTypeOrSpecialization(owner, derivedClass)) {
			return isAccessible(bindingVisibility, accessLevel);
		}

		ICPPBase[] bases = ClassTypeHelper.getBases(derivedClass, name);
		if (bases != null) {
			for (ICPPBase base : bases) {
				IBinding baseBinding = base.getBaseClass();
				if (baseBinding instanceof ICPPDeferredClassInstance) {
					// Support content assist for members of deferred instances.
					baseBinding= ((ICPPDeferredClassInstance) baseBinding).getTemplateDefinition();
				}
				if (!(baseBinding instanceof ICPPClassType)) {
					continue;
				}
				if (!isAccessible(base.getVisibility(), accessLevel)) {
					continue;
				}
				if (isAccessible(binding, bindingVisibility, owner,
						(ICPPClassType) baseBinding, accessLevel == v_private ? v_protected : accessLevel, depth + 1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns access level to the members of a class.
	 *
	 * @param classType A class
	 * @param inheritedAccessLevel Access level inherited from derived class.
	 *     One of: v_public, v_protected, v_private.
	 * @return One of: v_public, v_protected, v_private.
	 */
	private int getMemberAccessLevel(ICPPClassType classType, int inheritedAccessLevel) {
		int accessLevel = inheritedAccessLevel;
		for (IBinding contextBinding : context) {
			if (ClassTypeHelper.isFriend(contextBinding, classType))
				return v_private;

			if (accessLevel == v_public && contextBinding instanceof ICPPClassType) {
				ICPPClassType contextClass = (ICPPClassType) contextBinding;
				if (isAccessibleBaseClass(classType, contextClass, 0))
					accessLevel = v_protected;
			}
		}
		return accessLevel;
	}

	private boolean isAccessibleBaseClass(ICPPClassType classType, ICPPClassType derived, int depth) {
		if (depth > CPPSemantics.MAX_INHERITANCE_DEPTH)
			return false;

		if (derived.isSameType(classType))
			return true;

		ICPPBase[] bases = ClassTypeHelper.getBases(derived, name);
		if (bases != null) {
			for (ICPPBase base : bases) {
				IBinding baseClass = base.getBaseClass();
				if (baseClass instanceof ICPPDeferredClassInstance) {
					baseClass = ((ICPPDeferredClassInstance) baseClass).getTemplateDefinition();
				}
				if (!(baseClass instanceof ICPPClassType)) {
					continue;
				}
				if (depth > 0 && !isAccessible(base.getVisibility(), v_protected)) {
					continue;
				}
				if (isAccessibleBaseClass(classType, (ICPPClassType) baseClass, depth + 1)) {
					return true;
				}
			}
		}
		return false;
	}

	private ICPPClassType getFirstCandidateForNamingClass(IASTName name) throws DOMException {
		LookupData data = new LookupData(name);
		isUnqualifiedLookup= !data.qualified;
		
		ICPPScope scope = CPPSemantics.getLookupScope(name);
		while (scope != null && !(scope instanceof ICPPClassScope)) {
			if (scope instanceof ICPPInternalUnknownScope) {
				IType scopeType = ((ICPPInternalUnknownScope) scope).getScopeType();
				if (scopeType instanceof ICPPDeferredClassInstance) {
					return ((ICPPDeferredClassInstance) scopeType).getClassTemplate();
				}
			}

			scope = CPPSemantics.getParentScope(scope, data.getTranslationUnit());
		}
		if (scope instanceof ICPPClassScope) {
			return ((ICPPClassScope) scope).getClassType();
		}
		return null;
	}

	private ICPPClassType getNamingClass(ICPPClassType accessOwner) {
		ICPPClassType classType = firstCandidateForNamingClass;
		if (classType != null && isUnqualifiedLookup) {
			IBinding owner = classType.getOwner();
			while (owner instanceof ICPPClassType &&
					!derivesFrom(classType, accessOwner, name, CPPSemantics.MAX_INHERITANCE_DEPTH)) {
				classType= (ICPPClassType) owner;
				owner= classType.getOwner();
			}
		}
		return classType;
	}

	private static boolean derivesFrom(ICPPClassType derived, ICPPClassType target, IASTNode point,
			int maxdepth) {
		if (derived == target || derived.isSameType(target)) {
			return true;
		}
		if (maxdepth > 0) {
			for (ICPPBase cppBase : ClassTypeHelper.getBases(derived, point)) {
				IBinding base = cppBase.getBaseClass();
				if (!(target instanceof ICPPSpecialization)) {
					while (base instanceof ICPPSpecialization) {
						base = ((ICPPSpecialization) base).getSpecializedBinding();
					}
				}
				if (base instanceof ICPPClassType) {
					ICPPClassType tbase = (ICPPClassType) base;
					if (tbase.isSameType(target)) {
						return true;
					}
					if (derivesFrom(tbase, target, point, maxdepth - 1))
						return true;
				}
			}
		}
		return false;
	}

	private static IBinding[] getContext(IASTName name) {
		IBinding[] accessibilityContext = IBinding.EMPTY_BINDING_ARRAY;
		for (IBinding binding = CPPVisitor.findEnclosingFunctionOrClass(name);
				binding != null; binding = binding.getOwner()) {
			if (binding instanceof ICPPMethod ||
					// Definition of an undeclared method.
					binding instanceof IProblemBinding &&
					((IProblemBinding) binding).getID() == IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND) {
				continue;
			}
			if (binding instanceof ICPPFunction || binding instanceof ICPPClassType) {
				accessibilityContext = ArrayUtil.append(accessibilityContext, binding);
			}
		}
		return ArrayUtil.trim(accessibilityContext);
	}

	/**
	 * Checks if objects with the given visibility are accessible at the given access level.
	 *
	 * @param visibility one of: v_public, v_protected, v_private.
	 * @param accessLevel one of: v_public, v_protected, v_private.
	 * @return {@code true} if the access level is sufficiently high.
	 */
	private static boolean isAccessible(int visibility, int accessLevel) {
		// Note the ordering of numeric visibility values: v_public < v_protected < v_private.
		return accessLevel >= visibility;
	}
}
