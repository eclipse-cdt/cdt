/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext.getContextClassSpecialization;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructorSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumerationSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFieldTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartiallySpecializable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUnaryTypeTransformation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPAliasTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPAliasTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization.RecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredVariableInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumerationSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeParameterSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTemplateParameterSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeParameterSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedefSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDeclarationSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariableInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariableSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariableTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariableTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPComputableFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredVariableInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * Collection of static methods to perform template instantiation, member specialization and
 * type instantiation.
 */
public class CPPTemplates {
	// The three constants below are used as special return values for the various overloads
	// of CPPTemplates.determinePackSize() and for ICPPEvaluation.determinePackSize(), which
	// search a type, template argument, or value for a usage of a template parameter pack
	// and return the number of arguments bound to that parameter pack in an
	// ICPPTemplateParameterMap.

	// Used to indicate that the parameter pack is not bound to any arguments in the
	// template parameter map. Computation of the pack size needs to be deferred until
	// arguments for it become available.
	static final int PACK_SIZE_DEFER = -1;

	// Used to indicate that two different packs with different sizes were found.
	static final int PACK_SIZE_FAIL = -2;

	// Used to indicate that no template parameter packs were found.
	static final int PACK_SIZE_NOT_FOUND = Integer.MAX_VALUE;

	static enum TypeSelection {
		PARAMETERS, RETURN_TYPE, PARAMETERS_AND_RETURN_TYPE
	}

	// Infrastructure to protect against rogue template metaprograms that don't terminate.
	private static final int TEMPLATE_INSTANTIATION_DEPTH_LIMIT = 128;
	private static final ThreadLocal<Integer> fTemplateInstantiationDepth = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};
	private static final ThreadLocal<Set<TypeInstantiationRequest>> instantiationsInProgress = new ThreadLocal<Set<TypeInstantiationRequest>>() {
		@Override
		protected Set<TypeInstantiationRequest> initialValue() {
			return new HashSet<>();
		}
	};

	/**
	 * Instantiates a class or variable template with the given arguments. May return {@code null}.
	 */
	public static IBinding instantiate(ICPPPartiallySpecializable template, ICPPTemplateArgument[] args) {
		return instantiate(template, args, false, false);
	}

	/**
	 * Instantiates a class template with the given arguments. May return {@code null}.
	 */
	private static IBinding instantiate(ICPPPartiallySpecializable template, ICPPTemplateArgument[] args,
			boolean isDefinition, boolean isExplicitSpecialization) {
		try {
			ICPPTemplateArgument[] arguments = SemanticUtil.getSimplifiedArguments(args);
			// Add default arguments, if necessary.
			arguments = addDefaultArguments(template, arguments);
			if (arguments == null)
				return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);

			if (template instanceof ICPPTemplateTemplateParameter || hasDependentArgument(arguments)) {
				return deferredInstance(template, arguments);
			}

			if (template instanceof ICPPClassTemplatePartialSpecialization) {
				return instantiatePartialSpecialization((ICPPClassTemplatePartialSpecialization) template, arguments,
						isDefinition, null);
			}

			if (arguments == args) {
				arguments = args.clone(); // The createParameterMap call may modify the arguments array.
			}
			CPPTemplateParameterMap map = createParameterMap(template, arguments);
			if (map == null) {
				return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
			}

			ICPPTemplateInstance prim = getInstance(template, arguments, isDefinition);
			if (prim != null && (isExplicitSpecialization || prim.isExplicitSpecialization()))
				return prim;

			if (!isExplicitSpecialization) {
				IBinding result = selectSpecialization(template, arguments, isDefinition);
				if (result != null)
					return result;
			}

			return instantiatePrimaryTemplate(template, arguments, new InstantiationContext(map), isDefinition);
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	/**
	 * Instantiates an alias template with the given arguments.
	 */
	public static IBinding instantiateAliasTemplate(ICPPAliasTemplate aliasTemplate, ICPPTemplateArgument[] args) {
		try {
			args = addDefaultArguments(aliasTemplate, args);
			if (args == null) {
				return createProblem(aliasTemplate, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
			}
			ICPPTemplateParameterMap parameterMap = createParameterMap(aliasTemplate, args);
			if (parameterMap == null) {
				return createProblem(aliasTemplate, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
			}
			IType aliasedType = aliasTemplate.getType();
			IBinding owner = aliasTemplate.getOwner();
			return createAliasTemplateInstance(aliasTemplate, args, parameterMap, aliasedType, owner);
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	private static IBinding createProblem(ICPPTemplateDefinition template, int id) {
		return new ProblemBinding(CPPSemantics.getCurrentLookupPoint(), id, template.getNameCharArray());
	}

	static IBinding isUsedInClassTemplateScope(ICPPClassTemplate ct, IASTName name) {
		try {
			IScope scope = null;
			IASTNode node = name;
			while (node != null) {
				if (node.getPropertyInParent() == IASTCompositeTypeSpecifier.TYPE_NAME)
					return null;
				if (node instanceof IASTFunctionDefinition) {
					IASTName functionName = ASTQueries
							.findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator()).getName()
							.getLastName();
					// 'name' may be inside the qualifier of a method name in a out-of-line method definition.
					// In such a case, calling getContainingScope() on the method name will attempt to
					// resolve the qualifier, which will attempt to resolve 'name', which will get into
					// a recursion as 'name' is currently being resolved. Since an out-of-line method
					// definition cannot be inside a template scope, we can accurately return null
					// in this case.
					if (functionName.getParent() instanceof ICPPASTQualifiedName
							&& ASTQueries.isAncestorOf(functionName.getParent(), name)) {
						return null;
					}
					scope = CPPVisitor.getContainingScope(functionName);
					break;
				}
				if (node instanceof ICPPASTCompositeTypeSpecifier) {
					scope = ((ICPPASTCompositeTypeSpecifier) node).getScope();
					break;
				}
				node = node.getParent();
			}

			while (scope != null) {
				if (scope instanceof ISemanticProblem)
					return null;
				if (scope instanceof ICPPClassScope) {
					ICPPClassType b = ((ICPPClassScope) scope).getClassType();
					if (b != null && ct.isSameType(b)) {
						return ct;
					}
					if (b instanceof ICPPClassTemplatePartialSpecialization) {
						ICPPClassTemplatePartialSpecialization pspec = (ICPPClassTemplatePartialSpecialization) b;
						if (ct.isSameType(pspec.getPrimaryClassTemplate())) {
							return pspec;
						}
					} else if (b instanceof ICPPClassSpecialization) {
						ICPPClassSpecialization specialization = (ICPPClassSpecialization) b;
						if (ct.isSameType(specialization.getSpecializedBinding())) {
							return specialization;
						}
					}
				}
				if (scope instanceof IASTInternalScope) {
					IASTInternalScope internalScope = (IASTInternalScope) scope;
					IASTNode physicalNode = internalScope.getPhysicalNode();
					if (physicalNode instanceof ICPPASTCompositeTypeSpecifier
							&& ((ICPPASTCompositeTypeSpecifier) physicalNode)
									.getName() instanceof ICPPASTQualifiedName) {
						scope = scope.getParent();
					} else {
						scope = CPPVisitor.getContainingScope(physicalNode);
						if (scope == internalScope)
							return null;
					}
				} else {
					scope = scope.getParent();
				}
			}
		} catch (DOMException e) {
		}
		return null;
	}

	public static IBinding instantiateFunctionTemplate(ICPPFunctionTemplate template, ICPPTemplateArgument[] arguments,
			ICPPTemplateParameterMap tpMap) throws DOMException {
		ICPPTemplateInstance instance = getInstance(template, arguments, false);
		if (instance != null) {
			return instance;
		}

		IBinding owner = template.getOwner();
		instance = createInstance(owner, template, tpMap, arguments);
		if (instance instanceof ICPPFunction && SemanticUtil.isValidType(((ICPPFunction) instance).getType())) {
			addInstance(template, arguments, instance);
		}
		return instance;
	}

	/**
	 * Instantiates a partial class template specialization.
	 */
	private static IBinding instantiatePartialSpecialization(ICPPPartialSpecialization partialSpec,
			ICPPTemplateArgument[] args, boolean isDef, CPPTemplateParameterMap tpMap) throws DOMException {
		ICPPTemplateInstance instance = getInstance(partialSpec, args, isDef);
		if (instance != null)
			return instance;

		if (tpMap == null) {
			tpMap = new CPPTemplateParameterMap(args.length);
			if (!TemplateArgumentDeduction.fromTemplateArguments(partialSpec.getTemplateParameters(),
					partialSpec.getTemplateArguments(), args, tpMap)) {
				return null;
			}
		}

		instance = createInstance(partialSpec.getOwner(), partialSpec, tpMap, args);
		addInstance(partialSpec, args, instance);
		return instance;
	}

	/**
	 * Instantiates the selected template, without looking for specializations.
	 * May return {@code null}.
	 */
	private static IBinding instantiatePrimaryTemplate(ICPPPartiallySpecializable template,
			ICPPTemplateArgument[] arguments, InstantiationContext context, boolean isDef) throws DOMException {
		assert !(template instanceof ICPPClassTemplatePartialSpecialization);
		ICPPTemplateInstance instance = getInstance(template, arguments, isDef);
		if (instance != null) {
			return instance;
		}

		IBinding owner = template.getOwner();
		instance = createInstance(owner, template, context.getParameterMap(), arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	/**
	 * Obtains a cached instance from the template.
	 */
	private static ICPPTemplateInstance getInstance(ICPPTemplateDefinition template, ICPPTemplateArgument[] args,
			boolean forDefinition) {
		if (template instanceof ICPPInstanceCache) {
			ICPPTemplateInstance result = ((ICPPInstanceCache) template).getInstance(args);
			if (forDefinition && result instanceof IIndexBinding)
				return null;
			if (result != null && !result.isExplicitSpecialization()) {
				// Don't use the cached instance if its argument is an index type and the requested
				// argument is an AST type. Despite identical signatures the types may be different.
				ICPPTemplateArgument[] instanceArgs = result.getTemplateArguments();
				for (int i = 0; i < args.length; i++) {
					if (!(args[i].getTypeValue() instanceof IIndexType)
							&& (instanceArgs[i].getTypeValue() instanceof IIndexType)) {
						return null;
					}
				}
			}
			return result;
		}
		return null;
	}

	/**
	 * Caches an instance with the template.
	 */
	private static void addInstance(ICPPTemplateDefinition template, ICPPTemplateArgument[] args,
			ICPPTemplateInstance instance) {
		if (template instanceof ICPPInstanceCache) {
			((ICPPInstanceCache) template).addInstance(args, instance);
		}
	}

	private static IBinding deferredInstance(ICPPPartiallySpecializable template, ICPPTemplateArgument[] arguments)
			throws DOMException {
		ICPPTemplateInstance instance = getInstance(template, arguments, false);
		if (instance != null)
			return instance;

		if (template instanceof ICPPClassTemplate) {
			instance = new CPPDeferredClassInstance((ICPPClassTemplate) template, arguments);
			addInstance(template, arguments, instance);
		}
		if (template instanceof ICPPVariableTemplate) {
			instance = new CPPDeferredVariableInstance((ICPPVariableTemplate) template, arguments);
			addInstance(template, arguments, instance);
		}
		return instance;
	}

	private static ICPPTemplateArgument[] addDefaultArguments(ICPPTemplateDefinition template,
			ICPPTemplateArgument[] arguments) throws DOMException {
		if (template instanceof ICPPClassTemplatePartialSpecialization)
			return arguments;

		boolean havePackExpansion = false;
		for (int i = 0; i < arguments.length; i++) {
			ICPPTemplateArgument arg = arguments[i];
			if (arg.isPackExpansion()) {
				if (i != arguments.length - 1) {
					return arguments;
				}
				havePackExpansion = true;
			}
		}

		ICPPTemplateParameter[] tpars = template.getTemplateParameters();
		int tparCount = tpars.length;
		final int argCount = arguments.length;

		if (tparCount == argCount)
			return arguments;

		if (tparCount == 0)
			return null;

		// More arguments allowed if we have a parameter pack.
		if (tparCount < argCount) {
			if (tpars[tparCount - 1].isParameterPack())
				return arguments;

			if (havePackExpansion && tparCount + 1 == argCount)
				return arguments;
			return null;
		}

		// Fewer arguments are allowed with a pack expansion
		if (havePackExpansion)
			return arguments;

		// Fewer arguments are allowed with	default arguments
		if (tpars[tparCount - 1].isParameterPack())
			tparCount--;

		if (tparCount == argCount)
			return arguments;

		ICPPTemplateArgument[] completeArgs = new ICPPTemplateArgument[tparCount];
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(tparCount);
		InstantiationContext context = new InstantiationContext(map);
		for (int i = 0; i < tparCount; i++) {
			final ICPPTemplateParameter tpar = tpars[i];
			if (tpar.isParameterPack()) {
				// Parameter pack must be last template parameter.
				return null;
			}
			ICPPTemplateArgument arg;
			if (i < argCount) {
				arg = arguments[i];
			} else {
				ICPPTemplateArgument defaultArg = tpar.getDefaultValue();
				if (defaultArg == null) {
					if (template instanceof ICPPInternalClassTemplate) {
						defaultArg = ((ICPPInternalClassTemplate) template).getDefaultArgFromIndex(i);
					}
				}
				if (defaultArg == null)
					return null;
				arg = instantiateArgument(defaultArg, context);
				arg = SemanticUtil.getSimplifiedArgument(arg);
				if (!isValidArgument(arg)) {
					return null;
				}
			}
			context.addToParameterMap(tpar, arg);
			completeArgs[i] = arg;
		}
		return completeArgs;
	}

	public static ICPPDeferredClassInstance createDeferredInstance(ICPPClassTemplate ct) {
		ICPPTemplateArgument[] args;
		if (ct instanceof ICPPClassTemplatePartialSpecialization) {
			args = ((ICPPClassTemplatePartialSpecialization) ct).getTemplateArguments();
		} else {
			args = templateParametersAsArguments(ct);
		}
		return new CPPDeferredClassInstance(ct, args, (ICPPScope) ct.getCompositeScope());
	}

	public static ICPPTemplateArgument[] templateParametersAsArguments(ICPPClassTemplate template) {
		ICPPTemplateParameter[] tpars = template.getTemplateParameters();
		ICPPTemplateArgument[] args;
		args = new ICPPTemplateArgument[tpars.length];
		for (int i = 0; i < tpars.length; i++) {
			final ICPPTemplateParameter tp = tpars[i];
			if (tp instanceof IType) {
				IType t = (IType) tp;
				if (tp.isParameterPack()) {
					t = new CPPParameterPackType(t);
				}
				args[i] = new CPPTemplateTypeArgument(t);
			} else if (tp instanceof ICPPTemplateNonTypeParameter) {
				// Non-type template parameter pack already has type 'ICPPParameterPackType'
				final ICPPTemplateNonTypeParameter nttp = (ICPPTemplateNonTypeParameter) tp;
				args[i] = new CPPTemplateNonTypeArgument(DependentValue.create(template, nttp), nttp.getType());
			} else {
				assert false;
			}
		}
		return args;
	}

	/**
	 * Extracts the IASTName of a template parameter.
	 */
	public static IASTName getTemplateParameterName(ICPPASTTemplateParameter param) {
		if (param instanceof ICPPASTSimpleTypeTemplateParameter)
			return ((ICPPASTSimpleTypeTemplateParameter) param).getName();
		else if (param instanceof ICPPASTTemplatedTypeTemplateParameter)
			return ((ICPPASTTemplatedTypeTemplateParameter) param).getName();
		else if (param instanceof ICPPASTParameterDeclaration)
			return ASTQueries.findInnermostDeclarator(((ICPPASTParameterDeclaration) param).getDeclarator()).getName();
		return null;
	}

	public static ICPPTemplateDefinition getContainingTemplate(ICPPASTTemplateParameter param) {
		IASTNode parent = param.getParent();
		IBinding binding = null;
		if (parent instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration[] templates = new ICPPASTTemplateDeclaration[] {
					(ICPPASTTemplateDeclaration) parent };

			while (parent.getParent() instanceof ICPPASTTemplateDeclaration) {
				parent = parent.getParent();
				templates = ArrayUtil.append(ICPPASTTemplateDeclaration.class, templates,
						(ICPPASTTemplateDeclaration) parent);
			}
			templates = ArrayUtil.trim(ICPPASTTemplateDeclaration.class, templates);

			ICPPASTTemplateDeclaration templateDeclaration = templates[0];
			IASTDeclaration decl = templateDeclaration.getDeclaration();
			while (decl instanceof ICPPASTTemplateDeclaration) {
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			}

			IASTName name = null;
			if (decl instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
				if (dtors.length == 0) {
					IASTDeclSpecifier spec = simpleDecl.getDeclSpecifier();
					if (spec instanceof ICPPASTCompositeTypeSpecifier) {
						name = ((ICPPASTCompositeTypeSpecifier) spec).getName();
					} else if (spec instanceof ICPPASTElaboratedTypeSpecifier) {
						name = ((ICPPASTElaboratedTypeSpecifier) spec).getName();
					}
				} else {
					IASTDeclarator dtor = dtors[0];
					dtor = ASTQueries.findInnermostDeclarator(dtor);
					name = dtor.getName();
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				IASTDeclarator dtor = ((IASTFunctionDefinition) decl).getDeclarator();
				dtor = ASTQueries.findInnermostDeclarator(dtor);
				name = dtor.getName();
			} else if (decl instanceof ICPPASTAliasDeclaration) {
				name = ((ICPPASTAliasDeclaration) decl).getAlias();
			}
			if (name == null)
				return null;

			if (name instanceof ICPPASTQualifiedName) {
				int idx = templates.length;
				int i = 0;
				ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) name).getQualifier();
				for (ICPPASTNameSpecifier element : qualifier) {
					if (element instanceof ICPPASTTemplateId) {
						++i;
						if (i == idx) {
							binding = ((ICPPASTTemplateId) element).resolveBinding();
							break;
						}
					}
				}
				if (binding == null)
					binding = name.getLastName().resolveBinding();
			} else {
				binding = name.resolveBinding();
			}
		} else if (parent instanceof ICPPASTTemplatedTypeTemplateParameter) {
			ICPPASTTemplatedTypeTemplateParameter templatedParam = (ICPPASTTemplatedTypeTemplateParameter) parent;
			binding = templatedParam.getName().resolveBinding();
		}
		return (binding instanceof ICPPTemplateDefinition) ? (ICPPTemplateDefinition) binding : null;
	}

	public static IBinding createBinding(ICPPASTTemplateParameter tp) {
		if (tp instanceof ICPPASTSimpleTypeTemplateParameter) {
			return new CPPTemplateTypeParameter(((ICPPASTSimpleTypeTemplateParameter) tp).getName(),
					tp.isParameterPack());
		}
		if (tp instanceof ICPPASTTemplatedTypeTemplateParameter) {
			return new CPPTemplateTemplateParameter(((ICPPASTTemplatedTypeTemplateParameter) tp).getName(),
					tp.isParameterPack());
		}
		assert tp instanceof ICPPASTParameterDeclaration;
		final IASTDeclarator dtor = ((ICPPASTParameterDeclaration) tp).getDeclarator();
		return new CPPTemplateNonTypeParameter(ASTQueries.findInnermostDeclarator(dtor).getName());
	}

	public static IBinding createBinding(ICPPASTTemplateId id) {
		if (!isClassTemplate(id)) {
			// Functions are instantiated as part of the resolution process.
			IBinding result = CPPVisitor.createBinding(id);
			IASTName templateName = id.getTemplateName();
			if (result instanceof ICPPClassTemplate || result instanceof ICPPAliasTemplate
					|| result instanceof ICPPVariableTemplate) {
				templateName.setBinding(result);
				id.setBinding(null);
			} else {
				if (result instanceof ICPPTemplateInstance) {
					templateName.setBinding(((ICPPTemplateInstance) result).getTemplateDefinition());
				} else {
					templateName.setBinding(result);
				}
				return result;
			}
		}

		IASTNode parentOfName = id.getParent();
		boolean isLastName = true;
		if (parentOfName instanceof ICPPASTQualifiedName) {
			isLastName = ((ICPPASTQualifiedName) parentOfName).getLastName() == id;
			parentOfName = parentOfName.getParent();
		}

		boolean isDeclaration = false;
		boolean isDefinition = false;
		boolean isExplicitSpecialization = false;
		if (isLastName && parentOfName != null) {
			IASTNode declaration = parentOfName.getParent();
			if (declaration instanceof IASTSimpleDeclaration) {
				if (parentOfName instanceof ICPPASTElaboratedTypeSpecifier) {
					isDeclaration = true;
				} else if (parentOfName instanceof ICPPASTCompositeTypeSpecifier) {
					isDefinition = true;
				} else if (parentOfName instanceof ICPPASTDeclarator) {
					isDeclaration = true;
				}
				if (isDeclaration || isDefinition) {
					IASTNode parentOfDeclaration = declaration.getParent();
					if (parentOfDeclaration instanceof ICPPASTExplicitTemplateInstantiation) {
						isDeclaration = false;
					} else if (parentOfDeclaration instanceof ICPPASTTemplateSpecialization) {
						isExplicitSpecialization = true;
					}
				}
			}
		}
		CPPSemantics.pushLookupPoint(id);
		try {
			IBinding result = null;
			IASTName templateName = id.getTemplateName();
			IBinding template = templateName.resolvePreBinding();

			while (template instanceof CPPTypedefSpecialization) {
				template = ((CPPTypedefSpecialization) template).getSpecializedBinding();
			}

			// Alias template.
			if (template instanceof ICPPAliasTemplate) {
				ICPPAliasTemplate aliasTemplate = (ICPPAliasTemplate) template;
				ICPPTemplateArgument[] args = createTemplateArgumentArray(id);
				return instantiateAliasTemplate(aliasTemplate, args);
			}

			// Class or variable template.
			if (template instanceof ICPPConstructor) {
				template = template.getOwner();
			}

			if (template instanceof ICPPUnknownMemberClass) {
				IType owner = ((ICPPUnknownMemberClass) template).getOwnerType();
				ICPPTemplateArgument[] args = createTemplateArgumentArray(id);
				args = SemanticUtil.getSimplifiedArguments(args);
				return new CPPUnknownClassInstance(owner, id.getSimpleID(), args);
			}

			if (!(template instanceof ICPPPartiallySpecializable)
					|| template instanceof ICPPClassTemplatePartialSpecialization)
				return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE, templateName.toCharArray());

			final ICPPPartiallySpecializable classTemplate = (ICPPPartiallySpecializable) template;
			ICPPTemplateArgument[] args = createTemplateArgumentArray(id);
			if (hasDependentArgument(args)) {
				ICPPASTTemplateDeclaration tdecl = getTemplateDeclaration(id);
				if (tdecl != null) {
					if (argsAreTrivial(classTemplate.getTemplateParameters(), args)) {
						result = classTemplate;
					} else {
						args = addDefaultArguments(classTemplate, args);
						if (args == null) {
							return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS,
									templateName.toCharArray());
						}
						ICPPPartialSpecialization partialSpec = findPartialSpecialization(classTemplate, args);
						ICPPClassTemplatePartialSpecialization indexSpec = null;
						if ((isDeclaration || isDefinition)
								&& (partialSpec instanceof ICPPClassTemplatePartialSpecialization)) {
							indexSpec = (ICPPClassTemplatePartialSpecialization) partialSpec;
							partialSpec = null;
						}
						if (partialSpec == null) {
							if (isDeclaration || isDefinition) {
								if (template instanceof ICPPClassTemplate) {
									partialSpec = new CPPClassTemplatePartialSpecialization(id, args);
									if (indexSpec != null) {
										SemanticUtil.recordPartialSpecialization(indexSpec,
												(ICPPClassTemplatePartialSpecialization) partialSpec, id);
									} else if (template instanceof ICPPInternalClassTemplate) {
										((ICPPInternalClassTemplate) template).addPartialSpecialization(
												(ICPPClassTemplatePartialSpecialization) partialSpec);
									}
								} else if (template instanceof ICPPVariableTemplate) {
									if (template instanceof ICPPFieldTemplate) {
										partialSpec = new CPPFieldTemplatePartialSpecialization(id, args);
									} else {
										partialSpec = new CPPVariableTemplatePartialSpecialization(id, args);
									}
									if (template instanceof CPPVariableTemplate)
										((CPPVariableTemplate) template).addPartialSpecialization(partialSpec);
								}
								return partialSpec;
							}
							return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE,
									templateName.toCharArray());
						}
						result = partialSpec;
					}
				}
			}
			if (result == null) {
				result = instantiate(classTemplate, args, isDefinition, isExplicitSpecialization);
				if (result instanceof ICPPInternalBinding) {
					if (isDeclaration) {
						ASTInternal.addDeclaration(result, id);
					} else if (isDefinition) {
						ASTInternal.addDefinition(result, id);
					}
				}
			}
			return CPPSemantics.postResolution(result, id);
		} catch (DOMException e) {
			return e.getProblem();
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	private static IBinding createAliasTemplateInstance(ICPPAliasTemplate aliasTemplate, ICPPTemplateArgument[] args,
			ICPPTemplateParameterMap parameterMap, IType aliasedType, IBinding owner) {
		InstantiationContext context = createInstantiationContext(parameterMap, owner);
		IType instantiatedType = instantiateType(aliasedType, context);
		return new CPPAliasTemplateInstance(aliasTemplate, instantiatedType, owner, parameterMap, args);
	}

	static boolean isClassTemplate(ICPPASTTemplateId id) {
		IASTNode parentOfName = id.getParent();

		if (parentOfName instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) parentOfName).getLastName() != id)
				return true;
			parentOfName = parentOfName.getParent();
		}

		if (parentOfName instanceof ICPPASTElaboratedTypeSpecifier
				|| parentOfName instanceof ICPPASTCompositeTypeSpecifier
				|| parentOfName instanceof ICPPASTNamedTypeSpecifier || parentOfName instanceof ICPPASTBaseSpecifier) {
			return true;
		}

		if (parentOfName instanceof IASTDeclarator) {
			IASTDeclarator rel = ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) parentOfName);
			return !(rel instanceof IASTFunctionDeclarator);
		}
		return false;
	}

	public static ICPPTemplateInstance createInstance(IBinding owner, ICPPTemplateDefinition template,
			ICPPTemplateParameterMap tpMap, ICPPTemplateArgument[] args) {
		if (owner instanceof ICPPSpecialization) {
			ICPPTemplateParameterMap map = ((ICPPSpecialization) owner).getTemplateParameterMap();
			if (map != null) {
				((CPPTemplateParameterMap) tpMap).putAll(map);
			}
		}

		ICPPTemplateInstance instance = null;
		if (template instanceof ICPPClassType) {
			instance = new CPPClassInstance((ICPPClassType) template, owner, tpMap, args);
		} else if (template instanceof ICPPFunction) {
			ICPPFunction func = (ICPPFunction) template;
			InstantiationContext context = createInstantiationContext(tpMap, owner);
			ICPPFunctionType type = (ICPPFunctionType) instantiateType(func.getType(), context);
			IType[] exceptionSpecs = instantiateTypes(func.getExceptionSpecification(), context);
			CPPFunctionSpecialization spec;
			if (owner instanceof ICPPClassType && template instanceof ICPPMethod) {
				if (template instanceof ICPPConstructor) {
					spec = new CPPConstructorInstance((ICPPConstructor) template, (ICPPClassType) owner,
							context.getParameterMap(), args, type, exceptionSpecs);
				} else {
					spec = new CPPMethodInstance((ICPPMethod) template, (ICPPClassType) owner,
							context.getParameterMap(), args, type, exceptionSpecs);
				}
			} else {
				spec = new CPPFunctionInstance((ICPPFunction) template, owner, tpMap, args, type, exceptionSpecs);
			}
			spec.setParameters(
					specializeParameters(func.getParameters(), spec, context, IntegralValue.MAX_RECURSION_DEPTH));
			instance = (ICPPTemplateInstance) spec;
		} else if (template instanceof ICPPVariable) {
			ICPPVariable var = (ICPPVariable) template;
			InstantiationContext context = createInstantiationContext(tpMap, owner);
			IType type = instantiateType(var.getType(), context);

			IValue value;
			IASTNode point = CPPSemantics.getCurrentLookupPoint();
			ICPPASTDeclarator decl = ASTQueries.findAncestorWithType(point, ICPPASTDeclarator.class);
			if (point instanceof IASTName && ((IASTName) point).getRoleOfName(false) == IASTNameOwner.r_definition
					&& decl != null && decl.getInitializer() != null) {
				// Explicit specialization.
				value = SemanticUtil.getValueOfInitializer(decl.getInitializer(), type);
			} else {
				value = instantiateValue(var.getInitialValue(), context, IntegralValue.MAX_RECURSION_DEPTH);
			}

			if (template instanceof ICPPField) {
				instance = new CPPFieldInstance(template, owner, tpMap, args, type, value);
			} else {
				instance = new CPPVariableInstance(template, owner, tpMap, args, type, value);
			}
		}
		return instance;
	}

	public static ICPPParameter[] specializeParameters(ICPPParameter[] parameters, ICPPFunction functionSpec,
			InstantiationContext context, int maxdepth) {
		if (parameters.length == 0) {
			return parameters;
		}

		// Because of parameter packs there can be more or less parameters in the specialization
		IType[] specializedParameterTypes = functionSpec.getType().getParameterTypes();
		final int length = specializedParameterTypes.length;
		ICPPParameter par = null;
		ICPPParameter[] result = new ICPPParameter[length];
		for (int i = 0; i < length; i++) {
			if (i < parameters.length) {
				par = parameters[i];
			} // else reuse last parameter (which should be a pack)
			@SuppressWarnings("null")
			IValue defaultValue = par.getDefaultValue();
			IValue specializedValue = instantiateValue(defaultValue, context, maxdepth);
			result[i] = new CPPParameterSpecialization(par, functionSpec, specializedParameterTypes[i],
					specializedValue, context.getParameterMap());
		}
		return result;
	}

	// TODO(nathanridge): Handle this as a case in createSpecialization()
	public static ICPPVariable createVariableSpecialization(InstantiationContext context, ICPPVariable variable) {
		final IType type = variable.getType();
		final IType newType = instantiateType(type, context);
		final IValue value = variable.getInitialValue();
		final IValue newValue = instantiateValue(value, context, IntegralValue.MAX_RECURSION_DEPTH);
		if (type == newType && value == newValue) {
			return variable;
		}
		ICPPVariable newVariable = new CPPVariableSpecialization(variable, context.getContextSpecialization(),
				context.getParameterMap(), newType, newValue);
		context.putInstantiatedLocal(variable, newVariable);
		return newVariable;
	}

	/**
	 * IMPORTANT: Do NOT call this method directly, at least when (owner instanceof ICPPClassSpecialization).
	 *            Use ICPPClassSpecialization.specializeMember(decl) instead.
	 *
	 *            This ensures that the caching mechanism for member specializations implemented by
	 *            ICPPClassSpecialization.specializeMember() is not bypassed.
	 *
	 * TODO: Implement a caching mechanism for non-class owners, too, and make specializeMember()
	 *       a method of ICPPSpecialization itself.
	 */
	public static IBinding createSpecialization(ICPPSpecialization owner, IBinding decl) {
		IBinding spec = null;
		final ICPPTemplateParameterMap tpMap = owner.getTemplateParameterMap();
		final ICPPClassSpecialization classOwner = (owner instanceof ICPPClassSpecialization)
				? (ICPPClassSpecialization) owner
				: null;

		// Guard against infinite recursion during template instantiation with a depth limit.
		int instantiationDepth = fTemplateInstantiationDepth.get();
		if (instantiationDepth > TEMPLATE_INSTANTIATION_DEPTH_LIMIT) {
			return RecursionResolvingBinding.createFor(decl);
		}
		// Increment the instantiation depth for the duration of this call.
		fTemplateInstantiationDepth.set(instantiationDepth + 1);

		try {
			if (decl instanceof ICPPClassTemplatePartialSpecialization && classOwner != null) {
				try {
					ICPPClassTemplatePartialSpecialization pspec = (ICPPClassTemplatePartialSpecialization) decl;
					ICPPClassTemplate template = pspec.getPrimaryClassTemplate();
					ICPPTemplateArgument[] args = pspec.getTemplateArguments();

					template = (ICPPClassTemplate) classOwner.specializeMember(template);
					InstantiationContext context = createInstantiationContext(tpMap, owner);
					args = instantiateArguments(args, context, false);
					spec = new CPPClassTemplatePartialSpecializationSpecialization(pspec, tpMap, template, args);
				} catch (DOMException e) {
				}
			} else if (decl instanceof ICPPClassTemplate && classOwner != null) {
				ICPPClassTemplate template = (ICPPClassTemplate) decl;
				CPPClassTemplateSpecialization classTemplateSpec = new CPPClassTemplateSpecialization(template,
						classOwner, tpMap);
				classTemplateSpec.setTemplateParameters(specializeTemplateParameters(classTemplateSpec,
						(ICPPScope) classTemplateSpec.getScope(), template.getTemplateParameters(), classOwner));
				spec = classTemplateSpec;
			} else if (decl instanceof ICPPClassType && classOwner != null) {
				// TODO: Handle local classes
				IBinding oldOwner = decl.getOwner();
				if (oldOwner instanceof IType && classOwner.getSpecializedBinding().isSameType((IType) oldOwner)) {
					spec = new CPPClassSpecialization((ICPPClassType) decl, owner, tpMap);
				} else {
					spec = new CPPClassSpecialization((ICPPClassType) decl, oldOwner, tpMap);
				}
			} else if (decl instanceof ICPPField && classOwner != null) {
				ICPPField field = (ICPPField) decl;
				InstantiationContext context = createInstantiationContext(tpMap, owner);
				IType type = instantiateType(field.getType(), context);
				IValue value = instantiateValue(field.getInitialValue(), context, IntegralValue.MAX_RECURSION_DEPTH);
				if (decl instanceof ICPPFieldTemplate) {
					CPPFieldTemplateSpecialization fieldTempSpec = new CPPFieldTemplateSpecialization(decl, classOwner,
							tpMap, type, value);
					ICPPTemplateParameter[] params = specializeTemplateParameters(fieldTempSpec,
							(ICPPScope) fieldTempSpec.getScope(), ((ICPPFieldTemplate) decl).getTemplateParameters(),
							classOwner);
					fieldTempSpec.setTemplateParameters(params);
					spec = fieldTempSpec;
				} else {
					spec = new CPPFieldSpecialization(decl, classOwner, tpMap, type, value);
				}
			} else if (decl instanceof ICPPFunction) {
				ICPPFunction func = (ICPPFunction) decl;
				InstantiationContext context = createInstantiationContext(tpMap, owner);
				ICPPFunctionType type = (ICPPFunctionType) instantiateType(func.getType(), context);
				IType[] exceptionSpecs = instantiateTypes(func.getExceptionSpecification(), context);

				CPPFunctionSpecialization functionSpec = null;
				if (decl instanceof ICPPFunctionTemplate) {
					if (decl instanceof ICPPMethod && classOwner != null) {
						CPPMethodTemplateSpecialization methodSpec;
						if (decl instanceof ICPPConstructor) {
							methodSpec = new CPPConstructorTemplateSpecialization((ICPPConstructor) decl, classOwner,
									tpMap, type, exceptionSpecs);
						} else {
							methodSpec = new CPPMethodTemplateSpecialization((ICPPMethod) decl, classOwner, tpMap, type,
									exceptionSpecs);
						}
						methodSpec.setTemplateParameters(
								specializeTemplateParameters(methodSpec, (ICPPScope) methodSpec.getScope(),
										((ICPPFunctionTemplate) decl).getTemplateParameters(), classOwner));
						functionSpec = methodSpec;
					} else {
						IBinding oldOwner = decl.getOwner();
						functionSpec = new CPPFunctionTemplateSpecialization((ICPPFunctionTemplate) decl, oldOwner,
								tpMap, type, exceptionSpecs);
					}
				} else if (decl instanceof ICPPConstructor && classOwner != null) {
					functionSpec = new CPPConstructorSpecialization((ICPPConstructor) decl, classOwner, tpMap, type,
							exceptionSpecs);
				} else if (decl instanceof ICPPMethod && classOwner != null) {
					functionSpec = new CPPMethodSpecialization((ICPPMethod) decl, classOwner, tpMap, type,
							exceptionSpecs);
				} else if (decl instanceof ICPPFunction) {
					if (type.isSameType(func.getType())) {
						// There is no need to create a CPPFunctionSpecialization object since the function is
						// a friend function with the type that is not affected by the specialization.
						// See http://bugs.eclipse.org/513681
						spec = func;
					} else {
						IBinding oldOwner = decl.getOwner();
						functionSpec = new CPPFunctionSpecialization(func, oldOwner, tpMap, type, exceptionSpecs);
					}
				}
				if (functionSpec != null) {
					functionSpec.setParameters(specializeParameters(func.getParameters(), functionSpec, context,
							IntegralValue.MAX_RECURSION_DEPTH));
					spec = functionSpec;
				}
			} else if (decl instanceof ITypedef) {
				InstantiationContext context = createInstantiationContext(tpMap, owner);
				IType type = instantiateType(((ITypedef) decl).getType(), context);
				spec = new CPPTypedefSpecialization(decl, owner, tpMap, type);
			} else if (decl instanceof ICPPAliasTemplate) {
				ICPPAliasTemplate aliasTemplate = (ICPPAliasTemplate) decl;
				InstantiationContext context = createInstantiationContext(tpMap, owner);
				IType type = instantiateType(aliasTemplate.getType(), context);
				CPPAliasTemplateSpecialization aliasSpec = new CPPAliasTemplateSpecialization(aliasTemplate, owner,
						tpMap, type);
				aliasSpec.setTemplateParameters(specializeTemplateParameters(aliasSpec,
						(ICPPScope) aliasSpec.getScope(), aliasTemplate.getTemplateParameters(), classOwner));
				spec = aliasSpec;
			} else if (decl instanceof ICPPEnumeration && classOwner != null) {
				// TODO: Handle local enumerations
				spec = CPPEnumerationSpecialization.createInstance((ICPPEnumeration) decl, classOwner, tpMap);
			} else if (decl instanceof IEnumerator && classOwner != null) {
				IEnumerator enumerator = (IEnumerator) decl;
				ICPPEnumeration enumeration = (ICPPEnumeration) enumerator.getOwner();
				IBinding enumSpec = classOwner.specializeMember(enumeration);
				if (enumSpec instanceof ICPPEnumerationSpecialization) {
					spec = ((ICPPEnumerationSpecialization) enumSpec).specializeEnumerator(enumerator);
				}
			} else if (decl instanceof ICPPUsingDeclaration) {
				IBinding[] delegates = ((ICPPUsingDeclaration) decl).getDelegates();
				List<IBinding> result = new ArrayList<>();
				InstantiationContext context = createInstantiationContext(tpMap, owner);
				for (IBinding delegate : delegates) {
					try {
						if (delegate instanceof ICPPUnknownBinding) {
							delegate = resolveUnknown((ICPPUnknownBinding) delegate, context);
						}
						if (delegate instanceof CPPFunctionSet) {
							for (IBinding b : ((CPPFunctionSet) delegate).getBindings()) {
								result.add(b);
							}
						} else if (delegate != null) {
							result.add(delegate);
						}
					} catch (DOMException e) {
					}
				}
				delegates = result.toArray(new IBinding[result.size()]);
				spec = new CPPUsingDeclarationSpecialization((ICPPUsingDeclaration) decl, owner, tpMap, delegates);
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		} finally {
			// Restore original instantiation depth.
			fTemplateInstantiationDepth.set(instantiationDepth);
		}
		return spec;
	}

	private static InstantiationContext createInstantiationContext(ICPPTemplateParameterMap tpMap, IBinding owner) {
		return new InstantiationContext(tpMap, getSpecializationContext(owner));
	}

	public static ICPPClassSpecialization getSpecializationContext(IBinding owner) {
		ICPPClassSpecialization within = getContextClassSpecialization(owner);
		if (within == null)
			return null;
		ICPPClassType orig = within.getSpecializedBinding();
		while (true) {
			IBinding o1 = within.getOwner();
			IBinding o2 = orig.getOwner();
			if (!(o1 instanceof ICPPClassSpecialization && o2 instanceof ICPPClassType))
				return within;
			ICPPClassSpecialization nextWithin = (ICPPClassSpecialization) o1;
			orig = (ICPPClassType) o2;
			if (orig.isSameType(nextWithin))
				return within;
			within = nextWithin;
		}
	}

	public static IValue instantiateValue(IValue value, InstantiationContext context, int maxDepth) {
		if (value == null)
			return null;
		ICPPEvaluation evaluation = value.getEvaluation();
		if (evaluation == null)
			return value;
		ICPPEvaluation instantiated = evaluation.instantiate(context, maxDepth);
		if (instantiated == evaluation)
			return value;
		return instantiated.getValue();
	}

	public static boolean containsParameterPack(IType type) {
		return determinePackSize(type, CPPTemplateParameterMap.EMPTY) == PACK_SIZE_DEFER;
	}

	static int determinePackSize(IType type, ICPPTemplateParameterMap tpMap) {
		if (type instanceof ICPPFunctionType) {
			final ICPPFunctionType ft = (ICPPFunctionType) type;
			final IType rt = ft.getReturnType();
			int r = determinePackSize(rt, tpMap);
			if (r < 0)
				return r;
			IType[] ps = ft.getParameterTypes();
			for (IType pt : ps) {
				r = combinePackSize(r, determinePackSize(pt, tpMap));
				if (r < 0)
					return r;
			}
			return r;
		}

		if (type instanceof TypeOfDependentExpression) {
			return ((TypeOfDependentExpression) type).getEvaluation().determinePackSize(tpMap);
		}

		if (type instanceof ICPPUnknownBinding) {
			return determinePackSize((ICPPUnknownBinding) type, tpMap);
		}

		if (type instanceof ICPPParameterPackType)
			return PACK_SIZE_NOT_FOUND;

		int r = PACK_SIZE_NOT_FOUND;
		if (type instanceof IArrayType) {
			IArrayType at = (IArrayType) type;
			IValue asize = at.getSize();
			r = determinePackSize(asize, tpMap);
			if (r < 0)
				return r;
		}

		if (type instanceof ITypeContainer) {
			if (type instanceof ICPPAliasTemplateInstance) {
				ICPPTemplateArgument[] args = ((ICPPAliasTemplateInstance) type).getTemplateArguments();
				for (ICPPTemplateArgument arg : args) {
					r = combinePackSize(r, determinePackSize(arg, tpMap));
					if (r < 0) {
						return r;
					}
				}
			}
			final ITypeContainer typeContainer = (ITypeContainer) type;
			r = combinePackSize(r, determinePackSize(typeContainer.getType(), tpMap));
		}
		return r;
	}

	static int determinePackSize(ICPPUnknownBinding binding, ICPPTemplateParameterMap tpMap) {
		if (binding instanceof ICPPTemplateParameter) {
			ICPPTemplateParameter tpar = (ICPPTemplateParameter) binding;
			if (tpar.isParameterPack()) {
				ICPPTemplateArgument[] args = tpMap.getPackExpansion(tpar);
				if (args != null) {
					return args.length;
				}
				return PACK_SIZE_DEFER;
			}
			return PACK_SIZE_NOT_FOUND;
		}
		int r = PACK_SIZE_NOT_FOUND;
		if (binding instanceof ICPPDeferredClassInstance) {
			ICPPDeferredClassInstance dcl = (ICPPDeferredClassInstance) binding;
			if (dcl.getClassTemplate() instanceof ICPPTemplateTemplateParameter) {
				r = combinePackSize(r, determinePackSize((ICPPUnknownBinding) dcl.getClassTemplate(), tpMap));
			}
			ICPPTemplateArgument[] args = dcl.getTemplateArguments();
			for (ICPPTemplateArgument arg : args) {
				r = combinePackSize(r, determinePackSize(arg, tpMap));
				if (r < 0)
					return r;
			}
		}
		IBinding ownerBinding = binding.getOwner();
		if (ownerBinding instanceof IType)
			r = combinePackSize(r, determinePackSize((IType) ownerBinding, tpMap));

		return r;
	}

	static int determinePackSize(IValue value, ICPPTemplateParameterMap tpMap) {
		ICPPEvaluation eval = value.getEvaluation();
		if (eval == null)
			return PACK_SIZE_NOT_FOUND;

		return ((CPPEvaluation) eval).determinePackSize(tpMap);
	}

	static int determinePackSize(ICPPTemplateArgument arg, ICPPTemplateParameterMap tpMap) {
		if (arg.isTypeValue())
			return determinePackSize(arg.getTypeValue(), tpMap);
		return determinePackSize(arg.getNonTypeValue(), tpMap);
	}

	static int combinePackSize(int ps1, int ps2) {
		if (ps1 < 0 || ps2 == PACK_SIZE_NOT_FOUND)
			return ps1;
		if (ps2 < 0 || ps1 == PACK_SIZE_NOT_FOUND)
			return ps2;
		if (ps1 != ps2)
			return PACK_SIZE_FAIL;
		return ps1;
	}

	/**
	 * Instantiates types contained in an array.
	 *
	 * @param types an array of types
	 * @param context the template instantiation context
	 * @return an array containing instantiated types.
	 */
	public static IType[] instantiateTypes(IType[] types, InstantiationContext context) {
		if (types == null)
			return null;

		// Don't create a new array until it's really needed.
		IType[] result = types;
		int j = 0;
		for (int i = 0; i < types.length; i++) {
			IType origType = types[i];
			IType newType;
			if (origType instanceof ICPPParameterPackType) {
				IType innerType = ((ICPPParameterPackType) origType).getType();
				int packSize = determinePackSize(innerType, context.getParameterMap());
				if (packSize == PACK_SIZE_FAIL || packSize == PACK_SIZE_NOT_FOUND) {
					newType = new ProblemBinding(CPPSemantics.getCurrentLookupPoint(),
							IProblemBinding.SEMANTIC_INVALID_TYPE,
							types[i] instanceof IBinding ? ((IBinding) types[i]).getNameCharArray() : null);
				} else if (packSize == PACK_SIZE_DEFER) {
					// We're not expanding the pack, but arguments for template parameters of
					// enclosing templates may still need to be substituted into the expansion pattern.
					newType = instantiateType(origType, context);
				} else {
					IType[] newResult = new IType[result.length + packSize - 1];
					System.arraycopy(result, 0, newResult, 0, j);
					result = newResult;
					context.setExpandPack(true);
					int oldPackOffset = context.getPackOffset();
					for (int k = 0; k < packSize; k++) {
						context.setPackOffset(k);
						IType instantiated = instantiateType(innerType, context);
						if (context.isPackExpanded()) {
							if (instantiated != null) {
								instantiated = new CPPParameterPackType(instantiated);
							}
							// TODO: instantiateArguments() calls context.setPackExpanded(false) here
							//       Do we need to do the same here?
						}
						result[j++] = instantiated;

					}
					context.setPackOffset(oldPackOffset);
					context.setExpandPack(false);
					continue;
				}
			} else {
				newType = instantiateType(origType, context);
			}
			if (result != types) {
				result[j++] = newType;
			} else {
				if (newType != origType) {
					result = new IType[types.length];
					System.arraycopy(types, 0, result, 0, i);
					result[j] = newType;
				}
				j++;
			}
		}
		return result;
	}

	/**
	 * Instantiates arguments contained in an array. Instantiated arguments are checked for
	 * validity. If the {@code strict} parameter is {@code true}, the method returns {@code null} if
	 * any of the instantiated arguments are invalid. If the {@code strict} parameter is
	 * {@code false}, any invalid instantiated arguments are replaced by the corresponding original
	 * arguments.
	 */
	public static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args, InstantiationContext context,
			boolean strict) throws DOMException {
		// Don't create a new array until it's really needed.
		ICPPTemplateArgument[] result = args;
		int resultShift = 0;
		for (int i = 0; i < args.length; i++) {
			ICPPTemplateArgument origArg = args[i];
			ICPPTemplateArgument newArg;
			if (origArg.isPackExpansion()) {
				ICPPTemplateArgument pattern = origArg.getExpansionPattern();
				int packSize = determinePackSize(pattern, context.getParameterMap());
				if (packSize == PACK_SIZE_FAIL || packSize == PACK_SIZE_NOT_FOUND) {
					throw new DOMException(new ProblemBinding(CPPSemantics.getCurrentLookupPoint(),
							IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS, null));
				} else if (packSize == PACK_SIZE_DEFER) {
					// We're not expanding the pack, but arguments for template parameters of
					// enclosing templates may still need to be substituted into the expansion pattern.
					newArg = instantiateArgument(origArg, context);
					if (!isValidArgument(newArg)) {
						if (strict)
							return null;
						newArg = origArg;
					}
				} else {
					int shift = packSize - 1;
					ICPPTemplateArgument[] newResult = new ICPPTemplateArgument[args.length + resultShift + shift];
					System.arraycopy(result, 0, newResult, 0, i + resultShift);
					context.setExpandPack(true);
					int oldPackOffset = context.getPackOffset();
					for (int j = 0; j < packSize; j++) {
						context.setPackOffset(j);
						newArg = instantiateArgument(pattern, context);
						if (!isValidArgument(newArg)) {
							if (strict)
								return null;
							result[i + resultShift] = origArg;
							newResult = result;
							shift = 0;
							break;
						}
						if (context.isPackExpanded()) {
							IType type = newArg.getTypeValue();
							if (type != null) {
								type = new CPPParameterPackType(type);
								newArg = new CPPTemplateTypeArgument(type);
							}
							context.setPackExpanded(false);
						}
						newResult[i + resultShift + j] = newArg;
					}
					context.setPackOffset(oldPackOffset);
					context.setExpandPack(false);
					result = newResult;
					resultShift += shift;
					continue;
				}
			} else {
				newArg = instantiateArgument(origArg, context);
				if (!isValidArgument(newArg)) {
					if (strict)
						return null;
					newArg = origArg;
				}
			}

			if (result != args) {
				result[i + resultShift] = newArg;
			} else if (newArg != origArg) {
				assert resultShift == 0;
				result = new ICPPTemplateArgument[args.length];
				if (i > 0) {
					System.arraycopy(args, 0, result, 0, i);
				}
				result[i] = newArg;
			}
		}
		return result;
	}

	/**
	 * Instantiates a template argument.
	 */
	static ICPPTemplateArgument instantiateArgument(ICPPTemplateArgument arg, InstantiationContext context) {
		if (arg == null)
			return null;
		if (arg.isNonTypeValue()) {
			final ICPPEvaluation eval = arg.getNonTypeEvaluation();
			final ICPPEvaluation newEval = eval.instantiate(context, IntegralValue.MAX_RECURSION_DEPTH);
			if (eval == newEval)
				return arg;
			return new CPPTemplateNonTypeArgument(newEval);
		}

		// Which to instantiate, getOriginalTypeValue() or getTypeValue()?
		//
		// Using getOriginalTypeValue() is better for typedef preservation,
		// and in the case of alias template instances, also necessary for
		// correctness (since an alias template instance could have dependent
		// arguments that don't appear in the resulting type, and these
		// arguments could SFINAE out during instantiation; the popular
		// "void_t" technique relies on this).
		//
		// However, caching of template instances is based on the normalized
		// representation of arguments, which uses getTypeValue(). This,
		// together with certain deficiencies in ASTTypeUtil (namely, that
		// template parameters owned by different templates end up with the
		// same string representation), leads to tricky bugs if we try to
		// use getOriginalTypeValue() here all the time (observe, e.g., how
		// IndexCPPTemplateResolutionTest.testRegression_516338 fails if we
		// unconditionally use getOriginalTypeValue() here).
		//
		// As a compromise, we use getOriginalTypeValue() in the case where
		// it's important for correctness (alias template instances), and
		// getTypeValue() otherwise.
		IType type;
		final IType origType = arg.getOriginalTypeValue();
		if (origType instanceof ICPPAliasTemplateInstance) {
			type = origType;
		} else {
			type = arg.getTypeValue();
		}

		final IType inst = instantiateType(type, context);
		if (type == inst)
			return arg;
		return new CPPTemplateTypeArgument(inst);
	}

	private static CPPTemplateParameterMap instantiateArgumentMap(ICPPTemplateParameterMap orig,
			InstantiationContext context) {
		final Integer[] positions = orig.getAllParameterPositions();
		CPPTemplateParameterMap newMap = new CPPTemplateParameterMap(positions.length);
		for (Integer key : positions) {
			ICPPTemplateArgument arg = orig.getArgument(key);
			if (arg != null) {
				ICPPTemplateArgument newArg = instantiateArgument(arg, context);
				if (!isValidArgument(newArg))
					newArg = arg;
				newMap.put(key, newArg);
			} else {
				ICPPTemplateArgument[] args = orig.getPackExpansion(key);
				if (args != null) {
					try {
						newMap.put(key, instantiateArguments(args, context, false));
					} catch (DOMException e) {
						newMap.put(key, args);
					}
				}
			}
		}
		return newMap;
	}

	/**
	 * Instantiates the given type with the provided map and packОffset.
	 * The context is used to replace templates with their specialization, where appropriate.
	 */
	public static IType instantiateType(final IType type, InstantiationContext context) {
		if (context.getParameterMap() == null)
			return type;

		TypeInstantiationRequest instantiationRequest = new TypeInstantiationRequest(type, context);
		if (!instantiationsInProgress.get().add(instantiationRequest)) {
			return type instanceof ICPPFunctionType ? ProblemFunctionType.RECURSION_IN_LOOKUP
					: ProblemType.RECURSION_IN_LOOKUP;
		}

		try {
			if (type instanceof ICPPFunctionType) {
				final ICPPFunctionType ft = (ICPPFunctionType) type;
				IType[] ps = ft.getParameterTypes();
				IType[] params = instantiateTypes(ps, context);
				final IType r = ft.getReturnType();
				IType ret = instantiateType(r, context);
				ICPPEvaluation noex = ft.getNoexceptSpecifier();
				ICPPEvaluation noexcept = noex == null ? null
						: noex.instantiate(context, IntegralValue.MAX_RECURSION_DEPTH);
				if (ret == r && params == ps && noexcept == noex) {
					return type;
				}
				// The parameter types need to be adjusted.
				for (int i = 0; i < params.length; i++) {
					IType p = params[i];
					if (!isDependentType(p)) {
						params[i] = CPPVisitor.adjustParameterType(p, true);
					}
				}

				return new CPPFunctionType(ret, params, noexcept, ft.isConst(), ft.isVolatile(), ft.hasRefQualifier(),
						ft.isRValueReference(), ft.takesVarArgs());
			}

			if (type instanceof ICPPTemplateParameter) {
				return resolveTemplateTypeParameter((ICPPTemplateParameter) type, context);
			}

			if (type instanceof ICPPUnknownBinding) {
				if (type instanceof TypeOfDependentExpression) {
					TypeOfDependentExpression dependentType = (TypeOfDependentExpression) type;
					ICPPEvaluation eval = dependentType.getEvaluation();
					ICPPEvaluation instantiated = eval.instantiate(context, IntegralValue.MAX_RECURSION_DEPTH);
					if (instantiated != eval) {
						if (dependentType.isForDecltype()) {
							return CPPSemantics.getDeclTypeForEvaluation(instantiated);
						} else {
							return instantiated.getType();
						}
					}
				} else if (type instanceof TypeOfUnknownMember) {
					IBinding binding = resolveUnknown(((TypeOfUnknownMember) type).getUnknownMember(), context);
					if (binding instanceof IType) {
						return (IType) binding;
					} else if (binding instanceof IVariable) {
						return ((IVariable) binding).getType();
					} else if (binding instanceof IFunction) {
						return ((IFunction) binding).getType();
					}
					return type;
				} else {
					IBinding binding = resolveUnknown((ICPPUnknownBinding) type, context);
					if (binding instanceof IType)
						return (IType) binding;

					return type;
				}
			}

			if (context.getContextTypeSpecialization() != null && type instanceof IBinding) {
				IType unwound = getNestedType(type, TDEF);
				ICPPClassSpecialization withinClass = context.getContextClassSpecialization();
				if (unwound instanceof ICPPClassType && unwound.isSameType(withinClass.getSpecializedBinding())) {
					// Convert (partial) class-templates (specializations) to the more specialized
					// version.
					if (withinClass instanceof ICPPClassTemplate || !(unwound instanceof ICPPClassTemplate))
						return withinClass;
				}
				IBinding typeAsBinding = (IBinding) type;
				IBinding owner = typeAsBinding.getOwner();
				if (owner instanceof IType) {
					final IType ownerAsType = getNestedType((IType) owner, TDEF);
					Object newOwner = owner;
					if (ownerAsType instanceof ICPPClassType
							&& ownerAsType.isSameType(withinClass.getSpecializedBinding())) {
						// Convert (partial) class-templates (specializations) that are used as
						// owner of another binding, to the more specialized version.
						newOwner = withinClass;
					} else {
						newOwner = instantiateType(ownerAsType, context);
					}

					if (newOwner != owner && newOwner instanceof ICPPClassSpecialization) {
						return (IType) ((ICPPClassSpecialization) newOwner).specializeMember(typeAsBinding);
					}
				}
			}

			// An alias template instance may have dependent arguments that don't contribute
			// to the target type but can SFINAE out during instantiation, so it's not
			// sufficient to handle it in the ITypeContainer case.
			if (type instanceof ICPPAliasTemplateInstance) {
				// Cache instantiations of alias templates. This is necessary because below we can
				// potentially instantiate types that appear in the arguments of an alias template
				// instance up to three times (once in instantiateArguments(), once in
				// instantiateArgumentMap(), and if the argument appears in the aliased type, then
				// a third time in instantiateType()), leading to exponential runtime in cases of
				// nested alias template instances (which can be common in metaprogramming code
				// implemented using alias templates).
				IType result = getCachedInstantiation(instantiationRequest);
				if (result != null) {
					return result;
				}
				ICPPAliasTemplateInstance instance = (ICPPAliasTemplateInstance) type;
				ICPPAliasTemplate template = instance.getTemplateDefinition();
				ICPPTemplateArgument[] args = instance.getTemplateArguments();
				ICPPTemplateArgument[] newArgs = instantiateArguments(args, context, true);
				if (newArgs == null) {
					result = (IType) createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
				} else if (args != newArgs) {
					IType target = instantiateType(instance.getType(), context);
					CPPTemplateParameterMap map = createParameterMap(template, newArgs);
					if (map == null) {
						result = (IType) createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
					} else {
						result = new CPPAliasTemplateInstance(template, target, instance.getOwner(), map, newArgs);
					}
				} else {
					result = type;
				}
				putCachedInstantiation(instantiationRequest, result);
				return result;
			}

			if (type instanceof ITypeContainer) {
				final ITypeContainer typeContainer = (ITypeContainer) type;
				IType nestedType = typeContainer.getType();
				IType newNestedType = instantiateType(nestedType, context);
				if (typeContainer instanceof ICPPPointerToMemberType) {
					ICPPPointerToMemberType ptm = (ICPPPointerToMemberType) typeContainer;
					IType memberOfClass = ptm.getMemberOfClass();
					IType newMemberOfClass = instantiateType(memberOfClass, context);
					IType classType = SemanticUtil.getNestedType(newMemberOfClass, CVTYPE | TDEF);
					if (!(classType instanceof ICPPClassType || classType instanceof UniqueType
							|| classType instanceof ICPPUnknownBinding)) {
						return new ProblemType(ISemanticProblem.BINDING_INVALID_TYPE);
					}
					if (newNestedType != nestedType || newMemberOfClass != memberOfClass) {
						return new CPPPointerToMemberType(newNestedType, newMemberOfClass, ptm.isConst(),
								ptm.isVolatile(), ptm.isRestrict());
					}
					return typeContainer;
				}
				if (typeContainer instanceof IArrayType) {
					IArrayType at = (IArrayType) typeContainer;
					IValue asize = at.getSize();
					if (asize != null) {
						IValue newSize = instantiateValue(asize, context, IntegralValue.MAX_RECURSION_DEPTH);
						if (newSize != asize) {
							return new CPPArrayType(newNestedType, newSize);
						}
					}
				}
				if (newNestedType != nestedType) {
					return SemanticUtil.replaceNestedType(typeContainer, newNestedType);
				}
				return typeContainer;
			}

			if (type instanceof ICPPUnaryTypeTransformation) {
				ICPPUnaryTypeTransformation typeTransformation = (ICPPUnaryTypeTransformation) type;
				IType operand = instantiateType(typeTransformation.getOperand(), context);
				switch (typeTransformation.getOperator()) {
				case underlying_type:
					return TypeTraits.underlyingType(operand);
				default:
					return null; // shouldn't happen
				}
			}

			if (type instanceof CPPClosureType) {
				return ((CPPClosureType) type).instantiate(context);
			}

			return type;
		} catch (DOMException e) {
			return e.getProblem();
		} finally {
			instantiationsInProgress.get().remove(instantiationRequest);
		}
	}

	/**
	 * Specialize a template parameter of a nested template by substituting values for the template
	 * parameters of enclosing templates into the template parameter's default value and, in the case
	 * of a non-type template parameter, type.
	 *
	 * @param owner the specialization of the nested template. This will be the owner of the
	 *              specialized template parameter.
	 * @param scope the scope of the nested template specialization
	 * @param specialized the template parameter to be specialized
	 * @param within the specialization of the enclosing class
	 * @return the specialized template parameter
	 */
	public static ICPPTemplateParameter specializeTemplateParameter(ICPPSpecialization owner, ICPPScope scope,
			ICPPTemplateParameter specialized, ICPPClassSpecialization within) {
		if (specialized == null)
			return null;
		ICPPTemplateParameterMap tpMap = owner.getTemplateParameterMap();
		InstantiationContext context = new InstantiationContext(tpMap, 0, within);
		ICPPTemplateArgument defaultValue = instantiateArgument(specialized.getDefaultValue(), context);
		if (specialized instanceof ICPPTemplateNonTypeParameter) {
			ICPPTemplateNonTypeParameter spec = (ICPPTemplateNonTypeParameter) specialized;
			IType type = instantiateType(spec.getType(), context);
			return new CPPTemplateNonTypeParameterSpecialization(owner, scope, spec, defaultValue, type);
		} else if (specialized instanceof ICPPTemplateTypeParameter) {
			return new CPPTemplateTypeParameterSpecialization(owner, scope, (ICPPTemplateTypeParameter) specialized,
					defaultValue);
		} else if (specialized instanceof ICPPTemplateTemplateParameter) {
			return new CPPTemplateTemplateParameterSpecialization(owner, scope,
					(ICPPTemplateTemplateParameter) specialized, defaultValue);
		}
		return null;
	}

	/**
	 * Convenience method for specializing all template parameters in an array.
	 * See specializeTemplateParameter().
	 */
	public static ICPPTemplateParameter[] specializeTemplateParameters(ICPPSpecialization owner, ICPPScope scope,
			ICPPTemplateParameter[] specialized, ICPPClassSpecialization within) {
		ICPPTemplateParameter[] result = new ICPPTemplateParameter[specialized.length];
		for (int i = 0; i < specialized.length; ++i)
			result[i] = specializeTemplateParameter(owner, scope, specialized[i], within);
		return result;
	}

	public static IBinding instantiateBinding(IBinding binding, InstantiationContext context, int maxDepth)
			throws DOMException {
		if (binding instanceof ICPPClassTemplate) {
			binding = createDeferredInstance((ICPPClassTemplate) binding);
		}

		if (binding instanceof ICPPUnknownBinding) {
			return resolveUnknown((ICPPUnknownBinding) binding, context);
		} else if (binding instanceof ICPPMethod || binding instanceof ICPPField || binding instanceof ICPPEnumeration
				|| binding instanceof ICPPClassType) {
			IBinding owner = binding.getOwner();
			if (!(owner instanceof ICPPSpecialization)) {
				owner = instantiateBinding(owner, context, maxDepth);
			}
			if (owner instanceof ICPPClassSpecialization) {
				return ((ICPPClassSpecialization) owner).specializeMember(binding);
			}
		} else if (binding instanceof IEnumerator) {
			IBinding owner = binding.getOwner();
			ICPPTypeSpecialization within = context.getContextTypeSpecialization();
			if (within instanceof ICPPEnumerationSpecialization && within.getSpecializedBinding().equals(owner)) {
				owner = within;
			} else if (!(owner instanceof ICPPSpecialization)) {
				owner = instantiateBinding(owner, context, maxDepth);
			}
			if (owner instanceof ICPPEnumerationSpecialization) {
				return ((ICPPEnumerationSpecialization) owner).specializeEnumerator((IEnumerator) binding);
			}
		} else if (binding instanceof ICPPFunctionInstance) {
			// TODO(nathanridge):
			//   Maybe we should introduce an ICPPDeferredFunctionInstance and have things that can
			//   return a dependent ICPPFunctionInstance (like instantiateForAddressOfFunction)
			//   return that when appropriate?
			ICPPFunctionInstance origInstance = (ICPPFunctionInstance) binding;
			ICPPTemplateArgument[] origArgs = origInstance.getTemplateArguments();
			ICPPTemplateArgument[] newArgs = instantiateArguments(origArgs, context, false);
			if (origArgs != newArgs) {
				CPPTemplateParameterMap newMap = instantiateArgumentMap(origInstance.getTemplateParameterMap(),
						context);
				IType newType = instantiateType(origInstance.getType(), context);
				IType[] newExceptionSpecs = instantiateTypes(origInstance.getExceptionSpecification(), context);
				CPPFunctionInstance result = new CPPFunctionInstance(
						(ICPPFunction) origInstance.getTemplateDefinition(), origInstance.getOwner(), newMap, newArgs,
						(ICPPFunctionType) newType, newExceptionSpecs);
				result.setParameters(specializeParameters(origInstance.getParameters(), result, context, maxDepth));
				return result;
			}
		} else if (binding instanceof ICPPVariableInstance) {
			// TODO(nathanridge):
			//   Similar to the ICPPFunctionInstance case above, perhaps we should have an
			//   ICPPDeferredVariableInstance.
			ICPPVariableInstance origInstance = (ICPPVariableInstance) binding;
			ICPPTemplateArgument[] origArgs = origInstance.getTemplateArguments();
			ICPPTemplateArgument[] newArgs = instantiateArguments(origArgs, context, false);
			if (origArgs != newArgs) {
				CPPTemplateParameterMap newMap = instantiateArgumentMap(origInstance.getTemplateParameterMap(),
						context);
				IType newType = instantiateType(origInstance.getType(), context);
				IValue newValue = instantiateValue(origInstance.getInitialValue(), context,
						IntegralValue.MAX_RECURSION_DEPTH);
				return new CPPVariableInstance(origInstance.getTemplateDefinition(), origInstance.getOwner(), newMap,
						newArgs, newType, newValue);
			}
		}
		return binding;
	}

	public static IType resolveTemplateTypeParameter(final ICPPTemplateParameter tpar, InstantiationContext context) {
		ICPPTemplateArgument arg = null;
		if (tpar.isParameterPack()) {
			if (context.hasPackOffset()) {
				ICPPTemplateArgument[] args = context.getPackExpansion(tpar);
				if (args != null) {
					if (context.getPackOffset() >= args.length) {
						return new ProblemBinding(CPPSemantics.getCurrentLookupPoint(),
								IProblemBinding.SEMANTIC_INVALID_TYPE, tpar.getNameCharArray());
					}
					arg = args[context.getPackOffset()];
				}
				if (context.shouldExpandPack()) {
					if (arg != null) {
						IType type = arg.getTypeValue();
						if (type instanceof ICPPParameterPackType) {
							arg = new CPPTemplateTypeArgument(((ICPPParameterPackType) type).getType());
							context.setPackExpanded(true);
						}
					}
				}
			}
		} else {
			arg = context.getArgument(tpar);
		}

		if (arg != null) {
			IType t = arg.getOriginalTypeValue();
			if (t != null)
				return t;
		}
		return (IType) tpar;
	}

	/**
	 * Checks whether a given name corresponds to a template declaration and returns the AST node
	 * for it. This works for the name of a template-definition and also for a name needed to
	 * qualify a member definition:
	 * <pre>
	 * template &lt;typename T&gt; void MyTemplate&ltT&gt;::member() {}
	 * </pre>
	 * @param name a name for which the corresponding template declaration is searched for.
	 * @return the template declaration or {@code null} if {@code name} does not correspond
	 *     to a template declaration.
	 */
	public static ICPPASTTemplateDeclaration getTemplateDeclaration(IASTName name) {
		if (name == null)
			return null;

		// first look for a related sequence of template declarations
		ICPPASTInternalTemplateDeclaration tdecl = getInnerTemplateDeclaration(name);
		if (tdecl == null)
			return null;

		name = name.getLastName();
		IASTNode parent = name.getParent();
		if (!(parent instanceof ICPPASTQualifiedName)) {
			if (parent instanceof ICPPASTTemplateId) {
				return null;
			}
			// One name: use innermost template declaration
			return tdecl;
		}

		// last name can be associated even if it is not a template-id
		final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) parent;
		final IASTName lastName = qname.getLastName();
		final boolean lastIsTemplate = tdecl.isAssociatedWithLastName();
		if (name == lastName) {
			if (lastIsTemplate) {
				return tdecl;
			}
			return null;
		}

		// Not the last name, search for the matching template declaration
		if (!(name instanceof ICPPASTTemplateId))
			return null;

		if (lastIsTemplate) {
			// skip one
			tdecl = getDirectlyEnclosingTemplateDeclaration(tdecl);
		}
		final ICPPASTNameSpecifier[] qualifier = qname.getQualifier();
		for (int i = qualifier.length - 1; tdecl != null && i >= 0; i--) {
			final ICPPASTNameSpecifier n = qualifier[i];
			if (n == name) {
				return tdecl;
			}
			if (n instanceof ICPPASTTemplateId) {
				tdecl = getDirectlyEnclosingTemplateDeclaration(tdecl);
			}
		}
		// not enough template declarations
		return null;
	}

	public static void associateTemplateDeclarations(ICPPASTInternalTemplateDeclaration tdecl) {
		// Find innermost template declaration
		IASTDeclaration decl = tdecl.getDeclaration();
		while (decl instanceof ICPPASTInternalTemplateDeclaration) {
			tdecl = (ICPPASTInternalTemplateDeclaration) decl;
			decl = tdecl.getDeclaration();
		}
		final ICPPASTInternalTemplateDeclaration innerMostTDecl = tdecl;

		// Find name declared within the template declaration
		final IASTName declName = getNameForDeclarationInTemplateDeclaration(decl);

		// Count non-empty template declarations
		int instDeclCount = 0;
		int tdeclCount = 0;
		IASTNode node = tdecl;
		while (node instanceof ICPPASTInternalTemplateDeclaration) {
			tdecl = (ICPPASTInternalTemplateDeclaration) node;
			node = node.getParent();
			if (tdecl.getTemplateParameters().length == 0) {
				instDeclCount++;
			} else {
				instDeclCount = 0;
			}
			tdeclCount++;
		}
		final ICPPASTInternalTemplateDeclaration outerMostTDecl = tdecl;
		final int paramTDeclCount = tdeclCount - instDeclCount;

		// Determine association of names with template declarations
		boolean lastIsTemplate = true;
		int nestingLevel;
		if (declName instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qname = (ICPPASTQualifiedName) declName;

			// Count dependent-ids
			CharArraySet tparnames = collectTemplateParameterNames(outerMostTDecl);
			int depIDCount = 0;
			IBinding owner = null;
			final ICPPASTNameSpecifier[] qualifier = qname.getQualifier();
			for (int i = 0; i < qualifier.length; i++) {
				ICPPASTNameSpecifier n = qualifier[i];
				if (n instanceof ICPPASTTemplateId) {
					if (depIDCount > 0 || usesTemplateParameter((ICPPASTTemplateId) n, tparnames)) {
						depIDCount++;
					}
				}
				if (depIDCount == 0) {
					owner = n.resolveBinding();
				}
			}

			if (qname.getLastName() instanceof ICPPASTTemplateId || paramTDeclCount > depIDCount // not enough template ids
					|| qualifier.length < 1 // ::name
			) {
				lastIsTemplate = true;
				depIDCount++;
			} else {
				lastIsTemplate = false;
			}

			nestingLevel = 0;
			if (owner != null) {
				int consumesTDecl = 0;
				IBinding b = owner;
				if (b instanceof IType) {
					IType t = SemanticUtil.getNestedType((IType) b, TDEF);
					if (t instanceof IBinding)
						b = (IBinding) t;
				}
				while (b != null) {
					if (b instanceof ICPPTemplateInstance) {
						nestingLevel++;
						if (!((ICPPTemplateInstance) b).isExplicitSpecialization())
							consumesTDecl++;
					} else if (b instanceof ICPPClassTemplate || b instanceof ICPPClassTemplatePartialSpecialization) {
						nestingLevel++;
						consumesTDecl++;
					}
					b = b.getOwner();
				}
				if (depIDCount > 0) {
					nestingLevel += depIDCount;
				} else if (consumesTDecl < tdeclCount && !lastIsTemplate) {
					nestingLevel++;
					lastIsTemplate = true;
				}
			} else {
				nestingLevel += depIDCount;
				node = outerMostTDecl.getParent();
				while (node != null) {
					if (node instanceof ICPPASTInternalTemplateDeclaration) {
						nestingLevel += ((ICPPASTInternalTemplateDeclaration) node).getNestingLevel() + 1;
						break;
					}
					node = node.getParent();
				}
			}
		} else {
			nestingLevel = 1;
			lastIsTemplate = true;
			if (!isFriendFunctionDeclaration(innerMostTDecl.getDeclaration())) {
				node = outerMostTDecl.getParent();
				while (node != null) {
					if (node instanceof ICPPASTInternalTemplateDeclaration) {
						nestingLevel += ((ICPPASTInternalTemplateDeclaration) node).getNestingLevel() + 1;
						break;
					}
					node = node.getParent();
				}
			}
		}

		node = innerMostTDecl;
		while (node instanceof ICPPASTInternalTemplateDeclaration) {
			if (--nestingLevel < 0)
				nestingLevel = 0;
			tdecl = (ICPPASTInternalTemplateDeclaration) node;
			tdecl.setNestingLevel((short) nestingLevel);
			tdecl.setAssociatedWithLastName(false);
			node = tdecl.getParent();
		}
		innerMostTDecl.setAssociatedWithLastName(lastIsTemplate);
	}

	private static boolean isFriendFunctionDeclaration(IASTDeclaration declaration) {
		while (declaration instanceof ICPPASTTemplateDeclaration) {
			declaration = ((ICPPASTTemplateDeclaration) declaration).getDeclaration();
		}
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) declaration;
			ICPPASTDeclSpecifier declspec = (ICPPASTDeclSpecifier) sdecl.getDeclSpecifier();
			if (declspec.isFriend()) {
				IASTDeclarator[] dtors = sdecl.getDeclarators();
				if (dtors.length == 1
						&& ASTQueries.findTypeRelevantDeclarator(dtors[0]) instanceof IASTFunctionDeclarator) {
					return true;
				}
			}
		}
		return false;
	}

	private static CharArraySet collectTemplateParameterNames(ICPPASTTemplateDeclaration tdecl) {
		CharArraySet set = new CharArraySet(4);
		while (true) {
			ICPPASTTemplateParameter[] pars = tdecl.getTemplateParameters();
			for (ICPPASTTemplateParameter par : pars) {
				IASTName name = getTemplateParameterName(par);
				if (name != null)
					set.put(name.getLookupKey());
			}
			final IASTNode next = tdecl.getDeclaration();
			if (next instanceof ICPPASTTemplateDeclaration) {
				tdecl = (ICPPASTTemplateDeclaration) next;
			} else {
				break;
			}
		}
		return set;
	}

	private static boolean usesTemplateParameter(final ICPPASTTemplateId id, final CharArraySet names) {
		final boolean[] result = { false };
		ASTVisitor v = new ASTVisitor(false) {
			{
				shouldVisitNames = true;
				shouldVisitAmbiguousNodes = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name instanceof ICPPASTTemplateId)
					return PROCESS_CONTINUE;
				if (name instanceof ICPPASTQualifiedName) {
					ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
					if (qname.isFullyQualified())
						return PROCESS_SKIP;
					return PROCESS_CONTINUE;
				}

				if (names.containsKey(name.getLookupKey())) {
					IASTNode parent = name.getParent();
					if (parent instanceof ICPPASTQualifiedName) {
						ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) parent).getQualifier();
						if (qualifier.length > 0 && qualifier[0] != name) {
							return PROCESS_CONTINUE;
						}
						result[0] = true;
						return PROCESS_ABORT;
					} else if (parent instanceof IASTIdExpression || parent instanceof ICPPASTNamedTypeSpecifier) {
						result[0] = true;
						return PROCESS_ABORT;
					}
				}
				return PROCESS_CONTINUE;
			}

			@Override
			public int visit(ASTAmbiguousNode node) {
				IASTNode[] alternatives = node.getNodes();
				for (IASTNode alt : alternatives) {
					if (!alt.accept(this))
						return PROCESS_ABORT;
				}
				return PROCESS_CONTINUE;
			}
		};
		id.accept(v);
		return result[0];
	}

	private static IASTName getNameForDeclarationInTemplateDeclaration(IASTDeclaration decl) {
		IASTName name = null;
		if (decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
			IASTDeclarator[] dtors = sdecl.getDeclarators();
			if (dtors != null && dtors.length > 0) {
				name = ASTQueries.findInnermostDeclarator(dtors[0]).getName();
			} else {
				IASTDeclSpecifier declspec = sdecl.getDeclSpecifier();
				if (declspec instanceof IASTCompositeTypeSpecifier) {
					name = ((IASTCompositeTypeSpecifier) declspec).getName();
				} else if (declspec instanceof IASTElaboratedTypeSpecifier) {
					name = ((IASTElaboratedTypeSpecifier) declspec).getName();
				}
			}
		} else if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fdef = (IASTFunctionDefinition) decl;
			name = ASTQueries.findInnermostDeclarator(fdef.getDeclarator()).getName();
		}
		return name;
	}

	private static ICPPASTInternalTemplateDeclaration getInnerTemplateDeclaration(final IASTName name) {
		IASTNode parent = name.getParent();
		while (parent instanceof IASTName) {
			parent = parent.getParent();
		}
		if (parent instanceof IASTDeclSpecifier) {
			if (!(parent instanceof IASTCompositeTypeSpecifier) && !(parent instanceof IASTElaboratedTypeSpecifier)) {
				return null;
			}
			parent = parent.getParent();
		} else {
			while (parent instanceof IASTDeclarator) {
				parent = parent.getParent();
			}
		}
		if (!(parent instanceof IASTDeclaration))
			return null;

		parent = parent.getParent();
		if (parent instanceof ICPPASTInternalTemplateDeclaration)
			return (ICPPASTInternalTemplateDeclaration) parent;

		return null;
	}

	private static ICPPASTInternalTemplateDeclaration getDirectlyEnclosingTemplateDeclaration(
			ICPPASTInternalTemplateDeclaration tdecl) {
		final IASTNode parent = tdecl.getParent();
		if (parent instanceof ICPPASTInternalTemplateDeclaration)
			return (ICPPASTInternalTemplateDeclaration) parent;

		return null;
	}

	public static IASTName getTemplateName(ICPPASTTemplateDeclaration templateDecl) {
		if (templateDecl == null)
			return null;

		ICPPASTTemplateDeclaration decl = templateDecl;
		while (decl.getParent() instanceof ICPPASTTemplateDeclaration)
			decl = (ICPPASTTemplateDeclaration) decl.getParent();

		IASTDeclaration nestedDecl = templateDecl.getDeclaration();
		while (nestedDecl instanceof ICPPASTTemplateDeclaration) {
			nestedDecl = ((ICPPASTTemplateDeclaration) nestedDecl).getDeclaration();
		}

		IASTName name = null;
		if (nestedDecl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) nestedDecl;
			if (simple.getDeclarators().length == 1) {
				IASTDeclarator dtor = simple.getDeclarators()[0];
				while (dtor.getNestedDeclarator() != null)
					dtor = dtor.getNestedDeclarator();
				name = dtor.getName();
			} else if (simple.getDeclarators().length == 0) {
				IASTDeclSpecifier spec = simple.getDeclSpecifier();
				if (spec instanceof ICPPASTCompositeTypeSpecifier) {
					name = ((ICPPASTCompositeTypeSpecifier) spec).getName();
				} else if (spec instanceof ICPPASTElaboratedTypeSpecifier) {
					name = ((ICPPASTElaboratedTypeSpecifier) spec).getName();
				}
			}
		} else if (nestedDecl instanceof IASTFunctionDefinition) {
			IASTDeclarator declarator = ((IASTFunctionDefinition) nestedDecl).getDeclarator();
			declarator = ASTQueries.findInnermostDeclarator(declarator);
			name = declarator.getName();
		} else if (nestedDecl instanceof ICPPASTAliasDeclaration) {
			name = ((ICPPASTAliasDeclaration) nestedDecl).getAlias();
		}
		if (name != null) {
			if (name instanceof ICPPASTQualifiedName) {
				ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) name).getQualifier();
				IASTDeclaration currDecl = decl;
				for (ICPPASTNameSpecifier segment : qualifier) {
					if (segment instanceof ICPPASTTemplateId) {
						if (currDecl == templateDecl) {
							return (IASTName) segment;
						}
						if (!(currDecl instanceof ICPPASTTemplateDeclaration)) {
							return null;
						}
						currDecl = ((ICPPASTTemplateDeclaration) currDecl).getDeclaration();
					}
				}
				if (currDecl == templateDecl) {
					return name.getLastName();
				}
			} else {
				return name;
			}
		}

		return null;
	}

	public static boolean areSameArguments(ICPPTemplateArgument[] args, ICPPTemplateArgument[] specArgs) {
		if (args.length != specArgs.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			if (!specArgs[i].isSameValue(args[i]))
				return false;
		}
		return true;
	}

	/**
	 * @param id the template id containing the template arguments
	 * @return an array of template arguments, currently modeled as IType objects.
	 *	 The empty ICPPTemplateArgument array is returned if id is {@code null}
	 */
	public static ICPPTemplateArgument[] createTemplateArgumentArray(ICPPASTTemplateId id) throws DOMException {
		ICPPTemplateArgument[] result = ICPPTemplateArgument.EMPTY_ARGUMENTS;
		if (id != null) {
			IASTNode[] args = id.getTemplateArguments();
			result = new ICPPTemplateArgument[args.length];
			for (int i = 0; i < args.length; i++) {
				IASTNode arg = args[i];
				if (arg instanceof IASTTypeId) {
					result[i] = new CPPTemplateTypeArgument(CPPVisitor.createType((IASTTypeId) arg));
				} else if (arg instanceof ICPPASTExpression) {
					ICPPASTExpression expr = (ICPPASTExpression) arg;
					CPPSemantics.pushLookupPoint(expr);
					try {
						result[i] = new CPPTemplateNonTypeArgument(expr.getEvaluation());
					} finally {
						CPPSemantics.popLookupPoint();
					}
				} else if (arg instanceof ICPPASTAmbiguousTemplateArgument) {
					IProblemBinding problem = new ProblemBinding(id,
							IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
					throw new DOMException(problem);
				} else {
					throw new IllegalArgumentException("Unexpected type: " + arg.getClass().getName()); //$NON-NLS-1$
				}
			}
		}
		return result;
	}

	public static ICPPFunction[] instantiateForFunctionCall(ICPPFunction[] fns, ICPPTemplateArgument[] tmplArgs,
			List<IType> fnArgs, List<ValueCategory> argCats, boolean withImpliedObjectArg) {
		// Extract template arguments.
		boolean requireTemplate = tmplArgs != null;
		boolean haveTemplate = false;

		for (final ICPPFunction func : fns) {
			if (func instanceof ICPPConstructor || (func instanceof ICPPMethod && ((ICPPMethod) func).isDestructor()))
				requireTemplate = false;

			if (func instanceof ICPPFunctionTemplate) {
				if (containsDependentType(fnArgs))
					return new ICPPFunction[] { CPPDeferredFunction.createForCandidates(fns) };

				if (requireTemplate && hasDependentArgument(tmplArgs))
					return new ICPPFunction[] { CPPDeferredFunction.createForCandidates(fns) };

				haveTemplate = true;
				break;
			}
		}

		if (!haveTemplate && !requireTemplate)
			return fns;

		final List<ICPPFunction> result = new ArrayList<>(fns.length);
		for (ICPPFunction fn : fns) {
			if (fn != null) {
				if (fn instanceof ICPPFunctionTemplate) {
					ICPPFunctionTemplate fnTmpl = (ICPPFunctionTemplate) fn;
					ICPPFunction inst = instantiateForFunctionCall(fnTmpl, tmplArgs, fnArgs, argCats,
							withImpliedObjectArg);
					if (inst != null)
						result.add(inst);
				} else if (!requireTemplate || fn instanceof ICPPUnknownBinding) {
					result.add(fn);
				}
			}
		}
		return result.toArray(new ICPPFunction[result.size()]);
	}

	private static ICPPFunction instantiateForFunctionCall(ICPPFunctionTemplate template,
			ICPPTemplateArgument[] tmplArgs, List<IType> fnArgs, List<ValueCategory> argCats,
			boolean withImpliedObjectArg) {
		if (withImpliedObjectArg && template instanceof ICPPMethod) {
			fnArgs = fnArgs.subList(1, fnArgs.size());
			argCats = argCats.subList(1, argCats.size());
		}

		CPPTemplateParameterMap map = new CPPTemplateParameterMap(fnArgs.size());
		try {
			ICPPTemplateArgument[] args = TemplateArgumentDeduction.deduceForFunctionCall(template, tmplArgs, fnArgs,
					argCats, map);
			if (args != null) {
				IBinding instance = instantiateFunctionTemplate(template, args, map);
				if (instance instanceof ICPPFunction) {
					final ICPPFunction f = (ICPPFunction) instance;
					if (isValidFunctionType(f.getType())) {
						// The number of arguments have been checked against the function
						// template's required argument count at an earlier stage. However,
						// the process of instantiation can increase the required argument
						// count by expanding parameter packs. If arguments are provided
						// for a parameter pack explicitly, it's possible for deduction to
						// succeed without having enough function arguments to match a
						// corresponding function parameter pack - so we check again.
						if (fnArgs.size() >= f.getRequiredArgumentCount())
							return f;
					}
				}
			}
		} catch (DOMException e) {
		}
		return null;
	}

	/**
	 * Checks if the given function type is problem-free and that the return type is not a function set.
	 */
	public static boolean isValidFunctionType(IFunctionType type) {
		if (!SemanticUtil.isValidType(type))
			return false;

		IType t = type.getReturnType();
		t = SemanticUtil.getNestedType(t, ALLCVQ | TDEF | REF);
		if (t instanceof IPointerType)
			t = ((IPointerType) t).getType();
		if (t instanceof FunctionSetType)
			return false;
		return true;
	}

	/**
	 * 14.8.2.3 Deducing conversion function template arguments
	 * @param point
	 */
	static ICPPFunction[] instantiateConversionTemplates(ICPPFunction[] functions, IType conversionType) {
		boolean checkedForDependentType = false;
		ICPPFunction[] result = functions;
		int i = 0;
		boolean done = false;
		for (ICPPFunction f : functions) {
			ICPPFunction inst = f;
			if (f instanceof ICPPFunctionTemplate) {
				ICPPFunctionTemplate template = (ICPPFunctionTemplate) f;
				inst = null;

				// Extract template arguments and parameter types.
				if (!checkedForDependentType) {
					if (isDependentType(conversionType)) {
						inst = CPPDeferredFunction.createForCandidates(functions);
						done = true;
					}
					checkedForDependentType = true;
				}
				CPPTemplateParameterMap map = new CPPTemplateParameterMap(1);
				try {
					ICPPTemplateArgument[] args = TemplateArgumentDeduction.deduceForConversion(template,
							conversionType, map);
					if (args != null) {
						IBinding instance = instantiateFunctionTemplate(template, args, map);
						if (instance instanceof ICPPFunction) {
							inst = (ICPPFunction) instance;
						}
					}
				} catch (DOMException e) {
					// try next candidate
				}
			}
			if (result != functions || f != inst) {
				if (result == functions) {
					result = new ICPPFunction[functions.length];
					System.arraycopy(functions, 0, result, 0, i);
				}
				result[i++] = inst;
			}
			if (done)
				break;
		}
		return result;
	}

	/**
	 * 14.8.2.6 Deducing template arguments from a function declaration
	 * @param point
	 * @return
	 */
	static ICPPFunction instantiateForFunctionDeclaration(ICPPFunctionTemplate template, ICPPTemplateArgument[] args,
			ICPPFunctionType functionType) {
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(1);
		try {
			args = TemplateArgumentDeduction.deduceForDeclaration(template, args, functionType, map);
			if (args != null) {
				IBinding instance = instantiateFunctionTemplate(template, args, map);
				if (instance instanceof ICPPFunction) {
					return (ICPPFunction) instance;
				}
			}
		} catch (DOMException e) {
			// try next candidate
		}
		return null;
	}

	/**
	 * 14.8.2.2 Deducing template arguments taking the address of a function template [temp.deduct.funcaddr]
	 */
	static ICPPFunction instantiateForAddressOfFunction(ICPPFunctionTemplate template, IFunctionType target,
			ICPPTemplateArgument[] args) {
		try {
			if (target != null && isDependentType(target)) {
				return CPPDeferredFunction.createForCandidates(template);
			}

			if (template instanceof ICPPConstructor || args == null)
				args = ICPPTemplateArgument.EMPTY_ARGUMENTS;

			CPPTemplateParameterMap map = new CPPTemplateParameterMap(4);
			args = TemplateArgumentDeduction.deduceForAddressOf(template, args, target, map);
			if (args != null) {
				IBinding instance = instantiateFunctionTemplate(template, args, map);
				if (instance instanceof ICPPFunction) {
					return (ICPPFunction) instance;
				}
			}
		} catch (DOMException e) {
		}
		return null;
	}

	/**
	 * N4659: [temp.deduct.partial] p11
	 */
	static int disambiguateTrailingParameterPack(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2, int nExplicitArgs) {
		ICPPTemplateParameter[] f1params = f1.getTemplateParameters();
		ICPPTemplateParameter[] f2params = f2.getTemplateParameters();
		boolean f1hasTrailingPack = f1params[f1params.length - 1].isParameterPack();
		boolean f2hasTrailingPack = f2params[f2params.length - 1].isParameterPack();
		if (f1hasTrailingPack && f1params.length > nExplicitArgs && !f2hasTrailingPack) {
			return -1;
		} else if (f2hasTrailingPack && f2params.length > nExplicitArgs && !f1hasTrailingPack) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * 14.5.6.2 Partial ordering of function templates
	 *
	 * @param f1
	 * @param f2
	 * @param mode
	 * @param nExplicitArgs determines the number of parameters taken into consideration for ordering:
	 *        for ordering in the context of a call, nExplicitArguments should be the number of arguments in the call
	 *        for ordering in other contexts, nExplicitArguments should be Integer.MAX_VALUE to indicate
	 *         that all parameters should be considered
	 */
	static int orderFunctionTemplates(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2, TypeSelection mode,
			int nExplicitArgs) throws DOMException {
		if (f1 == f2)
			return 0;
		if (f1 == null)
			return -1;
		if (f2 == null)
			return 1;

		int s1 = compareSpecialization(f1, f2, mode, nExplicitArgs);
		int s2 = compareSpecialization(f2, f1, mode, nExplicitArgs);

		if (s1 == s2) {
			return disambiguateTrailingParameterPack(f1, f2, nExplicitArgs);
		}
		if (s1 < 0 || s2 > 0)
			return -1;
		assert s2 < 0 || s1 > 0;
		return 1;
	}

	static int orderFunctionTemplates(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2, TypeSelection mode)
			throws DOMException {
		return orderFunctionTemplates(f1, f2, mode, Integer.MAX_VALUE);
	}

	private static ICPPFunction transferFunctionTemplate(ICPPFunctionTemplate f) throws DOMException {
		final ICPPTemplateParameter[] tpars = f.getTemplateParameters();
		final int argLen = tpars.length;

		// Create arguments and map
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[argLen];
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(argLen);
		for (int i = 0; i < argLen; i++) {
			final ICPPTemplateParameter tpar = tpars[i];
			final ICPPTemplateArgument arg = uniqueArg(tpar);
			args[i] = arg;
			if (tpar.isParameterPack()) {
				map.put(tpar, new ICPPTemplateArgument[] { arg });
			} else {
				map.put(tpar, arg);
			}
		}

		IBinding result = instantiateFunctionTemplate(f, args, map);
		if (result instanceof ICPPFunction)
			return (ICPPFunction) result;

		return null;
	}

	private static ICPPTemplateArgument uniqueArg(final ICPPTemplateParameter tpar) throws DOMException {
		final ICPPTemplateArgument arg;
		if (tpar instanceof ICPPTemplateNonTypeParameter) {
			arg = new CPPTemplateNonTypeArgument(IntegralValue.unique(),
					((ICPPTemplateNonTypeParameter) tpar).getType());
		} else {
			arg = new CPPTemplateTypeArgument(new UniqueType(tpar.isParameterPack()));
		}
		return arg;
	}

	/**
	 * [temp.func.order] p5: Considers only parameters for which there are explicit call arguments.
	 *
	 * Since at this point non-viable functions are already excluded, it is safe to just ignore extra parameters
	 * without checking if they can be ignored, i.e. without checking if they are function parameter packs,
	 * parameters with default arguments, or ellipsis parameter.
	 */
	private static ICPPFunctionType getFunctionTypeIgnoringParametersWithoutExplicitArguments(ICPPFunction function,
			int nExplicitArgs) {
		ICPPFunctionType originalType = function.getType();
		if (nExplicitArgs < function.getParameters().length) {
			IType[] parameterTypesWithExplicitArguments = Arrays.copyOf(originalType.getParameterTypes(),
					nExplicitArgs);
			return new CPPFunctionType(originalType.getReturnType(), parameterTypesWithExplicitArguments,
					originalType.getNoexceptSpecifier(), originalType.isConst(), originalType.isVolatile(),
					originalType.hasRefQualifier(), originalType.isRValueReference(), originalType.takesVarArgs());
		} else
			return originalType;
	}

	private static int compareSpecialization(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2, TypeSelection mode,
			int nExplicitArgs) throws DOMException {
		ICPPFunction transF1 = transferFunctionTemplate(f1);
		if (transF1 == null)
			return -1;

		final ICPPFunctionType ft2 = f2.getType();
		final ICPPFunctionType transFt1 = getFunctionTypeIgnoringParametersWithoutExplicitArguments(transF1,
				nExplicitArgs);
		IType[] pars;
		IType[] args;
		switch (mode) {
		case RETURN_TYPE:
			pars = new IType[] { ft2.getReturnType() };
			args = new IType[] { transFt1.getReturnType() };
			break;
		case PARAMETERS_AND_RETURN_TYPE:
			pars = SemanticUtil.concatTypes(ft2.getReturnType(), ft2.getParameterTypes());
			args = SemanticUtil.concatTypes(transFt1.getReturnType(), transFt1.getParameterTypes());
			break;
		case PARAMETERS:
		default:
			pars = ft2.getParameterTypes();
			args = transFt1.getParameterTypes();
			boolean nonStaticMember1 = isNonStaticMember(f1);
			boolean nonStaticMember2 = isNonStaticMember(f2);
			if (nonStaticMember1 != nonStaticMember2) {
				if (nonStaticMember1) {
					args = SemanticUtil.addImplicitParameterType(args, (ICPPMethod) f1);
				} else {
					pars = SemanticUtil.addImplicitParameterType(pars, (ICPPMethod) f2);
				}
			}
			break;
		}
		return TemplateArgumentDeduction.deduceForPartialOrdering(f2.getTemplateParameters(), pars, args);
	}

	private static boolean isNonStaticMember(ICPPFunctionTemplate f) {
		return (f instanceof ICPPMethod) && !((ICPPMethod) f).isStatic();
	}

	private static ICPPPartialSpecialization findPartialSpecialization(ICPPPartiallySpecializable template,
			ICPPTemplateArgument[] args) throws DOMException {
		ICPPPartialSpecialization[] pspecs = template.getPartialSpecializations();
		if (pspecs != null && pspecs.length > 0) {
			final String argStr = ASTTypeUtil.getArgumentListString(args, true);
			for (ICPPPartialSpecialization pspec : pspecs) {
				if (argStr.equals(ASTTypeUtil.getArgumentListString(pspec.getTemplateArguments(), true)))
					return pspec;
			}
		}
		return null;
	}

	static IBinding selectSpecialization(ICPPPartiallySpecializable template, ICPPTemplateArgument[] args,
			boolean isDef) throws DOMException {
		if (template == null) {
			return null;
		}
		ICPPPartialSpecialization[] specializations = template.getPartialSpecializations();
		if (specializations == null || specializations.length == 0) {
			return null;
		}

		ICPPPartialSpecialization bestMatch = null;
		CPPTemplateParameterMap bestMap = null;
		boolean bestMatchIsBest = true;
		for (ICPPPartialSpecialization specialization : specializations) {
			final CPPTemplateParameterMap map = new CPPTemplateParameterMap(args.length);
			ICPPTemplateArgument[] specializationArguments = specialization.getTemplateArguments();
			if (TemplateArgumentDeduction.fromTemplateArguments(specialization.getTemplateParameters(),
					specializationArguments, args, map)
					&& checkInstantiationOfArguments(specializationArguments, map)) {
				int compare = orderSpecializations(bestMatch, specialization);
				if (compare == 0) {
					bestMatchIsBest = false;
				} else if (compare < 0) {
					bestMatch = specialization;
					bestMap = map;
					bestMatchIsBest = true;
				}
			}
		}

		// 14.5.4.1 If none of the specializations is more specialized than all the other matching
		// specializations, then the use of the class template is ambiguous and the program is
		// ill-formed.
		if (!bestMatchIsBest) {
			return new CPPTemplateDefinition.CPPTemplateProblem(CPPSemantics.getCurrentLookupPoint(),
					IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, template.getNameCharArray());
		}

		if (bestMatch == null)
			return null;

		if (bestMatch instanceof ICPPClassTemplatePartialSpecialization) {
			bestMatch = SemanticUtil.mapToAST((ICPPClassTemplatePartialSpecialization) bestMatch);
		}

		return instantiatePartialSpecialization(bestMatch, args, isDef, bestMap);
	}

	private static boolean checkInstantiationOfArguments(ICPPTemplateArgument[] args, CPPTemplateParameterMap tpMap)
			throws DOMException {
		return instantiateArguments(args, new InstantiationContext(tpMap), true) != null;
	}

	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * @param spec1
	 * @param spec2
	 * @param point
	 * @return
	 * @throws DOMException
	 */
	static private int orderSpecializations(ICPPPartialSpecialization spec1, ICPPPartialSpecialization spec2)
			throws DOMException {
		if (spec1 == null) {
			return -1;
		}

		// we avoid the transformation to function templates, of which the one parameter
		// will be used in the end.

		// 14.5.5.2
		// A template is more specialized than another if and only if it is at least as specialized as the
		// other template and that template is not at least as specialized as the first.
		boolean f1IsAtLeastAsSpecializedAsF2 = isAtLeastAsSpecializedAs(spec1, spec2);
		boolean f2IsAtLeastAsSpecializedAsF1 = isAtLeastAsSpecializedAs(spec2, spec1);

		if (f1IsAtLeastAsSpecializedAsF2 == f2IsAtLeastAsSpecializedAsF1)
			return 0;

		if (f1IsAtLeastAsSpecializedAsF2)
			return 1;

		return -1;
	}

	private static boolean isAtLeastAsSpecializedAs(ICPPPartialSpecialization f1, ICPPPartialSpecialization f2)
			throws DOMException {
		// 14.5.5.2
		// Using the transformed parameter list, perform argument deduction against the other
		// function template
		// The transformed template is at least as specialized as the other if and only if the deduction
		// succeeds and the deduced parameter types are an exact match.
		final ICPPTemplateParameter[] tpars1 = f1.getTemplateParameters();
		final ICPPTemplateParameter[] tpars2 = f2.getTemplateParameters();
		final ICPPTemplateArgument[] targs1 = f1.getTemplateArguments();
		final ICPPTemplateArgument[] targs2 = f2.getTemplateArguments();

		// Transfer arguments of specialization 1
		final int tpars1Len = tpars1.length;
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[tpars1Len];
		final CPPTemplateParameterMap transferMap = new CPPTemplateParameterMap(tpars1Len);
		for (int i = 0; i < tpars1Len; i++) {
			final ICPPTemplateParameter param = tpars1[i];
			final ICPPTemplateArgument arg = uniqueArg(param);
			args[i] = arg;
			if (param.isParameterPack()) {
				transferMap.put(param, new ICPPTemplateArgument[] { arg });
			} else {
				transferMap.put(param, arg);
			}
		}
		final ICPPTemplateArgument[] transferredArgs1 = instantiateArguments(targs1,
				new InstantiationContext(transferMap), false);

		// Deduce arguments for specialization 2
		final CPPTemplateParameterMap deductionMap = new CPPTemplateParameterMap(2);
		return TemplateArgumentDeduction.fromTemplateArguments(tpars2, targs2, transferredArgs1, deductionMap);
	}

	static boolean isValidArgument(ICPPTemplateArgument arg) {
		return arg != null
				&& SemanticUtil.isValidType(arg.isTypeValue() ? arg.getTypeValue() : arg.getTypeOfNonTypeValue());
	}

	static ICPPTemplateArgument matchTemplateParameterAndArgument(ICPPTemplateDefinition template,
			ICPPTemplateParameter param, ICPPTemplateArgument arg, CPPTemplateParameterMap map) {
		if (!isValidArgument(arg)) {
			return null;
		}
		if (param instanceof ICPPTemplateTypeParameter) {
			IType t = arg.getTypeValue();
			if (t != null && !(t instanceof ICPPTemplateDefinition))
				return arg;
			return null;
		}

		if (param instanceof ICPPTemplateTemplateParameter) {
			IType t = arg.getTypeValue();
			if (t instanceof ICPPUnknownType)
				return arg;
			while (!(t instanceof ICPPTemplateDefinition)) {
				if (t instanceof ICPPClassSpecialization) {
					// Undo the effect of specializing a template when the unqualified name
					// is used within the template itself.
					t = ((ICPPClassSpecialization) t).getSpecializedBinding();
				} else {
					return null;
				}
			}

			ICPPTemplateParameter[] pParams = null;
			ICPPTemplateParameter[] aParams = null;
			try {
				pParams = ((ICPPTemplateTemplateParameter) param).getTemplateParameters();
				aParams = ((ICPPTemplateDefinition) t).getTemplateParameters();
				if (!matchTemplateTemplateParameters(pParams, aParams))
					return null;
			} catch (DOMException e) {
				return null;
			}

			return arg;
		}

		if (param instanceof ICPPTemplateNonTypeParameter) {
			if (!arg.isNonTypeValue())
				return null;
			IType argType = arg.getTypeOfNonTypeValue();
			try {
				IType pType = ((ICPPTemplateNonTypeParameter) param).getType();
				if (pType instanceof ICPPParameterPackType) {
					pType = ((ICPPParameterPackType) pType).getType();
				}
				if (map != null && pType != null) {
					pType = instantiateType(pType, new InstantiationContext(map));
				}

				if (argType instanceof ICPPParameterPackType) {
					argType = ((ICPPParameterPackType) argType).getType();
				}
				if (argType instanceof ICPPUnknownType) {
					return new CPPTemplateNonTypeArgument(arg.getNonTypeValue(), pType);
				}
				return convertNonTypeTemplateArgument(template, pType, arg);
			} catch (DOMException e) {
				return null;
			}
		}
		assert false;
		return null;
	}

	private static boolean matchTemplateTemplateParameters(ICPPTemplateParameter[] pParams,
			ICPPTemplateParameter[] aParams) throws DOMException {
		int pi = 0;
		int ai = 0;
		while (pi < pParams.length && ai < aParams.length) {
			final ICPPTemplateParameter pp = pParams[pi];
			final ICPPTemplateParameter ap = aParams[ai];

			// A parameter pack does not match a regular template parameter.
			if (ap.isParameterPack() && !pp.isParameterPack())
				return false;

			boolean pb = pp instanceof ICPPTemplateTypeParameter;
			boolean ab = ap instanceof ICPPTemplateTypeParameter;
			if (pb != ab)
				return false;

			if (pb) {
				// Both are template type parameters
			} else {
				pb = pp instanceof ICPPTemplateNonTypeParameter;
				ab = ap instanceof ICPPTemplateNonTypeParameter;
				if (pb != ab)
					return false;

				if (pb) {
					// Both are non-type parameters
				} else {
					if (!(pp instanceof ICPPTemplateTemplateParameter)
							|| !(ap instanceof ICPPTemplateTemplateParameter)) {
						assert false;
						return false;
					}

					if (!matchTemplateTemplateParameters(((ICPPTemplateTemplateParameter) pp).getTemplateParameters(),
							((ICPPTemplateTemplateParameter) ap).getTemplateParameters()))
						return false;
				}
			}
			if (!pp.isParameterPack())
				pi++;
			ai++;
		}
		if (pi < pParams.length) {
			if (pi == pParams.length - 1 && pParams[pi].isParameterPack())
				return true;
			return false;
		}

		return ai == aParams.length;
	}

	/**
	 * Converts the template argument {@code arg} to match the parameter type {@code paramType}
	 * or returns {@code null}, if this violates the rules specified in 14.3.2 - 5.
	 *
	 * @throws DOMException
	 */
	private static ICPPTemplateArgument convertNonTypeTemplateArgument(ICPPTemplateDefinition template,
			final IType paramType, ICPPTemplateArgument arg) throws DOMException {
		// 14.1s8 function to pointer and array to pointer conversions.
		IType a = arg.getTypeOfNonTypeValue();
		if (a instanceof ICPPParameterPackType) {
			a = ((ICPPParameterPackType) a).getType();
		}
		IType p;
		if (paramType instanceof IFunctionType) {
			p = new CPPPointerType(paramType);
		} else if (paramType instanceof IArrayType) {
			p = new CPPPointerType(((IArrayType) paramType).getType());
		} else {
			p = paramType;
			if (p != null && p.isSameType(a))
				return arg;
		}

		if (a instanceof FunctionSetType) {
			if (p instanceof IPointerType) {
				p = ((IPointerType) p).getType();
			}
			if (p instanceof IFunctionType) {
				final CPPFunctionSet functionSet = ((FunctionSetType) a).getFunctionSet();
				for (ICPPFunction f : functionSet.getBindings()) {
					if (p.isSameType(f.getType())) {
						functionSet.applySelectedFunction(f);
						return new CPPTemplateNonTypeArgument(new EvalBinding(f, null, template));
					}
				}
			}
			return null;
		}

		if (paramType instanceof TypeOfDependentExpression) {
			// Partial support for C++17 template <auto>
			TypeOfDependentExpression type = (TypeOfDependentExpression) paramType;
			if (type.isForTemplateAuto()) {
				return arg;
			}
		}

		Cost cost = Conversions.checkImplicitConversionSequence(p, a, LVALUE, UDCMode.FORBIDDEN, Context.ORDINARY);
		if (cost == null || !cost.converts()) {
			ICPPEvaluation eval = arg.getNonTypeEvaluation();
			ICPPEvaluation newEval = CPPEvaluation.maybeApplyConversion(eval, p, false, true);
			if (newEval == EvalFixed.INCOMPLETE && newEval != eval)
				return null;
			return new CPPTemplateNonTypeArgument(newEval);
		}

		return new CPPTemplateNonTypeArgument(arg.getNonTypeValue(), paramType);
	}

	static boolean argsAreTrivial(ICPPTemplateParameter[] pars, ICPPTemplateArgument[] args) {
		if (pars.length != args.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			ICPPTemplateParameter par = pars[i];
			ICPPTemplateArgument arg = args[i];
			if (par instanceof IType) {
				if (arg.isNonTypeValue())
					return false;
				IType argType = arg.getTypeValue();
				if (argType == null)
					return false;
				if (par.isParameterPack()) {
					if (!(argType instanceof ICPPParameterPackType))
						return false;
					argType = ((ICPPParameterPackType) argType).getType();
					if (argType == null)
						return false;
				}
				if (!argType.isSameType((IType) par))
					return false;
			} else {
				if (arg.isTypeValue())
					return false;
				if (par.isParameterPack() != arg.isPackExpansion())
					return false;
				int parpos = IntegralValue.isTemplateParameter(arg.getNonTypeValue());
				if (parpos != par.getParameterID())
					return false;
			}
		}
		return true;
	}

	public static boolean hasDependentArgument(ICPPTemplateArgument[] args) {
		for (ICPPTemplateArgument arg : args) {
			if (isDependentArgument(arg))
				return true;
		}
		return false;
	}

	public static boolean isDependentArgument(ICPPTemplateArgument arg) {
		if (arg.isTypeValue())
			return isDependentType(arg.getTypeValue());

		ICPPEvaluation evaluation = arg.getNonTypeEvaluation();
		return evaluation.isTypeDependent() || evaluation.isValueDependent();
	}

	public static boolean containsDependentType(List<IType> ts) {
		for (IType t : ts) {
			if (isDependentType(t))
				return true;
		}
		return false;
	}

	public static boolean containsDependentType(IType[] ts) {
		for (IType t : ts) {
			if (isDependentType(t))
				return true;
		}
		return false;
	}

	public static boolean isDependentType(IType t) {
		while (true) {
			if (t instanceof ICPPUnknownType)
				return true;

			if (t instanceof ICPPFunctionType) {
				final ICPPFunctionType ft = (ICPPFunctionType) t;
				if (containsDependentType(ft.getParameterTypes()))
					return true;
				t = ft.getReturnType();
			} else if (t instanceof ICPPPointerToMemberType) {
				ICPPPointerToMemberType ptmt = (ICPPPointerToMemberType) t;
				if (isDependentType(ptmt.getMemberOfClass()))
					return true;
				t = ptmt.getType();
			} else if (t instanceof ICPPParameterPackType) {
				return true;
			} else if (t instanceof ITypeContainer) {
				if (t instanceof IArrayType) {
					IValue asize = ((IArrayType) t).getSize();
					if (asize != null && IntegralValue.isDependentValue(asize))
						return true;
				}
				t = ((ITypeContainer) t).getType();
			} else if (t instanceof InitializerListType) {
				return ((InitializerListType) t).getEvaluation().isTypeDependent();
			} else if (t instanceof IBinding) {
				IBinding owner = ((IBinding) t).getOwner();
				if (owner instanceof IFunction) {
					if (owner instanceof ICPPFunctionTemplate) {
						return true;
					}
					owner = owner.getOwner();
				}
				if (owner instanceof ICPPClassTemplate)
					return true;
				return (owner instanceof IType) && owner != t && isDependentType((IType) owner);
			} else {
				return false;
			}
		}
	}

	public static boolean containsDependentArg(ObjectMap tpMap) {
		for (Object arg : tpMap.valueArray()) {
			if (isDependentType((IType) arg))
				return true;
		}
		return false;
	}

	/**
	 * Attempts to (partially) resolve an unknown binding with the given arguments.
	 */
	public static IBinding resolveUnknown(ICPPUnknownBinding unknown, InstantiationContext context)
			throws DOMException {
		if (unknown instanceof ICPPDeferredClassInstance) {
			return resolveDeferredClassInstance((ICPPDeferredClassInstance) unknown, context);
		}
		if (unknown instanceof ICPPDeferredVariableInstance) {
			return resolveDeferredVariableInstance((ICPPDeferredVariableInstance) unknown, context);
		}
		if (unknown instanceof ICPPUnknownMember) {
			return resolveUnknownMember((ICPPUnknownMember) unknown, context);
		}
		if (unknown instanceof ICPPTemplateParameter && unknown instanceof IType) {
			IType type = resolveTemplateTypeParameter((ICPPTemplateParameter) unknown, context);
			if (type instanceof IBinding)
				return (IBinding) type;
		}
		if (unknown instanceof TypeOfDependentExpression) {
			IType type = instantiateType((IType) unknown, context);
			if (type instanceof IBinding)
				return (IBinding) type;
		}
		return unknown;
	}

	private static IBinding resolveUnknownMember(ICPPUnknownMember unknown, InstantiationContext context)
			throws DOMException {
		final IType ot0 = unknown.getOwnerType();
		if (ot0 == null)
			return unknown;

		IBinding result = unknown;
		IType ot1 = instantiateType(ot0, context);
		if (ot1 != null) {
			ot1 = SemanticUtil.getUltimateType(ot1, false);
			if (ot1 instanceof ICPPUnknownType) {
				if (unknown instanceof ICPPUnknownMemberClassInstance) {
					ICPPUnknownMemberClassInstance ucli = (ICPPUnknownMemberClassInstance) unknown;
					ICPPTemplateArgument[] args0 = ucli.getArguments();
					ICPPTemplateArgument[] args1 = instantiateArguments(args0, context, false);
					if (args0 != args1 || !ot1.isSameType(ot0)) {
						args1 = SemanticUtil.getSimplifiedArguments(args1);
						result = new CPPUnknownClassInstance(ot1, ucli.getNameCharArray(), args1);
					}
				} else if (!ot1.isSameType(ot0)) {
					if (unknown instanceof ICPPUnknownMemberClass) {
						result = new CPPUnknownMemberClass(ot1, unknown.getNameCharArray());
					} else {
						result = new CPPUnknownMethod(ot1, unknown.getNameCharArray());
					}
				}
			} else if (ot1 instanceof ICPPClassType) {
				IScope s = ((ICPPClassType) ot1).getCompositeScope();
				if (s != null) {
					result = CPPSemantics.resolveUnknownName(s, unknown);
					if (unknown instanceof ICPPUnknownMemberClassInstance && (result instanceof ICPPTemplateDefinition
							|| result instanceof ICPPAliasTemplateInstance)) {
						ICPPTemplateArgument[] args1 = instantiateArguments(
								((ICPPUnknownMemberClassInstance) unknown).getArguments(), context, false);
						if (result instanceof ICPPClassTemplate) {
							result = instantiate((ICPPClassTemplate) result, args1);
						} else if (result instanceof ICPPAliasTemplate) {
							result = instantiateAliasTemplate((ICPPAliasTemplate) result, args1);
						}
					}
				}
			} else if (ot1 != ot0) {
				return new ProblemBinding(new CPPASTName(unknown.getNameCharArray()),
						CPPSemantics.getCurrentLookupPoint(), IProblemBinding.SEMANTIC_BAD_SCOPE);
			}
		}

		return result;
	}

	private static IBinding resolveDeferredClassInstance(ICPPDeferredClassInstance dci, InstantiationContext context) {
		ICPPClassTemplate classTemplate = dci.getClassTemplate();
		ICPPTemplateArgument[] arguments = dci.getTemplateArguments();
		ICPPTemplateArgument[] newArgs;
		try {
			newArgs = instantiateArguments(arguments, context, true);
		} catch (DOMException e) {
			return e.getProblem();
		}
		if (newArgs == null)
			return createProblem(classTemplate, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);

		boolean changed = arguments != newArgs;
		IType classTemplateSpecialization = instantiateType(classTemplate, context);
		if (classTemplateSpecialization != classTemplate) {
			if (classTemplateSpecialization instanceof ICPPClassTemplate) {
				classTemplate = (ICPPClassTemplate) classTemplateSpecialization;
				changed = true;
			} else if (classTemplateSpecialization instanceof ICPPAliasTemplate) {
				IBinding inst = instantiateAliasTemplate((ICPPAliasTemplate) classTemplateSpecialization, newArgs);
				if (inst != null)
					return inst;
			}
		}

		if (changed) {
			IBinding inst = instantiate(classTemplate, newArgs);
			if (inst != null)
				return inst;
		}
		return dci;
	}

	private static IBinding resolveDeferredVariableInstance(ICPPDeferredVariableInstance dvi,
			InstantiationContext context) {
		ICPPVariableTemplate variableTemplate = dvi.getTemplateDefinition();
		ICPPTemplateArgument[] arguments = dvi.getTemplateArguments();
		ICPPTemplateArgument[] newArgs;
		try {
			newArgs = instantiateArguments(arguments, context, true);
		} catch (DOMException e) {
			return e.getProblem();
		}
		if (newArgs == null) {
			return createProblem(variableTemplate, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
		}

		// Unlike class templates, variable templates cannot be passed as template template arguments,
		// so there is no need to instantiate the template itself.

		if (arguments != newArgs) {
			IBinding inst = instantiate(variableTemplate, newArgs);
			if (inst != null)
				return inst;
		}
		return dvi;
	}

	public static boolean haveSameArguments(ICPPTemplateInstance i1, ICPPTemplateInstance i2) {
		final ICPPTemplateArgument[] m1 = i1.getTemplateArguments();
		final ICPPTemplateArgument[] m2 = i2.getTemplateArguments();

		if (m1 == null || m2 == null || m1.length != m2.length)
			return false;

		String s1 = ASTTypeUtil.getArgumentListString(m1, true);
		String s2 = ASTTypeUtil.getArgumentListString(m2, true);
		return s1.equals(s2);
	}

	/**
	 * Creates a template parameter map for the given template definition and template arguments. The template
	 * arguments are adjusted by converting types of the non-type arguments to match the types of the template
	 * parameters.
	 *
	 * @param template the template definition
	 * @param arguments the template arguments; arguments may be modified by this method due to type
	 *     conversion performed for non-type arguments
	 * @return the created template parameter map, or {@code null} if the arguments are invalid
	 */
	private static CPPTemplateParameterMap createParameterMap(ICPPTemplateDefinition template,
			ICPPTemplateArgument[] arguments) {
		final ICPPTemplateParameter[] parameters = template.getTemplateParameters();
		final int numArgs = arguments.length;
		final int numParams = parameters.length;
		final int length = Math.max(numArgs, numParams);

		CPPTemplateParameterMap map = new CPPTemplateParameterMap(numParams);

		boolean isPack = false;
		ICPPTemplateParameter param = null;
		for (int i = 0; i < length; i++) {
			if (!isPack || param == null) {
				if (i >= numParams)
					return null;
				param = parameters[i];
				isPack = param.isParameterPack();
			}
			if (i < numArgs) {
				ICPPTemplateArgument arg = arguments[i];
				ICPPTemplateArgument newArg = matchTemplateParameterAndArgument(template, param, arg, map);
				if (newArg == null)
					return null;
				if (newArg != arg) {
					arguments[i] = newArg;
				}
				if (!isPack) {
					map.put(param, newArg);
				}
			} else {
				// Parameter pack with empty arguments.
				assert isPack;
			}
		}

		if (isPack) {
			int packOffset = numParams - 1;
			int packSize = numArgs - packOffset;
			ICPPTemplateArgument[] pack = new ICPPTemplateArgument[packSize];
			System.arraycopy(arguments, packOffset, pack, 0, packSize);
			map.put(param, pack);
		}
		return map;
	}

	public static IBinding findDeclarationForSpecialization(IBinding binding) {
		while (binding instanceof ICPPSpecialization) {
			if (ASTInternal.hasDeclaration(binding))
				return binding;

			IBinding original = ((ICPPSpecialization) binding).getSpecializedBinding();
			if (original == null)
				return binding;
			binding = original;
		}
		return binding;
	}

	/**
	 * Returns the instantiated function body of the given function specialization.
	 */
	public static ICPPExecution instantiateFunctionBody(ICPPFunctionSpecialization f) {
		ICPPFunction spec = (ICPPFunction) f.getSpecializedBinding();
		ICPPExecution exec = null;
		if (spec instanceof ICPPComputableFunction) {
			exec = ((ICPPComputableFunction) spec).getFunctionBodyExecution();
			if (exec != null) {
				InstantiationContext context = new InstantiationContext(f.getTemplateParameterMap(), f);
				CPPTemplates.addInstantiatedParameters(context, f);
				exec = exec.instantiate(context, IntegralValue.MAX_RECURSION_DEPTH);
			}
		}
		return exec;
	}

	private static void addInstantiatedParameters(InstantiationContext context, ICPPFunctionSpecialization spec) {
		ICPPFunction specialized = (ICPPFunction) spec.getSpecializedBinding();
		ICPPParameter paramSpecs[] = spec.getParameters();
		ICPPParameter specializedParams[] = specialized.getParameters();
		for (int i = 0; i < paramSpecs.length; i++) {
			final ICPPParameter paramSpecialization = paramSpecs[i];
			final ICPPParameter specializedParam = specializedParams[i];
			context.putInstantiatedLocal(specializedParam, paramSpecialization);
			if (specializedParam.isParameterPack()) {
				break;
			}
		}
	}

	/**
	 * Returns the instantiated constructor chain of the given constructor specialization.
	 */
	public static ICPPExecution instantiateConstructorChain(ICPPConstructorSpecialization f) {
		ICPPConstructor spec = (ICPPConstructor) f.getSpecializedBinding();
		ICPPExecution exec = null;
		if (spec != null) {
			exec = spec.getConstructorChainExecution();
			if (exec != null) {
				InstantiationContext context = new InstantiationContext(f.getTemplateParameterMap(), f);
				exec = exec.instantiate(context, IntegralValue.MAX_RECURSION_DEPTH);
			}
		}
		return exec;
	}

	/**
	 * If 'name' is a destructor-name, return the name of the type (i.e. the
	 * portion following the '~'). Otherwise return null.
	 */
	public static String unwrapDestructorName(char[] name) {
		if (name == null || name.length == 0 || name[0] != '~') {
			return null;
		}
		return new String(name).substring(1).trim();
	}

	/**
	 * Instantiate a plain name (simple-id).
	 * Only destructor names require instantiation, e.g. the name "~T", when instantiated
	 * with a parameter map that maps T to C, needs to become "~C".
	 *
	 * @param name the name to be instantiated
	 * @param context the instantiation context
	 * @param enclosingTemplate The enclosing template definition. This is required because the
	 *                          instantiation context doesn't actually store parameter names, so
	 *                          we need to walk the chain of enclosing templates to find potential
	 *                          template parameter names.
	 * @return The instantiated name. If the provided name is not a destructor name, or if
	 *         the type named by the destructor name is not mapped to anything in the
	 *         instantiation context's parameter map, the provided name is returned unchanged.
	 */
	public static char[] instantiateName(char[] name, InstantiationContext context, IBinding enclosingTemplate) {
		String typename = unwrapDestructorName(name);
		if (typename == null) { // not a destructor-name
			return name;
		}
		ICPPTemplateParameterMap map = context.getParameterMap();
		IBinding enclosing = enclosingTemplate;
		while (enclosing != null) {
			if (enclosing instanceof ICPPTemplateDefinition) {
				for (ICPPTemplateParameter param : ((ICPPTemplateDefinition) enclosing).getTemplateParameters()) {
					if (param instanceof ICPPTemplateTypeParameter) {
						if (param.getName().equals(typename)) {
							ICPPTemplateArgument arg = map.getArgument(param);
							if (arg instanceof CPPTemplateTypeArgument) {
								IType argType = arg.getTypeValue();
								argType = SemanticUtil.getNestedType(argType, CVTYPE | TDEF);
								if (argType instanceof ICPPClassType) {
									// Destructor for class type.
									StringBuilder result = new StringBuilder();
									result.append('~');
									result.append(((ICPPClassType) argType).getName());
									return result.toString().toCharArray();
								} else if (argType instanceof ICPPBasicType) {
									// Pseudo-destructor for builtin type.
									StringBuilder result = new StringBuilder();
									result.append('~');
									ASTTypeUtil.appendType(argType, true, result);
									return result.toString().toCharArray();
								}
							}
						}
					}
				}
			}
			enclosing = enclosing.getOwner();
		}
		return name;
	}

	/**
	 * Checks whether a binding is fully instantiated, that is, it does not depend on template
	 * parameters that do not yet have values.
	 */
	public static boolean isFullyInstantiated(IBinding binding) {
		while (binding != null) {
			binding = binding.getOwner();
			if (binding instanceof ICPPTemplateDefinition) {
				return false;
			}
			if (!(binding instanceof ICPPClassType)) {
				break;
			}
		}
		return true;
	}

	private static Map<TypeInstantiationRequest, IType> getInstantiationCache() {
		IASTNode lookupPoint = CPPSemantics.getCurrentLookupPoint();
		if (lookupPoint != null) {
			IASTTranslationUnit tu = lookupPoint.getTranslationUnit();
			if (tu instanceof CPPASTTranslationUnit) {
				return ((CPPASTTranslationUnit) tu).getInstantiationCache();
			}
		}
		return null;
	}

	private static IType getCachedInstantiation(TypeInstantiationRequest instantiationRequest) {
		Map<TypeInstantiationRequest, IType> cache = getInstantiationCache();
		return cache != null ? cache.get(instantiationRequest) : null;
	}

	private static void putCachedInstantiation(TypeInstantiationRequest instantiationRequest, IType result) {
		Map<TypeInstantiationRequest, IType> cache = getInstantiationCache();
		if (cache != null) {
			cache.put(instantiationRequest, result);
		}
	}
}
