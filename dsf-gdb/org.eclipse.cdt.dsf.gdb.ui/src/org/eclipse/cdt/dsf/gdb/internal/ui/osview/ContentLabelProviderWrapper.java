/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.osview;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/** Helper class to change table and label provider of a TreeViewer in an atomic fashion.
 *
 *  Suppose we want to change both content and label provider of existing TreeViewer. Right now,
 *  if we set either, TreeViewer will try a refresh, using one new provider and one old. This
 *  is obviously nonsensical -- for example if we set set new content provider, then old label provider
 *  will be asked to provide labels for elements it has no idea what to do with, or for columns beyond
 *  its range, etc.
 *
 *  This class is wrapping our real content provider, and can be retargeted in one call -- after which
 *  refresh of TreeViewer sees consistent data.
 *  
 * @since 2.4
 * */
public class ContentLabelProviderWrapper<U extends ITableLabelProvider & IStructuredContentProvider>
implements ITableLabelProvider, IStructuredContentProvider
{

	public ContentLabelProviderWrapper(U realProvider)
	{
		this.realProvider = realProvider;
	}

	public void setData(U realProvider)
	{
		this.realProvider = realProvider;
	}

	@Override
	public Image getColumnImage(Object obj, int index) {
		return realProvider.getColumnImage(obj, index);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		realProvider.addListener(listener);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return realProvider.isLabelProperty(element, property);
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		realProvider.removeListener(listener);
	}

	@Override
	public String getColumnText(Object obj, int index) {
		return realProvider.getColumnText(obj, index);
	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		realProvider.inputChanged(v, oldInput, newInput);
	}

	@Override
	public void dispose() {
		realProvider.dispose();
	}

	@Override
	public Object[] getElements(Object parent) {
		return realProvider.getElements(parent);
	}

	private U realProvider;
}
