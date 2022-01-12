/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import org.eclipse.cdt.dsf.gdb.internal.commands.ISelectPrevTraceRecordHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

/**
 * Command handler to select the previous trace record
 *
 * @since 2.1
 */
public class SelectPrevTraceRecordCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<?> getCommandType() {
		return ISelectPrevTraceRecordHandler.class;
	}
}
