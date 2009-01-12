/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFieldSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;

/**
 * Collection of static methods to perform template instantiation, member specialization and
 * type instantiation.
 */
public class CPPTemplates {

	/**
	 * Instantiates a class template with the given arguments. May return <code>null</code>.
	 */
	public static IBinding instantiate(ICPPClassTemplate template, ICPPTemplateArgument[] arguments) {
		try {
			arguments= SemanticUtil.getSimplifiedArguments(arguments);
			if (template instanceof ICPPTemplateTemplateParameter || hasDependentArgument(arguments)) {
				return deferredInstance(template, arguments);
			}
			
			if (template instanceof ICPPClassTemplatePartialSpecialization) {
				return instantiatePartialSpecialization((ICPPClassTemplatePartialSpecialization) template, arguments);
			}
		
			// check whether we need to use default arguments
			final ICPPTemplateParameter[] parameters= template.getTemplateParameters();
			final int numArgs = arguments.length;
			final int numParams= parameters.length;
			if (numParams == 0 || numParams < numArgs) 
				return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);

			ICPPTemplateArgument[] completeArgs= new ICPPTemplateArgument[numParams];
			CPPTemplateParameterMap map= new CPPTemplateParameterMap(numParams);
				
			boolean hasDependentDefaultArg= false;
			for (int i = 0; i < numParams; i++) {
				ICPPTemplateArgument arg;
				ICPPTemplateParameter param= parameters[i];
				if (i < numArgs) {
					arg= arguments[i];
				} else {
					ICPPTemplateArgument defaultArg= param.getDefaultValue();
					if (defaultArg == null) {
						return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
					}
					arg= instantiateArgument(defaultArg, map, null);
					arg= SemanticUtil.getSimplifiedArgument(arg);
					hasDependentDefaultArg |= isDependentArgument(arg);
				}
				
				if (!hasDependentDefaultArg) {
					arg= CPPTemplates.matchTemplateParameterAndArgument(param, arg, map);
					if (arg == null)
						return createProblem(template, IProblemBinding.SEMANTIC_INVALID_TEMPLATE_ARGUMENTS);
				}
				
				map.put(param, arg);
				completeArgs[i]= arg;
			}

			if (hasDependentDefaultArg)
				return deferredInstance(template, completeArgs);


			ICPPTemplateDefinition tdef = CPPTemplates.selectSpecialization(template, completeArgs);
			if (tdef == null || tdef instanceof IProblemBinding) 
				return tdef;

			if (tdef instanceof ICPPClassTemplatePartialSpecialization) {
				return instantiatePartialSpecialization((ICPPClassTemplatePartialSpecialization) tdef, completeArgs);
			}

			return instantiatePrimaryTemplate(template, completeArgs, map);	
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
				start= CPPVisitor.findInnermostDeclarator(func.getDeclarator()).getName();
				start= start.getLastName();
			}

			IScope scope= CPPVisitor.getContainingScope(start);
			while (scope instanceof IASTInternalScope) {
				if (scope instanceof IProblemBinding)
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

	/**
	 * Instantiates a partial class template specialization.
	 */
	private static IBinding instantiatePartialSpecialization(ICPPClassTemplatePartialSpecialization partialSpec, ICPPTemplateArgument[] args) throws DOMException {
		ICPPTemplateInstance instance= getInstance(partialSpec, args);
		if (instance != null)
			return instance;

		CPPTemplateParameterMap tpMap= new CPPTemplateParameterMap(args.length);
		if (!CPPTemplates.deduceTemplateParameterMap(partialSpec.getTemplateArguments(), args, tpMap))
			return null;

		ICPPTemplateParameter[] params= partialSpec.getTemplateParameters();
		int numParams = params.length;
		for (int i = 0; i < numParams; i++) {
			final ICPPTemplateParameter param = params[i];
			if (tpMap.getArgument(param) == null)
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
			CPPTemplateParameterMap map) throws DOMException {
		
		assert !(template instanceof ICPPClassTemplatePartialSpecialization);
		ICPPTemplateInstance instance= getInstance(template, arguments);
		if (instance != null) {
			return instance;
		}

		IBinding owner= template.getOwner();
		instance = CPPTemplates.createInstance(owner, template, map, arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	private static IBinding instantiateFunctionTemplate(ICPPFunctionTemplate template, ICPPTemplateArgument[] arguments) 
			throws DOMException {
		ICPPTemplateInstance instance= getInstance(template, arguments);
		if (instance != null) {
			return instance;
		}

		final int length = arguments.length;
		ICPPTemplateParameter[] parameters= template.getTemplateParameters();
		if (parameters.length != length) 
			return null;

		CPPTemplateParameterMap map = new CPPTemplateParameterMap(length);
		for (int i = 0; i < length; i++) {
			map.put(parameters[i], arguments[i]);
		}

		IBinding owner= template.getOwner();
		instance = CPPTemplates.createInstance(owner, template, map, arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	/**
	 * Obtains a cached instance from the template.
	 */
	private static ICPPTemplateInstance getInstance(ICPPTemplateDefinition template, ICPPTemplateArgument[] args) {
		if (template instanceof ICPPInstanceCache) {
			return ((ICPPInstanceCache) template).getInstance(args);
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
		ICPPTemplateInstance instance= getInstance(template, arguments);
		if (instance != null)
			return instance;

		instance = new CPPDeferredClassInstance(template, arguments);
		addInstance(template, arguments, instance);
		return instance;
	}

	/**
	 * Instantiates the template for usage within its own body. May return <code>null</code>.
	 */
	public static ICPPClassType instantiateWithinClassTemplate(ICPPClassTemplate template) throws DOMException {
		if (template instanceof ICPPInternalClassTemplate) {
			return ((ICPPInternalClassTemplate) template).asDeferredInstance();
		}
		
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
				args[i] = new CPPTemplateArgument((IType) tp);
			} else if (tp instanceof ICPPTemplateNonTypeParameter) {
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
			return CPPVisitor.findInnermostDeclarator(((ICPPASTParameterDeclaration) param).getDeclarator()).getName();
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
					dtor= CPPVisitor.findInnermostDeclarator(dtor);
					name = dtor.getName();
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				IASTDeclarator dtor = ((IASTFunctionDefinition) decl).getDeclarator();
				dtor= CPPVisitor.findInnermostDeclarator(dtor);
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

	public static IBinding createBinding(ICPPASTTemplateParameter templateParameter) {
    	if (templateParameter instanceof ICPPASTSimpleTypeTemplateParameter) {
    		return new CPPTemplateTypeParameter(((ICPPASTSimpleTypeTemplateParameter) templateParameter).getName());
    	} 
    	if (templateParameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
        	return new CPPTemplateTemplateParameter(((ICPPASTTemplatedTypeTemplateParameter) templateParameter).getName());
    	}
    	assert templateParameter instanceof ICPPASTParameterDeclaration;
    	final IASTDeclarator dtor = ((ICPPASTParameterDeclaration) templateParameter).getDeclarator();
    	return new CPPTemplateNonTypeParameter(CPPVisitor.findInnermostDeclarator(dtor).getName());
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
			IBinding template = templateName.resolveBinding();

			if (template instanceof ICPPConstructor) {
				template= template.getOwner();
			}
			
			if (template instanceof ICPPUnknownClassType) {
				IBinding owner= template.getOwner();
				if (owner instanceof ICPPUnknownBinding) {
					ICPPTemplateArgument[] args= createTemplateArgumentArray(id);
					return new CPPUnknownClassInstance((ICPPUnknownBinding) template.getOwner(), id, args);
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
								// mstodo how to add partial specialization to class template from index?
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
				result= instantiate(classTemplate, args);
				if (result instanceof ICPPInternalBinding) {
					if (isDecl) {
						((ICPPInternalBinding) result).addDeclaration(id);
					} else if (isDef) {
						((ICPPInternalBinding) result).addDefinition(id);
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

		IASTNode parentOfDeclaration= parentOfName;
		while (parentOfDeclaration != null) {
			if (parentOfDeclaration instanceof IASTDeclaration) {
				parentOfDeclaration= parentOfDeclaration.getParent();
				break;
			}
			parentOfDeclaration= parentOfDeclaration.getParent();
		}

		return  parentOfName instanceof ICPPASTElaboratedTypeSpecifier ||
				parentOfName instanceof ICPPASTCompositeTypeSpecifier ||
				parentOfName instanceof ICPPASTNamedTypeSpecifier || 
				parentOfName instanceof ICPPASTBaseSpecifier;
	}

	/**
	 * Deduce arguments for a template function from the template id + the template function parameters.
	 */
	static private ICPPTemplateArgument[] deduceTemplateFunctionArguments(ICPPFunctionTemplate template,
			ICPPTemplateArgument[] tmplArgs, IType[] fnArgs, CPPTemplateParameterMap map) throws DOMException {
		final ICPPTemplateParameter[] tmplParams = template.getTemplateParameters();
		final int length = tmplParams.length;
		if (tmplArgs.length > length)
			return null;
		
		ICPPTemplateArgument[] result = new ICPPTemplateArgument[length];
		tmplArgs= SemanticUtil.getSimplifiedArguments(tmplArgs);
		for (int i = 0; i < tmplArgs.length; i++) {
			ICPPTemplateArgument tmplArg= tmplArgs[i];
			final ICPPTemplateParameter tmplParam= tmplParams[i];
			tmplArg= matchTemplateParameterAndArgument(tmplParam, tmplArg, map);
			if (tmplArg == null)
				return null;
			
			tmplArg= SemanticUtil.getSimplifiedArgument(tmplArg);
			map.put(tmplParam, tmplArg);
			result[i]= tmplArg;
		}
		
		if (!deduceTemplateParameterMapFromFunctionParameters(template, fnArgs, map)) 
			return null;
		
		for (int i = 0; i < length; i++) {
			if (result[i] == null) {
				ICPPTemplateArgument deducedArg= map.getArgument(tmplParams[i]);
				if (deducedArg == null)
					return null;
				result[i]= deducedArg;
			}
		}
		return result;
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
			instance = new CPPClassInstance(owner, (ICPPClassType) template, tpMap, args);
		} else if (owner instanceof ICPPClassType && template instanceof ICPPMethod) {
			if (template instanceof ICPPConstructor) {
				instance = new CPPConstructorInstance((ICPPClassType) owner, (ICPPConstructor) template, tpMap, args);
			} else {
				instance = new CPPMethodInstance((ICPPClassType) owner, (ICPPMethod) template, tpMap, args);
			}
		} else if (template instanceof ICPPFunction) {
			instance = new CPPFunctionInstance(owner, (ICPPFunction) template, tpMap, args);
		}
		return instance;
	}

	public static ICPPSpecialization createSpecialization(ICPPClassSpecialization owner, IBinding decl, ICPPTemplateParameterMap tpMap) {
		
		// mstodo- specializations of partial specializations
		if (decl instanceof ICPPClassTemplatePartialSpecialization)
			return null;

		ICPPSpecialization spec = null;
		if (decl instanceof ICPPClassTemplate) {
			spec = new CPPClassTemplateSpecialization((ICPPClassTemplate) decl, owner, tpMap);
		} else if (decl instanceof ICPPClassType) {
			spec = new CPPClassSpecialization((ICPPClassType) decl, owner, tpMap);
		} else if (decl instanceof ICPPField) {
			spec = new CPPFieldSpecialization(decl, owner, tpMap);
		} else if (decl instanceof ICPPFunctionTemplate) {
			if (decl instanceof ICPPConstructor)
				spec = new CPPConstructorTemplateSpecialization(decl, owner, tpMap);
			else if (decl instanceof ICPPMethod)
				spec = new CPPMethodTemplateSpecialization(decl, owner, tpMap);
			else
				spec = new CPPFunctionTemplateSpecialization(decl, owner, tpMap);
		} else if (decl instanceof ICPPConstructor) {
			spec = new CPPConstructorSpecialization(decl, owner, tpMap);
		} else if (decl instanceof ICPPMethod) {
			spec = new CPPMethodSpecialization(decl, owner, tpMap);
		} else if (decl instanceof ICPPFunction) {
			spec = new CPPFunctionSpecialization(decl, owner, tpMap);
		} else if (decl instanceof ITypedef) {
		    spec = new CPPTypedefSpecialization(decl, owner, tpMap);
		}
		return spec;
	}
	
	public static IValue instantiateValue(IValue value, ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within, int maxdepth) {
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
						resolved= resolveUnknown((ICPPUnknownBinding) unknown, tpMap, within);
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
			return Value.reevaluate(value, resolvedUnknowns, tpMap, maxdepth);
			
		if (Value.referencesTemplateParameter(value)) 
			return Value.reevaluate(value, unknowns, tpMap, maxdepth);

		return value;
	}

	/**
	 * This method propagates the specialization of a member to the types used by the member.
	 * @param type a type to instantiate.
	 * @param tpMap a mapping between template parameters and the corresponding arguments.
	 */
	public static IType instantiateType(IType type, ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within) {
		try {
			if (tpMap == null)
				return type;

			if (type instanceof IFunctionType) {
				IType ret = null;
				IType[] params = null;
				final IType r = ((IFunctionType) type).getReturnType();
				ret = instantiateType(r, tpMap, within);
				IType[] ps = ((IFunctionType) type).getParameterTypes();
				params = instantiateTypes(ps, tpMap, within);
				if (ret == r && params == ps) {
					return type;
				}
				return new CPPFunctionType(ret, params, ((ICPPFunctionType) type).getThisType());
			} 

			if (type instanceof ICPPTemplateParameter) {
				ICPPTemplateArgument arg= tpMap.getArgument((ICPPTemplateParameter) type);
				if (arg != null) {
					IType t= arg.getTypeValue();
					if (t != null)
						return t;
				}
				return type;
			} 

			if (type instanceof ICPPUnknownBinding) {
				IBinding binding= resolveUnknown((ICPPUnknownBinding) type, tpMap, within);
				if (binding instanceof IType)
					return (IType) binding;

				return type;
			}

			if (within != null && type instanceof IBinding && 
					(type instanceof ITypedef || type instanceof ICPPClassType)) {
				IBinding typeAsBinding= (IBinding) type;
				IBinding typeOwner= typeAsBinding.getOwner();
				ICPPClassType originalClass= within.getSpecializedBinding();
				if (typeOwner instanceof IType) {
					final IType parentType = (IType) typeOwner;
					if (parentType.isSameType(originalClass)) {
						return (IType) within.specializeMember(typeAsBinding);
					}
					IType newOwner= instantiateType(parentType, tpMap, within);
					if (newOwner != typeOwner && newOwner instanceof ICPPClassSpecialization) {
						return (IType) ((ICPPClassSpecialization) newOwner).specializeMember(typeAsBinding);
					}
					return type;
				}
			}		

			if (type instanceof ITypeContainer) {
				final ITypeContainer tc = (ITypeContainer) type;
				IType nestedType = tc.getType();
				IType newNestedType = instantiateType(nestedType, tpMap, within);
				if (type instanceof ICPPPointerToMemberType) {
					ICPPPointerToMemberType ptm = (ICPPPointerToMemberType) type;
					IType memberOfClass = ptm.getMemberOfClass();
					IType newMemberOfClass = instantiateType(memberOfClass, tpMap, within);
					if (newNestedType != nestedType || newMemberOfClass != memberOfClass) {
						if (newMemberOfClass instanceof ICPPClassType) {
							return new CPPPointerToMemberType(newNestedType, (ICPPClassType) newMemberOfClass,
								ptm.isConst(), ptm.isVolatile());
						}
						return type;
					}
				}
				if (newNestedType != nestedType) {
					return SemanticUtil.replaceNestedType(tc, newNestedType);
				} 
				return type;
			} 

			return type;
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	/**
	 * Instantiates types contained in an array.
	 * @param types an array of types
	 * @param tpMap template argument map
	 * @return an array containing instantiated types.
	 */
	public static IType[] instantiateTypes(IType[] types, ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within) {
		// Don't create a new array until it's really needed.
		IType[] result = types;
		for (int i = 0; i < types.length; i++) {
			IType type = CPPTemplates.instantiateType(types[i], tpMap, within);
			if (result != types) {
				result[i]= type;
			} else if (type != types[i]) {
				result = new IType[types.length];
				if (i > 0) {
					System.arraycopy(types, 0, result, 0, i);
				}
				result[i]= type;
			}
		}
		return result;
	}

	/**
	 * Instantiates arguments contained in an array.
	 */
	public static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] types, ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within) {
		// Don't create a new array until it's really needed.
		ICPPTemplateArgument[] result = types;
		for (int i = 0; i < types.length; i++) {
			ICPPTemplateArgument type = CPPTemplates.instantiateArgument(types[i], tpMap, within);
			if (result != types) {
				result[i]= type;
			} else if (type != types[i]) {
				result = new ICPPTemplateArgument[types.length];
				if (i > 0) {
					System.arraycopy(types, 0, result, 0, i);
				}
				result[i]= type;
			}
		}
		return result;
	}

	/**
	 * Instantiates an argument
	 */
	private static ICPPTemplateArgument instantiateArgument(ICPPTemplateArgument arg,
			ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within) {
		if (arg.isNonTypeValue()) {
			final IValue orig= arg.getNonTypeValue();
			final IValue inst= instantiateValue(orig, tpMap, within, Value.MAX_RECURSION_DEPTH);
			if (orig == inst)
				return arg;
			return new CPPTemplateArgument(inst, arg.getTypeOfNonTypeValue());
		}
		
		final IType orig= arg.getTypeValue();
		final IType inst= instantiateType(orig, tpMap, within);
		if (orig == inst)
			return arg;
		return new CPPTemplateArgument(inst);
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
		// find innermost template decl
		IASTDeclaration decl= tdecl.getDeclaration();
		while (decl instanceof ICPPASTInternalTemplateDeclaration) {
			tdecl= (ICPPASTInternalTemplateDeclaration) decl;
			decl= tdecl.getDeclaration();
		}
		final ICPPASTInternalTemplateDeclaration innerMostTDecl= tdecl;
		
		// find name declared in nested declaration
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
		int additionalLevels= 0;
		if (name instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qname= (ICPPASTQualifiedName) name;
			final IASTName lastName = qname.getLastName();
			final boolean lastIsID = lastName instanceof ICPPASTTemplateId;

			// count template-ids
			int idcount= 0;
			final IASTName[] ns= qname.getNames();
			for (int j = 0; j < ns.length; j++) {
				final IASTName n = ns[j];
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
				additionalLevels= idcount-tdeclcount;
			} else {
				additionalLevels= idcount+1-tdeclcount;
				if (additionalLevels > 0) {
					// last name is probably not a template
					additionalLevels--;
					lastIsTemplate= false;
					CharArraySet tparnames= collectTemplateParameterNames(outerMostTDecl);
					int j= 0;
					IASTName n;
					for (int i = 0; i < ns.length; i++) {
						n = ns[j];
						if (n instanceof ICPPASTTemplateId) {
							// if we find a dependent id, there can be no explicit specialization.
							ICPPASTTemplateId id= (ICPPASTTemplateId) n;
							if (usesTemplateParameter(id, tparnames))
								break;

							if (j++ == additionalLevels) {
								IBinding b= n.resolveBinding();
								if (b instanceof ICPPTemplateInstance && b instanceof ICPPClassType) {
									try {
										IScope s= ((ICPPClassType) b).getCompositeScope();
										if (!(s instanceof ICPPClassSpecializationScope)) {
											// template-id of an explicit specialization. 
											// here we don't have a template declaration. (see 14.7.3.5)
											additionalLevels++;
											lastIsTemplate= true;
										}
									} catch (DOMException e) {
										// assume that it is not an explicit instance
									}
								}
								break;
							}
						}
					}
				}
			}
		}
		
		if (additionalLevels < 0) {
			additionalLevels= 0; // too many template declarations
		}
		
		// determine nesting level of parent
		int level= 0;
		node= outerMostTDecl.getParent();
		while (node != null) {
			if (node instanceof ICPPASTInternalTemplateDeclaration) {
				level= ((ICPPASTInternalTemplateDeclaration) node).getNestingLevel() + 1;
				break;
			}
			node= node.getParent();
		}
		
		level += additionalLevels;
		
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
			{ shouldVisitNames= true;}
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
				name= CPPVisitor.findInnermostDeclarator(dtors[0]).getName();
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
			name= CPPVisitor.findInnermostDeclarator(fdef.getDeclarator()).getName();
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
		    declarator= CPPVisitor.findInnermostDeclarator(declarator);
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
		if (args.length == specArgs.length) {
			for (int i=0; i < args.length; i++) {
				if (!specArgs[i].isSameValue(args[i])) 
					return false;
			}
		}
		return true;
	}
	
	/**
	 * @param id the template id containing the template arguments
	 * @return an array of template arguments, currently modeled as IType objects. The
	 * empty IType array is returned if id is <code>null</code>
	 * @throws DOMException 
	 */
	static public ICPPTemplateArgument[] createTemplateArgumentArray(ICPPASTTemplateId id) throws DOMException {
		ICPPTemplateArgument[] result= ICPPTemplateArgument.EMPTY_ARGUMENTS;
		if (id != null) {
			IASTNode[] params= id.getTemplateArguments();
			result = new ICPPTemplateArgument[params.length];
			for (int i = 0; i < params.length; i++) {
				IASTNode param= params[i];
				IType type= CPPVisitor.createType(param);
				if (type == null)
					throw new DOMException(new ProblemBinding(id, IProblemBinding.SEMANTIC_INVALID_TYPE, id.toCharArray()));

				if (param instanceof IASTExpression) {
					IValue value= Value.create((IASTExpression) param, Value.MAX_RECURSION_DEPTH);
					result[i]= new CPPTemplateArgument(value, type);
				} else {
					result[i]= new CPPTemplateArgument(type);
				}
			}
		}
		return result;
	}
	
	/*
	 * aftodo - need to review this
	 */
	static public IType[] createTypeArray(Object[] params) {
		if (params == null)
			return IType.EMPTY_TYPE_ARRAY;

		if (params instanceof IType[])
			return (IType[]) params;

		IType[] result = new IType[params.length];
		for (int i = 0; i < params.length; i++) {
			IType type= null;
		    final Object param = params[i];
			if (param instanceof IASTNode) {
		    	type= CPPVisitor.createType((IASTNode) param);
			} else if (param instanceof IParameter) {
				try {
					type= ((IParameter) param).getType();
				} catch (DOMException e) {
					type= e.getProblem();
				}
			}
			// prevent null pointer exception when the type cannot be determined
			// happens when templates with still ambiguous template-ids are accessed during
			// resolution of other ambiguities.
		    result[i]= type == null ? new CPPBasicType(-1, 0) : type;
		}
		return result;
	}

	static protected IFunction[] selectTemplateFunctions(ObjectSet<IFunction> templates,
			Object[] functionArguments, IASTName name) {
		
		if (templates == null || templates.size() == 0)
			return null;

		ICPPTemplateArgument[] templateArguments = ICPPTemplateArgument.EMPTY_ARGUMENTS;
		final IType[] fnArgs= createTypeArray(functionArguments);
		try {
			if (containsDependentType(fnArgs))
				return new IFunction[] {CPPUnknownFunction.createForSample(templates.keyAt(0), name)};

			if (name instanceof ICPPASTTemplateId) {
				templateArguments = createTemplateArgumentArray((ICPPASTTemplateId) name);
				if (hasDependentArgument(templateArguments)) 
					return new IFunction[] {CPPUnknownFunction.createForSample(templates.keyAt(0), name)};
			}
		} catch (DOMException e) {
			return new IFunction[0];
		}

		IFunction[] instances= null;
		final int size = templates.size();
		for (int idx = 0; idx < size; idx++) {
			ICPPFunctionTemplate template = (ICPPFunctionTemplate) templates.keyAt(idx);
			CPPTemplateParameterMap map= new CPPTemplateParameterMap(fnArgs.length);
			try {
				ICPPTemplateArgument[] args= deduceTemplateFunctionArguments(template, templateArguments, fnArgs, map);
				if (args != null) {
					IBinding temp= instantiateFunctionTemplate(template, args);
					if (temp instanceof IFunction) {
						instances = (IFunction[]) ArrayUtil.append(IFunction.class, instances, temp);
					}
				}
			} catch (DOMException e) {
				// try next candidate
			}
		}

		return (IFunction[]) ArrayUtil.trim(IFunction.class, instances);
	}

	/**
	 * Deduces the mapping for the template parameters from the function parameters,
	 * returns <code>false</code> if there is no mapping.
	 */
	private static boolean deduceTemplateParameterMapFromFunctionParameters(ICPPFunctionTemplate template, IType[] fnArgs, CPPTemplateParameterMap map) throws DOMException{
		try {
			IType[] fnPars = template.getType().getParameterTypes();
			int len= Math.min(fnPars.length, fnArgs.length);
			for (int j= 0; j < len; j++) {
				IType par= fnPars[j];
				par= instantiateType(par, map, null);
				if (!isValidType(par))
					return false;

				par= SemanticUtil.adjustParameterType(par);
				if (isDependentType(par) && !deduceTemplateParameterMap(par, fnArgs[j], map)) {
					return false;
				}
			}
			return true;
		} catch (DOMException e) {
		}
		return false;
	}
	
	/**
	 * Deduces the template parameter mapping from pairs of template arguments.
	 */
	public static boolean deduceTemplateParameterMap(final ICPPTemplateArgument[] p, final ICPPTemplateArgument[] a, CPPTemplateParameterMap map) throws DOMException {
		final int len= a.length;
		if (p == null || p.length != len) {
			return false;
		}
		for (int j=0; j<len; j++) {
			if (!deduceTemplateParameterMap(p[j], a[j], map)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Deduces the template parameter mapping from one pair of template arguments.
	 */
	private static boolean deduceTemplateParameterMap(ICPPTemplateArgument p,
			ICPPTemplateArgument a, final CPPTemplateParameterMap map) throws DOMException {
		if (p.isNonTypeValue() != a.isNonTypeValue()) 
			return false;
		
		if (p.isNonTypeValue()) {
			IValue tval= p.getNonTypeValue();

			int parPos= Value.isTemplateParameter(tval);
			if (parPos >= 0) { 
				ICPPTemplateArgument old= map.getArgument(parPos);
				if (old == null) {
					map.put(parPos, a);
					return true;
				}
				return old.isSameValue(a);
			}
			
			IValue sval= a.getNonTypeValue();
			return tval.equals(sval); 
		} 
		
		return deduceTemplateParameterMap(p.getTypeValue(), a.getTypeValue(), map);
	}

	/**
	 * 14.8.2.1-2 If P is a cv-qualified type, the top level cv-qualifiers of P's type are ignored for type
	 * deduction.  If P is a reference type, the type referred to by P is used for Type deduction.
	 * @param pSymbol
	 * @return
	 */
	static private IType getParameterTypeForDeduction(IType pType) {
		IType result = pType;
		try {
			if (pType instanceof IQualifierType) {
				result = ((IQualifierType) pType).getType();
			} else if (pType instanceof ICPPReferenceType) {
				result = ((ICPPReferenceType) pType).getType();
			} else if (pType instanceof  CPPPointerType) {
				result = ((CPPPointerType) pType).stripQualifiers();
			}
		} catch (DOMException e) {
			result = e.getProblem();
		}
		return result;
	}

	/**
	 * 14.8.2.1-2
	 * if P is not a reference type
	 * - If A is an array type, the pointer type produced by the array-to-pointer conversion is used instead
	 * - If A is a function type, the pointer type produced by the function-to-pointer conversion is used instead
	 * - If A is a cv-qualified type, the top level cv-qualifiers are ignored for type deduction
	 * @param aInfo
	 * @return
	 */
	static private IType getArgumentTypeForDeduction(IType aType, boolean pIsAReferenceType) {
		if (aType instanceof ICPPReferenceType) {
		    try {
                aType = ((ICPPReferenceType) aType).getType();
            } catch (DOMException e) {
            }
		}
		IType result = aType;
		if (!pIsAReferenceType) {
			try {
				if (aType instanceof IArrayType) {
					result = new CPPPointerType(((IArrayType) aType).getType());
				} else if (aType instanceof IFunctionType) {
					result = new CPPPointerType(aType);
				} else if (aType instanceof IQualifierType) {
					result = ((IQualifierType) aType).getType();
				} else if (aType instanceof CPPPointerType) {
					result = ((CPPPointerType) aType).stripQualifiers();
				}
			} catch (DOMException e) {
				result = e.getProblem();
			}
		}

		return result;
	}

	private static boolean deduceTemplateParameterMap(IType p, IType a, CPPTemplateParameterMap map) throws DOMException {
		boolean pIsAReferenceType = (p instanceof ICPPReferenceType);
		p = getParameterTypeForDeduction(p);
		a = getArgumentTypeForDeduction(a, pIsAReferenceType);

		while (p != null) {
			while (a instanceof ITypedef)
				a = ((ITypedef) a).getType();
			if (p instanceof IBasicType) {
				return p.isSameType(a);
			} else if (p instanceof ICPPPointerToMemberType) {
				if (!(a instanceof ICPPPointerToMemberType))
					return false;
				if (!deduceTemplateParameterMap(((ICPPPointerToMemberType) p).getMemberOfClass(), ((ICPPPointerToMemberType) a).getMemberOfClass(),
						map)) {
					return false;
				}
				p = ((ICPPPointerToMemberType) p).getType();
				a = ((ICPPPointerToMemberType) a).getType();
			} else if (p instanceof IPointerType) {
				if (!(a instanceof IPointerType)) {
					return false;
				}
				p = ((IPointerType) p).getType();
				a = ((IPointerType) a).getType();
			} else if (p instanceof IQualifierType) {
				if (a instanceof IQualifierType) {
					a = ((IQualifierType) a).getType(); //TODO a = strip qualifiers from p out of a
				}
				p = ((IQualifierType) p).getType();
			} else if (p instanceof IFunctionType) {
				if (!(a instanceof IFunctionType))
					return false;
				if (!deduceTemplateParameterMap(((IFunctionType) p).getReturnType(), ((IFunctionType) a).getReturnType(),
						map)) {
					return false;
				}
				IType[] pParams = ((IFunctionType) p).getParameterTypes();
				IType[] aParams = ((IFunctionType) a).getParameterTypes();
				if (pParams.length != aParams.length)
					return false;
				for (int i = 0; i < pParams.length; i++) {
					if (!deduceTemplateParameterMap(pParams[i], aParams[i], map))
						return false;
				}
				return true;
			} else if (p instanceof ICPPTemplateParameter) {
				ICPPTemplateArgument current= map.getArgument((ICPPTemplateParameter) p);
				if (current != null) {
					if (current.isNonTypeValue())
						return false;
					return current.getTypeValue().isSameType(a); 
				}
				if (a == null)
					return false;
				map.put((ICPPTemplateParameter)p, new CPPTemplateArgument(a));
				return true;
			} else if (p instanceof ICPPTemplateInstance) {
				if (!(a instanceof ICPPTemplateInstance))
					return false;
				ICPPTemplateInstance pInst = (ICPPTemplateInstance) p;
				ICPPTemplateInstance aInst = (ICPPTemplateInstance) a;

				ICPPTemplateArgument[] pArgs = pInst.getTemplateArguments();
				pArgs= pArgs == null ? ICPPTemplateArgument.EMPTY_ARGUMENTS : pArgs; // aftodo - unnecessary?

				ICPPTemplateParameterMap aMap = aInst.getTemplateParameterMap();
				if (aMap != null && !(aInst.getTemplateDefinition() instanceof ICPPClassTemplatePartialSpecialization)) {
					ICPPTemplateParameter[] aParams = aInst.getTemplateDefinition().getTemplateParameters();
					if (pArgs.length != aParams.length)
						return false;
					for (int i = 0; i < pArgs.length; i++) {
						ICPPTemplateArgument t = aMap.getArgument(aParams[i]);
						if (t == null || !deduceTemplateParameterMap(pArgs[i], t, map))
							return false;
					}
				} else {
					ICPPTemplateArgument[] aArgs = aInst.getTemplateArguments();
					aArgs= aArgs == null ? ICPPTemplateArgument.EMPTY_ARGUMENTS : aArgs; // aftodo - unnecessary?

					if (aArgs.length != pArgs.length)
						return false;
					for (int i = 0; i < pArgs.length; i++) {
						if (!deduceTemplateParameterMap(pArgs[i], aArgs[i], map))
							return false;
					}
				}
				return true;
			} else if (p instanceof ICPPUnknownBinding) {
				return true;  // An unknown type may match anything.
			} else {
				return p.isSameType(a);
			}
		}

		return false;
	}

	/**
	 * Transforms a function template for use in partial ordering, as described in the
	 * spec 14.5.5.2-3
	 * @param template
	 * @return
	 * -for each type template parameter, synthesize a unique type and substitute that for each
	 * occurrence of that parameter in the function parameter list
	 * -for each non-type template parameter, synthesize a unique value of the appropriate type and
	 * substitute that for each occurrence of that parameter in the function parameter list
	 * for each template template parameter, synthesize a unique class template and substitute that
	 * for each occurrence of that parameter in the function parameter list
	 * @throws DOMException
	 */
	static private ICPPTemplateArgument[] createArgsForFunctionTemplateOrdering(ICPPFunctionTemplate template)
			throws DOMException{
		ICPPTemplateParameter[] paramList = template.getTemplateParameters();
		int size = paramList.length;
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[size];
		for (int i = 0; i < size; i++) {
			ICPPTemplateParameter param = paramList[i];
			if (param instanceof ICPPTemplateNonTypeParameter) {
				args[i]= new CPPTemplateArgument(Value.unique(), ((ICPPTemplateNonTypeParameter) param).getType());
			} else {
				args[i] = new CPPTemplateArgument(new CPPBasicType(-1, 0));
			}
		}
		return args;
	}

	static protected int orderTemplateFunctions(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2)
			throws DOMException {
		// Using the transformed parameter list, perform argument deduction against the other
		// function template
		CPPTemplateParameterMap m1= new CPPTemplateParameterMap(2);
		CPPTemplateParameterMap m2= new CPPTemplateParameterMap(2);

		ICPPTemplateArgument[] args = createArgsForFunctionTemplateOrdering(f1);
		IBinding function = instantiateFunctionTemplate(f1, args);
		if (function instanceof ICPPFunction)
			if (!deduceTemplateParameterMapFromFunctionParameters(f2, ((ICPPFunction) function).getType().getParameterTypes(), m1))
				m1= null;

		args = createArgsForFunctionTemplateOrdering(f2);
		function = instantiateFunctionTemplate(f2, args);
		if (function instanceof ICPPFunction)
			if (!deduceTemplateParameterMapFromFunctionParameters(f1, ((ICPPFunction) function).getType().getParameterTypes(), m2))
				m2= null;
		
		
		// The transformed template is at least as specialized as the other iff the deduction
		// succeeds and the deduced parameter types are an exact match.
		// A template is more specialized than another iff it is at least as specialized as the
		// other template and that template is not at least as specialized as the first.
		if (m1 == null) {
			if (m2 == null) 
				return 0;
			return -1;
		} 

		if (m2 == null) 
			return 1;

		// Count the number of cv-qualifications. The function with a lower number
		// of cv-qualifications is more specialized.
		int d1 = 0;
		for (ICPPTemplateArgument arg : m1.values()) {
			if (arg.getTypeValue() instanceof IQualifierType)
				d1++;
		}
		int d2 = 0;
		for (ICPPTemplateArgument arg : m2.values()) {
			if (arg.getTypeValue() instanceof IQualifierType) {
				d2++;
			}
		}
		return d1 - d2;
	}

	private static ICPPClassTemplatePartialSpecialization findPartialSpecialization(ICPPClassTemplate ct, ICPPTemplateArgument[] args) throws DOMException {
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

	static public ICPPTemplateDefinition selectSpecialization(ICPPClassTemplate template, ICPPTemplateArgument[] args)
			throws DOMException {
		if (template == null) {
			return null;
		}

		ICPPClassTemplatePartialSpecialization[] specializations = template.getPartialSpecializations();
		if (specializations == null) {
			return template;
		}
		final int size= specializations.length;
		if (size == 0) {
			return template;
		}

		ICPPClassTemplatePartialSpecialization bestMatch = null, spec = null;
		boolean bestMatchIsBest = true;
		for (int i = 0; i < size; i++) {
			spec = specializations[i];
			if (deduceTemplateParameterMap(spec.getTemplateArguments(), args, new CPPTemplateParameterMap(args.length))) {
				int compare = orderSpecializations(bestMatch, spec);
				if (compare == 0) {
					bestMatchIsBest = false;
				} else if (compare < 0) {
					bestMatch = spec;
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
			return template;
		
		return bestMatch;
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

		//to order class template specializations, we need to transform them into function templates
		ICPPFunctionTemplate template1 = classTemplateSpecializationToFunctionTemplate(spec1);
		ICPPFunctionTemplate template2 = classTemplateSpecializationToFunctionTemplate(spec2);
		if (template1 == null) {
			if (template2 == null)
				return 0;
			return 1;
		}
		if (template2 == null)
			return -1;
		
		return orderTemplateFunctions(template1, template2);
	}

	public static final class CPPImplicitFunctionTemplate extends CPPFunctionTemplate {
		IParameter[] functionParameters = null;
		ICPPTemplateParameter[] templateParameters = null;

		public CPPImplicitFunctionTemplate(ICPPTemplateParameter[] templateParameters, IParameter[] functionParameters) {
			super(null);
			this.functionParameters = functionParameters;
			this.templateParameters = templateParameters;
		}
		@Override
		public IParameter[] getParameters() {
			return functionParameters;
		}
		@Override
		public ICPPTemplateParameter[] getTemplateParameters() {
			return templateParameters;
		}
		@Override
		public IScope getScope() {
			return null;
		}
		@Override
		public ICPPFunctionType getType() {
			if (type == null) {
				type = CPPVisitor.createImplicitFunctionType(new CPPBasicType(IBasicType.t_void, 0), functionParameters, null);
			}
			return type;
		}
	}
	/**
	 * transform the class template to a function template as described in the spec
	 * 14.5.4.2-1
	 * @param template
	 * @return IParameterizedSymbol
	 * the function template has the same template parameters as the partial specialization and
	 * has a single function parameter whose type is a class template specialization with the template
	 * arguments of the partial specialization
	 */
	static private ICPPFunctionTemplate classTemplateSpecializationToFunctionTemplate(ICPPClassTemplatePartialSpecialization specialization) {
		try {
//			ICPPTemplateArgument[] args= specialization.getTemplateArguments();
			ICPPTemplateArgument[] args= (specialization).getTemplateArguments();
			IBinding paramType = deferredInstance(specialization, args);
			if (!(paramType instanceof IType))
				return null;

			IParameter[] functionParameters = new IParameter[] { new CPPParameter((IType) paramType) };

			return new CPPImplicitFunctionTemplate(specialization.getTemplateParameters(), functionParameters);
		} catch (DOMException e) {
			return null;
		}
	}

	static private boolean isValidType(IType t) {
		try {
			while (t instanceof ITypeContainer) {
				t = ((ITypeContainer) t).getType();
			}
		} catch (DOMException e) {
			return false;
		}
		return !(t instanceof IProblemBinding);
	}

	static protected ICPPTemplateArgument matchTemplateParameterAndArgument(ICPPTemplateParameter param, 
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
			} catch (DOMException e) {
				return null;
			}

			int size = pParams.length;
			if (aParams.length != size) {
				return null;
			}

			for (int i = 0; i < size; i++) {
				final ICPPTemplateParameter pParam = pParams[i];
				final ICPPTemplateParameter aParam = aParams[i];
				boolean pb= pParam instanceof ICPPTemplateTypeParameter;
				boolean ab= aParam instanceof ICPPTemplateTypeParameter;
				if (pb != ab)
					return null;
				if (!pb) {
					pb= pParam instanceof ICPPTemplateNonTypeParameter;
					ab= aParam instanceof ICPPTemplateNonTypeParameter;
					if (pb != ab)
						return null;
					assert pParam instanceof ICPPTemplateTemplateParameter; // no other choice left
					assert aParam instanceof ICPPTemplateTemplateParameter; // no other choice left
				}
			}

			return arg;
		} 
		
		if (param instanceof ICPPTemplateNonTypeParameter) {
			if (!arg.isNonTypeValue())
				return null;
			IType argType= arg.getTypeOfNonTypeValue();
			try {
				IType pType = ((ICPPTemplateNonTypeParameter) param).getType();
				if (map != null && pType != null) {
					pType= instantiateType(pType, map, null);
				}
				if (argType instanceof ICPPUnknownType || isNonTypeArgumentConvertible(pType, argType)) {
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
	    	try {
	    		paramType = new CPPPointerType(((IArrayType) paramType).getType());
			} catch (DOMException e) {
				paramType = e.getProblem();
			}
		}
		Cost cost = Conversions.checkStandardConversionSequence(arg, paramType, false);
		return cost != null && cost.rank != Cost.NO_MATCH_RANK;
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
				if (argType == null || !argType.isSameType((IType) par))
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
	
	public static boolean containsDependentType(IType[] ts) {
		for (IType t : ts) {
			if (isDependentType(t))
				return true;
		}
		return false;
	}
	
	public static boolean isDependentType(IType t) {
		try {
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
				} else if (t instanceof ITypeContainer) {
					t= ((ITypeContainer) t).getType();
				} else {
					return false;
				}
			}
		} catch (DOMException e) {
			// treat as non-dependent
			return false;
		}
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
	private static IBinding resolveUnknown(ICPPUnknownBinding unknown, ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within) throws DOMException {
        if (unknown instanceof ICPPDeferredClassInstance) {
        	return resolveDeferredClassInstance((ICPPDeferredClassInstance) unknown, tpMap, within);
        }

        final IBinding owner= unknown.getOwner();
        IBinding result = unknown;
        IType t = null;
		if (owner instanceof ICPPTemplateTypeParameter) {
			t = CPPTemplates.instantiateType((ICPPTemplateTypeParameter) owner, tpMap, null);
		} else if (owner instanceof ICPPUnknownClassType) {
        	IBinding binding= resolveUnknown((ICPPUnknownBinding) owner, tpMap, within);
        	if (binding instanceof IType) {
                t = (IType) binding;
            }
        } 
        if (t != null) {
            t = SemanticUtil.getUltimateType(t, false);
            if (t instanceof ICPPUnknownBinding) {
            	if (unknown instanceof ICPPUnknownClassInstance) {
            		ICPPUnknownClassInstance ucli= (ICPPUnknownClassInstance) unknown;
            		final ICPPTemplateArgument[] arguments = ucli.getArguments();
            		ICPPTemplateArgument[] newArgs = CPPTemplates.instantiateArguments(arguments, tpMap, within);
            		if (!t.equals(owner) && newArgs != arguments) {
            			result= new CPPUnknownClassInstance((ICPPUnknownBinding) t, ucli.getUnknownName(), newArgs);
            		}
            	} else if (!t.equals(owner)) {
            		if (unknown instanceof ICPPUnknownClassType) {
            			result= new CPPUnknownClass((ICPPUnknownBinding)t, unknown.getUnknownName());
            		} else if (unknown instanceof IFunction) {
            			result= new CPPUnknownClass((ICPPUnknownBinding)t, unknown.getUnknownName());
            		} else {
            			result= new CPPUnknownBinding((ICPPUnknownBinding) t, unknown.getUnknownName());
            		}
            	} 
            } else if (t instanceof ICPPClassType) {
	            IScope s = ((ICPPClassType) t).getCompositeScope();
	            if (s != null) {
	            	IASTName name= unknown.getUnknownName();
	            	if (name != null) {
	            		IBinding[] candidates;
	            		if (s instanceof ICPPASTInternalScope) {
	            			candidates= ((ICPPASTInternalScope) s).getBindings(name, true, false, null, false);
	            		} else {
	            			candidates= s.getBindings(name, true, false, null);
	            		}
	            		result= CPPSemantics.resolveAmbiguities(name, candidates);
	    	            if (unknown instanceof ICPPUnknownClassInstance && result instanceof ICPPTemplateDefinition) {
	    	            	ICPPTemplateArgument[] newArgs = CPPTemplates.instantiateArguments(((ICPPUnknownClassInstance) unknown).getArguments(), tpMap, within);
	    	            	if (result instanceof ICPPClassTemplate) {
	    	            		result = instantiate((ICPPClassTemplate) result, newArgs);
	    	            	}
	    	            }
	            	}
	            }
            }
        }
        
        return result;
	}

	private static IBinding resolveDeferredClassInstance(ICPPDeferredClassInstance dci, ICPPTemplateParameterMap tpMap, ICPPClassSpecialization within) {
		ICPPTemplateArgument[] arguments = dci.getTemplateArguments();
		ICPPTemplateArgument[] newArgs = CPPTemplates.instantiateArguments(arguments, tpMap, within);

		boolean changed= arguments != newArgs;
		ICPPClassTemplate classTemplate = dci.getClassTemplate();
		if (classTemplate instanceof ICPPTemplateParameter) {
			// template template parameter
			ICPPTemplateArgument arg= tpMap.getArgument((ICPPTemplateParameter) classTemplate);
			if (arg != null) {
				IType t= arg.getTypeValue();
				if (t instanceof ICPPClassTemplate) {
					classTemplate= (ICPPClassTemplate) t;
					changed= true;
				}
			}
		}

		if (changed) {
			IBinding inst= instantiate(classTemplate, newArgs);
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
						IType type= arg.isNonTypeValue() ? arg.getTypeOfNonTypeValue() : arg.getTypeValue();
						result.put(tps[numParam], type);
					}
				}
			}
			return result;
		} catch (DOMException e) {
		}
		return ObjectMap.EMPTY_MAP;
	}
}
