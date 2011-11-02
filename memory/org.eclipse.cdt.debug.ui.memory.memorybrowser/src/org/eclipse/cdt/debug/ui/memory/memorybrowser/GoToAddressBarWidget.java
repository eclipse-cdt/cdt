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
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.model.provisional.IRecurringDebugContext;
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
	
	/**
	 * Character sequence that is unlikely to appear naturally in a recurring
	 * debug context ID or memory space ID
	 */
	private static String SEPARATOR = "<seperator>";
	
	/**
	 * At a minimum, the expression history is kept on a per launch
	 * configuration basis. Where debug contexts (processes, in practice) can
	 * provide a recurring ID, we further divide the history by those IDs. This
	 * constant is used when no recurring context ID is available.
	 */
	private static String UNKNOWN_CONTEXT_ID = "Unknown";
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
	
	/** The launch configuration attribute prefix used to persist expression history */
	private final static String SAVED_EXPRESSIONS = "saved_expressions";  //$NON-NLS-1$
	
	private final static int MAX_SAVED_EXPRESSIONS = 15 ;
	
	private void addExpressionToHistoryPersistence( Object context, String expr, String memorySpace ) {
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
		
		String contextID = getRecurringContextID(context);
		
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		String currentExpressions = "";
		if (launchConfiguration != null) {
			try {
				ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
				if (wc != null) {
					currentExpressions = wc.getAttribute(getSaveExpressionKey(contextID,memorySpace), "");
					
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
						wc.setAttribute(getSaveExpressionKey(contextID,memorySpace), currentExpressions);					
						wc.doSave();
					}
				}			
			}
			catch(CoreException e) {
			}
		}
	}
	
	/**
	 * Clear all expression history persisted in the launch configuration that
	 * created the given debug context
	 * 
	 * @param context
	 *            the debug context. In practice, this will always be a process
	 *            context
	 */
	public void clearExpressionHistoryPersistence(Object context) {
		if(context == null) {
			return;
		}
		
		ILaunch launch = getLaunch(context);
		if(launch == null) {
			return;
		}
		
		// We maintain history for every process this launch configuration has
		// launched. And where memory spaces are involved, each space has its
		// own history. Here we just wipe out the persistence of all processes
		// and memory spaces stored in the launch configuration that created the
		// given processes.
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		if (launchConfiguration != null) {
			try {
				ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
				if (wc != null) {
					Map<?,?> attributes = wc.getAttributes();
					Iterator<?> iterator = attributes.keySet().iterator();
					while (iterator.hasNext()) {
						String key = (String)iterator.next();
						if (key.startsWith(SAVED_EXPRESSIONS)) {
							wc.removeAttribute(key);
						}
					}
					wc.doSave();
				}
			}
			catch(CoreException e) {
				// Some unexpected snag working with the launch configuration
				MemoryBrowserPlugin.log(e);
			}
		}
	}

	/**
	 * Get the expression history persisted in the launch configuration for the
	 * given debug context and memory space (where applicable)
	 * 
	 * @param context
	 *            the debug context. In practice, this will always be a process
	 *            context
	 * @param memorySpace
	 *            memory space ID or null if not applicable
	 * @return a list of expressions, or empty collection if no history
	 *         available (never null)
	 * @throws CoreException
	 *             if there's a problem working with the launch configuration
	 */
	private String[] getSavedExpressions(Object context, String memorySpace) throws CoreException {
		/*
		 * Get the saved expressions if any.
		 * 
		 * They are in the form
		 * 
		 *     expression,expression,.....,expression
		 */
		
		ILaunch launch = getLaunch(context);
		if(launch == null) {
			return new String[0];
		}
		
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		String expressions = "";
		if (launchConfiguration != null) {
			expressions = launchConfiguration.getAttribute(getSaveExpressionKey(getRecurringContextID(context),memorySpace), "");
		}

		StringTokenizer st = new StringTokenizer(expressions, ","); //$NON-NLS-1$
		/*
		 * Parse through the list creating an ordered array for display.
		 */
		ArrayList<String> list = new ArrayList<String>();
		while(st.hasMoreElements()) {
			list.add(st.nextToken());
		}
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Populate the expression history combobox based on the history persisted
	 * in the launch configuration for the given context and memory space (where
	 * applicable)
	 * 
	 * @param context
	 *            the debug context. In practice, this will always be a process
	 *            context
	 * @param memorySpace
	 *            memory space ID; null if not applicable
	 */
	public void loadSavedExpressions(Object context, String memorySpace)
	{
		try {
			String[] expressions = getSavedExpressions(context, memorySpace);
			String currentExpression = fExpression.getText(); 
			fExpression.removeAll();
			for (String expression : expressions) {
				fExpression.add(expression);
			}
			if (currentExpression != null) {
				fExpression.setText(currentExpression);
			}
			System.out.println("GoToAddressBarWidget: set context field to " + context);
		} catch (CoreException e) {
			// Unexpected snag dealing with launch configuration
			MemoryBrowserPlugin.log(e);
		}
	}
	
	public void addExpressionToHistory(Object context, String expr, String memorySpace) {
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
			 * Add the new expression to the combobox
			 */
			fExpression.add(expr);
			
		}
		/*
		 * Add it to the persistense database.
		 */
		addExpressionToHistoryPersistence(context, expr, memorySpace);
	}
	
	/**
	 * Clears the history of expressions for the given debug context, both in
	 * the GUI and the persistence data
	 * 
	 * @param context
	 *            the debug context. In practice, this will always be a process
	 *            context.
	 */
	public void clearExpressionHistory(Object context) {
		/*
		 * Clear the combobox
		 */
		fExpression.removeAll();
		fExpression.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		/*
		 * Clear the history persisted in the launch configuration
		 */
		clearExpressionHistoryPersistence(context);
		
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
    
    /**
	 * Get the identifier for the given context if it is a recurring one. See
	 * {@link IRecurringDebugContext}
	 * 
	 * @param context
	 *            the debug context
	 * @return the ID or UNKNOWN_CONTEXT_ID if the context is non-recurring or
	 *         can't provide us its ID
	 */
    private String getRecurringContextID(Object context)
    {
    	String id = UNKNOWN_CONTEXT_ID;
		if (context instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) context;
			IRecurringDebugContext recurringDebugContext = (IRecurringDebugContext)adaptable.getAdapter(IRecurringDebugContext.class);
			if (recurringDebugContext != null) {
				try {
					id = recurringDebugContext.getContextID();
				}
				catch(DebugException e) {
					// If the context can't give us the ID, just treat it as a
					// non-recurring context
				}
			}
		}
		return id;
    	
    }
    
    /**
	 * Get a key that we can use to persist the expression history for the given
	 * debug context and memory space (where applicable). The key is used within
	 * the scope of a launch configuration.
	 * 
	 * @param contextID
	 *            a recurring debug context ID; see
	 *            {@link IRecurringDebugContext}
	 * @param memorySpace
	 *            a memory space identifier, or null if not applicable
	 * @return they key which will be used to persist the expression history
	 */
    private String getSaveExpressionKey(String contextID, String memorySpace) {
    	assert contextID.length() > 0;
    	String key = SAVED_EXPRESSIONS + SEPARATOR + contextID;
    	if (memorySpace != null && memorySpace.length() > 0) { 
    		key += SEPARATOR + memorySpace;
    	}
    	return key;
    }

}
