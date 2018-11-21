/*******************************************************************************
 * Copyright (c) 2010, 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.util.ArrayList;

import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceFoldersRelativePathSourceContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.sourcelookup.BasicContainerContentProvider;
import org.eclipse.debug.internal.ui.sourcelookup.browsers.ProjectSourceContainerDialog;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The browser for adding a source folder relative path source container.
 */
public class SourceFoldersRelativePathSourceContainerBrowser extends AbstractSourceContainerBrowser {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		if (director.getLaunchConfiguration() == null) {
			TargetProjectSourceContainerDialog dialog = new TargetProjectSourceContainerDialog(shell);
			if (dialog.open() == Window.OK) {
				return new ISourceContainer[] {
						new SourceFoldersRelativePathSourceContainer(null, dialog.isAddReferencedProjects()) };
			}
		} else {
			Object input = ResourcesPlugin.getWorkspace().getRoot();
			IStructuredContentProvider contentProvider = new BasicContainerContentProvider();
			ILabelProvider labelProvider = new WorkbenchLabelProvider();
			ProjectSourceContainerDialog dialog = new ProjectSourceContainerDialog(shell, input, contentProvider,
					labelProvider, SourceLookupUIMessages.projectSelection_chooseLabel);
			if (dialog.open() == Window.OK) {
				ArrayList<ISourceContainer> res = new ArrayList<>();
				for (Object element : dialog.getResult()) {
					if (!(element instanceof IProject))
						continue;
					res.add(new SourceFoldersRelativePathSourceContainer((IProject) element,
							dialog.isAddRequiredProjects()));
				}
				return res.toArray(new ISourceContainer[res.size()]);
			}
		}
		return new ISourceContainer[0];
	}
}
