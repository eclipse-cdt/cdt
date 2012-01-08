/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * The ExternalEditorInputFactory is used to save and recreate an ExternalEditorInput object.
 * As such, it implements the IPersistableElement interface for storage
 * and the IElementFactory interface for recreation.
 *
 * @see IMemento
 * @see IPersistableElement
 * @see IElementFactory
 *
 * @since 4.0
 */
public class ExternalEditorInputFactory implements IElementFactory {

	public static final String ID = "org.eclipse.cdt.ui.ExternalEditorInputFactory"; //$NON-NLS-1$

    private static final String TAG_PATH = "path";//$NON-NLS-1$
    private static final String TAG_PROJECT = "project";//$NON-NLS-1$

	/*
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	@Override
	public IAdaptable createElement(IMemento memento) {
        // Get the file name.
        String fileName = memento.getString(TAG_PATH);
        if (fileName == null) {
			return null;
		}

        IPath location= new Path(fileName);
        ICProject cProject= null;
        
        String projectName= memento.getString(TAG_PROJECT);
        if (projectName != null) {
        	IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        	if (project.isAccessible() && CoreModel.hasCNature(project)) {
        		cProject= CoreModel.getDefault().create(project);
        	}
        }
		return EditorUtility.getEditorInputForLocation(location, cProject);
	}

	/**
	 * Save the element state.
	 * 
	 * @param memento  the storage
	 * @param input  the element
	 */
	static void saveState(IMemento memento, ExternalEditorInput input) {
		IPath location= input.getPath();
		if (location != null) {
			memento.putString(TAG_PATH, location.toOSString());
		}
		IProject project= null;
		ITranslationUnit unit= input.getTranslationUnit();
		if (unit != null) {
			project= unit.getCProject().getProject();
		}
		if (project == null && input.getMarkerResource() instanceof IProject) {
			project= (IProject)input.getMarkerResource();
		}
		if (project != null) {
			memento.putString(TAG_PROJECT, project.getName());
		}
	}

}
