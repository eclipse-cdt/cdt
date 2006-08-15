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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.rse.ui.widgets.SystemPortPrompt;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The form for the property page for core subsystem properties.
 */
public class SystemSubSystemPropertyPageCoreForm extends AbstractSystemSubSystemPropertyPageCoreForm
{
	
	protected SystemPortPrompt portPrompt;
	protected Label labelUserId, labelUserIdPrompt;
	protected InheritableEntryField textUserId;
    protected boolean portEditable=true, portApplicable=true, userIdApplicable=true;
		// validators
	protected ISystemValidator portValidator;	
	protected ISystemValidator userIdValidator;
		
	/**
	 * Constructor 
	 */
	public SystemSubSystemPropertyPageCoreForm(ISystemMessageLine msgLine, Object caller)
	{
		super(msgLine, caller);
	}

	/**
	 * Create the GUI contents.
	 */
	public Control createInner(Composite composite_prompts, Object inputElement, Shell shell)
	{
		this.shell = shell;
		this.inputElement = inputElement;


		// Port prompt
		// Composite portComposite = SystemWidgetHelpers.createComposite(composite_prompts, 2, 1, false, null, 0, 0);
		// labelPortPrompt = SystemWidgetHelpers.createLabel(composite_prompts, rb.getString(RESID_SUBSYSTEM_PORT_LABEL)+": ");
	    portPrompt = new SystemPortPrompt(composite_prompts, msgLine, true, isPortEditable(), getSubSystem().getConnectorService().getPort(), getPortValidator());

			
	    // UserId Prompt
	    String temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_USERID_LABEL);
		labelUserIdPrompt = SystemWidgetHelpers.createLabel(composite_prompts, temp);
	    userIdApplicable = isUserIdApplicable();
	    if (userIdApplicable)
	    {
          textUserId = SystemWidgetHelpers.createInheritableTextField(
             composite_prompts,SystemResources.RESID_SUBSYSTEM_USERID_INHERITBUTTON_TIP,SystemResources.RESID_SUBSYSTEM_USERID_TIP);
		  textUserId.setFocus();
	    }
        else
	      labelUserId = SystemWidgetHelpers.createLabel(composite_prompts, getTranslatedNotApplicable());	    
		  		  
	    if (!initDone)	
	      doInitializeFields();		  
		
		if (textUserId != null)
		  textUserId.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateUserIdInput();
				}
			}
		);	

		return composite_prompts;
	}

	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		if (portPrompt.isEditable())
		  return portPrompt.getPortField();
		else if (userIdApplicable)
		  return textUserId;
		else
		  return null;
	}

	/**
	 * Return true if the port is editable for this subsystem
	 */
	protected boolean isPortEditable()
	{
		return getSubSystem().getSubSystemConfiguration().isPortEditable();
	}
	/**
	 * Return true if the userId is applicable for this subsystem
	 */
	protected boolean isUserIdApplicable()
	{
		return getSubSystem().getConnectorService().supportsUserId();
	}
	
    private ISystemValidator getPortValidator()
    {
    	if (portValidator == null)
    	{
	      portValidator = getSubSystem().getSubSystemConfiguration().getPortValidator();
    	}
	    return portValidator;
    }
	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
	    ISubSystem ss = getSubSystem();
	    ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();
	    userIdValidator = ssFactory.getUserIdValidator();
	    //getPortValidator();
	    // vendor    
	    labelVendor.setText(ssFactory.getVendor());	    
	    // name    
	    labelName.setText(ss.getName());
	    // connection
	    labelConnection.setText(ss.getHostAliasName());
	    // profile
	    labelProfile.setText(ss.getSystemProfileName());
	    /*
	    // port
	    if (portEditable || portApplicable)
	    {
	      Integer port = ss.getPort();
	      String localPort = null;
	      if (port==null)
	        port = new Integer(0);
		  localPort = port.toString();		  
		  int iPort = port.intValue();
		  if (!portEditable)
		    labelPort.setText(localPort);
		  else
		  {
		    textPort.setLocalText(localPort);
		    textPort.setInheritedText("0 "+SystemResources.RESID_PORT_DYNAMICSELECT));
		    textPort.setLocal(iPort != 0);	    
		  }
	    }
	    */

	    // userId
	    if (userIdApplicable)
	    {
		  String localUserId = ss.getLocalUserId();
		  textUserId.setLocalText(localUserId);
		  String parentUserId = ss.getHost().getDefaultUserId();
		  textUserId.setInheritedText(parentUserId+" "+SystemPropertyResources.RESID_PROPERTY_INHERITED);
		  textUserId.setLocal((localUserId!=null)&&(localUserId.length()>0));	    
	    }
	}
	
	public void doInitializeInnerFields()
	{
		initDone = true;
	    ISubSystem ss = getSubSystem();
	    ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();
	    userIdValidator = ssFactory.getUserIdValidator();

	    // userId
	    if (userIdApplicable)
	    {
		  String localUserId = ss.getLocalUserId();
		  textUserId.setLocalText(localUserId);
		  String parentUserId = ss.getHost().getDefaultUserId();
		  textUserId.setInheritedText(parentUserId+" "+SystemPropertyResources.RESID_PROPERTY_INHERITED);
		  textUserId.setLocal((localUserId!=null)&&(localUserId.length()>0));	    
	    }	    
	}
	
  	/**
  	 * Validate user id value per keystroke
	 */	
	protected SystemMessage validateUserIdInput() 
	{			
	    errorMessage= null;
	    if (textUserId != null)
	    {
	      if (!textUserId.isLocal())
	        return null;
		  if (userIdValidator != null)
	        errorMessage= userIdValidator.validate(textUserId.getText());
	      else if (getUserId().equals(""))
		    errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY);
	    }
		setErrorMessage(errorMessage);		
		//setPageComplete();
		return errorMessage;		
	}

  	/*
  	 * Validate port value per keystroke
	 *
	protected SystemMessage validatePortInput() 
	{			
	    errorMessage= null;
	    if (textPort!=null)
	    {
	      if (!textPort.isLocal())
	        return null;
		  if (portValidator != null)
	        errorMessage= portValidator.validate(textPort.getText());
	      else if (getPort().equals(""))
		    errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY);
	    }
		setErrorMessage(errorMessage);		
		//setPageComplete();
		return errorMessage;		
	}*/


	/**
	 * Return user-entered User Id.
	 */	    
	protected String getUserId()
	{
		return textUserId.getText().trim();
	}
	/*
	 * Return user-entered Port number.
	 *	    
	protected String getPort()
	{
		return textPort.getText().trim();
	}*/

	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = false;
		if (errorMessage == null)
		  pageComplete = (getUserId().length() > 0) && portPrompt.isComplete();
		return pageComplete;
	}
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		boolean complete = isPageComplete();
		if (callerInstanceOfWizardPage)
		{			
		  ((WizardPage)caller).setPageComplete(complete);
		}
		else if (callerInstanceOfSystemPromptDialog)
		{
		  ((SystemPromptDialog)caller).setPageComplete(complete);
		}		
		else if (callerInstanceOfPropertyPage)
		{
		  ((PropertyPage)caller).setValid(complete);
		}		
	}

    /**
     * Validate all the widgets on the form
     */
    public boolean verifyFormContents()
    {
		boolean ok = true;
		SystemMessage errMsg = null;
		Control controlInError = null;
        clearErrorMessage();
		errMsg = portPrompt.validatePortInput();
		if (errMsg != null)
		  controlInError = portPrompt.getPortField(); //textPort.getTextField();
		else
		{
		  errMsg = validateUserIdInput();
		  if (errMsg != null)
		    controlInError = textUserId.getTextField();
		}		
		if (errMsg != null)
		{
		  ok = false;
		  controlInError.setFocus();
		  setErrorMessage(errMsg);
		}
		return ok;
    }

	
	/**
	 * Called by caller when user presses OK
	 */
	public boolean performOk()
	{
		boolean ok = verifyFormContents();
		if (ok)
		{
		  ISubSystem ss = getSubSystem();
		  // PROCESS PORT...
		  if (portPrompt.isEditable())
		    updatePort(ss);
		  
		  // PROCESS USER ID...
		  if (textUserId != null)
		  {
		    String userId = getUserId();
		    updateUserId(ss);
		  }
		}
		return ok;
	}

    /**
     * Change the subsystem user Id value
     */
    private void updateUserId(ISubSystem subsys)
    {
    	//int whereToUpdate = USERID_LOCATION_SUBSYSTEM;
    	String userId = textUserId.getLocalText(); // will be "" if !textuserid.getIsLocal(), which results in wiping out local override
        ISubSystemConfiguration ssFactory = subsys.getSubSystemConfiguration();	    
        // unlike with connection objects, we don't ever allow the user to change the parent's
        // userId value, even if it is empty, when working with subsystems. There is too much 
        // ambiquity as the parent could be the connnection or the user preferences setting for this
        // system type. Because of this decision, we don't need to tell updateSubSystem(...) where
        // to update, as it always the local subsystem.
	    ssFactory.updateSubSystem(getShell(), subsys, true, userId, false, subsys.getConnectorService().getPort()); 		  		                      
    }
    /**
     * Change the subsystem port value
     */
    private void updatePort(ISubSystem subsys)
    {
    	/*
    	String port = textPort.getLocalText(); // will be "" if !textPort.getIsLocal(), which results in wiping out local override
    	Integer portInteger = null;
    	if (textPort.isLocal() && (port.length()>0))
          portInteger = new Integer(port); 
    	else
    	  portInteger = new Integer(0);
    	 */
    	int portInteger = portPrompt.getPort();
        ISubSystemConfiguration ssFactory = subsys.getSubSystemConfiguration();	    
	    ssFactory.updateSubSystem(getShell(), subsys, false, subsys.getLocalUserId(), true, portInteger); 		  		                      
    }

	/**
	 * Return "Not applicable" translated
	 */
	private String getTranslatedNotApplicable()
	{
		if (xlatedNotApplicable == null)
		  xlatedNotApplicable = SystemPropertyResources.RESID_TERM_NOTAPPLICABLE;
		return xlatedNotApplicable;
	} 

    private void setErrorMessage(SystemMessage msg)
    {
    	if (msgLine != null)
    	  if (msg != null)
    	    msgLine.setErrorMessage(msg);
    	  else 
    	    msgLine.clearErrorMessage();
    }
    private void clearErrorMessage()
    {
    	if (msgLine != null)
    	  msgLine.clearErrorMessage();
    }
}