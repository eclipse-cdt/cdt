/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)   - [187711] Link with Editor action for System View
 * David McKnight   (IBM)   - [238294] ClassCastException using Link With Editor
 * David McKnight   (IBM)   - [281309] RSE Explorer View is not able to be sync with Editor in next Eclipse launched
 * David McKnight   (IBM)   - [354283] Refresh highlights wrong tab when two files of same name are open
 *******************************************************************************/
package org.eclipse.rse.internal.files.ui.actions;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.view.ContextObject;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.IViewLinker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;

public class LinkWithSystemViewAction implements IViewActionDelegate {

	/**
	 * Main thread runnable used to create tree items in system view and look for the target remote file
	 * item in the tree.  If the remote file item is not found, then this indirectly recurses via a new
	 * LinkFromFolderJob.
	 */
	private class ShowChildrenInTree implements Runnable
	{
		private Object _parentObject;
		private Object[] _children;
		private ISystemTree _systemTree;
		private IAdaptable _targetRemoteObj;
		private ISystemFilterReference _filterReference;
		
		public ShowChildrenInTree(Object parentObject, Object[] children, ISystemFilterReference filterReference, ISystemTree systemTree, IAdaptable targetRemoteObj)
		{
			_parentObject = parentObject;
			_children = children;
			_systemTree = systemTree;
			_targetRemoteObj = targetRemoteObj;
			_filterReference = filterReference;
		}
		
		private String getAbsolutePath(IAdaptable adaptable){
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)adaptable.getAdapter(ISystemViewElementAdapter.class);
			return adapter.getAbsoluteName(adaptable);
		}
		
		public void run()
		{ 	
			// make sure the filter is expanded
			_systemTree.revealAndExpand(_filterReference.getSubSystem(), _filterReference.getReferencedFilter());
			
			Vector matches = new Vector();
			
			_systemTree.findAllRemoteItemReferences(_parentObject, _parentObject, matches);
			if (matches.size() > 0){
				TreeItem item = (TreeItem)matches.get(0);
				_systemTree.createTreeItems(item, _children);				
				item.setExpanded(true);
				
				IAdaptable containingFolder = null;
				
				// is one of these items our remote file?
				for (int i = 0; i < item.getItemCount(); i++){
					TreeItem childItem = item.getItem(i);
					Object data = childItem.getData();
					if (data instanceof IAdaptable){
						IAdaptable childObj = (IAdaptable)data;
						String childPath = getAbsolutePath(childObj);
						String targetPath = getAbsolutePath(_targetRemoteObj);
						if (childPath.equals(targetPath)){
							// select our remote file
							_systemTree.getTree().setSelection(childItem);
							return; // we're done!
						}
						else if (targetPath.startsWith(childPath)){
							containingFolder = childObj; // using this to start a deeper search for the target remote file
						}
					}					
				}
				
				// remote file not found so now we have to expand further
				if (containingFolder != null){						
					LinkFromFolderJob job = new LinkFromFolderJob(containingFolder, _filterReference, _targetRemoteObj, _systemTree);
					job.schedule();
				}
			}
		}
	}
	
	/**
	 * Job for doing a query on a folder and then using Display.asyncExec() to reveal the results in the tree.
	 */
	private class LinkFromFolderJob extends Job
	{
		private ISubSystem _subSystem;
		private IAdaptable _remoteFolder;
		private IAdaptable _targetRemoteObj;
		private ISystemTree _systemTree;
		private ISystemFilterReference _filterRef;
		
		public LinkFromFolderJob(IAdaptable remoteFolder, ISystemFilterReference filterRef, IAdaptable targetRemoteObj, ISystemTree systemTree) {
			super(FileResources.MESSAGE_EXPANDING_FOLDER);
			_remoteFolder = remoteFolder;
			_subSystem = getSubSystem(remoteFolder);
			_filterRef = filterRef; // used for context of query
			_targetRemoteObj = targetRemoteObj;
			_systemTree = systemTree;
		}
		
		private ISubSystem getSubSystem(IAdaptable adaptable)
		{
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)adaptable.getAdapter(ISystemViewElementAdapter.class);
			return adapter.getSubSystem(adaptable);
		}
		
		public IStatus run(IProgressMonitor monitor){
			try
			{				
				// get the adapter
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)(_remoteFolder).getAdapter(ISystemViewElementAdapter.class);
				
				// get the context
				ContextObject contextObject = new ContextObject(_remoteFolder, _subSystem, _filterRef);
					
				// get the children	
				Object[] children = adapter.getChildren(contextObject, monitor);

				if (monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}
												
				// put these items in the tree and look for remoteFile
				// if we can't find the remote file under this filter, the ShowChildrenInTree will recurse
				Display.getDefault().asyncExec(new ShowChildrenInTree(_remoteFolder, children, _filterRef, _systemTree, _targetRemoteObj));
			}
			catch (Exception e){				
			}
			return Status.OK_STATUS;
		}		
		
	}
	
	/**
	 * Job for doing a query on a filter and then using Display.asyncExec() to reveal the results in the tree.
	 */
	private class LinkFromFilterJob extends Job
	{
		private ISubSystem _subSystem;
		private IAdaptable _targetRemoteObj;
		private ISystemTree _systemTree;
		
		public LinkFromFilterJob(IAdaptable targetRemoteObject, ISystemTree systemTree) {
			super(FileResources.MESSAGE_EXPANDING_FILTER);
			
			_targetRemoteObj = targetRemoteObject;
			_subSystem = getSubSystem(_targetRemoteObj);
			_systemTree = systemTree;
		}
		
		private ISystemViewElementAdapter getAdapter(IAdaptable adaptable)
		{
			return (ISystemViewElementAdapter)adaptable.getAdapter(ISystemViewElementAdapter.class);
		}
		
		private ISubSystem getSubSystem(IAdaptable adaptable)
		{
			ISystemViewElementAdapter adapter = getAdapter(adaptable);
			return adapter.getSubSystem(adaptable);
		}
		
		public IStatus run(IProgressMonitor monitor){
			try
			{
				// find matching filter reference
				ISystemFilterReference ref = findMatchingFilterReference();
				if (ref == null)
				{
					// the object is nowhere to be found!
					return Status.OK_STATUS;
				}

				// get the context
				ContextObject contextObject = new ContextObject(ref, _subSystem, ref);
				
				// get the children	
				Object[] children = getAdapter((IAdaptable)ref).getChildren(contextObject, monitor);
				
				if (monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}
				
				// put these items in the tree and look for remoteFile
				// if we can't find the remote file under this filter, the ShowChildrenInTree will recurse
				Display.getDefault().asyncExec(new ShowChildrenInTree(ref, children, ref, _systemTree, _targetRemoteObj));
			}
			catch (Exception e){	
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}		
		
		private ISystemFilterReference findMatchingFilterReference()
		{
			String remoteObjectName = getAbsolutePath(_targetRemoteObj);
	    	ISystemFilterPoolReferenceManager refmgr = _subSystem.getFilterPoolReferenceManager();
	    	if (refmgr != null){
			    ISystemFilterReference[] refs = refmgr.getSystemFilterReferences(_subSystem);
			    for (int i = 0; i < refs.length; i++) {
			        ISystemFilterReference ref = refs[i];

			        if (doesFilterEncompass(ref.getReferencedFilter(), remoteObjectName)){
			        	return ref;
			        }
			    }
	    	}
	    	return null;
		}
		
		private String getAbsolutePath(IAdaptable adaptable){
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)adaptable.getAdapter(ISystemViewElementAdapter.class);
			return adapter.getAbsoluteName(adaptable);
		}
		
		private boolean doesFilterEncompass(ISystemFilter filter, String remoteObjectAbsoluteName)
		{
			boolean would = false;
			String[] strings = filter.getFilterStrings();
	    	if (strings != null){
	      	  for (int idx=0; !would && (idx<strings.length); idx++)
	    	  {
	    	  	 if (strings[idx].equals("/*")) //$NON-NLS-1$
	    	  	   would = true;
	    	  	 else if (strings[idx].equals("./*")) //$NON-NLS-1$
	    	  	 {
	    	  		 // my home filter - will encompass iff remoteObjectAbsoluteName is within the home dir	    	  		 	    	  		
	    	  		 try
	    	  		 {	
	    	  			 IAdaptable homeObj = (IAdaptable)_subSystem.getObjectWithAbsoluteName(".", new NullProgressMonitor()); //$NON-NLS-1$
	    	  			 if (homeObj != null){
	    	  				 String homePath = getAbsolutePath(homeObj);
	    	  				 would = remoteObjectAbsoluteName.startsWith(homePath);
	    	  			 }
	    	  		 }
	    	  		 catch (Exception e){	    	  			 
	    	  		 }
	    	  	 }
	    	  	 else
	    	       would = doesFilterStringEncompass(strings[idx], remoteObjectAbsoluteName);    		
	    	  }
	    	}
	    	return would;
		}
		
		private boolean doesFilterStringEncompass(String filterString, String remoteObjectAbsoluteName)
		{
			if (_subSystem instanceof IRemoteFileSubSystem){
				RemoteFileFilterString rffs = new RemoteFileFilterString(((IRemoteFileSubSystem)_subSystem).getParentRemoteFileSubSystemConfiguration(), filterString);
				// ok, this is a tweak: if the absolute name has " -folder" at the end, that means it is a folder...
				if (remoteObjectAbsoluteName.endsWith(" -folder")) //$NON-NLS-1$
				{
					if (!rffs.getShowSubDirs())
						return false;
					remoteObjectAbsoluteName = remoteObjectAbsoluteName.substring(0, remoteObjectAbsoluteName.indexOf(" -folder")); //$NON-NLS-1$
				}
				// problem 1: we don't know if the given remote object name represents a file or folder. We have to assume a file,
				//  since we don't support filtering by folder names.
				if (!rffs.getShowFiles())
					return false;

				// step 1: verify the path of the remote object matches the path of the filter string
				String container = rffs.getPath();
				if (container == null)
					return false;
			
				if (container.equals(".")) //$NON-NLS-1$
				{
					try 
					{
						IAdaptable containerObj = (IAdaptable)_subSystem.getObjectWithAbsoluteName(container, new NullProgressMonitor());
						if (containerObj != null){
							container = getAbsolutePath(containerObj);
						}
					}
					catch (Exception e)
					{		        
					}
				}
			
				if (remoteObjectAbsoluteName.startsWith(container)){
					return true;
				}
			}
			
			return false;
		}
		
	}
	
	/**
	 * Job for doing a query on a file.  After the query it checks for the file in the tree on the main thread.  If the item
	 * is not found, then a search is started from the first matching filter via the LinkFromFilterJob.  
	 */
	private class SelectFileJob extends Job
	{
		private ISubSystem _subSystem;
		private String _path;
		private ISystemTree _systemTree;
		
		public SelectFileJob(ISubSystem subSystem, String path, ISystemTree systemTree) {
			super(FileResources.MESSSAGE_QUERYING_FILE);
			_subSystem = subSystem;
			_path = path;
			_systemTree = systemTree;
		}
		
		public IStatus run(IProgressMonitor monitor){
			try
			{
				// doing query to get the remote file
				final IAdaptable remoteObj = (IAdaptable)_subSystem.getObjectWithAbsoluteName(_path, monitor);

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						// on main thread, looking for the reference in the tree
						TreeItem item = (TreeItem)_systemTree.findFirstRemoteItemReference(remoteObj, null);
						if (item != null){
							_systemTree.getTree().setSelection(item);
						}
						else if (remoteObj != null)
						{
							// no reference in the tree so we will search forward from the filter in a job (avoiding query on the main thread)
							LinkFromFilterJob job = new LinkFromFilterJob(remoteObj, _systemTree);
							job.schedule();
						}
					}					
				});
			}
			catch (Exception e){				
			}
			return Status.OK_STATUS;
		}
	}
	
	public class ViewLinker implements IViewLinker
	{
		public void linkViewToEditor(Object remoteObject, IWorkbenchPage page)
		{
			Object obj = remoteObject;
			if (obj instanceof IAdaptable)
			{
				try
				{
					ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter)((IAdaptable)obj).getAdapter(ISystemRemoteElementAdapter.class);
					if (adapter != null)
					{
						
						if (adapter.canEdit(obj))
						{
							IEditorReference[] editorRefs = page.getEditorReferences();
							for (int i = 0; i < editorRefs.length; i++)
							{
								IEditorReference editorRef = editorRefs[i];
							
								IEditorPart editor = editorRef.getEditor(true);
								if (editor != null)
								{
									IEditorInput input = editor.getEditorInput();
									if (input instanceof FileEditorInput)
									{
										((FileEditorInput)input).getFile();				
										IFile file = ((FileEditorInput)input).getFile();				
										if (file.getProject().getName().equals(SystemRemoteEditManager.REMOTE_EDIT_PROJECT_NAME))
										{
											SystemIFileProperties properties = new SystemIFileProperties(file);
											String path = properties.getRemoteFilePath();																													
											
											if (path != null && path.equals(adapter.getAbsoluteName(obj)))
											{
												ISubSystem objSS = adapter.getSubSystem(obj);
												ISubSystem editorSS = null;
												// make sure this is for the correct file subsystem
												String subsystemID = properties.getRemoteFileSubSystem();
												if (subsystemID != null){
													ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
													editorSS = registry.getSubSystem(subsystemID);
												}
												if (objSS != null && editorSS != null && objSS.equals(editorSS)){													
													page.bringToTop(editor);
													return;
												}												
											}
										}								
									}											
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}						
			}	
		}
		
		public void linkEditorToView(IEditorPart editor, ISystemTree systemTree)
		{
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput)
			{
				IFileEditorInput fileInput = (IFileEditorInput) input;
				fileInput.getFile();

				IFile file = fileInput.getFile();
				SystemIFileProperties properties = new SystemIFileProperties(file);
				Object rmtEditable = properties.getRemoteFileObject();
				IAdaptable remoteObj = null;
				ISubSystem subSystem = null;
				if (rmtEditable != null && rmtEditable instanceof ISystemEditableRemoteObject)
				{
					ISystemEditableRemoteObject editable = (ISystemEditableRemoteObject) rmtEditable;
					remoteObj = editable.getRemoteObject();
					
					TreeItem item = (TreeItem)systemTree.findFirstRemoteItemReference(remoteObj, null);
					if (item != null){
						systemTree.getTree().setSelection(item);
					}
					else
					{
						ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)remoteObj.getAdapter(ISystemViewElementAdapter.class);
						subSystem = adapter.getSubSystem(remoteObj);
						
						// no match, so we will expand from filter
						// query matching filter in  a job (to avoid main thread)
						LinkFromFilterJob job = new LinkFromFilterJob(remoteObj, systemTree);
						job.schedule();
						
					}					
				}
				else
				{
					String subsystemId = properties.getRemoteFileSubSystem();
					String path = properties.getRemoteFilePath();
					if (subsystemId != null && path != null)
					{
						subSystem = RSECorePlugin.getTheSystemRegistry().getSubSystem(subsystemId);
						if (subSystem != null)
						{
							// query for file in a job (to avoid main thread)
							SelectFileJob job = new SelectFileJob(subSystem, path, systemTree);
							job.schedule();
						}
					}
				}
			}
		}

		
	}
	
	private SystemViewPart _systemViewPart;
	private IAction _action;
	private IViewLinker _linker;
	
	public LinkWithSystemViewAction()
	{
		super();
	}
	
	public void init(IViewPart view) {
		_systemViewPart = (SystemViewPart)view;
		_linker = new ViewLinker();
				
		boolean isLinkingEnabled = _systemViewPart.isLinkingEnabled();
		if (isLinkingEnabled){
			// set it here by default to true so that we have a _linker at the start
			// and restore from memento will be able to use the linker
			_systemViewPart.setLinkingEnabled(isLinkingEnabled, _linker); 
		}
	}

	public void run(IAction action) {
		if (_systemViewPart != null){
			boolean isToggled = action.isChecked();
			_systemViewPart.setLinkingEnabled(isToggled, _linker);
			
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (_action == null) {
			_action= action;
			_action.setChecked(_systemViewPart.isLinkingEnabled());
		}
		if (_systemViewPart.isLinkingEnabled() && !_action.isChecked()){ // if restored from memento
			_action.setChecked(true);
			_systemViewPart.setLinkingEnabled(true, _linker);
		}
	}
}
