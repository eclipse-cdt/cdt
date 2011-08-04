/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 *                     - created and used RSEPreferencesManager
 * Uwe Stieber (Wind River) - bugfixing and reworked new connection wizard
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * David Dykstal (IBM) - 180562: remove implementation of IRSEUserIdConstants
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * David McKnight     (IBM)      - [229610] [api] File transfers should use workspace text file encoding
 * David McKnight     (IBM)      - [238314] Default user ID on host properties page not disabled
 * David McKnight     (IBM)      - [353377] Connection name with ":" causes problems
 *******************************************************************************/

package org.eclipse.rse.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.dialogs.ISystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorConnectionName;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.validators.ValidatorUserId;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.rse.ui.wizards.newconnection.RSEAbstractNewConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.IDEEncoding;

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

public class SystemConnectionForm implements Listener, SelectionListener, Runnable, IRunnableWithProgress {

	public static final boolean CREATE_MODE = false;
	public static final boolean UPDATE_MODE = true;
	public static IRSESystemType lastSystemType = null;
	protected IHost conn;
	protected IRSESystemType defaultSystemType;
	protected IRSESystemType[] validSystemTypes;

	// GUI widgets
	protected Label labelType, labelConnectionName, labelHostName, labelUserId, labelDescription, labelProfile;
	protected Label labelTypeValue, labelSystemTypeValue, labelProfileValue;
	protected Combo textSystemType, textHostName, profileCombo;
	protected Text textConnectionName, textDescription;
	protected Button verifyHostNameCB;
	protected Group encodingGroup;
	protected Button remoteEncodingButton, otherEncodingButton;
	protected Combo otherEncodingCombo;

	// yantzi:artemis 6.0, work offline support
	protected Button workOfflineCB;

	protected InheritableEntryField textUserId;
	protected Label textSystemTypeReadOnly; // for update mode

	// validators
	protected ISystemValidator[] nameValidators;
	protected ISystemValidator hostValidator;
	protected ISystemValidator userIdValidator;
	private ISystemValidator fileNameValidator;

	// other inputs
	protected ISystemMessageLine msgLine;
	protected ISystemConnectionFormCaller caller;
	protected String defaultConnectionName, defaultHostName;
	protected String defaultUserId, defaultDescription, defaultProfile; // update mode initial values	                  
	protected String[] defaultProfileNames;
	protected boolean defaultWorkOffline;

	protected boolean userPickedVerifyHostnameCB = false;

	// max lengths
	protected int hostNameLength = 100;
	protected int connectionNameLength = ValidatorConnectionName.MAX_CONNECTIONNAME_LENGTH;
	protected int userIdLength = 100;
	protected int descriptionLength = 100;

	// state/output
	protected int userIdLocation = IRSEUserIdConstants.USERID_LOCATION_HOST;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog, callerInstanceOfPropertyPage;
	protected boolean userIdFromSystemTypeDefault;
	protected boolean updateMode = false;
	protected boolean contentsCreated = false;
	protected boolean connectionNameEmpty = false;
	protected boolean connectionNameListen = true;
	protected boolean singleTypeMode = false;
	protected String originalHostName = null;
	protected String currentHostName = null;
	protected SystemMessage errorMessage = null;
	protected SystemMessage verifyingHostName;
	
	// encoding fields
	protected boolean addEncodingFields = false;
    protected String defaultEncoding = null;
    protected boolean isRemoteEncoding = false;
    protected boolean isValidBefore = true;

	/**
	 * Constructor.
	 * @param msgLine A GUI widget capable of writing error messages to.
	 * @param caller The wizardpage or dialog hosting this form.
	 */
	public SystemConnectionForm(ISystemMessageLine msgLine, ISystemConnectionFormCaller caller) {
		this.msgLine = msgLine;
		this.caller = caller;
		this.defaultProfileNames = RSECorePlugin.getTheSystemRegistry().getSystemProfileManager().getActiveSystemProfileNames();
		callerInstanceOfWizardPage = caller instanceof IWizardPage;
		callerInstanceOfSystemPromptDialog = caller instanceof ISystemPromptDialog;
		callerInstanceOfPropertyPage = caller instanceof IWorkbenchPropertyPage;

		userIdValidator = new ValidatorUserId(true); // false => allow empty? Yes.
		defaultUserId = ""; //$NON-NLS-1$
		
		fileNameValidator = new ValidatorFileName();
	}

	// -------------------------------------------------------------
	// INPUT METHODS CALLABLE BY CALLER TO INITIALIZE INPUT OR STATE
	// -------------------------------------------------------------

	/**
	 * Often the message line is null at the time of instantiation, so we have to call this after
	 *  it is created.
	 */
	public void setMessageLine(ISystemMessageLine msgLine) {
		this.msgLine = msgLine;
	}

	/**
	 * Call this to specify a validator for the connection name. It will be called per keystroke.
	 * You must supply one per active profile, as connections must be unique per profile.
	 * The order must be the same as the order of profiles given by getActiveSystemProfiles() in
	 * the system registry.
	 */
	public void setConnectionNameValidators(ISystemValidator[] v) {
		nameValidators = v;
	}

	/**
	 * Call this to specify a validator for the hostname. It will be called per keystroke.
	 */
	public void setHostNameValidator(ISystemValidator v) {
		hostValidator = v;
	}

	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v) {
		userIdValidator = v;
	}

	/**
	 * Set the profile names to show in the combo
	 */
	public void setProfileNames(String[] names) {
		this.defaultProfileNames = names;
		if (profileCombo != null)
			profileCombo.setItems(names);
	}

	/**
	 * Set the profile name to preselect
	 */
	public void setProfileNamePreSelection(String selection) {
		this.defaultProfile = selection;
		if ((profileCombo != null) && (selection != null)) {
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
	public void setUserId(String userId) {
		defaultUserId = userId;
	}

	/**
	 * Set the currently selected connection so as to better initialize input fields
	 */
	public void setCurrentlySelectedConnection(IHost connection) {
		if (connection != null) {
			initializeInputFields(connection, false);
		}
	}

	/**
	 * Call this to restrict the system type that the user is allowed to choose.
	 * Must be called before the widgets are created in 
	 * {@link #createContents(Composite, boolean, String)}.
	 * 
	 * @param systemType the only IRSESystemType allowed, or
	 *     <code>null</code> to show all allowed system types.
	 */
	public void restrictSystemType(IRSESystemType systemType) {
		if (systemType==null) {
			restrictSystemTypes(null);
		} else {
			IRSESystemType[] types = new IRSESystemType[] { systemType };
			restrictSystemTypes (types);
		}
	}

	/**
	 * Call this to restrict the system types that the user is allowed to choose.
	 * 
	 * @param systemTypes the list of allowed system types, or
	 *     <code>null</code> to not restrict the allowed system types.
	 */
	public void restrictSystemTypes(IRSESystemType[] systemTypes) {
		//Remember the old selection before changing the data
		IRSESystemType oldSelection = getSystemType();
		
		//Update the known list of valid system types
		if (systemTypes == null) {
			validSystemTypes = SystemWidgetHelpers.getValidSystemTypes(null);
		} else {
			validSystemTypes = new IRSESystemType[systemTypes.length];
			System.arraycopy(systemTypes, 0, validSystemTypes, 0, systemTypes.length);
			SystemWidgetHelpers.sortSystemTypesByLabel(validSystemTypes);
		}
		
		//Restore the default system type based on the new list
		List systemTypesAsList = Arrays.asList(validSystemTypes);
		if (defaultSystemType == null || !systemTypesAsList.contains(defaultSystemType)) {
			defaultSystemType = validSystemTypes[0];
		} 

		//Set items in Combo and restore the previous selection
		if (textSystemType!=null) {
			textSystemType.setItems(SystemWidgetHelpers.getSystemTypeLabels(validSystemTypes));
			if (oldSelection!=null && Arrays.asList(validSystemTypes).contains(oldSelection)) {
				textSystemType.select(systemTypesAsList.indexOf(oldSelection));
			} else {
				textSystemType.select(0);
			}
		}
	}

	/**
	 * Initialize input fields to current values in update mode.
	 * Note in update mode we do NOT allow users to change the system type.
	 * <b>This must be called <i>after</i> calling getContents!
	 * You must also be sure to pass true in createContents in order to call this method.
	 * @param conn The SystemConnection object that is being modified.
	 */
	public void initializeInputFields(IHost conn) {
		initializeInputFields(conn, true);
	}

	/**
	 * Initialize input fields to current values in update mode.
	 * Note in update mode we do NOT allow users to change the system type.
	 * <b>This must be called <i>after</i> calling getContents!
	 * You must also be sure to pass true in createContents in order to call this method.
	 * @param conn The SystemConnection object that is being modified.
	 */
	public void initializeInputFields(IHost conn, boolean updateMode) {
		this.updateMode = updateMode;
		this.conn = conn;
		defaultSystemType = conn.getSystemType();
		defaultConnectionName = conn.getAliasName();
		defaultHostName = conn.getHostName();
		defaultUserId = conn.getLocalDefaultUserId();
		defaultDescription = conn.getDescription();
		defaultProfile = conn.getSystemProfile().getName();
		defaultWorkOffline = conn.isOffline();
		defaultEncoding = conn.getDefaultEncoding(false);
		
		if (defaultEncoding == null) {
			defaultEncoding = conn.getDefaultEncoding(true);
			isRemoteEncoding = true;
		}
		else {
			isRemoteEncoding = false;
		}

		if (updateMode) {
			defaultProfileNames = new String[1];
			defaultProfileNames[0] = defaultProfile;
		}

		if (contentsCreated) doInitializeFields();
	}

	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name) {
		defaultConnectionName = name;
		if (contentsCreated) {
			textConnectionName.setText(name != null ? name : ""); //$NON-NLS-1$
			verify(false);
		}
	}

	/**
	 * Preset the host name
	 */
	public void setHostName(String name) {
		defaultHostName = name;
		if (contentsCreated) {
			textHostName.setText(name != null ? name : ""); //$NON-NLS-1$
			verify(false);
		}
	}

	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete() {
		boolean pageComplete = false;

		if (errorMessage == null) {
			pageComplete = ((getConnectionName().length() > 0) && (getHostName().length() > 0) && (getProfileName().length() > 0));
		}

		return pageComplete;
	}

	/**
	 * @return whether the connection name is unique or not
	 */
	public boolean isConnectionUnique() {
		// DKM - d53587 - used to check connection name uniqueness without displaying the error
		// TODO - need to display a directive for the user to specify a unique connection name 
		//        when it's invalid to be consistent with the rest of eclipse rather than
		// 		  an error message
		int selectedProfile = 0;
		if (profileCombo != null) {
			selectedProfile = profileCombo.getSelectionIndex();
		}
		if (selectedProfile < 0)
			selectedProfile = 0;

		ISystemValidator nameValidator = null;
		if ((nameValidators != null) && (nameValidators.length > 0))
			nameValidator = nameValidators[selectedProfile];
		String connName = textConnectionName.getText().trim();
		if (nameValidator != null)
			errorMessage = nameValidator.validate(connName);

		if (errorMessage != null) {
			return false;
		} else {
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
	public boolean verify(boolean okPressed) {
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
		if ((errorMessage == null) && (textHostName != null)) {
			errorMessage = validateHostNameInput();
			if (errorMessage != null)
				controlInError = textHostName;
		}
		// validate user Id...
		if ((errorMessage == null) && (textUserId != null)) {
			errorMessage = validateUserIdInput();
			if (errorMessage != null)
				controlInError = textUserId;
		}
		// verify host name...
		if ((errorMessage == null) && okPressed && (textHostName != null) && verifyHostNameCB.getSelection()) {
			currentHostName = textHostName.getText().trim();
			if (currentHostName.length() > 0) {
				if (verifyingHostName == null) {
					verifyingHostName = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_HOSTNAME_VERIFYING);
				}

				try {
					getRunnableContext().run(true, true, this);
				} catch (InterruptedException e) {
					// user cancelled				
					ok = false;
					controlInError = textHostName;
				} catch (InvocationTargetException e) {
					// error found
					errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_HOSTNAME_NOTFOUND);
					errorMessage.makeSubstitution(currentHostName);
					controlInError = textHostName;
				}
			}
		}
		
		// validate host name...
		if ((errorMessage == null) && addEncodingFields) {
			errorMessage = validateEncoding();
			
			if (errorMessage != null) {
				controlInError = otherEncodingCombo;
			}
		}

		// if ok pressed, test for warning situation that connection name is in use in another profile...
		if (ok && (errorMessage == null) && okPressed) {
			String connectionName = textConnectionName.getText().trim();
			if (!connectionName.equals(defaultConnectionName)) {
				ok = ValidatorConnectionName.validateNameNotInUse(connectionName, caller.getShell());
			}
			controlInError = textConnectionName;
		}

		// ...end of validation...

		if (!ok || (errorMessage != null)) {
			ok = false;
			if (okPressed && controlInError != null)
				controlInError.setFocus();
			showErrorMessage(errorMessage);
		}
		// DETERMINE DEFAULT USER ID AND WHERE IT IS TO BE SET...
		// Possibly prompt for where to set user Id...
		else {
			boolean isLocal = false;
			if (textUserId != null) {
				isLocal = textUserId.isLocal();
				if (isLocal)
					userIdLocation = IRSEUserIdConstants.USERID_LOCATION_HOST; // edit this connection's local value
				else
					userIdLocation = IRSEUserIdConstants.USERID_LOCATION_DEFAULT_SYSTEMTYPE; // edit the preference value
			} else
				userIdLocation = IRSEUserIdConstants.USERID_LOCATION_NOTSET;
			SystemPreferencesManager.setVerifyConnection(verifyHostNameCB.getSelection());
		}

		return ok;
	}

	/**
	 * Return the runnable context from the hosting dialog or wizard, if
	 * applicable
	 */
	protected IRunnableContext getRunnableContext() {
		if (callerInstanceOfWizardPage) {
			return ((WizardPage)caller).getWizard().getContainer();
		} else if (callerInstanceOfSystemPromptDialog) {
			return ((SystemPromptDialog)caller);
		} else {
			return new ProgressMonitorDialog(caller.getShell());
		}
	}

	/**
	 * Check if this system type is enabled for offline support
	 */
	private boolean enableOfflineCB() {
		// disabled offline checkbox for new connections
		if (!updateMode) {
			return false;
		}
		RSESystemTypeAdapter sysTypeAdapter = (RSESystemTypeAdapter)(defaultSystemType.getAdapter(RSESystemTypeAdapter.class));
		return sysTypeAdapter.isEnableOffline(defaultSystemType);
	}

	/**
	 * Return user-entered System Type.
	 * Call this after finish ends successfully.
	 */
	public IRSESystemType getSystemType() {
		if (textSystemType != null) {
			int idx = textSystemType.getSelectionIndex();
			if (idx >= 0) {
				return validSystemTypes[idx];
			}
		}
		return defaultSystemType;
	}

	/**
	 * Return user-entered Connection Name.
	 * Call this after finish ends successfully.
	 */
	public String getConnectionName() {
		return textConnectionName.getText().trim();
	}

	/**
	 * Return user-entered Host Name.
	 * Call this after finish ends successfully.
	 */
	public String getHostName() {
		return textHostName.getText().trim();
	}

	/**
	 * Return user-entered Default User Id.
	 * Call this after finish ends successfully.
	 */
	public String getDefaultUserId() {
		if (textUserId != null)
			return textUserId.getText().trim();
		else
			return ""; //$NON-NLS-1$
	}

	/**
	 * Return the user-entered value for work offline.
	 * Call this after finish ends successfully.
	 */
	public boolean isWorkOffline() {
		if (workOfflineCB != null) {
			return workOfflineCB.getSelection();
		} else {
			return false;
		}
	}

	/**
	 * Return user-entered Description.
	 * Call this after finish ends successfully.
	 */
	public String getConnectionDescription() {
		return textDescription.getText().trim();
	}

	/**
	 * Return user-selected profile to contain this connection
	 * Call this after finish ends successfully, and only in update mode.
	 */
	public String getProfileName() {
		return (labelProfileValue != null) ? labelProfileValue.getText() : profileCombo.getText();
	}

	/**
	 * If a default userId was specified, the user may have been queried
	 * where to put the userId. This returns one of the constants from
	 * IRSEUserIdConstants.
	 * @return the user id location
	 * @see IRSEUserIdConstants
	 */
	public int getUserIdLocation() {
		if ((textUserId != null) && (textUserId.getText().trim().length() > 0))
			return userIdLocation;
		else
			return IRSEUserIdConstants.USERID_LOCATION_NOTSET;
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
	public Control createContents(Composite parent, boolean updateMode, String parentHelpId) {
		Label labelSystemType = null;
		String temp = null;

		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		SystemWidgetHelpers.setCompositeHelp(composite_prompts, parentHelpId);

		// Type display
		if (updateMode) {
			temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_TYPE_LABEL);
			labelType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
			labelTypeValue = SystemWidgetHelpers.createLabel(composite_prompts, SystemResources.RESID_CONNECTION_TYPE_VALUE);
		}

		// PROFILE SELECTION
		if (updateMode) {
			temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_PROFILE_LABEL);
			labelProfile = SystemWidgetHelpers.createLabel(composite_prompts, temp);
			labelProfile.setToolTipText(SystemResources.RESID_CONNECTION_PROFILE_READONLY_TIP);
			labelProfileValue = SystemWidgetHelpers.createLabel(composite_prompts, ""); //$NON-NLS-1$
			labelProfileValue.setToolTipText(SystemResources.RESID_CONNECTION_PROFILE_READONLY_TIP);
		} else // if (!updateMode)
		{
			temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_PROFILE_LABEL);
			labelProfile = SystemWidgetHelpers.createLabel(composite_prompts, temp);
			labelProfile.setToolTipText(SystemResources.RESID_CONNECTION_PROFILE_TIP);
			if (!updateMode) {
				profileCombo = SystemWidgetHelpers.createReadonlyCombo(composite_prompts, null, SystemResources.RESID_CONNECTION_PROFILE_TIP);
				SystemWidgetHelpers.setHelp(profileCombo, RSEUIPlugin.HELPPREFIX + "ccon0001"); //$NON-NLS-1$     
			}
		}

		if (!updateMode)
			SystemWidgetHelpers.createLabel(composite_prompts, " ", 2); // filler //$NON-NLS-1$

		// SYSTEMTYPE PROMPT IN UPDATE MODE OR RESTRICTED MODE
		if (updateMode || ((validSystemTypes != null) && (validSystemTypes.length == 1))) {
			if (updateMode) {
				temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL);
				labelSystemType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
				labelSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_READONLY_TIP);
				textSystemTypeReadOnly = SystemWidgetHelpers.createLabel(composite_prompts, ""); //$NON-NLS-1$	    
				textSystemTypeReadOnly.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_READONLY_TIP);
			} else
				singleTypeMode = true;
		}

		if (updateMode)
			SystemWidgetHelpers.createLabel(composite_prompts, " ", nbrColumns); // filler //$NON-NLS-1$

		// HOSTNAME PROMPT
		temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_HOSTNAME_LABEL);
		labelHostName = SystemWidgetHelpers.createLabel(composite_prompts, temp);
		labelHostName.setToolTipText(SystemResources.RESID_CONNECTION_HOSTNAME_TIP);

		if (!updateMode && (defaultSystemType == null)) {
			defaultSystemType = lastSystemType;
			if (defaultSystemType == null) {
				defaultSystemType = validSystemTypes[0];
			}
		}

		textHostName = SystemWidgetHelpers.createHostNameCombo(composite_prompts, null, defaultSystemType);
		textHostName.setToolTipText(SystemResources.RESID_CONNECTION_HOSTNAME_TIP);
		SystemWidgetHelpers.setHelp(textHostName, RSEUIPlugin.HELPPREFIX + "ccon0004"); //$NON-NLS-1$     

		// CONNECTION NAME PROMPT
		temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_CONNECTIONNAME_LABEL);
		labelConnectionName = SystemWidgetHelpers.createLabel(composite_prompts, temp);
		labelConnectionName.setToolTipText(SystemResources.RESID_CONNECTION_CONNECTIONNAME_TIP);
		textConnectionName = SystemWidgetHelpers.createTextField(composite_prompts, null, SystemResources.RESID_CONNECTION_CONNECTIONNAME_TIP);
		SystemWidgetHelpers.setHelp(textConnectionName, RSEUIPlugin.HELPPREFIX + "ccon0002"); //$NON-NLS-1$ 

		// SYSTEMTYPE PROMPT IN CREATE MODE
		// if (!updateMode)
		if ((labelSystemType == null) && !singleTypeMode) {
			temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_LABEL);
			labelSystemType = SystemWidgetHelpers.createLabel(composite_prompts, temp);
			labelSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
			if (validSystemTypes==null) {
				validSystemTypes = SystemWidgetHelpers.getValidSystemTypes(null);
			} else {
				SystemWidgetHelpers.sortSystemTypesByLabel(validSystemTypes);
			}
			textSystemType = SystemWidgetHelpers.createSystemTypeCombo(composite_prompts, null, validSystemTypes);
			textSystemType.setToolTipText(SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
			SystemWidgetHelpers.setHelp(textSystemType, RSEUIPlugin.HELPPREFIX + "ccon0003"); //$NON-NLS-1$ 
		}

		// USERID PROMPT
		/*
		 * We are testing the usability of not prompting for the user ID, so that the user has less to think about when
		 * creating a new connection. Phil.
		 */
		if (updateMode) // added for this experiment
		{
			temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_DEFAULTUSERID_LABEL);
			labelUserId = SystemWidgetHelpers.createLabel(composite_prompts, temp);
			labelUserId.setToolTipText(SystemResources.RESID_CONNECTION_DEFAULTUSERID_TIP);
			textUserId = SystemWidgetHelpers.createInheritableTextField(composite_prompts, SystemResources.RESID_CONNECTION_DEFAULTUSERID_INHERITBUTTON_TIP,
																																	SystemResources.RESID_CONNECTION_DEFAULTUSERID_TIP);
			SystemWidgetHelpers.setHelp(textUserId, RSEUIPlugin.HELPPREFIX + "ccon0005"); //$NON-NLS-1$     
		}

		// DESCRIPTION PROMPT
		temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_DESCRIPTION_LABEL);
		labelDescription = SystemWidgetHelpers.createLabel(composite_prompts, temp);
		labelDescription.setToolTipText(SystemResources.RESID_CONNECTION_DESCRIPTION_TIP);
		textDescription = SystemWidgetHelpers.createTextField(composite_prompts, null, SystemResources.RESID_CONNECTION_DESCRIPTION_TIP);
		SystemWidgetHelpers.setHelp(textDescription, RSEUIPlugin.HELPPREFIX + "ccon0006"); //$NON-NLS-1$     

		// VERIFY HOST NAME CHECKBOX
		SystemWidgetHelpers.createLabel(composite_prompts, " ", nbrColumns); // filler //$NON-NLS-1$
		verifyHostNameCB = SystemWidgetHelpers.createCheckBox(composite_prompts, nbrColumns, null, SystemResources.RESID_CONNECTION_VERIFYHOSTNAME_LABEL,
																													SystemResources.RESID_CONNECTION_VERIFYHOSTNAME_TOOLTIP);
		if (updateMode)
			verifyHostNameCB.setSelection(false);
		else
			verifyHostNameCB.setSelection(SystemPreferencesManager.getVerifyConnection());

		// yantzi: artemis 6.0, work offline
		if (enableOfflineCB()) {
			workOfflineCB = SystemWidgetHelpers.createCheckBox(composite_prompts, nbrColumns, null, SystemResources.RESID_OFFLINE_WORKOFFLINE_LABEL,
																													SystemResources.RESID_OFFLINE_WORKOFFLINE_TOOLTIP);
			SystemWidgetHelpers.setHelp(workOfflineCB, RSEUIPlugin.HELPPREFIX + "wofp0000"); //$NON-NLS-1$
		}

		connectionNameEmpty = (textConnectionName.getText().trim().length() == 0); // d43191

		textConnectionName.setFocus();

		// add keystroke listeners...
		textConnectionName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateConnectionNameInput(true);
			}
		});
		textHostName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateHostNameInput();
				validateConnectionNameInput(false);
			}
		});
		if (textUserId != null)
			textUserId.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
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

		if ((textSystemType != null) && (textSystemType.getSelectionIndex()>=0))
			caller.systemTypeSelected(validSystemTypes[textSystemType.getSelectionIndex()], true);
		else if ((validSystemTypes != null) && (validSystemTypes.length == 1))
			caller.systemTypeSelected(validSystemTypes[0], true);

		if (textUserId == null)
			userIdLocation = IRSEUserIdConstants.USERID_LOCATION_NOTSET;
		
		// check if an encodings field should be added
		if (addEncodingFields) {
			
			SystemWidgetHelpers.createLabel(composite_prompts, "", 2); //$NON-NLS-1$
			
			// encoding field
			encodingGroup = SystemWidgetHelpers.createGroupComposite(composite_prompts, 2, SystemResources.RESID_HOST_ENCODING_GROUP_LABEL);
			GridData data = new GridData();
			data.horizontalSpan = 2;
			data.horizontalAlignment = SWT.FILL;
			data.grabExcessHorizontalSpace = true;
			data.verticalAlignment = SWT.BEGINNING;
			data.grabExcessVerticalSpace = false;
			encodingGroup.setLayoutData(data);
			
	        Composite messageComposite = new Composite(encodingGroup, SWT.NONE);
	        GridLayout messageLayout = new GridLayout();
	        messageLayout.numColumns = 2;
	        messageLayout.marginWidth = 0;
	        messageLayout.marginHeight = 0;
	        messageComposite.setLayout(messageLayout);
	        messageComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
	        Label noteLabel = new Label(messageComposite, SWT.BOLD);
	        noteLabel.setText(SystemResources.RESID_HOST_ENCODING_SETTING_NOTE);
	        noteLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
	        data = new GridData();
	        data.grabExcessHorizontalSpace = false;
	        noteLabel.setLayoutData(data);
			
	        Label messageLabel = new Label(messageComposite, SWT.NULL);
	        messageLabel.setText(SystemResources.RESID_HOST_ENCODING_SETTING_MSG);
	        data = new GridData();
	        data.horizontalAlignment = SWT.BEGINNING;
	        data.grabExcessHorizontalSpace = true;
	        data.horizontalIndent = 0;
	        messageLabel.setLayoutData(data);
	        
			SystemWidgetHelpers.createLabel(encodingGroup, ""); //$NON-NLS-1$
		
			// remote encoding field
			String defaultEncodingLabel = SystemResources.RESID_HOST_ENCODING_REMOTE_LABEL;
			
			// check if user set encoding
			// if so, we leave default encoding label as is
			// if not, we check if remote encoding is set, and if it is, include the encoding in the label
			if (conn.getDefaultEncoding(false) == null) {
				
				String remoteEncoding = conn.getDefaultEncoding(true);
				
				if (remoteEncoding != null) {
					defaultEncodingLabel = SystemResources.RESID_HOST_ENCODING_REMOTE_ENCODING_LABEL;
					
					int idx = defaultEncodingLabel.indexOf('%');
					
					if (idx != -1) {
						defaultEncodingLabel = defaultEncodingLabel.substring(0, idx) + remoteEncoding + defaultEncodingLabel.substring(idx+2);
					}
				}
			}
			
			remoteEncodingButton = SystemWidgetHelpers.createRadioButton(encodingGroup, null, defaultEncodingLabel, SystemResources.RESID_HOST_ENCODING_REMOTE_TOOLTIP);
			data = new GridData();
			data.horizontalSpan = 2;
			data.grabExcessHorizontalSpace = true;
			remoteEncodingButton.setLayoutData(data);
			
			SelectionAdapter remoteButtonSelectionListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateEncodingGroupState(remoteEncodingButton.getSelection());
					validateEncoding();
				}
			};
			
			remoteEncodingButton.addSelectionListener(remoteButtonSelectionListener);
			
	        Composite otherComposite = new Composite(encodingGroup, SWT.NONE);
	        GridLayout otherLayout = new GridLayout();
	        otherLayout.numColumns = 2;
	        otherLayout.marginWidth = 0;
	        otherLayout.marginHeight = 0;
	        otherComposite.setLayout(otherLayout);
	        otherComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
			// other encoding field
			otherEncodingButton = SystemWidgetHelpers.createRadioButton(otherComposite, null, SystemResources.RESID_HOST_ENCODING_OTHER_LABEL, SystemResources.RESID_HOST_ENCODING_OTHER_TOOLTIP);
			data = new GridData();
			data.grabExcessHorizontalSpace = false;
			otherEncodingButton.setLayoutData(data);
			
			SelectionAdapter otherButtonSelectionListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateEncodingGroupState(!otherEncodingButton.getSelection());
					validateEncoding();
				}
			};
			
			otherEncodingButton.addSelectionListener(otherButtonSelectionListener);

			// other encoding combo
			otherEncodingCombo = SystemWidgetHelpers.createCombo(otherComposite, null, SystemResources.RESID_HOST_ENCODING_ENTER_TOOLTIP);
			data = new GridData();
			data.horizontalAlignment = SWT.BEGINNING;
			data.grabExcessHorizontalSpace = true;
			data.horizontalIndent = 0;
			otherEncodingCombo.setLayoutData(data);

			otherEncodingCombo.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
					validateEncoding();
				}
			});

			otherEncodingCombo.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent e) {
					validateEncoding();
				}
			});
			
			SystemWidgetHelpers.createLabel(encodingGroup, ""); //$NON-NLS-1$

			SystemWidgetHelpers.createLabel(composite_prompts, "", 2); //$NON-NLS-1$
		}

		doInitializeFields();

		contentsCreated = true;

		return composite_prompts; // composite;
	}
	
	/**
	 * Update the encoding group state.
	 * @param useDefault whether to update the state with default option on. <code>true</code> if the default option
	 * should be on, <code>false</code> if it should be off.
	 */
	private void updateEncodingGroupState(boolean useDefault) {
		remoteEncodingButton.setSelection(useDefault);
		otherEncodingButton.setSelection(!useDefault);
		
		if (useDefault) {
			
			if (defaultEncoding != null) {
				otherEncodingCombo.setText(defaultEncoding);
			}
			else {
				String workspaceDefault = SystemEncodingUtil.getInstance().getLocalDefaultEncoding();
				otherEncodingCombo.setText(workspaceDefault); //$NON-NLS-1$
			}
		}
		
		validateEncoding();
	}
	
	/**
	 * Updates the valid state of the encoding group.
	 */
	private SystemMessage validateEncoding() {
		boolean isValid = isEncodingValid();
		
		errorMessage = null;
		
		if (!isValid) {
			errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ENCODING_NOT_SUPPORTED);
		}
		
		showErrorMessage(errorMessage);
		setPageComplete();
		return errorMessage;
	}

	/**
	 * Returns whether the encoding is valid.
	 * @return <code>true</code> if the encoding is valid, <code>false</code> otherwise.
	 */
	private boolean isEncodingValid() {
		return remoteEncodingButton.getSelection() || isEncodingValid(otherEncodingCombo.getText());
	}
	
	/**
	 * Returns whether or not the given encoding is valid.
	 * @param encoding the encoding.
	 * @return <code>true</code> if the encoding is valid, <code>false</code> otherwise.
	 */
	private boolean isEncodingValid(String encoding) {
		try {
			return Charset.isSupported(encoding);
		}
		catch (IllegalCharsetNameException e) {
			return false;
		}
	}

	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl() {
		if (updateMode || !singleTypeMode)
			return textConnectionName;
		else
			return textHostName; // create mode and restricted to one system type
	}

	/**
	 * Default implementation to satisfy Listener interface. Does nothing.
	 */
	public void handleEvent(Event evt) {
	}

	/**
	 * Combo selection listener method
	 */
	public void widgetDefaultSelected(SelectionEvent event) {
	}

	/**
	 * Combo selection listener method
	 */
	public void widgetSelected(SelectionEvent event) {
		Object src = event.getSource();

		if (src == profileCombo) {
			profileCombo.getDisplay().asyncExec(this);
		} else if (src == textSystemType) // can only happen in create mode
		{
			String currHostName = textHostName.getText().trim();
			boolean hostNameChanged = !currHostName.equals(originalHostName);
			IRSESystemType currSystemType = getSystemType();
			textHostName.setItems(RSECorePlugin.getTheSystemRegistry().getHostNames(currSystemType));
			if (hostNameChanged) {
				textHostName.setText(currHostName);
			} else if (textHostName.getItemCount() > 0) {
				textHostName.setText(textHostName.getItem(0));
				originalHostName = textHostName.getText();
			} else {
				String connName = textConnectionName.getText().trim();
				if (connName.indexOf(' ') == -1)
					textHostName.setText(connName);
				else
					textHostName.setText(""); //$NON-NLS-1$
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
				if (errorMessage != null && errorMessage == RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_HOSTNAME_NOTFOUND)) {
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

						if (wizard instanceof RSEAbstractNewConnectionWizard) {
							RSEAbstractNewConnectionWizard connWizard = (RSEAbstractNewConnectionWizard)wizard;
							AbstractSystemWizardPage mainPage = (AbstractSystemWizardPage)(connWizard.getStartingPage());

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
						} else {
							pages = wizard.getPages();
						}

						for (int i = 0; i < pages.length; i++) {

							IWizardPage page = pages[i];

							if (page instanceof AbstractSystemWizardPage) {
								((AbstractSystemWizardPage)page).clearErrorMessage();
							} else if (page instanceof WizardPage) {
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
	private void doInitializeFields() {
		// -------
		// profile
		// -------
		// ...output-only:
		if (labelProfileValue != null) {
			if (defaultProfile != null)
				labelProfileValue.setText(defaultProfile);
		}
		// ...selectable:
		else {
			if (defaultProfileNames != null)
				profileCombo.setItems(defaultProfileNames);

			if (defaultProfile != null) {
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
		if ((textSystemTypeReadOnly != null) || singleTypeMode) {
			if (validSystemTypes != null) {
				if (textSystemTypeReadOnly != null)
					textSystemTypeReadOnly.setText(validSystemTypes[0].getLabel());
				if (defaultSystemType == null)
					defaultSystemType = validSystemTypes[0];
			} else if (defaultSystemType != null) {
				if (textSystemTypeReadOnly != null)
					textSystemTypeReadOnly.setText(defaultSystemType.getLabel());
			}
		}
		// ...selectable:
		else {
			if (defaultSystemType == null) {
				defaultSystemType = lastSystemType;
			}
			if (defaultSystemType != null) {
				int selIdx = Arrays.asList(validSystemTypes).indexOf(defaultSystemType);
				if (selIdx >= 0) {
					textSystemType.select(selIdx);
				} else {
					textSystemType.select(0);
				}
			} else {
				textSystemType.select(0);
			}
			defaultSystemType = getSystemType();
		}

		// ---------------------------------------------------		
		// connection name
		// ---------------------------------------------------
		if (defaultConnectionName != null)
			textConnectionName.setText(defaultConnectionName);
		textConnectionName.setTextLimit(connectionNameLength);

		// -----------		
		// host name (address)
		// -----------
		if (defaultHostName != null) {
			textHostName.setText(defaultHostName);
		} else if (textHostName.getItemCount() > 0) {
			textHostName.select(0);
		}
		textHostName.setTextLimit(hostNameLength);
		textHostName.clearSelection(); // should unselect the text, but it does not!

		// ---------------		
		// default user id
		// ---------------
		initializeUserIdField(defaultSystemType, defaultUserId);
		if (textUserId != null)
			textUserId.setTextLimit(userIdLength);
		// description
		if (defaultDescription != null)
			textDescription.setText(defaultDescription);
		textDescription.setTextLimit(descriptionLength);

		// ---------------		
		// Work offline
		// ---------------
		if (workOfflineCB != null) {
			workOfflineCB.setSelection(defaultWorkOffline);
		}
		
	    // the file encoding group
	    if (addEncodingFields) {
	    	List encodings = IDEEncoding.getIDEEncodings();
	    	String[] encodingStrings = new String[encodings.size()];
	    	encodings.toArray(encodingStrings);
	    	otherEncodingCombo.setItems(encodingStrings);

	    	// if the encoding is the same as the default encoding, then we want to choose the default encoding option
	    	if (isRemoteEncoding) {
	    		updateEncodingGroupState(true);
	    	}
	    	// otherwise choose the other encoding option
	    	else {
	    		otherEncodingCombo.setText(defaultEncoding);
	    		updateEncodingGroupState(false);
	    	}
	    	
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			
			// disable if any subsystem is connected
			if (!conn.getSystemType().getId().equalsIgnoreCase(IRSESystemType.SYSTEMTYPE_LOCAL_ID) && sr.isAnySubSystemConnected(conn)) {
				encodingGroup.setEnabled(false);
				remoteEncodingButton.setEnabled(false);
				otherEncodingButton.setEnabled(false);
				otherEncodingCombo.setEnabled(false);
			}
	    }

		verify(false);
	}

	/**
	 * Initialize userId values.
	 * We have to reset after user changes the system type
	 */
	private void initializeUserIdField(IRSESystemType systemType, String currentUserId) {
		// ---------------		
		// default user id
		// ---------------
		String parentUserId = RSEPreferencesManager.getDefaultUserId(systemType);
		if (textUserId != null) {
			textUserId.setInheritedText(parentUserId);
			boolean allowEditingOfInherited = ((parentUserId == null) || (parentUserId.length() == 0));
			textUserId.setAllowEditingOfInheritedText(allowEditingOfInherited); // if not set yet, let user set it!
		}
		// ----------------------------
		// default user id: update-mode
		// ----------------------------
		if ((currentUserId != null) && (currentUserId.length() > 0)) {
			if (textUserId != null) {
				textUserId.setLocalText(currentUserId);
				textUserId.setLocal(true);
			}
		}
		// ----------------------------
		// default user id: create-mode
		// ----------------------------
		else {
			if (textUserId != null)
				textUserId.setLocalText(""); //$NON-NLS-1$
			if ((parentUserId != null) && (parentUserId.length() > 0)) {
				userIdFromSystemTypeDefault = true;
				defaultUserId = parentUserId;
				if (textUserId != null)
					textUserId.setLocal(false);
			}
			// there is no local override, and no inherited value. Default to setting inherited value.
			else {
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
	protected SystemMessage validateConnectionNameInput(boolean userTyped) {
		if (!connectionNameListen)
			return null;
		errorMessage = null;
		int selectedProfile = 0;
		if (profileCombo != null) {
			selectedProfile = profileCombo.getSelectionIndex();
		}
		if (selectedProfile < 0)
			selectedProfile = 0;
		ISystemValidator nameValidator = null;
		if ((nameValidators != null) && (nameValidators.length > 0))
			nameValidator = nameValidators[selectedProfile];
		String connName = textConnectionName.getText().trim();
		if (nameValidator != null) {
			errorMessage = nameValidator.validate(connName);
		}
		
		if (errorMessage == null){
			// bug 353377
			// also validate file name - deals with ':' problem
			errorMessage = fileNameValidator.validate(connName);
		}
		
		showErrorMessage(errorMessage);
		setPageComplete();
		if (userTyped)
			connectionNameEmpty = (connName.length() == 0); // d43191
						
		return errorMessage;
	}

	/**
	 * Set the connection name internally without validation
	 */
	protected void internalSetConnectionName(String name) {
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
	protected SystemMessage validateHostNameInput() {
		final String hostName = textHostName.getText().trim();

		// d43191
		if (connectionNameEmpty && contentsCreated) {
			// make sure connection name doesn't use ':' - bug 353377
			String newConnectionName = hostName.replace(':', '_');
			internalSetConnectionName(newConnectionName);
		}

		errorMessage = null;

		if (hostValidator != null)
			errorMessage = hostValidator.validate(hostName);
		else if (getHostName().length() == 0)
			errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_HOSTNAME_EMPTY);

		if (updateMode && !userPickedVerifyHostnameCB) {
			boolean hostNameChanged = !hostName.equals(defaultHostName);
			verifyHostNameCB.setSelection(hostNameChanged);
		}

		showErrorMessage(errorMessage);
		setPageComplete();
		return errorMessage;
	}

	/**
	 * This hook method is called whenever the text changes in the input field. The default implementation delegates the
	 * request to an <code>ISystemValidator</code> object. If the <code>ISystemValidator</code> reports an error the
	 * error message is displayed in the Dialog's message line.
	 * @see #setUserIdValidator(ISystemValidator)
	 */
	protected SystemMessage validateUserIdInput() {
		errorMessage = null;
		if (textUserId != null) {
			if (userIdValidator != null)
				errorMessage = userIdValidator.validate(textUserId.getText());
			else if (getDefaultUserId().length() == 0)
				errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY);
		}
		showErrorMessage(errorMessage);
		setPageComplete();
		return errorMessage;
	}

	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete() {
		boolean complete = isPageComplete();
		if (complete && (textSystemType != null))
			lastSystemType = getSystemType();
		if (callerInstanceOfWizardPage) {
			((WizardPage)caller).setPageComplete(complete);
		} else if (callerInstanceOfSystemPromptDialog) {
			((SystemPromptDialog)caller).setPageComplete(complete);
		} else if (callerInstanceOfPropertyPage) {
			((PropertyPage)caller).setValid(complete);
		}
	}

	/**
	 * Display error message or clear error message
	 */
	private void showErrorMessage(SystemMessage msg) {
		if (msgLine != null)
			if (msg != null)
				msgLine.setErrorMessage(msg);
			else
				msgLine.clearErrorMessage();
		else
			SystemBasePlugin.logDebugMessage(this.getClass().getName(), "MSGLINE NULL. TRYING TO WRITE MSG " + msg); //$NON-NLS-1$
	}

	// ---------------------------------------------------------------
	// STATIC METHODS FOR GETTING A CONNECTION NAME VALIDATOR...
	// ---------------------------------------------------------------

	/**
	 * Reusable method to return a name validator for renaming a connection.
	 * @param conn the current connection object on updates. Can be null for new names. Used
	 *  to remove from the existing name list the current connection.
	 */
	public static ISystemValidator getConnectionNameValidator(IHost conn) {
		ISystemProfile profile = conn.getSystemProfile();
		Vector v = RSECorePlugin.getTheSystemRegistry().getHostAliasNames(profile);
		v.removeElement(conn.getAliasName());
		ValidatorConnectionName connNameValidator = new ValidatorConnectionName(v);
		return connNameValidator;
	}

	/**
	 * Reusable method to return a name validator for renaming a connection.
	 * @param profile the current connection object's profile from which to get the existing names. 
	 *  Can be null for syntax checking only, versus name-in-use.
	 */
	public static ISystemValidator getConnectionNameValidator(ISystemProfile profile) {
		Vector v = RSECorePlugin.getTheSystemRegistry().getHostAliasNames(profile);
		ValidatorConnectionName connNameValidator = new ValidatorConnectionName(v);
		return connNameValidator;
	}

	/**
	 * Reusable method to return name validators for creating a connection.
	 * There is one validator per active system profile.
	 */
	public static ISystemValidator[] getConnectionNameValidators() {
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISystemProfile[] profiles = sr.getSystemProfileManager().getActiveSystemProfiles();
		ISystemValidator[] connNameValidators = new ISystemValidator[profiles.length];
		for (int idx = 0; idx < profiles.length; idx++) {
			Vector v = sr.getHostAliasNames(profiles[idx]);
			connNameValidators[idx] = new ValidatorConnectionName(v);
		}
		return connNameValidators;
	}

	// -------------------------------------------------------
	// METHOD REQUIRED BY RUNNABLE, USED IN CALL TO ASYNCEXEC
	// -------------------------------------------------------

	public void run() {
		verify(false);
	}

	/**
	 * METHOD REQUIRED BY IRunnableWithProgress, USED TO SHOW PROGRESS WHILE
	 * VERIFYING HOSTNAME
	 */
	public void run(IProgressMonitor pm) throws InvocationTargetException, InterruptedException {
		pm.beginTask(verifyingHostName.getLevelOneText(), IProgressMonitor.UNKNOWN);
		try {
			InetAddress.getByName(currentHostName);
		} catch (java.net.UnknownHostException exc) {
			pm.done();
			throw new InvocationTargetException(exc);
		}
		pm.done();
	}
	
	/**
	 * Add fields to enable encoding for the host to be set. This form will not have any encoding fields unless this is called.
	 */
	public void addDefaultEncodingFields() {
		addEncodingFields = true;
	}
	
	/**
	 * Returns the encoding that was specified. Only applies if encoding fields were added to this form.
	 * @return the encoding that was specified. This will return <code>null</code> if the selection is to use the encoding from the remote system
	 * but that encoding has not been obtained yet.
	 * @see #addDefaultEncodingFields()
	 */
	public String getDefaultEncoding() {
		
		if (addEncodingFields) {
			return getSelectedEncoding();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns the currently selected encoding.
	 * @return the currently selected encoding.
	 */
	private String getSelectedEncoding() {
		if (remoteEncodingButton.getSelection()) {
			return defaultEncoding;
		}
		
		return otherEncodingCombo.getText();
	}
	
	/**
	 * Returns whether the encoding option is to use the encoding of the remote system. Only applies if encoding fields were added to this form.
	 * @return <code>true</code> if the encoding option is to use the encoding of the remote system, <code>false</code> if the user specified the encoding.
	 * @see #addDefaultEncodingFields()
	 */
	public boolean isEncodingRemoteDefault() {
		
		if (addEncodingFields) {
			return remoteEncodingButton.getSelection();
		}
		else {
			return false;
		}
	}
}