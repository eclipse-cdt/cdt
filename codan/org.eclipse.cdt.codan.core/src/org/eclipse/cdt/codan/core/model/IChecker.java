/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.core.resources.IResource;

public interface IChecker {
	public boolean processResource(IResource resource);

	boolean enabledInContext(IResource resource);

	/**
	 * Checker must implement this method to determine if it can run in editor
	 * "as you type", checker must be really light weight to run in this mode
	 * 
	 * @return true if need to be run in editor as user types, and false
	 *         otherwise
	 */
	boolean runInEditor();
}
