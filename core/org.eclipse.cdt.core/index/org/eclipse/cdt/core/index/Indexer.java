/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Indexer {
	private static Indexer indexer = null;
	private static IndexManager manager= CCorePlugin.getDefault().getCoreModel().getIndexManager();
	
	public static boolean indexEnabledOnAllProjects(){
		IProject[] projects= CCorePlugin.getWorkspace().getRoot().getProjects();
		boolean allEnabled = true;
		for (int i=0; i<projects.length; i++){
		  if (!indexEnabledOnProject(projects[i])){
		  	allEnabled=false;
		  	break;
		  }
		}
		
		return allEnabled;
	}
	
	public static boolean indexEnabledOnAnyProjects(){
		IProject[] projects= CCorePlugin.getWorkspace().getRoot().getProjects();
		boolean allEnabled = false;
		for (int i=0; i<projects.length; i++){
		  if (!projects[i].isOpen())
		  	continue;
		  
		  if (indexEnabledOnProject(projects[i])){
		  	allEnabled=true;
		  	break;
		  }
		}
		
		return allEnabled;
	}
	
	public static boolean indexEnabledOnProject(IProject project){
		boolean allEnabled = true;
		
		//TODO: BOG make this generic
		/*try {
			Boolean indexValue = (Boolean) project.getSessionProperty(IndexManager.activationKey);
			if (indexValue != null){
				if(!indexValue.booleanValue()){
					allEnabled = false;
				}
			}
			else {
				if (!manager.isIndexEnabled(project)){
					allEnabled=false;
				}
			}
			
		} catch (CoreException e) {}*/
		
		return allEnabled;
	}
}
