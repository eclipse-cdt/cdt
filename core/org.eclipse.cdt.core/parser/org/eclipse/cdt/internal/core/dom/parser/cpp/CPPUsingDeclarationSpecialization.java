/*******************************************************************************
 * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;

/**
 * Specialization of a typedef in the context of a class-specialization.
 */
public class CPPUsingDeclarationSpecialization extends CPPSpecialization implements ICPPUsingDeclaration {
	private final IBinding[] fDelegates;

	public CPPUsingDeclarationSpecialization(ICPPUsingDeclaration specialized, IBinding owner,
			ICPPTemplateParameterMap tpmap, IBinding[] delegates) {
		super(specialized, owner, tpmap);
		fDelegates = delegates;
	}

	@Override
	public IBinding[] getDelegates() {
		return fDelegates;
	}
}
