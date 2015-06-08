/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Nathan Ridge
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
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.index.IIndexScope;

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
	 * declared by the specified class, and the implicitly generated conversion
	 * operator for a closure type. This does not include inherited methods.
	 *
	 * @param clazz
	 * @return an array of conversion operators.
	 */
	public static final ICPPMethod[] getDeclaredConversionOperators(ICPPClassType clazz, IASTNode point) throws DOMException {
		ICPPMethod[] conversionOps= ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		if (clazz instanceof ICPPDeferredClassInstance) {
			clazz= (ICPPClassType) ((ICPPDeferredClassInstance) clazz).getTemplateDefinition();
		}
		ICPPMethod[] methods;
		// For a closure type, getDeclaredMethods() does not return the conversion
		// operator because it is implicitly generated. We can use getMethods()
		// however as a closure type does not have base classes.
		// A new API ICPPClosureType.getNoninheritedMethods() might be more
		// appropriate here.
		if (clazz instanceof CPPClosureType) {
			methods = ClassTypeHelper.getMethods(clazz, point);
		} else {
			methods = ClassTypeHelper.getDeclaredMethods(clazz, point);
		}
		if (methods != null) {
			for (ICPPMethod method : methods) {
				if (isConversionOperator(method)) {
					conversionOps= ArrayUtil.append(conversionOps, method);
				}
			}
		}
		return conversionOps;
	}

	/**
	 * Returns an array of ICPPMethod objects representing all conversion operators
	 * declared by the specified class and its ancestors. This includes inherited
	 * methods, and the implicitly generated conversion operator for a closure type.
	 *
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
		ObjectSet<ICPPClassType> done= new ObjectSet<>(2);
		ObjectSet<ICPPClassType> current= new ObjectSet<>(2);
		current.put(root);

		for (int count = 0; count < CPPSemantics.MAX_INHERITANCE_DEPTH && !current.isEmpty(); count++) {
			ObjectSet<ICPPClassType> next= new ObjectSet<>(2);

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
	public static IType getSimplifiedType(IType type) {
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
			return new CPPFunctionType(ret, params, ft.isConst(), ft.isVolatile(),
					ft.hasRefQualifier(), ft.isRValueReference(), ft.takesVarArgs());
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

		// Do not to add unnecessary qualifications (bug 24908). 
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

	/**
	 * Checks if the given type is problem-free.
	 */
	public static boolean isValidType(IType t) {
		while (true) {
			if (t instanceof ISemanticProblem) {
				return false;
			} else if (t instanceof IFunctionType) {
				IFunctionType ft= (IFunctionType) t;
				for (IType parameterType : ft.getParameterTypes()) {
					if (!isValidType(parameterType))
						return false;
				}
				t= ft.getReturnType();
			} else if (t instanceof ICPPPointerToMemberType) {
				ICPPPointerToMemberType mptr= (ICPPPointerToMemberType) t;
				if (!isValidType(mptr.getMemberOfClass()))
					return false;
				t= mptr.getType();
			} else if (t instanceof ITypeContainer) {
				t= ((ITypeContainer) t).getType();
			} else {
				return true;
			}
		}
	}

	public static IType mapToAST(IType type, IASTNode point) {
		if (point != null && type instanceof IIndexBinding && type instanceof ICPPClassType) {
			IASTTranslationUnit ast = point.getTranslationUnit();
			if (ast instanceof CPPASTTranslationUnit) {
				return ((CPPASTTranslationUnit) ast).mapToAST((ICPPClassType) type, point);
			}
		}
		return type;
	}

	public static ICPPTemplateArgument[] mapToAST(ICPPTemplateArgument[] args, IASTNode point) {
		if (point == null)
			return args;

		// Don't create a new array until it's really needed.
		ICPPTemplateArgument[] result = args;
		for (int i = 0; i < args.length; i++) {
			final ICPPTemplateArgument arg = args[i];
			ICPPTemplateArgument newArg = arg;
			if (arg != null) {
				newArg = mapToAST(arg, point);
				if (result != args) {
					result[i] = newArg;
				} else if (arg != newArg) {
					result = new ICPPTemplateArgument[args.length];
					if (i > 0) {
						System.arraycopy(args, 0, result, 0, i);
					}
					result[i] = newArg;
				}
			}
		}
		return result;
	}

	public static ICPPTemplateArgument mapToAST(ICPPTemplateArgument arg, IASTNode point) {
		IType type = arg.getTypeValue();
		if (type != null) {
			IType mappedType = mapToAST(type, point);
			IType originalType = arg.getOriginalTypeValue();
			IType mappedOriginalType = originalType == type ? mappedType : mapToAST(originalType, point);
			if (mappedType != type || mappedOriginalType != originalType) {
				return new CPPTemplateTypeArgument(mappedType, mappedOriginalType);
			}
		}
		return arg;
	}

	public static IScope mapToAST(IScope scope, IASTNode point) {
		if (scope instanceof IIndexScope && point != null) {
			IASTTranslationUnit ast = point.getTranslationUnit();
			if (ast instanceof ASTTranslationUnit) {
				return ((ASTTranslationUnit) ast).mapToASTScope(scope);
			}
		}
		return scope;
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
				return new CPPTemplateTypeArgument(newType, arg.getOriginalTypeValue());
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
	 * Returns {@code true} if two bindings have the same owner.
	 */
	public static boolean haveSameOwner(IBinding b1, IBinding b2) {
		if (b1 == b2)
			return true;
		b1 = b1.getOwner();
		b2 = b2.getOwner();
		if (b1 == b2)
			return true;
		if (b1 instanceof IType) {
			if (b2 instanceof IType)
				return ((IType) b1).isSameType((IType) b2);
			return false;
		}
		if (b1 instanceof ICPPNamespace && b2 instanceof ICPPNamespace)
			return isSameNamespace((ICPPNamespace) b1, (ICPPNamespace) b2);
		return false;
	}

	/**
	 * Returns {@code true} if the two given bindings represent the same type or namespace.
	 */
	public static boolean isSameNamespace(ICPPNamespace ns1, ICPPNamespace ns2) {
		IBinding b1 = ns1;
		IBinding b2 = ns2;

		while (true) {
			for (int i = 0; b1 instanceof ICPPNamespaceAlias && i < 20; i++)
				b1= ((ICPPNamespaceAlias) b1).getBinding();
			for (int i = 0; b2 instanceof ICPPNamespaceAlias && i < 20; i++)
				b2= ((ICPPNamespaceAlias) b2).getBinding();
	
			if (b1 == null)
				return b2 == null;
			if (b2 == null)
				return false;
	
			if (!(b1 instanceof ICPPNamespace) || !(b2 instanceof ICPPNamespace))
				return false;
			if (!CharArrayUtils.equals(b1.getNameCharArray(), b2.getNameCharArray()))
				return false;
			b1 = b1.getOwner();
			b2 = b2.getOwner();
		}
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
	 * Calculates the number of edges in the inheritance path of {@code type} to
	 * {@code ancestorToFind}, returning -1 if no inheritance relationship is found.
	 *
	 * @param type the class to search upwards from
	 * @param baseClass the class to find in the inheritance graph
	 * @return the number of edges in the inheritance graph, or -1 if the specified classes have
	 * 	   no inheritance relation
	 */
	public static final int calculateInheritanceDepth(IType type, IType baseClass, IASTNode point) {
		return calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, new HashSet<>(), type, baseClass, point);
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

			// The base classes may have changed since the definition of clazz was indexed.
			clazz = (ICPPClassType) mapToAST(clazz, point);

			for (ICPPBase cppBase : ClassTypeHelper.getBases(clazz, point)) {
				IBinding base= cppBase.getBaseClass();
				if (base instanceof IType && hashSet.add(base)) {
					IType tbase= (IType) base;
					if (tbase.isSameType(baseClass) ||
							(baseClass instanceof ICPPSpecialization && // Allow some flexibility with templates.  
							((IType) ((ICPPSpecialization) baseClass).getSpecializedBinding()).isSameType(tbase))) {
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

	public static boolean isUniqueTypeForParameterPack(IType type) {
		if (type instanceof UniqueType) {
			return ((UniqueType) type).isForParameterPack();
		}
		return false;
	}

	public static long computeMaxValue(IEnumeration enumeration) {
		long maxValue = Long.MIN_VALUE;
		IEnumerator[] enumerators = enumeration.getEnumerators();
		for (IEnumerator enumerator : enumerators) {
			IValue value = enumerator.getValue();
			if (value != null) {
				Long val = value.numericalValue();
				if (val != null) {
					long v = val.longValue();
					if (v > maxValue) {
						maxValue = v;
					}
				}
			}
		}
		return maxValue;
	}

	public static long computeMinValue(IEnumeration enumeration) {
		long minValue = Long.MAX_VALUE;
		IEnumerator[] enumerators = enumeration.getEnumerators();
		for (IEnumerator enumerator : enumerators) {
			IValue value = enumerator.getValue();
			if (value != null) {
				Long val = value.numericalValue();
				if (val != null) {
					long v = val.longValue();
					if (v < minValue) {
						minValue = v;
					}
				}
			}
		}
		return minValue;
	}

	public static int findSameType(IType type, IType[] types) {
		for (int i = 0; i < types.length; i++) {
			if (type.isSameType(types[i]))
				return i;
		}
		return -1;
	}
	
	/**
	 * Returns the value of the initializer of a variable.
	 *
	 * @param init the initializer's AST node
	 * @param type the type of the variable
	 */
	public static IValue getValueOfInitializer(IASTInitializer init, IType type) {
		IASTInitializerClause clause= null;
		if (init instanceof IASTEqualsInitializer) {
			clause= ((IASTEqualsInitializer) init).getInitializerClause();
		} else if (init instanceof ICPPASTConstructorInitializer) {
			IASTInitializerClause[] args= ((ICPPASTConstructorInitializer) init).getArguments();
			if (args.length == 1 && args[0] instanceof IASTExpression) {
				IType typeUpToPointers= SemanticUtil.getUltimateTypeUptoPointers(type);
				if (typeUpToPointers instanceof IPointerType || typeUpToPointers instanceof IBasicType) {
					clause= args[0];
				}
			}
		} else if (init instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList list= (ICPPASTInitializerList) init;
			switch (list.getSize()) {
			case 0:
				return Value.create(0);
			case 1:
				clause= list.getClauses()[0];
			}
		}
		if (clause instanceof IASTExpression) {
			return Value.create((IASTExpression) clause);
		}
		return Value.UNKNOWN;
	}
}
