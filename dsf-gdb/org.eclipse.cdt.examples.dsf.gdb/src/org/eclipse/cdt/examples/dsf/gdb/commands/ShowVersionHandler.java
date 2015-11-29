/*******************************************************************************
 * Copyright (c) 2015 Jonah Graham and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jonah Graham - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.gdb.commands;

import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class ShowVersionHandler extends DebugCommandHandler {

	@Override
	protected Class<IShowVersionHandler> getCommandType() {
		return IShowVersionHandler.class;
	}
}
