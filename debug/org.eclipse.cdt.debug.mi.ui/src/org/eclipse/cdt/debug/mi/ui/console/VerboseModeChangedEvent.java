/*******************************************************************************
 * Copyright (c) 2006, 2010 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * STMicroelectronics - Process console enhancements
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.ui.console;

import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;

/**
 * MISession event, verbose console mode changed
 * @since 6.1
 *
 */
public class VerboseModeChangedEvent extends MIEvent {

	private static final long serialVersionUID = 1L;

	public VerboseModeChangedEvent(MISession session, int token) {
		super(session, token);
		setPropagate(false);
	}

}
