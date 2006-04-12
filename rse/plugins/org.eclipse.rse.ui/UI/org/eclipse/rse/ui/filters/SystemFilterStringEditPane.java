/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.filters;
import java.util.Vector;

import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



/**
 * This class prompts the user to create or edit the contents of a single 
 * filter string. This edit pane is used in many places, so creating a decent
 * looking subclass is important:
 * <ul>
 *  <li>The first page of the New Filter wizard
 *  <li>The only page of the New Filter String wizard
 *  <li>The right side of the Change Filter dialog, when "new" or an existing filter string is selected
 * </ul>
 * So what is the "contract" the edit pane has to fulfill?
 * <ul>
 *  <li>work in "new" or "edit" mode. In the latter case it is given a String as input. This needs to be
 *       switchable on the fly. This is typically automated by use of a "state machine".
 *  <li>give as output a new or updated String
 *  <li>allow interested parties to know when the contents have been changed, as they change,
 *        and whether there are errors in those changes
 * </ul>
 * Contractually, here are the methods called by the main page of the new filter wizard:
 * <ul> 
 *   <li>addChangeListener                           ... no need to ever override
 *   <li>setSystemFilterPoolReferenceManagerProvider ... no need to ever override
 *   <li>setType                                     ... no need to ever override
 *   <li>setFilterStringValidator                    ... no need to ever override
 *   <li>isComplete                                  ... no need to ever override
 *   <li>createContents                              ... you will typically override
 *   <li>verify                                      ... you will typically override
 *   <li>getInitialFocusControl                      ... you will typically override
 *   <li>getFilterString                             ... you will typically override
 *   <li>areFieldsComplete                           ... you will typically override
 * </ul>
 */
public class SystemFilterStringEditPane implements SelectionListener
{
	// inputs
	protected Shell shell;
	protected String inputFilterString;
	protected Vector listeners = new Vector();
	protected ISystemFilterPoolReferenceManagerProvider refProvider = null;	
	protected ISystemFilterPoolManagerProvider provider = null;
	protected String type;
	protected boolean newMode = true;
	protected boolean changeFilterMode = false;
	protected boolean ignoreChanges;
	//protected boolean editable = true;
	
	// default GUI
	protected Label labelString;
	protected Text  textString;
	protected Button dlgTestButton;
	// state
	protected SystemMessage errorMessage;
    protected boolean skipEventFiring;
    protected int     currentSelectionIndex;	

	/**
	 * Constructor for SystemFilterStringEditPane.
	 * @param shell - the shell of the wizard or dialog host this
	 */
	public SystemFilterStringEditPane(Shell shell) 
	{
		super();
		this.shell  = shell;

	}

	// ------------------------------	
	// HELPER METHODS...
	// ------------------------------

	/**
	 * <i>Helper method. Do not override.</i><br>
	 * Return the shell given us in the ctor
	 */
	protected Shell getShell()
	{
		return shell;
	}
	/**
	 * <i>Helper method. Do not override.</i><br>
	 * Return the input filter string as given us in setFilterString
	 */
	protected String getInputFilterString()
	{
		return inputFilterString;
	}

    /**
	 * <i>Helper method. Do not override.</i><br>
     * Add a separator line. This is a physically visible line.
     */
	protected void addSeparatorLine(Composite parent, int nbrColumns)
	{
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    separator.setLayoutData(data);		
	}
	/**
	 * <i>Helper method. Do not override.</i><br>
	 * Add a spacer line
	 */
	protected void addFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    filler.setLayoutData(data);		
	}
	/**
	 * <i>Helper method. Do not override.</i><br>
	 * Add a spacer line that grows in height to absorb extra space
	 */
	protected void addGrowableFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    data.verticalAlignment = GridData.FILL;
        data.grabExcessVerticalSpace = true;
	    filler.setLayoutData(data);		
	}		
	// ------------------------------	
	// CONFIGURATION/INPUT METHODS...
	// ------------------------------
	/**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
	 * Set the input filter string, in edit mode. 
	 * Or pass null if reseting to new mode.
	 * @param filterString - the filter string to edit or null if new mode
	 * @param selectionIndex - the index of the currently selected filter string. Only used for getCurrentSelectionIndex().
	 */
	public void setFilterString(String filterString, int selectionIndex)
	{
		this.inputFilterString = filterString;	
		this.currentSelectionIndex = selectionIndex;	
		newMode = (filterString == null);
		setIgnoreChanges(true);
		resetFields();
		clearErrorsPending();
		if (inputFilterString != null)
		  doInitializeFields();
		setIgnoreChanges(false);
	}
	
	/**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
	 * Set the input filter string only without any initialzing. 	
	 */
	public void setInputFilterString(String filterString)
	{
		this.inputFilterString = filterString;				
	}
	
	
	/**
	 * <i>Lifecyle method. Call, but do not override.</i><br>
	 * Turn on ignore changes mode. Subclasses typically can just query the inherited
	 *  field ignoreChanges, unless they need to set the ignoreChanges mode in their 
	 *  own composite widgets, in which case they can override and intercept this.
	 */
	protected void setIgnoreChanges(boolean ignoreChanges)
	{
		this.ignoreChanges = ignoreChanges;
	}
	
	/**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
	 * Identify a listener interested in any changes made to the filter string,
	 * as they happen
	 */
	public void addChangeListener(ISystemFilterStringEditPaneListener l)
	{
		listeners.add(l);
	}
	/**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
	 * Remove a listener interested in any changes made to the filter string,
	 * as they happen
	 */
	public void removeChangeListener(ISystemFilterStringEditPaneListener l)
	{
		listeners.remove(l);
	}	
    /**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
     * Sets the contextual system filter pool reference manager provider. That is, it will
     *  be the currently selected subsystem if New Filter is launched from a subsystem.
     * <p>
     * Will be non-null if the current selection is a reference to a 
     * filter pool or filter, or a reference manager provider. 
     * <p>
     * This is not used by default but made available for subclasses.
     * @see #setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider)
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
    	this.refProvider = provider;
    }	
	/**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
	 * Sets the contextual system filter pool manager provider. That is, it will
	 *  be the subsystem factory of the given subsystem, filter pool or filter. Used
	 *  when there is no way to set setSystemFilterPoolReferenceManagerProvider, because
	 *  there isn't one derivable from the selection.
	 * <p>
	 * Will be non-null if the current selection is a reference to a 
	 * filter pool or filter, or a filter pool or filter, or a manager provider itself. 
	 * <p>
	 * This is not used by default but made available for subclasses.
	 * @see #setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider)
	 */
	public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider provider)
	{
		this.provider = provider;
	}	
	
    /**
	 * <i>Getter method, for the use of subclasses. Do not override.</i><br>
     * Return the contextual system filter pool reference manager provider (ie subsystem) that
     *  this was launched from. Will be null if not launched from a subsystem, or reference to a 
     *  filter pool or filter.
     * <p>
     * This is not used by default but made available for subclasses.
     */
    public ISystemFilterPoolReferenceManagerProvider getSystemFilterPoolReferenceManagerProvider()
    {
    	return refProvider;
    }
	/**
	 * <i>Getter method, for the use of subclasses. Do not override.</i><br>
	 * Return the contextual system filter pool manager provider (ie subsystemFactory) that
	 *  this was launched from. Will be null if not launched from a subsystem factory, or 
	 *  a filter pool or filter (or reference).
	 * <p>
	 * This is not used by default but made available for subclasses.
	 */
	public ISystemFilterPoolManagerProvider getSystemFilterPoolManagerProvider()
	{
		return provider;
	}
	
    /**
	 * <i>Helper method you do not need to ever override.</i><br>
     * Set the type of filter we are creating. Types are not used by the base filter 
     * framework but are a way for tools to create typed filters and have unique 
     * actions per filter type.
     * <p>
     * This simply sets the <samp>type</samp> instance variable, so that subclassing code may
     *  access it if it needs to know what type of filter is being created. This method is 
     *  called by the setType method in the SystemNewFilterWizard wizard.
     */
    public void setType(String type)
    {
    	this.type = type;
    }
    
    /**
	 * <i>Configuration method, called from Change Filter dialog. Do not override.</i><br>
     * Called by Change Filter dialog to set on our changeFilterMode flag in case we wish to 
     *  distinguish between new filter and change filter modes
     */
    public void setChangeFilterMode(boolean changeMode)
    {
    	this.changeFilterMode = changeMode;
    }
    /**
	 * <i>Configuration method, called from Change Filter dialog and New Filter wizard. Do not override.</i><br>
     * Called by Change Filter dialog or New Filter wizard when caller has indicated
     *  they want a test button. This is used to set the testButton instance variable.
     * Subclasses show enable/disable it as changes are made, according to valid state.
     */
    public void setTestButton(Button button)
    {
    	this.dlgTestButton = button;
    }
	/**
	 * <i><b>Overridable</b> method, if subclass supports a Test button.</i><br>
	 * Called by owning dialog when common Test button is pressed.
	 * Does nothing by default.
	 */
	public void processTest(Shell shell)
	{
		System.out.println("Someone forgot to override processTest in SystemFilterStringEditPane!");
	}

	/*
	 * Set if the edit pane is not to be editable
	 *
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}*/
	/*
	 * Return whether the edit pane is editable, as set by {@link #setEditable(boolean)}
	 *
	public boolean getEditable()
	{
		return editable;
	}*/

	// ------------------------------	
	// DATA EXTRACTION METHODS
	// ------------------------------
	
	/**
	 * <i><b>Overridable</b> getter method.</i><br>
	 * Get the filter string in its current form. 
	 * This should be overridden if createContents is overridden.
	 * <p>This is the functional opposite of doInitializeFields, which tears apart the input string in update mode,
	 *  to populate the GUIs. This method creates the filter string from the information in the GUI.
	 */
	public String getFilterString()
	{
		if (textString != null)
		  return textString.getText().trim();
		else
		  return inputFilterString;
	}
	/**
	 * <i>Getter method. Do not override.</i><br>
	 * Get the selection index of the filter string we are currently editing.
	 * Used in Change Filter dialog.
	 */
	public int getCurrentSelectionIndex()
	{
		return currentSelectionIndex;
	}
		
	/**
	 * <i><b>Overridable</b> getter method.</i><br>
	 * For page 2 of the New Filter wizard, if it is possible to 
	 *  deduce a reasonable default name from the user input here,
	 *  then return it here. Else, just return null (the default).
	 */
	public String getDefaultFilterName()
	{
		return null;
	}
	
	/**
	 * <i><b>Overridable</b> configuration method, called from Change Filter dialog and New Filter wizard.</i><br>
	 * <b>YOU MUST TEST IF THE GIVEN LABEL IS NULL!</b><br>
	 * In the Change Filter dialog, this edit pane is shown on the right side, beside
	 *  the filter string selection list. Above it is a label, that shows something
	 *  like "Selected Filter String" in edit mode, or "New Filter String" in new mode.
	 * <p>
	 * This method gives subclasses the opportunity to specify unique values for this label.
	 * In addition to setting the text, the tooltip text should also be set. 
	 * <p>
	 * Defaults are supplied.
	 */
	public void configureHeadingLabel(Label label)
	{
		if (label == null)
			return;
		if (!newMode)
		{
		  	label.setText(SystemResources.RESID_CHGFILTER_FILTERSTRING_LABEL);
		  	label.setToolTipText(SystemResources.RESID_CHGFILTER_FILTERSTRING_TOOLTIP);		 
		}
		else
		{
		  	label.setText(SystemResources.RESID_CHGFILTER_NEWFILTERSTRING_LABEL);
		  	label.setToolTipText(SystemResources.RESID_CHGFILTER_NEWFILTERSTRING_TOOLTIP);		 
	    }
	}
	
	// ------------------------------	
	// LIFECYCLE METHODS...
	// ------------------------------

	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * Populate the pane with the GUI widgets. This is where we populate the client area.
	 * @param parent - the composite that will be the parent of the returned client area composite
	 * @return Control - a client-area composite populated with widgets.
	 * 
	 * @see org.eclipse.rse.ui.SystemWidgetHelpers
	 */
	public Control createContents(Composite parent) 
	{		
		
		// Inner composite
		int nbrColumns = 1;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	
		((GridLayout)composite_prompts.getLayout()).marginWidth = 0;
		
		// FILTER STRING PROMPT
		textString = SystemWidgetHelpers.createLabeledTextField(composite_prompts,null,getFilterStringPromptLabel(), getFilterStringPromptTooltip());
		labelString = SystemWidgetHelpers.getLastLabel();
		((GridData)textString.getLayoutData()).widthHint=300;
		
		resetFields();
		doInitializeFields();

		textString.setFocus();
		  		  
		// add keystroke listeners...
		textString.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateStringInput();
				}
			}
		);		
		return composite_prompts;
	}
	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * Return the control to recieve initial focus. Should be overridden if you override createContents
	 */
	public Control getInitialFocusControl()
	{
		return textString;
	}	

	
	

	protected String getFilterStringPromptLabel()
	{
		return SystemResources.RESID_FILTERSTRING_STRING_LABEL;
	}
	
	protected String getFilterStringPromptTooltip()
	{
		return  SystemResources.RESID_FILTERSTRING_STRING_TIP;
	}
	
	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * Initialize the input fields based on the inputFilterString, and perhaps refProvider.
	 * This can be called before createContents, so test for null widgets first!
	 * Prior to this being called, resetFields is called to set the initial default state prior to input
	 */		
	protected void doInitializeFields()
	{
		if (textString == null)
		  return; // do nothing
		if (inputFilterString != null)
		  textString.setText(inputFilterString);
	}
	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * This is called in the change filter dialog when the user selects "new", or selects another string.
	 * You must override this if you override createContents. Be sure to test if the contents have even been created yet!
	 */
	protected void resetFields()
	{
		if (textString != null)
		{
		    textString.setText("");		
		}
	}
	/**
	 * <i>Lifecycle method. Do not override.</i><br>
	 * Instead, override {@link #areFieldsComplete()}.
	 * <p>
	 * This is called by the wizard page when first shown, to decide if the default information
	 *  is complete enough to enable finish. It doesn't do validation, that will be done when
	 *  finish is pressed.
	 */
	public boolean isComplete()
	{
		boolean complete = true;
		if (errorMessage != null) // pending errors?
		  complete = false; // clearly not complete.
		else
		  complete = areFieldsComplete();
		if (dlgTestButton != null)
		  dlgTestButton.setEnabled(complete);
		return complete;
	}
	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * Must be overridden if createContents is overridden.
	 * <p>
	 * This is called by the isComplete, to decide if the default information
	 *  is complete enough to enable finish. It doesn't do validation, that will be done when
	 *  finish is pressed.
	 */
	protected boolean areFieldsComplete()
	{
		if (textString == null)
		  return false;
		else
		  return (textString.getText().trim().length()>0);
	}
	
	/**
	 * <i>Lifecycle method. Do not override.</i><br>
	 * Are errors pending? Used in Change Filter dialog to prevent changing the filter string selection
	 */
	public boolean areErrorsPending()
	{ // d45795
		return (errorMessage != null);
	}
	/**
	 * <i>Lifecycle method. Do not override.</i><br>
	 * Clear any errors pending. Called when Reset is pressed.
	 */
	public void clearErrorsPending()
	{
		errorMessage = null;
	}
	
	// ------------------------------	
	// PRIVATE METHODS
	// ------------------------------
	/**
	 * <i><b>Private</b> method. Do not call or override.</i><br>
	 * Fire an event to all registered listeners, that the user has changed the 
	 *  filter string. Include the error message, if in error, so it can be displayed to the user.
	 * <p>
	 * Because this is used to enable/disable the Next and Finish buttons it is important
	 *  to call it when asked to do verification, even if nothing has changed.
	 * <p>
	 * It is more efficient, however, to defer the event firing during a full verification
	 *  until after the last widget has been verified. To enable this, set the protected
	 *  variable "skipEventFiring" to true at the top of your verify event, then to "false"
	 *  at the end. Then do fireChangeEvent(errorMessage);
	 */
	protected void fireChangeEvent(SystemMessage error)
	{
		if (skipEventFiring)
		   return;
		for (int idx=0; idx<listeners.size(); idx++)
		{
			ISystemFilterStringEditPaneListener l = (ISystemFilterStringEditPaneListener)listeners.elementAt(idx);
			l.filterStringChanged(error);
		}
	}
	
	/**
	 * Tell interested callers to backup changes-pending state, as we are about
	 *  to fire a change event, after which we will want to restore state.
	 */
	protected void fireBackupChangeEvent()
	{
		for (int idx=0; idx<listeners.size(); idx++)
		{
			ISystemFilterStringEditPaneListener l = (ISystemFilterStringEditPaneListener)listeners.elementAt(idx);
			l.backupChangedState();
		}		
	}
	/**
	 * <i><b>Private</b> method. Do not call or override.</i><br>
	 * Tell interested callers to restore changes-pending state, as we are done
	 *  firing a change event and in this case we don't want that state change side effect.
	 */
	protected void fireRestoreChangeEvent()
	{
		for (int idx=0; idx<listeners.size(); idx++)
		{
			ISystemFilterStringEditPaneListener l = (ISystemFilterStringEditPaneListener)listeners.elementAt(idx);
			l.restoreChangedState();
		}		
	}	
 	// ---------------------------------------------
	// METHODS FOR VERIFYING INPUT PER KEYSTROKE ...
	// ---------------------------------------------
  	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * Validates filter string as entered so far in the text field.
	 * Not called if you override createContents() and verify()
	 */
	protected SystemMessage validateStringInput() 
	{			
		if (ignoreChanges)
		  return errorMessage;
	    errorMessage= null;
		//if (validator != null)
	    //  errorMessage = validateFilterString(textString.getText());
	    if ((textString!=null) && (textString.getText().trim().length() == 0))
	      errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_FILTERSTRING_EMPTY);
	    fireChangeEvent(errorMessage);
		//setPageComplete();	      
        //setErrorMessage(errorMessage);	      
		return errorMessage;		
	}

  	/*
	 * Validates filter string using the supplied generic validator.
	 * Child classes who do their own syntax validation can call this method
	 *  to also do uniqueness validation. They are responsible for calling fireChangeEvent though.
	 * @see #setFilterStringValidator(ISystemValidator)
	 *
	protected SystemMessage validateFilterString(String filterString) 
	{			
		if (validator != null)
	      return validator.validate(filterString);
	    else
		  return null;		
	}*/

	// ---------------------------------
	// METHODS FOR VERIFICATION... 
	// ---------------------------------

	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * Does complete verification of input fields. If this 
	 * method returns null, there are no errors and the dialog or wizard can close.
	 * <p>Default implementation calls {@link #validateStringInput()}.
	 *
	 * @return error message if there is one, else null if ok
	 */
	public SystemMessage verify() 
	{
		errorMessage = null;
		Control controlInError = null;
		errorMessage = validateStringInput();
		if (errorMessage != null)
		  controlInError = textString;
		if (errorMessage != null)
		{
			if (controlInError != null)
		      controlInError.setFocus();
		}
		//setPageComplete();		
		return errorMessage;		
	}
	
	// ------------------	
	// EVENT LISTENERS...
	// ------------------
	
	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * User has selected something
	 */
	public void widgetSelected(SelectionEvent event)
	{		
	}
	/**
	 * <i><b>Overridable</b> lifecycle method.</i><br>
	 * User has selected something via enter/dbl-click
	 */
	public void widgetDefaultSelected(SelectionEvent event)
	{
	}
	
	
	// -----------------------
	// Saving related method
	// -----------------------
	/**
	 * Returns whether filter string can be saved implicitly. This is called in the Change dialog
	 * and property page to check whether filter string can be saved if the user does not
	 * explicitly click on Create/Apply button. So, for example, if this method returns <code>false</code>,
	 * and the user has pending changes when he clicks on another entry in the filter string list, we will
	 * not ask user to save pending changes.
	 * By default, returns <code>true</code>
	 * @return <code>true</code> to query user to save pending changes, <code>false</code> otherwise.
	 */
	public boolean canSaveImplicitly() {
		return true;
	}
}