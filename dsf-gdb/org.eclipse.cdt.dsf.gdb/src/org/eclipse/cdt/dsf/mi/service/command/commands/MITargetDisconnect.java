/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * This command disconnects from the remote target.
 * @since 4.1
 */
public class MITargetDisconnect extends MICommand<MIInfo> {

    public MITargetDisconnect(ICommandControlDMContext ctx) {
        super(ctx, "-target-disconnect"); //$NON-NLS-1$
    }
}
