/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;

/**
 * Context data for IASTName lookup
 */
public class LookupData {
	protected IASTName astName;
	protected CPPASTTranslationUnit tu;
	public Map<ICPPNamespaceScope, List<ICPPNamespaceScope>> usingDirectives= Collections.emptyMap();
	
	/** Used to ensure we don't visit things more than once. */
	public ObjectSet<IScope> visited= new ObjectSet<IScope>(1);

	public boolean checkWholeClassScope = false;
	public boolean ignoreUsingDirectives = false;
	public boolean usingDirectivesOnly = false;
	public boolean forceQualified = false;
	public boolean forAssociatedScopes = false;
	public boolean contentAssist = false;
	public boolean prefixLookup = false;
	public boolean typesOnly = false;
	/** For lookup of unknown bindings the point of declaration can be reversed. */
	public boolean checkPointOfDecl= true;
    /** For field references or qualified names, enclosing template declarations are ignored. */
	public boolean usesEnclosingScope= true;
    /** When computing the cost of a method call, treat the first argument as the implied object. */
	public boolean argsContainImpliedObject = false;
	public boolean ignoreMembers = false;
	/** In list-initialization **/
	public boolean fNoNarrowing= false;
	
	private ICPPASTParameterDeclaration[] functionParameters;
	private IASTInitializerClause[] functionArgs;
	private IType[] functionArgTypes;
	private ValueCategory[] functionArgValueCategories;

	public ICPPClassType skippedScope;
	public Object foundItems = null;
	public ProblemBinding problem;
	
	public LookupData(IASTName n) {
		astName = n;
		tu= (CPPASTTranslationUnit) astName.getTranslationUnit();
		typesOnly = typesOnly(astName);
		checkWholeClassScope = checkWholeClassScope(n);
	}
	
	public LookupData() {
		astName = null;
	}
	
	public final char[] getNameCharArray() {
		if (astName != null)
			return astName.toCharArray();
		return CharArrayUtils.EMPTY;
	}
	
	public boolean includeBlockItem(IASTNode item) {
	    if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return true;
	    if ((astName != null && astName.getParent() instanceof IASTIdExpression) ||
	    		item instanceof ICPPASTNamespaceDefinition  ||
	    		(item instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) item).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) ||
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
		    IASTName[] ns = ((ICPPASTQualifiedName) parent).getNames();
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
			IASTNode p2= ASTQueries.findOutermostDeclarator((IASTDeclarator) p1).getParent();
			if (p2 instanceof IASTSimpleDeclaration) {
				if (p2.getParent() instanceof ICPPASTExplicitTemplateInstantiation)
					return false;
				if (astName instanceof ICPPASTTemplateId &&
						((ICPPASTDeclSpecifier)((IASTSimpleDeclaration) p2).getDeclSpecifier()).isFriend())
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
			IASTNode p2= ASTQueries.findOutermostDeclarator((IASTDeclarator) p1).getParent();
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
			IASTNode p2= ASTQueries.findOutermostDeclarator((IASTDeclarator) p1).getParent();
			if (p2 instanceof IASTDeclaration) {
				return p2.getParent() instanceof ICPPASTExplicitTemplateInstantiation;
			}
		}
		return false;
	}

	public boolean qualified() {
	    if (forceQualified) return true;
		if (astName == null) return false;
		if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
		IASTNode p1 = astName.getParent();
		if (p1 instanceof ICPPASTQualifiedName) {
			final IASTName[] qnames = ((ICPPASTQualifiedName) p1).getNames();
			return qnames.length == 1 || qnames[0] != astName;
		}
		return p1 instanceof ICPPASTFieldReference;
	}
	
	public boolean isFunctionCall() {
	    if (astName == null) return false;
	    if (astName.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) return false;
	    IASTNode p1 = astName.getParent();
	    if (p1 instanceof ICPPASTQualifiedName)
	        p1 = p1.getParent();
	    return (p1 instanceof IASTIdExpression && p1.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME);
	}
	
    public static boolean checkWholeClassScope(IASTName name) {
        if (name == null) 
        	return false;
        
        ASTNodeProperty lastProp= name.getPropertyInParent();
        if (lastProp == CPPSemantics.STRING_LOOKUP_PROPERTY) 
        	return true;

        boolean inInitializer= false;
        IASTNode parent = name.getParent();
        while (parent instanceof IASTName) {
        	name= (IASTName) parent;
        	parent= name.getParent();
        }
        
        while (parent != null && !(parent instanceof IASTFunctionDefinition)) {
         	if (parent instanceof IASTInitializer) {
        		inInitializer= true;
        	}    		
        	lastProp= parent.getPropertyInParent();
            parent = parent.getParent();
        }
        if (parent instanceof IASTFunctionDefinition) {
        	if (lastProp == IASTFunctionDefinition.DECL_SPECIFIER ||
        			lastProp == IASTFunctionDefinition.DECLARATOR) {
        		if (!inInitializer)
        			return false;
        	} 

        	while (parent.getParent() instanceof ICPPASTTemplateDeclaration)
        		parent = parent.getParent();
            if (parent.getPropertyInParent() != IASTCompositeTypeSpecifier.MEMBER_DECLARATION)
                return false;

            ASTNodeProperty prop = name.getPropertyInParent();
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

    public boolean hasResultOrProblem() {
    	return problem != null || hasResults();
    }
    
    public boolean hasResults() {
        if (foundItems == null)
            return false;
        if (foundItems instanceof Object[])
            return ((Object[]) foundItems).length != 0;
        if (foundItems instanceof CharArrayObjectMap)
            return ((CharArrayObjectMap) foundItems).size() != 0;
        return false;
    }

    public boolean hasTypeOrMemberFunctionResult() {
    	if(foundItems == null)
    		return false;
    	if(foundItems instanceof Object[]) {
    		for(Object item : (Object[]) foundItems) {
    			if(item instanceof ICPPMethod || item instanceof IType) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Returns the implied object type, or <code>null</code>.
     */
    public IType getImpliedObjectType() {
        if (astName == null) 
        	return null;
        
        IASTName name= astName;
        IASTNode nameParent= name.getParent();
        while (nameParent instanceof IASTName) {
        	name= (IASTName) nameParent;
        	nameParent= name.getParent();
        }

        try {
        	final ASTNodeProperty prop = name.getPropertyInParent();
        	if (prop == CPPSemantics.STRING_LOOKUP_PROPERTY) {
        		return null;
        	}
        	if (prop == IASTFieldReference.FIELD_NAME) {
        		ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) nameParent;
        		IType implied= CPPSemantics.getFieldOwnerType(fieldRef);
        		if (fieldRef.isPointerDereference()) {
            		implied= SemanticUtil.getUltimateTypeUptoPointers(implied);
            		if (implied instanceof IPointerType)
            			return ((IPointerType) implied).getType();
    			}
        		return implied;
        	}
        	if (prop == IASTIdExpression.ID_NAME) {
        		IScope scope = CPPVisitor.getContainingScope(name);
        		if (scope instanceof ICPPClassScope) {
        			return ((ICPPClassScope) scope).getClassType();
        		} 

        		return CPPVisitor.getImpliedObjectType(scope);
        	}
        	if (prop == IASTDeclarator.DECLARATOR_NAME) {
        		if (forExplicitFunctionInstantiation()) {
            		IScope scope = CPPVisitor.getContainingScope(astName);
            		if (scope instanceof ICPPClassScope) {
            			return ((ICPPClassScope) scope).getClassType();
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
			if (!((ICPPASTDeclSpecifier) simple.getDeclSpecifier()).isFriend())
				return false;
			if (dtor != null)
				return true;
			return simple.getDeclarators().length == 0;
		} else if (decl instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition fnDef = (IASTFunctionDefinition) decl;
			if (!((ICPPASTDeclSpecifier) fnDef.getDeclSpecifier()).isFriend())
				return false;
			return (dtor != null);
		}
		return false;
	}
	
	public boolean checkAssociatedScopes() {
		IASTName name= astName;
		if (name == null || name instanceof ICPPASTQualifiedName)
			return false;
		
		IASTNode parent = name.getParent();
		if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME)
			parent= parent.getParent();
		
		if (parent instanceof ICPPASTQualifiedName) {
			return false;
		}
		return isFunctionCall();
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
			if (p instanceof IASTDeclarator && !(((IASTDeclarator) p).getName() instanceof ICPPASTQualifiedName))
				return false;
			if (p instanceof IASTDeclaration) {
				if (prop == IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
					if (p instanceof IASTSimpleDeclaration) {
						ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration) p).getDeclSpecifier();
						return declSpec.isFriend();
					} else if (p instanceof IASTFunctionDefinition) {
						ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition) p).getDeclSpecifier();
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
	
	public void setFunctionArguments(boolean containsImpliedObject, IASTInitializerClause... exprs) {
		argsContainImpliedObject= containsImpliedObject;
		functionArgs= exprs;
		if (exprs.length != 0) {
			IASTNode node= exprs[0];
			boolean checkForDependentName= false;
			while (node != null) {
				if (node instanceof ICPPASTTemplateDeclaration) {
					checkForDependentName= true;
					break;
				}
				node= node.getParent();
			}
			if (checkForDependentName) {
				IType[] types= getFunctionArgumentTypes();
				for (IType type : types) {
					if (CPPTemplates.isDependentType(type)) {
						checkPointOfDecl= false;
						break;
					}
				}
			}
		}
	}

	public IType[] getFunctionArgumentTypes() {
		if (functionArgTypes == null) {
			if (functionArgs != null) {
				IASTInitializerClause[] exprs= functionArgs;
				functionArgTypes= new IType[exprs.length];
				for (int i = 0; i < exprs.length; i++) {
					IASTInitializerClause e = exprs[i];
					if (e instanceof IASTExpression) {
						IType etype= ((IASTExpression) e).getExpressionType();
						functionArgTypes[i]= SemanticUtil.getSimplifiedType(etype);
					} else if (e instanceof ICPPASTInitializerList) {
						functionArgTypes[i]= new InitializerListType((ICPPASTInitializerList) e);
					}
				}
			} else if (functionParameters != null) {
				ICPPASTParameterDeclaration[] pdecls= functionParameters;
				functionArgTypes= new IType[pdecls.length];
				for (int i = 0; i < pdecls.length; i++) {
					functionArgTypes[i] = SemanticUtil.getSimplifiedType(CPPVisitor.createParameterType(
							pdecls[i], true));
				}
			} 
		}
		return functionArgTypes;
	}
	
	public ValueCategory[] getFunctionArgumentValueCategories() {
		if (functionArgValueCategories == null) {
			IASTInitializerClause[] args= functionArgs;
			if (args != null) {
				functionArgValueCategories= new ValueCategory[args.length];
				for (int i = 0; i < args.length; i++) {
					final IASTInitializerClause arg = args[i];
					if (arg instanceof IASTExpression) {
						functionArgValueCategories[i]= ((IASTExpression) arg).getValueCategory();
					} 
				}
			} else {
				IType[] argTypes= getFunctionArgumentTypes();
				if (argTypes != null) {
					functionArgValueCategories= new ValueCategory[argTypes.length];
					for (int i = 0; i < argTypes.length; i++) {
						IType t= argTypes[i];
						functionArgValueCategories[i]= valueCategoryFromReturnType(t);
					}
				} else {
					functionArgValueCategories= new ValueCategory[0];
				}
			}
		}
		return functionArgValueCategories;
	}

	public void setFunctionParameters(ICPPASTParameterDeclaration[] parameters) {
		functionParameters= parameters;
	}

	public int getFunctionArgumentCount() {
		if (functionArgs != null)
			return functionArgs.length;
		if (functionParameters != null)
			return functionParameters.length;
		return 0;
	}

	public boolean hasFunctionArguments() {
		return functionArgs != null || functionParameters != null;
	}

	public IBinding[] getFoundBindings() {
		if (foundItems instanceof Object[]) {
			Object[] items = (Object[]) foundItems;
			if (items.length != 0) { 
				IBinding[] bindings = new IBinding[items.length];
				int k = 0;
				for (Object item : items) {
					// Exclude using declarations, they have been expanded at this point.
					if (item instanceof IBinding && !(item instanceof ICPPUsingDeclaration)
							&& !(item instanceof CPPCompositeBinding)) {
						bindings[k++] = (IBinding) item;
					}
				}
				if (k != 0) {
					return ArrayUtil.trimAt(IBinding.class, bindings, k - 1);
				}
			}
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}
}
