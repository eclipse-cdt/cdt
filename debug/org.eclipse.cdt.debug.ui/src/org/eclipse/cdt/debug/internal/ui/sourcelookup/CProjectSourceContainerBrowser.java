/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
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
public class CProjectSourceContainerBrowser extends AbstractSourceContainerBrowser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.sourcelookup.ISourceContainerBrowser#createSourceContainers(org.eclipse.swt.widgets.Shell, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director) {
		if (director.getLaunchConfiguration() == null) {
			TargetProjectSourceContainerDialog dialog = new TargetProjectSourceContainerDialog(shell);
			if (dialog.open() == Window.OK) {		
				return new ISourceContainer[] {
						new CProjectSourceContainer(null, dialog.isAddReferencedProjects()) };				
			}	
		} else {
			Object input = ResourcesPlugin.getWorkspace().getRoot();
			IStructuredContentProvider contentProvider=new BasicContainerContentProvider();
			ILabelProvider labelProvider = new WorkbenchLabelProvider();
			ProjectSourceContainerDialog dialog =
					new ProjectSourceContainerDialog(shell, input, contentProvider, labelProvider,
							SourceLookupUIMessages.projectSelection_chooseLabel);
			if (dialog.open() == Window.OK) {		
				ArrayList<ISourceContainer> res= new ArrayList<ISourceContainer>();
				for (Object element : dialog.getResult()) {
					if (!(element instanceof IProject))
						continue;				
					res.add(new CProjectSourceContainer((IProject) element,
							dialog.isAddRequiredProjects()));
				}
				return res.toArray(new ISourceContainer[res.size()]);	
			}	
		}
		return new ISourceContainer[0];
	}
}
