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

/**
 * 
 * This class manage the start point of actual synchronize operation. The class
 * that implement this interface must have necessary informations for
 * synchronization.
 * 
 */
public interface ISynchronizer {

	/**
	 * Run the SynchronizeOperation. Preparing for the synchronization and
	 * invoke ISyncronizeOperation#synchronize() as an actual synchronize
	 * operation. Preparing is mapping the project to RepositoryProvider, and if
	 * needed, unmap for new synchronization. This method is called directory
	 * from GUI.
	 * 
	 * @param operation
	 * @return
	 */
	public boolean run(ISynchronizeOperation operation);

}
