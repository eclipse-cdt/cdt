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
		} else if (event.getType() == IResourceChangeEvent.PRE_BUILD) {
			/*
			 * JKR (bug 535565):
			 * This is called every time the project properties dialog is closed.
			 * LlvmPreferenceStore.addMinGWStdLib() and LlvmPreferenceStore.addStdLibUnix()
			 * try to find some sensible default settings for LLVM/CLang and *add* it to
			 * *every* project, regardless the consequences. This seems to be a relic from
			 * ancient times and is not needed (is harmful) nowadays, hence commented out.
			 * I think it can safely be removed.
			 */
			//			String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
			//			if (os.indexOf("win") >= 0) { //$NON-NLS-1$
			//				LlvmPreferenceStore.addMinGWStdLib();
			//				//				LlvmToolOptionPathUtil.addMissingCppIncludesForMingw(); //TODO: Remove when Scanner Discovery has been fixed
			//			} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 /*|| os.indexOf( "mac") >=0 */) { //$NON-NLS-1$ //$NON-NLS-2$
			//				LlvmPreferenceStore.addStdLibUnix();
			//			}

			/*
			 * try to add values (include and library paths and libraries) to
			 * projects's build configurations to ensure that newly added projects
			 * have necessary paths.
			 */
			/*
			 * JKR (bug 535565):
			 * I think that this also can be removed: I think that the data is collected by the
			 * removed LLVM-settings page. And if the data cannot be added, how can we collect it?
			 */
			//			LlvmToolOptionPathUtil.addAllIncludesToBuildConf();
			//			LlvmToolOptionPathUtil.addAllLibsToBuildConf();
			//			LlvmToolOptionPathUtil.addAllLibPathsToBuildConf();
		} else {
			return;
		}

	}

}