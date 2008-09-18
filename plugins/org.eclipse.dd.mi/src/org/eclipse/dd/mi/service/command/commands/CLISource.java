/*******************************************************************************
 * Copyright (c) 2008  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * 
 *     source FILE
 *
 *  Source a file of commands
 * 
 */
public class CLISource extends CLICommand<MIInfo> {
    /**
     * @since 1.1
     */
    public CLISource(ICommandControlDMContext ctx, String file) {
        super(ctx, "source " + file); //$NON-NLS-1$
    }

    @Deprecated
    public CLISource(MIControlDMContext ctx, String file) {
        this ((ICommandControlDMContext)ctx, file);
    }
}
