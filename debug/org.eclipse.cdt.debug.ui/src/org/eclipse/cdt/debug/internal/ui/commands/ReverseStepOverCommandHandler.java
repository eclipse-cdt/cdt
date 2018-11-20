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
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IReverseStepOverHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

/**
 * Command handler to trigger a reverse stepover operation
 *
 * @since 7.0
 */
public class ReverseStepOverCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<?> getCommandType() {
		return IReverseStepOverHandler.class;
	}
}