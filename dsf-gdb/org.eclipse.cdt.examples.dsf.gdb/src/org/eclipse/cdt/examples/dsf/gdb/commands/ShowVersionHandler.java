/*******************************************************************************
 * Copyright (c) 2015, 2016 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.gdb.commands;

import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class ShowVersionHandler extends DebugCommandHandler {

	@Override
	protected Class<IShowVersionHandler> getCommandType() {
		return IShowVersionHandler.class;
	}
}
