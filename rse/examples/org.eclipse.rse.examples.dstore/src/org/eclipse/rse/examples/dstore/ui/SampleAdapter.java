/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.examples.dstore.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.examples.dstore.subsystems.RemoteSampleObject;
import org.eclipse.rse.examples.dstore.subsystems.SampleRootResource;
import org.eclipse.rse.examples.dstore.subsystems.SampleSubSystem;
import org.eclipse.rse.examples.dstore.ui.actions.NewSampleObjectAction;
import org.eclipse.rse.examples.dstore.ui.actions.SampleAction;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class SampleAdapter extends AbstractSystemViewAdapter implements
		ISystemRemoteElementAdapter {
	
	private SampleAction _sampleAction;
	private NewSampleObjectAction _newSampleObjectAction;
	private NewSampleObjectAction _newSampleContainerAction;

	@Override
	public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
		RemoteSampleObject object = getSampleObject(element);
		if (object != null){
			if (object.isContainer()){
				SampleSubSystem ss = (SampleSubSystem)object.getSubSystem();
				return ss.list(object, monitor);
			}
		}
		return null;
	}


	@Override
	public Object getParent(Object element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getParent();
		}
		return null;
	}

	@Override
	public String getType(Object element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getType();
		}
		return null;
	}

	@Override
	public boolean hasChildren(IAdaptable element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.isContainer();
		}
		return false;
	}

	@Override
	protected Object internalGetPropertyValue(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAbsoluteParentName(Object element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getParent().getAbsolutePath();
		}
		return null;
	}

	public Object getRemoteParent(Object element, IProgressMonitor monitor)
			throws Exception {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getParent();
		}
		return null;
	}

	public String[] getRemoteParentNamesInUse(Object element,
			IProgressMonitor monitor) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean refreshRemoteObject(Object oldElement, Object newElement) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getRemoteSubType(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteType(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteTypeCategory(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSubSystemConfigurationId(Object element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getSubSystem().getSubSystemConfiguration().getId();
		}
		return null;
	}

	public String getText(Object element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getName();
		}
		return null;
	}

	public String getAbsoluteName(Object element) {
		
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			return obj.getAbsolutePath();
		}
		return null;
	}

	@Override
	public void addActions(SystemMenuManager menu,
			IStructuredSelection selection, Shell parent, String menuGroup) {
		Object firstSelection = selection.getFirstElement();
		if (firstSelection instanceof RemoteSampleObject){
			if (!(firstSelection instanceof SampleRootResource)){
				if (_sampleAction == null){
					_sampleAction = new SampleAction(parent);
				}		
				menu.add(ISystemContextMenuConstants.GROUP_CHANGE, _sampleAction);
			}
			
			if (_newSampleObjectAction == null){
				_newSampleObjectAction = new NewSampleObjectAction(shell, false);
			}
			if (_newSampleContainerAction == null){
				_newSampleContainerAction = new NewSampleObjectAction(shell, true);
			}
			
			if (((RemoteSampleObject)firstSelection).isContainer()){
				menu.add(ISystemContextMenuConstants.GROUP_NEW, _newSampleObjectAction);
				menu.add(ISystemContextMenuConstants.GROUP_NEW, _newSampleContainerAction);
			}
		}	
	}

	private RemoteSampleObject getSampleObject(Object element){
		if (element instanceof RemoteSampleObject){
			return (RemoteSampleObject)element;
		}
		return null;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(Object element) {
		RemoteSampleObject obj = getSampleObject(element);
		if (obj != null){
			if (obj.isContainer()){
				return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
			}
			else {
				return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
			}
		}
		return null;
	}

	@Override
	protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canRename(Object element) {
		if (element instanceof SampleRootResource){
			return false;
		}
		return true;
	}

	@Override
	public boolean doRename(Shell shell, Object element, String name,
			IProgressMonitor monitor) throws Exception {
		SampleSubSystem ss = (SampleSubSystem)getSubSystem(element);
		return ss.rename((RemoteSampleObject)element, name, monitor);
	}

	@Override
	public boolean canDelete(Object element) {
		if (element instanceof SampleRootResource){
			return false;
		}
		return true;
	}

	@Override
	public boolean doDelete(Shell shell, Object element,
			IProgressMonitor monitor) throws Exception {
		SampleSubSystem ss = (SampleSubSystem)getSubSystem(element);
		boolean result = ss.delete((RemoteSampleObject)element, monitor);	
		if (result){
			ss.list(((RemoteSampleObject)element).getParent(), monitor);
		}
		return result;
	}

	@Override
	public boolean supportsDeferredQueries(ISubSystem subSys) {
		return true;
	}

}
