/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class TypeUtil {

    public static boolean isDeclaringType(ICElement elem) {
        int type = elem.getElementType();
        return (type == ICElement.C_CLASS
            || type == ICElement.C_STRUCT
            || type == ICElement.C_ENUMERATION
            || type == ICElement.C_UNION
            || type == ICElement.C_TYPEDEF
            || type == ICElement.C_NAMESPACE);
    }

    public static boolean isMemberType(ICElement elem) {
        int type = elem.getElementType();
        if (type == ICElement.C_CLASS
            || type == ICElement.C_STRUCT
            || type == ICElement.C_ENUMERATION
            || type == ICElement.C_UNION
            || type == ICElement.C_TYPEDEF
            || type == ICElement.C_NAMESPACE)
        	return true;
        return elem instanceof IMember;
    }

    /**
     * Returns the type in which this member is declared, or <code>null</code>
     * if this member is not declared in a type (for example, a top-level type).
     * This is a handle-only method.
     * 
     * @return the type in which this member is declared, or <code>null</code>
     * if this member is not declared in a type (for example, a top-level type)
     */
    public static ICElement getDeclaringType(ICElement elem) {
        if (!isMemberType(elem))
            return null;
        ICElement parent = elem.getParent();
        while (parent != null && !(parent instanceof ITranslationUnit)) {
            if (isDeclaringType(parent))
                return parent;
            parent = parent.getParent();
        }
        return null;
    }
    
    public static ICElement getDeclaringClass(ICElement type) {
    	ICElement parentElement = type.getParent();
    	if (parentElement != null && isClassOrStruct(parentElement)) {
    	    return parentElement;
    	}

    	if (isClassOrStruct(type)) {
        	while (parentElement != null) {
        	    if (isClassOrStruct(parentElement)) {
        			return parentElement;
        		} else if (parentElement instanceof IMember) {
       				parentElement = parentElement.getParent();
       			} else {
       				return null;
        		}
        	}
        }

    	return null;
    }
    
    public static boolean isClassOrStruct(ICElement type) {
        int kind = type.getElementType();
		// case ICElement.C_TEMPLATE_CLASS:
		// case ICElement.C_TEMPLATE_STRUCT:
        return (kind == ICElement.C_CLASS || kind == ICElement.C_STRUCT);
    }

    public static boolean isClass(ICElement type) {
        return (type.getElementType() == ICElement.C_CLASS);
    }

    public static boolean isNamespace(ICElement type) {
        return (type.getElementType() == ICElement.C_NAMESPACE);
    }

    /**
     * Returns the top-level types declared in the given translation unit
     * in the order in which they appear in the source.
     *
     * @param tu the translation unit
     * @return the top-level types declared in the given translation unit
     * @throws CModelException if this element does not exist or if an
     *		exception occurs while accessing its corresponding resource
     */
    public static ICElement[] getTypes(ITranslationUnit tu) throws CModelException {
        List typeList = new ArrayList();
        ICElement[] children = tu.getChildren();
        for (int i = 0; i < children.length; ++i) {
            if (isDeclaringType(children[i]))
                typeList.add(children[i]);
        }
        return (ICElement[])typeList.toArray(new ICElement[typeList.size()]);
	}
    
    /**
     * Returns all types declared in the given translation unit in the order
     * in which they appear in the source. 
     * This includes all top-level types and nested member types.
     * It does NOT include local types (types defined in methods).
     *
     * @return the array of top-level and member types defined in the given translation unit, in declaration order.
     * @throws CModelException if this element does not exist or if an
     *		exception occurs while accessing its corresponding resource
     */
    public static ICElement[] getAllTypes(ITranslationUnit tu) throws CModelException {
    	ICElement[] types = getTypes(tu);
    	ArrayList allTypes = new ArrayList(types.length);
    	ArrayList typesToTraverse = new ArrayList(types.length);
    	for (int i = 0; i < types.length; i++) {
    		typesToTraverse.add(types[i]);
    	}
    	while (!typesToTraverse.isEmpty()) {
    		ICElement type = (ICElement) typesToTraverse.get(0);
    		typesToTraverse.remove(type);
    		allTypes.add(type);
    		types = getTypes(type);
    		for (int i = 0; i < types.length; i++) {
    			typesToTraverse.add(types[i]);
    		}
    	} 
        return (ICElement[])allTypes.toArray(new ICElement[allTypes.size()]);
	}
    

	/**
	 * Returns the immediate member types declared by the given element.
	 * The results are listed in the order in which they appear in the source file.
	 *
	 * @param elem the element
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the immediate member types declared by this type
	 */
    public static ICElement[] getTypes(ICElement elem) throws CModelException {
        List typeList = new ArrayList();
        if (isDeclaringType(elem) && elem instanceof IParent) {
            ICElement[] children = ((IParent)elem).getChildren();
            for (int i = 0; i < children.length; ++i) {
                if (isDeclaringType(children[i]))
                    typeList.add(children[i]);
            }
        }
        return (ICElement[])typeList.toArray(new ICElement[typeList.size()]);
	}
    
    public static ITranslationUnit getTranslationUnit(ICElement elem) {
        while (elem != null) {
            if (elem instanceof ITranslationUnit)
                return (ITranslationUnit)elem;
            elem = elem.getParent();
        }
        return null;
    }
	
//  TODO move method to CModelUtil
    public static IQualifiedTypeName getFullyQualifiedName(ICElement type) {
		String name = type.getElementName();
		IQualifiedTypeName qualifiedName = new QualifiedTypeName(name);
		ICElement parent = type.getParent();
		while (parent != null && (isNamespace(parent) || isClass(parent))) {
			qualifiedName = new QualifiedTypeName(parent.getElementName()).append(qualifiedName);
		    parent = parent.getParent();
		}
	    return qualifiedName;
	}
    
    public static IMethodDeclaration[] getMethods(ICElement elem) {
	    if (elem instanceof IStructure) {
	        try {
	            List list = ((IParent)elem).getChildrenOfType(ICElement.C_METHOD_DECLARATION);
	            if (list != null && !list.isEmpty()) {
	                return (IMethodDeclaration[]) list.toArray(new IMethodDeclaration[list.size()]);
	            }
	        } catch (CModelException e) {
	        }
	    }
	    return null;
	}

    public static ICElement[] getFields(ICElement elem) {
	    if (elem instanceof IStructure) {
	        try {
	            List list = ((IParent)elem).getChildrenOfType(ICElement.C_FIELD);
	            if (list != null && !list.isEmpty()) {
	                return (ICElement[]) list.toArray(new ICElement[list.size()]);
	            }
	        } catch (CModelException e) {
	        }
	    }
	    return null;
	}
    
    
	/**
	 * Finds a method by name.
	 * This searches for a method with a name and signature. Parameter types are only
	 * compared by the simple name, no resolving for the fully qualified type name is done.
	 * Constructors are only compared by parameters, not the name.
	 * @param name The name of the method to find
	 * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
	 * @param isConstructor If the method is a constructor
	 * @param methods The methods to search in
	 * @return The found method or <code>null</code>, if nothing found
	 */
//  TODO move methods to CModelUtil
	public static IMethodDeclaration findMethod(String name, String[] paramTypes, boolean isConstructor, boolean isDestructor, IMethodDeclaration[] methods) throws CModelException {
		for (int i= methods.length - 1; i >= 0; i--) {
			if (isSameMethodSignature(name, paramTypes, isConstructor, isDestructor, methods[i])) {
				return methods[i];
			}
		}
		return null;
	}
   
	/**
	 * Tests if a method equals to the given signature.
	 * Parameter types are only compared by the simple name, no resolving for
	 * the fully qualified type name is done. Constructors are only compared by
	 * parameters, not the name.
	 * @param name Name of the method
	 * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
	 * @param isConstructor Specifies if the method is a constructor
	 * @return Returns <code>true</code> if the method has the given name and parameter types and constructor state.
	 */
//TODO move methods to CModelUtil
	public static boolean isSameMethodSignature(String name, String[] paramTypes, boolean isConstructor, boolean isDestructor, IMethodDeclaration curr) throws CModelException {
		if (isConstructor || isDestructor || name.equals(curr.getElementName())) {
			if ((isConstructor == curr.isConstructor()) && (isDestructor == curr.isDestructor())) {
				String[] currParamTypes= curr.getParameterTypes();
				if (paramTypes.length == currParamTypes.length) {
					for (int i= 0; i < paramTypes.length; i++) {
						String t1= Signature.getSimpleName(Signature.toString(paramTypes[i]));
						String t2= Signature.getSimpleName(Signature.toString(currParamTypes[i]));
						if (!t1.equals(t2)) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Finds a method in a type.
	 * This searches for a method with the same name and signature. Parameter types are only
	 * compared by the simple name, no resolving for the fully qualified type name is done.
	 * Constructors are only compared by parameters, not the name.
	 * @param name The name of the method to find
	 * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
	 * @param isConstructor If the method is a constructor
	 * @return The first found method or <code>null</code>, if nothing found
	 */
//	TODO move methods to CModelUtil
	public static IMethodDeclaration findMethod(String name, String[] paramTypes, boolean isConstructor, boolean isDestructor, ICElement type) throws CModelException {
		return findMethod(name, paramTypes, isConstructor, isDestructor, getMethods(type));
	}

}
