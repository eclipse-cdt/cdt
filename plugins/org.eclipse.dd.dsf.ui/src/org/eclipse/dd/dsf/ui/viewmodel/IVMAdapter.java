package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactoryAdapter;

/**
 * The View Model adapter handles the layout of a given data model within a 
 * set of viewers.  This adapter should be returned by an adapter factory for 
 * the input object of the viewer, and this adapter implementation will then 
 * populate the view contents.  
 */
@ThreadSafe
@SuppressWarnings("restriction")
public interface IVMAdapter
    extends IElementContentProvider, IModelProxyFactoryAdapter, IColumnPresentationFactoryAdapter 
{
}
