/********************************************************************************
 * Copyright (c) 2010 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight     (IBM)  - [160105] [usability] Universal action needed to locate a resource in the Remote Systems View
 * David McKnight     (IBM)  - [218227][usability] Contribute a "Show in RSE" action to Resource Navigator and Project Explorer
 * David McKnight     (IBM)  - [341573] NPE on Show in Remote Systems view from Remote System Details view
 ********************************************************************************/
package org.eclipse.rse.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
import org.eclipse.rse.ui.view.ContextObject;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ShowInSystemsViewDelegate implements IViewActionDelegate {

	/**
	 * Main thread runnable used to create tree items in system view and look for the target remote object
	 * item in the tree.  If the remote object item is not found, then this indirectly recurses via a new
	 * LinkFromContainerJob.
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
			if (_filterReference != null){
				// make sure the filter is expanded
				_systemTree.revealAndExpand(_filterReference.getSubSystem(), _filterReference.getReferencedFilter());
			}
			else {
				// make sure the parent is expanded
				List matches = new ArrayList();
				_systemTree.findAllRemoteItemReferences(_parentObject, _parentObject, matches);
				if (matches.size() > 0){
					TreeItem item = (TreeItem)matches.get(0);
					item.setExpanded(true);
				}
			}

			Vector matches = new Vector();

			_systemTree.findAllRemoteItemReferences(_parentObject, _parentObject, matches);
			if (matches.size() > 0){
				TreeItem item = (TreeItem)matches.get(0);
				_systemTree.createTreeItems(item, _children);
				item.setExpanded(true);

				IAdaptable container = null;

				String targetPath = getAbsolutePath(_targetRemoteObj);

				// is one of these items our remote object
				for (int i = 0; i < item.getItemCount(); i++){
					TreeItem childItem = item.getItem(i);
					Object data = childItem.getData();
					if (data instanceof IAdaptable){
						IAdaptable childObj = (IAdaptable)data;
						String childPath = getAbsolutePath(childObj);

						if (childPath.equals(targetPath)){
							// select our remote file
							_systemTree.getTree().setSelection(childItem);
							return; // we're done!
						}
						else if (targetPath.startsWith(childPath)){
							container = childObj; // using this to start a deeper search for the target remote file
						}
					}
				}

				// remote file not found so now we have to expand further
				if (container != null){
					LinkFromContainerJob job = new LinkFromContainerJob(container, _filterReference, _targetRemoteObj, _systemTree);
					job.schedule();
				}
			}
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
			super(NLS.bind(CommonMessages.MSG_RESOLVE_PROGRESS, ShowInSystemsViewDelegate.getAdapter(targetRemoteObject).getAbsoluteName(targetRemoteObject)));

			_targetRemoteObj = targetRemoteObject;
			_subSystem = getSubSystem(_targetRemoteObj);
			_systemTree = systemTree;
		}



		private ISubSystem getSubSystem(IAdaptable adaptable)
		{
			ISystemViewElementAdapter adapter = ShowInSystemsViewDelegate.getAdapter(adaptable);
			return adapter.getSubSystem(adaptable);
		}

		public IStatus run(IProgressMonitor monitor){
			try
			{
				// find matching filter reference
				ISystemFilterReference ref = findMatchingFilterReference(_targetRemoteObj, monitor);
				if (ref == null)
				{
					// the object is nowhere to be found!
					return Status.OK_STATUS;
				}

				// get the context
				ContextObject contextObject = new ContextObject(ref, _subSystem, ref);

				// get the children
				Object[] children = ShowInSystemsViewDelegate.getAdapter((IAdaptable)ref).getChildren(contextObject, monitor);

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

		private ISystemFilterReference findMatchingFilterReference(IAdaptable targetObj, IProgressMonitor monitor)
		{
			String remoteObjectName = getAbsolutePath(targetObj);
	    	ISystemFilterPoolReferenceManager refmgr = _subSystem.getFilterPoolReferenceManager();
	    	if (refmgr != null){
			    ISystemFilterReference[] refs = refmgr.getSystemFilterReferences(_subSystem);
			    for (int i = 0; i < refs.length; i++) {
			        ISystemFilterReference ref = refs[i];

			        if (doesFilterEncompass(ref.getReferencedFilter(), remoteObjectName)){
			        	return ref;
			        }
			    }

			    // not finding it..use registry to try this
				ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
				List matches = sr.findFilterReferencesFor(targetObj, _subSystem, false);
				if (matches != null && matches.size() > 0){
					return (ISystemFilterReference)matches.get(0);
				}

				// child may be deep in the tree and not directly under a filter
				ISystemViewElementAdapter adapter = ShowInSystemsViewDelegate.getAdapter(targetObj);
				Object parent = adapter.getParent(targetObj);
				ISystemFilterReference filterRef = findMatchingFilterReference((IAdaptable)parent, monitor);
			    if (filterRef != null){
			    	return filterRef;
			    }
	    	}
	    	return null;
		}

		private String getAbsolutePath(IAdaptable adaptable){
			ISystemViewElementAdapter adapter = ShowInSystemsViewDelegate.getAdapter(adaptable);
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
			return false;
		}

	}

	/**
	 * Job for doing a query on a container and then using Display.asyncExec() to reveal the results in the tree.
	 */
	private class LinkFromContainerJob extends Job
	{
		private ISubSystem _subSystem;
		private IAdaptable _remoteContainer;
		private IAdaptable _targetRemoteObj;
		private ISystemTree _systemTree;
		private ISystemFilterReference _filterRef;

		public LinkFromContainerJob(IAdaptable remoteContainer, ISystemFilterReference filterRef, IAdaptable targetRemoteObj, ISystemTree systemTree) {

			super(NLS.bind(CommonMessages.MSG_RESOLVE_PROGRESS, ShowInSystemsViewDelegate.getAdapter(remoteContainer).getAbsoluteName(targetRemoteObj)));

			_remoteContainer = remoteContainer;
			_subSystem = getSubSystem(remoteContainer);
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
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)_remoteContainer.getAdapter(ISystemViewElementAdapter.class);

				// get the context
				ContextObject contextObject = new ContextObject(_remoteContainer, _subSystem, _filterRef);

				// get the children
				Object[] children = adapter.getChildren(contextObject, monitor);

				if (monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}

				// put these items in the tree and look for remoteFile
				// if we can't find the remote file under this filter, the ShowChildrenInTree will recurse
				Display.getDefault().asyncExec(new ShowChildrenInTree(_remoteContainer, children, _filterRef, _systemTree, _targetRemoteObj));
			}
			catch (Exception e){
			}
			return Status.OK_STATUS;
		}

	}
	private IAction _action;
	protected Object _selectedObject;
	private SystemViewPart _systemViewPart;

	public void init(IViewPart view) {

	}

	public void run(IAction action) {
		SystemViewPart viewPart = activateSystemView();
		SystemView systemTree = viewPart.getSystemView();

		// now we've got to show the object in this view part
		TreeItem item = (TreeItem)systemTree.findFirstRemoteItemReference(_selectedObject, null);
		if (item != null){
			systemTree.getTree().setSelection(item);
		}
		else if (_selectedObject instanceof IAdaptable)
		{
			ISystemViewElementAdapter adapter = getAdapter((IAdaptable)_selectedObject);
			if (adapter != null){
				ISubSystem subSystem = adapter.getSubSystem(_selectedObject);
				if (subSystem != null){
					if (subSystem.getSubSystemConfiguration().supportsFilters()){
						// no match, so we will expand from filter
						// query matching filter in  a job (to avoid main thread)
						LinkFromFilterJob job = new LinkFromFilterJob((IAdaptable)_selectedObject, systemTree);
						job.schedule();
					}
					else {
						// no filters so need to directly check children
						Object[] children = subSystem.getChildren();

						// put these items in the tree and look for remote object
						// if we can't find the remote object under this, the ShowChildrenInTree will recurse
						Display.getDefault().asyncExec(new ShowChildrenInTree(subSystem, children, null, systemTree, (IAdaptable)_selectedObject));
					}
				}
			}
		}
	}
	
	


	public SystemViewPart activateSystemView(){
        try
        {
        	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
             _systemViewPart = (SystemViewPart) page.showView(SystemViewPart.ID);
            page.bringToTop(_systemViewPart);
        }
        catch (PartInitException e)
        {
        	e.printStackTrace();
           	SystemBasePlugin.logError("Can not open system view part", e); //$NON-NLS-1$
        }

        return _systemViewPart;
    }

	public void selectionChanged(IAction action, ISelection selection) {
		if (_action == null) {
			_action= action;
		}
		IStructuredSelection sel = (IStructuredSelection)selection;
		if (sel.size() == 1){			
			_selectedObject = sel.getFirstElement();
			if (_selectedObject instanceof ISystemPromptableObject){
				_selectedObject = null; // promptables not supported here
				_action.setEnabled(false);
			}
			else {
				_action.setEnabled(true);
			}
		}
		else {
			_action.setEnabled(false);
		}
	}

	public static ISystemViewElementAdapter getAdapter(IAdaptable adaptable)
	{
		return (ISystemViewElementAdapter)adaptable.getAdapter(ISystemViewElementAdapter.class);
	}
}
