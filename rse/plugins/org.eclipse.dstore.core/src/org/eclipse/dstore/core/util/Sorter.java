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

package org.eclipse.dstore.core.util;

import java.util.ArrayList;

import org.eclipse.dstore.core.model.DataElement;

/**
 * Utility class used for sorting a list of DataElements based on 
 * their depth attributes.
 */
public class Sorter
{

	/**
	 * Sort a list of DataElements based on their depth attributes
	 * @param list a list of DataElements
	 * @return a sorted list of DataElements
	 */
	public static ArrayList sort(ArrayList list)
	{
		ArrayList sortedList = new ArrayList(list.size());
		while (list.size() > 0)
		{
			DataElement first = findFirst(list);
			sortedList.add(first);
		}

		return sortedList;
	}

	/**
	 * Find the DataElement with the highest depth in the list
	 * @param list a list of DataElements
	 * @return the DataElement with the highest depth
	 */
	private static DataElement findFirst(ArrayList list)
	{
		DataElement result = null;
		for (int i = 0; i < list.size(); i++)
		{
			DataElement item = (DataElement) list.get(i);
			if (item != null)
			{
				int depth = item.depth();
				if ((result == null) || (depth > result.depth()))
				{
					result = item;
				}
			}
		}

		list.remove(result);
		return result;
	}
}