/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * Xuan Chen        (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David McKnight   (IBM)        - [235221] Files truncated on exit of Eclipse
 * David McKnight   (IBM)        - [249544] Save conflict dialog appears when saving files in the editor
 * Kevin Doyle		(IBM)		 - [242389] [usability] RSE Save Conflict dialog should indicate which file is in conflict
 * David McKnight   (IBM)        - [267247] Wrong encoding
 * David McKnight   (IBM)        - [330804] Change the default selection of Save Conflict dialog
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.core.subsystems.SubSystem.SystemMessageDialogRunnable;
import org.eclipse.rse.files.ui.dialogs.FileDialogFactory;
import org.eclipse.rse.files.ui.dialogs.ISaveAsDialog;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;


/**
 * This is the default action used to handle upload conflicts
 */
public class SystemUploadConflictAction extends SystemBaseAction implements Runnable
{
	private class BackgroundSaveasJob extends Job
	{
		private IRemoteFile _saveasFile;
		public BackgroundSaveasJob(IRemoteFile saveasFile)
		{
			super("Save as"); // need to externalize //$NON-NLS-1$
			_saveasFile = saveasFile;
		}
		
		public IStatus run(IProgressMonitor monitor)
		{
			if (_saveasFile != null)
            {
				IRemoteFileSubSystem fs = _saveasFile.getParentRemoteFileSubSystem();
                try
                {                	
                    if (!_saveasFile.exists())
                    {
                        _saveasFile = fs.createFile(_saveasFile, monitor);
                    }                        
                }
                catch (SystemMessageException e)
                {
                    SystemBasePlugin.logError("Error in performSaveAs", e); //$NON-NLS-1$

                    SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID, 
                    		ICommonMessageIds.MSG_ERROR_UNEXPECTED,
                    		IStatus.ERROR, CommonMessages.MSG_ERROR_UNEXPECTED);
                    SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), message);
                    SystemMessageDialogRunnable runnable = ((SubSystem)fs).new SystemMessageDialogRunnable(dialog);
                    Display.getDefault().asyncExec(runnable);
                }
 
                try
                {                    	
            		String srcEncoding = RemoteFileUtility.getSourceEncoding(_tempFile);
                    // copy temp file to remote system
                    fs.upload(_tempFile.getLocation().makeAbsolute().toOSString(), _saveasFile, srcEncoding, monitor);
                 
                    // set original time stamp to 0 so that file will be overwritten next download
                    SystemIFileProperties properties = new SystemIFileProperties(_tempFile);
                    properties.setRemoteFileTimeStamp(0);
                    properties.setDirty(false);
                }
                catch (SystemMessageException e)
                {
                    //e.printStackTrace();
                }

                ReopenAction reopen = new ReopenAction(_tempFile, _saveasFile);

                Display.getDefault().asyncExec(reopen);
            }
			return Status.OK_STATUS;
		}
	}
	
	private class BackgroundDownloadJob extends Job
	{
		public BackgroundDownloadJob()
		{
			super("Download");	// need to externalize	 //$NON-NLS-1$
		}
		
		public IStatus run(IProgressMonitor monitor)
		{
			 try
             {     	
		        IRemoteFileSubSystem fs = _remoteFile.getParentRemoteFileSubSystem();
		        SystemIFileProperties properties = new SystemIFileProperties(_tempFile);
	            	
                // download remote version
		        String srcEncoding = RemoteFileUtility.getSourceEncoding(_tempFile);

                fs.download(_remoteFile, _tempFile.getLocation().makeAbsolute().toOSString(), srcEncoding, monitor);

                properties.setRemoteFileTimeStamp(_remoteFile.getLastModified());
					//properties.setRemoteFileTimeStamp(-1);                
                 
                 properties.setDirty(false);
                 properties.setUsedBinaryTransfer(_remoteFile.isBinary());
                     }
             catch (final SystemMessageException e)
             {
            	Display.getDefault().asyncExec(new Runnable() {
            		public void run() {
            			SystemMessageDialog.displayMessage(e);
            		}
            	});
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
             return Status.OK_STATUS;
		}
	}
	
	private class BackgroundUploadJob extends Job
	{
		
		public BackgroundUploadJob()
		{
			super("Upload"); // need to externalize //$NON-NLS-1$
		}
		
		public IStatus run(IProgressMonitor monitor)
		{
            // user wants to keep the local version
            // and user wants to overwrite the remote file with pending changes
            try
            {
            	IRemoteFileSubSystem fs = _remoteFile.getParentRemoteFileSubSystem();
            	SystemIFileProperties properties = new SystemIFileProperties(_tempFile);
            	
            	// making sure we have the same version as is in the cache
            	_remoteFile = fs.getRemoteFileObject(_remoteFile.getAbsolutePath(), monitor);
            	
        		String srcEncoding = RemoteFileUtility.getSourceEncoding(_tempFile);
        		
                fs.upload(_tempFile.getLocation().makeAbsolute().toOSString(), _remoteFile, srcEncoding, monitor);

                // wait for timestamp to update before re-fetching remote file
                _remoteFile.markStale(true);
                _remoteFile = fs.getRemoteFileObject(_remoteFile.getAbsolutePath(), monitor);

                long ts = _remoteFile.getLastModified();
                properties.setRemoteFileTimeStamp(ts);
                properties.setDirty(false);
            }
            catch (RemoteFileSecurityException e)
            {
            }
            catch (RemoteFileIOException e)
            {
            }
            catch (Exception e)
            {
            }
            return Status.OK_STATUS;
		}
	}
		

	/**
	 * This is the default dialog used to handle upload conflicts
	 */
    private class UploadConflictDialog extends SystemPromptDialog implements SelectionListener
    {
        private Button _overwriteLocalButton;
        private Button _overwriteRemoteButton;
        private Button _saveasButton;
        private Button _browseButton;
        private Text _saveasFileEntry;

        private boolean _overwriteLocal;
        private boolean _overwriteRemote;
        private boolean _saveas;

        private SystemMessage _errorMessage;

        private IRemoteFile _saveasLocation;
        private String _uploadFile;
        
  	  /**
	    * Constructor.
	    * @param shell the parent shell of the dialog
	    */
        public UploadConflictDialog(Shell shell, String file)
        {
            super(shell, FileResources.RESID_CONFLICT_SAVE_TITLE);
            _uploadFile = file;
            //pack();
        }

		/**
		 * Called when a button is pressed in the dialog
		 */
        protected void buttonPressed(int buttonId)
        {
            setReturnCode(buttonId);
            _overwriteLocal = _overwriteLocalButton.getSelection();
            _overwriteRemote = _overwriteRemoteButton.getSelection();
            _saveas = _saveasButton.getSelection();
            close();
        }

		/**
		 * Returns whether the user decided to overwrite the local file
		 * @return whether the user decided to overwrite the local file
		 */
        public boolean getOverwriteLocal()
        {
            return _overwriteLocal;
        }

		/**
		 * Returns whether the user decided to overwrite the remote file
		 * @return whether the user decided to overwrite the remote file
		 */
        public boolean getOverwriteRemote()
        {
            return _overwriteRemote;
        }

		/**
		 * Returns whether the user decided to save to a different location
		 * @return whether the user decided to save to a different location
		 */
        public boolean getSaveas()
        {
            return _saveas;
        }

		/**
		 * Returns the location where the cached file should be saved to
		 * @return the location where the cached file should be saved to
		 */
        public IRemoteFile getSaveasLocation()
        {
            return _saveasLocation;
        }

		/**
		 * Creates the dialog contents.
		 */
        public Control createInner(Composite parent)
        {
			Image image = getShell().getDisplay().getSystemImage(SWT.ICON_QUESTION);

            Composite c = new Composite(parent, SWT.NONE);

            GridLayout layout = new GridLayout();
            layout.numColumns = 1;
            c.setLayout(layout);
            c.setLayoutData(new GridData(GridData.FILL_BOTH));

            Composite m = new Composite(c, SWT.NONE);

            GridLayout mlayout = new GridLayout();
            mlayout.numColumns = 2;
            m.setLayout(mlayout);
            m.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            Label label = new Label(m, SWT.NONE);
            image.setBackground(label.getBackground());
            label.setImage(image);
            label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

            Text text = new Text(m, SWT.WRAP | SWT.MULTI);
            text.setEditable(false);
            text.setText(NLS.bind(FileResources.RESID_CONFLICT_SAVE_MESSAGE, _uploadFile));
            GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            text.setLayoutData(textData);

            Composite options = new Composite(c, SWT.NONE);
            GridLayout olayout = new GridLayout();
            olayout.numColumns = 1;
            options.setLayout(olayout);
            options.setLayoutData(new GridData(GridData.FILL_BOTH));

            _overwriteLocalButton = new Button(options, SWT.RADIO);
            _overwriteLocalButton.setText(FileResources.RESID_CONFLICT_SAVE_REPLACELOCAL);
            _overwriteLocalButton.addSelectionListener(this);
            

            _overwriteRemoteButton = new Button(options, SWT.RADIO);
            _overwriteRemoteButton.setText(FileResources.RESID_CONFLICT_SAVE_OVERWRITEREMOTE);
            _overwriteRemoteButton.addSelectionListener(this);

            _saveasButton = new Button(options, SWT.RADIO);
            _saveasButton.setText(FileResources.RESID_CONFLICT_SAVE_SAVETODIFFERENT);
            _saveasButton.addSelectionListener(this);
            _saveasButton.setSelection(true);

            Composite s = new Composite(options, SWT.NONE);
            GridLayout slayout = new GridLayout();
            slayout.numColumns = 2;
            s.setLayout(slayout);
            s.setLayoutData(new GridData(GridData.FILL_BOTH));

            _saveasFileEntry = new Text(s, SWT.BORDER);
            _saveasFileEntry.setEnabled(true);

            GridData fileEntryData = new GridData(GridData.FILL_BOTH);
            fileEntryData.widthHint = 100;
            _saveasFileEntry.setLayoutData(fileEntryData);
            _saveasFileEntry.setEditable(true);

            _browseButton = new Button(s, SWT.PUSH);
            _browseButton.setText(SystemResources.BUTTON_BROWSE);
            _browseButton.addSelectionListener(this);
            _browseButton.setEnabled(true);

            // since saveas is the new default, need to prompt dialog with error message
            enableOkButton(false);
            _errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,                     		
            		ISystemFileConstants.MSG_VALIDATE_PATH_EMPTY,
            		IStatus.ERROR,
            		FileResources.MSG_VALIDATE_PATH_EMPTY, 
            		FileResources.MSG_VALIDATE_PATH_EMPTY_DETAILS);

            setErrorMessage(_errorMessage);
            
            setHelp();

            return c;
        }

		/**
		 * Called when a widget is selected
		 */
        public void widgetSelected(SelectionEvent e)
        {
            if (_saveasButton.getSelection())
            {
                _browseButton.setEnabled(true);
                _saveasFileEntry.setEnabled(true);
                if (_saveasLocation != null)
                {
                	_errorMessage = null;
                	setErrorMessage(_errorMessage);
                    enableOkButton(true);
                }
                else
                {
                    enableOkButton(false);
                    _errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,                     		
                    		ISystemFileConstants.MSG_VALIDATE_PATH_EMPTY,
                    		IStatus.ERROR,
                    		FileResources.MSG_VALIDATE_PATH_EMPTY, 
                    		FileResources.MSG_VALIDATE_PATH_EMPTY_DETAILS);

                    setErrorMessage(_errorMessage);

                }
            }
            else
            {
                _browseButton.setEnabled(false);
                _saveasFileEntry.setEnabled(false);
                enableOkButton(true);
                _errorMessage = null;
                setErrorMessage(_errorMessage);
            }

            if (e.getSource() == _browseButton)
            {
            	
                ISaveAsDialog dlg = FileDialogFactory.makeSaveAsDialog(getShell());
                dlg.setMultipleSelectionMode(false);

                if (_remoteFile != null)
                {
                    dlg.setPreSelection(_remoteFile);

                    if (dlg.open() == OK)
                    {
                        Object output = dlg.getOutputObject();
                        if (output instanceof IRemoteFile)
                        {
                            IRemoteFile toCreate = (IRemoteFile) output;
                            // validate
                            try
                            {
                                _errorMessage = null;                               
                                _saveasLocation = toCreate;
                                _saveasFileEntry.setText(toCreate.getAbsolutePath());
                                
                                enableOkButton(_errorMessage == null);

                                setErrorMessage(_errorMessage);
                            }
                            catch (Exception ex)
                            {
                            }
                        }
                    }
                }
            }

        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
        }

		/**
		 * Returns the initial focus control
		 * @return the initial focus control
		 */
        protected Control getInitialFocusControl()
        {
            enableOkButton(true);
            return _overwriteLocalButton;
        }

        private void setHelp()
        {
            setHelp(RSEUIPlugin.HELPPREFIX + "scdl0000"); //$NON-NLS-1$
        }
    }

	/**
	 * Action used to close a specified editor
	 */
    public class CloseEditorAction implements Runnable
    {
        public IEditorPart _editor;

        public CloseEditorAction(IEditorPart editor)
        {
            _editor = editor;
        }

        public void run()
        {
            // close old editor
            SystemBasePlugin.getActiveWorkbenchWindow().getActivePage().closeEditor(_editor, false);
        }
    }

	/**
	 * Action used to reopen the editor for a cached file with a remote file from a different location
	 */
    public class ReopenAction implements Runnable
    {
        private IRemoteFile _saveasFile;

        public ReopenAction(IFile tempFile, IRemoteFile saveasFile)
        {
            _saveasFile = saveasFile;
        }

        private IEditorPart getEditorFor(IFile tempFile)
        {

            IWorkbenchWindow window = SystemBasePlugin.getActiveWorkbenchWindow();
            if (window != null)
            {
                IWorkbenchPage page = window.getActivePage();
                if (page != null)
                {
                    IEditorPart editor = page.getActiveEditor();
                    IEditorInput input = editor.getEditorInput();
                    if (input instanceof FileEditorInput)
                    {
                        FileEditorInput finput = (FileEditorInput) input;
                        if (finput.getFile().getFullPath().equals(tempFile.getFullPath()))
                        {
                            return editor;
                        }
                    }
                }
            }

            return null;
        }

        public void run()
        {
            try
            {
                _saveasFile = _saveasFile.getParentRemoteFileSubSystem().getRemoteFileObject(_saveasFile.getAbsolutePath(), new NullProgressMonitor());
                RSECorePlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(_saveasFile.getParentRemoteFile(), ISystemResourceChangeEvents.EVENT_REFRESH, null));
                        
            }
            catch (SystemMessageException e)
            {
            	e.printStackTrace();
            }

            // close editor			
            IEditorPart editor = getEditorFor(_tempFile);
            if (editor != null)
            {
                String id = editor.getEditorSite().getId();

                // open editor on new file
                SystemEditableRemoteFile edit = new SystemEditableRemoteFile(_saveasFile);
                try
                {
                    edit.download(getShell());
                    edit.addAsListener();
                    edit.setLocalResourceProperties();

                    // open new editor
                    edit.openEditor();

                    // close old editor
                    CloseEditorAction closeAction = new CloseEditorAction(editor);
                    Display.getDefault().asyncExec(closeAction);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private IFile _tempFile;
    private IRemoteFile _remoteFile;

	/**
	 * Constructor.
	 * @param shell the parent shell of the action
	 * @param tempFile the cached local file that is in conflict with the remote file
	 * @param remoteFile the remote file
	 * @param remoteNewer indicates whether the remote file has changed since it was last downloaded
	 */
    public SystemUploadConflictAction(Shell shell, IFile tempFile, IRemoteFile remoteFile, boolean remoteNewer)
    {
        super(FileResources.RESID_CONFLICT_SAVE_TITLE, shell);
        _tempFile = tempFile;
        _remoteFile = remoteFile;
    }

	/**
	 * Called when this action is invoked
	 */
    public void run()
    {
        SystemIFileProperties properties = new SystemIFileProperties(_tempFile);

        UploadConflictDialog cnfDialog = new UploadConflictDialog(SystemBasePlugin.getActiveWorkbenchShell(), _remoteFile.getName());
        if (cnfDialog.open() == Window.OK)
        {
            // does user want to open local or replace local with remote?
            if (cnfDialog.getOverwriteRemote())
            {
                // user wants to keep the local version
                // and user wants to overwrite the remote file with pending changes
            	BackgroundUploadJob ujob = new BackgroundUploadJob();
            	ujob.schedule();
            }
            else if (cnfDialog.getOverwriteLocal())
            {
                // user wants to replace local copy with the remote version
            	BackgroundDownloadJob djob = new BackgroundDownloadJob();
            	djob.schedule();
            }
            else if (cnfDialog.getSaveas())
            {
                IRemoteFile remoteFile = cnfDialog.getSaveasLocation();
                BackgroundSaveasJob sjob = new BackgroundSaveasJob(remoteFile);
                sjob.schedule();            
            }
        }
        else
        {
            // cancelled dialog, so no remote synchronization
            // set dirty flag!
            properties.setDirty(true);
        }
    }
}
