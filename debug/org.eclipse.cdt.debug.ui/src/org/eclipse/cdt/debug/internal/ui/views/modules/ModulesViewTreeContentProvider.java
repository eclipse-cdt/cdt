/**
 * Copyright 2007 ARM Limited. All rights reserved.
 *
 *    $ Rev: $
 * $ Author: $
 *   $ Date: $
 *    $ URL: $
 */
package org.eclipse.cdt.debug.internal.ui.views.modules;

import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.ui.elements.adapters.CDebugElementAdapterFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.TreeModelContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;

/**
 * org.eclipse.cdt.debug.internal.ui.views.modules.ModulesViewTreeContentProvider: 
 * //TODO Add description.
 */
public class ModulesViewTreeContentProvider extends TreeModelContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#getContentAdapter(java.lang.Object)
	 */
	protected IElementContentProvider getContentAdapter( Object element ) {
		IElementContentProvider adapter = null;
		if ( !(element instanceof ICDebugElement) ) {
			if ( element instanceof IElementContentProvider ) {
				adapter = (IElementContentProvider)element;
			}
			else if ( element instanceof IAdaptable ) {
				IAdaptable adaptable = (IAdaptable)element;
				adapter = (IElementContentProvider)adaptable.getAdapter( IElementContentProvider.class );
			}
		}
		else {
			IModuleRetrieval moduleRetrieval = (IModuleRetrieval)((ICDebugElement)element).getAdapter( IModuleRetrieval.class );
			if ( moduleRetrieval != null ) {
				adapter = (IElementContentProvider)new CDebugElementAdapterFactory().getAdapter( moduleRetrieval, IElementContentProvider.class );
			}
		}
		return adapter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#getModelProxyFactoryAdapter(java.lang.Object)
	 */
	protected IModelProxyFactory getModelProxyFactoryAdapter( Object element ) {
		IModelProxyFactory adapter = null;
		if ( !(element instanceof ICDebugElement) ) {
			if ( element instanceof IModelProxyFactory ) {
				adapter = (IModelProxyFactory)element;
			}
			else if ( element instanceof IAdaptable ) {
				IAdaptable adaptable = (IAdaptable)element;
				adapter = (IModelProxyFactory)adaptable.getAdapter( IModelProxyFactory.class );
			}
		}
		else {
			IModuleRetrieval moduleRetrieval = (IModuleRetrieval)((ICDebugElement)element).getAdapter( IModuleRetrieval.class );
			if ( moduleRetrieval != null ) {
				adapter = (IModelProxyFactory)new CDebugElementAdapterFactory().getAdapter( moduleRetrieval, IModelProxyFactory.class );
			}
		}
		return adapter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#getViewerStateAdapter(java.lang.Object)
	 */
	protected IElementMementoProvider getViewerStateAdapter( Object element ) {
		IElementMementoProvider adapter = null;
		if ( !(element instanceof ICDebugElement) ) {
			adapter = super.getViewerStateAdapter( element );
		}
		else {
			IModuleRetrieval moduleRetrieval = (IModuleRetrieval)((ICDebugElement)element).getAdapter( IModuleRetrieval.class );
			if ( moduleRetrieval != null ) {
				adapter = (IElementMementoProvider)new CDebugElementAdapterFactory().getAdapter( moduleRetrieval, IElementMementoProvider.class );
			}
		}
		return adapter;
	}
}
