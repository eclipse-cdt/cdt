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

package org.eclipse.rse.files.ui.view;
import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.actions.SystemAddToArchiveAction;
import org.eclipse.rse.files.ui.actions.SystemCompareFilesAction;
import org.eclipse.rse.files.ui.actions.SystemCompareWithEditionAction;
import org.eclipse.rse.files.ui.actions.SystemConvertAction;
import org.eclipse.rse.files.ui.actions.SystemEditFilesAction;
import org.eclipse.rse.files.ui.actions.SystemExtractAction;
import org.eclipse.rse.files.ui.actions.SystemExtractToAction;
import org.eclipse.rse.files.ui.actions.SystemMoveRemoteFileAction;
import org.eclipse.rse.files.ui.actions.SystemNewFileAction;
import org.eclipse.rse.files.ui.actions.SystemNewFileFilterFromFolderAction;
import org.eclipse.rse.files.ui.actions.SystemNewFolderAction;
import org.eclipse.rse.files.ui.actions.SystemRemoteFileOpenWithMenu;
import org.eclipse.rse.files.ui.actions.SystemReplaceWithEditionAction;
import org.eclipse.rse.files.ui.actions.SystemSearchAction;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.SystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceSet;
import org.eclipse.rse.model.SystemMessageObject;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.services.clientserver.StringCompare;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.search.HostSearchResultSet;
import org.eclipse.rse.services.search.IHostSearchConstants;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultConfigurationFactory;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileRemoteTypes;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileEmpty;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileRoot;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteSearchResultsContentsType;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileUniqueName;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemShowInMonitorAction;
import org.eclipse.rse.ui.actions.SystemShowInTableAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.operations.SystemFetchOperation;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemDragDropAdapter;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemMementoConstants;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewDropDestination;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemDNDTransferRunnable;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.rse.ui.view.search.SystemSearchTableView;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * Adapter for displaying remote file system objects in tree views.
 * These are children of RemoteFileSubSystem filter strings
 */
public class SystemViewRemoteFileAdapter
	extends AbstractSystemViewAdapter
	implements ISystemViewElementAdapter, ISystemRemoteElementAdapter, 
	ISystemMessages, ISystemPropertyConstants
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
	private SystemShowInTableAction showInTableAction;
	private SystemShowInMonitorAction showInMonitorAction;
	private SystemExtractAction extractAction;
	private SystemExtractToAction extractToAction;
	private SystemConvertAction convertAction;
	private SystemAddToArchiveAction addToArchiveAction;
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

	static final SystemMessage _uploadMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_UPLOADING_PROGRESS);
	static final SystemMessage _downloadMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DOWNLOADING_PROGRESS);
	
	/**
	 * Constructor
	 */
	public SystemViewRemoteFileAdapter()
	{
		super();
		xlatedSize = SystemViewResources.RESID_PROPERTY_FILE_SIZE_VALUE;
		xlatedCompressedSize = SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_VALUE;
		xlatedExpandedSize = SystemViewResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_VALUE;
		
		IWorkbench workbench = RSEUIPlugin.getDefault().getWorkbench();
		if (workbench != null)
			registry = workbench.getEditorRegistry();
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
		if (filesOnly && (filterString != null) && (filterString.indexOf("/ns") == -1))
			filterString = filterString + " /ns";
		else if (foldersOnly && (filterString != null) && (filterString.indexOf("/nf") == -1))
			filterString = filterString + " /nf";
		this.filterString = filterString;
	}
	
	/**
	 * Get the current filter string being used to subset the list by.
	 * Will be null unless setFilterString has previously been called.
	 */
	public String getFilterString()
	{
		return filterString;
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
		boolean allHaveContents = false;

		boolean canEdit = true;
		boolean supportsSearch = true;
		boolean supportsArchiveManagement = false;

		boolean isVirtual = false;

		// perf improvement... phil	
		Object firstSelection = selection.getFirstElement();
		IRemoteFile firstFile = null;
		if ((firstSelection != null) && (firstSelection instanceof IRemoteFile))
		{
			firstFile = (IRemoteFile) firstSelection;
			elementType = firstFile.isDirectory() || firstFile.isRoot() ? 1 : 0;
			isArchive = firstFile.isArchive();
			isVirtual = firstFile instanceof IVirtualRemoteFile;
			canEdit = firstFile.canRead();

			supportsSearch = firstFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsSearch();
			supportsArchiveManagement = firstFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();
		}
		else
			return;

		allHaveContents = isArchive;

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
				    allHaveContents = false;
					break;
				}
			}
		}

		if ((elementType == 1 || (isArchive && supportsArchiveManagement)))
		{
			if (!foldersOnly && canEdit)
			{
				if (addNewFile == null)
				{
					addNewFile = new SystemNewFileAction(shell);
				}
				menu.add(ISystemContextMenuConstants.GROUP_NEW, addNewFile);
			}
			if (!filesOnly)
			{
			    if (canEdit)
			    {
			        if (addNewFolder == null)
			        {
			            addNewFolder = new SystemNewFolderAction(shell);
			        }
			        menu.add(ISystemContextMenuConstants.GROUP_NEW, addNewFolder);
			    }

				if (addNewFilter == null)
				{
					addNewFilter = new SystemNewFileFilterFromFolderAction(shell);
				}

				menu.appendToGroup(ISystemContextMenuConstants.GROUP_NEW, new Separator());
				menu.add(ISystemContextMenuConstants.GROUP_NEW, addNewFilter);
			}

		}
		else
		{
		    if (canEdit)
		    {		    	
		    	// open 
		    	String label = SystemResources.ACTION_CASCADING_OPEN_LABEL;
		   	    String tooltip = SystemResources.ACTION_CASCADING_OPEN_TOOLTIP;
		   		SystemEditFilesAction action = new SystemEditFilesAction(label, tooltip, shell);
		   		menu.add(ISystemContextMenuConstants.GROUP_OPEN, action);
		    	
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
		}

		if (extractAction == null)
		{
			extractAction = new SystemExtractAction(shell);
		}
		if (extractToAction == null)
		{
			extractToAction = new SystemExtractToAction(shell);
		}
		if (convertAction == null)
		{
			convertAction = new SystemConvertAction(shell);
		}
		if (addToArchiveAction == null)
		{
			addToArchiveAction = new SystemAddToArchiveAction(shell);
		}
	
		if (allHaveContents && canEdit && !isVirtual)
		{
		    /*
			menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, extractAction);
			menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, extractToAction);
			menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, convertAction);
			*/
		}
		
		// add import and export actions for single selection of folder
/*		if ((elementType == 1 && selection.size() == 1) && canEdit)
		{
		    
		    if (importAction == null) {
		        importAction = new SystemImportToProjectAction(shell);
		    }
		    
		    if (exportAction == null) {
		        exportAction = new SystemExportFromProjectAction(shell);
		    }
		    
			menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, importAction);
			menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, exportAction);
		}*/

		if (moveAction == null)
			moveAction = new SystemMoveRemoteFileAction(shell);

		ISubSystem subsys = firstFile.getParentRemoteFileSubSystem();

		// DKM - clipboard based copy actions
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		Clipboard clipboard = registry.getSystemClipboard();

		if (pasteClipboardAction == null)
		{
			pasteClipboardAction = new SystemPasteFromClipboardAction(shell, clipboard);
		}
		if (copyClipboardAction == null)
		{
			copyClipboardAction = new SystemCopyToClipboardAction(shell, clipboard);
		}
		/** FIXME - these show now be contributed via plugin.xml from the shells plugin
		if (commandAction == null)
		{
			commandAction = new SystemCommandAction(shell, false, null);
		}
		if (shellAction == null)
		{
			shellAction = new SystemCommandAction(shell, true, null);
		}
		**/
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
		if (showInTableAction == null)
		{
			showInTableAction = new SystemShowInTableAction(shell);
		}
		if (showInMonitorAction == null)
		{
			showInMonitorAction = new SystemShowInMonitorAction(shell);
		}

		if (canEdit && supportsSearch)
		{
		    //menu.add(ISystemContextMenuConstants.GROUP_IMPORTEXPORT, addToArchiveAction);

			// add search action
			menu.add(ISystemContextMenuConstants.GROUP_SEARCH, searchAction);
		}

		if (!firstFile.isRoot() && canEdit)
		{
			menu.add(menuGroup, copyClipboardAction);
			if (elementType == 0)
			{
				menu.add(ISystemContextMenuConstants.GROUP_COMPAREWITH, compareFilesAction);
				menu.add(ISystemContextMenuConstants.GROUP_COMPAREWITH, compareEditionAction);
				menu.add(ISystemContextMenuConstants.GROUP_REPLACEWITH, replaceEditionAction);
			}
		}

		if (elementType == 1 || (isArchive && supportsArchiveManagement))
		{
		    if (canEdit)
		    {
		        menu.add(menuGroup, pasteClipboardAction);
		    }
			menu.add(ISystemContextMenuConstants.GROUP_OPEN, showInTableAction);
			

			menu.add(ISystemContextMenuConstants.GROUP_OPEN, showInMonitorAction);
			
			/** FIXME - shells now separate plugin
			if (elementType == 1)
			{
				if (!isVirtual)
				{
					menu.add(menuGroup, shellAction);
				}
			}
			*/
		}
		if (!firstFile.isRoot() && canEdit)
		{
			menu.add(menuGroup, moveAction);
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
			return registry.getImageDescriptor(file.getName());
		}
		else
		{
			boolean isOpen = false;
			if (getViewer() instanceof AbstractTreeViewer)
			{
				AbstractTreeViewer atv = (AbstractTreeViewer) getViewer();
				isOpen = atv.getExpandedState(element);
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
		return getType(element) + ": " + getAbsoluteName(element);
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
	public Object[] getChildren(IProgressMonitor monitor, Object element)
	{
		return internalGetChildren(monitor, element);
	}
	
	/**
	 * Return the children of this object.
	 * If this is a folder or root, we list all child folders and files.
	 */
	public Object[] getChildren(Object element)
	{
		_lastResults = internalGetChildren(null, element);
		return _lastResults;
		//		}
	}

	private synchronized Object[] internalGetChildren(IProgressMonitor monitor, Object element)
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
		RemoteFileFilterString orgRffs = file.getFilterString();
	
		String filter = null;
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
			else
			{
				filter = null; // this is our clue to RemoteFileSubSystemImpl to use all filter strings associated with this folder
			}
		}
		else if (foldersOnly)
		{
			if (filterString == null)
				filter = "* /nf";
			else
				filter = filterString;
		}
		else if (filesOnly)
		{
			if (filterString == null)
				filter = "* /ns";
			else
				filter = filterString;
		}
		else
		{
			if (filterString == null)
				filter = "*";
			else
				filter = filterString;	
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
	
		boolean hasChildren = file.hasContents(RemoteChildrenContentsType.getInstance(), filter);
		    
		if (hasChildren && !file.isStale())
		{
			children = file.getContents(RemoteChildrenContentsType.getInstance(), filter);
		}
		else
		{
			try
			{
			    if (monitor != null)
			    {
			        
			        children = ss.resolveFilterString(monitor, file, filter);
			    }
			    else
			    {
			        children = ss.resolveFilterString(file, filter, getShell());
			    }
				if ((children == null) || (children.length == 0))
				{
					//children = new SystemMessageObject[1];
					//children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_EMPTY),
					//                                      ISystemMessageObject.MSGTYPE_EMPTY, element);
					children = EMPTY_LIST;
				}

			}
			catch (InterruptedException exc)
			{
				children = new SystemMessageObject[1];
				children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_CANCELLED), ISystemMessageObject.MSGTYPE_CANCEL, element);
				//System.out.println("Canceled.");
			}
			catch (Exception exc)
			{
				children = new SystemMessageObject[1];
				children[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, element);
				SystemBasePlugin.logError("Exception resolving file filter strings", exc);
			} // message already issued        
		}
		return children;
	}
	/**
	 * Return true if this object has children.
	 * Since we can't predict the outcome of resolving the filter string, we return true.
	 */
	public boolean hasChildren(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		boolean supportsArchiveManagement = file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();
		boolean hasChildren = false;
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
		        hasChildren = file.hasContents(RemoteChildrenContentsType.getInstance());
		    }
		}

		return hasChildren;
	}

	

	public IPropertyDescriptor[] getUniquePropertyDescriptors()
	{
		
		IRemoteFile file = null;
		if (propertySourceInput instanceof IRemoteFile) 
		{
			file = (IRemoteFile) propertySourceInput;
		
			boolean supportsArchiveManagement = file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();
		
			boolean isArchive = file != null && file.isArchive() && supportsArchiveManagement;
			boolean isVirtual = file != null && file instanceof IVirtualRemoteFile && supportsArchiveManagement;
			boolean isRegular = !isArchive && !isVirtual;
			
			if (isRegular && uniquePropertyDescriptorArray == null ||
			    isArchive && uniqueArchiveDescriptorArray == null ||
			    isVirtual && uniqueVirtualDescriptorArray == null)
			{
				
				int nbrOfArchiveProperties = 2;
				int nbrOfVirtualProperties = 4;
				int nbrOfProperties = 4;
				if (isVirtual) nbrOfProperties += nbrOfVirtualProperties;
				else if (isArchive) nbrOfProperties += nbrOfArchiveProperties; 
	
				if (isRegular) uniquePropertyDescriptorArray = new PropertyDescriptor[nbrOfProperties];
				else if (isVirtual) uniqueVirtualDescriptorArray = new PropertyDescriptor[nbrOfProperties];
				else if (isArchive) uniqueArchiveDescriptorArray = new PropertyDescriptor[nbrOfProperties];
				//PropertyDescriptor[] defaultProperties = (PropertyDescriptor[]) getDefaultDescriptors();
	
				int i = -1;
	
				// add our unique property descriptors...
				RSEUIPlugin plugin = RSEUIPlugin.getDefault();
	
				// classification
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_CLASSIFICATION, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_CLASSIFICATION, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_CLASSIFICATION, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
				
				// last modified
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_LASTMODIFIED, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_LASTMODIFIED, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_LASTMODIFIED, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
	
				// size
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_SIZE, SystemViewResources.RESID_PROPERTY_FILE_SIZE_LABEL, SystemViewResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_SIZE, SystemViewResources.RESID_PROPERTY_FILE_SIZE_LABEL, SystemViewResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_SIZE, SystemViewResources.RESID_PROPERTY_FILE_SIZE_LABEL, SystemViewResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
						
				// canonical path
				if (isRegular) uniquePropertyDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_CANONICAL_PATH, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
				else if (isVirtual) uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_CANONICAL_PATH, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
				else if (isArchive) uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(P_FILE_CANONICAL_PATH, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
	
	
				if (isVirtual)
				{
					// add virtual property descriptors...
					
					// compressed size
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_VIRTUAL_COMPRESSEDSIZE, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_LABEL, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_DESCRIPTION);
				
					// compression ratio
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_VIRTUAL_COMPRESSIONRATIO, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_LABEL, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_DESCRIPTION);
				
					// compression method
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_VIRTUAL_COMPRESSIONMETHOD, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_LABEL,  SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_DESCRIPTION);
	
					// comment
					uniqueVirtualDescriptorArray[++i] = createSimplePropertyDescriptor(P_VIRTUAL_COMMENT, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_LABEL,  SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_DESCRIPTION);
				}
				else if (isArchive)
				{
					// add archive property descriptors...
					
					// expanded size
					uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(P_ARCHIVE_EXPANDEDSIZE, SystemViewResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_LABEL, SystemViewResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_DESCRIPTION);
				
					// comment
					uniqueArchiveDescriptorArray[++i] = createSimplePropertyDescriptor(P_ARCHIVE_COMMENT, SystemViewResources.RESID_PROPERTY_ARCHIVE_COMMENT_LABEL, SystemViewResources.RESID_PROPERTY_ARCHIVE_COMMENT_DESCRIPTION);
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
			int nbrOfProperties = 8;
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
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);
			briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor(P_FILE_PATH, SystemViewResources.RESID_PROPERTY_FILE_PATH_LABEL, SystemViewResources.RESID_PROPERTY_FILE_PATH_TOOLTIP);

			// filter string
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);
			briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor(P_FILTERSTRING, SystemViewResources.RESID_PROPERTY_FILTERSTRING_LABEL,SystemViewResources.RESID_PROPERTY_FILTERSTRING_TOOLTIP);

			// canonical path
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_CANONICAL_PATH, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL,SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_CANONICAL_PATH, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL,SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_CANONICAL_PATH, SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_LABEL,SystemViewResources.RESID_PROPERTY_FILE_CANONICAL_PATH_TOOLTIP);
			
			// last modified
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_LASTMODIFIED, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_LASTMODIFIED, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL,SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_LASTMODIFIED, SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_LABEL,SystemViewResources.RESID_PROPERTY_FILE_LASTMODIFIED_TOOLTIP);

			// size
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_SIZE, SystemViewResources.RESID_PROPERTY_FILE_SIZE_LABEL, SystemViewResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_SIZE, SystemViewResources.RESID_PROPERTY_FILE_SIZE_LABEL, SystemViewResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_SIZE, SystemViewResources.RESID_PROPERTY_FILE_SIZE_LABEL, SystemViewResources.RESID_PROPERTY_FILE_SIZE_TOOLTIP);

			// classification
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_CLASSIFICATION, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_CLASSIFICATION, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_CLASSIFICATION, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_LABEL, SystemViewResources.RESID_PROPERTY_FILE_CLASSIFICATION_TOOLTIP);
			
			// readonly
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_READONLY, SystemViewResources.RESID_PROPERTY_FILE_READONLY_LABEL, SystemViewResources.RESID_PROPERTY_FILE_READONLY_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_READONLY, SystemViewResources.RESID_PROPERTY_FILE_READONLY_LABEL, SystemViewResources.RESID_PROPERTY_FILE_READONLY_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_READONLY, SystemViewResources.RESID_PROPERTY_FILE_READONLY_LABEL, SystemViewResources.RESID_PROPERTY_FILE_READONLY_TOOLTIP);

			// hidden
			if (isRegular) propertyDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_HIDDEN, SystemViewResources.RESID_PROPERTY_FILE_HIDDEN_LABEL, SystemViewResources.RESID_PROPERTY_FILE_HIDDEN_TOOLTIP);
			else if (isVirtual) virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_HIDDEN, SystemViewResources.RESID_PROPERTY_FILE_HIDDEN_LABEL, SystemViewResources.RESID_PROPERTY_FILE_HIDDEN_TOOLTIP);
			else if (isArchive) archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_FILE_HIDDEN, SystemViewResources.RESID_PROPERTY_FILE_HIDDEN_LABEL, SystemViewResources.RESID_PROPERTY_FILE_HIDDEN_TOOLTIP);

			if (debug)
			{
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENTREMOTEFILE");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENTREMOTEFILE");
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENT");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENT");
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENTNOROOT");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENTNOROOT");
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_PARENTNAME");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_PARENTNAME");
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_ROOT");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_ROOT");
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_ISROOT");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_ISROOT");
				propertyDescriptorArray[++idx] = createSimplePropertyDescriptor("DEBUG_EXISTS");
				briefPropertyDescriptorArray[++briefIdx] = createSimplePropertyDescriptor("DEBUG_EXISTS");
			}
			
			if (isVirtual)
			{
				// add virtual property descriptors...
				
				// compressed size
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_VIRTUAL_COMPRESSEDSIZE, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_LABEL, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSEDSIZE_DESCRIPTION);
			
				// compression ratio
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_VIRTUAL_COMPRESSIONRATIO, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_LABEL, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONRATIO_DESCRIPTION);
			
				// compression method
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_VIRTUAL_COMPRESSIONMETHOD, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_LABEL, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMPRESSIONMETHOD_DESCRIPTION);

				// comment
				virtualDescriptorArray[++idx] = createSimplePropertyDescriptor(P_VIRTUAL_COMMENT, SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_LABEL,  SystemViewResources.RESID_PROPERTY_VIRTUALFILE_COMMENT_DESCRIPTION);
			}

			else if (isArchive)
			{
				// add archive property descriptors...
				
				// expanded size
				archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_ARCHIVE_EXPANDEDSIZE, SystemViewResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_LABEL,  SystemViewResources.RESID_PROPERTY_ARCHIVE_EXPANDEDSIZE_DESCRIPTION);
			
				// comment
				archiveDescriptorArray[++idx] = createSimplePropertyDescriptor(P_ARCHIVE_COMMENT, SystemViewResources.RESID_PROPERTY_ARCHIVE_COMMENT_LABEL, SystemViewResources.RESID_PROPERTY_ARCHIVE_COMMENT_DESCRIPTION);
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
			if (name.equals("DEBUG_PARENTREMOTEFILE"))
			{
				IRemoteFile parent = file.getParentRemoteFile();
				if (parent == null)
					return "null";
				else
					return "absPath='" + parent.getAbsolutePath() + "'";
			}
			else if (name.equals("DEBUG_PARENT"))
				return file.getParentPath();
			else if (name.equals("DEBUG_PARENTNOROOT"))
				return file.getParentNoRoot();
			else if (name.equals("DEBUG_PARENTNAME"))
				return file.getParentName();
			else if (name.equals("DEBUG_ROOT"))
				return file.getRoot();
			else if (name.equals("DEBUG_ISROOT"))
				return file.isRoot() ? "true" : "false";
			else if (name.equals("DEBUG_EXISTS"))
				return file.exists() ? "true" : "false";

		}
		// 
		if (name.equals(ISystemPropertyConstants.P_FILE_LASTMODIFIED))
		{
			Date date = file.getLastModifiedDate();
			if (date != null)
			{
				if (formatted)
				{
					SimpleDateFormat datefmt = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
					return datefmt.format(date);
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
				return sub(xlatedSize, MSG_SUB1, Long.toString(file.getLength()));
			}
			else
			{
				return new Long(file.getLength());
			}
		}
		else if (name.equals(ISystemPropertyConstants.P_ARCHIVE_EXPANDEDSIZE))
		{
			if (!isVirtual) return new Long(0);
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
			if (!isVirtual) return new Long(0);
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
			if (!isVirtual) return "";
			return virtualFile.getCompressionMethod();
		}
		else if (name.equals(ISystemPropertyConstants.P_VIRTUAL_COMPRESSIONRATIO))
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
			}
		}
		else
			return null; //super.getPropertyValue(name);
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
	        return ((IRemoteFile)element).canRead();
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
			boolean supportsArchiveManagement = file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();
			return file.canRead() && file.canWrite() && (file.isDirectory() || file.isRoot() || (file.isArchive() && supportsArchiveManagement));
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
			boolean supportsArchiveManagement = targetFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();
			if (!targetFile.isFile() || (targetFile.isArchive() && supportsArchiveManagement))
			{
				targetFile.canWrite();
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
			boolean supportsArchiveManagement = targetFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();
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
			return UniversalFileTransferUtility.copyRemoteResourceToWorkspace(srcFileOrFolder, monitor);
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
	 * @param sameSystemType indication of whether the source and target reside on the same type of system
	 * @param monitor the progress monitor
	 * @return a temporary workspace copies of the object that was copied
	 * 
	 */
	public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor)
	{
	
		boolean supportsSearch = ((IRemoteFileSubSystemConfiguration)set.getSubSystem().getSubSystemConfiguration()).supportsSearch();
		boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER);
		if (!doSuperTransferProperty && supportsSearch)
		{
			SystemRemoteResourceSet flatSet = new SystemRemoteResourceSet(set.getSubSystem(), set.getAdapter());
			long totalByteSize = getFlatRemoteResourceSet(set.getResourceSet(), flatSet, monitor);
			flatSet.setByteSize(totalByteSize);
			
			if (monitor != null)
			{	
			    monitor.beginTask(_downloadMessage.getLevelOneText(),  (int)totalByteSize);
			    //monitor.done();
			}
			
			try
			{
				SystemWorkspaceResourceSet flatResult = UniversalFileTransferUtility.copyRemoteResourcesToWorkspace(flatSet, monitor);		
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
						hierarchicalResult.addResource(UniversalFileTransferUtility.getTempFileFor(remoteFile));
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
			return UniversalFileTransferUtility.copyRemoteResourcesToWorkspace(set, monitor);
		}
	}
	
	/**
	 * Helper method to get the local file subsystem.
	 * @return the local file subsystem
	 */
	private IRemoteFileSubSystem getLocalFileSubSystem()
	{
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		IHost[] connections = registry.getHosts();
		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			IRemoteFileSubSystem anFS = RemoteFileUtility.getFileSubSystem(connection);
			if (anFS.getHost().getSystemType().equals("Local"))
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
			
			SystemSearchString searchString = new SystemSearchString("*", false, false, "*", false, false, true);
			
			for (int i = 0; i < initialResources.size(); i++)
			{
				IRemoteFile remoteFile = (IRemoteFile)initialResources.get(i);
				
				// get all files within directory
				if (remoteFile.isDirectory())
				{							
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
					while (display.readAndDispatch());
					try
					{
						Thread.sleep(100);
					}
					catch (Exception e)
					{						
					}
				}
				else
				{
					try
					{
						Thread.sleep(100);
					}
					catch (Exception e)
					{						
					}
				}
			}
			
			if (config.getStatus() == IHostSearchConstants.FINISHED)
			{			
				submonitor.worked(1);
				Object[] results = config.getResults();
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
	
	
	
	
	/**
	 *  Perform drop from the "fromSet" of objects to the "to" object
	 * @param from the source objects for the drop
	 * @param to the target object for the drop
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

			// make sure properties are uptodate
			try
			{
				//targetFolder.markStale(true);
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath());
			}
			catch (Exception e)
			{
			}

			if (!targetFolder.canWrite())
			{
				SystemMessage errorMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_SECURITY_ERROR);
				errorMsg.makeSubstitution(targetFS.getHostAliasName());
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

					boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER);
					if (!doSuperTransferProperty)
					{
						SystemWorkspaceResourceSet flatFromSet = new SystemWorkspaceResourceSet();
						long totalByteSize = getFlatWorkspaceResourceSet(fromSet.getResourceSet(), flatFromSet, monitor);
						
					    if (monitor != null)
						{		
					    	int size = (int)totalByteSize;
						    monitor.beginTask(_uploadMessage.getLevelOneText(), size);
						}  
						// back to hierarchy
						return UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)fromSet, targetFolder, monitor, getShell(), true);
					}
					else
					{
						return UniversalFileTransferUtility.copyWorkspaceResourcesToRemote((SystemWorkspaceResourceSet)fromSet, targetFolder, monitor, getShell(), true);
					}
				}
				else if (fromSet instanceof SystemRemoteResourceSet)
				{
					SystemRemoteResourceSet rmtSet = (SystemRemoteResourceSet)fromSet;
				
					ISystemDragDropAdapter srcAdapter = rmtSet.getAdapter();
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
								children = ((SubSystem)srcSubSystem).internalResolveFilterStrings(monitor, filterReference.getReferencedFilter().getFilterStrings());
							}
							catch (Exception e)
							{
							}	
							
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
						
						return doDrop(downloadedFilterResults, target, sameSystemType, srcSubSystem == targetFS, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE, monitor);
							
					}
					else if (first instanceof IRemoteFile)
					{
						List toCopy = new ArrayList();
						List toCopyNames = new ArrayList();
						List toCopyBatch = new ArrayList();

						for (int i = 0; i < set.size(); i++)
						{	
							IRemoteFile srcFileOrFolder = (IRemoteFile)set.get(i);																									
							if (!srcFileOrFolder.exists())
							{
								SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
								errorMessage.makeSubstitution(srcFileOrFolder.getAbsolutePath(), srcFileOrFolder.getSystemConnection().getAliasName());
								resultSet.setMessage(errorMessage);
								return resultSet;
							}
							if (!srcFileOrFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement())
							{
								SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_ARCHIVEMANAGEMENT_NOTSUPPORTED);
								resultSet.setMessage(errorMessage);
								return resultSet;
							}
			
		
							String name = srcFileOrFolder.getName();
	
							// same systemfor 
							if (sameSystem)
							{
								try
								{
									if (!targetFolder.getAbsolutePath().equals(srcFileOrFolder.getAbsolutePath()))
									{
								
										// should be better doing a query for all in the set																					
										IRemoteFile existingFileOrFolder = ((IRemoteFileSubSystem)srcSubSystem).getRemoteFileObject(targetFolder, name);
										if (existingFileOrFolder.exists())
										{
											ValidatorFileUniqueName validator = null;
											SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(getShell(), true, existingFileOrFolder, validator);									
											dlg.open();
											if (!dlg.wasCancelled())
												name = dlg.getNewName();
											else
												name = null;
											if (name != null)
											{
												toCopy.add(srcFileOrFolder);
												toCopyNames.add(name);
											}
										}
										else if (name != null)
										{
											toCopyBatch.add(srcFileOrFolder);
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
								System.out.println("HOW DID I GET HERE?!!");
							}
						}
						
						for (int x = 0; x < toCopy.size(); x++)
						{
							
							IRemoteFile srcFileOrFolder = (IRemoteFile)toCopy.get(x);
							String name = (String)toCopyNames.get(x);
						
							/*
							SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPY_PROGRESS);
							copyMessage.makeSubstitution(srcFileOrFolder.getName(), targetFolder.getName());
							if (monitor != null)
							{
								monitor.beginTask(copyMessage.getLevelOneText(), 100);
							}
							*/
							try
							{
								if (targetFS.copy(srcFileOrFolder, targetFolder, name, monitor))
								{
									IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, name);
									resultSet.addResource(copiedFile); 
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						
						// deal with batch copies now
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
										IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, srcFileOrFolders[x].getName());
										resultSet.addResource(copiedFile);
									}
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
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
	 * @param sameSystem an indication whether the target and source reside on the same type of system
	 * @param srcType type of source being transferred
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
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath());
			}
			catch (Exception e)
			{
			}

			if (!targetFolder.canWrite())
			{
				SystemMessage errorMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_SECURITY_ERROR);
				errorMsg.makeSubstitution(targetFS.getHostAliasName());
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
					IRemoteFile srcFileOrFolder = null;
					try
					{
						srcFileOrFolder = localFS.getRemoteFileObject((String) src);
					}
					catch (SystemMessageException e)
					{
						return e.getSystemMessage();
					}

					return doDrop(srcFileOrFolder, target, sameSystemType, sameSystem, SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE, monitor);
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
					return UniversalFileTransferUtility.copyWorkspaceResourceToRemote(srcFileOrFolder, targetFolder, monitor, true);
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
							children = filterSubSystem.internalResolveFilterStrings(monitor, filterReference.getReferencedFilter().getFilterStrings());
						}
						catch (Exception e)
						{
						}

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
							SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
							errorMessage.makeSubstitution(srcFileOrFolder.getAbsolutePath(), srcFileOrFolder.getSystemConnection().getAliasName());
							return errorMessage;
						}

						SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPY_PROGRESS);
						copyMessage.makeSubstitution(srcFileOrFolder.getName(), targetFolder.getAbsolutePath());

						IRemoteFileSubSystem localFS = srcFileOrFolder.getParentRemoteFileSubSystem();

						String name = srcFileOrFolder.getName();

						if (localFS == targetFS)
						{
							if (!targetFolder.getAbsolutePath().equals(srcFileOrFolder.getAbsolutePath()))
							{
								IRemoteFile existingFileOrFolder = localFS.getRemoteFileObject(targetFolder, name);

								if (existingFileOrFolder.exists())
								{

									ValidatorFileUniqueName validator = null;
									SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(getShell(), true, existingFileOrFolder, validator);
									dlg.open();
									if (!dlg.wasCancelled())
										name = dlg.getNewName();
									else
										name = null;
								}

								if (name != null)
								{
									monitor.subTask(copyMessage.getLevelOneText());
									if (targetFS.copy(srcFileOrFolder, targetFolder, name, monitor))
									{
										IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, name);
										return copiedFile;
 
									}
								}
							}
						}
						else
						{
							
							System.out.println("how do we get here!??");

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
									targetFS.upload(srcFileOrFolder.getAbsolutePath(), newPath, monitor);

									result = targetFS.getRemoteFileObject(targetFolder, name);
									return result;

								}
								catch (RemoteFileIOException e)
								{
									return e.getSystemMessage();

								}
								catch (RemoteFileSecurityException e)
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

								IRemoteFile newTargetFolder = targetFS.getRemoteFileObject(newPath);
								targetFS.createFolder(newTargetFolder);

								IRemoteFile[] children = localFS.listFoldersAndFiles(srcFileOrFolder);
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
		else
		{
			result = null;
		}

		return result;
	}

	protected String checkForCollision(Shell shell, IRemoteFile targetFolder, String oldName)
	{
		String newName = oldName;

		try
		{

			IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
			IRemoteFile targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName);

			//RSEUIPlugin.logInfo("CHECKING FOR COLLISION ON '"+srcFileOrFolder.getAbsolutePath() + "' IN '" +targetFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...TARGET FILE: '"+tgtFileOrFolder.getAbsolutePath()+"'");  		
			//RSEUIPlugin.logInfo("...target.exists()? "+tgtFileOrFolder.exists());
			if (targetFileOrFolder.exists())
			{
				//monitor.setVisible(false); wish we could!

				// we no longer have to set the validator here... the common rename dialog we all now use queries the input
				// object's system view adaptor for its name validator. See getNameValidator in SystemViewRemoteFileAdapter. phil
				ValidatorFileUniqueName validator = null; // new ValidatorFileUniqueName(shell, targetFolder, srcFileOrFolder.isDirectory());
				//SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
				SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(shell, true, targetFileOrFolder, validator); // true => copy-collision-mode

				dlg.open();
				if (!dlg.wasCancelled())
					newName = dlg.getNewName();
				else
					newName = null;
			}
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e);
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
		//System.out.println("INSIDE CANDELETE FOR ADAPTER: RETURNING " + !file.isRoot());
		return !file.isRoot() && file.canRead();
	}
	/**
	 * Perform the delete action. Defers request to the remote file subsystem
	 * @deprecated use the one with monitor now
	 */
	public boolean doDelete(Shell shell, Object element) throws Exception
	{
		return doDelete(shell, element, null);
	}
	
	/**
	 * Perform the delete action. Defers request to the remote file subsystem
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
				}
				catch (Exception e)
				{					
				}
			}
			
			/*

			ISystemEditableRemoteObject editable = getEditableRemoteObject(file);
			if (editable != null)
			{
				try
				{
					if (editable.checkOpenInEditor() == ISystemEditableRemoteObject.OPEN_IN_SAME_PERSPECTIVE)
					{
						// for now, leave this
					}
					else
					{
						IFile localfile = editable.getLocalResource();
						if (localfile != null)
						{
							// delete this too
							localfile.delete(true, null);
						}
					}
				}
				catch (Exception e)
				{

				}
			}
			*/
			ok = ss.delete(file, monitor);
			
			file.markStale(true);
			parentFile.markStale(true);
		}
		catch (Exception exc)
		{
			ok = false;
			SystemMessageDialog.displayErrorMessage(shell, RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_DELETE_FILE_FAILED).makeSubstitution(file.toString()));
		}
		return ok;
	}

	/**
	 * Perform the delete action. Defers request to the remote file subsystem
	 */
	public boolean doDeleteBatch(Shell shell, List resourceSet, IProgressMonitor monitor) throws Exception
	{
		boolean ok;
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
				ok = false;
				SystemMessageDialog.displayErrorMessage(shell, RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_DELETE_FILE_FAILED).makeSubstitution(file.toString()));
			}
		}
		ok = ss.deleteBatch(files, monitor);
		return ok;
	}
	// FOR COMMON RENAME ACTIONS
	/**
	 * Yes, remote file objects are renamable!
	 */
	public boolean canRename(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		return !file.isRoot() && file.canRead();
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
					moveTempFileProperties(member, ss, remotePath + "/" + member.getName());
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
					// there's an in-memory editable, so change the associated remote file
					IRemoteFile newRemoteFile = ss.getRemoteFileObject(remotePath);
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
	public boolean doRename(Shell shell, Object element, String newName) throws Exception
	{
		boolean ok = true;
		IRemoteFile file = (IRemoteFile) element;
		IRemoteFileSubSystem ss = file.getParentRemoteFileSubSystem();
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		try
		{
			
			String newRemotePath = file.getParentPath() + "/" + newName;
			IResource localResource = null;
			if (SystemRemoteEditManager.getDefault().doesRemoteEditProjectExist())
			{
				localResource = UniversalFileTransferUtility.getTempFileFor(file);
			}
					
			ss.rename(file, newName);
			if (localResource != null && localResource.exists())
			{
				
				IPath newLocalPath = localResource.getParent().getFullPath().append(newName);
				moveTempResource(localResource, newLocalPath, ss, newRemotePath);
			}

			if (file.isDirectory())
			{
				// update all tree views showing this remote folder...
				// Hmm, why do we do this, given SystemView sends a rename event? I think we needed to refresh all child cached references to parent folder name...
				SystemResourceChangeEvent event = new SystemResourceChangeEvent(file.getParentRemoteFile(), ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, null);
				sr.fireEvent(event);
				//sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED, file, file.getParentRemoteFile(), file.getParentRemoteFileSubSystem(), null, null);
			}		
			file.markStale(true);

		}
		catch (Exception exc)
		{
			ok = false;
			SystemMessageDialog.displayErrorMessage(shell, RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_RENAME_FILE_FAILED).makeSubstitution(file.toString()));
		}
		return ok;
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
				return file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().getFolderNameValidator();
			else
				return file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().getFileNameValidator();
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
			if (file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().isUnixStyle())
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
			if (file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().isUnixStyle())
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
	/**
	 * Return fully qualified name that uniquely identifies this object within its subsystem
	 */
	public String getAbsoluteName(Object element)
	{
		IRemoteFile file = (IRemoteFile) element;
		return file.getAbsolutePath();
	}
	/**
	 * Return fully qualified name that uniquely identifies this remote object's remote parent within its subsystem
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
		return null;
	}
	/** 
	 * Return the subsystem factory id that owns this remote object
	 * The value must not be translated, so that property pages registered via xml can subset by it.
	 */
	public String getSubSystemFactoryId(Object element)
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
				srcType = "blank";
			else if (srcType.length() == 0)
				srcType = "null";
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
		//System.out.println("INSIDE REFRESHREMOTEOBJECT");
		if (oldElement instanceof RemoteFile)
		{
			/* FIXME do we still need this?
			RemoteFile oldFile = (RemoteFile) oldElement;
			RemoteFile newFile = (RemoteFile) newElement;

			oldFile.setName(newFile.getName());
			oldFile.setCanRead(newFile.canRead());
			oldFile.setCanWrite(newFile.canWrite());
			oldFile.setIsHidden(newFile.isHidden());
			oldFile.setLastModified(newFile.getLastModified());
			oldFile.setLength(newFile.getLength());
			//  if (oldFile instanceof LocalFileImpl)
			//  {
			// 	LocalFileImpl oldLocalFile = (LocalFileImpl)oldFile;
			// 	LocalFileImpl newLocalFile = (LocalFileImpl)newFile;
			oldFile.setFile(newFile.getFile());
			// } 
			return oldFile.isDirectory(); // refresh kids if this is a directory
			*/
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
	public Object getRemoteParent(Shell shell, Object element) throws Exception
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
	public String[] getRemoteParentNamesInUse(Shell shell, Object element) throws Exception
	{
		String[] names = EMPTY_STRING_LIST;

		IRemoteFile file = (IRemoteFile) element;
		String parentName = file.getParentPath();
		if (parentName == null) // given a root?
			return names; // not much we can do. Should never happen: you can't rename a root!

		// DKM - changed this so that we can take advantage of caching
		Object[] children = getChildren(file.getParentRemoteFile());
		if ((children == null) || (children.length == 0))
			return names;

		names = new String[children.length];
		for (int idx = 0; idx < names.length; idx++)
			names[idx] = ((IRemoteFile) children[idx]).getName();

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
		else if (!remoteFile.isArchive() || !remoteFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement())
		{
			// only handle double click if object is a file
			ISystemEditableRemoteObject editable = getEditableRemoteObject(remoteFile);
			if (editable != null)
			{
				try
				{
					if (editable.checkOpenInEditor() != ISystemEditableRemoteObject.OPEN_IN_SAME_PERSPECTIVE)
					{
						editable.open(getShell());
					}
					else
					{
						editable.setLocalResourceProperties();
						editable.openEditor();
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

	public boolean canEdit(Object element)
	{
		IRemoteFile remoteFile = (IRemoteFile) element;
		if (remoteFile.isFile())
		{
			return remoteFile.canRead();
		}
		return false;
	}

	public ISystemEditableRemoteObject getEditableRemoteObject(Object element)
	{
		RemoteFile remoteFile = (RemoteFile) element;
		if (remoteFile.isFile())
		{
			try
			{
				IFile file = getCachedCopy(remoteFile);
				if (file != null)
				{
					SystemIFileProperties properties = new SystemIFileProperties(file);
					Object obj = properties.getRemoteFileObject();
					if (obj != null && obj instanceof ISystemEditableRemoteObject)
					{
						ISystemEditableRemoteObject rmtObj = (ISystemEditableRemoteObject) obj;
						IAdaptable rmtFile = rmtObj.getRemoteObject();
						if (rmtFile instanceof IRemoteFile)
						{
							//((IRemoteFile)rmtFile).markStale(true);
						}
						return rmtObj;
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
		if (SystemRemoteEditManager.getDefault().doesRemoteEditProjectExist())
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
				return remoteFile.getAbsolutePath() + remoteFile.getSeparator() + "*";
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
			
			
			if (inName.equals("classification")) 
			{
				String classification = tgt.getClassification();
				
				if (classification != null)
				{
					return StringCompare.compare(value, classification, true);
				}
			}
			else if (inName.equals("name"))
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
				if (val.endsWith("*"))  {
					return tgtName.startsWith(val.substring(0, val.length()-1));
				} 
				else {
					return val.equals(tgtName);
				}
			}
			else if (inName.equals("absolutePath".toLowerCase())) {
				
				boolean caseSensitive = tgt.getParentRemoteFileSubSystem().isCaseSensitive();
				
				String tgtPath = getAbsoluteName(target);
				String val = value;
				
				// if case does not matter, then lower case the compares
				if (!caseSensitive) {
					tgtPath = tgtPath.toLowerCase();
					val = val.toLowerCase();
				}
				
				// we have a wild card test, and * is the last character in the value
				if (val.endsWith("*"))  {
					return tgtPath.startsWith(val.substring(0, val.length()-1));
				}
				else {
					return val.equals(tgtPath);
				}
			}
			else if (inName.equals("extension")) {
				
				boolean caseSensitive = tgt.getParentRemoteFileSubSystem().isCaseSensitive();
				
				String tgtExtension = tgt.getExtension();
				
				if (tgtExtension == null) {
					return false;
				}
				
				StringTokenizer st = new StringTokenizer(value, " \t\n\r\f,");
				
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
                    if (val.endsWith("*")) {
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
			else if (inName.equals("isroot"))
            {
				return tgt.isRoot() && value.equals("true") ||
					!tgt.isRoot() && value.equals("false");				
			}
			else if (inName.equals("isfile"))
			{
				return tgt.isFile() && value.equals("true") ||
					!tgt.isFile() && value.equals("false");				
			}
			else if (inName.equals("isdirectory"))
			{
				return tgt.isDirectory() && value.equals("true") ||
					!tgt.isDirectory() && value.equals("false");				
			}
			else if (inName.equals("ishidden")) 
			{
				return tgt.isHidden() && value.equals("true") ||
					!tgt.isHidden() && value.equals("false");				
			}
			else if (inName.equals("canread")) 
			{
				return tgt.canRead() && value.equals("true") ||
					!tgt.canRead() && value.equals("false");				
			}
			else if (inName.equals("canwrite")) 
			{
				return tgt.canWrite() && value.equals("true") ||
					!tgt.canWrite() && value.equals("false");				
			}
			else if (inName.equals("isbinary")) 
			{
				return tgt.isBinary() && value.equals("true") ||
					!tgt.isBinary() && value.equals("false");				
			}
			else if (inName.equals("istext")) 
			{
				return tgt.isText() && value.equals("true") ||
					!tgt.isText() && value.equals("false");				
			}
			else if (inName.equals("isarchive")) {
				return tgt.isArchive() && value.equals("true") ||
					!tgt.isArchive() && value.equals("false");				
			}			
			else if (inName.equals("isvirtual")) 
			{
				return tgt instanceof IVirtualRemoteFile && value.equals("true") ||
					!(tgt instanceof IVirtualRemoteFile) && value.equals("false");
			}
			else if (inName.equals("isexecutable")) 
			{		
				return tgt.isExecutable() && value.equals("true") ||
				 !tgt.isExecutable() && value.equals("false");
			}
			else if (inName.equals("islink")) 
			{		
				return tgt.isLink() && value.equals("true") ||
				 !tgt.isLink() && value.equals("false");
			}
		}
		
		return super.testAttribute(target, name, value);
	}
	
	/*
	 * Return whether deferred queries are supported.
	 */
	public boolean supportsDeferredQueries()
	{
	    return true;
	}
	

	protected SystemFetchOperation getSystemFetchOperation(Object o, IElementCollector collector)
	{
	    return new SystemFetchOperation(null, (IAdaptable)o, this, collector, true);
	}

	/**
	 * Returns <code>false</code> if the file is a virtual file, otherwise defaults to asking the subsystem
	 * factory.
	 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter#supportsUserDefinedActions(java.lang.Object)
	 */
	public boolean supportsUserDefinedActions(Object object) {
		
		// if object is an instance of a remote file
		if (object instanceof IRemoteFile) {
			
			IRemoteFile file = (IRemoteFile)object;
			
			// virtual files do not support user defined actions
			if (file instanceof IVirtualRemoteFile) {
				return false;
			}
			else {
				return getSubSystem(object).getSubSystemConfiguration().supportsUserDefinedActions();
			}
		}
		else {
			return getSubSystem(object).getSubSystemConfiguration().supportsUserDefinedActions();
		}
	}
}