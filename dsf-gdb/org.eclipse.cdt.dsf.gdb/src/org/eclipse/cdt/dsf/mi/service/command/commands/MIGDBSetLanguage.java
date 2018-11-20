/*******************************************************************************
 * Copyright (c) 2013 AdaCore and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Philippe Gil (AdaCore) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 *
 *     -gdb-set language
 *
 * @since 4.3
 */
public class MIGDBSetLanguage extends MIGDBSet {

	public MIGDBSetLanguage(IDMContext ctx, String language) {
		super(ctx, new String[] { "language", language }); //$NON-NLS-1$
	}
}
