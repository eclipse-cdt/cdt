/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.core.model.IPathEntry;


public interface ICPathContainerPageExtension extends ICPathContainerPage {

	/**
	 * Method {@link #getNewContainers()} is called instead of {@link IClasspathContainerPage#getSelection() }
	 * to get the the newly added containers. {@link IClasspathContainerPage#getSelection() } is still used
	 * to get the edited elements.
	 * @return the classpath entries created on the page. All returned entries must be {@link
	 * IClasspathEntry#CPE_CONTAINER}
	 */
	public IPathEntry[] getNewContainers();

}
