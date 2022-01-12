/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
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
	 * Run code analysis on given resource in {@link CheckerLaunchMode#RUN_ON_FULL_BUILD} mode
	 *
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 */
	public void processResource(IResource resource, IProgressMonitor monitor);

	/**
	 * Run code analysis on given resource in a given mode
	 *
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 * @param mode - launch mode, @see {@link CheckerLaunchMode}
	 * @since 2.0
	 */
	public void processResource(IResource resource, IProgressMonitor monitor, CheckerLaunchMode mode);

	/**
	 * Run code analysis on given resource in a given mode
	 *
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 * @param mode - launch mode, @see {@link CheckerLaunchMode}
	 * @param model - runtime code model, such as AST, used when model is not in sync with resource
	 * @since 3.3
	 */
	public void processResource(IResource resource, IProgressMonitor monitor, CheckerLaunchMode mode, Object model);
}