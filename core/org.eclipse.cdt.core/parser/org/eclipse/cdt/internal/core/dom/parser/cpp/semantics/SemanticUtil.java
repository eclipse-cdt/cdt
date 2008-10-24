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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 *
 */
public class SemanticUtil {
	private static final char[] OPERATOR_CHARS = Keywords.OPERATOR.toCharArray();
	// Cache of overloadable operator names for fast lookup. Used by isConversionOperator.
	private static final CharArraySet cas= new CharArraySet(OverloadableOperator.values().length);
	
	static {
		final int OPERATOR_SPC= OPERATOR_CHARS.length + 1;
		for (OverloadableOperator op : OverloadableOperator.values()) {
			char[] name= op.toCharArray();
			cas.put(CharArrayUtils.subarray(name, OPERATOR_SPC, name.length));
		}
	}
	
	/**
	 * Returns an array of ICPPMethod objects representing all conversion operators
	 * declared by the specified class. This does not include inherited methods. Conversion
	 * operators cannot be implicit.
	 * @param clazz
	 * @return an array of conversion operators.
	 */
	public static final ICPPMethod[] getDeclaredConversionOperators(ICPPClassType clazz) throws DOMException {
		ICPPMethod[] methods= new ICPPMethod[0];
		if (clazz instanceof ICPPDeferredClassInstance) {
			clazz= (ICPPClassType) ((ICPPDeferredClassInstance)clazz).getTemplateDefinition();
		}
		ICPPMethod[] decs= clazz.getDeclaredMethods();
		if (decs != null) {
			for (ICPPMethod method : decs) {
				if (isConversionOperator(method)) {
					methods= (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, methods, method);
				}
			}
		}
		return methods;
	}
	
	/**
	 * Returns an array of ICPPMethod objects representing all conversion operators
	 * declared by the specified class and its ancestors. This includes inherited
	 * methods. Conversion operators cannot be implicit.
	 * @param clazz
	 * @return an array of conversion operators.
	 */
	public static ICPPMethod[] getConversionOperators(ICPPClassType clazz) throws DOMException {
		ICPPMethod[] methods= new ICPPMethod[0];
		ObjectSet<ICPPClassType> ancestry= inheritanceClosure(clazz);
		for (int i = 0; i < ancestry.size(); i++) {
			methods= (ICPPMethod[]) ArrayUtil.addAll(ICPPMethod.class, methods, getDeclaredConversionOperators(ancestry.keyAt(i)));
		}
		return methods;
	}
	
	/**
	 * @param root the class to start at
	 * @return the root and all its ancestor classes
	 * @throws DOMException
	 */
	public static ObjectSet<ICPPClassType> inheritanceClosure(ICPPClassType root) throws DOMException {
		ObjectSet<ICPPClassType> done= new ObjectSet<ICPPClassType>(2);
		ObjectSet<ICPPClassType> current= new ObjectSet<ICPPClassType>(2);
		current.put(root);

		for (int count = 0; count < CPPSemantics.MAX_INHERITANCE_DEPTH && !current.isEmpty(); count++) {
			ObjectSet<ICPPClassType> next= new ObjectSet<ICPPClassType>(2);

			for (int i = 0; i < current.size(); i++) {
				ICPPClassType clazz= current.keyAt(i);				
				done.put(clazz);
				
				for (ICPPBase base : clazz.getBases()) {
					IBinding binding= base.getBaseClass();
					if (binding instanceof ICPPClassType && !(binding instanceof IProblemBinding)) {
						ICPPClassType ct= (ICPPClassType) binding;
						if (!done.containsKey(ct)) {
							next.put(ct);
						}
					}
				}
			}

			current= next;
		}

		return done;
	}
	
	/**
	 * @param method
	 * @return true if the specified method is a conversion operator
	 */
	public static final boolean isConversionOperator(ICPPMethod method) {
		boolean result= false;
		if (!method.isImplicit()) {
			final char[] name= method.getNameCharArray();
			if (name.length > OPERATOR_CHARS.length + 1 &&
					CharArrayUtils.equals(name, 0, OPERATOR_CHARS.length, OPERATOR_CHARS)) {
				if (name[OPERATOR_CHARS.length]==' ') {
					result= !cas.containsKey(name, OPERATOR_CHARS.length+1, name.length - (OPERATOR_CHARS.length+1));
				}
			}
		}
		return result;
	}
	
	/**
	 * Descends into type containers, stopping at pointer-to-member types if
	 * specified.
	 * @param type the root type
	 * @param stopAtPointerToMember if true, do not descend into ICPPPointerToMember types
	 * @return the deepest type in a type container sequence
	 */
	public static IType getUltimateType(IType type, boolean stopAtPointerToMember) {
	   return getUltimateType(type, null, stopAtPointerToMember);
	}
	
	/**
	 * Descends into type containers, stopping at pointer-to-member types if
	 * specified.
	 * @param type the root type
	 * @param lastPointerType if non-null, the deepest pointer type encounter is stored in element zero
	 * @param stopAtPointerToMember if true, do not descend into ICPPPointerToMember types
	 * @return the deepest type in a type container sequence
	 */
	static IType getUltimateType(IType type, IType[] lastPointerType, boolean stopAtPointerToMember) {
	    try {
	        while (true) {
				if (type instanceof ITypedef) {
					IType tt= ((ITypedef) type).getType();
					if (tt == null)
						return type;
					type= tt;
				} else if (type instanceof IQualifierType) {
					type= ((IQualifierType) type).getType();
				} else if (stopAtPointerToMember && type instanceof ICPPPointerToMemberType) {
	                return type;
				} else if (type instanceof IPointerType) {
					if (lastPointerType!=null) {
						lastPointerType[0]= type;
					}
					type= ((IPointerType) type).getType();
				} else if (type instanceof ICPPReferenceType) {
					type= ((ICPPReferenceType) type).getType();
				} else { 
					return type;
				}
				
			}
	    } catch (DOMException e) {
	        return e.getProblem();
	    }
	}

	/**
	 * Descends into type containers, stopping at pointer or
	 * pointer-to-member types.
	 * @param type
	 * @return the ultimate type contained inside the specified type
	 */
	public static IType getUltimateTypeUptoPointers(IType type) {
	    try {
	        while (true) {
				if (type instanceof ITypedef) {
					IType tt= ((ITypedef) type).getType();
					if (tt == null)
						return type;
					type= tt;
				} else if (type instanceof IQualifierType) {
					type = ((IQualifierType) type).getType();
				} else if (type instanceof ICPPReferenceType) {
					type = ((ICPPReferenceType) type).getType();
				} else {
					return type;
				}
			}
	    } catch (DOMException e) {
	        return e.getProblem();
	    }
	}

	/**
	 * Descends into a typedef sequence.
	 * @param type
	 * @return
	 */
	static IType getUltimateTypeViaTypedefs(IType type) {
		try {
			while (type instanceof ITypedef) {
				IType t= ((ITypedef) type).getType();
				if (t == null) 
					return type;
				type= t;
			}
		} catch (DOMException e) {
			type= e.getProblem();
		}
		return type;
	}

	/**
	 * Simplifies type by resolving typedefs within the given type.
	 */
	static IType getSimplifiedType(IType type) {
		try {
			if (type instanceof IFunctionType) {
				IType ret = null;
				IType[] params = null;
				final IType r = ((IFunctionType) type).getReturnType();
				ret = getSimplifiedType(r);
				IType[] ps = ((IFunctionType) type).getParameterTypes();
				params = getSimplifiedTypes(ps);
				if (ret == r && params == ps) {
					return type;
				}
				return new CPPFunctionType(ret, params, ((ICPPFunctionType) type).getThisType());
			} 

			if (type instanceof ITypeContainer) {
				final IType nestedType= ((ITypeContainer) type).getType();
				if (nestedType == null) 
					return type;
				
				IType newType= getSimplifiedType(nestedType);
				if (newType != nestedType) {
					type= (IType) type.clone();
					((ITypeContainer) type).setType(newType);
					return type;
				} 
				return type;
			} 
		} catch (DOMException e) {
		}
		return type;
	}

	public static IType[] getSimplifiedTypes(IType[] types) {
		// Don't create a new array until it's really needed.
		IType[] result = types;
		for (int i = 0; i < types.length; i++) {
			final IType type = types[i];
			final IType newType= getSimplifiedType(type);
			if (result != types) {
				result[i]= newType;
			} else if (type != newType) {
				result = new IType[types.length];
				if (i > 0) {
					System.arraycopy(types, 0, result, 0, i);
				}
				result[i]= newType;
			}
		}
		return result;
	}

	public static ICPPTemplateArgument[] getSimplifiedArguments(ICPPTemplateArgument[] args) {
		// Don't create a new array until it's really needed.
		ICPPTemplateArgument[] result = args;
		for (int i = 0; i < args.length; i++) {
			final ICPPTemplateArgument arg= args[i];
			ICPPTemplateArgument newArg= arg;
			if (arg != null) {
				if (arg.isTypeValue()) {
					final IType type= arg.getTypeValue();
					final IType newType= getSimplifiedType(type);
					if (newType != type) {
						newArg= new CPPTemplateArgument(newType);
					}
				}
				if (result != args) {
					result[i]= newArg;
				} else if (arg != newArg) {
					result = new ICPPTemplateArgument[args.length];
					if (i > 0) {
						System.arraycopy(args, 0, result, 0, i);
					}
					result[i]= newArg;
				}
			}
		}
		return result;
	}
}
