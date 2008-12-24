/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * @author aniefer
 */
public class CPPSemantics {
	/**
	 * The maximum depth to search ancestors before assuming infinite looping.
	 */
	public static final int MAX_INHERITANCE_DEPTH= 10;
	
    public static final ASTNodeProperty STRING_LOOKUP_PROPERTY = new ASTNodeProperty("CPPSemantics.STRING_LOOKUP_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	public static final char[] EMPTY_NAME_ARRAY = new char[0];
	public static final String EMPTY_NAME = ""; //$NON-NLS-1$
	public static final char[] OPERATOR_ = new char[] {'o','p','e','r','a','t','o','r',' '};  
	public static final IType VOID_TYPE = new CPPBasicType(IBasicType.t_void, 0);

	// Set to true for debugging.
	public static boolean traceBindingResolution = false;
	
	static protected IBinding resolveBinding(IASTName name) {
		if (traceBindingResolution) {
			System.out.println("Resolving " + name); //$NON-NLS-1$
		}
		// 1: get some context info off of the name to figure out what kind of lookup we want
		LookupData data = createLookupData(name, true);
		
		try {
            // 2: lookup
            lookup(data, name);
        } catch (DOMException e) {
            data.problem = (ProblemBinding) e.getProblem();
        }
		
		if (data.problem != null)
		    return data.problem;
		
		// 3: resolve ambiguities
		IBinding binding;
        try {
            binding = resolveAmbiguities(data, name);
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        // 4: post processing
		binding = postResolution(binding, data);
		if (traceBindingResolution) {
			System.out.println("Resolved " + name + " to " + DebugUtil.toStringWithClass(binding)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return binding;
	}

	protected static IBinding postResolution(IBinding binding, IASTName name) {
		LookupData data = createLookupData(name, true);
		return postResolution(binding, data);
	}
	
	/**
     * @param binding
     * @param data
     * @param name
     * @return
     */
    private static IBinding postResolution(IBinding binding, LookupData data) {
        if (data.checkAssociatedScopes()) {
            //3.4.2 argument dependent name lookup, aka Koenig lookup
            try {
                IScope scope = (binding != null) ? binding.getScope() : null;
                if (scope == null || !(scope instanceof ICPPClassScope)) {
                    data.ignoreUsingDirectives = true;
                    data.forceQualified = true;
                    for (int i = 0; i < data.associated.size(); i++) {
                    	lookup(data, data.associated.keyAt(i));
                    }
                    binding = resolveAmbiguities(data, data.astName);
                }
            } catch (DOMException e) {
                binding = e.getProblem();
            }
        }
        if (binding == null && data.checkClassContainingFriend()) {
        	//3.4.1-10 if we don't find a name used in a friend declaration in the member declaration's class
        	//we should look in the class granting friendship
        	IASTNode parent = data.astName.getParent();
        	while (parent != null && !(parent instanceof ICPPASTCompositeTypeSpecifier))
        		parent = parent.getParent();
        	if (parent instanceof ICPPASTCompositeTypeSpecifier) {
        		IScope scope = ((ICPPASTCompositeTypeSpecifier)parent).getScope();
        		try {
		    		lookup(data, scope);
		    		binding = resolveAmbiguities(data, data.astName);
        		} catch (DOMException e) {
        			binding = e.getProblem();
        		}
        	}
        }

        /* 14.6.1-1: Class template name without argument list is equivalent to the injected-class-name followed by
		 * the template-parameters of the class template enclosed in <> */
		if (binding instanceof ICPPClassTemplate) {
			ASTNodeProperty prop = data.astName.getPropertyInParent();
			if (prop != ICPPASTQualifiedName.SEGMENT_NAME && prop != ICPPASTTemplateId.TEMPLATE_NAME &&
					binding instanceof ICPPInternalBinding) {
				try {
					IASTNode def = ((ICPPInternalBinding)binding).getDefinition();
					if (def != null) {
						def = def.getParent();
						IASTNode parent = data.astName.getParent();
						while (parent != null) {
							if (parent == def) {
								binding = CPPTemplates.instantiateWithinClassTemplate((ICPPClassTemplate) binding);
								break;
							}
							if (parent instanceof ICPPASTNamespaceDefinition)
								break;
							parent = parent.getParent();
						}
					}
				} catch (DOMException e) {
				}
			}
			
			/* If the class template name is used as a type name in a simple declaration,
             * outside of the class template scope, mark it as a problem.
			 */
			if (binding instanceof ICPPClassTemplate) {
				IASTNode parent= data.astName.getParent();
				if (parent instanceof IASTNamedTypeSpecifier) {
					if (parent.getParent() instanceof IASTSimpleDeclaration) {
						binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE, data.name());
					}
				}
			}
		}
		
        if (binding instanceof ICPPClassType && data.considerConstructors) {
        	ICPPClassType cls = (ICPPClassType) binding;
        	if (data.astName instanceof ICPPASTTemplateId && cls instanceof ICPPTemplateDefinition) {
        		ICPPASTTemplateId id = (ICPPASTTemplateId) data.astName;
        		IType[] args = CPPTemplates.createTemplateArgumentArray(id);
        		IBinding inst = ((ICPPInternalTemplateInstantiator)cls).instantiate(args); 
        		cls = inst instanceof ICPPClassType && !(inst instanceof ICPPDeferredTemplateInstance) ? (ICPPClassType)inst : cls; 
        	}
		    if (cls != null) {
			    try {
	                //force resolution of constructor bindings
	                IBinding[] ctors = cls.getConstructors();
	                if (ctors.length > 0 && !(ctors[0] instanceof IProblemBinding)) {
		                //then use the class scope to resolve which one.
		    		    binding = ((ICPPClassScope)cls.getCompositeScope()).getBinding(data.astName, true);
	                }
	            } catch (DOMException e) {
	                binding = e.getProblem();
	            }
		    }
		    
		}
        
        // in template declarations the template-ids get instantiated to deferred instances, revert that.
		IASTName name = data.astName;
		if (name instanceof ICPPASTTemplateId) {
			if (CPPTemplates.getTemplateDeclaration(name) != null && binding instanceof ICPPDeferredTemplateInstance) {
				ICPPDeferredTemplateInstance deferred= (ICPPDeferredTemplateInstance) binding;
				boolean useOriginal= true;
				final ObjectMap argMap = deferred.getArgumentMap();
				if (argMap != null) {
					for (int i = 0; useOriginal && i < argMap.size(); i++) {
						final Object key = argMap.keyAt(i);
						if (!key.equals(argMap.getAt(i))) {
							// bug 231868 non type parameters are modeled via their type :-(
							if (key instanceof ICPPTemplateNonTypeParameter == false) {
								useOriginal= false;
								break;
							}
						}
					}
				}
				if (useOriginal) {
					binding= deferred.getSpecializedBinding();
				}
			}
		}
		if (name.getParent() instanceof ICPPASTTemplateId) {
			if (binding instanceof ICPPTemplateInstance) {
				IBinding b = binding;
				binding = ((ICPPTemplateInstance)binding).getSpecializedBinding();
				name.setBinding(binding);
				name = (IASTName) name.getParent();
				name.setBinding(b);
			} else {
				name = (IASTName) name.getParent();
			}
		}
		if (name.getParent() instanceof ICPPASTQualifiedName) {
			if (name == ((ICPPASTQualifiedName)name.getParent()).getLastName())
				name = (IASTName) name.getParent();
		}
		
		// if the lookup in base-classes ran into a deferred instance, use the computed unknown binding.
		if (binding == null && data.unknownBinding != null) {
			binding= data.unknownBinding;
		}
		
        if (binding != null) {
	        if (name.getPropertyInParent() == IASTNamedTypeSpecifier.NAME && !(binding instanceof IType || binding instanceof ICPPConstructor)) {
	        	IASTNode parent = name.getParent().getParent();
	        	if (parent instanceof IASTTypeId && parent.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
	        		if (!(binding instanceof IType)) {
	        		    // a type id needs to hold a type
	        			binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE, data.name());
	        		}
	        		// don't create a problem here
	        	} else {
	        		binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE, data.name());
	        	}
	        }
        }
        
		if (binding != null && !(binding instanceof IProblemBinding)) {
		    if (data.forDefinition()) {
		        addDefinition(binding, data.astName);
		    } 
		}
		// If we're still null...
		if (binding == null) {
			if (name instanceof ICPPASTQualifiedName && data.forDefinition())
				binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND, data.name());
			else
				binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, data.name());
		}
        return binding;
    }

    private static LookupData createLookupData(IASTName name, boolean considerAssociatedScopes) {
		LookupData data = new LookupData(name);
		IASTNode parent = name.getParent();
		
		if (name instanceof ICPPASTTemplateId) {
			data.templateId= ((ICPPASTTemplateId)name);
		}
		
		if (parent instanceof ICPPASTTemplateId)
			parent = parent.getParent();
		if (parent instanceof ICPPASTQualifiedName)
			parent = parent.getParent();
		
		if (parent instanceof IASTDeclarator && parent.getPropertyInParent() == IASTSimpleDeclaration.DECLARATOR) {
		    IASTSimpleDeclaration simple = (IASTSimpleDeclaration) parent.getParent();
		    if (simple.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef)
		        data.forceQualified = true;
		}
		
		if (parent instanceof ICPPASTFunctionDeclarator) {
			data.functionParameters = ((ICPPASTFunctionDeclarator)parent).getParameters();
		} else if (parent instanceof IASTIdExpression) {
		    ASTNodeProperty prop = parent.getPropertyInParent();
		    if (prop == IASTFunctionCallExpression.FUNCTION_NAME) {
		        parent = parent.getParent();
				IASTExpression exp = ((IASTFunctionCallExpression)parent).getParameterExpression();
				if (exp instanceof IASTExpressionList)
					data.functionParameters = ((IASTExpressionList) exp).getExpressions();
				else if (exp != null)
					data.functionParameters = new IASTExpression[] { exp };
				else
					data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
			}
		} else if (parent instanceof ICPPASTFieldReference && parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
		    IASTExpression exp = ((IASTFunctionCallExpression)parent.getParent()).getParameterExpression();
			if (exp instanceof IASTExpressionList)
				data.functionParameters = ((IASTExpressionList) exp).getExpressions();
			else if (exp != null)
				data.functionParameters = new IASTExpression[] { exp };
			else
				data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else if (parent instanceof ICPPASTNamedTypeSpecifier && parent.getParent() instanceof IASTTypeId) {
	        IASTTypeId typeId = (IASTTypeId) parent.getParent();
	        if (typeId.getParent() instanceof ICPPASTNewExpression) {
	            ICPPASTNewExpression newExp = (ICPPASTNewExpression) typeId.getParent();
	            IASTExpression init = newExp.getNewInitializer();
	            if (init instanceof IASTExpressionList)
					data.functionParameters = ((IASTExpressionList) init).getExpressions();
				else if (init != null)
					data.functionParameters = new IASTExpression[] { init };
				else
					data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
	        }
		} else if (parent instanceof ICPPASTConstructorChainInitializer) {
			ICPPASTConstructorChainInitializer ctorinit = (ICPPASTConstructorChainInitializer) parent;
			IASTExpression val = ctorinit.getInitializerValue();
			if (val instanceof IASTExpressionList)
				data.functionParameters = ((IASTExpressionList) val).getExpressions();
			else if (val != null)
				data.functionParameters = new IASTExpression[] { val };
			else
				data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		}
		
		if (considerAssociatedScopes && !(name.getParent() instanceof ICPPASTQualifiedName) && data.functionCall()) {
		    data.associated = getAssociatedScopes(data);
		}
		
		return data;
	}
    
    static private ObjectSet<IScope> getAssociatedScopes(LookupData data) {
        IType[] ps = getSourceParameterTypes(data.functionParameters);
        ObjectSet<IScope> namespaces = new ObjectSet<IScope>(2);
        ObjectSet<ICPPClassType> classes = new ObjectSet<ICPPClassType>(2);
        for (IType p : ps) {
            p = getUltimateType(p, true);
            try {
                getAssociatedScopes(p, namespaces, classes, data.tu);
            } catch (DOMException e) {
            }
        }
        return namespaces;
    }

    static private void getAssociatedScopes(IType t, ObjectSet<IScope> namespaces, ObjectSet<ICPPClassType> classes, CPPASTTranslationUnit tu) throws DOMException{
        //3.4.2-2 
		if (t instanceof ICPPClassType) {
			ICPPClassType ct= (ICPPClassType) t;
		    if (!classes.containsKey(ct)) {
		        classes.put(ct);
				IScope scope = getContainingNamespaceScope((IBinding) t, tu);
				if (scope != null)
					namespaces.put(scope);

			    ICPPClassType cls = (ICPPClassType) t;
			    ICPPBase[] bases = cls.getBases();
			    for (ICPPBase base : bases) {
			        if (base instanceof IProblemBinding)
			            continue;
			        IBinding b = base.getBaseClass();
			        if (b instanceof IType)
			        	getAssociatedScopes((IType) b, namespaces, classes, tu);
			    }
		    }
		} else if (t instanceof IEnumeration) {
			IScope scope = getContainingNamespaceScope((IBinding) t, tu);
			if (scope!=null)
				namespaces.put(scope);
		} else if (t instanceof IFunctionType) {
		    IFunctionType ft = (IFunctionType) t;
		    
		    getAssociatedScopes(getUltimateType(ft.getReturnType(), true), namespaces, classes, tu);
		    IType[] ps = ft.getParameterTypes();
		    for (IType element : ps) {
		        getAssociatedScopes(getUltimateType(element, true), namespaces, classes, tu);
		    }
		} else if (t instanceof ICPPPointerToMemberType) {
		    IType binding = ((ICPPPointerToMemberType) t).getMemberOfClass();
	        getAssociatedScopes(binding, namespaces, classes, tu);
		}
		return;
    }
    
    static private ICPPNamespaceScope getContainingNamespaceScope(IBinding binding, CPPASTTranslationUnit tu) throws DOMException{
        if (binding == null) return null;
        IScope scope = binding.getScope();
        while (scope != null && !(scope instanceof ICPPNamespaceScope)) {
            scope = getParentScope(scope, tu);
        }
        return (ICPPNamespaceScope) scope;
    }
    
	static private ICPPScope getLookupScope(IASTName name) throws DOMException{
	    IASTNode parent = name.getParent();
	    IScope scope = null;
    	if (parent instanceof ICPPASTBaseSpecifier) {
    	    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) parent.getParent();
    	    IASTName n = compSpec.getName();
    	    if (n instanceof ICPPASTQualifiedName) {
    	        n = ((ICPPASTQualifiedName) n).getLastName();
    	    }
    	    
	        scope = CPPVisitor.getContainingScope(n);
	    } else if (parent instanceof ICPPASTConstructorChainInitializer) {
	    	ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) parent;
	    	IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) initializer.getParent();
	    	IBinding binding = dtor.getName().resolveBinding();
	    	if (!(binding instanceof IProblemBinding))
	    		scope = binding.getScope();
	    } else {
	    	scope = CPPVisitor.getContainingScope(name);
	    }
    	if (scope instanceof ICPPScope)
    		return (ICPPScope)scope;
    	else if (scope instanceof IProblemBinding)
    		return new CPPScope.CPPScopeProblem(((IProblemBinding)scope).getASTNode(), IProblemBinding.SEMANTIC_BAD_SCOPE, ((IProblemBinding)scope).getNameCharArray());
    	return new CPPScope.CPPScopeProblem(name, IProblemBinding.SEMANTIC_BAD_SCOPE, name.toCharArray());
	}

	private static void mergeResults(LookupData data, Object results, boolean scoped) {
	    if (!data.contentAssist) {
	        if (results instanceof IBinding) {
	            data.foundItems = ArrayUtil.append(Object.class, (Object[]) data.foundItems, results);
	        } else if (results instanceof Object[]) {
	            data.foundItems = ArrayUtil.addAll(Object.class, (Object[]) data.foundItems, (Object[]) results);
	        }
	    } else {
	        data.foundItems = mergePrefixResults((CharArrayObjectMap) data.foundItems, results, scoped);
	    }
	}
	
	/**
	 * @param dest
	 * @param source : either Object[] or CharArrayObjectMap
	 * @param scoped
	 * @return
	 */
	private static Object mergePrefixResults(CharArrayObjectMap dest, Object source, boolean scoped) {
		if (source == null) return dest; 
        CharArrayObjectMap resultMap = (dest != null) ? dest : new CharArrayObjectMap(2);
        
        CharArrayObjectMap map = null;
        Object[] objs = null;
        int size;
        if (source instanceof CharArrayObjectMap) {
        	map = (CharArrayObjectMap) source;
        	size= map.size();
	    } else {
			if (source instanceof Object[])
				objs = ArrayUtil.trim(Object.class, (Object[]) source);
			else 
				objs = new Object[]{ source };
			size= objs.length;
		} 

		int resultInitialSize = resultMap.size();
        for (int i = 0; i < size; i ++) {
        	char[] key;
        	Object so;
        	if (map != null) {
        		key= map.keyAt(i);
        		so= map.get(key);
        	} else if (objs != null) {
        		so= objs[i];
        		key= (so instanceof IBinding) ? ((IBinding) so).getNameCharArray() : ((IASTName) so).toCharArray();
        	} else {
        		return resultMap;
        	}
        	int idx = resultMap.lookup(key);
        	if (idx == -1) {
				resultMap.put(key, so);
			} else if (!scoped || idx >= resultInitialSize) {
			    Object obj = resultMap.get(key);
			    if (obj instanceof Object[]) {
			        if (so instanceof IBinding || so instanceof IASTName)
			            obj = ArrayUtil.append(Object.class, (Object[]) obj, so);
			        else
			            obj = ArrayUtil.addAll(Object.class, (Object[])obj, (Object[]) so);
			    } else {
			        if (so instanceof IBinding || so instanceof IASTName) {
			            obj = new Object[] { obj, so };
			        } else {
			            Object[] temp = new Object[((Object[])so).length + 1];
			            temp[0] = obj;
			            obj = ArrayUtil.addAll(Object.class, temp, (Object[]) so);
			        }
			    } 
				resultMap.put(key, obj);
			}
        }

        return resultMap;
	}
	
	private static IIndexFileSet getIndexFileSet(LookupData data) {
		if (data.tu != null) {
			final IIndexFileSet fs= data.tu.getIndexFileSet();
			if (fs != null) 
				return fs;
		}
		return IIndexFileSet.EMPTY;
	}

	/**
	 * Perform a lookup with the given data starting in the given scope, considering bases and parent scopes.
	 * @param data the lookup data created off a name
	 * @param start either a scope or a name.
	 */
	static protected void lookup(LookupData data, Object start) throws DOMException{
		final IIndexFileSet fileSet= getIndexFileSet(data);
		final boolean isIndexBased= fileSet != IIndexFileSet.EMPTY;
		
		IASTNode node = data.astName;
		ICPPScope scope = null;
		if (start instanceof ICPPScope)
		    scope = (ICPPScope) start;
		else if (start instanceof IASTName)
		    scope = getLookupScope((IASTName) start);
		else 
			return;
		
		if (data.astName == null) 
			return;
		
		boolean friendInLocalClass = false;
		if (scope instanceof ICPPClassScope && data.forFriendship()) {
			try {
				ICPPClassType cls = ((ICPPClassScope)scope).getClassType();
				friendInLocalClass = !cls.isGloballyQualified();
			} catch (DOMException e) {
			}
		}
		
		while (scope != null) {
			if (scope instanceof IIndexScope && data.tu != null) {
				scope= (ICPPScope) data.tu.mapToASTScope(((IIndexScope) scope));
			}
			IASTNode blockItem = CPPVisitor.getContainingBlockItem(node);
			
			if (!data.usingDirectivesOnly) {
				if (data.contentAssist) {
					if (!ASTInternal.isFullyCached(scope)) {
						lookupInScope(data, scope, blockItem);
					}
					// now scope is fully cached.
					final IBinding[] bindings = scope.getBindings(data.astName, true, data.prefixLookup, fileSet);
					mergeResults(data, bindings, true);
				} else {
					boolean done= false;
					if (!ASTInternal.isFullyCached(scope)) {
						final IASTName[] names= lookupInScope(data, scope, blockItem);
						if (names != null) {
							mergeResults(data, names, true);
							done= true;
						} 
					}
				
					if (!done) {
						// now scope is fully cached.
						final IBinding binding = scope.getBinding(data.astName, true, fileSet);
						if (binding != null && 
								(CPPSemantics.declaredBefore(binding, data.astName, isIndexBased) || 
										(scope instanceof ICPPClassScope && data.checkWholeClassScope))) {
							mergeResults(data, binding, true);	
						}
					}
				}
				
				// store using-directives found in this block or namespace for later use.
				if ((!data.hasResults() || !data.qualified() || data.contentAssist) && scope instanceof ICPPNamespaceScope) {
					final ICPPNamespaceScope blockScope= (ICPPNamespaceScope) scope;
					if (!(blockScope instanceof ICPPBlockScope)) {
						data.visited.put(blockScope);	// namespace has been searched.
						if (data.tu != null) {
							data.tu.handleAdditionalDirectives(blockScope);
						}
					}
					ICPPUsingDirective[] uds= blockScope.getUsingDirectives();
					if (uds != null && uds.length > 0) {
						HashSet<ICPPNamespaceScope> handled= new HashSet<ICPPNamespaceScope>();
						for (final ICPPUsingDirective ud : uds) {
							if (CPPSemantics.declaredBefore(ud, data.astName, false)) {
								storeUsingDirective(data, blockScope, ud, handled);
							}
						}
					}
				}
			}

			// lookup in nominated namespaces
			if (!data.ignoreUsingDirectives && scope instanceof ICPPNamespaceScope && !(scope instanceof ICPPBlockScope)) {
				if (!data.hasResults() || !data.qualified() || data.contentAssist) {
					lookupInNominated(data, (ICPPNamespaceScope) scope);
				}
			}
			
			if ((!data.contentAssist && (data.problem != null || data.hasResults())) ||
					(friendInLocalClass && !(scope instanceof ICPPClassScope))) {
				return;
			}
			
			if (!data.usingDirectivesOnly && scope instanceof ICPPClassScope) {
				mergeResults(data, lookupInParents(data, scope), true);
			}
			
			if (!data.contentAssist && (data.problem != null || data.hasResults()))
				return;
			
			// if still not found, loop and check our containing scope
			if (data.qualified() && !(scope instanceof ICPPTemplateScope)) {
				if (data.usingDirectives.isEmpty())
					break;
				data.usingDirectivesOnly = true;
			}
			
			if (blockItem != null)
				node = blockItem;
			
			ICPPScope parentScope = (ICPPScope) getParentScope(scope, data.tu);
			if (parentScope instanceof ICPPTemplateScope) {
			    IASTNode declNode = node;
			    while (declNode != null && !(declNode instanceof ICPPASTTemplateDeclaration)) {
			        node = declNode;
			        declNode = declNode.getParent();
			    }
			    if (declNode != null) {
			        ICPPASTTemplateDeclaration templateDecl = (ICPPASTTemplateDeclaration) declNode;
			        ICPPTemplateScope templateScope = templateDecl.getScope();
			        if (templateScope.getTemplateDefinition() == ((ICPPTemplateScope)parentScope).getTemplateDefinition()) {
			            parentScope = templateScope;
			        }
			    }
			}
			scope = parentScope;
		}
	}
	
	private static IScope getParentScope(IScope scope, CPPASTTranslationUnit unit) throws DOMException {
		IScope parentScope= scope.getParent();
		// the index cannot return the translation unit as parent scope
		if (unit != null) {
			if (parentScope == null && scope instanceof IIndexScope) {
				parentScope= unit.getScope();
			}
			else if (parentScope instanceof IIndexScope) {
				parentScope= unit.mapToASTScope((IIndexScope) parentScope);
			}
		}
		return parentScope;
	}

	private static Object lookupInParents(LookupData data, ICPPScope lookIn) throws DOMException{
		if (lookIn instanceof ICPPClassScope == false)
			return null;
		
		final ICPPClassType classType= ((ICPPClassScope)lookIn).getClassType();
		if (classType == null) 
			return null;
		
		final ICPPBase[] bases= classType.getBases();
		if (bases == null || bases.length == 0)
			return null;
	
		Object inherited = null;
		Object result = null;
		
				
		//use data to detect circular inheritance
		if (data.inheritanceChain == null)
			data.inheritanceChain = new ObjectSet<IScope>(2);
		
		data.inheritanceChain.put(lookIn);

		// workaround to fix 185828 
		if (data.inheritanceChain.size() > CPPSemantics.MAX_INHERITANCE_DEPTH) { 
			return null;
		}

		for (ICPPBase base : bases) {
			if (base instanceof IProblemBinding)
				continue;
			
			IBinding b = base.getBaseClass();
			if (b instanceof ICPPClassType == false)
				continue;

			inherited = null;
			
			final ICPPClassType cls = (ICPPClassType) b;
			final ICPPScope parent = (ICPPScope) cls.getCompositeScope();
			
			if (parent == null)
				continue;
			if (parent instanceof CPPUnknownScope) {
				if (data.unknownBinding == null && classType instanceof ICPPClassTemplate && data.astName != null) {
					ICPPClassTemplate template= ((ICPPClassTemplate) classType);
					IBinding thisType= CPPTemplates.instantiateWithinClassTemplate(template);
					if (thisType instanceof ICPPUnknownBinding) {
						data.unknownBinding= ((ICPPUnknownBinding) thisType).getUnknownScope().getBinding(data.astName, true);
					}
				}
				continue;
			}
	
			if (!base.isVirtual() || !data.visited.containsKey(parent)) {
				if (base.isVirtual()) {
					data.visited.put(parent);
				}
	
				//if the inheritanceChain already contains the parent, then that 
				//is circular inheritance
				if (!data.inheritanceChain.containsKey(parent)) {
					//is this name define in this scope?
					if (ASTInternal.isFullyCached(parent)) {
						if (data.astName != null && !data.contentAssist) {
							inherited = parent.getBinding(data.astName, true);
						} else if (data.astName != null) {
							inherited = parent.getBindings(data.astName, true, data.prefixLookup);
						}
					} else {
						inherited = lookupInScope(data, parent, null);
					}
					
					if (inherited == null || data.contentAssist) {
						Object temp = lookupInParents(data, parent);
						if (inherited != null) {
							inherited = mergePrefixResults(null, inherited, true);
							inherited = mergePrefixResults((CharArrayObjectMap)inherited, (CharArrayObjectMap)temp, true);
						} else {
							inherited = temp;
						}
					} else {
					    visitVirtualBaseClasses(data, cls);
					}
				} else {
				    data.problem = new ProblemBinding(null, IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE, cls.getNameCharArray());
				    return null;
				}
			}	
			
			if (inherited != null) {
				if (result == null) {
					result = inherited;
				} else if (!data.contentAssist) {
					if (result instanceof Object[]) {
						Object[] r = (Object[]) result;
						for (int j = 0; j < r.length && r[j] != null; j++) {
							if (checkForAmbiguity(data, r[j], inherited)) {
							    data.problem = new ProblemBinding(data.astName,
							    		IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name()); 
							    return null;
							}
						}
					} else {
						if (checkForAmbiguity(data, result, inherited)) {
						    data.problem = new ProblemBinding(data.astName,
						    		IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name()); 
						    return null;
						}
					}
				} else {
					CharArrayObjectMap temp = (CharArrayObjectMap) inherited;
					CharArrayObjectMap r = (CharArrayObjectMap) result;
					char[] key = null;
					int tempSize = temp.size();
					for (int ii = 0; ii < tempSize; ii++) {
					    key = temp.keyAt(ii);
						if (!r.containsKey(key)) {
							r.put(key, temp.get(key));
						} else {
							//TODO: prefixLookup ambiguity checking
						}
					}
				}
			}
		}
	
		data.inheritanceChain.remove(lookIn);
	
		return result;	
	}

	public static void visitVirtualBaseClasses(LookupData data, ICPPClassType cls) throws DOMException {		
		if (data.inheritanceChain == null)
			data.inheritanceChain = new ObjectSet<IScope>(2);
		
		IScope scope = cls.getCompositeScope();
		if (scope != null)
			data.inheritanceChain.put(scope);
		
	    ICPPBase[] bases = cls.getBases();

        for (ICPPBase base : bases) {
            IBinding b = base.getBaseClass();
            if (b instanceof ICPPClassType) {
            	IScope bScope = ((ICPPClassType)b).getCompositeScope();
            	if (base.isVirtual()) {
            		if (bScope != null)
            			data.visited.put(bScope);
            	} else if (bScope != null) {
            		if (!data.inheritanceChain.containsKey(bScope))
            			visitVirtualBaseClasses(data, (ICPPClassType) b);
            		else
            			data.problem = new ProblemBinding(null, IProblemBinding.SEMANTIC_CIRCULAR_INHERITANCE, cls.getNameCharArray());
            	}
            }
        }
        
        if (scope != null)
        	data.inheritanceChain.remove(scope);
	}
	
	private static boolean checkForAmbiguity(LookupData data, Object n, Object names) throws DOMException{
		if (names instanceof Object[]) {
		    names = ArrayUtil.trim(Object.class, (Object[]) names);
		    if (((Object[])names).length == 0)
		        return false;
		}

	    IBinding binding= (n instanceof IBinding) ? (IBinding) n : ((IASTName) n).resolveBinding();

	    int idx= 0;
	    Object[] objs= null;
	    Object o= names;
	    if (names instanceof Object[]) {
	    	objs= (Object[]) names;
	    	o= objs[0];
	    	idx= 1;
	    }
	    
	    while (o != null) {       
	        IBinding b = (o instanceof IBinding) ? (IBinding) o : ((IASTName)o).resolveBinding();
	        
	        if (b instanceof ICPPUsingDeclaration) {
	        	objs = ArrayUtil.append(Object.class, objs, ((ICPPUsingDeclaration)b).getDelegates());
	        } else {
		        if (binding != b)
		            return true;
				
				boolean ok = false;
				//3.4.5-4 if the id-expression  in a class member access is a qualified id... the result 
				//is not required to be a unique base class...
				if (binding instanceof ICPPClassType) {
					IASTNode parent = data.astName.getParent();
					if (parent instanceof ICPPASTQualifiedName && 
							parent.getPropertyInParent() == IASTFieldReference.FIELD_NAME) {
						ok = true;
					}
				}
			    //it is not ambiguous if they are the same thing and it is static or an enumerator
		        if (binding instanceof IEnumerator ||
		        		(binding instanceof IFunction && ASTInternal.isStatic((IFunction) binding, false)) ||
		        		(binding instanceof IVariable && ((IVariable)binding).isStatic())) {
		        	ok = true;
		        }
		        if (!ok)
					return true;
	        }
	        if (objs != null && idx < objs.length)
	        	o = objs[idx++];
	        else
	        	o = null;
	    }
		return false;
	}

	/**
	 * Stores the using directive with the scope where the members of the nominated namespace will appear.
	 * In case of an unqualified lookup the transitive directives are stored, also. This is important because
	 * the members nominated by a transitive directive can appear before those of the original directive.
	 */
	static private void storeUsingDirective(LookupData data, ICPPNamespaceScope container, 
			ICPPUsingDirective directive, Set<ICPPNamespaceScope> handled) throws DOMException {
		ICPPNamespaceScope nominated= directive.getNominatedScope();
		if (nominated instanceof IIndexScope && data.tu != null) {
			nominated= (ICPPNamespaceScope) data.tu.mapToASTScope((IIndexScope) nominated);
		}
		if (nominated == null || data.visited.containsKey(nominated) || (handled != null && !handled.add(nominated))) {
			return;
		}
		// 7.3.4.1 names appear at end of common enclosing scope of container and nominated scope. 
		final IScope appearsIn= getCommonEnclosingScope(nominated, container, data.tu);
		if (appearsIn instanceof ICPPNamespaceScope) {
			// store the directive with the scope where it has to be considered
			List<ICPPNamespaceScope> listOfNominated= data.usingDirectives.get(appearsIn);
			if (listOfNominated == null) {
				listOfNominated= new ArrayList<ICPPNamespaceScope>(1);
				if (data.usingDirectives.isEmpty()) {
					data.usingDirectives= new HashMap<ICPPNamespaceScope, List<ICPPNamespaceScope>>();
				}
				data.usingDirectives.put((ICPPNamespaceScope) appearsIn, listOfNominated);
			}
			listOfNominated.add(nominated);
		}
		
		// in a non-qualified lookup the transitive directive have to be stored right away, they may overtake the
		// container.
		if (!data.qualified() || data.contentAssist) {
			assert handled != null;
			if (data.tu != null) {
				data.tu.handleAdditionalDirectives(nominated);
			}
			ICPPUsingDirective[] transitive= nominated.getUsingDirectives();
			for (ICPPUsingDirective element : transitive) {
				storeUsingDirective(data, container, element, handled);
			}
		}
	}

	/**
	 * Computes the common enclosing scope of s1 and s2.
	 */
	static private ICPPScope getCommonEnclosingScope(IScope s1, IScope s2, CPPASTTranslationUnit tu) throws DOMException { 
		ObjectSet<IScope> set = new ObjectSet<IScope>(2);
		IScope parent= s1;
		while (parent != null) {
			set.put(parent);
			parent= getParentScope(parent, tu);
		}
		parent= s2;
		while (parent != null && !set.containsKey(parent)) {
			parent = getParentScope(parent, tu);
		}
		return (ICPPScope) parent;
	}

	/**
	 * 
	 * @param data may be null to use a fresh LookupData
	 * @param scope
	 * @param blockItem
	 * @return List of encountered using directives
	 * @throws DOMException
	 */
	public static IASTName[] lookupInScope(LookupData data, ICPPScope scope, IASTNode blockItem) throws DOMException {
		if (data == null) {
			data = new LookupData();
		}
		final boolean isIndexBased= data.tu != null && data.tu.getIndex() != null;
		Object possible = null;
		IASTNode[] nodes = null;
		IASTNode parent = ASTInternal.getPhysicalNodeOfScope(scope);
		
		IASTName[] namespaceDefs = null;
		int namespaceIdx = -1;
		
		if (data.associated.containsKey(scope)) {
			//we are looking in scope, remove it from the associated scopes list
			data.associated.remove(scope);
		}
		
		IASTName[] found = null;
		
		if (parent instanceof IASTCompoundStatement) {
			IASTNode p = parent.getParent();
		    if (p instanceof IASTFunctionDefinition) {
		        ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) ((IASTFunctionDefinition)p).getDeclarator();
		        nodes = dtor.getParameters();
		    } 
		    if (p instanceof ICPPASTCatchHandler) {
		    	parent = p;
		    } else if (nodes == null || nodes.length == 0) {
				IASTCompoundStatement compound = (IASTCompoundStatement) parent;
				nodes = compound.getStatements();
		    }
		} else if (parent instanceof IASTTranslationUnit) {
			IASTTranslationUnit translation = (IASTTranslationUnit) parent;
			nodes = translation.getDeclarations();
		} else if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) parent;
			nodes = comp.getMembers();
			
			//9-2 a class name is also inserted into the scope of the class itself
			IASTName n = comp.getName();
			if (nameMatches(data, n, scope)) {
				found = (IASTName[]) ArrayUtil.append(IASTName.class, found, n);
		    }
		} else if (parent instanceof ICPPASTNamespaceDefinition) {
		    //need binding because namespaces can be split
		    CPPNamespace namespace = (CPPNamespace) ((ICPPASTNamespaceDefinition)parent).getName().resolveBinding();
		    namespaceDefs = namespace.getNamespaceDefinitions();
		    nodes = ((ICPPASTNamespaceDefinition)namespaceDefs[++namespaceIdx].getParent()).getDeclarations();
			while (nodes.length == 0 && ++namespaceIdx < namespaceDefs.length) {
				nodes= ((ICPPASTNamespaceDefinition)namespaceDefs[namespaceIdx].getParent()).getDeclarations();
			}
		} else if (parent instanceof ICPPASTFunctionDeclarator) {
		    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
	        nodes = dtor.getParameters();
		} else if (parent instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) parent;
			nodes = template.getTemplateParameters();
		}
		
		int idx = -1;
		boolean checkWholeClassScope = (scope instanceof ICPPClassScope) && data.checkWholeClassScope;
		IASTNode item = (nodes != null ? (nodes.length > 0 ? nodes[++idx] : null) : parent);
		IASTNode[][] nodeStack = null;
		int[] nodeIdxStack = null;
		int nodeStackPos = -1;
		while (item != null) {
		    if (item instanceof ICPPASTLinkageSpecification) {
		        IASTDeclaration[] decls = ((ICPPASTLinkageSpecification)item).getDeclarations();
		        if (decls != null && decls.length > 0) {
			        nodeStack = (IASTNode[][]) ArrayUtil.append(IASTNode[].class, nodeStack, nodes);
			        nodeIdxStack = ArrayUtil.setInt(nodeIdxStack, ++nodeStackPos, idx);
			        nodes = ((ICPPASTLinkageSpecification)item).getDeclarations();
			        idx = 0;
				    item = nodes[idx];
				    continue;
		        }
			}

		    if (item instanceof IASTDeclarationStatement)
		        item = ((IASTDeclarationStatement)item).getDeclaration();
			if (item instanceof ICPPASTUsingDirective) {
				if (scope instanceof ICPPNamespaceScope) {
				    final ICPPNamespaceScope nsscope = (ICPPNamespaceScope)scope;
					final ICPPASTUsingDirective usingDirective = (ICPPASTUsingDirective) item;
					nsscope.addUsingDirective(new CPPUsingDirective(usingDirective));
				}
			} else if (item instanceof ICPPASTNamespaceDefinition &&
					   ((ICPPASTNamespaceDefinition)item).getName().toCharArray().length == 0) {
				if (scope instanceof ICPPNamespaceScope) {
				    final ICPPNamespaceScope nsscope = (ICPPNamespaceScope)scope;
				    final ICPPASTNamespaceDefinition nsdef= (ICPPASTNamespaceDefinition) item;
					nsscope.addUsingDirective(new CPPUsingDirective(nsdef));
				}
			} else {
			    //possible is IASTName or IASTName[]
				possible = collectResult(data, scope, item, (item == parent));
				if (possible != null) {
				    int jdx = -1;
				    IASTName temp;
				    if (possible instanceof IASTName)
				        temp = (IASTName) possible;
				    else
				        temp = ((IASTName[])possible)[++jdx];

				    while (temp != null) {
						if ((checkWholeClassScope || declaredBefore(temp, data.astName, isIndexBased)) &&
								(item != blockItem || data.includeBlockItem(item)))	{
							if (data.considerConstructors || 
									!(temp.getParent() instanceof IASTDeclarator &&
								    CPPVisitor.isConstructor(scope, (IASTDeclarator) temp.getParent()))) {
								found = (IASTName[]) ArrayUtil.append(IASTName.class, found, temp);
							}
						}
						if (++jdx > 0 && jdx < ((IASTName[])possible).length)
						    temp = ((IASTName[])possible)[jdx];
						else 
						    temp = null;
				    }
				}
			}
		    
			if (nodes != null && ++idx < nodes.length) {
				item = nodes[idx];
			} else {
			    item = null;
			    while (true) {
				    if (namespaceDefs != null) {
				        //check all definitions of this namespace
					    while (++namespaceIdx < namespaceDefs.length) {
					        nodes = ((ICPPASTNamespaceDefinition)namespaceDefs[namespaceIdx].getParent()).getDeclarations();
						    if (nodes.length > 0) {
						        idx = 0;
						        item = nodes[0];
						        break;
						    }     
					    }
				    } else if (parent instanceof IASTCompoundStatement && nodes instanceof IASTParameterDeclaration[]) {
				    	//function body, we were looking at parameters, now check the body itself
				        IASTCompoundStatement compound = (IASTCompoundStatement) parent;
						nodes = compound.getStatements(); 
						if (nodes.length > 0) {
					        idx = 0;
					        item = nodes[0];
					        break;
					    }  
				    } else if (parent instanceof ICPPASTCatchHandler) {
				    	parent = ((ICPPASTCatchHandler)parent).getCatchBody();
				    	if (parent instanceof IASTCompoundStatement) {
				    		nodes = ((IASTCompoundStatement)parent).getStatements();
				    		if (nodes.length > 0) {
				    			idx = 0;
				    			item = nodes[0];
				    			break;
				    		}  
				    	}
				    }
				    if (item == null && nodeStack != null && nodeIdxStack != null && nodeStackPos >= 0) {
				        nodes = nodeStack[nodeStackPos];
				        nodeStack[nodeStackPos] = null;
				        idx = nodeIdxStack[nodeStackPos--];
				        if (++idx >= nodes.length)
				            continue;
				        
			            item = nodes[idx];
				    }
				    break;
			    }
			}
		}
		

		ASTInternal.setFullyCached(scope, true);
		
		return found;
	}

	/**
	 * Perform lookup in nominated namespaces that appear in the given scope. For unqualified lookups the method assumes
	 * that transitive directives have been stored in the lookup-data. For qualified lookups the transitive directives
	 * are considered if the lookup of the original directive returns empty.
	 */
	static private void lookupInNominated(LookupData data, ICPPNamespaceScope scope) throws DOMException{
		List<ICPPNamespaceScope> allNominated= data.usingDirectives.remove(scope);
		while (allNominated != null) {
			for (ICPPNamespaceScope nominated : allNominated) {
				if (data.visited.containsKey(nominated)) {
					continue;
				}
				data.visited.put(nominated);

				boolean found = false;
				if (ASTInternal.isFullyCached(nominated)) {
					IBinding[] bindings= nominated.getBindings(data.astName, true, data.prefixLookup);
					if (bindings != null && bindings.length > 0) {
						mergeResults(data, bindings, true);
						found = true;
					}
				} else {
					IASTName[] f = lookupInScope(data, nominated, null);
					if (f != null) {
						mergeResults(data, f, true);
						found = true;
					}
				}

				// in the qualified lookup we have to nominate the transitive directives only when
				// the lookup did not succeed. In the qualified case this is done earlier, when the directive
				// is encountered.
				if (!found && data.qualified() && !data.contentAssist) {
					if (data.tu != null) {
						data.tu.handleAdditionalDirectives(nominated);
					}
					ICPPUsingDirective[] usings= nominated.getUsingDirectives();
					for (ICPPUsingDirective using : usings) {
						storeUsingDirective(data, scope, using, null);
					}
				}
			}
			// retry with transitive directives that may have been nominated in a qualified lookup
			allNominated= data.usingDirectives.remove(scope);
		}
	}

	static private Object collectResult(LookupData data, ICPPScope scope, IASTNode node, boolean checkAux) throws DOMException{
	    IASTName resultName = null;
	    IASTName[] resultArray = null;
	    
	    IASTDeclaration declaration = null;
	    if (node instanceof ICPPASTTemplateDeclaration) {
			declaration = ((ICPPASTTemplateDeclaration)node).getDeclaration();
	    } else if (node instanceof IASTDeclaration) { 
	        declaration = (IASTDeclaration) node;
	    } else if (node instanceof IASTDeclarationStatement) {
			declaration = ((IASTDeclarationStatement)node).getDeclaration();
	    } else if (node instanceof ICPPASTCatchHandler) {
			declaration = ((ICPPASTCatchHandler)node).getDeclaration();
	    } else if (node instanceof ICPPASTForStatement && checkAux) {
			ICPPASTForStatement forStatement = (ICPPASTForStatement) node;
			if (forStatement.getConditionDeclaration() == null) {
				if (forStatement.getInitializerStatement() instanceof IASTDeclarationStatement)
					declaration = ((IASTDeclarationStatement)forStatement.getInitializerStatement()).getDeclaration();
			} else {
				if (forStatement.getInitializerStatement() instanceof IASTDeclarationStatement) {
					Object o = collectResult(data, scope, forStatement.getInitializerStatement(), checkAux);
					if (o instanceof IASTName)
						resultName = (IASTName) o;
					else if (o instanceof IASTName[])
						resultArray = (IASTName[]) o;
				}
				declaration = forStatement.getConditionDeclaration();
			}
        } else if (node instanceof ICPPASTSwitchStatement) {
        	declaration = ((ICPPASTSwitchStatement)node).getControllerDeclaration();
        } else if (node instanceof ICPPASTIfStatement) {
        	declaration = ((ICPPASTIfStatement)node).getConditionDeclaration();
	    } else if (node instanceof ICPPASTWhileStatement) {
	    	declaration = ((ICPPASTWhileStatement)node).getConditionDeclaration();
	    } else if (node instanceof IASTParameterDeclaration) {
		    IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) node;
		    IASTDeclarator dtor = parameterDeclaration.getDeclarator();
            if (dtor != null) { // could be null when content assist in the declSpec
    		    while (dtor.getNestedDeclarator() != null)
    		    	dtor = dtor.getNestedDeclarator();
    			IASTName declName = dtor.getName();
    			ASTInternal.addName(scope,  declName);
    			if (!data.typesOnly && nameMatches(data, declName, scope)) {
    			    return declName;
    		    }
            }
		} else if (node instanceof ICPPASTTemplateParameter) {
			IASTName name = CPPTemplates.getTemplateParameterName((ICPPASTTemplateParameter) node);
			ASTInternal.addName(scope,  name);
			if (nameMatches(data, name, scope)) {
		        return name;
		    }
		}
		if (declaration == null)
			return null;
		
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) simpleDeclaration.getDeclSpecifier();
			IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
			if (!declSpec.isFriend()) {
				for (IASTDeclarator declarator : declarators) {
					declarator= CPPVisitor.findInnermostDeclarator(declarator);
					IASTName declaratorName = declarator.getName();
					ASTInternal.addName(scope,  declaratorName);
					if (!data.typesOnly || simpleDeclaration.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
						if (nameMatches(data, declaratorName, scope)) {
							if (resultName == null)
							    resultName = declaratorName;
							else if (resultArray == null)
							    resultArray = new IASTName[] { resultName, declaratorName };
							else
							    resultArray = (IASTName[]) ArrayUtil.append(IASTName.class, resultArray, declaratorName);
						}
					}
				}
			}
	
			//decl spec 
			
			IASTName specName = null;
			if (declarators.length == 0 && declSpec instanceof IASTElaboratedTypeSpecifier) {
				specName = ((IASTElaboratedTypeSpecifier)declSpec).getName();
			} else if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) declSpec;
				specName = compSpec.getName();
				
				//anonymous union?             //GCC supports anonymous structs too
				if (declarators.length == 0 && /*compSpec.getKey() == IASTCompositeTypeSpecifier.k_union &&*/
				    specName.toCharArray().length == 0)
				{
				    Object o = null;
				    IASTDeclaration[] decls = compSpec.getMembers();
				    for (IASTDeclaration decl : decls) {
                        o = collectResult(data, scope, decl, checkAux);
                        if (o instanceof IASTName) {
                            if (resultName == null)
    						    resultName = (IASTName) o;
    						else if (resultArray == null)
    						    resultArray = new IASTName[] { resultName, (IASTName) o };
    						else
    						    resultArray = (IASTName[]) ArrayUtil.append(IASTName.class, resultArray, o);
                        } else if (o instanceof IASTName[]) {
                            IASTName[] oa = (IASTName[]) o;
                            if (resultName == null) {
    						    resultName = oa[0];
    						    resultArray = oa;
                            } else if (resultArray == null) {
    						    resultArray = new IASTName[1 + oa.length];
    						    resultArray[0] = resultName;
    						    resultArray = (IASTName[]) ArrayUtil.addAll(IASTName.class, resultArray, oa);
                            } else {
                                resultArray = (IASTName[]) ArrayUtil.addAll(IASTName.class, resultArray, oa);
                            }
                        }
                    }
				}
			} else if (declSpec instanceof IASTEnumerationSpecifier) {
			    IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) declSpec;
			    specName = enumeration.getName();

			    //check enumerators too
			    IASTEnumerator[] list = enumeration.getEnumerators();
			    IASTName tempName;
			    for (IASTEnumerator enumerator : list) {
			        if (enumerator == null) break;
			        tempName = enumerator.getName();
			        ASTInternal.addName(scope,  tempName);
			        if (!data.typesOnly && nameMatches(data, tempName, scope)) {
			            if (resultName == null)
						    resultName = tempName;
						else if (resultArray == null)
						    resultArray = new IASTName[] { resultName, tempName };
						else
						    resultArray = (IASTName[]) ArrayUtil.append(IASTName.class, resultArray, tempName);
					}
			    }
			}
			if (specName != null) {
				if (!(specName instanceof ICPPASTQualifiedName) ||
						data.astName == ((ICPPASTQualifiedName) specName).getLastName()) {
					ASTInternal.addName(scope, specName);
				}
			    if (nameMatches(data, specName, scope)) {
				    if (resultName == null)
					    resultName = specName;
					else if (resultArray == null)
					    resultArray = new IASTName[] { resultName, specName };
					else
					    resultArray = (IASTName[]) ArrayUtil.append(IASTName.class, resultArray, specName);
			    }
			}
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) declaration;
			IASTName name = using.getName();
			if (name instanceof ICPPASTQualifiedName) {
				name = ((ICPPASTQualifiedName) name).getLastName();
			}
			ASTInternal.addName(scope,  name);
			if (nameMatches(data, name, scope)) {
				return name;
			}
		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			IASTName namespaceName = ((ICPPASTNamespaceDefinition) declaration).getName();
			ASTInternal.addName(scope,  namespaceName);
			if (nameMatches(data, namespaceName, scope))
				return namespaceName;
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			IASTName alias = ((ICPPASTNamespaceAlias) declaration).getAlias();
			ASTInternal.addName(scope,  alias);
			if (nameMatches(data, alias, scope))
				return alias;
		} else if (declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			if (!((ICPPASTDeclSpecifier) functionDef.getDeclSpecifier()).isFriend()) {
				IASTFunctionDeclarator declarator = functionDef.getDeclarator();
				
				//check the function itself
				IASTName declName = CPPVisitor.findInnermostDeclarator(declarator).getName();
				ASTInternal.addName(scope,  declName);
	
			    if (!data.typesOnly && nameMatches(data, declName, scope)) {
					return declName;
				}
			}
		}
		
		if (resultArray != null)
		    return resultArray;
		return resultName;
	}

	private static final boolean nameMatches(LookupData data, IASTName potential, IScope scope) throws DOMException{
	    if (potential instanceof ICPPASTQualifiedName) {
	    	IASTNode phn= ASTInternal.getPhysicalNodeOfScope(scope);
			if (phn instanceof ICPPASTCompositeTypeSpecifier == false && phn instanceof ICPPASTNamespaceDefinition == false)
				return false;

	        //A qualified name implies the name actually belongs to a different scope, and should
	        //not be considered here, except the qualifier names the scope itself
			final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) potential;
		    if (scope instanceof CPPScope == false || ((CPPScope) scope).canDenoteScopeMember(qname))
		    	return false;
				
			potential= qname.getLastName();
	    }
	    char[] c = potential.toCharArray();
	    char[] n = data.name();
	    return (data.prefixLookup && CharArrayUtils.equals(c, 0, n.length, n, true))
			|| (!data.prefixLookup && CharArrayUtils.equals(c, n));
	}
	
	private static void addDefinition(IBinding binding, IASTName name) {
		if (binding instanceof IFunction) {
			IASTNode node =  name.getParent();
			if (node instanceof ICPPASTQualifiedName)
				node = node.getParent();
			if (node instanceof ICPPASTFunctionDeclarator && node.getParent() instanceof IASTFunctionDefinition) {
				if (binding instanceof ICPPInternalBinding)
				((ICPPInternalBinding)binding).addDefinition(node);
			}
		}
	}

	public static IBinding resolveAmbiguities(IASTName name, Object[] bindings) {
	    bindings = ArrayUtil.trim(Object.class, bindings);
	    if (bindings == null || bindings.length == 0) {
	        return null;
	    } else if (bindings.length == 1) {
	    	IBinding candidate= null;
	        if (bindings[0] instanceof IBinding) {
	        	candidate= (IBinding) bindings[0];
	        } else if (bindings[0] instanceof IASTName) {
	    		candidate= ((IASTName) bindings[0]).getBinding();
	    	} else {
	    		return null;
	    	}
	        if (candidate != null) {
	        	if (!(candidate instanceof IType) && !(candidate instanceof ICPPNamespace) &&
	        			!(candidate instanceof ICPPUsingDeclaration) &&
	        			LookupData.typesOnly(name)) {
	        		return null;
	        	}

	        	// bug 238180
	        	if (candidate instanceof ICPPClassTemplatePartialSpecialization) 
	        		return null;
	        	
	        	return candidate;
	        }
	    }
	    
	    if (name.getPropertyInParent() != STRING_LOOKUP_PROPERTY) {
		    LookupData data = createLookupData(name, false);
		    data.foundItems = bindings;
		    try {
	            return resolveAmbiguities(data, name);
	        } catch (DOMException e) {
	            return e.getProblem();
	        }
	    }
	    
        IBinding[] result = null;
        for (Object binding : bindings) {
            if (binding instanceof IASTName)
                result = (IBinding[]) ArrayUtil.append(IBinding.class, result, ((IASTName)binding).resolveBinding());
            else if (binding instanceof IBinding)
                result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
        }
        return new CPPCompositeBinding(result);
	}
	
	static public boolean declaredBefore(Object obj, IASTNode node, boolean indexBased) {
	    if (node == null) return true;
	    if (node.getPropertyInParent() == STRING_LOOKUP_PROPERTY) return true;
	    final int pointOfRef= ((ASTNode) node).getOffset();
	    
	    ASTNode nd = null;
	    if (obj instanceof ICPPSpecialization) {
	        obj = ((ICPPSpecialization)obj).getSpecializedBinding();
	    }
	    
	    int pointOfDecl= -1;
	    if (obj instanceof ICPPInternalBinding) {
	        ICPPInternalBinding cpp = (ICPPInternalBinding) obj;
	        // for bindings in global or namespace scope we don't know whether there is a 
	        // previous declaration in one of the skipped header files. For bindings that
	        // are likely to be redeclared we need to assume that there is a declaration
	        // in one of the headers.
	    	if (indexBased) {
    			try {
    				if (cpp instanceof ICPPNamespace || cpp instanceof ICPPFunction || cpp instanceof ICPPVariable) {
	    				IScope scope= cpp.getScope();
	    				if (scope instanceof ICPPBlockScope == false && scope instanceof ICPPNamespaceScope) {
	    					return true;
	    				}
    				} else if (cpp instanceof ICompositeType || cpp instanceof IEnumeration) {
	    				IScope scope= cpp.getScope();
	    				if (scope instanceof ICPPBlockScope == false && scope instanceof ICPPNamespaceScope) {
	    					// if this is not the definition, it may be found in a header. (bug 229571)
	    					if (cpp.getDefinition() == null) {
	    						return true;
	    					}
	    				}
    				}
    			} catch (DOMException e) {
    			}
	    	}
	        IASTNode[] n = cpp.getDeclarations();
	        if (n != null && n.length > 0) {
	        	nd = (ASTNode) n[0];
	        }
	        ASTNode def = (ASTNode) cpp.getDefinition();
	        if (def != null) {
	        	if (nd == null || def.getOffset() < nd.getOffset())
	        		nd = def;
	        }
	        if (nd == null) 
	            return true;
	    } else if (obj instanceof ASTNode) {
	        nd = (ASTNode) obj;
	    } else if (obj instanceof ICPPUsingDirective) {
	    	pointOfDecl= ((ICPPUsingDirective) obj).getPointOfDeclaration();
	    }
	    
	    if (pointOfDecl < 0 && nd != null) {
            ASTNodeProperty prop = nd.getPropertyInParent();
            if (prop == IASTDeclarator.DECLARATOR_NAME || nd instanceof IASTDeclarator) {
                // point of declaration for a name is immediately after its complete declarator and before its initializer
                IASTDeclarator dtor = (IASTDeclarator)((nd instanceof IASTDeclarator) ? nd : nd.getParent());
                while (dtor.getParent() instanceof IASTDeclarator)
                    dtor = (IASTDeclarator) dtor.getParent();
                IASTInitializer init = dtor.getInitializer();
                if (init != null)
                    pointOfDecl = ((ASTNode)init).getOffset() - 1;
                else
                    pointOfDecl = ((ASTNode)dtor).getOffset() + ((ASTNode)dtor).getLength();
            } else if (prop == IASTEnumerator.ENUMERATOR_NAME) {
                // point of declaration for an enumerator is immediately after it enumerator-definition
                IASTEnumerator enumtor = (IASTEnumerator) nd.getParent();
                if (enumtor.getValue() != null) {
                    ASTNode exp = (ASTNode) enumtor.getValue();
                    pointOfDecl = exp.getOffset() + exp.getLength();
                } else {
                    pointOfDecl = nd.getOffset() + nd.getLength();
                }
            } else if (prop == ICPPASTUsingDeclaration.NAME) {
                nd = (ASTNode) nd.getParent();
            	pointOfDecl = nd.getOffset();
            } else if (prop == ICPPASTNamespaceAlias.ALIAS_NAME) {
            	nd = (ASTNode) nd.getParent();
            	pointOfDecl = nd.getOffset() + nd.getLength();
            } else { 
                pointOfDecl = nd.getOffset() + nd.getLength();
            }
	    }
	    return (pointOfDecl < pointOfRef);
	}
	
	static private IBinding resolveAmbiguities(LookupData data, IASTName name) throws DOMException {
	    if (!data.hasResults() || data.contentAssist)
	        return null;
	      
	    final boolean indexBased= data.tu != null && data.tu.getIndex() != null;	    
	    @SuppressWarnings("unchecked")
	    ObjectSet<IFunction> fns= ObjectSet.EMPTY_SET;
	    @SuppressWarnings("unchecked")
	    ObjectSet<IFunction> templateFns= ObjectSet.EMPTY_SET;
	    IBinding type = null;
	    IBinding obj  = null;
	    IBinding temp = null;
	    boolean fnsFromAST= false;
	    
	    Object[] items = (Object[]) data.foundItems;
	    for (int i = 0; i < items.length && items[i] != null; i++) {
	        Object o = items[i];
	        boolean declaredBefore = declaredBefore(o, name, indexBased);
	        boolean checkResolvedNamesOnly= false;
	        if (!data.checkWholeClassScope && !declaredBefore) {
	        	if (name.getRoleOfName(false) != IASTNameOwner.r_reference) {
	        		checkResolvedNamesOnly= true;
	        		declaredBefore= true;
	        	} else {
	        		continue;
	        	}
	        }
	        if (o instanceof IASTName) {
	        	IASTName on= (IASTName) o;
	            temp = checkResolvedNamesOnly ? on.getBinding() : on.resolveBinding();
	            if (temp == null)
	                continue;
	        } else if (o instanceof IBinding) {
	            temp = (IBinding) o;
	        } else {
	            continue;
	        }

	        // select among those bindings that have been created without problems.
	        if (temp instanceof IProblemBinding)
	        	continue;

	        if (!declaredBefore && !(temp instanceof ICPPMember) && !(temp instanceof IType) &&
	        		!(temp instanceof IEnumerator)) {
                continue;
	        }
	        if (temp instanceof ICPPUsingDeclaration) {
	        	IBinding[] bindings = ((ICPPUsingDeclaration) temp).getDelegates();
	        	mergeResults(data, bindings, false);
	        	items = (Object[]) data.foundItems;
	        	continue;
	        } else if (temp instanceof CPPCompositeBinding) {
	        	IBinding[] bindings = ((CPPCompositeBinding)temp).getBindings();
	        	mergeResults(data, bindings, false);
	        	items = (Object[]) data.foundItems;
	        	continue;
	        } else if (temp instanceof IFunction) {
	        	IFunction function= (IFunction) temp;
	        	if (function instanceof ICPPFunctionTemplate) {
	        		if (templateFns == ObjectSet.EMPTY_SET)
	        			templateFns = new ObjectSet<IFunction>(2);
	        		templateFns.put(function);
	        	} else { 
	        		if (fns == ObjectSet.EMPTY_SET)
	        			fns = new ObjectSet<IFunction>(2);
	        		if (isFromIndex(function)) {
	        			// accept bindings from index only, in case we have none in the AST
	        			if (!fnsFromAST) {
	        				fns.put(function);
	        			}
	        		} else {
	        			if (!fnsFromAST) {
	        				fns.clear();
	        				fnsFromAST= true;
	        			}
	        			fns.put(function);
	        		}
	        	}
	        } else if (temp instanceof IType) {
	        	if (type == null) {
	                type = temp;
	        	} else if (type instanceof ICPPClassTemplate && temp instanceof ICPPSpecialization &&
						  ((IType) type).isSameType((IType) ((ICPPSpecialization)temp).getSpecializedBinding())) {
					// ok, stay with the template, the specialization, if applicable, will come out during instantiation
				} else if (type != temp && !((IType)type).isSameType((IType) temp)) {
	                return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());
	            }
	        } else {
	        	if (obj == null) {
	        		obj = temp;
	        	} else if (obj == temp) {
	        	    //ok, delegates are synonyms
	        	} else {
	        		// ignore index stuff in case we have bindings from the ast
	        		boolean ibobj= isFromIndex(obj);
	        		boolean ibtemp= isFromIndex(temp);
	        		// blame it on the index
	        		if (ibobj != ibtemp) {
	        			if (ibobj) 
	        				obj= temp;
	        		} else { 
	        			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());
	        		}
	        	}
	        }
	    }
	    if (data.forUsingDeclaration()) {
	        IBinding[] bindings = null;
	        if (obj != null) {
	            if (fns.size() > 0) return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());
	//            if (type == null) return obj;
	            bindings = (IBinding[]) ArrayUtil.append(IBinding.class, bindings, obj);
	            bindings = (IBinding[]) ArrayUtil.append(IBinding.class, bindings, type);
	        } else {
//	            if (fns == null) return type;
	            bindings = (IBinding[]) ArrayUtil.append(IBinding.class, bindings, type);
	            bindings = (IBinding[]) ArrayUtil.addAll(IBinding.class, bindings, fns.keyArray());
	            bindings = (IBinding[]) ArrayUtil.addAll(IBinding.class, bindings, templateFns.keyArray());
	        }
	        bindings = (IBinding[]) ArrayUtil.trim(IBinding.class, bindings);
	        ICPPUsingDeclaration composite = new CPPUsingDeclaration(data.astName, bindings);
	        return composite;	
	    }
	        
	    int numTemplateFns = templateFns.size();
		if (numTemplateFns > 0) {
			if (data.functionParameters != null && !data.forDefinition()) {
				IFunction[] fs = CPPTemplates.selectTemplateFunctions(templateFns, data.functionParameters, data.astName);
				if (fs != null && fs.length > 0) {
				    if (fns == ObjectSet.EMPTY_SET)
				        fns = new ObjectSet<IFunction>(fs.length);
					fns.addAll(fs);
				}
			} else {
				if (fns == ObjectSet.EMPTY_SET)
					fns = templateFns;
				else
					fns.addAll(templateFns);
			}
		}
		int numFns = fns.size();
	    if (type != null) {
	    	if (data.typesOnly || (obj == null && numFns == 0))
	    		return type;
	    }
	   
	    if (numFns > 0) {
	    	if (obj != null)
	    		return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());
	    	return resolveFunction(data, fns.keyArray(IFunction.class));
	    }
	    
	    if (data.typesOnly && obj instanceof ICPPNamespace == false) {
	    	return null;
	    }
	    return obj;
	}

	private static boolean isFromIndex(IBinding binding) {
		if (binding instanceof IIndexBinding) {
			return true;
		}
		if (binding instanceof ICPPSpecialization) {
			return ((ICPPSpecialization) binding).getSpecializedBinding() instanceof IIndexBinding;
		}
		return false;
	}
	
	static private boolean functionHasParameters(IFunction function, IASTParameterDeclaration[] params) throws DOMException{
		IFunctionType ftype = function.getType();
		if (params.length == 0) {
			return ftype.getParameterTypes().length == 0;
		}
		
		IASTNode node = params[0].getParent();
		if (node instanceof ICPPASTFunctionDeclarator) {
			return isSameFunction(function, (IASTDeclarator) node);
		}
	 	return false;
	}
	
	static private void reduceToViable(LookupData data, IBinding[] functions) throws DOMException{
	    if (functions == null || functions.length == 0)
	        return;
	    
		Object[] fParams = data.functionParameters;
		int numParameters = (fParams != null) ? fParams.length : 0;		
		int num;	
		boolean def = data.forDefinition();	
		// Trim the list down to the set of viable functions
		IFunction function = null;
		int size = functions.length;
		for (int i = 0; i < size && functions[i] != null; i++) {
			function = (IFunction) functions[i];
			if (function instanceof IProblemBinding) {
				functions[i]= null;
				continue;
			}
			num = function.getParameters().length;
		
			// if there are m arguments in the list, all candidate functions having m parameters
			// are viable	 
			if (num == numParameters) {
				if (def && !isMatchingFunctionDeclaration(function, data)) {
					functions[i] = null;
				}
				continue;
			} else if (numParameters == 0 && num == 1) {
				// check for void
			    IParameter param = function.getParameters()[0];
			    IType t = param.getType();
			    if (t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_void)
			        continue;
			}
			
			if (def) {
				// if this is for a definition, we had to match the number of parameters.
				functions[i] = null;
				continue;
			}
			
			if (num < numParameters) {
				// A candidate function having fewer than m parameters is viable only if it has an 
				// ellipsis in its parameter list.
			    if (function.takesVarArgs())
			        continue;
				// not enough parameters, remove it
				functions[i] = null;
			} else {
				//a candidate function having more than m parameters is viable only if the (m+1)-st
				//parameter has a default argument
			    IParameter[] params = function.getParameters();
			    for (int j = num - 1; j >= numParameters; j--) {
					if (!((ICPPParameter) params[j]).hasDefaultValue()) {
					    functions[i] = null;
						break;
					}
				}
			}
		}
	}
	static private boolean isMatchingFunctionDeclaration(IFunction candidate, LookupData data) {
		IASTName name = data.astName;
		ICPPASTTemplateDeclaration decl = CPPTemplates.getTemplateDeclaration(name);
		if (decl != null && !(candidate instanceof ICPPTemplateDefinition)) 
		    return false;
		
		if (candidate instanceof ICPPTemplateDefinition && decl instanceof ICPPASTTemplateSpecialization) {
			ICPPFunctionTemplate fn = CPPTemplates.resolveTemplateFunctions(new Object[] { candidate }, data.astName);
			return (fn != null && !(fn instanceof IProblemBinding));
		} 

		try {
		    IASTNode node = data.astName.getParent();
		    while (node instanceof IASTName)
		        node = node.getParent();
		    if (!(node instanceof ICPPASTFunctionDeclarator))
		        return false;
		    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
		    ICPPFunctionType ftype = (ICPPFunctionType) candidate.getType();
		    if (dtor.isConst() != ftype.isConst() || dtor.isVolatile() != ftype.isVolatile())
		        return false;
			return functionHasParameters(candidate, (IASTParameterDeclaration[]) data.functionParameters);
		} catch (DOMException e) {
		} 
		return false;
	}
	
	static private IType[] getSourceParameterTypes(Object[] params) {
	    if (params instanceof IType[]) {
	        return (IType[]) params;
	    } 
	    
	    if (params == null || params.length == 0)
	        return new IType[] { VOID_TYPE };
	    
	    if (params instanceof IASTExpression[]) {
			IASTExpression[] exps = (IASTExpression[]) params;
			IType[] result = new IType[exps.length];
			for (int i = 0; i < exps.length; i++) {
			    result[i] = CPPVisitor.getExpressionType(exps[i]);
            }
			return result;
		} else if (params instanceof IASTParameterDeclaration[]) {
		    IASTParameterDeclaration[] decls = (IASTParameterDeclaration[]) params;
		    IType[] result = new IType[decls.length];
			for (int i = 0; i < params.length; i++) {
			    result[i] = CPPVisitor.createType(decls[i].getDeclarator());
            }
			return result;
		}
		return null;
	}
	
	static private IType[] getTargetParameterTypes(IFunction fn) throws DOMException{
	    final ICPPFunctionType ftype = (ICPPFunctionType) fn.getType();
	    if (ftype == null)
	    	return IType.EMPTY_TYPE_ARRAY;
	    
		final IType[] ptypes= ftype.getParameterTypes();
	    if (fn instanceof ICPPMethod == false || fn instanceof ICPPConstructor)
	    	return ptypes;
	    	
	    final IType[] result = new IType[ptypes.length + 1];
	    System.arraycopy(ptypes, 0, result, 1, ptypes.length);
	    IScope scope = fn.getScope();
	    if (scope instanceof ICPPTemplateScope)
	    	scope = scope.getParent();
	    ICPPClassType cls = null;
	    if (scope instanceof ICPPClassScope) {
	    	cls = ((ICPPClassScope)scope).getClassType();
	    } else {
	    	cls = new CPPClassType.CPPClassTypeProblem(ASTInternal.getPhysicalNodeOfScope(scope), IProblemBinding.SEMANTIC_BAD_SCOPE, fn.getNameCharArray());
	    }
	    if (cls instanceof ICPPClassTemplate) {
	    	IBinding within = CPPTemplates.instantiateWithinClassTemplate((ICPPClassTemplate) cls);
	    	if (within instanceof ICPPClassType)
	    		cls = (ICPPClassType)within;
	    }
	    IType implicitType = cls;
	    if (ftype.isConst() || ftype.isVolatile()) {
	    	implicitType = new CPPQualifierType(implicitType, ftype.isConst(), ftype.isVolatile());
	    }
	    result[0]= new CPPReferenceType(implicitType);
	    return result;
	}
	
	static IBinding resolveFunction(LookupData data, IFunction[] fns) throws DOMException {
	    fns= (IFunction[]) ArrayUtil.trim(IFunction.class, fns);
	    if (fns == null || fns.length == 0)
	        return null;
	    
		if (data.forUsingDeclaration()) {
			return new CPPUsingDeclaration(data.astName, fns);
		}
		
		// We don't have any arguments with which to resolve the function
		if (data.functionParameters == null) {
		    return resolveTargetedFunction(data, fns);
		}
		// Reduce our set of candidate functions to only those who have the right number of parameters
		reduceToViable(data, fns);
		
		if (data.forDefinition() || data.forExplicitInstantiation()) {
			for (IFunction fn : fns) {
				if (fn != null) {
					return fn;
				}
			}
			return null;
		}
		
		IFunction bestFn = null;				// the best function
		IFunction currFn = null;				// the function currently under consideration
		Cost[] bestFnCost = null;				// the cost of the best function
		Cost[] currFnCost = null;				// the cost for the current function
				
		IASTExpression sourceExp; 
		IType source = null;					// parameter we are called with
		IType target = null;					// function's parameter
		
		int comparison;
		Cost cost = null;						// the cost of converting source to target
				 
		boolean hasWorse = false;				// currFn has a worse parameter fit than bestFn
		boolean hasBetter = false;				// currFn has a better parameter fit than bestFn
		boolean ambiguous = false;				// ambiguity, 2 functions are equally good
		boolean currHasAmbiguousParam = false;	// currFn has an ambiguous parameter conversion (ok if not bestFn)
		boolean bestHasAmbiguousParam = false;  // bestFn has an ambiguous parameter conversion (not ok, ambiguous)

		final IType[] sourceParameters = getSourceParameterTypes(data.functionParameters); // the parameters the function is being called with
		final boolean sourceVoid = (data.functionParameters == null || data.functionParameters.length == 0);
		final IType impliedObjectType = data.getImpliedObjectArgument();
		
		// Loop over all functions
		function_loop: for (int fnIdx = 0; fnIdx < fns.length; fnIdx++) {
			currFn= fns[fnIdx];
			if (currFn == null || bestFn == currFn) {
				continue;
			}
	
			final IType[] targetParameters = getTargetParameterTypes(currFn);
			final int useImplicitObj = (currFn instanceof ICPPMethod && !(currFn instanceof ICPPConstructor)) ? 1 : 0;
			final int sourceLen= Math.max(sourceParameters.length + useImplicitObj, 1);
			
			if (currFnCost == null || currFnCost.length != sourceLen) {
				currFnCost= new Cost[sourceLen];	
			}
			
			comparison = 0;
			boolean varArgs = false;
			boolean isImpliedObject= false;
			for (int j = 0; j < sourceLen; j++) {
			    if (useImplicitObj > 0) {
			    	isImpliedObject= (j == 0);
			        source= isImpliedObject ? impliedObjectType : sourceParameters[j - 1];
			        Object se= isImpliedObject || data.functionParameters.length == 0 ? null : data.functionParameters[j - 1];
			        sourceExp= se instanceof IASTExpression ? (IASTExpression) se : null;
			    } else { 
			        source = sourceParameters[j];
			        Object se= data.functionParameters.length == 0 ? null : data.functionParameters[j];
			        sourceExp= se instanceof IASTExpression ? (IASTExpression) se : null;
			    }

			    if (j < targetParameters.length) {
			    	target = targetParameters[j];
			    } else if (currFn.takesVarArgs()) {
					varArgs = true;
			    } else {
			        target = VOID_TYPE;
			    }
				
				if (isImpliedObject && ASTInternal.isStatic(currFn, false)) {
				    //13.3.1-4 for static member functions, the implicit object parameter is considered to match any object
				    cost = new Cost(source, target);
					cost.rank = Cost.IDENTITY_RANK;	// exact match, no cost
				} else if (source == null) {
				    continue function_loop;
				} else if (varArgs) {
					cost = new Cost(source, null);
					cost.rank = Cost.ELLIPSIS_CONVERSION;
				} else if (source.isSameType(target) || (sourceVoid && j == useImplicitObj)) {
					cost = new Cost(source, target);
					cost.rank = Cost.IDENTITY_RANK;	// exact match, no cost
				} else {
					cost= Conversions.checkImplicitConversionSequence(!data.forUserDefinedConversion, sourceExp, source, target, isImpliedObject);
				}
				
				currFnCost[j] = cost;
			}
			
			
			hasWorse = false;
			hasBetter = false;
			// In order for this function to be better than the previous best, it must
			// have at least one parameter match that is better that the corresponding
			// match for the other function, and none that are worse.
			int len = (bestFnCost == null || currFnCost.length < bestFnCost.length) ? currFnCost.length : bestFnCost.length;
			for (int j = 1; j <= len; j++) {
				Cost currCost = currFnCost[currFnCost.length - j];
				if (currCost.rank < 0) {
					hasWorse = true;
					hasBetter = false;
					break;
				}
				
				// An ambiguity in the user defined conversion sequence is only a problem
				// if this function turns out to be the best.
				currHasAmbiguousParam = (currCost.userDefined == 1);
				if (bestFnCost != null) {
					comparison = currCost.compare(bestFnCost[bestFnCost.length - j]);
					hasWorse |= (comparison < 0);
					hasBetter |= (comparison > 0);
				} else {
					hasBetter = true;
				}
			}
			
			// If function has a parameter match that is better than the current best,
			// and another that is worse (or everything was just as good, neither better nor worse),
			// then this is an ambiguity (unless we find something better than both later).
			ambiguous |= (hasWorse && hasBetter) || (!hasWorse && !hasBetter);
			
			if (!hasWorse) {
				// If they are both template functions, we can order them that way
				ICPPFunctionTemplate bestAsTemplate= asTemplate(bestFn);
				ICPPFunctionTemplate currAsTemplate= asTemplate(currFn);
				if (bestAsTemplate != null && currAsTemplate != null) {
					int order = CPPTemplates.orderTemplateFunctions(bestAsTemplate, currAsTemplate);
					if (order < 0) {
						hasBetter = true;	 				
					} else if (order > 0) {
						ambiguous = false;
					}
				} else if (bestAsTemplate != null) {
					// We prefer normal functions over template functions, unless we specified template arguments
					if (data.preferTemplateFunctions())
						ambiguous = false;
					else
						hasBetter = true;
				} else if (currAsTemplate != null) {
					if (data.preferTemplateFunctions())
						hasBetter = true;
					else
						ambiguous = false;
				} 
				if (hasBetter) {
					// The new best function.
					ambiguous = false;
					bestFnCost = currFnCost;
					bestHasAmbiguousParam = currHasAmbiguousParam;
					currFnCost = null;
					bestFn = currFn;
				} 
			}
		}

		if (ambiguous || bestHasAmbiguousParam) {
			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());
		}
						
		return bestFn;
	}

	private static ICPPFunctionTemplate asTemplate(IFunction function) {
		if (function instanceof ICPPSpecialization) {
			IBinding original= ((ICPPSpecialization) function).getSpecializedBinding();
			if (original instanceof ICPPFunctionTemplate) {
				return (ICPPFunctionTemplate) original;
			}
		}
		return null;
	}
	
	/**
	 * 13.4-1 A use of an overloaded function without arguments is resolved in certain contexts to a function
     * @param data
     * @param fns
     * @return
     */
    private static IBinding resolveTargetedFunction(LookupData data, IBinding[] fns) {
        if (fns.length == 1)
            return fns[0];
        
        if (data.forAssociatedScopes) {
            return new CPPCompositeBinding(fns);
        }
        
        IBinding result = null;
        
        Object o = getTargetType(data);
        IType type, types[] = null;
        int idx = -1;
        if (o instanceof IType[]) {
            types = (IType[]) o;
            type = types[++idx];
        } else {
            type = (IType) o;
        }
        
        while (type != null) {
            type = getUltimateType(type, false);
            if (type == null || !(type instanceof IFunctionType))
                return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());

            for (IBinding fn2 : fns) {
                IFunction fn = (IFunction) fn2;
                IType ft = null;
                try {
                     ft = fn.getType();
                } catch (DOMException e) {
                    ft = e.getProblem();
                }
                if (type.isSameType(ft)) {
                    if (result == null)
                        result = fn;
                    else
                        return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name());
                }
            }

            if (types != null && ++idx < types.length) {
                type = types[idx];
            } else {
                type = null;
            }
        }
                
        return (result != null) ? result : new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.name()); 
    }

    private static Object getTargetType(LookupData data) {
        IASTName name = data.astName;
        
        if (name.getPropertyInParent() == ICPPASTQualifiedName.SEGMENT_NAME)
            name = (IASTName) name.getParent();
        
        if (name.getPropertyInParent() != IASTIdExpression.ID_NAME)
            return null;
        
        IASTIdExpression idExp = (IASTIdExpression) name.getParent();
        IASTNode node = idExp;
        ASTNodeProperty prop = null;
        while (node != null) {
            prop = node.getPropertyInParent();
			if (prop == IASTDeclarator.INITIALIZER) {
	            // target is an object or reference being initialized
				IASTDeclarator dtor = (IASTDeclarator) node.getParent();
				return CPPVisitor.createType(dtor);
			} else if (prop == IASTInitializerExpression.INITIALIZER_EXPRESSION) {
                IASTInitializerExpression initExp = (IASTInitializerExpression) node.getParent();
                if (initExp.getParent() instanceof IASTDeclarator) {
	                IASTDeclarator dtor = (IASTDeclarator) initExp.getParent();
	                return CPPVisitor.createType(dtor);
                }
                return null;
            } else if (prop == IASTBinaryExpression.OPERAND_TWO && 
                     ((IASTBinaryExpression)node.getParent()).getOperator() == IASTBinaryExpression.op_assign) {
                // target is the left side of an assignment
                IASTBinaryExpression binaryExp = (IASTBinaryExpression) node.getParent();
                IASTExpression exp = binaryExp.getOperand1();
                return CPPVisitor.getExpressionType(exp);
            } else if (prop == IASTFunctionCallExpression.PARAMETERS ||
            		(prop == IASTExpressionList.NESTED_EXPRESSION &&
                    node.getParent().getPropertyInParent() == IASTFunctionCallExpression.PARAMETERS)) {
                // target is a parameter of a function
                // if this function call refers to an overloaded function, there is more than one possiblity
                // for the target type
                IASTFunctionCallExpression fnCall = null;
                int idx = -1;
                if (prop == IASTFunctionCallExpression.PARAMETERS) {
                    fnCall = (IASTFunctionCallExpression) node.getParent();
                    idx = 0;
                } else {
                    IASTExpressionList list = (IASTExpressionList) node.getParent();
                    fnCall = (IASTFunctionCallExpression) list.getParent();
                    IASTExpression[] exps = list.getExpressions();
                    for (int i = 0; i < exps.length; i++) {
                        if (exps[i] == node) {
                            idx = i;
                            break;
                        }
                    }
                }
                IFunctionType[] types = getPossibleFunctions(fnCall);
                if (types == null) return null;
                IType[] result = null;
                for (int i = 0; i < types.length && types[i] != null; i++) {
                    IType[] pts = null;
                    try {
                        pts = types[i].getParameterTypes();
                    } catch (DOMException e) {
                        continue;
                    }
                    if (pts.length > idx)
                        result = (IType[]) ArrayUtil.append(IType.class, result, pts[idx]);
                }
                return result;
            } else if (prop == IASTCastExpression.OPERAND) {
                // target is an explicit type conversion
            	IASTCastExpression cast = (IASTCastExpression) node.getParent();
            	return CPPVisitor.createType(cast.getTypeId().getAbstractDeclarator());
            } else if (prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
                //target is a template non-type parameter (14.3.2-5)
                ICPPASTTemplateId id = (ICPPASTTemplateId) node.getParent();
                IASTNode[] args = id.getTemplateArguments();
                int i = 0;
                for (; i < args.length; i++) {
                    if (args[i] == node) {
                        break;
                    }
                }
                ICPPTemplateDefinition template = (ICPPTemplateDefinition) id.getTemplateName().resolveBinding();
                if (template != null) {
                    try {
                        ICPPTemplateParameter[] ps = template.getTemplateParameters();
                        if (i < args.length && i < ps.length && ps[i] instanceof ICPPTemplateNonTypeParameter) {
                            return ((ICPPTemplateNonTypeParameter)ps[i]).getType();
                        }
                    } catch (DOMException e) {
                        return null;
                    }
                }
            } else if (prop == IASTReturnStatement.RETURNVALUE) {
                // target is the return value of a function, operator or conversion
            	while (!(node instanceof IASTFunctionDefinition)) {
            		node = node.getParent();
            	}
            	IASTDeclarator dtor = ((IASTFunctionDefinition)node).getDeclarator();
            	dtor= CPPVisitor.findInnermostDeclarator(dtor);
            	IBinding binding = dtor.getName().resolveBinding();
            	if (binding instanceof IFunction) {
            		try {
	            		IFunctionType ft = ((IFunction)binding).getType();
	            		return ft.getReturnType();
            		} catch (DOMException e) {
            		}
            	}
            } else if (prop == IASTUnaryExpression.OPERAND) {
                IASTUnaryExpression parent = (IASTUnaryExpression) node.getParent();
                if (parent.getOperator() == IASTUnaryExpression.op_bracketedPrimary ||
                		parent.getOperator() == IASTUnaryExpression.op_amper) {
                    node = parent;
                	continue;
                }
            }
            break;
        }
        return null;
    }
    
    static private IFunctionType[] getPossibleFunctions(IASTFunctionCallExpression call) {
        IFunctionType[] result = null;
        
        IASTExpression exp = call.getFunctionNameExpression();
        if (exp instanceof IASTIdExpression) {
            IASTIdExpression idExp = (IASTIdExpression) exp;
            IASTName name = idExp.getName();
	        LookupData data = createLookupData(name, false);
			try {
	            lookup(data, name);
	        } catch (DOMException e1) {
	            return null;
	        }
		    final boolean isIndexBased= data.tu == null ? false : data.tu.getIndex() != null;
	        if (data.hasResults()) {
	            Object[] items = (Object[]) data.foundItems;
	            IBinding temp = null;
	            for (Object o : items) {
	                if (o == null) break;
	                if (o instanceof IASTName) {
	    	            temp = ((IASTName) o).resolveBinding();
	                } else if (o instanceof IBinding) {
	    	            temp = (IBinding) o;
	    	            if (!declaredBefore(temp, name, isIndexBased))
	    	                continue;
	    	        } else {
	    	            continue;
	    	        }
	                
	                try {
		                if (temp instanceof IFunction) {
		                    result = (IFunctionType[]) ArrayUtil.append(IFunctionType.class, result, ((IFunction)temp).getType());
		                } else if (temp instanceof IVariable) {
                            IType type = getUltimateType(((IVariable) temp).getType(), false);
                            if (type instanceof IFunctionType)
                                result = (IFunctionType[]) ArrayUtil.append(IFunctionType.class, result, type);
		                }
	                } catch (DOMException e) {
	                }
	            }
	        }
        } else {
            IType type = CPPVisitor.getExpressionType(exp);
            type = getUltimateType(type, false);
            if (type instanceof IFunctionType) {
                result = new IFunctionType[] { (IFunctionType) type };
            }
        }
        return result;
    }
    
    /**
     * For a pointer dereference expression e1->e2, return the type that e1 ultimately evaluates to
     * when chaining overloaded class member access operators <code>operator->()</code> calls.
     * @param fieldReference
     * @return the type the field owner expression ultimately evaluates to when chaining overloaded
     * class member access operators <code>operator->()</code> calls.
     * @throws DOMException
     */
    public static IType getChainedMemberAccessOperatorReturnType(ICPPASTFieldReference fieldReference) throws DOMException {
    	IASTExpression owner = fieldReference.getFieldOwner();
    	IType result= CPPVisitor.getExpressionType(owner);

    	if (fieldReference.isPointerDereference()) {
    		IType type= getUltimateTypeUptoPointers(result);
    		boolean needCheckClassMemberAccessOperator= true;
    		if (type instanceof IPointerType) {
    			type= getUltimateTypeUptoPointers(((IPointerType) type).getType());
    			if (type instanceof ICPPClassType) {
    				needCheckClassMemberAccessOperator= false;
    			}
    		}

    		if (needCheckClassMemberAccessOperator) {
    			IType temp= result;
    			result= null;

    			// bug 205964: as long as the type is a class type, recurse. 
    			// Be defensive and allow a max of 10 levels.
    			for (int j = 0; j < 10; j++) {
    				IType uTemp= getUltimateTypeUptoPointers(temp);
    				if (uTemp instanceof ICPPClassType) {
    					/*
    					 * 13.5.6-1: An expression x->m is interpreted as (x.operator->())->m for a
    					 * class object x of type T
    					 * 
    					 * Construct an AST fragment for x.operator-> which the lookup routines can
    					 * examine for type information.
    					 */

    					CPPASTName x= new CPPASTName();
    					boolean isConst= false, isVolatile= false;
    					if (temp instanceof IQualifierType) {
    						isConst= ((IQualifierType)temp).isConst();
    						isVolatile= ((IQualifierType)temp).isVolatile();
    					}
    					x.setBinding(createVariable(x, uTemp, isConst, isVolatile));
    					
    					IASTName arw= new CPPASTName(OverloadableOperator.ARROW.toCharArray());
    					IASTFieldReference innerFR= new CPPASTFieldReference(arw, new CPPASTIdExpression(x));
    					innerFR.setParent(fieldReference); // connect to the AST 

    					ICPPFunction op = CPPSemantics.findOperator(innerFR, (ICPPClassType) uTemp);
    					if (op != null) {
    						result= temp= op.getType().getReturnType();
    						continue;
    					}
    				}
    				break;
    			}
    		}
    	}

    	return result;
    }
    
    private static ICPPVariable createVariable(IASTName name, final IType type, final boolean isConst, final boolean isVolatile) {
    	return new CPPVariable(name) {
			@Override public IType getType() {
				return new CPPQualifierType(type, isConst, isVolatile);
			}
		};
    }
    
    public static ICPPFunction findOperator(IASTExpression exp, ICPPClassType cls) {
		IScope scope = null;
		try {
			scope = cls.getCompositeScope();
		} catch (DOMException e1) {
			return null;
		}
		if (scope == null)
			return null;
		
		CPPASTName astName = new CPPASTName();
		astName.setParent(exp);
	    astName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
	    LookupData data = null;
	    
	    if (exp instanceof IASTUnaryExpression) {
	    	astName.setName(OverloadableOperator.STAR.toCharArray());
		    data = new LookupData(astName);
		    data.forceQualified = true;
		    data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
	    } else if (exp instanceof IASTArraySubscriptExpression) {
		    astName.setName(OverloadableOperator.BRACKET.toCharArray());
		    data = new LookupData(astName);
		    data.forceQualified = true;
		    data.functionParameters = new IASTExpression[] { ((IASTArraySubscriptExpression)exp).getSubscriptExpression() };
		} else if (exp instanceof IASTFieldReference) {
			astName.setName(OverloadableOperator.ARROW.toCharArray());
			data = new LookupData(astName);
			data.forceQualified = true;
			data.functionParameters = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		} else if (exp instanceof IASTFunctionCallExpression) {
			astName.setName(OverloadableOperator.PAREN.toCharArray());
			data = new LookupData(astName);
			data.forceQualified = true;
			final IASTExpression paramExpression = ((IASTFunctionCallExpression)exp).getParameterExpression();
			if (paramExpression == null) {
				data.functionParameters= IASTExpression.EMPTY_EXPRESSION_ARRAY;
			}
			else if (paramExpression instanceof IASTExpressionList) {
				data.functionParameters= ((IASTExpressionList) paramExpression).getExpressions();
			} else {
				data.functionParameters = new IASTExpression[] {paramExpression};
			}
		} else {
			return null;
		}
		
		try {
		    lookup(data, scope);
		    IBinding binding = resolveAmbiguities(data, astName);
		    if (binding instanceof ICPPFunction)
		    	return (ICPPFunction) binding;
		} catch (DOMException e) {
		}
		return null;
	}
	
	public static IBinding[] findBindings(IScope scope, String name, boolean qualified) throws DOMException{
		return findBindings(scope, name.toCharArray(), qualified);
	}
	
	public static IBinding[] findBindings(IScope scope, char[] name, boolean qualified) throws DOMException{
	    CPPASTName astName = new CPPASTName();
	    astName.setName(name);
	    astName.setParent(ASTInternal.getPhysicalNodeOfScope(scope));
	    astName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
	    
		LookupData data = new LookupData(astName);
		data.forceQualified = qualified;
		return standardLookup(data, scope);
	}
	
	public static IBinding[] findBindingsForContentAssist(IASTName name, boolean prefixLookup) {
		LookupData data = createLookupData(name, true);
		data.contentAssist = true;
		data.prefixLookup = prefixLookup;
		data.foundItems = new CharArrayObjectMap(2);

		return contentAssistLookup(data, name);
	}

    private static IBinding[] contentAssistLookup(LookupData data, Object start) {        
        try {
            lookup(data, start);
        } catch (DOMException e) {
        }
        CharArrayObjectMap map = (CharArrayObjectMap) data.foundItems;
        IBinding[] result = null;
        if (!map.isEmpty()) {
            char[] key = null;
            Object obj = null;
            int size = map.size(); 
            for (int i = 0; i < size; i++) {
                key = map.keyAt(i);
                obj = map.get(key);
                if (obj instanceof IBinding) {
                    result = (IBinding[]) ArrayUtil.append(IBinding.class, result, obj);
                } else if (obj instanceof IASTName) {
					IBinding binding = ((IASTName) obj).resolveBinding();
                    if (binding != null && !(binding instanceof IProblemBinding))
                        result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
                } else if (obj instanceof Object[]) {
					Object[] objs = (Object[]) obj;
					for (int j = 0; j < objs.length && objs[j] != null; j++) {
						Object item = objs[j];
						if (item instanceof IBinding) {
		                    result = (IBinding[]) ArrayUtil.append(IBinding.class, result, item);
						} else if (item instanceof IASTName) {
							IBinding binding = ((IASTName) item).resolveBinding();
		                    if (binding != null && !(binding instanceof IProblemBinding))
		                        result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
		                }
					}
                }
            }
        }

        return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
    }

    private static IBinding[] standardLookup(LookupData data, Object start) {
    	try {
			lookup(data, start);
		} catch (DOMException e) {
			return new IBinding[] { e.getProblem() };
		}
		
		Object[] items = (Object[]) data.foundItems;
		if (items == null)
		    return new IBinding[0];
		
		ObjectSet<IBinding> set = new ObjectSet<IBinding>(items.length);
		IBinding binding = null;
		for (Object item : items) {
		    if (item instanceof IASTName) {
		        binding = ((IASTName) item).resolveBinding();
		    } else if (item instanceof IBinding) {
		        binding = (IBinding) item;
		    } else {
		        binding = null;
		    }
		    
		    if (binding != null) {
		    	if (binding instanceof ICPPUsingDeclaration) {
		    		set.addAll(((ICPPUsingDeclaration) binding).getDelegates());
		    	} else if (binding instanceof CPPCompositeBinding) {
                    set.addAll(((CPPCompositeBinding) binding).getBindings());
			    } else {
			        set.put(binding);
			    }
		    }
		}
		
	    return set.keyArray(IBinding.class);
    }
    
	public static boolean isSameFunction(IFunction function, IASTDeclarator declarator) {
		IASTName name = declarator.getName();
		ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);

		boolean fnIsTemplate = (function instanceof ICPPFunctionTemplate);
		boolean dtorIsTemplate = (templateDecl != null); 
		if (fnIsTemplate && dtorIsTemplate) {
			return CPPTemplates.isSameTemplate((ICPPTemplateDefinition)function, name);
		} else if (fnIsTemplate ^ dtorIsTemplate) {
			return false;
		} 
		IType type = null;
		try {
			type = function.getType();
			return type.isSameType(CPPVisitor.createType(declarator));
		} catch (DOMException e) {
		}
		return false;
	}
}
