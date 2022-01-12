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
 * Retrieves register groups information
 *
 * <pre>
 *    C: groups
 *    R: {group 1}|{group 2}|{group 3}|...|
 * </pre>
 */
@Immutable
public class PDAGroupsCommand extends AbstractPDACommand<PDAListResult> {

	public PDAGroupsCommand(PDAVirtualMachineDMContext context) {
		super(context, "groups");
	}

	@Override
	public PDAListResult createResult(String resultText) {
		return new PDAListResult(resultText);
	}
}
