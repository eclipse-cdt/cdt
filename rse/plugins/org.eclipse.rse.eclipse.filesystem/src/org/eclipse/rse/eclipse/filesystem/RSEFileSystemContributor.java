package org.eclipse.rse.eclipse.filesystem;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

public class RSEFileSystemContributor extends FileSystemContributor {


	public URI browseFileSystem(String initialPath, Shell shell) 
	{
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell, "Select Folder");
		
		
		//SystemSelectRemoteFileOrFolderDialog dlg = new SystemSelectRemoteFileOrFolderDialog(shell, "Select File", false);
		/*
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog
				.setMessage(IDEWorkbenchMessages.ProjectLocationSelectionDialog_directoryLabel);
	*/
		if (!initialPath.equals(IDEResourceInfoUtils.EMPTY_STRING)) 
		{
			try
			{
			URI uri = new URI(initialPath);
			IHost host = RSEFileSystem.getConnectionFor(uri.getHost());
			IRemoteFileSubSystem fs = RSEFileSystem.getRemoteFileSubSystem(host);
			dlg.setInputObject(fs.getRemoteFileObject(uri.getPath()));			
			}
			catch (Exception e)
			{
				
			}
		}

		dlg.setNeedsProgressMonitor(true);

	/*
		String selectedDirectory = dialog.open();
		if (selectedDirectory == null) {
			return null;
		}
		return new File(selectedDirectory).toURI();
		*/
		if (dlg.open() == dlg.OK)
		{
			Object selected = dlg.getSelectedObject();
			if (selected instanceof ISystemFilterReference)
			{
				ISubSystem targetSubSystem = ((ISystemFilterReference)selected).getSubSystem();
				ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
				if (factory.supportsDropInFilters())
				{											        
					selected = targetSubSystem.getTargetForFilter((ISystemFilterReference)selected);										            
				}
			}
			IRemoteFile file = (IRemoteFile)selected;
			String path = file.getAbsolutePath();
			IHost host = dlg.getSelectedConnection();
			String hostName = host.getHostName();
			try
			{
				return new URI("rse", hostName, path, null);
			}
			catch (Exception e)
			{
				
			}
		}
		return null;

	}

	public URI getURI(String string){
		try
		{
			return new URI(string);
		}
		catch (Exception e)
		{			
		}
		return null;
	}
}
