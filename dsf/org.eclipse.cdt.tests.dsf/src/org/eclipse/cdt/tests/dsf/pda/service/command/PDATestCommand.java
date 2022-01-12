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
package org.eclipse.cdt.tests.dsf.pda.service.command;

import org.eclipse.cdt.examples.dsf.pda.service.PDAVirtualMachineDMContext;
import org.eclipse.cdt.examples.dsf.pda.service.commands.AbstractPDACommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDACommandResult;

/**
 *
 */
class PDATestCommand extends AbstractPDACommand<PDACommandResult> {
	PDATestCommand(PDAVirtualMachineDMContext context, String command) {
		super(context, command);
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
