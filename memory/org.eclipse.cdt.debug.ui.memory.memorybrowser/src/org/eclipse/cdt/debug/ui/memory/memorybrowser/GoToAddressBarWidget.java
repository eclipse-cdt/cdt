/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.memorybrowser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class GoToAddressBarWidget {
	
	private Text fExpression;
	private ControlDecoration fEmptyExpression;
	private ControlDecoration fWrongExpression;
	
	private Button fOKButton;
	private Button fOKNewTabButton;
	private Composite fComposite;
	
	protected static int ID_GO_NEW_TAB = 2000;

	/**
	 * @param parent
	 * @return
	 */
	public Control createControl(Composite parent)
	{
		fComposite = new Composite(parent, SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fComposite, // FIXME 	
					".GoToAddressComposite_context"); //$NON-NLS-1$
				
		GridLayout layout = new GridLayout();
		layout.numColumns = 6;
		layout.makeColumnsEqualWidth = false;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		fComposite.setLayout(layout);
	
		fExpression = createExpressionField(fComposite);
		
		fOKButton = new Button(fComposite, SWT.NONE);
		fOKButton.setText(Messages.getString("GoToAddressBarWidget.Go")); //$NON-NLS-1$
		fOKButton.setEnabled(false);
		
		fOKNewTabButton = new Button(fComposite, SWT.NONE);
		fOKNewTabButton.setText(Messages.getString("GoToAddressBarWidget.NewTab")); //$NON-NLS-1$
		fOKNewTabButton.setEnabled(false);
		
		return fComposite;
	}

	private Text createExpressionField(Composite parent) {
		Text expression = new Text(parent, SWT.SINGLE | SWT.BORDER);
		expression.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});
		fEmptyExpression = new ControlDecoration(expression, SWT.LEFT | SWT.CENTER);
		fEmptyExpression.setDescriptionText(Messages.getString("GoToAddressBarWidget.EnterExpressionMessage")); //$NON-NLS-1$
		FieldDecoration fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
		fEmptyExpression.setImage(fieldDec.getImage());

		fWrongExpression = new ControlDecoration(expression, SWT.LEFT | SWT.TOP);
		fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		fWrongExpression.setImage(fieldDec.getImage());
		fWrongExpression.hide();
		
		// leave enough room for decorators
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = Math.max(fEmptyExpression.getImage().getBounds().width, fWrongExpression.getImage().getBounds().width);
		expression.setLayoutData(data);
		return expression;
	}
		
	protected void updateButtons() {
		boolean empty = getExpressionText().length() == 0;
		
		fOKNewTabButton.setEnabled(!empty);
		fOKButton.setEnabled(!empty);
		
		if (empty) 
			fEmptyExpression.show();
		else 
			fEmptyExpression.hide();

		fWrongExpression.hide();
	}

	public int getHeight()
	{
		int height = fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		return height;
	}
	
	public Button getButton(int id)
	{
		if (id == IDialogConstants.OK_ID)
			return fOKButton;
		if (id == ID_GO_NEW_TAB)
			return fOKNewTabButton;
		return null;
	}
	
	public String getExpressionText()
	{
		return fExpression.getText().trim();
	}
	
	public Text getExpressionWidget()
	{
		return fExpression;
	}
	
	/**
	 * decorate expression field according to the status
	 * @param message
	 */
	public void handleExpressionStatus(final IStatus message) {
		if (message.isOK()) {
			fWrongExpression.hide();
		} else {
			fWrongExpression.setDescriptionText(message.getMessage());
			fWrongExpression.show();
		}
	}
}