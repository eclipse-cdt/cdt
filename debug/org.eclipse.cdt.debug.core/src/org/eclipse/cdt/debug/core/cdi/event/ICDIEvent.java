/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

/**
 * 
 * A base interface for all CDI events.
 * 
 * @since Jul 18, 2002
 */
public interface ICDIEvent {
	/**
	 * The CDI object on which the event initially occurred.
	 * 
	 * @return the CDI object on which the event initially occurred
	 */
	ICDIObject getSource();
}
