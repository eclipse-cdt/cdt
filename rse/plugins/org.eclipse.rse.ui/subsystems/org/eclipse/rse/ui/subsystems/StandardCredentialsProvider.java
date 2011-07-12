/********************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * Richie Yu (IBM) - [241716] Handle change expired password
 * Don Yantzi (IBM) - [233970] Handle messages provided by ICredentialsValidator
 * David Dykstal (IBM) - [261047] StandardCredentialsProvider does not cause a reacquire of a password when validation fails in a background thread 
 * David McKnight   (IBM)        - [334839] File Content Conflict is not handled properly
 * David McKnight   (IBM)        - [351883] StandardCredentialsProvider.getCredentials() ignores password cache
 ********************************************************************************/
package org.eclipse.rse.ui.subsystems;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.AbstractCredentialsProvider;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ICredentials;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.logging.LoggerFactory;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.dialogs.ICredentialsValidator;
import org.eclipse.rse.ui.dialogs.ISystemPasswordPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemChangePasswordDialog;
import org.eclipse.rse.ui.dialogs.SystemPasswordPromptDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * The {@link StandardCredentialsProvider} is an extension of
 * {@link AbstractCredentialsProvider} that provides for the prompting of a userid
 * and password.
 * <p>
 * It uses a {@link PasswordPersistenceManager} to store the passwords in the
 * keychain keyed by {@link IHost} and possibly by {@link ISubSystemConfiguration}.
 * <p>
 * This is suitable for use by subclasses of {@link StandardConnectorService}
 * that wish to provide prompting and persistence for userids and passwords when
 * connecting.
 * <p>
 * This class is may be subclassed. Typically to provide connector service
 * specific prompting.
 */
public class StandardCredentialsProvider extends AbstractCredentialsProvider {
	/**
	 * A runnable that will actually perform the prompting.
	 */
	private class PromptForCredentials implements Runnable {
		private boolean cancelled = false;

		/**
		 * @return true if prompting was cancelled.
		 */
		public boolean isCancelled() {
			return cancelled;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			Shell shell = getShell();
			if (shell == null){
				shell = new Shell(); // need this for the case of being prompted during workbench shutdown
			}
			if (shell != null) {
				ISystemPasswordPromptDialog dialog = getPasswordPromptDialog(shell);
				dialog.setSystemInput(getConnectorService());
				dialog.setForceToUpperCase(forcePasswordToUpperCase());
				dialog.setSignonValidator(getSignonValidator());
				if (supportsUserId()) {
					dialog.setUserIdValidator(getUserIdValidator());
				}
				if (supportsPassword()) {
					dialog.setSavePassword(savePassword);
					dialog.setPassword(password);
					dialog.setPasswordValidator(getPasswordValidator());
				}
				try {
					dialog.open();
				} catch (Exception e) {
					logException(e);
				}
				cancelled = dialog.wasCancelled();
				if (!cancelled) {
					userId = dialog.getUserId();
					password = dialog.getPassword();
					saveUserId = dialog.getIsUserIdChangePermanent();
					savePassword = dialog.getIsSavePassword();
				}
			} else {
				cancelled = true;
			}
		}
	}

	/**
	 * A runnable that will prompt for a new password. Typically used when
	 * a password has expired.
	 */
	private class PromptForNewPassword implements Runnable {
		private SystemMessage message;
		private boolean cancelled = false;

		public PromptForNewPassword(SystemMessage message) {
			this.message = message;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public void run() {
			Shell shell = getShell();
			if (shell != null) {
				SystemChangePasswordDialog dlg = new SystemChangePasswordDialog(shell, getConnectorService().getHostName(), getUserId(), message);
				dlg.setSavePassword(savePassword);
				dlg.open();
				cancelled = dlg.wasCancelled();
				if (!cancelled) {
					password = dlg.getNewPassword();
					savePassword = dlg.getIsSavePassword();
				}
			} else {
				cancelled = true;
			}
		}
	}

	private String userId = null;
	private String password = null;
	private boolean savePassword = false;
	private boolean saveUserId = false;
	private boolean acquiring = false;

	/**
	 * Creates a standard credentials provider for a connector service.
	 * @param connectorService the connector service associated with this
	 * provider.
	 * @see IConnectorService
	 */
	public StandardCredentialsProvider(IConnectorService connectorService) {
		super(connectorService);
	}

	/**
	 * Acquires the credentials (userid and password) for this connector
	 * service. The search order for the password is as follows:
	 * </p>
	 * <ol>
	 * <li>First check if the password is already known by this connector
	 * service and that it is still valid.
	 * <li>If password not known then look in the password store and verify that
	 * it is still valid.
	 * <li>If a valid password is not found or is to be re-acquired then prompt
	 * the user
	 * </ol>
	 *
	 * @param reacquire if true then present the prompt even if the password was
	 * 		found and is valid.
	 * @throws OperationCanceledException if user is prompted and user cancels
	 * 		that prompt or if {@link #isSuppressed()} is true. Before RSE 3.0,
	 * 		the method would have thrown {@link InterruptedException} in that
	 * 		case.
	 * @since 3.0
	 */
	public final void acquireCredentials(boolean reacquire) throws OperationCanceledException {
		if (isSuppressed()) {
			throw new OperationCanceledException();
		}
		ISubSystem subsystem = getPrimarySubSystem();
		IHost host = subsystem.getHost();
		String hostName = host.getHostName();
		IRSESystemType systemType = host.getSystemType();
		savePassword = false;
		if (supportsUserId()) {
			boolean sameHost = hostName.equalsIgnoreCase(getConnectorService().getHostName());
			if (!sameHost) {
				clearCredentials();
			}
			getUserId();
		}
		if (supportsPassword()) {
			if (password == null) {
				PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();
				SystemSignonInformation savedSignonInformation = ppm.find(systemType, hostName, userId);
				if (savedSignonInformation != null) {
					password = savedSignonInformation.getPassword();
					savePassword = true;
				}
			}
		}
		ICredentialsValidator validator = getSignonValidator();
		ICredentials credentials = getCredentials();
		if (validator != null) {
			SystemMessage m = validator.validate(credentials);
			// update the password in case an expired password was changed in validate - ry
			password = credentials.getPassword();  
			if (m != null) { // If we ran into an invalid stored password we need to tell the user and force reaquire
				Shell shell = getShell();
				if (shell != null) {
					SystemMessageDialog dialog = new SystemMessageDialog(shell, m);
					dialog.open();
				}
				reacquire = true;
			}
		}
		if (supportsPassword() || supportsUserId()) {
			boolean passwordNeeded = supportsPassword() && password == null;
			boolean userIdNeeded = supportsUserId() && userId == null;
			if (passwordNeeded || userIdNeeded || reacquire) {
				promptForCredentials();
				acquiring = true;
				if (savePassword) {
					getConnectorService().savePassword();
				} else {
					getConnectorService().removePassword();
				}
				if (saveUserId) {
					getConnectorService().saveUserId();
				}
				acquiring = false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#clearCredentials()
	 */
	public final void clearCredentials() {
		if (!acquiring) {
			password = null;
			userId = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#clearPassword()
	 */
	final public void clearPassword() {
		if (!acquiring) {
			password = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#getCredentials()
	 */
	public final ICredentials getCredentials() {
		IHost host = getConnectorService().getHost();
		String hostName = host.getHostName();
		IRSESystemType systemType = host.getSystemType();
		SystemSignonInformation result = null;
		if (password == null && savePassword) { // no password, then read it if we can
			PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();
			result = ppm.find(systemType, hostName, userId);
			if (result != null) {
				password = result.getPassword();
			}
		}
		if (result == null) {
			result = new SystemSignonInformation(hostName, userId, password, systemType);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#getUserId()
	 */
	final public String getUserId() {
		if (supportsUserId()) {
			if (userId == null) {
				userId = getSubSystemUserId();
			}
		}
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#repairCredentials(org.eclipse.rse.services.clientserver.messages.SystemMessage)
	 */
	public final void repairCredentials(SystemMessage prompt) throws OperationCanceledException {
		promptForNewPassword(prompt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ICredentialsProvider#setPassword(java.lang.String)
	 */
	public final void setPassword(String password) {
		this.password = password;
	}

	/**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * Set the password if you got it from somewhere
	 * @param matchingUserId the user for which to set the password
	 * @param password the password to set for this userid
	 * @param persist true if the password is to be persisted as well
	 */
	public final void setPassword(String matchingUserId, String password, boolean persist) {
		if (getPrimarySubSystem().forceUserIdToUpperCase()) {
			matchingUserId = matchingUserId.toUpperCase();
		}
		IConnectorService cs = getConnectorService();
		IRSESystemType systemType = cs.getHost().getSystemType();
		String hostName = cs.getHostName();
		SystemSignonInformation signonInformation = new SystemSignonInformation(hostName, matchingUserId, password, systemType);
		setSignonInformation(signonInformation);
		if (persist) { // if password should be persisted, then add to disk
			PasswordPersistenceManager.getInstance().add(signonInformation, true, false);
		} else { // otherwise, remove from both memory and disk
			PasswordPersistenceManager.getInstance().remove(systemType, hostName, userId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setUserId(java.lang.String)
	 */
	final public void setUserId(String newId) {
		if (userId == null || !userId.equals(newId)) {
			userId = newId;
		}
	}

	/**
	 * <i>A default implementation is supplied, but can be overridden if desired.</i><br>
	 * Instantiates and returns the dialog to prompt for the userId and password.
	 * @return An instance of a dialog class that implements {@link ISystemPasswordPromptDialog}
	 */
	protected ISystemPasswordPromptDialog getPasswordPromptDialog(Shell shell) {
		ISystemPasswordPromptDialog dlg = new SystemPasswordPromptDialog(shell, requiresUserId(), requiresPassword());
		return dlg;
	}

	/**
	 * <i>Optionally overridable, not implemented by default.</i><br>
	 * Get the credentials validator to use to validate the credentials as entered
	 * in the dialog. This should only do a local validation.
	 * @return null indicating that no signon validator exists.
	 */
	protected ICredentialsValidator getSignonValidator() {
		return null;
	}

	private boolean forcePasswordToUpperCase() {
		return getPrimarySubSystem().forceUserIdToUpperCase();
	}

	private ISystemValidator getPasswordValidator() {
		ISubSystemConfiguration subsystemConfiguration = getPrimarySubSystem().getSubSystemConfiguration();
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter) Platform.getAdapterManager().getAdapter(subsystemConfiguration, ISubSystemConfigurationAdapter.class);
		return adapter.getPasswordValidator(subsystemConfiguration);
	}

	private ISubSystem getPrimarySubSystem() {
		return getConnectorService().getPrimarySubSystem();
	}

	private Shell getShell() {
		Shell shell = null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {
				shell = window.getShell();
			}
		}
		return shell;
	}

	private String getSubSystemUserId() {
		ISubSystem ss = getPrimarySubSystem();
		String result = ss.getUserId();
		return result;
	}

	private ISystemValidator getUserIdValidator() {
		ISubSystemConfiguration subsystemConfiguration = getPrimarySubSystem().getSubSystemConfiguration();
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter) subsystemConfiguration.getAdapter(ISubSystemConfigurationAdapter.class);
		// TODO This typically runs in the UI thread. It should probably be
		// moved into the promptForCredentials() method which typically runs in
		// a Job, or even into {@link SubSystem#promptForPassword()}. See
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=218304
		if (adapter == null) {
			Platform.getAdapterManager().loadAdapter(subsystemConfiguration, ISubSystemConfigurationAdapter.class.getName());
			adapter = (ISubSystemConfigurationAdapter) subsystemConfiguration.getAdapter(ISubSystemConfigurationAdapter.class);
		}
		ISystemValidator validator = adapter.getUserIdValidator(subsystemConfiguration);
		return validator;
	}

	private void logException(Throwable t) {
		Logger log = LoggerFactory.getLogger(RSEUIPlugin.getDefault());
		log.logError("Unexpected exception", t); //$NON-NLS-1$
	}

	private void promptForCredentials() throws OperationCanceledException {
		PromptForCredentials runnable = new PromptForCredentials();
		Display.getDefault().syncExec(runnable);
		if (runnable.isCancelled()) {
			throw new OperationCanceledException();
		}
	}

	private void promptForNewPassword(SystemMessage prompt) throws OperationCanceledException {
		PromptForNewPassword runnable = new PromptForNewPassword(prompt);
		Display.getDefault().syncExec(runnable);
		if (runnable.isCancelled()) {
			throw new OperationCanceledException();
		}
	}

	private void setSignonInformation(SystemSignonInformation signonInfo) {
		password = signonInfo.getPassword();
		userId = signonInfo.getUserId();
	}
}
