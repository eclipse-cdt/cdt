/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.CONST;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.CONST_RESTRICT;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.CONST_VOLATILE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.CONST_VOLATILE_RESTRICT;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.NONE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.RESTRICT;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.VOLATILE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier.VOLATILE_RESTRICT;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 * Collection of static methods operating on C++ bindings.
 */
public class SemanticUtil {
	private static final char[] OPERATOR_CHARS = Keywords.OPERATOR.toCharArray();
	// Cache of overloadable operator names for fast lookup. Used by isConversionOperator.
	private static final CharArraySet cas= new CharArraySet(OverloadableOperator.values().length);

	// Resolve typedefs.
	public static final int TDEF =      0x01;
	// Resolve typedefs, but only if necessary for a nested type transformation.
	public static final int COND_TDEF = 0x02; 
	public static final int REF =       0x04;
	public static final int CVTYPE =    0x08;
	public static final int ALLCVQ =    0x10;
	public static final int PTR =       0x20;
	public static final int MPTR =      0x40;
	public static final int ARRAY =     0x80;
	
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
	public static final ICPPMethod[] getDeclaredConversionOperators(ICPPClassType clazz, IASTNode point) throws DOMException {
		ICPPMethod[] methods= ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		if (clazz instanceof ICPPDeferredClassInstance) {
			clazz= (ICPPClassType) ((ICPPDeferredClassInstance) clazz).getTemplateDefinition();
		}
		ICPPMethod[] decs= ClassTypeHelper.getDeclaredMethods(clazz, point);
		if (decs != null) {
			for (ICPPMethod method : decs) {
				if (isConversionOperator(method)) {
					methods= ArrayUtil.append(methods, method);
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
	public static ICPPMethod[] getConversionOperators(ICPPClassType clazz, IASTNode point) throws DOMException {
		ICPPMethod[] methods= ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		ObjectSet<ICPPClassType> ancestry= inheritanceClosure(clazz, point);
		for (int i = 0; i < ancestry.size(); i++) {
			methods= ArrayUtil.addAll(methods, getDeclaredConversionOperators(ancestry.keyAt(i), point));
		}
		return methods;
	}
	
	/**
	 * @param root the class to start at
	 * @return the root and all its ancestor classes
	 * @throws DOMException
	 */
	public static ObjectSet<ICPPClassType> inheritanceClosure(ICPPClassType root, IASTNode point) throws DOMException {
		ObjectSet<ICPPClassType> done= new ObjectSet<ICPPClassType>(2);
		ObjectSet<ICPPClassType> current= new ObjectSet<ICPPClassType>(2);
		current.put(root);

		for (int count = 0; count < CPPSemantics.MAX_INHERITANCE_DEPTH && !current.isEmpty(); count++) {
			ObjectSet<ICPPClassType> next= new ObjectSet<ICPPClassType>(2);

			for (int i = 0; i < current.size(); i++) {
				ICPPClassType clazz= current.keyAt(i);				
				done.put(clazz);

				for (ICPPBase base : ClassTypeHelper.getBases(clazz, point)) {
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
	public static final boolean isConversionOperator(ICPPFunction method) {
		if (method instanceof ICPPMethod) {
			final char[] name= method.getNameCharArray();
			if (name.length > OPERATOR_CHARS.length + 1 && name[OPERATOR_CHARS.length] == ' ' &&
					CharArrayUtils.equals(name, 0, OPERATOR_CHARS.length, OPERATOR_CHARS)) {
				return !cas.containsKey(name, OPERATOR_CHARS.length + 1, name.length - (OPERATOR_CHARS.length+1));
			}
		}
		return false;
	}
	
	public static CVQualifier getCVQualifier(IType t) {
		if (t instanceof IQualifierType) {
			IQualifierType qt= (IQualifierType) t;
			return qt.isConst() 
				? qt.isVolatile() ? CONST_VOLATILE : CONST
				: qt.isVolatile() ? VOLATILE : NONE;
		} 
		if (t instanceof IPointerType) {
			IPointerType pt= (IPointerType) t;
			return pt.isConst() 
					? pt.isVolatile() 
							? pt.isRestrict() ? CONST_VOLATILE_RESTRICT : CONST_VOLATILE
							: pt.isRestrict() ? CONST_RESTRICT : CONST
					: pt.isVolatile() 
							? pt.isRestrict() ? VOLATILE_RESTRICT : VOLATILE
							: pt.isRestrict() ? RESTRICT : NONE;
		}
		if (t instanceof IArrayType) {
			return getCVQualifier(((IArrayType) t).getType());
		}
		return NONE;
	}
		
	/**
	 * Descends into type containers, stopping at pointer-to-member types if
	 * specified.
	 * @param type the root type
	 * @param stopAtPointerToMember if true, do not descend into ICPPPointerToMember types
	 * @return the deepest type in a type container sequence
	 */
	public static IType getUltimateType(IType type, boolean stopAtPointerToMember) {
		final int options = TDEF | ALLCVQ | PTR | ARRAY | REF;
		return getNestedType(type, stopAtPointerToMember ? options : (options | MPTR));
	}
	
	/**
	 * Descends into type containers, stopping at array, pointer or
	 * pointer-to-member types.
	 * @param type
	 * @return the ultimate type contained inside the specified type
	 */
	public static IType getUltimateTypeUptoPointers(IType type) {
		return getNestedType(type, TDEF | REF | CVTYPE);
	}

	/**
	 * Descends into typedefs, references, etc. as specified by options.
	 */
	public static IType getNestedType(IType type, int options) {
		final boolean tdef= (options & TDEF) != 0;
		final boolean cond_tdef= (options & COND_TDEF) != 0;
		final boolean ptr= (options & PTR) != 0;
		final boolean mptr= (options & MPTR) != 0;
		final boolean allcvq= (options & ALLCVQ) != 0;
		final boolean cvtype= (options & CVTYPE) != 0;

		IType beforeTypedefs = null;

		while (true) {
			IType t= null;
			if (type instanceof ITypedef) {
				if (tdef || cond_tdef) {
					if (beforeTypedefs == null && cond_tdef) {
						beforeTypedefs = type;
					}
					t= ((ITypedef) type).getType();
				}
			} else if (type instanceof IPointerType) {
				final boolean isMbrPtr = type instanceof ICPPPointerToMemberType;
				if ((ptr && !isMbrPtr) || (mptr && isMbrPtr)) {
					t= ((IPointerType) type).getType();
					beforeTypedefs = null;
				} else if (allcvq) {
					IPointerType pt= (IPointerType) type;
					if (pt.isConst() || pt.isVolatile() || pt.isRestrict()) {
						if (pt instanceof ICPPPointerToMemberType) {
							final IType memberOfClass = ((ICPPPointerToMemberType) pt).getMemberOfClass();
							return new CPPPointerToMemberType(pt.getType(), memberOfClass, false, false, false);
						} else {
							return new CPPPointerType(pt.getType(), false, false, false);
						}
					}
				}
			} else if (type instanceof IQualifierType) {
				final IQualifierType qt = (IQualifierType) type;
				final IType qttgt = qt.getType();
				if (allcvq || cvtype) {
					t= qttgt;
					beforeTypedefs = null;
				} else if (tdef || cond_tdef) {
					t= getNestedType(qttgt, options);
					if (t == qttgt) 
						return qt;
					return addQualifiers(t, qt.isConst(), qt.isVolatile(), false);
				} 
			} else if (type instanceof IArrayType) {
				final IArrayType atype= (IArrayType) type;
				if ((options & ARRAY) != 0) {
					t= atype.getType();
					beforeTypedefs = null;
				} else if (allcvq) {
					IType nested= atype.getType();
					IType newNested= getNestedType(nested, ALLCVQ);
					if (nested == newNested)
						return type;
					return replaceNestedType((ITypeContainer) atype, newNested);
				}
			} else if (type instanceof ICPPReferenceType) {
				final ICPPReferenceType rt = (ICPPReferenceType) type;
				if ((options & REF) != 0) {
					t= rt.getType();
					beforeTypedefs = null;
				} else if (tdef) {
					// A typedef within the reference type can influence whether the reference is lvalue or rvalue
					IType nested= rt.getType();
					IType newNested = getNestedType(nested, TDEF);
					if (nested == newNested) 
						return type;
					return replaceNestedType((ITypeContainer) rt, newNested);
				}
			}
			// Pack expansion types are dependent types, there is no need to descend into those.
			if (t == null) {
				if (beforeTypedefs != null) {
					return beforeTypedefs;
				}
				return type;
			}
			
			type= t;
		}
	}

	/**
	 * Simplifies type by resolving typedefs within the given type.
	 */
	static IType getSimplifiedType(IType type) {
		if (type instanceof ICPPFunctionType) {
			final ICPPFunctionType ft = (ICPPFunctionType) type;
			IType ret = null;
			IType[] params = null;
			final IType r = ft.getReturnType();
			ret = getSimplifiedType(r);
			IType[] ps = ft.getParameterTypes();
			params = getSimplifiedTypes(ps);
			if (ret == r && params == ps) {
				return type;
			}
			return new CPPFunctionType(ret, params, ft.isConst(), ft.isVolatile(), ft.takesVarArgs());
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
		return type;
	}
	
	static boolean isSimplified(IType type) {
		if (type instanceof ICPPFunctionType) {
			final ICPPFunctionType ft = (ICPPFunctionType) type;
			if (!isSimplified(ft.getReturnType()))
				return false;
			
			IType[] ps = ft.getParameterTypes();
			for (IType p : ps) {
				if (!isSimplified(p))
					return false;
			}
			return true;
		} 
		if (type instanceof ITypedef) {
			return false;
		}
		if (type instanceof ITypeContainer) {
			return isSimplified(((ITypeContainer) type).getType());
		}
		return true;
	}

	public static IType replaceNestedType(ITypeContainer type, IType newNestedType) {
		if (newNestedType == null)
			return type;
		
		// Bug 249085 make sure not to add unnecessary qualifications
		if (type instanceof IQualifierType) {
			IQualifierType qt= (IQualifierType) type;
			return addQualifiers(newNestedType, qt.isConst(), qt.isVolatile(), false);
		}

		type = (ITypeContainer) type.clone();
		type.setType(newNestedType);
		return type;
	}

	/**
	 * Replaces the given type or its nested type with a typedef if that type is the same as
	 * the type the typedef resolves to. 
	 * 
	 * @param type the type subject to substitution 
	 * @param typedefType the type possibly containing the typedef as its nested type.
	 * @return the given type with the nested type replaced by the typedef, or {@code null} if
	 *	   the typedefType doesn't contain a typedef or the nested type doesn't match the typedef.
	 */
	public static IType substituteTypedef(IType type, IType typedefType) {
		typedefType = getNestedType(typedefType, REF | ALLCVQ | PTR | ARRAY);
		if (!(typedefType instanceof ITypedef))
			return null;
		IType nestedType = type;
		while (!nestedType.isSameType(((ITypedef) typedefType).getType())) {
			if (nestedType instanceof IQualifierType) {
				nestedType = ((IQualifierType) nestedType).getType();
			} else if (nestedType instanceof IPointerType) {
				nestedType = ((IPointerType) nestedType).getType();
			} else if (nestedType instanceof IArrayType) {
				nestedType = ((IArrayType) nestedType).getType();
			} else if (nestedType instanceof ICPPReferenceType) {
				nestedType = ((ICPPReferenceType) nestedType).getType();
			} else {
				return null;
			}
		}

		IType result = null;
		ITypeContainer containerType = null;
		for (IType t = type; ; t = containerType.getType()) {
			IType newType = t == nestedType ? typedefType : (IType) t.clone();
			if (result == null)
				result = newType;
			if (containerType != null) {
				containerType.setType(newType);
			}
			if (t == nestedType)
				return result;
			if (!(t instanceof ITypeContainer))
				return null;
			containerType = (ITypeContainer) t;
		}
	}

	public static IType mapToAST(IType type, IASTNode node) {
		if (node == null)
			return type;
		
		if (type instanceof IFunctionType) {
			final ICPPFunctionType ft = (ICPPFunctionType) type;
			final IType r = ft.getReturnType();
			final IType ret = mapToAST(r, node);
			if (ret == r) {
				return type;
			}
			return new CPPFunctionType(ret, ft.getParameterTypes(), ft.isConst(), ft.isVolatile(), ft.takesVarArgs());
		}
		if (type instanceof ITypeContainer) {
			final ITypeContainer tc = (ITypeContainer) type;
			final IType nestedType= tc.getType();
			if (nestedType == null) 
				return type;
			
			IType newType= mapToAST(nestedType, node);
			if (newType != nestedType) {
				return replaceNestedType(tc, newType);
			} 
			return type;
		} else if (type instanceof ICPPClassType && type instanceof IIndexBinding) {
			IASTTranslationUnit tu = node.getTranslationUnit();
			if (tu instanceof CPPASTTranslationUnit) {
				return ((CPPASTTranslationUnit) tu).mapToAST((ICPPClassType) type, node);
			}
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
				return new CPPTemplateTypeArgument(newType);
			}
		}
		return arg;
	}

	public static IType constQualify(IType baseType) {
		return addQualifiers(baseType, true, false, false);
	}

	public static IType addQualifiers(IType baseType, boolean cnst, boolean vol, boolean restrict) {
		if (cnst || vol || restrict) {
			if (baseType instanceof IQualifierType) {
				IQualifierType qt= (IQualifierType) baseType;
				if ((cnst && !qt.isConst()) || (vol && !qt.isVolatile())) {
					return new CPPQualifierType(qt.getType(), cnst || qt.isConst(), vol || qt.isVolatile());
				}
				return baseType;
			} else if (baseType instanceof ICPPPointerToMemberType) {
				ICPPPointerToMemberType pt= (ICPPPointerToMemberType) baseType;
				if ((cnst && !pt.isConst()) || (vol && !pt.isVolatile()) 
						|| (restrict && !pt.isRestrict())) {
					return new CPPPointerToMemberType(pt.getType(), pt.getMemberOfClass(), 
							cnst || pt.isConst(), vol || pt.isVolatile(), restrict || pt.isRestrict());
				}
				return baseType;
			} else if (baseType instanceof IPointerType) {
				IPointerType pt= (IPointerType) baseType;
				if ((cnst && !pt.isConst()) || (vol && !pt.isVolatile()) 
						|| (restrict && !pt.isRestrict())) {
					return new CPPPointerType(pt.getType(), 
							cnst || pt.isConst(), vol || pt.isVolatile(), restrict || pt.isRestrict());
				}
				return baseType;
			} else if (baseType instanceof IArrayType) {
				IArrayType at= (IArrayType) baseType;
				IType nested= at.getType();
				IType newNested= addQualifiers(nested, cnst, vol, restrict);
				if (newNested != nested && at instanceof ITypeContainer) {
					return replaceNestedType((ITypeContainer) at, newNested);
				}
				return at;
			} else if (baseType instanceof ICPPReferenceType) {
				return baseType;
			} else if (baseType == null) {
				return null;
			}
			
			return new CPPQualifierType(baseType, cnst, vol);
		}
		return baseType;
	}

	/**
	 * Returns <code>true</code> if two bindings have the same owner.
	 */
	public static boolean isSameOwner(IBinding owner1, IBinding owner2) {
		// Ignore anonymous namespaces
		while (owner1 instanceof ICPPNamespace && owner1.getNameCharArray().length == 0)
			owner1= owner1.getOwner();
		// Ignore anonymous namespaces
		while (owner2 instanceof ICPPNamespace && owner2.getNameCharArray().length == 0)
			owner2= owner2.getOwner();

		if (owner1 == null)
			return owner2 == null;
		if (owner2 == null)
			return false;

		if (owner1 instanceof IType) {
			if (owner2 instanceof IType) {
				return ((IType) owner1).isSameType((IType) owner2);
			}
		} else if (owner1 instanceof ICPPNamespace) {
			if (owner2 instanceof ICPPNamespace) {
				if (!CharArrayUtils.equals(owner1.getNameCharArray(), owner2.getNameCharArray())) 
					return false;
				return isSameOwner(owner1.getOwner(), owner2.getOwner());
			}
		}
		return false;
	}

	public static boolean isVoidType(IType ptype) {
		while (ptype instanceof ITypedef) {
			ptype= ((ITypedef) ptype).getType();
		}
		if (ptype instanceof IBasicType) {
			return ((IBasicType) ptype).getKind() == Kind.eVoid;
		}
		return false;
	}

	public static boolean isEmptyParameterList(IType[] parameters) {
		if (parameters.length == 0) {
			return true;
		} 
		if (parameters.length == 1 && isVoidType(parameters[0])) {
			return true;
		}
		return false;
	}

	/**
	 * Calculates the number of edges in the inheritance path of <code>type</code> to
	 * <code>ancestorToFind</code>, returning -1 if no inheritance relationship is found.
	 * @param type the class to search upwards from
	 * @param baseClass the class to find in the inheritance graph
	 * @return the number of edges in the inheritance graph, or -1 if the specified classes have
	 * no inheritance relation
	 */
	public static final int calculateInheritanceDepth(IType type, IType baseClass, IASTNode point) {
		return calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, new HashSet<Object>(), type, baseClass, point);
	}
	
	private static final int calculateInheritanceDepth(int maxdepth, Set<Object> hashSet, IType type, IType baseClass, IASTNode point) {
		if (type == baseClass || type.isSameType(baseClass)) {
			return 0;
		}
	
		if (maxdepth > 0 && type instanceof ICPPClassType && baseClass instanceof ICPPClassType) {
			ICPPClassType clazz = (ICPPClassType) type;
			if (clazz instanceof ICPPDeferredClassInstance) {
				clazz= (ICPPClassType) ((ICPPDeferredClassInstance) clazz).getSpecializedBinding();
			}
			
			for (ICPPBase cppBase : ClassTypeHelper.getBases(clazz, point)) {
				IBinding base= cppBase.getBaseClass();
				if (base instanceof IType && hashSet.add(base)) {
					IType tbase= (IType) base;
					if (tbase.isSameType(baseClass) || 
							(baseClass instanceof ICPPSpecialization &&  // allow some flexibility with templates 
							((IType)((ICPPSpecialization) baseClass).getSpecializedBinding()).isSameType(tbase))) {
						return 1;
					}
	
					if (tbase instanceof ICPPClassType) {
						int n= calculateInheritanceDepth(maxdepth - 1, hashSet, tbase, baseClass, point);
						if (n > 0)
							return n + 1;
					}
				}
			}
		}
	
		return -1;
	}

	public static boolean containsUniqueTypeForParameterPack(IType type) {
		if (type instanceof ICPPFunctionType) {
			final ICPPFunctionType ft = (ICPPFunctionType) type;
			if (containsUniqueTypeForParameterPack(ft.getReturnType()))
				return true;
			
			for (IType pt : ft.getParameterTypes()) {
				if (containsUniqueTypeForParameterPack(pt))
					return true;
			}
			return false;
		} 
		
		if (type instanceof ICPPPointerToMemberType) {
			if (containsUniqueTypeForParameterPack(((ICPPPointerToMemberType) type).getMemberOfClass()))
				return true;
		}

		if (type instanceof IBinding) {
			IBinding owner = ((IBinding) type).getOwner();
			if (owner instanceof IType) {
				if (containsUniqueTypeForParameterPack((IType) owner))
					return true;
			}
		}
		
		if (type instanceof ICPPTemplateInstance) {
			ICPPTemplateArgument[] args = ((ICPPTemplateInstance) type).getTemplateArguments();
			for (ICPPTemplateArgument arg : args) {
				if (containsUniqueTypeForParameterPack(arg.getTypeValue()))
					return true;
			}
		}

		if (type instanceof ITypeContainer) {
			final ITypeContainer tc = (ITypeContainer) type;
			final IType nestedType= tc.getType();
			return containsUniqueTypeForParameterPack(nestedType);
		}
		
		if (type instanceof UniqueType) {
			return ((UniqueType) type).isForParameterPack();
		}
		return false;
	}
}
