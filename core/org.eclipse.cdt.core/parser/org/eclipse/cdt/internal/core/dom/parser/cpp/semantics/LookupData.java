/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getSimplifiedType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IScope.ScopeLookupData;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;

/**
 * Context data for IASTName lookup
 */
public class LookupData extends ScopeLookupData {
	public ICPPTemplateArgument[] fTemplateArguments;
	public Map<ICPPNamespaceScope, List<ICPPNamespaceScope>> usingDirectives= Collections.emptyMap();
	
	/** Used to ensure we don't visit things more than once. */
	public ObjectSet<IScope> visited= new ObjectSet<IScope>(1);

	public boolean contentAssist = false;

	public boolean typesOnly = false;
	public boolean usingDirectivesOnly = false;
	public boolean ignoreUsingDirectives = false;
	public boolean ignoreMembers = false;

	public boolean qualified = false;
	public boolean checkAssociatedScopes= false;

	public boolean forUsingDeclaration = false;
	
    /** When computing the cost of a method call, treat the first argument as the implied object. */
	public boolean argsContainImpliedObject = false;
	/** In list-initialization **/
	public boolean fNoNarrowing= false;
	
	private IASTDeclarator fDeclarator;
	private boolean fFunctionCall;
	private IType fImpliedObjectType;
	private ICPPEvaluation[] functionArgs;
	private IType[] functionArgTypes;
	private ValueCategory[] functionArgValueCategories;

	public ICPPClassType skippedScope;
	public Object foundItems = null;
	public ProblemBinding problem;
		
	public LookupData(IASTName n) {
		super(n, true, false);
		if (n == null)
			throw new IllegalArgumentException();

		ICPPTemplateArgument[] args = null;
		if (n instanceof ICPPASTTemplateId) {
			try {
				args= CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) n);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		fTemplateArguments= args;
		configureWith(n);
	}
	
	public LookupData(char[] name, ICPPTemplateArgument[] templateArgs, IASTNode lookupPoint) {
		super(name, lookupPoint);
		fTemplateArguments= templateArgs;
	}
	
	public LookupData(char[] name, ICPPTemplateArgument[] templateArgs, IASTTranslationUnit tu) {
		super(name, tu);
		fTemplateArguments= templateArgs;
	}

	@Override
	public CPPASTTranslationUnit getTranslationUnit() {
		return (CPPASTTranslationUnit) super.getTranslationUnit();
	}

	private void configureWith(final IASTName name) {
		IASTName tn= name;
		if (tn.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			tn= (IASTName) tn.getParent();
		}
		
		IASTNode parent = tn.getParent();
		IASTNode nameParent= parent;
		if (parent instanceof ICPPASTQualifiedName) {
			final ICPPASTQualifiedName qn = (ICPPASTQualifiedName) parent;
			if (qn.getLastName() != tn) {
				// For resolving template id ambiguities we need to consider non-types.
				if (!(tn instanceof ICPPASTTemplateId)) {
					typesOnly= true;
				}
			} else {
				nameParent= parent.getParent();
			}
			final ICPPASTNameSpecifier[] qualifier = qn.getQualifier();
			if (qn.isFullyQualified()) {
				qualified= true;
			} else if (qualifier.length > 0) {
				if (qualifier[0] != tn) {
					qualified= true;
				} 
			}
		} 
		
		if (nameParent instanceof ICPPASTBaseSpecifier
				|| nameParent instanceof ICPPASTElaboratedTypeSpecifier
				|| nameParent instanceof ICPPASTCompositeTypeSpecifier) {
			typesOnly= true;
		} else if (nameParent instanceof ICPPASTUsingDeclaration) {
			forUsingDeclaration= true;
		} else if (nameParent instanceof IASTDeclarator) {
			fDeclarator= (IASTDeclarator) nameParent;
		} else if (nameParent instanceof IASTFieldReference) {
			qualified= true;
		} else if (nameParent instanceof IASTIdExpression) {
			if (nameParent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
				fFunctionCall= true;
			}
		}
	}
	
	/**
	 * Returns whether the name belongs to a simple declaration or function definition.
	 */
	public IASTDeclaration forDeclaration() {
		IASTNode node = fDeclarator;
		while (node instanceof IASTDeclarator)
			node= node.getParent();

		if (node instanceof IASTSimpleDeclaration || node instanceof IASTFunctionDefinition)
			return (IASTDeclaration) node;
		
		return null;
	}

	public IASTDeclarator getDeclarator() {
		return fDeclarator;
	}
	
	public boolean forExplicitFunctionSpecialization() {
		IASTName n = getLookupName();
		if (n == null)
			return false;
		
		IASTDeclaration decl= forDeclaration();
		if (decl != null) {
			if (n.getParent() instanceof ICPPASTTemplateId)
				n = (IASTName) n.getParent();

			ICPPASTTemplateDeclaration tmplDecl = CPPTemplates.getTemplateDeclaration(n);
			return tmplDecl instanceof ICPPASTTemplateSpecialization;
		}
		return false;
	}

	public boolean forExplicitFunctionInstantiation() {
		IASTDeclaration decl= forDeclaration();
		return decl != null && decl.getParent() instanceof ICPPASTExplicitTemplateInstantiation;
	}

	public boolean isFunctionCall() {
		return fFunctionCall;
	}
	
    public static boolean checkWholeClassScope(IASTName name) {
        if (name == null) 
        	return true;
        
        IASTNode node = name.getParent();
        while (node instanceof IASTName) {
        	name= (IASTName) node;
        	node= name.getParent();
        }
        
    	final ASTNodeProperty nameProp = name.getPropertyInParent();
    	if (nameProp == IASTIdExpression.ID_NAME ||
    			nameProp == IASTFieldReference.FIELD_NAME ||
    			nameProp == ICASTFieldDesignator.FIELD_NAME ||
    			nameProp == ICPPASTUsingDirective.QUALIFIED_NAME ||
    			nameProp == ICPPASTUsingDeclaration.NAME ||
    			nameProp == IASTFunctionCallExpression.FUNCTION_NAME ||
    			nameProp == IASTNamedTypeSpecifier.NAME ||
    			nameProp == ICPPASTConstructorChainInitializer.MEMBER_ID) {
    		// Potentially we need to consider the entire class scope
    	} else {
    		return false;
    	}
        
        for (; node != null; node= node.getParent()) {
        	// 3.3.7-5
        	if (node.getParent() instanceof IASTFunctionDefinition) {
            	// In a function body
        		final ASTNodeProperty prop = node.getPropertyInParent();
        		if (prop == IASTFunctionDefinition.DECL_SPECIFIER ||
        				prop == IASTFunctionDefinition.DECLARATOR) {
        			return false;
        		}
        		IASTNode parent = node.getParent();
				while (parent != null) {
					if (parent instanceof ICPPASTCompositeTypeSpecifier)
						return true;
					parent= parent.getParent();
				}
				// No inline method.
				return false;
        	} 
        	if (node instanceof IASTInitializerList || node instanceof IASTEqualsInitializer) {
        		if (node.getPropertyInParent() == IASTDeclarator.INITIALIZER) {
        			IASTNode decl= node.getParent();
        			while (decl instanceof IASTDeclarator) {
        				decl= decl.getParent();
        			}
        			if (decl instanceof IASTParameterDeclaration) {
        				// Default argument
        				IASTNode parent = decl.getParent();
        				while (parent != null) {
        					if (parent instanceof ICPPASTCompositeTypeSpecifier)
        						return true;
        					parent= parent.getParent();
        				}
        				// Not within a class definition
        				return false;
        			}
        			
        			if (decl instanceof IASTSimpleDeclaration && 
        					decl.getPropertyInParent() == IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
        				// Initializer of non-static data member
        				IASTDeclSpecifier declSpec= ((IASTSimpleDeclaration) decl).getDeclSpecifier();
        				if (declSpec.getStorageClass() != IASTDeclSpecifier.sc_static) {
        					return true;
        				}
        				// Continue search, we could still be in a method.
        			}  
        		}
        	}
        }
        return false;
    }
    
    public static boolean typesOnly(IASTName tn) {
		if (tn.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			tn= (IASTName) tn.getParent();
		}
		
		IASTNode parent = tn.getParent();
		if (parent instanceof ICPPASTBaseSpecifier
				|| parent instanceof ICPPASTElaboratedTypeSpecifier
				|| parent instanceof ICPPASTCompositeTypeSpecifier) {
			return true;
		} else if (parent instanceof ICPPASTQualifiedName) {
			final ICPPASTQualifiedName qn = (ICPPASTQualifiedName) parent;
			if (qn.getLastName() != tn) {
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
            return ((CharArrayObjectMap<?>) foundItems).size() != 0;
        return false;
    }

    public boolean hasTypeOrMemberFunctionOrVariableResult() {
    	if (foundItems == null)
    		return false;
    	if (foundItems instanceof Object[]) {
    		for (Object item : (Object[]) foundItems) {
    			if (item instanceof ICPPMethod || item instanceof IType || item instanceof IVariable) {
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
    	if (fImpliedObjectType == null) {
    		fImpliedObjectType= determineImpliedObjectType();
    	}
    	return fImpliedObjectType;
    }
    
    /**
     * Explicitly set the implied object type.
     * This is for use in cases where implied object type cannot
     * be determined automatically because there is no lookup name.
     */
    public void setImpliedObjectType(IType impliedObjectType) {
    	fImpliedObjectType = impliedObjectType;
    }

	private IType determineImpliedObjectType() {
		IASTName tn = getLookupName();
		if (tn == null)
			return null;
		
		if (tn.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME) {
			tn= (IASTName) tn.getParent();
		}
		
		IASTNode parent = tn.getParent();
		IASTNode nameParent= parent;
		if (parent instanceof ICPPASTQualifiedName) {
			final ICPPASTQualifiedName qn = (ICPPASTQualifiedName) parent;
			if (qn.getLastName() == tn) {
				nameParent= parent.getParent();
			}
		} 
		
		if (nameParent instanceof IASTFieldReference) {
			return ((ICPPASTFieldReference) nameParent).getFieldOwnerType();
		} else if (nameParent instanceof IASTIdExpression) {
			if (nameParent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
				IScope scope = CPPVisitor.getContainingScope(nameParent);
				if (scope instanceof ICPPClassScope) {
					return ((ICPPClassScope) scope).getClassType();
				} else {
					return CPPVisitor.getImpliedObjectType(scope);
				}
			}
		}
		return null;
	}

	public boolean forFriendship() {
		IASTName lookupName= getLookupName();
		if (lookupName == null)
			return false;
		
		IASTNode node = lookupName.getParent();
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
		return !qualified && fFunctionCall;
	}

	public boolean checkClassContainingFriend() {
		IASTName lookupName= getLookupName();
		if (lookupName == null || lookupName instanceof ICPPASTQualifiedName)
			return false;

		IASTNode p = lookupName.getParent();
		ASTNodeProperty prop = null;
		while (p != null) {
			prop = p.getPropertyInParent();
			if (prop == ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT || prop == IASTDeclarator.DECLARATOR_NAME)
				return false;
			if (p instanceof IASTDeclarator && !(((IASTDeclarator) p).getName() instanceof ICPPASTQualifiedName))
				return false;
			if (p instanceof IASTDeclaration) {
				if (prop == IASTCompositeTypeSpecifier.MEMBER_DECLARATION) {
					return CPPVisitor.isFriendDeclaration(p);
				} else {
					return false;
				}
			}
			p = p.getParent();
		}
		return false;
	}
	
	public void setFunctionArguments(boolean containsImpliedObject, ICPPEvaluation... exprs) {
		argsContainImpliedObject= containsImpliedObject;
		functionArgs= exprs;
	}

	public void setFunctionArguments(boolean containsImpliedObject, IASTInitializerClause... exprs) {
		ICPPEvaluation[] evals= new ICPPEvaluation[exprs.length];
		for (int i = 0; i < evals.length; i++) {
			evals[i]= ((ICPPASTInitializerClause) exprs[i]).getEvaluation();
		}
		setFunctionArguments(containsImpliedObject, evals);
	}

	public IType[] getFunctionArgumentTypes() {
		if (functionArgTypes == null && functionArgs != null) {
			functionArgTypes= new IType[functionArgs.length];
			for (int i = 0; i < functionArgs.length; i++) {
				ICPPEvaluation e = functionArgs[i];
				functionArgTypes[i]= getSimplifiedType(e.getTypeOrFunctionSet(getLookupPoint()));
			}
		}
		return functionArgTypes;
	}
	
	public ValueCategory[] getFunctionArgumentValueCategories() {
		if (functionArgValueCategories == null) {
			ICPPEvaluation[] args= functionArgs;
			if (args != null) {
				functionArgValueCategories= new ValueCategory[args.length];
				for (int i = 0; i < args.length; i++) {
					final ICPPEvaluation arg = args[i];
					functionArgValueCategories[i] = arg.getValueCategory(getLookupPoint());
				}
			} 
		}
		return functionArgValueCategories;
	}

	public int getFunctionArgumentCount() {
		if (functionArgs != null)
			return functionArgs.length;
		return 0;
	}

	public int getFunctionArgumentPackExpansionCount() { 
		int count= 0;
		if (functionArgs != null) {
			for (ICPPEvaluation arg : functionArgs) {
				if (arg instanceof EvalParameterPack)
					count++;
			}
		}
		return count;
	}

	public boolean hasFunctionArguments() {
		return functionArgs != null;
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
	
	public boolean ignoreRecursionResolvingBindings() {
		// When name lookup is performed during template instantiation
		// rather than for an AST name, infinite recursion can sometimes
		// result when a binding with a given name uses the same name
		// in its definition (e.g. "typedef C::name name" where C is
		// the current (template) class). In such cases, we want to
		// ignore the resulting IRecursionResolvingBindings and allow
		// name lookup to proceed to outer (or base class) scopes.
		return getLookupName() == null;
	}
}
