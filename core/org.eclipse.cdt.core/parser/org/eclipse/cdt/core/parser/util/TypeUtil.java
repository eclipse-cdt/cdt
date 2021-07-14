package org.eclipse.cdt.core.parser.util;

import java.util.EnumSet;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTypedef;

public class TypeUtil {

	public static boolean isReference(IType type, IType... types) {
		if (!isReference(type)) {
			return false;
		}

		for (IType typ : types) {
			if (!isReference(typ)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isReference(IType type) {
		return type instanceof ICPPReferenceType;
	}

	public static boolean isRValueReference(IType toCheck, IType... types) {
		if (!isRValueReference(toCheck)) {
			return false;
		}

		for (IType type : types) {
			if (!isRValueReference(type)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isRValueReference(IType toCheck) {
		if (isReference(toCheck)) {
			return ((ICPPReferenceType) toCheck).isRValueReference();
		} else if (isFunction(toCheck)) {
			return ((ICPPFunctionType) toCheck).isRValueReference();
		}
		return false;
	}

	public static boolean isFunction(IType toCheck) {
		return toCheck instanceof ICPPFunctionType;
	}

	public static boolean isPointer(IType type) {
		return type instanceof IPointerType;
	}

	public static boolean isAbtract(IType type) {
		if (isClass(type)) {
			for (ICPPMethod m : ((ICPPClassType) type).getMethods()) {
				if (m.isVirtual()) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isClass(IType type, IType... types) {
		if (!isClass(type)) {
			return false;
		}

		for (IType tp : types) {
			if (!isClass(tp)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isClass(IType type) {
		if (!(type instanceof ICPPClassType)) {
			return false;
		}

		return true;
	}

	public static boolean isEnum(IType type, IType... types) {
		if (!isEnum(type)) {
			return false;
		}

		for (IType tp : types) {
			if (!isEnum(tp)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isEnum(IType type) {
		if (!(type instanceof ICPPEnumeration)) {
			return false;
		}

		return true;
	}

	public static boolean isNumeric(IType type, IType... types) {
		if (!isNumeric(type)) {
			return false;
		}

		for (IType typ : types) {
			if (!isNumeric(typ)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isNumeric(IType type) {
		if (type instanceof ICPPReferenceType) {
			return isNumeric(((ICPPReferenceType) type).getType());
		}
		if (type instanceof CPPBasicType) {
			CPPBasicType basicType = (CPPBasicType) type;
			return EnumSet.of(Kind.eInt, Kind.eFloat, Kind.eDouble, Kind.eInt128, Kind.eFloat128, Kind.eDecimal32,
					Kind.eDecimal64, Kind.eDecimal128).contains(basicType.getKind());
		}
		return false;
	}

	public static boolean isBool(IType type) {
		if (type instanceof CPPBasicType) {
			CPPBasicType basicType = (CPPBasicType) type;
			return EnumSet.of(Kind.eBoolean).contains(basicType.getKind());
		}
		return false;
	}

	public static boolean isConst(IType toCheck, IType... types) {
		if (!isConst(toCheck)) {
			return false;
		}

		for (IType typ : types) {
			if (!isConst(typ)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isConst(IType toCheck) {
		if (toCheck instanceof IQualifierType) {
			return ((IQualifierType) toCheck).isConst();
		} else if (toCheck instanceof ICPPFunctionType) {
			return ((ICPPFunctionType) toCheck).isConst();
		} else if (toCheck instanceof ICPPReferenceType) {
			return isConst(((ICPPReferenceType) toCheck).getType());
		} else if (toCheck instanceof IArrayType) {
			return isConst(((IArrayType) toCheck).getType());
		}

		return false;
	}

	public static boolean isConstReference(IType toCheck, IType... types) {
		if (!isConstReference(toCheck)) {
			return false;
		}

		for (IType typ : types) {
			if (!isConstReference(typ)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isConstReference(IType toCheck) {
		if (isReference(toCheck)) {
			return isConst(((ICPPReferenceType) toCheck).getType());
		}
		return false;
	}

	public static boolean isUnConstReference(IType toCheck) {
		if (isReference(toCheck)) {
			return !isConst(((ICPPReferenceType) toCheck).getType());
		}
		return false;
	}

	public static boolean isVolatile(IType type) {
		if (type instanceof ICPPReferenceType) {
			return isVolatile((ICPPReferenceType) type);
		} else if (type instanceof IQualifierType) {
			return isVolatile((IQualifierType) type);
		} else if (type instanceof IArrayType) {
			return isVolatile(((IArrayType) type).getType());
		}
		return false;
	}

	public static boolean isVolatile(IQualifierType type) {
		return type.isVolatile();
	}

	public static boolean isVolatile(ICPPReferenceType type) {
		if (type.getType() instanceof CPPQualifierType) {
			return isVolatile((CPPQualifierType) type.getType());
		} else if (type.getType() instanceof IArrayType) {
			return isVolatile(((IArrayType) type.getType()).getType());
		} else {
			return false;
		}
	}

	public static boolean isArray(IType toCheck, IType... types) {
		if (!isArray(toCheck)) {
			return false;
		}

		for (IType typ : types) {
			if (!isArray(typ)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isArray(IType toCheck) {
		if (toCheck instanceof IArrayType) {
			return true;
		} else if (toCheck instanceof ICPPReferenceType) {
			return isArray(((ICPPReferenceType) toCheck).getType());
		}
		return false;
	}

	public static boolean isUnion(IType toCheck) {
		if (toCheck instanceof CPPClassType) {
			IASTNode parent = ((CPPClassType) toCheck).getDefinition().getParent();

			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier compositeTypeSpecifier = (ICPPASTCompositeTypeSpecifier) parent;
				int key = compositeTypeSpecifier.getKey();
				return key == IASTCompositeTypeSpecifier.k_union;
			}
		}
		return false;
	}

	public static IType getChild(IType parent) {
		if (parent instanceof ICPPReferenceType) {
			return ((ICPPReferenceType) parent).getType();
		} else if (parent instanceof IPointerType) {
			return ((IPointerType) parent).getType();
		} else if (parent instanceof IQualifierType) {
			return ((IQualifierType) parent).getType();
		} else if (parent instanceof ITypedef) {
			return ((ITypedef) parent).getType();
		} else if (parent instanceof ICPPFunctionType) {
			return ((ICPPFunctionType) parent).getReturnType();
		} else {
			return parent;
		}
	}

	public static boolean isNonTrivial(IType toCheck) {
		if (toCheck instanceof ICPPClassType) {

			//TODO: explicit non-trivial copy operators/constructors can be defined in the union explicitly.
			for (ICPPField member : ((ICPPClassType) toCheck).getDeclaredFields()) {

				if (isClass(member.getType())) {
					ICPPClassType chkMbr = (ICPPClassType) member.getType();

					for (ICPPConstructor c : chkMbr.getConstructors()) {
						if (isCopyConstructor(c, chkMbr)) {
							return true;
						}

						if (isMoveConstructor(c, chkMbr)) {
							return true;
						}
					}

					for (ICPPMethod m : chkMbr.getDeclaredMethods()) {

						if (isCopyAssignment(m, chkMbr)) {
							return true;
						}

						if (isMoveAssignment(m, chkMbr)) {
							return true;
						}

						if (m.isDestructor()) {
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	public static boolean isMoveConstructor(ICPPConstructor c, ICPPClassType toCheck) {
		if (c.getParameters().length == 1) {
			ICPPParameter p = c.getParameters()[0];
			IType pt = p.getType();

			if (isRValueReference(pt) && resolveInnerType(pt).isSameType(resolveInnerType(toCheck))) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCopyConstructor(ICPPConstructor c, ICPPClassType toCheck) {
		if (c.getParameters().length == 1) {
			ICPPParameter p = c.getParameters()[0];
			IType pt = p.getType();

			if (isReference(pt) && !isRValueReference(toCheck) && isSameAsSecondInnerType(toCheck, pt)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSameAsSecondInnerType(IType toCheck, IType pt) {
		return resolveInnerType(pt).isSameType(resolveInnerType(toCheck));
	}

	public static boolean isMoveAssignment(ICPPMethod m, ICPPClassType toCheck) {
		if (isEqualsOperator(m)) {
			if (m.getParameters().length == 1) {
				ICPPParameter mp = m.getParameters()[0];
				IType pt = mp.getType();

				if (isReference(pt) && isRValueReference(toCheck) && isSameAsSecondInnerType(toCheck, pt)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isCopyAssignment(ICPPMethod m, ICPPClassType toCheck) {
		if (isEqualsOperator(m)) {
			if (m.getParameters().length == 1) {
				ICPPParameter mp = m.getParameters()[0];
				IType pt = mp.getType();

				if (isReference(pt) && !isRValueReference(toCheck) && isSameAsSecondInnerType(toCheck, pt)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isEqualsOperator(ICPPMethod m) {
		return m.getName().startsWith("operator ="); //$NON-NLS-1$
	}

	public static boolean hasCopyConstructor(ICPPClassType clazzToCheck, IType paramToCheck) {
		for (ICPPConstructor constructor : clazzToCheck.getConstructors()) {
			if (isCopyConstructor(constructor, clazzToCheck)) {
				ICPPParameter p2c = constructor.getParameters()[0];
				if (isRValueReference(p2c.getType())
						&& (!isRValueReference(paramToCheck) || isConst(getChild(paramToCheck)))) {
					return false;
				}
				return true;
			}
		}

		return false;
	}

	public static IType resolveInnerType(IType toResolve) {
		if (toResolve instanceof ICPPReferenceType) {
			return resolveInnerType(((ICPPReferenceType) toResolve).getType());
		} else if (toResolve instanceof IPointerType) {
			return resolveInnerType(((IPointerType) toResolve).getType());
		} else if (toResolve instanceof IQualifierType) {
			return resolveInnerType(((IQualifierType) toResolve).getType());
		} else if (toResolve instanceof ICPPFunctionType) {
			return resolveInnerType(((ICPPFunctionType) toResolve).getReturnType());
		} else if (toResolve instanceof CPPTypedef) {
			return resolveInnerType(((CPPTypedef) toResolve).getType());
		} else {
			return toResolve;
		}
	}
}
