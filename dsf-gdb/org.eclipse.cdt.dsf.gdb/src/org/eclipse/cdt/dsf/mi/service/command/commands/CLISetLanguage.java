/*******************************************************************************
 * Copyright (c) 2013 AdaCore and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     AdaCore - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * This command set the current language
 *
 * @since 4.3
 */
public class CLISetLanguage extends CLICommand<MIInfo> {

	public CLISetLanguage(IDMContext ctx, String language) {
		super(ctx, "set language " + language);  //$NON-NLS-1$
	}
}
