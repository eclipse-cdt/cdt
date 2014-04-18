/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.UNSPECIFIED_TYPE;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

/**
 * Base implementation for c++ scopes.
 */
public class CPPClassScope extends CPPScope implements ICPPClassScope {
    private static final ICPPFunctionType DESTRUCTOR_FUNCTION_TYPE =
    		CPPVisitor.createImplicitFunctionType(UNSPECIFIED_TYPE, EMPTY_CPPPARAMETER_ARRAY, false, false);

	private ICPPMethod[] implicits;

	public CPPClassScope(ICPPASTCompositeTypeSpecifier physicalNode) {
		super(physicalNode);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	/**
	 * Adds in default constructor, copy constructor, copy assignment operator and destructor,
	 * if appropriate.
	 * Method will be called after ambiguity resolution.
	 */
	public void createImplicitMembers() {
	    // Create bindings for the implicit members, if the user declared them then those
		// declarations will resolve to these bindings.
	    ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();

        IASTName name = compTypeSpec.getName().getLastName();
        IBinding binding = name.resolveBinding();
        if (!(binding instanceof ICPPClassType))
        	return;

        ICPPClassType clsType = (ICPPClassType) binding;
        if (clsType instanceof ICPPClassTemplate) {
            clsType= (ICPPClassType) ((ICPPClassTemplate) clsType).asDeferredInstance();
        }
        char[] className = name.getLookupKey();

		IType pType = new CPPReferenceType(SemanticUtil.constQualify(clsType), false);
		ICPPParameter[] ps = new ICPPParameter[] { new CPPParameter(pType, 0) };

		int i= 0;
		ImplicitsAnalysis ia= new ImplicitsAnalysis(compTypeSpec, clsType);
		implicits= new ICPPMethod[ia.getImplicitsToDeclareCount()];

		if (!ia.hasUserDeclaredConstructor()) {
			// Default constructor: A(void)
			ICPPMethod m = new CPPImplicitConstructor(this, className, EMPTY_CPPPARAMETER_ARRAY);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredCopyConstructor()) {
			// Copy constructor: A(const A &)
			ICPPMethod m = new CPPImplicitConstructor(this, className, ps);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredCopyAssignmentOperator()) {
			// Copy assignment operator: A& operator = (const A &)
			IType refType = new CPPReferenceType(clsType, false);
			ICPPFunctionType ft= CPPVisitor.createImplicitFunctionType(refType, ps, false, false);
			ICPPMethod m = new CPPImplicitMethod(this, OverloadableOperator.ASSIGN.toCharArray(), ft, ps);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredDestructor()) {
			// Destructor: ~A()
			char[] dtorName = CharArrayUtils.concat("~".toCharArray(), className);  //$NON-NLS-1$
			ICPPMethod m = new CPPImplicitMethod(this, dtorName, DESTRUCTOR_FUNCTION_TYPE, EMPTY_CPPPARAMETER_ARRAY);
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
		// Don't add names from inactive code branches.
		if (!name.isActive())
			return;
		
		if (name instanceof ICPPASTQualifiedName) {
			// Check whether the qualification matches.
			IBinding b= getClassType();
			final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
			final ICPPASTNameSpecifier[] qualifier = qname.getQualifier();
			for (int i = qualifier.length; --i >= 0;) {
				if (b == null)
					return;
				
				char[] segmentName;
				if (qualifier[i] instanceof IASTName) {
					segmentName = ((IASTName) qualifier[i]).getLookupKey();
				} else {
					IBinding segmentBinding = qualifier[i].resolveBinding();
					if (segmentBinding == null)
						return;
					segmentName = segmentBinding.getNameCharArray();
				}
				
				if (!CharArrayUtils.equals(segmentName, b.getNameCharArray()))
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
	public IBinding[] getBindings(ScopeLookupData lookup) {
	    char[] c = lookup.getLookupKey();
	    final boolean prefixLookup= lookup.isPrefixLookup();
	    
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName().getLastName();
		if (compName instanceof ICPPASTTemplateId) {
			compName= ((ICPPASTTemplateId) compName).getTemplateName();
		}
	    IBinding[] result = null;
	    if ((!prefixLookup && CharArrayUtils.equals(c, compName.getLookupKey()))
				|| (prefixLookup && ContentAssistMatcherFactory.getInstance().match(c, compName.getLookupKey()))) {
	        final IASTName lookupName = lookup.getLookupName();
			if (shallReturnConstructors(lookupName, prefixLookup)) {
	            result = ArrayUtil.addAll(IBinding.class, result, getConstructors(lookupName, lookup.isResolve()));
	        }
            //9.2 ... The class-name is also inserted into the scope of the class itself
            result = ArrayUtil.append(IBinding.class, result, compName.resolveBinding());
	    }
	    result = ArrayUtil.addAll(IBinding.class, result, super.getBindings(lookup));
	    return ArrayUtil.trim(IBinding.class, result);
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

	@Override
	public ICPPConstructor[] getConstructors() {
		return getConstructors(null, true);
	}

	private ICPPConstructor[] getConstructors(IASTName forName, boolean forceResolve) {
		populateCache();

		final CharArrayObjectMap<Object> nameMap = bindings;
		if (nameMap == null)
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;

		Object o = nameMap.get(CONSTRUCTOR_KEY);
		if (o != null) {
			IBinding binding = null;
	        if (o instanceof ObjectSet<?>) {
	        	ObjectSet<?> set = (ObjectSet<?>) o;
	        	ICPPConstructor[] bs = ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
        		for (int i = 0; i < set.size(); i++) {
        			Object obj = set.keyAt(i);
        			if (obj instanceof IASTName) {
        				IASTName n = (IASTName) obj;
        				binding = shouldResolve(forceResolve, n, forName) ? n.resolveBinding() : n.getBinding();
        				if (binding instanceof ICPPConstructor) {
    						bs = ArrayUtil.append(bs, (ICPPConstructor) binding);
        				}
        			} else if (obj instanceof ICPPConstructor) {
						bs = ArrayUtil.append(bs, (ICPPConstructor) obj);
        			}
        		}
        		return ArrayUtil.trim(ICPPConstructor.class, bs);
	        } else if (o instanceof IASTName) {
	        	if (shouldResolve(forceResolve, (IASTName) o, forName) || ((IASTName) o).getBinding() != null) {
	        		// Always store the name, rather than the binding, such that we can properly flush the scope.
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

	@Override
	public IBinding[] find(String name) {
	    char[] n = name.toCharArray();
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName().getLastName();
		if (compName instanceof ICPPASTTemplateId) {
			compName= ((ICPPASTTemplateId) compName).getTemplateName();
		}

	    if (CharArrayUtils.equals(compName.getLookupKey(), n)) {
	        return new IBinding[] { compName.resolveBinding() };
	    }

	    return super.find(name);
	}

	public static boolean shallReturnConstructors(IASTName name, boolean isPrefixLookup) {
		if (name == null)
			return false;
		
		if (!isPrefixLookup)
			return CPPVisitor.isConstructorDeclaration(name);
		
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

	@Override
	public ICPPClassType getClassType() {
		ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
		final IASTName name = compSpec.getName();
		IBinding binding = name.resolveBinding();
		if (binding instanceof ICPPClassType)
			return (ICPPClassType) binding;

		return new CPPClassType.CPPClassTypeProblem(name, ISemanticProblem.BINDING_NO_CLASS, name.toCharArray());
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		if (implicits == null)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		return implicits;
	}

    @Override
	public IName getScopeName() {
        IASTNode node = getPhysicalNode();
        if (node instanceof ICPPASTCompositeTypeSpecifier) {
            return ((ICPPASTCompositeTypeSpecifier)node).getName();
        }
        return null;
    }
}