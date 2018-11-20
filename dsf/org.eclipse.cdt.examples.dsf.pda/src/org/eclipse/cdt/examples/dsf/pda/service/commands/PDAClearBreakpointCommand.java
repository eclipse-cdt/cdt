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
import org.eclipse.cdt.examples.dsf.pda.service.PDAVirtualMachineDMContext;

/**
 * Clears any breakpoint set on given line
 *
 * <pre>
 *    C: clear {line}
 *    R: ok
 * </pre>

 */
@Immutable
public class PDAClearBreakpointCommand extends AbstractPDACommand<PDACommandResult> {

	public PDAClearBreakpointCommand(PDAVirtualMachineDMContext context, int line) {
		super(context, "clear " + (line - 1));
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
