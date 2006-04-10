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

package org.eclipse.rse.files.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.ISaveAsDialog;
import org.eclipse.rse.files.ui.resources.SaveAsDialog;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
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

	/**
	 * This is the default dialog used to handle upload conflicts
	 */
    private class UploadConflictDialog extends SystemPromptDialog implements ISystemMessages, SelectionListener
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

  	  /**
	    * Constructor.
	    * @param shell the parent shell of the dialog
	    */
        public UploadConflictDialog(Shell shell)
        {
            super(shell, FileResources.RESID_CONFLICT_SAVE_TITLE);
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
            m.setLayoutData(new GridData(GridData.FILL_BOTH));

            Label label = new Label(m, 0);
            image.setBackground(label.getBackground());
            label.setImage(image);
            label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));

            Text text = new Text(m, SWT.WRAP | SWT.MULTI);
            text.setEditable(false);
            text.setText(FileResources.RESID_CONFLICT_SAVE_MESSAGE);

            GridData textData = new GridData(GridData.FILL_BOTH);
            textData.widthHint = 100;
            textData.horizontalSpan = 2;
            textData.verticalSpan = 5;

            Composite options = new Composite(c, SWT.NONE);
            GridLayout olayout = new GridLayout();
            olayout.numColumns = 1;
            options.setLayout(olayout);
            options.setLayoutData(new GridData(GridData.FILL_BOTH));

            _overwriteLocalButton = new Button(options, SWT.RADIO);
            _overwriteLocalButton.setText(FileResources.RESID_CONFLICT_SAVE_REPLACELOCAL);
            _overwriteLocalButton.addSelectionListener(this);
            _overwriteLocalButton.setSelection(true);

            _overwriteRemoteButton = new Button(options, SWT.RADIO);
            _overwriteRemoteButton.setText(FileResources.RESID_CONFLICT_SAVE_OVERWRITEREMOTE);
            _overwriteRemoteButton.addSelectionListener(this);

            _saveasButton = new Button(options, SWT.RADIO);
            _saveasButton.setText(FileResources.RESID_CONFLICT_SAVE_SAVETODIFFERENT);
            _saveasButton.addSelectionListener(this);

            Composite s = new Composite(options, SWT.NONE);
            GridLayout slayout = new GridLayout();
            slayout.numColumns = 2;
            s.setLayout(slayout);
            s.setLayoutData(new GridData(GridData.FILL_BOTH));

            _saveasFileEntry = new Text(s, SWT.BORDER);
            _saveasFileEntry.setEnabled(false);

            GridData fileEntryData = new GridData(GridData.FILL_BOTH);
            fileEntryData.widthHint = 100;
            _saveasFileEntry.setLayoutData(fileEntryData);
            _saveasFileEntry.setEditable(false);

            _browseButton = new Button(s, SWT.PUSH);
            _browseButton.setText(SystemResources.BUTTON_BROWSE);
            _browseButton.addSelectionListener(this);
            _browseButton.setEnabled(false);

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
                    _errorMessage = SystemPlugin.getPluginMessage(MSG_VALIDATE_PATH_EMPTY);
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
            	
                ISaveAsDialog dlg = SaveAsDialog.getSaveAsDialog(getShell());
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
            setHelp(SystemPlugin.HELPPREFIX + "scdl0000");
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
        private IFile _tempFile;
        private IRemoteFile _saveasFile;

        public ReopenAction(IFile tempFile, IRemoteFile saveasFile)
        {
            _tempFile = tempFile;
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
                _saveasFile = _saveasFile.getParentRemoteFileSubSystem().getRemoteFileObject(_saveasFile.getAbsolutePath());
                SystemPlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(_saveasFile.getParentRemoteFile(), ISystemResourceChangeEvents.EVENT_REFRESH, null));
                        
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
                SystemEditableRemoteFile edit = new SystemEditableRemoteFile(_saveasFile, id);
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

        UploadConflictDialog cnfDialog = new UploadConflictDialog(SystemBasePlugin.getActiveWorkbenchShell());
        if (cnfDialog.open() == Window.OK)
        {
            IRemoteFileSubSystem fs = _remoteFile.getParentRemoteFileSubSystem();

            // does user want to open local or replace local with remote?
            if (cnfDialog.getOverwriteRemote())
            {
                // user wants to keep the local version
                // and user wants to overwrite the remote file with pending changes
                try
                {
                    fs.uploadUTF8(_tempFile, _remoteFile, null);
                    _remoteFile.markStale(true);
                    _remoteFile = fs.getRemoteFileObject(_remoteFile.getAbsolutePath());
                    properties.setRemoteFileTimeStamp(_remoteFile.getLastModified());
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
            }
            else if (cnfDialog.getOverwriteLocal())
            {
                // user wants to replace local copy with the remote version
                try
                {     	
                    // download remote version
                    fs.downloadUTF8(_remoteFile, _tempFile, null);

                    properties.setRemoteFileTimeStamp(_remoteFile.getLastModified());
					//properties.setRemoteFileTimeStamp(-1);                
                    
                    properties.setDirty(false);
                    properties.setUsedBinaryTransfer(_remoteFile.isBinary());
                        }
                catch (RemoteFileSecurityException e)
                {
                    e.printStackTrace();
                }
                catch (RemoteFileIOException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (cnfDialog.getSaveas())
            {
                IRemoteFile remoteFile = cnfDialog.getSaveasLocation();
                if (remoteFile != null)
                {
                    try
                    {
                    	fs = remoteFile.getParentRemoteFileSubSystem();
                        if (!remoteFile.exists())
                        {
                            fs.createFile(remoteFile);
                        }                        
                    }
                    catch (SystemMessageException e)
                    {
                        SystemBasePlugin.logError("Error in performSaveAs", e);
                        SystemMessage message = SystemPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
                        SystemMessageDialog dialog = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), message);
                        dialog.open();
                        return;
                    }

                    try
                    {
                        // copy temp file to remote system
                        fs.uploadUTF8(_tempFile, remoteFile, null);
                     
                        // set original time stamp to 0 so that file will be overwritten next download
                        properties.setRemoteFileTimeStamp(0);
                        properties.setDirty(false);
                    }
                    catch (SystemMessageException e)
                    {
                        //e.printStackTrace();
                    }

                    ReopenAction reopen = new ReopenAction(_tempFile, remoteFile);

                    Display.getDefault().asyncExec(reopen);
                }
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