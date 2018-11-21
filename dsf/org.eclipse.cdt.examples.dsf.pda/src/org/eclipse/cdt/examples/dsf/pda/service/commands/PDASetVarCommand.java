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
 * Sets a variable value
 *
 * <pre>
 *    C: setvar {thread_id} {frame_number} {variable} {value}
 *    R: ok
 *
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDASetVarCommand extends AbstractPDACommand<PDACommandResult> {

	public PDASetVarCommand(PDAThreadDMContext thread, int frame, String variable, String value) {
		super(thread, "setvar " + thread.getID() + " " + frame + " " + variable + " " + value);
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
