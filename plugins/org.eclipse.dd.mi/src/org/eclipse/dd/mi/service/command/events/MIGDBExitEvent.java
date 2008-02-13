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
import org.eclipse.dd.mi.service.command.MIControlDMContext;


/**
 * Gdb Session terminated.
 */
@Immutable
public class MIGDBExitEvent extends MIEvent<MIControlDMContext> {

    public MIGDBExitEvent(MIControlDMContext ctx, int token) {
    	super(ctx, token, null);
    }
}
