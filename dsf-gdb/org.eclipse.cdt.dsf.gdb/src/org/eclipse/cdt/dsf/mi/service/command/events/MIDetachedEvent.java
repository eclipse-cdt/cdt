/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;


/**
 *
 *  ^running
 */
@Immutable
public class MIDetachedEvent extends MIEvent<ICommandControlDMContext> {

    /**
     * @since 1.1
     */
    public MIDetachedEvent(ICommandControlDMContext ctx, int token) {
        super(ctx, token, null);
    }
    
    @Override
    public String toString() {
        return "Detached"; //$NON-NLS-1$
    }
}
