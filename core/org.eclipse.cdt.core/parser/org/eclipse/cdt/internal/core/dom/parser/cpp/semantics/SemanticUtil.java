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
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

/**
 *
 */
public class SemanticUtil {
	public static IType getUltimateType(IType type, boolean stopAtPointerToMember) {
	   return getUltimateType(type, null, stopAtPointerToMember);
	}

	static IType getUltimateType(IType type, IType[] lastPointerType, boolean stopAtPointerToMember) {
	    try {
	        while( true ){
				if( type instanceof ITypedef )
					type= ((ITypedef)type).getType();
	            else if( type instanceof IQualifierType )
	            	type= ((IQualifierType)type).getType();
	            else if( stopAtPointerToMember && type instanceof ICPPPointerToMemberType )
	                return type;
				else if( type instanceof IPointerType ) {
					if(lastPointerType!=null) {
						lastPointerType[0]= type;
					}
					type= ((IPointerType) type).getType();
				} else if( type instanceof ICPPReferenceType )
					type= ((ICPPReferenceType)type).getType();
				else 
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
				if( type instanceof ITypedef )
				    type = ((ITypedef)type).getType();
	            else if( type instanceof IQualifierType )
					type = ((IQualifierType)type).getType();
				else if( type instanceof ICPPReferenceType )
					type = ((ICPPReferenceType)type).getType();
				else 
					return type;
			}
	    } catch ( DOMException e ) {
	        return e.getProblem();
	    }
	}

	/**
	 * Unravels a type by following purely typedefs
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
