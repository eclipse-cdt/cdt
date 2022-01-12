/*******************************************************************************
 * Copyright (c) 2012, 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IDebugNewExecutableHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class DebugNewExecutableHandler extends DebugCommandHandler {

	@Override
	protected Class<?> getCommandType() {
		return IDebugNewExecutableHandler.class;
	}
}
