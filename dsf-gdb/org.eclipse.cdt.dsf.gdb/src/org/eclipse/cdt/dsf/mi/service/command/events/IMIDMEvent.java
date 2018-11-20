/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.events;

/**
 * Common interface for events that are directly caused by some MI event.
 *
 * @since 1.1
 */
public interface IMIDMEvent {

	/**
	 * Returns the underlying MI event that triggered this event.
	 * <p>
	 * Note: the return type is an object which can be safely cast to
	 * an MIEvent.  However returning a parametrized MIEvent type here
	 * leads to compiler warnings related to generics (see bug 240997)
	 * </p>
	 * @see MIEvent
	 */
	public Object getMIEvent();
}
