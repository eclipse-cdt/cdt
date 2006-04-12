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

package org.eclipse.rse.ui;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.propertypages.RemoteSystemsPreferencePage;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorConnectionName;
import org.eclipse.rse.ui.validators.ValidatorUserId;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.rse.ui.wizards.SystemNewConnectionWizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;



/**
 * A reusable form for prompting for connection information,
 *  in new or update mode.
 * May be used to populate a dialog or a wizard page.
 * <p>
 * You may subclass this to refine the form. In this case you can
 *  override the getForm method of SystemNewConnectionWizardDefaultMainPage 
 *  and SystemUpdateConnection to return your subclass.
 * </p>
 */

public class SystemConnectionForm 
	   implements Listener,  ISystemMessages, ISystemUserIdConstants, 
	               SelectionListener, Runnable, IRunnableWithProgress
{
	
	public static final boolean CREATE_MODE = false;
	public static final boolean UPDATE_MODE = true;
	public static String lastSystemType = null;  

	
	// GUI widgets
	protected Label labelType, labelConnectionName,labelHostName,labelUserId, labelDescription, labelProfile;
	protected Label labelTypeValue, labelSystemTypeValue, labelProfileValue;
	protected Combo textSystemType,textHostName, profileCombo;
	protected Text  textConnectionName, textDescription;
	protected Button verifyHostNameCB;
	
	// yantzi:artemis 6.0, work offline support
	protected Button workOfflineCB;
	
	protected InheritableEntryField textUserId;
	protected Label  textSystemTypeReadOnly; // for update mode

	// validators
	protected ISystemValidator[] nameValidators;
	protected ISystemValidator hostValidator;	
	protected ISystemValidator userIdValidator;
	
	// other inputs
	protected ISystemMessageLine          msgLine;
	protected ISystemConnectionFormCaller caller;
    protected String[]                    restrictSystemTypesTo;	
	protected String   defaultSystemType, defaultConnectionName, defaultHostName;
	protected String   defaultUserId, defaultDescription, defaultProfile; // update mode initial values	                  
	protected String[] defaultProfileNames;
	protected boolean  defaultWorkOffline;
	
	protected boolean  userPickedVerifyHostnameCB = false;
	
	// max lengths
	protected int hostNameLength = 100;
	protected int connectionNameLength = ValidatorConnectionName.MAX_CONNECTIONNAME_LENGTH;
	protected int userIdLength = 100;
	protected int descriptionLength = 100;
	
	// state/output
	protected int     userIdLocation = USERID_LOCATION_CONNECTION;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog, callerInstanceOfPropertyPage;
	protected boolean userIdFromSystemTypeDefault;
	protected boolean updateMode = false;
	protected boolean initDone = false;
	protected boolean contentsCreated = false;
	protected boolean connectionNameEmpty = false;
    protected boolean connectionNameListen = true;
    protected boolean singleTypeMode = false;
	protected String  originalHostName = null;
	protected String  currentHostName = null;
	protected SystemMessage  errorMessage = null;
	protected SystemMessage  verifyingHostName;
    
	/**
	 * Constructor.
	 * @param msgLine A GUI widget capable of writing error messages to.
	 * @param caller The wizardpage or dialog hosting this form.
	 */
	public SystemConnectionForm(ISystemMessageLine msgLine, ISystemConnectionFormCaller caller)
	{
		this.msgLine = msgLine;
		this.caller = caller;
		this.defaultProfileNames = RSEUIPlugin.getTheSystemRegistry().getActiveSystemProfileNames();
		callerInstanceOfWizardPage = (caller instanceof WizardPage);
		callerInstanceOfSystemPromptDialog = (caller instanceof SystemPromptDialog);		
		callerInstanceOfPropertyPage = (caller instanceof PropertyPage);

        userIdValidator = new ValidatorUserId(true); // false => allow empty? Yes.
        defaultUserId = "";
	}

    // -------------------------------------------------------------
    // INPUT METHODS CALLABLE BY CALLER TO INITIALIZE INPUT OR STATE
    // -------------------------------------------------------------
    
	/**
	 * Often the message line is null at the time of instantiation, so we have to call this after
	 *  it is created.
	 */
	public void setMessageLine(ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
	}

	/**
	 * Call this to specify a validator for the connection name. It will be called per keystroke.
	 * You must supply one per active profile, as connections must be unique per profile.
	 * The order must be the same as the order of profiles given by getActiveSystemProfiles() in
	 * the system registry.
	 */
	public void setConnectionNameValidators(ISystemValidator[] v)
	{
		nameValidators = v;
	}
	/**
	 * Call this to specify a validator for the hostname. It will be called per keystroke.
	 */
	public void setHostNameValidator(ISystemValidator v)
	{
		hostValidator = v;
	}	
	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v)
	{
		userIdValidator = v;
	}
 
    /**
     * Set the profile names to show in the combo
     */
    public void setProfileNames(String[] names)
    {
    	this.defaultProfileNames = names;
    	if (profileCombo != null)
    	  profileCombo.setItems(names);
    }       
    /**
     * Set the profile name to preselect
     */
    public void setProfileNamePreSelection(String selection)
    {
    	this.defaultProfile = selection;
    	if ((profileCombo != null) && (selection != null))
    	{
	        int selIdx = profileCombo.indexOf(selection);
	        if (selIdx >= 0)
		      profileCombo.select(selIdx);			
		    else
		      profileCombo.select(0);
    	}
    }       
	
    /**
     * For "new" mode, allows setting of the initial user Id. Sometimes subsystems
     *  like to have their own default userId preference page option. If so, query
     *  it and set it here by calling this.
     */
    public void setUserId(String userId)
    {
    	defaultUserId = userId;
    }	
    /**
     * Set the currently selected connection so as to better initialize input fields
     */
    public void setCurrentlySelectedConnection(IHost connection)
    {
    	if (connection != null)
    	{
		  initializeInputFields(connection, false);
    	}
    }
    /**
     * Call this to restrict the system type that the user is allowed to choose
     */
    public void restrictSystemType(String systemType)
    {
    	if (systemType.equals("*"))
    	  return;
    	this.restrictSystemTypesTo = new String[1];
    	this.restrictSystemTypesTo[0] = systemType;
    	if (defaultSystemType == null)
    	  defaultSystemType = systemType;
    }	
    /**
     * Call this to restrict the system types that the user is allowed to choose
     */
    public void restrictSystemTypes(String[] systemTypes)
    {
    	if (systemTypes == null)
    	  return;
    	else if ((systemTypes.length==1) && (systemTypes[0].equals("*")))
    	  return;
    	this.restrictSystemTypesTo = systemTypes;
    	if (defaultSystemType == null)
    	  defaultSystemType = systemTypes[0];
    }	

     
	/**
	 * Initialize input fields to current values in update mode.
	 * Note in update mode we do NOT allow users to change the system type.
	 * <b>This must be called <i>after</i> calling getContents!
	 * You must also be sure to pass true in createContents in order to call this method.
	 * @param conn The SystemConnection object that is being modified.
	 */
	public void initializeInputFields(IHost conn)
	{
		initializeInputFields(conn, true);
	}
	/**
	 * Initialize input fields to current values in update mode.
	 * Note in update mode we do NOT allow users to change the system type.
	 * <b>This must be called <i>after</i> calling getContents!
	 * You must also be sure to pass true in createContents in order to call this method.
	 * @param conn The SystemConnection object that is being modified.
	 */
	public void initializeInputFields(IHost conn, boolean updateMode)
	{
		this.updateMode = updateMode;
		defaultSystemType = conn.getSystemType();
		defaultConnectionName = conn.getAliasName();
		defaultHostName = conn.getHostName();
		defaultUserId = conn.getLocalDefaultUserId();
		defaultDescription = conn.getDescription();
		defaultProfile = conn.getSystemProfile().getName();
		defaultWorkOffline = conn.isOffline();

		if (updateMode)
		{
		  defaultProfileNames = new String[1];
		  defaultProfileNames[0] = defaultProfile;
		}
		
	    if (contentsCreated)	
	      doInitializeFields();
	}
	
	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name)
	{
		defaultConnectionName = name;
	}
	/**
	 * Preset the host name
	 */
	public void setHostName(String name)
	{
		defaultHostName = name;
	}

    
	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = false;
		
		if (errorMessage == null) {
		    pageComplete = ((getConnectionName().length() > 0)
		                  && (getHostName().length() > 0) 
		                  && (getProfileName().length() > 0));
		}
		               
		return pageComplete;
	}
	
	/**
	 * @return whether the connection name is unique or not
	 */
	public boolean isConnectionUnique()
	{		
		// DKM - d53587 - used to check connection name uniqueness without displaying the error
		// TODO - need to display a directive for the user to specify a unique connection name 
		//        when it's invalid to be consistent with the rest of eclipse rather than
		// 		  an error message
		int selectedProfile = 0;
		if (profileCombo != null)
		{
			selectedProfile = profileCombo.getSelectionIndex();
		}
		if (selectedProfile < 0)
		  selectedProfile = 0;
		
		ISystemValidator nameValidator = null;
		if ((nameValidators!=null) && (nameValidators.length>0))
			nameValidator = nameValidators[selectedProfile];
		String connName = textConnectionName.getText().trim();
		if (nameValidator != null)
			errorMessage = nameValidator.validate(connName);
			
		if (errorMessage != null)
		{
			return false;
		}
		else
		{
			return true;			  
		}	
		
	}
	
    
	// ---------------------------------------------
	// OUTPUT METHODS FOR EXTRACTING USER DATA ... 
	// ---------------------------------------------
	/**
	 * Verifies all input. Call this when user presses OK or Finish your dialog or wizard.
	 * @param okPressed true if this verify is being done when OK is pressed. If so, we position
	 *    cursor on error checking, else we do not.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify(boolean okPressed) 
	{
		boolean ok = true;
		
		//SystemMessage errMsg = null;
		Control controlInError = null;
		if (msgLine != null)
			msgLine.clearErrorMessage();
		  
		// validate connection name...
		errorMessage = validateConnectionNameInput(false);
		if (errorMessage != null)
			controlInError = textConnectionName;
		
		// validate host name...
		if ((errorMessage == null) && (textHostName != null))
		{
			errorMessage = validateHostNameInput();
			if (errorMessage != null)
		   		controlInError = textHostName;
		}		
		// validate user Id...
		if ((errorMessage == null) && (textUserId != null))
		{
		  	errorMessage = validateUserIdInput();
		  	if (errorMessage != null)
		  		controlInError = textUserId;
		}
		// verify host name...
		if ((errorMessage == null) && okPressed && (textHostName != null) && verifyHostNameCB.getSelection())
		{
			currentHostName = textHostName.getText().trim();
			if (currentHostName.length() > 0)
			{
			    if (verifyingHostName == null) {
			        verifyingHostName = RSEUIPlugin.getPluginMessage(MSG_HOSTNAME_VERIFYING);
			    }
			    
			    try 
			    {
			       getRunnableContext().run(true, true, this);
			    } 
			    catch (InterruptedException e)
			    {     
			    	// user canceled				
			    	ok = false;
			    	controlInError = textHostName;
			    }
			    catch (InvocationTargetException e)
			    {
				 	// error found
				    errorMessage = RSEUIPlugin.getPluginMessage(MSG_HOSTNAME_NOTFOUND);
				    errorMessage.makeSubstitution(currentHostName);
				    controlInError = textHostName;				
			    }			
			}
		}
		
		// if ok pressed, test for warning situation that connection name is in use in another profile...
		if (ok && (errorMessage==null) && okPressed)
		{
			String connectionName = textConnectionName.getText().trim();
			if (!connectionName.equals(defaultConnectionName))
			{
				ok = ValidatorConnectionName.validateNameNotInUse(connectionName, caller.getShell());
			}
			controlInError = textConnectionName;
		}

        // ...end of validation...
        
		if (!ok || (errorMessage != null))
		{
			ok = false;
			if (okPressed)
				controlInError.setFocus();
			showErrorMessage(errorMessage);		  
		}
		// DETERMINE DEFAULT USER ID AND WHERE IT IS TO BE SET...
		// Possibly prompt for where to set user Id...
		else 
		{
			boolean isLocal = false;
			if (textUserId != null)
			{
				isLocal = textUserId.isLocal();
				if (isLocal)
					userIdLocation = USERID_LOCATION_CONNECTION; // edit this connection's local value
				else 
					userIdLocation = USERID_LOCATION_DEFAULT_SYSTEMTYPE; // edit the preference value
			}
			else
				userIdLocation = USERID_LOCATION_NOTSET;
			SystemPreferencesManager.getPreferencesManager().setVerifyConnection(verifyHostNameCB.getSelection());
		}
		
		return ok;
	}    
	
	/**
	 * Return the runnable context from the hosting dialog or wizard, if
	 * applicable
	 */
	protected IRunnableContext getRunnableContext()
	{
		if (callerInstanceOfWizardPage)
		{
			return ((WizardPage)caller).getWizard().getContainer();
		}
		else if (callerInstanceOfSystemPromptDialog)
		{
			return ((SystemPromptDialog)caller);
		}
		else
		{
			return new ProgressMonitorDialog(caller.getShell());
		}
	}

	/**
	 * Check if this system type is enabled for offline support
	 * 
	 * @since RSE 6.0 
	 */
	private boolean enableOfflineCB()
	{
		// disabled offline checkbox for new connections
		if (!updateMode)
		{
			return false;
		}
		
		IRSESystemType sysType = RSECorePlugin.getDefault().getRegistry().getSystemType(defaultSystemType);
		RSESystemTypeAdapter sysTypeAdapter = (RSESystemTypeAdapter)(sysType.getAdapter(IRSESystemType.class));
		return sysTypeAdapter.isEnableOffline(sysType);
	}
	
	/**
	 * Return user-entered System Type.
	 * Call this after finish ends successfully.
	 */
	public String getSystemType()
	{
		if (textSystemType != null)
		  return textSystemType.getText().trim();
		else
		  return defaultSystemType;
	}    
	/**
	 * Return user-entered Connection Name.
	 * Call this after finish ends successfully.
	 */
	public String getConnectionName()
	{
		return textConnectionName.getText().trim();
	}    
	/**
	 * Return user-entered Host Name.
	 * Call this after finish ends successfully.
	 */
	public String getHostName()
	{
		return textHostName.getText().trim();
	}
	/**
	 * Return user-entered Default User Id.
	 * Call this after finish ends successfully.
	 */	    
	public String getDefaultUserId()
	{
		if (textUserId != null)
		  return textUserId.getText().trim();
		else
		  return "";
	}
	
	/**
	 * Return the user-entered value for work offline.
	 * Call this after finish ends successfully.
	 */
	public boolean isWorkOffline()
	{
		if (workOfflineCB != null)
		{
			return workOfflineCB.getSelection();
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Return user-entered Description.
	 * Call this after finish ends successfully.
	 */	    
	public String getConnectionDescription()
	{
		return textDescription.getText().trim();
	}    
	/**
	 * Return user-selected profile to contain this connection
	 * Call this after finish ends successfully, and only in update mode.
	 */	    
	public String getProfileName()
	{
		return (labelProfileValue!=null)?labelProfileValue.getText():profileCombo.getText();
	}    

    /**
     * If a default userId was specified, the user may have been queried
     *  where to put the userId. This returns one of the constants from
     *  ISystemUserIdConstants
     */
    public int getUserIdLocation()
    {
    	if ((textUserId != null) && (textUserId.getText().trim().length()>0))
    	  return userIdLocation;
    	else
    	  return USERID_LOCATION_NOTSET;
    }
    
    // --------------------
    // INTERNAL METHODS...
    // --------------------

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 * @param parent The parent composite
	 * @param updateMode true if we are in update mode versus create mode.
	 */
	public Control createContents(Composite parent, boolean updateMode, String parentHelpId)
	{	    
		contentsCreated = true;
		Label labelSystemType = null;
		String temp = null;

		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		SystemWidgetHelpers.setCompositeHelp(composite_prompts, parentHelpId);

		// Type display
		if (updateMode)
		{
		  temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_TYPE_LABEL);
		  labelType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
		  labelTypeValue = SystemWidgetHelpers.createLabel(
			composite_prompts, SystemResources.RESID_CONNECTION_TYPE_VALUE);
		}

        // PROFILE SELECTION
        if (updateMode)
        {
  		  temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_PROFILE_LABEL);
		  labelProfile = SystemWidgetHelpers.createLabel(composite_prompts, temp);
   		  labelProfile.setToolTipText(SystemResources.RESID_CONNECTION_PROFILE_READONLY_TIP);
   		  labelProfileValue = SystemWidgetHelpers.createLabel(composite_prompts, "");
   		  labelProfileValue.setToolTipText(SystemResources.RESID_CONNECTION_PROFILE_READONLY_TIP);
        }
        else //if (!updateMode)
        {
    	  temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_PROFILE_LABEL);
		  labelProfile = SystemWidgetHelpers.createLabel(composite_prompts, temp);
   		  labelProfile.setToolTipText(SystemResources.RESID_CONNECTION_PROFILE_TIP);			
	      if (!updateMode)
	      {
		    profileCombo  = SystemWidgetHelpers.createReadonlyCombo(
		      composite_prompts,null,SystemResources.RESID_CONNECTION_PROFILE_TIP);		
		    SystemWidgetHelpers.setHelp(profileCombo, RSEUIPlugin.HELPPREFIX + "ccon0001");     
	      }
        }
        
	    if (!updateMode)
	      SystemWidgetHelpers.createLabel(composite_prompts, " ", 2); // filler

		// SYSTEMTYPE PROMPT IN UPDATE MODE OR RESTRICTED MODE
	    if (updateMode || ((restrictSystemTypesTo != null) && (restrictSystemTypesTo.length==1)) )
	    {
	    	if (updateMode)
	    	{
	    	 temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL);
		     labelSystemType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
             labelSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_READONLY_TIP);
		     textSystemTypeReadOnly = SystemWidgetHelpers.createLabel(composite_prompts,"");	    
		     textSystemTypeReadOnly.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_READONLY_TIP);
	    	}
	    	else
	    	  singleTypeMode = true;
	    }
	    
	    if (updateMode)
	      SystemWidgetHelpers.createLabel(composite_prompts, " ", nbrColumns); // filler

		// CONNECTION NAME PROMPT
	    temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_CONNECTIONNAME_LABEL);
		labelConnectionName = SystemWidgetHelpers.createLabel(composite_prompts, temp);	    
	    labelConnectionName.setToolTipText(SystemResources.RESID_CONNECTION_CONNECTIONNAME_TIP);
		textConnectionName  = SystemWidgetHelpers.createTextField(
			composite_prompts,null,SystemResources.RESID_CONNECTION_CONNECTIONNAME_TIP);			
	    SystemWidgetHelpers.setHelp(textConnectionName, RSEUIPlugin.HELPPREFIX + "ccon0002"); 

		// SYSTEMTYPE PROMPT IN CREATE MODE
	    //if (!updateMode)
	    if ((labelSystemType == null) && !singleTypeMode)
	    {	    	
	      temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL);
		  labelSystemType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
          labelSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
		  textSystemType = SystemWidgetHelpers.createSystemTypeCombo(composite_prompts,null,restrictSystemTypesTo);		
          textSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
	      SystemWidgetHelpers.setHelp(textSystemType, RSEUIPlugin.HELPPREFIX + "ccon0003"); 
	    }

		// HOSTNAME PROMPT
	    temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_HOSTNAME_LABEL);
		labelHostName = SystemWidgetHelpers.createLabel(composite_prompts, temp);
        labelHostName.setToolTipText(SystemResources.RESID_CONNECTION_HOSTNAME_TIP);
        
        if (!updateMode && (defaultSystemType==null))
        {
		  defaultSystemType = RemoteSystemsPreferencePage.getSystemTypePreference();
		  if ((defaultSystemType == null) || (defaultSystemType.length() == 0))
		    defaultSystemType = lastSystemType;
		  if ((defaultSystemType == null) || (defaultSystemType.length() == 0))
            defaultSystemType = textSystemType.getItem(0); 	
        }
        
		textHostName = SystemWidgetHelpers.createHostNameCombo(composite_prompts,null,defaultSystemType);
        textHostName.setToolTipText(SystemResources.RESID_CONNECTION_HOSTNAME_TIP);
	    SystemWidgetHelpers.setHelp(textHostName, RSEUIPlugin.HELPPREFIX + "ccon0004");     
		
		// USERID PROMPT
		/* We are testing the usability of not prompting for the user ID, so that the
		 *  user has less to think about when creating a new connection. Phil.
		 */
		if (updateMode) // added for this experiment
		{
			temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_DEFAULTUSERID_LABEL);
		   labelUserId = SystemWidgetHelpers.createLabel(composite_prompts, temp);
   		   labelUserId.setToolTipText(SystemResources.RESID_CONNECTION_DEFAULTUSERID_TIP);
           textUserId = SystemWidgetHelpers.createInheritableTextField(
               composite_prompts,SystemResources.RESID_CONNECTION_DEFAULTUSERID_INHERITBUTTON_TIP,SystemResources.RESID_CONNECTION_DEFAULTUSERID_TIP);
	       SystemWidgetHelpers.setHelp(textUserId, RSEUIPlugin.HELPPREFIX + "ccon0005");     
		}

		// DESCRIPTION PROMPT
		temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_DESCRIPTION_LABEL);
		labelDescription = SystemWidgetHelpers.createLabel(composite_prompts, temp);
        labelDescription.setToolTipText(SystemResources.RESID_CONNECTION_DESCRIPTION_TIP);
		textDescription  = SystemWidgetHelpers.createTextField(
		   composite_prompts,null,SystemResources.RESID_CONNECTION_DESCRIPTION_TIP);		   
	    SystemWidgetHelpers.setHelp(textDescription, RSEUIPlugin.HELPPREFIX + "ccon0006");     

		// VERIFY HOST NAME CHECKBOX
	    SystemWidgetHelpers.createLabel(composite_prompts, " ", nbrColumns); // filler
	    verifyHostNameCB = SystemWidgetHelpers.createCheckBox(composite_prompts, nbrColumns, null, SystemResources.RESID_CONNECTION_VERIFYHOSTNAME_LABEL, SystemResources.RESID_CONNECTION_VERIFYHOSTNAME_TOOLTIP);
	    if (updateMode)
	    	verifyHostNameCB.setSelection(false);
	    else
	    	verifyHostNameCB.setSelection(SystemPreferencesManager.getPreferencesManager().getVerifyConnection());
	     
	    // yantzi: artemis 6.0, work offline
	    if (enableOfflineCB())
	    {
		    workOfflineCB = SystemWidgetHelpers.createCheckBox(composite_prompts, nbrColumns, null, SystemResources.RESID_OFFLINE_WORKOFFLINE_LABEL, SystemResources.RESID_OFFLINE_WORKOFFLINE_TOOLTIP);
			SystemWidgetHelpers.setHelp(workOfflineCB, RSEUIPlugin.HELPPREFIX + "wofp0000");
	    }     

	    if (!initDone)	
	      doInitializeFields();		  
	      
        connectionNameEmpty = (textConnectionName.getText().trim().length() == 0); // d43191

		textConnectionName.setFocus();
		  		  
		
		// add keystroke listeners...
		textConnectionName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateConnectionNameInput(true);
				}
			}
		);
		textHostName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateHostNameInput();
				}
			}
		);		
		if (textUserId!=null)
		  textUserId.addModifyListener(
		  	new ModifyListener() 
		  	{
				public void modifyText(ModifyEvent e) 
				{
					validateUserIdInput();
				}
			});
 
        if (profileCombo != null) {
            profileCombo.addSelectionListener(this);
        }
        
        if (textSystemType != null) {
            originalHostName = textHostName.getText();
            textSystemType.addSelectionListener(this);
        }
        
        if (verifyHostNameCB != null) {
            verifyHostNameCB.addSelectionListener(this);
        }

        if ((textSystemType!=null) && (textSystemType.getText()!=null))
          caller.systemTypeSelected(textSystemType.getText(), true);
        else if ((restrictSystemTypesTo!=null) && (restrictSystemTypesTo.length==1))
          caller.systemTypeSelected(restrictSystemTypesTo[0], true);
              
        if (textUserId == null)
          userIdLocation = USERID_LOCATION_NOTSET;
                               
		return composite_prompts; // composite;
	}
	
	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		 if (updateMode || !singleTypeMode)
		    return textConnectionName;
		 else
		    return textHostName; // create mode and restricted to one system type
	}
	
	/**
	 * Default implementation to satisfy Listener interface. Does nothing.
	 */
	public void handleEvent(Event evt) {}
	
	/**
	 * Combo selection listener method
	 */
	public void widgetDefaultSelected(SelectionEvent event)
	{
	}
	/**
	 * Combo selection listener method
	 */
	public void widgetSelected(SelectionEvent event)
	{
		Object src = event.getSource();
		
		if (src == profileCombo)
		{
            profileCombo.getDisplay().asyncExec(this);
		}
		else if (src == textSystemType) // can only happen in create mode
		{
			String currHostName = textHostName.getText().trim();
			boolean hostNameChanged = !currHostName.equals(originalHostName);
			String currSystemType = textSystemType.getText().trim();
		    textHostName.setItems(RSEUIPlugin.getTheSystemRegistry().getHostNames(currSystemType));
		    if (hostNameChanged)
		    {
		      textHostName.setText(currHostName);
		    }
		    else if (textHostName.getItemCount()>0)
		    {
		      textHostName.setText(textHostName.getItem(0));
		      originalHostName = textHostName.getText();
		    }
		    else
		    {
		      String connName = textConnectionName.getText().trim();
		      if (connName.indexOf(' ') == -1)
		        textHostName.setText(connName);
		      else
		        textHostName.setText("");
		      originalHostName = textHostName.getText();		      
		    }

		    initializeUserIdField(currSystemType, null);

		    verify(false); // re-check all fields in top-down order
		      
		    caller.systemTypeSelected(currSystemType, false);
		}
		// if verify host name checkbox event
		else if (src == verifyHostNameCB) {
		    
			userPickedVerifyHostnameCB = true;
		    // if the check box was unselected
		    if (!verifyHostNameCB.getSelection()) {
		    	
		        // clear host name not valid or not found error message so that wizard next page is enabled
		        if (errorMessage != null && errorMessage == RSEUIPlugin.getPluginMessage(MSG_HOSTNAME_NOTFOUND)) {
		            errorMessage = null;
		            
		    		if (msgLine != null) {
		    			msgLine.clearErrorMessage();
		    		}
		    		
		    		// set this page to complete so we can go to next page
		    		setPageComplete();
		    		
		    		// now go through each page and clear error message if there is one
		    		if (callerInstanceOfWizardPage) {
		    		    IWizard wizard = ((WizardPage)caller).getWizard();
		    		    
		    		    IWizardPage[] pages = null;
		    		    
		    		    if (wizard instanceof SystemNewConnectionWizard) {
		    		        SystemNewConnectionWizard connWizard = (SystemNewConnectionWizard)wizard;
		    		        AbstractSystemWizardPage mainPage = (AbstractSystemWizardPage)(connWizard.getMainPage());
		    		        
		    		        Vector pageList = new Vector();
		    		        
		    		        IWizardPage page = mainPage;
		    		        
		    		        while (page != null) {
		    		            
		    		            if (page != mainPage) {
		    		                pageList.add(page);
		    		            }
		    		            
	    		                page = connWizard.getNextPage(page);
		    		        }
		    		        
		    		        pages = new IWizardPage[pageList.size()];
		    		        
		    		        for (int j = 0; j < pageList.size(); j++) {
		    		            pages[j] = (IWizardPage)(pageList.get(j));
		    		        }
		    		    }
		    		    else {
			    		     pages = wizard.getPages();
		    		    }
		    		    
		    		    for (int i = 0; i < pages.length; i++) {
		    		    
		    		        IWizardPage page = pages[i];
		    		        
		    		        if (page instanceof AbstractSystemWizardPage) {
		    		            ((AbstractSystemWizardPage)page).clearErrorMessage();
		    		        }
		    		        else if (page instanceof WizardPage) {
		    		            ((WizardPage)page).setErrorMessage(null);
		    		        }
		    		    }
		    		}
		        }
		    }
		}
	}
	
	/**
	 * Initialize values of input fields based on input
	 */
	private void doInitializeFields()
	{
		// -------
		// profile
		// -------
		// ...output-only:
		if (labelProfileValue != null)
		{
		  if (defaultProfile != null)
		    labelProfileValue.setText(defaultProfile);
		}
		// ...selectable:
		else
		{
		  if (defaultProfileNames != null)
		    profileCombo.setItems(defaultProfileNames);

		  if (defaultProfile != null)
		  {
	        int selIdx = profileCombo.indexOf(defaultProfile);
	        if (selIdx >= 0)
		      profileCombo.select(selIdx);			
		    else
		      profileCombo.select(0);
		  }
		}

        // -----------
		// system type
        // -----------
        // ...output-only:
        if ((textSystemTypeReadOnly != null) || singleTypeMode)
        {
        	if (restrictSystemTypesTo != null)
        	{
        		if (textSystemTypeReadOnly != null)
        	       textSystemTypeReadOnly.setText(restrictSystemTypesTo[0]);        	  
        	    if (defaultSystemType == null)
        	       defaultSystemType = restrictSystemTypesTo[0];
        	}
        	else if (defaultSystemType != null)
        	{
        		if (textSystemTypeReadOnly != null)
        	      textSystemTypeReadOnly.setText(defaultSystemType);
        	}
        }
        // ...selectable:
        else
        {
        	if (defaultSystemType == null)
        	{
		        defaultSystemType = RemoteSystemsPreferencePage.getSystemTypePreference();
		        if ((defaultSystemType == null) || (defaultSystemType.length()==0))
		          defaultSystemType = lastSystemType;
        	}
        	if (defaultSystemType != null)
        	{
	          int selIdx = textSystemType.indexOf(defaultSystemType);
	          if (selIdx >= 0)
		        textSystemType.select(selIdx);	
        	}
		    else
		    {
		        textSystemType.select(0);
		  	    defaultSystemType = textSystemType.getText();
		    }
        }
        
        // ---------------		
		// connection name
        // ---------------
		if (defaultConnectionName != null)
		  textConnectionName.setText(defaultConnectionName);
		textConnectionName.setTextLimit(connectionNameLength);					

        // -----------		
		// host name
        // -----------
		if (defaultHostName != null)
		{
		    textHostName.setText(defaultHostName);
		}
		else if (textHostName.getItemCount() > 0)
		{
			textHostName.select(0);
		}
		textHostName.setTextLimit(hostNameLength);
		textHostName.clearSelection(); // should unselect the text, but it does not!

        // ---------------		
		// default user id
        // ---------------
        initializeUserIdField(defaultSystemType, defaultUserId);
		if (textUserId!=null)
		  textUserId.setTextLimit(userIdLength);		
		// description
		if (defaultDescription != null)
		  textDescription.setText(defaultDescription);           		
		textDescription.setTextLimit(descriptionLength);		
		
		// ---------------		
		// Work offline
		// ---------------
		if (workOfflineCB != null)
		{
			workOfflineCB.setSelection(defaultWorkOffline);
		}
		
		initDone = true;
	}
	
	/**
	 * Initialize userId values.
	 * We have to reset after user changes the system type
	 */
	private void initializeUserIdField(String systemType, String currentUserId)
	{
        // ---------------		
		// default user id
        // ---------------
        String parentUserId = SystemPreferencesManager.getPreferencesManager().getDefaultUserId(systemType);
		if (textUserId!=null)
		{        
		  textUserId.setInheritedText(parentUserId);
		  boolean allowEditingOfInherited = ((parentUserId == null) || (parentUserId.length()==0));
		  textUserId.setAllowEditingOfInheritedText(allowEditingOfInherited); // if not set yet, let user set it!
		}
        // ----------------------------
		// default user id: update-mode
        // ----------------------------
		if ((currentUserId != null) && (currentUserId.length()>0))
		{
			if (textUserId != null)
			{
		      textUserId.setLocalText(currentUserId);
		      textUserId.setLocal(true);
			}
		}
        // ----------------------------
		// default user id: create-mode
        // ----------------------------
		else 
		{
			if (textUserId != null)			
		       textUserId.setLocalText("");
		    if ((parentUserId != null) && (parentUserId.length() > 0))
		    {
		  	   userIdFromSystemTypeDefault = true;		   
		  	   defaultUserId = parentUserId;
			   if (textUserId != null)			
		  	     textUserId.setLocal(false);
		    }
		    // there is no local override, and no inherited value. Default to setting inherited value.
		    else
		    {
			   if (textUserId != null)			
		         textUserId.setLocal(false);
		    }
		}		
	}
	       
	// ------------------------------------------------------
	// INTERNAL METHODS FOR VERIFYING INPUT PER KEYSTROKE ...
	// ------------------------------------------------------
		
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setConnectionNameValidators(ISystemValidator[])
	 */
	protected SystemMessage validateConnectionNameInput(boolean userTyped) 
	{			
		if (!connectionNameListen)
			return null;
	    errorMessage= null;
	    int selectedProfile = 0;
	    if (profileCombo != null)
	    {
	    	selectedProfile = profileCombo.getSelectionIndex();
	    }
	    if (selectedProfile < 0)
	    	selectedProfile = 0;
	    ISystemValidator nameValidator = null;
	    if ((nameValidators!=null) && (nameValidators.length>0))
	    	nameValidator = nameValidators[selectedProfile];
        String connName = textConnectionName.getText().trim();
		if (nameValidator != null)
		{
			errorMessage = nameValidator.validate(connName);
		}
		showErrorMessage(errorMessage);
		setPageComplete();	
		if (userTyped)	
			connectionNameEmpty = (connName.length()==0); // d43191
		return errorMessage;		
	}
	/**
	 * Set the connection name internally without validation
	 */
	protected void internalSetConnectionName(String name)
	{
		SystemMessage currErrorMessage = errorMessage;
	    connectionNameListen = false;
	    textConnectionName.setText(name);
	    connectionNameListen = true;
	    errorMessage = currErrorMessage;
	}
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setHostNameValidator(ISystemValidator)
	 */
	protected SystemMessage validateHostNameInput() 
	{			
	    String hostName = textHostName.getText().trim();
	    if (connectionNameEmpty) // d43191
	      internalSetConnectionName(hostName);	    
	    errorMessage= null;
		if (hostValidator != null)
	      errorMessage= hostValidator.validate(hostName);
	    else if (getHostName().length() == 0)
		  errorMessage = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_HOSTNAME_EMPTY);    	
		if (updateMode && !userPickedVerifyHostnameCB)
		{
			boolean hostNameChanged = !hostName.equals(defaultHostName);
			verifyHostNameCB.setSelection(hostNameChanged);
		}
		showErrorMessage(errorMessage);
		setPageComplete();		
		return errorMessage;		
	}
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setUserIdValidator(ISystemValidator)	
	 */	
	protected SystemMessage validateUserIdInput() 
	{			
	    errorMessage= null;
	    if (textUserId != null)			
	    {
		  if (userIdValidator != null)
	        errorMessage= userIdValidator.validate(textUserId.getText());
	      else if (getDefaultUserId().length()==0)    	
		     errorMessage = RSEUIPlugin.getPluginMessage(MSG_VALIDATE_USERID_EMPTY);    	
	    }
		showErrorMessage(errorMessage);		
		setPageComplete();
		return errorMessage;		
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		boolean complete = isPageComplete();
		if (complete && (textSystemType!=null))
		  lastSystemType = textSystemType.getText().trim();
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
     * Display error message or clear error message
     */
	private void showErrorMessage(SystemMessage msg)
	{
		if (msgLine != null)
		  if (msg != null)
		    msgLine.setErrorMessage(msg);
		  else
		    msgLine.clearErrorMessage();
		else
		  SystemBasePlugin.logDebugMessage(this.getClass().getName(), "MSGLINE NULL. TRYING TO WRITE MSG " + msg);
	}
    
    // ---------------------------------------------------------------
	// STATIC METHODS FOR GETTING A CONNECTION NAME VALIDATOR...
	// ---------------------------------------------------------------
	
	/**
	 * Reusable method to return a name validator for renaming a connection.
	 * @param the current connection object on updates. Can be null for new names. Used
	 *  to remove from the existing name list the current connection.
	 */
	public static ISystemValidator getConnectionNameValidator(IHost conn)
	{
		ISystemProfile profile = conn.getSystemProfile();
    	Vector v = RSEUIPlugin.getTheSystemRegistry().getHostAliasNames(profile);
    	if (conn != null) // hmm, line 1 of this method will crash if this is the case!
    	  v.removeElement(conn.getAliasName());
	    ValidatorConnectionName connNameValidator = new ValidatorConnectionName(v);		
	    return connNameValidator;
	}	
	/**
	 * Reusable method to return a name validator for renaming a connection.
	 * @param the current connection object's profile from which to get the existing names. 
	 *  Can be null for syntax checking only, versus name-in-use.
	 */
	public static ISystemValidator getConnectionNameValidator(ISystemProfile profile)
	{
    	Vector v = RSEUIPlugin.getTheSystemRegistry().getHostAliasNames(profile);
	    ValidatorConnectionName connNameValidator = new ValidatorConnectionName(v);		
	    return connNameValidator;
	}	

	/**
	 * Reusable method to return name validators for creating a connection.
	 * There is one validator per active system profile.
	 */
	public static ISystemValidator[] getConnectionNameValidators()
	{
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISystemProfile[] profiles = sr.getActiveSystemProfiles();
		ISystemValidator[] connNameValidators = new ISystemValidator[profiles.length];
		for (int idx=0; idx<profiles.length; idx++)
		{
		   Vector v = sr.getHostAliasNames(profiles[idx]);
		   connNameValidators[idx] = new ValidatorConnectionName(v);				   
		}
	    return connNameValidators;
	}		
	
	// -------------------------------------------------------
	// METHOD REQUIRED BY RUNNABLE, USED IN CALL TO ASYNCEXEC
	// -------------------------------------------------------
	
	public void run()
	{
	    verify(false);	
	}

	/**
	 * METHOD REQUIRED BY IRunnableWithProgress, USED TO SHOW PROGRESS WHILE
	 * VERIFYING HOSTNAME
	 */
	public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException
	{
           pm.beginTask(verifyingHostName.getLevelOneText(),IProgressMonitor.UNKNOWN);
	       try
	       {	       	    
		  	    InetAddress address = InetAddress.getByName(currentHostName);
	       } 
	       catch (java.net.UnknownHostException exc)
	       {
	         	pm.done();
	         	throw new InvocationTargetException(exc);
	       }
	       pm.done();
	}	
}