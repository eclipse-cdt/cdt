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

package org.eclipse.rse.ui.widgets;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * @author coulthar
 *
 * This class attempts to encapsulate the states an edit page (with apply/reset buttons)
 * can go through and handle managing the state transitions. For example, it manages
 * the enabled/disabled state of the apply/reset buttons. 
 * <p>
 * There are three <b>modes</b> supported:
 * <ol>
 *   <li>New -> user is creating a "new thing". In this state, the apply button label is
 *               "New", and the reset button is hidden
 *   <li>Edit -> user is editing an existing thing. The apply and reset buttons have usual labels
 *   <li>Unset -> overall composite is hidden
 * </ol>
 * In addition to the modes, there are these <b>states</b> supported
 * <ol>
 *   <li>No changes -> the apply and reset buttons are disabled
 *   <li>Changes pending -> the apply and reset buttons are enabled
 *   <li>Changes made -> the apply and reset buttons are disabled
 * </ol>
 * There are constants for these modes and states in {@link org.eclipse.rse.ui.widgets.ISystemEditPaneStates}
 * <p>
 * To use this properly, call the following methods at the appropriate times:
 * <ul>
 *  <li>{@link #setNewMode()} -> when users selects to create a new thing. 
 *  <li>{@link #setEditMode()} -> when user selects to edit an existing thing.
 *  <li>{@link #setUnsetMode()} -> when user selects nothing or something not editable
 *  <li>{@link #setChangesMade()} -> when user changes anything in the pane!
 *  <li>{@link #isSaveRequired()} -> if changes are pending, this will prompt the user if they wish to save the
 *                              changes or discard the changes. Returns true or false.
 *  <li>{@link #applyPressed()} -> when user successfully presses apply
 *  <li>{@link #resetPressed()} -> when user successfully presses reset
 * </ul>
 */
public class SystemEditPaneStateMachine implements ISystemEditPaneStates
                                                      //, SelectionListener
{
	// state
	private Composite composite;
	private Button    applyButton, resetButton;
	private int       mode, state;
	private int       backupMode, backupState;
	private SystemMessage pendingMsg;
	private String    applyLabel_applyMode;
	private String    applyLabel_newMode;
	private String    applyTip_applyMode;
	private String    applyTip_newMode;
	private boolean  applyLabelMode;
	private boolean newSetByDelete; //d47125
	
		
	/**
	 * Constructor for SystemEditPaneStateMachine.
	 * <p>
	 * This constructor sets the initial mode to MODE_UNSET.
	 * <p>
	 * While this class will handle enabling/disabling the apply/reset buttons,
	 *  it is still your job to add listeners and actually do the applying and resetting!
	 * @param composite - overall composite of the edit pane
	 * @param applyButton - the Apply pushbutton
	 * @param resetButton - the Reset pushbutton. Can be null.
	 */
	public SystemEditPaneStateMachine(Composite composite, Button applyButton, Button resetButton) 
	{
		super();
		this.composite = composite;
		this.applyButton = applyButton;
		this.resetButton = resetButton;
		
		this.applyLabel_applyMode = applyButton.getText();
		this.applyTip_applyMode = applyButton.getToolTipText();
		this.applyLabelMode = true;
		
		setApplyLabelForNewMode(SystemResources.BUTTON_CREATE_LABEL, SystemResources.BUTTON_CREATE_TOOLTIP);
		
		setUnsetMode();
		//setMode(MODE_UNSET);
		//setState(STATE_INITIAL);
		//enableButtons();		
		
		// I have decided it is safer to force the user of this class to call this,
		// since it is possible that Apply will find errors and not actually do the apply
		/*
		applyButton.addSelectionListener(this);
		if (resetButton != null)
		  resetButton.addSelectionListener(this);
		*/
	}
	
	/**
	 * Set the label and tooltip to use for the apply button in "new" mode. 
	 * By default, generic values are used
	 */
	public void setApplyLabelForNewMode(String label, String tooltip)
	{
		this.applyLabel_newMode = label;
		this.applyTip_newMode = tooltip;
	}
	
	/**
	 * Set the mode to "New". User has selected "new" and wants to create a new thing.
	 * It is your responsibility to call {@link #isSaveRequired()} first.
	 * It is assumed that after the object is created by pressing Apply, your UI will 
	 *  select the new object and then call setEditMode
	 */
	public void setNewMode()
	{
        setButtonText(mode, MODE_NEW);
		setMode(MODE_NEW);
		setState(STATE_INITIAL);
		enableButtons();
		if (!composite.isVisible())
		  composite.setVisible(true);
	}
	/**
	 * Set the mode to "Edit". User has selected an existing object and wants to changed/edit it
	 * It is your responsibility to call {@link #isSaveRequired()} first.
	 */
	public void setEditMode()
	{
        setButtonText(mode, MODE_EDIT);
		setMode(MODE_EDIT);
		setState(STATE_INITIAL);
		enableButtons();
		if (!composite.isVisible())
		  composite.setVisible(true);
	}
	/**
	 * Set the mode to "Unset". User has selected nothing or something not editable
	 * It is your responsibility to call {@link #isSaveRequired()} first.
	 */
	public void setUnsetMode()
	{
        setButtonText(mode, MODE_UNSET);
		setMode(MODE_UNSET);
		setState(STATE_INITIAL);
		enableButtons();
		if (composite.isVisible())
		  composite.setVisible(false);
	}
	/**
	 * User has made changes, such as typing text or selecting a checkbox or radio button.
	 * It is VERY important this be called religiously for every possible change the user can make!
	 */
	public void setChangesMade()
	{
		setState(STATE_PENDING);
		enableButtons();
	}
	/**
	 * Query if it is ok to switch modes. 
	 * If no changes pending, returns false
	 * If changes pending, user is asked to whether to save (true) or discard (false).
	 */
	public boolean isSaveRequired()
	{
		boolean changesPending = areChangesPending();
		if (changesPending)
		{
			if (pendingMsg == null)
			{
				pendingMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONFIRM_CHANGES);
			}
		    SystemMessageDialog pendingMsgDlg = new SystemMessageDialog(composite.getShell(), pendingMsg);
			try {
			  changesPending = pendingMsgDlg.openQuestion();
			} catch (Exception exc) {}
		}
	    //if (!changesPending) // user has made decision, so clear state
		setState(STATE_INITIAL); // one way or another, decision has been made
		return changesPending;
	}
	
	/**
	 * User has successfully pressed Apply (that is, no errors found)
	 */
	public void applyPressed()
	{
    	setState(STATE_APPLIED);
    	enableButtons();
	}
	/**
	 * User has successfully pressed Reset (that is, no errors found)
	 */
	public void resetPressed()
	{
    	setState(STATE_INITIAL);
    	enableButtons();
	}	
	
	/**
	 * Are any changes pending?
	 */
	public boolean areChangesPending()
	{
		return (state == STATE_PENDING);
	}

    // -----------------------------------
    // GETTERS FOR STUFF PASSED IN CTOR...
    // -----------------------------------    
	/**
	 * Returns the resetButton.
	 * @return Button
	 */
	public Button getResetButton() 
	{
		return resetButton;
	}

	/**
	 * Returns the applyButton.
	 * @return Button
	 */
	public Button getApplyButton() 
	{
		return applyButton;
	}

	/**
	 * Returns the composite.
	 * @return Composite
	 */
	public Composite getComposite() 
	{
		return composite;
	}

    // -----------------------------------
    // GETTERS FOR MODE AND STATE
    // -----------------------------------    
	/**
	 * Returns the mode.
	 * @return int
	 * @see org.eclipse.rse.ui.widgets.ISystemEditPaneStates
	 */
	public int getMode() 
	{
		return mode;
	}

	/**
	 * Returns the state.
	 * @return int
	 * @see org.eclipse.rse.ui.widgets.ISystemEditPaneStates
	 */
	public int getState() 
	{
		return state;
	}


    // -------------------
    // INTERNAL METHODS...
    // -------------------
    
    /**
     * enable/disable buttons based on state
     */
    private void enableButtons()
    {
    	boolean enableApply = false;
    	boolean enableReset = false;
    	switch(state)
    	{
    		case STATE_INITIAL:
    		    enableApply = false;
    		    enableReset = false;
    		    break;
    		case STATE_APPLIED:
    		    enableApply = false;
    		    enableReset = false; // true; only true if reset returns to pre-applied values. Not usually the case
    		    break;
    		case STATE_PENDING:
    		    enableApply = true;
    		    enableReset = true;
    		    break;
    	}
    	applyButton.setEnabled(enableApply);
    	if (resetButton != null)
    	  resetButton.setEnabled(enableReset);    	  
    }
    
    /**
     * Change apply button label and tooltiptext when switching
     *  to/from new/edit modes.
     */
    private void setButtonText(int oldMode, int newMode)
    {
    	if (oldMode != newMode)
    	{
    		if ((newMode == MODE_NEW) && applyLabelMode)
    		{
    			applyButton.setText(applyLabel_newMode);
    			applyButton.setToolTipText(applyTip_newMode);
    			applyLabelMode = false;
    			if (resetButton != null)
    			{
    			  //resetButton.setVisible(false);
    			  //GridData gd = (GridData)applyButton.getLayoutData();
    			  //if (gd != null)
    			  //{
    			  //  gd.horizontalSpan = 2;
    			  //  composite.layout(true);
    			  //}
    			}
    		}
    		else if ((newMode == MODE_EDIT) && !applyLabelMode)
    		{
    			applyButton.setText(applyLabel_applyMode);
    			applyButton.setToolTipText(applyTip_applyMode);
    			applyLabelMode = true;
    			if (resetButton != null)
    			{
    			  //resetButton.setVisible(true);
    			  //GridData gd = (GridData)applyButton.getLayoutData();
    			  //if (gd != null)
    			  //{
    			  //  gd.horizontalSpan = 1;
    			  //  composite.layout(true);
    			  //}
    			}
    		}
    	}
    }

	/**
	 * Sets the mode.
	 * @param mode The mode to set
	 * @see org.eclipse.rse.ui.widgets.ISystemEditPaneStates
	 */
	private void setMode(int mode) 
	{
		this.mode = mode;
	}

	/**
	 * Sets the state.
	 * @param state The state to set
	 * @see org.eclipse.rse.ui.widgets.ISystemEditPaneStates
	 */
	private void setState(int state) 
	{
		this.state = state;
	}
    
    /*
     * Keep track of the fact that New is selected by the Delete action and not by user
     * so that user can exit later by using OK without supplying a command    //d47125  
     */  
	public void setNewSetByDelete(boolean newSetByDelete) 
	{
		this.newSetByDelete = newSetByDelete;
	}
    
    public boolean getNewSetByDelete() 
	{
		return newSetByDelete;
	}
    /*
     * Internal method.
     * From SelectionListener. Called when user presses Apply or Reset buttons
     *
    public void widgetSelected(SelectionEvent event)
    {
    	Object source = event.getSource();
    	if (source == applyButton)
    	{
    		setState(STATE_APPLIED);
    		enableButtons();
    	}
    	else if (source == resetButton)
    	{
    		setState(STATE_INITIAL);
    		enableButtons();
    	}
    }    
     **
     * Internal method.
     * From SelectionListener. Called when user presses Enter?
     *
    public void widgetDefaultSelected(SelectionEvent event)
    {
    }
    */
    
    /**
     * Backup state method
     */
    public void backup()
    {
    	backupMode = mode;
    	backupState = state;    	
    }
    
    /**
     * Restore state method
     */
    public void restore()
    {
    	switch(backupMode)
    	{
    		case MODE_UNSET:
    		     setUnsetMode();
    		     break;
    		case MODE_NEW:
    		     setNewMode();
    		     break;
    		case MODE_EDIT:
    		     setEditMode();
    		     break;
    	}
    	switch(backupState)
    	{
    		case STATE_PENDING:
    		     setChangesMade();
    		     break;
    		case STATE_INITIAL:
    		     break;
    		case STATE_APPLIED:
    		     applyPressed();
    		     break;
    	}
    }
}