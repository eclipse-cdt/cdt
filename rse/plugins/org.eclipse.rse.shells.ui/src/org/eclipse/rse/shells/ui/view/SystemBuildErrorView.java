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

package org.eclipse.rse.shells.ui.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteError;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


public class SystemBuildErrorView extends SystemTableView 
{
	
	private class ErrorViewerFilter extends ViewerFilter 
	{
		public boolean select(Viewer viewer, Object obj1, Object obj2)
		{	
			if (obj2 instanceof IRemoteError)
			{
				return true;												
			}
			return false;
		}
	}


	private Color _errColor;
	private Color _outColor;
	private Color _infColor;
	private Color _warColor;
	private Color _prmColor;


	
	public SystemBuildErrorView(Table table, ISystemMessageLine msgLine)
	{
		super(table, msgLine);

		Display display = getControl().getDisplay();

		_errColor = new Color(display, 255, 0, 0);
		_outColor = new Color(display, 50, 50, 50);
		_infColor = new Color(display, 0, 0, 255);
		_warColor = new Color(display, 200, 150, 0);
		_prmColor = new Color(display, 0, 50, 0);
		
		_provider = new SystemBuildErrorViewProvider();
		setContentProvider(_provider);
		addFilter(new ErrorViewerFilter());
	}

	public void refresh()
	{
		super.refresh();

		Table table = getTable();
		if (table != null && table.getItemCount() > 0)
		{
			TableItem lastItem = table.getItem(table.getItemCount() - 1);
			table.showItem(lastItem);
		}
	}

	public IPropertyDescriptor[] getVisibleDescriptors(Object object)
	{
		SystemViewRemoteErrorAdapter adpt = new SystemViewRemoteErrorAdapter();
		return adpt.getUniquePropertyDescriptors();
	}
	
	public IPropertyDescriptor getNameDescriptor(Object object)
	{
		SystemViewRemoteErrorAdapter adpt = new SystemViewRemoteErrorAdapter();
		return adpt.getPropertyDescriptors()[0];
	}
	
	public void setOffset(int offset)
	{
		((SystemBuildErrorViewProvider)_provider).setOffset(offset);
	}
		
	public synchronized void updateChildren()
	{
		computeLayout();
		internalRefresh(getInput());
		
	}	


	public void clearAllItems()
	{
		Object input = getInput();
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
		Object[] children = provider.getChildren(input);

		((SystemBuildErrorViewProvider)_provider).moveOffsetToEnd();
		clearFirstItems(children, children.length - 1);
		provider.flushCache();
		
	}

	private void clearFirstItems(Object[] children, int items)
	{
		Table table = getTable();

		table.setRedraw(false);
		synchronized (table)
		{
			int count = table.getItemCount();
			table.remove(0, items);
		
		}
		table.setRedraw(true);
	}

	protected Item newItem(Widget parent, int flags, int ix)
	{
		if (parent instanceof Table)
		{
			return new TableItem((Table) parent, flags);
		}

		return null;
	}

	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		Object child = event.getSource();

		if (event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH)
		{
			if (child == getInput())
			{
				SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
				if (provider != null)
				{
					provider.flushCache();

					updateChildren();
					return;
				}
			}
		}
		
		//super.systemResourceChanged(event);
	}
	
	protected Object getParentForContent(Object element)
	{
		return getAdapter(element).getParent(element);		
	}

}