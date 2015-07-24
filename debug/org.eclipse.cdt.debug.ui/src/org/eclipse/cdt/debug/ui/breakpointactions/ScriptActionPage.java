/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.File;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 7.6
 */
public class ScriptActionPage implements IBreakpointActionPage {
	
	private ScriptAction fAction;
	
	private Text fScriptFileText;

	public ScriptActionPage() {
	}

	@Override
	public void actionDialogCanceled() {
	}

	@Override
	public void actionDialogOK() {
		getAction().setScriptFile(new File(fScriptFileText.getText().trim()));
	}

	@Override
	public Composite createComposite(IBreakpointAction action, final Composite composite, int style) {
		assert (action instanceof ScriptAction);
		
		fAction = (ScriptAction)action;
		
		Composite comp = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("ScriptActionPage.Script_file")); //$NON-NLS-1$
		label.setToolTipText(Messages.getString("ScriptActionPage.Script_file_to_run")); //$NON-NLS-1$
		
		fScriptFileText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fScriptFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button btn = new Button(comp, SWT.PUSH);
		btn.setText("..."); //$NON-NLS-1$
		btn.setToolTipText(Messages.getString("ScriptActionPage.Browse_for_script_file")); //$NON-NLS-1$
		btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(composite.getShell());
				dialog.setFileName(fScriptFileText.getText().trim());
				String newFileName = dialog.open();
				if (newFileName != null) {
					fScriptFileText.setText(newFileName);
				}
			}
		});
		
		initialize(fAction);

		return comp;
	}
	
	protected ScriptAction getAction() {
		return fAction;
	}
	
	private void initialize(ScriptAction action) {
		fScriptFileText.setText(
			action.getScriptFile() != null ? action.getScriptFile().getAbsolutePath() : ""); //$NON-NLS-1$
	}
}
