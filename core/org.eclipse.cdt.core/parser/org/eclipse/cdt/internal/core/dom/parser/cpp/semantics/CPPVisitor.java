/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructorTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumeration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPLambdaExpressionParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethodTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * Collection of methods to extract information from a C++ translation unit.
 */
public class CPPVisitor extends ASTQueries {
	private static final CPPBasicType UNSIGNED_LONG = new CPPBasicType(Kind.eInt, IBasicType.IS_LONG | IBasicType.IS_UNSIGNED);
	private static final CPPBasicType INT_TYPE = new CPPBasicType(Kind.eInt, 0);

	public static final String BEGIN_STR = "begin"; //$NON-NLS-1$
	public static final char[] BEGIN = BEGIN_STR.toCharArray(); 
	public static final char[] END = "end".toCharArray();  //$NON-NLS-1$
	static final String STD = "std"; //$NON-NLS-1$
	private static final char[] SIZE_T = "size_t".toCharArray(); //$NON-NLS-1$
	private static final char[] PTRDIFF_T = "ptrdiff_t".toCharArray(); //$NON-NLS-1$
	private static final char[] TYPE_INFO= "type_info".toCharArray(); //$NON-NLS-1$
	private static final char[] INITIALIZER_LIST = "initializer_list".toCharArray(); //$NON-NLS-1$
	private static final char[][] EMPTY_CHAR_ARRAY_ARRAY = {};
	public static final IASTInitializerClause[] NO_ARGS = {};

	// Thread-local set of DeclSpecifiers for which auto types are being created.
	// Used to prevent infinite recursion while processing invalid self-referencing
	// auto-type declarations.
	private static final ThreadLocal<Set<IASTDeclSpecifier>> autoTypeDeclSpecs =
			new ThreadLocal<Set<IASTDeclSpecifier>>() {
		@Override
		protected Set<IASTDeclSpecifier> initialValue() {
			return new HashSet<IASTDeclSpecifier>();
		}
	};
	
	public static IBinding createBinding(IASTName name) {
		IASTNode parent = name.getParent();
		IBinding binding = null;
		if (parent instanceof IASTNamedTypeSpecifier ||
				parent instanceof ICPPASTBaseSpecifier ||
				parent instanceof ICPPASTConstructorChainInitializer ||
				name.getPropertyInParent() == ICPPASTNamespaceAlias.MAPPING_NAME) {
		    if (name.getLookupKey().length == 0)
		    	return null;
		  
			return CPPSemantics.resolveBinding(name); 
		} else if (parent instanceof ICPPASTQualifiedName) {
		    if (name.getLookupKey().length == 0)
		    	return null;

			final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) parent;
			if (name != qname.getLastName())
				return CPPSemantics.resolveBinding(name);
			
			parent = parent.getParent();
			if (!declaresMemberInClassOrNamespace(qname)) {
				binding = CPPSemantics.resolveBinding(name);
				if (parent instanceof IASTCompositeTypeSpecifier) {
					if (binding instanceof IIndexBinding) {
						// Need to create an AST binding
					} else {
						ASTInternal.addDefinition(binding, parent);
						return binding;
					}
				} else {
					return binding;
				}
			}
		} else if (parent instanceof ICPPASTTemplateId) {
			final ICPPASTTemplateId id = (ICPPASTTemplateId) parent;
			if (CPPTemplates.isClassTemplate(id))
				return CPPSemantics.resolveBinding(name); 
			
			// function templates/instances/specializations must be resolved via the id
			id.resolveBinding();
			return name.getBinding();
		}
		if (parent instanceof IASTIdExpression) {
			return resolveBinding(parent);
		} else if (parent instanceof ICPPASTFieldReference) {
			return resolveBinding(parent);
		} else if (parent instanceof ICPPASTCompositeTypeSpecifier) {
			return createBinding((ICPPASTCompositeTypeSpecifier) parent);
		} else if (parent instanceof IASTDeclarator) {
			return createBinding((IASTDeclarator) parent);
		} else if (parent instanceof ICPPASTElaboratedTypeSpecifier) {
			return createBinding((ICPPASTElaboratedTypeSpecifier) parent);
		} else if (parent instanceof IASTDeclaration) {
			return createBinding((IASTDeclaration) parent);
		} else if (parent instanceof ICPPASTEnumerationSpecifier) {
		    return createBinding((ICPPASTEnumerationSpecifier) parent);
		} else if (parent instanceof IASTEnumerator) {
		    return createBinding((IASTEnumerator) parent);
		} else if (parent instanceof IASTGotoStatement) {
		    return createBinding((IASTGotoStatement) parent);
		} else if (parent instanceof IASTLabelStatement) {
		    return createBinding((IASTLabelStatement) parent);
		} else if (parent instanceof ICPPASTTemplateParameter) {
			return CPPTemplates.createBinding((ICPPASTTemplateParameter) parent);
		}
		
		if (name.getLookupKey().length > 0)
			return binding;
		return null;
	}
	
	private static boolean declaresMemberInClassOrNamespace(ICPPASTQualifiedName qname) {
		IASTName[] names= qname.getNames();
		if (names.length < 2)
			return false;
		
		IBinding pb= names[names.length-2].resolvePreBinding();
		if (pb instanceof IProblemBinding) 
			return false;
		
		IScope scope= null;
		if (pb instanceof IType) {
			IType t= SemanticUtil.getNestedType((IType) pb, TDEF);
			if (t instanceof ICPPClassType) {
				scope= ((ICPPClassType) t).getCompositeScope();
			}
		} else if (pb instanceof ICPPNamespace) {
			scope= ((ICPPNamespace)pb).getNamespaceScope();
		} 
		if (scope == null)
			return false;

		IASTNode parent= qname.getParent();
		IASTNode decl= null;
		if (parent instanceof IASTCompositeTypeSpecifier) {
			decl= parent.getParent();
		} else if (parent instanceof IASTDeclarator) {
			decl= ASTQueries.findOutermostDeclarator((IASTDeclarator) parent).getParent();
		}
		while (decl != null) {
			ASTNodeProperty prop = decl.getPropertyInParent();
			if (prop == IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
				return scope == ((ICPPASTCompositeTypeSpecifier) decl.getParent()).getScope();
			} 
			if (prop == ICPPASTNamespaceDefinition.OWNED_DECLARATION) {
				return scope == ((ICPPASTNamespaceDefinition) decl.getParent()).getScope();
			} 
			
			if (prop == ICPPASTTemplateDeclaration.OWNED_DECLARATION) { 
				decl= decl.getParent();
			} else {
				return false;
			}			
		} 
		return false;
	}

	private static IBinding createBinding(IASTGotoStatement gotoStatement) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope(gotoStatement.getName());
	    IASTName name = gotoStatement.getName();
	    IBinding binding;
        binding = functionScope.getBinding(name, false);
		if (binding == null || !(binding instanceof ILabel)) {
		    binding = new CPPLabel(name);
		    ASTInternal.addName(functionScope,  name);
		}
        
	    return binding;
	}
	
	private static IBinding createBinding(IASTLabelStatement labelStatement) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope(labelStatement.getName());
	    IASTName name = labelStatement.getName();
	    IBinding binding;
        binding = functionScope.getBinding(name, false);
		if (binding == null || !(binding instanceof ILabel)) {
		    binding = new CPPLabel(name);
		    ASTInternal.addName(functionScope,  name);
		} else {
		    ((CPPLabel) binding).setLabelStatement(name);
		}
        
	    return binding;
	}
	
    private static IBinding createBinding(IASTEnumerator enumerator) {
        ICPPScope scope = (ICPPScope) getContainingScope(enumerator);
        IBinding enumtor;
        enumtor = scope.getBinding(enumerator.getName(), false);
		if (enumtor == null || !(enumtor instanceof IEnumerator)) {
		    enumtor = new CPPEnumerator(enumerator.getName());
		}
        
        return enumtor;
    }

    private static IBinding createBinding(ICPPASTEnumerationSpecifier specifier) {
        ICPPScope scope = (ICPPScope) getContainingScope(specifier);
        final IASTName name = specifier.getName();
		IType fixedType= createEnumBaseType(specifier);
		IBinding binding = scope.getBinding(name, false);
		if (binding instanceof CPPEnumeration) {
			CPPEnumeration e= (CPPEnumeration) binding;
			if (e.isScoped() == specifier.isScoped()) {
				IType ft2= e.getFixedType();
				if (fixedType == ft2 || (fixedType != null && fixedType.isSameType(ft2))) {
					if (specifier.isOpaque()) {
						e.addDeclaration(name);
					} else if (e.getDefinition() == null) {
						e.addDefinition(name);
					} else {
						return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
					}
					return e;
				}
			}
			return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
		} 
		return new CPPEnumeration(specifier, fixedType);
    }

	private static IType createEnumBaseType(ICPPASTEnumerationSpecifier specifier) {
		ICPPASTDeclSpecifier declspec = specifier.getBaseType();
		if (declspec != null) {
			IType type= createType(declspec);
			return SemanticUtil.getNestedType(type, ALLCVQ);
		}
		if (specifier.isScoped()) {
			return CPPSemantics.INT_TYPE;
		}
		return null;
	}

	private static IBinding createBinding(final ICPPASTElaboratedTypeSpecifier elabType) {
	    final IASTNode parent = elabType.getParent();
	    IBinding binding = null;
	    boolean mustBeSimple = true;
	    boolean isFriend = false;
	    boolean qualified = false;
	    IASTName name = elabType.getName();
	    if (name instanceof ICPPASTQualifiedName) {
	        qualified = true;
	        IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
	        name = ns[ns.length - 1];
	    }
	    if (parent instanceof IASTSimpleDeclaration) {
	        IASTDeclarator[] dtors = ((IASTSimpleDeclaration) parent).getDeclarators();
	        ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration) parent).getDeclSpecifier();
	        isFriend = declSpec.isFriend() && dtors.length == 0;
	        if (dtors.length > 0 || isFriend) {
	        	binding = CPPSemantics.resolveBinding(name);
	        	mustBeSimple = !isFriend;
	        } else {
	        	mustBeSimple = false;
	        }
	    } else if (parent instanceof IASTParameterDeclaration || 
	    		   parent instanceof IASTDeclaration ||
				   parent instanceof IASTTypeId) {
	    	binding = CPPSemantics.resolveBinding(elabType.getName());
	    }
	    if (binding instanceof IIndexBinding && binding instanceof ICPPClassType) {
	    	binding= ((CPPASTTranslationUnit) elabType.getTranslationUnit()).mapToAST((ICPPClassType) binding);
	    	ASTInternal.addDeclaration(binding, elabType);
	    }
	    
		if (binding != null && 
				(!(binding instanceof IProblemBinding) ||
				((IProblemBinding) binding).getID() != IProblemBinding.SEMANTIC_NAME_NOT_FOUND))	{
			return binding;
    	}
		
		// 7.1.5.3-2 ... If name lookup does not find a declaration for the name, the elaborated-type-specifier is ill-formed
		// unless it is of the simple form class-key identifier
	    if (mustBeSimple && 
	    		(elabType.getName() instanceof ICPPASTQualifiedName || elabType.getKind() == IASTElaboratedTypeSpecifier.k_enum)) {
	    	return binding;
	    }
	    
        try {
        	ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);
        	ICPPScope scope = (ICPPScope) getContainingScope(name);
        	while (scope instanceof ICPPTemplateScope) {
        		scope= (ICPPScope) scope.getParent();
        	}
		
        	if (mustBeSimple) {
        		// 3.3.1-5 ... the identifier is declared in the smallest non-class non-function-prototype scope that contains
        		// the declaration
        		while (scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope) {
        			scope = (ICPPScope) getParentScope(scope, elabType.getTranslationUnit());
        		}
        	}
        	if (scope instanceof ICPPClassScope && isFriend && !qualified) {
        		while (scope instanceof ICPPClassScope)
        			scope = (ICPPScope) getParentScope(scope, elabType.getTranslationUnit());
        	}
        	if (scope != null) {
        		binding = scope.getBinding(elabType.getName(), false);
    			if (binding instanceof ICPPUsingDeclaration) {
    				IBinding[] expanded = ((ICPPUsingDeclaration) binding).getDelegates();
    				if (expanded.length == 1 && expanded[0] instanceof IType) {
    					binding= expanded[0];
    				}
    			}
        	}
        	if (binding instanceof ICPPInternalBinding) {
        		if (!name.isActive())
        			return binding;
        		
        		if (binding instanceof ICPPClassType) {
        			final ICPPInternalBinding ib = (ICPPInternalBinding) binding;
        			if (templateParametersMatch((ICPPClassType) binding, templateDecl)) {
        				ib.addDeclaration(elabType);
        				return binding;
        			}

        			if (CPPSemantics.declaredBefore(ib, name, false)) {
        				return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
        			}
        			markRedeclaration(ib);
        		} 
        	}
        	
        	// Create a binding
        	if (elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum) {
        		if (templateDecl != null)
        			binding = new CPPClassTemplate(name);
        		else
        			binding = new CPPClassType(name, binding);
        		// name may live in a different scope, so make sure to add it to the owner scope, as well.
        		ASTInternal.addName(scope,  elabType.getName());
    		}
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        
		return binding;
	}

	public static void markRedeclaration(final ICPPInternalBinding ib) {
		// Mark the other declarations as problem and create the binding
		final IASTNode[] decls = ib.getDeclarations();
		if (decls != null) {
			for (IASTNode decl : decls) {
				if (decl instanceof IASTName) {
					final IASTName n = (IASTName) decl;
					n.setBinding(new ProblemBinding(n, IProblemBinding.SEMANTIC_INVALID_REDECLARATION));
				}
			}
		}
		IASTNode decl= ib.getDefinition();
		if (decl instanceof IASTName) {
			final IASTName n = (IASTName) decl;
			n.setBinding(new ProblemBinding(n, IProblemBinding.SEMANTIC_INVALID_REDEFINITION));
		}
	}

	/**
	 * Tests whether a class binding matches the template parameters of another declaration
	 */
	private static boolean templateParametersMatch(ICPPClassType binding,
			ICPPASTTemplateDeclaration templateDecl) {
		final boolean isTemplate= binding instanceof ICPPClassTemplate;
		if (templateDecl == null)
			return !isTemplate;
		if (!isTemplate)
			return false;
		
		ICPPTemplateParameter[] pars1 = ((ICPPClassTemplate) binding).getTemplateParameters();
		ICPPASTTemplateParameter[] pars2 = templateDecl.getTemplateParameters();
		
		int i=0;
		for (ICPPASTTemplateParameter p2 : pars2) {
			if (i >= pars1.length)
				return true;

			if (!CPPSemantics.isSameTemplateParameter(pars1[i++], p2))
				return false;
		}
		return true;
	}

	private static IBinding createBinding(ICPPASTCompositeTypeSpecifier compType) {
		IASTName name = compType.getName();
		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
		if (name instanceof ICPPASTTemplateId) 
			return CPPTemplates.createBinding((ICPPASTTemplateId) name);
		
    	ICPPScope scope = (ICPPScope) getContainingScope(name);
		try {
        	while (scope instanceof ICPPTemplateScope) {
        		scope= (ICPPScope) scope.getParent();
        	}
        } catch (DOMException e) {
            return e.getProblem();
        }
		
    		// Can't lookup anonymous names
		IBinding binding= null;
		ICPPASTTemplateDeclaration templateDecl = CPPTemplates.getTemplateDeclaration(name);
		if (name.getLookupKey().length > 0 && scope != null) { 
			binding = scope.getBinding(name, false);

			if (binding instanceof ICPPInternalBinding 
					&& binding instanceof ICPPClassType && name.isActive()) {
				ICPPInternalBinding ib = (ICPPInternalBinding) binding;
				if (ib.getDefinition() == null 
						&& templateParametersMatch((ICPPClassType) binding, templateDecl)) {
					ASTInternal.addDefinition(ib, compType);
					return binding;
				} 
				if (CPPSemantics.declaredBefore(ib, name, false)) {
					return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
				}
				markRedeclaration(ib);
			}
		}
		if (templateDecl != null) 
			return new CPPClassTemplate(name);

		return  new CPPClassType(name, binding);
	}
	
	private static IBinding createBinding(IASTDeclaration declaration) {
		if (declaration instanceof ICPPASTNamespaceDefinition) {
			ICPPASTNamespaceDefinition namespaceDef = (ICPPASTNamespaceDefinition) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope(namespaceDef);
			IBinding binding;
            binding = scope.getBinding(namespaceDef.getName(), false);
			if (!(binding instanceof ICPPInternalBinding) || binding instanceof IProblemBinding 
					|| !(binding instanceof ICPPNamespace)) {
				binding = new CPPNamespace(namespaceDef);
			}
			return binding;
		} else if (declaration instanceof ICPPASTUsingDirective) {
			return CPPSemantics.resolveBinding(((ICPPASTUsingDirective) declaration).getQualifiedName());
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
		    ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
		    ICPPScope scope = (ICPPScope) getContainingScope(declaration);
		    IBinding binding;
		    binding = scope.getBinding(alias.getAlias(), false);
			if (!(binding instanceof ICPPInternalBinding)) {
			    IBinding namespace = alias.getMappingName().resolveBinding();
			    if (namespace instanceof IProblemBinding) {
			    	IProblemBinding problem = (IProblemBinding) namespace;
			    	namespace = new CPPNamespace.CPPNamespaceProblem(problem.getASTNode(),
			    			problem.getID(), alias.getMappingName().toCharArray());
			    }
			    if (namespace instanceof ICPPNamespace) { 
			        binding = new CPPNamespaceAlias(alias.getAlias(), (ICPPNamespace) namespace);
			    } else {
			        binding = new ProblemBinding(alias.getAlias(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
			    }
			}
			
			return binding;
		}

		return null;
	}
	
	private static IBinding createBinding(IASTDeclarator declarator) {
		IASTNode parent = findOutermostDeclarator(declarator).getParent();
		declarator= findInnermostDeclarator(declarator);

		final IASTDeclarator typeRelevantDtor= findTypeRelevantDeclarator(declarator);
			
		IASTName name= declarator.getName();
		if (name instanceof ICPPASTQualifiedName) {
			name= ((ICPPASTQualifiedName) name).getLastName();
		}
		
		// in case the binding was created starting from another name within the declarator.
		IBinding candidate= name.getBinding();
		if (candidate != null) {
			return candidate;
		}
		
		// function type
		if (parent instanceof IASTTypeId)
		    return CPPSemantics.resolveBinding(name);

		// function type for non-type template parameter
		ASTNodeProperty prop = parent.getPropertyInParent();
		if (prop == ICPPASTTemplateDeclaration.PARAMETER || prop == ICPPASTTemplatedTypeTemplateParameter.PARAMETER) {
			return CPPTemplates.createBinding((ICPPASTTemplateParameter) parent);
		}

		// explicit instantiations
		if (prop == ICPPASTExplicitTemplateInstantiation.OWNED_DECLARATION) 
			return CPPSemantics.resolveBinding(name);
		
		// explicit specializations
		ICPPASTTemplateDeclaration tmplDecl= CPPTemplates.getTemplateDeclaration(name);
		if (tmplDecl instanceof ICPPASTTemplateSpecialization) {
			IBinding b= CPPSemantics.resolveBinding(name);
			if (parent instanceof ICPPASTFunctionDefinition) {
				ASTInternal.addDefinition(b, name);
			} else { 
				ASTInternal.addDeclaration(b, name);
			}
			return b;
		} 

		// parameter declarations
        if (parent instanceof ICPPASTParameterDeclaration) {
			ICPPASTParameterDeclaration param = (ICPPASTParameterDeclaration) parent;
			parent = param.getParent();
			if (parent instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) param.getParent();
				// Create parameter bindings only if the declarator declares a function 
				if (findTypeRelevantDeclarator(fdtor) != fdtor)
					return null;

				final IASTNode dtorParent= findOutermostDeclarator(fdtor).getParent();
				if (dtorParent instanceof ICPPASTLambdaExpression) {
					return new CPPLambdaExpressionParameter(name);
				}
				
				if (dtorParent instanceof IASTDeclaration) {
					IASTParameterDeclaration[] params = fdtor.getParameters();
					int i= 0;
					for (; i < params.length; i++) {
						if (params[i] == param)
							break;
					}
					return new CPPParameter(name, i);
				}
				return null;
			} else if (parent instanceof ICPPASTTemplateDeclaration) {
				return CPPTemplates.createBinding(param);
			}
			return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_TYPE);
        }

		// function declaration/definition
		IBinding binding= null;
		final boolean template= tmplDecl != null;
		boolean isFriendDecl= false;
		ICPPScope scope = (ICPPScope) getContainingNonTemplateScope(name);
		if (scope instanceof ICPPClassScope) {
			isFriendDecl = isFriendDeclaration(parent);
			if (isFriendDecl) {
				try {
					while (scope.getKind() == EScopeKind.eClassType) {
						scope = (ICPPScope) getParentScope(scope, name.getTranslationUnit());
					}
				} catch (DOMException e1) {
				}
			}
		}
		boolean forceResolve= isFriendDecl && name instanceof ICPPASTTemplateId;
		if (name.getLookupKey().length != 0 && scope != null) {
			binding = scope.getBinding(name, forceResolve);
		}
        
        boolean isFunction= false;
        if (parent instanceof ICPPASTFunctionDefinition) {
        	isFunction= true;
        } else if (parent instanceof IASTSimpleDeclaration) {
        	IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;
        	if (simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef) {
        		// Typedef declaration
        		if (binding instanceof ICPPInternalBinding && binding instanceof ITypedef && name.isActive()) {
        			IType t1 = ((ITypedef) binding).getType();
        			IType t2 = createType(declarator);
        			if (t1 != null && t2 != null && t1.isSameType(t2)) {
        				ASTInternal.addDeclaration(binding, name);
        				return binding;
        			}
        			return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
        		}
        		// If we don't resolve the target type first, we get a problem binding in case the typedef
        		// redeclares the target type, otherwise it is safer to defer the resolution of the target type.
        		IType targetType= createType(declarator);
        		CPPTypedef td= new CPPTypedef(name);
        		td.setType(targetType);
        		binding = td;
        	} else if (typeRelevantDtor instanceof IASTFunctionDeclarator) {
        		// Function declaration via function declarator
    			isFunction= true;
    		} else {
        		// Looks like a variable declaration
        	    IType t1 = createType(declarator);
        	    if (SemanticUtil.getNestedType(t1, TDEF) instanceof IFunctionType) {
        	    	// Function declaration via a typedef for a function type
        	    	isFunction= true;
        	    } else if (binding instanceof IParameter) {
        	    	// Variable declaration redeclaring a parameter
        	    	binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
        	    } else {
        	    	// Variable declaration
        	    	IType t2= null;
        	    	if (binding != null && binding instanceof IVariable && !(binding instanceof IIndexBinding)) {
        	    		t2 = ((IVariable) binding).getType();
        	    	}
        	    	if (t1 != null && t2 != null) {
        	    		if (t1.isSameType(t2) || isCompatibleArray(t1, t2) != null) {
        	    			ASTInternal.addDeclaration(binding, name);
        	    		} else {
        	    			binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
        	    		}
        	    	} else if (simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
        	    		binding = new CPPField(name); 
        	    	} else {
        	    		binding = new CPPVariable(name);
        	    	}
        	    }
        	}
		} 
        
        if (isFunction) {
			if (binding instanceof ICPPInternalBinding && binding instanceof ICPPFunction && name.isActive()) {
				ICPPFunction function = (ICPPFunction) binding;
			    if (CPPSemantics.isSameFunction(function, typeRelevantDtor)) {
			    	binding= CPPSemantics.checkDeclSpecifier(binding, name, parent);
			    	if (binding instanceof IProblemBinding)
			    		return binding;

			        ICPPInternalBinding internal = (ICPPInternalBinding) function;
			        if (parent instanceof IASTSimpleDeclaration) {
			            ASTInternal.addDeclaration(internal, name);
			        } else if (internal.getDefinition() == null) {
			        	ASTInternal.addDefinition(internal, name);
			        } else {
		                IASTNode def = internal.getDefinition();
		                if (def instanceof IASTDeclarator)
		                    def = ((IASTDeclarator) def).getName();
		                if (def != name) {
		                    return new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
		                }
		            }
			        
			        return function;
			    }
			} 
						
			if (scope instanceof ICPPClassScope) {
				if (isConstructor(scope, typeRelevantDtor)) {
					binding = template ? (ICPPConstructor)  new CPPConstructorTemplate(name)
									   : new CPPConstructor((ICPPASTFunctionDeclarator) typeRelevantDtor);
				} else {
					binding = template ? (ICPPMethod) new CPPMethodTemplate(name)
							           : new CPPMethod(typeRelevantDtor);
				}
			} else {
				binding = template ? (ICPPFunction) new CPPFunctionTemplate(name)
								   : new CPPFunction(typeRelevantDtor);
			}
			binding= CPPSemantics.checkDeclSpecifier(binding, name, parent);
        }
        
		return binding;
	}

	public static boolean isFriendDeclaration(IASTNode decl) {
		IASTDeclSpecifier declSpec;
		if (decl instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
		} else if (decl instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) decl).getDeclSpecifier();
		} else {
			return false;
		}
		return declSpec instanceof ICPPASTDeclSpecifier && ((ICPPASTDeclSpecifier) declSpec).isFriend();
	}

	public static boolean isConstructor(IScope containingScope, IASTDeclarator declarator) {
		if (containingScope instanceof ICPPClassScope && isConstructorDtor(declarator)) {
			ICPPClassType classType= ((ICPPClassScope) containingScope).getClassType();
		    final char[] dtorName = findInnermostDeclarator(declarator).getName().getLookupKey();
			return CharArrayUtils.equals(dtorName, classType.getNameCharArray());
		}
		return false;
	}

	public static boolean isConstructorDeclaration(IASTName name) {
		if (name == null)
			return false;
		final ASTNodeProperty propertyInParent = name.getPropertyInParent();
		if (propertyInParent == CPPSemantics.STRING_LOOKUP_PROPERTY || propertyInParent == null)
			return false;
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTTemplateId) {
			name= (IASTName) parent;
			parent= name.getParent();
		}
		if (parent instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) parent).getLastName() != name)
				return false;
			name= (IASTName) parent;
			parent= name.getParent();
		}
		if (parent instanceof IASTDeclarator) {
			if (isConstructorDtor((IASTDeclarator) parent)) {
				if (name instanceof ICPPASTQualifiedName) {
					IASTName[] names = ((ICPPASTQualifiedName) name).getNames();
					if (names.length >= 2) {
						IBinding b= names[names.length-2].resolvePreBinding();
						if (b instanceof IType) {
							IType classType= getNestedType((IType) b, TDEF);
							if (classType instanceof ICPPClassType) {
							    final char[] dtorName = names[names.length-1].getLookupKey();
								final char[] className = ((ICPPClassType) classType).getNameCharArray();
								return CharArrayUtils.equals(dtorName, className);
							}
						}
					}
					return false;
				}			
				while (parent != null) {
					if (parent instanceof ICPPASTCompositeTypeSpecifier) {
						final char[] className= ((ICPPASTCompositeTypeSpecifier) parent).getName().getLastName().getLookupKey();
					    final char[] dtorName = name.getLookupKey();
						return CharArrayUtils.equals(dtorName, className);
					}
					parent= parent.getParent();
				}
			}
		}
		return false;
	}
	
	private static boolean isConstructorDtor(IASTDeclarator declarator) {
		if (declarator == null || !(declarator instanceof IASTFunctionDeclarator))
			return false;
        
	    IASTDeclSpecifier declSpec = null;
	    IASTNode parent = findOutermostDeclarator(declarator).getParent();
	    if (parent instanceof IASTSimpleDeclaration) {
	        declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
	    } else if (parent instanceof IASTFunctionDefinition) {
	        declSpec = ((IASTFunctionDefinition) parent).getDeclSpecifier();
	    }
	    if (declSpec != null && declSpec instanceof IASTSimpleDeclSpecifier) {
	        return (((IASTSimpleDeclSpecifier) declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified); 
	    }
	    
	    return false;
	}
	
	public static IScope getContainingNonTemplateScope(final IASTNode inputNode) {
		IScope scope= getContainingScope(inputNode);
		while (scope instanceof ICPPTemplateScope) {
			scope= CPPVisitor.getContainingScope(((ICPPTemplateScope) scope).getTemplateDeclaration());
		}
		return scope;
	}
	
	public static IScope getContainingScope(final IASTNode inputNode) {
		if (inputNode == null || inputNode instanceof IASTTranslationUnit)
			return null;
		IASTNode node= inputNode;
		while (node != null) {
		    if (node instanceof IASTName && !(node instanceof ICPPASTQualifiedName)) {
				return getContainingScope((IASTName) node);
			}
		    if (node instanceof IASTDeclaration) {
				IASTNode parent = node.getParent();
				if (parent instanceof IASTTranslationUnit) {
					return ((IASTTranslationUnit) parent).getScope();
				} else if (parent instanceof IASTDeclarationStatement) {
					return getContainingScope((IASTStatement) parent);
				} else if (parent instanceof IASTForStatement) {
				    return ((IASTForStatement) parent).getScope();
				} else if (parent instanceof ICPPASTRangeBasedForStatement) {
				    return ((ICPPASTRangeBasedForStatement) parent).getScope();
				} else if (parent instanceof IASTCompositeTypeSpecifier) {
				    return ((IASTCompositeTypeSpecifier) parent).getScope();
				} else if (parent instanceof ICPPASTNamespaceDefinition) {
					return ((ICPPASTNamespaceDefinition) parent).getScope();
				} else if (parent instanceof ICPPASTSwitchStatement) {
					return ((ICPPASTSwitchStatement) parent).getScope();
				} else if (parent instanceof ICPPASTIfStatement) {
					return ((ICPPASTIfStatement) parent).getScope();
				} else if (parent instanceof ICPPASTWhileStatement) {
					return ((ICPPASTWhileStatement) parent).getScope();
				} else if (parent instanceof ICPPASTTemplateDeclaration) {
					return ((ICPPASTTemplateDeclaration) parent).getScope();
				} else if (parent instanceof ICPPASTCatchHandler) {
					return ((ICPPASTCatchHandler) parent).getScope();
				}
			} else if (node instanceof IASTStatement) {
		        return getContainingScope((IASTStatement) node); 
			} else if (node instanceof IASTTypeId) {
				ASTNodeProperty prop = node.getPropertyInParent();
				if (prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT || prop == ICPPASTConversionName.TYPE_ID) {
					node= node.getParent(); // template-id or conversion name
					while (node instanceof IASTName) { 
						node= node.getParent();
					}
					continue;
				} else if (prop == ICPPASTFunctionDeclarator.TRAILING_RETURN_TYPE) {
					IScope result = scopeViaFunctionDtor((ICPPASTFunctionDeclarator) node.getParent());
					if (result != null)
						return result;

				}
			} else if (node instanceof IASTParameterDeclaration) {
			    IASTNode parent = node.getParent();
			    if (parent instanceof ICPPASTFunctionDeclarator) {
					IScope result = scopeViaFunctionDtor((ICPPASTFunctionDeclarator) parent);
					if (result != null)
						return result;
			    } else if (parent instanceof ICPPASTTemplateDeclaration) {
			    	return ((ICPPASTTemplateDeclaration) parent).getScope();
			    }
			} else if (node instanceof IASTInitializer) {
				if (node instanceof ICPPASTConstructorChainInitializer) {
					// The name of the member initializer is resolved in the scope of the
					// owner of the ctor.
					ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) node;
					IASTFunctionDefinition fdef= (IASTFunctionDefinition) initializer.getParent();
					IBinding binding = fdef.getDeclarator().getName().resolveBinding();
					try {
						return binding.getScope();
					} catch (DOMException e) {
					}
			    } else {	
			    	IASTNode parent = node.getParent();
			    	if (parent instanceof IASTDeclarator) {
			    		IASTDeclarator dtor = (IASTDeclarator) parent;
			    		IASTName name = dtor.getName();
			    		if (name instanceof ICPPASTQualifiedName) {
			    			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			    			return getContainingScope(ns[ns.length - 1]);
			    		}
			    	} else if (parent instanceof ICPPASTConstructorChainInitializer) {
			    		// The initializer for the member initializer is resolved in
			    		// the body of the ctor.
				    	IASTNode temp = getContainingBlockItem(node);
				    	if (temp instanceof IASTFunctionDefinition) {
				    		IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition) temp).getBody();
				    		return body.getScope();
				    	}
				    	node= parent;
			    	}
			    }
		    } else if (node instanceof IASTExpression) {
		    	IASTNode parent = node.getParent();
			    if (parent instanceof IASTForStatement) {
			        return ((IASTForStatement) parent).getScope();
				} else if (parent instanceof ICPPASTRangeBasedForStatement) {
				    return ((ICPPASTRangeBasedForStatement) parent).getScope();
			    } else if (parent instanceof ICPPASTIfStatement) {
			    	return ((ICPPASTIfStatement) parent).getScope();
			    } else if (parent instanceof ICPPASTSwitchStatement) {
			    	return ((ICPPASTSwitchStatement) parent).getScope();
			    } else if (parent instanceof ICPPASTWhileStatement) {
			    	return ((ICPPASTWhileStatement) parent).getScope();
			    } else if (parent instanceof IASTCompoundStatement) {
			        return ((IASTCompoundStatement) parent).getScope();
			    } else if (parent instanceof IASTArrayModifier) {
			        IASTNode d = parent.getParent();
					while (!(d instanceof IASTDeclarator || d instanceof IASTExpression)) {
			            d = d.getParent();
			        }
					if (d instanceof IASTDeclarator) {
						IASTDeclarator dtor = (IASTDeclarator) d;
						while (dtor.getNestedDeclarator() != null)
							dtor = dtor.getNestedDeclarator();
						IASTName name = dtor.getName();
						if (name instanceof ICPPASTQualifiedName) {
							IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
							return getContainingScope(ns[ns.length - 1]);
						}
					}
			    } else if (parent instanceof ICPPASTTemplateId &&
			    		node.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT) {
					node= parent; // template-id
					while (node instanceof IASTName) { 
						node= node.getParent();
					}
					continue;
			    }
		    } else if (node instanceof ICPPASTTemplateParameter) {
		    	if (node instanceof ICPPASTTemplatedTypeTemplateParameter && node != inputNode) {
		    		return ((ICPPASTTemplatedTypeTemplateParameter) node).asScope();
		    	}
		    	IASTNode parent = node.getParent();
		    	if (parent instanceof ICPPASTTemplateDeclaration) {
		    		return ((ICPPASTTemplateDeclaration) parent).getScope();
		    	}
		    } else if (node instanceof ICPPASTBaseSpecifier) {
	    	    ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) node.getParent();
	    	    IASTName n = compSpec.getName();
	    	    if (n instanceof ICPPASTQualifiedName) {
	    	        IASTName[] ns = ((ICPPASTQualifiedName) n).getNames();
	    	        n = ns[ns.length - 1];
	    	    }
	    	    
		        return getContainingScope(n);
		    } else if (node instanceof IASTEnumerator) {
		    	node= node.getParent();
		    	if (node instanceof ICPPASTEnumerationSpecifier) {
		    		ICPPASTEnumerationSpecifier enumSpec= (ICPPASTEnumerationSpecifier) node;
		    		IBinding binding = enumSpec.getName().resolveBinding();
		    		if (binding instanceof ICPPEnumeration) {
		    			ICPPEnumeration enumType = (ICPPEnumeration) binding;
		    			if (enumType.isScoped()) {
		    				return enumType.asScope();
		    			}
		    		}
		    	}
		    }
		    node = node.getParent();
		}
	    return new CPPScope.CPPScopeProblem(inputNode, IProblemBinding.SEMANTIC_BAD_SCOPE, 
	    		inputNode.getRawSignature().toCharArray());
	}

	private static IScope scopeViaFunctionDtor(ICPPASTFunctionDeclarator dtor) {
		if (ASTQueries.findTypeRelevantDeclarator(dtor) == dtor) {
			IASTDeclarator outerDtor = ASTQueries.findOutermostDeclarator(dtor);
			ASTNodeProperty prop = outerDtor.getPropertyInParent();
			if (prop == IASTSimpleDeclaration.DECLARATOR) {
			    return dtor.getFunctionScope();
			}
			if (prop == IASTFunctionDefinition.DECLARATOR) {
			    final IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition) outerDtor.getParent()).getBody();
			    if (body != null)
			    	return body.getScope();
			    return dtor.getFunctionScope();
			} 
			if (prop == ICPPASTLambdaExpression.DECLARATOR) {
			    final IASTCompoundStatement body = ((ICPPASTLambdaExpression) outerDtor.getParent()).getBody();
			    if (body != null)
			    	return body.getScope();
			    return dtor.getFunctionScope();
			}							
		}
		return null;
	}
		
	public static IScope getContainingScope(IASTName name) {
		return getContainingScope(name, null);
	}
	
	public static IScope getContainingScope(IASTName name, LookupData data) {
		IScope scope= getContainingScopeOrNull(name, data);
		if (scope == null) {
			return new CPPScope.CPPScopeProblem(name, IProblemBinding.SEMANTIC_BAD_SCOPE);
		}

		return scope;
	}
	
	private static IScope getContainingScopeOrNull(IASTName name, LookupData data) {
		if (name == null) {
			return null;
		}
		IASTNode parent = name.getParent();
		try {
		    if (parent instanceof ICPPASTTemplateId) {
		        name = (IASTName) parent;
		        parent = name.getParent();
		    }
	            
			if (parent instanceof ICPPASTQualifiedName) {
				final ICPPASTQualifiedName qname= (ICPPASTQualifiedName) parent;
				final IASTName[] names = qname.getNames();
				int i = 0;
				for (; i < names.length; i++) {
					if (names[i] == name) break;
				}
				final IASTTranslationUnit tu = parent.getTranslationUnit();
				if (i == 0) {
					if (qname.isFullyQualified()) {
						if (tu == null)
							return null;
						return tu.getScope();
					} 
					if (qname.getParent() instanceof ICPPASTFieldReference) {
						name= qname;
						parent= name.getParent();
					}
				} else if (i > 0) {
					if (data != null) {
						data.usesEnclosingScope= false;
					}
					// For template functions we may need to resolve a template parameter
					// as a parent of an unknown type used as parameter type.
					IBinding binding = names[i - 1].resolvePreBinding();
					
					// 7.1.3-7 Unwrap typedefs, delete cv-qualifiers.
					if (binding instanceof ITypedef) {
						IType type= getNestedType((ITypedef) binding, TDEF | CVTYPE);
						if (type instanceof IBinding) {
							binding= (IBinding) type;
						}
					}
					boolean done= true;
					IScope scope= null;
					if (binding instanceof ICPPClassType) {
						if (binding instanceof IIndexBinding && tu != null) {
							binding= (((CPPASTTranslationUnit) tu)).mapToAST((ICPPClassType) binding);
						}
						scope= ((ICPPClassType) binding).getCompositeScope();
					} else if (binding instanceof ICPPNamespace) {
						scope= ((ICPPNamespace) binding).getNamespaceScope();
					} else if (binding instanceof ICPPEnumeration) {
						scope= ((ICPPEnumeration) binding).asScope();
					} else if (binding instanceof ICPPUnknownBinding) {
					    scope= ((ICPPUnknownBinding) binding).asScope();
					} else if (binding instanceof IProblemBinding) {
						if (binding instanceof ICPPScope)
							scope= (IScope) binding;
					} else {
						done= false;
					}
					if (done) {
						if (scope == null) {
							return new CPPScope.CPPScopeProblem(names[i - 1], IProblemBinding.SEMANTIC_BAD_SCOPE);
						}
						return scope;
					}
				} 
			} 
			
			if (parent instanceof ICPPASTFieldReference) {
				if (data != null) {
					data.usesEnclosingScope= false;
				}
				final ICPPASTFieldReference fieldReference = (ICPPASTFieldReference) parent;
				IType type = fieldReference.getFieldOwnerType();
				type= getUltimateTypeUptoPointers(type);
				if (type instanceof ICPPClassType) {
					type= SemanticUtil.mapToAST(type, fieldReference);
					return ((ICPPClassType) type).getCompositeScope();
				} else if (type instanceof ICPPUnknownBinding) {
					return ((ICPPUnknownBinding) type).asScope();
				} else {
					// mstodo introduce problem category
					return new CPPScope.CPPScopeProblem(name, ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION); 
				}
			} else if (parent instanceof IASTGotoStatement || parent instanceof IASTLabelStatement) {
			    while (!(parent instanceof IASTFunctionDefinition)) {
			        parent = parent.getParent();
			    }
			    IASTFunctionDefinition fdef = (IASTFunctionDefinition) parent;
			    return ((ICPPASTFunctionDeclarator) fdef.getDeclarator()).getFunctionScope();
			}
		} catch (DOMException e) {
			IProblemBinding problem = e.getProblem();
			if (problem instanceof ICPPScope)
				return problem;
			return new CPPScope.CPPScopeProblem(problem.getASTNode(), problem.getID(), problem.getNameCharArray()); 
		}
		return getContainingScope(parent);
	}

	public static IScope getContainingScope(IASTStatement statement) {
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if (parent instanceof IASTCompoundStatement) {
		    IASTCompoundStatement compound = (IASTCompoundStatement) parent;
		    scope = compound.getScope();
		} else if (parent instanceof IASTForStatement) {
		    scope = ((IASTForStatement) parent).getScope();
		} else if (parent instanceof ICPPASTRangeBasedForStatement) {
		    scope= ((ICPPASTRangeBasedForStatement) parent).getScope();
		} else if (parent instanceof ICPPASTSwitchStatement) {
			scope = ((ICPPASTSwitchStatement) parent).getScope();
		} else if (parent instanceof ICPPASTIfStatement) {
			scope = ((ICPPASTIfStatement) parent).getScope();
		} else if (parent instanceof ICPPASTWhileStatement) {
			scope = ((ICPPASTWhileStatement) parent).getScope();
		} else if (parent instanceof IASTStatement) {
			scope = getContainingScope((IASTStatement) parent);
		} else if (parent instanceof IASTFunctionDefinition) {
		    IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent).getDeclarator();
		    IASTName name = findInnermostDeclarator(fnDeclarator).getName();
		    if (name instanceof ICPPASTQualifiedName) {
		        IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
		        name = ns[ns.length -1];
		    }
		    return getContainingScope(name);
		}
		
		if (scope == null)
			return getContainingScope(parent);
		return scope;
	}
	
	public static IASTNode getContainingBlockItem(IASTNode node) {
	    if (node == null) return null;
	    if (node.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return null;
		IASTNode parent = node.getParent();
		if (parent == null)
		    return null;
		while (parent != null) {
			if (parent instanceof IASTDeclaration) {
				IASTNode p = parent.getParent();
				if (p instanceof IASTDeclarationStatement)
					return p;
				return parent;
			} else if (parent instanceof IASTExpression) {
				IASTNode p = parent.getParent();
				if (p instanceof IASTForStatement)
				    return parent;
				if (p instanceof ICPPASTRangeBasedForStatement)
					return parent;
				if (p instanceof IASTStatement)
					return p;
			} else if (parent instanceof IASTStatement || parent instanceof IASTTranslationUnit) {
				return parent;
			} else if (parent instanceof IASTFunctionDeclarator && node.getPropertyInParent() == IASTStandardFunctionDeclarator.FUNCTION_PARAMETER) {
			    return node;
			} else if (parent instanceof IASTEnumerationSpecifier.IASTEnumerator) {
			    return parent;
			}
			node = parent;
			parent = node.getParent();
		}
		return null;
	}
	
	static private IBinding resolveBinding(IASTNode node) {
		IASTName name = null;
		while (node != null) {
			if (node instanceof IASTIdExpression) {
				name = ((IASTIdExpression) node).getName();
				break;
			} else if (node instanceof ICPPASTFieldReference) {
				name = ((ICPPASTFieldReference) node).getFieldName();
				break;
			} else if (node instanceof IASTFunctionCallExpression) {
				node = ((IASTFunctionCallExpression) node).getFunctionNameExpression();
			} else if (node instanceof IASTUnaryExpression) {
				node = ((IASTUnaryExpression) node).getOperand();
			} else if (node instanceof IASTBinaryExpression) {
				node = ((IASTBinaryExpression) node).getOperand2();
			} else {
				node = null;
			}
		}
		if (name != null) {
			name= name.getLastName();
			IBinding binding = name.getPreBinding();
			if (binding == null) {
				binding = CPPSemantics.resolveBinding(name);
				name.setBinding(binding);
				if (name instanceof ICPPASTTemplateId && binding instanceof ICPPSpecialization) {
					((ICPPASTTemplateId) name).getTemplateName().setBinding(((ICPPSpecialization) binding).getSpecializedBinding());
				}
			}
			return binding;
		}
		return null;
	}

	private static class CollectProblemsAction extends ASTGenericVisitor {
		private List<IASTProblem> fProblems = null;
		
		CollectProblemsAction() {
			super(true);
		}

		private void addProblem(IASTProblem problem) {
			if (fProblems == null) {
				fProblems= new ArrayList<IASTProblem>();
			}
			fProblems.add(problem);
		}
				
		public IASTProblem[] getProblems() {
			if (fProblems == null)
				return new IASTProblem[0];
			
			return fProblems.toArray(new IASTProblem[fProblems.size()]);
		}
	    
		@Override
		public int genericVisit(IASTNode node) {
			if (node instanceof IASTProblemHolder)
				addProblem(((IASTProblemHolder) node).getProblem());

			return PROCESS_CONTINUE;
		}
	}

	public static class CollectDeclarationsAction extends ASTVisitor {
	    private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] decls;
		private IBinding[] bindings;
		private int idx = 0;
		private int kind;
		private char[] requiredName;
		private IIndex index;

		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE = 4;
		private static final int KIND_COMPOSITE = 5;
		private static final int KIND_TEMPLATE_PARAMETER = 6;

		public CollectDeclarationsAction(IBinding binding) {
			shouldVisitTranslationUnit = true;
			shouldVisitNames = true;
			this.decls = new IASTName[DEFAULT_LIST_SIZE];
			
			final String bname= binding.getName();
			if (bname.length() > 0 && !bname.startsWith("operator")) { //$NON-NLS-1$
				requiredName= bname.toCharArray();
			}
			this.bindings = new IBinding[] {binding};
			if (binding instanceof ICPPUsingDeclaration) {
				this.bindings= ((ICPPUsingDeclaration) binding).getDelegates();
				kind= KIND_COMPOSITE;
			} else if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICPPTemplateParameter) {
				kind = KIND_TEMPLATE_PARAMETER;
			} else if (binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else if (binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else if (binding instanceof IParameter) {
				requiredName= null;
				kind = KIND_OBJ_FN;
			} else {
				kind = KIND_OBJ_FN;
			}
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			index = tu.getIndex();
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName) return PROCESS_CONTINUE;
			if (requiredName != null && !CharArrayUtils.equals(name.getLookupKey(), requiredName)) {
				return PROCESS_CONTINUE;
			}
			
			ASTNodeProperty prop = name.getPropertyInParent();
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME)
				prop = name.getParent().getPropertyInParent();
			
			switch (kind) {
				case KIND_TEMPLATE_PARAMETER:
					if (prop == ICPPASTSimpleTypeTemplateParameter.PARAMETER_NAME ||
							prop == ICPPASTTemplatedTypeTemplateParameter.PARAMETER_NAME) {
						break;
					} else if (prop == IASTDeclarator.DECLARATOR_NAME) {
						IASTNode d = name.getParent();
						while (d.getParent() instanceof IASTDeclarator)
							d = d.getParent();
						if (d.getPropertyInParent() == IASTParameterDeclaration.DECLARATOR) {
							break;
						}
					}
					return PROCESS_CONTINUE;

				case KIND_LABEL:
					if (prop == IASTLabelStatement.NAME)
						break;
					return PROCESS_CONTINUE;

				case KIND_TYPE:
				case KIND_COMPOSITE:
				    if (prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
					        prop == IASTEnumerationSpecifier.ENUMERATION_NAME ||
							prop == ICPPASTUsingDeclaration.NAME) {
				        break;
				    } else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME) {
						IASTNode p = name.getParent().getParent();
						if (p instanceof IASTSimpleDeclaration &&
								((IASTSimpleDeclaration) p).getDeclarators().length == 0) {
							break;
						}
					} else if (prop == IASTDeclarator.DECLARATOR_NAME) {
					    IASTNode p = name.getParent();
					    while (p instanceof IASTDeclarator) {
					    	p= p.getParent();
					    }
					    if (p instanceof IASTSimpleDeclaration) {
					        IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) p).getDeclSpecifier();
					        if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef)
					            break;
					    }
					}
        
					if (kind == KIND_TYPE)
					    return PROCESS_CONTINUE;
					// $FALL-THROUGH$

				case KIND_OBJ_FN:
					if (prop == IASTDeclarator.DECLARATOR_NAME ||
						    prop == IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME ||
							prop == ICPPASTUsingDeclaration.NAME) {
						break;
					}
					return PROCESS_CONTINUE;

				case KIND_NAMESPACE:
					if (prop == ICPPASTNamespaceDefinition.NAMESPACE_NAME ||
							prop == ICPPASTNamespaceAlias.ALIAS_NAME) {
						break;
					}					
					return PROCESS_CONTINUE;
			}
			
			if (bindings != null) {
				if (isDeclarationsBinding(name.resolveBinding())) {
					if (decls.length == idx) {
						IASTName[] temp = new IASTName[decls.length * 2];
						System.arraycopy(decls, 0, temp, 0, decls.length);
						decls = temp;
					}
					decls[idx++] = name;
			    }   
			}
			return PROCESS_CONTINUE;
		}

		private boolean isDeclarationsBinding(IBinding nameBinding) {
			if (nameBinding != null) {
				for (IBinding binding : bindings) {
					if (areEquivalentBindings(nameBinding, binding)) {
						return true;
					}
					// A using declaration is a declaration for the references of its delegates
					if (nameBinding instanceof ICPPUsingDeclaration) {
						if (ArrayUtil.contains(((ICPPUsingDeclaration) nameBinding).getDelegates(), binding)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean areEquivalentBindings(IBinding binding1, IBinding binding2) {
			if (binding1.equals(binding2)) {
				return true;
			}
			if ((binding1 instanceof IIndexBinding) != (binding2 instanceof IIndexBinding) &&
					index != null) {
				if (binding1 instanceof IIndexBinding) {
					binding2 = index.adaptBinding(binding2);
				} else {
					binding1 = index.adaptBinding(binding1);
				}
				if (binding1 == null || binding2 == null) {
					return false;
				}
				if (binding1.equals(binding2)) {
					return true;
				}
			}
			return false;
		}

		public IASTName[] getDeclarations() {
			if (idx < decls.length) {
				IASTName[] temp = new IASTName[idx];
				System.arraycopy(decls, 0, temp, 0, idx);
				decls = temp;
			}
			return decls;
		}

	}

	protected static IBinding unwindBinding(IBinding binding) {
		while (true) {
			if (binding instanceof ICPPSpecialization) {
				binding= ((ICPPSpecialization) binding).getSpecializedBinding();
			} else {
				break;
			}
		}
		return binding;
	}

	public static class CollectReferencesAction extends ASTVisitor {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName[] refs;
		private IBinding[] bindings;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE   = 4;
		private static final int KIND_COMPOSITE = 5;

		public CollectReferencesAction(IBinding binding) {
			shouldVisitNames = true;
			this.refs = new IASTName[DEFAULT_LIST_SIZE];

			binding = unwindBinding(binding);
			this.bindings = new IBinding[] {binding};
			
			if (binding instanceof ICPPUsingDeclaration) {
				this.bindings= ((ICPPUsingDeclaration) binding).getDelegates();
				kind= KIND_COMPOSITE;
			} else if (binding instanceof ILabel) {
				kind = KIND_LABEL;
			} else if (binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration) {
				kind = KIND_TYPE;
			} else if (binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else if (binding instanceof ICPPTemplateParameter) {
			    kind = KIND_COMPOSITE;
			} else { 
				kind = KIND_OBJ_FN;
			}
		}
		
		@SuppressWarnings("fallthrough")
		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName || name instanceof ICPPASTTemplateId)
				return PROCESS_CONTINUE;
			
			ASTNodeProperty prop = name.getPropertyInParent();
			ASTNodeProperty p2 = null;
			if (prop == ICPPASTQualifiedName.SEGMENT_NAME) {
			    p2 = prop;
				prop = name.getParent().getPropertyInParent();
			}
			
			switch (kind) {
				case KIND_LABEL:
					if (prop == IASTGotoStatement.NAME)
						break;
					return PROCESS_CONTINUE;
				case KIND_TYPE:
				case KIND_COMPOSITE:
					if (prop == IASTNamedTypeSpecifier.NAME || 
							prop == ICPPASTPointerToMember.NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							prop == ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.NAME ||
							prop == ICPPASTTemplateId.TEMPLATE_NAME ||
							p2 == ICPPASTQualifiedName.SEGMENT_NAME) {
						break;
					} else if (prop == IASTElaboratedTypeSpecifier.TYPE_NAME)	{
						IASTNode p = name.getParent().getParent();
						if (!(p instanceof IASTSimpleDeclaration) ||
							((IASTSimpleDeclaration) p).getDeclarators().length > 0)
						{
							break;
						}
					}
					if (kind == KIND_TYPE)
					    return PROCESS_CONTINUE;
					// fall through

				case KIND_OBJ_FN:
					if (prop == IASTIdExpression.ID_NAME || 
							prop == IASTFieldReference.FIELD_NAME || 
							prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							prop == IASTFunctionCallExpression.FUNCTION_NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							prop == IASTNamedTypeSpecifier.NAME ||
							prop == ICPPASTConstructorChainInitializer.MEMBER_ID ||
							prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT ||
							prop == IASTImplicitNameOwner.IMPLICIT_NAME) {
						break;
					}
					return PROCESS_CONTINUE;
				case KIND_NAMESPACE:
					if (prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
							prop == ICPPASTNamespaceAlias.MAPPING_NAME ||
							prop == ICPPASTUsingDeclaration.NAME ||
							p2 == ICPPASTQualifiedName.SEGMENT_NAME) {
						break;
					}
					return PROCESS_CONTINUE;
			}
			
			if (bindings != null) {
			    if (isReferenceBinding(name.resolveBinding())) {
			    	if (refs.length == idx) {
			    		IASTName[] temp = new IASTName[refs.length * 2];
			    		System.arraycopy(refs, 0, temp, 0, refs.length);
			    		refs = temp;
			    	}
			    	refs[idx++] = name;
			    }
			}
			return PROCESS_CONTINUE;
		}

		private boolean isReferenceBinding(IBinding nameBinding) {
			nameBinding= unwindBinding(nameBinding);
			if (nameBinding != null) {
				for (IBinding binding : bindings) {
					if (nameBinding.equals(binding)) {
						return true;
					}
				}
				if (nameBinding instanceof ICPPUsingDeclaration) {
					IBinding[] delegates= ((ICPPUsingDeclaration) nameBinding).getDelegates();
					for (IBinding delegate : delegates) {
						if (isReferenceBinding(delegate)) {
							return true;
						}
					}
					return false;
				} else {
					return false;
				}
			}
			return false;
		}
		
		public IASTName[] getReferences() {
			if (idx < refs.length) {
				IASTName[] temp = new IASTName[idx];
				System.arraycopy(refs, 0, temp, 0, idx);
				refs = temp;
			}
			return refs;
		}
	}

	/**
	 * Generate a function type for an implicit function.
	 * NOTE: This does not correctly handle parameters with typedef types.
	 */
	public static ICPPFunctionType createImplicitFunctionType(IType returnType, IParameter[] parameters, boolean isConst, boolean isVolatile) {
	    IType[] pTypes = new IType[parameters.length];
	    IType pt = null;
	    
	    for (int i = 0; i < parameters.length; i++) {
	        pt = parameters[i].getType();
         
			// remove qualifiers
			if (pt instanceof IQualifierType) {
				pt= ((IQualifierType) pt).getType();
			}

			if (pt instanceof IArrayType) {
				pt = new CPPPointerType(((IArrayType) pt).getType());
			} else if (pt instanceof IFunctionType) {
				pt = new CPPPointerType(pt);
			}
	        
	        pTypes[i] = pt; 
	    }
	    
	    return new CPPFunctionType(returnType, pTypes, isConst, isVolatile, false);
	}

	/**
	 * Creates the type for the given type id.
	 */
	public static IType createType(IASTTypeId typeid) {
		return createType(typeid.getAbstractDeclarator());
	}

	/**
	 * Creates the type for a parameter declaration.
	 */
	public static IType createType(final ICPPASTParameterDeclaration pdecl, boolean forFuncType) {
		IType pt;
		IASTDeclSpecifier pDeclSpec = pdecl.getDeclSpecifier();
		ICPPASTDeclarator pDtor = pdecl.getDeclarator();
		pt = createType(pDeclSpec);
		if (pDtor != null) {
			pt = createType(pt, pDtor);
		}
		pt=  adjustParameterType(pt, forFuncType);
		
		if (pDtor != null && CPPVisitor.findInnermostDeclarator(pDtor).declaresParameterPack()) {
			pt= new CPPParameterPackType(pt);
		}
		return pt;
	}

	private static IType createType(IType returnType, ICPPASTFunctionDeclarator fnDtor) {
	    IType[] pTypes = createParameterTypes(fnDtor);
	     
	    IASTName name = fnDtor.getName();
		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}
	    if (name instanceof ICPPASTConversionName) {
	    	returnType = createType(((ICPPASTConversionName) name).getTypeId());
	    } else {
	    	returnType = getPointerTypes(returnType, fnDtor);
	    }
	    
	    CPPFunctionType type = new CPPFunctionType(returnType, pTypes, fnDtor.isConst(), fnDtor.isVolatile(),
	    		fnDtor.takesVarArgs());
	    final IASTDeclarator nested = fnDtor.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}

	/**
	 * Creates an array of types for the parameters of the given function declarator.
	 */
	public static IType[] createParameterTypes(ICPPASTFunctionDeclarator fnDtor) {
		ICPPASTParameterDeclaration[] params = fnDtor.getParameters();
	    IType[] pTypes = new IType[params.length];
	    for (int i = 0; i < params.length; i++) {
	        pTypes[i]= createType(params[i], true);
	    }
		return pTypes;
	}

	
	/**
	 * Adjusts the parameter type according to 8.3.5-3:
	 * cv-qualifiers are deleted, arrays and function types are converted to pointers.
	 */
	static IType adjustParameterType(final IType pt, boolean forFunctionType) {
		// bug 239975
		IType t= SemanticUtil.getNestedType(pt, TDEF);
		if (t instanceof IArrayType) {
			IArrayType at = (IArrayType) t;
			return new CPPPointerType(at.getType());
		}
		if (t instanceof IFunctionType) {
			return new CPPPointerType(pt);
		}

		// 8.3.5-3 
		// Any cv-qualifier modifying a parameter type is deleted. The parameter type remains
		// to be qualified.
		if (forFunctionType && SemanticUtil.getCVQualifier(t) != CVQualifier.NONE) {
			return SemanticUtil.getNestedType(t, TDEF | ALLCVQ);
		}
		return pt;
	}
	
	private static IType getPointerTypes(IType type, IASTDeclarator declarator) {
	    IASTPointerOperator[] ptrOps = declarator.getPointerOperators();
		for (IASTPointerOperator ptrOp : ptrOps) {
		    if (ptrOp instanceof ICPPASTPointerToMember) {
				type = new CPPPointerToMemberType(type, (ICPPASTPointerToMember) ptrOp);
		    } else if (ptrOp instanceof IASTPointer) {
		        type = new CPPPointerType(type, (IASTPointer) ptrOp);
		    } else if (ptrOp instanceof ICPPASTReferenceOperator) {
		        final ICPPASTReferenceOperator refOp = (ICPPASTReferenceOperator) ptrOp;
				type = new CPPReferenceType(type, refOp.isRValueReference());
		    }
		}
		return type;
	}

	private static IType getArrayTypes(IType type, IASTArrayDeclarator declarator) {
	    IASTArrayModifier[] mods = declarator.getArrayModifiers();
	    for (int i = mods.length -1; i >= 0; i--) {
	    	IASTArrayModifier mod = mods[i];
	        type = new CPPArrayType(type, mod.getConstantExpression());
	    }
	    return type;
	}
	
	public static IType createType(IASTDeclarator declarator) {
		if (declarator == null) 
			return new ProblemType(ISemanticProblem.TYPE_NO_NAME);
		
		declarator= findOutermostDeclarator(declarator);
		IASTNode parent = declarator.getParent();
		
		IASTDeclSpecifier declSpec = null;
		boolean isPackExpansion= false;
		if (parent instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTParameterDeclaration) {
			declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) parent).getDeclSpecifier();
		} else if (parent instanceof ICPPASTTypeId) {
			final ICPPASTTypeId typeId = (ICPPASTTypeId) parent;
			declSpec = typeId.getDeclSpecifier();
			isPackExpansion= typeId.isPackExpansion();
		} else {
			throw new IllegalArgumentException();
		}

		if (declSpec instanceof ICPPASTSimpleDeclSpecifier &&
				((ICPPASTSimpleDeclSpecifier) declSpec).getType() == IASTSimpleDeclSpecifier.t_auto) {
			return createAutoType(declSpec, declarator);
		}
		
		IType type = createType(declSpec);
		type = createType(type, declarator);

		// C++ specification 8.3.4.3 and 8.5.1.4
		IASTNode initClause= declarator.getInitializer();
		if (initClause instanceof IASTEqualsInitializer) {
			initClause= ((IASTEqualsInitializer) initClause).getInitializerClause();
		}
		if (initClause instanceof IASTInitializerList) {
			IType t= SemanticUtil.getNestedType(type, TDEF);
			if (t instanceof IArrayType) {
				IArrayType at= (IArrayType) t;
				if (at.getSize() == null) {
					type= new CPPArrayType(at.getType(), Value.create(((IASTInitializerList) initClause).getSize()));
				}
			}
		}

		if (isPackExpansion) {
			type= new CPPParameterPackType(type);
		}
		return type;
	}

	private static IType createAutoType(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		if (declarator instanceof ICPPASTFunctionDeclarator) {
			return createAutoFunctionType(declSpec, (ICPPASTFunctionDeclarator) declarator);
		}
		IASTInitializerClause autoInitClause= null;
		IASTNode parent = declarator.getParent().getParent();
		if (parent instanceof ICPPASTNewExpression) {
			IASTInitializer initializer = ((ICPPASTNewExpression) parent).getInitializer();
			if (initializer != null) {
				IASTInitializerClause[] arguments = ((ICPPASTConstructorInitializer) initializer).getArguments();
				if (arguments.length == 1) {
					autoInitClause = arguments[0];
				} 
			}
		} else if (parent instanceof ICPPASTRangeBasedForStatement) {
			// See 6.5.4 The range-based for statement [stmt.ranged]
			ICPPASTRangeBasedForStatement forStmt= (ICPPASTRangeBasedForStatement) parent;
			IASTInitializerClause forInit = forStmt.getInitializerClause();
			IASTExpression beginExpr= null;
			if (forInit instanceof IASTExpression) {
				final IASTExpression expr = (IASTExpression) forInit;
				IType type= SemanticUtil.getNestedType(expr.getExpressionType(), TDEF|CVTYPE);
				if (type instanceof IArrayType) {
					beginExpr= expr.copy();
				} 
			}
			if (beginExpr == null) {
				IASTImplicitName[] implicits= forStmt.getImplicitNames();
				if (implicits.length > 0) {
					IBinding b= implicits[0].getBinding();
					CPPASTName name= new CPPASTName();
					name.setBinding(b);
					if (b instanceof ICPPMethod) {
						beginExpr= new CPPASTFunctionCallExpression(
								new CPPASTFieldReference(name, null), NO_ARGS);
					} else {
						beginExpr= new CPPASTFunctionCallExpression(new CPPASTIdExpression(name), NO_ARGS);
					}
				} else {
					return new ProblemType(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE);
				}
			}
			autoInitClause= new CPPASTUnaryExpression(IASTUnaryExpression.op_star, beginExpr);
			autoInitClause.setParent(forStmt);
			autoInitClause.setPropertyInParent(ICPPASTRangeBasedForStatement.INITIALIZER);
		} else if (parent instanceof IASTCompositeTypeSpecifier &&
				declSpec.getStorageClass() != IASTDeclSpecifier.sc_static) {
			// Non-static auto-typed class members are not allowed.
			return new ProblemType(ISemanticProblem.TYPE_AUTO_FOR_NON_STATIC_FIELD);
		} else {
			IASTInitializer initClause= declarator.getInitializer();
			if (initClause instanceof IASTEqualsInitializer) {
				autoInitClause= ((IASTEqualsInitializer) initClause).getInitializerClause();
			} else if (initClause instanceof IASTInitializerClause) {
				autoInitClause= (IASTInitializerClause) initClause;
			}
		}
		return createAutoType(autoInitClause, declSpec, declarator);
	}

	private static IType createAutoType(IASTInitializerClause initClause, IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		//  C++0x: 7.1.6.4
		if (initClause == null || !autoTypeDeclSpecs.get().add(declSpec)) {
			// Detected a self referring auto type, e.g.: auto x = x;
			return new ProblemType(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE);
		}

		IType type = AutoTypeResolver.AUTO_TYPE;
		IType initType = null;
		ValueCategory valueCat= null;
		ICPPClassTemplate initializer_list_template = null;
		try {
			if (initClause instanceof ICPPASTInitializerList) {
				initializer_list_template = get_initializer_list(declSpec);
				if (initializer_list_template == null) {
					return new ProblemType(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE);
				}
				type = (IType) CPPTemplates.instantiate(initializer_list_template,
						new ICPPTemplateArgument[] { new CPPTemplateArgument(type) });
				if (type instanceof IProblemBinding) {
					return new ProblemType(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE);
				}
			}
			type = decorateType(type, declSpec, declarator);
	
			if (initClause instanceof IASTExpression) {
				final IASTExpression expression = (IASTExpression) initClause;
				initType = expression.getExpressionType();
				valueCat= expression.getValueCategory();
			} else if (initClause instanceof ICPPASTInitializerList) {
				initType = new InitializerListType((ICPPASTInitializerList) initClause);
			}
			if (initType == null) {
				return new ProblemType(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE);
			}
		} finally {
			autoTypeDeclSpecs.get().remove(declSpec);
		}
		ICPPFunctionTemplate template = new AutoTypeResolver(type);
		CPPTemplateParameterMap paramMap = new CPPTemplateParameterMap(1);
		TemplateArgumentDeduction.deduceFromFunctionArgs(template, Collections.singletonList(initType),
				Collections.singletonList(valueCat), paramMap);
		ICPPTemplateArgument argument = paramMap.getArgument(0, 0);
		if (argument == null) {
			return new ProblemType(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE);
		}
		type = argument.getTypeValue();
		if (initClause instanceof ICPPASTInitializerList) {
			type = (IType) CPPTemplates.instantiate(initializer_list_template,
					new ICPPTemplateArgument[] { new CPPTemplateArgument(type) });
		}
		return decorateType(type, declSpec, declarator);
	}

	/**
	 * C++0x: [8.3.5-2]
	 */
	private static IType createAutoFunctionType(IASTDeclSpecifier declSpec, ICPPASTFunctionDeclarator declarator) {
		IASTTypeId id= declarator.getTrailingReturnType();
		if (id == null)
			return new ProblemType(ISemanticProblem.TYPE_NO_NAME);
		
		IType t= createType(id.getAbstractDeclarator());
		t= qualifyType(t, declSpec);
		return createType(t, declarator);
	}

	public static IType createType(IASTDeclSpecifier declSpec) {
	    IType type = getBaseType(declSpec);
		return qualifyType(type, declSpec);
	}

	private static IType getBaseType(IASTDeclSpecifier declSpec) {
	    IASTName name;
	    if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			name = ((ICPPASTCompositeTypeSpecifier) declSpec).getName();
	    } else if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
	    	name = ((ICPPASTNamedTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier) {
			name = ((ICPPASTElaboratedTypeSpecifier) declSpec).getName();
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			name = ((IASTEnumerationSpecifier) declSpec).getName();
		} else if (declSpec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier spec = (ICPPASTSimpleDeclSpecifier) declSpec;
			// Check for decltype(expr)
			IType type = getDeclType(spec);
			if (type != null)
				return type;
			return new CPPBasicType(spec);
		} else {
			throw new IllegalArgumentException();
		}
	    if (name == null)
	    	return new ProblemType(ISemanticProblem.TYPE_NO_NAME);

	    IBinding binding = name.resolvePreBinding();
	    if (!(binding instanceof IProblemBinding)) {
	    	if (binding instanceof ICPPConstructor) 
	    		return ((ICPPConstructor) binding).getClassOwner();

	    	if (binding instanceof IType) 
	    		return (IType) binding;
	    }
    	return new ProblemType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
	}

	private static IType decorateType(IType type, IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		type = qualifyType(type, declSpec);
		return createType(type, declarator);
	}

	private static IType qualifyType(IType type, IASTDeclSpecifier declSpec) {
		return SemanticUtil.addQualifiers(type, declSpec.isConst(), declSpec.isVolatile(), declSpec.isRestrict());
	}

	private static IType createType(IType baseType, IASTDeclarator declarator) {
	    if (declarator instanceof ICPPASTFunctionDeclarator)
	        return createType(baseType, (ICPPASTFunctionDeclarator) declarator);
		
		IType type = baseType;
		type = getPointerTypes(type, declarator);
		if (declarator instanceof IASTArrayDeclarator)
		    type = getArrayTypes(type, (IASTArrayDeclarator) declarator);

	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if (nested != null) {
	    	return createType(type, nested);
	    }
	    return type;
	}

	/**
	 * Compute the type for decltype(expr) or typeof(expr)
	 */
	private static IType getDeclType(ICPPASTSimpleDeclSpecifier spec) {
		IASTExpression expr = spec.getDeclTypeExpression();
		if (expr == null) 
			return null;
		
		if (spec.getType() == IASTSimpleDeclSpecifier.t_decltype) {
			IASTName namedEntity= null;
			if (expr instanceof IASTIdExpression) {
				namedEntity= ((IASTIdExpression) expr).getName();
			} else if (expr instanceof IASTFieldReference) {
				namedEntity= ((IASTFieldReference) expr).getFieldName();
			}
			if (namedEntity != null) {
				IBinding b= namedEntity.resolvePreBinding();
				if (b instanceof IType) {
					return (IType) b;
				}
				if (b instanceof IVariable) {
					return ((IVariable) b).getType();
				}
				if (b instanceof IFunction) {
					return ((IFunction) b).getType();
				}
			}
		}
		IType type = expr.getExpressionType();
		if (spec.getType() == IASTSimpleDeclSpecifier.t_decltype) {
			switch((expr).getValueCategory()) {
			case XVALUE:
				type= new CPPReferenceType(type, true);
				break;
			case LVALUE:
				type= new CPPReferenceType(type, false);
				break;
			case PRVALUE:
				break;
			}
		} 
		return type;
	}

	public static IType getImpliedObjectType(IScope scope) {
	    try {
			IASTNode node = null;
			while (scope != null) {
				if (scope instanceof ICPPBlockScope || scope instanceof ICPPFunctionScope) {
					node = ASTInternal.getPhysicalNodeOfScope(scope);
					if (node instanceof IASTFunctionDeclarator)
						break;
					if (node.getParent() instanceof IASTFunctionDefinition)
						break;
				}
				scope = scope.getParent();
			}
			if (node != null) {
				if (node.getParent() instanceof IASTFunctionDefinition) {
					IASTFunctionDefinition def = (IASTFunctionDefinition) node.getParent();
					node = def.getDeclarator();
				}
				if (node instanceof IASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node;
					IASTName funcName = findInnermostDeclarator(dtor).getName();
					if (funcName instanceof ICPPASTQualifiedName) {
					    IASTName[] ns = ((ICPPASTQualifiedName) funcName).getNames();
					    funcName = ns[ns.length - 1];
					}
					IScope s = getContainingScope(funcName);
					while (s instanceof ICPPTemplateScope) {
						s = s.getParent();
					}
					if (s instanceof ICPPClassScope) {
						ICPPClassScope cScope = (ICPPClassScope) s;
						IType type = cScope.getClassType();
						if (type instanceof ICPPClassTemplate) {
					    	type= (ICPPClassType) ((ICPPClassTemplate) type).asDeferredInstance();
						}
						return SemanticUtil.addQualifiers(type, dtor.isConst(), dtor.isVolatile(), false);
					}
				}
			}
		} catch (DOMException e) {
		    return e.getProblem();
		}
		return null;
	}
	
	public static IType getPointerDiffType(final IASTBinaryExpression binary) {
		IType t= getStdType(binary, PTRDIFF_T);
		return t != null ? t : INT_TYPE;
	}

	private static IType getStdType(final IASTNode node, char[] name) {
		IBinding[] std= node.getTranslationUnit().getScope().find(STD);
		for (IBinding binding : std) {
			if (binding instanceof ICPPNamespace) {
				final ICPPNamespaceScope scope = ((ICPPNamespace) binding).getNamespaceScope();
				IBinding[] bs= CPPSemantics.findBindings(scope, name, false, node);
				if (bs.length > 0) {
					for (IBinding b : bs) {
						if (b instanceof IType && CPPSemantics.declaredBefore(b, node, false)) {
							return (IType) b;
						}
					}
				}
			}
		}
		return null;
	}

	public static IType get_type_info(IASTExpression expression) {
		IType t= getStdType(expression, TYPE_INFO);
		return t != null ? t : INT_TYPE;
	}

	public static IType get_SIZE_T(IASTNode sizeofExpr) {
		IType t= getStdType(sizeofExpr, SIZE_T);
		return t != null ? t : UNSIGNED_LONG;
	}

	public static ICPPClassTemplate get_initializer_list(IASTNode node) {
		IType t= getStdType(node, INITIALIZER_LIST);
		if (t instanceof ICPPClassTemplate)
			return (ICPPClassTemplate) t;
		return null;
	}

	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		tu.accept(action);
		return action.getProblems();
	}

	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding);
		tu.accept(action);
		return action.getReferences();
	}
	
	public static IASTName[] getImplicitReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction(binding) {
			{ 
				shouldVisitNames = false;
				shouldVisitImplicitNames = true; 
				shouldVisitImplicitNameAlternates = true; 
			}
		};
		tu.accept(action);
		return action.getReferences();
	}
	
	public static IASTName[] getDeclarations(IASTTranslationUnit tu, IBinding binding) {
	    CollectDeclarationsAction action = new CollectDeclarationsAction(binding);
	    tu.accept(action);
	    
		IASTName[] found = action.getDeclarations();
		if (found.length == 0 && binding instanceof ICPPSpecialization && binding instanceof ICPPInternalBinding) {
			IASTNode node = ((ICPPInternalBinding) binding).getDefinition();
			if (node == null) {
				IASTNode[] nds = ((ICPPInternalBinding) binding).getDeclarations();
				if (nds != null && nds.length > 0)
					node = nds[0]; 
			}
			if (node != null) {
				IASTName name = null;
				if (node instanceof IASTDeclarator) {
					name = ((IASTDeclarator) node).getName();
				} else if (node instanceof IASTName) {
					name = (IASTName) node;
				}
				if (name != null)
					found = new IASTName[] { name };
			}
		}
		
		return found;
	}
	
	public static String[] getQualifiedName(IBinding binding) {
		String[] ns = null;
	    for (IBinding owner= binding.getOwner(); owner != null; owner= owner.getOwner()) {
			if (owner instanceof ICPPEnumeration && !((ICPPEnumeration) owner).isScoped()) {
				continue;
			}
			String n= owner.getName();
			if (n == null)
				break;
		    if (owner instanceof ICPPFunction) 
		        break;
		    if (owner instanceof ICPPNamespace && n.length() == 0) {
		    	// TODO(sprigogin): Do not ignore anonymous namespaces.
		    	continue;
		    }
		
		    ns = ArrayUtil.append(String.class, ns, n);
		}
        ns = ArrayUtil.trim(String.class, ns);
        String[] result = new String[ns.length + 1];
        for (int i = ns.length - 1; i >= 0; i--) {
            result[ns.length - i - 1] = ns[i];
        }
        result[ns.length]= binding.getName();
	    return result;
	}
	
	public static char[][] getQualifiedNameCharArray(IBinding binding) {
		char[][] ns = EMPTY_CHAR_ARRAY_ARRAY;
		ns = ArrayUtil.append(ns, binding.getNameCharArray());
		for (IBinding owner= binding.getOwner(); owner != null; owner= owner.getOwner()) {
			char[] n= owner.getNameCharArray();
			if (n == null)
				break;
		    if (owner instanceof ICPPFunction) 
		        break;
		    if (owner instanceof ICPPNamespace && n.length == 0)
		    	continue;
		
		    ns = ArrayUtil.append(ns, n);
		}
        ns = ArrayUtil.trim(ns);
        ArrayUtil.reverse(ns);
	    return ns;
	}

	private static IScope getParentScope(IScope scope, IASTTranslationUnit unit) throws DOMException {
		IScope parentScope= scope.getParent();
		// the index cannot return the translation unit as parent scope
		if (parentScope == null && scope instanceof IIndexScope && unit != null) {
			parentScope= unit.getScope();
		}
		return parentScope;
	}

	public static boolean isExternC(IASTNode node) {
		while (node != null) {
			node= node.getParent();
			if (node instanceof ICPPASTLinkageSpecification) {
				if ("\"C\"".equals(((ICPPASTLinkageSpecification) node).getLiteral())) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}

	@Deprecated
	public static boolean isLValueReference(IType t) {
		t= SemanticUtil.getNestedType(t, TDEF);
		return t instanceof ICPPReferenceType && !((ICPPReferenceType) t).isRValueReference();
	}
		
	/**
	 * Searches for the function enclosing the given node. May return <code>null</code>.
	 */
	public static IBinding findEnclosingFunction(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node= node.getParent();
		}
		if (node == null)
			return null;
		
		IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
		if (dtor != null) {
			IASTName name= dtor.getName();
			if (name != null) {
				return name.resolveBinding();
			}
		}
		return null;
	}

	/**
	 * Searches for the function or class enclosing the given node. May return <code>null</code>.
	 */
	public static IBinding findEnclosingFunctionOrClass(IASTNode node) {
		IASTName name = null;
		for (; node != null; node= node.getParent()) {
			if (node instanceof IASTFunctionDefinition) {
				IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
				if (dtor != null) {
					name= dtor.getName();
				}
				break;
			} 
			if (node instanceof IASTCompositeTypeSpecifier) {
				name= ((IASTCompositeTypeSpecifier) node).getName();
				break;
			}
		}
		if (name == null) 
			return null;
		
		return name.resolveBinding();
	}

	public static IBinding findNameOwner(IASTName name, boolean allowFunction) {
		IASTNode node= name.getLastName();
		while (node instanceof IASTName) {
			if (node instanceof ICPPASTQualifiedName) {
				IASTName[] qn= ((ICPPASTQualifiedName) node).getNames();
				int i= qn.length;
				while (--i >= 0) {
					if (qn[i] == name) {
						break;
					}
				}
				if (--i < 0) 
					break;
				return bindingToOwner(qn[i].resolveBinding());
			}
			name= (IASTName) node;
			node= node.getParent();
		}
		return findDeclarationOwner(node, allowFunction);
	}

	private static IBinding bindingToOwner(IBinding b) {
		if (b instanceof ITypedef) {
			IType t= SemanticUtil.getNestedType((IType) b, TDEF);
			if (t instanceof IBinding) 
				return (IBinding) t;
			
			return b;
		}
		while (b instanceof ICPPNamespaceAlias) {
			b= ((ICPPNamespaceAlias) b).getBinding();
		}
		return b;
	}

	/**
	 * Searches for the first class, namespace, or function, if <code>allowFunction</code>
	 * is <code>true</code>, enclosing the declaration the provided node belongs to and returns
	 * the binding for it. Returns <code>null</code>, if the declaration is not enclosed by any
	 * of the above constructs.
	 */
	public static IBinding findDeclarationOwner(IASTNode node, boolean allowFunction) {
		// Search for declaration
		boolean isNonSimpleElabDecl= false;
		while (!(node instanceof IASTDeclaration)) {
			if (node == null)
				return null;
			if (node instanceof IASTElaboratedTypeSpecifier) {
				isNonSimpleElabDecl= true;
				final IASTNode parent= node.getParent();
				if (parent instanceof IASTSimpleDeclaration) {
					final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) parent;
					if (sdecl.getDeclarators().length == 0) {
						isNonSimpleElabDecl= false;
					}
				}
			} else if (node instanceof IASTEnumerator) {
				break;
			}
			node= node.getParent();
		}

		boolean isFriend= isFriendDeclaration(node);
		
		// Search for enclosing binding
		IASTName name= null;
		node= node.getParent();
		for (; node != null; node= node.getParent()) {
			if (node instanceof IASTFunctionDefinition) {
				if (!allowFunction) 
					continue;

				IASTDeclarator dtor= findInnermostDeclarator(((IASTFunctionDefinition) node).getDeclarator());
				if (dtor != null) {
					name= dtor.getName();
				}
				break;
			} 
			if (node instanceof IASTCompositeTypeSpecifier) {
				if (isFriend || isNonSimpleElabDecl)
					continue;
				name= ((IASTCompositeTypeSpecifier) node).getName();
				break;
			}
			if (node instanceof ICPPASTNamespaceDefinition) {
				name= ((ICPPASTNamespaceDefinition) node).getName();
				break;
			}
			if (node instanceof ICPPASTEnumerationSpecifier) {
				name= ((ICPPASTEnumerationSpecifier) node).getName();
				break;
			}
		}
		if (name == null) 
			return null;
		
		return name.resolveBinding();
	}

	public static boolean doesNotSpecifyType(IASTDeclSpecifier declspec) {
		if (declspec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier ds= (ICPPASTSimpleDeclSpecifier) declspec;
			if (ds.getType() == IASTSimpleDeclSpecifier.t_unspecified) {
				if (ds.isShort() || ds.isLong() || ds.isLongLong() || ds.isSigned() || ds.isUnsigned())
					return false;

				return true;
			}
		}
		return false;
	}
	
	public static ICPPASTDeclarator findInnermostDeclarator(ICPPASTDeclarator dtor) {
		return (ICPPASTDeclarator) ASTQueries.findInnermostDeclarator(dtor);
	}

	/**
	 * Traverses parent chain of the given node and returns the first node of the given type.
	 * @param node the start node
	 * @param type the type to look for
	 * @return the node itself or its closest ancestor that has the given type, or {@code null}
	 *     if no such node is found.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IASTNode> T findAncestorWithType(IASTNode node, Class<T> type) {
		do {
			if (type.isInstance(node)) {
				return (T) node;
			}
		} while ((node = node.getParent()) != null);
		return null;
	}
}
