/*******************************************************************************
 * Copyright (c) 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @author Emanuel Graf IFS
 */
public abstract class CRefactoringDescription extends RefactoringDescriptor {
	public static final String FILE_NAME = "fileName"; //$NON-NLS-1$
	public static final String SELECTION = "selection"; //$NON-NLS-1$
	protected Map<String, String> arguments;

	public CRefactoringDescription(String id, String project, String description, String comment, int flags,
			Map<String, String> arguments) {
		super(id, project, description, comment, flags);
		this.arguments = arguments;
	}

	public Map<String, String> getParameterMap() {
		return arguments;
	}

	protected ISelection getSelection() throws CoreException {
		ISelection selection;
		String selectStrings[] = arguments.get(SELECTION).split(","); //$NON-NLS-1$
		if (selectStrings.length >= 2) {
			int offset = Integer.parseInt(selectStrings[0]);
			int length = Integer.parseInt(selectStrings[1]);
			selection = new TextSelection(offset,length);
		} else {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, "Illegal Selection")); //$NON-NLS-1$
		}
		return selection;
	}

	protected ICProject getCProject() throws CoreException {
		ICProject proj;
		IProject iProject = ResourcesPlugin.getWorkspace().getRoot().getProject(getProject());
		proj = CoreModel.getDefault().create(iProject);
		if (proj == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, "Unknown Project")); //$NON-NLS-1$
		}
		return proj;
	}

	protected IFile getFile() throws CoreException {
		IFile file;
		try {
			file = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new URI(arguments.get(FILE_NAME)))[0];
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return file;
	}
}