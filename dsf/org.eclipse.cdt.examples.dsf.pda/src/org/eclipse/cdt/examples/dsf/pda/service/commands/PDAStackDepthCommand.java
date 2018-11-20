/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.service.commands;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.examples.dsf.pda.service.PDAThreadDMContext;

/**
 * Retrieves command stack depth
 *
 * <pre>
 *    C: stackdepth {thread_id}
 *    R: {depth}
 *
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAStackDepthCommand extends AbstractPDACommand<PDAStackDepthCommandResult> {

	public PDAStackDepthCommand(PDAThreadDMContext thread) {
		super(thread, "stackdepth " + thread.getID());
	}

	@Override
	public PDAStackDepthCommandResult createResult(String resultText) {
		return new PDAStackDepthCommandResult(resultText);
	}
}
