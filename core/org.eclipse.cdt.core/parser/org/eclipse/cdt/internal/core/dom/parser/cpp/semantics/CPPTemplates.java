/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
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
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecializationSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedefSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;

/**
 * Collection of static methods to perform template instantiation, member specialization and
 * type instantiation.
 */
public class CPPTemplates {
	private static final int PACK_SIZE_DEFER = -1;
	private static final int PACK_SIZE_FAIL = -2;
	private static final int PACK_SIZE_NOT_FOUND = Integer.MAX_VALUE;
	private static final ICPPFunction[] NO_FUNCTIONS = {};
	static enum TypeSelection {PARAMETERS, RETURN_TYPE, PARAMETERS_AND_RETURN_TYPE}
	
	/**
	 * Instantiates a class template with the given arguments. May return <code>null</code>.
	 */
	public static IBinding instantiate(ICPPClassTemplate template, ICPPTemplateArgument[] args, boolean isDef) {
		try {
			// Add default arguments, if necessary.
			ICPPTemplateArgument[] arguments= SemanticUtil.getSimplifiedArguments(args);
			arguments= addDefaultArguments(template, arguments);
			if (arguments == null)
				return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);

			if (template instanceof ICPPTemplateTemplateParameter || hasDependentArgument(arguments)) {
				return deferredInstance(template, arguments);
			}
			
			if (template instanceof ICPPClassTemplatePartialSpecialization) {
				return instantiatePartialSpecialization((ICPPClassTemplatePartialSpecialization) template, arguments, isDef, null);
			}
		
			
			final ICPPTemplateParameter[] parameters= template.getTemplateParameters();
			final int numArgs = arguments.length;
			final int numParams= parameters.length;
			final int length= Math.max(numArgs, numParams);

			CPPTemplateParameterMap map= new CPPTemplateParameterMap(numParams);
				
			boolean isPack= false;
			ICPPTemplateParameter param= null;
			for (int i = 0; i < length; i++) {
				if (!isPack || param == null) {
					if (i < numParams) {
						param= parameters[i];
						isPack= param.isParameterPack();
					} else {
						return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
					}
				}
				if (i < numArgs) {
					ICPPTemplateArgument arg= arguments[i];
					ICPPTemplateArgument newArg = CPPTemplates.matchTemplateParameterAndArgument(param, arg, map);
					if (newArg == null)
						return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
					if (newArg != arg) {
						if (arguments == args) {
							arguments= args.clone();
						}
						arguments[i]= newArg;
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
				int packOffset= numParams-1;
				int packSize= numArgs - packOffset;
				ICPPTemplateArgument[] pack= new ICPPTemplateArgument[packSize];
				System.arraycopy(arguments, packOffset, pack, 0, packSize);
				map.put(param, pack);
			}

			IBinding result= CPPTemplates.selectSpecialization(template, arguments, isDef);
			if (result != null)
				return result;
			
			return instantiatePrimaryTemplate(template, arguments, map, isDef);	
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	private static IBinding createProblem(ICPPClassTemplate template, int id) {
		IASTNode node= new CPPASTName(template.getNameCharArray());
		return new ProblemBinding(node, id, template.getNameCharArray());
	}

	static IBinding isUsedInClassTemplateScope(ICPPClassTemplate ct, IASTName name) {
		try {
			IASTName start= name;
			ICPPASTFunctionDefinition func= CPPVisitor.findEnclosingFunctionDefinition(name);
			if (func != null) {
				start= ASTQueries.findInnermostDeclarator(func.getDeclarator()).getName();
				start= start.getLastName();
			}

			IScope scope= CPPVisitor.getContainingScope(start);
			while (scope instanceof IASTInternalScope) {
				if (scope instanceof ISemanticProblem)
					return null;
				final IASTInternalScope internalScope = (IASTInternalScope) scope;
				if (scope instanceof ICPPClassScope) {
					final IName scopeName = internalScope.getScopeName();
					if (scopeName instanceof IASTName) {
						IBinding b= ((IASTName) scopeName).resolveBinding();
						if (b instanceof IType && ct.isSameType((IType) b)) {
							return CPPTemplates.instantiateWithinClassTemplate(ct);
						}
						if (b instanceof ICPPClassTemplatePartialSpecialization) {
							ICPPClassTemplatePartialSpecialization pspec= (ICPPClassTemplatePartialSpecialization) b;
							if (ct.isSameType(pspec.getPrimaryClassTemplate())) {
								return CPPTemplates.instantiateWithinClassTemplate(pspec);
							}
						} else if (b instanceof ICPPClassSpecialization) {
							ICPPClassSpecialization specialization= (ICPPClassSpecialization) b;
							if (ct.isSameType(specialization.getSpecializedBinding())) {
								return specialization;
							}
						}
					}
				}
				scope= CPPVisitor.getContainingScope(internalScope.getPhysicalNode());
				if (scope == internalScope)
					return null;
			}
		} catch (DOMException e) {
		}
		return null;
	}

	private static IBinding instantiateFunctionTemplate(ICPPFunctionTemplate template,
			ICPPTemplateArgument[] arguments, CPPTemplateParameterMap map) throws DOMException {
		ICPPTemplateInstance instance= getInstance(template, arguments, false);
		if (instance != null) {
			return instance;
		}
	
		IBinding owner= template.getOwner();
		instance = CPPTemplates.createInstance(owner, template, map, arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	/**
	 * Instantiates a partial class template specialization.
	 */
	private static IBinding instantiatePartialSpecialization(
			ICPPClassTemplatePartialSpecialization partialSpec, ICPPTemplateArgument[] args, boolean isDef,
			CPPTemplateParameterMap tpMap) throws DOMException {
		ICPPTemplateInstance instance= getInstance(partialSpec, args, isDef);
		if (instance != null)
			return instance;

		if (tpMap == null) {
			tpMap = new CPPTemplateParameterMap(args.length);
			if (!TemplateArgumentDeduction.fromTemplateArguments(partialSpec.getTemplateParameters(),
					partialSpec.getTemplateArguments(), args, tpMap))
				return null;
		}

		instance= createInstance(partialSpec.getOwner(), partialSpec, tpMap, args);
		addInstance(partialSpec, args, instance);
		return instance;
	}

	/** 
	 * Instantiates the selected template, without looking for specializations. May return <code>null</code>.
	 * @param map 
	 */
	private static IBinding instantiatePrimaryTemplate(ICPPClassTemplate template, ICPPTemplateArgument[] arguments, 
			CPPTemplateParameterMap map, boolean isDef) throws DOMException {
		
		assert !(template instanceof ICPPClassTemplatePartialSpecialization);
		ICPPTemplateInstance instance= getInstance(template, arguments, isDef);
		if (instance != null) {
			return instance;
		}

		IBinding owner= template.getOwner();
		instance = CPPTemplates.createInstance(owner, template, map, arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	/**
	 * Obtains a cached instance from the template.
	 */
	private static ICPPTemplateInstance getInstance(ICPPTemplateDefinition template,
			ICPPTemplateArgument[] args, boolean forDefinition) {
		if (template instanceof ICPPInstanceCache) {
			ICPPTemplateInstance result = ((ICPPInstanceCache) template).getInstance(args);
			if (forDefinition && result instanceof IIndexBinding)
				return null;
			return result;
		}
		return null;
	}

	/**
	 * Caches an instance with the template.
	 */
	private static void addInstance(ICPPTemplateDefinition template, ICPPTemplateArgument[] args, ICPPTemplateInstance instance) {
		if (template instanceof ICPPInstanceCache) {
			((ICPPInstanceCache) template).addInstance(args, instance);
		}
	}

	private static IBinding deferredInstance(ICPPClassTemplate template, ICPPTemplateArgument[] arguments) throws DOMException {
		ICPPTemplateInstance instance= getInstance(template, arguments, false);
		if (instance != null)
			return instance;

		instance = new CPPDeferredClassInstance(template, arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	private static ICPPTemplateArgument[] addDefaultArguments(ICPPClassTemplate template,
			ICPPTemplateArgument[] arguments) throws DOMException {
		if (template instanceof ICPPClassTemplatePartialSpecialization)
			return arguments;
		
		boolean havePackExpansion= false;
		for (int i = 0; i < arguments.length; i++) {
			ICPPTemplateArgument arg = arguments[i];
			if (arg.isPackExpansion()) {
				if (i != arguments.length-1) {
					return arguments;
				}
				havePackExpansion= true;
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
			if (tpars[tparCount-1].isParameterPack())
				return arguments;
			
			if (havePackExpansion && tparCount+1 == argCount)
				return arguments;
			return null;
		} 
		
		// Fewer arguments are allowed with a pack expansion
		if (havePackExpansion)
			return arguments;
		
		// Fewer arguments are allowed with	default arguments
		if (tpars[tparCount-1].isParameterPack())
			tparCount--;
		
		if (tparCount == argCount) 
			return arguments;
		
		ICPPTemplateArgument[] completeArgs= new ICPPTemplateArgument[tparCount];
		CPPTemplateParameterMap map= new CPPTemplateParameterMap(tparCount);
		for (int i = 0; i < tparCount; i++) {
			final ICPPTemplateParameter tpar = tpars[i];
			if (tpar.isParameterPack()) {
				// Parameter pack must be last template parameter.
				return null;
			}
			ICPPTemplateArgument arg;
			if (i < argCount) {
				arg= arguments[i];
			} else {
				ICPPTemplateArgument defaultArg= tpar.getDefaultValue();
				if (defaultArg == null) {
					if (template instanceof ICPPInternalClassTemplate) {
						defaultArg= ((ICPPInternalClassTemplate) template).getDefaultArgFromIndex(i);
					}
				}
				if (defaultArg == null)
					return null;
				arg= instantiateArgument(defaultArg, map, -1, null);
				if (!isValidArgument(arg)) {
					return null;
				}								
			}
			map.put(tpar, arg);
			completeArgs[i]= arg;
		}
		return completeArgs;
	}

	/**
	 * Instantiates the template for usage within its own body. May return <code>null</code>.
	 */
	public static ICPPClassType instantiateWithinClassTemplate(ICPPClassTemplate template) throws DOMException {
		ICPPTemplateInstance di= template.asDeferredInstance();
		if (di instanceof ICPPClassType)
			return (ICPPClassType) di;
		
		ICPPTemplateArgument[] args;
		if (template instanceof ICPPClassTemplatePartialSpecialization) {
			args= ((ICPPClassTemplatePartialSpecialization) template).getTemplateArguments();
		} else {
			ICPPTemplateParameter[] templateParameters = template.getTemplateParameters();
			args = templateParametersAsArguments(templateParameters);
		}
		IBinding result = deferredInstance(template, args);
    	if (result instanceof ICPPClassType)
    		return (ICPPClassType) result;
    	
    	return template;
	}

	public static ICPPTemplateArgument[] templateParametersAsArguments(
			ICPPTemplateParameter[] templateParameters) throws DOMException {
		ICPPTemplateArgument[] args;
		args = new ICPPTemplateArgument[templateParameters.length];
		for (int i = 0; i < templateParameters.length; i++) {
			final ICPPTemplateParameter tp = templateParameters[i];
			if (tp instanceof IType) {
				IType t= (IType) tp;
				if (tp.isParameterPack()) {
					t= new CPPParameterPackType(t);
				}
				args[i] = new CPPTemplateArgument(t);
			} else if (tp instanceof ICPPTemplateNonTypeParameter) {
				// Non-type template parameter pack already has type 'ICPPParameterPackType'
				final ICPPTemplateNonTypeParameter nttp = (ICPPTemplateNonTypeParameter) tp;
				args[i] = new CPPTemplateArgument(Value.create(nttp), nttp.getType());
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
			ICPPASTTemplateDeclaration[] templates = new ICPPASTTemplateDeclaration[] { (ICPPASTTemplateDeclaration) parent };

			while (parent.getParent() instanceof ICPPASTTemplateDeclaration) {
				parent = parent.getParent();
				templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.append(ICPPASTTemplateDeclaration.class, templates, parent);
			}
			templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.trim(ICPPASTTemplateDeclaration.class, templates);

			ICPPASTTemplateDeclaration templateDeclaration = templates[0];
			IASTDeclaration decl = templateDeclaration.getDeclaration();
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();

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
					dtor= ASTQueries.findInnermostDeclarator(dtor);
					name = dtor.getName();
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				IASTDeclarator dtor = ((IASTFunctionDefinition) decl).getDeclarator();
				dtor= ASTQueries.findInnermostDeclarator(dtor);
				name = dtor.getName();
			}
			if (name == null)
				return null;

			if (name instanceof ICPPASTQualifiedName) {
				int idx = templates.length;
				int i = 0;
				IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
				for (IASTName element : ns) {
					if (element instanceof ICPPASTTemplateId) {
						++i;
						if (i == idx) {
							binding = ((ICPPASTTemplateId) element).resolveBinding();
							break;
						}
					}
				}
				if (binding == null)
					binding = ns[ns.length - 1].resolveBinding();
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
    		return new CPPTemplateTypeParameter(((ICPPASTSimpleTypeTemplateParameter) tp).getName(), tp.isParameterPack());
    	} 
    	if (tp instanceof ICPPASTTemplatedTypeTemplateParameter) {
        	return new CPPTemplateTemplateParameter(((ICPPASTTemplatedTypeTemplateParameter) tp).getName(), tp.isParameterPack());
    	}
    	assert tp instanceof ICPPASTParameterDeclaration;
    	final IASTDeclarator dtor = ((ICPPASTParameterDeclaration) tp).getDeclarator();
    	return new CPPTemplateNonTypeParameter(ASTQueries.findInnermostDeclarator(dtor).getName());
	}
	
	static public ICPPScope getContainingScope(IASTNode node) {
		while (node != null) {
			if (node instanceof ICPPASTTemplateParameter) {
				IASTNode parent = node.getParent();
				if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration) parent).getScope();
				}
			}
			node = node.getParent();
		}

		return null;
	}

	public static IBinding createBinding(ICPPASTTemplateId id) {
		if (!isClassTemplate(id)) {
			//functions are instantiated as part of the resolution process
			IBinding result= CPPVisitor.createBinding(id);
			IASTName templateName = id.getTemplateName();
			if (result instanceof ICPPTemplateInstance) {
				templateName.setBinding(((ICPPTemplateInstance) result).getTemplateDefinition());
			} else {
				templateName.setBinding(result);
			}
			return result;
		}
		
		IASTNode parentOfName = id.getParent();
		boolean isLastName= true;
		if (parentOfName instanceof ICPPASTQualifiedName) {
			isLastName= ((ICPPASTQualifiedName) parentOfName).getLastName() == id;
			parentOfName = parentOfName.getParent();
		}

		boolean isDecl= false;
		boolean isDef= false;
		if (isLastName) {
			if (parentOfName instanceof ICPPASTElaboratedTypeSpecifier) {
				IASTNode parentOfDeclaration= parentOfName;
				while (parentOfDeclaration != null) {
					if (parentOfDeclaration instanceof IASTDeclaration) {
						parentOfDeclaration= parentOfDeclaration.getParent();
						break;
					}
					parentOfDeclaration= parentOfDeclaration.getParent();
				}

				isDecl= !(parentOfDeclaration instanceof ICPPASTExplicitTemplateInstantiation);
			} else if (parentOfName instanceof ICPPASTCompositeTypeSpecifier) {
				isDef= true;
			} 
		}
		try {
			// class template instance
			IBinding result= null;
			IASTName templateName = id.getTemplateName();
			IBinding template = templateName.resolvePreBinding();

			if (template instanceof ICPPConstructor) {
				template= template.getOwner();
			}
			
			if (template instanceof ICPPUnknownClassType) {
				IBinding owner= template.getOwner();
				if (owner instanceof ICPPUnknownBinding) {
					ICPPTemplateArgument[] args= createTemplateArgumentArray(id);
					return new CPPUnknownClassInstance((ICPPUnknownBinding) template.getOwner(), id.getSimpleID(), args);
				}
			}

			if (!(template instanceof ICPPClassTemplate) || template instanceof ICPPClassTemplatePartialSpecialization) 
				return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE, templateName.toCharArray());

			final ICPPClassTemplate classTemplate = (ICPPClassTemplate) template;
			ICPPTemplateArgument[] args= createTemplateArgumentArray(id);
			if (hasDependentArgument(args)) {
				ICPPASTTemplateDeclaration tdecl= getTemplateDeclaration(id);
				if (tdecl != null) {
					if (argsAreTrivial(classTemplate.getTemplateParameters(), args)) {
						result= classTemplate;  
					} else {
						ICPPClassTemplatePartialSpecialization partialSpec= findPartialSpecialization(classTemplate, args);
						if (isDecl || isDef) {
							if (partialSpec == null) {
								partialSpec = new CPPClassTemplatePartialSpecialization(id);
								if (template instanceof ICPPInternalClassTemplate)
									((ICPPInternalClassTemplate) template).addPartialSpecialization(partialSpec);
								return partialSpec;
							} 
						}
						if (partialSpec == null)
							return new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE, templateName.toCharArray());
						result= partialSpec;
					}
				}
			}
			if (result == null) {
				result= instantiate(classTemplate, args, isDef);
				if (result instanceof ICPPInternalBinding) {
					if (isDecl) {
						ASTInternal.addDeclaration(result, id);
					} else if (isDef) {
						ASTInternal.addDefinition(result, id);
					}
				}
			}
			return CPPSemantics.postResolution(result, id);
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	static boolean isClassTemplate(ICPPASTTemplateId id) {
		IASTNode parentOfName = id.getParent();

		if (parentOfName instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) parentOfName).getLastName() != id)
				return true;
			parentOfName= parentOfName.getParent();
		}

		return  parentOfName instanceof ICPPASTElaboratedTypeSpecifier ||
				parentOfName instanceof ICPPASTCompositeTypeSpecifier ||
				parentOfName instanceof ICPPASTNamedTypeSpecifier || 
				parentOfName instanceof ICPPASTBaseSpecifier;
	}


	public static ICPPTemplateInstance createInstance(IBinding owner, ICPPTemplateDefinition template, 
			CPPTemplateParameterMap tpMap, ICPPTemplateArgument[] args) {
		if (owner instanceof ICPPSpecialization) {
			ICPPTemplateParameterMap map= ((ICPPSpecialization) owner).getTemplateParameterMap();
			if (map != null) {
				tpMap.putAll(map);
			}
		}
		
		ICPPTemplateInstance instance = null;
		if (template instanceof ICPPClassType) {
			instance = new CPPClassInstance((ICPPClassType) template, owner, tpMap, args);
		} else if (owner instanceof ICPPClassType && template instanceof ICPPMethod) {
			if (template instanceof ICPPConstructor) {
				instance = new CPPConstructorInstance((ICPPConstructor) template, (ICPPClassType) owner, tpMap, args);
			} else {
				instance = new CPPMethodInstance((ICPPMethod) template, (ICPPClassType) owner, tpMap, args);
			}
		} else if (template instanceof ICPPFunction) {
			instance = new CPPFunctionInstance((ICPPFunction) template, owner, tpMap, args);
		}
		return instance;
	}

	public static IBinding createSpecialization(ICPPClassSpecialization owner, IBinding decl) {
		IBinding spec = null;
		final ICPPTemplateParameterMap tpMap= owner.getTemplateParameterMap();
		if (decl instanceof ICPPClassTemplatePartialSpecialization) {
			try {
				ICPPClassTemplatePartialSpecialization pspec= (ICPPClassTemplatePartialSpecialization) decl;
				ICPPClassTemplate template= (ICPPClassTemplate) owner.specializeMember(pspec.getPrimaryClassTemplate());
				spec= new CPPClassTemplatePartialSpecializationSpecialization(pspec, template, tpMap);
			} catch (DOMException e) {
			}
		} else if (decl instanceof ICPPClassTemplate) {
			spec = new CPPClassTemplateSpecialization((ICPPClassTemplate) decl, owner, tpMap);
		} else if (decl instanceof ICPPClassType) {
			spec = new CPPClassSpecialization((ICPPClassType) decl, owner, tpMap);
		} else if (decl instanceof ICPPField) {
			spec = new CPPFieldSpecialization(decl, owner, tpMap);
		} else if (decl instanceof ICPPFunctionTemplate) {
			if (decl instanceof ICPPConstructor)
				spec = new CPPConstructorTemplateSpecialization((ICPPConstructor) decl, owner, tpMap);
			else if (decl instanceof ICPPMethod)
				spec = new CPPMethodTemplateSpecialization((ICPPMethod) decl, owner, tpMap);
			else
				spec = new CPPFunctionTemplateSpecialization((ICPPFunctionTemplate) decl, owner, tpMap);
		} else if (decl instanceof ICPPConstructor) {
			spec = new CPPConstructorSpecialization((ICPPConstructor) decl, owner, tpMap);
		} else if (decl instanceof ICPPMethod) {
			spec = new CPPMethodSpecialization((ICPPMethod) decl, owner, tpMap);
		} else if (decl instanceof ICPPFunction) {
			spec = new CPPFunctionSpecialization((ICPPFunction) decl, owner, tpMap);
		} else if (decl instanceof ITypedef) {
		    spec = new CPPTypedefSpecialization(decl, owner, tpMap);
		} else if (decl instanceof IEnumeration || decl instanceof IEnumerator) {
			// TODO(sprigogin): Deal with a case when an enumerator value depends on a template parameter.
		    spec = decl;
		}
		return spec;
	}
	
	public static IValue instantiateValue(IValue value, ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within, int maxdepth) {
		if (value == null)
			return null;
		IBinding[] unknowns= value.getUnknownBindings();
		IBinding[] resolvedUnknowns= null;
		if (unknowns.length != 0) {
			for (int i = 0; i < unknowns.length; i++) {
				IBinding unknown= unknowns[i];
				IBinding resolved= unknown;
				if (unknown instanceof ICPPUnknownBinding) {
					try {
						resolved= resolveUnknown((ICPPUnknownBinding) unknown, tpMap, packOffset, within);
					} catch (DOMException e) {
						return Value.UNKNOWN;
					}
				}
				if (resolvedUnknowns != null) {
					resolvedUnknowns[i]= resolved;
				} else if (resolved != unknown) {
					resolvedUnknowns= new IBinding[unknowns.length];
					System.arraycopy(unknowns, 0, resolvedUnknowns, 0, i);
					resolvedUnknowns[i]= resolved;
				}
			}
		}
		
		if (resolvedUnknowns != null)
			return Value.reevaluate(value, packOffset, resolvedUnknowns, tpMap, maxdepth);
			
		if (Value.referencesTemplateParameter(value)) 
			return Value.reevaluate(value, packOffset, unknowns, tpMap, maxdepth);

		return value;
	}

	public static boolean containsParameterPack(IType type) {
		return determinePackSize(type, CPPTemplateParameterMap.EMPTY) == PACK_SIZE_DEFER;
	}

	private static int determinePackSize(IType type, ICPPTemplateParameterMap tpMap) {
		if (type instanceof ICPPFunctionType) {
			final ICPPFunctionType ft = (ICPPFunctionType) type;
			final IType rt = ft.getReturnType();
			int r = determinePackSize(rt, tpMap);
			if (r < 0) 
				return r;
			IType[] ps = ft.getParameterTypes();
			for (IType pt : ps) {
				r= combine(r, determinePackSize(pt, tpMap));
				if (r < 0)
					return r;
			}
			return r;
		} 

		if (type instanceof ICPPTemplateParameter) {
			final ICPPTemplateParameter tpar = (ICPPTemplateParameter) type;
			if (tpar.isParameterPack()) {
				ICPPTemplateArgument[] args= tpMap.getPackExpansion(tpar);
				if (args != null)
					return args.length;
				return PACK_SIZE_DEFER;
			}
			return PACK_SIZE_NOT_FOUND;
		} 

		int r= PACK_SIZE_NOT_FOUND;
		if (type instanceof ICPPUnknownBinding) {
			if (type instanceof ICPPDeferredClassInstance) {
				ICPPDeferredClassInstance dcl= (ICPPDeferredClassInstance) type;
				ICPPTemplateArgument[] args = dcl.getTemplateArguments();
				for (ICPPTemplateArgument arg : args) {
					r= combine(r, determinePackSize(arg, tpMap));
					if (r < 0)
						return r;
				}
			}
			IBinding binding= ((ICPPUnknownBinding) type).getOwner();
			if (binding instanceof IType)
				r= combine(r, determinePackSize((IType) binding, tpMap));

			return r;
		}

		if (type instanceof ICPPParameterPackType)
			return PACK_SIZE_NOT_FOUND;
		
		if (type instanceof IArrayType) {
			IArrayType at= (IArrayType) type;
			IValue asize= at.getSize();
			r= determinePackSize(asize, tpMap);
			if (r < 0)
				return r;
		}

		if (type instanceof ITypeContainer) {
			final ITypeContainer typeContainer = (ITypeContainer) type;
			r= combine(r, determinePackSize(typeContainer.getType(), tpMap));
		} 
		return r;
	}

	private static int combine(int ps1, int ps2) {
		if (ps1 < 0 || ps2 == PACK_SIZE_NOT_FOUND)
			return ps1;
		if (ps2 < 0 || ps1 == PACK_SIZE_NOT_FOUND)
			return ps2;
		if (ps1 != ps2)
			return PACK_SIZE_FAIL;
		return ps1;
	}

	private static int determinePackSize(IValue value, ICPPTemplateParameterMap tpMap) {
		int r= PACK_SIZE_NOT_FOUND;
		IBinding[] unknown= value.getUnknownBindings();
		for (IBinding binding : unknown) {
			if (binding instanceof IType) {
				r= combine(r, determinePackSize((IType) binding, tpMap));
				if (r < 0)
					return r;
			}
		}
		int[] tpars= Value.getParameterPackReferences(value);
		for (int parID : tpars) {
			ICPPTemplateArgument[] args= tpMap.getPackExpansion(parID);
			if (args != null) {
				r= combine(r, args.length);
				if (r < 0)
					return r;
			}
			return PACK_SIZE_DEFER;
		}
		return r;
	}

	private static int determinePackSize(ICPPTemplateArgument arg, ICPPTemplateParameterMap tpMap) {
		if (arg.isTypeValue())
			return determinePackSize(arg.getTypeValue(), tpMap);
		return determinePackSize(arg.getNonTypeValue(), tpMap);
	}

	/**
	 * Instantiates types contained in an array.
	 * @param types an array of types
	 * @param tpMap template argument map
	 * @return an array containing instantiated types.
	 */
	public static IType[] instantiateTypes(IType[] types, ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within) {
		// Don't create a new array until it's really needed.
		IType[] result = types;
		for (int i = 0; i < types.length; i++) {
			IType origType = types[i];
			IType newType;
			if (origType instanceof ICPPParameterPackType) {
				origType= ((ICPPParameterPackType) origType).getType();
				int packSize= determinePackSize(origType, tpMap);
				if (packSize == PACK_SIZE_FAIL || packSize == PACK_SIZE_NOT_FOUND) {
					newType= new ProblemBinding(null, IProblemBinding.SEMANTIC_INVALID_TYPE);
				} else if (packSize == PACK_SIZE_DEFER) {
					newType= origType;
				} else {
					IType[] packResult= new IType[types.length+packSize-1];
					System.arraycopy(result, 0, packResult, 0, types.length-1);
					for(int j=0; j<packSize; j++) {
						packResult[i+j]= CPPTemplates.instantiateType(origType, tpMap, j, within);
					}
					result= packResult;
					continue;
				}
			} else {
				newType = CPPTemplates.instantiateType(origType, tpMap, packOffset, within);
			}
			if (result != types) {
				result[i]= newType;
			} else if (newType != origType) {
				result = new IType[types.length];
				if (i > 0) {
					System.arraycopy(types, 0, result, 0, i);
				}
				result[i]= newType;
			}
		}
		return result;
	}

	/**
	 * Instantiates arguments contained in an array.
	 */
	public static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args,
			ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within)
			throws DOMException {
		// Don't create a new array until it's really needed.
		ICPPTemplateArgument[] result = args;
		int resultShift= 0;
		for (int i = 0; i < args.length; i++) {
			ICPPTemplateArgument origArg = args[i];
			ICPPTemplateArgument newArg;
			if (origArg.isPackExpansion()) {
				origArg= origArg.getExpansionPattern();
				int packSize= determinePackSize(origArg, tpMap);
				if (packSize == PACK_SIZE_FAIL || packSize == PACK_SIZE_NOT_FOUND) {
					throw new DOMException(new ProblemBinding(null, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS));
				} else if (packSize == PACK_SIZE_DEFER) {
					newArg= origArg;
				} else {
					final int shift = packSize-1;
					ICPPTemplateArgument[] newResult= new ICPPTemplateArgument[args.length + resultShift + shift];
					System.arraycopy(result, 0, newResult, 0, i+resultShift);
					for(int j=0; j<packSize; j++) {
						newResult[i+resultShift+j]= CPPTemplates.instantiateArgument(origArg, tpMap, j, within);
					}
					result= newResult;
					resultShift+= shift;
					continue;
				}
			} else {
				newArg = CPPTemplates.instantiateArgument(origArg, tpMap, packOffset, within);
			}
			if (result != args) {
				result[i+resultShift]= newArg;
			} else if (newArg != origArg) {
				assert resultShift==0;
				result = new ICPPTemplateArgument[args.length];
				if (i > 0) {
					System.arraycopy(args, 0, result, 0, i);
				}
				result[i]= newArg;
			}
		}
		return result;
	}

	/**
	 * Instantiates an argument
	 */
	static ICPPTemplateArgument instantiateArgument(ICPPTemplateArgument arg,
			ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within) {
		if (arg == null)
			return null;
		if (arg.isNonTypeValue()) {
			final IValue origValue= arg.getNonTypeValue();
			final IType origType= arg.getTypeOfNonTypeValue();
			final IValue instValue= instantiateValue(origValue, tpMap, packOffset, within, Value.MAX_RECURSION_DEPTH);
			final IType instType= instantiateType(origType, tpMap, packOffset, within);
			if (origType == instType && origValue == instValue)
				return arg;
			return new CPPTemplateArgument(instValue, instType);
		}
		
		final IType orig= arg.getTypeValue();
		final IType inst= instantiateType(orig, tpMap, packOffset, within);
		if (orig == inst)
			return arg;
		return new CPPTemplateArgument(inst);
	}

	/**
	 * This method propagates the specialization of a member to the types used by the member.
	 * @param type a type to instantiate.
	 * @param tpMap a mapping between template parameters and the corresponding arguments.
	 */
	public static IType instantiateType(IType type, ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within) {
		try {
			if (tpMap == null)
				return type;

			if (type instanceof ICPPFunctionType) {
				final ICPPFunctionType ft = (ICPPFunctionType) type;
				IType ret = null;
				IType[] params = null;
				final IType r = ft.getReturnType();
				ret = instantiateType(r, tpMap, packOffset, within);
				IType[] ps = ft.getParameterTypes();
				params = instantiateTypes(ps, tpMap, packOffset, within);
				if (ret == r && params == ps) {
					return type;
				}
				return new CPPFunctionType(ret, params, ft.isConst(), ft.isVolatile(), ft.takesVarArgs());
			} 

			if (type instanceof ICPPTemplateParameter) {
				final ICPPTemplateParameter tpar = (ICPPTemplateParameter) type;
				ICPPTemplateArgument arg= null;
				if (tpar.isParameterPack()) {
					if (packOffset >= 0) {
						ICPPTemplateArgument[] args = tpMap.getPackExpansion(tpar);
						if (args != null) {
							if (packOffset >= args.length) {
								return new ProblemBinding(null, IProblemBinding.SEMANTIC_INVALID_TYPE);
							}
							arg= args[packOffset];
						}
					}
				} else {
					arg= tpMap.getArgument(tpar);
				}

				if (arg != null) {
					IType t= arg.getTypeValue();
					if (t != null)
						return t;
				}
				return type;
			} 

			if (type instanceof ICPPUnknownBinding) {
				IBinding binding= resolveUnknown((ICPPUnknownBinding) type, tpMap, packOffset, within);
				if (binding instanceof IType)
					return (IType) binding;

				return type;
			}

			if (within != null && type instanceof IBinding && 
					(type instanceof ITypedef || type instanceof ICPPClassType)) {
				ICPPClassType originalClass= within.getSpecializedBinding();
				if (originalClass.isSameType(type))
					return within;
				
				IBinding typeAsBinding= (IBinding) type;
				IBinding typeOwner= typeAsBinding.getOwner();
				if (typeOwner instanceof IType) {
					IType newOwner= instantiateType((IType) typeOwner, tpMap, packOffset, within);
					if (newOwner != typeOwner && newOwner instanceof ICPPClassSpecialization) {
						return (IType) ((ICPPClassSpecialization) newOwner).specializeMember(typeAsBinding);
					}
					return type;
				}
			}		

			if (type instanceof ITypeContainer) {
				final ITypeContainer typeContainer = (ITypeContainer) type;
				IType nestedType = typeContainer.getType();
				IType newNestedType = instantiateType(nestedType, tpMap, packOffset, within);
				if (typeContainer instanceof ICPPPointerToMemberType) {
					ICPPPointerToMemberType ptm = (ICPPPointerToMemberType) typeContainer;
					IType memberOfClass = ptm.getMemberOfClass();
					IType newMemberOfClass = instantiateType(memberOfClass, tpMap, packOffset, within);
					if (!(newMemberOfClass instanceof ICPPClassType || newMemberOfClass instanceof UniqueType 
							|| newMemberOfClass instanceof ICPPUnknownBinding)) {
						newMemberOfClass = memberOfClass;
					}
					if (newNestedType != nestedType || newMemberOfClass != memberOfClass) {
						return new CPPPointerToMemberType(newNestedType, newMemberOfClass,
								ptm.isConst(), ptm.isVolatile(), ptm.isRestrict());
					}
					return typeContainer;
				} 
				if (typeContainer instanceof IArrayType) {
					IArrayType at= (IArrayType) typeContainer;
					IValue asize= at.getSize();
					if (asize != null) {
						IValue newSize= instantiateValue(asize, tpMap, packOffset, within, Value.MAX_RECURSION_DEPTH);
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

			return type;
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	/**
	 * Checks whether a given name corresponds to a template declaration and returns the ast node for it.
	 * This works for the name of a template-definition and also for a name needed to qualify a member
	 * definition:
	 * <pre>
	 * template &lttypename T&gt void MyTemplate&ltT&gt::member() {}
	 * </pre>
	 * @param name a name for which the corresponding template declaration is searched for.
	 * @return the template declaration or <code>null</code> if <code>name</code> does not
	 * correspond to a template declaration.
	 */
	public static ICPPASTTemplateDeclaration getTemplateDeclaration(IASTName name) {
		if (name == null) 
			return null;

		// first look for a related sequence of template declarations
		ICPPASTInternalTemplateDeclaration tdecl= getInnerTemplateDeclaration(name);
		if (tdecl == null)
			return null;
		
		name= name.getLastName();
		IASTNode parent= name.getParent();
		if (!(parent instanceof ICPPASTQualifiedName)) {
			if (parent instanceof ICPPASTTemplateId) {
				return null;
			}
			// one name: use innermost template declaration
			return tdecl;
		} 
		
		// last name can be associated even if it is not a template-id
		final ICPPASTQualifiedName qname= (ICPPASTQualifiedName) parent;
		final IASTName lastName = qname.getLastName();
		final boolean lastIsTemplate= tdecl.isAssociatedWithLastName();
		if (name == lastName) { 
			if (lastIsTemplate) {
				return tdecl;
			}
			return null;
		} 
		
		// not the last name, search for the matching template declaration
		if (!(name instanceof ICPPASTTemplateId)) 
			return null;
			
		if (lastIsTemplate) {
			// skip one
			tdecl= getDirectlyEnclosingTemplateDeclaration(tdecl);
		}
		final IASTName[] ns= qname.getNames();
		for (int i = ns.length-2; tdecl != null && i >= 0; i--) {
			final IASTName n = ns[i];
			if (n == name) {
				return tdecl;
			}
			if (n instanceof ICPPASTTemplateId) {
				tdecl= getDirectlyEnclosingTemplateDeclaration(tdecl);
			}
		}
		// not enough template declartaions
		return null;
	}
	
	public static void associateTemplateDeclarations(ICPPASTInternalTemplateDeclaration tdecl) {
		// find innermost template declaration
		IASTDeclaration decl= tdecl.getDeclaration();
		while (decl instanceof ICPPASTInternalTemplateDeclaration) {
			tdecl= (ICPPASTInternalTemplateDeclaration) decl;
			decl= tdecl.getDeclaration();
		}
		final ICPPASTInternalTemplateDeclaration innerMostTDecl= tdecl;
		
		// find name declared within the template declaration
		IASTName name= getNameForDeclarationInTemplateDeclaration(decl);

		// count template declarations
		int tdeclcount= 1;
		IASTNode node= tdecl.getParent();
		while (node instanceof ICPPASTInternalTemplateDeclaration) {
			tdeclcount++;
			tdecl = (ICPPASTInternalTemplateDeclaration) node;
			node= node.getParent();
		}
		final ICPPASTInternalTemplateDeclaration outerMostTDecl= tdecl;

		// determine association of names with template declarations
		boolean lastIsTemplate= true;
		int missingTemplateDecls= 0;
		if (name instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qname= (ICPPASTQualifiedName) name;
			final IASTName lastName = qname.getLastName();
			final boolean lastIsID = lastName instanceof ICPPASTTemplateId;

			// count template-ids
			int idcount= 0;
			final IASTName[] ns= qname.getNames();
			for (final IASTName n : ns) {
				if (n instanceof ICPPASTTemplateId) {
					idcount++;
				}
			}
			
			boolean isCtorWithTemplateID= false;
			if (lastIsID && ns.length > 1) {
				IASTName secondLastName= ns[ns.length-2];
				if (secondLastName instanceof ICPPASTTemplateId) {
					final char[] lastNamesLookupKey = lastName.getLookupKey();
					if (CharArrayUtils.equals(lastNamesLookupKey, ((ICPPASTTemplateId) secondLastName).getLookupKey()) ||
							(lastNamesLookupKey.length > 0 && lastNamesLookupKey[0] == '~')) {
						isCtorWithTemplateID= true;
						idcount--;
					} 
				}
			}
			
			if (lastIsID && !isCtorWithTemplateID) {
				missingTemplateDecls= idcount-tdeclcount;
			} else {
				missingTemplateDecls= idcount+1-tdeclcount;
				if (missingTemplateDecls > 0) {
					// last name is probably not a template
					missingTemplateDecls--;
					lastIsTemplate= false;
					CharArraySet tparnames= collectTemplateParameterNames(outerMostTDecl);
					int j= 0;
					for (IASTName n : ns) {
						if (n instanceof ICPPASTTemplateId) {
							// if we find a dependent id, there can be no explicit specialization.
							ICPPASTTemplateId id= (ICPPASTTemplateId) n;
							if (usesTemplateParameter(id, tparnames))
								break;

							if (j++ == missingTemplateDecls) {
								IBinding b= n.resolveBinding();
								if (b instanceof ICPPTemplateInstance && b instanceof ICPPClassType) {
									if (((ICPPTemplateInstance) b).isExplicitSpecialization()) {
										// For a template-id of an explicit specialization. 
										// we don't have a template declaration. (see 14.7.3.5)
										missingTemplateDecls++;
										lastIsTemplate= true;
									}
								}
								break;
							}
						}
					}
				}
			}
		}
		
		if (missingTemplateDecls < 0) {
			missingTemplateDecls= 0; // too many template declarations
		}
		
		// determine nesting level of parent
		int level= missingTemplateDecls;
		if (!CPPVisitor.isFriendFunctionDeclaration(innerMostTDecl.getDeclaration())) {
			node= outerMostTDecl.getParent();
			while (node != null) {
				if (node instanceof ICPPASTInternalTemplateDeclaration) {
					level+= ((ICPPASTInternalTemplateDeclaration) node).getNestingLevel() + 1;
					break;
				}
				node= node.getParent();
			}
		}
		
		tdecl= outerMostTDecl;
		while(true) {
			tdecl.setNestingLevel((short) level++);
			tdecl.setAssociatedWithLastName(false);
			node= tdecl.getDeclaration();
			if (node instanceof ICPPASTInternalTemplateDeclaration) {
				tdecl= (ICPPASTInternalTemplateDeclaration) node;
			} else {
				break;
			}
		}
		innerMostTDecl.setAssociatedWithLastName(lastIsTemplate);
	}

	private static CharArraySet collectTemplateParameterNames(ICPPASTTemplateDeclaration tdecl) {
		CharArraySet set= new CharArraySet(4);
		while(true) {
			ICPPASTTemplateParameter[] pars = tdecl.getTemplateParameters();
			for (ICPPASTTemplateParameter par : pars) {
				IASTName name= CPPTemplates.getTemplateParameterName(par);
				if (name != null)
					set.put(name.getLookupKey());
			}
			final IASTNode next= tdecl.getDeclaration();
			if (next instanceof ICPPASTTemplateDeclaration) {
				tdecl= (ICPPASTTemplateDeclaration) next;
			} else {
				break;
			}
		}
		return set;
	}

	private static boolean usesTemplateParameter(final ICPPASTTemplateId id, final CharArraySet names) {
		final boolean[] result= {false};
		ASTVisitor v= new ASTVisitor(false) {
			{ shouldVisitNames= true; shouldVisitAmbiguousNodes=true;}
			@Override
			public int visit(IASTName name) {
				if (name instanceof ICPPASTTemplateId)
					return PROCESS_CONTINUE;
				if (name instanceof ICPPASTQualifiedName) {
					ICPPASTQualifiedName qname= (ICPPASTQualifiedName) name;
					if (qname.isFullyQualified())
						return PROCESS_SKIP;
					return PROCESS_CONTINUE;
				}
				
				if (names.containsKey(name.getLookupKey())) {
					IASTNode parent= name.getParent();
					if (parent instanceof ICPPASTQualifiedName) {
						if (((ICPPASTQualifiedName) parent).getNames()[0] != name) {
							return PROCESS_CONTINUE;
						}
						result[0]= true;
						return PROCESS_ABORT;
					} else if (parent instanceof IASTIdExpression ||
							parent instanceof ICPPASTNamedTypeSpecifier) {
						result[0]= true;
						return PROCESS_ABORT;
					}
				}
				return PROCESS_CONTINUE;
			}
			@Override
			public int visit(ASTAmbiguousNode node) {
				IASTNode[] alternatives= node.getNodes();
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
		IASTName name= null;
		if (decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl= (IASTSimpleDeclaration) decl;
			IASTDeclarator[] dtors = sdecl.getDeclarators();
			if (dtors != null && dtors.length > 0) {
				name= ASTQueries.findInnermostDeclarator(dtors[0]).getName();
			} else {
				IASTDeclSpecifier declspec = sdecl.getDeclSpecifier();
				if (declspec instanceof IASTCompositeTypeSpecifier) {
					name= ((IASTCompositeTypeSpecifier) declspec).getName();
				} else if (declspec instanceof IASTElaboratedTypeSpecifier) {
					name= ((IASTElaboratedTypeSpecifier) declspec).getName();
				}
			} 
		} else if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fdef= (IASTFunctionDefinition) decl;
			name= ASTQueries.findInnermostDeclarator(fdef.getDeclarator()).getName();
		}
		return name;
	}
	

	private static ICPPASTInternalTemplateDeclaration getInnerTemplateDeclaration(final IASTName name) {
		IASTNode parent = name.getParent();
		while (parent instanceof IASTName) {
		    parent = parent.getParent();
		}
		if (parent instanceof IASTDeclSpecifier) {
			if (!(parent instanceof IASTCompositeTypeSpecifier) &&
					!(parent instanceof IASTElaboratedTypeSpecifier)) {
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
			ICPPASTInternalTemplateDeclaration tdecl ) {
		final IASTNode parent= tdecl.getParent();
		if (parent instanceof ICPPASTInternalTemplateDeclaration) 
			return (ICPPASTInternalTemplateDeclaration) parent;
		
		return null;
	}

	public static IASTName getTemplateName(ICPPASTTemplateDeclaration templateDecl) {
	    if (templateDecl == null) return null;

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
		        if (spec instanceof ICPPASTCompositeTypeSpecifier)
		            name = ((ICPPASTCompositeTypeSpecifier) spec).getName();
		        else if (spec instanceof ICPPASTElaboratedTypeSpecifier)
		            name = ((ICPPASTElaboratedTypeSpecifier) spec).getName();
		    }
		} else if (nestedDecl instanceof IASTFunctionDefinition) {
		    IASTDeclarator declarator = ((IASTFunctionDefinition) nestedDecl).getDeclarator();
		    declarator= ASTQueries.findInnermostDeclarator(declarator);
			name = declarator.getName();
		}
		if (name != null) {
		    if (name instanceof ICPPASTQualifiedName) {
				IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
				IASTDeclaration currDecl = decl;
				for (int j = 0; j < ns.length; j++) {
					if (ns[j] instanceof ICPPASTTemplateId || j + 1 == ns.length) {
						if (currDecl == templateDecl) {
							return ns[j];
						}
						if (currDecl instanceof ICPPASTTemplateDeclaration) {
							currDecl = ((ICPPASTTemplateDeclaration) currDecl).getDeclaration();
						} else {
							return null;
						}
					}
				}
		    } else {
		        return name;
		    }
		}

		return  null;
	}

	public static boolean areSameArguments(ICPPTemplateArgument[] args, ICPPTemplateArgument[] specArgs) {
		if (args.length != specArgs.length) {
			return false;
		}
		for (int i=0; i < args.length; i++) {
			if (!specArgs[i].isSameValue(args[i])) 
				return false;
		}
		return true;
	}
	
	/**
	 * @param id the template id containing the template arguments
	 * @return an array of template arguments, currently modeled as IType objects. The
	 * empty IType array is returned if id is <code>null</code>
	 */
	public static ICPPTemplateArgument[] createTemplateArgumentArray(ICPPASTTemplateId id) {
		ICPPTemplateArgument[] result= ICPPTemplateArgument.EMPTY_ARGUMENTS;
		if (id != null) {
			IASTNode[] args= id.getTemplateArguments();
			result = new ICPPTemplateArgument[args.length];
			for (int i = 0; i < args.length; i++) {
				IASTNode arg= args[i];
				if (arg instanceof IASTTypeId) {
					result[i]= new CPPTemplateArgument(CPPVisitor.createType((IASTTypeId) arg));
				} else if (arg instanceof IASTExpression) {
					IASTExpression expr= (IASTExpression) arg;
					IType type= expr.getExpressionType();
					IValue value= Value.create((IASTExpression) arg, Value.MAX_RECURSION_DEPTH);
					result[i]= new CPPTemplateArgument(value, type);
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
		return result;
	}
	
	static ICPPFunction[] instantiateForFunctionCall(IASTName name, ICPPFunction[] fns,
			List<IType> fnArgs, List<ValueCategory> argCats, boolean withImpliedObjectArg) {
		if (name != null && name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			name= (IASTName) name.getParent();
		}

		// Extract template arguments.
		ICPPTemplateArgument[] tmplArgs= ICPPTemplateArgument.EMPTY_ARGUMENTS;
		boolean requireTemplate= name instanceof ICPPASTTemplateId;
		boolean haveTemplate= false;
		
		for (final ICPPFunction func : fns) {
			if (func instanceof ICPPConstructor || (func instanceof ICPPMethod && ((ICPPMethod) func).isDestructor()))
				requireTemplate= false;

			if (func instanceof ICPPFunctionTemplate) {
				ICPPFunctionTemplate template= (ICPPFunctionTemplate) func;
				try {
					if (containsDependentType(fnArgs)) 
						return new ICPPFunction[] {CPPUnknownFunction.createForSample(template)};

					if (requireTemplate) {
						tmplArgs = createTemplateArgumentArray((ICPPASTTemplateId) name);
						if (hasDependentArgument(tmplArgs)) 
							return new ICPPFunction[] {CPPUnknownFunction.createForSample(template)};
					}
				} catch (DOMException e) {
					return NO_FUNCTIONS;
				}
				haveTemplate= true;
				break;
			}
		}

		if (!haveTemplate && !requireTemplate) 
			return fns;
		
		final List<ICPPFunction> result= new ArrayList<ICPPFunction>(fns.length);
		for (ICPPFunction fn : fns) {
			if (fn != null) {
				if (fn instanceof ICPPFunctionTemplate) {
					ICPPFunctionTemplate fnTmpl= (ICPPFunctionTemplate) fn;
					ICPPFunction inst = instantiateForFunctionCall(fnTmpl, tmplArgs, fnArgs, argCats, withImpliedObjectArg);
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
			fnArgs= fnArgs.subList(1, fnArgs.size());
			argCats= argCats.subList(1, argCats.size());
		}
		
		CPPTemplateParameterMap map= new CPPTemplateParameterMap(fnArgs.size());
		try {
			ICPPTemplateArgument[] args= TemplateArgumentDeduction.deduceForFunctionCall(template, tmplArgs, fnArgs, argCats, map);
			if (args != null) {
				IBinding instance= instantiateFunctionTemplate(template, args, map);
				if (instance instanceof ICPPFunction) {
					return (ICPPFunction) instance;
				} 
			}
		} catch (DOMException e) {
		}
		return null;
	}

	static void instantiateConversionTemplates(IFunction[] functions, IType conversionType) {
		boolean checkedForDependentType= false;
		for (int i = 0; i < functions.length; i++) {
			IFunction func = functions[i];
			if (func instanceof ICPPFunctionTemplate) {
				ICPPFunctionTemplate template= (ICPPFunctionTemplate) func;
				functions[i]= null;
				
				// extract template arguments and parameter types.
				if (!checkedForDependentType) {
					try {
						if (isDependentType(conversionType)) {
							functions[i]= CPPUnknownFunction.createForSample(template);
							return;
						}
						checkedForDependentType= true;
					} catch (DOMException e) {
						return;
					}
				}
				CPPTemplateParameterMap map= new CPPTemplateParameterMap(1);
				try {
					ICPPTemplateArgument[] args= TemplateArgumentDeduction.deduceForConversion(template, conversionType, map);
					if (args != null) {
						IBinding instance= instantiateFunctionTemplate(template, args, map);
						if (instance instanceof IFunction) {
							functions[i]= (IFunction) instance;
						} 
					}
				} catch (DOMException e) {
					// try next candidate
				}
			} 
		}
	}

	/**
	 * 14.8.2.2 Deducing template arguments taking the address of a function template [temp.deduct.funcaddr]
	 */
	static ICPPFunction instantiateForAddressOfFunction(ICPPFunctionTemplate template, IFunctionType target, IASTName name) {
		if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			name= (IASTName) name.getParent();
		}
		try {
			if (target != null && isDependentType(target)) {
				return CPPUnknownFunction.createForSample(template);
			}

			ICPPTemplateArgument[] tmplArgs;
			if (name instanceof ICPPASTTemplateId && !(template instanceof ICPPConstructor)) {
				tmplArgs = createTemplateArgumentArray((ICPPASTTemplateId) name);
				if (hasDependentArgument(tmplArgs)) {
					return CPPUnknownFunction.createForSample(template);
				}
			} else {
				tmplArgs= ICPPTemplateArgument.EMPTY_ARGUMENTS;
			}

			CPPTemplateParameterMap map= new CPPTemplateParameterMap(4);
			ICPPTemplateArgument[] args= TemplateArgumentDeduction.deduceForAddressOf(template, tmplArgs, target, map);
			if (args != null) {
				IBinding instance= instantiateFunctionTemplate(template, args, map);
				if (instance instanceof ICPPFunction) {
					return (ICPPFunction) instance;
				} 
			}
		} catch (DOMException e) {
		}
		return null;
	}

	// 14.5.6.2 Partial ordering of function templates
	static int orderFunctionTemplates(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2, TypeSelection mode)
			throws DOMException {
		int s1 = compareSpecialization(f1, f2, mode);
		int s2 = compareSpecialization(f2, f1, mode);
		
		if (s1 == s2)
			return 0;
		if (s1 < 0 || s2 > 0)
			return -1;
		assert s2 < 0 || s1 > 0;
		return 1;
	}

	private static ICPPFunction transferFunctionTemplate(ICPPFunctionTemplate f) throws DOMException {
		final ICPPTemplateParameter[] tpars = f.getTemplateParameters();
		final int argLen = tpars.length;
		
		// Create arguments and map
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[argLen];
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(argLen);
		for (int i = 0; i < argLen; i++) {
			final ICPPTemplateParameter tpar = tpars[i];
			final CPPTemplateArgument arg = uniqueArg(tpar);
			args[i]= arg;
			if (tpar.isParameterPack()) {
				map.put(tpar, new ICPPTemplateArgument[] {arg});
			} else { 
				map.put(tpar, arg);
			}
		}

		IBinding result = instantiateFunctionTemplate(f, args, map);
		if (result instanceof ICPPFunction)
			return (ICPPFunction) result;
		
		return null;
	}

	private static CPPTemplateArgument uniqueArg(final ICPPTemplateParameter tpar) throws DOMException {
		final CPPTemplateArgument arg; 
		if (tpar instanceof ICPPTemplateNonTypeParameter) {
			arg = new CPPTemplateArgument(Value.unique(), ((ICPPTemplateNonTypeParameter) tpar).getType());
		} else {
			arg = new CPPTemplateArgument(new UniqueType(tpar.isParameterPack()));
		}
		return arg;
	}

	private static int compareSpecialization(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2, TypeSelection mode) throws DOMException {
		ICPPFunction transF1 = transferFunctionTemplate(f1);
		if (transF1 == null)
			return -1;

		final ICPPFunctionType ft2 = f2.getType();
		final ICPPFunctionType transFt1 = transF1.getType();
		IType[] pars;
		IType[] args;
		switch(mode) {
		case RETURN_TYPE:
			pars= new IType[] {ft2.getReturnType()};
			args= new IType[] {transFt1.getReturnType()};
			break;
		case PARAMETERS_AND_RETURN_TYPE:
			pars= concat(ft2.getReturnType(), ft2.getParameterTypes());
			args= concat(transFt1.getReturnType(), transFt1.getParameterTypes());
			break;
		case PARAMETERS:
		default:
			pars= ft2.getParameterTypes();
			args = transFt1.getParameterTypes();
			boolean nonStaticMember1= isNonStaticMember(f1);
			boolean nonStaticMember2= isNonStaticMember(f2);
			if (nonStaticMember1 != nonStaticMember2) {
				if (nonStaticMember1) {
					args= addImplicitParameterType(args, (ICPPMethod) f1);
				} else {
					pars= addImplicitParameterType(pars, (ICPPMethod) f2);
				}
			}
			break;
		}
		return TemplateArgumentDeduction.deduceForPartialOrdering(f2.getTemplateParameters(), pars, args);
	}

	private static boolean isNonStaticMember(ICPPFunctionTemplate f) {
		return (f instanceof ICPPMethod) && !((ICPPMethod) f).isStatic();
	}

	private static IType[] addImplicitParameterType(IType[] types, ICPPMethod m) {
		try {
			IType t= CPPSemantics.getImplicitParameterType(m);
			return concat(t, types);
		} catch (DOMException e) {
			return types;
		}
	}

	private static IType[] concat(final IType t, IType[] types) {
		IType[] result= new IType[types.length+1];
		result[0]= t;
		System.arraycopy(types, 0, result, 1, types.length);
		return result;
	}

	private static ICPPClassTemplatePartialSpecialization findPartialSpecialization(ICPPClassTemplate ct,
			ICPPTemplateArgument[] args) throws DOMException {
		ICPPClassTemplatePartialSpecialization[] pspecs = ct.getPartialSpecializations();
		if (pspecs != null && pspecs.length > 0) {
			final String argStr= ASTTypeUtil.getArgumentListString(args, true);
			for (ICPPClassTemplatePartialSpecialization pspec : pspecs) {
				try {
					if (argStr.equals(ASTTypeUtil.getArgumentListString(pspec.getTemplateArguments(), true)))
						return pspec;
				} catch (DOMException e) {
					// ignore partial specializations with problems
				}
			}
		}
		return null;
	}

	private static IBinding selectSpecialization(ICPPClassTemplate template, ICPPTemplateArgument[] args,
			boolean isDef) throws DOMException {
		if (template == null) {
			return null;
		}
		ICPPClassTemplatePartialSpecialization[] specializations = template.getPartialSpecializations();
		if (specializations == null || specializations.length == 0) {
			return null;
		}

		ICPPClassTemplatePartialSpecialization bestMatch = null, spec = null;
		CPPTemplateParameterMap bestMap= null;
		boolean bestMatchIsBest = true;
		for (ICPPClassTemplatePartialSpecialization specialization : specializations) {
			spec = specialization;
			final CPPTemplateParameterMap map = new CPPTemplateParameterMap(args.length);
			if (TemplateArgumentDeduction.fromTemplateArguments(spec.getTemplateParameters(), spec.getTemplateArguments(), args, map)) {
				int compare = orderSpecializations(bestMatch, spec);
				if (compare == 0) {
					bestMatchIsBest = false;
				} else if (compare < 0) {
					bestMatch = spec;
					bestMap= map;
					bestMatchIsBest = true;
				}
			}
		}

		//14.5.4.1 If none of the specializations is more specialized than all the other matching
		//specializations, then the use of the class template is ambiguous and the program is ill-formed.
		if (!bestMatchIsBest) {
			return new CPPTemplateDefinition.CPPTemplateProblem(null, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, null);
		}

		if (bestMatch == null)
			return null;
		
		return instantiatePartialSpecialization(bestMatch, args, isDef, bestMap);
	}

	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * @param spec1
	 * @param spec2
	 * @return
	 * @throws DOMException
	 */
	static private int orderSpecializations(ICPPClassTemplatePartialSpecialization spec1, ICPPClassTemplatePartialSpecialization spec2) throws DOMException {
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

	private static boolean isAtLeastAsSpecializedAs(ICPPClassTemplatePartialSpecialization f1, ICPPClassTemplatePartialSpecialization f2) throws DOMException {
		// 14.5.5.2
		// Using the transformed parameter list, perform argument deduction against the other
		// function template
		// The transformed template is at least as specialized as the other if and only if the deduction
		// succeeds and the deduced parameter types are an exact match.
		final ICPPTemplateParameter[] tpars1 = f1.getTemplateParameters();
		final ICPPTemplateParameter[] tpars2 = f2.getTemplateParameters();
		final ICPPTemplateArgument[] targs1 = f1.getTemplateArguments();
		final ICPPTemplateArgument[] targs2 = f2.getTemplateArguments();
		if (targs1.length != targs2.length)
			return false;
		
		// Transfer arguments of specialization 1
		final int tpars1Len = tpars1.length;
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[tpars1Len];
		final CPPTemplateParameterMap transferMap= new CPPTemplateParameterMap(tpars1Len);
		for (int i = 0; i < tpars1Len; i++) {
			final ICPPTemplateParameter param = tpars1[i];
			final CPPTemplateArgument arg = uniqueArg(param);
			args[i]= arg;
			transferMap.put(param, arg);
		}
		final ICPPTemplateArgument[] transferredArgs1 = instantiateArguments(targs1, transferMap, -1, null);
		
		// Deduce arguments for specialization 2
		final CPPTemplateParameterMap deductionMap= new CPPTemplateParameterMap(2);
		if (!TemplateArgumentDeduction.fromTemplateArguments(tpars2, targs2, transferredArgs1, deductionMap))
			return false;
		
		// Compare
		for (int i = 0; i < targs2.length; i++) {
			ICPPTemplateArgument transferredArg2= instantiateArgument(targs2[i], deductionMap, -1, null);
			if (!transferredArg2.isSameValue(transferredArgs1[i]))
				return false;
		}
		return true;
	}

	static boolean isValidType(IType t) {
		while (t instanceof ITypeContainer) {
			t = ((ITypeContainer) t).getType();
		}
		return !(t instanceof ISemanticProblem);
	}
	
	static boolean isValidArgument(ICPPTemplateArgument arg) {
		return arg != null && isValidType(arg.isTypeValue() ? arg.getTypeValue() : arg.getTypeOfNonTypeValue());
	}

	static ICPPTemplateArgument matchTemplateParameterAndArgument(ICPPTemplateParameter param, 
			ICPPTemplateArgument arg, CPPTemplateParameterMap map) {
		if (arg == null || !isValidType(arg.getTypeValue())) {
			return null;
		}
		if (param instanceof ICPPTemplateTypeParameter) {
			IType t= arg.getTypeValue();
			if (t != null && ! (t instanceof ICPPTemplateDefinition))
				return arg;
			return null;
		} 
		
		if (param instanceof ICPPTemplateTemplateParameter) {
			IType t= arg.getTypeValue();
			if (!(t instanceof ICPPTemplateDefinition))
				return null;

			ICPPTemplateParameter[] pParams = null;
			ICPPTemplateParameter[] aParams = null;
			try {
				pParams = ((ICPPTemplateTemplateParameter) param).getTemplateParameters();
				aParams = ((ICPPTemplateDefinition) t).getTemplateParameters();
				if (!compareTemplateParameters(pParams, aParams))
					return null;
			} catch (DOMException e) {
				return null;
			}

			return arg;
		} 
		
		if (param instanceof ICPPTemplateNonTypeParameter) {
			if (!arg.isNonTypeValue())
				return null;
			IType argType= arg.getTypeOfNonTypeValue();
			try {
				IType pType = ((ICPPTemplateNonTypeParameter) param).getType();
				if (pType instanceof ICPPParameterPackType) {
					pType= ((ICPPParameterPackType) pType).getType();
				}
				if (map != null && pType != null) {
					pType= instantiateType(pType, map, -1, null);
				}
				if (argType instanceof ICPPUnknownType || argType instanceof ISemanticProblem || isNonTypeArgumentConvertible(pType, argType)) {
					return new CPPTemplateArgument(arg.getNonTypeValue(), pType);
				}
				return null;
				
			} catch (DOMException e) {
				return null;
			}
		}
		assert false;
		return null;
	}
	
	private static boolean compareTemplateParameters(ICPPTemplateParameter[] params1,
			ICPPTemplateParameter[] params2) throws DOMException {
		int size = params1.length;
		if (params2.length != size) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			final ICPPTemplateParameter p1 = params1[i];
			final ICPPTemplateParameter p2 = params2[i];
			if (p1.isParameterPack() != p2.isParameterPack())
				return false;
			
			boolean pb= p1 instanceof ICPPTemplateTypeParameter;
			boolean ab= p2 instanceof ICPPTemplateTypeParameter;
			if (pb != ab)
				return false;
			
			if (pb) {
				// Both are template type parameters
			} else {
				pb= p1 instanceof ICPPTemplateNonTypeParameter;
				ab= p2 instanceof ICPPTemplateNonTypeParameter;
				if (pb != ab)
					return false;

				if (pb) {
					// Both are non-type parameters
				} else {
					if (!(p1 instanceof ICPPTemplateTemplateParameter) ||
							!(p2 instanceof ICPPTemplateTemplateParameter)) {
						assert false;
						return false;
					}
			
					if (!compareTemplateParameters(((ICPPTemplateTemplateParameter) p1).getTemplateParameters(),
					((ICPPTemplateTemplateParameter) p2).getTemplateParameters()) )
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns whether the template argument <code>arg</code> can be converted to
	 * the same type as <code>paramType</code> using the rules specified in 14.3.2.5.
	 * @param paramType
	 * @param arg
	 * @return
	 * @throws DOMException
	 */
	private static boolean isNonTypeArgumentConvertible(IType paramType, IType arg) throws DOMException {
		//14.1s8 function to pointer and array to pointer conversions
		if (paramType instanceof IFunctionType) {
			paramType = new CPPPointerType(paramType);
	    } else if (paramType instanceof IArrayType) {
	    	paramType = new CPPPointerType(((IArrayType) paramType).getType());
		}
		Cost cost = Conversions.checkImplicitConversionSequence(paramType, arg, LVALUE, UDCMode.FORBIDDEN, Context.ORDINARY);
		return cost != null && cost.converts();
	}

	static boolean argsAreTrivial(ICPPTemplateParameter[] pars, ICPPTemplateArgument[] args) {
		if (pars.length != args.length) {
			return false;
		}
		for (int i = 0; i < args.length; i++) {
			ICPPTemplateParameter par= pars[i];
			ICPPTemplateArgument arg = args[i];
			if (par instanceof IType) {
				if (arg.isNonTypeValue())
					return false;
				IType argType= arg.getTypeValue();
				if (argType == null)
					return false;
				if (par.isParameterPack()) {
					if (!(argType instanceof ICPPParameterPackType))
						return false;
					argType= ((ICPPParameterPackType) argType).getType();
					if (argType == null)
						return false;
				}
				if (!argType.isSameType((IType) par)) 
					return false;
			} else {
				if (arg.isTypeValue())
					return false;
				int parpos= Value.isTemplateParameter(arg.getNonTypeValue());
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
		
		return Value.isDependentValue(arg.getNonTypeValue());
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
				t= ft.getReturnType();
			} else if (t instanceof ICPPPointerToMemberType) {
				ICPPPointerToMemberType ptmt= (ICPPPointerToMemberType) t;
				if (isDependentType(ptmt.getMemberOfClass()))
					return true;
				t= ptmt.getType();
			} else if (t instanceof ICPPParameterPackType) {
				return true;
			} else if (t instanceof ITypeContainer) {
				if (t instanceof IArrayType) {
					IValue asize= ((IArrayType) t).getSize();
					if (asize != null && Value.isDependentValue(asize))
						return true;
				}
				t= ((ITypeContainer) t).getType();
			} else if (t instanceof InitializerListType) {
				return isDependentInitializerList(((InitializerListType) t).getInitializerList());
			} else {
				return false;
			}
		}
	}
	
	private static boolean isDependentInitializerList(ICPPASTInitializerList initializerList) {
		IASTInitializerClause[] clauses= initializerList.getClauses();
		for (IASTInitializerClause clause : clauses) {
			if (clause instanceof IASTExpression) {
				IType t= ((IASTExpression) clause).getExpressionType();
				if (isDependentType(t))
					return true;
			} else if (clause instanceof ICPPASTInitializerList) {
				if (isDependentInitializerList((ICPPASTInitializerList) clause))
					return true;
			}
		}
		return false;
	}

	public static boolean containsDependentArg(ObjectMap tpMap) {
		for (Object arg : tpMap.valueArray()) {
			if (isDependentType((IType)arg))
				return true;
		}
		return false;
	}

	/**
	 * Attempts to (partially) resolve an unknown binding with the given arguments.
	 */
	private static IBinding resolveUnknown(ICPPUnknownBinding unknown, ICPPTemplateParameterMap tpMap,
			int packOffset, ICPPClassSpecialization within) throws DOMException {
        if (unknown instanceof ICPPDeferredClassInstance) {
        	return resolveDeferredClassInstance((ICPPDeferredClassInstance) unknown, tpMap, packOffset, within);
        }

        final IBinding owner= unknown.getOwner();
        if (!(owner instanceof ICPPTemplateTypeParameter || owner instanceof ICPPUnknownClassType))
        	return unknown;
        
        IBinding result = unknown;
        IType t = CPPTemplates.instantiateType((IType) owner, tpMap, packOffset, within);
        if (t != null) {
            t = SemanticUtil.getUltimateType(t, false);
            if (t instanceof ICPPUnknownBinding) {
            	if (unknown instanceof ICPPUnknownClassInstance) {
            		ICPPUnknownClassInstance ucli= (ICPPUnknownClassInstance) unknown;
            		final ICPPTemplateArgument[] arguments = ucli.getArguments();
            		ICPPTemplateArgument[] newArgs = CPPTemplates.instantiateArguments(arguments, tpMap, packOffset, within);
            		if (!t.equals(owner) && newArgs != arguments) {
            			result= new CPPUnknownClassInstance((ICPPUnknownBinding) t, ucli.getNameCharArray(), newArgs);
            		}
            	} else if (!t.equals(owner)) {
            		if (unknown instanceof ICPPUnknownClassType) {
            			result= new CPPUnknownClass((ICPPUnknownBinding)t, unknown.getNameCharArray());
            		} else if (unknown instanceof IFunction) {
            			result= new CPPUnknownClass((ICPPUnknownBinding)t, unknown.getNameCharArray());
            		} else {
            			result= new CPPUnknownBinding((ICPPUnknownBinding) t, unknown.getNameCharArray());
            		}
            	} 
            } else if (t instanceof ICPPClassType) {
	            IScope s = ((ICPPClassType) t).getCompositeScope();
	            if (s != null) {
	            	result= CPPSemantics.resolveUnknownName(s, unknown);
	            	if (unknown instanceof ICPPUnknownClassInstance && result instanceof ICPPTemplateDefinition) {
	            		ICPPTemplateArgument[] newArgs = CPPTemplates.instantiateArguments(
	            				((ICPPUnknownClassInstance) unknown).getArguments(), tpMap, packOffset, within);
	            		if (result instanceof ICPPClassTemplate) {
	            			result = instantiate((ICPPClassTemplate) result, newArgs, false);
	            		}
	            	}
	            }
            }
        }
        
        return result;
	}

	private static IBinding resolveDeferredClassInstance(ICPPDeferredClassInstance dci,
			ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within) {
		ICPPTemplateArgument[] arguments = dci.getTemplateArguments();
		ICPPTemplateArgument[] newArgs;
		try {
			newArgs = CPPTemplates.instantiateArguments(arguments, tpMap, packOffset, within);
		} catch (DOMException e) {
			return e.getProblem();
		}

		boolean changed= arguments != newArgs;
		ICPPClassTemplate classTemplate = dci.getClassTemplate();
		IType classTemplateSpecialization= instantiateType(classTemplate, tpMap, packOffset, within);
		if (classTemplateSpecialization != classTemplate && classTemplateSpecialization instanceof ICPPClassTemplate) {
			classTemplate= (ICPPClassTemplate) classTemplateSpecialization;
			changed= true;
		}

		if (changed) {
			IBinding inst= instantiate(classTemplate, newArgs, false);
			if (inst != null)
				return inst;
		}
		return dci;
	}

	public static boolean haveSameArguments(ICPPTemplateInstance i1, ICPPTemplateInstance i2) {
		final ICPPTemplateArgument[] m1= i1.getTemplateArguments();
		final ICPPTemplateArgument[] m2= i2.getTemplateArguments();
		
		if (m1 == null || m2 == null || m1.length != m2.length)
			return false;

		String s1 = ASTTypeUtil.getArgumentListString(m1, true);
		String s2 = ASTTypeUtil.getArgumentListString(m2, true);
		return s1.equals(s2);
	}

	public static ICPPTemplateParameterMap createParameterMap(ICPPTemplateDefinition tdef, ICPPTemplateArgument[] args) {
		try {
			ICPPTemplateParameter[] tpars= tdef.getTemplateParameters();
			int len= Math.min(tpars.length, args.length);
			CPPTemplateParameterMap result= new CPPTemplateParameterMap(len);
			for (int i = 0; i < len; i++) {
				result.put(tpars[i], args[i]);
			}
			return result;
		} catch (DOMException e) {
			return CPPTemplateParameterMap.EMPTY;
		}
	}

	/**
	 * @deprecated for backwards compatibility, only.
	 */
	@Deprecated
	public static IType[] getArguments(ICPPTemplateArgument[] arguments) {
		IType[] types= new IType[arguments.length];
		for (int i = 0; i < types.length; i++) {
			final ICPPTemplateArgument arg= arguments[i];
			if (arg == null) {
				types[i]= null;
			} else if (arg.isNonTypeValue()) {
				types[i]= arg.getTypeOfNonTypeValue();
			} else {
				types[i]= arg.getTypeValue();
			}
		}
		return types;
	}
		
	/**
	 * @deprecated for backwards compatibility, only.
	 */
	@Deprecated
	public static ObjectMap getArgumentMap(IBinding b, ICPPTemplateParameterMap tpmap) {
		// backwards compatibility
		Integer[] keys= tpmap.getAllParameterPositions();
		if (keys.length == 0)
			return ObjectMap.EMPTY_MAP;
		
		try {
			List<ICPPTemplateDefinition> defs= new ArrayList<ICPPTemplateDefinition>();
			IBinding owner= b;
			while (owner != null) {
				if (owner instanceof ICPPTemplateDefinition) {
					defs.add((ICPPTemplateDefinition) owner);
				} else if (owner instanceof ICPPTemplateInstance) {
					defs.add(((ICPPTemplateInstance) owner).getTemplateDefinition());
				}
				owner= owner.getOwner();
			}
			Collections.reverse(defs);

			ObjectMap result= new ObjectMap(keys.length);
			for (int key: keys) {
				int nestingLevel= key >> 16;
				int numParam= key & 0xffff;

				if (0 <= numParam && 0 <= nestingLevel && nestingLevel < defs.size()) {
					ICPPTemplateDefinition tdef= defs.get(nestingLevel);
					ICPPTemplateParameter[] tps= tdef.getTemplateParameters();
					if (numParam < tps.length) {
						ICPPTemplateArgument arg= tpmap.getArgument(key);
						if (arg != null) {
							IType type= arg.isNonTypeValue() ? arg.getTypeOfNonTypeValue() : arg.getTypeValue();
							result.put(tps[numParam], type);
						}
					}
				}
			}
			return result;
		} catch (DOMException e) {
		}
		return ObjectMap.EMPTY_MAP;
	}

	public static IBinding findDeclarationForSpecialization(IBinding binding) {
		while (binding instanceof ICPPSpecialization) {
			if (ASTInternal.hasDeclaration(binding)) 
				return binding;
			
			IBinding original= ((ICPPSpecialization) binding).getSpecializedBinding();
			if (original == null)
				return binding;
			binding= original;
		}
		return binding;
	}
}
