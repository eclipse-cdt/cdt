/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;

import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.SystemActionViewerFilter;
import org.eclipse.rse.ui.view.SystemResourceSelectionForm;
import org.eclipse.rse.ui.view.SystemResourceSelectionInputProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


public abstract class SystemRemoteResourceDialog extends SystemPromptDialog
{
	private SystemResourceSelectionForm	_form;
	private SystemResourceSelectionInputProvider _inputProvider;
	private Object _preSelection;
	private IValidatorRemoteSelection _selectionValidator;
	private boolean _multipleSelectionMode;
	private boolean _showPropertySheet = false;
	private IHost _outputConnection;
	private SystemActionViewerFilter _customViewerFilter;
	private String _message, _tip;
	

	public SystemRemoteResourceDialog(Shell shell, String title, SystemResourceSelectionInputProvider inputProvider)
	{
		super(shell, title);
		_inputProvider = inputProvider;
	}

	protected Control createInner(Composite parent)
	{		
		_form = new SystemResourceSelectionForm(getShell(), parent, this, _inputProvider, getVerbage(), _multipleSelectionMode, getMessageLine());
		initForm();
		createMessageLine(parent);
		return _form.getInitialFocusControl();
	}
	
	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		ISystemMessageLine msgLine = super.createMessageLine(c);
		_form.setMessageLine(msgLine);
		return fMessageLine;
	}
	
	public void initForm()
	{
		_form.setPreSelection(_preSelection);		
		if (_customViewerFilter != null)
		{
			_form.applyViewerFilter(_customViewerFilter);
		}
		else
		{
			_form.applyViewerFilter(getViewerFilter());
		}
		_form.setSelectionValidator(_selectionValidator);
	 	_form.setShowPropertySheet(_showPropertySheet);
	 	_form.setSelectionTreeToolTipText(getTreeTip());
	 	if (_message != null)
	 		_form.setMessage(_message);
	 	if (_tip != null)
	 		_form.setSelectionTreeToolTipText(_tip);
	}

	public void setDefaultSystemConnection(IHost connection, boolean onlyConnection)
	{
		_inputProvider.setSystemConnection(connection, onlyConnection);
	}
	
	public void setSystemTypes(String[] types)
	{
		_inputProvider.setSystemTypes(types);
	}
	
	protected Control getInitialFocusControl()
	{
		return _form.getInitialFocusControl();
	}
	
	public void setPreSelection(Object selection)
	{
		_preSelection = selection;
		if (_form != null)
		{
			_form.setPreSelection(selection);
		}
	}
	
	public void setSelectionValidator(IValidatorRemoteSelection validator)
	{
		_selectionValidator = validator;
	}
	
	public void setCustomViewerFilter(SystemActionViewerFilter viewerFilter)
	{
		_customViewerFilter = viewerFilter;
	}
	
    /**
     * Set multiple selection mode. Default is single selection mode
     * <p>
     * If you turn on multiple selection mode, you must use the getSelectedObjects()
     *  method to retrieve the list of selected objects.
     * <p>
     * Further, if you turn this on, it has the side effect of allowing the user
     *  to select any remote object. The assumption being if you are prompting for
     *  files, you also want to allow the user to select a folder, with the meaning
     *  being that all files within the folder are implicitly selected. 
     *
     * @see #getSelectedObjects()
     */
    public void setMultipleSelectionMode(boolean multiple)
    {
    	_multipleSelectionMode = multiple;
   
    }
    
    /**
     * Set the message shown at the top of the form
     */
    public void setMessage(String message)
    {
    	_message = message;
    	if (_form != null)
    	{
    		_form.setMessage(message);
    	}
    }
    /**
     * Set the tooltip text for the remote systems tree from which an item is selected.
     */
    public void setSelectionTreeToolTipText(String tip)
    {
    	_tip = tip;
    	if (_tip != null)
    	{
    		_form.setSelectionTreeToolTipText(tip);
    	}
    }
    
    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected object.
     * <p>
     * This overload always shows the property sheet
     * <p>
     * Default is false
     */
    public void setShowPropertySheet(boolean show)
    {
    	_showPropertySheet = show;
    }
    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected object.
     * <p>
     * This overload shows a Details>>> button so the user can decide if they want to see the
     * property sheet. 
     * <p>
     * @param show True if show the property sheet within the dialog
     * @param initialState True if the property is to be initially displayed, false if it is not
     *  to be displayed until the user presses the Details button.
     */
    public void setShowPropertySheet(boolean show, boolean initialState)
    {
    	if (show)
    	{
    		_showPropertySheet = initialState;
    	  setShowDetailsButton(true, !initialState);
    	}
    }
    
    /**
     * Return selected file or folder
     */	
    public Object getSelectedObject()
    {
    	if (getOutputObject() instanceof Object[])
    	  return ((Object[])getOutputObject())[0];
    	else
    	  return getOutputObject();
    }
    /**
     * Return all selected objects. This method will return an array of one
     *  unless you have called setMultipleSelectionMode(true)!
     * @see #setMultipleSelectionMode(boolean)
     */	
    public Object[] getSelectedObjects()
    {
    	if (getOutputObject() instanceof Object[])
    	  return (Object[])getOutputObject();
    	else if (getOutputObject() instanceof Object)
    	  return new Object[] {getOutputObject()};
    	else
    	  return null;
    }
    
    public IHost getSelectedConnection()
    {
    	return _form.getSelectedConnection();
    }
    
    /**
     * Private method. 
     * <p>
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		boolean closeDialog = _form.verify();
		if (closeDialog)
		{
			_outputConnection = _form.getSelectedConnection();
			if (_multipleSelectionMode)
			  setOutputObject(_form.getSelectedObjects());
			else
			  setOutputObject(_form.getSelectedObject());
		}
		else
		  setOutputObject(null);
		return closeDialog;
	}	
	/**
	 * Private method.
	 * <p>
	 * Called when user presses DETAILS button. 
	 * <p>
	 * Note the text is automatically toggled for us! We need only
	 * do whatever the functionality is that we desire
	 * 
	 * @param hideMode the current state of the details toggle, prior to this request. If we return true from
	 *   this method, this state and the button text will be toggled.
	 * 
	 * @return true if the details state toggle was successful, false if it failed.
	 */
	protected boolean processDetails(boolean hideMode) 
	{
		_form.toggleShowPropertySheet(getShell(), getContents());
		return true;
	}	
    
	public abstract SystemActionViewerFilter getViewerFilter();
	public abstract String getVerbage();
	public abstract String getTreeTip();
}