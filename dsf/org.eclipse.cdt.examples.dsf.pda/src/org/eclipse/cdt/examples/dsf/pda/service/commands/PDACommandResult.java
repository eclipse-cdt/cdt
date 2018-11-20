/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;

/**
 * Basic command result object.  This command result simply allows access to the
 * PDA response.  Sub-classes may override to optionally parse the response text
 * and return higher-level objects.
 */
@Immutable
public class PDACommandResult implements ICommandResult {

	final public String fResponseText;

	public PDACommandResult(String response) {
		fResponseText = response;
	}

	@Override
	public <V extends ICommandResult> V getSubsetResult(ICommand<V> command) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PDACommandResult) {
			PDACommandResult result = (PDACommandResult) obj;
			return fResponseText.equals(result.fResponseText);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fResponseText.hashCode();
	}
}
