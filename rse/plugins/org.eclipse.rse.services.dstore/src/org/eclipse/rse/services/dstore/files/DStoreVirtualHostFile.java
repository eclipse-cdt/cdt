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

package org.eclipse.rse.services.dstore.files;

import org.eclipse.dstore.core.model.DataElement;

public class DStoreVirtualHostFile extends DStoreHostFile
{

	public static final int ATTRIBUTE_COMMENT = 6;
	public static final int ATTRIBUTE_COMPRESSEDSIZE = 7;	
	public static final int ATTRIBUTE_COMPRESSIONMETHOD = 8;
	public static final int ATTRIBUTE_COMPRESSIONRATIO = 9;
	public static final int ATTRIBUTE_EXPANDEDSIZE = 10;	
	
	public DStoreVirtualHostFile(DataElement element)
	{
		super(element);
	}
	
	protected static long getCompressedSize(String attributes) 
	{
		String str = getAttribute(attributes, ATTRIBUTE_COMPRESSEDSIZE);
		return Long.parseLong(str);
	}
	
	protected static long getExpandedSize(String attributes) 
	{
		String str = getAttribute(attributes, ATTRIBUTE_EXPANDEDSIZE);
		return Long.parseLong(str);
	}

	protected static double getCompressionRatio(String attributes) 
	{
		String str = getAttribute(attributes, ATTRIBUTE_COMPRESSIONRATIO);
		return Double.parseDouble(str);
	}
	
	public String getComment()
	{
		return getAttribute(_element.getSource(), ATTRIBUTE_COMMENT);
	}

	public long getCompressedSize() 
	{
		return getCompressedSize(_element.getSource());
	}

	public String getCompressionMethod() 
	{
		return getAttribute(_element.getSource(), ATTRIBUTE_COMPRESSIONMETHOD);
	}

	public double getCompressionRatio() 
	{
		return getCompressionRatio(_element.getSource());
	}

	public long getExpandedSize() 
	{
		return getExpandedSize(_element.getSource());
	}

}