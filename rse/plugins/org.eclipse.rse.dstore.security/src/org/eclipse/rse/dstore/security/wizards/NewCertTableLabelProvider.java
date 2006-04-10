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

package org.eclipse.rse.dstore.security.wizards;



import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.rse.dstore.security.preference.Element;
import org.eclipse.rse.dstore.security.preference.X509CertificateElement;
import org.eclipse.swt.graphics.Image;



public class NewCertTableLabelProvider
	extends LabelProvider
	implements ITableLabelProvider 
	{

	public Image getColumnImage(Object element, int columnIndex) 
	{
		if (columnIndex == 0)
		{
			if (element instanceof Element)
			{
				return ((Element)element).getImage();		
			}
		}
		return null;
	}
	/**
	 * @see ITableLabelProvider#getColumnText(Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) 
	{
		if (element instanceof Element) 
		{
			X509CertificateElement myTableElement = (X509CertificateElement) element;

			switch (columnIndex)
			{		
			case 0: // issued to
			{
				String name = myTableElement.getSubjectName();
				if (name == null || name.length() == 0)
				{
					name = myTableElement.getSubjectUnit();
					if (name == null || name.length() == 0)
					{
						name = myTableElement.getSubjectOrg();
					}
				}
				return name;
			}
			case 1: // issuer
			{
				String name = myTableElement.getIssuerName();
				if (name == null || name.length() == 0)
				{
					name = myTableElement.getIssuerUnit();
					if (name == null || name.length() == 0)
					{
						name = myTableElement.getIssuerOrg();
					}
				}

				return name;
			}
			case 2: // expires
				return myTableElement.getNotAfter();
		
			default:
				break;
			}
		}
		return "";
	}

	
	

}