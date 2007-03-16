/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

public class EnvironmentChangeEvent {
	private static final IEnvironmentVariable[] EMPTY_VAR_ARRAY = new IEnvironmentVariable[0];
	
	private IEnvironmentVariable[] fAddedVars, fRemovedVars, fChangedVars;
	
	EnvironmentChangeEvent(IEnvironmentVariable[] addedVars, IEnvironmentVariable[] removedVars, IEnvironmentVariable[] changedVars){
		fAddedVars = addedVars != null ? (IEnvironmentVariable[])addedVars.clone() : null;
		fRemovedVars = removedVars != null ? (IEnvironmentVariable[])removedVars.clone() : null;
		fChangedVars = changedVars != null ? (IEnvironmentVariable[])changedVars.clone() : null;
	}
	public IEnvironmentVariable[] getAddedVariables(){
		return fAddedVars != null ? (IEnvironmentVariable[])fAddedVars.clone() : EMPTY_VAR_ARRAY;
	}

	public IEnvironmentVariable[] getRemovedVariables(){
		return fRemovedVars != null ? (IEnvironmentVariable[])fRemovedVars.clone() : EMPTY_VAR_ARRAY;
	}

	public IEnvironmentVariable[] getChangedVariables(){
		return fChangedVars != null ? (IEnvironmentVariable[])fChangedVars.clone() : EMPTY_VAR_ARRAY;
	}

}
