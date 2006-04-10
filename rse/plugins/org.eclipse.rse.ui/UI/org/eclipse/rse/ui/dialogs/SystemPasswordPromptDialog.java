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

package org.eclipse.rse.ui.dialogs;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



/**
 * Prompt user for password.
 * This class is final due to the sensitive nature of the information being prompted for.
 */
public final class SystemPasswordPromptDialog
       extends SystemPromptDialog 
       implements  ISystemMessages, ISystemPasswordPromptDialog
{
	
	// lables are not as big as text fields so we need to set the height for the system type 
	// and hostname labels so they are equally spaced with the user ID and password entry fields
	private static final int LABEL_HEIGHT = 17;
	
    protected Text textPassword;

	// yantzi:  artemis 6.0, at request of zOS team I am changing the system type and hostname 
	// to labels so they are clearer to read then non-editable entry fields    
    //protected Text textSystemType, textHostName, textUserId;
	protected Text textUserId;
    
    protected Button userIdPermanentCB, savePasswordCB;
    //protected String userId,password;
    protected String originalUserId;
    protected String userId, password;
    protected boolean userIdPermanent = false;
    protected boolean savePassword = false;
    protected boolean forceToUpperCase;
    protected boolean userIdChanged = false;
    protected boolean userIdOK = true;
    protected boolean passwordOK = false;
    protected boolean noValidate = false;
	protected ISystemValidator userIdValidator, passwordValidator;    
	protected ISignonValidator signonValidator;
	protected SystemMessage errorMessage = null;
    
	/**
	 * Constructor for SystemPasswordPromptDialog
	 */
	public SystemPasswordPromptDialog(Shell shell)
	{
		super(shell, SystemResources.RESID_PASSWORD_TITLE);
		//pack();
		setHelp(SystemPlugin.HELPPREFIX+"pwdp0000");		
	}
	/**
	 * Set the input System object in which the user is attempting to do a connect action.
	 * This is used to query the system type, host name and userId to display to the user for
	 * contextual information.
	 * <p>
	 * This must be called right after instantiating this dialog.
	 */
	public void setSystemInput(IConnectorService systemObject)
	{
		setInputObject(systemObject);
	}
	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v)
	{
		userIdValidator = v;
	}
	/**
	 * Call this to specify a validator for the password. It will be called per keystroke.
	 */
	public void setPasswordValidator(ISystemValidator v)
	{
		passwordValidator = v;
	}
	/**
	 * Call this to specify a validator for the signon.  It will be called when the user presses OK.
	 */
	public void setSignonValidator(ISignonValidator v)
	{
		signonValidator = v;
	}
	/**
	 * Call this to force the userId and password to uppercase
	 */
	public void setForceToUpperCase(boolean force)
	{
		this.forceToUpperCase = force;
	}
	/**
	 * Call this to query the force-to-uppercase setting
	 */
	public boolean getForceToUpperCase()
	{
		return forceToUpperCase;
	}
	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		okButton.setEnabled(false);
		
		
		if (textUserId.getText().length()==0)
		  return textUserId;
		else
		{
		    if (password != null)
			{
			    validatePasswordInput();
			    textPassword.selectAll();
			}
		    return textPassword;
		}
	}


	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
  	    // top level composite  	    
		Composite composite = new Composite(parent,SWT.NONE);        
		composite.setLayout(new GridLayout());
	    composite.setLayoutData(new GridData(
		   GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(
			composite, 2);				

		IConnectorService systemObject = (IConnectorService)getInputObject();

		// System type
		//textSystemType = SystemWidgetHelpers.createLabeledReadonlyTextField(
		//	                     composite_prompts,rb,RESID_CONNECTION_SYSTEMTYPE_READONLY_ROOT);
		String text = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_READONLY_LABEL);
		Label label = SystemWidgetHelpers.createLabel(composite_prompts, text);
		GridData gd = new GridData();
		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);	

		label = SystemWidgetHelpers.createLabel(composite_prompts, systemObject.getHostType());
		gd = new GridData();
		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);		

		// Host name
		//textHostName = SystemWidgetHelpers.createLabeledReadonlyTextField(
		//	composite_prompts, rb, ISystemConstants.RESID_CONNECTION_HOSTNAME_READONLY_ROOT);
		text = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_HOSTNAME_READONLY_LABEL);
		label = SystemWidgetHelpers.createLabel(composite_prompts, text);
		gd = new GridData();
		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);		
		label = SystemWidgetHelpers.createLabel(composite_prompts, systemObject.getHostName());
		gd = new GridData();
		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);		
		
		// UserId
		textUserId = SystemWidgetHelpers.createLabeledTextField(
			composite_prompts,this,SystemResources.RESID_CONNECTION_USERID_LABEL, SystemResources.RESID_CONNECTION_USERID_TIP);
				    	    
		// Password prompt    
		textPassword = SystemWidgetHelpers.createLabeledTextField(
			composite_prompts,this,SystemResources.RESID_PASSWORD_LABEL, SystemResources.RESID_PASSWORD_TIP);
		textPassword.setEchoChar('*');

		// UserId_make_permanent checkbox
		// DY:  align user ID checkbox with entry fields
		// yantzi:5.1 move checkboxes to be below entry fields
		SystemWidgetHelpers.createLabel(composite_prompts, "");
		userIdPermanentCB = SystemWidgetHelpers.createCheckBox(
			 composite_prompts, 1, this, SystemResources.RESID_PASSWORD_USERID_ISPERMANENT_LABEL, SystemResources.RESID_PASSWORD_USERID_ISPERMANENT_TIP );
		userIdPermanentCB.setEnabled(false);

	    // Save signon information checkbox
	    // DY:  align password checkbox with entry fields
	    SystemWidgetHelpers.createLabel(composite_prompts, "");
	    savePasswordCB = SystemWidgetHelpers.createCheckBox(
	    	composite_prompts, 1, this, SystemResources.RESID_PASSWORD_SAVE_LABEL, SystemResources.RESID_PASSWORD_SAVE_TOOLTIP);
	    savePasswordCB.setSelection(savePassword);
    	// disable until the user enters something for consistency with the save user ID checkbox
    	savePasswordCB.setEnabled(false);
	    			
		initializeInput();	

		// add keystroke listeners...
		textUserId.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateUserIdInput();
				}
			}
		);
		textPassword.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validatePasswordInput();
				}
			}
		);
			
		
		//SystemWidgetHelpers.setHelp(composite, SystemPlugin.HELPPREFIX+"pwdp0000");
        return composite;		
	}

// yantzi: artemis 6.0 not required, the Window class handles ESC processing	
//	/**
//	 * @see SystemPromptDialog#createContents(Composite)
//	 */
//	protected Control createContents(Composite parent) 
//	{
//		//System.out.println("INSIDE CREATECONTENTS");
//		Control c = super.createContents(parent);
//		// Listen for ESC keypress, simulate the user pressing 
//		// the cancel button
//		
//		KeyListener keyListener = new KeyAdapter() {
//				public void keyPressed(KeyEvent e) {
//					if (e.character == SWT.ESC) {
//						buttonPressed(CANCEL_ID);
//					}
//				}
//			};
//			
//		textUserId.addKeyListener(keyListener);
//		textPassword.addKeyListener(keyListener);
//		userIdPermanentCB.addKeyListener(keyListener);
//		okButton.addKeyListener(keyListener);
//		cancelButton.addKeyListener(keyListener);
//			
//		return c;
//	}

	
	/**
	 * Init values using input data
	 */
	protected void initializeInput()
	{
		IConnectorService systemObject = (IConnectorService)getInputObject();
		//textSystemType.setText(systemObject.getSystemType());
		//textHostName.setText(systemObject.getHostName());		
		originalUserId = systemObject.getUserId();
		if ((originalUserId != null) && (originalUserId.length()>0))
		{
		  //textUserId.setEditable(false);
		  //textUserId.setEnabled(false);							    		     
		  textUserId.setText(originalUserId);		  
		}
		else
		{
			// added by phil: if we don't prompt for userId at new connection time,
			//  then we should default here to the preferences setting for the user id,
			//  by SystemType...
			String preferencesUserId = SystemPreferencesManager.getPreferencesManager().getDefaultUserId(systemObject.getHostType());
			if (preferencesUserId != null)
			  textUserId.setText(preferencesUserId);
		    originalUserId = "";
		}
		
		if (password != null)
		{
		    textPassword.setText(password);
		}
		
	}
    /**
     * Return the userId entered by user
     */
    private String internalGetUserId()
    {
        userId = textUserId.getText().trim();	
        return userId;
    }

    /**
     * Return the password entered by user
     */
    private String internalGetPassword()
    {
        password = textPassword.getText().trim();	
        return password;
    }
    /**
     * Return true if the user elected to make the changed user Id a permanent change.
     */
    private boolean internalGetIsUserIdChangePermanent()
    {
        userIdPermanent = userIdPermanentCB.getSelection();
        return userIdPermanent;
    }
    /**
     * Return true if the user elected to save the password
     */
    private boolean internalGetIsSavePassword()
    {
        savePassword = savePasswordCB.getSelection();
        return savePassword;
    }


  	/**
	 * This hook method is called whenever the text changes in the user Id input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setUserIdValidator(ISystemValidator)	
	 */	
	protected SystemMessage validateUserIdInput() 
	{			
		if (noValidate)
		  return null;
		clearErrorMessage();
	    errorMessage= null;
	    String userId = internalGetUserId();
	    userIdChanged = !userId.equals(originalUserId);
        userIdPermanentCB.setEnabled(userIdChanged);
		if (userIdValidator != null)
	      errorMessage= userIdValidator.validate(userId);
		else if (userId.equals(""))
		  errorMessage = SystemPlugin.getPluginMessage(MSG_VALIDATE_USERID_EMPTY);    	
		userIdOK = (errorMessage == null);
	    if (!userIdOK)
	    {
	      okButton.setEnabled(false);
		  setErrorMessage(errorMessage);		
	    }
	    else
	      okButton.setEnabled(passwordOK);
		return errorMessage;		
	}

  	/**
	 * This hook method is called whenever the text changes in the password input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setPasswordValidator(ISystemValidator)	
	 */	
	protected SystemMessage validatePasswordInput() 
	{	
		// yantzi: artemis 6.0, disable save checkbox when blank
		savePasswordCB.setEnabled(!internalGetPassword().equals(""));
				
		if (noValidate)
		  return null;
		clearErrorMessage();
	    errorMessage= null;
	    String password = internalGetPassword();
		if (passwordValidator != null)
	      errorMessage= passwordValidator.validate(password);
		else if (password.equals(""))
		  errorMessage = SystemPlugin.getPluginMessage(MSG_VALIDATE_PASSWORD_EMPTY);	
		passwordOK = (errorMessage == null);
	    if (!passwordOK)
	    {
		  setErrorMessage(errorMessage);		
		  okButton.setEnabled(false);
	    }
	    else
	      okButton.setEnabled(userIdOK);
		return errorMessage;		
	}
    
    /**
     * Return the userId entered by user
     */
    public String getUserId()
    {
        return userId;	
    }

    /**
     * Return the password entered by user
     */
    public String getPassword()
    {
        return password;	
    }
	
	/**
	 * Sets the password
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
    /**
     * Return true if the user changed the user id
     */
    public boolean getIsUserIdChanged()
    {
    	return userIdChanged;
    }
    /**
     * Return true if the user elected to make the changed user Id a permanent change.
     */
    public boolean getIsUserIdChangePermanent()
    {
        return userIdPermanent;
    }
    /**
     * Return true if the user elected to make the changed user Id a permanent change.
     */
    public boolean getIsSavePassword()
    {
        return savePassword;
    }
    /**
     * Preselect the save password checkbox.  Default value is to not 
     * select the save password checkbox.
     */
    public void setSavePassword(boolean save)
    {
    	savePassword = save;
    }
	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	protected boolean verify() 
	{
		SystemMessage errMsg = null;
		Control controlInError = null;
		clearErrorMessage();
		errorMessage = null;
		errMsg = validateUserIdInput();
		if (errMsg != null)
		  controlInError = textUserId;
		else
		{
		  errMsg = validatePasswordInput();
		  if (errMsg != null)
		    controlInError = textPassword;
		}		
		if (errMsg != null)
		  controlInError.setFocus(); // validate methods already displayed error message
		return (errMsg == null);
	}
    
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
 	    //busyCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT);
        //getShell().setCursor(busyCursor);
        setBusyCursor(true); // phil
    				
		password = internalGetPassword();
		userId = internalGetUserId();
		userIdPermanent = internalGetIsUserIdChangePermanent();
		savePassword = internalGetIsSavePassword();
        if (forceToUpperCase)
        {          
          userId = userId.toUpperCase();
          password = password.toUpperCase();
          noValidate = true;
          textUserId.setText(userId);
          textPassword.setText(password);
          noValidate = false;
        }
		
		boolean closeDialog = verify();

        //getShell().setCursor(null);
        //busyCursor.dispose();       
        setBusyCursor(false); // phil        

		// If all inputs are OK then verify signon
		if (closeDialog && (signonValidator != null)) 
		{
			SystemMessage msg = signonValidator.isValid(this, userId, password);
			if (msg != null) 
			{
				closeDialog = false;
				setErrorMessage(msg);			
			}
		}
     
		return closeDialog;
	}	    
}