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

package org.eclipse.rse.ui.propertypages;
import java.util.ResourceBundle;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.ISystemMessageLineTarget;
import org.eclipse.rse.ui.messages.SystemDialogPageMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * A base class for property pages that offers value over the base Eclipse PropertyPage
 * class:
 * <ul>
 *   <li>Adds a message line and {@link org.eclipse.rse.ui.messages.ISystemMessageLine} message methods. 
 *   <li>Automatically assigns mnemonics to controls on this page, simplifying this common task. See {#wantMnemonics()}. 
 *   <li>If no Default and Apply buttons wanted (default), the area reserved for this is removed, removing extra white space.
 *   <li>For pages with input controls, simplifies the page validation burden: only one method need be overridden: {@link #verifyPageContents()}
 *       <br>To do on-the-fly validation, in your handler calling setErrorMessage/clearErrorMessage automatically calls setValid, although 
 *               you can call it directly too if you desire.
 *       <br>verifyPageContents is called by default by performOk (be sure to call super.performOk if you override), and 
 *               for multiple property pages, is called when another one is selected.
 * </ul>
 * <p>To get these benefits you must override {@link #createContentArea(Composite)} instead of createContents. 
 * Our base implementation of createContents configures the message line and then calls 
 *  createContentArea and then assigns mnemonics to the content area.
 * </p>
 * 
 */
public abstract class SystemBasePropertyPage extends PropertyPage 
       implements ISystemMessages, ISystemMessageLine, ISystemMessageLineTarget
{


	protected ISystemMessageLine   msgLine;
	protected boolean             msgLineSet = false;
    protected Composite            contentArea, buttonsComposite;
	private   Cursor               waitCursor;
	private   String               helpId;            
	    	
	/**
	 * Constructor for SystemBasePropertyPage
	 */
	public SystemBasePropertyPage() 
	{
		super();
	}
	
	/**
	 * <i>Parent intercept. No need to call or override.</i><br>
     * Our base implementation of createContents configures them message line and then calls 
     *  {@link #createContentArea(Composite)} and then assigns mnemonics to the content area. 
     *  Also calls {@link #noDefaultAndApplyButton()} if {@link #wantDefaultAndApplyButton()} returns false.
     * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 * @see #createContentArea(Composite)
	 */
	protected Control createContents(Composite parent)
	{
		// TODO - redesign message line so it works in Eclipse 3.0
		// DKM commenting this out for now to avoid exceptions
		//configureMessageLine();
		if (!wantDefaultAndApplyButton())
		  noDefaultAndApplyButton();				 
		Control c = createContentArea(parent);
		if ((c != null) && (c instanceof Composite))
		{
			contentArea = (Composite)c;
			if (helpId != null)
			  	SystemWidgetHelpers.setHelp(contentArea, helpId);	 
			if (wantMnemonics())
              	(new Mnemonics()).setOnPreferencePage(true).setMnemonics(contentArea);        				  
		}
			configureMessageLine();
		return c;
	}
	
	/**
	 * <i>Configuration method. Override only to change the default. </i><br>
	 * Return true if you want to see Apply and Restore Defaults buttons. This is queried by
	 *  the default implementation of createContents and the default is false, we don't want
	 *  to see them. Default is <b>false</b>.
	 */
	protected boolean wantDefaultAndApplyButton()
	{
		return false;
	}
	
	/**
	 * <i>Configuration method. Override only to change the default. </i><br>
	 * Return false if you don't want to have mnemonics automatically applied to your page
	 * by this parent class. Default is <b>true</b>.
	 */
	protected boolean wantMnemonics()
	{
		return true;
	}
	/**
	 * <i>Configuration method. Override only to change the default. </i><br>
	 * Return false if you don't want to automatically set whether the page is valid based
	 *  on error message status. Default is <b>true</b>
	 */
	protected boolean wantAutomaticValidManagement()
	{
		return true;
	}

	/**
	 * For setting the default overall help for the dialog.
	 * This can be overridden per control by calling {@link #setHelp(Control, String)}.
	 */
	public void setHelp(String helpId)
	{
		if (contentArea != null)
		{
		  SystemWidgetHelpers.setHelp(contentArea, helpId); 
		  SystemWidgetHelpers.setHelp(contentArea, helpId); 
		  //SystemWidgetHelpers.setCompositeHelp(parentComposite, helpId, helpIdPerControl);		
		  //SystemWidgetHelpers.setCompositeHelp(buttonsComposite, helpId, helpIdPerControl);		
		}
		this.helpId = helpId;
	}
		
	/**
	 * <i><b>Abstract</b>. You must override.</i><br>
	 * This is where child classes create their content area versus createContent,
	 *  in order to have the message line configured for them and mnemonics assigned.
	 */
	protected abstract Control createContentArea(Composite parent);
	
	
	/**
	 * <i><b>Private</b>. No need to call or override.</i><br>
	 * Configure the message line if not already. Called for you if you override createContentArea
	 *  versus createContents, else you might choose to call it yourself.
	 */
	protected void configureMessageLine()
	{
//		if (msgLine == null)
          //msgLine = SystemPropertiesMessageLine.configureMessageLine(this);
         // msgLine = SystemDialogPageMessageLine.createPropertyPageMsgLine(this);
	}

    /**
	 * <i><b>Private</b>. No need to call or override.</i><br>
     * Override of parent to delete the button bar since we don't use it, and to make this
     *  page fit on a 800x600 display
     */
    protected void contributeButtons(Composite buttonBar) 
    {
    	this.buttonsComposite = buttonBar;
		if (helpId != null)
		  SystemWidgetHelpers.setHelp(buttonsComposite, helpId);	

		if (wantDefaultAndApplyButton())
		  super.contributeButtons(buttonBar);
		else
		{
		  // see createControl method in org.eclipse.jface.preference.PreferencePage
    	  Composite content = buttonBar.getParent();
    	  Composite pageContainer = content.getParent();
    	  //DY The parent PreferencePage class handles this now for us
    	  //DY buttonBar.setVisible(false);
    	  //DY buttonBar.dispose();   

    	  if ((contentArea != null) && (contentArea.getLayout() != null) &&
    	      (contentArea.getLayout() instanceof GridLayout))
    	  {
    	    ((GridLayout)contentArea.getLayout()).marginHeight = 0;
    	    if (contentArea.getLayoutData() instanceof GridData)
    	      ((GridData)contentArea.getLayoutData()).grabExcessVerticalSpace = false;
    	    contentArea.pack();
    	  }
    	  if (content != null)
    	  {
    	  	if (content.getLayout() instanceof GridLayout)
    	  	{
    	  		GridLayout layout = (GridLayout)content.getLayout();
	            //layout.marginHeight= 0; layout.marginWidth= 0;
    	  	}
    	    content.pack();
    	  }
		}
    }

	/**
	 * <i>Parent intercept. No need to call or override.</i><br>
	 * The <code>PreferencePage</code> implementation of this 
	 * <code>IPreferencePage</code> method returns <code>true</code>
	 * if the page is valid.
	 * <p>
	 * We first test isValid() just like our parent implementation does, 
	 * but since that only represents the valid state of the
	 * last control the user interacted with, we also call verifyPageContents.
	 * <p>
	 * Subclasses must override {@link #verifyPageContents()} to do full error checking on all
	 *  the widgets on the page. 
	 */
	public boolean okToLeave() 
	{		
		super.okToLeave();
		boolean ok = isValid();
		if (ok)
		{
			ok = verifyPageContents();
		}
		//System.out.println("Inside okToLeave. returning "+ok);
		return ok;
	}

    /**
	 * <i><b>Abstract</b>. You must override. Return true if no input fields to check.</i><br>
     * Validate all the widgets on the page. Based on this, the Eclipse framework will know whether
     *  to veto any user attempt to select another property page from the list on the left in the 
     *  Properties dialog.
	 * <p>
	 * Subclasses should override to do full error checking on all the widgets on the page. Recommendation:<br>
	 * <ul>
	 * <li>If an error is detected, issue a {@link org.eclipse.rse.ui.messages.SystemMessage} via {@link #setErrorMessage(SystemMessage)} or text message via {@link #setErrorMessage(String)}.
	 * <li>If no errors detected, clear the message line via {@link #clearErrorMessage()}
	 * </ul>
	 * 
	 * @return true if there are no errors, false if any errors were found.
     */
    protected abstract boolean verifyPageContents();
    /*
    {
    	return true;
    }*/

	/** 
	 * Method declared on IPreferencePage.
	 * Our implementation is to call okToLeave(), which in turn calls verifyPageContents,
	 *  returning true iff they do.
	 * If you override, call super.performOk() to get default processing, and return false if that returns false.
	 * @return true if all is well, false if there is an error.
	 */
	public boolean performOk() 
	{
		boolean oldValid = isValid();
		boolean newValid = okToLeave();		
		setValid(oldValid);
		return newValid;
	}	
    // -----------------------------------
    // ISystemMessageLineTarget methods...
    // -----------------------------------
    /**
	 * <i>ISystemMessageLineTarget method.</i><br>
     * Set the message line to use for issuing messages
     */
    public void setMessageLine(ISystemMessageLine msgLine)
    {
    	//System.out.println("Inside setMessageLine");
    	this.msgLine = msgLine;
    	msgLineSet = (msgLine != null);
    }
    /**
	 * <i>ISystemMessageLineTarget method.</i><br>
     * Get the message line to use for issuing messages
     */
    public ISystemMessageLine getMessageLine()
    {
    	//if (msgLineSet)
    	//  return msgLine;    	
    	//else
    	  return this;
    }

    // -----------------------------
    // Helper methods...
    // -----------------------------
	/**
	 * <i>Helper method.</i><br>
	 * Set the cursor to the wait cursor (true) or restores it to the normal cursor (false).
	 */
	public void setBusyCursor(boolean setBusy)
	{
		if (setBusy)
		{
          // Set the busy cursor to all shells.
    	  Display d = getShell().getDisplay();
    	  waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
		  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(getShell(), waitCursor);
		}
		else
		{
		  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(getShell(), null);
		  if (waitCursor != null)
		    waitCursor.dispose();
		  waitCursor = null;
		}
	}

    /**
	 * <i>Helper method.</i><br>
     * Add a separator line. This is a physically visible line.
     */
	protected Label addSeparatorLine(Composite parent, int nbrColumns)
	{
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    separator.setLayoutData(data);		
	    return separator;
	}
	/**
	 * <i>Helper method.</i><br>
	 * Add a spacer line
	 */
	protected Label addFillerLine(Composite parent, int nbrColumns)
	{
		Label filler = new Label(parent, SWT.LEFT);	
	    GridData data = new GridData();
	    data.horizontalSpan = nbrColumns;
	    data.horizontalAlignment = GridData.FILL;
	    filler.setLayoutData(data);	
	    return filler;	
	}

	/**
	 * Sets this control to grab any excess horizontal space
	 * left in the window. This is useful to do in a property page
	 * to force all the labels on the right to not be squished up on the left.
	 *
	 * @param control  the control for which to grab excess space
	 */
	protected Control grabExcessSpace(Control control) 
	{
		GridData gd = (GridData) control.getLayoutData();
		if (gd != null) 
			gd.grabExcessHorizontalSpace = true;
		return control;
	}
	
	/**
	 * Create a labeled label, where the label on the right grabs excess space and has an indent so it 
	 *   isn't smashed up against the prompt on the left.
	 * @see SystemWidgetHelpers#createLabeledLabel(Composite, ResourceBundle, String, boolean)
	 * @see #grabExcessSpace(Control)
	 */
	protected Label createLabeledLabel(Composite c, String label, String tooltip)
	{
		Label l = SystemWidgetHelpers.createLabeledLabel(c, label, tooltip, false);
		GridData gd = (GridData)l.getLayoutData();
		if (gd != null) 
		{
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalIndent = 10;
		}
		return l;
	}
	/**
	 * Create a labeled combo, where the combo on the right grabs excess space and has an indent so it 
	 *   isn't smashed up against the prompt on the left.
	 * @see SystemWidgetHelpers#createLabeledCombo(Composite, Listener, ResourceBundle, String)
	 * @see #grabExcessSpace(Control)
	 */
	protected Combo createLabeledCombo(Composite c, String label, String tooltip)
	{
		Combo combo = SystemWidgetHelpers.createLabeledCombo(c, null, label, tooltip);
		GridData gd = (GridData)combo.getLayoutData();
		if (gd != null) 
		{
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalIndent = 10;
		}
		return combo;
	}
	/**
	 * Create a labeled entry field, where the field on the right grabs excess space and has an indent so it 
	 *   isn't smashed up against the prompt on the left.
	 * @see SystemWidgetHelpers#createLabeledTextField(Composite, Listener, ResourceBundle, String)
	 * @see #grabExcessSpace(Control)
	 */
	protected Text createLabeledText(Composite c, String label, String tooltip)
	{
		Text field = SystemWidgetHelpers.createLabeledTextField(c, null, label, tooltip);
		GridData gd = (GridData)field.getLayoutData();
		if (gd != null) 
		{
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalIndent = 10;
		}
		return field;
	}
	/**
	 * Create a labeled verbage field, where the field on the right grabs excess space and has an indent so it 
	 *   isn't smashed up against the prompt on the left.
	 * @see SystemWidgetHelpers#createLabeledTextField(Composite, Listener, ResourceBundle, String)
	 * @see #grabExcessSpace(Control)
	 */
	protected Label createLabeledVerbage(Composite c, String label, String tooltip)
	{
		Label verbage = SystemWidgetHelpers.createLabeledVerbage(c, label, tooltip, 1, false, 200);
		GridData gd = (GridData)verbage.getLayoutData();
		if (gd != null) 
		{
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalIndent = 10;
		}
		return verbage;
	}
    // -----------------------------
    // ISystemMessageLine methods...
    // -----------------------------
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage() 
	{
		if (msgLine!=null)
			msgLine.clearErrorMessage();
	    else
	       super.setErrorMessage(null);
		if (wantAutomaticValidManagement())
	      setValid(true);
	}
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		if (msgLine!=null)
			msgLine.clearMessage();
	    else
	       super.setMessage(null);
	}
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage()
	{
		if (msgLine!=null)
		  return msgLine.getSystemErrorMessage();
		else
		  return null;		
	}
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		super.setErrorMessage(message);
		if (wantAutomaticValidManagement())
          setValid(message == null);
        if (msgLine != null)
          ((SystemDialogPageMessageLine)msgLine).internalSetErrorMessage(message);
	}
	
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message)
	{
		if (msgLine!=null)
			msgLine.setErrorMessage(message);
	    else
	       super.setErrorMessage(message.getLevelOneText());
		if (wantAutomaticValidManagement())
          setValid(message == null);
	}
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Convenience method to set an error message from an exception
	 */
	public void setErrorMessage(Throwable exc)
	{
		if (msgLine != null)
		  msgLine.setErrorMessage(exc);
	}	
	
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Set the error message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message) 
	{
		if (msgLine!=null)
			msgLine.setMessage(message);
	    else
	       super.setMessage(message.getLevelOneText());
	}
	/**
	 * <i>ISystemMessageLine method.</i><br>
	 * Set the non-error message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message) 
	{
		super.setMessage(message);
		if (msgLine!=null)
          ((SystemDialogPageMessageLine)msgLine).internalSetMessage(message);
	}

}