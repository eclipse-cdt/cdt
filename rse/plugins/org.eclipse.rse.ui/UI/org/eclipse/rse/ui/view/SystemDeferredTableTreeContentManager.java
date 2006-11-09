/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed the initial implementation:
 * David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class SystemDeferredTableTreeContentManager extends
		DeferredTreeContentManager {

	private SystemTableTreeViewProvider _provider;
	private SystemTableTreeView _view;
	public SystemDeferredTableTreeContentManager(SystemTableTreeViewProvider provider, SystemTableTreeView viewer) {
		super(provider, viewer);
		_provider = provider;
		_view = viewer;
	}


	protected void addChildren(Object parent, Object[] children, IProgressMonitor monitor) {
		super.addChildren(parent, children, monitor);
		
		_provider.setCachedObjects(parent, children);
		IPropertyDescriptor[] descriptors = _view.getUniqueDescriptors();
		if (descriptors == null)
		{
			Display.getDefault().asyncExec(new RelayoutView(parent));
		}
	}
	
	
	public class RelayoutView implements Runnable
	{
		public RelayoutView(Object parent)
		{
		}
		
		public void run()
		{
			_view.computeLayout();
			_view.refresh(true);
			//SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			//registry.fireEvent(new SystemResourceChangeEvent(_parent, ISystemResourceChangeEvents.EVENT_PROPERTYSHEET_UPDATE, _parent));
		}
	}

	
}
