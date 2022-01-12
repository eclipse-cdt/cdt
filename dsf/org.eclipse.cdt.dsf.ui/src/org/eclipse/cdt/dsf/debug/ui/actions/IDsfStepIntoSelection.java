/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;

/**
 * @since 2.4
 */
public interface IDsfStepIntoSelection {

	/**
	 * Checks if within a state to perform step into selection
	 * @param dmc
	 * @return
	 */
	public boolean isExecutable(final IExecutionDMContext dmc);

	/**
	 * Carries out the actual step into selection action to the specified function location
	 * @param fileName
	 * @param lineLocation
	 * @param selectedFunction
	 * @param context
	 */
	public void runToSelection(final String fileName, final int lineLocation,
			final IFunctionDeclaration selectedFunction, final IExecutionDMContext context);
}
