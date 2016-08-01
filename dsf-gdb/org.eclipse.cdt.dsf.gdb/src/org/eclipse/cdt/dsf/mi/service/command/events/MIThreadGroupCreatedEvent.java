/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;


/**
 * This can only be detected by gdb/mi after GDB 6.8.
 * @since 1.1
 *
 */
@Immutable
public class MIThreadGroupCreatedEvent extends MIEvent<IProcessDMContext> {

    final private String fGroupId;
    final private String fLaunchType;

    public MIThreadGroupCreatedEvent(IProcessDMContext ctx, int token, String groupId) {
        this(ctx, token, null, null);
    }
    
    /** @since 5.1 */
    public MIThreadGroupCreatedEvent(IProcessDMContext ctx, int token, String groupId, String launchType) {
        super(ctx, token, null);
        fGroupId = groupId;
        fLaunchType = launchType;
    }

    public String getGroupId() { return fGroupId; }
    
    /** @since 5.1 */
    public String getLaunchType() { return fLaunchType; }

}
