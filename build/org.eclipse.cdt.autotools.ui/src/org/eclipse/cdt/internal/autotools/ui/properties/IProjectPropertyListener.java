/*******************************************************************************
 * Copyright (c) 2007 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.core.resources.IProject;

public interface IProjectPropertyListener {

	/**
	 * Handler for property changes
	 * 
	 * @param project the project to which the property changed
	 * @param property the name of the property changed
	 */
	void handleProjectPropertyChanged(IProject project, String property);
	
}
