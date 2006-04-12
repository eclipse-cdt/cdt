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
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * This is the default action used to handle download conflicts
 */
public class SystemDownloadConflictAction extends SystemBaseAction implements Runnable
{

	/**
	 * This is the default dialog used to handle download conflicts
	 */
	protected class DownloadConflictDialog extends SystemPromptDialog implements ISystemMessages
	{
		private Button _keepLocalButton;
		private Button _replaceLocalButton;

		private boolean _keepLocal;
		private String _openLocalText;
		private String _replaceText;
		private String _dialogText;
		private String _helpId;

  	   /**
	    * Constructor.
	    * @param shell the parent shell of the dialog
	    * @param remoteNewer indicates whether the remote file has changed since it was last downloaded
	    */
		public DownloadConflictDialog(Shell shell, boolean remoteNewer)
		{
			super(shell, FileResources.RESID_CONFLICT_DOWNLOAD_TITLE);
		}

		/**
		 * Return whether the user decided to keep the local cached file
		 * @returns the whether the user decided to keep the local cached file
		 */
		public boolean keepLocal()
		{
			return _keepLocal;
		}

		/**
		 * Called when a button is pressed in the dialog
		 */
		protected void buttonPressed(int buttonId)
		{
			setReturnCode(buttonId);
			_keepLocal = _keepLocalButton.getSelection();
			close();
		}

		/**
		 * Creates the dialog content
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
			text.setText(_dialogText);

			Composite options = new Composite(c, SWT.NONE);
			GridLayout olayout = new GridLayout();
			olayout.numColumns = 1;
			options.setLayout(olayout);
			options.setLayoutData(new GridData(GridData.FILL_BOTH));

			_replaceLocalButton = new Button(options, SWT.RADIO);
			_replaceLocalButton.setText(_replaceText);
			_replaceLocalButton.setSelection(true);

			_keepLocalButton = new Button(options, SWT.RADIO);
			_keepLocalButton.setText(_openLocalText);

			setHelp();
			return c;
		}

		/**
		 * Returns the initial focus control
		 * @return the initial focus control
		 */
		protected Control getInitialFocusControl()
		{
			enableOkButton(true);
			return _replaceLocalButton;
		}

		private void setHelp()
		{
			setHelp(_helpId);			
		}

		/**
		 * Sets the help id for this dialog
		 * @param id the help id
		 */
		public void setHelpId(String id)
		{
			_helpId= id;	
		}

		/**
		 * Sets the dialog message
		 * @param dialogText the dialog message
		 */
		public void setDialogText(String dialogText)
		{
			_dialogText = dialogText;
		}

		/**
		 * Sets the dialog action message for replacing the local file with the remote file
		 * @param replaceText the dialog action message for replacing the local file
		 */
		public void setReplaceText(String replaceText)
		{
			_replaceText = replaceText;
		}

		/**
		 * Sets the dialog action message for keeping the local file
		 * @param openLocalText the dialog action message for keeping the local file
		 */
		public void setOpenLocalText(String openLocalText)
		{
			_openLocalText = openLocalText;
		}

	}

	protected IFile _tempFile;
	protected boolean _remoteNewer;
	protected int _state;

	public static final int REPLACE_WITH_REMOTE = 0;
	public static final int OPEN_WITH_LOCAL = 1;
	public static final int CANCELLED = 2;

	/**
	 * Constructor.
	 * @param tempFile the cached local file that is in conflict with the remote file
	 * @param remoteNewer indicates whether the remote file has changed since it was last downloaded
	 */
	public SystemDownloadConflictAction(IFile tempFile, boolean remoteNewer)
	{
		super(FileResources.RESID_CONFLICT_DOWNLOAD_TITLE, null);
		_tempFile = tempFile;
		_remoteNewer = remoteNewer;
	}
	
	/**
	 * Constructor.
	 * @param title the title for the action
	 * @param tempFile the cached local file that is in conflict with the remote file
	 * @param remoteNewer indicates whether the remote file has changed since it was last downloaded
	 */
	public SystemDownloadConflictAction(String title, IFile tempFile, boolean remoteNewer)
	{
		super(title, null);
		_tempFile = tempFile;
		_remoteNewer = remoteNewer;
	}

	/**
	 * Returns the action taken in response to the conflict.   The state may be one of the following:
	 * <ul>
	 *   <li> REPLACE_WITH_REMOTE
	 *   <li> OPEN_WITH_LOCAL
	 *   <li> CANCELLED
	 * </ul>
	 * @return the response to the conflict
	 */
	public int getState()
	{
		return _state;
	}

	/**
	 * Returns the dialog used to prompt a user on how to resolve a conflict.
	 * @returns the dialog
	 */
	protected DownloadConflictDialog getConflictDialog()
	{
		DownloadConflictDialog dlg = new DownloadConflictDialog(SystemBasePlugin.getActiveWorkbenchShell(), _remoteNewer);

		if (_remoteNewer)
		{
			dlg.setDialogText(FileResources.RESID_CONFLICT_DOWNLOAD_MESSAGE_REMOTECHANGED);
		}
		else
		{
			dlg.setDialogText(FileResources.RESID_CONFLICT_DOWNLOAD_MESSAGE_LOCALCHANGED);
		}

		dlg.setReplaceText(FileResources.RESID_CONFLICT_DOWNLOAD_REPLACELOCAL);
		dlg.setOpenLocalText(FileResources.RESID_CONFLICT_DOWNLOAD_OPENWITHLOCAL);
		dlg.setHelpId(RSEUIPlugin.HELPPREFIX + "lcdl0000");
		return dlg;
	}

	/**
	 * Called when this action is invoked
	 */
	public void run()
	{
		setShell(RSEUIPlugin.getTheSystemRegistry().getShell());
		SystemIFileProperties properties = new SystemIFileProperties(_tempFile);

		DownloadConflictDialog cnfDialog = getConflictDialog();
		if (cnfDialog.open() == Window.OK)
		{
			// does user want to open local or replace local with remote?
			if (cnfDialog.keepLocal())
			{
				// user wants to keep the local version
				// don't synchronize with server, save that for the save operation  
				_state = OPEN_WITH_LOCAL;
			}
			else
			{
				// user wants to replace local copy with the remote version
				_state = REPLACE_WITH_REMOTE;
			}

		}
		else
		{
			_state = CANCELLED;

			// cancelled dialog, so no remote synchronization
			// set dirty flag!
			properties.setDirty(true);
		}
	}
}