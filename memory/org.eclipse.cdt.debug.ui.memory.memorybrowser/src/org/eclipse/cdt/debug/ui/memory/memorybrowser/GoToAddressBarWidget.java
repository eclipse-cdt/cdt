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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.model.provisional.ITargetLabelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class GoToAddressBarWidget {
	
	private static String SEPARATOR = "<sperator>";
	private static String UNKNOWN_TARGET_NAME = "Unknown";
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
	private final static int MAX_SAVED_EXPRESSIONS = 15 ;
	
	private void saveExpression( String memorySpace, Object context, String expr ) {
		/*
		 * Get the saved expressions if any.
		 * 
		 * They are in the form
		 * 
		 *     expression,expression,.....,expression
		 */
		ILaunch launch = getLaunch(context);
		if(launch == null)
		{
			return;
		}
		
		String targetName = getTargetName(context);
		
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		String currentExpressions = "";
		if (launchConfiguration != null) {
			try {
				ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
				if (wc != null) {
					currentExpressions = wc.getAttribute(getSaveExpressionKey(targetName,memorySpace), "");
					
					StringTokenizer st = new StringTokenizer(currentExpressions, ","); //$NON-NLS-1$
					/*
					 * Parse through the list creating an ordered array for display.
					 */
					ArrayList<String> list = new ArrayList<String>();
					while(st.hasMoreElements())
					{
						String expression = (String) st.nextElement();
						list.add(expression);
					}
					if(!list.contains(expr))
					{
						list.add(expr);
					
						while(list.size() > MAX_SAVED_EXPRESSIONS)
						{
							list.remove(0);
						}
						
						currentExpressions = "";
						for ( int idx =0 ; idx < list.size(); idx ++ ) {
							if(idx > 0)
							{
								currentExpressions += ",";
							}
							currentExpressions += list.get(idx);
						}
						wc.setAttribute(getSaveExpressionKey(targetName,memorySpace), currentExpressions);					
						wc.doSave();
					}
				}			
			}
			catch(CoreException e) {
			}
		}
	}
	
	public void deleteExpressions(Object context) {
		
		if(context == null)
		{
			return;
		}
		
		ILaunch launch = getLaunch(context);
		if(launch == null)
		{
			return;
		}
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		if (launchConfiguration != null) {
			try {
				ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
				if (wc != null) {
					@SuppressWarnings("unchecked")
					Map<String,Object> attributes = (Map<String,Object>)wc.getAttributes();
					if (attributes != null && !attributes.isEmpty()) {

						Iterator<String> iterator = attributes.keySet().iterator();
						while(iterator.hasNext())
						{
							String key = iterator.next();
							if(key.startsWith(SAVED_EXPRESSIONS))
							{
								wc.removeAttribute(key);
							}
						}
						wc.doSave();
					}	
				}
			}
			catch(CoreException e) {
			}
		}

	}

	private String[] getSavedExpressions(String memorySpace, Object context) {
		/*
		 * Get the saved expressions if any.
		 * 
		 * They are in the form
		 * 
		 *     expression,expression,.....,expression
		 */
		
		ILaunch launch = getLaunch(context);
		if(launch == null)
		{
			return new String[0];
		}
		
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		String expressions = "";
		if (launchConfiguration != null) {
			try {
				expressions = launchConfiguration.getAttribute(getSaveExpressionKey(getTargetName(context),memorySpace), "");
			}
			catch(CoreException e) {
			}
		}

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
	
	public void loadSavedExpressions(String memorySpace, Object context)
	{
		String[] expressions = getSavedExpressions(memorySpace, context);
		String text = fExpression.getText(); 
		fExpression.removeAll();
		for(int idx=0; idx < expressions.length; idx++)
		{
			fExpression.add(expressions[idx]);
		}
		if(text != null)
		{
			fExpression.setText(text);
		}
	}
	
	public void addExpressionToList( String memorySpace, Object context, String expr ) {
		/*
		 * Make sure it does not already exist, we do not want to show duplicates.
		 */
		if ( fExpression.indexOf(expr) == -1 ) {
			/*
			 * Cap the size of the list.
			 */
			while ( fExpression.getItemCount() >= MAX_SAVED_EXPRESSIONS ) {
				fExpression.remove(0);
			}
			
			/*
			 * Add the new expression to the dropdown.
			 */
			fExpression.add(expr);
			
		}
		/*
		 * Add it to the persistense database.
		 */
		saveExpression(memorySpace, context, expr);
	}
	
	public void clearExpressionsFromList(String[] memorySpaces, Object context) {
		/*
		 * Clean up the combo list.
		 */
		fExpression.removeAll();
		fExpression.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		/*
		 * Clean out the expression persistense.
		 */
		deleteExpressions(context);
		
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
	
    private ILaunch getLaunch(Object context)
    {
        IAdaptable adaptable = null;
        ILaunch launch  = null;
		if(context instanceof IAdaptable)
		{
			adaptable = (IAdaptable) context;
			launch  = ((ILaunch) adaptable.getAdapter(ILaunch.class));
		}
		
		return launch;
    	
    }
    
    private String getTargetName(Object context)
    {
    	String targetName = null;
		if(context instanceof IAdaptable)
		{
			IAdaptable adaptable = (IAdaptable) context;
			ITargetLabelProvider labelProvider = (ITargetLabelProvider)adaptable.getAdapter(ITargetLabelProvider.class);
			if(labelProvider != null)
			{
				try
				{
					targetName = labelProvider.getLabel();
				}
				catch(DebugException e)
				{
				}
			}
		}
		if(targetName == null || targetName.trim().length() == 0)
		{
			targetName = UNKNOWN_TARGET_NAME;
		}
		return targetName;
    	
    }
    
    private String getSaveExpressionKey(String targetName, String memorySpace)
    {
    	String key = SAVED_EXPRESSIONS + SEPARATOR + targetName.trim();
    	if(memorySpace != null && memorySpace.trim().length() > 0)
    	{ 
    		key += SEPARATOR + memorySpace.trim();
    	}
    	return key;
    }

}
