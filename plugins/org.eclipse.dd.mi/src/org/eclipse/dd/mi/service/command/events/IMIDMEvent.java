/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.events;

import org.eclipse.dd.dsf.datamodel.IDMContext;

/**
 * Common interface for events that are directly caused by some MI event.
 */
public interface IMIDMEvent<V extends IDMContext> {
	public MIEvent<V> getMIEvent();
}
