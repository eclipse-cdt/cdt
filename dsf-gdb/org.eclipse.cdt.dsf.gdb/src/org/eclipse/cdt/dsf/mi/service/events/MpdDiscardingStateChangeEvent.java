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
 * @since 5.5
 */
public class MpdDiscardingStateChangeEvent extends MIEvent<ICommandControlDMContext> {

	private boolean fIsDiscarding;

	public MpdDiscardingStateChangeEvent(ICommandControlDMContext context, boolean isDiscarding) {
		super(context, 0, null);
		fIsDiscarding = isDiscarding;
	}

	public boolean isDiscarding() {
		return fIsDiscarding;
	}

}
