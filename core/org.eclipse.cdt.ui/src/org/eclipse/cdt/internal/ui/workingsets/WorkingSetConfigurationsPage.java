/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for the configurations of a working set.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public class WorkingSetConfigurationsPage extends PropertyPage {

	private WorkingSetConfigurationBlock block;

	/**
	 * Initializes me.
	 */
	public WorkingSetConfigurationsPage() {
		super();
		
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(result);

		final WorkspaceSnapshot workspace = WorkingSetConfigurationManager.getDefault()
				.createWorkspaceSnapshot();
		final IWorkingSetProxy.ISnapshot workingSet = getWorkingSet(workspace);

		if (workingSet == null) {
			new Label(result, SWT.NONE).setText(WorkingSetMessages.WSetConfigsPage_noProjects);
		} else {
			block = new WorkingSetConfigurationBlock(workspace, workingSet);
			block.setWorkingSetFilter(new IFilter() {

				@Override
				public boolean select(Object toTest) {
					return toTest == workingSet;
				}
			});

			Control contents = block.createContents(result);
			contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		return result;
	}

	private IWorkingSetProxy.ISnapshot getWorkingSet(WorkspaceSnapshot workspace) {
		IWorkingSetProxy.ISnapshot result = null;
		IWorkingSet realWorkingSet = (IWorkingSet) getElement().getAdapter(IWorkingSet.class);

		if (realWorkingSet != null) {
			result = workspace.getWorkingSet(realWorkingSet.getName());
			if ((result != null) && result.resolveProjects().isEmpty()) {
				// no C/C++ projects to configure
				result = null;
			}
		}

		return result;
	}
	
	@Override
	public boolean performOk() {
		if (!block.build()) {
			// user cancelled: don't save, and don't close the property page
			return false;
		}

		block.save();
		return true;
	}
}
