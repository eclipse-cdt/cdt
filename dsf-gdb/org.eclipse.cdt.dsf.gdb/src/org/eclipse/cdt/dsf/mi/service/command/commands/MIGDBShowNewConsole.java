/*******************************************************************************
 * Copyright (c) 2017  Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIGDBShowNewConsoleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * 
 * -gdb-show new-console
 * 
 * @since 5.4
 * 
 */
public class MIGDBShowNewConsole extends MIGDBShow<MIGDBShowNewConsoleInfo> {

	public MIGDBShowNewConsole(IDMContext ctx) {
		super(ctx, new String[] { "new-console" }); //$NON-NLS-1$
	}

	@Override
	public MIGDBShowNewConsoleInfo getResult(MIOutput miResult) {
		return new MIGDBShowNewConsoleInfo(miResult);
	}
}
