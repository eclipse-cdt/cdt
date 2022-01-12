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
 * Retrieves registers definition information
 *
 * <pre>
 *    C: registers {group name}
 *    R: {register name} {true|false}|{bit field name} {start bit} {bit count} {mnemonic 1} {mnemonic 2} ...#{register name} ...
 * </pre>
 */
@Immutable
public class PDARegistersCommand extends AbstractPDACommand<PDARegistersCommandResult> {

	public PDARegistersCommand(PDAThreadDMContext context, String group) {
		super(context, "registers " + group);
	}

	@Override
	public PDARegistersCommandResult createResult(String resultText) {
		return new PDARegistersCommandResult(resultText);
	}
}
