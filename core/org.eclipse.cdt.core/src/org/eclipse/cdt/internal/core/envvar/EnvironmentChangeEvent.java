/*******************************************************************************
 * Copyright (c) 2005, 2013 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    Andrew Gvozdev    - Implementation of notification mechanism including changes to this API
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.Collection;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.utils.envvar.IEnvironmentChangeEvent;

/**
 * Concrete implementation of event describing changes to environment variables defined by user
 * on CDT Environment page in Preferences.
 */
public class EnvironmentChangeEvent implements IEnvironmentChangeEvent {
	private IEnvironmentVariable[] oldVariables;
	private IEnvironmentVariable[] newVariables;

	/**
	 * Constructor.
	 * 
	 * @param oldVars - set of environment variables before the change.
	 * @param newVars - set of environment variables after the change.
	 */
	public EnvironmentChangeEvent(Collection<IEnvironmentVariable> oldVars, Collection<IEnvironmentVariable> newVars) {
		oldVariables = oldVars.toArray(new IEnvironmentVariable[oldVars.size()]);
		newVariables = newVars.toArray(new IEnvironmentVariable[newVars.size()]);
	}

	@Override
	public IEnvironmentVariable[] getOldVariables() {
		return oldVariables;
	}

	@Override
	public IEnvironmentVariable[] getNewVariables() {
		return newVariables;
	}
}
