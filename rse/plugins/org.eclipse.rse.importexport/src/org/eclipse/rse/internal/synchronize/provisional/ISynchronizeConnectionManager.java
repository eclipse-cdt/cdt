/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.provisional;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.TeamException;

public interface ISynchronizeConnectionManager {
	/**
	 * Return if the project is already shared or not.
	 * 
	 * @param project
	 * @return
	 */
	public boolean isConnected(IProject project);

	/**
	 * Share the project. Sharing project is necessary for synchronization.
	 * 
	 * @param project
	 * @throws TeamException
	 */
	public void connect(IProject project) throws TeamException;

	/**
	 * Finish sharing the project. When sharing finished, re-synchronization no
	 * longer runs.
	 * 
	 * @param project
	 * @throws TeamException
	 */
	public void disconnect(IProject project) throws TeamException;

}
