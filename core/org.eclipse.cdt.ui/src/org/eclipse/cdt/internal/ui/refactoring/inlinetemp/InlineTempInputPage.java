/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinetemp;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.internal.ui.refactoring.inlinetemp.InlineTempSettings.SelectionType;



public class InlineTempInputPage extends UserInputWizardPage {
	
	private static final String PAGE_NAME = "InputPage"; //$NON-NLS-1$
	
	
	private static class InputForm extends Composite {
		Label lbl;
		public InputForm(Composite parent, final InlineTempSettings settings) {
			super(parent, SWT.NONE);
			final FillLayout layout = new FillLayout(SWT.VERTICAL);
			GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			gridData.horizontalAlignment = GridData.FILL;
			this.setLayoutData(gridData);
			this.setLayout(layout);
			lbl = new Label(this, SWT.HORIZONTAL);
			lbl.setText(NLS.bind(Messages.InputPage_label, settings.getSelected().getName()));
			
			final Button inlineAll = new Button(this, SWT.CHECK);
			inlineAll.setText(Messages.InputPage_inlineAll);
			inlineAll.setSelection(settings.isInlineAll());
			inlineAll.setEnabled(settings.getSelectionType() != SelectionType.DECLARATION);
			inlineAll.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					settings.setInlineAll(inlineAll.getSelection());
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			
			final Button remove = new Button(this, SWT.CHECK);
			remove.setText(Messages.InputPage_removeDeclaration);
			remove.setSelection(settings.isRemoveDeclaration());
			remove.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					settings.setRemoveDeclaration(remove.getSelection());
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			final Button addParenthesis = new Button(this, SWT.CHECK);
			addParenthesis.setText(Messages.InputPage_addParenthesis);
			addParenthesis.setSelection(settings.isAlwaysAddParenthesis());
			addParenthesis.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					settings.setAlwaysAddParenthesis(addParenthesis.getSelection());
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
		}
	}
	
	private InputForm control;
	private final InlineTempSettings settings;
	
	public InlineTempInputPage(InlineTempRefactoring refactoring) {
		super(PAGE_NAME);
		this.settings = refactoring.getSettings();
	}

	
	
	@Override
	public void createControl(Composite parent) {
		this.control = new InputForm(parent, this.settings);
		this.setTitle(this.getName());
		this.setControl(this.control);
	}
}
