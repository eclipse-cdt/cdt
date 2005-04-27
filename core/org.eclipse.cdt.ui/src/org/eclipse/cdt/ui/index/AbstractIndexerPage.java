/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.index;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Bogdan Gheorghe
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {

   protected IProject currentProject;
   protected IPreferenceStore prefStore=CUIPlugin.getDefault().getPreferenceStore();
   
   protected AbstractIndexerPage() {
   	  super();
   }
   
  /**
   * Called by BaseIndexerBlock to give the indexer page a chance to load its state from store
   * @param currentProject - the project that this page is being created for  
   */
   abstract public void initialize(IProject currentProject);
   /**
    * Called by the indexer block to give the indexer page an opportunity to
    * load any preferecnes previously set
    */
   abstract public void loadPreferences();
   /**
    * Called on indexer preference changes to allow former indexer pages
    * to clean up the preferences store
    */
   abstract public void removePreferences();
   
	public IProject getCurrentProject() {
		return currentProject;
	}
	public void setCurrentProject(IProject currentProject) {
		this.currentProject = currentProject;
	}
}
