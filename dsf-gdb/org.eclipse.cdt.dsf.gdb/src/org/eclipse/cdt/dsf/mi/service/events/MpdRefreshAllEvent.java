/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jonah Graham / Baha El kassaby - Bug 530443 - discard events
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.events;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIEvent;

/**
 * Event to inform everything to flush all caches and refresh everything.
 * 
 * @since 5.5
 */
public class MpdRefreshAllEvent extends MIEvent<ICommandControlDMContext> {

	public MpdRefreshAllEvent(ICommandControlDMContext context) {
		super(context, 0, null);
	}

}
