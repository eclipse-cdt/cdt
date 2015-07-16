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
package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIGDBSet;

/**
 * -gdb-set substitute-path from to
 * 
 * @since 4.8
 */
public class MISetSubstitutePath extends MIGDBSet {

	public MISetSubstitutePath(IDMContext context, String from, String to) {
		super(context, new String[] { "substitute-path", from, to }); //$NON-NLS-1$
	}

}
