/*
 * Created on Jul 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.core.runtime.CoreException;

/**
 * @author CWiebe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TypeUtil {

    public static boolean isDeclaredType(ICElement elem) {
        int type = elem.getElementType();
        return (type == ICElement.C_CLASS
                || type == ICElement.C_STRUCT
                || type == ICElement.C_ENUMERATION
                || type == ICElement.C_UNION
                || type == ICElement.C_TYPEDEF
                || type == ICElement.C_NAMESPACE);
    }

    public static ICElement getDeclaringContainerType(ICElement elem) {
        while (elem != null) {
	        if (elem instanceof IStructure
	                || elem instanceof INamespace
	                || elem instanceof IEnumeration) {
	            return elem;
	        }
	        elem = elem.getParent();
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

    public static ICElement[] getTypes(ICElement elem) {
        final List typeList = new ArrayList(3);
        try {
            elem.accept(new ICElementVisitor() {
                public boolean visit(ICElement element) throws CoreException {
                    // TODO Auto-generated method stub
                    if (element instanceof IStructure) {
                        typeList.add(element);
                    }
                    return false;
                }});
        } catch (CoreException e) {
        }
        return (ICElement[])typeList.toArray(new ICElement[typeList.size()]);
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

	/**
	 * Finds a method declararion in a type's hierarchy. The search is top down, so this
	 * returns the first declaration of the method in the hierarchy.
	 * This searches for a method with a name and signature. Parameter types are only
	 * compared by the simple name, no resolving for the fully qualified type name is done.
	 * Constructors are only compared by parameters, not the name.
	 * @param type Searches in this type's supertypes.
	 * @param name The name of the method to find
	 * @param paramTypes The type signatures of the parameters e.g. <code>{"QString;","I"}</code>
	 * @param isConstructor If the method is a constructor
	 * @return The first method found or null, if nothing found
	 */
//	TODO move methods to CModelUtil
	public static IMethodDeclaration findMethodDeclarationInHierarchy(ITypeHierarchy hierarchy, ICElement type, String name, String[] paramTypes, boolean isConstructor, boolean isDestructor) throws CModelException {
		ICElement[] superTypes= hierarchy.getAllSupertypes(type);
		for (int i= superTypes.length - 1; i >= 0; i--) {
		    IMethodDeclaration first= findMethod(name, paramTypes, isConstructor, isDestructor, superTypes[i]);
			if (first != null && first.getVisibility() != ASTAccessVisibility.PRIVATE) {
				// the order getAllSupertypes does make assumptions of the order of inner elements -> search recursivly
			    IMethodDeclaration res= findMethodDeclarationInHierarchy(hierarchy, TypeUtil.getDeclaringClass(first), name, paramTypes, isConstructor, isDestructor);
				if (res != null) {
					return res;
				}
				return first;
			}
		}
		return null;
	}
	
}
