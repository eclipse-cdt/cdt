/*******************************************************************************
 * Copyright (c) 2005 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.core.dom.ast.c.ICQualifierType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPBasicType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPQualifierType;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.c.CExternalFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CExternalVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CStructure;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;

/**
 * This is a utility class to help convert AST elements to Strings corresponding to the
 * AST element's type.
 * 
 * @author dsteffle
 */
public class ASTTypeUtil {
	
	private static final String COMMA_SPACE = ", "; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SPACE = " "; //$NON-NLS-1$
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final int DEAULT_ITYPE_SIZE = 2;

	/**
	 * Returns a String represnetation of the parameter type of an IFunctionType.
	 * 
	 * This function calls ASTTypeUtil#getParameterTypeStringArray(IFunctionType) and wraps the
	 * results in "()" with a comma separated list.
	 * 
	 * @param type
	 * @return the represnetation of the parameter type of an IFunctionType
	 */
	public static String getParameterTypeString(IFunctionType type) {
		StringBuffer result = new StringBuffer();
		String[] parms = getParameterTypeStringArray(type);
		
		result.append(Keywords.cpLPAREN);
		for(int i=0; i<parms.length; i++) {
			if (parms[i] != null) {
				result.append(parms[i]);
				if (i<parms.length-1) result.append(COMMA_SPACE);
			}
		}
		result.append(Keywords.cpRPAREN);
		return result.toString();
	}

	/**
	 * Returns String[] corresponding to the types of the parameters for the IFunctionType.
	 * 
	 * @param type
	 * @return the types of the parameters for the IFunctionType
	 */
	public static String[] getParameterTypeStringArray(IFunctionType type) {
		IType[] parms = null;
		try {
			parms = type.getParameterTypes();
		} catch (DOMException e) { return EMPTY_STRING_ARRAY; }
		
		String[] result = new String[parms.length];
		
		for(int i=0; i<parms.length; i++) {
			if (parms[i] != null) {
				result[i] = getType(parms[i]);
			}
		}
		
		return result;
	}
	
	private static String getTypeString(IType type) {
		StringBuffer result = new StringBuffer();
		boolean needSpace = false;
		
		if (type instanceof IArrayType) {
			result.append(Keywords.cpLBRACKET);
			if (type instanceof ICArrayType) {
				try {
					if (((ICArrayType)type).isConst()) { result.append(Keywords.CONST); needSpace=true; }
					if (((ICArrayType)type).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
					if (((ICArrayType)type).isStatic()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.STATIC); needSpace=true; }
					if (((ICArrayType)type).isVolatile()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOLATILE); }
				} catch (DOMException e) {}
			}
			result.append(Keywords.cpRBRACKET);
		} else if (type instanceof IBasicType) {
			try {
				if (((IBasicType)type).isSigned()) { result.append(Keywords.SIGNED); needSpace = true; }
				else if (((IBasicType)type).isUnsigned()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.UNSIGNED); needSpace=true; }
				if (((IBasicType)type).isLong()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.LONG); needSpace = true; }
				else if (((IBasicType)type).isShort()) { if (needSpace) { result.append(SPACE); needSpace=false; }result.append(Keywords.SHORT); needSpace = true; }
			} catch (DOMException e) {}
			
			if (type instanceof IGPPBasicType) {
				try {
					if (((IGPPBasicType)type).isLongLong()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.LONG_LONG); needSpace=true; }
					
					switch (((IGPPBasicType)type).getType()) {
						case IGPPBasicType.t_Complex:
							result.append(Keywords.c_COMPLEX);
							break;
						case IGPPBasicType.t_Imaginary:
							result.append(Keywords.c_IMAGINARY);
							break;
						case IGPPBasicType.t_typeof:
							result.append(GCCKeywords.TYPEOF);
							break;						
					}
				} catch (DOMException e) {}
			} else if (type instanceof ICPPBasicType) {
				try {
					switch (((ICPPBasicType)type).getType()) {
						case ICPPBasicType.t_bool:
							result.append(Keywords.BOOL);
							break;
						case ICPPBasicType.t_wchar_t:
							result.append(Keywords.WCHAR_T);
							break;
					}
				} catch (DOMException e) {}
			} else if (type instanceof ICBasicType) {
				try {
					switch (((ICBasicType)type).getType()) {
						case ICBasicType.t_Bool:
							result.append(Keywords.c_BOOL);
							break;
						case ICBasicType.t_Complex:
							result.append(Keywords.c_COMPLEX);
							break;
						case ICBasicType.t_Imaginary:
							result.append(Keywords.c_IMAGINARY);
							break;
					}
				} catch (DOMException e) {}
			}
			
			try {
				switch (((IBasicType)type).getType()) {
					case IBasicType.t_char:
						result.append(Keywords.CHAR);
						break;
					case IBasicType.t_double:
						result.append(Keywords.DOUBLE);
						break;
					case IBasicType.t_float:
						result.append(Keywords.FLOAT);
						break;
					case IBasicType.t_int:
						result.append(Keywords.INT);
						break;
					case IBasicType.t_void:
						result.append(Keywords.VOID);
						break;
				}
			} catch (DOMException e) {}
			
		} else if (type instanceof ICompositeType) {
			if (type instanceof ICPPClassType) {
				try {
					switch(((ICPPClassType)type).getKey()) {
						case ICPPClassType.k_class:
							result.append(Keywords.CLASS);
							break;
					}
				} catch (DOMException e) {}
			}
			
			try {
				switch(((ICompositeType)type).getKey()) {
					case ICompositeType.k_struct:
						result.append(Keywords.STRUCT);
						break;
					case ICompositeType.k_union:
						result.append(Keywords.UNION);
						break;
				}
			} catch (DOMException e) {}
			
			if (type instanceof CStructure) {
				result.append(SPACE);
				result.append(((CStructure)type).getName());
			}
			if (type instanceof CPPClassType) {
				result.append(SPACE);
				result.append(((CPPClassType)type).getName());
			}
		} else if (type instanceof ICPPReferenceType) {
			result.append(Keywords.cpAMPER);
		} else if (type instanceof ICPPTemplateTypeParameter) {
			try {
				result.append(getType(((ICPPTemplateTypeParameter)type).getDefault()));
			} catch (DOMException e) {}
		} else if (type instanceof IEnumeration) {
			result.append(Keywords.ENUM);
		} else if (type instanceof IFunctionType) {
			try {
				String temp = getType(((IFunctionType)type).getReturnType());
				if (temp != null && !temp.equals(EMPTY_STRING)) { result.append(temp); needSpace=true; }
				if (needSpace) { result.append(SPACE); needSpace=false; }
				temp = getParameterTypeString((IFunctionType)type);
				if (temp != null && !temp.equals(EMPTY_STRING)) { result.append(temp); needSpace=false; }
			} catch (DOMException e) {}
		} else if (type instanceof IPointerType) {
			result.append(Keywords.cpSTAR); needSpace=true;
			
			if (type instanceof IGPPPointerType) {
				if (((IGPPPointerType)type).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
			} else if (type instanceof ICPointerType) {
				if (((ICPointerType)type).isRestrict()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.RESTRICT); needSpace=true; }
			}
			
			try {
				if (((IPointerType)type).isConst()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CONST); needSpace=true; }
				if (((IPointerType)type).isVolatile()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOLATILE); needSpace=true; }
			} catch (DOMException e) {}
			
		} else if (type instanceof IQualifierType) {
			
			if (type instanceof ICQualifierType) {
				if (((ICQualifierType)type).isRestrict()) { result.append(Keywords.RESTRICT); needSpace=true; }
			} else if (type instanceof IGPPQualifierType) {
				if (((IGPPQualifierType)type).isRestrict()) { result.append(Keywords.RESTRICT); needSpace=true; }
			}
			
			try {
				if (((IQualifierType)type).isConst()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.CONST); needSpace=true; }
				if (((IQualifierType)type).isVolatile()) { if (needSpace) { result.append(SPACE); needSpace=false; } result.append(Keywords.VOLATILE); needSpace=true; }
			} catch (DOMException e) {}
			
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the type represntation of the IType as a String.  This function uses the IType interfaces to build the 
	 * String representation of the IType.
	 * @param type
	 * @return the type represntation of the IType
	 */
	public static String getType(IType type) {
		StringBuffer result = new StringBuffer();
		IType[] types = new IType[DEAULT_ITYPE_SIZE];
		
		// push all of the types onto the stack
		while(type != null && type instanceof ITypeContainer) {
		    types = (IType[]) ArrayUtil.append( IType.class, types, type );
			
			try {
				type = ((ITypeContainer)type).getType();
			} catch (DOMException e) {}
		}
		
		if (type != null && !(type instanceof ITypeContainer)) {
		    types = (IType[]) ArrayUtil.append( IType.class, types, type );
		}
		
		// pop all of the types off of the stack, and build the string representation while doing so
		for(int j=types.length-1; j>=0; j--) {
			if (types[j] instanceof ITypedef)
				continue;

			if (types[j] != null && result.length() > 0) result.append(SPACE); // only add a space if this is not the first type being added
			
			if (types[j] != null)
				result.append(getTypeString(types[j]));
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the type representation of the declarator (including parameters) as a String.
	 * 
	 * @param decltor
	 * @return the type representation of the IASTDeclarator (including parameters)
	 */
	public static String getType(IASTDeclarator decltor) {
		// get the most nested declarator
		while(decltor.getNestedDeclarator() != null)
			decltor = decltor.getNestedDeclarator();
		
		IBinding binding = decltor.getName().resolveBinding();
		IType type = null;
		
		try {
			if (binding instanceof CExternalFunction) {
				type = ((CExternalFunction)binding).getType();
			} else if (binding instanceof CExternalVariable) {
				type = ((CExternalVariable)binding).getType();
			} else if (binding instanceof IEnumerator) {
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
	 * Return's the String representation of a node's type (if available).  This is
	 * currently only being used for testing.
	 * 
	 * TODO Remove this function when done testing if it is no longer needed
	 * 
	 * @param node
	 * @return the String representation of a node's type (if available)
	 */
	public static String getNodeType(IASTNode node) {
		try {
			if (node instanceof IASTDeclarator)
				return getType((IASTDeclarator)node);
			if (node instanceof IASTName && ((IASTName)node).resolveBinding() instanceof IVariable)
				return getType(((IVariable)((IASTName)node).resolveBinding()).getType());
			if (node instanceof IASTName && ((IASTName)node).resolveBinding() instanceof IFunction)
				return getType(((IFunction)((IASTName)node).resolveBinding()).getType());
			if (node instanceof IASTName && ((IASTName)node).resolveBinding() instanceof IType)
				return getType((IType)((IASTName)node).resolveBinding());
			if (node instanceof IASTTypeId)
				return getType((IASTTypeId)node);
		} catch (DOMException e) { return EMPTY_STRING; }
		
		return EMPTY_STRING;
	}
	
	/**
	 * Retuns the type representation of the IASTTypeId as a String.
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
	
}
