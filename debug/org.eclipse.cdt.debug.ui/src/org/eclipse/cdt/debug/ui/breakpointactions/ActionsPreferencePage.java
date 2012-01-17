/*******************************************************************************
 * Copyright (c) 2007, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class ActionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private String contextHelpID = "breakpoint_actions_page_help"; //$NON-NLS-1$

	public ActionsPreferencePage() {
		super();
		setPreferenceStore(CDebugUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);

		final Label breakpointActionsAvailableLabel = new Label(container, SWT.NONE);
		breakpointActionsAvailableLabel.setText(Messages.getString("ActionsPreferencePage.0")); //$NON-NLS-1$
		final GlobalActionsList actionsList = new GlobalActionsList(container, SWT.NONE, false);
		actionsList.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		actionsList.getDeleteButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionsList.HandleDeleteButton();
			}
		});
		
		String helpContextID = CDebugUIPlugin.PLUGIN_ID + "." + contextHelpID; //$NON-NLS-1$	
		PlatformUI.getWorkbench().getHelpSystem().setHelp(super.getControl(), helpContextID);

		return container;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performCancel() {
		CDebugCorePlugin.getDefault().getBreakpointActionManager().revertActionData();
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		CDebugCorePlugin.getDefault().getBreakpointActionManager().saveActionData();
		return super.performOk();
	}

}
