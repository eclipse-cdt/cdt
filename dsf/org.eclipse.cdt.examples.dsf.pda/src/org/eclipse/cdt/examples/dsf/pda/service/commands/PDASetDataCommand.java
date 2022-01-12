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
 * Sets a data value in the data stack at the given location
 *
 * <pre>
 *    C: setdata {thread_id} {index} {value}
 *    R: ok
 *
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDASetDataCommand extends AbstractPDACommand<PDACommandResult> {

	public PDASetDataCommand(PDAThreadDMContext thread, int index, String value) {
		super(thread, "setdata " + thread.getID() + " " + index + " " + value);
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
