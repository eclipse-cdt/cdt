/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.ISaveTraceDataHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

/**
 * Command handler to save the trace data to a file
 * 
 * @since 7.0
 */
public class SaveTraceDataCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<?> getCommandType() {
		return ISaveTraceDataHandler.class;
	}
}
