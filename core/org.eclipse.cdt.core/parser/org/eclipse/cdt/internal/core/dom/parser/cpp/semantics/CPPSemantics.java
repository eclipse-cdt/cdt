/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *    Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeOrFunctionSet;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCat;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalNamespaceScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.Context;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Conversions.UDCMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.Cost.Rank;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

/**
 * Name resolution
 */
public class CPPSemantics {
	/**
	 * The maximum depth to search ancestors before assuming infinite looping.
	 */
	public static final int MAX_INHERITANCE_DEPTH= 16;
	
    public static final ASTNodeProperty STRING_LOOKUP_PROPERTY =
    		new ASTNodeProperty("CPPSemantics.STRING_LOOKUP_PROPERTY - STRING_LOOKUP"); //$NON-NLS-1$
	public static final String EMPTY_NAME = ""; //$NON-NLS-1$
	public static final char[] OPERATOR_ = new char[] {'o','p','e','r','a','t','o','r',' '};  
	private static final char[] CALL_FUNCTION = "call-function".toCharArray(); //$NON-NLS-1$
	public static final IType VOID_TYPE = new CPPBasicType(Kind.eVoid, 0);
	public static final IType INT_TYPE = new CPPBasicType(Kind.eInt, 0);

	// Set to true for debugging.
	public static boolean traceBindingResolution = false;
	public static int traceIndent= 0;
	
	// special return value for costForFunctionCall
	private static final FunctionCost CONTAINS_DEPENDENT_TYPES = new FunctionCost(null, 0);
	static protected IBinding resolveBinding(IASTName name) {
		if (traceBindingResolution) {
			for (int i = 0; i < traceIndent; i++) 
				System.out.print("  "); //$NON-NLS-1$
			System.out.println("Resolving " + name + ':' + ((ASTNode) name).getOffset()); //$NON-NLS-1$
			traceIndent++;
		}
		if (name instanceof CPPASTNameBase) {
			((CPPASTNameBase) name).incResolutionDepth();
		}
		
		// 1: get some context info off of the name to figure out what kind of lookup we want
		LookupData data = createLookupData(name);
		
		try {
            // 2: lookup
            lookup(data, null);

            // Perform argument dependent lookup
            if (data.checkAssociatedScopes() && !data.hasTypeOrMemberFunctionResult()) {
                doKoenigLookup(data);
            }
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
			traceIndent--;
			for (int i = 0; i < traceIndent; i++) 
				System.out.print("  "); //$NON-NLS-1$
			System.out.println("Resolved  " + name + ':' + ((ASTNode) name).getOffset() +  //$NON-NLS-1$
					" to " + DebugUtil.toStringWithClass(binding) + ':' + System.identityHashCode(binding)); //$NON-NLS-1$
		}
		return binding;
	}

	protected static IBinding postResolution(IBinding binding, IASTName name) {
		LookupData data = createLookupData(name);
		return postResolution(binding, data);
	}

    private static IBinding postResolution(IBinding binding, LookupData data) {
        if (binding instanceof IProblemBinding)
        	return binding;
        
        if (binding == null && data.checkClassContainingFriend()) {
        	// 3.4.1-10 if we don't find a name used in a friend declaration in the member declaration's class
        	// we should look in the class granting friendship
        	IASTNode parent = data.astName.getParent();
        	while (parent != null && !(parent instanceof ICPPASTCompositeTypeSpecifier))
        		parent = parent.getParent();
        	if (parent instanceof ICPPASTCompositeTypeSpecifier) {
        		IScope scope = ((ICPPASTCompositeTypeSpecifier) parent).getScope();
        		try {
		    		lookup(data, scope);
		    		binding = resolveAmbiguities(data, data.astName);
        		} catch (DOMException e) {
        			binding = e.getProblem();
        		}
        	}
        }

        /* 14.6.1-1: 
         * Within the scope of a class template, when the name of the template is neither qualified nor 
         * followed by <, it is equivalent to the name followed by the template parameters enclosed in <>.
         */
		if (binding instanceof ICPPClassTemplate && !(binding instanceof ICPPClassSpecialization) &&
				!(binding instanceof ICPPTemplateParameter) && !(data.astName instanceof ICPPASTTemplateId)) {
			ASTNodeProperty prop = data.astName.getPropertyInParent();
			if (prop != ICPPASTTemplateId.TEMPLATE_NAME && prop != ICPPASTQualifiedName.SEGMENT_NAME) {
				// You cannot use a class template name outside of the class template scope, 
				// mark it as a problem.
				IBinding replacement= CPPTemplates.isUsedInClassTemplateScope((ICPPClassTemplate) binding, data.astName);
				if (replacement != null) {
					binding= replacement;
				} else {
					boolean ok= false;
					IASTNode node= data.astName.getParent();
					while (node != null && !ok) {
						if (node instanceof ICPPASTTemplateId ||
								node instanceof ICPPASTTemplatedTypeTemplateParameter) {
							ok= true; // can be argument or default-value for template template parameter
							break;
						} else if (node instanceof IASTElaboratedTypeSpecifier) {
							IASTNode parent= node.getParent();
							if (parent instanceof IASTSimpleDeclaration) {
								IASTDeclSpecifier declspec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
								if (declspec instanceof ICPPASTDeclSpecifier) {
									if (((ICPPASTDeclSpecifier) declspec).isFriend()) {
										ok= true;  // a friend class template declarations uses resolution.
										break;
									}
								}
							}
						}
						node= node.getParent();
					}
					if (!ok) {
						binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE,
								data.getFoundBindings());
					}
				}
			}
		} else if (binding instanceof ICPPDeferredClassInstance) {
			// try to replace binding by the one pointing to the enclosing template declaration.
			ICPPDeferredClassInstance dcl= (ICPPDeferredClassInstance) binding;
			IBinding usedHere= CPPTemplates.isUsedInClassTemplateScope(dcl.getClassTemplate(), data.astName);
			if (usedHere instanceof ICPPDeferredClassInstance) {
				ICPPDeferredClassInstance alt= (ICPPDeferredClassInstance) usedHere;
				if (CPPTemplates.areSameArguments(alt.getTemplateArguments(), dcl.getTemplateArguments())) {
					binding= alt;
				}
			}
		}
		
		if (binding instanceof ICPPClassType) {
			if (convertClassToConstructor(data.astName)) {
				if (binding instanceof IIndexBinding) {
					binding= data.tu.mapToAST((ICPPClassType) binding);
				}
				ICPPClassType cls= (ICPPClassType) binding;

				try {
					if (data.astName instanceof ICPPASTTemplateId && cls instanceof ICPPClassTemplate) {
						if (data.tu != null) {
							ICPPASTTemplateId id = (ICPPASTTemplateId) data.astName;
							ICPPTemplateArgument[] args = CPPTemplates.createTemplateArgumentArray(id);
							IBinding inst= CPPTemplates.instantiate((ICPPClassTemplate) cls, args, false);
							if (inst instanceof ICPPClassType) {
								cls= (ICPPClassType) inst;
							}
						}
					}
					if (cls instanceof ICPPUnknownBinding) {
						binding= new CPPUnknownConstructor(cls);
					} else {
						binding= CPPSemantics.resolveFunction(data, cls.getConstructors(), true);
					}
				} catch (DOMException e) {
					return e.getProblem();
				}
			}
		}
        
        IASTName name= data.astName;
        IASTNode nameParent= name.getParent();
		if (nameParent instanceof ICPPASTTemplateId) {
			if (binding instanceof ICPPTemplateInstance) {
				final ICPPTemplateInstance instance = (ICPPTemplateInstance) binding;
				binding = instance.getSpecializedBinding();
				name.setBinding(binding);
				((ICPPASTTemplateId) nameParent).setBinding(instance);
			} 
			name= (ICPPASTTemplateId) nameParent;
			nameParent= name.getParent();
		}
		if (nameParent instanceof ICPPASTQualifiedName) {
			if (name == ((ICPPASTQualifiedName) nameParent).getLastName()) {
				name= (IASTName) nameParent;
				nameParent= name.getParent();
			}
		}
		
		// if the lookup in base-classes ran into a deferred instance, use the computed unknown binding.
		final ASTNodeProperty namePropertyInParent = name.getPropertyInParent();
		if (binding == null && data.skippedScope != null) {
			if (data.hasFunctionArguments()) {
				binding= new CPPUnknownFunction(data.skippedScope, name.getSimpleID());
			} else {
				if (namePropertyInParent == IASTNamedTypeSpecifier.NAME) {
					binding= new CPPUnknownClass(data.skippedScope, name.getSimpleID());
				} else {
					binding= new CPPUnknownBinding(data.skippedScope, name.getSimpleID());
				}
			}
		}
		
        if (binding != null) {
	        if (namePropertyInParent == IASTNamedTypeSpecifier.NAME) {
	        	if (!(binding instanceof IType || binding instanceof ICPPConstructor)) {
	        		IASTNode parent = name.getParent().getParent();
	        		if (parent instanceof IASTTypeId && parent.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
	        			if (!(binding instanceof IType)) {
	        				// a type id needs to hold a type
	        				binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE,
	        						data.getFoundBindings());
	        			}
	        			// don't create a problem here
	        		} else {
	        			binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE,
	        					data.getFoundBindings());
	        		}
	        	} 
	        } else if (namePropertyInParent == IASTIdExpression.ID_NAME) {
	        	if (binding instanceof IType) {
		        	IASTNode parent= name.getParent().getParent();
		        	if (parent instanceof ICPPASTTemplatedTypeTemplateParameter) {
			        	// default for template template parameter is an id-expression, which is a type.
					} else if (parent instanceof ICPPASTUnaryExpression
							&& ((ICPPASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_sizeofParameterPack) {
						// argument of sizeof... can be a type
					} else if ((binding instanceof ICPPUnknownType || binding instanceof ITypedef || binding instanceof IEnumeration)
							&& convertClassToConstructor(data.astName)) {
						// constructor or simple-type constructor
					} else {
		        		binding= new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_INVALID_TYPE,
		        				data.getFoundBindings());
		        	}
	        	}
	        }
        }
        
        // explicit function specializations are found via name resolution, need to
        // add name as definition and check the declaration specifier.
		if (binding instanceof IFunction) {
			if (data.forFunctionDeclaration()) {
				IASTNode declaration= data.astName;
				while (declaration instanceof IASTName)
					declaration= declaration.getParent();
				while (declaration instanceof IASTDeclarator)
					declaration= declaration.getParent();

				binding= checkDeclSpecifier(binding, data.astName, declaration);
				if (!(binding instanceof IProblemBinding)) {
					if (declaration instanceof ICPPASTFunctionDefinition) {
						ASTInternal.addDefinition(binding, data.astName);
					}
				}
			}
		}
		
		// Definitions of static fields are found via name resolution, need to add name to
		// the binding to get the right type of arrays that may be declared incomplete.
		if (binding instanceof ICPPField && data.astName.isDefinition()) { 
			IASTNode declaration= data.astName;
			while (declaration instanceof IASTName)
				declaration= declaration.getParent();
			while (declaration instanceof IASTDeclarator)
				declaration= declaration.getParent();
			if (declaration.getPropertyInParent() != IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
				ASTInternal.addDefinition(binding, data.astName);
			}
		}
		
		// If we're still null...
		if (binding == null) {
			if (name instanceof ICPPASTQualifiedName && data.forFunctionDeclaration()) {
				binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND,
						data.getFoundBindings());
			} else {
				binding = new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_NAME_NOT_FOUND,
						data.getFoundBindings());
			}
		}
        return binding;
    }

	private static boolean convertClassToConstructor(IASTName name) {
		if (name == null)
			return false;
		final ASTNodeProperty propertyInParent = name.getPropertyInParent();
		if (propertyInParent == CPPSemantics.STRING_LOOKUP_PROPERTY || propertyInParent == null)
			return false;
		
		if (propertyInParent == ICPPASTTemplateId.TEMPLATE_NAME)
			return false;
		
		IASTNode parent= name.getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) parent).getLastName() != name)
				return false;
			parent= parent.getParent();
		}
		if (parent instanceof ICPPASTConstructorChainInitializer) {
			return true;
		}
		if (parent instanceof IASTExpression) {
			ASTNodeProperty propInParent= parent.getPropertyInParent();
			if (parent instanceof IASTIdExpression) {
				parent= parent.getParent();
			}
			while (parent instanceof IASTUnaryExpression
					&& ((IASTUnaryExpression) parent).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
				propInParent = parent.getPropertyInParent();
				parent= parent.getParent();
			}
			if (parent instanceof IASTFunctionCallExpression && propInParent == IASTFunctionCallExpression.FUNCTION_NAME) {
				return true;
			}
		} else if (parent instanceof ICPPASTNamedTypeSpecifier) {
			parent= parent.getParent();
			if (parent instanceof IASTTypeId && parent.getParent() instanceof ICPPASTNewExpression) {
				IASTDeclarator dtor = ((IASTTypeId) parent).getAbstractDeclarator();
				if (dtor != null && dtor.getPointerOperators().length == 0)
					return true;
			}
		} 
		return false;
	}

	private static void doKoenigLookup(LookupData data) throws DOMException {
		data.ignoreUsingDirectives = true;
		data.forceQualified = true;
		Set<ICPPNamespaceScope> associated = getAssociatedScopes(data);
		for (ICPPNamespaceScope scope : associated) {
			if (!data.visited.containsKey(scope)) {
				lookup(data, scope);
			}
		}
	}
       
	static IBinding checkDeclSpecifier(IBinding binding, IASTName name, IASTNode decl) {
		// check for empty declaration specifiers
		if (!isCtorOrConversionOperator(binding)) {
			IASTDeclSpecifier declspec= null;
			if (decl instanceof IASTSimpleDeclaration) {
				declspec= ((IASTSimpleDeclaration) decl).getDeclSpecifier();
			} else if (decl instanceof IASTFunctionDefinition) {
				declspec= ((IASTFunctionDefinition) decl).getDeclSpecifier();
			}
			if (declspec != null && CPPVisitor.doesNotSpecifyType(declspec)) {
				binding= new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_TYPE);
			}
		}
		return binding;
	}

	private static boolean isCtorOrConversionOperator(IBinding binding) {
		if (binding instanceof ICPPConstructor)
			return true;
		
		if (binding instanceof ICPPMethod) {
			ICPPMethod m= (ICPPMethod) binding;
			if (m.isDestructor())
				return true;
			return isConversionOperator(m);
		}
		return false;
	}

	public static LookupData createLookupData(IASTName name) {
		LookupData data = new LookupData(name);
		IASTNode parent = name.getParent();
		
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
			data.setFunctionParameters(((ICPPASTFunctionDeclarator) parent).getParameters());
		} else if (parent instanceof IASTIdExpression) {
			IASTNode grand= parent.getParent();
			while (grand instanceof IASTUnaryExpression
					&& ((IASTUnaryExpression) grand).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
				parent= grand;
				grand = grand.getParent();
			}
		    if (parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
		        parent = parent.getParent();
				IASTInitializerClause[] args = ((IASTFunctionCallExpression) parent).getArguments();
				data.setFunctionArguments(false, args);
			}
		} else if (parent instanceof ICPPASTFieldReference) {
			IASTNode grand= parent.getParent();
			while (grand instanceof IASTUnaryExpression
					&& ((IASTUnaryExpression) grand).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
				parent= grand;
				grand = grand.getParent();
			}
			if (parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
				IASTInitializerClause[] exp = ((IASTFunctionCallExpression) parent.getParent()).getArguments();
				data.setFunctionArguments(false, exp);
			}
		} else if (parent instanceof ICPPASTNamedTypeSpecifier && parent.getParent() instanceof IASTTypeId) {
	        IASTTypeId typeId = (IASTTypeId) parent.getParent();
	        if (typeId.getParent() instanceof ICPPASTNewExpression) {
	            ICPPASTNewExpression newExp = (ICPPASTNewExpression) typeId.getParent();
	            IASTInitializer init = newExp.getInitializer();
	            if (init == null) {
	            	data.setFunctionArguments(false);
	            } else if (init instanceof ICPPASTConstructorInitializer) {
					data.setFunctionArguments(false, ((ICPPASTConstructorInitializer) init).getArguments());
	            } else if (init instanceof ICPPASTInitializerList) {
	            	data.setFunctionArguments(false, (ICPPASTInitializerList) init);
	            }
	        }
		} else if (parent instanceof ICPPASTConstructorChainInitializer) {
			ICPPASTConstructorChainInitializer ctorinit = (ICPPASTConstructorChainInitializer) parent;
			IASTInitializer init = ctorinit.getInitializer();
            if (init instanceof ICPPASTConstructorInitializer) {
				data.setFunctionArguments(false, ((ICPPASTConstructorInitializer) init).getArguments());
            } else if (init instanceof ICPPASTInitializerList) {
            	data.setFunctionArguments(false, (ICPPASTInitializerList) init);
            }
		}
		
		return data;
	}

    private static Set<ICPPNamespaceScope> getAssociatedScopes(LookupData data) {
    	if (!data.hasFunctionArguments())
    		return Collections.emptySet();
    	
        IType[] ps = data.getFunctionArgumentTypes();
        Set<ICPPNamespaceScope> namespaces = new HashSet<ICPPNamespaceScope>(2);
        ObjectSet<IType> handled = new ObjectSet<IType>(2);
        for (IType p : ps) {
            try {
                getAssociatedScopes(p, namespaces, handled, data.tu);
            } catch (DOMException e) {
            }
        }
        return namespaces;
    }

    // 3.4.2-2 
    private static void getAssociatedScopes(IType t, Set<ICPPNamespaceScope> namespaces,
    		ObjectSet<IType> handled, CPPASTTranslationUnit tu) throws DOMException {
        t = getNestedType(t, TDEF | CVTYPE | PTR | ARRAY | REF);
    	if (t instanceof IBinding) {
            if (handled.containsKey(t))
            	return;
            handled.put(t);
            
    		IBinding owner= ((IBinding) t).getOwner();
    		if (owner instanceof ICPPClassType) {
    			getAssociatedScopes((IType) owner, namespaces, handled, tu);
    		} else {
    			getAssociatedNamespaceScopes(getContainingNamespaceScope((IBinding) t, tu), namespaces);
    		}
    	}
		if (t instanceof ICPPClassType && !(t instanceof ICPPClassTemplate)) {
			ICPPClassType ct= (ICPPClassType) t;
			ICPPBase[] bases = ct.getBases();
			for (ICPPBase base : bases) {
				if (!(base instanceof IProblemBinding)) {
					IBinding b = base.getBaseClass();
					if (b instanceof IType)
						getAssociatedScopes((IType) b, namespaces, handled, tu);
				}
			}
			// Furthermore, if T is a class template ... 
			// * ... types of the template arguments for template type parameters 
			//       (excluding template template parameters); 
			// * ... owners of which any template template arguments are members; 
			if (ct instanceof ICPPTemplateInstance) {
				ICPPTemplateArgument[] args = ((ICPPTemplateInstance) ct).getTemplateArguments();
				for (ICPPTemplateArgument arg : args) {
					if (arg.isTypeValue()) {
						getAssociatedScopes(arg.getTypeValue(), namespaces, handled, tu);
					}
				}
			}
		} else if (t instanceof IFunctionType) {
		    IFunctionType ft = (IFunctionType) t;
		    getAssociatedScopes(ft.getReturnType(), namespaces, handled, tu);
		    IType[] ps = ft.getParameterTypes();
		    for (IType pt : ps) {
		        getAssociatedScopes(pt, namespaces, handled, tu);
		    }
		} else if (t instanceof ICPPPointerToMemberType) {
		    final ICPPPointerToMemberType pmt = (ICPPPointerToMemberType) t;
			getAssociatedScopes(pmt.getMemberOfClass(), namespaces, handled, tu);
	        getAssociatedScopes(pmt.getType(), namespaces, handled, tu);
		} else if (t instanceof FunctionSetType) {
			FunctionSetType fst= (FunctionSetType) t;
			for (ICPPFunction fn : fst.getFunctionSet()) { 
				getAssociatedScopes(fn.getType(), namespaces, handled, tu);
			}
		}			
    }

    private static ICPPNamespaceScope getContainingNamespaceScope(IBinding binding,
    		CPPASTTranslationUnit tu) throws DOMException {
		if (binding == null)
			return null;
        IScope scope = binding.getScope();
        if (scope instanceof IIndexScope) {
        	scope= tu.mapToASTScope((IIndexScope) scope);
        }
        while (scope != null && !(scope instanceof ICPPNamespaceScope)) {
            scope = getParentScope(scope, tu);
        }
        return (ICPPNamespaceScope) scope;
    }

	public static void getAssociatedNamespaceScopes(ICPPNamespaceScope scope, Set<ICPPNamespaceScope> namespaces) {
		if (scope == null || !namespaces.add(scope))
			return;
				
		if (scope instanceof ICPPInternalNamespaceScope) {
			final ICPPInternalNamespaceScope internalScope = (ICPPInternalNamespaceScope) scope;
			for (ICPPNamespaceScope mem : internalScope.getEnclosingNamespaceSet()) {
				namespaces.add(mem);
			}
		}
	}
    
	static ICPPScope getLookupScope(IASTName name, LookupData data) throws DOMException {
	    IASTNode parent = name.getParent();
	    IScope scope = null;
    	if (parent instanceof ICPPASTBaseSpecifier) {
    	    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) parent.getParent();
    	    IASTName n = compSpec.getName();
    	    if (n instanceof ICPPASTQualifiedName) {
    	        n = ((ICPPASTQualifiedName) n).getLastName();
    	    }
	        scope = CPPVisitor.getContainingScope(n);
	    } else {
	    	scope = CPPVisitor.getContainingScope(name, data);
	    }
    	if (scope instanceof ICPPScope) {
    		return (ICPPScope) scope;
    	} else if (scope instanceof IProblemBinding) {
    		return new CPPScope.CPPScopeProblem(((IProblemBinding) scope).getASTNode(),
    				IProblemBinding.SEMANTIC_BAD_SCOPE, ((IProblemBinding) scope).getNameCharArray());
    	}
    	return new CPPScope.CPPScopeProblem(name, IProblemBinding.SEMANTIC_BAD_SCOPE);
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
	static CharArrayObjectMap mergePrefixResults(CharArrayObjectMap dest, Object source, boolean scoped) {
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
        		key= (so instanceof IBinding) ? ((IBinding) so).getNameCharArray() : ((IASTName) so).getSimpleID();
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
			            obj = ArrayUtil.addAll(Object.class, (Object[]) obj, (Object[]) so);
			    } else {
			        if (so instanceof IBinding || so instanceof IASTName) {
			            obj = new Object[] { obj, so };
			        } else {
			            Object[] temp = new Object[((Object[]) so).length + 1];
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
	static protected void lookup(LookupData data, IScope start) throws DOMException {
		if (data.astName == null) 
			return;

		if (start == null && lookupDestructor(data, start)) {
			return;
		}
		
		ICPPScope nextScope= null;
		ICPPTemplateScope nextTmplScope= null;
		if (start instanceof ICPPScope) {
			nextScope= (ICPPScope) start;
		} else {
			nextScope= getLookupScope(data.astName, data);

			if (nextScope instanceof ICPPTemplateScope) {
				nextTmplScope= (ICPPTemplateScope) nextScope;
				nextScope= getParentScope(nextScope, data.tu);
			} else {
				nextTmplScope= enclosingTemplateScope(data.astName);
			}
			if (!data.usesEnclosingScope && nextTmplScope != null) {
				nextTmplScope= null;
				if (dependsOnTemplateFieldReference(data.astName)) {
					data.checkPointOfDecl= false;
				}
			}
		}
		if (nextScope == null)
			return;

		boolean friendInLocalClass = false;
		if (nextScope instanceof ICPPClassScope && data.forFriendship()) {
			try {
				ICPPClassType cls = ((ICPPClassScope) nextScope).getClassType();
				friendInLocalClass = !cls.isGloballyQualified();
			} catch (DOMException e) {
			}
		}

		final IIndexFileSet fileSet= getIndexFileSet(data);
		while (nextScope != null || nextTmplScope != null) {
			// when the non-template scope is no longer contained within the first template scope,
			// we use the template scope for the next iteration.
			boolean useTemplScope= false;
			if (nextTmplScope != null) {
				useTemplScope= true;
				if (nextScope instanceof IASTInternalScope) {
					final IASTNode node= ((IASTInternalScope) nextScope).getPhysicalNode();
					if (node != null && nextTmplScope.getTemplateDeclaration().contains(node)) {
						useTemplScope= false;
					} 
				}
			}
			ICPPScope scope= useTemplScope ? nextTmplScope : nextScope;
			if (scope instanceof IIndexScope && data.tu != null) {
				scope= (ICPPScope) data.tu.mapToASTScope(((IIndexScope) scope));
			}
			
			if (!data.usingDirectivesOnly && !(data.ignoreMembers && scope instanceof ICPPClassScope)) {
				mergeResults(data, getBindingsFromScope(scope, fileSet, data), true);

				// Nominate using-directives found in this block or namespace.
				if (scope instanceof ICPPNamespaceScope) {
					final ICPPNamespaceScope blockScope= (ICPPNamespaceScope) scope;
					
					if (data.qualified() && blockScope.getKind() != EScopeKind.eLocal) {
						lookupInlineNamespaces(data, fileSet, blockScope);
					}
					if (data.contentAssist || !data.hasResults() || !data.qualified()) {
						// Nominate namespaces
						nominateNamespaces(data, blockScope);
					}
				}
			}

			// Lookup in nominated namespaces
			if (!data.ignoreUsingDirectives && scope instanceof ICPPNamespaceScope && !(scope instanceof ICPPBlockScope)) {
				if (!data.hasResults() || !data.qualified() || data.contentAssist) {
					lookupInNominated(data, fileSet, (ICPPNamespaceScope) scope);
				}
			}
			
			if (friendInLocalClass && !(scope instanceof ICPPClassScope))
				return;			
			if (!data.contentAssist && data.hasResultOrProblem()) 
				return;
			
			// Lookup in base classes
			if (!data.usingDirectivesOnly && scope instanceof ICPPClassScope) {
				BaseClassLookup.lookupInBaseClasses(data, (ICPPClassScope) scope, fileSet);
				if (!data.contentAssist && data.hasResultOrProblem()) 
					return;
			}
			
			if (data.qualified() && !(scope instanceof ICPPTemplateScope)) {
				if (data.ignoreUsingDirectives || data.usingDirectives.isEmpty())
					return;
				data.usingDirectivesOnly = true;
			}
			
			// compute next scopes
			if (useTemplScope && nextTmplScope != null) {
				nextTmplScope= enclosingTemplateScope(nextTmplScope.getTemplateDeclaration());
			} else {
				nextScope= getParentScope(scope, data.tu);
			}
		}
	}

	private static void lookupInlineNamespaces(LookupData data, IIndexFileSet fileSet, ICPPNamespaceScope namespace) throws DOMException {
		if (namespace instanceof ICPPInternalNamespaceScope) {
			ICPPInternalNamespaceScope ns= (ICPPInternalNamespaceScope) namespace;
			for (ICPPInternalNamespaceScope inline : ns.getInlineNamespaces()) {
				mergeResults(data, getBindingsFromScope(inline, fileSet, data), true);
				lookupInlineNamespaces(data, fileSet, inline);
				nominateNamespaces(data, inline);
			}
		}
	}

	private static void nominateNamespaces(LookupData data, final ICPPNamespaceScope blockScope)
			throws DOMException {
		final boolean isBlockScope = blockScope.getKind() == EScopeKind.eLocal;
		if (!isBlockScope) {
			data.visited.put(blockScope);	// Mark as searched.
			if (data.tu != null) {
				data.tu.handleAdditionalDirectives(blockScope);
			}
		}
		ICPPUsingDirective[] uds= blockScope.getUsingDirectives();
		if (uds != null && uds.length > 0) {
			HashSet<ICPPNamespaceScope> handled= new HashSet<ICPPNamespaceScope>();
			for (final ICPPUsingDirective ud : uds) {
				if (declaredBefore(ud, data.astName, false)) {
					storeUsingDirective(data, blockScope, ud, handled);
				}
			}
		}
	}

	private static boolean lookupDestructor(LookupData data, IScope start) throws DOMException {
		IASTName typeDtorName= data.astName;
		final char[] typeDtorChars= typeDtorName.getSimpleID();
		if (typeDtorChars.length == 0 || typeDtorChars[0] != '~') 
			return false;
		
		// Assume class C; typedef C T;
		// When looking up ~T the strategy is to lookup T::~C in two steps:
		// * First resolve 'T', then compute '~C' and resolve it.
		CPPASTQualifiedName syntheticName= new CPPASTQualifiedName();
		IASTNode parent= typeDtorName.getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName dqname= (ICPPASTQualifiedName) parent;
			if (dqname.getLastName() != data)
				return false;

			syntheticName.setFullyQualified(dqname.isFullyQualified());
			IASTName[] children = dqname.getNames();
			for (IASTName child : children) {
				if (child != data) {
					final IASTName childCopy = child.copy();
					childCopy.setBinding(child.resolveBinding());
					syntheticName.addName(childCopy);
				}
			}
			syntheticName.setOffsetAndLength((ASTNode) parent);
			syntheticName.setParent(parent.getParent());
			syntheticName.setPropertyInParent(parent.getPropertyInParent());
		} else {
			syntheticName.setOffsetAndLength((ASTNode) typeDtorName);
			syntheticName.setParent(parent);
			syntheticName.setPropertyInParent(typeDtorName.getPropertyInParent());
		}
		char[] tchars= new char[typeDtorChars.length-1];
		System.arraycopy(typeDtorChars, 1, tchars, 0, tchars.length);

		final CPPASTName typeName = new CPPASTName(tchars);
		typeName.setOffsetAndLength((ASTNode) typeDtorName);
		syntheticName.addName(typeName);

		final CPPASTName classDtorName = new CPPASTName(typeDtorChars);
		classDtorName.setOffsetAndLength((ASTNode) typeDtorName);
		syntheticName.addName(classDtorName);
		
		IBinding type= resolveBinding(typeName);
		if (!(type instanceof ITypedef)) 
			return false;
		
		IType t= SemanticUtil.getNestedType((ITypedef) type, TDEF);
		if (t instanceof ICPPUnknownBinding || t instanceof IProblemBinding ||
				!(t instanceof ICPPClassType)) {
			return false;
		}
		
		ICPPClassType classType= (ICPPClassType) t;
		final IScope scope = ((ICPPClassType) t).getCompositeScope();
		if (scope == null) {
			return false;
		}
		
		char[] classChars= classType.getNameCharArray();
		char[] classDtorChars= new char[classChars.length+1];
		classDtorChars[0]= '~';
		System.arraycopy(classChars, 0, classDtorChars, 1, classChars.length);
		classDtorName.setName(classDtorChars);
		
		data.astName = classDtorName;
		try {
			lookup(data, scope);
		} finally {
			data.astName= typeDtorName;
		}
		return true;
	}

	/**
	 * Checks whether the name directly or indirectly depends on the this pointer.
	 */
	private static boolean dependsOnTemplateFieldReference(IASTName astName) {
		if (astName.getPropertyInParent() != IASTFieldReference.FIELD_NAME) 
			return false;

		final boolean[] result= {false};
		final IASTExpression fieldOwner = ((IASTFieldReference) astName.getParent()).getFieldOwner();
		fieldOwner.accept(new ASTVisitor() {
			{
				shouldVisitNames= true;
				shouldVisitExpressions= true;
			}

			@Override
			public int visit(IASTName name) {
				IBinding b= name.resolvePreBinding();
				if (b instanceof ICPPUnknownBinding || b instanceof ICPPTemplateDefinition) {
					result[0]= true;
					return PROCESS_ABORT;
				}
				if (b instanceof ICPPMember) {
					ICPPMember mem= (ICPPMember) b;
					if (!mem.isStatic()) {
						ICPPClassType owner= mem.getClassOwner();
						if (owner instanceof ICPPUnknownBinding || owner instanceof ICPPTemplateDefinition) {
							result[0]= true;
							return PROCESS_ABORT;
						}
					}
				}
				if (b instanceof IVariable) {
					try {
						IType t= SemanticUtil.getUltimateType(((IVariable) b).getType(), true);
						if (t instanceof ICPPUnknownBinding || t instanceof ICPPTemplateDefinition) {
							result[0]= true;
							return PROCESS_ABORT;
						}
					} catch (DOMException e) {
					}
				}
				if (name instanceof ICPPASTTemplateId)
					return PROCESS_SKIP;
				return PROCESS_CONTINUE;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTLiteralExpression) {
					if (((IASTLiteralExpression) expression).getKind() == IASTLiteralExpression.lk_this) {
						final IType thisType = SemanticUtil.getNestedType(typeOrFunctionSet(expression), TDEF | ALLCVQ | PTR | ARRAY | MPTR | REF);
						if (thisType instanceof ICPPUnknownBinding || thisType instanceof ICPPTemplateDefinition) {
							result[0]= true;
							return PROCESS_ABORT;
						}
					}
				}
				if (expression instanceof IASTUnaryExpression) {
					switch (((IASTUnaryExpression) expression).getOperator()) {
					case IASTUnaryExpression.op_sizeof:
					case IASTUnaryExpression.op_sizeofParameterPack:
					case IASTUnaryExpression.op_typeid:
					case IASTUnaryExpression.op_throw:
						return PROCESS_SKIP;
					}
				} else if (expression instanceof IASTTypeIdExpression) {
					switch (((IASTTypeIdExpression) expression).getOperator()) {
					case IASTTypeIdExpression.op_sizeof:
					case IASTTypeIdExpression.op_typeid:
						return PROCESS_SKIP;
					}
				} else if (expression instanceof IASTCastExpression) {
					if (!((IASTCastExpression) expression).getTypeId().accept(this)) {
						return PROCESS_ABORT;
					}
					return PROCESS_SKIP;
				} else if (expression instanceof ICPPASTNewExpression) {
					if (!((ICPPASTNewExpression) expression).getTypeId().accept(this)) {
						return PROCESS_ABORT;
					}
					return PROCESS_SKIP;
				} else if (expression instanceof ICPPASTSimpleTypeConstructorExpression) {
					return PROCESS_SKIP;
				} else if (expression instanceof IASTTypeIdInitializerExpression) {
					if (!((IASTTypeIdInitializerExpression) expression).getTypeId().accept(this)) {
						return PROCESS_ABORT;
					}
					return PROCESS_SKIP;
				}
				return PROCESS_CONTINUE;
			}
		});
		return result[0];
	}

	static IBinding[] getBindingsFromScope(ICPPScope scope, final IIndexFileSet fileSet, LookupData data) throws DOMException {
		IBinding[] bindings;

		// For internal scopes we need to check the point of declaration
		if (scope instanceof ICPPASTInternalScope) {
			final IASTName astName = data.astName;
			final ICPPASTInternalScope internalScope = (ICPPASTInternalScope) scope;
			bindings= internalScope.getBindings(astName, true, data.prefixLookup, fileSet, data.checkPointOfDecl);

			// Bug 103857: Members declared after the point of completion cannot be
			//     found in the partial AST, we look them up in the index
			if (data.checkWholeClassScope && scope instanceof ICPPClassScope) {
				final IASTTranslationUnit tu = astName.getTranslationUnit();
				if (tu instanceof ASTTranslationUnit && ((ASTTranslationUnit) tu).isForContentAssist()) {
					IIndex index = tu.getIndex();
					IASTNode node = internalScope.getPhysicalNode();
					if (index != null && node != null && node.contains(astName)) {
						IBinding indexBinding= index.adaptBinding(((ICPPClassScope) scope).getClassType());
						if (indexBinding instanceof ICPPClassType) {
							IScope scopeInIndex= ((ICPPClassType) indexBinding).getCompositeScope();
							bindings= ArrayUtil.addAll(bindings, scopeInIndex.getBindings(astName, true, data.prefixLookup, fileSet));
						}
					}
				}
			}
		} else { 
			// For index scopes the point of declaration is ignored.
			bindings= scope.getBindings(data.astName, true, data.prefixLookup, fileSet);
		}
		
		if (data.typesOnly) {
			return removeObjects(bindings);
		}
		return bindings;
	}

	private static IBinding[] removeObjects(final IBinding[] bindings) {
		final int length = bindings.length;
		IBinding[] copy= null;
		int pos= 0;
		for (int i = 0; i < length; i++) {
			final IBinding binding= bindings[i];
			IBinding check= binding;
			if (binding instanceof ICPPUsingDeclaration) {
				IBinding[] delegates= ((ICPPUsingDeclaration) binding).getDelegates();
				if (delegates.length > 0)
					check= delegates[0];
			}
			if (check instanceof IType || check instanceof ICPPNamespace) {
				if (copy != null) {
					copy[pos]= binding;
				} 
				pos++;
			} else {
				if (copy == null) {
					copy= new IBinding[length-1];
					System.arraycopy(bindings, 0, copy, 0, pos);
				}
			} 
		}
		if (pos == 0)
			return IBinding.EMPTY_BINDING_ARRAY;
		
		return copy == null ? bindings : copy;
	}

	private static ICPPTemplateScope enclosingTemplateScope(IASTNode node) {
		IASTNode parent= node.getParent();
		if (parent instanceof IASTName) {
			if (parent instanceof ICPPASTTemplateId) {
				node= parent;
				parent= node.getParent();
			}
			if (parent instanceof ICPPASTQualifiedName) {
				ICPPASTQualifiedName qname= (ICPPASTQualifiedName) parent;
				if (qname.isFullyQualified() || qname.getNames()[0] != node)
					return null;
			}
		}
		while (!(parent instanceof ICPPASTTemplateDeclaration)) {
			if (parent == null)
				return null;
			parent= parent.getParent();
		}
		return ((ICPPASTTemplateDeclaration) parent).getScope();
	}

	static ICPPScope getParentScope(IScope scope, CPPASTTranslationUnit unit) throws DOMException {
		IScope parentScope= scope.getParent();
		// the index cannot return the translation unit as parent scope
		if (unit != null) {
			if (parentScope == null
					&& (scope instanceof IIndexScope || scope instanceof ICPPClassSpecializationScope)) {
				parentScope = unit.getScope();
			} else if (parentScope instanceof IIndexScope) {
				parentScope = unit.mapToASTScope((IIndexScope) parentScope);
			}
		}
		return (ICPPScope) parentScope;
	}

	/**
	 * Stores the using directive with the scope where the members of the nominated namespace will appear.
	 * In case of an unqualified lookup the transitive directives are stored, also. This is important because
	 * the members nominated by a transitive directive can appear before those of the original directive.
	 */
	private static void storeUsingDirective(LookupData data, ICPPNamespaceScope container, 
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
	private static ICPPScope getCommonEnclosingScope(IScope s1, IScope s2, CPPASTTranslationUnit tu) throws DOMException { 
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

	public static void populateCache(ICPPASTInternalScope scope) {
		IASTNode[] nodes = null;
		IASTNode parent= ASTInternal.getPhysicalNodeOfScope(scope);
		
		IASTName[] namespaceDefs = null;
		int namespaceIdx = -1;
		
		if (parent instanceof IASTCompoundStatement) {
			IASTNode p = parent.getParent();
		    if (p instanceof IASTFunctionDefinition) {
		        ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) ((IASTFunctionDefinition) p).getDeclarator();
		        nodes = dtor.getParameters();
		    } else if (p instanceof ICPPASTLambdaExpression) {
		        ICPPASTFunctionDeclarator dtor = ((ICPPASTLambdaExpression) p).getDeclarator();
		        if (dtor != null) {
		        	nodes = dtor.getParameters();
		        }
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
		} else if (parent instanceof ICPPASTNamespaceDefinition) {
		    // need binding because namespaces can be split
		    CPPNamespace namespace = (CPPNamespace) ((ICPPASTNamespaceDefinition) parent).getName().resolveBinding();
		    namespaceDefs = namespace.getNamespaceDefinitions();
		    nodes = ((ICPPASTNamespaceDefinition) namespaceDefs[++namespaceIdx].getParent()).getDeclarations();
			while (nodes.length == 0 && ++namespaceIdx < namespaceDefs.length) {
				nodes= ((ICPPASTNamespaceDefinition) namespaceDefs[namespaceIdx].getParent()).getDeclarations();
			}
		} else if (parent instanceof ICPPASTFunctionDeclarator) {
		    ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
		    nodes = dtor.getParameters();
		} else if (parent instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) parent;
			nodes = template.getTemplateParameters();
		} else if (parent instanceof ICPPASTForStatement) {
			ICPPASTForStatement forStatement = (ICPPASTForStatement) parent;
			final IASTDeclaration conditionDeclaration = forStatement.getConditionDeclaration();
			IASTStatement initDeclaration= forStatement.getInitializerStatement();
			if (conditionDeclaration != null) {
				nodes= new IASTNode[] {initDeclaration, conditionDeclaration};
			} else {
				nodes= new IASTNode[] {initDeclaration};
			}
		} else if (parent instanceof ICPPASTEnumerationSpecifier) {
			// The enumeration scope contains the enumeration items
	    	for (IASTEnumerator enumerator : ((ICPPASTEnumerationSpecifier) parent).getEnumerators()) {
	    		ASTInternal.addName(scope, enumerator.getName());
	    	}
	    	return;
		}
		
		int idx = -1;
		IASTNode item = (nodes != null ? (nodes.length > 0 ? nodes[++idx] : null) : parent);
		IASTNode[][] nodeStack = null;
		int[] nodeIdxStack = null;
		int nodeStackPos = -1;
		while (item != null) {
		    if (item instanceof ICPPASTLinkageSpecification) {
		        IASTDeclaration[] decls = ((ICPPASTLinkageSpecification) item).getDeclarations();
		        if (decls != null && decls.length > 0) {
			        nodeStack = (IASTNode[][]) ArrayUtil.append(IASTNode[].class, nodeStack, nodes);
			        nodeIdxStack = ArrayUtil.setInt(nodeIdxStack, ++nodeStackPos, idx);
			        nodes = ((ICPPASTLinkageSpecification) item).getDeclarations();
			        idx = 0;
				    item = nodes[idx];
				    continue;
		        }
			}
		    while (item instanceof IASTLabelStatement) 
		    	item= ((IASTLabelStatement) item).getNestedStatement();
		    if (item instanceof IASTDeclarationStatement)
		        item = ((IASTDeclarationStatement) item).getDeclaration();
			if (item instanceof ICPPASTUsingDirective) {
				if (scope instanceof ICPPNamespaceScope) {
				    final ICPPNamespaceScope nsscope = (ICPPNamespaceScope) scope;
					final ICPPASTUsingDirective usingDirective = (ICPPASTUsingDirective) item;
					nsscope.addUsingDirective(new CPPUsingDirective(usingDirective));
				}
			} else if (item instanceof ICPPASTNamespaceDefinition) {
				final ICPPASTNamespaceDefinition nsDef = (ICPPASTNamespaceDefinition) item;
				final boolean isUnnamed = nsDef.getName().getLookupKey().length == 0;
				final boolean isInline = nsDef.isInline();
				if (isUnnamed || isInline) {
					if (scope instanceof CPPNamespaceScope) {
						final CPPNamespaceScope nsscope = (CPPNamespaceScope) scope;
						nsscope.addUsingDirective(new CPPUsingDirective(nsDef));
						if (isInline) {
							nsscope.addInlineNamespace(nsDef);
						}
					}
				}
				if (!isUnnamed) {
					populateCache(scope, item);
				}
			} else {
				populateCache(scope, item);
			}
		    
			if (nodes != null && ++idx < nodes.length) {
				item = nodes[idx];
			} else {
			    item = null;
			    while (true) {
				    if (namespaceDefs != null) {
				        // check all definitions of this namespace
					    while (++namespaceIdx < namespaceDefs.length) {
					        nodes = ((ICPPASTNamespaceDefinition) namespaceDefs[namespaceIdx].getParent()).getDeclarations();
						    if (nodes.length > 0) {
						        idx = 0;
						        item = nodes[0];
						        break;
						    }     
					    }
				    } else if (parent instanceof IASTCompoundStatement && nodes instanceof IASTParameterDeclaration[]) {
				    	// function body, we were looking at parameters, now check the body itself
				        IASTCompoundStatement compound = (IASTCompoundStatement) parent;
						nodes = compound.getStatements(); 
						if (nodes.length > 0) {
					        idx = 0;
					        item = nodes[0];
					        break;
					    }  
				    } else if (parent instanceof ICPPASTCatchHandler) {
				    	parent = ((ICPPASTCatchHandler) parent).getCatchBody();
				    	if (parent instanceof IASTCompoundStatement) {
				    		nodes = ((IASTCompoundStatement) parent).getStatements();
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
	}

	public static void populateCache(ICPPASTInternalScope scope, IASTNode node) {
	    IASTDeclaration declaration = null;
	    if (node instanceof ICPPASTTemplateDeclaration) {
			declaration = ((ICPPASTTemplateDeclaration) node).getDeclaration();
	    } else if (node instanceof IASTDeclaration) { 
	        declaration = (IASTDeclaration) node;
	    } else if (node instanceof IASTDeclarationStatement) {
			declaration = ((IASTDeclarationStatement) node).getDeclaration();
	    } else if (node instanceof ICPPASTCatchHandler) {
			declaration = ((ICPPASTCatchHandler) node).getDeclaration();
	    } else if (node instanceof ICPPASTSwitchStatement) {
        	declaration = ((ICPPASTSwitchStatement) node).getControllerDeclaration();
        } else if (node instanceof ICPPASTIfStatement) {
        	declaration = ((ICPPASTIfStatement) node).getConditionDeclaration();
	    } else if (node instanceof ICPPASTWhileStatement) {
	    	declaration = ((ICPPASTWhileStatement) node).getConditionDeclaration();
	    } else if (node instanceof IASTParameterDeclaration) {
		    IASTParameterDeclaration parameterDeclaration = (IASTParameterDeclaration) node;
		    IASTDeclarator dtor = parameterDeclaration.getDeclarator();
			IASTDeclarator innermost= dtor;
			while (dtor != null) {
				if (dtor instanceof IASTAmbiguousDeclarator)
					return;
				innermost= dtor;
				dtor= dtor.getNestedDeclarator();
			}
            if (innermost != null) { // could be null when content assist in the declSpec
    			IASTName declName = innermost.getName();
    			ASTInternal.addName(scope, declName);
    			return;
            }
		} else if (node instanceof ICPPASTTemplateParameter) {
			IASTName name = CPPTemplates.getTemplateParameterName((ICPPASTTemplateParameter) node);
			ASTInternal.addName(scope, name);
			return;
		}
		if (declaration == null || declaration instanceof ASTAmbiguousNode) {
			return;
		}

		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) simpleDeclaration.getDeclSpecifier();
			IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
			if (!declSpec.isFriend()) {
				for (IASTDeclarator declarator : declarators) {
					IASTDeclarator innermost= null;
					while (declarator != null) {
						if (declarator instanceof IASTAmbiguousDeclarator) {
							innermost= null;
							break;
						}
						innermost= declarator;
						declarator= declarator.getNestedDeclarator();
					}
					if (innermost != null) {
						IASTName declaratorName = innermost.getName();
						ASTInternal.addName(scope, declaratorName);
					}
				}
			}
			
			// Declaration specifiers defining or declaring a type
			IASTName specName = null;
			final EScopeKind scopeKind = scope.getKind();
			if (declSpec instanceof IASTElaboratedTypeSpecifier) {
				// 3.3.1.5 Point of declaration
				if (!declSpec.isFriend()) {
					if (declarators.length == 0 || scopeKind == EScopeKind.eGlobal
							|| scopeKind == EScopeKind.eNamespace) {
						specName = ((IASTElaboratedTypeSpecifier) declSpec).getName();
					}
				}
			} else if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) declSpec;
				specName = compSpec.getName();
				
				// Anonymous union or struct (GCC supports anonymous structs too)
				if (declarators.length == 0 && specName.getLookupKey().length == 0) {
				    IASTDeclaration[] decls = compSpec.getMembers();
				    for (IASTDeclaration decl : decls) {
                        populateCache(scope, decl);
                    }
				} 
			} else if (declSpec instanceof ICPPASTEnumerationSpecifier) {
				ICPPASTEnumerationSpecifier enumeration = (ICPPASTEnumerationSpecifier) declSpec;
			    specName = enumeration.getName();

			    // Add unscoped enumerators to the enclosing scope
			    if (!enumeration.isScoped()) {
			    	for (IASTEnumerator enumerator : enumeration.getEnumerators()) {
			    		ASTInternal.addName(scope, enumerator.getName());
			    	}
			    }
			}
			if (specName != null) {
				if (!(specName instanceof ICPPASTQualifiedName)) {
					ASTInternal.addName(scope, specName);
				}
			}
			// Collect friends and elaborated type specifiers with declarators 
			// from nested classes
			if (declarators.length > 0 || declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				switch (scopeKind) {
				case eLocal:
				case eGlobal:
				case eNamespace:
					NamespaceTypeCollector visitor = new NamespaceTypeCollector(scope);
					declSpec.accept(visitor);
					for (IASTDeclarator dtor : declarators) {
						dtor.accept(visitor);
					}
					break;
				case eEnumeration:
				case eClassType:
				case eTemplateDeclaration:
					break;
				}
			}
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) declaration;
			IASTName name = using.getName();
			if (name instanceof ICPPASTQualifiedName) {
				name = ((ICPPASTQualifiedName) name).getLastName();
			}
			ASTInternal.addName(scope, name);
		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			IASTName namespaceName = ((ICPPASTNamespaceDefinition) declaration).getName();
			ASTInternal.addName(scope, namespaceName);
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			IASTName alias = ((ICPPASTNamespaceAlias) declaration).getAlias();
			ASTInternal.addName(scope, alias);
		} else if (declaration instanceof IASTFunctionDefinition) {
 			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			final IASTDeclSpecifier declSpec = functionDef.getDeclSpecifier();
			IASTFunctionDeclarator declarator = functionDef.getDeclarator();

			if (!((ICPPASTDeclSpecifier) declSpec).isFriend()) {
				// check the function itself
				IASTName declName = ASTQueries.findInnermostDeclarator(declarator).getName();
				ASTInternal.addName(scope, declName);
			}
			// Collect elaborated type specifiers and friends 
			final EScopeKind scopeKind = scope.getKind();
			switch (scopeKind) {
			case eLocal:
			case eGlobal:
			case eNamespace:
				NamespaceTypeCollector visitor = new NamespaceTypeCollector(scope);
				declSpec.accept(visitor);
				declarator.accept(visitor);
				break;
			case eClassType:
			case eTemplateDeclaration:
			case eEnumeration:
				break;
			}
		}
	}

	/**
	 * Perform lookup in nominated namespaces that appear in the given scope. For unqualified lookups the method assumes
	 * that transitive directives have been stored in the lookup-data. For qualified lookups the transitive directives
	 * are considered if the lookup of the original directive returns empty.
	 * @param fileSet 
	 */
	private static void lookupInNominated(LookupData data, IIndexFileSet fileSet, ICPPNamespaceScope scope) throws DOMException {
		List<ICPPNamespaceScope> allNominated= data.usingDirectives.remove(scope);
		while (allNominated != null) {
			for (ICPPNamespaceScope nominated : allNominated) {
				if (data.visited.containsKey(nominated)) {
					continue;
				}
				data.visited.put(nominated);

				boolean found = false;
				IBinding[] bindings= getBindingsFromScope(nominated, fileSet, data);
				if (bindings != null && bindings.length > 0) {
					mergeResults(data, bindings, true);
					found = true;
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

	public static IBinding resolveAmbiguities(IASTName name, Object[] bindings) {
	    bindings = ArrayUtil.trim(Object.class, bindings);
	    if (bindings == null || bindings.length == 0) {
	        return null;
	    } else if (bindings.length == 1) {
	    	IBinding candidate= null;
	        if (bindings[0] instanceof IBinding) {
	        	candidate= (IBinding) bindings[0];
	        } else if (bindings[0] instanceof IASTName) {
	    		candidate= ((IASTName) bindings[0]).getPreBinding();
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
	        	
		        // specialization is selected during instantiation
		        if (candidate instanceof ICPPTemplateInstance)
		        	candidate= ((ICPPTemplateInstance) candidate).getSpecializedBinding();
	        	
		        if (!(candidate instanceof ICPPFunctionTemplate))
		        	return candidate;
	        }
	    }
	    
	    if (name.getPropertyInParent() != STRING_LOOKUP_PROPERTY) {
		    LookupData data = createLookupData(name);
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
                result = (IBinding[]) ArrayUtil.append(IBinding.class, result, ((IASTName) binding).resolveBinding());
            else if (binding instanceof IBinding)
                result = (IBinding[]) ArrayUtil.append(IBinding.class, result, binding);
        }
        return new CPPCompositeBinding(result);
	}
	
	static public boolean declaredBefore(Object obj, IASTNode node, boolean indexBased) {
	    if (node == null) 
	    	return true;
	    
	    final int pointOfRef= ((ASTNode) node).getOffset();
	    if (node.getPropertyInParent() == STRING_LOOKUP_PROPERTY && pointOfRef <= 0) {
	    	return true;
	    }

	    ASTNode nd = null;
	    if (obj instanceof ICPPSpecialization) {
	        obj = ((ICPPSpecialization) obj).getSpecializedBinding();
	    }
	    
	    int pointOfDecl= -1;
	    if (obj instanceof ICPPInternalBinding) {
	        ICPPInternalBinding cpp = (ICPPInternalBinding) obj;
	        // for bindings in global or namespace scope we don't know whether there is a 
	        // previous declaration in one of the skipped header files. For bindings that
	        // are likely to be redeclared we need to assume that there is a declaration
	        // in one of the headers.
	    	if (indexBased && acceptDeclaredAfter(cpp)) {
	    		return true;
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
	    } else {
	        if (indexBased && obj instanceof IASTName) {
	        	IBinding b= ((IASTName) obj).getPreBinding();
	        	if (b instanceof ICPPInternalBinding) {
	        		if (acceptDeclaredAfter((ICPPInternalBinding) b))
	        			return true;
	        	}
	        }
	    	if (obj instanceof ASTNode) {
	    		nd = (ASTNode) obj;
	    	} else if (obj instanceof ICPPUsingDirective) {
	    		pointOfDecl= ((ICPPUsingDirective) obj).getPointOfDeclaration();
	    	}
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
                    pointOfDecl = ((ASTNode) init).getOffset() - 1;
                else
                    pointOfDecl = ((ASTNode) dtor).getOffset() + ((ASTNode) dtor).getLength();
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

	private static boolean acceptDeclaredAfter(ICPPInternalBinding cpp) {
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
		return false;
	}
	
	private static IBinding resolveAmbiguities(LookupData data, IASTName name) throws DOMException {
	    if (!data.hasResults() || data.contentAssist)
	        return null;
	      
	    final boolean indexBased= data.tu != null && data.tu.getIndex() != null;	    
	    @SuppressWarnings("unchecked")
	    ObjectSet<ICPPFunction> fns= ObjectSet.EMPTY_SET;
	    IBinding type = null;
	    IBinding obj  = null;
	    IBinding temp = null;
	    
	    Object[] items = (Object[]) data.foundItems;
	    for (int i = 0; i < items.length && items[i] != null; i++) {
	        Object o = items[i];
	        boolean declaredBefore = !data.checkPointOfDecl || declaredBefore(o, name, indexBased);
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
	        	if (checkResolvedNamesOnly) {
	        		temp = on.getPreBinding();
	        	} else {
	        		temp= on.resolvePreBinding();
	        	}
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
	        	IBinding[] bindings = ((CPPCompositeBinding) temp).getBindings();
	        	mergeResults(data, bindings, false);
	        	items = (Object[]) data.foundItems;
	        	continue;
	        } else if (temp instanceof ICPPFunction) {
	        	if (temp instanceof ICPPTemplateInstance) {
	        		temp= ((ICPPTemplateInstance) temp).getSpecializedBinding();
	        		if (!(temp instanceof IFunction))
	        			continue;
	        	}
	        	if (fns == ObjectSet.EMPTY_SET)
	        		fns = new ObjectSet<ICPPFunction>(2);
	        	fns.put((ICPPFunction) temp);
	        } else if (temp instanceof IType) {
		        // specializations are selected during instantiation
	        	if (temp instanceof ICPPClassTemplatePartialSpecialization) 
		        	continue;
	        	if (temp instanceof ICPPTemplateInstance) {
	        		temp= ((ICPPTemplateInstance) temp).getSpecializedBinding();
	        		if (!(temp instanceof IType))
	        			continue;
	        	}

	        	if (type == null) {
	                type = temp;
	        	} else if (!type.equals(temp)) {
	        		int c = compareByRelevance(data.tu, type, temp);
	        		if (c < 0) {
        				type= temp;
	        		} else if (c == 0) {
        				if (((IType) type).isSameType((IType) temp)) {
        					if (type instanceof ITypedef && !(temp instanceof ITypedef)) {
        						// Between same types prefer non-typedef.
        						type= temp;
        					}
        				} else {
        					return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
        							data.getFoundBindings());
        				}
        			}
	            }
	        } else {
	        	if (obj == null) {
	        		obj = temp;
	        	} else if (obj.equals(temp)) {
	        	    // Ok, delegates are synonyms.
	        	} else {
	        		int c = compareByRelevance(data.tu, obj, temp);
	        		if (c < 0) {
	        			obj= temp;
	        		} else if (c == 0) {
	        			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
	        					data.getFoundBindings());
	        		}
	        	}
	        }
	    }
	    if (data.forUsingDeclaration()) {
        	int cmp= -1;
	        if (obj != null) {
	        	cmp= 1;
	            if (fns.size() > 0) {
		    		IFunction[] fnArray= fns.keyArray(IFunction.class);
					cmp= compareByRelevance(data, obj, fnArray);
					if (cmp == 0) {
						return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
								data.getFoundBindings());
					} 
	            }
	        }

	        IBinding[] bindings = null;
	        if (cmp > 0) {
	            bindings = (IBinding[]) ArrayUtil.append(IBinding.class, bindings, obj);
	            bindings = (IBinding[]) ArrayUtil.append(IBinding.class, bindings, type);
	        } else {
	            bindings = (IBinding[]) ArrayUtil.append(IBinding.class, bindings, type);
	            bindings = (IBinding[]) ArrayUtil.addAll(IBinding.class, bindings, fns.keyArray());
	        }
	        bindings = (IBinding[]) ArrayUtil.trim(IBinding.class, bindings);
	        ICPPUsingDeclaration composite = new CPPUsingDeclaration(data.astName, bindings);
	        return composite;	
	    }

	    if (obj != null && type != null) {
	    	if (obj instanceof ICPPNamespace) {
	    		if (compareByRelevance(data.tu, type, obj) >= 0) {
	    			obj= null;
	    		}
	    	} else if (!data.typesOnly && overrulesByRelevance(data, type, obj)) {
	    		obj= null;
	    	}
	    }

	    if (data.typesOnly) {
	    	if (obj instanceof ICPPNamespace)
	    		return obj;

	    	return type;
	    }
	    
	    
		if (fns.size() > 0) {
	    	final ICPPFunction[] fnArray = fns.keyArray(ICPPFunction.class);
	    	if (type != null && overrulesByRelevance(data, type, fnArray)) {
	    		return type;
	    	} 
	    	
	    	if (obj != null) {
	    		int cmp= compareByRelevance(data, obj, fnArray);
	    		if (cmp == 0) {
	    			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
	    					data.getFoundBindings());
	    		}
	    		if (cmp > 0) {
	    			return obj;
	    		}
	    	}
			return resolveFunction(data, fnArray, true);
	    }
	    
	    if (obj != null) {
	    	return obj;
	    }
	    return type;
	}

	/**
	 * Compares two bindings for relevance in the context of an AST. AST bindings are
	 * considered more relevant than index ones since the index may be out of date,
	 * built for a different configuration, etc. Index bindings reachable through includes
	 * are more relevant than unreachable ones.
	 * @param ast
	 * @param b1
	 * @param b2
	 * @return 1 if binding <code>b1</code> is more relevant than <code>b2</code>; 0 if
	 * the two bindings have the same relevance; -1 if <code>b1</code> is less relevant than
	 * <code>b2</code>.
	 */
	static int compareByRelevance(IASTTranslationUnit tu, IBinding b1, IBinding b2) {
		boolean b1FromIndex= isFromIndex(b1);
		boolean b2FromIndex= isFromIndex(b2);
		if (b1FromIndex != b2FromIndex) {
			return !b1FromIndex ? 1 : -1;
		} else if (b1FromIndex) {
			// Both are from index.
			if (tu != null) {
	    		boolean b1Reachable= isReachableFromAst(tu, b1);
	    		boolean b2Reachable= isReachableFromAst(tu, b2);
	    		if (b1Reachable != b2Reachable) {
	    			return b1Reachable ? 1 : -1;
	    		}
			}
		}
		return 0;
	}

	/**
	 * Compares two bindings for relevance in the context of an AST. Type bindings are
	 * considered to overrule object bindings when the former is reachable but the 
	 * latter is not.
	 */
	static boolean overrulesByRelevance(LookupData data, IBinding type, IBinding b2) {
		if (data != null && data.tu != null) {
			return !isReachableFromAst(data.tu, b2) && isReachableFromAst(data.tu, type);
		}
		return false;
	}

	/**
	 * Compares a binding with a list of function candidates for relevance in the 
	 * context of an AST. Types are considered to overrule object bindings when 
	 * the former is reachable but none of the functions are.
	 */
	static boolean overrulesByRelevance(LookupData data, IBinding type, IFunction[] fns) {
		if (data == null || data.tu == null) {
			return false;
		}
		
		for (int i = 0; i < fns.length; i++) {
			if (!isFromIndex(fns[i])) {
				return false;	// function from ast
			}
		}
		
		if (!isReachableFromAst(data.tu, type)) {
			return false;
		}
		
		for (IFunction fn : fns) {
			if (isReachableFromAst(data.tu, fn)) {
				return false;	// function from ast
			}
		}
    	return true; 
	}


	/**
	 * Compares two bindings for relevance in the context of an AST. AST bindings are
	 * considered more relevant than index ones since the index may be out of date,
	 * built for a different configuration, etc. Index bindings reachable through includes
	 * are more relevant than unreachable ones.
	 * @param ast
	 * @param b1
	 * @param b2
	 * @return 1 if binding <code>b1</code> is more relevant than <code>b2</code>; 0 if
	 * the two bindings have the same relevance; -1 if <code>b1</code> is less relevant than
	 * <code>b2</code>.
	 */
	static int compareByRelevance(LookupData data, IName b1, IName b2) {
		boolean b1FromIndex= (b1 instanceof IIndexName);
		boolean b2FromIndex= (b2 instanceof IIndexName);
		if (b1FromIndex != b2FromIndex) {
			return !b1FromIndex ? 1 : -1;
		} else if (b1FromIndex) {
			// Both are from index.
			if (data.tu != null) {
	    		boolean b1Reachable= isReachableFromAst(data.tu, b1);
	    		boolean b2Reachable= isReachableFromAst(data.tu, b2);
	    		if (b1Reachable != b2Reachable) {
	    			return b1Reachable ? 1 : -1;
	    		}
			}
		}
		return 0;
	}

	/**
	 * Compares a binding with a list of function candidates for relevance in the context of an AST. AST bindings are
	 * considered more relevant than index ones since the index may be out of date,
	 * built for a different configuration, etc. Index bindings reachable through includes
	 * are more relevant than unreachable ones.
	 * @return 1 if binding <code>obj</code> is more relevant than the function candidates; 0 if
	 * the they have the same relevance; -1 if <code>obj</code> is less relevant than
	 * the function candidates.
	 */
	static int compareByRelevance(LookupData data, IBinding obj, IFunction[] fns) {
		if (isFromIndex(obj)) {
    		for (int i = 0; i < fns.length; i++) {
    			if (!isFromIndex(fns[i])) {
    				return -1;	// function from ast
    			}
    		}
    		// everything is from the index
    		if (!isReachableFromAst(data.tu, obj)) {
    			return -1; // obj not reachable
    		} 

    		for (IFunction fn : fns) {
    			if (isReachableFromAst(data.tu, fn)) {
    				return 0; // obj reachable, 1 function reachable
    			}
    		}
    		return 1;  // no function is reachable
		} 
		
		// obj is not from the index
		for (int i = 0; i < fns.length; i++) {
			if (!isFromIndex(fns[i])) {
				return 0; // obj and function from ast
			}
		}
    	return 1; // only obj is from ast.
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
	
	/**
	 * Checks if a binding is an AST binding, or is reachable from the AST through includes.
	 * The binding is assumed to belong to the AST, if it is not an IIndexBinding and not
	 * a specialization of an IIndexBinding.
	 * @param ast
	 * @param binding
	 * @return <code>true</code> if the <code>binding</code> is reachable from <code>ast</code>.
	 */
	private static boolean isReachableFromAst(IASTTranslationUnit ast, IBinding binding) {
		IIndexBinding indexBinding = null;
		if (binding instanceof IIndexBinding) {
			indexBinding = (IIndexBinding) binding;
		}
		if (binding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
			if (binding instanceof IIndexBinding) {
				indexBinding = (IIndexBinding) binding;
			}
		}
		if (indexBinding == null) {
			// We don't check if the binding really belongs to the AST specified by the ast
			// parameter assuming that the caller doesn't deal with two ASTs at a time.
			return true;
		}
		IIndexFileSet indexFileSet = ast.getIndexFileSet();
		return indexFileSet != null && indexFileSet.containsDeclaration(indexBinding);
	}

	/**
	 * Checks if a binding is an AST binding, or is reachable from the AST through includes.
	 * The binding is assumed to belong to the AST, if it is not an IIndexBinding and not
	 * a specialization of an IIndexBinding.
	 * @param ast
	 * @param binding
	 * @return <code>true</code> if the <code>binding</code> is reachable from <code>ast</code>.
	 */
	private static boolean isReachableFromAst(IASTTranslationUnit ast, IName name) {
		if (!(name instanceof IIndexName)) {
			return true;
		}
		IIndexName indexName = (IIndexName) name;
		try {
			IIndexFile file= indexName.getFile();
			IIndexFileSet indexFileSet = ast.getIndexFileSet();
			return indexFileSet != null && indexFileSet.contains(file);
		} catch (CoreException e) {
			return false;
		}
	}

	private static ICPPFunction[] selectByArgumentCount(LookupData data, ICPPFunction[] functions) throws DOMException {
	    assert !data.forFunctionDeclaration();

	    int argumentCount = data.getFunctionArgumentCount();

	    // Trim the list down to the set of viable functions
	    ICPPFunction[] result= new ICPPFunction[functions.length];
	    int idx= 0;
	    for (ICPPFunction fn : functions) {
			if (fn != null && !(fn instanceof IProblemBinding)) {
				if (fn instanceof ICPPUnknownBinding) {
					return new ICPPFunction[] {fn};
				} 
				
				// The index is optimized to provide the function type, try not to use the parameters
				// as long as possible.
				final ICPPFunctionType ft = fn.getType();
				final IType[] parameterTypes = ft.getParameterTypes();
				int numPars = parameterTypes.length;
				if (numPars == 1 && SemanticUtil.isVoidType(parameterTypes[0]))
					numPars= 0;

				int numArgs = argumentCount;
				if (fn instanceof ICPPMethod && data.argsContainImpliedObject)
					numArgs--;

				boolean ok;
				if (numArgs > numPars) {
					// more arguments than parameters --> need ellipsis or parameter pack
					ok= fn.takesVarArgs() || fn.hasParameterPack();
				} else {
					ok = numArgs >= fn.getRequiredArgumentCount();
				}
				if (ok) {
					if (fn instanceof IIndexBinding) {
						for (ICPPFunction other : result) {
							if (other == null || other instanceof IIndexBinding)
								break;
							if (other.getType().isSameType(ft)) {
								ok= false;
								break;
							}
						}
					}
					if (ok) {
						result[idx++]= fn;
					}
				}
			}
	    }
	    return result;
	}
	
	private static boolean isMatchingFunctionDeclaration(ICPPFunction candidate, LookupData data) {		
		IASTNode node = data.astName.getParent();
		while (node instanceof IASTName)
			node = node.getParent();
		if (node instanceof IASTDeclarator) {
			return isSameFunction(candidate, (IASTDeclarator) node);
		}
		return false;
	}
	
	static IBinding resolveFunction(LookupData data, ICPPFunction[] fns, boolean allowUDC) throws DOMException {
	    fns= (ICPPFunction[]) ArrayUtil.trim(ICPPFunction.class, fns);
	    if (fns == null || fns.length == 0)
	        return null;
	    
	    sortAstBeforeIndex(fns);
	    	    
		if (data.forUsingDeclaration()) 
			return new CPPUsingDeclaration(data.astName, fns);		

		// No arguments to resolve function
		if (!data.hasFunctionArguments()) {
			return createFunctionSet(data.astName, fns);
		}

		if (data.astName instanceof ICPPASTConversionName) {
			return resolveUserDefinedConversion(data, fns);
		}

	    if (data.forFunctionDeclaration()) 
	    	return resolveFunctionDeclaration(data, fns);

		// Reduce our set of candidate functions to only those who have the right number of parameters
	    final IType[] argTypes = data.getFunctionArgumentTypes();
		ICPPFunction[] tmp= selectByArgumentCount(data, fns);
	    tmp= CPPTemplates.instantiateForFunctionCall(data.astName, tmp, 
	    		Arrays.asList(argTypes), 
	    		Arrays.asList(data.getFunctionArgumentValueCategories()), data.argsContainImpliedObject);
	    if (tmp.length == 0 || tmp[0] == null) 
			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, fns);
	    	
		
		int viableCount= 0;
		for (IFunction f : tmp) {
			if (f instanceof ICPPUnknownBinding) {
				setTargetedFunctionsToUnknown(argTypes);
				return f;
			}
			if (f == null) 
				break;
			++viableCount;
		}
		if (viableCount == 0) 
			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, fns);

		// Check for dependent arguments
		fns= tmp;
		if (CPPTemplates.containsDependentType(argTypes)) {
			if (viableCount == 1) 
				return fns[0];
			setTargetedFunctionsToUnknown(argTypes);
			return CPPUnknownFunction.createForSample(fns[0]);
		}
		
		IFunction[] ambiguousFunctions= null;   // ambiguity, 2 functions are equally good
		FunctionCost bestFnCost = null;		    // the cost of the best function

		// Loop over all functions
		List<FunctionCost> potentialCosts= null;
		for (IFunction fn : fns) {
			if (fn == null) 
				continue;
			
			final FunctionCost fnCost= costForFunctionCall(fn, allowUDC, data);
			if (fnCost == null)
				continue;
			
			if (fnCost == CONTAINS_DEPENDENT_TYPES) {
				if (viableCount == 1) 
					return fns[0];
				setTargetedFunctionsToUnknown(argTypes);
				return CPPUnknownFunction.createForSample(fns[0]);
			}
			
			if (fnCost.hasDeferredUDC()) {
				if (potentialCosts == null) {
					potentialCosts= new ArrayList<FunctionCost>();
				}
				potentialCosts.add(fnCost);
				continue;
			}
			int cmp= fnCost.compareTo(data.tu, bestFnCost);
			if (cmp < 0) {
				bestFnCost= fnCost;
				ambiguousFunctions= null;
			} else if (cmp == 0) {
				ambiguousFunctions= (IFunction[]) ArrayUtil.append(IFunction.class, ambiguousFunctions, fn);
			}
		}
		
		if (potentialCosts != null) {
			for (FunctionCost fnCost : potentialCosts) {
				if (!fnCost.mustBeWorse(bestFnCost) && fnCost.performUDC()) {
					int cmp= fnCost.compareTo(data.tu, bestFnCost);
					if (cmp < 0) {
						bestFnCost= fnCost;
						ambiguousFunctions= null;
					} else if (cmp == 0) {
						ambiguousFunctions= (IFunction[]) ArrayUtil.append(IFunction.class, ambiguousFunctions, fnCost.getFunction());
					}
				}
			}
		}

		if (bestFnCost == null)
			return null;
		
		if (ambiguousFunctions != null) {
			ambiguousFunctions= (IFunction[]) ArrayUtil.append(IFunction.class, ambiguousFunctions, bestFnCost.getFunction());
			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
					ambiguousFunctions);
		}
		if (bestFnCost.hasAmbiguousUserDefinedConversion()) {
			return new ProblemBinding(data.astName, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP,
					data.getFoundBindings());
		}
		
		for (int i = 0; i < argTypes.length; i++) {
			IType iType = argTypes[i];
			if (iType instanceof FunctionSetType) {
				((FunctionSetType) iType).applySelectedFunction(bestFnCost.getCost(i).getSelectedFunction());
			}
		}
		IFunction result= bestFnCost.getFunction();
		if (bestFnCost.isDirectInitWithCopyCtor()) {
			Cost c0= bestFnCost.getCost(0);
			IFunction firstConversion= c0.getUserDefinedConversion();
			if (firstConversion instanceof ICPPConstructor) 
				return firstConversion;
		}
		return result;
	}

	private static IBinding createFunctionSet(IASTName name, ICPPFunction[] fns) {
		// First try to find a unique function
		ICPPFunction f= getUniqueFunctionForSet(name, fns);
		return f == null ? new CPPFunctionSet(fns) : f;
	}

	private static ICPPFunction getUniqueFunctionForSet(IASTName name, ICPPFunction[] fns) {
		// First try to find a unique function
		if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			name= (IASTName) name.getParent();
		}
		final boolean haveTemplateArgs= name instanceof ICPPASTTemplateId;
		ICPPFunction result= null;
		boolean haveASTResult= false;
		for (ICPPFunction f : fns) {
			// Use the ast binding
			final boolean fromIndex = f instanceof IIndexBinding;
			if (haveASTResult && fromIndex) 
				break;
			
			if (f instanceof ICPPFunctionTemplate) {
				// Works only if there are template arguments
				if (!haveTemplateArgs || result != null) 
					return null;
				result= f;
				haveASTResult= !fromIndex;
			} else if (!haveTemplateArgs) {
				if (result != null)
					return null;
				result= f;
				haveASTResult= !fromIndex;
			}
		}
		
		if (result instanceof ICPPFunctionTemplate) 
			return CPPTemplates.instantiateForAddressOfFunction((ICPPFunctionTemplate) result, null, name);
			
		return result;
	}

	private static void setTargetedFunctionsToUnknown(IType[] argTypes) {
		for (IType argType : argTypes) {
			if (argType instanceof FunctionSetType) {
				((FunctionSetType) argType).setToUnknown();
			}
		}
	}

	private static IBinding resolveFunctionDeclaration(LookupData data, ICPPFunction[] fns) throws DOMException {
		if (data.forExplicitFunctionSpecialization()) {
			fns = CPPTemplates.instantiateForFunctionCall(data.astName,
					fns,
					Arrays.asList(data.getFunctionArgumentTypes()), Arrays.asList(data.getFunctionArgumentValueCategories()),
					data.argsContainImpliedObject);
		}

		int argCount = data.getFunctionArgumentCount();
	    if (argCount == 1) {
	    	// check for parameter of type void
			final IType[] argTypes = data.getFunctionArgumentTypes();
	    	if (argTypes.length == 1 && SemanticUtil.isVoidType(argTypes[0])) {
	    		argCount= 0;
	    	}
	    }
			
	    for (ICPPFunction fn : fns) {
	    	if (fn != null && !(fn instanceof IProblemBinding) && !(fn instanceof ICPPUnknownBinding)) {
	    		// The index is optimized to provide the function type, avoid using the parameters
	    		// as long as possible.
	    		final IType[] parameterTypes = fn.getType().getParameterTypes();
	    		int parCount = parameterTypes.length;
	    		if (parCount == 1 && SemanticUtil.isVoidType(parameterTypes[0]))
	    			parCount= 0;
			
	    		if (parCount == argCount && isMatchingFunctionDeclaration(fn, data)) {
	    			return fn;
	    		}
	    	}
	    }
	    return null;
	}

	public static void sortAstBeforeIndex(IFunction[] fns) {
		int iast= 0;
	    for (int i = 0; i < fns.length; i++) {
			IFunction fn= fns[i];
			if (!(fn instanceof IIndexBinding)) {
				if (iast != i) {
					fns[i]= fns[iast];
					fns[iast]= fn;
				}
				iast++;
			}
		}
	}

	private static FunctionCost costForFunctionCall(IFunction fn, boolean allowUDC, LookupData data)
			throws DOMException {
		IType[] argTypes = data.getFunctionArgumentTypes();
		ValueCategory[] isLValue= data.getFunctionArgumentValueCategories();
		int skipArg= 0;
	    final ICPPFunctionType ftype= (ICPPFunctionType) fn.getType();
	    if (ftype == null)
	    	return null;

		IType implicitParameterType= null;
		IType impliedObjectType= null;
		final IType[] paramTypes= ftype.getParameterTypes();
		if (fn instanceof ICPPMethod && !(fn instanceof ICPPConstructor)) {
		    implicitParameterType = getImplicitParameterType((ICPPMethod) fn, ftype.isConst(), ftype.isVolatile());
		    if (data.argsContainImpliedObject) {
		    	impliedObjectType= argTypes[0];
		    	skipArg= 1;
			}
		}

		int k= 0;
	    Cost cost;
		final int sourceLen= argTypes.length - skipArg;
		final FunctionCost result;
		if (implicitParameterType == null) {
			result= new FunctionCost(fn, sourceLen);
		} else {
			result= new FunctionCost(fn, sourceLen + 1);
			
			ValueCategory sourceIsLValue= LVALUE;
			if (impliedObjectType == null) {
				impliedObjectType= data.getImpliedObjectType();
			}
			if (fn instanceof ICPPMethod && 
					(((ICPPMethod) fn).isDestructor() || ASTInternal.isStatic(fn, false))) {
			    // 13.3.1-4 for static member functions, the implicit object parameter always matches, no cost
			    cost = new Cost(impliedObjectType, implicitParameterType, Rank.IDENTITY);
			} else if (impliedObjectType == null) {
				return null;
			} else if (impliedObjectType.isSameType(implicitParameterType)) {
				cost = new Cost(impliedObjectType, implicitParameterType, Rank.IDENTITY);
			} else {
				cost = Conversions.checkImplicitConversionSequence(implicitParameterType, impliedObjectType, sourceIsLValue, UDCMode.FORBIDDEN, Context.IMPLICIT_OBJECT);
			    if (!cost.converts()) {
				    if (CPPTemplates.isDependentType(implicitParameterType) || CPPTemplates.isDependentType(impliedObjectType)) {
				    	IType s= getNestedType(impliedObjectType, TDEF|REF|CVTYPE);
				    	IType t= getNestedType(implicitParameterType, TDEF|REF|CVTYPE);
				    	if (SemanticUtil.calculateInheritanceDepth(s, t) >= 0)
				    		return null;
				    	
				    	return CONTAINS_DEPENDENT_TYPES;
				    }
			    }
			}
			if (!cost.converts())
				return null;
			
			result.setCost(k++, cost, sourceIsLValue);
		}

		final UDCMode udc = allowUDC ? UDCMode.DEFER : UDCMode.FORBIDDEN;
		for (int j = 0; j < sourceLen; j++) {
			final IType argType= SemanticUtil.getNestedType(argTypes[j+skipArg], TDEF | REF);
			if (argType == null)
				return null;

			final ValueCategory sourceIsLValue = isLValue[j+skipArg];

			IType paramType;
			if (j < paramTypes.length) {
				paramType= getNestedType(paramTypes[j], TDEF);
			} else if (!fn.takesVarArgs()) {
				paramType= VOID_TYPE;
			} else {
				cost = new Cost(argType, null, Rank.ELLIPSIS_CONVERSION);
				result.setCost(k++, cost, sourceIsLValue);
				continue;
			} 
			
			if (argType instanceof FunctionSetType) {
				cost= ((FunctionSetType) argType).costForTarget(paramType);
			} else if (argType.isSameType(paramType)) {
				cost = new Cost(argType, paramType, Rank.IDENTITY);
			} else {
			    if (CPPTemplates.isDependentType(paramType))
			    	return CONTAINS_DEPENDENT_TYPES;
			    
			    Context ctx= Context.ORDINARY;
			    if (j==0 && sourceLen == 1 && fn instanceof ICPPConstructor) {
			    	if (paramType instanceof ICPPReferenceType) {
			    		if (((ICPPConstructor) fn).getClassOwner().isSameType(getNestedType(paramType, TDEF|REF|CVTYPE))) {
			    			ctx= Context.FIRST_PARAM_OF_DIRECT_COPY_CTOR;
			    			result.setIsDirectInitWithCopyCtor(true);
			    		}
			    	}
			    }
				cost = Conversions.checkImplicitConversionSequence(paramType, argType, sourceIsLValue, udc, ctx);
				if (data.fNoNarrowing && cost.isNarrowingConversion()) {
					cost= Cost.NO_CONVERSION;
				}
			}
			if (!cost.converts())
				return null;
			
			result.setCost(k++, cost, sourceIsLValue);
		}
		return result;
	}

	static IType getImplicitParameterType(ICPPMethod m, final boolean isConst, final boolean isVolatile)
			throws DOMException {
		IType implicitType;
		ICPPClassType owner= m.getClassOwner();
		if (owner instanceof ICPPClassTemplate) {
			owner= CPPTemplates.instantiateWithinClassTemplate((ICPPClassTemplate) owner);
		}
		implicitType= SemanticUtil.addQualifiers(owner, isConst, isVolatile);
		implicitType= new CPPReferenceType(implicitType, false);
		return implicitType;
	}

	private static IBinding resolveUserDefinedConversion(LookupData data, IFunction[] fns) {
		ICPPASTConversionName astName= (ICPPASTConversionName) data.astName;
		IType t= CPPVisitor.createType(astName.getTypeId());
		if (t == null) {
			return new ProblemBinding(astName, IProblemBinding.SEMANTIC_INVALID_TYPE, data.getFoundBindings());
		}
		if (!data.forFunctionDeclaration() || data.forExplicitFunctionSpecialization()) {
			CPPTemplates.instantiateConversionTemplates(fns, t);
		}

		IFunction unknown= null;
		for (IFunction function : fns) {
			if (function != null) {
				try {
					IType t2= function.getType().getReturnType();
					if (t.isSameType(t2))
						return function;
					if (unknown == null && function instanceof ICPPUnknownBinding) {
						unknown= function;
					}
				} catch (DOMException e) {
					// ignore, try other candidates
				}
			}
		}
		if (unknown != null)
			return unknown;
		return new ProblemBinding(astName, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, data.getFoundBindings());
	}

	/**
	 * 13.4-1 A use of an overloaded function without arguments is resolved in certain contexts to a function
     */
    static IBinding resolveTargetedFunction(IASTName name, ICPPFunction[] fns) {
    	boolean addressOf= false;
    	IASTNode node= name.getParent();
    	while (node instanceof IASTName) 
    		node= node.getParent();
    	
    	if (!(node instanceof IASTIdExpression)) 
    		return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD);
    	
    	ASTNodeProperty prop= node.getPropertyInParent();
    	IASTNode parent = node.getParent();
    	while (parent instanceof IASTUnaryExpression) {
    		final int op= ((IASTUnaryExpression) parent).getOperator();
    		if (op == IASTUnaryExpression.op_bracketedPrimary) {
    		} else if (!addressOf && op == IASTUnaryExpression.op_amper) {
    			addressOf= true;
    		} else {
    			break;
    		}
    		node= parent;
    		prop= node.getPropertyInParent();
    		parent= node.getParent();
    	}
    	    	
    	IType targetType= null;
    	if (prop == IASTDeclarator.INITIALIZER) {
    		// Target is an object or reference being initialized
    		IASTDeclarator dtor = (IASTDeclarator) parent;
    		targetType= CPPVisitor.createType(dtor);
    	} else if (prop == IASTEqualsInitializer.INITIALIZER) {
    		IASTEqualsInitializer initExp = (IASTEqualsInitializer) parent;
    		if (initExp.getParent() instanceof IASTDeclarator) {
    			IASTDeclarator dtor = (IASTDeclarator) initExp.getParent();
    			targetType= CPPVisitor.createType(dtor);
    		}
    	} else if (prop == ICPPASTConstructorInitializer.ARGUMENT) {
    		ICPPASTConstructorInitializer init = (ICPPASTConstructorInitializer) parent;
    		if (init.getArguments().length == 1) {
    			final IASTNode parentOfInit = init.getParent();
    			if (parentOfInit instanceof IASTDeclarator) {
    				IASTDeclarator dtor = (IASTDeclarator) parentOfInit;
    				targetType= CPPVisitor.createType(dtor);
    			} else if (parentOfInit instanceof ICPPASTConstructorChainInitializer) {
    				ICPPASTConstructorChainInitializer memInit= (ICPPASTConstructorChainInitializer) parentOfInit;
    				IBinding var= memInit.getMemberInitializerId().resolveBinding();
    				if (var instanceof IVariable) {
    					try {
							targetType= ((IVariable) var).getType();
						} catch (DOMException e) {
						}
    				}
    			}
    		}
    	} else if (prop == IASTBinaryExpression.OPERAND_TWO) {
            IASTBinaryExpression binaryExp = (IASTBinaryExpression) parent;
            if (binaryExp.getOperator() == IASTBinaryExpression.op_assign) {
                targetType= binaryExp.getOperand1().getExpressionType();
            } 
    	} else if (prop == IASTFunctionCallExpression.ARGUMENT) {
    		// Target is a parameter of a function, need to resolve the function call
    		IASTFunctionCallExpression fnCall = (IASTFunctionCallExpression) parent;
    		IType t= SemanticUtil.getNestedType(fnCall.getFunctionNameExpression().getExpressionType(), TDEF|REF|CVTYPE);
			if (t instanceof IPointerType) {
				t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
			}
			if (t instanceof IFunctionType) {
				int i= 0;
				for (IASTNode arg : fnCall.getArguments()) {
					if (arg == node) {
						IType[] params= ((IFunctionType) t).getParameterTypes();
						if (params.length > i) {
							targetType= params[i];
						}
						break;
					}
					i++;
				}
			}
    	} else if (prop == IASTCastExpression.OPERAND) {
    		// target is an explicit type conversion
    		IASTCastExpression cast = (IASTCastExpression) parent;
    		targetType= CPPVisitor.createType(cast.getTypeId().getAbstractDeclarator());
		} else if (prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
			// target is a template non-type parameter (14.3.2-5)
			ICPPASTTemplateId id = (ICPPASTTemplateId) parent;
			IASTNode[] args = id.getTemplateArguments();
			int i = 0;
			for (; i < args.length; i++) {
				if (args[i] == node) {
					break;
				}
			}
			IBinding template = id.getTemplateName().resolveBinding();
			if (template instanceof ICPPTemplateDefinition) {
				try {
					ICPPTemplateParameter[] ps = ((ICPPTemplateDefinition) template).getTemplateParameters();
					if (i < args.length && i < ps.length && ps[i] instanceof ICPPTemplateNonTypeParameter) {
						targetType= ((ICPPTemplateNonTypeParameter) ps[i]).getType();
					}
				} catch (DOMException e) {
				}
			}
		} else if (prop == IASTReturnStatement.RETURNVALUE) {
    		// target is the return value of a function, operator or conversion
    		while (parent != null && !(parent instanceof IASTFunctionDefinition)) {
    			parent = parent.getParent();
    		}
    		if (parent instanceof IASTFunctionDefinition) {
    			IASTDeclarator dtor = ((IASTFunctionDefinition) parent).getDeclarator();
    			dtor= ASTQueries.findInnermostDeclarator(dtor);
    			IBinding binding = dtor.getName().resolveBinding();
    			if (binding instanceof IFunction) {
    				try {
    					IFunctionType ft = ((IFunction) binding).getType();
    					targetType= ft.getReturnType();
    				} catch (DOMException e) {
    				}
    			}
    		}
		}
    	if (targetType == null && parent instanceof IASTExpression 
    			&& parent instanceof IASTImplicitNameOwner) {
			// Trigger resolution of overloaded operator, which may resolve the
			// function set.
			((IASTImplicitNameOwner) parent).getImplicitNames(); 
			final IBinding newBinding = name.getPreBinding();
			if (!(newBinding instanceof CPPFunctionSet)) 
				return newBinding;
		} 

    	ICPPFunction function = resolveTargetedFunction(targetType, name, fns);
    	if (function == null)
    		return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD);
 
    	return function;
    }

	static ICPPFunction resolveTargetedFunction(IType targetType, IASTName name, ICPPFunction[] fns) {
		targetType= getNestedType(targetType, TDEF | REF | CVTYPE | PTR | MPTR);
    	if (!(targetType instanceof ICPPFunctionType))
    		return null;

    	// First pass, consider functions
    	for (ICPPFunction fn : fns) {
    		try {
    			if (!(fn instanceof ICPPFunctionTemplate)) {
    				if (targetType.isSameType(fn.getType()))
    					return fn;
    			}
    		} catch (DOMException e) {
    		}
    	}
    	
    	// Second pass, consider templates
    	ICPPFunction result= null;
    	ICPPFunctionTemplate resultTemplate= null;
    	boolean isAmbiguous= false;
    	final IASTTranslationUnit tu= name.getTranslationUnit();
    	for (IFunction fn : fns) {
    		try {
    			if (fn instanceof ICPPFunctionTemplate) {
    				final ICPPFunctionTemplate template = (ICPPFunctionTemplate) fn;
					ICPPFunction inst= CPPTemplates.instantiateForAddressOfFunction(template, (ICPPFunctionType) targetType, name);
    				if (inst != null) {
    					int cmp= -1;
    					if (result != null) {
    						cmp= CPPTemplates.orderFunctionTemplates(resultTemplate, template);
    						if (cmp == 0) 
    							cmp= compareByRelevance(tu, resultTemplate, template);
    					}
    					if (cmp == 0)
    						isAmbiguous= true;
    					
    					if (cmp < 0) {
    						isAmbiguous= false;
    						resultTemplate= template;
    						result= inst;
    					}
    				}
    			}
    		} catch (DOMException e) {
    		}
    	}
    	if (isAmbiguous)
    		return null;
    	
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
    public static IType getFieldOwnerType(ICPPASTFieldReference fieldReference) throws DOMException {
    	return getFieldOwnerType(fieldReference, null);
    }
    
    /*
     * Also collects the function bindings if requested.
     */
    public static IType getFieldOwnerType(ICPPASTFieldReference fieldReference, Collection<ICPPFunction> functionBindings) throws DOMException {
    	final IASTExpression owner = fieldReference.getFieldOwner();
    	if (owner == null)
    		return null;
    	
    	IType type= SemanticUtil.getNestedType(owner.getExpressionType(), TDEF|REF);
    	if (!fieldReference.isPointerDereference())
    		return type;
    	
    	// bug 205964: as long as the type is a class type, recurse. 
    	// Be defensive and allow a max of 10 levels.
    	boolean foundOperator= false;
    	for (int j = 0; j < 10; j++) {
    		IType uTemp= getUltimateTypeUptoPointers(type);
    		if (uTemp instanceof IPointerType)
    			return type;

    		// for unknown types we cannot determine the overloaded -> operator
    		if (uTemp instanceof ICPPUnknownType)
    			return CPPUnknownClass.createUnnamedInstance();

    		if (!(uTemp instanceof ICPPClassType)) 
    			break;
    		
    		/*
    		 * 13.5.6-1: An expression x->m is interpreted as (x.operator->())->m for a
    		 * class object x of type T
    		 * 
    		 * Construct an AST fragment for x.operator-> which the lookup routines can
    		 * examine for type information.
    		 */

    		IASTExpression arg = createArgForType(fieldReference, type);
    		ICPPFunction op = findOverloadedOperator(fieldReference, new IASTExpression[] {arg}, uTemp, OverloadableOperator.ARROW, LookupMode.NO_GLOBALS);
    		if (op == null) 
    			break;

    		if (functionBindings != null)
    			functionBindings.add(op);
    		
    		type= SemanticUtil.getNestedType(op.getType().getReturnType(), TDEF|REF);
			type= SemanticUtil.mapToAST(type, owner);
    		foundOperator= true;
    	}
    	
    	return foundOperator ? type : null;
    }
    
    private static ICPPVariable createVariable(IASTName name, final IType type, final boolean isConst, final boolean isVolatile) {
    	return new CPPVariable(name) {
			@Override public IType getType() {
				return SemanticUtil.addQualifiers(type, isConst, isVolatile);
			}
		};
    }

    public static ICPPFunction findOverloadedOperator(IASTArraySubscriptExpression exp) {
    	final IASTExpression arrayExpression = exp.getArrayExpression();
		IASTInitializerClause[] args = {arrayExpression, exp.getArgument()};
    	IType type = typeOrFunctionSet(arrayExpression);
    	type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
    	return findOverloadedOperator(exp, args, type, OverloadableOperator.BRACKET, LookupMode.NO_GLOBALS);
    }

    public static ICPPFunction findOverloadedOperator(IASTFunctionCallExpression exp, ICPPClassType type) {
    	IASTInitializerClause[] args = exp.getArguments();
    	ArrayList<IASTInitializerClause> argsToPass = new ArrayList<IASTInitializerClause>(args.length + 1);
    	argsToPass.add(exp.getFunctionNameExpression());
    	for (IASTInitializerClause e : args) {
    		argsToPass.add(e);
    	}
    	args = argsToPass.toArray(new IASTInitializerClause[argsToPass.size()]);
    	
    	return findOverloadedOperator(exp, args, type, OverloadableOperator.PAREN, LookupMode.NO_GLOBALS);
    }
    
    public static ICPPFunction findOverloadedOperator(ICPPASTNewExpression exp) {
		OverloadableOperator op = OverloadableOperator.fromNewExpression(exp);
		
		IType type = exp.getExpressionType();
		if (!(type instanceof IPointerType))
			return null;
		type = ((IPointerType) type).getType();
		
		IASTTypeId typeId = exp.getTypeId().copy();
    	IASTExpression sizeExpression = new CPPASTTypeIdExpression(IASTTypeIdExpression.op_sizeof, typeId);
    	sizeExpression.setParent(exp);
    	
    	IASTInitializerClause[] placement = exp.getPlacementArguments();
    	List<IASTInitializerClause> args = new ArrayList<IASTInitializerClause>();
    	args.add(sizeExpression);
    	if (placement != null) {
    		for (IASTInitializerClause p : placement) {
    			args.add(p);
    		} 
    	} 
    	IASTInitializerClause[] argArray = args.toArray(new IASTInitializerClause[args.size()]);
		return findOverloadedOperator(exp, argArray, type, op, LookupMode.ALL_GLOBALS);
    }

    public static ICPPFunction findOverloadedOperator(ICPPASTDeleteExpression exp) {
    	OverloadableOperator op = OverloadableOperator.fromDeleteExpression(exp);
    	IType classType = getNestedClassType(exp);
    	IASTExpression[] args = new IASTExpression[] {createArgForType(exp, classType)};
		return findOverloadedOperator(exp, args, classType, op, LookupMode.ALL_GLOBALS);
    }
    
    private static ICPPClassType getNestedClassType(ICPPASTDeleteExpression exp) {
    	IType type = exp.getOperand().getExpressionType();
    	type = SemanticUtil.getUltimateTypeUptoPointers(type);
    	if (type instanceof IPointerType) {
    		IType classType = ((IPointerType) type).getType();
			if (classType instanceof ICPPClassType)
				return (ICPPClassType) classType;
    	}
		return null;
    }

    /**
     * Returns constructor called by a declarator, or <code>null</code> if no constructor is called.
     */
    public static ICPPConstructor findImplicitlyCalledConstructor(final ICPPASTDeclarator declarator) {
    	if (declarator.getNestedDeclarator() != null)
    		return null;
    	IASTDeclarator dtor= ASTQueries.findOutermostDeclarator(declarator);
    	IASTNode parent = dtor.getParent();
    	if (parent instanceof IASTSimpleDeclaration) {
    		final IASTInitializer initializer = dtor.getInitializer();
			if (initializer == null) {
    			IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
    			parent = parent.getParent();
    			if (parent instanceof IASTCompositeTypeSpecifier ||
    					declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern) {
    				// No initialization is performed for class members and extern declarations
    				// without an initializer.
    				return null;
    			}
			}
	    	return findImplicitlyCalledConstructor(declarator.getName(), initializer);
		}
    	return null;
    }

    /**
     * Returns constructor called by a class member initializer in a constructor initializer chain.
     * Returns <code>null</code> if no constructor is called.
     */
    public static ICPPConstructor findImplicitlyCalledConstructor(ICPPASTConstructorChainInitializer initializer) {
    	return findImplicitlyCalledConstructor(initializer.getMemberInitializerId(), initializer.getInitializer());
    }

    /**
     * Returns constructor called by a variable declarator or an initializer in a constructor initializer
     * chain. Returns <code>null</code> if no constructor is called.
     */
    private static ICPPConstructor findImplicitlyCalledConstructor(IASTName name, IASTInitializer initializer) {
    	IBinding binding = name.resolveBinding();
    	if (!(binding instanceof ICPPVariable))
    		return null;
    	IType type;
		try {
			type = SemanticUtil.getNestedType(((ICPPVariable) binding).getType(), TDEF | CVTYPE);
	    	if (!(type instanceof ICPPClassType))
	    		return null;
	    	if (type instanceof ICPPClassTemplate || type instanceof ICPPUnknownClassType || type instanceof IProblemBinding)
	    		return null;
	    	
	    	final ICPPClassType classType = (ICPPClassType) type;
	    	if (initializer instanceof IASTEqualsInitializer) {
		    	// Copy initialization
	    		IASTEqualsInitializer eqInit= (IASTEqualsInitializer) initializer;
	    		IASTInitializerClause initClause = eqInit.getInitializerClause();
	    		IType sourceType= null;
	    		ValueCategory isLValue= PRVALUE;
	    		if (initClause instanceof IASTExpression) {
	    			final IASTExpression expr = (IASTExpression) initClause;
					isLValue= valueCat(expr);
	    			sourceType= SemanticUtil.getSimplifiedType(typeOrFunctionSet(expr));
	    		} else if (initClause instanceof ICPPASTInitializerList) {
	    			sourceType= new InitializerListType((ICPPASTInitializerList) initClause);
	    		}
	    		if (sourceType != null) {
	    			Cost c= Conversions.checkImplicitConversionSequence(type, sourceType, isLValue, UDCMode.ALLOWED, Context.ORDINARY);
	    			if (c.converts()) {
						ICPPFunction f = c.getUserDefinedConversion();
						if (f instanceof ICPPConstructor)
							return (ICPPConstructor) f;
						// If a conversion is used, the constructor is elided.
	    			}
	    		}
	    	} else if (initializer instanceof ICPPASTInitializerList) {
	    		// List initialization
	    		final InitializerListType listType = new InitializerListType((ICPPASTInitializerList) initializer);
				Cost c= Conversions.listInitializationSequence(listType, type, UDCMode.ALLOWED, true);
    			if (c.converts()) {
    				ICPPFunction f = c.getUserDefinedConversion();
    				if (f instanceof ICPPConstructor)
    					return (ICPPConstructor) f;
    			}
	    	} else if (initializer instanceof ICPPASTConstructorInitializer) {
		    	// Direct Initialization
			    final IASTInitializerClause[] arguments = ((ICPPASTConstructorInitializer) initializer).getArguments();
				CPPASTName astName = new CPPASTName();
			    astName.setName(classType.getNameCharArray());
			    astName.setOffsetAndLength((ASTNode) name);
				CPPASTIdExpression idExp = new CPPASTIdExpression(astName);
				idExp.setParent(name.getParent());
				idExp.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

			    LookupData data = new LookupData(astName);
				data.setFunctionArguments(false, arguments);
			    data.forceQualified = true;
			    data.foundItems = classType.getConstructors();
			    binding = resolveAmbiguities(data, astName);
			    if (binding instanceof ICPPConstructor)
			    	return (ICPPConstructor) binding;
	    	} else if (initializer == null) {
	    		// Default initialization
	    		ICPPConstructor[] ctors = classType.getConstructors();
				for (ICPPConstructor ctor : ctors) {
					if (ctor.getRequiredArgumentCount() == 0)
						return ctor;
				}
				return null;
	    	}
		} catch (DOMException e) {
		}
		return null;
    }

    public static ICPPFunction findImplicitlyCalledDestructor(ICPPASTDeleteExpression expr) {
    	ICPPClassType cls = getNestedClassType(expr);
    	if (cls == null)
    		return null;

    	IScope scope = cls.getCompositeScope();
		if (scope == null)
			return null;

		CPPASTName astName = new CPPASTName();
		astName.setParent(expr);
	    astName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
	    astName.setName(CharArrayUtils.concat("~".toCharArray(), cls.getNameCharArray())); //$NON-NLS-1$

		IASTExpression arg = createArgForType(expr, cls);

	    LookupData data = new LookupData(astName);
	    data.forceQualified = true;
	    data.setFunctionArguments(true, arg);
	    try {
		    lookup(data, scope);
		    IBinding binding = resolveAmbiguities(data, astName);
		    if (binding instanceof ICPPFunction)
		    	return (ICPPFunction) binding;
		} catch (DOMException e) {
		}
		return null;
    }

	public static IASTExpression createArgForType(IASTNode node, IType type) {
		CPPASTName x= new CPPASTName();
		x.setBinding(createVariable(x, type, false, false));    		
		final CPPASTIdExpression idExpression = new CPPASTIdExpression(x);
		idExpression.setParent(node);
		return idExpression;
	}

    public static ICPPFunction findOverloadedOperator(IASTUnaryExpression exp) {
    	final IASTExpression operand = exp.getOperand();
		if (operand == null)
    		return null;

    	OverloadableOperator op = OverloadableOperator.fromUnaryExpression(exp);
		if (op == null)
			return null;

    	IType type = typeOrFunctionSet(operand);
		type = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
		if (!isUserDefined(type)) 
			return null;

		IASTExpression[] args;
		int operator = exp.getOperator();
	    if (operator == IASTUnaryExpression.op_postFixDecr || operator == IASTUnaryExpression.op_postFixIncr) { 
	    	args = new IASTExpression[] { operand, CPPASTLiteralExpression.INT_ZERO };
	    } else {
	    	args = new IASTExpression[] { operand };
	    }	
		return findOverloadedOperator(exp, args, type, op, LookupMode.LIMITED_GLOBALS);
    }

    public static ICPPFunction findOverloadedOperator(IASTBinaryExpression exp) {
    	OverloadableOperator op = OverloadableOperator.fromBinaryExpression(exp);
		if (op == null)
			return null;
		
		final IASTExpression op1 = exp.getOperand1();
		final IASTExpression op2 = exp.getOperand2();
		if(op2==null){
			return null;
		}
		IType op1type = getNestedType(typeOrFunctionSet(op1), TDEF | REF | CVTYPE);
		IType op2type = getNestedType(typeOrFunctionSet(op2), TDEF | REF | CVTYPE);
		if (!isUserDefined(op1type) && !isUserDefined(op2type))
			return null;
		
		final IASTExpression[] args = new IASTExpression[] { op1, op2 };
		final LookupMode lookupNonMember;
		if (exp.getOperator() == IASTBinaryExpression.op_assign) {
			lookupNonMember = LookupMode.NO_GLOBALS;
		} else {
			lookupNonMember= LookupMode.LIMITED_GLOBALS;
		}
		return findOverloadedOperator(exp, args, op1type, op, lookupNonMember);
    }
    
    /**
     * For simplicity returns an operator of form RT (T, T) rather than RT (boolean, T, T) 
     */
    public static ICPPFunction findOverloadedConditionalOperator(IASTExpression positive, IASTExpression negative) {
		final IASTExpression parent = (IASTExpression) positive.getParent();
		final IASTExpression[] args = new IASTExpression[] {positive, negative};
		return findOverloadedOperator(parent, args, null, 
				OverloadableOperator.CONDITIONAL_OPERATOR, LookupMode.NO_GLOBALS);
    }

    /**
     * Returns the operator,() function that would apply to the two given arguments. 
     * The lookup type of the class where the operator,() might be found must also be provided.
     */
    public static ICPPFunction findOverloadedOperatorComma(IASTExpression first, final IType lookupType, final ValueCategory valueCat, IASTExpression second) {
		IType op1type = getNestedType(lookupType, TDEF | REF | CVTYPE);
		IType op2type = getNestedType(typeOrFunctionSet(second), TDEF | REF | CVTYPE);
		if (!isUserDefined(op1type) && !isUserDefined(op2type))
			return null;

    	IASTUnaryExpression dummy = new CPPASTUnaryExpression() {
			@Override public IType getExpressionType() { return lookupType; }
			@Override public ValueCategory getValueCategory() { return valueCat; }
    	};
    	dummy.setParent(first);
    	
    	IASTExpression[] args = new IASTExpression[] { dummy , second };
    	return findOverloadedOperator(dummy, args, lookupType, OverloadableOperator.COMMA, LookupMode.LIMITED_GLOBALS);
    }

    private static enum LookupMode {NO_GLOBALS, LIMITED_GLOBALS, ALL_GLOBALS}
    private static ICPPFunction findOverloadedOperator(IASTExpression parent, IASTInitializerClause[] args, IType methodLookupType, 
    		OverloadableOperator operator, LookupMode mode) {
    	ICPPClassType callToObjectOfClassType= null;
		IType type2= null;
		if (args.length >= 2 && args[1] instanceof IASTExpression) {
			type2 = typeOrFunctionSet((IASTExpression) args[1]);
			type2= getNestedType(type2, TDEF | REF | CVTYPE);
		}
		
		if (methodLookupType instanceof ICPPUnknownType || type2 instanceof ICPPUnknownType) {
			if (methodLookupType instanceof FunctionSetType) 
				((FunctionSetType) methodLookupType).setToUnknown();
			if (type2 instanceof FunctionSetType) 
				((FunctionSetType) type2).setToUnknown();
			
			return new CPPUnknownFunction(null, operator.toCharArray());
		}

    	// Find a method
    	LookupData methodData = null;
    	CPPASTName methodName = null;
    	if (methodLookupType instanceof IProblemBinding)
    		return null;
    	if (methodLookupType instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) methodLookupType;

			methodName = new CPPASTName(operator.toCharArray());
        	methodName.setParent(parent);
        	methodName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
    	    methodData = new LookupData(methodName);
    	    methodData.setFunctionArguments(true, args);
    	    methodData.forceQualified = true; // (13.3.1.2.3)
    	    
			try {
				IScope scope = classType.getCompositeScope();
				if (scope == null)
					return null;
				lookup(methodData, scope);
			
				if (parent instanceof IASTFunctionCallExpression) {
					callToObjectOfClassType= classType;
				}
			} catch (DOMException e) {
				return null;
			}
    	}
    	
		// Find a function
    	CPPASTName funcName = new CPPASTName(operator.toCharArray());
	    funcName.setParent(parent);
	    funcName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
    	LookupData funcData = new LookupData(funcName);
    	funcData.setFunctionArguments(true, args);
    	funcData.ignoreMembers = true; // (13.3.1.2.3)
    	if (mode != LookupMode.NO_GLOBALS || callToObjectOfClassType != null) {
			try {
				if (mode != LookupMode.NO_GLOBALS) {
					IScope scope = CPPVisitor.getContainingScope(parent);
					if (scope == null)
						return null;
					lookup(funcData, scope);
					try {
						doKoenigLookup(funcData);
					} catch (DOMException e) {
					}
					// Filter with file-set
					IASTTranslationUnit tu= parent.getTranslationUnit();
					if (tu != null && funcData.foundItems instanceof Object[]) {
						final IIndexFileSet fileSet = tu.getIndexFileSet();
						if (fileSet != null) {
							int j=0;
							final Object[] items= (Object[]) funcData.foundItems;
							for (int i = 0; i < items.length; i++) {
								Object item = items[i];
								items[i]= null;
								if (item instanceof IIndexBinding) {
									if (!fileSet.containsDeclaration((IIndexBinding) item)) {
										continue;
									}
								}
								items[j++]= item;
							}
						}
					}
				}
			} catch (DOMException e) {
				return null;
			}

			// 13.3.1.2.3
			// However, if no operand type has class type, only those non-member functions ...
			if (mode == LookupMode.LIMITED_GLOBALS) {
				if (funcData.foundItems != null && !(methodLookupType instanceof ICPPClassType) && !(type2 instanceof ICPPClassType)) {
					IEnumeration enum1= null;
					IEnumeration enum2= null;
					if (methodLookupType instanceof IEnumeration) {
						enum1= (IEnumeration) methodLookupType;
					}
					if (type2 instanceof IEnumeration) {
						enum2= (IEnumeration) type2;
					}
					Object[] items= (Object[]) funcData.foundItems;
					int j= 0;
					for (Object object : items) {
						if (object instanceof ICPPFunction) {
							ICPPFunction func= (ICPPFunction) object;
							try {
								ICPPFunctionType ft = func.getType();
								IType[] pts= ft.getParameterTypes();
								if ((enum1 != null && pts.length > 0 && enum1.isSameType(getUltimateTypeUptoPointers(pts[0]))) ||
										(enum2 != null && pts.length > 1 && enum2.isSameType(getUltimateTypeUptoPointers(pts[1])))) {
									items[j++]= object;
								} 
							} catch (DOMException e) {
							}
						}
					}
					while (j < items.length) {
						items[j++]= null;
					}
				} 
			}
			
			if (callToObjectOfClassType != null) {	
				try {
					// 13.3.1.1.2 call to object of class type
					ICPPMethod[] ops = SemanticUtil.getConversionOperators(callToObjectOfClassType);
					for (ICPPMethod op : ops) {
						if (op.isExplicit())
							continue;
						IFunctionType ft= op.getType();
						if (ft != null) {
							IType rt= SemanticUtil.getNestedType(ft.getReturnType(), SemanticUtil.TDEF);
							if (rt instanceof IPointerType) {
								IType ptt= SemanticUtil.getNestedType(((IPointerType) rt).getType(), SemanticUtil.TDEF);
								if (ptt instanceof IFunctionType) {
									IFunctionType ft2= (IFunctionType) ptt;
									IBinding sf= createSurrogateCallFunction(parent.getTranslationUnit().getScope(), ft2.getReturnType(), rt, ft2.getParameterTypes());
									mergeResults(funcData, sf, false);
								}
							}
						}
					}
				} catch (DOMException e) {
					return null;
				}
			}
    	}
		
    	if (methodLookupType instanceof ICPPClassType || type2 instanceof ICPPClassType) {
    		ICPPFunction[] builtins= BuiltinOperators.create(operator, args, parent.getTranslationUnit(), (Object[]) funcData.foundItems);
    		mergeResults(funcData, builtins, false);
    	}
    	
    	try {
    		IBinding binding = null;
    		if (methodData != null && funcData.hasResults()) {
    			// if there was two lookups then merge the results
    			mergeResults(funcData, methodData.foundItems, false);
    			binding = resolveAmbiguities(funcData, funcName);
    		} else if (funcData.hasResults()) {
    			binding = resolveAmbiguities(funcData, funcName);
    		} else if (methodData != null) {
    			binding = resolveAmbiguities(methodData, methodName);
    		}
		    
		    if (binding instanceof ICPPFunction)
		    	return (ICPPFunction) binding;
		} catch (DOMException e) {
		}
		
		return null;
    }
    
	private static IBinding createSurrogateCallFunction(IScope scope, IType returnType, IType rt, IType[] parameterTypes) {
		IType[] parms = new IType[parameterTypes.length + 1];
		ICPPParameter[] theParms = new ICPPParameter[parms.length];

		parms[0] = rt;
		theParms[0]= new CPPBuiltinParameter(rt);
		for (int i = 1; i < parms.length; i++) {
			IType t = parameterTypes[i - 1];
			parms[i]= t;
			theParms[i]= new CPPBuiltinParameter(t);
		}
		ICPPFunctionType functionType = new CPPFunctionType(returnType, parms);
		return new CPPImplicitFunction(CALL_FUNCTION, scope, functionType, theParms, false);
	}

	private static boolean isUserDefined(IType type) {
		if (type instanceof IProblemBinding)
			return false;
		
    	return type instanceof ICPPClassType || type instanceof IEnumeration || type instanceof ICPPUnknownType;
    }
    
    public static IBinding[] findBindings(IScope scope, String name, boolean qualified) {
		return findBindings(scope, name.toCharArray(), qualified, null);
	}

	public static IBinding[] findBindings(IScope scope, char[] name, boolean qualified) {
		return findBindings(scope, name, qualified, null);
	}

	public static IBinding[] findBindings(IScope scope, char[] name, boolean qualified, IASTNode beforeNode) {
	    CPPASTName astName = new CPPASTName();
	    astName.setName(name);
	    astName.setParent(ASTInternal.getPhysicalNodeOfScope(scope));
	    astName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
	    if (beforeNode instanceof ASTNode) {
	    	astName.setOffsetAndLength((ASTNode) beforeNode);
	    }
	    
		LookupData data = new LookupData(astName);
		data.forceQualified = qualified;
		return standardLookup(data, scope);
	}
	
	public static IBinding[] findBindingsForContentAssist(IASTName name, boolean prefixLookup,
			String[] additionalNamespaces) {
		LookupData data = createLookupData(name);
		data.contentAssist = true;
		data.prefixLookup = prefixLookup;
		data.foundItems = new CharArrayObjectMap(2);

		// Convert namespaces to scopes.
		List<ICPPScope> nsScopes= new ArrayList<ICPPScope>();
		IASTTranslationUnit tu = name.getTranslationUnit();
		if (additionalNamespaces != null && tu != null) {
			for (String nsName : additionalNamespaces) {
				nsName= nsName.trim();
				if (nsName.startsWith("::")) { //$NON-NLS-1$
					nsName= nsName.substring(2);
				}
				String[] namespaceParts = nsName.split("::"); //$NON-NLS-1$
				try {
					ICPPScope nsScope = getNamespaceScope(tu, namespaceParts);
					if (nsScope != null) {
						nsScopes.add(nsScope);
					}
				} catch (DOMException e) {
					// Errors in source code, continue with next candidate.
				}
			}
		}
		return contentAssistLookup(data, nsScopes);
	}

	private static ICPPScope getNamespaceScope(IASTTranslationUnit tu, String[] namespaceParts)
			throws DOMException {
		ICPPScope nsScope= (ICPPScope) tu.getScope();
		outer: for (String nsPart : namespaceParts) {
			nsPart= nsPart.trim();
			if (nsPart.length() != 0) {
				CPPASTName searchName= new CPPASTName(nsPart.toCharArray());
				searchName.setParent(tu);
				searchName.setPropertyInParent(STRING_LOOKUP_PROPERTY);
				IBinding[] nsBindings = nsScope.getBindings(searchName, true, false);
				for (IBinding nsBinding : nsBindings) {
					if (nsBinding instanceof ICPPNamespace) {
						nsScope= ((ICPPNamespace) nsBinding).getNamespaceScope();
						continue outer;
					}
				}
				// There was no matching namespace
				return null;
			}
		}
		
		// Name did not specify a namespace, e.g. "::"
		if (nsScope == tu.getScope())
			return null;
		
		return nsScope;
	}

	private static IBinding[] contentAssistLookup(LookupData data, List<ICPPScope> additionalNamespaces) {        
        try {
            lookup(data, null);
            
            if (additionalNamespaces != null) {
        		data.ignoreUsingDirectives = true;
        		data.forceQualified = true;
            	for (ICPPScope nsScope : additionalNamespaces) {
        			if (!data.visited.containsKey(nsScope)) {
        				lookup(data, nsScope);
        			}
				}
    		}
        } catch (DOMException e) {
        }
        CharArrayObjectMap map = (CharArrayObjectMap) data.foundItems;
        IBinding[] result = IBinding.EMPTY_BINDING_ARRAY;
        if (!map.isEmpty()) {
            char[] key = null;
            int size = map.size(); 
            for (int i = 0; i < size; i++) {
                key = map.keyAt(i);
                result = addContentAssistBinding(result, map.get(key));
            }
        }
        return ArrayUtil.trim(result);
    }

	public static IBinding[] addContentAssistBinding(IBinding[] result, Object obj) {
		if (obj instanceof Object[]) {
			for (Object	o : (Object[]) obj) {
				result= addContentAssistBinding(result, o);
			}
			return result;
		}
		
        if (obj instanceof IASTName) {
            return addContentAssistBinding(result, ((IASTName) obj).resolveBinding());
        }
        
        if (obj instanceof IBinding && !(obj instanceof IProblemBinding)) {
        	final IBinding binding = (IBinding) obj;
        	if (binding instanceof ICPPFunction) {
        		final ICPPFunction function = (ICPPFunction) binding;
				if (function.isDeleted()) {
        			return result;
        		}
        	}
			return ArrayUtil.append(result, binding);
        }
        
        return result;
	}

    private static IBinding[] standardLookup(LookupData data, IScope start) {
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
    
	public static boolean isSameFunction(ICPPFunction function, IASTDeclarator declarator) {
		final ICPPASTDeclarator innerDtor = (ICPPASTDeclarator) ASTQueries.findInnermostDeclarator(declarator);
		IASTName name = innerDtor.getName();
		ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);
		if (templateDecl != null) {
			if (templateDecl instanceof ICPPASTTemplateSpecialization) {
				if (!(function instanceof ICPPSpecialization))
					return false;
			} else {
				if (!(function instanceof ICPPTemplateDefinition))
					return false;
			}
		} else if (function instanceof ICPPTemplateDefinition) {
			return false;
		}

		declarator= ASTQueries.findTypeRelevantDeclarator(declarator);
		try {
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				IType type = function.getType();
				return type.isSameType(CPPVisitor.createType(declarator));
			} 
		} catch (DOMException e) {
		}
		return false;
	}
	
	static protected IBinding resolveUnknownName(IScope scope, ICPPUnknownBinding unknown) {
		final IASTName unknownName = unknown.getUnknownName();
		LookupData data = new LookupData(unknownName);
		data.checkPointOfDecl= false;
		data.typesOnly= unknown instanceof IType;
		
		try {
            // 2: lookup
            lookup(data, scope);
        } catch (DOMException e) {
            data.problem = (ProblemBinding) e.getProblem();
        }
		
		if (data.problem != null)
		    return data.problem;
		
		// 3: resolve ambiguities
		IBinding binding;
        try {
            binding = resolveAmbiguities(data, unknownName);
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        // 4: post processing
		binding = postResolution(binding, data);
		return binding;
	}
}
