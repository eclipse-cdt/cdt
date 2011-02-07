/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.memorybrowser;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class GoToAddressBarWidget {
	
	private Combo fExpression;
	private ControlDecoration fEmptyExpression;
	private ControlDecoration fWrongExpression;
	
	private Button fOKButton;
	private Button fOKNewTabButton;
	private Composite fComposite;
	
	protected static int ID_GO_NEW_TAB = 2000;
	
	private IStatus fExpressionStatus = Status.OK_STATUS;
	
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
	
	private final static String SAVED_EXPRESSIONS = "saved_expressions";  //$NON-NLS-1$
	private final static int MAX_SAVED_EXPRESSIONS = 30 ;
	
	private void saveExpression( String memorySpace, String expr ) {
		/*
		 * Get the saved expressions if any.
		 * 
		 * They are in the form
		 * 
		 *     expression,expression,.....,expression
		 */
		IPreferenceStore store = MemoryBrowserPlugin.getDefault().getPreferenceStore();
		String currentExpressions = store.getString(SAVED_EXPRESSIONS);
		if ( currentExpressions != null && currentExpressions.length() != 0 ) {
			store.setValue(SAVED_EXPRESSIONS, currentExpressions + ","  + expr);
		}
		else {
			store.setValue(SAVED_EXPRESSIONS, expr);
		}
	}
	
	public void deleteExpressions(String memorySpace) {
		MemoryBrowserPlugin.getDefault().getPreferenceStore().setValue(SAVED_EXPRESSIONS, "");
	}

	private String[] getSavedExpressions(String memorySpace) {
		/*
		 * Get the saved expressions if any.
		 * 
		 * They are in the form
		 * 
		 *     expression,expression,.....,expression
		 */
		IPreferenceStore store = MemoryBrowserPlugin.getDefault().getPreferenceStore();
		String expressions = store.getString(SAVED_EXPRESSIONS);
		
		StringTokenizer st = new StringTokenizer(expressions, ","); //$NON-NLS-1$
		/*
		 * Parse through the list creating an ordered array for display.
		 */
		ArrayList<String> list = new ArrayList<String>();
		while(st.hasMoreElements())
		{
			String expr = (String) st.nextElement();
			list.add(expr);
		}
		return list.toArray(new String[list.size()]);
	}
	
	private String removeOldestExpression( String memorySpace ) {
		String[] currentSavedExpressions = getSavedExpressions(memorySpace);
		if ( currentSavedExpressions.length > 0 ) {
			/*
			 * Remove all expressions and then repopulate the list.
			 */
			deleteExpressions(memorySpace);
			/*
			 * The first in the list is the oldest. So we will delete it by not
			 * putting it back.
			 */
			for ( int idx = 1 ; idx < currentSavedExpressions.length; idx ++ ) {
				saveExpression( memorySpace, currentSavedExpressions[idx]);
			}
			return currentSavedExpressions[0];
		}
		return null;
	}
	
	public void addExpressionToList( String memorySpace, String expr ) {
		/*
		 * Make sure it does not already exist, we do not want to show duplicates.
		 */
		if ( fExpression.indexOf(expr) == -1 ) {
			/*
			 * Cap the size of the list.
			 */
			if ( ( fExpression.getItemCount() + 1 ) > MAX_SAVED_EXPRESSIONS ) {
				fExpression.remove(removeOldestExpression(memorySpace));
			}
			
			/*
			 * Add the new expression to the dropdown.
			 */
			fExpression.add(expr);
			
			/*
			 * Add it to the persistense database.
			 */
			saveExpression(memorySpace, expr);
		}
	}
	
	public void clearExpressionsFromList(String memorySpace) {
		/*
		 * Clean up the combo list.
		 */
		fExpression.removeAll();
		fExpression.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		/*
		 * Clean out the expression persistense.
		 */
		deleteExpressions(memorySpace);
		
		/*
		 * Make sure the status image indicator shows OK.
		 */
		handleExpressionStatus(Status.OK_STATUS);
	}
	
	private Combo createExpressionField(Composite parent){
		/*
		 * Create the dropdown box for the editable expressions.
		 */
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.BORDER);
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});
		
		/*
		 * Populate the list with the expressions from the last time the view was brought up.
		 */
		String[] expressions = getSavedExpressions("");
		for ( String expr : expressions ) {
			combo.add( expr );
		}
		
		fEmptyExpression = new ControlDecoration(combo, SWT.LEFT | SWT.CENTER);
		fEmptyExpression.setDescriptionText(Messages.getString("GoToAddressBarWidget.EnterExpressionMessage")); //$NON-NLS-1$
		FieldDecoration fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);
		fEmptyExpression.setImage(fieldDec.getImage());

		fWrongExpression = new ControlDecoration(combo, SWT.LEFT | SWT.TOP);
		fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		fWrongExpression.setImage(fieldDec.getImage());
		fWrongExpression.hide();
		
		// leave enough room for decorators
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = Math.max(fEmptyExpression.getImage().getBounds().width, fWrongExpression.getImage().getBounds().width);
		combo.setLayoutData(data);
		return combo;
	}
		
	protected void updateButtons() {
		boolean empty = getExpressionText().length() == 0;
		
		fOKNewTabButton.setEnabled(!empty);
		fOKButton.setEnabled(!empty);
		
		if (empty) 
			fEmptyExpression.show();
		else 
			fEmptyExpression.hide();
		
		if (fExpressionStatus.isOK())
		    fWrongExpression.hide();
		else
		    fWrongExpression.show();
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
	
	/**
	 * Get expression text
	 * @return
	 */
	public String getExpressionText()
	{
		return fExpression.getText().trim();
	}

	/**
	 * Update expression text from the widget
	 * @param text 
	 */
	public void setExpressionText(String text)
	{
		fExpression.setText(text);
	}

	public Combo getExpressionWidget()
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
		
		fExpressionStatus = message;
	}
	
	/**
	 * Return the expression status
	 * @return expression status
	 */
	public IStatus getExpressionStatus()
    {
        return fExpressionStatus;
    }
}
