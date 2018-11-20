/*******************************************************************************
 * Copyright (c) 2007, 2015 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.tracepointactions;

import org.eclipse.cdt.dsf.gdb.internal.tracepointactions.TracepointActionManager;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
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

/**
 * @since 2.1
 */
public class TracepointActionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private String contextHelpID = "tracepoint_actions_page_help"; //$NON-NLS-1$

	public TracepointActionsPreferencePage() {
		super();
		setPreferenceStore(GdbUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);

		final Label breakpointActionsAvailableLabel = new Label(container, SWT.NONE);
		breakpointActionsAvailableLabel
				.setText(MessagesForTracepointActions.TracepointActions_Preferences_Actions_Available);
		final TracepointGlobalActionsList actionsList = new TracepointGlobalActionsList(container, SWT.NONE, false,
				null, false);
		actionsList.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		actionsList.getDeleteButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionsList.HandleDeleteButton();
			}
		});
		String helpContextID = GdbUIPlugin.PLUGIN_ID + "." + contextHelpID; //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(super.getControl(), helpContextID);

		return container;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performCancel() {
		TracepointActionManager.getInstance().revertActionData();
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		TracepointActionManager.getInstance().saveActionData();
		return super.performOk();
	}

}
