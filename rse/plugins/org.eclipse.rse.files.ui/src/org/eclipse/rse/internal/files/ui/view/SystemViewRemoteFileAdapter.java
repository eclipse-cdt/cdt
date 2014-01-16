/*******************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186997] No deferred queries in Local Files
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * Xuan Chen        (IBM)        - [180671] [refresh] It is not possible to refresh editor with double clicking on it
 * David Dykstal (IBM) - [160776] format file size according to client system conventions and locale
 * David McKnight   (IBM)        - [197089] Need to set the filter when there is no separator in filter string
 * David McKnight   (IBM)        - [196662] hasChildren() should return false when the file doesn't exist
 * David McKnight   (IBM)        - [197784] Need to check if last separator is at 0
 * Kevin Doyle  (IBM)            - [198576] Renaming a folder directly under a Filter doesn't update children
 * David McKnight   (IBM)        - [199568] Removing synchronized from internalGetChildren
 * Kevin Doyle (IBM) 			 - [197855] Can't Delete/Rename/Move a Read-Only File
 * Xuan Chen (IBM)        - [202949] [archives] copy a folder from one connection to an archive file in a different connection does not work
 * Kevin Doyle 		(IBM)		 - [204810] Saving file in Eclipse does not update remote file
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Kevin Doyle      (IBM)		 - [186125] Changing encoding of a file is not reflected when it was opened before
 * David McKnight   (IBM)        - [208803] add exists() method
 * David McKnight   (IBM)        - [209375] download using copyRemoteResourcesToWorkspaceMultiple
 * Rupen Mardirossian (IBM)		 - [208435] added constructor to nested RenameRunnable class to take in names that are previously used as a parameter for multiple renaming instances
 * David McKnight   (IBM)        - [209660] need to check if remote encoding has changed before using cached file
 * Xuan Chen        (IBM)        - [160775] [api] [breaking] [nl] rename (at least within a zip) blocks UI thread
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 * Xuan Chen        (IBM)        - [191370] [dstore] Supertransfer zip not deleted when cancelling copy
 * David McKnight   (IBM)        - [189873] DownloadJob changed to DownloadAndOpenJob
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Xuan Chen        (IBM) - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * Rupen Mardirossian (IBM)      - [210682] Copy collisions will use SystemCopyDialog now instead of renameDialog when there is a copy collision within the same connection
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * Rupen Mardirossian (IBM)      - [198728] Folder being copied across systems is added to original set of files in order to extract empty (sub)folders in doDrop method
 * David McKnight     (IBM)      - [229610] [api] File transfers should use workspace text file encoding
 * Rupen Mardirossian (IBM)      - [227213] Copy and pasting to the parent folder will create a "Copy of" that resource
 * David Dykstal (IBM) [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * Anna Dushistova  (MontaVista) - [226550] [api] Launch Shell and Launch Terminal actions should be contributed declaratively
 * Martin Oberhuber (Wind River) - [234215] improve API documentation for doDelete and doDeleteBatch
 * David McKnight     (IBM)      - [251860] Rename a file/folder to a hidden file causes problems
 * David McKnight   (IBM)        - [261019] New File/Folder actions available in Work Offline mode
 * David McKnight   (IBM)        - [254769] Don't get latest file when opening a file always
 * David McKnight   (IBM)        - [264607] Unable to delete a broken symlink
 * David McKnight   (IBM)        - [276103] Files with names in different cases are not handled properly
 * David McKnight     (IBM)      - [276534] Cache Conflict After Synchronization when Browsing Remote System with Case-Differentiated-Only Filenames
 * David McKnight   (IBM)        - [280466] File download keeps running in case sensitive case
 * David McKnight   (IBM)        - [309813] RSE permits opening of file after access removed
 * David McKnight   (IBM)        - [308221] Bidi3.6: Improper display of date in Properties and Table Views
 * David McKnight   (IBM)        - [317541] Show blank as the last modified for a file with no last modified
 * David McKnight   (IBM)        - [323299] [files] remote file view adapter needs to use the latest version of IRemoteFile
 * David McKnight   (IBM)        - [324192] Cannot open a renamed file
 * David McKnight     (IBM)      - [228743] [usability][dnd] Paste into read-only folder fails silently
 * David McKnight   (IBM)        - [284157] [performance] too many jobs kicked off for getting file permissions for table
 * David McKnight   (IBM)        - [330398] RSE leaks SWT resources
 * David McKnight   (IBM)        - [215814] [performance] Duplicate Queries between Table and Remote Systems View
 * David McKnight   (IBM)        - [249031] Last used editor should be set to SystemEditableRemoteFile
 * David McKnight   (IBM)        - [341244] folder selection input to unlocked Remote Systems Details view sometimes fails
 * David McKnight   (IBM)        - [363490] PHP files opening in system editor (Dreamweaver)
 * Rick Sawyer      (IBM)        - [376535] RSE does not respect editor overrides
 * David McKnight   (IBM)        - [389838] Fast folder transfer does not account for code page
 * David Mcknight   (IBM)        - [374681] Incorrect number of children on the properties page of a directory
 * Samuel Wu        (IBM)        - [398988] [ftp] FTP Only support to zVM
 * Xuan Chen        (IBM)        - [399101] RSE edit actions on local files that map to actually workspace resources should not use temp files
 * David McKnight   (IBM)        - [420798] Slow performances in RDz 9.0 with opening 7000 files located on a network driver.
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.view;
import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.files.ui.actions.SystemCompareFilesAction;
import org.eclipse.rse.internal.files.ui.actions.SystemCompareWithEditionAction;
import org.eclipse.rse.internal.files.ui.actions.SystemEditFilesAction;
import org.eclipse.rse.internal.files.ui.actions.SystemMoveRemoteFileAction;
import org.eclipse.rse.internal.files.ui.actions.SystemNewFileAction;
import org.eclipse.rse.internal.files.ui.actions.SystemNewFileFilterFromFolderAction;
import org.eclipse.rse.internal.files.ui.actions.SystemNewFolderAction;
import org.eclipse.rse.internal.files.ui.actions.SystemRemoteFileOpenWithMenu;
import org.eclipse.rse.internal.files.ui.actions.SystemReplaceWithEditionAction;
import org.eclipse.rse.internal.files.ui.actions.SystemSearchAction;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.dialogs.CopyRunnable;
import org.eclipse.rse.internal.ui.view.ISystemMementoConstants;
import org.eclipse.rse.internal.ui.view.SystemDNDTransferRunnable;
import org.eclipse.rse.internal.ui.view.SystemViewResources;
import org.eclipse.rse.internal.ui.view.search.SystemSearchTableView;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.StringCompare;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;
import org.eclipse.rse.services.files.PendingHostFilePermissions;
import org.eclipse.rse.services.search.HostSearchResultSet;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileRemoteTypes;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileEmpty;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileRoot;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultsContentsType;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileUniqueName;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.operations.SystemFetchOperation;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewDropDestination;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;


/**
 * Adapter for displaying remote file system objects in tree views.
 * These are children of RemoteFileSubSystem filter strings
 */
public class SystemViewRemoteFileAdapter
	extends AbstractSystemViewAdapter
 implements ISystemRemoteElementAdapter
{

	private String xlatedSize = null;
	private String xlatedCompressedSize = null;
	private String xlatedExpandedSize = null;

	private static final Object[] EMPTY_LIST = new Object[0];
	private boolean filesOnly, foldersOnly;
	private SystemNewFileAction addNewFile;
	private SystemNewFolderAction addNewFolder;
	private SystemNewFileFilterFromFolderAction addNewFilter;

	private SystemMoveRemoteFileAction moveAction;

	private SystemCopyToClipboardAction copyClipboardAction;
	private SystemPasteFromClipboardAction pasteClipboardAction;

	private SystemCompareFilesAction compareFilesAction;
	private SystemCompareWithEditionAction compareEditionAction;
	private SystemReplaceWithEditionAction replaceEditionAction;
	// FIXME shells now separate plugin
	//private SystemCommandAction commandAction, shellAction;

	private SystemSearchAction searchAction;

	private IEditorRegistry registry;
	private SystemRemoteFileOpenWithMenu openWithMenu;

	private boolean debug = false; //true;

	// -------------------
	// property descriptors
	// -------------------
	private static PropertyDescriptor[] propertyDescriptorArray = null;
	private static PropertyDescriptor[] briefPropertyDescriptorArray = null;

	// DKM
	private static PropertyDescriptor[] uniquePropertyDescriptorArray = null;
	private static PropertyDescriptor[] uniqueArchiveDescriptorArray = null;
	private static PropertyDescriptor[] uniqueVirtualDescriptorArray = null;

	// MJB
	private static PropertyDescriptor[] archiveDescriptorArray = null;
	private static PropertyDescriptor[] virtualDescriptorArray = null;

	static final String _uploadMessage = FileResources.MSG_UPLOADING_PROGRESS;
	static final String _downloadMessage = FileResources.MSG_DOWNLOADING_PROGRESS;

	public HashMap _permissionsJobMap = new HashMap();
	
	class MultiFetchPermissionsJob extends Job {
		private List _files;
		private IFilePermissionsService _service;
		private boolean _started = false;
		
		public MultiFetchPermissionsJob(IFilePermissionsService service){						
			super(RSECoreMessages.RSESubSystemOperation_Get_properties_message); 
			_files = new ArrayList();
			_service = service;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			final ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			IRemoteFile[] files = null;
			_started = true;
			synchronized (_files){
				files = (IRemoteFile[])_files.toArray(new IRemoteFile[_files.size()]);			
			}
			for (int i = 0; i < files.length; i++){			
				IRemoteFile rFile = files[i];

				try {
					// service will take care of setting this on the host file
					_service.getFilePermissions(rFile.getHostFile(), monitor);	
					registry.fireEvent(new SystemResourceChangeEvent(rFile, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, rFile));						
				}
				catch (Exception e){
				}
			}
						
			_permissionsJobMap.remove(_service);
			return Status.OK_STATUS;
		}

		public int size(){
			return _files.size();
		}
		
		public boolean isStarted(){
			return _started;
		}
		
		public void addFile(IRemoteFile file){
			if (!_files.contains(file)){
				_files.add(file);
			}
		}
		
		
	}

	
	/**
	 * Constructor
	 */
	public SystemViewRemoteFileAdapter()
	{
		super();
		xlatedSize = FileResources.RESID_PROPERTY_FILE_SIZE_VALUE;
		xlatedCompressedSize = FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_VALUE;
		xlatedExpandedSize = FileResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_VALUE;

	}

	private IEditorRegistry getEditorRegistry()
	{
		if (registry == null)
		{
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null)
				registry = workbench.getEditorRegistry();
		}
		return registry;
	}
	/**
	 * Constructor for folders or files only
	 */
	public SystemViewRemoteFileAdapter(boolean foldersOnly, boolean filesOnly)
	{
		this();
		this.foldersOnly = foldersOnly;
		this.filesOnly = filesOnly;
	}
	/**
	 * Set a filter string to subset the list by. For example, "A*.java" or "java,class,".
	 * Only valid if filesOnly mode or foldersOnly mode.
	 */
	public void setFilterString(String filterString)
	{
		if (filesOnly && (filterString != null) && (filterString.indexOf("/ns") == -1)) //$NON-NLS-1$
			filterString = filterString + " /ns"; //$NON-NLS-1$
		else if (foldersOnly && (filterString != null) && (filterString.indexOf("/nf") == -1)) //$NON-NLS-1$
			filterString = filterString + " /nf"; //$NON-NLS-1$
		super.setFilterString(filterString);
	}

	/**
	 * Get the current filter string being used to subset the list by.
	 * Will be null unless setFilterString has previously been called.
	 */
	public String getFilterString()
	{
		return super.getFilterString();
	}

	/**
	 * Returns any actions that should be contributed to the popup menu
	 * for the given filter string object.
	 * @param menu The menu to contribute actions to
	 * @param selection The window's current selection.
	 * @param shell Shell of viewer
	 * @param menuGroup recommended menu group to add actions to. If added to another group, you must be sure to create that group first.
	 */
	public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell shell, String menuGroup)
	{
		int elementType = 0;
		boolean isArchive = false;

		boolean canRead = true;
		boolean supportsSearch = true;
		boolean supportsArchiveManagement = false;
		boolean offline = false;

		// perf improvement... phil
		Object firstSelection = selection.getFirstElement();
		IRemoteFile firstFile = null;
		if ((firstSelection != null) && (firstSelection instanceof IRemoteFile))
		{
			firstFile = (IRemoteFile) firstSelection;
			elementType = firstFile.isDirectory() || firstFile.isRoot() ? 1 : 0;
			isArchive = firstFile.isArchive();
			canRead = firstFile.canRead();

			IRemoteFileSubSystem fileSubSystem = firstFile.getParentRemoteFileSubSystem();

			offline = fileSubSystem.isOffline();

			supportsSearch = fileSubSystem.getParentRemoteFileSubSystemConfiguration().supportsSearch();
			supportsArchiveManagement = fileSubSystem.getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement();
		}
		else
			return;


		if (isArchive)
		{
			Iterator elements = selection.iterator();
			Object element = null;

			while (elements.hasNext())
			{
				element = elements.next();
				IRemoteFile remoteObj = (IRemoteFile) element;
				if (!remoteObj.isArchive())
				{
					break;
				}
			}
		}

		if ((elementType == 1 || (isArchive && supportsArchiveManagement)))
		{
			if (!foldersOnly)
			{
				if (addNewFile == null)
				{
					addNewFile = new SystemNewFileAction(shell);
				}
				addNewFile.setEnabled(canRead && !offline);
				menu.add(ISystemContextMenuConstants.GROUP_NEW, addNewFile);
			}
			if (!filesOnly)
			{
		        if (addNewFolder == null)
		        {
		            addNewFolder = new SystemNewFolderAction(shell);
		        }
		        addNewFolder.setEnabled(canRead && !offline);
		        menu.add(ISystemContextMenuConstants.GROUP_NEW, addNewFolder);

				if (addNewFilter == null)
				{
					addNewFilter = new SystemNewFileFilterFromFolderAction(shell);
				}

				menu.appendToGroup(ISystemContextMenuConstants.GROUP_NEW, new Separator());
				menu.add(ISystemContextMenuConstants.GROUP_NEW, addNewFilter);
			}

		}
		else {
	    	// open
	    	String label = SystemResources.ACTION_CASCADING_OPEN_LABEL;
	   	    String tooltip = SystemResources.ACTION_CASCADING_OPEN_TOOLTIP;
	   		SystemEditFilesAction action = new SystemEditFilesAction(label, tooltip, shell);
	   		menu.add(ISystemContextMenuConstants.GROUP_OPEN, action);
	   		action.setEnabled(canRead && !offline);

	   		// open with ->

			MenuManager submenu =
				new MenuManager(FileResources.ResourceNavigator_openWith,
					ISystemContextMenuConstants.GROUP_OPENWITH);

			if (openWithMenu == null)
			{
				openWithMenu = new SystemRemoteFileOpenWithMenu();
			}
			openWithMenu.updateSelection(selection);
			submenu.add(openWithMenu);
			menu.getMenuManager().appendToGroup(ISystemContextMenuConstants.GROUP_OPENWITH, submenu);
		}


		if (moveAction == null)
			moveAction = new SystemMoveRemoteFileAction(shell);

		if (pasteClipboardAction == null)
		{
			pasteClipboardAction = new SystemPasteFromClipboardAction(shell, null);
		}
		if (copyClipboardAction == null)
		{
			copyClipboardAction = new SystemCopyToClipboardAction(shell, null);
		}
		if (searchAction == null)
		{
			searchAction = new SystemSearchAction(shell);
		}
		if (compareFilesAction == null)
		{
			compareFilesAction = new SystemCompareFilesAction(shell);
		}
		if (compareEditionAction == null)
		{
			compareEditionAction = new SystemCompareWithEditionAction(shell);
		}
		if (replaceEditionAction == null)
		{
			replaceEditionAction = new SystemReplaceWithEditionAction(shell);
		}
		if (supportsSearch)
		{
		    //menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, addToArchiveAction);

			// add search action
			menu.add(ISystemContextMenuConstants.GROUP_SEARCH, searchAction);
			searchAction.setEnabled(canRead && !offline);
		}

		if (!firstFile.isRoot())
		{
			menu.add(menuGroup, copyClipboardAction);
			copyClipboardAction.setEnabled(canRead && !offline);
			if (elementType == 0)
			{
				menu.add(ISystemContextMenuConstants.GROUP_COMPAREWITH, compareFilesAction);
				menu.add(ISystemContextMenuConstants.GROUP_COMPAREWITH, compareEditionAction);
				menu.add(ISystemContextMenuConstants.GROUP_REPLACEWITH, replaceEditionAction);

				compareFilesAction.setEnabled(canRead && !offline);
				compareEditionAction.setEnabled(canRead && !offline);
				replaceEditionAction.setEnabled(canRead && !offline);
			}
		}

		if (elementType == 1 || (isArchive && supportsArchiveManagement))
		{
		    menu.add(menuGroup, pasteClipboardAction);
		    pasteClipboardAction.setEnabled(canRead && !offline);
		}
		if (!firstFile.isRoot())
		{
			menu.add(menuGroup, moveAction);
			moveAction.setEnabled(canRead && !offline);
		}
	}

	/**
	 * Returns an image descriptor for the image. More efficient than getting the image.
	 * @param element The element for which an image is desired
	 */
	public ImageDescriptor getImageDescriptor(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		if (file.isFile() || file.isArchive()) // hack to show zips without folder icons
		{
			// bug #376535 - need to respect editor overrides
			IFile localFile = getLocalResource(file);
			if (localFile != null) {
				IEditorDescriptor editorDesc = getEditorRegistry().getDefaultEditor(file.getName(), IDE.guessContentType(localFile));
				// Using reflection in case IDE is older version, without this method
				//editorDesc = IDE.overrideDefaultEditorAssociation(new FileEditorInput(localFile), IDE.guessContentType(localFile), editorDesc);
				Class clazz = IDE.class;
				try {
					Class parmtypes[] = {IEditorInput.class, IContentType.class, IEditorDescriptor.class};
					Method method = clazz.getMethod("overrideDefaultEditorAssociation", parmtypes); //$NON-NLS-1$
					if (method != null) {
						Object args[] = {new FileEditorInput(localFile), IDE.guessContentType(localFile), editorDesc};
						editorDesc = (IEditorDescriptor) method.invoke(null, args);
					}
				} catch (Exception e) {
				}
				
				if (editorDesc != null) {
					ImageDescriptor image = editorDesc.getImageDescriptor();
					if (image != null) {
						return image;
					}
				}
			}
			
			return getEditorRegistry().getImageDescriptor(file.getName());
		}
		else
		{
			boolean isOpen = false;
			if (getViewer() instanceof AbstractTreeViewer)
			{
				AbstractTreeViewer atv = (AbstractTreeViewer) getViewer();
				isOpen = atv.getExpandedState(element);
				if (!isOpen)
				{
					// if there are children but they are empty then we've queried this but there are no children
					// so we have an empty open folder
					Object[] contents = file.getContents(RemoteChildrenContentsType.getInstance());
					if (contents != null && contents.length == 0)
						isOpen = true;
				}
			}
			if (file.isRoot())
				return RSEUIPlugin.getDefault().getImageDescriptor(isOpen ? ISystemIconConstants.ICON_SYSTEM_ROOTDRIVEOPEN_ID : ISystemIconConstants.ICON_SYSTEM_ROOTDRIVE_ID);
			else if (isOpen)
			    return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
			else
				return RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_FOLDER_ID);
		}
	}

	/**
	 * Get the local cache of the remote file, or <code>null</code> if none.
	 * @param remoteFile the remote file.
	 * @return the local cached resource, or <code>null</code> if none.
	 */
	private IFile getLocalResource(IRemoteFile remoteFile) 
	{
		IFile file = null;
		if (remoteFile.getHost().getSystemType().isLocal())
		{
			String absolutePath = remoteFile.getAbsolutePath();
			file = getProjectFileForLocation(absolutePath);
		}
		if (file == null) {
			file = (IFile)UniversalFileTransferUtility.getTempFileFor(remoteFile);
		}
	    return file;
	}
	
	/**
	 * Return the label for this object. Uses getName() on the remote file object.
	 */
	public String getText(Object element)
	{
		return getName(element);
	}

	/**
	 * Return the label for this object. Uses getName() on the remote file object.
	 */
	public String getAlternateText(Object element)
	{
		return ((IRemoteFile) element).getLabel();
	}

	/**
	 * Return the name of this object, which may be different than the display text ({#link #getText(Object)}.
	 * <p>
	 * Called by common rename and delete actions.
	 */
	public String getName(Object element)
	{
		return ((IRemoteFile) element).getName();
	}
	/**
	 * Return the type label for this object
	 */
	public String getType(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;

		if (file.isRoot())
			return SystemViewResources.RESID_PROPERTY_FILE_TYPE_ROOT_VALUE;
		else if (file.isDirectory())
			return SystemViewResources.RESID_PROPERTY_FILE_TYPE_FOLDER_VALUE;
		else
			return SystemViewResources.RESID_PROPERTY_FILE_TYPE_FILE_VALUE;
	}

	/**
	 * Return the string to display in the status line when the given object is selected.
	 * We return:
	 * <getType()>: <getAbsoluteName()>
	 */
	public String getStatusLineText(Object element)
	{
		return getType(element) + ": " + getAbsoluteName(element); //$NON-NLS-1$
	}

	/**
	 * Returns whether the specified element is represented as existing.  Note that
	 * it's possible that the represented element will been seen to exist when on
	 * a remote host it may not - that is because this call does not query the host.
	 * Returns whether the remote file representation exists.
	 *
	 * @param element the element to check
	 * @return true if the element exists
	 *
	 */
	public boolean exists(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		if (file != null)
		{
			return file.exists();
		}
		return false;
	}

	/**
	 * Return the parent of this object
	 */
	public Object getParent(Object element)
	{
		//System.out.println("Inside getParent for: "+element);
		IRemoteFile file = (IRemoteFile) element;

			IRemoteFile parent = file.getParentRemoteFile();
			if ((parent != null) && parent.getAbsolutePath().equals(file.getAbsolutePath()))
				// should never happen but sometimes it does, leading to infinite loop.
				parent = null;
			return parent;

	}

	/**
	 * Return the children of this object.
	 * If this is a folder or root, we list all child folders and files.
	 */
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor)
	{
		return internalGetChildren(element, null, monitor);
	}

	/**
	 * Return the children of this object.
	 * If this is a folder or root, we list all child folders and files.
	 */
	public Object[] getChildren(IContextObject context, IProgressMonitor monitor)
	{
		return internalGetChildren(context.getModelObject(), context.getFilterReference(), monitor);
	}

	private Object[] internalGetChildren(IAdaptable element, ISystemFilterReference filterReference, IProgressMonitor monitor)
	{
		//System.out.println("Inside getChildren for: "+element);
		IRemoteFile file = (IRemoteFile) element;
		if (file instanceof RemoteFileEmpty) // cut to the chase
		{
			//System.out.println("FileAdapter.getChildren(): empty list-request");
			return EMPTY_LIST;
		}
		else if (file instanceof RemoteFileRoot)
		{
			//System.out.println("FileAdapter.getChildren(): children of root list-request");
			return ((RemoteFileRoot) file).getRootFiles();
		}
		IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();
		
		// make sure we have the lastest cached version otherwise could be working with a bad file that never got marked as stale
		IRemoteFile originalFile = file;
		if (ss instanceof RemoteFileSubSystem){
			IRemoteFile cachedFile = ((RemoteFileSubSystem)ss).getCachedRemoteFile(file.getAbsolutePath());
			if (cachedFile != null && cachedFile != originalFile){
				file = cachedFile;
				if (originalFile.isStale()){ // the original file was marked stale, so the cached one should be too
					file.markStale(true);
				}
			}
		}


		/*
		RemoteFileFilterString orgRffs = file.getFilterString();


		if (orgRffs != null)
		{
			if (foldersOnly)
			{
				RemoteFileFilterString rffs = (RemoteFileFilterString) orgRffs.clone();
				rffs.setPath(null);
				rffs.setShowFiles(false);
				rffs.setShowSubDirs(true);
				filter = rffs.toString();
			}
			else if (filesOnly)
			{
				RemoteFileFilterString rffs = (RemoteFileFilterString) orgRffs.clone();
				rffs.setPath(null);
				rffs.setShowSubDirs(false);
				rffs.setShowFiles(true);
				filter = rffs.toString();
			}
		}
		else
		*/
		String filter = null;
		if (filterReference != null)
		{
			ISystemFilter filterObject = filterReference.getReferencedFilter();
			if (filterObject.getFilterStringCount() > 0)
			{
				String filterString = filterObject.getFilterStrings()[0];
				String separator = PathUtility.getSeparator(filterString);

				int sepIndex = filterString.lastIndexOf(separator);
				if (sepIndex >= 0)
				{
					filter = filterString.substring(sepIndex + 1);
				}
				else
				{
					// fix for 197089
					filter = filterString;
				}
			}
		}
		else
		{

		String filterString = getFilterString();
		if (foldersOnly)
		{
			if (filterString == null)
				filter = "* /nf"; //$NON-NLS-1$
			else
				filter = filterString;
		}
		else if (filesOnly)
		{
			if (filterString == null)
				filter = "* /ns"; //$NON-NLS-1$
			else
				filter = filterString;
		}
		else
		{
			if (filterString == null)
				filter = "*"; //$NON-NLS-1$
			else
				filter = filterString;
		}
		}

		Object[] children = null;

		Viewer v = getViewer();

		if ((v != null) && (v instanceof SystemSearchTableView)) {

		    SystemSearchTableView view = (SystemSearchTableView)v;
		    Iterator iter = view.getResultSet().getSearchConfigurations();
		    boolean hasSearchResults = false;
		    while (iter.hasNext() && !hasSearchResults)
		    {
		    	IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)(iter.next());
		    	String searchString = config.getSearchString().getTextString();

		    	hasSearchResults = file.hasContents(RemoteSearchResultsContentsType.getInstance(), searchString);

		    	if (hasSearchResults)
		    	{
		    		children = file.getContents(RemoteSearchResultsContentsType.getInstance(), searchString);
		    		return children;
		    	}
		    }
		}

		// taking out the synchronized block to avoid potential deadlock
		// TODO next release, find a risk-free way to avoid duplicate queries
		// synchronized (file)
		
		{
			boolean hasChildren = file.hasContents(RemoteChildrenContentsType.getInstance(), filter);
	
			if (hasChildren && !file.isStale())
			{
				children = file.getContents(RemoteChildrenContentsType.getInstance(), filter);
				children = filterChildren(children);
			}
			else
			{
				try
				{
				    if (monitor != null)
				    {
	
				        children = ss.resolveFilterString(file, filter, monitor);
				    }
				    else
				    {
				        children = ss.resolveFilterString(file, filter, new NullProgressMonitor());
				    }
	
					if ((children == null) || (children.length == 0))
					{
						children = EMPTY_LIST;
					}
					else
					{
						if (children.length == 1 && children[0] instanceof SystemMessageObject)
						{
							// don't filter children so that the message gets propagated
						}
						else
						{
							children = filterChildren(children);
						}
					}
	
				}
				catch (InterruptedException exc)
				{
					children = new SystemMessageObject[1];
					SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ICommonMessageIds.MSG_EXPAND_CANCELLED,
							IStatus.CANCEL, CommonMessages.MSG_EXPAND_CANCELLED);
					children[0] = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_CANCEL, element);
				}
				catch (Exception exc)
				{
					children = new SystemMessageObject[1];
	
					SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ICommonMessageIds.MSG_EXPAND_FAILED,
							IStatus.ERROR,
							CommonMessages.MSG_EXPAND_FAILED);
					children[0] = new SystemMessageObject(msg, ISystemMessageObject.MSGTYPE_ERROR, element);
					SystemBasePlugin.logError("Exception resolving file filter strings", exc); //$NON-NLS-1$
				} // message already issued
			}
			file.markStale(false);
			if (originalFile != null && originalFile != file){
				originalFile.markStale(false);
			}
		}
		return children;
	}

	private Object[] filterChildren(Object[] children) {

		boolean showHidden = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.SHOWHIDDEN);

		if (showHidden) {
			return children;
		}
		else {

			ArrayList results = new ArrayList(children.length);

			for (int i = 0; i < children.length; i++) {

				if (children[i] instanceof IRemoteFile) {
					IRemoteFile remoteFile = (IRemoteFile)(children[i]);

					if (!remoteFile.isHidden()) {
						results.add(remoteFile);
					}
				}
			}

			return results.toArray();
		}
	}

	/**
	 * Return true if this object has children.
	 * Since we can't predict the outcome of resolving the filter string, we return true.
	 */
	public boolean hasChildren(IContextObject element)
	{
		return internalHasChildren(element.getModelObject(), element.getFilterReference());
	}

	/**
	 * Return true if this object has children.
	 * Since we can't predict the outcome of resolving the filter string, we return true.
	 */
	public boolean hasChildren(IAdaptable element)
	{
		return internalHasChildren(element, null);
	}

	public boolean internalHasChildren(IAdaptable element, ISystemFilterReference filterReference)
	{
		IRemoteFile file = (IRemoteFile) element;

		IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();
		
		// make sure we have the lastest cached version otherwise could be working with a bad file that never got marked as stale
		IRemoteFile originalFile = file;
		if (ss instanceof RemoteFileSubSystem){
			IRemoteFile cachedFile = ((RemoteFileSubSystem)ss).getCachedRemoteFile(file.getAbsolutePath());
			if (cachedFile != null && cachedFile != originalFile){
				file = cachedFile;
				if (originalFile.isStale()){ // the original file was marked stale, so the cached one should be too
					file.markStale(true);
				}
			}
		}		
		
		if (!file.exists())
			return false;

		boolean supportsArchiveManagement = file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement();
		boolean hasChildren = false;

		String filter = "*"; //$NON-NLS-1$
		if (filterReference != null)
		{
			ISystemFilter filterObject = filterReference.getReferencedFilter();
			if (filterObject.getFilterStringCount() > 0)
			{
				String filterString = filterObject.getFilterStrings()[0];
				String separator = PathUtility.getSeparator(filterString);

				int sepIndex = filterString.lastIndexOf(separator);
				if (sepIndex > 0)
				{
					filter = filterString.substring(sepIndex + 1);
				}
			}
		}

		if (file instanceof IVirtualRemoteFile)
		{
			hasChildren = ((IVirtualRemoteFile)file).isVirtualFolder();
		}
		else
		{
			hasChildren = !file.isFile() || (file.isArchive() && supportsArchiveManagement);
		}

		if (!hasChildren) {

		    Viewer v = getViewer();

		    if ((v != null) && (v instanceof SystemSearchTableView)) {

			    SystemSearchTableView view = (SystemSearchTableView)v;
			    Iterator iter = view.getResultSet().getSearchConfigurations();
			    while (iter.hasNext() && !hasChildren)
			    {
			    	IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)(iter.next());
			    	String searchString = config.getSearchString().getTextString();

		        	hasChildren = file.hasContents(RemoteSearchResultsContentsType.getInstance(), searchString);
			    }
		    }
		    else {
		        hasChildren = file.hasContents(RemoteChildrenContentsType.getInstance(), filter);
		    }
		}
		else
		{
			// check that the children are actually there
			//Object[] contents = file.getContents(RemoteChildrenContentsType.getInstance());
			hasChildren = file.hasContents(RemoteChildrenContentsType.getInstance(), filter);
			//if (!hasChildren && !file.isStale())
			if (!hasChildren && file.isStale() && (file.isDirectory() || file.isArchive())) // there are no children, but the resource is stale, therefore it still needs to be queried
				hasChildren = true;
		}

		return hasChildren;
	}



	public IPropertyDescriptor[] getUniquePropertyDescriptors()
	{

		IRemoteFile file = null;
		if (propertySourceInput instanceof IRemoteFile)
		{
			file = (IRemoteFile) propertySourceInput;

			boolean supportsArchiveManagement = file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement();

			boolean isArchive = file.isArchive() && supportsArchiveManagement;
			boolean isVirtual = file instanceof IVirtualRemoteFile && supportsArchiveManagement;
			boolean isRegular = !isArchive && !isVirtual;

			if (isRegular && uniquePropertyDescriptorArray == null ||
			    isArchive && uniqueArchiveDescriptorArray == null ||
			    isVirtual && uniqueVirtualDescriptorArray == null)
			{

				int nbrOfArchiveProperties = 2;
				int nbrOfVirtualProperties = 4;
				int nbrOfProperties = 8;
				if (isVirtual) nbrOfProperties += nbrOfVirtualProperties;
				else if (isArchive) nbrOfProperties += nbrOfArchiveProperties;

				if (isRegular) uniquePropertyDescriptorArray = new PropertyDescriptor[nbrOfProperties];
				else if (isVirtual) uniqueVirtualDescriptorArray = new PropertyDescriptor[nbrOfProperties];
				else if (isArchive) uniqueArchiveDescriptorArray = new PropertyDescriptor[nbrOfProperties];
				//PropertyDescriptor[] defaultProperties = (PropertyDescriptor[]) getDefaultDescriptors();

				int i = -1;

				// add our unique property descriptors...
				//RSEUIPlugin plugin = RSEUIPlugin.getDefault();

				// classification
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CLASSIFICATION, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CLASSIFICATION, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CLASSIFICATION, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);

				// last modified
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_LASTMODIFIED, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_LASTMODIFIED, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_LASTMODIFIED, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);

				// size
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_SIZE, FileResources.RESID_PROPERTY_FILE_SIZE_LABEL, FileResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_SIZE, FileResources.RESID_PROPERTY_FILE_SIZE_LABEL, FileResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_SIZE, FileResources.RESID_PROPERTY_FILE_SIZE_LABEL, FileResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);

				// canonical path
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CANONICAL_PATH, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CANONICAL_PATH, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CANONICAL_PATH, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);

				// file extension
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_EXTENSION, FileResources.RESID_PROPERTY_FILE_EXTENSION_LABEL, FileResources.RESID_PROPERTY_FILE_EXTENSION_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_EXTENSION, FileResources.RESID_PROPERTY_FILE_EXTENSION_LABEL, FileResources.RESID_PROPERTY_FILE_EXTENSION_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_EXTENSION, FileResources.RESID_PROPERTY_FILE_EXTENSION_LABEL, FileResources.RESID_PROPERTY_FILE_EXTENSION_TOOLTIP);

				// file permissions
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PERMISSIONS, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_LABEL, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PERMISSIONS, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_LABEL, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PERMISSIONS, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_LABEL, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP);

				// file owner
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_OWNER, FileResources.RESID_PROPERTY_FILE_OWNER_LABEL, FileResources.RESID_PROPERTY_FILE_OWNER_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_OWNER, FileResources.RESID_PROPERTY_FILE_OWNER_LABEL, FileResources.RESID_PROPERTY_FILE_OWNER_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_OWNER, FileResources.RESID_PROPERTY_FILE_OWNER_LABEL, FileResources.RESID_PROPERTY_FILE_OWNER_TOOLTIP);

				// file group
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_GROUP, FileResources.RESID_PROPERTY_FILE_GROUP_LABEL, FileResources.RESID_PROPERTY_FILE_GROUP_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_GROUP, FileResources.RESID_PROPERTY_FILE_GROUP_LABEL, FileResources.RESID_PROPERTY_FILE_GROUP_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_GROUP, FileResources.RESID_PROPERTY_FILE_GROUP_LABEL, FileResources.RESID_PROPERTY_FILE_GROUP_TOOLTIP);


				if (isVirtual)
				{
					// add virtual property descriptors...

					// compressed size
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMPRESSEDSIZE, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_LABEL, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_DESCRIPTION);

					// compression ratio
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONRATIO, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_LABEL, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_DESCRIPTION);

					// compression method
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONMETHOD, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_LABEL,  FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_DESCRIPTION);

					// comment
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMMENT, FileResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_LABEL,  FileResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_DESCRIPTION);
				}
				else if (isArchive)
				{
					// add archive property descriptors...

					// expanded size
					uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ARCHIVE_EXPANDEDSIZE, FileResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_LABEL, FileResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_DESCRIPTION);

					// comment
					uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ARCHIVE_COMMENT, FileResources.RESID_PROPERTY_ARCHIVE_COMMENT_LABEL, FileResources.RESID_PROPERTY_ARCHIVE_COMMENT_DESCRIPTION);
				}
			}

		// DKM - I commented this stuff out since it's screwing up table view
		///*
			if (isRegular) return uniquePropertyDescriptorArray;
			else if (isVirtual) return uniqueVirtualDescriptorArray;
			else if (isArchive) return uniqueArchiveDescriptorArray;
			else return uniquePropertyDescriptorArray;
		//*/
		}
		return uniquePropertyDescriptorArray;
	}

	/**
	 * Return our unique property descriptors
	 */
	protected IPropertyDescriptor[] internalGetPropertyDescriptors()
	{
		//System.out.println("Inside beg getPropertyDescriptors: "+propertyDescriptorArray);
		// IT TURNS OUT THAT FOR IFS FILES, THE QUERY OF THE FULL SET OF PROPERTIES CAN TAKE UP
		//  TO 5 SECONDS, CONSIDERABLY SLOWING DOWN RESPONSE TIME TO THE POINT OF PAIN. TO FIX THIS,
		//  WE HAVE MADE IT POSSIBLE FOR REMOTE FILE OBJECTS TO DECIDE TO SHOW ONLY A SUBSET OF THE
		//  PROPERTIES, WHICH IFS FILES EXPLOIT. PHIL.

		IRemoteFile file = null;
		if (propertySourceInput instanceof IRemoteFile) file = (IRemoteFile) propertySourceInput;
		boolean isArchive = false;//file != null && file.isArchive();

		boolean isVirtual = file != null && file instanceof IVirtualRemoteFile;
		boolean isRegular = !isArchive && !isVirtual;

		if (isRegular && propertyDescriptorArray == null ||//uniquePropertyDescriptorArray == null ||
			isArchive && archiveDescriptorArray == null ||
			isVirtual && virtualDescriptorArray == null)
		{
			int nbrOfArchiveProperties = 2;
			int nbrOfVirtualProperties = 4;
			int nbrOfProperties = 12;
			int nbrOfBriefProperties = 2;
			if (debug)
				nbrOfProperties += 7;

			if (isVirtual) nbrOfProperties += nbrOfVirtualProperties;
			else if (isArchive) nbrOfProperties += nbrOfArchiveProperties;

			if (isRegular) propertyDescriptorArray = new PropertyDescriptor[nbrOfProperties];
			else if (isVirtual) virtualDescriptorArray = new PropertyDescriptor[nbrOfProperties];
			else if (isArchive) archiveDescriptorArray = new PropertyDescriptor[nbrOfProperties];

			briefPropertyDescriptorArray = new PropertyDescriptor[nbrOfBriefProperties];
			int idx = -1;
			int briefIdx = idx;

			// path
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);

			// filter string
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
			briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);

			// canonical path
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CANONICAL_PATH, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL,FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CANONICAL_PATH, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL,FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CANONICAL_PATH, FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL,FileResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);

			// last modified
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_LASTMODIFIED, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_LASTMODIFIED, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL,FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_LASTMODIFIED, FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL,FileResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);

			// size
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_SIZE, FileResources.RESID_PROPERTY_FILE_SIZE_LABEL, FileResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_SIZE, FileResources.RESID_PROPERTY_FILE_SIZE_LABEL, FileResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_SIZE, FileResources.RESID_PROPERTY_FILE_SIZE_LABEL, FileResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);

			// classification
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CLASSIFICATION, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CLASSIFICATION, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_CLASSIFICATION, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, FileResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);

			// readonly
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_READONLY, FileResources.RESID_PROPERTY_FILE_READONLY_LABEL, FileResources.RESID_PROPERTY_FILE_READONLY_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_READONLY, FileResources.RESID_PROPERTY_FILE_READONLY_LABEL, FileResources.RESID_PROPERTY_FILE_READONLY_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_READONLY, FileResources.RESID_PROPERTY_FILE_READONLY_LABEL, FileResources.RESID_PROPERTY_FILE_READONLY_TOOLTIP);

			// hidden
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_HIDDEN, FileResources.RESID_PROPERTY_FILE_HIDDEN_LABEL, FileResources.RESID_PROPERTY_FILE_HIDDEN_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_HIDDEN, FileResources.RESID_PROPERTY_FILE_HIDDEN_LABEL, FileResources.RESID_PROPERTY_FILE_HIDDEN_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_HIDDEN, FileResources.RESID_PROPERTY_FILE_HIDDEN_LABEL, FileResources.RESID_PROPERTY_FILE_HIDDEN_TOOLTIP);

			// file extension
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_EXTENSION, FileResources.RESID_PROPERTY_FILE_EXTENSION_LABEL, FileResources.RESID_PROPERTY_FILE_EXTENSION_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_EXTENSION, FileResources.RESID_PROPERTY_FILE_EXTENSION_LABEL, FileResources.RESID_PROPERTY_FILE_EXTENSION_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_EXTENSION, FileResources.RESID_PROPERTY_FILE_EXTENSION_LABEL, FileResources.RESID_PROPERTY_FILE_EXTENSION_TOOLTIP);

			// file permissions
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PERMISSIONS, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_LABEL, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PERMISSIONS, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_LABEL, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_PERMISSIONS, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_LABEL, FileResources.RESID_PROPERTY_FILE_PERMISSIONS_TOOLTIP);

			// file owner
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_OWNER, FileResources.RESID_PROPERTY_FILE_OWNER_LABEL, FileResources.RESID_PROPERTY_FILE_OWNER_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_OWNER, FileResources.RESID_PROPERTY_FILE_OWNER_LABEL, FileResources.RESID_PROPERTY_FILE_OWNER_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_OWNER, FileResources.RESID_PROPERTY_FILE_OWNER_LABEL, FileResources.RESID_PROPERTY_FILE_OWNER_TOOLTIP);

			// file group
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_GROUP, FileResources.RESID_PROPERTY_FILE_GROUP_LABEL, FileResources.RESID_PROPERTY_FILE_GROUP_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_GROUP, FileResources.RESID_PROPERTY_FILE_GROUP_LABEL, FileResources.RESID_PROPERTY_FILE_GROUP_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_FILE_GROUP, FileResources.RESID_PROPERTY_FILE_GROUP_LABEL, FileResources.RESID_PROPERTY_FILE_GROUP_TOOLTIP);


			if (debug)
			{
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENTREMOTEFILE"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENTREMOTEFILE"); //$NON-NLS-1$
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENT"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENT"); //$NON-NLS-1$
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENTNOROOT"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENTNOROOT"); //$NON-NLS-1$
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENTNAME"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENTNAME"); //$NON-NLS-1$
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_ROOT"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_ROOT"); //$NON-NLS-1$
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_ISROOT"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_ISROOT"); //$NON-NLS-1$
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_EXISTS"); //$NON-NLS-1$
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_EXISTS"); //$NON-NLS-1$
			}

			if (isVirtual)
			{
				// add virtual property descriptors...

				// compressed size
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMPRESSEDSIZE, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_LABEL, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_DESCRIPTION);

				// compression ratio
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONRATIO, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_LABEL, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_DESCRIPTION);

				// compression method
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONMETHOD, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_LABEL, FileResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_DESCRIPTION);

				// comment
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_VIRTUAL_COMMENT, FileResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_LABEL,  FileResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_DESCRIPTION);
			}

			else if (isArchive)
			{
				// add archive property descriptors...

				// expanded size
				archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ARCHIVE_EXPANDEDSIZE, FileResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_LABEL,  FileResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_DESCRIPTION);

				// comment
				archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(ISystemPropertyConstants.P_ARCHIVE_COMMENT, FileResources.RESID_PROPERTY_ARCHIVE_COMMENT_LABEL, FileResources.RESID_PROPERTY_ARCHIVE_COMMENT_DESCRIPTION);
			}

		}

//		return propertyDescriptorArray;
		// DKM - I've commented this out because it's too expensive to query archive properties during a folder query
		// we need to come back to this and change this do work in a more performance-sensitive way

		//System.out.println("Inside end getPropertyDescriptors: "+propertyDescriptorArray.length);
		if ((propertySourceInput instanceof IRemoteFile) && ((IRemoteFile) propertySourceInput).showBriefPropertySet())
			return propertyDescriptorArray;
		    //return briefPropertyDescriptorArray;

		else
		{
			if (isRegular) return propertyDescriptorArray;
			else if (isVirtual) return virtualDescriptorArray;
			else if (isArchive) return archiveDescriptorArray;
			else return propertyDescriptorArray;
		}

	}

	/**
	 * Create and return a simple string readonly property descriptor. For debug purposes
	 */
	protected static PropertyDescriptor createSimplePropertyDescriptor(String keyAndLabel)
	{
		PropertyDescriptor pd = new PropertyDescriptor(keyAndLabel, keyAndLabel);
		return pd;
	}

	/**
	 * Returns the current value for the named property.
	 * @return the current value of the given property
	 */
	protected Object internalGetPropertyValue(Object key)
	{
		return getPropertyValue(key, true);
	}


	public Object getPropertyValue(Object key)
	{
		String name = (String)key;
		if (name.equals(ISystemPropertyConstants.P_NBRCHILDREN))
		{
			IRemoteFile file = (IRemoteFile) propertySourceInput;
			{
				ISystemContainer container = file;
				Object[] contents = container.getContents(RemoteChildrenContentsType.getInstance());
				if (contents == null)
					return "0"; //$NON-NLS-1$
				else
					return Integer.toString(contents.length);
			}
		}
 		return super.getPropertyValue(key);
	}

	/**
	 * Returns the current value for the named property.
	 *
	 * @param property the name or key of the property as named by its property descriptor
	 * @param formatted indication of whether to return the value in formatted or raw form
	 * @return the current value of the given property
	 */
	public Object getPropertyValue(Object property, boolean formatted)
	{
		String name = (String) property;
		IRemoteFile file = (IRemoteFile) propertySourceInput;
		IVirtualRemoteFile virtualFile = null;
		boolean isVirtual = file instanceof IVirtualRemoteFile;
		if (isVirtual)
		{
			virtualFile = (IVirtualRemoteFile) file;
		}
		if (debug)
		{
			if (name.equals("DEBUG_PARENTREMOTEFILE")) //$NON-NLS-1$
			{
				IRemoteFile parent = file.getParentRemoteFile();
				if (parent == null)
					return "null"; //$NON-NLS-1$
				else
					return "absPath='" + parent.getAbsolutePath() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (name.equals("DEBUG_PARENT")) //$NON-NLS-1$
				return file.getParentPath();
			else if (name.equals("DEBUG_PARENTNOROOT")) //$NON-NLS-1$
				return file.getParentNoRoot();
			else if (name.equals("DEBUG_PARENTNAME")) //$NON-NLS-1$
				return file.getParentName();
			else if (name.equals("DEBUG_ROOT")) //$NON-NLS-1$
				return file.getRoot();
			else if (name.equals("DEBUG_ISROOT")) //$NON-NLS-1$
				return file.isRoot() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
			else if (name.equals("DEBUG_EXISTS")) //$NON-NLS-1$
				return file.exists() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$

		}
		//
		if (name.equals(ISystemPropertyConstants.P_FILE_LASTMODIFIED))
		{
			Date date = file.getLastModifiedDate();
			if (date != null)
			{
				long t = date.getTime();
				
				if (formatted)
				{
					if (t == 0){
						// no time available, we should leave this blank
						return ""; //$NON-NLS-1$
					}
					else {
						ULocale locale = ULocale.getDefault();					
						DateFormat  icufmt = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale);
						String formattedDate = icufmt.format(date);
						return formattedDate;
					}
				}
				else
				{
					return date;
				}
			}
			return date;
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_SIZE))
		{
			if (formatted)
			{
		    	NumberFormat fmt = NumberFormat.getIntegerInstance();
		    	String formattedNumber = fmt.format(file.getLength());
				return sub(xlatedSize, MSG_SUB1, formattedNumber);
			}
			else
			{
				return new Long(file.getLength());
			}
		}
		else if (name.equals(ISystemPropertyConstants.P_ARCHIVE_EXPANDEDSIZE))
		{
			if (!isVirtual || virtualFile == null) return new Long(0);

			if (formatted)
			{
				return sub(xlatedExpandedSize, MSG_SUB1, Long.toString(virtualFile.getExpandedSize()));
			}
			else
			{
				return new Long(virtualFile.getExpandedSize());
			}
		}
		else if (name.equals(ISystemPropertyConstants.P_VIRTUAL_COMPRESSEDSIZE))
		{
			if (!isVirtual || virtualFile == null) return new Long(0);
			if (formatted)
			{
				return sub(xlatedCompressedSize, MSG_SUB1, Long.toString(virtualFile.getCompressedSize()));
			}
			else
			{
				return new Long(virtualFile.getCompressedSize());
			}
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_PATH))
		{
			return file.getParentPath();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_CANONICAL_PATH))
		{
			return file.getCanonicalPath();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_EXTENSION))
		{
			if(!file.isDirectory()) {
				String ext = file.getExtension();
				return ext == null?"":ext; //$NON-NLS-1$
			}
			else
				return ""; //$NON-NLS-1$
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_PERMISSIONS))
		{
			IHostFilePermissions permissions = file.getPermissions();
			if (permissions == null){

				if (getFilePermissions(file)){
					return FileResources.MESSAGE_PENDING;
				}
				return FileResources.MESSAGE_NOT_SUPPORTED;
			}
			if (permissions instanceof PendingHostFilePermissions){
				return FileResources.MESSAGE_PENDING;
			}
			return permissions.toAlphaString();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_OWNER))
		{
			IHostFilePermissions permissions = file.getPermissions();
			if (permissions == null){

				if (getFilePermissions(file)){
					return FileResources.MESSAGE_PENDING;
				}
				return FileResources.MESSAGE_NOT_SUPPORTED;
			}
			if (permissions instanceof PendingHostFilePermissions){
				return FileResources.MESSAGE_PENDING;
			}
			return permissions.getUserOwner();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_GROUP))
		{
			IHostFilePermissions permissions = file.getPermissions();
			if (permissions == null){

				if (getFilePermissions(file)){
					return FileResources.MESSAGE_PENDING;
				}
				return FileResources.MESSAGE_NOT_SUPPORTED;
			}
			if (permissions instanceof PendingHostFilePermissions){
				return FileResources.MESSAGE_PENDING;
			}
			return permissions.getGroupOwner();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_CLASSIFICATION))
		{
			return file.getClassification();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_READONLY))
		{
			return file.canWrite() ? getTranslatedNo() : getTranslatedYes();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_READABLE))
		{
			return file.canRead() ? getTranslatedYes() : getTranslatedNo();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_WRITABLE))
		{
			return file.canWrite() ? getTranslatedYes() : getTranslatedNo();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILE_HIDDEN))
		{
			return file.isHidden() ? getTranslatedYes() : getTranslatedNo();
		}
		else if (name.equals(ISystemPropertyConstants.P_FILTERSTRING))
		{
			return file.getFilterString();
		}
		else if (name.equals(ISystemPropertyConstants.P_ARCHIVE_COMMENT) || name.equals(ISystemPropertyConstants.P_VIRTUAL_COMMENT))
		{
			return file.getComment();
		}
		else if (name.equals(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONMETHOD))
		{
			if (!isVirtual || virtualFile == null) return ""; //$NON-NLS-1$
			return virtualFile.getCompressionMethod();
		}
		else if (name.equals(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONRATIO))
		{
			if (virtualFile != null)
			{
			Double ratio = new Double(virtualFile.getCompressionRatio());
			if (formatted)
			{
				NumberFormat numfmt = NumberFormat.getPercentInstance();
				return numfmt.format(ratio);
			}
			else
			{
				return ratio;
			}}
			else
				return null;
		}
		else
			return null; //super.getPropertyValue(name);
	}

	private boolean getFilePermissions(IRemoteFile file){
		if (file instanceof IAdaptable){
			final IFilePermissionsService service = (IFilePermissionsService)((IAdaptable)file).getAdapter(IFilePermissionsService.class);

			if (service != null && (service.getCapabilities(file.getHostFile()) & IFilePermissionsService.FS_CAN_GET_PERMISSIONS) != 0){

				final IRemoteFile rFile = file;
				if (rFile.getHostFile() instanceof IHostFilePermissionsContainer){
					((IHostFilePermissionsContainer)rFile.getHostFile()).setPermissions(new PendingHostFilePermissions());
				}
				
				MultiFetchPermissionsJob deferredFetch = (MultiFetchPermissionsJob)_permissionsJobMap.get(service);
				if (deferredFetch == null || deferredFetch.isStarted() || deferredFetch.size() > 50){ // max 50 files per job
					deferredFetch = new MultiFetchPermissionsJob(service);
					_permissionsJobMap.put(service, deferredFetch);
					deferredFetch.addFile(file);
					deferredFetch.schedule(100);
				}
				else {
					deferredFetch.addFile(file);
				}
				
				return true; // query kicked off
			}
		}
		return false; // no query kicked off
	}


	// Drag and Drop Implementation

	/**
	 * Indicates whether the specified object can be copied or not.
	 * @param element the object we want to try to copy
	 * @return whether this object can be copied or not
	 */
	public boolean canDrag(Object element)
	{
	    if (element instanceof IRemoteFile)
	    {
	    	IRemoteFile file = (IRemoteFile)element;
	    	boolean offline = file.getParentRemoteFileSubSystem().isOffline();
	        return file.canRead() && !offline;
	    }
		return true;
	}

	/**
	 * Indicates whether the specified object can have another object copied to it
	 * @param element the object we want to try to copy to
	 * @return whether this object can be copied to or not
	 */
	public boolean canDrop(Object element)
	{
		if (element instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) element;
			IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();
			IRemoteFileSubSystemConfiguration config = ss.getParentRemoteFileSubSystemConfiguration();
	    	boolean offline = ss.isOffline();
	    	boolean isWindows = !config.isUnixStyle(); // windows check for bug 228743
			boolean supportsArchiveManagement = config.supportsArchiveManagement();
								
			return !offline && file.canRead() && (file.canWrite() || isWindows) && (file.isDirectory() || file.isRoot() || (file.isArchive() && supportsArchiveManagement));
		}

		return false;
	}

	/**
	  * Return true if it is valid for the src object to be dropped in the target
	  * @param srcSet the objects to drop
	  * @param target the object which src is dropped in
	  * @param sameSystem whether this is the same system
	  * @return whether this is a valid operation
	  */
	public boolean validateDrop(ISystemResourceSet srcSet, Object target, boolean sameSystem)
	{
		if (target instanceof IRemoteFile)
		{
			IRemoteFile targetFile = (IRemoteFile) target;
			IRemoteFileSubSystemConfiguration config = targetFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration();
			boolean supportsArchiveManagement = config.supportsArchiveManagement();
			if (!targetFile.isFile() || (targetFile.isArchive() && supportsArchiveManagement))
			{
				boolean isWindows = !config.isUnixStyle();
				return targetFile.canWrite() || isWindows; // windows check for bug 228743
			}
			// all objects are of same type, so we only need to use first to validate
			Object first = srcSet.get(0);
			if (first instanceof IRemoteFile)
			{
				return true;
			}
			else if (first instanceof IResource)
			{
				return true;
			}
			else if (first instanceof ISystemFilterReference)
			{
				return true;
			}
			else  // akh11: proposed modification -- 01/28/2005
			{   // ask source adapter if it supports drop of its type on this target:
				ISystemViewDropDestination adapter = 	(ISystemViewDropDestination)Platform.getAdapterManager().getAdapter(first,ISystemViewDropDestination.class);
				if (adapter != null)
					return adapter.supportDropDestination(target);
			}
		}
		return false;
	}

	/**
	  * Return true if it is valid for the src object to be dropped in the target
	  * @param src the object to drop
	  * @param target the object which src is dropped in
	  * @param sameSystem whether this is the same system
	  * @return whether this is a valid operation
	  */
	public boolean validateDrop(Object src, Object target, boolean sameSystem)
	{
		if (target instanceof IRemoteFile)
		{
			IRemoteFile targetFile = (IRemoteFile) target;
			IRemoteFileSubSystemConfiguration config = targetFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration();
			boolean supportsArchiveManagement = config.supportsArchiveManagement();
			if (!targetFile.isFile() || (targetFile.isArchive() && supportsArchiveManagement))
			{
				// get properties
				// this is asynchronous so we call
				// it here to prepare for subsequent operation
				targetFile.canWrite();

				if (src instanceof IRemoteFile)
				{
					return true;
				}
				else if (src instanceof IResource)
				{
					return true;
				}
				else if (src instanceof String)
				{
					// check if this is a file
					java.io.File localFile = new java.io.File((String) src);
					if (localFile.exists())
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else if (src instanceof ISystemFilterReference)
				{
					return true;
				}

				else  // akh11: proposed modification -- 01/28/2005
				{   // ask source adapter if it supports drop of its type on this target:
					ISystemViewDropDestination adapter = 	(ISystemViewDropDestination)Platform.getAdapterManager().getAdapter(src,ISystemViewDropDestination.class);
					if (adapter != null)
						return adapter.supportDropDestination(target);
				}
			}
		}

		return false;
	}



	/**
	 * Performs a drag copy operation.  The source object is uploaded to a temporary location so that it can
	 * later be dropped on another remote system.
	 * @param element the object which is being copied
	 * @param sameSystem an indication whether a transfer is being made between the same types of systems.
	 * @param monitor a progress monitor
	 * @return the temporary object that was created after the upload
	 */
	public Object doDrag(Object element, boolean sameSystem, IProgressMonitor monitor)
	{

		// copy all resources into temporary location
		if (element instanceof IRemoteFile)
		{
			IRemoteFile srcFileOrFolder = (IRemoteFile) element;
			return UniversalFileTransferUtility.downloadResourceToWorkspace(srcFileOrFolder, monitor);
		}
		else if (element instanceof File) {
			return UniversalFileTransferUtility.downloadResourceToWorkspace((File)element, monitor);
		}
		else if (element instanceof IResource)
		{
			// if the src is an IResource, then this is our temp object
			return element;
		}

		return null;
	}

	/**
     * <i><b>Overridable</b> by subclasses, and is iff drag and drop supported.</i><br>
	 * Perform the drag on the given objects.  This default implementation simply iterates through the
	 * set.  For optimal performance, this should be overridden.
	 *
	 * @param set the set of objects to copy
	 * @param monitor the progress monitor
	 * @return a temporary workspace copies of the object that was copied
	 *
	 */
	public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor)
	{

		boolean supportsSearch = ((IRemoteFileSubSystemConfiguration)set.getSubSystem().getSubSystemConfiguration()).supportsSearch();
//		boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER);
		boolean doSuperTransferProperty = false; // disabling due to potential corruption
		if (!doSuperTransferProperty && supportsSearch)
		{
			//flatset will contain all FILES that will be copied to workspace in UniversalFileTransferUtility and create corresponding folders.  Empty folders will be ignored
			SystemRemoteResourceSet flatSet = new SystemRemoteResourceSet(set.getSubSystem(), set.getAdapter());
			long totalByteSize = getFlatRemoteResourceSet(set.getResourceSet(), flatSet, monitor);
			flatSet.setByteSize(totalByteSize);

			if (monitor != null)
			{
			    monitor.beginTask(_downloadMessage,  (int)totalByteSize);
			    //monitor.done();
			}

			//add folders to set that are being copied to the workspace in order to strip out empty folders in UniversalFileTransferUtility
			for (int i=0;i<set.size();i++)
			{
				IRemoteFile remoteFile = (IRemoteFile)set.get(i);
				//make sure it is a folder as files are being accounted for already
				if(remoteFile.isDirectory())
				{
					flatSet.addResource(remoteFile);
				}
			}

			try
			{
				//SystemWorkspaceResourceSet flatResult = UniversalFileTransferUtility.copyRemoteResourcesToWorkspace(flatSet, monitor);
				// for bug 209375, using multiple instead of single
				SystemWorkspaceResourceSet flatResult = UniversalFileTransferUtility.downloadResourcesToWorkspaceMultiple(flatSet, monitor);
				if (flatResult.hasMessage())
				{
					return flatResult;
				}
				else
				{
					SystemWorkspaceResourceSet hierarchicalResult = new SystemWorkspaceResourceSet();
					for (int i = 0; i < set.size(); i++)
					{
						IRemoteFile remoteFile = (IRemoteFile)set.get(i);
						IResource tempResource = UniversalFileTransferUtility.getTempFileFor(remoteFile);
						if (tempResource instanceof IContainer)
						{
							UniversalFileTransferUtility.discardReplicasOfDeletedFiles((IRemoteFileSubSystem)set.getSubSystem(), (IContainer)tempResource);
						}
						hierarchicalResult.addResource(tempResource);
					}
					return hierarchicalResult;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return UniversalFileTransferUtility.downloadResourcesToWorkspace(set, monitor);
		}
	}

	/**
	 * Helper method to get the local file subsystem.
	 * @return the local file subsystem
	 */
	private IRemoteFileSubSystem getLocalFileSubSystem()
	{
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost[] connections = registry.getHosts();
		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			IRemoteFileSubSystem anFS = RemoteFileUtility.getFileSubSystem(connection);
			if ( anFS != null && anFS.getHost().getSystemType().isLocal())
			{
				return anFS;
			}
		}

		return null;
	}

	protected long getFlatRemoteResourceSet(List initialResources, SystemRemoteResourceSet flatSet, IProgressMonitor monitor)
	{
		long totalByteSize = 0;
		List configList = new ArrayList();

		// create a search result set to contain all the results
		IHostSearchResultSet searchSet = new HostSearchResultSet();

		IRemoteFileSubSystem subsys = (IRemoteFileSubSystem)flatSet.getSubSystem();
		if (subsys instanceof FileServiceSubSystem)
		{
			FileServiceSubSystem ss = (FileServiceSubSystem)subsys;

			for (int i = 0; i < initialResources.size(); i++)
			{
				IRemoteFile remoteFile = (IRemoteFile)initialResources.get(i);

				// get all files within directory
				if (remoteFile.isDirectory())
				{
					SystemSearchString searchString = null;
					if (ArchiveHandlerManager.isVirtual(remoteFile.getAbsolutePath()))
					{
						//If this file to create flatset with is a virtual directory, we want to make sure the includeArchives flag
						//for the searchString is set to true.  This way, we could search inside the archive file
						searchString = new SystemSearchString("*", false, false, "*", false, true, true); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else
					{
						searchString = new SystemSearchString("*", false, false, "*", false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
					}

					// create the configuration for this folder
					IHostSearchResultConfiguration config = ss.createSearchConfiguration(searchSet, remoteFile, searchString);

					// kick off search for all files in the folder
					subsys.search(config);
					configList.add(config);
				}
				else
				{
					flatSet.addResource(remoteFile);
				}
			}
		}



		SubProgressMonitor submonitor = null;
		if (monitor != null)
		{
			submonitor = new SubProgressMonitor(monitor, configList.size());
			submonitor.setTaskName(FileResources.RESID_SEARCH_MESSAGE_SEARCHING);

		}
		// accumulate results
		for (int n = 0; n < configList.size(); n++)
		{
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)configList.get(n);
			while (config.getStatus() != IHostSearchConstants.FINISHED)
			{
				if (monitor != null)
				{
					if (monitor.isCanceled())
					{
						return totalByteSize;
					}
					Display display = Display.getCurrent();
					while (display!=null && display.readAndDispatch()) {
						//Process everything on event queue
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						//no action
					}
				}
				else
				{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						//no action
					}
				}
			}

			if (config.getStatus() == IHostSearchConstants.FINISHED)
			{
				if (submonitor != null)
					submonitor.worked(1);
				Object[] results = config.getResults();
				if (results == null || results.length == 0){
					// make sure search is really done
					System.out.println("waiting for results"); //$NON-NLS-1$

				}


				for (int m = 0; m < results.length; m++)
				{
					Object result = results[m];
					if (result instanceof IRemoteFile)
					{
						IRemoteFile file = (IRemoteFile)result;
						flatSet.addResource(file);
						totalByteSize += file.getLength();
					}
				}
			}
		}
		//submonitor.done();
		return totalByteSize;
	}

	protected long getFlatWorkspaceResourceSet(List resources, SystemWorkspaceResourceSet flatSet, IProgressMonitor monitor)
	{
		long totalBytes = 0;
		for (int i = 0; i < resources.size(); i++)
		{
			IResource resource = (IResource)resources.get(i);
			if (resource instanceof IFile)
			{
				IFile file = (IFile)resource;
				flatSet.addResource(file);
				File osFile = file.getLocation().toFile();
				totalBytes += osFile.length();
			}
			else if (resource instanceof IContainer)
			{
				IContainer container = (IContainer)resource;
				try
				{
					IResource[] members = container.members();
					totalBytes += getFlatWorkspaceResourceSet(members, flatSet, monitor);
				}
				catch (Exception e)
				{
				}
			}
		}
		return totalBytes;
	}

	protected long getFlatWorkspaceResourceSet(IResource[] resources, SystemWorkspaceResourceSet flatSet, IProgressMonitor monitor)
	{
		long totalBytes = 0;
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			if (resource instanceof IFile)
			{
				IFile file = (IFile)resource;
				flatSet.addResource(file);
				File osFile = file.getLocation().toFile();
				totalBytes += osFile.length();
			}
			else if (resource instanceof IContainer)
			{
				IContainer container = (IContainer)resource;
				try
				{
					IResource[] members = container.members();
					totalBytes += getFlatWorkspaceResourceSet(members, flatSet, monitor);
				}
				catch (Exception e)
				{
				}
			}
		}
		return totalBytes;
	}



	public static class RenameRunnable implements Runnable
	{
		private IRemoteFile _targetFileOrFolder;
		private String _newName;
		private List _namesInUse = new ArrayList();
		public RenameRunnable(IRemoteFile targetFileOrFolder)
		{
			_targetFileOrFolder = targetFileOrFolder;
		}
		public RenameRunnable(IRemoteFile targetFileOrFolder, List namesInUse)
		{
			_targetFileOrFolder = targetFileOrFolder;
			_namesInUse=namesInUse;
		}

		public void run() {
			ValidatorFileUniqueName validator = null;
			SystemRenameSingleDialog dlg;
			if(_namesInUse!=null && _namesInUse.size()>0)
			{
				dlg = new SystemRenameSingleDialog(null, true, _targetFileOrFolder, validator, _namesInUse); // true => copy-collision-mode
			}
			else
			{
				dlg = new SystemRenameSingleDialog(null, true, _targetFileOrFolder, validator); // true => copy-collision-mode
			}
			dlg.open();
			if (!dlg.wasCancelled())
				_newName = dlg.getNewName();
			else
				_newName = null;
		}

		public String getNewName()
		{
			return _newName;
		}
	}

	/**
	 *  Perform drop from the "fromSet" of objects to the "to" object
	 * @param fromSet the source objects for the drop
	 * @param target the target object for the drop
	 * @param sameSystemType indication of whether the source and target reside of the same type of system
	 * @param sameSystem indication of whether the source and target are on the same system
	 * @param srcType the type of objects to be dropped
	 * @param monitor the progress monitor
	 *
	 * @return the set of new objects created from the drop
	 *
	 */
	public ISystemResourceSet doDrop(ISystemResourceSet fromSet, Object target, boolean sameSystemType, boolean sameSystem, int srcType, IProgressMonitor monitor)
	{
		SystemRemoteResourceSet resultSet = new SystemRemoteResourceSet(getSubSystem(target), this);

		if (!sameSystem && sameSystemType)
		{
			fromSet = doDrag((SystemRemoteResourceSet)fromSet, monitor);
		}

		if (target instanceof IRemoteFile)
		{
			IRemoteFile targetFolder = (IRemoteFile) target;
			IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();
			boolean isWindows = !targetFS.getParentRemoteFileSubSystemConfiguration().isUnixStyle();

			// make sure properties are uptodate
			try
			{
				//targetFolder.markStale(true);
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath(), monitor);
				if (targetFolder == null)
					targetFolder = (IRemoteFile)target;
			}
			catch (Exception e)
			{
			}

			if (!targetFolder.canWrite() && !isWindows)  // windows check for bug 228743
			{
				String msgTxt = FileResources.FILEMSG_SECURITY_ERROR;
				String msgDetails = NLS.bind(FileResources.FILEMSG_SECURITY_ERROR_DETAILS, targetFS.getHostAliasName());
				SystemMessage errorMsg = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.FILEMSG_SECURITY_ERROR,
						IStatus.ERROR, msgTxt, msgDetails);
				resultSet.setMessage(errorMsg);
				return resultSet;
			}

			if (!targetFS.isConnected())
			{
				return null;
			}

			List set = fromSet.getResourceSet();
			if (set.size() > 0)
			{
				if (fromSet instanceof SystemWorkspaceResourceSet)
				{
					//boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER);
					boolean doSuperTransferProperty = false; // disabling due to potential corruption
					if (!doSuperTransferProperty)
					{
						SystemWorkspaceResourceSet flatFromSet = new SystemWorkspaceResourceSet();
						long totalByteSize = getFlatWorkspaceResourceSet(fromSet.getResourceSet(), flatFromSet, monitor);

					    if (monitor != null)
						{
					    	int size = (int)totalByteSize;
						    monitor.beginTask(_uploadMessage, size);
						}
						// back to hierarchy
						resultSet = UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)fromSet, targetFolder, monitor, true);
					}
					else
					{
						resultSet = UniversalFileTransferUtility.uploadResourcesFromWorkspace((SystemWorkspaceResourceSet)fromSet, targetFolder, monitor, true);
					}
					if (resultSet.hasMessage())
					{
						SystemMessage msg = resultSet.getMessage();
						if (monitor.isCanceled() && resultSet.size() > 0)
						{
							//Get the moved file names
							Object thisObject = resultSet.get(0);
							String copiedFileNames = null;
							if (thisObject instanceof IRemoteFile)
							{
								copiedFileNames = ((IRemoteFile)thisObject).getName();
								for (int i=1; i<(resultSet.size()); i++)
								{
									if (thisObject instanceof IRemoteFile)
									{
										copiedFileNames = copiedFileNames + "\n" + ((IRemoteFile)thisObject).getName(); //$NON-NLS-1$
									}
								}
							}
							//getMessage("RSEG1125").makeSubstitution(movedFileName));
							if (copiedFileNames != null)
							{
								String msgTxt = FileResources.FILEMSG_COPY_INTERRUPTED;
								String msgDetails = NLS.bind(FileResources.FILEMSG_COPY_INTERRUPTED_DETAILS, copiedFileNames);

								SystemMessage thisMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
										ISystemFileConstants.FILEMSG_COPY_INTERRUPTED,
										IStatus.ERROR, msgTxt, msgDetails);
								resultSet.setMessage(thisMessage);
								//SystemMessageDialog.displayErrorMessage(shell, thisMessage);
							}
						}
					}
					return resultSet;
				}
				else if (fromSet instanceof SystemRemoteResourceSet)
				{
					SystemRemoteResourceSet rmtSet = (SystemRemoteResourceSet)fromSet;

					//ISystemDragDropAdapter srcAdapter = rmtSet.getAdapter();
					ISubSystem srcSubSystem = rmtSet.getSubSystem();

					Object first = set.get(0);
					if (first instanceof ISystemFilterReference)
					{
						SystemWorkspaceResourceSet downloadedFilterResults = new SystemWorkspaceResourceSet();
						for (int i = 0; i < set.size(); i++)
						{
							ISystemFilterReference ref = (ISystemFilterReference)set.get(i);
							SystemFilterReference filterReference = (SystemFilterReference) ref;


							Object[] children = null;
							try
							{
								children = ((SubSystem)srcSubSystem).internalResolveFilterStrings(filterReference.getReferencedFilter().getFilterStrings(), monitor);
							}
							catch (Exception e)
							{
							}

							if (children != null)
							{
							for (int c = 0; c < children.length; c++)
							{
								Object child = children[c];

								if (child instanceof IAdaptable)
								{
									Object newSrc = child;

									if (srcSubSystem != targetFS)
									{
										ISystemDragDropAdapter cAdapter = (ISystemDragDropAdapter) ((IAdaptable) child).getAdapter(ISystemDragDropAdapter.class);
										newSrc = cAdapter.doDrag(child, sameSystemType, monitor);
										if (newSrc instanceof SystemMessage)
										{
											resultSet.setMessage((SystemMessage)newSrc);
											return resultSet;
										}
										else
										{
											downloadedFilterResults.addResource(newSrc);
										}
									}
								}
							}
							}
						}

						return doDrop(downloadedFilterResults, target, sameSystemType, srcSubSystem == targetFS, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE, monitor);

					}
					else if (first instanceof IRemoteFile)
					{
						List toCopy = new ArrayList();
						List toCopyNames = new ArrayList();
						//List toCopyBatch = new ArrayList();
						List existing = new ArrayList();
						boolean overwrite=false;

						for (int i = 0; i < set.size(); i++)
						{
							IRemoteFile srcFileOrFolder = (IRemoteFile)set.get(i);
							if (!srcFileOrFolder.exists())
							{
								String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND,
										srcFileOrFolder.getAbsolutePath(),
										srcFileOrFolder.getHost().getAliasName());

								SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
										ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
										IStatus.ERROR, msgTxt);
								resultSet.setMessage(errorMessage);
								return resultSet;
							}
							/* DKM - not sure what this is doing here...
							 *    maybe there used to be a check for an archive
							if (!srcFileOrFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement())
							{
								SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_ARCHIVEMANAGEMENT_NOTSUPPORTED);
								resultSet.setMessage(errorMessage);
								return resultSet;
							}
							*/


							String name = srcFileOrFolder.getName();
							String originalName = srcFileOrFolder.getName();
							int count = 1;
							boolean go = true;

							// same systemfor
							if (sameSystem)
							{
								try
								{
									if (!targetFolder.getAbsolutePath().equals(srcFileOrFolder.getAbsolutePath()))
									{
										//Handle resources being copied to their parent folder.  Name = "Copy of " + name
										if(targetFolder.getAbsolutePath().equals(srcFileOrFolder.getParentRemoteFile().getAbsolutePath()))
										{
											name = MessageFormat.format(FileResources.RESID_CONFLICT_COPY_PATTERN, new Object[] {
												    new Integer(count), originalName });
											while(go)
											{
												IRemoteFile existingFileOrFolder = ((IRemoteFileSubSystem)srcSubSystem).getRemoteFileObject(targetFolder, name, monitor);
												if (existingFileOrFolder.exists())
												{
													count++;
													name = MessageFormat.format(FileResources.RESID_CONFLICT_COPY_PATTERN, new Object[] {
														    new Integer(count), originalName });
												}
												else
												{
													toCopy.add(srcFileOrFolder);
													toCopyNames.add(name);
													go = false;
												}
											}
										}
										else
										{
											// should be better doing a query for all in the set
											IRemoteFile existingFileOrFolder = ((IRemoteFileSubSystem)srcSubSystem).getRemoteFileObject(targetFolder, name, monitor);
											if (existingFileOrFolder.exists())
											{
												/*RenameRunnable rr = new RenameRunnable(existingFileOrFolder, toCopyNames);
												Display.getDefault().syncExec(rr);
												name = rr.getNewName();
												*/
												existing.add(existingFileOrFolder);
											}

											if (name != null)
											{
												toCopy.add(srcFileOrFolder);
												toCopyNames.add(name);
												//toCopyBatch.add(srcFileOrFolder);
											}
											/*else if (name != null)
											{
												toCopyBatch.add(srcFileOrFolder);
											}*/
										}
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
							else // why not same system? should we even get here?
							{
								//System.out.println("HOW DID I GET HERE?!!");
							}
						}


						if(existing.size()>0)
						{
							CopyRunnable rr = new CopyRunnable(existing);
							Display.getDefault().syncExec(rr);
							overwrite = rr.getOk();
						}



						//Following code used originally with the rename dialog which no longer exists
						//Resources will be copied with same names if an overwrite is desired from the user
						//Resources that are copied to their parent will be renamed to "Copy of " + name of source
						if(existing.size()==0 || overwrite)
						{
							for (int x = 0; x < toCopy.size(); x++)
							{

								IRemoteFile srcFileOrFolder = (IRemoteFile)toCopy.get(x);
								String name = (String)toCopyNames.get(x);


							SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPY_PROGRESS);
							copyMessage.makeSubstitution(srcFileOrFolder.getName(), targetFolder.getName());
							if (monitor != null)
							{
								monitor.beginTask(copyMessage.getLevelOneText(), 100);
							}

								try
								{
									targetFS.copy(srcFileOrFolder, targetFolder, name, monitor);
									IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, name, monitor);
									resultSet.addResource(copiedFile);
								}
								catch (SystemMessageException e)
								{
									if (monitor.isCanceled() && resultSet.size() > 0)
									{
										//Get the moved file names
										Object thisObject = resultSet.get(0);
										String copiedFileNames = null;
										if (thisObject instanceof IRemoteFile)
										{
											copiedFileNames = ((IRemoteFile)thisObject).getName();
											for (int i=1; i<(resultSet.size()); i++)
											{
												if (thisObject instanceof IRemoteFile)
												{
													copiedFileNames = copiedFileNames + "\n" + ((IRemoteFile)thisObject).getName(); //$NON-NLS-1$
												}
											}
										}
										//getMessage("RSEG1125").makeSubstitution(movedFileName));
										if (copiedFileNames != null)
										{
											String msgTxt = FileResources.FILEMSG_COPY_INTERRUPTED;
											String msgDetails = NLS.bind(FileResources.FILEMSG_COPY_INTERRUPTED_DETAILS, copiedFileNames);

										SystemMessage thisMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
												ISystemFileConstants.FILEMSG_COPY_INTERRUPTED,
												IStatus.ERROR, msgTxt, msgDetails);
										SystemMessageDialog.displayErrorMessage(shell, thisMessage);
										}
										else
										{
											SystemMessageDialog.displayMessage(e);
										}
									}
									else
									{
										SystemMessageDialog.displayMessage(e);
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
						// deal with batch copies now
						/*if(existing.size()==0 || overwrite)
						{
							IRemoteFile[] srcFileOrFolders = new IRemoteFile[toCopyBatch.size()];
							for (int x = 0; x < toCopyBatch.size(); x++)
							{
								srcFileOrFolders[x] = (IRemoteFile)toCopyBatch.get(x);
							}
							if (toCopyBatch.size() > 0)
							{
								try
								{
									if (targetFS.copyBatch(srcFileOrFolders, targetFolder, monitor))
									{
										for (int x = 0; x < toCopyBatch.size(); x++)
										{
											IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, srcFileOrFolders[x].getName(), monitor);
											resultSet.addResource(copiedFile);
										}
									}
								}
								catch (SystemMessageException e)
								{
									if (monitor.isCanceled() && srcFileOrFolders.length > 1)
									{
										//ISystemViewElementAdapter adapter = fromSet.getViewAdapter();
										for (int i = 0; i < srcFileOrFolders.length; i++)
										{
											IRemoteFile thisCopiedFile = null;
											try
											{
												thisCopiedFile = targetFS.getRemoteFileObject(targetFolder, srcFileOrFolders[i].getName(), null);
											}
											catch (SystemMessageException thsiException)
											{
												thsiException.printStackTrace();
												thisCopiedFile = null;
											}
											if (thisCopiedFile != null && thisCopiedFile.exists())
											{
												//This object has been deleted
												resultSet.addResource(thisCopiedFile);
											}
										}
										if (resultSet.size() > 0)
										{
											//Get the copied file names
											Object thisObject = resultSet.get(0);
											String copiedFileNames = null;
											copiedFileNames = ((IRemoteFile)thisObject).getName();
											for (int i=1; i<(resultSet.size()); i++)
											{
												thisObject = resultSet.get(i);
												copiedFileNames = copiedFileNames + "\n" + ((IRemoteFile)resultSet.get(i)).getName(); //$NON-NLS-1$
											}
											String msgTxt = FileResources.FILEMSG_COPY_INTERRUPTED;
											String msgDetails = NLS.bind(FileResources.FILEMSG_COPY_INTERRUPTED_DETAILS, copiedFileNames);

											SystemMessage thisMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
													ISystemFileConstants.FILEMSG_COPY_INTERRUPTED,
													IStatus.ERROR, msgTxt, msgDetails);
											SystemMessageDialog.displayErrorMessage(shell, thisMessage);
										}
										else
										{
											SystemMessageDialog.displayMessage(e);
										}
									}
									else
									{
										SystemMessageDialog.displayMessage(e);
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}*/
					}
				}
			}
		}
		return resultSet;
	}
	/**
	 * Perform a copy via drag and drop.
	 * @param src the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param target the object to be copied to.
	 * @param sameSystemType indication of whether the source and target reside on the same type of system
	 * @param sameSystem an indication whether the target and source reside on the same type of system
	 * @param sourceType type of source being transferred
	 * @param monitor the progress monitor
	 * @return an indication whether the operation was successful or not.
	 */
	public Object doDrop(Object src, Object target, boolean sameSystemType, boolean sameSystem, int sourceType, IProgressMonitor monitor)
	{
		Object result = null;

		// same system type but not necessarily same computer
		if (sameSystemType && !sameSystem)
		{
			src = doDrag(src, sameSystem, monitor);
		}


		if (target instanceof IRemoteFile)
		{
			IRemoteFile targetFolder = (IRemoteFile) target;
			IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();

			// make sure properties are uptodate
			try
			{
				//targetFolder.markStale(true);
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath(), monitor);
				if (targetFolder == null)
					targetFolder = (IRemoteFile)target;
			}
			catch (Exception e)
			{
			}

			if (!targetFolder.canWrite())
			{
				String msgTxt = FileResources.FILEMSG_SECURITY_ERROR;
				String msgDetails = NLS.bind(FileResources.FILEMSG_SECURITY_ERROR_DETAILS, targetFS.getHostAliasName());
				SystemMessage errorMsg = null;

				errorMsg = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.FILEMSG_SECURITY_ERROR,
						IStatus.ERROR, msgTxt, msgDetails);

				return errorMsg;
			}

			if (!targetFS.isConnected())
			{
				return null;
			}

			// non-Eclipse file transfer

			if (sourceType == SystemDNDTransferRunnable.SRC_TYPE_OS_RESOURCE)
			{
				if (src instanceof String)
				{
					IRemoteFileSubSystem localFS = getLocalFileSubSystem();

					try
					{
						if (localFS != null) {
							IRemoteFile srcFileOrFolder = localFS.getRemoteFileObject((String)src, monitor);
							return doDrop(srcFileOrFolder, target, true, sameSystem, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE, monitor);
						}
						else {
							File srcFileOrFolder = new File((String)src);
							return doDrop(srcFileOrFolder, target, true, sameSystem, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE, monitor);
						}
					}
					catch (SystemMessageException e)
					{
						return e.getSystemMessage();
					}
				}
			}
			if (sourceType == SystemDNDTransferRunnable.SRC_TYPE_TEXT)
			{
				if (src instanceof String)
				{
					// noop for now
				}
			}


			if (sourceType == SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE || sourceType == SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE)
			{
				// Eclipse resource transfer
				if (src instanceof IResource)
				{
					IResource srcFileOrFolder = (IResource) src;
					return UniversalFileTransferUtility.uploadResourceFromWorkspace(srcFileOrFolder, targetFolder, monitor, true);
				}
			}


			// RSE remote file transfer on same system
			if (sourceType == SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE)
			{
				if (src instanceof ISystemFilterReference)
				{
					SystemFilterReference filterReference = (SystemFilterReference) src;
					Object adapter = filterReference.getAdapter(ISystemViewElementAdapter.class);
					ISystemViewElementAdapter filterAdapter = (ISystemViewElementAdapter) adapter;
					if (filterAdapter != null)
					{
						SubSystem filterSubSystem = (SubSystem) filterAdapter.getSubSystem(filterReference);
						Object[] children = null;
						try
						{
							children = filterSubSystem.internalResolveFilterStrings(filterReference.getReferencedFilter().getFilterStrings(), monitor);
						}
						catch (Exception e)
						{
						}

						if (children != null)
						{
						for (int c = 0; c < children.length; c++)
						{
							Object child = children[c];

							if (child instanceof IAdaptable)
							{
								Object newSrc = child;

								if (filterSubSystem != targetFS)
								{
									ISystemDragDropAdapter cAdapter = (ISystemDragDropAdapter) ((IAdaptable) child).getAdapter(ISystemDragDropAdapter.class);
									newSrc = cAdapter.doDrag(child, sameSystemType, monitor);
									if (newSrc instanceof SystemMessage)
									{
										return newSrc;
									}
								}
								doDrop(newSrc, target, sameSystemType, filterSubSystem == targetFS, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE, monitor);
							}
						}
						}
						return target;
					}
				}
				else if (src instanceof IRemoteFile)
				{
					try
					{
						IRemoteFile srcFileOrFolder = (IRemoteFile) src;
						if (!srcFileOrFolder.exists())
						{
							String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND,
									srcFileOrFolder.getAbsolutePath(),
									srcFileOrFolder.getHost().getAliasName());

							SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
									ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
									IStatus.ERROR, msgTxt);
							return errorMessage;
						}

						String msgTxt = NLS.bind(CommonMessages.MSG_COPY_PROGRESS, srcFileOrFolder.getName(), targetFolder.getAbsolutePath());
						SystemMessage copyMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
								ICommonMessageIds.MSG_COPY_PROGRESS,
								IStatus.INFO, msgTxt);


						IRemoteFileSubSystem localFS = srcFileOrFolder.getParentRemoteFileSubSystem();

						String name = srcFileOrFolder.getName();

						if (localFS == targetFS)
						{
							if (!targetFolder.getAbsolutePath().equals(srcFileOrFolder.getAbsolutePath()))
							{
								IRemoteFile existingFileOrFolder = localFS.getRemoteFileObject(targetFolder, name, monitor);

								if (existingFileOrFolder.exists())
								{
									RenameRunnable rr = new RenameRunnable(existingFileOrFolder);
									Display.getDefault().syncExec(rr);
									name = rr.getNewName();
								}

								if (name != null)
								{
									monitor.subTask(copyMessage.getLevelOneText());
									targetFS.copy(srcFileOrFolder, targetFolder, name, monitor);
									IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, name, monitor);
									return copiedFile;
								}
							}
						}
						else
						{
							// Not sure how we can get here since if the source and target subsystems are different, then a doDrag() needs to
							// occur, resulting in a local resource (i.e. IFile) rather than a remote resource (i.e. IRemoteFile).
							// TODO investigate to see if we can get rid of this code
							if (srcFileOrFolder.isFile())
							{
								try
								{
									name = checkForCollision(getShell(), targetFolder, name);
									if (name == null)
									{
										return null;
									}

									boolean isTargetArchive = targetFolder.isArchive();
									StringBuffer newPathBuf = new StringBuffer(targetFolder.getAbsolutePath());
									if (isTargetArchive)
									{
										newPathBuf.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
									}
									else
									{
										newPathBuf.append(targetFolder.getSeparatorChar());
									}
									newPathBuf.append(name);

									String newPath = newPathBuf.toString();

									monitor.subTask(copyMessage.getLevelOneText());

									targetFS.upload(srcFileOrFolder.getAbsolutePath(), SystemEncodingUtil.ENCODING_UTF_8, newPath, System.getProperty("file.encoding"), monitor); //$NON-NLS-1$

									result = targetFS.getRemoteFileObject(targetFolder, name, monitor);
									return result;

								}
								catch (SystemMessageException e)
								{
									return e.getSystemMessage();

								}
								catch (Exception e)
								{
								}

								return null;
							}
							else
							{
								// this is a directory
								// recursively copy
								boolean isTargetArchive = targetFolder.isArchive();
								StringBuffer newPathBuf = new StringBuffer(targetFolder.getAbsolutePath());
								if (isTargetArchive)
								{
									newPathBuf.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
								}
								else
								{
									newPathBuf.append(targetFolder.getSeparatorChar());
								}
								newPathBuf.append(name);

								String newPath = newPathBuf.toString();

								IRemoteFile newTargetFolder = targetFS.getRemoteFileObject(newPath, monitor);
								targetFS.createFolder(newTargetFolder, monitor);

								IRemoteFile[] children = localFS.list(srcFileOrFolder, monitor);
								if (children != null)
								{
									for (int i = 0; i < children.length; i++)
									{
										if (monitor.isCanceled())
										{
											return null;
										}
										else
										{
											IRemoteFile child = children[i];
											if (doDrop(child, newTargetFolder, sameSystemType, sameSystem, sourceType, monitor) == null)
											{
												return null;
											}
										}
									}
								}

								return newTargetFolder;
							}
						}
					}
					catch (SystemMessageException e)
					{
						return e.getSystemMessage();
					}
				}
			}
		}


		return result;
	}

	protected String checkForCollision(Shell shell, IRemoteFile targetFolder, String oldName)
	{
		String newName = oldName;

		try
		{

			IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
			IRemoteFile targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName, new NullProgressMonitor());

			//RSEUIPlugin.logInfo("CHECKING FOR COLLISION ON '"+srcFileOrFolder.getAbsolutePath() + "' IN '" +targetFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...TARGET FILE: '"+tgtFileOrFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...target.exists()? "+tgtFileOrFolder.exists());
			if (targetFileOrFolder.exists())
			{
				RenameRunnable rr = new RenameRunnable(targetFileOrFolder);
				Display.getDefault().syncExec(rr);
				newName = rr.getNewName();
			}
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e); //$NON-NLS-1$
		}

		return newName;
	}

	// FOR COMMON DELETE ACTIONS
	/**
	 * Yes, remote file objects are deletable!
	 */
	public boolean canDelete(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
    	boolean offline = file.getParentRemoteFileSubSystem().isOffline();
    	
    	if (offline || file.isRoot()){
    		return false;
    	}
    	else {
    		/*
    		if (file.getHost().getSystemType().isWindows()){
    			return true;
    		}
    		else {
    			// for deletion, you need write access to the containing directory
    			IRemoteFile parentFile = file.getParentRemoteFile();
    			return parentFile.canWrite();
    		}*/
    		return true;
    	}
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * Defers request to the remote file subsystem.
	 */
	public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) throws Exception
	{
		boolean ok;
		IRemoteFile file = (IRemoteFile) element;
		IRemoteFile parentFile = file.getParentRemoteFile();

		IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();
		try
		{
			//  DKM - propagate the deletion to temp files project
			IResource tmp = UniversalFileTransferUtility.getTempFileFor(file);
			if (tmp.exists())
			{
				try
				{
					tmp.delete(false, null);
					
					// get rid of associated editable if there was one before
					SystemIFileProperties properties = new SystemIFileProperties(tmp);
					properties.setRemoteFileObject(null);					
				}
				catch (Exception e)
				{
				}
			}

			ss.delete(file, monitor);
			ok = true;
			file.markStale(true);
			parentFile.markStale(true);
		}
		catch (Exception exc)
		{
			ok = false;
			String msgTxt = NLS.bind(FileResources.FILEMSG_DELETE_FILE_FAILED, file.toString());
			String msgDetails = FileResources.FILEMSG_DELETE_FILE_FAILED_DETAILS;

			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.FILEMSG_DELETE_FILE_FAILED,
					IStatus.ERROR, msgTxt, msgDetails);
			SystemMessageDialog.displayErrorMessage(shell, msg);
		}
		return ok;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Defers request to the remote file subsystem
	 */
	public boolean doDeleteBatch(Shell shell, List resourceSet, IProgressMonitor monitor) throws Exception
	{
		IRemoteFileSubSystem ss = null;
		IRemoteFile[] files = new IRemoteFile[resourceSet.size()];
		for (int i = 0; i < resourceSet.size(); i++)
		{
			IRemoteFile file = (IRemoteFile) resourceSet.get(i);
			files[i] = file;
			IRemoteFile parentFile = file.getParentRemoteFile();
			if (ss == null) ss = file.getParentRemoteFileSubSystem();
			try
			{
				//  DKM - propagate the deletion to temp files project
				IResource tmp = UniversalFileTransferUtility.getTempFileFor(file);
				if (tmp.exists())
				{
					try
					{
						tmp.delete(false, null);
					}
					catch (Exception e)
					{
					}
				}
				file.markStale(true);
				parentFile.markStale(true);
			}
			catch (Exception exc)
			{
				String msgTxt = NLS.bind(FileResources.FILEMSG_DELETE_FILE_FAILED, file.toString());
				String msgDetails = FileResources.FILEMSG_DELETE_FILE_FAILED_DETAILS;

				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.FILEMSG_DELETE_FILE_FAILED,
						IStatus.ERROR, msgTxt, msgDetails);
				SystemMessageDialog.displayErrorMessage(shell, msg);
			}
		}
		if (ss != null)
		{
			ss.deleteBatch(files, monitor);
		}
		return true;
	}
	// FOR COMMON RENAME ACTIONS
	/**
	 * Yes, remote file objects are renamable!
	 */
	public boolean canRename(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
    	boolean offline = file.getParentRemoteFileSubSystem().isOffline();
		return !file.isRoot() && file.canRead() && !offline;
	}

	private void moveTempResource(IResource localResource, IPath newLocalPath, IRemoteFileSubSystem ss, String newRemotePath)
	{
		if (localResource != null)
		{
			try
			{
				moveTempFileProperties(localResource, ss, newRemotePath);
				localResource.move(newLocalPath, true, null);

			}
			catch (Exception e)
			{
				SystemBasePlugin.logError(e.getLocalizedMessage(), e);
			}

		}
	}

	private void moveTempFileProperties(IResource localResource, IRemoteFileSubSystem ss, String remotePath)
	{

		if (localResource instanceof IContainer)
		{
			IContainer localContainer = (IContainer) localResource;
			try
			{
				IResource[] members = localContainer.members();
				for (int i = 0; i < members.length; i++)
				{
					IResource member = members[i];
					moveTempFileProperties(member, ss, remotePath + "/" + member.getName()); //$NON-NLS-1$
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (localResource instanceof IFile)
		{
			IFile localFile = (IFile)localResource;
			try
			{
				SystemIFileProperties properties = new SystemIFileProperties(localFile);
				properties.setRemoteFilePath(remotePath);

				Object editableObj = properties.getRemoteFileObject();
				if (editableObj != null)
				{
					SystemEditableRemoteFile editable = (SystemEditableRemoteFile)editableObj;
			
					// change the remote file regardless of whether it's open in an editor or not
					// there's an in-memory editable, so change the associated remote file
					IRemoteFile newRemoteFile = ss.getRemoteFileObject(remotePath, new NullProgressMonitor());
					editable.setRemoteFile(newRemoteFile);

				}
			}
			catch (Exception e)
			{
			}

		}

	}

	/**
	 * Perform the rename action. Defers request to the remote file subsystem
	 */
	public boolean doRename(Shell shell, Object element, String newName, IProgressMonitor monitor) throws Exception
	{
		IRemoteFile file = (IRemoteFile) element;
		IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();


			String newRemotePath = file.getParentPath() + "/" + newName; //$NON-NLS-1$
			IResource localResource = null;
			IResource localProjectResource = null;
			if (file.getHost().getSystemType().isLocal())
			{
				if (file.isFile()) {
					localProjectResource = getProjectFileForLocation(file.getAbsolutePath());
				}
				else {
					localProjectResource = getProjectFolderForLocation(file.getAbsolutePath());
				}
			}
			if (localProjectResource != null) {
				//This is a local project file.  So we will rename it directly in the workbench.
				IPath newLocalPath = localProjectResource.getParent().getFullPath().append(newName);
				localProjectResource.move(newLocalPath, true, null);
				return true;
			}
			if (SystemRemoteEditManager.getInstance().doesRemoteEditProjectExist())
			{
				localResource = UniversalFileTransferUtility.getTempFileFor(file);
			}

			ss.rename(file, newName, monitor);
			if (localResource != null && localResource.exists())
			{

				IPath newLocalPath = localResource.getParent().getFullPath().append(newName);
				moveTempResource(localResource, newLocalPath, ss, newRemotePath);
			}

			// Firing a refresh event before a rename event will cause views to refresh
			// but the TreeItems contain the old data, and refresh will display an error
			// below the TreeItem saying it's not readable as it's using the old name.
			// This is not an issue for the SystemView as it does the refresh in a job, so
			// the rename event is actually handled first then the refresh.
			// Commented out for bug #198576
//			if (file.isDirectory())
//			{
//				// update all tree views showing this remote folder...
//				// Hmm, why do we do this, given SystemView sends a rename event? I think we needed to refresh all child cached references to parent folder name...
//				SystemResourceChangeEvent event = new SystemResourceChangeEvent(file.getParentRemoteFile(), ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null);
//				sr.fireEvent(event);
//				//sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED, file, file.getParentRemoteFile(), file.getParentRemoteFileSubSystem(), null, null);
//			}
//			file.markStale(true);
			return true;

	}

	/**
	 * Return a validator for verifying the new name is correct.
	 * Defers request to the subsystem factory, calling either getFileNameValidator or getFolderNameValidator.
	 */
	public ISystemValidator getNameValidator(Object element)
	{
		if (element instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) element;
			if (file.isDirectory())
				return file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().getFolderNameValidator();
			else
				return file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().getFileNameValidator();
		}
		return null;
	}
	/**
	 * Parent override.
	 * <p>
	 * Form and return a new canonical (unique) name for this object, given a candidate for the new
	 *  name. This is called by the generic multi-rename dialog to test that all new names are unique.
	 *  To do this right, sometimes more than the raw name itself is required to do uniqueness checking.
	 * <p>
	 * Returns newName, but uppercased for Windows file systems
	 */
	public String getCanonicalNewName(Object element, String newName)
	{
		if (element instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) element;
			if (file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().isUnixStyle())
				return newName;
			else
				return newName.toUpperCase();
		}
		else
			return newName;
	}
	/**
	 * Parent override.
	 * <p>
	 * Compare the name of the given element to the given new name to decide if they are equal.
	 * Allows adapters to consider case and quotes as appropriate.
	 * <p>
	 * For Unix/Linux, returns the result of getName(element).equals(newName), which is to say,
	 * it is a case sensitive compare. For windows, it is case insensitive (equalsIgnoreCase).
	 */
	public boolean namesAreEqual(Object element, String newName)
	{
		if (element instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) element;
			if (file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().isUnixStyle())
				return getName(element).equals(newName);
			else
				return getName(element).equalsIgnoreCase(newName);
		}
		else
			return super.namesAreEqual(element, newName);
	}
	// ------------------------------------------------------------
	// METHODS FOR SAVING AND RESTORING EXPANSION STATE OF VIEWER...
	// ------------------------------------------------------------
	/**
	 * Return a short string to uniquely identify the type of resource. Eg "conn" for connection.
	 * This just defaults to getType, but if that is not sufficient override it here, since that is
	 * a translated string.
	 */
	public String getMementoHandleKey(Object element)
	{
		return ISystemMementoConstants.MEMENTO_KEY_REMOTE;
	}

	// --------------------------------------------------------------------
	// METHODS PRESCRIBED BY THE ISYSTEMREMOTEELEMENT ADAPTER INTERFACE...
	// --------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier#getAbsoluteName(java.lang.Object)
	 */
	public String getAbsoluteName(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		return file.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#getAbsoluteParentName(java.lang.Object)
	 */
	public String getAbsoluteParentName(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		return file.getParentPath();
	}

	/**
	 * Return subsystem
	 */
	public ISubSystem getSubSystem(Object element)
	{
		if (element instanceof IRemoteFile)
		{
			IRemoteFile file = (IRemoteFile) element;
			return file.getParentRemoteFileSubSystem();
		}
		return super.getSubSystem(element);
	}
	/**
	 * Return the subsystem factory id that owns this remote object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getSubSystemConfigurationId(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		return file.getParentRemoteFileSubSystem().getSubSystemConfiguration().getId();
	}

	/**
	 * Return a value for the type category property for this object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getRemoteTypeCategory(Object element)
	{
		return ISystemFileRemoteTypes.TYPECATEGORY;
	}
	/**
	 * Return the untranslated type for this object.
	 * For files, returns the "file"
	 * For folders or roots, returns "folder"
	 */
	public String getRemoteType(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		if (!file.isRoot() && !file.isDirectory())
			return ISystemFileRemoteTypes.TYPE_FILE;
		else
			return ISystemFileRemoteTypes.TYPE_FOLDER;
	}
	/**
	 * Return the untranslated subtype for this object.
	 * For files, returns the file extension. Eg, for abc.java this returns "java".
	 * For folders, returns "root" or "subfolder"
	 */
	public String getRemoteSubType(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		if (file.isFile())
			return file.getExtension();
		else if (file.isRoot())
			return ISystemFileRemoteTypes.SUBTYPE_ROOT;
		else
			return ISystemFileRemoteTypes.SUBTYPE_SUBFOLDER;
	}
	/**
	 * Return the untranslated sub-subtype for this object.
	 * Returns null for now.
	 */
	public String getRemoteSubSubType(Object element)
	{
		return null;
	}
	/**
	 * Return the source type of the selected object. Typically, this only makes sense for compilable
	 *  source members. For non-compilable remote objects, this typically just returns null.
	 * <p>
	 * For files, this returns the extension. For folders, it returns null.
	 */
	public String getRemoteSourceType(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		String srcType = null;
		if (file.isFile())
		{
			srcType = file.getExtension();
			if (srcType == null)
				srcType = "blank"; //$NON-NLS-1$
			else if (srcType.length() == 0)
				srcType = "null"; //$NON-NLS-1$
		}
		return srcType;
	}
	/**
	 * Some view has updated the name or properties of this remote object. As a result, the
	 *  remote object's contents need to be refreshed. You are given the old remote object that has
	 *  old data, and you are given the new remote object that has the new data. For example, on a
	 *  rename the old object still has the old name attribute while the new object has the new
	 *  new attribute.
	 * <p>
	 * This is called by viewers like SystemView in response to rename and property change events.
	 * <p>
	 * @param oldElement the element that was found in the tree
	 * @param newElement the updated element that was passed in the REFRESH_REMOTE event
	 * @return true if you want the viewer that called this to refresh the children of this object,
	 *   such as is needed on a rename of a folder, say.
	 */
	public boolean refreshRemoteObject(Object oldElement, Object newElement)
	{
		if ((oldElement instanceof RemoteFile) && (newElement instanceof IRemoteFile))
		{
			RemoteFile oldFile = (RemoteFile)oldElement;
			RemoteFile newFile = (RemoteFile)newElement;

			if (       (oldFile != newFile)
					&& (       (oldFile.isFile() && newFile.isFile())
							|| (oldFile.isDirectory() && newFile.isDirectory()) )) {
				oldFile.getHostFile().renameTo(newFile.getAbsolutePath());
			}

			return true;
		}

		return false;
	}

	/**
	 * Given a remote object, returns it remote parent object. Eg, given a file, return the folder
	 *  it is contained in.
	 * <p>
	 * The shell is required in order to set the cursor to a busy state if a remote trip is required.
	 *
	 * @return an IRemoteFile object for the parent
	 */
	public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception
	{
		return ((IRemoteFile) element).getParentRemoteFile();
	}
	/**
	 * Given a remote object, return the unqualified names of the objects contained in that parent. This is
	 *  used for testing for uniqueness on a rename operation, for example. Sometimes, it is not
	 *  enough to just enumerate all the objects in the parent for this purpose, because duplicate
	 *  names are allowed if the types are different, such as on iSeries. In this case return only
	 *  the names which should be used to do name-uniqueness validation on a rename operation.
	 *
	 * @return an array of all file and folder names in the parent of the given IRemoteFile object
	 */
	public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception
	{		String[] names = EMPTY_STRING_LIST;

		IRemoteFile file = (IRemoteFile) element;
		String parentName = file.getParentPath();
		if (parentName == null) // given a root?
			return names; // not much we can do. Should never happen: you can't rename a root!

		// changed to do the same as the new file wizards since
		// as per bug 251860, we can't use the cache and we need to bypass the hidden preference
		IRemoteFile parentFolder = file.getParentRemoteFile();
		IRemoteFile[] children = parentFolder.getParentRemoteFileSubSystem().list(parentFolder, monitor);

		if ((children == null) || (children.length == 0))
			return names;

		names = new String[children.length];
		for (int idx = 0; idx < names.length; idx++)
		{
			names[idx] = (children[idx]).getName();
		}

		return names;
	}

	/**
	 * User has double clicked on an object. We want to open the object in the applicable editor.
	 * Return true to indicate that we are handling the double-click event.
	 */
	public boolean handleDoubleClick(Object element)
	{
		IRemoteFile remoteFile = (IRemoteFile) element;
		if (!remoteFile.canRead())
		{
		    return false;
		}
		/** FIXME commands now separate from this
		else if (testAttribute(remoteFile, "classification", "*executable*") ||
		    testAttribute(remoteFile, "classification", "*script") ||
		    testAttribute(remoteFile, "classification", "symbolic link(script)*")
		)
		{
		   // instead of opening in editor
		    // attempt to execute it
		   return RemoteCommandHelpers.runUniversalCommand(getShell(), remoteFile.getName(), remoteFile.getParentPath(),
		           remoteFile.getParentRemoteFileSubSystem().getCommandSubSystem());
		}
		*/
		else if (!remoteFile.isArchive() || !remoteFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement())
		{
			// make sure we're using the latest version of remoteFile
			try {
				remoteFile = remoteFile.getParentRemoteFileSubSystem().getRemoteFileObject(remoteFile.getAbsolutePath(), new NullProgressMonitor());
			}
			catch (Exception e){				
			}
			
			// only handle double click if object is a file
			String absolutePath = remoteFile.getAbsolutePath();
			IFile localFile = null;
			if (remoteFile.getHost().getSystemType().isLocal())
			{
				localFile = getProjectFileForLocation(absolutePath);
			}
			if (localFile != null) {
				try {
					if (!localFile.exists()) {
						
						localFile.refreshLocal(IResource.DEPTH_ZERO, null);
					}
					openEditor(localFile,  !remoteFile.canWrite());
				}
				catch (Exception e) {
					SystemBasePlugin.logError(e.getLocalizedMessage(), e);
					return false;
				}
				return true;
			}
			ISystemEditableRemoteObject editable = getEditableRemoteObject(remoteFile);	
			if (editable != null)
			{
				String remotePath = remoteFile.getAbsolutePath();
				String replicaRemotePath = editable.getAbsolutePath();
				// first make sure that the correct remote file is referenced (might be difference because of different case)
				if (!replicaRemotePath.equals(remotePath)){ // for bug 276103
					
					IEditorPart editor = editable.getEditorPart();
					boolean editorWasClosed = false;
					if (editor == null){
						editorWasClosed = true;
					}
					else if (editor.isDirty()){
						editorWasClosed = editor.getEditorSite().getPage().closeEditor(editor, true);
						if (editorWasClosed)
							editable.doImmediateSaveAndUpload();								
					}
					else {
						editorWasClosed = editor.getEditorSite().getPage().closeEditor(editor, true);
					}
					
					if (!editorWasClosed){
						// use cancelled operation so we need to get out of here
						return false;
					}
					
					try {
						IFile file = editable.getLocalResource();
						file.delete(true, new NullProgressMonitor());												
					}
					catch (CoreException e){
					}
					
					// open new editor for correct replica
					editable = getEditableRemoteObject(remoteFile);
				}
				
				
				/* Commenting out - no longer needed with fix to bug #376535
				if (editable instanceof SystemEditableRemoteFile){
					SystemEditableRemoteFile edit = (SystemEditableRemoteFile)editable;
					IEditorDescriptor oldDescriptor = edit.getEditorDescriptor();
					IEditorDescriptor curDescriptor = null;
					IFile file = editable.getLocalResource();
					
					if (file == null || !file.exists()){
						curDescriptor = registry.getDefaultEditor(remoteFile.getName());						
					}
					if (curDescriptor == null){
						try {
							curDescriptor = IDE.getEditorDescriptor(file);
						} catch (PartInitException e) {
							curDescriptor = IDE.getDefaultEditor(file);
						}
					}
					if (oldDescriptor != curDescriptor){
						edit.setEditorDescriptor(curDescriptor);
					}								
				}
				*/
				
				try
				{
					boolean isOpen = editable.checkOpenInEditor() != ISystemEditableRemoteObject.NOT_OPEN;
					boolean isFileCached = isFileCached(editable, remoteFile);
					if (isFileCached)
					{
						if (!isOpen) {
							editable.setLocalResourceProperties();
							editable.addAsListener();
						}
						editable.openEditor();
					}
					else
					{
						DownloadAndOpenJob oJob = new DownloadAndOpenJob(editable, false);
						oJob.schedule();
					}

				}
				catch (Exception e)
				{
				}


			}
			else if (remoteFile.isDirectory())
			{
				return false;
			}

			//SystemDoubleClickEditAction editAction = new SystemDoubleClickEditAction(element);
			//editAction.run();
			return true;
		}
		else
		{ // if object is a folder, do not handle it
			return false;
		}
	}

	private boolean isFileCached(ISystemEditableRemoteObject editable, IRemoteFile remoteFile)
	{
		// DY:  check if the file exists and is read-only (because it was previously opened
		// in the system editor)
		IFile file = editable.getLocalResource();
		SystemIFileProperties properties = new SystemIFileProperties(file);
		try {
			// refresh workspace with just added resource
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			SystemBasePlugin.logError(e.getLocalizedMessage(), e);
		}
		boolean newFile = !file.exists();

		// detect whether there exists a temp copy already
		if (!newFile)
		{
			// we have a local copy of this file, so we need to compare timestamps
			
			// get stored modification stamp
			long storedModifiedStamp = properties.getRemoteFileTimeStamp();
			long oldRemoteModifiedStamp = remoteFile.getLastModified();

			// get updated remoteFile so we get the current remote timestamp
			IRemoteFileSubSystem subsystem = remoteFile.getParentRemoteFileSubSystem();
			if (!subsystem.isOffline()){ // only do this check when online..if offline we assume the temp file is okay			
				remoteFile.markStale(true);
				try
				{
					remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath(), new NullProgressMonitor());
				}
				catch (Exception e)
				{
				}
				
				// make sure that the editable is using an uptodate version
				((SystemEditableRemoteFile)editable).setRemoteFile(remoteFile);
			}

			// get the remote modified stamp
			long remoteModifiedStamp = remoteFile.getLastModified();

			// get dirty flag
			boolean dirty = properties.getDirty();

			boolean remoteNewer = (oldRemoteModifiedStamp != remoteModifiedStamp) || (storedModifiedStamp != remoteModifiedStamp);

			String remoteEncoding = remoteFile.getEncoding();
			String storedEncoding = properties.getEncoding();

			boolean encodingChanged = storedEncoding == null || !(remoteEncoding.equals(storedEncoding));

			boolean usedBinary = properties.getUsedBinaryTransfer();
			boolean isBinary = remoteFile.isBinary();

			boolean usedReadOnly = properties.getReadOnly();
			boolean isReadOnly = !remoteFile.canWrite();
						
			return (!dirty &&
					!remoteNewer &&
					usedBinary == isBinary &&
					usedReadOnly == isReadOnly && 
					!encodingChanged);
		}
		return false;
	}

	public boolean canEdit(Object element)
	{
		IRemoteFile remoteFile = (IRemoteFile) element;
		if (remoteFile.isFile())
		{
	    	boolean offline = remoteFile.getParentRemoteFileSubSystem().isOffline();
			return remoteFile.canRead() && !offline;
		}
		return false;
	}

	public ISystemEditableRemoteObject getEditableRemoteObject(Object element)
	{
		RemoteFile remoteFile = (RemoteFile) element;
		if (remoteFile.isFile())
		{
			String absolutePath = remoteFile.getAbsolutePath();
			IFile localProjectFile = null;
			if (remoteFile.getHost().getSystemType().isLocal())
			{
				localProjectFile = getProjectFileForLocation(absolutePath);
			}
			try
			{
				IFile file = null;
				if (localProjectFile == null) {
					file = getCachedCopy(remoteFile); // Note that this is a case-sensitive check
				}
				else {
					file = localProjectFile;
				}
				if (file != null)
				{
					SystemIFileProperties properties = new SystemIFileProperties(file);
					
					Object obj = properties.getRemoteFileObject();
					if (obj != null && obj instanceof SystemEditableRemoteFile)
					{
						SystemEditableRemoteFile rmtObj = (SystemEditableRemoteFile) obj;					
						return rmtObj; // return regardless of whehter it's open - open handling is taken care of after
					}
				}
				return new SystemEditableRemoteFile(remoteFile);
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	public IFile getCachedCopy(IRemoteFile remoteFile) throws SystemMessageException
	{
		if (SystemRemoteEditManager.getInstance().doesRemoteEditProjectExist())
		{
			IResource replica = UniversalFileTransferUtility.getTempFileFor(remoteFile);
			if (replica != null && replica.exists())
			{					
				return (IFile)replica;
			}
		}
		return null;
	}

	/**
	 * Return a filter string that corresponds to this object.
	 * @param object the object to obtain a filter string for
	 * @return the corresponding filter string if applicable
	 */
	public String getFilterStringFor(Object object)
	{
		if (object instanceof IRemoteFile)
		{
			IRemoteFile remoteFile = (IRemoteFile) object;
			if (remoteFile.isDirectory())
			{
				return remoteFile.getAbsolutePath() + remoteFile.getSeparator() + "*"; //$NON-NLS-1$
			}
			else
			{
				return remoteFile.getAbsolutePath();
			}
		}
		return null;
	}

	/**
	 * From <samp>IActionFilter</samp>, it exposes properties for decorator and popupMenus extension points.
	 * <p>
	 * <ol>
	 * <li>name="name". The given value must match the name exactly or if ends with an asterisk the beginning must match.
	 * (case sensitiveness depends on the subsystem)</li>
	 * <li>name="absolutePath". The given value must match the absolute path exactly or if ends with an asterisk the beginning must match.
	 * (case sensitiveness depends on the subsystem)</li>
	 * <li>name="extension". The given value must match the extension exactly or if ends with an asterisk the beginning must match.
	 * (case sensitiveness depends on the subsystem)</li>
	 * <li>name="isRoot". If the given value is <code>true</code>, then returns <code>true</code> if the target is a root file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not root.</li>
	 * <li>name="isFile". If the given value is <code>true</code>, then returns <code>true</code> if the target is a file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not a file.</li>
	 * <li>name="isDirectory". If the given value is <code>true</code>, then returns <code>true</code> if the target is a directory.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not a directory.</li>
	 * <li>name="isHidden". If the given value is <code>true</code>, then returns <code>true</code> if the target is a hidden file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not a hidden file.</li>
	 * <li>name="canRead". If the given value is <code>true</code>, then returns <code>true</code> if the target is readable.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not readable.</li>
	 * <li>name="canWrite". If the given value is <code>true</code>, then returns <code>true</code> if the target is writable.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not writable.</li>
	 * <li>name="isBinary". If the given value is <code>true</code>, then returns <code>true</code> if the target is a binary file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not a binary file.</li>
	 * <li>name="isText". If the given value is <code>true</code>, then returns <code>true</code> if the target is a text file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not a text file.</li>
	 * <li>name="isArchive". If the given value is <code>true</code>, then returns <code>true</code> if the target is an archive file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not an archive file.</li>
	 * <li>name="isVirtual". If the given value is <code>true</code>, then returns <code>true</code> if the target is a virtual file.
	 * If the given value is <code>false</code>, then returns <code>true</code> if the target is not a virtual file.</li>
	 * </ol>
	 * <p>
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean testAttribute(Object target, String name, String value) {

		if (target instanceof IRemoteFile)
		{
			IRemoteFile tgt = (IRemoteFile) target;

			String inName = name.toLowerCase();


			if (inName.equals("classification"))  //$NON-NLS-1$
			{
				String classification = tgt.getClassification();

				if (classification != null)
				{
					return StringCompare.compare(value, classification, true);
				}
			}
			else if (inName.equals("name")) //$NON-NLS-1$
			{
				boolean caseSensitive = tgt.getParentRemoteFileSubSystem().isCaseSensitive();

				String tgtName = getName(target);
				String val = value;

				// if case does not matter, then lower case the compares
				if (!caseSensitive) {
					tgtName = tgtName.toLowerCase();
					val = val.toLowerCase();
				}

				// we have a wild card test, and * is the last character in the value
				if (val.endsWith("*"))  { //$NON-NLS-1$
					return tgtName.startsWith(val.substring(0, val.length()-1));
				}
				else {
					return val.equals(tgtName);
				}
			}
			else if (inName.equals("absolutePath".toLowerCase())) { //$NON-NLS-1$

				boolean caseSensitive = tgt.getParentRemoteFileSubSystem().isCaseSensitive();

				String tgtPath = getAbsoluteName(target);
				String val = value;

				// if case does not matter, then lower case the compares
				if (!caseSensitive) {
					tgtPath = tgtPath.toLowerCase();
					val = val.toLowerCase();
				}

				// we have a wild card test, and * is the last character in the value
				if (val.endsWith("*"))  { //$NON-NLS-1$
					return tgtPath.startsWith(val.substring(0, val.length()-1));
				}
				else {
					return val.equals(tgtPath);
				}
			}
			else if (inName.equals("extension")) { //$NON-NLS-1$

				boolean caseSensitive = tgt.getParentRemoteFileSubSystem().isCaseSensitive();

				String tgtExtension = tgt.getExtension();

				if (tgtExtension == null) {
					return false;
				}

				StringTokenizer st = new StringTokenizer(value, " \t\n\r\f,"); //$NON-NLS-1$

				String val = null;

				while (st.hasMoreTokens()) {

				    val = st.nextToken();

                    // if case does not matter, then lower case the compares
                    if (!caseSensitive) {
                        tgtExtension = tgtExtension.toLowerCase();
                        val = val.toLowerCase();
                    }

                    boolean match = false;

                    // we have a wild card test, and * is the last character in
                    // the value
                    if (val.endsWith("*")) { //$NON-NLS-1$
                        match = tgtExtension.startsWith(val.substring(0, val.length() - 1));
                    }
                    else {
                        match = val.equals(tgtExtension);
                    }

                    // if there is a match, return true, otherwise check against next extension
                    if (match) {
                        return true;
                    }
				}

				// return false if no match
				return false;
			}
			else if (inName.equals("isroot")) //$NON-NLS-1$
            {
				return tgt.isRoot() && value.equals("true") || //$NON-NLS-1$
					!tgt.isRoot() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("isfile")) //$NON-NLS-1$
			{
				return tgt.isFile() && value.equals("true") || //$NON-NLS-1$
					!tgt.isFile() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("isdirectory")) //$NON-NLS-1$
			{
				return tgt.isDirectory() && value.equals("true") || //$NON-NLS-1$
					!tgt.isDirectory() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("ishidden"))  //$NON-NLS-1$
			{
				return tgt.isHidden() && value.equals("true") || //$NON-NLS-1$
					!tgt.isHidden() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("canread"))  //$NON-NLS-1$
			{
				return tgt.canRead() && value.equals("true") || //$NON-NLS-1$
					!tgt.canRead() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("canwrite"))  //$NON-NLS-1$
			{
				return tgt.canWrite() && value.equals("true") || //$NON-NLS-1$
					!tgt.canWrite() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("isbinary"))  //$NON-NLS-1$
			{
				return tgt.isBinary() && value.equals("true") || //$NON-NLS-1$
					!tgt.isBinary() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("istext"))  //$NON-NLS-1$
			{
				return tgt.isText() && value.equals("true") || //$NON-NLS-1$
					!tgt.isText() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("isarchive")) { //$NON-NLS-1$
				return tgt.isArchive() && value.equals("true") || //$NON-NLS-1$
					!tgt.isArchive() && value.equals("false");				 //$NON-NLS-1$
			}
			else if (inName.equals("isvirtual"))  //$NON-NLS-1$
			{
				return tgt instanceof IVirtualRemoteFile && value.equals("true") || //$NON-NLS-1$
					!(tgt instanceof IVirtualRemoteFile) && value.equals("false"); //$NON-NLS-1$
			}
			else if (inName.equals("isexecutable"))  //$NON-NLS-1$
			{
				return tgt.isExecutable() && value.equals("true") || //$NON-NLS-1$
				 !tgt.isExecutable() && value.equals("false"); //$NON-NLS-1$
			}
			else if (inName.equals("islink"))  //$NON-NLS-1$
			{
				return tgt.isLink() && value.equals("true") || //$NON-NLS-1$
				 !tgt.isLink() && value.equals("false"); //$NON-NLS-1$
			}
			else if (inName.equals("supportspermissions")) //$NON-NLS-1$
			{
				if (value.equals("true")){ //$NON-NLS-1$
					// check service
					if (tgt instanceof IAdaptable){
						IFilePermissionsService service = (IFilePermissionsService)((IAdaptable)tgt).getAdapter(IFilePermissionsService.class);
						if (service != null){

							return (service.getCapabilities(tgt.getHostFile()) & IFilePermissionsService.FS_CAN_GET_PERMISSIONS) != 0;
						}
					}
				}
				return false;
			}
		}

		return super.testAttribute(target, name, value);
	}

	/*
	 * Return whether deferred queries are supported.
	 */
	public boolean supportsDeferredQueries(ISubSystem subSys)
	{
		if (subSys instanceof IRemoteFileSubSystem){
			return ((IRemoteFileSubSystem)subSys).getParentRemoteFileSubSystemConfiguration().supportsDeferredQueries();
		}
		return !subSys.getHost().getSystemType().isLocal();
	}


	protected SystemFetchOperation getSystemFetchOperation(Object o, IElementCollector collector)
	{
	    return new SystemFetchOperation(null, o, this, collector, true);
	}
	
	private static IFile getProjectFileForLocation(String absolutePath)
	{
		IPath workspacePath = new Path(absolutePath);
		IFile file = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(workspacePath);
		return file;
	}
	
	private static IContainer getProjectFolderForLocation(String absolutePath)
	{
		IPath workspacePath = new Path(absolutePath);
		IContainer container = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(workspacePath);
		return container;
	}
	
	private static void openEditor(IFile localFile, boolean readOnly) throws PartInitException {
		IEditorDescriptor editorDescriptor = null;
		
		try {
			editorDescriptor = IDE.getEditorDescriptor(localFile);
		} catch (PartInitException e) {
			SystemBasePlugin.logError(e.getLocalizedMessage(), e);
		}
		
		if (editorDescriptor == null) {
			if (PlatformUI.isWorkbenchRunning())
			{
				IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
				editorDescriptor = registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			}
		}
		
		//This file is from local connection, and it is inside a project in the work
		IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();
		
		ResourceAttributes attr = localFile.getResourceAttributes();
		if (attr!=null) {
			attr.setReadOnly(readOnly);
			try
			{
				localFile.setResourceAttributes(attr);
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError(e.getLocalizedMessage(), e);
			}
		}

		// set editor as preferred editor for this file
		String editorId = null;
		if (editorDescriptor != null) {
				editorId = editorDescriptor.getId();
		}

		IDE.setDefaultEditor(localFile, editorId);
		if (editorDescriptor.isOpenExternal()){
			openSystemEditor(localFile); // opening regular way doesn't work anymore
		}
		else {
			FileEditorInput finput = new FileEditorInput(localFile);
			// check for files already open
			if (editorDescriptor != null && editorDescriptor.isOpenExternal()){
				((WorkbenchPage)activePage).openEditorFromDescriptor(new FileEditorInput(localFile), editorDescriptor, true, null);
			}
			else {
				activePage.openEditor(finput, editorDescriptor.getId());
			}
	
			return;
		}
	}
	
	private static void openSystemEditor(IFile localFile) throws PartInitException {
		IEditorDescriptor editorDescriptor = null;
		
		try {
			editorDescriptor = IDE.getEditorDescriptor(localFile);
		} catch (PartInitException e) {
			SystemBasePlugin.logError(e.getLocalizedMessage(), e);
		}
		
		if (editorDescriptor == null) {
			if (PlatformUI.isWorkbenchRunning())
			{
				IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
				editorDescriptor = registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			}
		}
		
		//This file is from local connection, and it is inside a project in the work
		IWorkbenchPage activePage = SystemBasePlugin.getActiveWorkbenchWindow().getActivePage();

		// set editor as preferred editor for this file
		String editorId = null;
		if (editorDescriptor != null) {
				editorId = editorDescriptor.getId();
		}

		IDE.setDefaultEditor(localFile, editorId);
		
		FileEditorInput fileInput = new FileEditorInput(localFile);
		activePage.openEditor(fileInput, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
	
		return;
		
	}
	
}
