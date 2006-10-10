package org.eclipse.rse.ui.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.DeferredTreeContentManager;

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
		Display.getDefault().asyncExec(new RelayoutView(parent));
	}
	
	
	public class RelayoutView implements Runnable
	{
		private Object _parent;
		public RelayoutView(Object parent)
		{
			_parent = parent;
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
