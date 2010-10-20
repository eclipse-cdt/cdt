/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPQualifierType;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * This is a utility class to help convert AST elements to Strings corresponding to the
 * AST element's type.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ASTTypeUtil {
	private static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final int DEAULT_ITYPE_SIZE = 2;

	/**
	 * Returns a string representation for the parameters of the given function type. The 
	 * representation contains the comma-separated list of the normalized parameter type
	 * representations wrapped in parentheses. 
	 */
	public static String getParameterTypeString(IFunctionType type) {
		StringBuilder result = new StringBuilder();
		appendParameterTypeString(type, result);
		return result.toString();
	}
	
	private static void appendParameterTypeString(IFunctionType ft, StringBuilder result) {
		IType[] types = ft.getParameterTypes();
		result.append(Keywords.cpLPAREN);
		boolean needComma= false;
		for (IType type : types) {
			if (type != null) {
				if (needComma)
					result.append(COMMA_SPACE);
				appendType(type, true, result);
				needComma= true;
			}
		}
		if (ft instanceof ICPPFunctionType && ((ICPPFunctionType) ft).takesVarArgs()) {
			if (needComma)
				result.append(COMMA_SPACE);
			result.append(Keywords.cpELLIPSIS);
		}
		result.append(Keywords.cpRPAREN);
	}

	/**
	 * @return Whether the function matching the given function binding takes
	 *         parameters or not.
	 * 
	 * @since 5.1
	 */
	public static boolean functionTakesParameters(IFunction function) {
		IParameter[] parameters = function.getParameters();

		if (parameters.length == 0) {
			return false;
		} 
		if (parameters.length == 1 && SemanticUtil.isVoidType(parameters[0].getType())) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a string representation for the type array. The representation is
	 * a comma-separated list of the normalized string representations of the 
	 * provided types.
	 * @see #getTypeListString(IType[], boolean)
	 */
	public static String getTypeListString(IType[] types) {
		return getTypeListString(types, true);
	}

	/**
	 * Returns a String representation of the type array as a
	 * comma-separated list.
	 * @param types
	 * @return representation of the type array as a comma-separated list 
	 * @since 5.1
	 */
	public static String getTypeListString(IType[] types, boolean normalize) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			if (types[i] != null) {
				appendType(types[i], normalize, result);
				if (i < types.length - 1)
					result.append(COMMA_SPACE);
			}
		}
		return result.toString();
	}
	
	/**
	 * Returns a comma-separated list of the string representations of the arguments, enclosed
	 * in angle brackets. 
	 * Optionally normalization is performed:
	 * <br> template parameter names are represented by their parameter position,
	 * <br> further normalization may be performed in future versions.
	 * @param normalize indicates whether normalization shall be performed
	 * @since 5.1
	 */
	public static String getArgumentListString(ICPPTemplateArgument[] args, boolean normalize) {
		StringBuilder result= new StringBuilder();
		appendArgumentList(args, normalize, result);
		return result.toString();
	}

	private static void appendArgumentList(ICPPTemplateArgument[] args, boolean normalize, StringBuilder result) {
		boolean first= true;
		result.append('<');
		for (ICPPTemplateArgument arg : args) {
			if (!first) {
				result.append(',');
			}
			first= false;
			appendArgument(arg, normalize, result);
		}
		result.append('>');
	}

	/**
	 * Returns a string representation for an template argument. Optionally 
	 * normalization is performed:
	 * <br> template parameter names are represented by their parameter position,
	 * <br> further normalization may be performed in future versions.
	 * @param normalize indicates whether normalization shall be performed
	 * @since 5.1
	 */
	public static String getArgumentString(ICPPTemplateArgument arg, boolean normalize) {
		StringBuilder buf= new StringBuilder();
		appendArgument(arg, normalize, buf);
		return buf.toString();
	}
	
	private static void appendArgument(ICPPTemplateArgument arg, boolean normalize, StringBuilder buf) {
		IValue val= arg.getNonTypeValue();
		if (val != null) {
			buf.append(val.getSignature());
		} else {
			appendType(arg.getTypeValue(), normalize, buf);
		}
	}

	/**
	 * Returns an array of normalized string representations for the parameter types of the
	 * given function type.
	 * @see #getType(IType, boolean)
	 */
	public static String[] getParameterTypeStringArray(IFunctionType type) {
		IType[] parms = type.getParameterTypes();
		String[] result = new String[parms.length];
		
		for (int i = 0; i < parms.length; i++) {
			if (parms[i] != null) {
				result[i] = getType(parms[i]);
			}
		}
		
		return result;
	}
	
	private static void appendTypeString(IType type, boolean normalize, StringBuilder result) {
		boolean needSpace = false;
		
		if (type instanceof IArrayType) {
			result.append(Keywords.cpLBRACKET);
			if (type instanceof ICArrayType) {
				final ICArrayType catype = (ICArrayType) type;
				if (catype.isConst()) {
					result.append(Keywords.CONST); needSpace = true;
				}
				if (catype.isRestrict()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.RESTRICT); needSpace = true;
				}
				if (catype.isStatic()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.STATIC); needSpace = true;
				}
				if (catype.isVolatile()) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(Keywords.VOLATILE);
				}
			} 
			IValue val= ((IArrayType) type).getSize();
			if (val != null && val != Value.UNKNOWN) {
				if (normalize) {
					if (needSpace) {
						result.append(SPACE); needSpace = false;
					}
					result.append(val.getSignature());
				} else {
					Long v= val.numericalValue();
					if (v != null) {
						if (needSpace) {
							result.append(SPACE); needSpace = false;
						}
						result.append(v.longValue());
					}
				}
			}
			result.append(Keywords.cpRBRACKET);
		} else if (type instanceof IBasicType) {
			IBasicType basicType= (IBasicType) type;
			final Kind kind = basicType.getKind();
			if (basicType.isSigned()) {
				// 3.9.1.2: signed integer types
				if (!normalize || kind == Kind.eChar) {
					result.append(Keywords.SIGNED); needSpace = true;
				}
			} else if (basicType.isUnsigned()) {
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				result.append(Keywords.UNSIGNED); needSpace = true;
			}
			if (basicType.isLong()) {
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				result.append(Keywords.LONG); needSpace = true;
			} else if (basicType.isShort()) {
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				result.append(Keywords.SHORT); needSpace = true;
			} else if (basicType.isLongLong()) {
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				result.append(Keywords.LONG_LONG); needSpace = true;
			} 

			if (basicType.isComplex()) {
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				result.append(Keywords.c_COMPLEX); needSpace = true;
			}
			if ((basicType).isImaginary()) {
				if (needSpace) {
					result.append(SPACE); needSpace = false;
				}
				result.append(Keywords.c_IMAGINARY); needSpace = true;
			}
			
			switch (kind) {
			case eChar:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.CHAR);
				break;
			case eDouble:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.DOUBLE);
				break;
			case eFloat:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.FLOAT);
				break;
			case eInt:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.INT);
				break;
			case eVoid:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.VOID);
				break;
			case eBoolean:
				if (needSpace) result.append(SPACE);
				if (basicType instanceof ICPPBasicType) {
					result.append(Keywords.BOOL);
				} else {
					result.append(Keywords.c_BOOL);
				}
				break;
			case eWChar:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.WCHAR_T);
				break;
			case eChar16:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.CHAR16_T);
				break;
			case eChar32:
				if (needSpace) result.append(SPACE);
				result.append(Keywords.CHAR32_T);
				break;
			case eUnspecified:
				break;
			}
		} else if (type instanceof ICPPTemplateParameter) {
			appendCppName((ICPPTemplateParameter) type, normalize, true, result);
		} else if (type instanceof ICPPBinding) {
			if (type instanceof IEnumeration) {
				result.append(Keywords.ENUM);
				result.append(SPACE);
			}
			appendCppName((ICPPBinding) type, normalize, true, result);
		} else if (type instanceof ICompositeType) {
//			101114 fix, do not display class, and for consistency don't display struct/union as well
			appendNameCheckAnonymous((ICompositeType) type, result);
		} else if (type instanceof ITypedef) {
			result.append(((ITypedef) type).getNameCharArray());
		} else if (type instanceof ICPPReferenceType) {
			if (((ICPPReferenceType) type).isRValueReference()) {
				result.append(Keywords.cpAND);
			} else {
				result.append(Keywords.cpAMPER);
			}
		} else if (type instanceof ICPPParameterPackType) {
			result.append(Keywords.cpELLIPSIS);
		} else if (type instanceof IEnumeration) {
			result.append(Keywords.ENUM);
			result.append(SPACE);
			appendNameCheckAnonymous((IEnumeration) type, result);
		} else if (type instanceof IFunctionType) {
			appendParameterTypeString((IFunctionType) type, result);
			needSpace = false;
			if (type instanceof ICPPFunctionType) {
				ICPPFunctionType ft= (ICPPFunctionType) type;
				needSpace= appendCVQ(result, needSpace, ft.isConst(), ft.isVolatile(), false);
			}
		} else if (type instanceof IPointerType) {
			if (type instanceof ICPPPointerToMemberType) {
				appendTypeString(((ICPPPointerToMemberType) type).getMemberOfClass(), normalize, result);
				result.append(Keywords.cpCOLONCOLON);
			}
			result.append(Keywords.cpSTAR); needSpace = true;
			IPointerType pt= (IPointerType) type;
			needSpace= appendCVQ(result, needSpace, pt.isConst(), pt.isVolatile(), pt.isRestrict());
		} else if (type instanceof IQualifierType) {
			if (type instanceof ICQualifierType) {
				if (((ICQualifierType) type).isRestrict()) {
					result.append(Keywords.RESTRICT); needSpace = true;
				}
			} else if (type instanceof IGPPQualifierType) {
				if (((IGPPQualifierType) type).isRestrict()) {
					result.append(Keywords.RESTRICT); needSpace = true;
				}
			}
			
			IQualifierType qt= (IQualifierType) type;
			needSpace= appendCVQ(result, needSpace, qt.isConst(), qt.isVolatile(), false);
		} else if (type instanceof ISemanticProblem) {
			result.append('?');
		} else if (type != null) {
			result.append('@').append(type.hashCode()); 
		}
	}

	private static void appendTemplateParameter(ICPPTemplateParameter type, boolean normalize, StringBuilder result) {
		if (normalize) {
			result.append('#');
			result.append(Integer.toString(type.getParameterID(), 16));
		} else {
			result.append(type.getName());
		}
	}

	private static boolean appendCVQ(StringBuilder target, boolean needSpace, final boolean isConst,
			final boolean isVolatile, final boolean isRestrict) {
		if (isConst) {
			if (needSpace) {
				target.append(SPACE); 
			}
			target.append(Keywords.CONST); 
			needSpace = true;
		}
		if (isVolatile) {
			if (needSpace) {
				target.append(SPACE); 
			}
			target.append(Keywords.VOLATILE); 
			needSpace = true;
		}
		if (isRestrict) {
			if (needSpace) {
				target.append(SPACE); 
			}
			target.append(Keywords.RESTRICT); 
			needSpace = true;
		}
		return needSpace;
	}

	/**
	 * Returns the normalized string representation of the type. 
	 * @see #getType(IType, boolean)
	 */
	public static String getType(IType type) {
		return getType(type, true);
	}
		
	/**
	 * Returns a string representation of a type.  
	 * Optionally the representation is normalized:
	 * <br> typedefs are resolved
	 * <br> template parameter names are represented by their parameter position
	 * <br> further normalization may be performed in the future.
	 * @param type a type to compute the string representation for.
	 * @param normalize whether or not normalization should be performed.
	 * @return the type representation of the IType
	 */
	public static String getType(IType type, boolean normalize) {
		StringBuilder result = new StringBuilder();
		appendType(type, normalize, result);
		return result.toString();
	}
	
	/**
	 * Appends the the result of {@link #getType(IType, boolean)} to the given buffer.
	 * @since 5.3
	 */
	public static void appendType(IType type, boolean normalize, StringBuilder result) {
		IType[] types = new IType[DEAULT_ITYPE_SIZE];
		
		// push all of the types onto the stack
		int i = 0;
		IQualifierType cvq= null;
		ICPPReferenceType ref= null;
		while (type != null && ++i < 100) {
			if (type instanceof ITypedef) {
				if (normalize || type instanceof ICPPSpecialization) {
					// Skip the typedef and proceed with its target type.
				} else {
					// Output reference, qualifier and typedef, then stop.
					if (ref != null) {
						types = (IType[]) ArrayUtil.append(IType.class, types, ref);
						ref= null;
					}
					if (cvq != null) {
						types = (IType[]) ArrayUtil.append(IType.class, types, cvq);
						cvq= null;
					}
					types = (IType[]) ArrayUtil.append(IType.class, types, type);
					type= null; 
				}
			} else {
				if (type instanceof ICPPReferenceType) {
					// reference types ignore cv-qualifiers
					cvq=null;
					// lvalue references win over rvalue references
					if (ref == null || ref.isRValueReference()) {
						// delay reference to see if there are more
						ref= (ICPPReferenceType) type;
					}
				} else {
					if (cvq != null) {
						// merge cv qualifiers
						if (type instanceof IQualifierType || type instanceof IPointerType) {
							type= SemanticUtil.addQualifiers(type, cvq.isConst(), cvq.isVolatile(), false);
							cvq= null;
						} 
					} 
					if (type instanceof IQualifierType) {
						// delay cv qualifier to merge it with others
						cvq= (IQualifierType) type;
					} else {
						// no reference, no cv qualifier: output reference and cv-qualifier
						if (ref != null) {
							types = (IType[]) ArrayUtil.append(IType.class, types, ref);
							ref= null;
						}
						if (cvq != null) {
							types = (IType[]) ArrayUtil.append(IType.class, types, cvq);
							cvq= null;
						}
						types = (IType[]) ArrayUtil.append(IType.class, types, type);
					} 
				}
			}
			if (type instanceof ITypeContainer) {
				type = ((ITypeContainer) type).getType();
			} else if (type instanceof IFunctionType) {
				type= ((IFunctionType) type).getReturnType();
			} else {
				type= null;
			}
		}	 
		
		// pop all of the types off of the stack, and build the string representation while doing so
		List<IType> postfix= null;
		BitSet parenthesis= null;
		boolean needParenthesis= false;
		boolean needSpace= false;
		for (int j = types.length - 1; j >= 0; j--) {
			IType tj = types[j];
			if (tj != null) {
				if (j > 0 && types[j - 1] instanceof IQualifierType) {
					if (needSpace)
						result.append(SPACE); 
					appendTypeString(types[j - 1], normalize, result);
					result.append(SPACE);
					appendTypeString(tj, normalize, result);
					needSpace= true;
					--j;
				} else {
					// handle post-fix 
					if (tj instanceof IFunctionType || tj instanceof IArrayType) {
						if (j == 0) {
							if (needSpace)
								result.append(SPACE); 
							appendTypeString(tj, normalize, result);
							needSpace= true;
						} else {
							if (postfix == null) {
								postfix= new ArrayList<IType>();
							}
							postfix.add(tj);
							needParenthesis= true;
						}
					} else {
						if (needSpace)
							result.append(SPACE);
						if (needParenthesis && postfix != null) {
							result.append('(');
							if (parenthesis == null) {
								parenthesis= new BitSet();
							}
							parenthesis.set(postfix.size()-1);
						}
						appendTypeString(tj, normalize, result);
						needParenthesis= false;
						needSpace= true;
					}
				}
			}
		}

		if (postfix != null) {
			for (int j = postfix.size() - 1; j >= 0; j--) {
				if (parenthesis != null && parenthesis.get(j)) {
					result.append(')');
				}
				IType tj = postfix.get(j);
				appendTypeString(tj, normalize, result);
			}
		}
	}

	/**
	 * For testing purposes, only.
	 * Returns the normalized string representation of the type defined by the given declarator.
	 *  
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String getType(IASTDeclarator decltor) {
		// get the most nested declarator
		while (decltor.getNestedDeclarator() != null)
			decltor = decltor.getNestedDeclarator();
		
		IBinding binding = decltor.getName().resolveBinding();
		IType type = null;
		
		try {
			if (binding instanceof IEnumerator) {
				type = ((IEnumerator)binding).getType();
			} else if (binding instanceof IFunction) {
				type = ((IFunction)binding).getType();
			} else if (binding instanceof ITypedef) {
				type = ((ITypedef)binding).getType();
			} else if (binding instanceof IVariable) {
				type = ((IVariable)binding).getType();
			}
		} catch (DOMException e) {
			return EMPTY_STRING;
		}
		
		if (type != null) {
			return getType(type);
		}
		
		return EMPTY_STRING;
	}

	/**
	 * For testing purposes, only.
	 * Return's the String representation of a node's type (if available).  
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String getNodeType(IASTNode node) {
		if (node instanceof IASTDeclarator)
			return getType((IASTDeclarator) node);
		if (node instanceof IASTName && ((IASTName) node).resolveBinding() instanceof IVariable)
			return getType(((IVariable)((IASTName) node).resolveBinding()).getType());
		if (node instanceof IASTName && ((IASTName) node).resolveBinding() instanceof IFunction)
			return getType(((IFunction)((IASTName) node).resolveBinding()).getType());
		if (node instanceof IASTName && ((IASTName) node).resolveBinding() instanceof IType)
			return getType((IType)((IASTName) node).resolveBinding());
		if (node instanceof IASTTypeId)
			return getType((IASTTypeId) node);
		
		return EMPTY_STRING;
	}
	
	/**
	 * Returns the type representation of the IASTTypeId as a String.
	 * 
	 * @param typeId
	 * @return the type representation of the IASTTypeId as a String
	 */
	public static String getType(IASTTypeId typeId) {
		if (typeId instanceof CASTTypeId)
			return createCType(typeId.getAbstractDeclarator());
		else if (typeId instanceof CPPASTTypeId)
			return createCPPType(typeId.getAbstractDeclarator());
		
		return EMPTY_STRING;
	}
	
	private static String createCType(IASTDeclarator declarator) {
		IType type = CVisitor.createType(declarator);
		return getType(type);
	}
	
	private static String createCPPType(IASTDeclarator declarator) {
		IType type = CPPVisitor.createType(declarator);
		return getType(type);
	}
	
	/**
	 * @deprecated don't use it does something strange
	 */
	@Deprecated
	public static boolean isConst(IType type) {
		if (type instanceof IQualifierType) {
			return ((IQualifierType) type).isConst();
		} else if (type instanceof ITypeContainer) {
			return isConst(((ITypeContainer) type).getType());
		} else if (type instanceof IArrayType) {
			return isConst(((IArrayType) type).getType());
		} else if (type instanceof ICPPReferenceType) {
			return isConst(((ICPPReferenceType) type).getType());
		} else if (type instanceof IFunctionType) {
			return isConst(((IFunctionType) type).getReturnType());
		} else if (type instanceof IPointerType) {
			return isConst(((IPointerType) type).getType());
		} else if (type instanceof ITypedef) {
			return isConst(((ITypedef) type).getType());
		} else {
			return false;
		}
	}

	private static void appendCppName(IBinding binding, boolean normalize,  boolean addTemplateArgs, StringBuilder result) {
		ICPPTemplateParameter tpar= getTemplateParameter(binding);
		if (tpar != null) {
			appendTemplateParameter(tpar, normalize, result);
		} else {
			if (normalize) {
				IBinding owner= binding.getOwner();
				if (owner instanceof ICPPNamespace || owner instanceof IType) {
					int pos= result.length();
					appendCppName(owner, normalize, normalize, result);
					if (result.length() > pos)
						result.append("::"); //$NON-NLS-1$
				}
			}
			appendNameCheckAnonymous(binding, result);
		}
		
		if (binding instanceof ICPPTemplateInstance) {
			appendArgumentList(((ICPPTemplateInstance) binding).getTemplateArguments(), normalize, result);
		} else if (binding instanceof ICPPUnknownClassInstance) {
			appendArgumentList(((ICPPUnknownClassInstance) binding).getArguments(), normalize, result);
		}
	}
	
	private static ICPPTemplateParameter getTemplateParameter(IBinding binding) {
		if (binding instanceof ICPPTemplateParameter) 
			return (ICPPTemplateParameter) binding;
		
		if (binding instanceof ICPPDeferredClassInstance)
			return getTemplateParameter(((ICPPDeferredClassInstance) binding).getTemplateDefinition());

		return null;
	}

	private static void appendNameCheckAnonymous(IBinding binding, StringBuilder result) {
		char[] name= binding.getNameCharArray();
		if (name != null && name.length > 0) {
			result.append(name);
		} else if (!(binding instanceof ICPPNamespace)) {
			appendNameForAnonymous(binding, result);
		}
	}

	public static char[] createNameForAnonymous(IBinding binding) {
		StringBuilder result= new StringBuilder();
		appendNameForAnonymous(binding, result);
		if (result.length() == 0)
			return null;
		
		return extractChars(result);
	}
	
	private static char[] extractChars(StringBuilder buf) {
		final int length = buf.length();
		char[] result= new char[length];
		buf.getChars(0, length, result, 0);
		return result;
	}

	private static void appendNameForAnonymous(IBinding binding, StringBuilder buf) {
		IASTNode node= null;
		if (binding instanceof ICInternalBinding) {
			node= ((ICInternalBinding) binding).getPhysicalNode();
		} else if (binding instanceof ICPPInternalBinding) {
			node= ((ICPPInternalBinding) binding).getDefinition();
		}
		if (node != null) {
			IASTFileLocation loc= node.getFileLocation();
			if (loc == null) {
				node= node.getParent();
				if (node != null) {
					loc= node.getFileLocation();
				}
			}
			if (loc != null) {
				char[] fname= loc.getFileName().toCharArray();
				int fnamestart= findFileNameStart(fname);
				buf.append('{');
				buf.append(fname, fnamestart, fname.length-fnamestart);
				buf.append(':');
				buf.append(loc.getNodeOffset());
				buf.append('}');
			}
		}
	}

	private static int findFileNameStart(char[] fname) {
		for (int i= fname.length - 2; i >= 0; i--) {
			switch (fname[i]) {
			case '/':
			case '\\':
				return i+1;
			}
		}
		return 0;
	}
}
