/*******************************************************************************
 *  Copyright (c) 2011 Wind River Systems, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPFunctionSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Specialization of a typedef in the context of a class-specialization.
 */
public class CPPUsingDeclarationSpecialization extends CPPSpecialization implements ICPPUsingDeclaration {
	private IBinding[] fDelegates;

    public CPPUsingDeclarationSpecialization(ICPPUsingDeclaration specialized, ICPPClassSpecialization owner, 
    		ICPPTemplateParameterMap tpmap) {
        super(specialized, owner, tpmap);
    }

	@Override
	public IBinding[] getDelegates() {
		if (fDelegates == null) {
			fDelegates= specializeDelegates();
		}
		return fDelegates;
	}

	private IBinding[] specializeDelegates() {
		IBinding[] origDelegates= ((ICPPUsingDeclaration) getSpecializedBinding()).getDelegates();
		List<IBinding> result= new ArrayList<IBinding>();
		ICPPClassSpecialization owner= (ICPPClassSpecialization) getOwner();
		for (IBinding delegate : origDelegates) {
			if (delegate instanceof ICPPUnknownBinding) {
				try {
					delegate= CPPTemplates.resolveUnknown((ICPPUnknownBinding) delegate, 
							owner.getTemplateParameterMap(), -1, null);
					if (delegate instanceof CPPFunctionSet) {
						for (IBinding b : ((CPPFunctionSet) delegate).getBindings()) {
							result.add(b);
						}
					} else if (delegate != null) {
						result.add(delegate);
					}
				} catch (DOMException e) {
				}
			} else {
				result.add(delegate);
			}
		}
		return result.toArray(new IBinding[result.size()]);
	}
}
