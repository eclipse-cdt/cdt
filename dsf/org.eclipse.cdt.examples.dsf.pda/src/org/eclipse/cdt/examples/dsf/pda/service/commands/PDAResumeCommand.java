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
 * Resumes the execution of a single thread.  Can be issued only if the virtual
 * machine is running.
 *
 * <pre>
 *    C: resume {thread_id}
 *    R: ok
 *    E: resumed {thread_id} client
 *
 * Errors:
 *    error: invalid thread
 *    error: cannot resume thread when vm is suspended
 *    error: thread already running
 * </pre>
 */
@Immutable
public class PDAResumeCommand extends AbstractPDACommand<PDACommandResult> {

	public PDAResumeCommand(PDAThreadDMContext thread) {
		super(thread, "resume " + thread.getID());
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
