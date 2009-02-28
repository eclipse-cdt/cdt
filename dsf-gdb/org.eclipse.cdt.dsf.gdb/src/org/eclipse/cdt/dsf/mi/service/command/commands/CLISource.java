/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

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
}
