/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.service;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * This service keeps synchronized the CDT debug view selection and GDB's
 * internal focus - GDB's current thread, stack frame, and (implicitly) inferior.
 *
 * @since 5.2
 */
public interface IGDBFocusSynchronizer extends IDsfService {
	/**
	 * Returns an array of contexts, representing the current synchronized focus
	 */
	IDMContext[] getFocus();

	/**
	 * Sets the service's current focus and propagate it to the GDB corresponding to this
	 * service's instance, when appropriate.
	 *
	 * @param  focus An array of IDMContext, each context representing a focus'ed element
	 * 		   from the Debug View
	 * @param rm the request monitor
	 */
	void setFocus(IDMContext[] focus, RequestMonitor rm);

	/**
	 * The service sends this event to indicate that GDB has changed its focus, as a
	 * result of an event not triggered by CDT. For example a console command typed by
	 * the user.
	 * Note: the full focus might not be reflected in the included context. The service
	 * can be queried to get the complete picture.
	 */
	interface IGDBFocusChangedEvent extends IDMEvent<IDMContext> {
	}

	/**
	 * This tells the synchronizer that the session, corresponding to this service's
	 * instance, has been selected. This can be called, for example, when a specific
	 * debugger console has become active, so that the synchronizer will reflect this
	 * in the Debug View selection.
	 */
	void sessionSelected();
}
