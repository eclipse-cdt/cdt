/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * unset substitute-path
 * 
 * Deletes all the path substitutions.
 * 
 * @since 5.0
 */
public class CLIUnsetSubstitutePath extends CLICommand<MIInfo> {

	public CLIUnsetSubstitutePath(IDMContext ctx) {
		super(ctx, "unset substitute-path"); //$NON-NLS-1$
	}
}
