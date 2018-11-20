/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;

/**
 * @author Emanuel Graf IFS
 */
public abstract class CRefactoringDescriptor extends RefactoringDescriptor {
	public static final String FILE_NAME = "fileName"; //$NON-NLS-1$
	public static final String SELECTION = "selection"; //$NON-NLS-1$
	protected Map<String, String> arguments;

	public CRefactoringDescriptor(String id, String project, String description, String comment, int flags,
			Map<String, String> arguments) {
		super(id, project, description, comment, flags);
		this.arguments = arguments;
	}

	public Map<String, String> getParameterMap() {
		return arguments;
	}

	@Override
	public abstract CRefactoring createRefactoring(RefactoringStatus status) throws CoreException;

	@Override
	public CRefactoringContext createRefactoringContext(RefactoringStatus status) throws CoreException {
		CRefactoring refactoring = createRefactoring(status);
		if (refactoring == null)
			return null;
		return new CRefactoringContext(refactoring);
	}

	protected ISelection getSelection() throws CoreException {
		ISelection selection;
		String selectStrings[] = arguments.get(SELECTION).split(","); //$NON-NLS-1$
		if (selectStrings.length >= 2) {
			int offset = Integer.parseInt(selectStrings[0]);
			int length = Integer.parseInt(selectStrings[1]);
			selection = new TextSelection(offset, length);
		} else {
			throw new CoreException(
					new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, Messages.CRefactoringDescriptor_illegal_selection));
		}
		return selection;
	}

	protected ICProject getCProject() throws CoreException {
		String projectName = getProject();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		ICProject cProject = CoreModel.getDefault().create(project);
		if (cProject == null) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
					NLS.bind(Messages.CRefactoringDescriptor_unknown_project, projectName)));
		}
		return cProject;
	}

	protected IFile getFile() throws CoreException {
		try {
			String filename = arguments.get(FILE_NAME);
			return ResourceLookup.selectFileForLocationURI(new URI(filename),
					ResourcesPlugin.getWorkspace().getRoot().getProject(getProject()));
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	protected ITranslationUnit getTranslationUnit() throws CoreException {
		try {
			String filename = arguments.get(FILE_NAME);
			return CoreModelUtil.findTranslationUnitForLocation(new URI(filename), getCProject());
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}
}