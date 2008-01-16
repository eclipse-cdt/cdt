/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
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
 ********************************************************************************/
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
import org.eclipse.rse.services.files.IFileOwnerService;
import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
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
		return ((IRemoteFile)element);
	}
	
	protected Control createContentArea(Composite parent) {		

		
		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 1);	
		
		IRemoteFile file = getRemoteFile();
		IFilePermissionsService service = getPermissionsService(file);
		if (service == null || !service.canGetFilePermissions(file.getParentPath(), file.getName())){
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
	

	private IFileOwnerService getOwnerService(IRemoteFile remoteFile){

		if (remoteFile instanceof IAdaptable){
			return (IFileOwnerService)((IAdaptable)remoteFile).getAdapter(IFileOwnerService.class);
		}

		return null;
	}
	
	
	private void initFields() {
		IRemoteFile remoteFile = getRemoteFile();
		
		IFilePermissionsService ps = getPermissionsService(remoteFile);
		if (ps == null){
			enablePermissionFields(false);
		}
		else {
			initPermissionFields(remoteFile, ps);
		}
		
		IFileOwnerService os = getOwnerService(remoteFile);
		if (ps == null){
			enableOwnershipFields(false);
		}
		else {
			initOwnershipFields(remoteFile, os);
		}
	}
	
	private void initPermissionFields(IRemoteFile file, IFilePermissionsService service){
		_permissions = null; // null set so that we make sure we're getting fresh permissions
		
		final IRemoteFile rFile = file;
		final IFilePermissionsService pService = service;
		String remoteParent = file.getParentPath();
		String name = file.getName();
		
		if (service.canGetFilePermissions(remoteParent, name)){
			enablePermissionFields(true);
			try
			{
				_permissions = file.getPermissions();
				if (_permissions == null){
					Job deferredFetch = new Job(FileResources.MESSAGE_GETTING_PERMISSIONS)
					{
						public IStatus run(IProgressMonitor monitor){
							try
							{
								String remoteParent = rFile.getParentPath();
								String fname = rFile.getName();
								_permissions = pService.getFilePermissions(remoteParent, fname, monitor);
								if (_permissions != null && rFile instanceof RemoteFile){
									((RemoteFile)rFile).setPermissions(_permissions);
								}
									
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
									}
								});
							}
							catch (Exception e)
							{						
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
				}
			}
			catch (Exception e){
				
			}
		}
		else {
			enablePermissionFields(false);
		}
	}

	
	private void initOwnershipFields(IRemoteFile file, IFileOwnerService service){
		_owner = null;
		_group = null;
		
		String remoteParent = file.getParentPath();
		String name = file.getName();
		final IRemoteFile rFile = file;
		final IFileOwnerService oService = service;
		
		if (service.canGetFileOwner(remoteParent, name)){
			enableOwnershipFields(true);
			try
			{
				_owner = file.getOwner();
				if (_owner == null){
					Job deferredFetch = new Job(FileResources.MESSAGE_GETTING_OWNER)
					{
						public IStatus run(IProgressMonitor monitor){
							try
							{
								String remoteParent = rFile.getParentPath();
								String fname = rFile.getName();
								_owner = oService.getFileUserOwner(remoteParent, fname, monitor);
								if (_owner != null && rFile instanceof RemoteFile){
									((RemoteFile)rFile).setOwner(_owner);
								}
								
								// notify change 
								Display.getDefault().asyncExec(new Runnable()
								{
									public void run()
									{
										_userEntry.setText(_owner);
									}
								});							
							}
							catch (Exception e)
							{						
							}
							return Status.OK_STATUS;
						}
					};
					deferredFetch.schedule();
					_userEntry.setText(FileResources.MESSAGE_PENDING);
				}
				else {
					_userEntry.setText(_owner);
				}
				
				_group = file.getGroup();
				if (_group == null){
					Job deferredFetch = new Job(FileResources.MESSAGE_GETTING_GROUP)
					{
						public IStatus run(IProgressMonitor monitor){
							try
							{
								String remoteParent = rFile.getParentPath();
								String fname = rFile.getName();
								_group = oService.getFileGroupOwner(remoteParent, fname, monitor);
								if (_group != null && rFile instanceof RemoteFile){
									((RemoteFile)rFile).setGroup(_group);
								}
								
								// notify change 
								Display.getDefault().asyncExec(new Runnable()
								{
									public void run()
									{
										_groupEntry.setText(_group);
									}
								});
							
							}
							catch (Exception e)
							{						
							}
							return Status.OK_STATUS;
						}
					};
					deferredFetch.schedule();
					_groupEntry.setText(FileResources.MESSAGE_PENDING);
				}
				else {
					_groupEntry.setText(_group);
				}
			}
			catch (Exception e){
				
			}
		}
		else {
			enableOwnershipFields(false);
		}
	}

	public boolean performOk() {
		IRemoteFile remoteFile = getRemoteFile();
		
		boolean changed = false;
		
		// permission changes
		if (_permissions != null){
			IFilePermissionsService service = getPermissionsService(remoteFile);
			
			String remoteParent = remoteFile.getParentPath();
			String name = remoteFile.getName();
			
			
			if (service.canSetFilePermissions(remoteParent, name)){
				try
				{
					
					
					if (_permissions.getPermission(IHostFilePermissions.PERM_USER_READ) != _userRead.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_USER_READ, _userRead.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_USER_WRITE) != _userWrite.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_USER_WRITE, _userWrite.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_USER_EXECUTE) != _userExecute.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_USER_EXECUTE, _userExecute.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_GROUP_READ) != _groupRead.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_GROUP_READ, _groupRead.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_GROUP_WRITE) != _groupWrite.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_GROUP_WRITE, _groupWrite.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_GROUP_EXECUTE) != _groupExecute.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_GROUP_EXECUTE, _groupExecute.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_OTHER_READ) != _otherRead.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_OTHER_READ, _otherRead.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_OTHER_WRITE) != _otherWrite.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_OTHER_WRITE, _otherWrite.getSelection());
					}
					if (_permissions.getPermission(IHostFilePermissions.PERM_OTHER_EXECUTE) != _otherExecute.getSelection()){
						changed = true;
						_permissions.setPermission(IHostFilePermissions.PERM_OTHER_EXECUTE, _otherExecute.getSelection());
					}

					if (changed){
						service.setFilePermissions(remoteParent, name, _permissions, new NullProgressMonitor());
					}
				}
				catch (Exception e){
					
				}
			}						
		}
		if (_owner != null){
			IFileOwnerService service = getOwnerService(remoteFile);
			
			String remoteParent = remoteFile.getParentPath();
			String name = remoteFile.getName();
			
			if (service.canSetFileOwner(remoteParent, name)){
				try
				{
					if (_owner != _userEntry.getText()){
						changed = true;
						if (remoteFile instanceof RemoteFile){
							((RemoteFile)remoteFile).setOwner(_owner);
						}
							
						service.setFileUserOwner(remoteParent, name, _userEntry.getText(), new NullProgressMonitor());
					}
					if (_group != _groupEntry.getText()){
						changed = true;
						if (remoteFile instanceof RemoteFile){
							((RemoteFile)remoteFile).setGroup(_group);
						}
						service.setFileGroupOwner(remoteParent, name, _groupEntry.getText(), new NullProgressMonitor());
					}
				}
				catch (Exception e){
					
				}
			}	
		}
		
		if (changed){
			// notify views of change
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(remoteFile, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, remoteFile));
		}
		
		
		return super.performOk();
	}
	
}
