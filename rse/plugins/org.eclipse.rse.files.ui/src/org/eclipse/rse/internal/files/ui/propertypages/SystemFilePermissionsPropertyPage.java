/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * David McKnight   (IBM)        - [209703] apply encoding and updating remote file when apply on property page
 * Martin Oberhuber (Wind River) - [234038] Force refresh IRemoteFile after changing permissions
 * David McKnight   (IBM)        - [234038] [files][refresh] Changing file permissions does not update property sheet or refresh tree
 * David McKnight   (IBM)        - [234045] [ftp] Errors while changing file permissions are not displayed to the user
 *********************************************************************************/
package org.eclipse.rse.internal.files.ui.propertypages;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.PendingHostFilePermissions;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Property page for viewing and changing the user, group and permissions
 * for a particular file.
 * */
public class SystemFilePermissionsPropertyPage extends SystemBasePropertyPage {

	private Button _userRead;
	private Button _userWrite;
	private Button _userExecute;
	private Button _groupRead;
	private Button _groupWrite;
	private Button _groupExecute;
	private Button _otherRead;
	private Button _otherWrite;
	private Button _otherExecute;

	private Text _userEntry;
	private Text _groupEntry;

	private IHostFilePermissions _permissions;
	private String _owner;
	private String _group;

	/**
	 * Get the input remote file object
	 */
	protected IRemoteFile getRemoteFile()
	{
		Object element = getElement();
		IRemoteFile file = (IRemoteFile)element;


		return file;
	}

	protected Control createContentArea(Composite parent) {


		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 1);

		IRemoteFile file = getRemoteFile();
		IFilePermissionsService service = getPermissionsService(file);
		if (service == null ||
				(service.getCapabilities(file.getHostFile()) & IFilePermissionsService.FS_CAN_GET_PERMISSIONS) == 0){
			// not supported
			SystemWidgetHelpers.createLabel(parent, FileResources.MESSAGE_FILE_PERMISSIONS_NOT_SUPPORTED);
		}
		else
		{
			Group permissionsGroup = SystemWidgetHelpers.createGroupComposite(composite_prompts,4, FileResources.RESID_PREF_PERMISSIONS_PERMISSIONS_LABEL);
			GridData data = new GridData();
			data.horizontalSpan = 5;
			data.horizontalAlignment = SWT.FILL;
			data.grabExcessHorizontalSpace = false;
			data.verticalAlignment = SWT.BEGINNING;
			data.grabExcessVerticalSpace = false;
			permissionsGroup.setLayoutData(data);

			Label userTypeLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_TYPE_LABEL);

			Label readLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_READ_LABEL);
			data = new GridData();
			data.horizontalIndent = 20;
			readLabel.setLayoutData(data);

			Label writeLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_WRITE_LABEL);
			data = new GridData();
			data.horizontalIndent = 20;
			writeLabel.setLayoutData(data);

			Label executeLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_EXECUTE_LABEL);
			data = new GridData();
			data.horizontalIndent = 20;
			executeLabel.setLayoutData(data);


			Label userLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_USER_LABEL);
			_userRead = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_userRead.setLayoutData(data);

			_userWrite = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_userWrite.setLayoutData(data);

			_userExecute = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_userExecute.setLayoutData(data);

			Label groupLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_GROUP_LABEL);

			_groupRead = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_groupRead.setLayoutData(data);

			_groupWrite = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_groupWrite.setLayoutData(data);

			_groupExecute = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_groupExecute.setLayoutData(data);

			Label otherLabel = SystemWidgetHelpers.createLabel(permissionsGroup, FileResources.RESID_PREF_PERMISSIONS_OTHERS_LABEL);
			_otherRead = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_otherRead.setLayoutData(data);

			_otherWrite = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_otherWrite.setLayoutData(data);

			_otherExecute = new Button(permissionsGroup, SWT.CHECK);
			data = new GridData();
			data.horizontalIndent = 20;
			_otherExecute.setLayoutData(data);

			Group ownerGroup = SystemWidgetHelpers.createGroupComposite(composite_prompts, 2, FileResources.RESID_PREF_PERMISSIONS_OWNERSHIP_LABEL);
			data = new GridData();
			data.horizontalSpan = 1;
			data.verticalSpan = 5;
			data.horizontalAlignment = SWT.FILL;
			data.grabExcessHorizontalSpace = false;
			data.verticalAlignment = SWT.BEGINNING;
			data.grabExcessVerticalSpace = false;
			ownerGroup.setLayoutData(data);

			Label userOwnerLabel = SystemWidgetHelpers.createLabel(ownerGroup, FileResources.RESID_PREF_PERMISSIONS_USER_LABEL);
			_userEntry = new Text(ownerGroup, SWT.BORDER);
			data = new GridData();
			data.widthHint = 100;
			_userEntry.setLayoutData(data);

			Label groupOwnerLabel = SystemWidgetHelpers.createLabel(ownerGroup, FileResources.RESID_PREF_PERMISSIONS_GROUP_LABEL);
			_groupEntry = new Text(ownerGroup, SWT.BORDER);
			data = new GridData();
			data.widthHint = 100;
			_groupEntry.setLayoutData(data);


			initFields();
		}

		return composite_prompts;
	}

	protected boolean verifyPageContents() {
		return true;
	}

	private void enableOwnershipFields(boolean enabled) {

		_userEntry.setEnabled(enabled);
		_groupEntry.setEnabled(enabled);
	}

	private void enablePermissionFields(boolean enabled){

		_groupExecute.setEnabled(enabled);
		_groupRead.setEnabled(enabled);
		_groupWrite.setEnabled(enabled);

		_userExecute.setEnabled(enabled);
		_userRead.setEnabled(enabled);
		_userWrite.setEnabled(enabled);

		_otherExecute.setEnabled(enabled);
		_otherRead.setEnabled(enabled);
		_otherWrite.setEnabled(enabled);
	}

	private IFilePermissionsService getPermissionsService(IRemoteFile remoteFile){

		if (remoteFile instanceof IAdaptable){
			return (IFilePermissionsService)((IAdaptable)remoteFile).getAdapter(IFilePermissionsService.class);
		}

		return null;
	}




	private void initFields() {
		IRemoteFile remoteFile = getRemoteFile();

		IFilePermissionsService ps = getPermissionsService(remoteFile);
		if (ps == null){
			enablePermissionFields(false);
			enableOwnershipFields(false);
		}
		else {
			initPermissionFields(remoteFile, ps);
		}
	}

	private void initPermissionFields(IRemoteFile file, IFilePermissionsService service){
		_permissions = null; // null set so that we make sure we're getting fresh permissions

		final IRemoteFile rFile = file;
		final IFilePermissionsService pService = service;

		int capabilities = service.getCapabilities(file.getHostFile());
		if ((capabilities & IFilePermissionsService.FS_CAN_SET_PERMISSIONS) != 0){
			enablePermissionFields(true);
		}
		else {
			enablePermissionFields(false);
		}

		if ((capabilities & IFilePermissionsService.FS_CAN_SET_OWNER) != 0){
			enableOwnershipFields(true);
		}
		else {
			enableOwnershipFields(false);
		}

		if ((capabilities & IFilePermissionsService.FS_CAN_GET_PERMISSIONS) != 0){
			_permissions = file.getPermissions();
			if (_permissions == null || _permissions instanceof PendingHostFilePermissions){
				Job deferredFetch = new Job(FileResources.MESSAGE_GETTING_PERMISSIONS)
				{
					public IStatus run(IProgressMonitor monitor){
						try
						{
							_permissions = pService.getFilePermissions(rFile.getHostFile(), monitor);

							// notify change
							Display.getDefault().asyncExec(new Runnable()
							{
								public void run()
								{
									_userRead.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_USER_READ));
									_userWrite.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_USER_WRITE));
									_userExecute.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_USER_EXECUTE));
									_groupRead.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_GROUP_READ));
									_groupWrite.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_GROUP_WRITE));
									_groupExecute.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_GROUP_EXECUTE));
									_otherRead.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_OTHER_READ));
									_otherWrite.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_OTHER_WRITE));
									_otherExecute.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_OTHER_EXECUTE));

									_owner = _permissions.getUserOwner();
									_group = _permissions.getGroupOwner();

									_userEntry.setText(_owner);
									_groupEntry.setText(_group);

								}
							});
						}
						catch (SystemMessageException e)
						{
							setMessage(e.getSystemMessage());
						}
						return Status.OK_STATUS;
					}
				};
				deferredFetch.schedule();
			}
			else {
				_userRead.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_USER_READ));
				_userWrite.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_USER_WRITE));
				_userExecute.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_USER_EXECUTE));
				_groupRead.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_GROUP_READ));
				_groupWrite.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_GROUP_WRITE));
				_groupExecute.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_GROUP_EXECUTE));
				_otherRead.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_OTHER_READ));
				_otherWrite.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_OTHER_WRITE));
				_otherExecute.setSelection(_permissions.getPermission(IHostFilePermissions.PERM_OTHER_EXECUTE));

				_owner = _permissions.getUserOwner();
				_group = _permissions.getGroupOwner();

				_userEntry.setText(_owner);
				_groupEntry.setText(_group);
			}
		}
		else {
			enablePermissionFields(false);
			enableOwnershipFields(false);
		}
	}



	public boolean performOk() {
		IRemoteFile remoteFile = getRemoteFile();

		boolean changed = false;

		// permission changes
		if (_permissions != null){
			IFilePermissionsService service = getPermissionsService(remoteFile);

			int capabilities = service.getCapabilities(remoteFile.getHostFile());
			if ((capabilities & IFilePermissionsService.FS_CAN_SET_PERMISSIONS) != 0){
				try
				{
					IHostFilePermissions newPermissions = (IHostFilePermissions)_permissions.clone();

					if (_permissions.getPermission(IHostFilePermissions.PERM_USER_READ) != _userRead.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_USER_READ, _userRead.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_USER_WRITE) != _userWrite.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_USER_WRITE, _userWrite.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_USER_EXECUTE) != _userExecute.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_USER_EXECUTE, _userExecute.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_GROUP_READ) != _groupRead.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_GROUP_READ, _groupRead.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_GROUP_WRITE) != _groupWrite.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_GROUP_WRITE, _groupWrite.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_GROUP_EXECUTE) != _groupExecute.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_GROUP_EXECUTE, _groupExecute.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_OTHER_READ) != _otherRead.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_OTHER_READ, _otherRead.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_OTHER_WRITE) != _otherWrite.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_OTHER_WRITE, _otherWrite.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_OTHER_EXECUTE) != _otherExecute.getSelection()){
						changed = true;
						newPermissions.setPermission(IHostFilePermissions.PERM_OTHER_EXECUTE, _otherExecute.getSelection());
					}

					if (_owner != _userEntry.getText()){
						changed = true;
						newPermissions.setUserOwner(_userEntry.getText());
					}
					if (_group != _groupEntry.getText()){
						changed = true;
						newPermissions.setGroupOwner(_groupEntry.getText());
					}


					if (changed){
						//mark file stale even if an exception is thrown later, to ensure proper re-get
						remoteFile.markStale(true, true);
						// assuming permissions are good
						service.setFilePermissions(remoteFile.getHostFile(), newPermissions, new NullProgressMonitor());

						_permissions = newPermissions;
					}
				}
				catch (SystemMessageException e){
					setMessage(e.getSystemMessage());
				}
				catch (CloneNotSupportedException e){
					// unexpected, not showing but logging
					SystemBasePlugin.logError(e.getMessage());
				}
			}
		}

		if (changed){
			remoteFile.markStale(true);
			// notify views of change
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			// refresh the file, since ftp and ssh will need new file objects
			registry.fireEvent(new SystemResourceChangeEvent(remoteFile, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, remoteFile));

			//registry.fireEvent(new SystemResourceChangeEvent(remoteFile, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, remoteFile));
		}


		return super.performOk();
	}

	protected boolean wantDefaultAndApplyButton()
	{
		return true;
	}

	protected void performApply() {
		performOk();
	}

	protected void performDefaults() {
		IRemoteFile file = getRemoteFile();
		IFilePermissionsService service = (IFilePermissionsService)((IAdaptable)file).getAdapter(IFilePermissionsService.class);
		initPermissionFields(file, service);
	}

	public void setVisible(boolean visible) {
		if (visible){
			IRemoteFile file = getRemoteFile();
			if (file.isStale()){ // has file changed?
				try
				{
					file = file.getParentRemoteFileSubSystem().getRemoteFileObject(file.getAbsolutePath(), new NullProgressMonitor());
				}
				catch (SystemMessageException e){
					// unexpected, logging but not showing user
					SystemBasePlugin.logMessage(e.getSystemMessage());
				}
				setElement((IAdaptable)file);

				// reset according to the changed file
				performDefaults();
			}
		}
		super.setVisible(visible);
	}
}
