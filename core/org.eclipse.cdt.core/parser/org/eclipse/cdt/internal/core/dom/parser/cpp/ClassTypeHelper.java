/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *     Anton Gorenkov
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.parser.util.ArrayUtil.appendAt;
import static org.eclipse.cdt.core.parser.util.ArrayUtil.trim;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * Holds common implementation of methods for ICPPClassType implementations that have
 * a corresponding textual definition in the source code.
 *
 * @see CPPClassType
 * @see CPPClassTemplate
 */
public class ClassTypeHelper {
	public static IBinding[] getFriends(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(host);
				if (backup != null)
					return backup.getFriends();
				IASTNode[] declarations = host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length != 0) ? declarations[0] : null;
				return new IBinding[] { new ProblemBinding(node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND,
						host.getNameCharArray()) };
			}
		}
		ObjectSet<IBinding> resultSet = new ObjectSet<>(2);
		IASTDeclaration[] members = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : members) {
			while (decl instanceof ICPPASTTemplateDeclaration) {
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			}

			if (decl instanceof IASTSimpleDeclaration) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration) decl)
						.getDeclSpecifier();
				if (declSpec.isFriend()) {
					IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
					if (dtors.length == 0) {
						if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
							resultSet.put(((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding());
						} else if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
							resultSet.put(((ICPPASTNamedTypeSpecifier) declSpec).getName().resolveBinding());
						}
					} else {
						for (IASTDeclarator dtor : dtors) {
							if (dtor == null)
								break;
							dtor = ASTQueries.findInnermostDeclarator(dtor);
							resultSet.put(dtor.getName().resolveBinding());
						}
					}
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition) decl)
						.getDeclSpecifier();
				if (declSpec.isFriend()) {
					IASTDeclarator dtor = ((IASTFunctionDefinition) decl).getDeclarator();
					dtor = ASTQueries.findInnermostDeclarator(dtor);
					resultSet.put(dtor.getName().resolveBinding());
				}
			}
		}

		return resultSet.keyArray(IBinding.class);
	}

	/**
	 * Checks if a binding is a friend of a class. Only classes and functions can be friends of a class.
	 * A class is considered a friend of itself.
	 *
	 * @param binding a binding.
	 * @param classType a class.
	 * @return {@code true} if {@code binding} is a friend of {@code classType}.
	 */
	public static boolean isFriend(IBinding binding, ICPPClassType classType) {
		IType type;
		if (binding instanceof ICPPClassType) {
			type = (IType) binding;
			if (type.isSameType(classType)) {
				return true;
			}
			for (IBinding friend : classType.getFriends()) {
				if (friend instanceof ICPPClassType && type.isSameType((IType) friend)) {
					return true;
				}
			}
		} else if (binding instanceof ICPPFunction) {
			type = ((ICPPFunction) binding).getType();
			char[] name = binding.getNameCharArray();
			for (IBinding friend : classType.getFriends()) {
				if (friend instanceof ICPPFunction && CharArrayUtils.equals(name, friend.getNameCharArray())
						&& SemanticUtil.haveSameOwner(binding, friend)
						&& type.isSameType(((ICPPFunction) friend).getType())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * A host maybe backed up with a definition from the index.
	 */
	private static ICPPClassType getBackupDefinition(ICPPInternalClassTypeMixinHost host) {
		ICPPClassScope scope = host.getCompositeScope();
		if (scope != null) {
			ICPPClassType b = scope.getClassType();
			if (!(b instanceof ICPPInternalClassTypeMixinHost))
				return b;
		}
		return null;
	}

	public static ICPPBase[] getBases(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(host);
				if (backup != null)
					return backup.getBases();

				return ICPPBase.NO_BASES_BECAUSE_TYPE_IS_INCOMPLETE;
			}
		}

		ICPPASTBaseSpecifier[] baseSpecifiers = host.getCompositeTypeSpecifier().getBaseSpecifiers();
		if (baseSpecifiers.length == 0)
			return ICPPBase.EMPTY_BASE_ARRAY;

		ICPPBase[] bases = new ICPPBase[baseSpecifiers.length];
		for (int i = 0; i < baseSpecifiers.length; i++) {
			bases[i] = new CPPBaseClause(baseSpecifiers[i]);
		}

		return bases;
	}

	public static ICPPField[] getDeclaredFields(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(host);
				if (backup != null)
					return backup.getDeclaredFields();

				return ICPPField.EMPTY_CPPFIELD_ARRAY;
			}
		}
		IBinding binding = null;
		ICPPField[] result = ICPPField.EMPTY_CPPFIELD_ARRAY;
		int resultSize = 0;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			if (decl instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
				for (IASTDeclarator dtor : dtors) {
					binding = ASTQueries.findInnermostDeclarator(dtor).getName().resolveBinding();
					if (binding instanceof ICPPField)
						result = ArrayUtil.appendAt(result, resultSize++, (ICPPField) binding);
				}
			} else if (decl instanceof ICPPASTUsingDeclaration) {
				IASTName n = ((ICPPASTUsingDeclaration) decl).getName();
				binding = n.resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] bs = ((ICPPUsingDeclaration) binding).getDelegates();
					for (IBinding element : bs) {
						if (element instanceof ICPPField)
							result = ArrayUtil.appendAt(result, resultSize++, (ICPPField) element);
					}
				} else if (binding instanceof ICPPField) {
					result = ArrayUtil.appendAt(result, resultSize++, (ICPPField) binding);
				}
			}
		}
		return ArrayUtil.trim(result, resultSize);
	}

	/**
	 * Returns all direct and indirect base classes.
	 *
	 * @param classType a class
	 * @return An array of base classes in arbitrary order.
	 */
	public static ICPPClassType[] getAllBases(ICPPClassType classType) {
		Set<ICPPClassType> result = new HashSet<>();
		result.add(classType);
		getAllBases(classType, result);
		result.remove(classType);
		return result.toArray(new ICPPClassType[result.size()]);
	}

	private static void getAllBases(ICPPClassType classType, Set<ICPPClassType> result) {
		ICPPBase[] bases = classType.getBases();
		for (ICPPBase base : bases) {
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType) {
				final ICPPClassType baseClass = (ICPPClassType) b;
				if (result.add(baseClass)) {
					getAllBases(baseClass, result);
				}
			}
		}
	}

	/**
	 * Returns all (direct or indirect) virtual base classes of {@code classType}.
	 */
	public static ICPPClassType[] getVirtualBases(ICPPClassType classType) {
		Set<ICPPClassType> virtualBases = new HashSet<>();
		Set<ICPPClassType> nonvirtualBases = new HashSet<>();
		nonvirtualBases.add(classType);
		getVirtualBases(classType, virtualBases, nonvirtualBases);
		return virtualBases.toArray(new ICPPClassType[virtualBases.size()]);
	}

	/**
	 * Helper function for {@link #getVirtualBases(ICPPClassType)}.
	 */
	private static void getVirtualBases(ICPPClassType classType, Set<ICPPClassType> virtualBases,
			Set<ICPPClassType> nonvirtualBases) {
		ICPPBase[] bases = classType.getBases();
		for (ICPPBase base : bases) {
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType) {
				final ICPPClassType baseClass = (ICPPClassType) b;
				if (base.isVirtual()) {
					if (virtualBases.add(baseClass)) {
						getVirtualBases(baseClass, virtualBases, nonvirtualBases);
					}
				} else {
					// A non-virtual base could have virtual bases in its hierarchy.
					if (nonvirtualBases.add(baseClass)) {
						getVirtualBases(baseClass, virtualBases, nonvirtualBases);
					}
				}
			}
		}
	}

	/**
	 * Checks inheritance relationship between two classes.
	 *
	 * @return {@code true} if {@code subclass} is a subclass of {@code superclass}.
	 */
	public static boolean isSubclass(ICPPClassType subclass, ICPPClassType superclass) {
		ICPPBase[] bases = subclass.getBases();
		for (ICPPBase base : bases) {
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType) {
				ICPPClassType baseClass = (ICPPClassType) b;
				if (baseClass.isSameType(superclass)) {
					return true;
				}
				if (isSubclass(baseClass, superclass)) {
					return true;
				}
			}
		}
		return false;
	}

	public static ICPPMethod[] getAllDeclaredMethods(ICPPClassType ct) {
		ICPPMethod[] methods = ct.getDeclaredMethods();
		ICPPClassType[] bases = getAllBases(ct);
		for (ICPPClassType base : bases) {
			methods = ArrayUtil.addAll(ICPPMethod.class, methods, base.getDeclaredMethods());
		}
		return ArrayUtil.trim(ICPPMethod.class, methods);
	}

	public static ICPPMethod[] getMethods(ICPPClassType ct) {
		ObjectSet<ICPPMethod> set = getOwnMethods(ct);

		ICPPClassType[] bases = getAllBases(ct);
		for (ICPPClassType base : bases) {
			set.addAll(base.getDeclaredMethods());
			set.addAll(getImplicitMethods(base));
		}
		return set.keyArray(ICPPMethod.class);
	}

	/**
	 * Returns methods either declared by the given class or generated by the compiler. Does not
	 * include methods declared in base classes.
	 */
	public static ObjectSet<ICPPMethod> getOwnMethods(ICPPClassType classType) {
		ObjectSet<ICPPMethod> set = new ObjectSet<>(4);
		set.addAll(classType.getDeclaredMethods());
		set.addAll(getImplicitMethods(classType));
		return set;
	}

	public static ICPPMethod[] getImplicitMethods(ICPPClassType classType) {
		return getImplicitMethods(classType.getCompositeScope());
	}

	public static ICPPMethod[] getImplicitMethods(IScope scope) {
		if (scope instanceof ICPPClassScope) {
			return ((ICPPClassScope) scope).getImplicitMethods();
		}
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public static ICPPMethod[] getDeclaredMethods(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(host);
				if (backup != null)
					return backup.getDeclaredMethods();

				return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
			}
		}
		IBinding binding = null;
		ICPPMethod[] result = ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		int resultSize = 0;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
				if (!((ICPPASTDeclSpecifier) sdecl.getDeclSpecifier()).isFriend()) {
					IASTDeclarator[] dtors = sdecl.getDeclarators();
					for (IASTDeclarator dtor : dtors) {
						binding = ASTQueries.findInnermostDeclarator(dtor).getName().resolveBinding();
						if (binding instanceof ICPPMethod)
							result = ArrayUtil.appendAt(result, resultSize++, (ICPPMethod) binding);
					}
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				final IASTFunctionDefinition fdef = (IASTFunctionDefinition) decl;
				if (!((ICPPASTDeclSpecifier) fdef.getDeclSpecifier()).isFriend()) {
					IASTDeclarator dtor = fdef.getDeclarator();
					dtor = ASTQueries.findInnermostDeclarator(dtor);
					binding = dtor.getName().resolveBinding();
					if (binding instanceof ICPPMethod) {
						result = ArrayUtil.appendAt(result, resultSize++, (ICPPMethod) binding);
					}
				}
			} else if (decl instanceof ICPPASTUsingDeclaration) {
				IASTName n = ((ICPPASTUsingDeclaration) decl).getName();
				binding = n.resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] bs = ((ICPPUsingDeclaration) binding).getDelegates();
					for (IBinding element : bs) {
						if (element instanceof ICPPMethod)
							result = ArrayUtil.appendAt(result, resultSize++, (ICPPMethod) element);
					}
				} else if (binding instanceof ICPPMethod) {
					result = ArrayUtil.appendAt(result, resultSize++, (ICPPMethod) binding);
				}
			}
		}
		return ArrayUtil.trim(result, resultSize);
	}

	/**
	 * @see ICPPClassType#getConstructors()
	 */
	public static ICPPConstructor[] getConstructors(ICPPInternalClassTypeMixinHost host) {
		ICPPClassScope scope = host.getCompositeScope();
		if (scope == null) {
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
		ICPPConstructor[] constructors = scope.getConstructors();
		return getAllConstructors(host, constructors);
	}

	/**
	 * Returns all constructors for a given class type. The returned constructors include the explicitly
	 * declared, the implicit, and the inherited ones.
	 *
	 * @param classType the class to get the constructors for
	 * @param declaredAndImplicitConstructors the declared and implicit constructors of the class
	 * @return an array of all class constructors
	 */
	public static ICPPConstructor[] getAllConstructors(ICPPClassType classType,
			ICPPConstructor[] declaredAndImplicitConstructors) {
		IType[][] paramTypes = new IType[declaredAndImplicitConstructors.length][];
		for (int i = 0; i < declaredAndImplicitConstructors.length; i++) {
			ICPPConstructor ctor = declaredAndImplicitConstructors[i];
			paramTypes[i] = ctor.getType().getParameterTypes();
		}
		ICPPConstructor[] inheritedConstructors = getInheritedConstructors(
				(ICPPClassScope) classType.getCompositeScope(), classType.getBases(), paramTypes);
		return ArrayUtil.addAll(declaredAndImplicitConstructors, inheritedConstructors);
	}

	/**
	 * Returns inherited constructors for a given class scope.
	 *
	 * @param scope the composite scope of the class to get the constructors for
	 * @param bases the base class relationships of the class
	 * @param existingConstructorParamTypes parameter types of the declared and the implicit constructors
	 * @return an array of all inherited constructors
	 */
	public static ICPPConstructor[] getInheritedConstructors(ICPPClassScope scope, ICPPBase[] bases,
			IType[][] existingConstructorParamTypes) {
		ICPPConstructor[] inheritedConstructors = ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		int n = 0;
		for (ICPPBase base : bases) {
			if (!base.isInheritedConstructorsSource())
				continue;
			IBinding baseType = base.getBaseClass();
			if (!(baseType instanceof ICPPClassType))
				continue;
			ICPPClassType baseClass = (ICPPClassType) baseType;
			ICPPConstructor[] ctors = baseClass.getConstructors();
			for (ICPPConstructor ctor : ctors) {
				if (canBeInherited(ctor, baseClass, existingConstructorParamTypes))
					inheritedConstructors = appendAt(inheritedConstructors, n++, ctor);
			}
		}
		return trim(inheritedConstructors, n);
	}

	private static boolean canBeInherited(ICPPConstructor ctor, ICPPClassType baseClass,
			IType[][] existingConstructorParamTypes) {
		ICPPParameter[] params = ctor.getParameters();
		// http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2015/p0136r1.html
		// 7.3.3-4 [Note] If a constructor or assignment operator brought from a base class into a derived
		// class has the signature of a copy/move constructor or assignment operator for the derived class
		// (12.8), the using-declaration does not by itself suppress the implicit declaration of the derived
		// class member; the member from the base class is hidden or overridden by the implicitly-declared
		// copy/move constructor or assignment operator of the derived class, as described below.
		for (int k = Math.max(ctor.getRequiredArgumentCount(), 1); k <= params.length; k++) {
			if (k == 1 && isReferenceToClass(params[0].getType(), baseClass)) {
				continue; // Skip the copy constructor.
			}
			if (findMatchingSignature(params, k, existingConstructorParamTypes) < 0) {
				return true;
			}
		}
		return false;
	}

	private static boolean isReferenceToClass(IType type, IType classType) {
		type = SemanticUtil.getNestedType(type, TDEF);
		if (type instanceof ICPPReferenceType && !((ICPPReferenceType) type).isRValueReference()) {
			type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
			return classType.isSameType(type);
		}
		return false;
	}

	private static int findMatchingSignature(ICPPParameter[] params, int numParams, IType[][] paramTypes) {
		for (int i = 0; i < paramTypes.length; i++) {
			if (doParameterTypesMatch(params, numParams, paramTypes[i]))
				return i;
		}
		return -1;
	}

	private static boolean doParameterTypesMatch(ICPPParameter[] params, int numParams, IType[] types) {
		if (numParams != types.length)
			return false;
		for (int i = 0; i < numParams; i++) {
			if (!params[i].getType().isSameType(types[i]))
				return false;
		}
		return true;
	}

	public static ICPPClassType[] getNestedClasses(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(host);
				if (backup != null)
					return backup.getNestedClasses();

				return ICPPClassType.EMPTY_CLASS_ARRAY;
			}
		}

		ICPPClassType[] result = ICPPClassType.EMPTY_CLASS_ARRAY;
		int resultSize = 0;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				IBinding binding = null;
				IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					binding = ((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
				} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier
						&& ((IASTSimpleDeclaration) decl).getDeclarators().length == 0) {
					binding = ((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding();
				}
				if (binding instanceof ICPPClassType)
					result = ArrayUtil.appendAt(result, resultSize++, (ICPPClassType) binding);
			}
		}
		return ArrayUtil.trim(result, resultSize);
	}

	public static ICPPUsingDeclaration[] getUsingDeclarations(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(host);
				if (backup != null)
					return backup.getUsingDeclarations();

				return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
			}
		}
		ICPPUsingDeclaration[] result = ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
		int resultSize = 0;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			if (decl instanceof ICPPASTUsingDeclaration) {
				IBinding binding = ((ICPPASTUsingDeclaration) decl).getName().resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					result = ArrayUtil.appendAt(result, resultSize++, (ICPPUsingDeclaration) binding);
				}
			}
		}
		return ArrayUtil.trim(result, resultSize);

	}

	public static ICPPField[] getFields(ICPPClassType ct) {
		ICPPField[] fields = ct.getDeclaredFields();
		ICPPClassType[] bases = getAllBases(ct);
		for (ICPPClassType base : bases) {
			fields = ArrayUtil.addAll(ICPPField.class, fields, base.getDeclaredFields());
		}
		return ArrayUtil.trim(ICPPField.class, fields);
	}

	public static IField findField(ICPPClassType ct, String name) {
		IBinding[] bindings = CPPSemantics.findBindings(ct.getCompositeScope(), name, true);
		IField field = null;
		for (IBinding binding : bindings) {
			if (binding instanceof IField) {
				if (field == null) {
					field = (IField) binding;
				} else {
					IASTNode[] decls = ASTInternal.getDeclarationsOfBinding(ct);
					IASTNode node = (decls != null && decls.length > 0) ? decls[0] : null;
					return new CPPField.CPPFieldProblem(ct, node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
							name.toCharArray());
				}
			}
		}
		return field;
	}

	/**
	 * Returns whether {@code method} is virtual. This is the case if it is declared to be virtual
	 * or overrides another virtual method.
	 */
	public static boolean isVirtual(ICPPMethod m) {
		if (m instanceof ICPPConstructor)
			return false;
		if (m.isVirtual())
			return true;

		final char[] mname = m.getNameCharArray();
		final ICPPClassType mcl = m.getClassOwner();
		if (mcl != null) {
			final ICPPFunctionType mft = m.getType();
			ICPPMethod[] allMethods = ClassTypeHelper.getMethods(mcl);
			for (ICPPMethod method : allMethods) {
				if (CharArrayUtils.equals(mname, method.getNameCharArray())
						&& functionTypesAllowOverride(mft, method.getType())) {
					if (method.isVirtual()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the function types are consistent enough to be considered overrides.
	 */
	private static boolean functionTypesAllowOverride(ICPPFunctionType a, ICPPFunctionType b) {
		if (a.isConst() != b.isConst() || a.isVolatile() != b.isVolatile() || a.takesVarArgs() != b.takesVarArgs()) {
			return false;
		}

		IType[] paramsA = a.getParameterTypes();
		IType[] paramsB = b.getParameterTypes();

		if (paramsA.length == 1 && paramsB.length == 0) {
			if (!SemanticUtil.isVoidType(paramsA[0]))
				return false;
		} else if (paramsB.length == 1 && paramsA.length == 0) {
			if (!SemanticUtil.isVoidType(paramsB[0]))
				return false;
		} else if (paramsA.length != paramsB.length) {
			return false;
		} else {
			for (int i = 0; i < paramsA.length; i++) {
				if (paramsA[i] == null || !paramsA[i].isSameType(paramsB[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} if {@code source} overrides {@code target}.
	 */
	public static boolean isOverrider(ICPPMethod source, ICPPMethod target) {
		if (source instanceof ICPPConstructor || target instanceof ICPPConstructor)
			return false;
		if (!isVirtual(target))
			return false;
		if (!functionTypesAllowOverride(source.getType(), target.getType()))
			return false;

		final ICPPClassType sourceClass = source.getClassOwner();
		final ICPPClassType targetClass = target.getClassOwner();
		if (sourceClass == null || targetClass == null)
			return false;

		ICPPClassType[] bases = getAllBases(sourceClass);
		for (ICPPClassType base : bases) {
			if (base.isSameType(targetClass))
				return true;
		}

		return false;
	}

	/**
	 * Returns all methods that are overridden by the given {@code method}.
	 */
	public static ICPPMethod[] findOverridden(ICPPMethod method) {
		if (method instanceof ICPPConstructor)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		final char[] mname = method.getNameCharArray();
		final ICPPClassType mcl = method.getClassOwner();
		if (mcl == null)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		final ArrayList<ICPPMethod> result = new ArrayList<>();
		final HashMap<ICPPClassType, Boolean> virtualInClass = new HashMap<>();
		final ICPPFunctionType methodType = method.getType();

		virtualInClass.put(mcl, method.isVirtual());
		ICPPBase[] bases = mcl.getBases();
		for (ICPPBase base : bases) {
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType) {
				findOverridden((ICPPClassType) b, mname, methodType, virtualInClass, result,
						CPPSemantics.MAX_INHERITANCE_DEPTH);
			}
		}

		// List is filled from most derived up to here, reverse it.
		Collections.reverse(result);
		return result.toArray(new ICPPMethod[result.size()]);
	}

	/**
	 * Searches for overridden methods starting in {@code classType}. The map {@code virtualInClass}
	 * contains a mapping of classes that have been visited to the information whether they
	 * (or a base-class) contain an overridden method.
	 *
	 * @return whether {@code classType} contains an overridden method.
	 */
	private static boolean findOverridden(ICPPClassType classType, char[] methodName, ICPPFunctionType methodType,
			Map<ICPPClassType, Boolean> virtualInClass, List<ICPPMethod> result, int maxdepth) {
		// Prevent recursion due to a hierarchy of unbounded depth, e.g. A<I> deriving from A<I - 1>.
		if (maxdepth <= 0)
			return false;

		Boolean visitedBefore = virtualInClass.get(classType);
		if (visitedBefore != null)
			return visitedBefore;

		ICPPMethod[] methods = classType.getDeclaredMethods();
		ICPPMethod candidate = null;
		boolean hasOverridden = false;
		for (ICPPMethod method : methods) {
			if (methodName[0] == '~' && method.isDestructor()
					|| (CharArrayUtils.equals(methodName, method.getNameCharArray())
							&& functionTypesAllowOverride(methodType, method.getType()))) {
				candidate = method;
				hasOverridden = method.isVirtual();
				break;
			}
		}

		// Prevent recursion due to a class inheriting (directly or indirectly) from itself.
		virtualInClass.put(classType, hasOverridden);
		ICPPBase[] bases = classType.getBases();
		for (ICPPBase base : bases) {
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType) {
				if (findOverridden((ICPPClassType) b, methodName, methodType, virtualInClass, result, maxdepth - 1)) {
					hasOverridden = true;
				}
			}
		}
		if (hasOverridden) {
			// The candidate is virtual.
			if (candidate != null)
				result.add(candidate);
			virtualInClass.put(classType, hasOverridden);
		}
		return hasOverridden;
	}

	/**
	 * Returns all methods found in the index, that override the given {@code method}.
	 */
	public static ICPPMethod[] findOverriders(IIndex index, ICPPMethod method) throws CoreException {
		if (!isVirtual(method))
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		final ICPPClassType mcl = method.getClassOwner();
		if (mcl == null)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		ICPPClassType[] subclasses = getSubClasses(index, mcl);
		return findOverriders(subclasses, method);
	}

	/**
	 * Returns all methods belonging to the given set of classes that override the given {@code method}.
	 */
	public static ICPPMethod[] findOverriders(ICPPClassType[] subclasses, ICPPMethod method) {
		final char[] mname = method.getNameCharArray();
		final ICPPFunctionType mft = method.getType();
		final ArrayList<ICPPMethod> result = new ArrayList<>();
		for (ICPPClassType subClass : subclasses) {
			ICPPMethod[] methods = subClass.getDeclaredMethods();
			for (ICPPMethod candidate : methods) {
				if (CharArrayUtils.equals(mname, candidate.getNameCharArray())
						&& functionTypesAllowOverride(mft, candidate.getType())) {
					result.add(candidate);
				}
			}
		}
		return result.toArray(new ICPPMethod[result.size()]);
	}

	private static ICPPClassType[] getSubClasses(IIndex index, ICPPClassType mcl) throws CoreException {
		Deque<ICPPBinding> result = new ArrayDeque<>();
		HashSet<String> handled = new HashSet<>();
		getSubClasses(index, mcl, result, handled);
		result.removeFirst();
		return result.toArray(new ICPPClassType[result.size()]);
	}

	private static void getSubClasses(IIndex index, ICPPBinding classOrTypedef, Collection<ICPPBinding> result,
			HashSet<String> handled) throws CoreException {
		if (!(classOrTypedef instanceof IType))
			return;

		final String key = ASTTypeUtil.getType((IType) classOrTypedef, true);
		if (!handled.add(key)) {
			return;
		}

		if (classOrTypedef instanceof ICPPClassType) {
			result.add(classOrTypedef);
		}

		// TODO(nathanridge): Also find subclasses referenced via decltype-specifiers rather than names.
		IIndexName[] names = index.findNames(classOrTypedef, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
		for (IIndexName indexName : names) {
			if (indexName.isBaseSpecifier()) {
				IIndexName subClassDef = indexName.getEnclosingDefinition();
				if (subClassDef != null) {
					IBinding subClass = index.findBinding(subClassDef);
					if (subClass instanceof ICPPBinding) {
						getSubClasses(index, (ICPPBinding) subClass, result, handled);
					}
				}
			}
		}
	}

	public enum MethodKind {
		DEFAULT_CTOR, COPY_CTOR, MOVE_CTOR, COPY_ASSIGNMENT_OP, MOVE_ASSIGNMENT_OP, DTOR, OTHER
	}

	public static MethodKind getMethodKind(ICPPClassType classType, ICPPMethod method) {
		if (method instanceof ICPPConstructor) {
			final List<IType> params = getTypesOfRequiredParameters(method);
			if (params.isEmpty())
				return MethodKind.DEFAULT_CTOR;
			if (params.size() == 1) {
				IType t = SemanticUtil.getNestedType(params.get(0), SemanticUtil.TDEF);
				if (SemanticUtil.isVoidType(t))
					return MethodKind.DEFAULT_CTOR;

				ICPPReferenceType refToClass = getRefToClass(classType, t);
				if (refToClass != null)
					return refToClass.isRValueReference() ? MethodKind.MOVE_CTOR : MethodKind.COPY_CTOR;
			}
			return MethodKind.OTHER;
		}

		if (method.isDestructor())
			return MethodKind.DTOR;

		if (CharArrayUtils.equals(method.getNameCharArray(), OverloadableOperator.ASSIGN.toCharArray())) {
			final List<IType> params = getTypesOfRequiredParameters(method);
			if (params.size() == 1) {
				IType t = params.get(0);
				ICPPReferenceType refToClass = getRefToClass(classType, t);
				if (refToClass != null)
					return refToClass.isRValueReference() ? MethodKind.MOVE_ASSIGNMENT_OP
							: MethodKind.COPY_ASSIGNMENT_OP;
			}
			return MethodKind.OTHER;
		}
		return MethodKind.OTHER;
	}

	/**
	 * Returns types of method parameters that don't have defaults.
	 */
	private static List<IType> getTypesOfRequiredParameters(ICPPMethod method) {
		ICPPParameter[] parameters = method.getParameters();
		if (parameters.length == 0)
			return Collections.emptyList();
		List<IType> types = new ArrayList<>(parameters.length);
		for (ICPPParameter parameter : parameters) {
			if (!parameter.hasDefaultValue() && !parameter.isParameterPack())
				types.add(parameter.getType());
		}
		return types;
	}

	/**
	 * For implicit methods the exception specification is inherited, search it.
	 */
	public static IType[] getInheritedExceptionSpecification(ICPPMethod implicitMethod) {
		// See 15.4.13
		ICPPClassType owner = implicitMethod.getClassOwner();
		if (owner == null || owner.getBases().length == 0)
			return null;

		// We use a list as types aren't comparable, and can have duplicates (15.4.6)
		MethodKind kind = getMethodKind(owner, implicitMethod);
		if (kind == MethodKind.OTHER)
			return null;

		List<IType> inheritedTypeids = new ArrayList<>();
		ICPPClassType[] bases = getAllBases(owner);
		for (ICPPClassType base : bases) {
			if (!(base instanceof ICPPDeferredClassInstance)) {
				ICPPMethod baseMethod = getMethodInClass(base, kind);
				if (baseMethod != null) {
					IType[] baseExceptionSpec = baseMethod.getExceptionSpecification();
					if (baseExceptionSpec == null)
						return null;
					for (IType baseTypeId : baseMethod.getExceptionSpecification()) {
						inheritedTypeids.add(baseTypeId);
					}
				}
			}
		}
		return inheritedTypeids.toArray(new IType[inheritedTypeids.size()]);
	}

	/**
	 * If {@code type} is a, possibly qualified, reference type referring to {@code classType},
	 * returns that reference type. Otherwise returns {@code null}.
	 */
	private static ICPPReferenceType getRefToClass(ICPPClassType classType, IType type) {
		while (type instanceof ITypedef) {
			type = ((ITypedef) type).getType();
		}

		if (type instanceof ICPPReferenceType) {
			ICPPReferenceType refType = (ICPPReferenceType) type;
			type = refType.getType();
			while (type instanceof ITypedef) {
				type = ((ITypedef) type).getType();
			}
			if (type instanceof IQualifierType) {
				type = ((IQualifierType) type).getType();
				if (classType.isSameType(type))
					return refType;
			}
		}
		return null;
	}

	public static ICPPMethod getMethodInClass(ICPPClassType ct, MethodKind kind) {
		switch (kind) {
		case DEFAULT_CTOR:
		case COPY_CTOR:
		case MOVE_CTOR:
			for (ICPPConstructor ctor : ct.getConstructors()) {
				if (!ctor.isImplicit() && getMethodKind(ct, ctor) == kind)
					return ctor;
			}
			return null;
		case COPY_ASSIGNMENT_OP:
		case MOVE_ASSIGNMENT_OP:
			for (ICPPMethod method : ct.getDeclaredMethods()) {
				if (method instanceof ICPPConstructor)
					continue;
				if (getMethodKind(ct, method) == kind)
					return method;
			}
			return null;
		case DTOR:
			for (ICPPMethod method : ct.getDeclaredMethods()) {
				if (method.isDestructor())
					return method;
			}
			return null;
		case OTHER:
			break;
		}
		return null;
	}

	/**
	 * Returns the visibility for a given {@code member} in the {@code host}.
	 * Throws an IllegalArgumentException if {@code member} is not a member of {@code host}
	 *
	 * @param classType The class to get the member's visibility specifier of.
	 * @return the visibility of the {@code member}.
	 */
	public static int getVisibility(ICPPInternalClassTypeMixinHost classType, IBinding member) {
		if (classType.getDefinition() == null) {
			classType.checkForDefinition();
			if (classType.getDefinition() == null) {
				ICPPClassType backup = getBackupDefinition(classType);
				if (backup != null) {
					return backup.getVisibility(member);
				}
				if (classType instanceof ICPPClassSpecialization) {
					// A class instance doesn't have a definition. Delegate to the class template.
					ICPPClassType specialized = ((ICPPClassSpecialization) classType).getSpecializedBinding();
					if (!specialized.equals(member.getOwner())) {
						if (!(member instanceof ICPPSpecialization))
							throw invalidMember(specialized, member);
						member = ((ICPPSpecialization) member).getSpecializedBinding();
					}
					return specialized.getVisibility(member);
				}

				return ICPPClassType.v_public; // Fallback visibility
			}
		}

		// The concept of visibility does not apply to a lambda, which can end
		// up having a class as its owner if they are used in the initializer
		// of a field or a member function parameter.
		if (member instanceof CPPClosureType) {
			return ICPPClassType.v_public;
		}

		ICPPASTCompositeTypeSpecifier classDeclSpec = classType.getCompositeTypeSpecifier();
		int visibility = getVisibility(classDeclSpec, member);
		if (visibility >= 0)
			return visibility;

		ICPPMethod[] implicitMethods = getImplicitMethods(classType);
		for (ICPPMethod implicitMethod : implicitMethods) {
			if (member.equals(implicitMethod)) {
				return ICPPClassType.v_public;
			}
		}

		// It's possible that we haven't found the member because the class was illegally redefined
		// and the member belongs to another definition. Try to search the definition containing
		// the member.
		if (member instanceof ICPPInternalBinding) {
			IASTNode node = ((ICPPInternalBinding) member).getDefinition();
			if (node != null) {
				IASTName ownerName = CPPVisitor.findDeclarationOwnerDefinition(node, false);
				if (ownerName != null && !ownerName.equals(classDeclSpec.getName())
						&& ownerName.getPropertyInParent() == ICPPASTCompositeTypeSpecifier.TYPE_NAME) {
					classDeclSpec = (ICPPASTCompositeTypeSpecifier) ownerName.getParent();
					visibility = getVisibility(classDeclSpec, member);
					if (visibility >= 0)
						return visibility;
				}
			}
		}

		throw invalidMember(classType, member);
	}

	private static int getVisibility(ICPPASTCompositeTypeSpecifier classDeclSpec, IBinding member) {
		int visibility = classDeclSpec.getKey() == ICPPASTCompositeTypeSpecifier.k_class ? ICPPClassType.v_private
				: ICPPClassType.v_public;
		IASTDeclaration[] hostMembers = classDeclSpec.getMembers();
		for (IASTDeclaration hostMember : hostMembers) {
			if (hostMember instanceof ICPPASTVisibilityLabel) {
				visibility = ((ICPPASTVisibilityLabel) hostMember).getVisibility();
				continue;
			}
			while (hostMember instanceof ICPPASTTemplateDeclaration) {
				hostMember = ((ICPPASTTemplateDeclaration) hostMember).getDeclaration();
			}
			if (hostMember instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration memberDeclaration = (IASTSimpleDeclaration) hostMember;
				for (IASTDeclarator memberDeclarator : memberDeclaration.getDeclarators()) {
					IBinding memberBinding = ASTQueries.findInnermostDeclarator(memberDeclarator).getName()
							.resolveBinding();
					if (member.equals(memberBinding)) {
						return visibility;
					}
				}

				IASTDeclSpecifier declSpec = memberDeclaration.getDeclSpecifier();
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					IBinding memberBinding = ((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
					if (member.equals(memberBinding)) {
						return visibility;
					}
				} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier
						&& memberDeclaration.getDeclarators().length == 0) {
					IBinding memberBinding = ((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding();
					if (member.equals(memberBinding)) {
						return visibility;
					}
				} else if (declSpec instanceof ICPPASTEnumerationSpecifier) {
					IBinding enumerationBinding = ((ICPPASTEnumerationSpecifier) declSpec).getName().resolveBinding();
					if (member.equals(enumerationBinding)) {
						return visibility;
					}
				}
			} else if (hostMember instanceof IASTFunctionDefinition) {
				IASTDeclarator declarator = ((IASTFunctionDefinition) hostMember).getDeclarator();
				declarator = ASTQueries.findInnermostDeclarator(declarator);
				IBinding functionBinding = declarator.getName().resolveBinding();
				if (member.equals(functionBinding)) {
					return visibility;
				}
			} else if (hostMember instanceof ICPPASTAliasDeclaration) {
				IBinding aliasBinding = ((ICPPASTAliasDeclaration) hostMember).getAlias().resolveBinding();
				if (member.equals(aliasBinding)) {
					return visibility;
				}
			} else if (hostMember instanceof ICPPASTUsingDeclaration) {
				IBinding usingBinding = ((ICPPASTUsingDeclaration) hostMember).getName().resolveBinding();
				if (member.equals(usingBinding)) {
					return visibility;
				}
			} else if (hostMember instanceof ICPPASTNamespaceDefinition) { // Not valid but possible due to the parser
				IBinding namespaceBinding = ((ICPPASTNamespaceDefinition) hostMember).getName().resolveBinding();
				if (member.equals(namespaceBinding)) {
					return visibility;
				}
			}
		}
		return -1;
	}

	private static IllegalArgumentException invalidMember(IBinding classType, IBinding member) {
		String name = member.getName();
		if (name.isEmpty())
			name = "<anonymous>"; //$NON-NLS-1$
		return new IllegalArgumentException(name + " is not a member of " + classType.getName()); //$NON-NLS-1$
	}
}
