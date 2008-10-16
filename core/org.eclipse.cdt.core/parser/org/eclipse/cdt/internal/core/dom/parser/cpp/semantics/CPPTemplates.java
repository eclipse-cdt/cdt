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

import java.math.BigInteger;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
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
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunctionInstance;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedefSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.core.runtime.Assert;

/**
 * Collection of static methods to perform template instantiation, member specialization and
 * type instantiation.
 */
public class CPPTemplates {

	/**
	 * Instantiates a template with the given arguments. May return <code>null</code>.
	 */
	public static IBinding instantiate(ICPPTemplateDefinition template, IType[] arguments) {
		try {
			if (template instanceof ICPPTemplateTemplateParameter) {
				return deferredInstance(template, arguments);
			}

			if (template instanceof ICPPClassTemplate) {
				template= CPPTemplates.selectSpecialization((ICPPClassTemplate) template, arguments);
				if (template == null || template instanceof IProblemBinding) 
					return template;

				if (template instanceof ICPPClassTemplatePartialSpecialization) {
					final ICPPClassTemplatePartialSpecialization partialSpec = (ICPPClassTemplatePartialSpecialization) template;
					return instantiatePartialSpecialization(partialSpec, arguments);
				}

				return instantiateSelectedTemplate(template, arguments);	
			}
			return instantiateSelectedTemplate(template, arguments);
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	/**
	 * Instantiates a partial class template specialization.
	 */
	private static IBinding instantiatePartialSpecialization(ICPPClassTemplatePartialSpecialization partialSpec, IType[] args) throws DOMException {
		args= SemanticUtil.getSimplifiedTypes(args);
		ICPPTemplateInstance instance= getInstance(partialSpec, args);
		if (instance != null)
			return instance;

		ObjectMap argMap= CPPTemplates.deduceTemplateArguments(partialSpec.getArguments(), args, true);
		if (argMap == null)
			return null;

		ICPPTemplateParameter[] params= partialSpec.getTemplateParameters();
		int numParams = params.length;
		for (int i = 0; i < numParams; i++) {
			if (params[i] instanceof IType && !argMap.containsKey(params[i]))
				return null;
		}

		instance= createInstance(partialSpec.getOwner(), partialSpec, argMap, args);
		addInstance(partialSpec, args, instance);
		return instance;
	}

	/** 
	 * Instantiates the selected template, without looking for specializations. May return <code>null</code>.
	 */
	private static IBinding instantiateSelectedTemplate(ICPPTemplateDefinition template, IType[] arguments) 
			throws DOMException {
		Assert.isTrue(!(template instanceof ICPPClassTemplatePartialSpecialization));

		ICPPTemplateParameter[] parameters= template.getTemplateParameters();
		if (parameters == null || parameters.length == 0) 
			return null;

		final int numParams= parameters.length;
		int numArgs = arguments.length;

		ObjectMap map = new ObjectMap(numParams);
		ICPPTemplateParameter param = null;
		IType arg = null;
		IType[] actualArgs = new IType[numParams];
		boolean argsContainDependentType = false;

		arguments= SemanticUtil.getSimplifiedTypes(arguments);
		for (int i = 0; i < numParams; i++) {
			arg= null;
			param= parameters[i];

			if (i < numArgs) {
				arg= arguments[i];
			} else {
				IType defaultType = null;
				if (param instanceof ICPPTemplateTypeParameter)
					defaultType = ((ICPPTemplateTypeParameter) param).getDefault();
				else if (param instanceof ICPPTemplateTemplateParameter)
					defaultType = ((ICPPTemplateTemplateParameter) param).getDefault();
				else if (param instanceof ICPPTemplateNonTypeParameter)
					defaultType = CPPVisitor.getExpressionType(((ICPPTemplateNonTypeParameter) param).getDefault());

				if (defaultType == null) {
					return null;
				}

				if (defaultType instanceof ICPPTemplateParameter) {
					if (map.containsKey(defaultType)) {
						arg = (IType) map.get(defaultType);
					}
				} else if (defaultType instanceof ICPPUnknownBinding) {
					// A default template parameter may be depend on a previously defined
					// parameter: template<typename T1, typename T2 = A<T1> > class B {};
					IType resolvedType= null;
					try {
						IBinding resolved= resolveUnknown((ICPPUnknownBinding) defaultType, map, null);
						if (resolved instanceof IType) {
							resolvedType= (IType) resolved;
						}
					} catch (DOMException e) {
					}
					arg= resolvedType == null ? defaultType : resolvedType;
				} else {
					arg = defaultType;
				}
			}

			if (CPPTemplates.matchTemplateParameterAndArgument(param, arg, map)) {
				if (!param.equals(arg)) {
					map.put(param, arg);
				}
				actualArgs[i] = arg;
				if (CPPTemplates.isDependentType(arg)) {
					argsContainDependentType = true;
				}
			} else {
				return null;
			}
		}

		ICPPTemplateInstance instance= getInstance(template, arguments);
		// we do not correctly distinguish between type and non-type parameters, this works
		// around getting the wrong instance when providing a value rather than a type.
		if (instance != null) {
			if (argsContainDependentType || !(instance instanceof ICPPDeferredClassInstance))  
				return instance;
		}

		if (argsContainDependentType) {
			return deferredInstance(template, actualArgs);
		}

		IBinding owner= template.getOwner();
		instance = CPPTemplates.createInstance(owner, template, map, actualArgs);
		addInstance(template, actualArgs, instance);
		return instance;
	}

	/**
	 * Obtains a cached instance from the template.
	 */
	private static ICPPTemplateInstance getInstance(ICPPTemplateDefinition template, IType[] args) {
		if (template instanceof ICPPInstanceCache) {
			return ((ICPPInstanceCache) template).getInstance(args);
		}
		return null;
	}

	/**
	 * Caches an instance with the template.
	 */
	private static void addInstance(ICPPTemplateDefinition template, IType[] args, ICPPTemplateInstance instance) {
		if (template instanceof ICPPInstanceCache) {
			((ICPPInstanceCache) template).addInstance(args, instance);
		}
	}

	private static IBinding deferredInstance(ICPPTemplateDefinition template, IType[] arguments) throws DOMException {
		ICPPTemplateInstance instance= getInstance(template, arguments);
		if (instance != null)
			return instance;

		if (template instanceof ICPPClassTemplate) {
			instance = new CPPDeferredClassInstance((ICPPClassTemplate) template, arguments);
			addInstance(template, arguments, instance);
			return instance;
		}
		if (template instanceof ICPPFunctionTemplate) {
			instance = new CPPDeferredFunctionInstance((ICPPFunctionTemplate) template, arguments);
			addInstance(template, arguments, instance);
			return instance;
		}
		return null;
	}

	/**
	 * Instantiates the template for usage within its own body. May return <code>null</code>.
	 */
	public static IBinding instantiateWithinClassTemplate(ICPPClassTemplate template) throws DOMException {
		ICPPTemplateParameter[] templateParameters = template.getTemplateParameters();
		IType[] args = new IType[templateParameters.length];
		for (int i = 0; i < templateParameters.length; i++) {
			if (templateParameters[i] instanceof IType) {
				args[i] = (IType) templateParameters[i];
			} else if (templateParameters[i] instanceof ICPPTemplateNonTypeParameter) {
				args[i] = ((ICPPTemplateNonTypeParameter) templateParameters[i]).getType();
			}
		}

		return deferredInstance(template, args);
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
							binding = ((ICPPASTTemplateId) element).getTemplateName().resolveBinding();
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
		return  (binding instanceof ICPPTemplateDefinition) ? (ICPPTemplateDefinition) binding : null;
	}

	public static IBinding createBinding(ICPPASTTemplateParameter templateParameter) {
		ICPPTemplateDefinition template = getContainingTemplate(templateParameter);

		IBinding binding = null;
		if (template instanceof CPPTemplateTemplateParameter) {
			binding = ((CPPTemplateTemplateParameter) template).resolveTemplateParameter(templateParameter);
		} else if (template instanceof CPPTemplateDefinition) {
			binding = ((CPPTemplateDefinition) template).resolveTemplateParameter(templateParameter);
		} else if (template != null) {
			IASTName name = CPPTemplates.getTemplateParameterName(templateParameter);
			binding = name.getBinding();

			if (binding == null) {
				ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) templateParameter.getParent();
				ICPPASTTemplateParameter[] ps = templateDecl.getTemplateParameters();

				int i = 0;
				for (; i < ps.length; i++) {
					if (templateParameter == ps[i])
						break;
				}

				try {
					ICPPTemplateParameter[] params = template.getTemplateParameters();
					if (i < params.length) {
						binding = params[i];
						name.setBinding(binding);
					}
				} catch (DOMException e) {
				}
			}
		}

	    return binding;
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
		IASTNode parent = id.getParent();
		int segment = -1;
		if (parent instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) parent).getNames();
			segment = (ns[ns.length - 1] == id) ? 1 : 0;
			parent = parent.getParent();
		}

		IASTNode decl = parent.getParent();
		while (!(decl instanceof IASTDeclaration))
			decl = decl.getParent();
		decl = decl.getParent();

		try {
			if (decl instanceof ICPPASTExplicitTemplateInstantiation &&
					parent instanceof ICPPASTElaboratedTypeSpecifier && segment != 0) {
				return createExplicitClassInstantiation((ICPPASTElaboratedTypeSpecifier) parent);
			} else if (((parent instanceof ICPPASTElaboratedTypeSpecifier &&
					decl instanceof ICPPASTTemplateDeclaration) ||
					parent instanceof ICPPASTCompositeTypeSpecifier) &&
					segment != 0) {
				return createExplicitClassSpecialization((ICPPASTDeclSpecifier) parent);
			} else if (parent instanceof ICPPASTFunctionDeclarator && segment != 0) {
				return createFunctionSpecialization(id);
			}
		} catch (DOMException e) {
			return e.getProblem();
		}

		//a reference: class or function template?
		IBinding template = null;
		if (parent instanceof ICPPASTNamedTypeSpecifier ||
				parent instanceof ICPPASTElaboratedTypeSpecifier ||
				parent instanceof ICPPASTBaseSpecifier ||
				segment == 0) {
			//class template
			IASTName templateName = id.getTemplateName();
			template = templateName.resolveBinding();
			if (template instanceof ICPPClassTemplatePartialSpecialization) {
				//specializations are selected during the instantiation, start with the primary template
				try {
					template = ((ICPPClassTemplatePartialSpecialization) template).getPrimaryClassTemplate();
				} catch (DOMException e) {
					return e.getProblem();
				}
			} else if (template instanceof ICPPSpecialization && !(template instanceof ICPPTemplateDefinition)) {
				template = ((ICPPSpecialization) template).getSpecializedBinding();
			}

			if (template instanceof ICPPTemplateDefinition) {
				IType[] types= CPPTemplates.createTemplateArgumentArray(id);
				IBinding instance= instantiate((ICPPTemplateDefinition) template, types);
				return CPPSemantics.postResolution(instance, id);
			}
		} else {
			//functions are instantiated as part of the resolution process
			template = CPPVisitor.createBinding(id);
			if (template instanceof ICPPTemplateInstance) {
				IASTName templateName = id.getTemplateName();
				templateName.setBinding(((ICPPTemplateInstance) template).getTemplateDefinition());
			}
		}

		return template;
	}

	protected static IBinding createExplicitClassInstantiation(ICPPASTElaboratedTypeSpecifier elabSpec) {
	    IASTName name = elabSpec.getName();
	    if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
	    ICPPASTTemplateId id = (ICPPASTTemplateId) name;
	    IBinding template = id.getTemplateName().resolveBinding();
		if (template instanceof ICPPClassTemplate) {
			ICPPClassTemplate classTemplate = (ICPPClassTemplate) template;
			IType[] args= createTemplateArgumentArray(id);
			IBinding binding= instantiate(classTemplate, args);
			if (binding != null) 
				return binding;
		}
		return new ProblemBinding(elabSpec, IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray());
	}

	/**
	 * Creates the binding for a partial or explicit class specialization.
	 * @throws DOMException 
	 */
	protected static IBinding createExplicitClassSpecialization(ICPPASTDeclSpecifier compSpec) throws DOMException {
		IASTName name = null;
		if (compSpec instanceof ICPPASTElaboratedTypeSpecifier)
			name = ((ICPPASTElaboratedTypeSpecifier) compSpec).getName();
		else if (compSpec instanceof ICPPASTCompositeTypeSpecifier)
			name = ((ICPPASTCompositeTypeSpecifier) compSpec).getName();
		else
			return null;

		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
		ICPPASTTemplateId id = (ICPPASTTemplateId) name;

		IBinding binding = id.getTemplateName().resolveBinding();
		if (!(binding instanceof ICPPClassTemplate))
			return null;  //TODO: problem?

		ICPPClassTemplate template = (ICPPClassTemplate) binding;

		ICPPASTTemplateDeclaration templateDecl = getTemplateDeclaration(id);
		if (templateDecl instanceof ICPPASTTemplateSpecialization) {
			ICPPTemplateInstance inst = null;
			ICPPTemplateParameter[] templateParams= template.getTemplateParameters();
			IType[] args= createTemplateArgumentArray(id);
			ObjectMap argMap = new ObjectMap(templateParams.length);
			if (templateParams.length != args.length) {
				return null; //TODO problem
			}
			for (int i = 0; i < templateParams.length; i++) {
				argMap.put(templateParams[i], args[i]);
			}
			inst= getInstance(template, args);
			if (inst == null) {
				IBinding owner= binding.getOwner();
				inst= new CPPClassInstance(owner, template, argMap, args);
				addInstance(template, args, inst);
			}
			if (inst instanceof ICPPInternalBinding) {
				IASTNode parent = id.getParent();
				while (!(parent instanceof IASTDeclSpecifier))
					parent = parent.getParent();
				if (parent instanceof IASTElaboratedTypeSpecifier)
					((ICPPInternalBinding) inst).addDeclaration(id);
				else if (parent instanceof IASTCompositeTypeSpecifier)
					((ICPPInternalBinding) inst).addDefinition(id);
			}
			return inst;
		}
		//else partial specialization
		//CPPClassTemplate template = (CPPClassTemplate) binding;
		ICPPClassTemplatePartialSpecialization spec= null;
		try {
			ICPPClassTemplatePartialSpecialization[] specs = template.getPartialSpecializations();
			if (specs != null) {
				for (ICPPClassTemplatePartialSpecialization specialization : specs) {
					if (isSameTemplate(specialization, id)) {
						spec = specialization;
						break;
					}
				}
			}
		} catch (DOMException e) {
		}

		if (spec != null) {
			if (spec instanceof ICPPInternalBinding)
				((ICPPInternalBinding) spec).addDefinition(id);
			return spec;
		}

		spec = new CPPClassTemplatePartialSpecialization(id);
		if (template instanceof ICPPInternalClassTemplate)
			((ICPPInternalClassTemplate) template).addPartialSpecialization(spec);
		return spec;
	}

	protected static IBinding createFunctionSpecialization(IASTName name) throws DOMException {
		try {
			LookupData data = new LookupData(name);
			data.forceQualified = true;
			ICPPScope scope = (ICPPScope) CPPVisitor.getContainingScope(name);
			if (scope instanceof ICPPTemplateScope) {
				scope = (ICPPScope) scope.getParent();
			}
			CPPSemantics.lookup(data, scope);

			ICPPFunctionTemplate function = resolveTemplateFunctions((Object[]) data.foundItems, name);
			if (function == null)
				return new ProblemBinding(name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray());
			if (function instanceof IProblemBinding)
				return function;

			if (name instanceof ICPPASTTemplateId) {
				((ICPPASTTemplateId) name).getTemplateName().setBinding(function);
			}
			IASTNode parent = name.getParent();
			while (parent instanceof IASTName)
				parent = parent.getParent();

			IASTParameterDeclaration[] ps = ((ICPPASTFunctionDeclarator) parent).getParameters();
			Object[] map_types= deduceTemplateFunctionArguments(function, ps, data.templateId);
			if (map_types == null) 
				return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_TYPE, name.toCharArray());
			
		    while (!(parent instanceof IASTDeclaration))
				parent = parent.getParent();

	    	final IType[] args = (IType[]) map_types[1];
	    	final ObjectMap argMap= (ObjectMap) map_types[0];
    		IBinding owner= function.getOwner();
    		ICPPTemplateInstance instance= getInstance(function, args);
    		if (instance == null) {
    			instance = createInstance(owner, function, argMap, args);
    			addInstance(function, args, instance);
    		}
    		if (instance instanceof ICPPInternalBinding) {
    			if (parent instanceof IASTSimpleDeclaration)
    				((ICPPInternalBinding) instance).addDeclaration(name);
    			else if (parent instanceof IASTFunctionDefinition)
    				((ICPPInternalBinding) instance).addDefinition(name);
    		}
		    return instance;
		} catch (DOMException e) {
			return e.getProblem();
		}
	}

	static protected ICPPFunctionTemplate resolveTemplateFunctions(Object[] items, IASTName name) {
		if (items == null)
			return null;
		ICPPFunctionTemplate[] templates = null;
		IBinding temp = null;
		for (Object o : items) {
			if (o instanceof IASTName) {
	            temp = ((IASTName) o).resolveBinding();
	            if (temp == null)
	                continue;
	        } else if (o instanceof IBinding) {
	            temp = (IBinding) o;
	        } else {
	            continue;
	        }

			if (temp instanceof ICPPTemplateInstance)
				temp = ((ICPPTemplateInstance) temp).getTemplateDefinition();
			if (temp instanceof ICPPFunctionTemplate)
				templates = (ICPPFunctionTemplate[]) ArrayUtil.append(ICPPFunctionTemplate.class, templates, temp);
		}

		if (templates == null)
			return null;

		IType[] templateArguments = null;

		if (name instanceof ICPPASTTemplateId) {
			templateArguments= createTemplateArgumentArray((ICPPASTTemplateId) name);
		}
		int numArgs = (templateArguments != null) ? templateArguments.length : 0;


		if (name.getParent() instanceof IASTName)
			name = (IASTName) name.getParent();
		IASTNode n = name.getParent();
		if (n instanceof ICPPASTQualifiedName) {
			n = n.getParent();
		}
		ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) n;
		IType[] functionParameters = createTypeArray(fdtor.getParameters());

		ICPPFunctionTemplate result = null;
		outer: for (int i = 0; i < templates.length && templates[i] != null; i++) {
			ICPPFunctionTemplate tmpl = templates[i];

			ObjectMap map = ObjectMap.EMPTY_MAP;
			try {
				map = deduceTemplateArguments(tmpl, functionParameters);
			} catch (DOMException e) {
			}

			if (map == null)
				continue;
			ICPPTemplateParameter[] params = null;
			try {
				params = tmpl.getTemplateParameters();
			} catch (DOMException e) {
				continue;
			}

			int numParams = params.length;
			IType arg = null;
			for (int j = 0; j < numParams; j++) {
				ICPPTemplateParameter param = params[j];
				if (j < numArgs && templateArguments != null) {
					arg = templateArguments[j];
				} else {
					arg = null;
				}
				if (map.containsKey(param)) {
					IType t = (IType) map.get(param);
					if (arg == null) {
						arg = t;
					} else if (!CPPTemplates.isSameTemplateArgument(t, arg)) {
						continue outer;
					}
				} else if (arg == null || !matchTemplateParameterAndArgument(param, arg, map)) {
					continue outer;
				}
			}
			//made it this far, its a match
			if (result != null) {
				return new CPPFunctionTemplate.CPPFunctionTemplateProblem(name, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray());
			}
			result = tmpl;
		}

		return result;
	}

	/**
	 * return Object[] of { ObjectMap, IType[] }
	 * @throws DOMException
	 */
	static protected Object[] deduceTemplateFunctionArguments(ICPPFunctionTemplate primaryTemplate,
			IASTParameterDeclaration[] ps, ICPPASTTemplateId id) throws DOMException {
		ICPPTemplateParameter[] templateParameters = primaryTemplate.getTemplateParameters();
		IType[] arguments= createTemplateArgumentArray(id);
		IType[] result = new IType[templateParameters.length];

		ObjectMap map = null;

		if (arguments.length == result.length) {
			map = new ObjectMap(result.length);
			for (int i = 0; i < templateParameters.length; i++) {
				result[i] = arguments[i];
				map.put(templateParameters, arguments[i]);
			}
			return new Object[] { map, result };
		}

		//else need to deduce some arguments
		IType[] paramTypes = createTypeArray(ps);
		map = deduceTemplateArguments(primaryTemplate, paramTypes);
		if (map != null) {
			for (int i = 0; i < templateParameters.length; i++) {
				ICPPTemplateParameter param = templateParameters[i];
				IType arg = null;
				if (i < arguments.length) {
					arg = arguments[i];
					map.put(param, arg);
				} else if (map.containsKey(param)) {
					arg = (IType) map.get(param);
				}

				if (arg == null || !matchTemplateParameterAndArgument(param, arg, map))
					return null;

				result[i] = arg;
			}
			return new Object[] { map, result };
		}

		return null;
	}

	public static ICPPTemplateInstance createInstance(IBinding owner, ICPPTemplateDefinition template, 
			ObjectMap argMap, IType[] args) {
		if (owner instanceof ICPPSpecialization) {
			ObjectMap map= ((ICPPSpecialization) owner).getArgumentMap();
			if (map != null) {
				for (int i = 0; i < map.size(); i++) {
					argMap.put(map.keyAt(i), map.getAt(i));
				}
			}
		}
		
		ICPPTemplateInstance instance = null;
		if (template instanceof ICPPClassType) {
			instance = new CPPClassInstance(owner, (ICPPClassType) template, argMap, args);
		} else if (owner instanceof ICPPClassType && template instanceof ICPPMethod) {
			if (template instanceof ICPPConstructor) {
				instance = new CPPConstructorInstance((ICPPClassType) owner, (ICPPConstructor) template, argMap, args);
			} else {
				instance = new CPPMethodInstance((ICPPClassType) owner, (ICPPMethod) template, argMap, args);
			}
		} else if (template instanceof ICPPFunction) {
			instance = new CPPFunctionInstance(owner, (ICPPFunction) template, argMap, args);
		}
		return instance;
	}

	public static ICPPSpecialization createSpecialization(ICPPClassSpecialization owner, IBinding decl, ObjectMap argMap) {
		
		// mstodo- specializations of partial specializations
		if (decl instanceof ICPPClassTemplatePartialSpecialization)
			return null;

		ICPPSpecialization spec = null;
		if (decl instanceof ICPPClassTemplate) {
			spec = new CPPClassTemplateSpecialization((ICPPClassTemplate) decl, owner, argMap);
		} else if (decl instanceof ICPPClassType) {
			spec = new CPPClassSpecialization((ICPPClassType) decl, owner, argMap);
		} else if (decl instanceof ICPPField) {
			spec = new CPPFieldSpecialization(decl, owner, argMap);
		} else if (decl instanceof ICPPFunctionTemplate) {
			if (decl instanceof ICPPConstructor)
				spec = new CPPConstructorTemplateSpecialization(decl, owner, argMap);
			else if (decl instanceof ICPPMethod)
				spec = new CPPMethodTemplateSpecialization(decl, owner, argMap);
			else
				spec = new CPPFunctionTemplateSpecialization(decl, owner, argMap);
		} else if (decl instanceof ICPPConstructor) {
			spec = new CPPConstructorSpecialization(decl, owner, argMap);
		} else if (decl instanceof ICPPMethod) {
			spec = new CPPMethodSpecialization(decl, owner, argMap);
		} else if (decl instanceof ICPPFunction) {
			spec = new CPPFunctionSpecialization(decl, owner, argMap);
		} else if (decl instanceof ITypedef) {
		    spec = new CPPTypedefSpecialization(decl, owner, argMap);
		}
		return spec;
	}
	
	public static IValue instantiateValue(IValue value, ObjectMap argMap) {
		if (value == null)
			return null;
		// mstodo instantiate values
		return value;
	}

	/**
	 * This method propagates the specialization of a member to the types used by the member.
	 * @param type a type to instantiate.
	 * @param argMap a mapping between template parameters and the corresponding arguments.
	 */
	public static IType instantiateType(IType type, ObjectMap argMap, ICPPClassSpecialization within) {
		try {
			if (argMap == null)
				return type;

			if (type instanceof IFunctionType) {
				IType ret = null;
				IType[] params = null;
				final IType r = ((IFunctionType) type).getReturnType();
				ret = instantiateType(r, argMap, within);
				IType[] ps = ((IFunctionType) type).getParameterTypes();
				params = instantiateTypes(ps, argMap, within);
				if (ret == r && params == ps) {
					return type;
				}
				return new CPPFunctionType(ret, params, ((ICPPFunctionType) type).getThisType());
			} 

			if (type instanceof ICPPTemplateParameter) {
				IType t = (IType) argMap.get(type);
				if (t != null) {
					return t;
				}
				for (int i = 0; i < argMap.size(); i++) {
					Object key = argMap.keyAt(i);
					if (key instanceof IType && type.isSameType((IType) key)) {
						return (IType) argMap.getAt(i);
					}
				}
				return type;
			} 

			if (type instanceof ICPPUnknownBinding) {
				IBinding binding= resolveUnknown((ICPPUnknownBinding) type, argMap, within);
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
					IType newOwner= instantiateType(parentType, argMap, within);
					if (newOwner != typeOwner && newOwner instanceof ICPPClassSpecialization) {
						return (IType) ((ICPPClassSpecialization) newOwner).specializeMember(typeAsBinding);
					}
					return type;
				}
			}		

			if (type instanceof ITypeContainer) {
				IType nestedType = ((ITypeContainer) type).getType();
				IType newNestedType = instantiateType(nestedType, argMap, within);
				if (type instanceof ICPPPointerToMemberType) {
					ICPPPointerToMemberType ptm = (ICPPPointerToMemberType) type;
					IType memberOfClass = ptm.getMemberOfClass();
					IType newMemberOfClass = instantiateType(memberOfClass, argMap, within);
					if ((newNestedType != nestedType || newMemberOfClass != memberOfClass) &&
							newMemberOfClass instanceof ICPPClassType) {
						return new CPPPointerToMemberType(newNestedType, (ICPPClassType) newMemberOfClass,
								ptm.isConst(), ptm.isVolatile());
					}
				}
				if (newNestedType != nestedType) {
					// bug 249085 make sure not to add unnecessary qualifications
					if (type instanceof IQualifierType) {
						IQualifierType qt1= (IQualifierType) type;
						if (newNestedType instanceof IQualifierType) {
							IQualifierType qt2= (IQualifierType) newNestedType;
							return new CPPQualifierType(qt2.getType(), qt1.isConst() || qt2.isConst(), qt1.isVolatile() || qt2.isVolatile());
						} else if (newNestedType instanceof IPointerType) {
							IPointerType pt2= (IPointerType) newNestedType;
							return new CPPPointerType(pt2.getType(), qt1.isConst() || pt2.isConst(), qt1.isVolatile() || pt2.isVolatile());
						}
					}
					type = (IType) type.clone();
					((ITypeContainer) type).setType(newNestedType);
					return type;
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
	 * @param argMap template argument map
	 * @return an array containing instantiated types.
	 */
	public static IType[] instantiateTypes(IType[] types, ObjectMap argMap, ICPPClassSpecialization within) {
		// Don't create a new array until it's really needed.
		IType[] result = types;
		for (int i = 0; i < types.length; i++) {
			IType type = CPPTemplates.instantiateType(types[i], argMap, within);
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
		if (name == null) {
			return null;
		}

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
		if (!(parent instanceof IASTDeclaration)) {
			return null;
		}
		
		parent = parent.getParent();
		if (parent instanceof ICPPASTTemplateDeclaration) {
		    ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) parent;

			IASTName[] ns;
			if (name instanceof ICPPASTQualifiedName) {
				ns = ((ICPPASTQualifiedName) name).getNames();
				name = ns[ns.length - 1];
			} else if (name.getParent() instanceof ICPPASTQualifiedName) {
				ns = ((ICPPASTQualifiedName) name.getParent()).getNames();
			} else {
				// one name: use innermost template declaration
				return templateDecl;
			}
			
			// start with outermost template declaration
		    while (templateDecl.getParent() instanceof ICPPASTTemplateDeclaration)
		        templateDecl = (ICPPASTTemplateDeclaration) templateDecl.getParent();
		    
			for (int j = 0; j < ns.length; j++) {
				final IASTName singleName = ns[j];
				if (singleName == name) {
					if (singleName instanceof ICPPASTTemplateId || j == ns.length-1) {
						return templateDecl;
					}
					return null;
				}
				if (singleName instanceof ICPPASTTemplateId) {
					final IASTDeclaration next= templateDecl.getDeclaration();
					if (next instanceof ICPPASTTemplateDeclaration) {
						templateDecl= (ICPPASTTemplateDeclaration) next;
					} else {
						return null;
					}
				}
			}
		}
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

	private static class ClearBindingAction extends CPPASTVisitor {
		public ObjectSet<IBinding> bindings = null;
		public ClearBindingAction(ObjectSet<IBinding> bindings) {
			shouldVisitNames = true;
			shouldVisitStatements = true;
			this.bindings = bindings;
		}
		@Override
		public int visit(IASTName name) {
			if (name.getBinding() != null) {
				IBinding binding = name.getBinding();
				boolean clear = bindings.containsKey(name.getBinding());
				if (!clear && binding instanceof ICPPTemplateInstance) {
					IType[] args = ((ICPPTemplateInstance) binding).getArguments();
					for (IType arg : args) {
						if (arg instanceof IBinding) {
							if (bindings.containsKey((IBinding)arg)) {
								clear = true;
								break;
							}
						}
					}
				}
				if (clear) {
					if (binding instanceof ICPPInternalBinding)
						((ICPPInternalBinding) binding).removeDeclaration(name);
					name.setBinding(null);
				}
			}
			return PROCESS_CONTINUE;
		}
		@Override
		public int visit(IASTStatement statement) {
			return PROCESS_SKIP;
		}
	}

	public static boolean isSameTemplate(ICPPTemplateDefinition definition, IASTName name) {
		ICPPTemplateParameter[] defParams = null;
		try {
			defParams = definition.getTemplateParameters();
		} catch (DOMException e1) {
			return false;
		}
		ICPPASTTemplateDeclaration templateDecl = getTemplateDeclaration(name);
		if (templateDecl == null)
			return false;

		ICPPASTTemplateParameter[] templateParams = templateDecl.getTemplateParameters();
		if (defParams.length != templateParams.length)
			return false;

		ObjectSet<IBinding> bindingsToClear = null;
		for (int i = 0; i < templateParams.length; i++) {
			IASTName tn = getTemplateParameterName(templateParams[i]);
			if (tn.getBinding() != null)
				return (tn.getBinding() == defParams[i]);
			if (bindingsToClear == null)
				bindingsToClear = new ObjectSet<IBinding>(templateParams.length);
			tn.setBinding(defParams[i]);
			if (defParams[i] instanceof ICPPInternalBinding)
				((ICPPInternalBinding) defParams[i]).addDeclaration(tn);
			bindingsToClear.put(defParams[i]);
		}

		boolean result = false;
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTFunctionDeclarator) {
			try {
				IASTParameterDeclaration[] params = ((ICPPASTFunctionDeclarator) parent).getParameters();
				IParameter[] ps = ((ICPPFunction) definition).getParameters();
				if (ps.length == params.length) {
					int i = 0;
					for (; i < ps.length; i++) {
						IType t1 = CPPVisitor.createType(params[i].getDeclarator());
						IType t2 = ps[i].getType();
						if (! t1.isSameType(t2)) {
							break;
						}
					}
					if (i == ps.length)
						result = true;
				}
			} catch (DOMException e) {
			}
		} else if (parent instanceof IASTDeclSpecifier) {
			if (name instanceof ICPPASTTemplateId) {
				if (definition instanceof ICPPClassTemplatePartialSpecialization) {
					ICPPClassTemplatePartialSpecialization spec = (ICPPClassTemplatePartialSpecialization) definition;
					IType[] args= createTemplateArgumentArray((ICPPASTTemplateId)name);
					try {
						IType[] specArgs = spec.getArguments();
						if (args.length == specArgs.length) {
							int i = 0;
							for (; i < args.length; i++) {
								if (isSameTemplateArgument(specArgs[i], args[i]))
									continue;
								break;
							}
							result = (i == args.length);
						}
					} catch (DOMException e) {
						result = false;
					}
				}
			} else {
				result = CharArrayUtils.equals(definition.getNameCharArray(), name.toCharArray());
			}
		}

		if (bindingsToClear != null && !result) {
			ClearBindingAction action = new ClearBindingAction(bindingsToClear);
			templateDecl.accept(action);
		}

		return result;
	}

	/**
	 * @param argA may be null
	 * @param argB may be null
	 * @return whether the two specified template arguments are the same
	 */
	public static final boolean isSameTemplateArgument(IType argA, IType argB) {
		if (argA == argB)
			return true;
		
		// special case treatment for non-type integral parameters
		if (argA instanceof ICPPBasicType && argB instanceof ICPPBasicType) {
			try {
				IASTExpression eA= ((ICPPBasicType) argA).getValue();
				IASTExpression eB= ((ICPPBasicType) argB).getValue();				
				if (eA != null && eB != null) {
					// TODO - we should normalize template arguments
					// rather than check their original expressions
					// are equivalent.
					return isNonTypeArgumentConvertible(argA, argB) && expressionsEquivalent(eA, eB);
				}
			} catch (DOMException de) {
				CCorePlugin.log(de);
				return false;
			}
		}
		
		return argA != null && argB != null && argA.isSameType(argB);
	}
	
	/**
	 * @param id the template id containing the template arguments
	 * @return an array of template arguments, currently modeled as IType objects. The
	 * empty IType array is returned if id is <code>null</code>
	 */
	static public IType[] createTemplateArgumentArray(ICPPASTTemplateId id) {
		IType[] result= IType.EMPTY_TYPE_ARRAY;
		if (id != null) {
			IASTNode[] params= id.getTemplateArguments();
			result = new IType[params.length];
			for (int i = 0; i < params.length; i++) {
				IASTNode param= params[i];
				/*
				 * id-expression's which resolve to const variables can be
				 * modeled by the type of the initialized expression (which
				 * will include its value)
				 */
				if (param instanceof IASTIdExpression) {
					param= CPPVisitor.reverseConstantPropagationLookup((IASTIdExpression)param);
				}
				
				IType type= CPPVisitor.createType(param);
				// prevent null pointer exception when the type cannot be determined
				// happens when templates with still ambiguous template-ids are accessed during
				// resolution of other ambiguities.
				result[i]= type == null ? new CPPBasicType(-1, 0) : type;
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

	static protected IFunction[] selectTemplateFunctions(
			ObjectSet<IFunction> templates,
			Object[] functionArguments, IASTName name) {
		
		if (templates == null || templates.size() == 0)
			return null;

		IFunction[] instances = null;

		int size = templates.size();

		int numTemplateArgs = 0;
		IType[] templateArguments = null;
		if (name instanceof ICPPASTTemplateId)	{
			templateArguments = createTemplateArgumentArray((ICPPASTTemplateId) name);
			numTemplateArgs = templateArguments.length;
		}

		IType[] fnArgs= createTypeArray(functionArguments);

		outer: for (int idx = 0; idx < size; idx++) {
			ICPPFunctionTemplate template = (ICPPFunctionTemplate) templates.keyAt(idx);

			ObjectMap map = null;
			try {
				map = deduceTemplateArguments(template, fnArgs);
			} catch (DOMException e) {
				continue;
			}
			if (map == null)
				continue;

			ICPPTemplateParameter[] templateParams = null;
			try {
				templateParams = template.getTemplateParameters();
			} catch (DOMException e1) {
				continue outer;
			}
			int numTemplateParams = templateParams.length;

			IType[] instanceArgs = null;
			for (int i = 0; i < numTemplateParams; i++) {
				IType arg = (i < numTemplateArgs && templateArguments != null) ? templateArguments[i] : null;
				IType mapped = (IType) map.get(templateParams[i]);

				if (arg != null && mapped != null) {
					if (arg.isSameType(mapped)) // compare as IType: 'mapped' is not a template argument
						instanceArgs = (IType[]) ArrayUtil.append(IType.class, instanceArgs, arg);
					else
						continue outer;
				} else if (arg == null && mapped == null) {
				    IType def = null;
				    try {
					    if (templateParams[i] instanceof ICPPTemplateTypeParameter) {
	                        def = ((ICPPTemplateTypeParameter) templateParams[i]).getDefault();
					    } else if (templateParams[i] instanceof ICPPTemplateTemplateParameter) {
					        def = ((ICPPTemplateTemplateParameter) templateParams[i]).getDefault();
					    } else if (templateParams[i] instanceof ICPPTemplateNonTypeParameter) {
					        def = CPPVisitor.getExpressionType(((ICPPTemplateNonTypeParameter) templateParams[i]).getDefault());
					    }
				    } catch (DOMException e) {
				        continue outer;
				    }
				    if (def != null) {
				        if (def instanceof ICPPTemplateParameter) {
				            for (int j = 0; j < i; j++) {
                                if (templateParams[j] == def && instanceArgs != null) {
                                    def = instanceArgs[j];
                                }
                            }
				        }
				        instanceArgs = (IType[]) ArrayUtil.append(IType.class, instanceArgs, def);
				    } else {
				        continue outer;
				    }
				} else {
					instanceArgs = (IType[]) ArrayUtil.append(IType.class, instanceArgs, (arg != null) ? arg : mapped);
				}
			}
			instanceArgs= (IType[]) ArrayUtil.trim(IType.class, instanceArgs);
			IBinding temp= instantiate(template, instanceArgs);
			if (temp instanceof IFunction) {
				instances = (IFunction[]) ArrayUtil.append(IFunction.class, instances, temp);
			}
		}

		return (IFunction[]) ArrayUtil.trim(IFunction.class, instances);
	}

	/**
	 *
	 * @param template
	 * @param args
	 * @return
	 *
	 * A type that is specified in terms of template parameters (P) is compared with an actual
	 * type (A), and an attempt is made to find template argument vaules that will make P,
	 * after substitution of the deduced values, compatible with A.
	 * @throws DOMException
	 */
	static private ObjectMap deduceTemplateArguments(ICPPFunctionTemplate template, IType[] arguments) throws DOMException{
		ICPPFunction function = (ICPPFunction) template;
		IType[] functionParameters = null;
		try {
			functionParameters = function.getType().getParameterTypes();
		} catch (DOMException e) {
			return null;
		}
		return deduceTemplateArguments(functionParameters, arguments, false);
	}
	
	/**
	 * @param specArgs
	 * @param args
	 * @param all whether to match all arguments
	 * @return the mapping required to pairwise match the specified arguments, or null if no mapping exists
	 */
	public static ObjectMap deduceTemplateArguments(final IType[] specArgs, final IType[] args, final boolean all) {
		if (specArgs == null || (all && specArgs.length != args.length)) {
			return null;
		}
		ObjectMap map= new ObjectMap(specArgs.length);
		int len= all ? specArgs.length : Math.min(specArgs.length, args.length);
		for (int j= 0; j < len; j++) {
			try {
				if (!deduceTemplateArgument(map, specArgs[j], args[j])) {
					return null;
				}
			} catch (DOMException de) {
				return null;
			}
		}
		return map;
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

	private static boolean expressionsEquivalent(IASTExpression e1, IASTExpression e2) {
		if (e1 == null)
			return true;

		e1= CPPVisitor.reverseConstantPropagationLookup(e1);
		e2= CPPVisitor.reverseConstantPropagationLookup(e2);		
		
		if (e1 instanceof IASTLiteralExpression && e2 instanceof IASTLiteralExpression) {
			IType t1= e1.getExpressionType();
			IType t2= e2.getExpressionType();
			try {
				if (t1 instanceof ICPPBasicType && t2 instanceof ICPPBasicType) {
					BigInteger i1= CPPVisitor.parseIntegral(e1.toString());
					BigInteger i2= CPPVisitor.parseIntegral(e2.toString());
					return i1.equals(i2);
				}
			} catch (NumberFormatException nfe) {
				/* fall through */
			}
			return e1.toString().equals(e2.toString());
		}
		return false;
	}
	
	private static boolean deduceTemplateArgument(ObjectMap map, IType p, IType a) throws DOMException {
		boolean pIsAReferenceType = (p instanceof ICPPReferenceType);
		p = getParameterTypeForDeduction(p);
		a = getArgumentTypeForDeduction(a, pIsAReferenceType);

		if (p instanceof IBasicType) {
			if (a instanceof IBasicType) {
				IBasicType pbt= (IBasicType) p;
				IBasicType abt= (IBasicType) a;
				
				// non-type argument comparison
				if (pbt.getValue() != null && abt.getValue() != null) {
					return isNonTypeArgumentConvertible(p, a)
						&& expressionsEquivalent(pbt.getValue(), abt.getValue());
				}
				
				// type argument comparison
				return p.isSameType(a);
			}
		} else {
			while (p != null) {
				while (a instanceof ITypedef)
					a = ((ITypedef) a).getType();
				if (p instanceof IBasicType) {
					return p.isSameType(a);
				} else if (p instanceof ICPPPointerToMemberType) {
					if (!(a instanceof ICPPPointerToMemberType))
						return false;
					if (!deduceTemplateArgument(map, ((ICPPPointerToMemberType) p).getMemberOfClass(),
							((ICPPPointerToMemberType) a).getMemberOfClass())) {
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
					if (!deduceTemplateArgument(map, ((IFunctionType) p).getReturnType(),
							((IFunctionType) a).getReturnType())) {
						return false;
					}
					IType[] pParams = ((IFunctionType) p).getParameterTypes();
					IType[] aParams = ((IFunctionType) a).getParameterTypes();
					if (pParams.length != aParams.length)
						return false;
					for (int i = 0; i < pParams.length; i++) {
						if (!deduceTemplateArgument(map, pParams[i], aParams[i]))
							return false;
					}
					return true;
				} else if (p instanceof ICPPTemplateParameter) {
					if (map.containsKey(p)) {
						IType current = (IType) map.get(p);
						return current.isSameType(a);
					}
					if (a == null)
						return false;
					map.put(p, a);
					return true;
				} else if (p instanceof ICPPTemplateInstance) {
					if (!(a instanceof ICPPTemplateInstance))
						return false;
					ICPPTemplateInstance pInst = (ICPPTemplateInstance) p;
					ICPPTemplateInstance aInst = (ICPPTemplateInstance) a;

					IType[] pArgs = pInst.getArguments();
					pArgs= pArgs == null ? IType.EMPTY_TYPE_ARRAY : pArgs; // aftodo - unnecessary?
					
					ObjectMap aMap = aInst.getArgumentMap();
					if (aMap != null && !(aInst.getTemplateDefinition() instanceof ICPPClassTemplatePartialSpecialization)) {
						ICPPTemplateParameter[] aParams = aInst.getTemplateDefinition().getTemplateParameters();
						if (pArgs.length != aParams.length)
							return false;
						for (int i = 0; i < pArgs.length; i++) {
							IType t = (IType) aMap.get(aParams[i]);
							if (t == null || !deduceTemplateArgument(map, pArgs[i], t))
								return false;
						}
					} else {
						IType[] aArgs = aInst.getArguments();
						aArgs= aArgs == null ? IType.EMPTY_TYPE_ARRAY : aArgs; // aftodo - unnecessary?
						
						if (aArgs.length != pArgs.length)
							return false;
						for (int i = 0; i < pArgs.length; i++) {
							if (!deduceTemplateArgument(map, pArgs[i], aArgs[i]))
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
	static private IType[] createArgsForFunctionTemplateOrdering(ICPPFunctionTemplate template)
			throws DOMException{
		ICPPTemplateParameter[] paramList = template.getTemplateParameters();
		int size = paramList.length;
		IType[] args = new IType[size];
		for (int i = 0; i < size; i++) {
			ICPPTemplateParameter param = paramList[i];
			if (param instanceof ICPPTemplateNonTypeParameter) {
				IType t = ((ICPPTemplateNonTypeParameter) param).getType();
				if (t instanceof CPPBasicType) {
					CPPASTLiteralExpression exp = new CPPASTLiteralExpression();
					exp.setValue(String.valueOf(i));
					CPPBasicType temp = (CPPBasicType) t.clone();
					temp.setFromExpression(exp); // mstodo is that necessary??
					args[i] = temp;
				}
			} else {
				args[i] = new CPPBasicType(-1, 0);
			}
		}
		return args;
	}

	static protected int orderTemplateFunctions(ICPPFunctionTemplate f1, ICPPFunctionTemplate f2)
			throws DOMException {
		// Using the transformed parameter list, perform argument deduction against the other
		// function template
		ObjectMap m1 = null;
		ObjectMap m2 = null;

		IType[] args = createArgsForFunctionTemplateOrdering(f1);
		IBinding function = instantiate(f1, args);
		if (function instanceof ICPPFunction)
			m1 = deduceTemplateArguments(f2, ((ICPPFunction) function).getType().getParameterTypes());

		args = createArgsForFunctionTemplateOrdering(f2);
		function = instantiate(f2, args);
		if (function instanceof ICPPFunction)
			m2 = deduceTemplateArguments(f1, ((ICPPFunction) function).getType().getParameterTypes());
		
		// The transformed template is at least as specialized as the other iff the deduction
		// succeeds and the deduced parameter types are an exact match.
		// A template is more specialized than another iff it is at least as specialized as the
		// other template and that template is not at least as specialized as the first.
		if (m1 == null) {
			if (m2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (m2 == null) {
				return 1;
			} else {
				// Count the number of cv-qualifications. The function with a lower number
				// of cv-qualifications is more specialized.
				int d1 = 0;
				for (int i = 0; i < m1.size(); i++) {
					if (m1.getAt(i) instanceof IQualifierType) {
						d1++;
					}
				}
				int d2 = 0;
				for (int i = 0; i < m2.size(); i++) {
					if (m2.getAt(i) instanceof IQualifierType) {
						d2++;
					}
				}
				return d1 - d2;
			}
		}
	}

	static public ICPPTemplateDefinition selectSpecialization(ICPPClassTemplate template, IType[] args)
			throws DOMException {
		if (template == null) {
			return null;
		}

		args= SemanticUtil.getSimplifiedTypes(args);
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
			ObjectMap map= deduceTemplateArguments(spec.getArguments(), args, true);
			if (map != null) {
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
		public IFunctionType getType() {
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
		ICPPTemplateDefinition template = specialization;
		IType[] args = null;
		try {
			args = specialization.getArguments();
		} catch (DOMException e1) {
			return null;
		}

		IType paramType = (IType) instantiate(template, args);
		if (paramType == null)
			return null;
		
		IParameter[] functionParameters = new IParameter[] { new CPPParameter(paramType) };

		try {
			return new CPPImplicitFunctionTemplate(specialization.getTemplateParameters(), functionParameters);
		} catch (DOMException e) {
			return null;
		}
	}

	static private boolean isValidArgument(ICPPTemplateParameter param, IType argument) {
		try {
			while (argument instanceof ITypeContainer) {
				argument = ((ITypeContainer) argument).getType();
			}
		} catch (DOMException e) {
			return false;
		}
		return !(argument instanceof IProblemBinding);
	}

	static protected boolean matchTemplateParameterAndArgument(ICPPTemplateParameter param, IType argument,
			ObjectMap map) {
		if (!isValidArgument(param, argument)) {
			return false;
		}
		if (param instanceof ICPPTemplateTypeParameter) {
			return true;
		} else if (param instanceof ICPPTemplateTemplateParameter) {
			if (!(argument instanceof ICPPTemplateDefinition))
				return false;

			ICPPTemplateParameter[] pParams = null;
			ICPPTemplateParameter[] aParams = null;
			try {
				pParams = ((ICPPTemplateTemplateParameter) param).getTemplateParameters();
				aParams = ((ICPPTemplateDefinition) argument).getTemplateParameters();
			} catch (DOMException e) {
				return false;
			}

			int size = pParams.length;
			if (aParams.length != size) {
				return false;
			}

			for (int i = 0; i < size; i++) {
				if ((pParams[i] instanceof ICPPTemplateTypeParameter && !(aParams[i] instanceof ICPPTemplateTypeParameter)) ||
						(pParams[i] instanceof ICPPTemplateTemplateParameter && !(aParams[i] instanceof ICPPTemplateTemplateParameter)) ||
						(pParams[i] instanceof ICPPTemplateNonTypeParameter && !(aParams[i] instanceof ICPPTemplateNonTypeParameter))) {
					return false;
				}
			}

			return true;
		} else {
			try {
				IType pType = ((ICPPTemplateNonTypeParameter) param).getType();
				if (map != null && pType != null && map.containsKey(pType)) {
					pType = (IType) map.get(pType);
				}

				if (!isNonTypeArgumentConvertible(pType, argument)) {
					return false;
				}
			} catch (DOMException e) {
				return false;
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
	    	try {
	    		paramType = new CPPPointerType(((IArrayType) paramType).getType());
			} catch (DOMException e) {
				paramType = e.getProblem();
			}
		}
		Cost cost = Conversions.checkStandardConversionSequence(arg, paramType, false);
		return cost != null && cost.rank != Cost.NO_MATCH_RANK;
	}

	public static boolean isDependentType(IType t) {
		if (t instanceof ICPPTemplateParameter)
			return true;
		t = SemanticUtil.getUltimateType(t, false);
		return t instanceof ICPPUnknownBinding;
	}
	
	public static boolean containsDependentArg(ObjectMap argMap) {
		for (Object arg : argMap.valueArray()) {
			if (isDependentType((IType)arg))
				return true;
		}
		return false;
	}

	/**
	 * Attempts to (partially) resolve an unknown binding with the given arguments.
	 */
	private static IBinding resolveUnknown(ICPPUnknownBinding unknown, ObjectMap argMap, ICPPClassSpecialization within) throws DOMException {
        if (unknown instanceof ICPPDeferredTemplateInstance) {
        	return resolveDeferredClassInstance((ICPPDeferredClassInstance) unknown, argMap, within);
        }

        final IBinding owner= unknown.getOwner();
        IBinding result = unknown;
        IType t = null;
		if (owner instanceof ICPPTemplateTypeParameter) {
			t = CPPTemplates.instantiateType((ICPPTemplateTypeParameter) owner, argMap, null);
		} else if (owner instanceof ICPPUnknownClassType) {
        	IBinding binding= resolveUnknown((ICPPUnknownBinding) owner, argMap, within);
        	if (binding instanceof IType) {
                t = (IType) binding;
            }
        } 
        if (t != null) {
            t = SemanticUtil.getUltimateType(t, false);
            if (t instanceof ICPPUnknownBinding) {
            	if (unknown instanceof ICPPUnknownClassInstance) {
            		ICPPUnknownClassInstance ucli= (ICPPUnknownClassInstance) unknown;
            		final IType[] arguments = ucli.getArguments();
					IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap, within);
            		if (!t.equals(owner) && newArgs != arguments) {
            			result= new CPPUnknownClassInstance((ICPPUnknownBinding) t, ucli.getUnknownName(), newArgs);
            		}
            	} else if (unknown instanceof ICPPUnknownClassType) {
            		if (!t.equals(owner)) {
            			result= new CPPUnknownClass((ICPPUnknownBinding)t, ((ICPPUnknownClassType)unknown).getUnknownName());
            		}
            	}
            } else if (t instanceof ICPPClassType) {
	            IScope s = ((ICPPClassType) t).getCompositeScope();
	            if (s != null && ASTInternal.isFullyCached(s)) {
	            	// If name did not come from an AST but was created just to encapsulate
	            	// a simple identifier, we should not use getBinding method since it may
	            	// lead to a NullPointerException.
	            	IASTName name= unknown.getUnknownName();
	            	if (name != null) {
	            		if (name.getParent() != null) {
	            			result = s.getBinding(name, true);
	            		} else {
	            			IBinding[] bindings = s.find(name.toString());
	            			if (bindings != null && bindings.length > 0) {
	            				result = bindings[0];
	            			} 
	            		}
	    	            if (unknown instanceof ICPPUnknownClassInstance && result instanceof ICPPTemplateDefinition) {
	    	            	IType[] newArgs = CPPTemplates.instantiateTypes(((ICPPUnknownClassInstance) unknown).getArguments(), argMap, within);
	    	            	result = instantiate((ICPPTemplateDefinition) result, newArgs);
	    	            }
	            	}
	            }
            }
        }
        
        return result;
	}
	
	private static IBinding resolveDeferredClassInstance(ICPPDeferredClassInstance dci, ObjectMap argMap, ICPPClassSpecialization within) {
		IType[] arguments = dci.getArguments();
		IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap, within);

		boolean changed= arguments != newArgs;
		ICPPClassTemplate classTemplate = dci.getClassTemplate();
		if (argMap.containsKey(classTemplate)) {
			classTemplate = (ICPPClassTemplate) argMap.get(classTemplate);
			changed= true;
		}

		if (changed) {
			IBinding inst= instantiate(classTemplate, newArgs);
			if (inst != null)
				return inst;
		}
		return dci;
	}
}
