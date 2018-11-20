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
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * A factory to create DSF services.  Using this interface allows
 * to easily have different service implementation for different backends.
 *
 * @since 1.1
 */
public interface IDsfDebugServicesFactory {
	<V> V createService(Class<V> clazz, DsfSession session, Object... optionalArguments);
}
