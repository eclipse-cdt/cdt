/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * unset substitute-path
 *
 * Deletes all the path substitutions.
 *
 * @since 5.0
 */
public class CLIUnsetSubstitutePath extends MIInterpreterExecConsole<MIInfo> {

	public CLIUnsetSubstitutePath(ISourceLookupDMContext ctx) {
		super(ctx, "unset substitute-path"); //$NON-NLS-1$
	}
}
