/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.core.resources.IProject;


/**
 * A listener which gets notified when the type cache changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface ITypeCacheChangedListener {

	/**
	 * Notifies that the type cache for the given project has changed in some way
	 * and should be refreshed at some point to make it consistent with the current
	 * state of the C model.
	 * 
	 * @param project the given project
	 */
	void typeCacheChanged(IProject project);
}
