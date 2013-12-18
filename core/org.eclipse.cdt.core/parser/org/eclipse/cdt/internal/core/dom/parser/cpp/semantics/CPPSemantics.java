/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Mike Kucera (IBM)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ARRAY;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.MPTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.PTR;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.calculateInheritanceDepth;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.isConversionOperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
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
import org.eclipse.cdt.core.dom.ast.IScope.ScopeLookupData;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
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
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.core.parser.util.IUnaryPredicate;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.IRecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalNamespaceScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates.TypeSelection;
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

	public static final String EMPTY_NAME = ""; //$NON-NLS-1$
	public static final char[] OPERATOR_ = new char[] {'o','p','e','r','a','t','o','r',' '};
	public static final IType VOID_TYPE = new CPPBasicType(Kind.eVoid, 0);
	public static final IType INT_TYPE = new CPPBasicType(Kind.eInt, 0);

	private static final char[] CALL_FUNCTION = "call-function".toCharArray(); //$NON-NLS-1$
	private static final ICPPEvaluation[] NO_INITCLAUSE_EVALUATION = {};

	// Set to true for debugging.
	public static boolean traceBindingResolution = false;
	public static int traceIndent= 0;

	// special return value for costForFunctionCall
	private static final FunctionCost CONTAINS_DEPENDENT_TYPES = new FunctionCost(null, 0, null);

	// A regular expression for matching qualified names.  This allows for optional global qualification
	// (leading ::) and then separates the first part of the name from the rest (if present).  There are
	// three capture groups:
	//   (1) If the input name specifies the global namespace (leading ::) then capture group 1 will
	//       be ::.  Group 1 will be null otherwise.
	//   (2) The text of the first component of the qualified name, including leading :: if present in
	//       the input string.  Leading and trailing whitespace is trimmed.  There is no effort to check
	//       that the name contains valid C++ identifier characters.
	//   (3) The text of everything after the first component of the qualified name.
	//
	// E.g., -- Input Name --   ---- Capture Groups ----
	//       "::nsA::nsB::b" => { "::", "nsA", "nsB::b" }
	//       "a"             => { null, "a",   null     }
	//       "::  i"         => { "::", "i",   null     }
	private static final Pattern QUALNAME_REGEX = Pattern.compile("^\\s*(::)?\\s*([^\\s:]+)\\s*(?:::(.*))?$"); //$NON-NLS-1$

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

		// 1: Get some context info off of the name to figure out what kind of lookup we want.
		LookupData data = createLookupData(name);

		try {
            // 2: Lookup
            lookup(data, null);

            // Perform argument dependent lookup
            if (data.checkAssociatedScopes() && !data.hasTypeOrMemberFunctionOrVariableResult()) {
                doKoenigLookup(data);
            }
        } catch (DOMException e) {
            data.problem = (ProblemBinding) e.getProblem();
        }
		if (data.problem != null)
		    return data.problem;

		// 3: Resolve ambiguities
		IBinding binding;
        try {
            binding = resolveAmbiguities(data);
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        // 4: Post processing
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

        final IASTName lookupName = data.getLookupName();
        if (lookupName == null)
        	return binding;

		IASTNode lookupPoint = data.getLookupPoint();

        if (binding == null && data.checkClassContainingFriend()) {
        	// 3.4.1-10 If we don't find a name used in a friend declaration in the member
        	// declaration's class, we should look in the class granting friendship.
			IASTNode parent = lookupName.getParent();
        	while (parent != null && !(parent instanceof ICPPASTCompositeTypeSpecifier))
        		parent = parent.getParent();
        	if (parent instanceof ICPPASTCompositeTypeSpecifier) {
        		IScope scope = ((ICPPASTCompositeTypeSpecifier) parent).getScope();
        		try {
		    		lookup(data, scope);
		    		binding = resolveAmbiguities(data);
        		} catch (DOMException e) {
        			binding = e.getProblem();
        		}
        	}
        }

        // Explicit type conversion in functional notation.
		if (binding instanceof ICPPClassTemplate && lookupName instanceof ICPPASTTemplateId) {
			final IASTNode parent = lookupName.getParent();
			if (parent instanceof IASTIdExpression &&
					parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
				return binding;
			}
		}

        /* 14.6.1-1:
         * Within the scope of a class template, when the name of the template is neither qualified
         * nor followed by <, it is equivalent to the name followed by the template arguments
         * enclosed in <>.
         */
		if (binding instanceof ICPPClassTemplate
				&& !(binding instanceof ICPPClassSpecialization)
				&& !(binding instanceof ICPPTemplateParameter)
				&& !(lookupName instanceof ICPPASTTemplateId)) {
			ASTNodeProperty prop = lookupName.getPropertyInParent();
			if (prop != ICPPASTConstructorChainInitializer.MEMBER_ID &&
					prop != ICPPASTTemplateId.TEMPLATE_NAME && !lookupName.isQualified()) {
				// You cannot use a class template name outside of the class template scope,
				// mark it as a problem.
				IBinding user= CPPTemplates.isUsedInClassTemplateScope((ICPPClassTemplate) binding, lookupName);
				if (user instanceof ICPPClassTemplate) {
					binding= ((ICPPClassTemplate) user).asDeferredInstance();
				} else if (user != null) {
					binding= user;
				} else {
					boolean ok= false;
					IASTNode node= lookupName.getParent();
					while (node != null && !ok) {
						if (node instanceof ICPPASTTemplateId ||
								node instanceof ICPPASTTemplatedTypeTemplateParameter) {
							ok= true; // Can be argument or default-value for template template parameter
							break;
						} else if (node instanceof IASTElaboratedTypeSpecifier) {
							IASTNode parent= node.getParent();
							if (parent instanceof IASTSimpleDeclaration) {
								IASTDeclSpecifier declspec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
								if (declspec instanceof ICPPASTDeclSpecifier) {
									if (((ICPPASTDeclSpecifier) declspec).isFriend()) {
										ok= true;  // A friend class template declarations uses resolution.
										break;
									}
								}
							}
						}
						node= node.getParent();
					}
					if (!ok) {
						binding = new ProblemBinding(lookupName, lookupPoint,
								IProblemBinding.SEMANTIC_INVALID_TYPE, data.getFoundBindings());
					}
				}
			}
		} else if (binding instanceof ICPPDeferredClassInstance) {
			// Try to replace binding by the one pointing to the enclosing template declaration.
			ICPPDeferredClassInstance dcl= (ICPPDeferredClassInstance) binding;
			IBinding usedHere= CPPTemplates.isUsedInClassTemplateScope(dcl.getClassTemplate(), lookupName);
			if (usedHere instanceof ICPPClassTemplatePartialSpecialization) {
				if (CPPTemplates.areSameArguments(((ICPPClassTemplatePartialSpecialization) usedHere).getTemplateArguments(), dcl.getTemplateArguments()))
					binding= ((ICPPClassTemplatePartialSpecialization) usedHere).asDeferredInstance();
			} else if (usedHere instanceof ICPPClassTemplate) {
				if (CPPTemplates.areSameArguments(CPPTemplates.templateParametersAsArguments((ICPPClassTemplate) usedHere), dcl.getTemplateArguments())) {
					binding= ((ICPPClassTemplate) usedHere).asDeferredInstance();
				}
			}
		}

		if (binding instanceof IType) {
			IType t = getNestedType((IType) binding, TDEF);
			if (t instanceof ICPPClassType && convertClassToConstructor(lookupName)) {
				ICPPClassType cls= (ICPPClassType) t;
				if (cls instanceof IIndexBinding) {
					cls= data.getTranslationUnit().mapToAST(cls, lookupName);
				}
				try {
					if (lookupName instanceof ICPPASTTemplateId && cls instanceof ICPPClassTemplate) {
						if (data.getTranslationUnit() != null) {
							ICPPASTTemplateId id = (ICPPASTTemplateId) lookupName;
							ICPPTemplateArgument[] args = CPPTemplates.createTemplateArgumentArray(id);
							IBinding inst= CPPTemplates.instantiate((ICPPClassTemplate) cls, args, lookupPoint);
							if (inst instanceof ICPPClassType) {
								cls= (ICPPClassType) inst;
							}
						}
					}
					if (cls instanceof ICPPUnknownBinding) {
						binding= new CPPUnknownConstructor(cls);
					} else {
						// Do not interpret template arguments to a template class as being
						// explicit template arguments to its templated constructor.
						data.fTemplateArguments = null;
						binding= CPPSemantics.resolveFunction(data, ClassTypeHelper.getConstructors(cls, lookupPoint), true);
					}
				} catch (DOMException e) {
					return e.getProblem();
				}
			}
		}

        IASTName name= lookupName;
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

		// If the lookup in base-classes ran into a deferred instance, use the computed unknown
		// binding.
		final ASTNodeProperty namePropertyInParent = name.getPropertyInParent();
		if (binding == null && data.skippedScope != null) {
			if (data.hasFunctionArguments()) {
				binding= new CPPDeferredFunction(data.skippedScope, name.getSimpleID(), null);
			} else {
				if (namePropertyInParent == IASTNamedTypeSpecifier.NAME) {
					binding= new CPPUnknownMemberClass(data.skippedScope, name.getSimpleID());
				} else {
					binding= new CPPUnknownMethod(data.skippedScope, name.getSimpleID());
				}
			}
		}

        if (binding != null) {
	        if (namePropertyInParent == IASTNamedTypeSpecifier.NAME) {
	        	if (!(binding instanceof IType || binding instanceof ICPPConstructor)) {
	        		IASTNode parent = name.getParent().getParent();
	        		if (parent instanceof IASTTypeId && parent.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
	        			if (!(binding instanceof IType)) {
	        				// A type id needs to hold a type.
	        				binding = new ProblemBinding(lookupName, lookupPoint,
	        						IProblemBinding.SEMANTIC_INVALID_TYPE, data.getFoundBindings());
	        			}
	        			// Don't create a problem here.
	        		} else {
	        			binding = new ProblemBinding(lookupName, lookupPoint,
	        					IProblemBinding.SEMANTIC_INVALID_TYPE, data.getFoundBindings());
	        		}
	        	}
	        } else if (namePropertyInParent == IASTIdExpression.ID_NAME) {
	        	if (binding instanceof IType) {
		        	final IASTNode idExpr = name.getParent();
					ASTNodeProperty pip = idExpr.getPropertyInParent();
		        	if (pip == ICPPASTTemplatedTypeTemplateParameter.DEFAULT_VALUE) {
			        	// Default for template template parameter is a type.
					} else if (pip == IASTFunctionCallExpression.FUNCTION_NAME) {
						// Explicit type conversion in functional notation.
					} else if (pip == IASTUnaryExpression.OPERAND
							&& ((ICPPASTUnaryExpression) idExpr.getParent()).getOperator() == IASTUnaryExpression.op_sizeofParameterPack) {
						// Argument of sizeof... can be a type
					} else {
		        		binding= new ProblemBinding(lookupName, lookupPoint,
		        				IProblemBinding.SEMANTIC_INVALID_TYPE, data.getFoundBindings());
		        	}
	        	}
	        }
        }

        // Some declarations are found via name resolution (e.g. when using a qualified name),
        // add name as definition and check the declaration specifier.
		final IASTDeclaration declaration = data.forDeclaration();
		if (declaration != null) {
			// Functions
			if (binding instanceof IFunction) {
				binding= checkDeclSpecifier(binding, lookupName, declaration);
				if (!(binding instanceof IProblemBinding)) {
					if (declaration instanceof ICPPASTFunctionDefinition) {
						ASTInternal.addDefinition(binding, lookupName);
					}
				}
			}
			// Definitions of static fields.
			if (binding instanceof ICPPField && lookupName.isDefinition()) {
				if (declaration.getPropertyInParent() != IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
					ASTInternal.addDefinition(binding, lookupName);
				}
			}
		}

		// If this is the unqualified name of a function in a function call in a template and some
		// of the function arguments are dependent, the name could be resolved via
		// argument-dependent lookup at the point of instantiation.
		if (binding == null) {
			if (!data.qualified && data.isFunctionCall() && CPPTemplates.containsDependentType(data.getFunctionArgumentTypes())) {
				binding = CPPDeferredFunction.createForName(lookupName.getSimpleID());
			}
		}

		// If we're still null...
		if (binding == null) {
			if (name instanceof ICPPASTQualifiedName && declaration != null) {
				binding = new ProblemBinding(lookupName, lookupPoint,
						IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND, data.getFoundBindings());
			} else {
				binding = new ProblemBinding(lookupName, lookupPoint,
						IProblemBinding.SEMANTIC_NAME_NOT_FOUND, data.getFoundBindings());
			}
		}
        return binding;
    }

	private static boolean convertClassToConstructor(IASTName name) {
		if (name == null)
			return false;
		final ASTNodeProperty propertyInParent = name.getPropertyInParent();
		if (propertyInParent == null)
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
		return false;
	}

	public static void doKoenigLookup(LookupData data) throws DOMException {
		data.ignoreUsingDirectives = true;
		// Set 'qualified' to true for the duration of this function call so the calls to lookup()
		// don't ascend into enclosing scopes.
		boolean originalQualified = data.qualified;
		data.qualified = true;
        Set<ICPPFunction> friendFns = new HashSet<ICPPFunction>(2);
		Set<ICPPNamespaceScope> associated = getAssociatedScopes(data, friendFns);
		for (ICPPNamespaceScope scope : associated) {
			if (!data.visited.containsKey(scope)) {
				lookup(data, scope);
			}
		}
		Object[] matchingFriendFns = CollectionUtils.filter(
				friendFns, 
				new NameMatcherPredicate(data.getLookupKey())).toArray();
		mergeResults(data, matchingFriendFns, false);
		data.qualified = originalQualified;
	}
	
	private static class NameMatcherPredicate implements IUnaryPredicate<ICPPFunction> {
		private char[] fKey;
		
		public NameMatcherPredicate(char[] key) {
			fKey = key;
		}
		
		@Override
		public boolean apply(ICPPFunction argument) {
			return Arrays.equals(argument.getNameCharArray(), fKey);
		}
	}
	
	static IBinding checkDeclSpecifier(IBinding binding, IASTName name, IASTNode decl) {
		// Check for empty declaration specifiers.
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
		        data.qualified = true;
		}

		if (parent instanceof IASTIdExpression) {
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
	            	data.setFunctionArguments(false, NO_INITCLAUSE_EVALUATION);
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

    private static Set<ICPPNamespaceScope> getAssociatedScopes(LookupData data, Set<ICPPFunction> friendFns) {
    	if (!data.hasFunctionArguments())
    		return Collections.emptySet();

        IType[] ps = data.getFunctionArgumentTypes();
        Set<ICPPNamespaceScope> namespaces = new HashSet<ICPPNamespaceScope>(2);
        ObjectSet<IType> handled = new ObjectSet<IType>(2);
        for (IType p : ps) {
            try {
                getAssociatedScopes(p, namespaces, friendFns, handled, data.getTranslationUnit());
            } catch (DOMException e) {
            }
        }

        IASTName lookupName= data.getLookupName();
		if (lookupName != null) {
	        final char[] simpleID = lookupName.getSimpleID();
	        if (CharArrayUtils.equals(CPPVisitor.BEGIN, simpleID) || CharArrayUtils.equals(CPPVisitor.END, simpleID)) {
	        	IASTNode parent = lookupName.getParent();     // id-expression
	        	if (parent != null)
	        		parent= parent.getParent();     		  // function call
	        	if (parent != null)
	        		parent= parent.getParent();     		  // the loop
	        	if (parent instanceof ICPPASTRangeBasedForStatement) {
	        		IBinding[] std= parent.getTranslationUnit().getScope().find(CPPVisitor.STD);
	        		for (IBinding binding : std) {
	        			if (binding instanceof ICPPNamespace) {
	        				namespaces.add(((ICPPNamespace) binding).getNamespaceScope());
	        			}
	        		}
	        	}
	        }
        }
        return namespaces;
    }

    // 3.4.2-2
    private static void getAssociatedScopes(IType t, Set<ICPPNamespaceScope> namespaces,
    		Set<ICPPFunction> friendFns, ObjectSet<IType> handled, CPPASTTranslationUnit tu) throws DOMException {
        t = getNestedType(t, TDEF | CVTYPE | PTR | ARRAY | REF);
        // No point getting namespaces associated with a dependent type - we don't know what they
        // are yet.
        if (CPPTemplates.isDependentType(t))
        	return;
    	if (t instanceof IBinding) {
            if (handled.containsKey(t))
            	return;
            handled.put(t);

    		IBinding owner= ((IBinding) t).getOwner();
    		if (owner instanceof ICPPClassType) {
    			getAssociatedScopes((IType) owner, namespaces, friendFns, handled, tu);
    		} else {
    			getAssociatedNamespaceScopes(getContainingNamespaceScope((IBinding) t, tu), namespaces);
    		}
    	}
		if (t instanceof ICPPClassType && !(t instanceof ICPPClassTemplate)) {
			ICPPClassType ct= (ICPPClassType) t;
			ICPPBase[] bases = ClassTypeHelper.getBases(ct, tu);
			for (ICPPBase base : bases) {
				IBinding b = base.getBaseClass();
				if (b instanceof IType)
					getAssociatedScopes((IType) b, namespaces, friendFns, handled, tu);
			}
			// Furthermore, if T is a class template ...
			// * ... types of the template arguments for template type parameters
			//       (excluding template template parameters);
			// * ... owners of which any template template arguments are members;
			if (ct instanceof ICPPTemplateInstance) {
				for (IBinding friend : ClassTypeHelper.getFriends(ct, tu)) {
					if (friend instanceof ICPPFunction) {
						friendFns.add((ICPPFunction) friend);
					}
				}
				ICPPTemplateArgument[] args = ((ICPPTemplateInstance) ct).getTemplateArguments();
				for (ICPPTemplateArgument arg : args) {
					if (arg.isTypeValue()) {
						getAssociatedScopes(arg.getTypeValue(), namespaces, friendFns, handled, tu);
					}
				}
			}
		} else if (t instanceof IFunctionType) {
		    IFunctionType ft = (IFunctionType) t;
		    getAssociatedScopes(ft.getReturnType(), namespaces, friendFns, handled, tu);
		    IType[] ps = ft.getParameterTypes();
		    for (IType pt : ps) {
		        getAssociatedScopes(pt, namespaces, friendFns, handled, tu);
		    }
		} else if (t instanceof ICPPPointerToMemberType) {
		    final ICPPPointerToMemberType pmt = (ICPPPointerToMemberType) t;
			getAssociatedScopes(pmt.getMemberOfClass(), namespaces, friendFns, handled, tu);
	        getAssociatedScopes(pmt.getType(), namespaces, friendFns, handled, tu);
		} else if (t instanceof FunctionSetType) {
			FunctionSetType fst= (FunctionSetType) t;
			for (ICPPFunction fn : fst.getFunctionSet().getBindings()) {
				getAssociatedScopes(fn.getType(), namespaces, friendFns, handled, tu);
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

	static ICPPScope getLookupScope(IASTName name) throws DOMException {
	    IASTNode parent = name.getParent();
	    IScope scope = null;
    	if (parent instanceof ICPPASTBaseSpecifier) {
    	    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) parent.getParent();
    	    IASTName n = compSpec.getName();
    	    if (n instanceof ICPPASTQualifiedName) {
    	        n = n.getLastName();
    	    }
	        scope = CPPVisitor.getContainingScope(n);
	    } else {
	    	scope = CPPVisitor.getContainingScope(name);
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
	        @SuppressWarnings("unchecked")
			final CharArrayObjectMap<Object> oldItems = (CharArrayObjectMap<Object>) data.foundItems;
			data.foundItems = mergePrefixResults(oldItems, results, scoped);
	    }
	}

	/**
	 * @param dest
	 * @param source : either Object[] or CharArrayObjectMap
	 * @param scoped
	 * @return
	 */
	static CharArrayObjectMap<Object> mergePrefixResults(CharArrayObjectMap<Object> dest, Object source, boolean scoped) {
		if (source == null) return dest;
        CharArrayObjectMap<Object> resultMap = (dest != null) ? dest : new CharArrayObjectMap<Object>(2);

        CharArrayObjectMap<Object> map = null;
        Object[] objs = null;
        int size;
        if (source instanceof CharArrayObjectMap) {
        	@SuppressWarnings("unchecked")
			final CharArrayObjectMap<Object> sourceMap = (CharArrayObjectMap<Object>) source;
			map = sourceMap;
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

	/**
	 * Perform a lookup with the given data starting in the given scope, considering bases and parent scopes.
	 * @param data the lookup data created off a name
	 * @param start either a scope or a name.
	 */
	static protected void lookup(LookupData data, IScope start) throws DOMException {
		if (start == null && lookupDestructor(data)) {
			return;
		}

		ICPPScope nextScope= null;
		ICPPTemplateScope nextTmplScope= null;
		if (start instanceof ICPPScope) {
			nextScope= (ICPPScope) start;
		} else {
			IASTName lookupName= data.getLookupName();
			if (lookupName == null)
				return;

			nextScope= getLookupScope(lookupName);

			if (nextScope instanceof ICPPTemplateScope) {
				nextTmplScope= (ICPPTemplateScope) nextScope;
				nextScope= getParentScope(nextScope, data.getTranslationUnit());
			} else {
				nextTmplScope= enclosingTemplateScope(lookupName);
			}
			if (data.qualified && nextTmplScope != null) {
				nextTmplScope= null;
				if (dependsOnTemplateFieldReference(lookupName)) {
					data.setIgnorePointOfDeclaration(true);
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
			CPPASTTranslationUnit tu = data.getTranslationUnit();
			if (scope instanceof IIndexScope && tu != null) {
				scope= (ICPPScope) tu.mapToASTScope(((IIndexScope) scope));
			}

			if (!data.usingDirectivesOnly && !(data.ignoreMembers && scope instanceof ICPPClassScope)) {
				mergeResults(data, getBindingsFromScope(scope, data), true);

				// Nominate using-directives found in this block or namespace.
				if (scope instanceof ICPPNamespaceScope) {
					final ICPPNamespaceScope blockScope= (ICPPNamespaceScope) scope;

					if (data.qualified && blockScope.getKind() != EScopeKind.eLocal) {
						lookupInlineNamespaces(data, blockScope);
					}
					if (data.contentAssist || !data.hasResults() || !data.qualified) {
						// Nominate namespaces
						nominateNamespaces(data, blockScope);
					}
				}
			}

			// Lookup in nominated namespaces
			if (!data.ignoreUsingDirectives && scope instanceof ICPPNamespaceScope && !(scope instanceof ICPPBlockScope)) {
				if (!data.hasResults() || !data.qualified || data.contentAssist) {
					lookupInNominated(data, (ICPPNamespaceScope) scope);
				}
			}

			if (friendInLocalClass && !(scope instanceof ICPPClassScope))
				return;
			if (!data.contentAssist && hasReachableResult(data))
				return;

			// Lookup in base classes
			if (!data.usingDirectivesOnly && scope instanceof ICPPClassScope && !data.ignoreMembers) {
				BaseClassLookup.lookupInBaseClasses(data, (ICPPClassScope) scope);
				if (!data.contentAssist && data.hasResultOrProblem())
					return;
			}

			if (data.qualified && !(scope instanceof ICPPTemplateScope)) {
				if (data.ignoreUsingDirectives || data.usingDirectives.isEmpty())
					return;
				data.usingDirectivesOnly = true;
			}

			// Compute next scopes
			if (useTemplScope && nextTmplScope != null) {
				nextTmplScope= enclosingTemplateScope(nextTmplScope.getTemplateDeclaration());
			} else {
				nextScope= getParentScope(scope, data.getTranslationUnit());
			}
		}
	}

	/**
	 * Checks if lookup data contains result bindings reachable through includes
	 * from the translation unit where lookup started. Any binding is considered reachable
	 * if the lookup is not done in a context of a translation unit.
	 *
	 * @param data the LookupData object.
	 * @return {@code true} if the lookup data contains at least one reachable binding.
	 */
	private static boolean hasReachableResult(LookupData data) {
    	if (data.foundItems instanceof Object[]) {
    		for (Object item : (Object[]) data.foundItems) {
    			if (item instanceof IBinding) {
    				IBinding binding = (IBinding) item;
    				CPPASTTranslationUnit tu = data.getTranslationUnit();
    				if (!isFromIndex(binding) || tu == null || isReachableFromAst(tu, binding)) {
    					return true;
    				}
    			}
    		}
    	}
    	return false;
	}

	private static void lookupInlineNamespaces(LookupData data, ICPPNamespaceScope namespace) throws DOMException {
		if (namespace instanceof ICPPInternalNamespaceScope) {
			ICPPInternalNamespaceScope ns= (ICPPInternalNamespaceScope) namespace;
			for (ICPPInternalNamespaceScope inline : ns.getInlineNamespaces()) {
				mergeResults(data, getBindingsFromScope(inline, data), true);
				lookupInlineNamespaces(data, inline);
				nominateNamespaces(data, inline);
			}
		}
	}

	private static void nominateNamespaces(LookupData data, final ICPPNamespaceScope blockScope)
			throws DOMException {
		final boolean isBlockScope = blockScope.getKind() == EScopeKind.eLocal;
		if (!isBlockScope) {
			data.visited.put(blockScope);	// Mark as searched.
			CPPASTTranslationUnit tu= data.getTranslationUnit();
			if (tu != null) {
				tu.handleAdditionalDirectives(blockScope);
			}
		}
		ICPPUsingDirective[] uds= blockScope.getUsingDirectives();
		if (uds != null && uds.length > 0) {
			HashSet<ICPPNamespaceScope> handled= new HashSet<ICPPNamespaceScope>();
			for (final ICPPUsingDirective ud : uds) {
				if (data.isIgnorePointOfDeclaration() || declaredBefore(ud, data.getLookupPoint(), false)) {
					storeUsingDirective(data, blockScope, ud, handled);
				}
			}
		}
	}

	private static boolean lookupDestructor(LookupData data) throws DOMException {
		IASTName typeDtorName= data.getLookupName();
		if (typeDtorName == null)
			return false;
		final char[] typeDtorChars= typeDtorName.getSimpleID();
		if (typeDtorChars.length == 0 || typeDtorChars[0] != '~')
			return false;

		// Assume class C; typedef C T;
		// When looking up ~T the strategy is to lookup T::~C in two steps:
		// * First resolve 'T', then compute '~C' and resolve it.
		IASTNode parent= typeDtorName.getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName dqname= (ICPPASTQualifiedName) parent;
			if (dqname.getLastName() != typeDtorName)
				return false;
		}
		char[] tchars= new char[typeDtorChars.length-1];
		System.arraycopy(typeDtorChars, 1, tchars, 0, tchars.length);

		LookupData ld2= new LookupData(tchars, data.fTemplateArguments, data.getLookupPoint());
		ld2.setIgnorePointOfDeclaration(data.isIgnorePointOfDeclaration());
		ld2.contentAssist= data.contentAssist;
		ld2.fNoNarrowing= data.fNoNarrowing;
		ld2.qualified= parent instanceof ICPPASTQualifiedName;
		ld2.typesOnly= true;
		lookup(ld2, getLookupScope(typeDtorName));
		IBinding[] typedefs = ld2.getFoundBindings();
		if (typedefs.length < 1 || !(typedefs[0] instanceof ITypedef))
			return false;

		IType t= SemanticUtil.getNestedType((ITypedef) typedefs[0], TDEF);
		if (t instanceof ICPPUnknownBinding || t instanceof ISemanticProblem ||
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
		data.setLookupKey(classDtorChars);
		lookup(data, scope);
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
					IType t= SemanticUtil.getUltimateType(((IVariable) b).getType(), true);
					if (t instanceof ICPPUnknownBinding || t instanceof ICPPTemplateDefinition) {
						result[0]= true;
						return PROCESS_ABORT;
					}
				}
				if (name instanceof ICPPASTTemplateId)
					return PROCESS_SKIP;
				return PROCESS_CONTINUE;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTLiteralExpression) {
					final ICPPASTLiteralExpression litExpr = (ICPPASTLiteralExpression) expression;
					if (litExpr.getKind() == IASTLiteralExpression.lk_this) {
						final IType thisType = SemanticUtil.getNestedType(litExpr.getEvaluation().getTypeOrFunctionSet(litExpr), TDEF | ALLCVQ | PTR | ARRAY | MPTR | REF);
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

	static IBinding[] getBindingsFromScope(ICPPScope scope, LookupData data) throws DOMException {
		IBinding[] bindings= scope.getBindings(data);

		if (scope instanceof ICPPASTInternalScope && scope instanceof ICPPClassScope) {
			final IASTName lookupName = data.getLookupName();
			if (LookupData.checkWholeClassScope(lookupName)) {
				// Bug 103857: Members declared after the point of completion cannot be
				//     found in the partial AST, we look them up in the index
				CPPASTTranslationUnit tu = data.getTranslationUnit();
				if (tu != null && tu.isForContentAssist()) {
					IIndex index = tu.getIndex();
					IASTNode node = ((IASTInternalScope) scope).getPhysicalNode();
					if (index != null && node != null && node.contains(lookupName)) {
						IBinding indexBinding= index.adaptBinding(((ICPPClassScope) scope).getClassType());
						if (indexBinding instanceof ICPPClassType) {
							IScope scopeInIndex= ((ICPPClassType) indexBinding).getCompositeScope();
							bindings= ArrayUtil.addAll(bindings, scopeInIndex.getBindings(data));
						}
					}
				}
			}
		}

		if (data.ignoreRecursionResolvingBindings()) {
			bindings = ArrayUtil.filter(bindings, new RecursionResolvingBindingFilter());
		}

		return expandUsingDeclarationsAndRemoveObjects(bindings, data);
	}

	private static class RecursionResolvingBindingFilter implements IUnaryPredicate<IBinding> {
		@Override
		public boolean apply(IBinding argument) {
			return !(argument instanceof IRecursionResolvingBinding);
		}
	}
	
	private static IBinding[] expandUsingDeclarationsAndRemoveObjects(final IBinding[] bindings,
			LookupData data) {
		if (bindings == null || bindings.length == 0)
			return IBinding.EMPTY_BINDING_ARRAY;

		for (IBinding b : bindings) {
			if (b == null)
				break;

			if (b instanceof ICPPUsingDeclaration || (data.typesOnly && isObject(b))) {
				List<IBinding> result= new ArrayList<IBinding>(bindings.length);
				expandUsingDeclarations(bindings, data, result);
				return result.toArray(new IBinding[result.size()]);
			}
		}
		return bindings;
	}

	private static boolean isObject(IBinding b) {
		return !(b instanceof IType || b instanceof ICPPNamespace);
	}

	private static void expandUsingDeclarations(IBinding[] bindings, LookupData data, List<IBinding> result) {
		if (bindings != null) {
			for (IBinding b : bindings) {
				if (b == null)
					return;
				// Lookup for a declaration shall ignore the using declarations.
				if (b instanceof ICPPUsingDeclaration) {
					if (data.forDeclaration() == null) {
						for (IBinding d : ((ICPPUsingDeclaration) b).getDelegates()) {
							if (d != null && !(data.typesOnly && isObject(d))) {
								result.add(d);
							}
						}
					}
				} else if (!(data.typesOnly && isObject(b))) {
					result.add(b);
				}
			}
		}
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
				if (qname.isFullyQualified() || qname.getQualifier()[0] != node)
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

	static ICPPScope getParentScope(IScope scope, ICPPASTTranslationUnit unit) throws DOMException {
		IScope parentScope= scope.getParent();
		// the index cannot return the translation unit as parent scope
		if (unit instanceof CPPASTTranslationUnit) {
			if (parentScope == null
					&& (scope instanceof IIndexScope || scope instanceof ICPPClassSpecializationScope)) {
				parentScope = unit.getScope();
			} else if (parentScope instanceof IIndexScope) {
				parentScope = ((CPPASTTranslationUnit) unit).mapToASTScope((IIndexScope) parentScope);
			}
		}
		return (ICPPScope) parentScope;
	}

	/**
	 * Stores the using directive with the scope where the members of the nominated namespace will
	 * appear. In case of an unqualified lookup the transitive directives are stored, also.
	 * This is important because the members nominated by a transitive directive can appear before
	 * those of the original directive.
	 */
	private static void storeUsingDirective(LookupData data, ICPPNamespaceScope container,
			ICPPUsingDirective directive, Set<ICPPNamespaceScope> handled) throws DOMException {
		ICPPNamespaceScope nominated= directive.getNominatedScope();
		CPPASTTranslationUnit tu= data.getTranslationUnit();
		if (nominated instanceof IIndexScope && tu != null) {
			nominated= (ICPPNamespaceScope) tu.mapToASTScope((IIndexScope) nominated);
		}
		if (nominated == null || data.visited.containsKey(nominated) || (handled != null && !handled.add(nominated))) {
			return;
		}
		// 7.3.4.1 names appear at end of common enclosing scope of container and nominated scope.
		final IScope appearsIn= getCommonEnclosingScope(nominated, container, tu);
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

		// In a non-qualified lookup the transitive directive have to be stored right away,
		// they may overtake the container.
		if (!data.qualified || data.contentAssist) {
			assert handled != null;
			if (tu != null) {
				tu.handleAdditionalDirectives(nominated);
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
	private static ICPPScope getCommonEnclosingScope(IScope s1, IScope s2, ICPPASTTranslationUnit tu)
			throws DOMException {
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
		    // Need binding because namespaces can be split.
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
		} else if (parent instanceof ICPPASTRangeBasedForStatement) {
			ICPPASTRangeBasedForStatement forStatement = (ICPPASTRangeBasedForStatement) parent;
			final IASTDeclaration decl = forStatement.getDeclaration();
			nodes= new IASTNode[] {decl};
		} else if (parent instanceof ICPPASTEnumerationSpecifier) {
			// The enumeration scope contains the enumeration items
	    	for (IASTEnumerator enumerator : ((ICPPASTEnumerationSpecifier) parent).getEnumerators()) {
	    		ASTInternal.addName(scope, enumerator.getName());
	    	}
	    	return;
		} else if (parent instanceof ICPPASTTemplatedTypeTemplateParameter) {
			// The template-template parameter scope contains the parameters
	    	for (ICPPASTTemplateParameter par : ((ICPPASTTemplatedTypeTemplateParameter) parent).getTemplateParameters()) {
	    		IASTName name= CPPTemplates.getTemplateParameterName(par);
	    		if (name != null) {
	    			ASTInternal.addName(scope, name);
	    		}
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
			        nodeStack = ArrayUtil.append(IASTNode[].class, nodeStack, nodes);
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
				        // Check all definitions of this namespace.
					    while (++namespaceIdx < namespaceDefs.length) {
					        nodes = ((ICPPASTNamespaceDefinition) namespaceDefs[namespaceIdx].getParent()).getDeclarations();
						    if (nodes.length > 0) {
						        idx = 0;
						        item = nodes[0];
						        break;
						    }
					    }
				    } else if (parent instanceof IASTCompoundStatement && nodes instanceof IASTParameterDeclaration[]) {
				    	// Function body, we were looking at parameters, now check the body itself.
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
            if (innermost != null) { // Could be null when content assist in the declSpec
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
			// Collect friends and elaborated type specifiers with declarators from nested classes.
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

				default:
					break;
				}
			}
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) declaration;
			IASTName name = using.getName();
			if (name instanceof ICPPASTQualifiedName) {
				name = name.getLastName();
			}
			ASTInternal.addName(scope, name);
		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			IASTName namespaceName = ((ICPPASTNamespaceDefinition) declaration).getName();
			ASTInternal.addName(scope, namespaceName);
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			IASTName alias = ((ICPPASTNamespaceAlias) declaration).getAlias();
			ASTInternal.addName(scope, alias);
		} else if (declaration instanceof ICPPASTAliasDeclaration) {
			IASTName alias = ((ICPPASTAliasDeclaration) declaration).getAlias();
			ASTInternal.addName(scope, alias);
		} else if (declaration instanceof IASTFunctionDefinition) {
 			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
			final IASTDeclSpecifier declSpec = functionDef.getDeclSpecifier();
			IASTFunctionDeclarator declarator = functionDef.getDeclarator();

			if (!((ICPPASTDeclSpecifier) declSpec).isFriend()) {
				// Check the function itself
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

			default:
				break;
			}
		}
	}

	/**
	 * Perform lookup in nominated namespaces that appear in the given scope. For unqualified
	 * lookups the method assumes that transitive directives have been stored in the lookup-data.
	 * For qualified lookups the transitive directives are considered if the lookup of the original
	 * directive returns empty.
	 */
	private static void lookupInNominated(LookupData data, ICPPNamespaceScope scope) throws DOMException {
		List<ICPPNamespaceScope> allNominated= data.usingDirectives.remove(scope);
		while (allNominated != null) {
			for (ICPPNamespaceScope nominated : allNominated) {
				if (data.visited.containsKey(nominated)) {
					continue;
				}
				data.visited.put(nominated);

				boolean found = false;
				IBinding[] bindings= getBindingsFromScope(nominated, data);
				if (bindings != null && bindings.length > 0) {
					mergeResults(data, bindings, true);
					found = true;
				}

				// In the qualified lookup we have to nominate the transitive directives only when
				// the lookup did not succeed. In the qualified case this is done earlier, when
				// the directive is encountered.
				if (!found && data.qualified && !data.contentAssist) {
					final CPPASTTranslationUnit tu = data.getTranslationUnit();
					if (tu != null) {
						tu.handleAdditionalDirectives(nominated);
					}
					ICPPUsingDirective[] usings= nominated.getUsingDirectives();
					for (ICPPUsingDirective using : usings) {
						storeUsingDirective(data, scope, using, null);
					}
				}
			}
			// Retry with transitive directives that may have been nominated in a qualified lookup
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

	        	// Bug 238180
	        	if (candidate instanceof ICPPClassTemplatePartialSpecialization)
	        		return null;

		        // Specialization is selected during instantiation
		        if (candidate instanceof ICPPTemplateInstance)
		        	candidate= ((ICPPTemplateInstance) candidate).getSpecializedBinding();

		        if (!(candidate instanceof ICPPFunctionTemplate))
		        	return candidate;
	        }
	    }

	    LookupData data = createLookupData(name);
	    data.foundItems = bindings;
	    try {
	    	return resolveAmbiguities(data);
	    } catch (DOMException e) {
	    	return e.getProblem();
	    }
//
//        IBinding[] result = null;
//        for (Object binding : bindings) {
//            if (binding instanceof IASTName) {
//                result = ArrayUtil.append(IBinding.class, result, ((IASTName) binding).resolveBinding());
//            } else if (binding instanceof IBinding) {
//                result = ArrayUtil.append(IBinding.class, result, (IBinding) binding);
//            }
//        }
//        return new CPPCompositeBinding(result);
	}

	public static boolean declaredBefore(Object obj, IASTNode node, boolean indexBased) {
	    if (node == null)
	    	return true;

	    final int pointOfRef= ((ASTNode) node).getOffset();
	    ASTNode nd = null;
	    if (obj instanceof ICPPSpecialization) {
	        obj = ((ICPPSpecialization) obj).getSpecializedBinding();
	    }

	    int pointOfDecl= -1;
	    if (obj instanceof ICPPInternalBinding) {
	        ICPPInternalBinding cpp = (ICPPInternalBinding) obj;
	        // For bindings in global or namespace scope we don't know whether there is a
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
                // Point of declaration for a name is immediately after its complete declarator
            	// and before its initializer.
                IASTDeclarator dtor = (IASTDeclarator)((nd instanceof IASTDeclarator) ? nd : nd.getParent());
                while (dtor.getParent() instanceof IASTDeclarator)
                    dtor = (IASTDeclarator) dtor.getParent();
                IASTInitializer init = dtor.getInitializer();
                if (init != null)
                    pointOfDecl = ((ASTNode) init).getOffset() - 1;
                else
                    pointOfDecl = ((ASTNode) dtor).getOffset() + ((ASTNode) dtor).getLength();
            } else if (prop == IASTEnumerator.ENUMERATOR_NAME) {
                // Point of declaration for an enumerator is immediately after it
            	// enumerator-definition
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
				if (!(scope instanceof ICPPBlockScope) && scope instanceof ICPPNamespaceScope) {
					return true;
				}
			} else if (cpp instanceof ICompositeType || cpp instanceof IEnumeration) {
				IScope scope= cpp.getScope();
				if (!(scope instanceof ICPPBlockScope) && scope instanceof ICPPNamespaceScope) {
					// If this is not the definition, it may be found in a header. (bug 229571)
					if (cpp.getDefinition() == null) {
						return true;
					}
				}
			}
		} catch (DOMException e) {
		}
		return false;
	}

	private static IBinding resolveAmbiguities(LookupData data) throws DOMException {
	    if (!data.hasResults() || data.contentAssist)
	        return null;

	    final IASTName lookupName = data.getLookupName();
        IASTNode lookupPoint = data.getLookupPoint();
	    final boolean indexBased= data.getIndex() != null;
	    final boolean checkWholeClass= lookupName == null || LookupData.checkWholeClassScope(lookupName);
	    @SuppressWarnings("unchecked")
	    ObjectSet<ICPPFunction> fns= ObjectSet.EMPTY_SET;
	    IBinding type = null;
	    IBinding obj  = null;
	    IBinding temp = null;

	    final CPPASTTranslationUnit tu = data.getTranslationUnit();
	    Object[] items = (Object[]) data.foundItems;
		for (int i = 0; i < items.length && items[i] != null; i++) {
	        Object o = items[i];
			boolean declaredBefore = data.isIgnorePointOfDeclaration() || declaredBefore(o, lookupPoint, indexBased);
	        boolean checkResolvedNamesOnly= false;
	        if (!checkWholeClass && !declaredBefore) {
	        	if (lookupName != null && lookupName.getRoleOfName(false) != IASTNameOwner.r_reference) {
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

	        // Select among those bindings that have been created without problems.
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
		        // Specializations are selected during instantiation
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
					int c = compareByRelevance(tu, type, temp);
	        		if (c < 0) {
        				type= temp;
	        		} else if (c == 0) {
        				if (((IType) type).isSameType((IType) temp)) {
        					if (type instanceof ITypedef && !(temp instanceof ITypedef)) {
        						// Between same types prefer non-typedef.
        						type= temp;
        					}
        				} else {
        					return new ProblemBinding(lookupName, lookupPoint,
        							IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.getFoundBindings());
        				}
        			}
	            }
	        } else {
	        	if (obj == null) {
	        		obj = temp;
	        	} else if (obj.equals(temp)) {
	        	    // Ok, delegates are synonyms.
	        	} else {
	        		int c = compareByRelevance(tu, obj, temp);
	        		if (c < 0) {
	        			obj= temp;
	        		} else if (c == 0) {
	        			return new ProblemBinding(lookupName, lookupPoint,
	        					IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.getFoundBindings());
	        		}
	        	}
	        }
	    }
	    if (data.forUsingDeclaration) {
        	int cmp= -1;
	        if (obj != null) {
	        	cmp= 1;
	            if (fns.size() > 0) {
		    		IFunction[] fnArray= fns.keyArray(IFunction.class);
					cmp= compareByRelevance(data, obj, fnArray);
					if (cmp == 0) {
						return new ProblemBinding(lookupName, lookupPoint,
								IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.getFoundBindings());
					}
	            }
	        }

	        IBinding[] bindings = IBinding.EMPTY_BINDING_ARRAY;
	        if (cmp > 0) {
	            bindings = ArrayUtil.append(bindings, obj);
	            bindings = ArrayUtil.append(bindings, type);
	        } else {
	            bindings = ArrayUtil.append(bindings, type);
	            bindings = ArrayUtil.addAll(bindings, fns.keyArray());
	        }
	        bindings = ArrayUtil.trim(IBinding.class, bindings);
	        ICPPUsingDeclaration composite = new CPPUsingDeclaration(lookupName, bindings);
	        return composite;
	    }

	    if (obj != null && type != null) {
	    	if (obj instanceof ICPPNamespace) {
	    		if (compareByRelevance(tu, type, obj) >= 0) {
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
	    			return new ProblemBinding(lookupName, lookupPoint,
	    					IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.getFoundBindings());
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
		if (data == null)
			return false;
		final CPPASTTranslationUnit tu = data.getTranslationUnit();
		if (tu != null) {
			return !isReachableFromAst(tu, b2) && isReachableFromAst(tu, type);
		}
		return false;
	}

	/**
	 * Compares a binding with a list of function candidates for relevance in the
	 * context of an AST. Types are considered to overrule object bindings when
	 * the former is reachable but none of the functions are.
	 */
	static boolean overrulesByRelevance(LookupData data, IBinding type, IFunction[] fns) {
		if (data == null)
			return false;
		final CPPASTTranslationUnit tu = data.getTranslationUnit();

		for (int i = 0; i < fns.length; i++) {
			if (!isFromIndex(fns[i])) {
				return false;	// function from ast
			}
		}

		if (!isReachableFromAst(tu, type)) {
			return false;
		}

		for (IFunction fn : fns) {
			if (isReachableFromAst(tu, fn)) {
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
			final CPPASTTranslationUnit tu = data.getTranslationUnit();
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
    		// Everything is from the index
    		final CPPASTTranslationUnit tu = data.getTranslationUnit();
			if (!isReachableFromAst(tu, obj)) {
    			return -1; // obj not reachable
    		}

    		for (IFunction fn : fns) {
    			if (isReachableFromAst(tu, fn)) {
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
		IIndexFileSet astFileSet = ast.getASTFileSet();
		return indexFileSet != null &&
				(indexFileSet.containsDeclaration(indexBinding) ||
				 astFileSet.containsDeclaration(indexBinding));
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
	    assert data.forDeclaration() == null;

	    final int argumentCount = data.getFunctionArgumentCount();
	    final int packExpansionCount= data.getFunctionArgumentPackExpansionCount();

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
				if (numArgs - packExpansionCount > numPars) {
					// More arguments than parameters --> need ellipsis or parameter pack
					ok= fn.takesVarArgs() || fn.hasParameterPack();
				} else {
					ok = numArgs >= fn.getRequiredArgumentCount() || packExpansionCount > 0;
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

	public static IBinding resolveFunction(LookupData data, ICPPFunction[] fns, boolean allowUDC) throws DOMException {
	    final IASTName lookupName = data.getLookupName();
		if (fns == null || fns.length == 0 || fns[0] == null)
	        return null;
	    fns= ArrayUtil.trim(fns);

	    sortAstBeforeIndex(fns);

		if (data.forUsingDeclaration)
			return new CPPUsingDeclaration(lookupName, fns);

		if (lookupName instanceof ICPPASTConversionName) {
			return resolveUserDefinedConversion(data, fns);
		}

	    if (data.forDeclaration() != null) {
	    	return resolveFunctionDeclaration(data, fns);
	    }

		// No arguments to resolve function
		final IASTNode lookupPoint = data.getLookupPoint();
		if (!data.hasFunctionArguments()) {
			return createFunctionSet(fns, data.fTemplateArguments, lookupPoint, lookupName);
		}

		// Reduce our set of candidate functions to only those who have the right number of parameters
	    final IType[] argTypes = data.getFunctionArgumentTypes();
		ICPPFunction[] tmp= selectByArgumentCount(data, fns);
	    tmp= CPPTemplates.instantiateForFunctionCall(tmp, data.fTemplateArguments,
	    		Arrays.asList(argTypes),
	    		Arrays.asList(data.getFunctionArgumentValueCategories()),
	    		data.argsContainImpliedObject, lookupPoint);
	    if (tmp.length == 0 || tmp[0] == null) {
			return new ProblemBinding(lookupName, lookupPoint, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, fns);
	    }

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
			return new ProblemBinding(lookupName, lookupPoint, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, fns);

		// Check for dependent arguments
		fns= tmp;
		if (CPPTemplates.containsDependentType(argTypes)) {
			if (viableCount == 1)
				return fns[0];
			setTargetedFunctionsToUnknown(argTypes);
			return CPPDeferredFunction.createForCandidates(fns);
		}

		IFunction[] ambiguousFunctions= null;   // Ambiguity, 2 functions are equally good.
		FunctionCost bestFnCost = null;		    // The cost of the best function.

		// Loop over all functions
		List<FunctionCost> potentialCosts= null;
		ICPPFunction unknownFunction= null;
		final CPPASTTranslationUnit tu = data.getTranslationUnit();
		for (ICPPFunction fn : fns) {
			if (fn == null)
				continue;

			final FunctionCost fnCost= costForFunctionCall(fn, allowUDC, data);
			if (fnCost == null)
				continue;

			if (fnCost == CONTAINS_DEPENDENT_TYPES) {
				if (viableCount == 1)
					return fn;
				unknownFunction = fn;
				continue;
			}

			if (fnCost.hasDeferredUDC()) {
				if (potentialCosts == null) {
					potentialCosts= new ArrayList<FunctionCost>();
				}
				potentialCosts.add(fnCost);
				continue;
			}
			int cmp= fnCost.compareTo(tu, bestFnCost);
			if (cmp < 0) {
				bestFnCost= fnCost;
				ambiguousFunctions= null;
			} else if (cmp == 0) {
				ambiguousFunctions= ArrayUtil.append(IFunction.class, ambiguousFunctions, fn);
			}
		}

		if (potentialCosts != null) {
			for (FunctionCost fnCost : potentialCosts) {
				if (!fnCost.mustBeWorse(bestFnCost) && fnCost.performUDC(lookupPoint)) {
					int cmp= fnCost.compareTo(tu, bestFnCost);
					if (cmp < 0) {
						bestFnCost= fnCost;
						ambiguousFunctions= null;
					} else if (cmp == 0) {
						ambiguousFunctions= ArrayUtil.append(IFunction.class, ambiguousFunctions, fnCost.getFunction());
					}
				}
			}
		}

		if (bestFnCost == null) {
			if (unknownFunction == null)
				return null;

			setTargetedFunctionsToUnknown(argTypes);
			return CPPDeferredFunction.createForCandidates(fns);
		}

		if (ambiguousFunctions != null) {
			ambiguousFunctions= ArrayUtil.append(IFunction.class, ambiguousFunctions, bestFnCost.getFunction());
			return new ProblemBinding(lookupName, lookupPoint,
					IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, ambiguousFunctions);
		}
		if (bestFnCost.hasAmbiguousUserDefinedConversion()) {
			return new ProblemBinding(lookupName, lookupPoint,
					IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, data.getFoundBindings());
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

	private static IBinding createFunctionSet(ICPPFunction[] fns, ICPPTemplateArgument[] args, IASTNode point, IASTName name) {
		// First try to find a unique function
		if (name != null && name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			name= (IASTName) name.getParent();
		}
		ICPPFunction f= getUniqueFunctionForSet(fns, args, point);
		return f == null ? new CPPFunctionSet(fns, args, name) : f;
	}

	private static ICPPFunction getUniqueFunctionForSet(ICPPFunction[] fns,
			ICPPTemplateArgument[] args, IASTNode point) {
		// First try to find a unique function
		ICPPFunction result= null;
		boolean haveASTResult= false;
		for (ICPPFunction f : fns) {
			// Use the ast binding
			final boolean fromIndex = isFromIndex(f);
			if (haveASTResult && fromIndex)
				break;

			boolean isCandidate;
			if (f instanceof ICPPFunctionTemplate) {
				if (args == null) {
					isCandidate= true;
				} else {
					// See 14.3-7
					ICPPFunctionTemplate funcTemp = (ICPPFunctionTemplate) f;
					final ICPPTemplateParameter[] tpars = funcTemp.getTemplateParameters();
					final CPPTemplateParameterMap map = new CPPTemplateParameterMap(tpars.length);
					isCandidate= TemplateArgumentDeduction.addExplicitArguments(funcTemp, tpars, args, map, point);
				}
			} else {
				isCandidate= args == null;
			}
			if (isCandidate) {
				if (result != null)
					return null;
				result= f;
				haveASTResult= !fromIndex;
			}
		}

		if (result instanceof ICPPFunctionTemplate)
			return CPPTemplates.instantiateForAddressOfFunction((ICPPFunctionTemplate) result, null, args, point);

		return result;
	}

	private static void setTargetedFunctionsToUnknown(IType[] argTypes) {
		for (IType argType : argTypes) {
			if (argType instanceof FunctionSetType) {
				((FunctionSetType) argType).setToUnknown();
			}
		}
	}

	/**
	 * Called for declarations with qualified name or template-id. Also for explicit function
	 * specializations or instantiations.
	 */
	private static IBinding resolveFunctionDeclaration(LookupData data, ICPPFunction[] fns) throws DOMException {
		final IASTDeclarator dtor= ASTQueries.findTypeRelevantDeclarator(data.getDeclarator());
		final IType t = CPPVisitor.createType(dtor);
		if (!(t instanceof ICPPFunctionType))
			return null;

		final ICPPFunctionType ft= (ICPPFunctionType) t;

		IASTName templateID= data.getLookupName();
		if (templateID.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			templateID= (ICPPASTTemplateId) templateID.getParent();
		}

		// 14.5.4 Friends with template ids require instantiation
		boolean isFriend= CPPVisitor.isFriendDeclaration(data.forDeclaration());
		if (!data.forExplicitFunctionSpecialization()
				&& !(isFriend && templateID instanceof ICPPASTTemplateId)) {
			// Search for a matching function
			for (ICPPFunction fn : fns) {
				if (fn != null && !(fn instanceof IProblemBinding) && !(fn instanceof ICPPUnknownBinding)) {
					if (isSameFunction(fn, dtor)) {
						return fn;
					}
				}
			}
			// 14.5.4 Friends with qualified ids allow for instantiation
			if (!data.forExplicitFunctionInstantiation()
					&& !(isFriend && templateID.getParent() instanceof ICPPASTQualifiedName)) {
				return null;
			}
		}

		// Try to instantiate a template
		IASTTranslationUnit tu= data.getTranslationUnit();
		ICPPTemplateArgument[] tmplArgs= ICPPTemplateArgument.EMPTY_ARGUMENTS;
		if (templateID instanceof ICPPASTTemplateId) {
			tmplArgs = CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) templateID);
		}

		ICPPFunctionTemplate bestTemplate= null;
		ICPPFunction bestInst= null;
		boolean isAmbiguous= false;
		for (ICPPFunction fn : fns) {
			if (fn instanceof ICPPFunctionTemplate
					&& !(fn instanceof IProblemBinding) && !(fn instanceof ICPPUnknownBinding)) {
				// If the declared function type is dependent, there is no point trying to use it
				// to instantiate the template, so return a deferred function instead.
				// Note that CPPTemplates.instantiateForFunctionCall() behaves similarly.
				if (CPPTemplates.isDependentType(ft)) {
					return CPPDeferredFunction.createForCandidates(fns);
				}
				ICPPFunctionTemplate template= (ICPPFunctionTemplate) fn;
				ICPPFunction inst= CPPTemplates.instantiateForFunctionDeclaration(template, tmplArgs, ft, data.getLookupPoint());
				if (inst != null) {
					int cmp= CPPTemplates.orderFunctionTemplates(bestTemplate, template, TypeSelection.PARAMETERS_AND_RETURN_TYPE, data.getLookupPoint());
					if (cmp == 0)
						cmp= compareByRelevance(tu, bestTemplate, template);

					if (cmp == 0)
						isAmbiguous= true;

					if (cmp < 0) {
						isAmbiguous= false;
						bestTemplate= template;
						bestInst= inst;
					}
				}
			}
		}
		if (isAmbiguous)
			return new ProblemBinding(data.getLookupName(), data.getLookupPoint(),
					IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, fns);

		return bestInst;
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

	private static FunctionCost costForFunctionCall(ICPPFunction fn, boolean allowUDC, LookupData data)
			throws DOMException {
		IType[] argTypes = data.getFunctionArgumentTypes();
		ValueCategory[] isLValue= data.getFunctionArgumentValueCategories();
		int skipArg= 0;
	    final ICPPFunctionType ftype= fn.getType();
	    if (ftype == null)
	    	return null;

		IType implicitParameterType= null;
		IType impliedObjectType= null;
		final IType[] paramTypes= ftype.getParameterTypes();
		if (fn instanceof ICPPMethod && !(fn instanceof ICPPConstructor)) {
		    implicitParameterType = getImplicitParameterType((ICPPMethod) fn);
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
			result= new FunctionCost(fn, sourceLen, data.getLookupPoint());
		} else {
			result= new FunctionCost(fn, sourceLen + 1, data.getLookupPoint());

			ValueCategory sourceIsLValue= LVALUE;
			if (impliedObjectType == null) {
				impliedObjectType= data.getImpliedObjectType();
			}
			if (fn instanceof ICPPMethod &&
					(((ICPPMethod) fn).isDestructor() || ASTInternal.isStatic(fn, false))) {
			    // 13.3.1-4 for static member functions, the implicit object parameter always matches, no cost
			    cost = new Cost(impliedObjectType, implicitParameterType, Rank.IDENTITY);
			    cost.setImpliedObject();
			} else if (impliedObjectType == null) {
				return null;
			} else if (impliedObjectType.isSameType(implicitParameterType)) {
				cost = new Cost(impliedObjectType, implicitParameterType, Rank.IDENTITY);
				cost.setImpliedObject();
			} else {
				cost = Conversions.checkImplicitConversionSequence(implicitParameterType, impliedObjectType, sourceIsLValue, UDCMode.FORBIDDEN, Context.IMPLICIT_OBJECT, data.getLookupPoint());
				if (cost.converts()) {
					cost.setImpliedObject();
				} else {
				    if (CPPTemplates.isDependentType(implicitParameterType) || CPPTemplates.isDependentType(impliedObjectType)) {
				    	IType s= getNestedType(impliedObjectType, TDEF|REF|CVTYPE);
				    	IType t= getNestedType(implicitParameterType, TDEF|REF|CVTYPE);
				    	if (SemanticUtil.calculateInheritanceDepth(s, t, data.getLookupPoint()) >= 0)
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
			final IType argType= SemanticUtil.getNestedType(argTypes[j + skipArg], TDEF | REF);
			if (argType == null)
				return null;

			final ValueCategory sourceIsLValue = isLValue[j + skipArg];

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
				cost= ((FunctionSetType) argType).costForTarget(paramType, data.getLookupPoint());
			} else if (argType.isSameType(paramType)) {
				cost = new Cost(argType, paramType, Rank.IDENTITY);
			} else {
			    if (CPPTemplates.isDependentType(paramType))
			    	return CONTAINS_DEPENDENT_TYPES;

			    Context ctx= Context.ORDINARY;
			    if (j == 0 && sourceLen == 1 && fn instanceof ICPPConstructor) {
			    	if (paramType instanceof ICPPReferenceType) {
			    		if (((ICPPConstructor) fn).getClassOwner().isSameType(getNestedType(paramType, TDEF|REF|CVTYPE))) {
			    			ctx= Context.FIRST_PARAM_OF_DIRECT_COPY_CTOR;
			    			result.setIsDirectInitWithCopyCtor(true);
			    		}
			    	}
			    }
				cost = Conversions.checkImplicitConversionSequence(paramType, argType, sourceIsLValue,
						udc, ctx, data.getLookupPoint());
				if (data.fNoNarrowing && cost.isNarrowingConversion(data.getLookupPoint())) {
					cost= Cost.NO_CONVERSION;
				}
			}
			if (!cost.converts())
				return null;

			result.setCost(k++, cost, sourceIsLValue);
		}
		return result;
	}

	static IType getImplicitParameterType(ICPPMethod m)	throws DOMException {
		IType implicitType;
		ICPPClassType owner= m.getClassOwner();
		if (owner instanceof ICPPClassTemplate) {
			owner= (ICPPClassType) ((ICPPClassTemplate) owner).asDeferredInstance();
		}
		ICPPFunctionType ft= m.getType();
		implicitType= SemanticUtil.addQualifiers(owner, ft.isConst(), ft.isVolatile(), false);
		return new CPPReferenceType(implicitType, false);
	}

	private static IBinding resolveUserDefinedConversion(LookupData data, ICPPFunction[] fns) {
		ICPPASTConversionName astName= (ICPPASTConversionName) data.getLookupName();
		IType t= CPPVisitor.createType(astName.getTypeId());
		if (t instanceof ISemanticProblem) {
			return new ProblemBinding(astName, data.getLookupPoint(),
					IProblemBinding.SEMANTIC_INVALID_TYPE, data.getFoundBindings());
		}
		if (data.forDeclaration() == null ||
				data.forExplicitFunctionSpecialization() || data.forExplicitFunctionInstantiation()) {
			fns= CPPTemplates.instantiateConversionTemplates(fns, t, data.getLookupPoint());
		}

		IFunction unknown= null;
		for (IFunction function : fns) {
			if (function != null) {
				IType t2= function.getType().getReturnType();
				if (t.isSameType(t2))
					return function;
				if (unknown == null && function instanceof ICPPUnknownBinding) {
					unknown= function;
				}
			}
		}
		if (unknown != null)
			return unknown;
		return new ProblemBinding(astName, data.getLookupPoint(),
				IProblemBinding.SEMANTIC_NAME_NOT_FOUND, data.getFoundBindings());
	}

	/**
	 * 13.4-1 A use of an overloaded function without arguments is resolved in certain contexts to
	 * a function.
     */
    static IBinding resolveTargetedFunction(IASTName name, CPPFunctionSet functionSet) {
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
    		final IASTNode grandpa = parent.getParent();
			if (grandpa instanceof IASTDeclarator) {
    			IASTDeclarator dtor = ASTQueries.findInnermostDeclarator((IASTDeclarator) grandpa);
    			IBinding var= dtor.getName().resolvePreBinding();
    			if (var instanceof IVariable)
    				targetType= ((IVariable) var).getType();
    		}
    	} else if (prop == ICPPASTConstructorInitializer.ARGUMENT) {
    		ICPPASTConstructorInitializer init = (ICPPASTConstructorInitializer) parent;
			final IASTNode parentOfInit = init.getParent();
			if (parentOfInit instanceof IASTDeclarator) {
				IASTDeclarator dtor = (IASTDeclarator) parentOfInit;
				targetType= CPPVisitor.createType(dtor);
			} else if (parentOfInit instanceof ICPPASTConstructorChainInitializer) {
				ICPPASTConstructorChainInitializer memInit= (ICPPASTConstructorChainInitializer) parentOfInit;
				IBinding var= memInit.getMemberInitializerId().resolveBinding();
				if (var instanceof IVariable) {
					targetType= ((IVariable) var).getType();
				}
			}
			targetType= getNestedType(targetType, TDEF | REF | CVTYPE | PTR | MPTR);
    		if (init.getArguments().length != 1 || !(targetType instanceof ICPPFunctionType)) {
    			if (targetType instanceof ICPPClassType) {
    				LookupData data= new LookupData(name);
    				data.setFunctionArguments(false, init.getArguments());
    				try {
    					IBinding ctor = CPPSemantics.resolveFunction(data,
    							ClassTypeHelper.getConstructors((ICPPClassType) targetType, name), true);
    					if (ctor instanceof ICPPConstructor) {
    						int i= 0;
    						for (IASTNode arg : init.getArguments()) {
    							if (arg == node) {
    								IType[] params= ((ICPPConstructor) ctor).getType().getParameterTypes();
    								if (params.length > i) {
    									targetType= params[i];
    								}
    								break;
    							}
    							i++;
    						}
    					}
    				} catch (DOMException e) {
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
    		IType t= SemanticUtil.getNestedType(fnCall.getFunctionNameExpression().getExpressionType(), TDEF | REF | CVTYPE);
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
				ICPPTemplateParameter[] ps = ((ICPPTemplateDefinition) template).getTemplateParameters();
				if (i < args.length && i < ps.length && ps[i] instanceof ICPPTemplateNonTypeParameter) {
					targetType= ((ICPPTemplateNonTypeParameter) ps[i]).getType();
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
    				IFunctionType ft = ((IFunction) binding).getType();
					targetType= ft.getReturnType();
    			}
    		}
		}
    	if (targetType == null && parent instanceof ICPPASTExpression
    			&& parent instanceof IASTImplicitNameOwner) {
			// Trigger resolution of overloaded operator, which may resolve the
			// function set.
			((IASTImplicitNameOwner) parent).getImplicitNames();
			final IBinding newBinding = name.getPreBinding();
			if (!(newBinding instanceof CPPFunctionSet))
				return newBinding;

			// If we're in a dependent context, we don't have enough information
			// to resolve the function set.
			if (((ICPPASTExpression) parent).getEvaluation().isTypeDependent()) {
				return CPPDeferredFunction.createForCandidates(functionSet.getBindings());
			}
		}

    	ICPPFunction function = resolveTargetedFunction(targetType, functionSet, name);
    	if (function == null)
    		return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_OVERLOAD);

    	return function;
    }

	static ICPPFunction resolveTargetedFunction(IType targetType, CPPFunctionSet set, IASTNode point) {
		targetType= getNestedType(targetType, TDEF | REF | CVTYPE | PTR | MPTR);
    	if (!(targetType instanceof ICPPFunctionType))
    		return null;

    	// First pass, consider functions
    	ICPPFunction[] fns= set.getBindings();
    	for (ICPPFunction fn : fns) {
    		if (!(fn instanceof ICPPFunctionTemplate)) {
				if (targetType.isSameType(fn.getType()))
					return fn;
			}
    	}

    	// Second pass, consider templates
    	ICPPFunction result= null;
    	ICPPFunctionTemplate resultTemplate= null;
    	boolean isAmbiguous= false;
    	final IASTTranslationUnit tu= point.getTranslationUnit();
    	for (IFunction fn : fns) {
    		try {
    			if (fn instanceof ICPPFunctionTemplate) {
    				final ICPPFunctionTemplate template = (ICPPFunctionTemplate) fn;
					ICPPFunction inst= CPPTemplates.instantiateForAddressOfFunction(template,
							(ICPPFunctionType) targetType, set.getTemplateArguments(), point);
    				if (inst != null) {
    					int cmp= CPPTemplates.orderFunctionTemplates(resultTemplate, template,
    							TypeSelection.PARAMETERS_AND_RETURN_TYPE, point);
    					if (cmp == 0)
    						cmp= compareByRelevance(tu, resultTemplate, template);

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

    public static ICPPFunction findOverloadedBinaryOperator(IASTNode pointOfInstantiation, IScope pointOfDefinition,
    		OverloadableOperator op, ICPPEvaluation arg1, ICPPEvaluation arg2) {
		if (op == null || arg1 == null || arg2 == null)
			return null;

		IType op1type = getNestedType(arg1.getTypeOrFunctionSet(pointOfInstantiation), TDEF | REF | CVTYPE);
		if (!isUserDefined(op1type) && !isUserDefined(
				getNestedType(arg2.getTypeOrFunctionSet(pointOfInstantiation), TDEF | REF | CVTYPE)))
			return null;

		final LookupMode lookupNonMember;
		if (op == OverloadableOperator.ASSIGN || op == OverloadableOperator.BRACKET) {
			lookupNonMember = LookupMode.NO_GLOBALS;
		} else {
			lookupNonMember= LookupMode.LIMITED_GLOBALS;
		}
		return findOverloadedOperator(pointOfInstantiation, pointOfDefinition, new ICPPEvaluation[] {arg1, arg2},
				op1type, op, lookupNonMember);
    }

    public static ICPPFunction findOverloadedOperator(ICPPASTNewExpression expr) {
		OverloadableOperator op = OverloadableOperator.fromNewExpression(expr);
		final ICPPEvaluation evaluation = expr.getEvaluation();
		if (evaluation.isTypeDependent())
			return null;

		final IASTInitializerClause[] placement = expr.getPlacementArguments();
		final ICPPEvaluation arg1= new EvalUnary(IASTUnaryExpression.op_star, evaluation, null, expr);
		final ICPPEvaluation arg2= new EvalUnary(IASTUnaryExpression.op_sizeof, evaluation, null, expr);

		ICPPEvaluation[] args;
		if (placement == null) {
    		args= new ICPPEvaluation[] { arg1, arg2 };
    	} else {
    		args= new ICPPEvaluation[2 + placement.length];
    		args[0]= arg1;
    		args[1]= arg2;
    		int i= 2;
    		for (IASTInitializerClause p : placement) {
    			final ICPPASTInitializerClause arg = (ICPPASTInitializerClause) p;
				final ICPPEvaluation a = arg.getEvaluation();
				if (a.isTypeDependent())
					return null;
				args[i++]= a;
    		}
    	}
		IType type= getNestedType(arg1.getTypeOrFunctionSet(expr), TDEF | REF | CVTYPE);
		return findOverloadedOperator(expr, null, args, type, op, LookupMode.GLOBALS_IF_NO_MEMBERS);
    }

    public static ICPPFunction findOverloadedOperator(ICPPASTDeleteExpression expr) {
    	OverloadableOperator op = OverloadableOperator.fromDeleteExpression(expr);
    	IType type = getTypeOfPointer(expr.getOperand().getExpressionType());
    	if (type == null)
    		return null;

    	ICPPEvaluation[] args = {
    			new EvalFixed(type, LVALUE, Value.UNKNOWN),
    			((ICPPASTExpression) expr.getOperand()).getEvaluation()
    		};
		return findOverloadedOperator(expr, null, args, type, op, LookupMode.GLOBALS_IF_NO_MEMBERS);
    }

    private static IType getTypeOfPointer(IType type) {
    	type = SemanticUtil.getNestedType(type, SemanticUtil.TDEF | SemanticUtil.REF | SemanticUtil.CVTYPE);
    	if (type instanceof IPointerType) {
    		return getNestedType(((IPointerType) type).getType(), TDEF | REF | CVTYPE);
    	}
		return null;
    }

    /**
     * Returns constructor called by a declarator, or {@code null} if no constructor is called.
     */
    public static IBinding findImplicitlyCalledConstructor(final ICPPASTDeclarator declarator) {
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
     * Returns {@code null} if no constructor is called.
     */
    public static IBinding findImplicitlyCalledConstructor(ICPPASTConstructorChainInitializer initializer) {
    	return findImplicitlyCalledConstructor(initializer.getMemberInitializerId(), initializer.getInitializer());
    }

    /**
     * Returns constructor called by a variable declarator or an initializer in a constructor
     * initializer chain. Returns {@code null} if no constructor is called.
     */
    private static IBinding findImplicitlyCalledConstructor(IASTName name, IASTInitializer initializer) {
    	IBinding binding = name.resolveBinding();
    	if (!(binding instanceof ICPPVariable))
    		return null;

    	IType type = ((ICPPVariable) binding).getType();
		type = SemanticUtil.getNestedType(type, TDEF | CVTYPE);
    	if (!(type instanceof ICPPClassType))
    		return null;
    	if (type instanceof ICPPClassTemplate || type instanceof ICPPUnknownType || type instanceof ISemanticProblem)
    		return null;

    	return findImplicitlyCalledConstructor((ICPPClassType) type, initializer, name);
    }

	public static IBinding findImplicitlyCalledConstructor(ICPPASTNewExpression expr) {
		IType type = getNestedType(expr.getExpressionType(), TDEF | REF | CVTYPE);
		if (!(type instanceof IPointerType))
			return null;
		type = ((IPointerType) type).getType();
		if (type instanceof ICPPClassType) {
			return findImplicitlyCalledConstructor((ICPPClassType) type,
					expr.getInitializer(), expr.getTypeId());
		}
		return null;
	}

    private static IBinding findImplicitlyCalledConstructor(ICPPClassType type, IASTInitializer initializer,
    		IASTNode typeId) {
    	try {
	    	if (initializer instanceof IASTEqualsInitializer) {
		    	// Copy initialization.
	    		IASTEqualsInitializer eqInit= (IASTEqualsInitializer) initializer;
	    		ICPPASTInitializerClause initClause = (ICPPASTInitializerClause) eqInit.getInitializerClause();
	    		final ICPPEvaluation evaluation = initClause.getEvaluation();
	    		IType sourceType= evaluation.getTypeOrFunctionSet(typeId);
				ValueCategory isLValue= evaluation.getValueCategory(typeId);
	    		if (sourceType != null) {
	    			Cost c;
	    			if (calculateInheritanceDepth(sourceType, type, typeId) >= 0) {
	    				c = Conversions.copyInitializationOfClass(isLValue, sourceType, type, false, typeId);
	    			} else {
	    				c = Conversions.checkImplicitConversionSequence(type, sourceType, isLValue, UDCMode.ALLOWED, Context.ORDINARY, typeId);
	    			}
	    			if (c.converts()) {
						ICPPFunction f = c.getUserDefinedConversion();
						if (f instanceof ICPPConstructor)
							return f;
						// If a conversion is used, the constructor is elided.
	    			}
	    		}
	    	} else if (initializer instanceof ICPPASTInitializerList) {
	    		// List initialization.
	    		ICPPEvaluation eval= ((ICPPASTInitializerList) initializer).getEvaluation();
	    		if (eval instanceof EvalInitList) {
	    			Cost c= Conversions.listInitializationSequence((EvalInitList) eval, type, UDCMode.ALLOWED, true, typeId);
	    			if (c.converts()) {
	    				ICPPFunction f = c.getUserDefinedConversion();
	    				if (f instanceof ICPPConstructor)
	    					return f;
	    			}
	    		}
	    	} else if (initializer instanceof ICPPASTConstructorInitializer) {
		    	// Direct initialization.
			    return findImplicitlyCalledConstructor(type,
			    		(ICPPASTConstructorInitializer) initializer, typeId);
	    	} else if (initializer == null) {
	    		// Default initialization.
	    		ICPPConstructor[] ctors = ClassTypeHelper.getConstructors(type, typeId);
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

	private static IBinding findImplicitlyCalledConstructor(ICPPClassType classType,
			ICPPASTConstructorInitializer initializer, IASTNode typeId) {
		final IASTInitializerClause[] arguments = initializer.getArguments();
		CPPASTName astName = new CPPASTName();
		astName.setName(classType.getNameCharArray());
		astName.setOffsetAndLength((ASTNode) typeId);
		CPPASTIdExpression idExp = new CPPASTIdExpression(astName);
		idExp.setParent(typeId.getParent());
		idExp.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

		LookupData data = new LookupData(astName);
		data.setFunctionArguments(false, arguments);
		data.qualified = true;
		data.foundItems = ClassTypeHelper.getConstructors(classType, typeId);
		try {
			return resolveAmbiguities(data);
		} catch (DOMException e) {
			return null;
		}
	}

    public static ICPPFunction findImplicitlyCalledDestructor(ICPPASTDeleteExpression expr) {
    	IType t = getTypeOfPointer(expr.getOperand().getExpressionType());
    	if (!(t instanceof ICPPClassType))
    		return null;

    	ICPPClassType cls = (ICPPClassType) t;
    	IScope scope = cls.getCompositeScope();
		if (scope == null)
			return null;

	    final char[] name = CharArrayUtils.concat("~".toCharArray(), cls.getNameCharArray()); //$NON-NLS-1$
	    LookupData data = new LookupData(name, null, expr);
	    data.qualified = true;
	    data.setFunctionArguments(true, new EvalFixed(cls, LVALUE, Value.UNKNOWN));
	    try {
		    lookup(data, scope);
		    IBinding[] found= data.getFoundBindings();
		    if (found.length > 0 && found[0] instanceof ICPPFunction) {
		    	return (ICPPFunction) found[0];
		    }
		} catch (DOMException e) {
		}
		return null;
    }

	public static ICPPASTExpression createArgForType(IASTNode node, final IType type) {
		CPPASTName x= new CPPASTName();
		x.setBinding(new CPPVariable(x) {
			@Override public IType getType() {
				return type;
			}
		});
		final CPPASTIdExpression idExpression = new CPPASTIdExpression(x);
		idExpression.setParent(node);
		return idExpression;
	}



    /**
     * For simplicity returns an operator of form RT (T, T) rather than RT (boolean, T, T)
     */
    public static ICPPFunction findOverloadedConditionalOperator(IASTNode pointOfInstantiation, IScope pointOfDefinition,
    		ICPPEvaluation positive, ICPPEvaluation negative) {
		final ICPPEvaluation[] args = new ICPPEvaluation[] {positive, negative};
		return findOverloadedOperator(pointOfInstantiation, pointOfDefinition, args, null,
				OverloadableOperator.CONDITIONAL_OPERATOR, LookupMode.NO_GLOBALS);
    }

    /**
     * Returns the operator,() function that would apply to the two given arguments.
     * The lookup type of the class where the operator,() might be found must also be provided.
     */
    public static ICPPFunction findOverloadedOperatorComma(IASTNode pointOfInstantiation, IScope pointOfDefinition,
    		ICPPEvaluation arg1, ICPPEvaluation arg2) {
		IType op1type = getNestedType(arg1.getTypeOrFunctionSet(pointOfInstantiation), TDEF | REF | CVTYPE);
		IType op2type = getNestedType(arg2.getTypeOrFunctionSet(pointOfInstantiation), TDEF | REF | CVTYPE);
		if (!isUserDefined(op1type) && !isUserDefined(op2type))
			return null;

		ICPPEvaluation[] args = { arg1 , arg2 };
    	return findOverloadedOperator(pointOfInstantiation, pointOfDefinition, args, op1type,
    			OverloadableOperator.COMMA, LookupMode.LIMITED_GLOBALS);
    }


    static enum LookupMode {NO_GLOBALS, GLOBALS_IF_NO_MEMBERS, LIMITED_GLOBALS, ALL_GLOBALS}
    static ICPPFunction findOverloadedOperator(IASTNode pointOfInstantiation, IScope pointOfDefinition,
    		ICPPEvaluation[] args, IType methodLookupType, OverloadableOperator operator, LookupMode mode) {
    	while (pointOfInstantiation instanceof IASTName)
    		pointOfInstantiation= pointOfInstantiation.getParent();

    	ICPPClassType callToObjectOfClassType= null;
		IType type2= null;
		if (args.length >= 2) {
			type2 = args[1].getTypeOrFunctionSet(pointOfInstantiation);
			type2= getNestedType(type2, TDEF | REF | CVTYPE);
		}

    	// Find a method
    	LookupData methodData = null;
    	if (methodLookupType instanceof ISemanticProblem)
    		return null;
    	if (methodLookupType instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) methodLookupType;
    	    methodData = new LookupData(operator.toCharArray(), null, pointOfInstantiation);
    	    methodData.setFunctionArguments(true, args);
    	    methodData.qualified = true; // (13.3.1.2.3)

			try {
				IScope scope = classType.getCompositeScope();
				if (scope == null)
					return null;
				lookup(methodData, scope);

				if (operator == OverloadableOperator.PAREN) {
					callToObjectOfClassType= classType;
				}
			} catch (DOMException e) {
				return null;
			}
    	}

		// Find a function
    	LookupData funcData = new LookupData(operator.toCharArray(), null, pointOfInstantiation);

    	// Global new and delete operators do not take an argument for the this pointer.
    	switch (operator) {
    	case DELETE: case DELETE_ARRAY:
    	case NEW: case NEW_ARRAY:
    		args= ArrayUtil.removeFirst(args);
    		break;
    	default:
    		break;
    	}
    	funcData.setFunctionArguments(true, args);
    	funcData.ignoreMembers = true; // (13.3.1.2.3)
    	boolean haveMembers= methodData != null && methodData.hasResults();
    	if (mode == LookupMode.ALL_GLOBALS || mode == LookupMode.LIMITED_GLOBALS
    			|| (mode == LookupMode.GLOBALS_IF_NO_MEMBERS && !haveMembers)) {
			try {
				IScope scope = CPPVisitor.getContainingScope(pointOfInstantiation);
				if (scope == null)
					return null;
				lookup(funcData, scope);
				try {
					doKoenigLookup(funcData);
				} catch (DOMException e) {
				}

				// Also do a lookup at the point of definition.
				if (pointOfDefinition != null) {
					LookupData funcData2 = new LookupData(operator.toCharArray(), null, pointOfInstantiation);
					funcData2.setFunctionArguments(true, args);
					funcData2.ignoreMembers = true;
					lookup(funcData2, pointOfDefinition);
					if (funcData2.hasResults()) {
						mergeResults(funcData, funcData2.foundItems, false);
					}
				}

				// Filter with file-set
				IASTTranslationUnit tu= pointOfInstantiation.getTranslationUnit();
				if (tu != null && funcData.foundItems instanceof Object[]) {
					final IIndexFileSet fileSet = tu.getIndexFileSet();
					if (fileSet != null) {
						int j= 0;
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
			} catch (DOMException e) {
				return null;
			}

			if (operator == OverloadableOperator.NEW || operator == OverloadableOperator.DELETE
		    	|| operator == OverloadableOperator.NEW_ARRAY || operator == OverloadableOperator.DELETE_ARRAY) {

				// Those operators replace the built-in operator
				Object[] items= (Object[]) funcData.foundItems;
				int j= 0;
				for (Object object : items) {
					if (object instanceof ICPPFunction) {
						ICPPFunction func= (ICPPFunction) object;
						if (!(func instanceof CPPImplicitFunction))
							items[j++]= func;
					}
				}
				if (j > 0) {
					while (j < items.length)
						items[j++]= null;
				}
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
							ICPPFunctionType ft = func.getType();
							IType[] pts= ft.getParameterTypes();
							if ((enum1 != null && pts.length > 0 && enum1.isSameType(getUltimateTypeUptoPointers(pts[0]))) ||
									(enum2 != null && pts.length > 1 && enum2.isSameType(getUltimateTypeUptoPointers(pts[1])))) {
								items[j++]= object;
							}
						}
					}
					while (j < items.length) {
						items[j++]= null;
					}
				}
			}
    	}

    	if (callToObjectOfClassType != null) {
    		try {
    			// 13.3.1.1.2 call to object of class type
    			ICPPMethod[] ops = SemanticUtil.getConversionOperators(callToObjectOfClassType, pointOfInstantiation);
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
    							IBinding sf= createSurrogateCallFunction(pointOfInstantiation.getTranslationUnit().getScope(), ft2.getReturnType(), rt, ft2.getParameterTypes());
    							mergeResults(funcData, sf, false);
    						}
    					}
    				}
    			}
    		} catch (DOMException e) {
    			return null;
    		}
    	}

    	if (methodLookupType instanceof ICPPClassType || type2 instanceof ICPPClassType) {
    		ICPPFunction[] builtins= BuiltinOperators.create(operator, args, pointOfInstantiation, (Object[]) funcData.foundItems);
    		mergeResults(funcData, builtins, false);
    	}

    	try {
    		IBinding binding = null;
    		if (methodData != null && funcData.hasResults()) {
    			// if there was two lookups then merge the results
    			mergeResults(funcData, methodData.foundItems, false);
    			binding = resolveAmbiguities(funcData);
    		} else if (funcData.hasResults()) {
    			binding = resolveAmbiguities(funcData);
    		} else if (methodData != null) {
    			binding = resolveAmbiguities(methodData);
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

	static boolean isUserDefined(IType type) {
		if (type instanceof ISemanticProblem)
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
		LookupData data;
		if (beforeNode == null) {
			data= new LookupData(name, null, ASTInternal.getPhysicalNodeOfScope(scope));
			data.setIgnorePointOfDeclaration(true);
		} else {
			data= new LookupData(name, null, beforeNode);
			data.setIgnorePointOfDeclaration(false);
		}
		data.qualified = qualified;
		return standardLookup(data, scope);
	}

	public static IBinding[] findBindingsForContentAssist(IASTName name, boolean prefixLookup,
			String[] additionalNamespaces) {
		LookupData data = createLookupData(name);
		data.contentAssist = true;
		data.setPrefixLookup(prefixLookup);
		data.foundItems = new CharArrayObjectMap<Object>(2);

		// Convert namespaces to scopes.
		List<ICPPScope> nsScopes= new ArrayList<ICPPScope>();
		IASTTranslationUnit tu = name.getTranslationUnit();
		if (additionalNamespaces != null && tu instanceof CPPASTTranslationUnit) {
			for (String nsName : additionalNamespaces) {
				nsName= nsName.trim();
				if (nsName.startsWith("::")) { //$NON-NLS-1$
					nsName= nsName.substring(2);
				}
				String[] namespaceParts = nsName.split("::"); //$NON-NLS-1$
				try {
					ICPPScope nsScope = getNamespaceScope((CPPASTTranslationUnit) tu, namespaceParts, name);
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

	private static IScope getLookupScope(IASTNode node) {
		if (node == null)
			return null;

		if (node instanceof IASTCompositeTypeSpecifier)
			return ((IASTCompositeTypeSpecifier) node).getScope();

		if (node instanceof ICPPASTNamespaceDefinition)
			return ((ICPPASTNamespaceDefinition) node).getScope();

		if (!(node instanceof ICPPInternalBinding))
			return null;

		IASTNode defn = ((ICPPInternalBinding) node).getDefinition();
		if (defn == null)
			return null;

		return getLookupScope(defn.getParent());
	}

	private static IScope getLookupScope(IBinding binding) {
		if (binding == null)
			return null;

		if (binding instanceof IASTCompositeTypeSpecifier)
			return ((IASTCompositeTypeSpecifier) binding).getScope();

		if (!(binding instanceof ICPPInternalBinding))
			return null;

		IASTNode defn = ((ICPPInternalBinding) binding).getDefinition();
		if (defn == null)
			return null;

		return getLookupScope(defn.getParent());
	}

	/**
	 * Uses C++ lookup semantics to find the possible bindings for the given qualified name starting
	 * in the given scope.
	 */
	public static IBinding[] findBindingsForQualifiedName(IScope scope, String qualifiedName) {
		// Return immediately if the qualifiedName does not match a known format.
		Matcher m = QUALNAME_REGEX.matcher(qualifiedName);
		if (!m.matches())
			return IBinding.EMPTY_BINDING_ARRAY;

		// If the qualified name is rooted in the global namespace, then navigate to that scope.
		boolean isGlobal = m.group(1) != null;
		if (isGlobal) {
			IScope global = scope;
			try {
				while (global.getParent() != null) {
					global = global.getParent();
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
			scope = global;
		}

		Set<IBinding> bindings = new HashSet<IBinding>();

		// Look for the name in the given scope.
		findBindingsForQualifiedName(scope, qualifiedName, bindings);

		// If the qualified name is not rooted in the global namespace (with a leading ::), then
		// look at all parent scopes.
		if (!isGlobal) {
			try {
				while (scope != null) {
					scope = scope.getParent();
					if (scope != null)
						findBindingsForQualifiedName(scope, qualifiedName, bindings);
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}

		return bindings.size() == 0 ? IBinding.EMPTY_BINDING_ARRAY : bindings.toArray(new IBinding[bindings.size()]);
	}

	private static void findBindingsForQualifiedName(IScope scope, String qualifiedName, Collection<IBinding> bindings) {
		// Split the qualified name into the first part (before the first :: qualifier) and the rest. All
		// bindings for the first part are found and their scope is used to find the rest of the name.  When
		// the call tree gets to a leaf (non-qualified name) then a simple lookup happens and all matching
		// bindings are added to the result.
		Matcher m = QUALNAME_REGEX.matcher(qualifiedName);
		if (!m.matches())
			return;

		String part1 = m.group(2);
		String part2 = m.group(3);

		// When we're down to a single component name, then use the normal lookup method.
		if (part2 == null || part2.isEmpty()) {
			bindings.addAll(Arrays.asList(findBindings(scope, part1, false)));
			return;
		}

		// Find all bindings that match the first part of the name.  For each such binding,
		// lookup the second part of the name.
		for (IBinding binding : CPPSemantics.findBindings(scope, part1, false)) {
			findBindingsForQualifiedName(getLookupScope(binding), part2, bindings);
		}
	}

	private static ICPPScope getNamespaceScope(CPPASTTranslationUnit tu, String[] namespaceParts, IASTNode point)
			throws DOMException {
		ICPPScope nsScope= tu.getScope();
		outer: for (String nsPart : namespaceParts) {
			nsPart= nsPart.trim();
			if (nsPart.length() != 0) {
				IBinding[] nsBindings = nsScope.getBindings(new ScopeLookupData(nsPart.toCharArray(), point));
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
        		data.qualified = true;
            	for (ICPPScope nsScope : additionalNamespaces) {
        			if (!data.visited.containsKey(nsScope)) {
        				lookup(data, nsScope);
        			}
				}
    		}
        } catch (DOMException e) {
        }
        @SuppressWarnings("unchecked")
		CharArrayObjectMap<Object> map = (CharArrayObjectMap<Object>) data.foundItems;
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
				if (!(function instanceof ICPPTemplateInstance))
					return false;
				if (!((ICPPTemplateInstance) function).isExplicitSpecialization())
					return false;
			} else {
				if (function instanceof ICPPTemplateDefinition) {
					final ICPPTemplateDefinition funcTemplate = (ICPPTemplateDefinition) function;
					if (!isSameTemplateParameterList(funcTemplate.getTemplateParameters(), templateDecl.getTemplateParameters())) {
						return false;
					}
				} else {
					return false;
				}
			}
		} else if (function instanceof ICPPTemplateDefinition) {
			return false;
		}

		declarator= ASTQueries.findTypeRelevantDeclarator(declarator);
		if (declarator instanceof ICPPASTFunctionDeclarator) {
			IType type = function.getType();
			return type.isSameType(CPPVisitor.createType(declarator));
		}
		return false;
	}

	private static boolean isSameTemplateParameterList(ICPPTemplateParameter[] tplist, ICPPASTTemplateParameter[] tps) {
		if (tplist.length != tps.length)
			return false;

		for (int i = 0; i < tps.length; i++) {
			if (!isSameTemplateParameter(tplist[i], tps[i]))
				return false;
		}
		return true;
	}

	static boolean isSameTemplateParameter(ICPPTemplateParameter tp1, ICPPASTTemplateParameter tp2) {
		if (tp1.isParameterPack() != tp2.isParameterPack())
			return false;

		if (tp1 instanceof ICPPTemplateNonTypeParameter) {
			if (tp2 instanceof ICPPASTParameterDeclaration) {
				IType t1= ((ICPPTemplateNonTypeParameter) tp1).getType();
				IType t2= CPPVisitor.createType((ICPPASTParameterDeclaration) tp2, true);
				return t1 != null && t1.isSameType(t2);
			}
			return false;
		}
		if (tp1 instanceof ICPPTemplateTypeParameter) {
			if (tp2 instanceof ICPPASTSimpleTypeTemplateParameter) {
				return true;
			}
			return false;
		}
		if (tp1 instanceof ICPPTemplateTemplateParameter) {
			if (tp2 instanceof ICPPASTTemplatedTypeTemplateParameter) {
				final ICPPTemplateTemplateParameter ttp1 = (ICPPTemplateTemplateParameter) tp1;
				final ICPPASTTemplatedTypeTemplateParameter ttp2 = (ICPPASTTemplatedTypeTemplateParameter) tp2;
				return isSameTemplateParameterList(ttp1.getTemplateParameters(), ttp2.getTemplateParameters());
			}
			return false;
		}

		return false;
	}

	protected static IBinding resolveUnknownName(IScope scope, ICPPUnknownBinding unknown, IASTNode point) {
		final char[] unknownName = unknown.getNameCharArray();
		LookupData data = new LookupData(unknownName, null, point);
		data.setIgnorePointOfDeclaration(true);
		data.typesOnly= unknown instanceof IType;
		data.qualified= true;

		try {
            // 2: Lookup
            lookup(data, scope);
        } catch (DOMException e) {
            data.problem = (ProblemBinding) e.getProblem();
        }

		if (data.problem != null)
		    return data.problem;

		// 3: Resolve ambiguities
		IBinding binding;
        try {
            binding = resolveAmbiguities(data);
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        // 4: Normal post processing is not possible, because the name is not rooted in AST
        if (binding == null)
			binding = new ProblemBinding(new CPPASTName(unknownName), point, IProblemBinding.SEMANTIC_NAME_NOT_FOUND);

		return binding;
	}
}
