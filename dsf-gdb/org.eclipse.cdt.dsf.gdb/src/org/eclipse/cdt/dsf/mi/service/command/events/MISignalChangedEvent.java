/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.dsf.debug.service.ISignals.ISignalsDMContext;


/**
 *
 */
@Immutable
public class MISignalChangedEvent extends MIEvent<ISignalsDMContext> {

    final private String name;

    public MISignalChangedEvent(ISignalsDMContext ctx, String n) {
    	this(ctx, 0, n);
    }

    public MISignalChangedEvent(ISignalsDMContext ctx, int id, String n) {
    	super(ctx, id, null);
    	name = n;
    }

    public String getName() {
    	return name;
    }

}
