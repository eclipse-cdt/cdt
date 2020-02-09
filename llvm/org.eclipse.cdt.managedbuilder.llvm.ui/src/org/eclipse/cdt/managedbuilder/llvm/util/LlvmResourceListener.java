/*******************************************************************************
 * Copyright (c) 2010, 2015 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia Siemens Networks - initial implementation
 *     Petri Tuononen - Initial implementation
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.util;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;

/**
 * Implements Resource listener.
 */
public class LlvmResourceListener implements IResourceChangeListener {

	/**
	 * Defines what happens when resources have changed.
	 *
	 * @param event IResourceChangeEvent
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		if (event.getType() == IResourceChangeEvent.POST_BUILD) { //refresh every project after build

			/*
			 * FIXME: M-A.L: (Bug 405909) I commented out the refresh code because it was a
			 * major performance issue even without LLVM projects in the
			 * workspace. Unfortunately, I could not track down the reason as to
			 * why there was a refresh in the first the place by looking at the
			 * history (git and svn) and by manual testing
			 */

			//			//get all projects
			//			IProject[] projects = LlvmToolOptionPathUtil.getProjectsInWorkspace();
			//
			//			//refresh the projects
			//			for (IProject proj : projects) {
			//				try {
			//					proj.refreshLocal(IResource.DEPTH_INFINITE, null);
			//				} catch (CoreException e) {
			//					e.printStackTrace();
			//				}
			//			}
		} else {
			return;
		}

	}

}