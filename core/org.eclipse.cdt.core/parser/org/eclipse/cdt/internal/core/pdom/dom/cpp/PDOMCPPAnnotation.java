/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;

class PDOMCPPAnnotation {

	// "Mutable" shares the same offset as "inline" because
	// only fields can be mutable and only functions can be inline.
	public static final int MUTABLE_OFFSET = PDOMCAnnotation.INLINE_OFFSET;
	// extern C shares the same offset as visibility because
	// only members have visibility and cannot be extern C.
	public static final int EXTERN_C_OFFSET= 6;
	public static final int VISIBILITY_OFFSET = 6;
	private static final int VISIBILITY_MASK = 0x03;

	// Extra C++-specific annotations that don't fit on the first
	// byte of annotations.
	public static final int VIRTUAL_OFFSET = 0;
	public static final int DESTRUCTOR_OFFSET = 1;
	public static final int IMPLICIT_METHOD_OFFSET = 2;
	public static final int EXPLICIT_CONSTRUCTOR_OFFSET = 3;
	public static final int PURE_VIRTUAL_OFFSET = 4;
	public static final int MAX_EXTRA_OFFSET= PURE_VIRTUAL_OFFSET;
	
	/**
	 * Encodes storage class specifiers and other annotation, including
	 * C++-specific annotation, from an IBinding as a bit vector.
	 * 
	 * @param binding the IBinding whose annotation will be encoded.
	 * @return a bit vector of the annotation.
	 * @throws DOMException
	 */	
	public static byte encodeAnnotation(IBinding binding) throws DOMException {
		byte modifiers = PDOMCAnnotation.encodeAnnotation(binding);
		if (binding instanceof ICPPMember) {
			ICPPMember member = (ICPPMember) binding;
			int mask = ~(VISIBILITY_MASK << VISIBILITY_OFFSET);
			modifiers &= mask;
			modifiers |= (member.getVisibility() & VISIBILITY_MASK) << VISIBILITY_OFFSET;

			if (binding instanceof ICPPField) {
				ICPPField variable = (ICPPField) binding;
				modifiers |= (variable.isMutable() ? 1 : 0) << MUTABLE_OFFSET;
			}
		}
		else {
			if (binding instanceof ICPPFunction) {
				if (((ICPPFunction) binding).isExternC()) {
					modifiers |= 1 << EXTERN_C_OFFSET;
				}
			}
			if (binding instanceof ICPPVariable) {
				if (((ICPPVariable) binding).isExternC()) {
					modifiers |= 1 << EXTERN_C_OFFSET;
				}
			}
		}
		return modifiers;
	}

	/**
	 * Encodes C++-specific annotation not already handled by
	 * encodeAnnotation() as a bit vector.
	 * 
	 * @param binding the IBinding whose annotation will be encoded.
	 * @return a bit vector of the annotation.
	 * @throws DOMException
	 */	
	public static byte encodeExtraAnnotation(IBinding binding) throws DOMException {
		byte modifiers = 0;
		if (binding instanceof ICPPMethod) {
			ICPPMethod method = (ICPPMethod) binding;
			modifiers |= (method.isVirtual() ? 1 : 0) << VIRTUAL_OFFSET;
			modifiers |= (method.isDestructor() ? 1 : 0) << DESTRUCTOR_OFFSET;
			modifiers |= (method.isImplicit() ? 1 : 0) << IMPLICIT_METHOD_OFFSET;
			modifiers |= (method.isPureVirtual() ? 1 : 0) << PURE_VIRTUAL_OFFSET;
		}
		if (binding instanceof ICPPConstructor) {
			ICPPConstructor constructor= (ICPPConstructor) binding;
			if (constructor.isExplicit()) {
				modifiers |= (1 << EXPLICIT_CONSTRUCTOR_OFFSET);
			}
		}
		return modifiers;
	}
	
	/**
	 * Unpacks visibility information from a bit vector of annotation.
	 * @param annotation Annotation containing visibility information.
	 * @return The visibility component of the annotation.
	 */
	public static int getVisibility(int annotation) {
		return (annotation >> VISIBILITY_OFFSET) & VISIBILITY_MASK;
	}
}
