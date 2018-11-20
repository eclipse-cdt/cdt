/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;

/**
 * Event describing Build Variables changes.
 */
public class VariableChangeEvent {
	private static final ICdtVariable[] EMPTY_VAR_ARRAY = new ICdtVariable[0];

	private ICdtVariable[] fAddedVars, fRemovedVars, fChangedVars;

	VariableChangeEvent(ICdtVariable[] addedVars, ICdtVariable[] removedVars, ICdtVariable[] changedVars) {
		fAddedVars = addedVars != null ? (ICdtVariable[]) addedVars.clone() : null;
		fRemovedVars = removedVars != null ? (ICdtVariable[]) removedVars.clone() : null;
		fChangedVars = changedVars != null ? (ICdtVariable[]) changedVars.clone() : null;
	}

	public ICdtVariable[] getAddedVariables() {
		return fAddedVars != null ? (ICdtVariable[]) fAddedVars.clone() : EMPTY_VAR_ARRAY;
	}

	public ICdtVariable[] getRemovedVariables() {
		return fRemovedVars != null ? (ICdtVariable[]) fRemovedVars.clone() : EMPTY_VAR_ARRAY;
	}

	public ICdtVariable[] getChangedVariables() {
		return fChangedVars != null ? (ICdtVariable[]) fChangedVars.clone() : EMPTY_VAR_ARRAY;
	}

}
