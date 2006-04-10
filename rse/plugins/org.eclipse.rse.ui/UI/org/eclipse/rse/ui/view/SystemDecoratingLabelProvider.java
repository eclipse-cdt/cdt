/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SystemDecoratingLabelProvider extends DecoratingLabelProvider implements ITableLabelProvider
{
	private ITableLabelProvider _tableLabelProvider;

	public SystemDecoratingLabelProvider(ITableLabelProvider provider, ILabelDecorator decorator) {
		super((ILabelProvider)provider, decorator);
		// TODO Auto-generated constructor stub
		_tableLabelProvider = provider;
	}
	

	public Image getColumnImage(Object element, int columnIndex)
	{
		if (columnIndex == 0) //TODO: Make this more generic
		{
			return getImage(element);
		}
		return _tableLabelProvider.getColumnImage(element, columnIndex);
	}

	public String getColumnText(Object element, int columnIndex)
	{
		if (columnIndex == 0) //TODO: Make this more generic
		{
			return getText(element);
		}
		return _tableLabelProvider.getColumnText(element, columnIndex);
	}
}