/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

/**
 * Represents a reference to a constructor (instance), which cannot be resolved because
 * it depends on a template parameter. A compiler would resolve it during instantiation.
 */
public class CPPDeferredConstructor extends CPPDeferredFunction implements ICPPConstructor {

	public CPPDeferredConstructor(ICPPClassType owner) {
		super(owner, owner.getNameCharArray(), null);
	}

	public CPPDeferredConstructor(ICPPClassType owner, ICPPFunction[] candidates) {
		super(owner, owner.getNameCharArray(), candidates);
	}

	@Override
	public boolean isExplicit() {
		return false;
	}

	@Override
	public boolean isDestructor() {
		return false;
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

	@Override
	public boolean isPureVirtual() {
		return false;
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getVisibility() {
		return v_public;
	}

	@Override
	public boolean isOverride() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		return null;
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}
}
