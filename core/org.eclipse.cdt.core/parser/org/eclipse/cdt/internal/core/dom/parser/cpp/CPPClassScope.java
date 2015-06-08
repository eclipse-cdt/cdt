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
import static org.eclipse.cdt.core.parser.util.ArrayUtil.addAll;
import static org.eclipse.cdt.core.parser.util.ArrayUtil.append;
import static org.eclipse.cdt.core.parser.util.ArrayUtil.appendAt;
import static org.eclipse.cdt.core.parser.util.ArrayUtil.trim;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.UNSPECIFIED_TYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.index.IIndexFileSet;
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
	 * if appropriate. The method will be called after ambiguity resolution.
	 */
	public void createImplicitMembers() {
	    // Create bindings for the implicit members, if the user declared them then those
		// declarations will resolve to these bindings.
	    ICPPASTCompositeTypeSpecifier compTypeSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();

        IASTName name = compTypeSpec.getName().getLastName();
        IBinding binding = name.resolveBinding();
        if (!(binding instanceof ICPPClassType))
        	return;

        ICPPClassType classType = (ICPPClassType) binding;
        if (classType instanceof ICPPClassTemplate) {
            classType= (ICPPClassType) ((ICPPClassTemplate) classType).asDeferredInstance();
        }
        char[] className = name.getLookupKey();

		IType pType = new CPPReferenceType(SemanticUtil.constQualify(classType), false);
		ICPPParameter[] params = new ICPPParameter[] { new CPPParameter(pType, 0) };

		int i= 0;
		ImplicitsAnalysis ia= new ImplicitsAnalysis(compTypeSpec, classType);
		implicits= new ICPPMethod[ia.getImplicitsToDeclareCount()];

		if (!ia.hasUserDeclaredConstructor()) {
			// Default constructor: A(void)
			ICPPMethod m = new CPPImplicitConstructor(this, className, EMPTY_CPPPARAMETER_ARRAY);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredCopyConstructor()) {
			// Copy constructor: A(const A &)
			ICPPMethod m = new CPPImplicitConstructor(this, className, params);
			implicits[i++] = m;
			addBinding(m);
		}

		if (!ia.hasUserDeclaredCopyAssignmentOperator()) {
			// Copy assignment operator: A& operator = (const A &)
			IType refType = new CPPReferenceType(classType, false);
			ICPPFunctionType ft= CPPVisitor.createImplicitFunctionType(refType, params, false, false);
			ICPPMethod m = new CPPImplicitMethod(this, OverloadableOperator.ASSIGN.toCharArray(), ft, params);
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

		ICPPBase[] inheritedConstructorsSources = findInheritedConstructorsSourceBases(compTypeSpec);
    	ICPPMethod[] inheritedConstructors = createInheritedConsructors(this, className,
    			inheritedConstructorsSources, ia.getParametersOfNontrivialUserDeclaredConstructors(),
    			compTypeSpec);
    	implicits = addAll(implicits, inheritedConstructors);
    	for (ICPPMethod ctor : inheritedConstructors) {
    		addBinding(ctor);
    	}
	}

	private ICPPBase[] findInheritedConstructorsSourceBases(
			ICPPASTCompositeTypeSpecifier compositeTypeSpec) {
		ICPPBase[] bases = ClassTypeHelper.getBases(getClassType(), compositeTypeSpec);
		if (bases.length == 0)
			return bases;
		ICPPBase[] results = ICPPBase.EMPTY_BASE_ARRAY;
		IASTDeclaration[] members = compositeTypeSpec.getMembers();
		int n = 0;
        for (IASTDeclaration member : members) {
			if (member instanceof ICPPASTUsingDeclaration) {
				IASTName name = ((ICPPASTUsingDeclaration) member).getName();
				if (!(name instanceof ICPPASTQualifiedName))
					continue;
				ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name;
				ICPPASTNameSpecifier[] qualifier = qName.getQualifier();
				IBinding parent = qualifier[qualifier.length - 1].resolveBinding();
				if (!(parent instanceof IType) || parent instanceof IProblemBinding)
					continue;
				IType type = SemanticUtil.getNestedType((IType) parent, TDEF);
				if (type instanceof IBinding &&
					Arrays.equals(((IBinding) type).getNameCharArray(), qName.getLastName().getSimpleID())) {
					for (ICPPBase base : bases) {
						IType baseClass = base.getBaseClassType();
						if (type.isSameType(baseClass)) {
							((CPPBaseClause) base).setInheritedConstructorsSource(true);
							results = appendAt(results, n++, base);
						}
					}
				}
			}
        }
        return trim(results, n);
	}

	static ICPPMethod[] createInheritedConsructors(ICPPClassScope scope, char[] className,
			ICPPBase[] bases, IType[][] existingConstructorParamTypes, IASTNode point) {
		ICPPMethod[] inheritedConstructors = ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		int n = 0;
		for (ICPPBase base : bases) {
			if (!base.isInheritedConstructorsSource())
				continue;
			IBinding baseClass = base.getBaseClass();
        	if (!(baseClass instanceof ICPPClassType))
        		continue;
    		ICPPConstructor[] ctors = ClassTypeHelper.getConstructors((ICPPClassType) baseClass, point);
    		for (ICPPConstructor ctor : ctors) {
    			ICPPParameter[] prototypeParams = ctor.getParameters();
    			// 12.9-1 For each non-template constructor of X that has at least one parameter
    			// with a default argument, the set of constructors that results from omitting
    			// any ellipsis parameter specification and successively omitting parameters
    			// with a default argument from the end of the parameter-type-list.
    			for (int k = Math.max(ctor.getRequiredArgumentCount(), 1); k <= prototypeParams.length; k++) {
        			if (k == 1 && isReferenceToClass(prototypeParams[0].getType(), (ICPPClassType) baseClass)) {
        				continue;  // Skip the copy constructor.
        			}
        			if (findMatchingSignature(prototypeParams, k, existingConstructorParamTypes) < 0) {
        				ICPPParameter[] params = deriveParameters(prototypeParams, k);
        				CPPInheritedConstructor inheritedConstructor =
        						new CPPInheritedConstructor(scope, className, ctor, params);
        				inheritedConstructors = appendAt(inheritedConstructors, n++, inheritedConstructor);
        			}
    			}
    		}
		}
		return trim(inheritedConstructors, n);
	}

	private static ICPPParameter[] deriveParameters(ICPPParameter[] prototypes, int count) {
		ICPPParameter[] params = new ICPPParameter[count];
		for (int i = 0; i < count; i++) {
			params[i] = new CPPParameter(prototypes[i].getType(), i);
		}
		return params;
	}

	private static boolean isReferenceToClass(IType type, IType classType) {
		type= SemanticUtil.getNestedType(type, TDEF);
		if (type instanceof ICPPReferenceType && !((ICPPReferenceType) type).isRValueReference()) {
			type= SemanticUtil.getNestedType(type, TDEF|REF|CVTYPE);
			return classType.isSameType(type);
		}
		return false;
	}

	private static int findMatchingSignature(ICPPParameter[] params, int numParams, IType[][] paramTypes) {
		for (int i = 0; i < paramTypes.length; i++) {
			if (doParameterTypesMatch(params, numParams, paramTypes[i]))
				return i;
		}
		return -1;
	}

	private static boolean doParameterTypesMatch(ICPPParameter[] params, int numParams, IType[] types) {
		if (numParams != types.length)
			return false;
		for (int i = 0; i < numParams; i++) {
			if (!params[i].getType().isSameType(types[i]))
				return false;
		}
		return true;
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
                ((ObjectSet) o).put(constructor);
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
            // 9.2 ... The class-name is also inserted into the scope of the class itself.
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
	            result = addAll(IBinding.class, result, getConstructors(lookupName, lookup.isResolve()));
	        }
            // 9.2 ... The class-name is also inserted into the scope of the class itself.
            result = append(IBinding.class, result, compName.resolveBinding());
	    }
	    result = addAll(IBinding.class, result, super.getBindings(lookup));
	    return trim(IBinding.class, result);
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
    						bs = append(bs, (ICPPConstructor) binding);
        				}
        			} else if (obj instanceof ICPPConstructor) {
						bs = append(bs, (ICPPConstructor) obj);
        			}
        		}
        		return trim(ICPPConstructor.class, bs);
	        } else if (o instanceof IASTName) {
	        	if (shouldResolve(forceResolve, (IASTName) o, forName) || ((IASTName) o).getBinding() != null) {
	        		// Always store the name, rather than the binding, so that we can properly flush the scope.
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
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		return find(name);
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
			if (((ICPPASTQualifiedName) node).getLastName() == name) {
				node = node.getParent();
			} else {
				return false;
			}
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
            return ((ICPPASTCompositeTypeSpecifier) node).getName();
        }
        return null;
    }
}