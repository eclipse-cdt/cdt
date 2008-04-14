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
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArraySet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 *
 */
public class SemanticUtil {
	private static final char[] OPERATOR_CHARS = Keywords.OPERATOR.toCharArray();
	/**
	 * Cache of overloadable operator names for fast lookup. Used by isConversionOperator.
	 */
	private static final CharArraySet cas= new CharArraySet(OverloadableOperator.values().length);
	
	/**
	 * Switch for enabling fix for bug 224364 
	 */
	public static final boolean ENABLE_224364= System.getProperty("cdt.enable.224364") != null; //$NON-NLS-1$
	
	static {
		final int OPERATOR_SPC= OPERATOR_CHARS.length + 1;
		for(OverloadableOperator op : OverloadableOperator.values()) {
			char[] name= op.toCharArray();
			cas.put(CharArrayUtils.subarray(name, OPERATOR_SPC, name.length));
		}
	}
	
	/**
	 * Returns a list of ICPPMethod objects representing all conversion operators
	 * declared by the specified class. This does not include inherited methods. Conversion
	 * operators cannot be implicit.
	 * @param clazz
	 * @return List of ICPPMethod
	 */
	public static final ICPPMethod[] getDeclaredConversionOperators(ICPPClassType clazz) throws DOMException {
		ICPPMethod[] methods= new ICPPMethod[0];
		ICPPMethod[] decs= clazz.getDeclaredMethods();
		if(decs != null) {
			for(ICPPMethod method : decs) {
				if(isConversionOperator(method)) {
					methods= (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, methods, method);
				}
			}
		}
		return methods;
	}
	
	/**
	 * Returns a list of ICPPMethod objects representing all conversion operators
	 * declared by the specified class and its ancestors. This includes inherited
	 * methods. Conversion operators cannot be implicit.
	 * @param clazz
	 * @return List of ICPPMethod
	 */
	public static ICPPMethod[] getConversionOperators(ICPPClassType clazz) throws DOMException {
		ICPPMethod[] methods= new ICPPMethod[0];
		ObjectSet<ICPPClassType> ancestry= inheritanceClosure(clazz);
		for(int i=0; i<ancestry.size(); i++) {
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

		for(int count=0; count < CPPSemantics.MAX_INHERITANCE_DEPTH && !current.isEmpty(); count++) {
			ObjectSet<ICPPClassType> next= new ObjectSet<ICPPClassType>(2);

			for(int i=0; i<current.size(); i++) {
				ICPPClassType clazz= current.keyAt(i);				
				done.put(clazz);
				
				for(ICPPBase base : clazz.getBases()) {
					IBinding binding= base.getBaseClass();
					if(binding instanceof ICPPClassType && !(binding instanceof IProblemBinding)) {
						ICPPClassType ct= (ICPPClassType) binding;
						if(!done.containsKey(ct)) {
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
		if(!method.isImplicit()) {
			final char[] name= method.getNameCharArray();
			if (name.length > OPERATOR_CHARS.length + 1 &&
					CharArrayUtils.equals(name, 0, OPERATOR_CHARS.length, OPERATOR_CHARS)) {
				if(name[OPERATOR_CHARS.length]==' ') {
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
	        while( true ){
				if( type instanceof ITypedef ) {
					type= ((ITypedef)type).getType();
				} else if( type instanceof IQualifierType ) {
					type= ((IQualifierType)type).getType();
				} else if( stopAtPointerToMember && type instanceof ICPPPointerToMemberType )
	                return type;
				else if( type instanceof IPointerType ) {
					if(lastPointerType!=null) {
						lastPointerType[0]= type;
					}
					type= ((IPointerType) type).getType();
				} else if( type instanceof ICPPReferenceType ) {
					type= ((ICPPReferenceType)type).getType();
				} else 
					return type;
				
			}
	    } catch ( DOMException e ) {
	        return e.getProblem();
	    }
	}

	/**
	 * Descends into type containers, stopping at pointer or
	 * pointer-to-member types.
	 * @param type
	 * @return the ultimate type contained inside the specified type
	 */
	public static IType getUltimateTypeUptoPointers(IType type){
	    try {
	        while( true ){
				if( type instanceof ITypedef ) {
					type = ((ITypedef)type).getType();
				} else if( type instanceof IQualifierType ) {
					type = ((IQualifierType)type).getType();
				} else if( type instanceof ICPPReferenceType ) {
					type = ((ICPPReferenceType)type).getType();
				} else 
					return type;
			}
	    } catch ( DOMException e ) {
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
			while(type instanceof ITypedef) {
				type= ((ITypedef)type).getType();
			}
		} catch(DOMException e) {
			type= e.getProblem();
		}
		return type;
	}

}
