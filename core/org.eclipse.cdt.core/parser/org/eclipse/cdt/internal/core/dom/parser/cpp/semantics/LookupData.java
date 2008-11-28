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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;

/**
 * Context data for IASTName lookup
 */
class LookupData {
	protected IASTName astName;
	protected CPPASTTranslationUnit tu;
	public Map<ICPPNamespaceScope, List<ICPPNamespaceScope>> usingDirectives= Collections.emptyMap();
	
	/*
	 * Used to ensure we don't visit things more than once
	 */
	public ObjectSet<IScope> visited= new ObjectSet<IScope>(1);
	
	/*
	 * Used to detect circular inheritance
	 */
	public ObjectSet<IScope> inheritanceChain;
	
	@SuppressWarnings("unchecked")
	public ObjectSet<IScope> associated = ObjectSet.EMPTY_SET;
	
	public boolean checkWholeClassScope = false;
	public boolean ignoreUsingDirectives = false;
	public boolean usingDirectivesOnly = false;
	public boolean forceQualified = false;
	public boolean forUserDefinedConversion = false;
	public boolean forAssociatedScopes = false;
	public boolean contentAssist = false;
	public boolean prefixLookup = false;
	public boolean typesOnly = false;
	public boolean considerConstructors = false;

	public IBinding unknownBinding= null;
	public Object foundItems = null;
	public Object[] functionParameters;
	public ICPPASTTemplateId templateId;
	public ProblemBinding problem;
	
	public LookupData(IASTName n) {
		astName = n;
		tu= (CPPASTTranslationUnit) astName.getTranslationUnit();
		typesOnly = typesOnly(astName);
		considerConstructors = considerConstructors();
		checkWholeClassScope = checkWholeClassScope();
	}
	
	public LookupData() {
		astName = null;
	}
	
	public final char[] name() {
		if (astName != null)
			return astName.toCharArray();
		return CPPSemantics.EMPTY_NAME_ARRAY;
	}
	
	public boolean includeBlockItem(IASTNode item) {
	    if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return true;
	    if ((astName != null && astName.getParent() instanceof IASTIdExpression) ||
	    		item instanceof ICPPASTNamespaceDefinition  ||
	    		(item instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)item).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) ||
			    item instanceof ICPPASTTemplateDeclaration) {
	        return true;
	    }
	    return false;
	}
	
	static boolean typesOnly(IASTName name) {
		if (name == null) return false;
		if (name.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTBaseSpecifier || parent instanceof ICPPASTElaboratedTypeSpecifier ||
		    parent instanceof ICPPASTCompositeTypeSpecifier)
		    return true;
		if (parent instanceof ICPPASTQualifiedName) {
		    IASTName[] ns = ((ICPPASTQualifiedName)parent).getNames();
		    return (name != ns[ns.length -1]);
		}
		return false;
	}
	
	public boolean forUsingDeclaration() {
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		IASTNode p1 = astName.getParent();
		if (p1 instanceof ICPPASTUsingDeclaration)
		    return true;
		
		if (p1 instanceof ICPPASTQualifiedName) {
		    IASTNode p2 = p1.getParent();
		    if (p2 instanceof ICPPASTUsingDeclaration) {
		        IASTName[] ns = ((ICPPASTQualifiedName) p1).getNames();
		        return (ns[ns.length - 1] == astName);
		    }
		}
		return false;
	}
	
	public boolean forFunctionDeclaration() {
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		
		IASTName n = astName;
		if (n.getParent() instanceof ICPPASTTemplateId)
		    n = (IASTName) n.getParent();
		IASTNode p1 = n.getParent();
		if (p1 instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) p1).getLastName() != n)
				return false;
		    p1 = p1.getParent();
		}			
		
		if (p1 instanceof IASTDeclarator) {
			IASTNode p2= CPPVisitor.findOutermostDeclarator((IASTDeclarator) p1).getParent();
			if (p2 instanceof IASTSimpleDeclaration) {
				if (p2.getParent() instanceof ICPPASTExplicitTemplateInstantiation)
					return false;
				if (astName instanceof ICPPASTTemplateId &&
						((ICPPASTDeclSpecifier)((IASTSimpleDeclaration)p2).getDeclSpecifier()).isFriend())
					return false;
				
				return true;
			} 
			return p2 instanceof IASTFunctionDefinition;
		}
		return false;
	}
	
	public boolean forExplicitFunctionSpecialization() {
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		
		IASTName n = astName;
		if (n.getParent() instanceof ICPPASTTemplateId)
		    n = (IASTName) n.getParent();
		IASTNode p1 = n.getParent();
		if (p1 instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) p1).getLastName() != n)
				return false;
		    p1 = p1.getParent();
		}			
		
		if (p1 instanceof IASTDeclarator) {
			IASTNode p2= CPPVisitor.findOutermostDeclarator((IASTDeclarator) p1).getParent();
			if (p2 instanceof IASTSimpleDeclaration || p2 instanceof IASTFunctionDefinition) {
				ICPPASTTemplateDeclaration tmplDecl = CPPTemplates.getTemplateDeclaration(n);
				return tmplDecl instanceof ICPPASTTemplateSpecialization;
			}
		}
		return false;
	}

	public boolean forExplicitFunctionInstantiation() {
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		
		IASTName n = astName;
		if (n.getParent() instanceof ICPPASTTemplateId)
		    n = (IASTName) n.getParent();
		IASTNode p1 = n.getParent();
		if (p1 instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) p1).getLastName() != n)
				return false;
		    p1 = p1.getParent();
		}			
		
		if (p1 instanceof IASTDeclarator) {
			IASTNode p2= CPPVisitor.findOutermostDeclarator((IASTDeclarator) p1).getParent();
			if (p2 instanceof IASTDeclaration) {
				return p2.getParent() instanceof ICPPASTExplicitTemplateInstantiation;
			}
		}
		return false;
	}

	private boolean considerConstructors() {
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		IASTNode p1 = astName.getParent();
		IASTNode p2 = p1.getParent();
		
		if (p1 instanceof ICPPASTConstructorChainInitializer)
			return true;
		if (p1 instanceof ICPPASTNamedTypeSpecifier && p2 instanceof IASTTypeId) {
			return p2.getParent() instanceof ICPPASTNewExpression;
		} else if (p1 instanceof ICPPASTQualifiedName) {
			if (p2 instanceof ICPPASTFunctionDeclarator) {
				IASTName[] names = ((ICPPASTQualifiedName)p1).getNames();
				if (names.length >= 2 && names[names.length - 1] == astName)
				    return CPPVisitor.isConstructor(names[names.length - 2], (IASTDeclarator) p2);
			} else if (p2 instanceof ICPPASTNamedTypeSpecifier) {
				IASTNode p3 = p2.getParent();
				return p3 instanceof IASTTypeId && p3.getParent() instanceof ICPPASTNewExpression;
			} else if (p2 instanceof IASTIdExpression) {
				return p2.getParent() instanceof IASTFunctionCallExpression;
			}
		} else if (p1 instanceof IASTFunctionCallExpression || p2 instanceof IASTFunctionCallExpression) {
			return true;
		}
		return false;
	}
	
	public boolean qualified() {
	    if (forceQualified) return true;
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		IASTNode p1 = astName.getParent();
		if (p1 instanceof ICPPASTQualifiedName) {
			final IASTName[] qnames = ((ICPPASTQualifiedName)p1).getNames();
			return qnames.length == 1 || qnames[0] != astName;
		}
		return p1 instanceof ICPPASTFieldReference;
	}
	
	public boolean functionCall() {
	    if (astName == null) return false;
	    if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
	    IASTNode p1 = astName.getParent();
	    if (p1 instanceof ICPPASTQualifiedName)
	        p1 = p1.getParent();
	    return (p1 instanceof IASTIdExpression && p1.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME);
	}
	
    private boolean checkWholeClassScope() {
        if (astName == null) return false;
        if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return true;

        IASTNode parent = astName.getParent();
        while (parent != null && !(parent instanceof IASTFunctionDefinition)) {
            parent = parent.getParent();
        }
        if (parent instanceof IASTFunctionDefinition) {
        	while (parent.getParent() instanceof ICPPASTTemplateDeclaration)
        		parent = parent.getParent();
            if (parent.getPropertyInParent() != IASTCompositeTypeSpecifier.MEMBER_DECLARATION)
                return false;

            ASTNodeProperty prop = astName.getPropertyInParent();
            if (prop == ICPPASTQualifiedName.SEGMENT_NAME)
                prop = astName.getParent().getPropertyInParent();
            if (prop == IASTIdExpression.ID_NAME ||
					prop == IASTFieldReference.FIELD_NAME ||
					prop == ICASTFieldDesignator.FIELD_NAME ||
					prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
					prop == ICPPASTUsingDeclaration.NAME ||
					prop == IASTFunctionCallExpression.FUNCTION_NAME ||
					prop == IASTNamedTypeSpecifier.NAME ||
					prop == ICPPASTConstructorChainInitializer.MEMBER_ID) {
                return true;
            }
        }
        return false;
    }

    public boolean hasResults() {
        if (foundItems == null)
            return false;
        if (foundItems instanceof Object[])
            return ((Object[])foundItems).length != 0;
        if (foundItems instanceof CharArrayObjectMap)
            return ((CharArrayObjectMap)foundItems).size() != 0;
        return false;
    }

    /**
     * an IType[] of function arguments, including the implied object argument
     */
    public IType getImpliedObjectArgument() {
        if (astName == null) 
        	return null;
        
        IASTName tempName= astName;
        IASTNode tempNameParent= tempName.getParent();
        while (tempNameParent instanceof IASTName) {
        	tempName= (IASTName) tempNameParent;
        	tempNameParent= tempName.getParent();
        }

        try {
        	final ASTNodeProperty prop = tempName.getPropertyInParent();
        	if (prop == CPPSemantics.STRING_LOOKUP_PROPERTY) {
        		if (tempNameParent instanceof ICPPASTUnaryExpression) {
        			ICPPASTUnaryExpression unaryExp = (ICPPASTUnaryExpression) tempNameParent;
        			IASTExpression oprd= unaryExp.getOperand();
        			return CPPVisitor.getExpressionType(oprd);
        		}
        		if (tempNameParent instanceof ICPPASTFieldReference) {
        			ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) tempNameParent;
        			IType implied = CPPVisitor.getExpressionType(fieldRef.getFieldOwner());
        			if (fieldRef.isPointerDereference() && implied instanceof IPointerType) {
        				return ((IPointerType)implied).getType();
        			}
        			return implied;
        		}
        		if (tempNameParent instanceof IASTArraySubscriptExpression) {
             		IASTExpression exp = ((IASTArraySubscriptExpression)tempNameParent).getArrayExpression();
             		return CPPVisitor.getExpressionType(exp);
             	} 
        		if (tempNameParent instanceof IASTFunctionCallExpression) {
        			return CPPVisitor.getExpressionType(((IASTFunctionCallExpression) tempNameParent).getFunctionNameExpression());
        		}
        		return null;
        	} 
        	if (prop == IASTFieldReference.FIELD_NAME) {
        		ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) tempNameParent;
        		IType implied= CPPSemantics.getChainedMemberAccessOperatorReturnType(fieldRef);
        		implied= SemanticUtil.getUltimateTypeUptoPointers(implied);
        		if (fieldRef.isPointerDereference() && implied instanceof IPointerType) {
    				return ((IPointerType)implied).getType();
    			}
        		return implied;
        	}
        	if (prop == IASTIdExpression.ID_NAME) {
        		IScope scope = CPPVisitor.getContainingScope(tempName);
        		if (scope instanceof ICPPClassScope) {
        			return ((ICPPClassScope)scope).getClassType();
        		} 

        		IType implied = CPPVisitor.getThisType(scope);
        		if (implied instanceof IPointerType) {
        			return ((IPointerType)implied).getType();
        		}
        		return implied;
        	}
        	if (prop == IASTDeclarator.DECLARATOR_NAME) {
        		if (forExplicitFunctionInstantiation()) {
            		IScope scope = CPPVisitor.getContainingScope(astName);
            		if (scope instanceof ICPPClassScope) {
            			return ((ICPPClassScope)scope).getClassType();
            		} 
        		}
        	}
        	return null;
        } catch (DOMException e) {
        	return e.getProblem();
        }
    }

	public boolean forFriendship() {
		if (astName == null)
			return false;
		IASTNode node = astName.getParent();
		while (node instanceof IASTName)
			node = node.getParent();
		
		IASTDeclaration decl = null;
		IASTDeclarator dtor = null;
		if (node instanceof ICPPASTDeclSpecifier && node.getParent() instanceof IASTDeclaration) {
			decl = (IASTDeclaration) node.getParent();
		} else if (node instanceof IASTDeclarator) {
			dtor = (IASTDeclarator) node;
			while (dtor.getParent() instanceof IASTDeclarator)
				dtor = (IASTDeclarator) dtor.getParent();
			if (!(dtor.getParent() instanceof IASTDeclaration))
				return false;
			decl = (IASTDeclaration) dtor.getParent();
		} else {
			return false;
		}
		if (decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) decl;
			if (!((ICPPASTDeclSpecifier)simple.getDeclSpecifier()).isFriend())
				return false;
			if (dtor != null)
				return true;
			return simple.getDeclarators().length == 0;
		} else if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fnDef = (IASTFunctionDefinition) decl;
			if (!((ICPPASTDeclSpecifier)fnDef.getDeclSpecifier()).isFriend())
				return false;
			return (dtor != null);
		}
		return false;
	}
	
	public boolean checkAssociatedScopes() {
		if (astName == null || astName instanceof ICPPASTQualifiedName)
			return false;
		IASTNode parent = astName.getParent();
		if (parent instanceof ICPPASTQualifiedName) {
			IASTName[] ns = ((ICPPASTQualifiedName)parent).getNames();
			if (ns[ns.length - 1] != astName)
				return false;
		}
		return functionCall() && (associated.size() > 0);
	}

	public boolean checkClassContainingFriend() {
		if (astName == null || astName instanceof ICPPASTQualifiedName)
			return false;
		
		IASTNode p = astName.getParent();
		ASTNodeProperty prop = null;
		while (p != null) {
			prop = p.getPropertyInParent();
			if (prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT || prop == IASTDeclarator.DECLARATOR_NAME)
				return false;
			if (p instanceof IASTDeclarator && !(((IASTDeclarator)p).getName() instanceof ICPPASTQualifiedName))
				return false;
			if (p instanceof IASTDeclaration) {
				if (prop == IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
					if (p instanceof IASTSimpleDeclaration) {
						ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)p).getDeclSpecifier();
						return declSpec.isFriend();
					} else if (p instanceof IASTFunctionDefinition) {
						ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)p).getDeclSpecifier();
						return declSpec.isFriend();
					}
				} else {
					return false;
				}
			}
			p = p.getParent();
		}
		return false;
	}
	
	public boolean preferTemplateFunctions() {
		if (astName == null)
			return false;
		return (astName instanceof ICPPASTTemplateId || astName.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME);
	}
}
