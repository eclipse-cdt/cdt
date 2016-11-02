/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * -gdb-set record full stop-at-limit [on | off]
 * @since 5.2
 * 
 */
public class MIGDBSetRecordFullStopAtLimit extends MIGDBSet 
{
    public MIGDBSetRecordFullStopAtLimit(ICommandControlDMContext ctx, boolean isSet) {
        super(ctx, new String[] {"record", "full", "stop-at-limit", isSet ? "on" : "off"});//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
}