/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguageMappingChangeEvent;
import org.eclipse.cdt.core.model.ILanguageMappingChangeListener;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * @author crecoskie
 *
 *	This class handles changes in language mappings for the PDOM by reindexing the appropriate projects.
 *  This class is a a work in progress and will be changed soon to be smarter about the resources it reindexes.
 */
public class LanguageMappingChangeListener implements ILanguageMappingChangeListener {
	private IIndexManager fManager;
	
	public LanguageMappingChangeListener(IIndexManager manager) {
		fManager = manager;
	}
	
	@Override
	public void handleLanguageMappingChangeEvent(ILanguageMappingChangeEvent event) {
		CModelManager manager = CModelManager.getDefault();
		if (event.getType() == ILanguageMappingChangeEvent.TYPE_WORKSPACE) {
			// For now reindex all projects.
			// TODO: This should be smarter about figuring out which projects
			// are potentially unaffected due to project settings
			try {
				ICProject[] cProjects = manager.getCModel().getCProjects();
				for (int i = 0; i < cProjects.length; i++) {
					ICProject project = cProjects[i];
					fManager.reindex(project);
				}
			} catch (CModelException e) {
				CCorePlugin.log(e);
			}
		} else if (event.getType() == ILanguageMappingChangeEvent.TYPE_PROJECT) {
			// For now, reindex the entire project since we don't know which
			// files are affected.
			ICProject cProject = manager.getCModel().getCProject(event.getProject());
			fManager.reindex(cProject);
		} else if (event.getType() == ILanguageMappingChangeEvent.TYPE_FILE) {
			// Just reindex the affected file.
			IFile file = event.getFile();
			ICProject cProject = manager.getCModel().getCProject(file);
			ICElement element = manager.create(file, cProject);
			try {
				fManager.update(new ICElement[] {element}, IIndexManager.UPDATE_ALL);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}
}
