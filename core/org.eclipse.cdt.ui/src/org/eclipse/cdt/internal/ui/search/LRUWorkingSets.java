/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
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
	
	ArrayList<IWorkingSet[]> workingSetsCache = null;
	int size=0;
	
	public LRUWorkingSets(int size){
		workingSetsCache = new ArrayList<IWorkingSet[]>(size);
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
	
	private IWorkingSet[] find(ArrayList<IWorkingSet[]> list, IWorkingSet[] workingSet) {
		Set<IWorkingSet> workingSetList= new HashSet<IWorkingSet>(Arrays.asList(workingSet));
		Iterator<IWorkingSet[]> iter= list.iterator();
		while (iter.hasNext()) {
			IWorkingSet[] lruWorkingSets= iter.next();
			Set<IWorkingSet> lruWorkingSetList= new HashSet<IWorkingSet>(Arrays.asList(lruWorkingSets));
			if (lruWorkingSetList.equals(workingSetList))
				return lruWorkingSets;
		}
		return null;
	}

	private void cleanUpCache(){
     //Remove any previously deleted entries
	 Iterator<IWorkingSet[]> iter = workingSetsCache.iterator();
	 while (iter.hasNext()){
	 	IWorkingSet[] workingSet = iter.next();
	 	for (int i= 0; i < workingSet.length; i++) {
			if (PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSet[i].getName()) == null) {
				workingSetsCache.remove(workingSet);
				break;
			}
		}
	 }
	}

	public Iterator<IWorkingSet[]> iterator() {
		return workingSetsCache.iterator(); 
	}
}
