/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;

/**
 * Scope for class-specializations which specializes members in a lazy manner.
 */
public class CPPClassSpecializationScope extends AbstractCPPClassSpecializationScope implements ICPPASTInternalScope {

	public CPPClassSpecializationScope(ICPPClassSpecialization specialization) {
		super(specialization);
	}

	// This scope does not cache its own names
	@Override
	public void addName(IASTName name, boolean adlOnly) {
	}

	@Override
	public IASTNode getPhysicalNode() {
		return null;
	}

	@Override
	public void addBinding(IBinding binding) {
	}

	@Override
	public void populateCache() {
	}

	@Override
	public void removeNestedFromCache(IASTNode container) {
	}
}
