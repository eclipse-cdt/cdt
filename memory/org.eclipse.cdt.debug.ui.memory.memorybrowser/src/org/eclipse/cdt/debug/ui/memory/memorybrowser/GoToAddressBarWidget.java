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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class GoToAddressBarWidget {
	
	private Text fExpression;
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
	
		fExpression = new Text(fComposite, SWT.SINGLE | SWT.BORDER);
		fExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fOKButton = new Button(fComposite, SWT.NONE);
		fOKButton.setText("Go");
		
		fOKNewTabButton = new Button(fComposite, SWT.NONE);
		fOKNewTabButton.setText("New Tab");
		
		return fComposite;
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
}