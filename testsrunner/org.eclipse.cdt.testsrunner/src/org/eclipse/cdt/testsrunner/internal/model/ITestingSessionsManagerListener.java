/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.model;

import org.eclipse.cdt.testsrunner.model.ITestingSession;

/**
 * Testing sessions manager listener is notified of testing sessions management.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestingSessionsManagerListener {

	/**
	 * Notifies the listener that the specified testing session was activated.
	 *
	 * @param testingSession the activated testing session
	 */
	void sessionActivated(ITestingSession testingSession);

}
