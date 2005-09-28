/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IProject;

/**
 * @author Doug Schaefer
 * 
 * This is the interface to a PDOM Provider. The provider manages the
 * relationships between PDOMs and projects and provides implementations of
 * the PDOM interfaces.
 */
public interface IPDOMProvider {

	public static final String ID 
		= CCorePlugin.PLUGIN_ID + ".PDOMProvider"; //$NON-NLS-1$
	
	/**
	 * Get the PDOM for the given project. If the PDOM is unavailable for this
	 * project, null is returned.
	 * 
	 * @param project
	 * @return the PDOM for the project
	 */
	public IPDOM getPDOM(IProject project);
	
}
