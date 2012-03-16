/*******************************************************************************
 * Copyright (c) 2012  Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * Base class for the 'remote' command of GDB.
 * 
 * @since 4.1
 */
public class CLIRemote extends CLICommand<MIInfo> 
{
    public CLIRemote(IDMContext ctx, String[] params) {
        super(ctx, "remote"); //$NON-NLS-1$
        setParameters(params);
    }
}