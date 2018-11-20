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
 * Retrieves data stack information
 *
 * <pre>
 *    C: data {thread_id}
 *    R: {value 1}|{value 2}|{value 3}|...|
 *
 * Errors:
 *    error: invalid thread
 * </pre>
 */
@Immutable
public class PDADataCommand extends AbstractPDACommand<PDAListResult> {

	public PDADataCommand(PDAThreadDMContext thread) {
		super(thread, "data " + thread.getID());
	}

	@Override
	public PDAListResult createResult(String resultText) {
		return new PDAListResult(resultText);
	}
}
