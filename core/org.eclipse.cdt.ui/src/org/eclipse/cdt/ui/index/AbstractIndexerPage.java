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

import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.core.resources.IProject;

/**
 * @author Bogdan Gheorghe
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {

   
   protected AbstractIndexerPage() {
   	  super();
   }
   
  /**
   * Called by BaseIndexerBlock to give the indexer page a chance to load its state from store
   * @param currentProject - the project that this page is being created for  
   */
   abstract public void initialize(IProject currentProject);

}
