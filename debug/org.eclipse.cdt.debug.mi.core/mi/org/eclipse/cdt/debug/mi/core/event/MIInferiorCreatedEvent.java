/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * MIInferiorCreatedEvent
 */
public class MIInferiorCreatedEvent extends MICreatedEvent {

	/**
	 * @param source
	 * @param id
	 */
	public MIInferiorCreatedEvent(MISession source, int id) {
		super(source, id);
	}

}
