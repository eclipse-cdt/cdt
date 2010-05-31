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
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for "Codan Builder". Clients can call processResource method to
 * traverse the resource tree. It will be calling all the checkers (this
 * interface allows to call framework without using UI). You can obtain instance
 * of this class as CodanRuntime.getInstance().getBuilder()
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICodanBuilder {
	/**
	 * Run code analysis on given resource
	 * 
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 */
	public void processResource(IResource resource, IProgressMonitor monitor);
}