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
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.service.DsfSession;

/*
 * A factory to create DSF services.  Using this interface allows
 * to easily have different service implementation for different backends.
 */
public interface IDsfDebugServicesFactory {
	public <V> V createService(DsfSession session, Class<V> clazz);
}
