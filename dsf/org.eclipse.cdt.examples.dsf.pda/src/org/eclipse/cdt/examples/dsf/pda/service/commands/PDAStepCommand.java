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
 * Executes next instruction
 *
 * <pre>
 * If VM running:
 *    C: step {thread_id}
 *    R: ok
 *    E: resumed {thread_id} client
 *    E: suspended {thread_id} step
 *
 * If VM suspended:
 *    C: step {thread_id}
 *    R: ok
 *    E: vmresumed client
 *    E: vmsuspended {thread_id} step
 *
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDAStepCommand extends AbstractPDACommand<PDACommandResult> {

	public PDAStepCommand(PDAThreadDMContext thread) {
		super(thread, "step " + thread.getID());
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
