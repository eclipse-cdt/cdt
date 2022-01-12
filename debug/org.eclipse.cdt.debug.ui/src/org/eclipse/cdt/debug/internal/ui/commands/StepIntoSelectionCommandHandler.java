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
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IStepIntoSelectionHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class StepIntoSelectionCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<?> getCommandType() {
		return IStepIntoSelectionHandler.class;
	}
}
