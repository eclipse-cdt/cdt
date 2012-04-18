/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
