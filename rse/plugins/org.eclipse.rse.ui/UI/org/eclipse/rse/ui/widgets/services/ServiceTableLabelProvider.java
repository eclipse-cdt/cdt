/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.widgets.services;



import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;




public class ServiceTableLabelProvider
	extends LabelProvider
	implements ITableLabelProvider 
	{

	public Image getColumnImage(Object element, int columnIndex) 
	{
		if (columnIndex == 0)
		{
			return getImage(element);
		}
		return null;
	}
	
	
	
	public Image getImage(Object element)
	{
		ServiceElement serviceElement = getServiceElement(element);
		return serviceElement.getImage();
	}



	public String getText(Object element)
	{
		return getColumnText(element, 0);
	}

	public ServiceElement getServiceElement(Object element)
	{
		return (ServiceElement)element;
	}


	/**
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) 
	{
		if (element instanceof ServiceElement) 
		{
			ServiceElement serviceElement = (ServiceElement)element;

			switch (columnIndex)
			{
			case 0: // name
			{
				return serviceElement.getName();			
			}
	
			default:
			{
				return "";
			}
			}
		}
		return "";
	}

	
	

}