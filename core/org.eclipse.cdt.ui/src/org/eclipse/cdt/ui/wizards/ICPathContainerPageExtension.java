/*
 * Created on Apr 14, 2004
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
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
