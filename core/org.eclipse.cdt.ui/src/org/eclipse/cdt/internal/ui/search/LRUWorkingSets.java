/*
 * Created on Aug 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;



/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LRUWorkingSets {
	
	ArrayList workingSetsCache = null;
	int size=0;
	
	public LRUWorkingSets(int size){
		workingSetsCache = new ArrayList(size);
		this.size = size;
	}
	
	public void add(IWorkingSet[] workingSet){
		cleanUpCache();
		//See if this working set has been previously added to the 
		IWorkingSet[] existingWorkingSets= find(workingSetsCache, workingSet);
		if (existingWorkingSets != null)
			workingSetsCache.remove(existingWorkingSets);
		else if (workingSetsCache.size() == size)
			workingSetsCache.remove(size - 1);
		workingSetsCache.add(0, workingSet);
	}
	
	/**
	 * @param workingSetsCache2
	 * @param workingSet
	 * @return
	 */
	private IWorkingSet[] find(ArrayList list, IWorkingSet[] workingSet) {
		Set workingSetList= new HashSet(Arrays.asList(workingSet));
		Iterator iter= list.iterator();
		while (iter.hasNext()) {
			IWorkingSet[] lruWorkingSets= (IWorkingSet[])iter.next();
			Set lruWorkingSetList= new HashSet(Arrays.asList(lruWorkingSets));
			if (lruWorkingSetList.equals(workingSetList))
				return lruWorkingSets;
		}
		return null;
	}

	private void cleanUpCache(){
     //Remove any previously deleted entries
	 Iterator iter = workingSetsCache.iterator();
	 while (iter.hasNext()){
	 	IWorkingSet[] workingSet = (IWorkingSet []) iter.next();
	 	for (int i= 0; i < workingSet.length; i++) {
			if (PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSet[i].getName()) == null) {
				workingSetsCache.remove(workingSet);
				break;
			}
		}
	 }
	}

	/**
	 * @return
	 */
	public Iterator iterator() {
		return workingSetsCache.iterator(); 
	}
}
