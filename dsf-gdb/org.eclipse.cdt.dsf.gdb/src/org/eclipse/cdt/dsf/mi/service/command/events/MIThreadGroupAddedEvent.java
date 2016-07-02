/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;


/**
 * This can only be detected by gdb/mi after GDB ???.
 * @since 5.1
 *
 */
@Immutable
public class MIThreadGroupAddedEvent extends MIEvent<IMIContainerDMContext> {

    public MIThreadGroupAddedEvent(IMIContainerDMContext ctx, int token) {
        super(ctx, token, null);
    }
}
