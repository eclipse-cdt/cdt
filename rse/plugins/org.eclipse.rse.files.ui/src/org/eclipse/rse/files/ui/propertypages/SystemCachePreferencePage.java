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

package org.eclipse.rse.files.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.GenericMessages;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.Mnemonics;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;


/**
 * Preference page for generic Remote System cache preferences
 */
public class SystemCachePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, Listener
{

	private Button _clearButton;
	private Button _maxCacheCheckbox;
	private Text _maxCacheSize;

	/**
	 * Constructor
	 */
	public SystemCachePreferencePage()
	{
		super();
		setPreferenceStore(RSEUIPlugin.getDefault().getPreferenceStore());
		setDescription(FileResources.RESID_PREF_CACHE_DESCRIPTION);
	}

	/**
	 * Configure the composite. We intercept to set the help.
	 */
	public void createControl(Composite parent)
	{
		super.createControl(parent);
	}

	/**
	 * 
	 */
	protected Control createContents(Composite gparent)
	{
		Composite parent = SystemWidgetHelpers.createComposite(gparent, 1);

		Composite maxComp = SystemWidgetHelpers.createComposite(parent, 2);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		maxComp.setLayout(layout);
		maxComp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		_maxCacheCheckbox =
			SystemWidgetHelpers.createCheckBox(
				maxComp,
				FileResources.RESID_PREF_CACHE_MAX_CACHE_SIZE_LABEL,
				this);
		_maxCacheCheckbox.setToolTipText(
				FileResources.RESID_PREF_CACHE_MAX_CACHE_SIZE_TOOLTIP);

		_maxCacheSize = SystemWidgetHelpers.createTextField(maxComp, this);
		GridData gd = new GridData();
		gd.widthHint = 75;
		_maxCacheSize.setLayoutData(gd);

		_maxCacheSize.setTextLimit(5);
		_maxCacheSize.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent e)
			{
				e.doit = true;
				for (int loop = 0; loop < e.text.length(); loop++)
				{
					if (!Character.isDigit(e.text.charAt(loop)))
						e.doit = false;
				}
			}
		});

		Composite clearComp = SystemWidgetHelpers.createComposite(parent, 2);
		layout = new GridLayout();
		layout.numColumns = 2;
		clearComp.setLayout(layout);
		clearComp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		SystemWidgetHelpers.createLabel(
			clearComp,
			FileResources.RESID_PREF_CACHE_CLEAR_LABEL);
		_clearButton =
			SystemWidgetHelpers.createPushButton(
				clearComp,
				FileResources.RESID_PREF_CACHE_CLEAR,
				this);
		_clearButton.setToolTipText(FileResources.RESID_PREF_CACHE_CLEAR_TOOLTIP);
		gd = new GridData();
		gd.widthHint = 75;
		_clearButton.setLayoutData(gd);

		Composite warningComp = SystemWidgetHelpers.createComposite(clearComp, 2);
		gd = new GridData();
		gd.horizontalSpan = 2;
		warningComp.setLayoutData(gd);

		Display display = getControl().getDisplay();
		Label warningLabel =
			SystemWidgetHelpers.createLabel(
				warningComp,
				FileResources.RESID_PREF_CACHE_CLEAR_WARNING_LABEL);
		FontData oldData = warningLabel.getFont().getFontData()[0];
		FontData data = new FontData(oldData.getName(), oldData.getHeight(), SWT.BOLD);
		Font fFont = new Font(display, data);

		warningLabel.setFont(fFont);
		SystemWidgetHelpers.createLabel(
			warningComp,
			FileResources.RESID_PREF_CACHE_CLEAR_WARNING_DESCRIPTION);

		(new Mnemonics()).setOnPreferencePage(true).setMnemonics(parent);
		SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "fchp0000");

		initControls();
		return parent;
	}

	private void initControls()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		boolean enableMaxSize = store.getBoolean(ISystemPreferencesConstants.LIMIT_CACHE);
		_maxCacheSize.setEnabled(enableMaxSize);
		
		String maxCacheSizeStr = store.getString(ISystemPreferencesConstants.MAX_CACHE_SIZE);
		
		if (maxCacheSizeStr == null || maxCacheSizeStr.equals("")) {
			maxCacheSizeStr = ISystemPreferencesConstants.DEFAULT_MAX_CACHE_SIZE;
		}
		
		_maxCacheSize.setText(maxCacheSizeStr);
		_maxCacheCheckbox.setSelection(enableMaxSize);
	}

	/**
	 * Inherited method.
	 */
	public void init(IWorkbench workbench)
	{

	}

	/**
		 * @see FieldEditorPreferencePage#performDefaults()
		 */
	protected void performDefaults()
	{
		super.performDefaults();

		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();

		boolean enableMaxSize = store.getDefaultBoolean(ISystemPreferencesConstants.LIMIT_CACHE);
		_maxCacheCheckbox.setSelection(enableMaxSize);

		_maxCacheSize.setEnabled(enableMaxSize);
		_maxCacheSize.setText(store.getDefaultString(ISystemPreferencesConstants.MAX_CACHE_SIZE));
	}

	/**
	 * Set default preferences for the communications preference page.
	 * 
	 * @param store PreferenceStore used for this preference page.
	 */
	public static void initDefaults(IPreferenceStore store)
	{
		store.setDefault(ISystemPreferencesConstants.LIMIT_CACHE, ISystemPreferencesConstants.DEFAULT_LIMIT_CACHE);
		store.setDefault(ISystemPreferencesConstants.MAX_CACHE_SIZE, ISystemPreferencesConstants.DEFAULT_MAX_CACHE_SIZE);
	}

	/**
	 * @see FieldEditorPreferencePage#performOk()
	 */
	public boolean performOk()
	{
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		String size = _maxCacheSize.getText();
		
		if (size == null || size.trim().equals("")) {
			size = ISystemPreferencesConstants.DEFAULT_MAX_CACHE_SIZE;
		}

		store.setValue(ISystemPreferencesConstants.MAX_CACHE_SIZE, size);
		store.setValue(ISystemPreferencesConstants.LIMIT_CACHE, _maxCacheCheckbox.getSelection());

		return super.performOk();
	}

	public class ClearTempFilesRunnable implements IRunnableWithProgress
	{
		public void run(IProgressMonitor monitor)
		{
			SystemRemoteEditManager mgr = SystemRemoteEditManager.getDefault();
			// if no temp file project, nothing to do
			if (!mgr.doesRemoteEditProjectExist())
			{
				return;
			}
			
			IProject tempFiles = mgr.getRemoteEditProject();
			if (tempFiles != null)
			{
				try
				{
					IWorkspace workspace = SystemBasePlugin.getWorkspace();
					IResource[] members = tempFiles.members();
					if (members != null)
					{
						for (int i = 0; i < members.length; i++)
						{
							IResource member = members[i];
							if ((member instanceof IFile) && 
								member.getName().equals(".project"))
							{								
							}
							else
							{
								// DKM - passing true now so that out-of-synch temp files are deleted too (i.e. generated .evt files)
								// this solves the worse part of 58951
								member.delete(true, monitor);
							}
						}
					}

				}
				catch (Exception e)
				{
				}
				finally
				{
				    mgr.getRemoteEditProject();
				    /*
					try
					{
					    

						// recreate .project
						IProjectDescription description = tempFiles.getDescription();
						String[] natures = description.getNatureIds();
						String[] newNatures = new String[natures.length + 1];

						// copy all previous natures
						for (int i = 0; i < natures.length; i++)
						{
							newNatures[i] = natures[i];
						}

						newNatures[newNatures.length - 1] = SystemRemoteEditManager.REMOTE_EDIT_PROJECT_NATURE_ID;
						description.setNatureIds(newNatures);
						tempFiles.setDescription(description, null);
						
						mgr.addCSupport(tempFiles);
						mgr.addJavaSupport(tempFiles);
						
					 
					}
					catch (CoreException e)
					{
					}
					*/
				}

			}
		}
	}

	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistry().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}
		else
		{
			// for other cases, use statusbar
			IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
			if (win != null)
			{
				Shell winShell = SystemBasePlugin.getActiveWorkbenchShell();
				if (winShell != null && winShell.isVisible() && !winShell.isDisposed())
				{
					shell = winShell;
					return win;
				}
				else
				{
					win = null;
				}
			}

			return new ProgressMonitorDialog(shell);
		}
	}
	public void handleEvent(Event e)
	{
		if (e.widget == _clearButton)
		{

			if (checkDirtyEditors())
			{
				IRunnableContext runnableContext = getRunnableContext(SystemBasePlugin.getActiveWorkbenchShell());
				try
				{
					// currently we don't run this in a thread because
					//  in some cases dialogs are launched in the operation
					//  (widgets can only be legally used on the main thread)
					runnableContext.run(false, true, new ClearTempFilesRunnable());
					// inthread, cancellable, IRunnableWithProgress	
				}
				catch (java.lang.reflect.InvocationTargetException exc)
				{
				}
				catch (java.lang.InterruptedException ex)
				{
				}
			}

		}
		else if (e.widget == _maxCacheCheckbox)
		{
			_maxCacheSize.setEnabled(_maxCacheCheckbox.getSelection());
		}
	}

	protected boolean getDirtyReplicas(IContainer parent, List dirtyReplicas)
	{
		try
		{
			IResource[] children = parent.members();
			for (int i = 0; i < children.length; i++)
			{
				IResource child = children[i];
				if (child instanceof IFile)
				{
					SystemIFileProperties properties = new SystemIFileProperties(child);
					if (properties.getDirty())
					{
						if (properties.getRemoteFileObject() != null)
						{
							ISystemEditableRemoteObject editable =
								(ISystemEditableRemoteObject) properties.getRemoteFileObject();
							dirtyReplicas.add(editable);
						}
						// get the modified timestamp from the File, not the IFile
						// for some reason, the modified timestamp from the IFile does not always return
						// the right value. There is a Javadoc comment saying the value from IFile might be a
						// cached value and that might be the cause of the problem.
						else if (properties.getDownloadFileTimeStamp() != child.getLocation().toFile().lastModified())
						{
							String ssString = properties.getRemoteFileSubSystem();
							ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
							ISubSystem subsystem = registry.getSubSystem(ssString);
							if (subsystem != null)
							{
								String path = properties.getRemoteFilePath();
								try
								{
									IAdaptable remoteFile = (IAdaptable) subsystem.getObjectWithAbsoluteName(path);
									if (remoteFile != null)
									{
										ISystemRemoteElementAdapter adapter =
											(ISystemRemoteElementAdapter) remoteFile.getAdapter(
												ISystemRemoteElementAdapter.class);
										ISystemEditableRemoteObject editable =
											adapter.getEditableRemoteObject(remoteFile);
										editable.openEditor();
										// need this to get a reference back to the object
										properties.setRemoteFileObject(editable);
										dirtyReplicas.add(editable);
									}
									else
									{
										return false;
									}
								}
								catch (Exception e)
								{
									return false;
								}
							}
						}
					}
				}
				else if (child instanceof IContainer)
				{
					if (!getDirtyReplicas((IContainer) child, dirtyReplicas))
					{
						return false;
					}
				}
			}
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	protected boolean getDirtyReplicas(List results)
	{
		SystemRemoteEditManager mgr = SystemRemoteEditManager.getDefault();
		IProject tempFilesProject = mgr.getRemoteEditProject();
		if (!getDirtyReplicas(tempFilesProject, results))
		{
			return false;
		}
		if (!getDirtyEditors(results))
		{
			return false;
		}
		return true;
	}

	protected boolean getDirtyEditors(List results)
	{
		SystemRemoteEditManager editMgr = SystemRemoteEditManager.getDefault();
		
		// if there's no temp file project, there's no dirty editors
		if (!editMgr.doesRemoteEditProjectExist())
			return false;
			
		IProject tempFilesProject = editMgr.getRemoteEditProject();
		IWorkbenchWindow activeWindow = SystemBasePlugin.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		IEditorReference[] activeReferences = activePage.getEditorReferences();

		IEditorPart part;

		for (int k = 0; k < activeReferences.length; k++)
		{
			part = activeReferences[k].getEditor(false);

			if (part != null)
			{
				IEditorInput editorInput = part.getEditorInput();

				if (editorInput instanceof IFileEditorInput)
				{
					IFile file = ((IFileEditorInput) editorInput).getFile();
					if (file.getProject() == tempFilesProject)
					{
						if (part.isDirty())
						{
							SystemIFileProperties properties = new SystemIFileProperties(file);
							if (properties.getDirty())
							{
								// then this is already added via getDirtyReplicas()
							}
							else
							{

								if (properties.getRemoteFileObject() != null)
								{
									ISystemEditableRemoteObject editable =
										(ISystemEditableRemoteObject) properties.getRemoteFileObject();
									results.add(editable);
								}
								else
								{
									String ssString = properties.getRemoteFileSubSystem();
									ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
									ISubSystem subsystem = registry.getSubSystem(ssString);
									if (subsystem != null)
									{
										String path = properties.getRemoteFilePath();
										try
										{

											IAdaptable remoteFile =
												(IAdaptable) subsystem.getObjectWithAbsoluteName(path);
											if (remoteFile != null)
											{
												ISystemRemoteElementAdapter adapter =
													(ISystemRemoteElementAdapter) remoteFile.getAdapter(
														ISystemRemoteElementAdapter.class);
												ISystemEditableRemoteObject editable =
													adapter.getEditableRemoteObject(remoteFile);
												editable.openEditor();
												// need this to get a reference back to the object
												properties.setRemoteFileObject(editable);
												results.add(editable);
											}
										}
										catch (Exception e)
										{
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	protected ISystemEditableRemoteObject getEditableFor(IAdaptable selected)
	{
		ISystemRemoteElementAdapter adapter =
			(ISystemRemoteElementAdapter) ((IAdaptable) selected).getAdapter(ISystemRemoteElementAdapter.class);
		if (adapter.canEdit(selected))
		{
			ISystemEditableRemoteObject editable = adapter.getEditableRemoteObject(selected);
			try
			{
				editable.setLocalResourceProperties();
			}
			catch (Exception e)
			{
			}
			return editable;
		}
		return null;
	}

	protected boolean checkDirtyEditors()
	{
		SystemRemoteEditManager mgr = SystemRemoteEditManager.getDefault();
		if (!mgr.doesRemoteEditProjectExist())
		{
			return true;
		}
		
		List dirtyEditors = new ArrayList();
		if (!getDirtyReplicas(dirtyEditors))
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CACHE_UNABLE_TO_SYNCH);
			SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
			dlg.open();

			return false;
		}
		if (dirtyEditors.size() > 0)
		{
			AdaptableList input = new AdaptableList();
			for (int i = 0; i < dirtyEditors.size(); i++)
			{
				ISystemEditableRemoteObject rmtObj = (ISystemEditableRemoteObject) dirtyEditors.get(i);
				input.add(rmtObj.getRemoteObject());
			}

			WorkbenchContentProvider cprovider = new WorkbenchContentProvider();
			SystemTableViewProvider lprovider = new SystemTableViewProvider();

			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CACHE_UPLOAD_BEFORE_DELETE);

			ListSelectionDialog dlg =
				new ListSelectionDialog(getShell(), input, cprovider, lprovider, msg.getLevelOneText());

			dlg.setInitialSelections(input.getChildren());
			// TODO: Cannot use WorkbenchMessages -- it's internal
			dlg.setTitle(GenericMessages.EditorManager_saveResourcesTitle);

			int result = dlg.open();

			//Just return false to prevent the operation continuing
			if (result == IDialogConstants.CANCEL_ID)
				return false;

			Object[] filesToSave = dlg.getResult();
			for (int s = 0; s < filesToSave.length; s++)
			{
				IAdaptable rmtObj = (IAdaptable) filesToSave[s];
				ISystemEditableRemoteObject editable = getEditableFor(rmtObj);
				if (!editable.doImmediateSaveAndUpload())
				{
					return false;
				}
			}
		}
		return true;
	}

}