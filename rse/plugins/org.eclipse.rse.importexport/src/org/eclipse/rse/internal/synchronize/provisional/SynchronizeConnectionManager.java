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
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;

public class SynchronizeConnectionManager implements ISynchronizeConnectionManager {
	public void connect(IProject project) throws TeamException {
		RepositoryProvider.map(project, RSESyncUtils.PROVIDER_ID);
	}

	public void disconnect(IProject project) throws TeamException {
		if (isConnected(project)) {
			RepositoryProvider.unmap(project);
		}
	}

	public boolean isConnected(IProject project) {
		return RepositoryProvider.isShared(project);
	}

}
