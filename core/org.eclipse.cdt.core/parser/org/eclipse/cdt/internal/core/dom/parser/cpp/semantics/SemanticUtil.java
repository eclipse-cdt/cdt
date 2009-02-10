/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IArrayType;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
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
	
	static final int TYPEDEFS = 0x1;
	static final int REFERENCES = 0x2;
	static final int QUALIFIERS = 0x4;
	static final int PTR_QUALIFIERS= 0x8;
	
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
				if (name[OPERATOR_CHARS.length] == ' ') {
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
				type= ((IPointerType) type).getType();
			} else if (type instanceof IArrayType) {
				type= ((IArrayType) type).getType();
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
	 */
	public static IType getUltimateTypeViaTypedefs(IType type) {
		return getNestedType(type, TYPEDEFS);
	}
	
	/**
	 * Descends into typedefs, references, etc. as specified by options.
	 */
	public static IType getNestedType(IType type, int options) {
		boolean typedefs= (options & TYPEDEFS) != 0;
		boolean refs= (options & REFERENCES) != 0;
		boolean qualifiers= (options & QUALIFIERS) != 0;
		boolean ptrQualifiers= (options & PTR_QUALIFIERS) != 0;
		try {
			while (true) {
				IType t= null;
				if (typedefs && type instanceof ITypedef) {
					t= ((ITypedef) type).getType();
				} else if (refs && type instanceof ICPPReferenceType) {
					t= ((ICPPReferenceType) type).getType();
				} else if (qualifiers && type instanceof IQualifierType) {
					t= ((IQualifierType) type).getType();
				} else if (ptrQualifiers && type instanceof IPointerType) {
					if (type instanceof CPPPointerType) {
						return ((CPPPointerType) type).stripQualifiers();
					}
					IPointerType ptr= (IPointerType) type;
					if (ptr.isConst() || ptr.isVolatile()) {
						if (ptr instanceof ICPPPointerToMemberType) {
							final IType memberOfClass = ((ICPPPointerToMemberType) ptr).getMemberOfClass();
							if (memberOfClass instanceof ICPPClassType)
								return new CPPPointerToMemberType(ptr.getType(), memberOfClass, false, false);
						} else {
							return new CPPPointerType(ptr.getType(), false, false);
						}
					}
				}
				if (t == null)
					return type;
				
				type= t;
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
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

			if (type instanceof ITypedef) {
				IType t= ((ITypedef) type).getType();
				if (t != null)
					return getSimplifiedType(t);
				return type;
			}
			if (type instanceof ITypeContainer) {
				final ITypeContainer tc = (ITypeContainer) type;
				final IType nestedType= tc.getType();
				if (nestedType == null) 
					return type;
				
				IType newType= getSimplifiedType(nestedType);
				if (newType != nestedType) {
					return replaceNestedType(tc, newType);
				} 
				return type;
			} 
		} catch (DOMException e) {
		}
		return type;
	}

	public static IType replaceNestedType(ITypeContainer type, IType newNestedType) throws DOMException {
		// bug 249085 make sure not to add unnecessary qualifications
		if (type instanceof IQualifierType) {
			IQualifierType qt1= (IQualifierType) type;
			if (newNestedType instanceof IQualifierType) {
				IQualifierType qt2= (IQualifierType) newNestedType;
				return new CPPQualifierType(qt2.getType(), qt1.isConst() || qt2.isConst(), qt1.isVolatile() || qt2.isVolatile());
			} else if (newNestedType instanceof IPointerType) {
				IPointerType pt2= (IPointerType) newNestedType;
				return new CPPPointerType(pt2.getType(), qt1.isConst() || pt2.isConst(), qt1.isVolatile() || pt2.isVolatile());
			}
		}
		type = (ITypeContainer) type.clone();
		type.setType(newNestedType);
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
				newArg = getSimplifiedArgument(arg);
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

	public static ICPPTemplateArgument getSimplifiedArgument(final ICPPTemplateArgument arg) {
		if (arg.isTypeValue()) {
			final IType type= arg.getTypeValue();
			final IType newType= getSimplifiedType(type);
			if (newType != type) {
				return new CPPTemplateArgument(newType);
			}
		}
		return arg;
	}

	/**
	 * Adjusts the parameter type according to 8.3.5-3:
	 * cv-qualifiers are deleted, arrays and function types are converted to pointers.
	 */
	public static IType adjustParameterType(final IType pt, boolean forFunctionType) {
		// bug 239975
		IType t= SemanticUtil.getUltimateTypeViaTypedefs(pt);
		try {
			if (t instanceof IArrayType) {
				IArrayType at = (IArrayType) t;
				return new CPPPointerType(at.getType());
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		if (t instanceof IFunctionType) {
			return new CPPPointerType(pt);
		}

		//8.3.5-3 
		//Any cv-qualifier modifying a parameter type is deleted.
		if (forFunctionType && (t instanceof IQualifierType || t instanceof IPointerType)) {
			return SemanticUtil.getNestedType(t, TYPEDEFS | QUALIFIERS | PTR_QUALIFIERS);
		}
		return pt;
	}
	
	public static IType addQualifiers(IType baseType, boolean cnst, boolean vol) {
		if (cnst || vol) {
			try {
				if (baseType instanceof IQualifierType) {
					IQualifierType qt= (IQualifierType) baseType;
					if ((cnst && !qt.isConst()) || (vol && !qt.isVolatile())) {
						return new CPPQualifierType(qt.getType(), cnst || qt.isConst(), vol || qt.isVolatile());
					}
					return baseType;
				} else if (baseType instanceof ICPPPointerToMemberType) {
					ICPPPointerToMemberType pt= (ICPPPointerToMemberType) baseType;
					if ((cnst && !pt.isConst()) || (vol && !pt.isVolatile())) {
						return new CPPPointerToMemberType(pt.getType(), pt.getMemberOfClass(), cnst
								|| pt.isConst(), vol || pt.isVolatile());
					}
					return baseType;
				} else if (baseType instanceof IPointerType) {
					IPointerType pt= (IPointerType) baseType;
					if ((cnst && !pt.isConst()) || (vol && !pt.isVolatile())) {
						return new CPPPointerType(pt.getType(), cnst || pt.isConst(), vol || pt.isVolatile());
					}
					return baseType;
				}
			} catch (DOMException e) {
			} 
			
			return new CPPQualifierType(baseType, cnst, vol);
		}
		return baseType;
	}

}
