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
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowLanguageInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * Returns the current language.
 * 
 * @since 4.3
 */
public class CLIShowLanguage extends MIInterpreterExecConsole<CLIShowLanguageInfo> {

	private static final String SHOW_LANGUAGE = "show language";  //$NON-NLS-1$

	public CLIShowLanguage(IDMContext ctx) {
		super(ctx, SHOW_LANGUAGE);
	}

	@Override
	public CLIShowLanguageInfo getResult(MIOutput miResult) {
		return new CLIShowLanguageInfo(miResult);
	}
}
