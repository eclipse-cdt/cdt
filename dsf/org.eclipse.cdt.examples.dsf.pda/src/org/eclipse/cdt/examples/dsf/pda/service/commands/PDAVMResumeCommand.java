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
 * Resumes the execution of the whole virtual machine
 *
 * <pre>
 *    C: vmresume
 *    R: ok
 *    E: vmresumed client
 *
 * Errors:
 *    error: vm already running
 * </pre>
 */
@Immutable
public class PDAVMResumeCommand extends AbstractPDACommand<PDACommandResult> {

	public PDAVMResumeCommand(PDAVirtualMachineDMContext context) {
		super(context, "vmresume");
	}

	@Override
	public PDACommandResult createResult(String resultText) {
		return new PDACommandResult(resultText);
	}
}
