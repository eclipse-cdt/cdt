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

package org.eclipse.rse.ui;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The TreeViewer widget does not seem to guarantee that multiple-selections are
 *  returned sorted in order of appearance in the tree. Some action require they be,
 *  such as MoveUp and MoveDown. This class captures the information for each selected item
 *  and permits sorting via position, using the Arrays helper class in java.util.
 */
public class SystemSortableSelection implements Comparable 
{
	
    private Object selectedObject;
    private int    position;

	/**
	 * Constructor for SystemSortableSelection
	 */
	public SystemSortableSelection(Object selectedObject) 
	{
		super();
		this.selectedObject = selectedObject;
	}

    /**
     * Get the selected object
     */
    public Object getSelectedObject()
    {
    	return selectedObject;
    }
    
    /**
     * Set the position of the selected object within its parent list
     */
    public void setPosition(int position)
    {
    	this.position = position;
    }
    
    /**
     * Get the poisition of the selected object within its parent list
     */
    public int getPosition()
    {
        return position;
    }
    
	/**
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object other) 
	{
		SystemSortableSelection otherSelectableObject = (SystemSortableSelection)other;
		int otherPosition = otherSelectableObject.getPosition();
		if (position < otherPosition)
		  return -1;
		else if (position == otherPosition)
		  return 0;
		else
		  return 1;
	}

    public boolean equals(Object other)
    {
    	if (!(other instanceof SystemSortableSelection))
    	  return super.equals(other);
		SystemSortableSelection otherSelectableObject = (SystemSortableSelection)other;
		int otherPosition = otherSelectableObject.getPosition();
        return (position == otherPosition);    	
    }
    
    /**
     * Convert structured selection into an array of these objects.
     * YOU MUST CALL SET POSITION AFTER FOR EACH ITEM, BEFORE YOU CAN SORT THIS ARRAY!
     */
    public static SystemSortableSelection[] makeSortableArray(IStructuredSelection selection)
    {
    	SystemSortableSelection[] array = new SystemSortableSelection[selection.size()];
    	Iterator i = selection.iterator();
    	int idx=0;
    	while (i.hasNext())
    	  array[idx++] = new SystemSortableSelection(i.next());
    	return array;
    }
 
    /**
     * IF YOU HAVE CALLED SETPOSITION ON EACH ITEM IN THE GIVEN ARRAY, THIS WILL SORT THAT ARRAY
     * BY THE POSITION
     */   
    public static void sortArray(SystemSortableSelection[] sortableArray)
    {
    	Arrays.sort(sortableArray);
    }
    
    /**
     * IF YOU HAVE CALLED SETPOSITION ON EACH ITEM IN THE GIVEN ARRAY, AND CALLED SORTARRAY,
     * THIS CONVERTS INTO A SORTED ARRAY THAT JUST HAS THE ORIGINAL OBJECTS IN IT FROM THE SELECTION LIST.
     */
    public static Object[] getSortedObjects(SystemSortableSelection[] sortedArray, Object[] outputArray)
    {
    	//System.out.println("in SystemSortableSelection#getSortedObjects:");
    	for (int idx=0;idx<outputArray.length; idx++)
    	{
    	   outputArray[idx] = sortedArray[idx].getSelectedObject();
    	   //System.out.println("...selected Object: " + outputArray[idx]);
    	   //System.out.println("...position.......: " + sortedArray[idx].getPosition());
    	}
    	//System.out.println();
    	return outputArray;
    }

}