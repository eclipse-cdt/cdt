/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.events;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;


/**
 * This can not be detected yet by gdb/mi.
 *
 */
@Immutable
public class MIThreadCreatedEvent extends MIEvent<IContainerDMContext> {

    final private int tid;

    public MIThreadCreatedEvent(IContainerDMContext ctx, int id) {
        this(ctx, 0, id);
    }

    public MIThreadCreatedEvent(IContainerDMContext ctx, int token, int id) {
        super(ctx, token, null);
        tid = id;
    }

    public int getId() {
        return tid;
    }
}
