/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.ui.propertypages;

import org.eclipse.rse.files.ui.propertypages.SystemAbstractRemoteFilePropertyPageExtensionAction;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import samples.RSESamplesPlugin;

/**
 * A sample property page for a remote object, which in this case is scoped via the
 *  extension point xml to only apply to folder objects.
 */
public class FolderInfoPropertyPage
	extends SystemAbstractRemoteFilePropertyPageExtensionAction
	implements SelectionListener
{
	// gui widgets...
	private Label sizeLabel, filesLabel, foldersLabel;
	private Button stopButton;
	// state...
	private int totalSize = 0;
	private int totalFolders = 0;
	private int totalFiles = 0;
	private boolean stopped = false;
	private Thread workerThread;
	private Runnable guiUpdater;

	/**
	 * Constructor for FolderInfoPropertyPage.
	 */
	public FolderInfoPropertyPage()
	{
		super();
	}

	// --------------------------
	// Parent method overrides...
	// --------------------------
	

	/* (non-Javadoc)
	 * @see org.eclipse.rse.files.ui.propertypages.SystemAbstractRemoteFilePropertyPageExtensionAction#createContentArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		Composite composite = SystemWidgetHelpers.createComposite(parent, 2);
		// draw the gui		
		sizeLabel = SystemWidgetHelpers.createLabeledLabel(composite, 
				RSESamplesPlugin.getResourceString("pp.size.label"), //$NON-NLS-1$
				RSESamplesPlugin.getResourceString("pp.size.tooltip"), //$NON-NLS-1$
				false);
		filesLabel = SystemWidgetHelpers.createLabeledLabel(composite, 
				RSESamplesPlugin.getResourceString("pp.files.label"), //$NON-NLS-1$
				RSESamplesPlugin.getResourceString("pp.files.tooltip"), //$NON-NLS-1$
				false);
		foldersLabel = SystemWidgetHelpers.createLabeledLabel(composite, 
				RSESamplesPlugin.getResourceString("pp.folders.label"), //$NON-NLS-1$
				RSESamplesPlugin.getResourceString("pp.folders.tooltip"), //$NON-NLS-1$
				false);
		stopButton = SystemWidgetHelpers.createPushButton(composite, null, 
				RSESamplesPlugin.getResourceString("pp.stopButton.label"), //$NON-NLS-1$
				RSESamplesPlugin.getResourceString("pp.stopButton.tooltip") //$NON-NLS-1$
				);
		stopButton.addSelectionListener(this);
		
		setValid(false); // Disable OK button until thread is done
		
		// show "Processing..." message
		setMessage(RSESamplesPlugin.getPluginMessage("RSSG1002")); //$NON-NLS-1$
		
		// create instance of Runnable to allow asynchronous GUI updates from background thread	   
		guiUpdater = new RunnableGUIClass();
		// spawn a thread to calculate the information
		workerThread = new RunnableClass(getRemoteFile());
		workerThread.start();
		
		return composite;
	}
	
	/**
	 * Intercept from PreferencePage. Called when user presses Cancel button.
	 * We stop the background thread.
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
	public boolean performCancel() 
	{
		killThread();
		return true;
	}			
	
	/**
	 * Intercept from DialogPage. Called when dialog going away.
	 * If the user presses the X to close this dialog, we 
	 *  need to stop that background thread.
	 */
	public void dispose()
	{
		killThread();
		super.dispose();
	}
	
	/**
	 * Private method to kill our background thread.
	 * Control doesn't return until it ends.
	 */
	private void killThread()
	{
		if (!stopped && workerThread.isAlive())
		{
		    stopped = true;
		    try {
		      workerThread.join(); // wait for thread to end
		    } catch (InterruptedException exc) {}
		}		
	}

	// -------------------------------------------
	// Methods from SelectionListener interface...
	// -------------------------------------------
	
	/**
	 * From SelectionListener
	 * @see SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) 
	{
		if (event.getSource() == stopButton)
		{
			stopped = true;
			stopButton.setEnabled(false);
		}
	}
	/**
	 * From SelectionListener
	 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent event)
	{
	}

	// ----------------
	// Inner classes...
	// ----------------
	/**
	 * Inner class encapsulating the background work to be done, so it may be executed
	 *  in background thread.
	 */
	private class RunnableClass extends Thread
	{
		IRemoteFile inputFolder;
		
		RunnableClass(IRemoteFile inputFolder)
		{
			this.inputFolder = inputFolder;
		}
		
		public void run()
		{
			if (stopped)
			  return;
			walkFolder(inputFolder);						
			updateGUI();
			if (!stopped)
			{
				stopped = true;
				updateGUI();
			}
		}
		
		/**
		 * Recursively walk a folder, updating the running tallies. 
		 * Update the GUI after processing each subfolder.
		 */
		private void walkFolder(IRemoteFile currFolder)
		{
			IRemoteFile[] folders = currFolder.getParentRemoteFileSubSystem().listFoldersAndFiles(currFolder);
			if ((folders != null) && (folders.length>0))
			{
				for (int idx=0; !stopped && (idx<folders.length); idx++)
				{
					// is this a folder? 
					if (folders[idx].isDirectory())
					{
						++totalFolders;
						walkFolder(folders[idx]);
						updateGUI();
					}
					// is this a file?
					else
					{
						++totalFiles;
						totalSize += folders[idx].getLength();
					}
				}
			}
		} // end of walkFolder method

	} // end of inner class
	
	/**
	 * Inner class encapsulating the GUI work to be done from the
	 *  background thread.
	 */
	private class RunnableGUIClass implements Runnable
	{
		public void run()
		{
			if (stopButton.isDisposed())
			  return; 
			if (!stopped)
			{
				sizeLabel.setText(Integer.toString(totalSize));		
				filesLabel.setText(Integer.toString(totalFiles));
				foldersLabel.setText(Integer.toString(totalFolders));
			}
			else if (stopped)
			{				
				setValid(true); // re-enable OK button								
				stopButton.setEnabled(false); // disable Stop button				
				clearMessage(); // clear "Processing..." message
			}			  
		}
	}	

	
	/**
	 * Update the GUI with the current status
	 */
	private void updateGUI()
	{
		Display.getDefault().asyncExec(guiUpdater);
	}


}
