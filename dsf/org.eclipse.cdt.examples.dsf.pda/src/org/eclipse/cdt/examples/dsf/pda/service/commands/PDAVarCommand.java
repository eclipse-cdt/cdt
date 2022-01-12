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
 * Retrieves variable value
 *
 * <pre>
 *    C: var  {thread_id} {frame_number} {variable_name}
 *    R: {variable_value}
 *
 * Errors:
 *    error: invalid thread
 *    error: variable undefined
 * </pre>
 */
@Immutable
public class PDAVarCommand extends AbstractPDACommand<PDACommandResult> {

	public PDAVarCommand(PDAThreadDMContext thread, int frameId, String name) {
		super(thread, "var " + thread.getID() + " " + frameId + " " + name);
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
