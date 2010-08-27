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
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Base implementation for c++ scopes.
 */
public class CPPClassScope extends CPPScope implements ICPPClassScope {
    private ICPPMethod[] implicits = null;

	public CPPClassScope(ICPPASTCompositeTypeSpecifier physicalNode) {
		super(physicalNode);
		((CPPASTCompositeTypeSpecifier) physicalNode).setScope(this);
		createImplicitMembers();
	}

	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	// 12.1 The default constructor, copy constructor, copy assignment operator, and destructor are
	//special member functions.  The implementation will implicitly declare these member functions
	//for a class type when the program does not declare them.
	private void createImplicitMembers() {
	    //create bindings for the implicit members, if the user declared them then those declarations
	    //will resolve to these bindings.
	    ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();

        IASTName name = compTypeSpec.getName().getLastName();
        IBinding binding = name.resolveBinding();
        if (!(binding instanceof ICPPClassType))
        	return;

        ICPPClassType clsType = (ICPPClassType) binding;
        if (clsType instanceof ICPPClassTemplate) {
            try {
            	clsType= CPPTemplates.instantiateWithinClassTemplate((ICPPClassTemplate) clsType);
            } catch (DOMException e) {
            }
        }
        char[] className = name.getLookupKey();

        ICPPParameter[] voidPs = new ICPPParameter[] { new CPPParameter(CPPSemantics.VOID_TYPE, 0) };
		IType pType = new CPPReferenceType(SemanticUtil.addQualifiers(clsType, true, false), false);
		ICPPParameter[] ps = new ICPPParameter[] { new CPPParameter(pType, 0) };

		int i= 0;
		ImplicitsAnalysis ia= new ImplicitsAnalysis(compTypeSpec);
		implicits= new ICPPMethod[ia.getImplicitsToDeclareCount()];

		if (!ia.hasUserDeclaredConstructor()) {
			//default constructor: A(void)
			ICPPMethod m = new CPPImplicitConstructor(this, className, voidPs);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredCopyConstructor()) {
			//copy constructor: A(const A &)

			ICPPMethod m = new CPPImplicitConstructor(this, className, ps);
			implicits[i++]=m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredCopyAssignmentOperator()) {
			//copy assignment operator: A& operator = (const A &)
			IType refType = new CPPReferenceType(clsType, false);
			ICPPFunctionType ft= CPPVisitor.createImplicitFunctionType(refType, ps, false, false);
			ICPPMethod m = new CPPImplicitMethod(this, OverloadableOperator.ASSIGN.toCharArray(), ft, ps);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredDestructor()) {
			//destructor: ~A()
			ICPPFunctionType ft= CPPVisitor.createImplicitFunctionType(new CPPBasicType(Kind.eUnspecified, 0), voidPs, false, false);
			char[] dtorName = CharArrayUtils.concat("~".toCharArray(), className);  //$NON-NLS-1$
			ICPPMethod m = new CPPImplicitMethod(this, dtorName, ft, voidPs);
			implicits[i++] = m;
			addBinding(m);
		}
	}

	@Override
	public IScope getParent() {
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName().getLastName();
		return CPPVisitor.getContainingNonTemplateScope(compName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	@Override
	public void addBinding(IBinding binding) {
	    if (binding instanceof ICPPConstructor) {
	        addConstructor(binding);
	        return;
	    }
        super.addBinding(binding);
	}

	@Override
	public void addName(IASTName name) {
		// don't add names from inactive code branches
		if (!name.isActive())
			return;
		
		if (name instanceof ICPPASTQualifiedName) {
			// check whether the qualification matches
			IBinding b= getClassType();
			final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
			final IASTName[] names= qname.getNames();
			for (int i = names.length-2; i>=0; i--) {
				if (b == null || !CharArrayUtils.equals(names[i].getLookupKey(), b.getNameCharArray()))
					return;
				
				b= b.getOwner();
			}
			if (qname.isFullyQualified() && b != null)
				return;
		}
		IASTNode parent = name.getParent();
		if (parent instanceof IASTDeclarator) {
			if (CPPVisitor.isConstructor(this, (IASTDeclarator) parent)) {
				addConstructor(name);
				return;
			}
		}
		super.addName(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addConstructor(Object constructor) {
		if (bindings == null)
            bindings = new CharArrayObjectMap(1);

        Object o = bindings.get(CONSTRUCTOR_KEY);
        if (o != null) {
            if (o instanceof ObjectSet) {
                ((ObjectSet)o).put(constructor);
            } else {
                ObjectSet set = new ObjectSet(2);
                set.put(o);
                set.put(constructor);
                bindings.put(CONSTRUCTOR_KEY, set);
            }
        } else {
            bindings.put(CONSTRUCTOR_KEY, constructor);
        }
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
	    char[] c = name.getLookupKey();

	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName().getLastName();
		if (compName instanceof ICPPASTTemplateId) {
			compName= ((ICPPASTTemplateId) compName).getTemplateName();
		}
	    if (CharArrayUtils.equals(c, compName.getLookupKey())) {
            //9.2 ... The class-name is also inserted into the scope of the class itself
            return compName.resolveBinding();
	    }
	    return super.getBinding(name, resolve, fileSet);
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet, 
			boolean checkPointOfDecl) {
	    char[] c = name.getLookupKey();

	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName().getLastName();
		if (compName instanceof ICPPASTTemplateId) {
			compName= ((ICPPASTTemplateId) compName).getTemplateName();
		}
	    IBinding[] result = null;
	    if ((!prefixLookup && CharArrayUtils.equals(c, compName.getLookupKey()))
	    		|| (prefixLookup && CharArrayUtils.equals(compName.getLookupKey(), 0, c.length, c, true))) {
	        if (shallReturnConstructors(name, prefixLookup)) {
	            result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, getConstructors(name, resolve));
	        }
            //9.2 ... The class-name is also inserted into the scope of the class itself
            result = (IBinding[]) ArrayUtil.append(IBinding.class, result, compName.resolveBinding());
            if (!prefixLookup)
            	return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	    }
	    result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result,
	    		super.getBindings(name, resolve, prefixLookup, fileSet, checkPointOfDecl));
	    return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

	static protected boolean shouldResolve(boolean force, IASTName candidate, IASTName forName) {
		if (!force || candidate == forName)
			return false;
		if (forName == null)
			return true;
		if (!forName.isReference() && !CPPSemantics.declaredBefore(candidate, forName, false))
			return false;
		return true;
	}

	public ICPPConstructor[] getConstructors() {
		return getConstructors(null, true);
	}

	private ICPPConstructor[] getConstructors(IASTName forName, boolean forceResolve) {
		populateCache();

		final CharArrayObjectMap nameMap = bindings;
		if (nameMap == null)
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;

		Object o = nameMap.get(CONSTRUCTOR_KEY);
		if (o != null) {
			IBinding binding = null;
	        if (o instanceof ObjectSet<?>) {
	        	ObjectSet<?> set = (ObjectSet<?>) o;
	        	IBinding[] bs = null;
        		for (int i = 0; i < set.size(); i++) {
        			Object obj = set.keyAt(i);
        			if (obj instanceof IASTName) {
        				IASTName n = (IASTName) obj;
        				binding = shouldResolve(forceResolve, n, forName) ? n.resolveBinding() : n.getBinding();
        				if (binding instanceof ICPPConstructor) {
    						bs = (IBinding[]) ArrayUtil.append(ICPPConstructor.class, bs, binding);
        				}
        			} else if (obj instanceof ICPPConstructor) {
						bs = (IBinding[]) ArrayUtil.append(ICPPConstructor.class, bs, obj);
        			}
        		}
        		return (ICPPConstructor[]) ArrayUtil.trim(ICPPConstructor.class, bs);
	        } else if (o instanceof IASTName) {
	        	if (shouldResolve(forceResolve, (IASTName) o, forName) || ((IASTName)o).getBinding() != null) {
	        		// always store the name, rather than the binding, such that we can properly flush the scope.
	        		nameMap.put(CONSTRUCTOR_KEY, o);
	        		binding = ((IASTName)o).resolveBinding();
	        	}
	        } else if (o instanceof IBinding) {
	        	binding = (IBinding) o;
	        }
	        if (binding != null && binding instanceof ICPPConstructor) {
	        	return new ICPPConstructor[] { (ICPPConstructor) binding };
	        }
	    }
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	@Override
	public IBinding[] find(String name) {
	    char[] n = name.toCharArray();
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName().getLastName();
		if (compName instanceof ICPPASTTemplateId) {
			compName= ((ICPPASTTemplateId) compName).getTemplateName();
		}

	    if (CharArrayUtils.equals(compName.getLookupKey(), n)) {
	        return new IBinding[] {compName.resolveBinding()};
	    }

	    return super.find(name);
	}

	public static boolean shallReturnConstructors(IASTName name, boolean isPrefixLookup) {
		if (!isPrefixLookup)
			return CPPVisitor.isConstructorDeclaration(name);
		
		if (name.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY)
			return false;
		
		IASTNode node = name.getParent();
		if (node instanceof ICPPASTTemplateId)
			return false;
		if (node instanceof ICPPASTQualifiedName) {
			if (((ICPPASTQualifiedName) node).getLastName() == name)
				node = node.getParent();
			else
				return false;
		}
		if (node instanceof IASTDeclSpecifier) {
			IASTNode parent = node.getParent();
			if (parent instanceof IASTTypeId && parent.getParent() instanceof ICPPASTNewExpression)
				return true;
			return false;
		} else if (node instanceof IASTFieldReference) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getClassType()
	 */
	public ICPPClassType getClassType() {
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
		final IASTName name = compSpec.getName();
		IBinding binding = name.resolveBinding();
		if (binding instanceof ICPPClassType)
			return (ICPPClassType) binding;

		return new CPPClassType.CPPClassTypeProblem(name, IProblemBinding.SEMANTIC_BAD_SCOPE, name.toCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getImplicitMethods()
	 */
	public ICPPMethod[] getImplicitMethods() {
		if (implicits == null)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		return implicits;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
     */
    @Override
	public IName getScopeName() {
        IASTNode node = getPhysicalNode();
        if (node instanceof ICPPASTCompositeTypeSpecifier) {
            return ((ICPPASTCompositeTypeSpecifier)node).getName();
        }
        return null;
    }
}

/**
 * Helps analysis of the class declaration for user declared members relevant
 * to deciding which implicit bindings to declare.
 *
 * @see chapter 12 of the ISO specification
 */
class ImplicitsAnalysis {
	private boolean hasUserDeclaredConstructor;
	private boolean hasUserDeclaredCopyConstructor;
	private boolean hasUserDeclaredCopyAssignmentOperator;
	private boolean hasUserDeclaredDestructor;

	ImplicitsAnalysis(ICPPASTCompositeTypeSpecifier compSpec) {
		ICPPASTFunctionDeclarator[] ctors= getUserDeclaredCtorOrDtor(compSpec, true);

		hasUserDeclaredConstructor= ctors.length> 0;
		hasUserDeclaredCopyConstructor= false;
		hasUserDeclaredCopyAssignmentOperator= false;
		hasUserDeclaredDestructor= getUserDeclaredCtorOrDtor(compSpec, false).length>0;

		outer: for (int i=0; i<ctors.length; i++) {
			ICPPASTFunctionDeclarator dcltor= ctors[i];
			IASTParameterDeclaration[] ps = dcltor.getParameters();
        	if (ps.length >= 1) {
        		if (paramHasTypeReferenceToTheAssociatedClassType(ps[0], compSpec.getName().getRawSignature())) {
            		// and all remaining arguments have initializers
        			for (int j = 1; j < ps.length; j++) {
            			if (ps[j].getDeclarator().getInitializer() == null) {
            				continue outer;
            			}
            		}
        			hasUserDeclaredCopyConstructor= true;
        		}
        	}
	    }

		boolean hasUserDeclaredCAO= getUserDeclaredCopyAssignmentOperators(compSpec).length > 0;
		hasUserDeclaredCopyAssignmentOperator= hasUserDeclaredCAO;
	}

	public int getImplicitsToDeclareCount() {
		return (!hasUserDeclaredDestructor ? 1 : 0)
			+ (!hasUserDeclaredConstructor ? 1 : 0)
			+ (!hasUserDeclaredCopyConstructor ? 1 : 0)
			+ (!hasUserDeclaredCopyAssignmentOperator ? 1 : 0);
	}

	private static ICPPASTFunctionDeclarator[] getUserDeclaredCtorOrDtor(ICPPASTCompositeTypeSpecifier compSpec, boolean constructor) {
		List<ICPPASTFunctionDeclarator> result= new ArrayList<ICPPASTFunctionDeclarator>();
		IASTDeclaration[] members = compSpec.getMembers();
		char[] name = compSpec.getName().getLookupKey();
		IASTDeclarator dcltor = null;
		IASTDeclSpecifier spec = null;
        for (IASTDeclaration member : members) {
			if (member instanceof IASTSimpleDeclaration) {
			    IASTDeclarator[] dtors = ((IASTSimpleDeclaration)member).getDeclarators();
			    if (dtors.length == 0 || dtors.length > 1)
			    	continue;
			    dcltor = dtors[0];
			    spec = ((IASTSimpleDeclaration)member).getDeclSpecifier();
			} else if (member instanceof IASTFunctionDefinition) {
			    dcltor = ((IASTFunctionDefinition)member).getDeclarator();
			    spec = ((IASTFunctionDefinition)member).getDeclSpecifier();
			}


			if (!(dcltor instanceof ICPPASTFunctionDeclarator) || !(spec instanceof IASTSimpleDeclSpecifier) ||
					((IASTSimpleDeclSpecifier)spec).getType() != IASTSimpleDeclSpecifier.t_unspecified)	{
				continue;
			}

			boolean nameEquals= false;
			char[] dtorname= ASTQueries.findInnermostDeclarator(dcltor).getName().getLookupKey();
			if (constructor) {
				nameEquals= CharArrayUtils.equals(dtorname, name);
			} else {
				if (dtorname.length > 0 && dtorname[0] == '~') {
					nameEquals= CharArrayUtils.equals(dtorname, 1, name.length, name);
				}
			}

			if (!nameEquals)
				continue;

			result.add((ICPPASTFunctionDeclarator) dcltor);
        }
        return result.toArray(new ICPPASTFunctionDeclarator[result.size()]);
	}

	private static ICPPASTFunctionDeclarator[] getUserDeclaredCopyAssignmentOperators(ICPPASTCompositeTypeSpecifier compSpec) {
		List<ICPPASTFunctionDeclarator> result= new ArrayList<ICPPASTFunctionDeclarator>();
		IASTDeclaration[] members = compSpec.getMembers();
		IASTDeclarator dcltor = null;
        for (IASTDeclaration member : members) {
			if (member instanceof IASTSimpleDeclaration) {
			    IASTDeclarator[] dtors = ((IASTSimpleDeclaration)member).getDeclarators();
			    if (dtors.length == 0 || dtors.length > 1)
			    	continue;
			    dcltor = dtors[0];
			} else if (member instanceof IASTFunctionDefinition) {
			    dcltor = ((IASTFunctionDefinition)member).getDeclarator();
			}
			if (dcltor instanceof ICPPASTFunctionDeclarator == false)
				continue;
			
			final char[] nchars= ASTQueries.findInnermostDeclarator(dcltor).getName().getLookupKey();
			if (!CharArrayUtils.equals(nchars, OverloadableOperator.ASSIGN.toCharArray())) 
	        	continue;
			
			IASTParameterDeclaration[] ps = ((ICPPASTFunctionDeclarator)dcltor).getParameters();
        	if (ps.length != 1 || !paramHasTypeReferenceToTheAssociatedClassType(ps[0], null))
        		continue;

			result.add((ICPPASTFunctionDeclarator)dcltor);
        }
        return result.toArray(new ICPPASTFunctionDeclarator[result.size()]);
	}

	/**
	 * @param compSpec the name the parameter must have in order to match, or null for any name
	 * @param dec
	 * @return whether the specified parameter is a reference to the associated class type, and
     * (optionally) if it has the specified name
	 */
	private static boolean paramHasTypeReferenceToTheAssociatedClassType(IASTParameterDeclaration dec, String name) {
		boolean result= false;
		IASTDeclarator pdtor= ASTQueries.findTypeRelevantDeclarator(dec.getDeclarator());
		if (pdtor.getPointerOperators().length == 1 &&
				pdtor.getPointerOperators()[0] instanceof ICPPASTReferenceOperator &&
				pdtor.getParent() == dec &&
				dec.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
			ICPPASTNamedTypeSpecifier nts= (ICPPASTNamedTypeSpecifier) dec.getDeclSpecifier();
			if (name == null || name.equals(nts.getName().getRawSignature())) {
				result= true;
			}
		}
		return result;
	}

	public boolean hasUserDeclaredConstructor() {
		return hasUserDeclaredConstructor;
	}

	public boolean hasUserDeclaredCopyConstructor() {
		return hasUserDeclaredCopyConstructor;
	}

	public boolean hasUserDeclaredCopyAssignmentOperator() {
		return hasUserDeclaredCopyAssignmentOperator;
	}

	public boolean hasUserDeclaredDestructor() {
		return hasUserDeclaredDestructor;
	}
}