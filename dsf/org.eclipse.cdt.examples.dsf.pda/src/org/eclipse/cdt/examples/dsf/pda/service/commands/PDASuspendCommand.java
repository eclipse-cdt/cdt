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
 * Suspends execution of a single thread.  Can be issued only if the virtual
 * machine is running.
 *
 * <pre>
 *    C: suspend {thread_id}
 *    R: ok
 *    E: suspended {thread_id} client
 *
 * Errors:
 *    error: invalid thread
      error: vm already suspended
 *    error: thread already suspended
 * </pre>
 */
@Immutable
public class PDASuspendCommand extends AbstractPDACommand<PDACommandResult> {

	public PDASuspendCommand(PDAThreadDMContext thread) {
		super(thread, "suspend " + thread.getID());
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
