/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import java.util.ArrayList;
import java.util.BitSet;
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
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * Collection of methods to extract information from a C++ translation unit.
 */
public class CPPVisitor extends ASTQueries {
	public static final char[] SIZE_T = "size_t".toCharArray(); //$NON-NLS-1$
	public static final char[] PTRDIFF_T = "ptrdiff_t".toCharArray(); //$NON-NLS-1$
	public static final String STD = "std"; //$NON-NLS-1$
	public static final String TYPE_INFO= "type_info"; //$NON-NLS-1$
	private static final String INITIALIZER_LIST = "initializer_list"; //$NON-NLS-1$
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
			    parent instanceof ICPPASTQualifiedName ||
				parent instanceof ICPPASTBaseSpecifier ||
				parent instanceof ICPPASTConstructorChainInitializer ||
				name.getPropertyInParent() == ICPPASTNamespaceAlias.MAPPING_NAME) {
		    if (name.getLookupKey().length == 0)
		    	return null;
		    
			binding = CPPSemantics.resolveBinding(name); 
			if (binding instanceof IProblemBinding && parent instanceof ICPPASTQualifiedName && 
					!(parent.getParent() instanceof ICPPASTNamespaceAlias)) {
				final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) parent;
			    final IASTName[] ns = qname.getNames();
			    if (ns[ns.length - 1] != name) 
			    	return binding;
				
			    if (ns.length > 1 && ns[ns.length-2].getBinding() instanceof IProblemBinding)
			    	return binding;
			    
				parent = parent.getParent();
			    if (((IProblemBinding) binding).getID() == IProblemBinding.SEMANTIC_MEMBER_DECLARATION_NOT_FOUND) {
					IASTNode node = getContainingBlockItem(name.getParent());
					ASTNodeProperty prop= node.getPropertyInParent();
					while (prop == ICPPASTTemplateDeclaration.OWNED_DECLARATION) {
						node= node.getParent();
						prop= node.getPropertyInParent();
					}
					if (prop != IASTCompositeTypeSpecifier.MEMBER_DECLARATION &&
							prop != ICPPASTNamespaceDefinition.OWNED_DECLARATION) {
						return binding;
					}
					IScope scope= getContainingScope(qname);
					while (scope instanceof ICPPTemplateScope) {
						try {
							scope= scope.getParent();
						} catch (DOMException e) {
							return binding;
						}
					}
				    if (scope != getContainingScope(name))
				        return binding;
				}
			} else {
				if (binding instanceof ICPPClassType && binding instanceof IIndexBinding && name.isDefinition()) {
					parent= parent.getParent(); // need to create an ast binding.
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
						e.addDeclaration(specifier);
					} else if (e.getDefinition() == null) {
						e.addDefinition(specifier);
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
        	boolean template = false;
        	ICPPScope scope = (ICPPScope) getContainingScope(name);
        	while (scope instanceof ICPPTemplateScope) {
        		template = true;
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
        	}
            if (!(binding instanceof ICPPInternalBinding) || !(binding instanceof ICPPClassType) && name.isActive()) {
    			if (elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum) {
					if (template)
	            		binding = new CPPClassTemplate(name);
	            	else
						binding = new CPPClassType(name, binding);
					// name may live in a different scope, so make sure to add it to the owner scope, as well.
    				ASTInternal.addName(scope,  elabType.getName());
    			}
    		} else {
				if ((binding instanceof ICPPClassTemplate) == template) {
					ASTInternal.addDeclaration(binding, elabType);
				} else {
    				binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
    			}
    		}
        } catch (DOMException e) {
            binding = e.getProblem();
        }
        
		return binding;
	}

	private static IBinding createBinding(ICPPASTCompositeTypeSpecifier compType) {
		IASTName name = compType.getName();
		if (name instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName) name).getNames();
			name = ns[ns.length - 1];
		}

    	IBinding binding = null;
    	ICPPScope scope = (ICPPScope) getContainingScope(name);
        try {
        	boolean template = false;
        	while (scope instanceof ICPPTemplateScope) {
        		template = true;
        		scope= (ICPPScope) scope.getParent();
        	}
    		if (name instanceof ICPPASTTemplateId) {
    			return CPPTemplates.createBinding((ICPPASTTemplateId) name);
    		} 
        	if (name.getLookupKey().length > 0 && scope != null) // can't lookup anonymous things
        		binding = scope.getBinding(name, false);
            if (binding instanceof ICPPInternalBinding && binding instanceof ICPPClassType && name.isActive()) {
            	ICPPInternalBinding internal = (ICPPInternalBinding) binding;
				if (internal.getDefinition() == null && (binding instanceof ICPPClassTemplate) == template) {
            		ASTInternal.addDefinition(internal, compType);
            	} else {
            		binding = new ProblemBinding(name, IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
            	}
    		} else {
    			if (template) {
    				binding = new CPPClassTemplate(name);
    			} else {
    				binding = new CPPClassType(name, binding);
    			}
    		}
        } catch (DOMException e) {
            binding = e.getProblem();
        }
		return binding;
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
			if (parent instanceof IASTSimpleDeclaration) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration) parent).getDeclSpecifier();
				isFriendDecl= declSpec.isFriend();
			} else if (parent instanceof IASTFunctionDefinition) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition) parent).getDeclSpecifier();
				isFriendDecl= declSpec.isFriend();
			}
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
        	    		try {
        	    			t2 = ((IVariable) binding).getType();
        	    		} catch (DOMException e1) {
        	    		}
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

	public static boolean isConstructor(IScope containingScope, IASTDeclarator declarator) {
	    if (containingScope == null || !(containingScope instanceof ICPPClassScope))
	        return false;
	    
        IASTNode node = ASTInternal.getPhysicalNodeOfScope(containingScope);
		if (!(node instanceof ICPPASTCompositeTypeSpecifier)) {
			return false;
		}

		ICPPASTCompositeTypeSpecifier clsTypeSpec = (ICPPASTCompositeTypeSpecifier) node;
        IASTName clsName = clsTypeSpec.getName();
        if (clsName instanceof ICPPASTQualifiedName) {
	        IASTName[] names = ((ICPPASTQualifiedName) clsName).getNames(); 
	        clsName = names[names.length - 1];
	    }
        return isConstructor(clsName, declarator);
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
			IASTDeclarator dtor= findTypeRelevantDeclarator((IASTDeclarator) parent);
			if (dtor instanceof ICPPASTFunctionDeclarator) {
				if (name instanceof ICPPASTQualifiedName) {
					IASTName[] names = ((ICPPASTQualifiedName) name).getNames();
					if (names.length >= 2) {
						return CPPVisitor.isConstructor(names[names.length - 2], dtor);
					}
				} else {
					while (parent != null && !(parent instanceof ICPPASTCompositeTypeSpecifier)) {
						parent= parent.getParent();
					}
					if (parent instanceof ICPPASTCompositeTypeSpecifier) {
						IASTName compName= ((ICPPASTCompositeTypeSpecifier) parent).getName().getLastName();
						return CPPVisitor.isConstructor(compName, dtor);
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isConstructor(IASTName parentName, IASTDeclarator declarator) {
		if (declarator == null || !(declarator instanceof IASTFunctionDeclarator))
			return false;
        
	    IASTName name = findInnermostDeclarator(declarator).getName();
	    if (!CharArrayUtils.equals(name.getLookupKey(), parentName.getLookupKey()))
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
				}
			} else if (node instanceof IASTParameterDeclaration) {
			    IASTNode parent = node.getParent();
			    if (parent instanceof ICPPASTFunctionDeclarator) {
					ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) parent;
					if (ASTQueries.findTypeRelevantDeclarator(dtor) == dtor) {
						while (parent.getParent() instanceof IASTDeclarator)
						    parent = parent.getParent();
						ASTNodeProperty prop = parent.getPropertyInParent();
						if (prop == IASTSimpleDeclaration.DECLARATOR) {
						    return dtor.getFunctionScope();
						}
						if (prop == IASTFunctionDefinition.DECLARATOR) {
						    final IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition) parent.getParent()).getBody();
						    if (body != null)
						    	return body.getScope();
						    return dtor.getFunctionScope();
						} 
						if (prop == ICPPASTLambdaExpression.DECLARATOR) {
						    final IASTCompoundStatement body = ((ICPPASTLambdaExpression) parent.getParent()).getBody();
						    if (body != null)
						    	return body.getScope();
						    return dtor.getFunctionScope();
						}							
					}
			    } else if (parent instanceof ICPPASTTemplateDeclaration) {
			    	return CPPTemplates.getContainingScope(node);
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
		    	return CPPTemplates.getContainingScope(node);
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
	
	/**
	 * Returns enclosing function definition, or <code>null</code> if the given node
	 * is not part of a function definition.
	 */
	public static ICPPASTFunctionDefinition findEnclosingFunctionDefinition(IASTNode node) {
		while (node != null) {
			if (node instanceof ICPPASTFunctionDefinition) {
				return (ICPPASTFunctionDefinition) node;
			}
			node= node.getParent();
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
				if (i == 0) {
					if (qname.isFullyQualified()) {
						return parent.getTranslationUnit().getScope();
					} 
				}
				if (i > 0) {
					if (data != null) {
						data.usesEnclosingScope= false;
					}
					// For template functions we may need to resolve a template parameter
					// as a parent of an unknown type used as parameter type.
					IBinding binding = names[i - 1].resolvePreBinding();
					while (binding instanceof ITypedef) {
						IType t = ((ITypedef) binding).getType();
						if (t instanceof IBinding)
							binding = (IBinding) t;
						else break;
					}
					boolean done= true;
					IScope scope= null;
					if (binding instanceof ICPPClassType) {
						if (binding instanceof IIndexBinding) {
							binding= (((CPPASTTranslationUnit) parent.getTranslationUnit())).mapToAST((ICPPClassType) binding);
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
			} else if (parent instanceof ICPPASTFieldReference) {
				if (data != null) {
					data.usesEnclosingScope= false;
				}
				final ICPPASTFieldReference fieldReference = (ICPPASTFieldReference) parent;
				IType type = CPPSemantics.getChainedMemberAccessOperatorReturnType(fieldReference);
				if (fieldReference.isPointerDereference()) {
					type= getUltimateType(type, false);
				} else {
					type= getUltimateTypeUptoPointers(type);
				}
				if (type instanceof ICPPClassType) {
					if (type instanceof IIndexBinding) {
						type= (((CPPASTTranslationUnit) fieldReference.getTranslationUnit())).mapToAST((ICPPClassType) type);
					}
					return ((ICPPClassType) type).getCompositeScope();
				} else if (type instanceof ICPPUnknownBinding) {
					return ((ICPPUnknownBinding) type).asScope();
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
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE   = 4;
		private static final int KIND_COMPOSITE = 5;
		private static final int KIND_TEMPLATE_PARAMETER = 6;
		
		
		public CollectDeclarationsAction(IBinding binding) {
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
		
		@SuppressWarnings("fallthrough")
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
					// fall through
					
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
					if (nameBinding.equals(binding)) {
						return true;
					}
					// a using declaration is a declaration for the references of its delegates
					if (nameBinding instanceof ICPPUsingDeclaration) {
						if (ArrayUtil.contains(((ICPPUsingDeclaration) nameBinding).getDelegates(), binding)) {
							return true;
						}
					}
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
	        try {
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
            } catch (DOMException e) {
                pt = e.getProblem();
            }
	        
	        pTypes[i] = pt; 
	    }
	    
	    return new CPPFunctionType(returnType, pTypes, isConst, isVolatile, false);
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

	public static IType[] createParameterTypes(ICPPASTFunctionDeclarator fnDtor) {
		ICPPASTParameterDeclaration[] params = fnDtor.getParameters();
	    IType[] pTypes = new IType[params.length];
	    for (int i = 0; i < params.length; i++) {
	        pTypes[i]= createParameterType(params[i], true);
	    }
		return pTypes;
	}

	/**
	 * Creates the type for a parameter declaration.
	 */
	public static IType createParameterType(final ICPPASTParameterDeclaration pdecl, boolean forFuncType) {
		IType pt;
		IASTDeclSpecifier pDeclSpec = pdecl.getDeclSpecifier();
		ICPPASTDeclarator pDtor = pdecl.getDeclarator();
		pt = createType(pDeclSpec);
		if (pDtor != null) {
			pt = createType(pt, pDtor);
		}
		pt=  adjustParameterType(pt, forFuncType);
		
		if (pt != null && CPPVisitor.findInnermostDeclarator(pDtor).declaresParameterPack()) {
			pt= new CPPParameterPackType(pt);
		}
		return pt;
	}
	
	/**
	 * Adjusts the parameter type according to 8.3.5-3:
	 * cv-qualifiers are deleted, arrays and function types are converted to pointers.
	 */
	private static IType adjustParameterType(final IType pt, boolean forFunctionType) {
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
		if (forFunctionType && SemanticUtil.getCVQualifier(t) != CVQualifier._) {
			return SemanticUtil.getNestedType(t, TDEF | ALLCVQ);
		}
		return pt;
	}
	
	private static IType getPointerTypes(IType type, IASTDeclarator declarator) {
	    IASTPointerOperator[] ptrOps = declarator.getPointerOperators();
		for (IASTPointerOperator ptrOp : ptrOps) {
		    if (ptrOp instanceof IGPPASTPointerToMember) {
		        type = new GPPPointerToMemberType(type, (IGPPASTPointerToMember) ptrOp);
		    } else if (ptrOp instanceof ICPPASTPointerToMember) {
				type = new CPPPointerToMemberType(type, (ICPPASTPointerToMember) ptrOp);
		    } else if (ptrOp instanceof IGPPASTPointer) {
			    type = new GPPPointerType(type, (IGPPASTPointer) ptrOp);
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
	
	public static IType createType(IASTNode node) {
	    if (node == null)
	        return null;
		if (node instanceof IASTExpression)
			return ((IASTExpression) node).getExpressionType();
		if (node instanceof IASTTypeId)
			return createType(((IASTTypeId) node).getAbstractDeclarator());
		if (node instanceof IASTParameterDeclaration)
			return createType(((IASTParameterDeclaration) node).getDeclarator());
		return null;
	}

	public static IType createType(IASTDeclarator declarator) {
		if (declarator == null) 
			return null;
		
		declarator= findOutermostDeclarator(declarator);
		IASTNode parent = declarator.getParent();
		
		IASTDeclSpecifier declSpec = null;
		boolean isPackExpansion= false;
		if (parent instanceof IASTSimpleDeclaration) {
			declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTFunctionDefinition) {
			declSpec = ((IASTFunctionDefinition) parent).getDeclSpecifier();
		} else if (parent instanceof ICPPASTTypeId) {
			final ICPPASTTypeId typeId = (ICPPASTTypeId) parent;
			declSpec = typeId.getDeclSpecifier();
			isPackExpansion= typeId.isPackExpansion();
		} else {
			assert false;
			return null;
		}

		IASTNode initClause= declarator.getInitializer();
		if (initClause instanceof IASTEqualsInitializer) {
			initClause= ((IASTEqualsInitializer) initClause).getInitializerClause();
		}
		
		if (declSpec instanceof ICPPASTSimpleDeclSpecifier &&
				((ICPPASTSimpleDeclSpecifier) declSpec).getType() == IASTSimpleDeclSpecifier.t_auto) {
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				return createAutoFunctionType(declSpec, (ICPPASTFunctionDeclarator) declarator);
			}

			parent = parent.getParent();
			if (parent instanceof ICPPASTNewExpression) {
				IASTInitializer initializer = ((ICPPASTNewExpression) parent).getInitializer();
				IASTInitializerClause[] arguments = ((ICPPASTConstructorInitializer) initializer).getArguments();
				if (arguments.length == 1) {
					initClause = arguments[0];
				} 
			} else if (parent instanceof IASTCompositeTypeSpecifier &&
					declSpec.getStorageClass() != IASTDeclSpecifier.sc_static) {
				// Non-static auto-typed class members are not allowed.
				return null;
			}
			return createAutoType(initClause, declSpec, declarator);
		}
		
		IType type = createType(declSpec);
		type = createType(type, declarator);

		// C++ specification 8.3.4.3 and 8.5.1.4
		if (initClause instanceof IASTInitializerList) {
			IType t= SemanticUtil.getNestedType(type, TDEF);
			if (t instanceof IArrayType) {
				IArrayType at= (IArrayType) t;
				if (at.getSize() == null) {
					type= new CPPArrayType(at.getType(), Value.create(((IASTInitializerList) initClause).getSize()));
				}
			}
		}

		if (type != null && isPackExpansion) {
			type= new CPPParameterPackType(type);
		}
		return type;
	}

	private static IType createAutoType(IASTNode initClause, IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		//  C++0x: 7.1.6.4
		if (!autoTypeDeclSpecs.get().add(declSpec)) {
			// Detected a self referring auto type, e.g.: auto x = x;
			return null;
		}

		IType type = AutoTypeResolver.AUTO_TYPE;
		IType initType = null;
		ICPPClassTemplate initializer_list_template = null;
		try {
			if (initClause instanceof ICPPASTInitializerList) {
				initializer_list_template = get_initializer_list(declSpec);
				if (initializer_list_template == null) {
					return null;
				}
				type = (IType) CPPTemplates.instantiate(initializer_list_template,
						new ICPPTemplateArgument[] { new CPPTemplateArgument(type) }, true);
			}
			type = decorateType(type, declSpec, declarator);
	
			if (initClause instanceof IASTExpression) {
				initType = ((IASTExpression) initClause).getExpressionType();
			} else if (initClause instanceof ICPPASTInitializerList) {
				initType = new InitializerListType((ICPPASTInitializerList) initClause);
			}
			if (initType == null) {
				return null;
			}
		} finally {
			autoTypeDeclSpecs.get().remove(declSpec);
		}
		ICPPFunctionTemplate template = new AutoTypeResolver(type);
		CPPTemplateParameterMap paramMap = new CPPTemplateParameterMap(1);
		TemplateArgumentDeduction.deduceFromFunctionArgs(template, new IType[] { initType }, new BitSet(),
				paramMap, false);
		ICPPTemplateArgument argument = paramMap.getArgument(0, 0);
		if (argument == null) {
			return null;
		}
		type = argument.getTypeValue();
		if (initClause instanceof ICPPASTInitializerList) {
			type = (IType) CPPTemplates.instantiate(initializer_list_template,
					new ICPPTemplateArgument[] { new CPPTemplateArgument(type) }, true);
		}
		return decorateType(type, declSpec, declarator);
	}

	/**
	 * C++0x: [8.3.5-2]
	 */
	private static IType createAutoFunctionType(IASTDeclSpecifier declSpec, ICPPASTFunctionDeclarator declarator) {
		IASTTypeId id= declarator.getTrailingReturnType();
		if (id == null)
			return null;
		IType t= createType(id.getAbstractDeclarator());
		t= qualifyType(t, declSpec);
		return createType(t, declarator);
	}

	public static IType createType(IASTDeclSpecifier declSpec) {
	    IType type = getBaseType(declSpec);
	    if (type == null) {
	    	return null;
	    }
		return qualifyType(type, declSpec);
	}

	private static IType getBaseType(IASTDeclSpecifier declSpec) {
	    IType type = null;
	    IASTName name = null;
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
			type = getDeclType(spec);
			if (type == null && spec.getType() != IASTSimpleDeclSpecifier.t_auto) {
				type = new CPPBasicType(spec);
			}
		}
		if (name != null) {
			IBinding binding = name.resolvePreBinding();
			if (binding instanceof ICPPConstructor) {
				type= ((ICPPConstructor) binding).getClassOwner();
			} else if (binding instanceof IType) {
				type = (IType) binding;
			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
				//TODO workaround... is there anything better? 
				try {
					type = ((ICPPTemplateNonTypeParameter) binding).getType();
				} catch (DOMException e) {
					type = e.getProblem();
				}
			} else if (binding instanceof IVariable) {
				//this is to help with the ambiguity between typeid & idexpression in template arguments
				try {
					type = ((IVariable) binding).getType();
				} catch (DOMException e) {
					type = e.getProblem();
				}
			}
		}
		return type;
	}

	private static IType decorateType(IType type, IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		type = qualifyType(type, declSpec);
		type = createType(type, declarator);
		return type;
	}

	private static IType qualifyType(IType type, IASTDeclSpecifier declSpec) {
		return SemanticUtil.addQualifiers(type, declSpec.isConst(), declSpec.isVolatile());
	}

	/**
	 * @param declarator
	 * @return
	 */
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
				try {
					if (b instanceof IVariable) {
						return ((IVariable) b).getType();
					}
					if (b instanceof IFunction) {
						return ((IFunction) b).getType();
					}
				} catch (DOMException e) {
					return e.getProblem();
				}
			}
		}
		IType type = expr.getExpressionType();
		if (spec.getType() == IASTSimpleDeclSpecifier.t_decltype) {
			while (expr instanceof IASTUnaryExpression
					&& ((IASTUnaryExpression) expr).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
				expr = ((IASTUnaryExpression) expr).getOperand();
			}
			if (!(expr instanceof IASTFunctionCallExpression)) {
				type= SemanticUtil.getNestedType(type, TDEF | REF);
				if (expr.isLValue())
					type= new CPPReferenceType(type, false);
			}
		} else {
			type= SemanticUtil.getNestedType(type, TDEF | REF);
		}
		return type;
	}

	public static IType getThisType(IScope scope) {
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
					    	type= CPPTemplates.instantiateWithinClassTemplate((ICPPClassTemplate) type);
						}
						type = SemanticUtil.addQualifiers(type, dtor.isConst(), dtor.isVolatile());
						type = new CPPPointerType(type);
						return type;
					}
				}
			}
		} catch (DOMException e) {
		    return e.getProblem();
		}
		return null;
	}
	
	public static IType getPointerDiffType(final IASTBinaryExpression binary) {
		CPPBasicType basicType;
		IScope scope = getContainingScope(binary);
		IBinding[] bs= CPPSemantics.findBindings(scope, PTRDIFF_T, false, binary);
		if (bs.length > 0) {
			for (IBinding b : bs) {
				if (b instanceof IType && CPPSemantics.declaredBefore(b, binary, false)) {
					return (IType) b;
				}
			}
		}
		basicType= new CPPBasicType(Kind.eInt, 0);
		basicType.setFromExpression(binary);
		return basicType;
	}

	public static IType get_type_info(IASTExpression expression) {
		IBinding[] std= expression.getTranslationUnit().getScope().find(STD);
		for (IBinding binding : std) {
			if (binding instanceof ICPPNamespace) {
				IBinding[] typeInfo= ((ICPPNamespace) binding).getNamespaceScope().find(TYPE_INFO);
				for (IBinding t : typeInfo) {
					if (t instanceof ICPPClassType) {
						return (ICPPClassType) t;
					}
				}
			}
		}
		return new CPPBasicType(Kind.eInt, 0);
	}

	public static IType get_SIZE_T(IASTNode sizeofExpr) {
		IScope scope = getContainingScope(sizeofExpr);
		IBinding[] bs = CPPSemantics.findBindings(scope, SIZE_T, false, sizeofExpr);
		if (bs.length > 0 && bs[0] instanceof IType) {
			return (IType) bs[0];
		}
		return new CPPBasicType(Kind.eInt, IBasicType.IS_LONG | IBasicType.IS_UNSIGNED);
	}

	public static ICPPClassTemplate get_initializer_list(IASTNode node) {
		IBinding[] std= node.getTranslationUnit().getScope().find(STD);
		for (IBinding binding : std) {
			if (binding instanceof ICPPNamespace) {
				IBinding[] initializer_list= ((ICPPNamespace) binding).getNamespaceScope().find(INITIALIZER_LIST);
				for (IBinding t : initializer_list) {
					if (t instanceof ICPPClassTemplate) {
						return (ICPPClassTemplate) t;
					}
				}
			}
		}
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
	
	/**
	 * Return the qualified name by concatenating component names with the 
	 * Scope resolution operator ::
	 * @param qn the component names
	 * @return the qualified name
	 */
	public static String renderQualifiedName(String[] qn) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < qn.length; i++) {
			result.append(qn[i] + (i + 1 < qn.length ? "::" : ""));  //$NON-NLS-1$//$NON-NLS-2$
		}
		return result.toString();
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
		
		    ns = (String[]) ArrayUtil.append(String.class, ns, n);
		}
        ns = (String[]) ArrayUtil.trim(String.class, ns);
        String[] result = new String[ns.length + 1];
        for (int i = ns.length - 1; i >= 0; i--) {
            result[ns.length - i - 1] = ns[i];
        }
        result[ns.length]= binding.getName();
	    return result;
	}
	
	public static char[][] getQualifiedNameCharArray(IBinding binding) {
		char[][] ns = null;
		for (IBinding owner= binding.getOwner(); owner != null; owner= owner.getOwner()) {
			char[] n= owner.getNameCharArray();
			if (n == null)
				break;
		    if (owner instanceof ICPPFunction) 
		        break;
		    if (owner instanceof ICPPNamespace && n.length == 0)
		    	continue;
		
		    ns = (char[][]) ArrayUtil.append(n.getClass(), ns, n);
		}
        final char[] bname = binding.getNameCharArray();
        ns = (char[][]) ArrayUtil.trim(bname.getClass(), ns);
        char[][] result = new char[ns.length + 1][];
        for (int i = ns.length - 1; i >= 0; i--) {
            result[ns.length - i - 1] = ns[i];
        }
		result[ns.length]= bname;
	    return result;
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
		IASTNode node= name;
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
				return qn[i].resolveBinding();
			}
			name= (IASTName) node;
			node= node.getParent();
		}
		return findDeclarationOwner(node, allowFunction);
	}

	/**
	 * Searches for the first class, namespace, or function, if <code>allowFunction</code>
	 * is <code>true</code>, enclosing the declaration the provided node belongs to and returns
	 * the binding for it. Returns <code>null</code>, if the declaration is not enclosed by any
	 * of the above constructs.
	 */
	public static IBinding findDeclarationOwner(IASTNode node, boolean allowFunction) {
		// Search for declaration
		boolean isFriend= false;
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

		if (node instanceof IASTSimpleDeclaration) {
			final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) node;
			ICPPASTDeclSpecifier declSpec= (ICPPASTDeclSpecifier) sdecl.getDeclSpecifier();
			if (declSpec.isFriend()) {
				isFriend= true;
			} 
		} else if (node instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDefinition = (IASTFunctionDefinition) node;
			ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) funcDefinition.getDeclSpecifier();
			if (declSpec.isFriend()) {
				isFriend= true;
			}
		}
		
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

	/**
	 * Check whether a given declaration is a friend function declaration.
	 */
	public static boolean isFriendFunctionDeclaration(IASTDeclaration declaration) {
		while (declaration instanceof ICPPASTTemplateDeclaration) {
			declaration= ((ICPPASTTemplateDeclaration) declaration).getDeclaration();
		}
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) declaration;
			ICPPASTDeclSpecifier declspec= (ICPPASTDeclSpecifier) sdecl.getDeclSpecifier();
			if (declspec.isFriend()) {
				IASTDeclarator[] dtors= sdecl.getDeclarators();
				if (dtors.length == 1 && findTypeRelevantDeclarator(dtors[0]) instanceof IASTFunctionDeclarator) {
					return true;
				}
			}
		}
		return false;
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
}
