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
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexerBlock extends AbstractIndexerPage {
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.index.AbstractIndexerPage#initialize(org.eclipse.core.resources.IProject)
     */
    public void initialize(IProject project) {
		this.currentProject = project;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void performApply(IProgressMonitor monitor) throws CoreException {
    	
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
     */
    public void performDefaults() {
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite page = ControlFactory.createComposite(parent, 1);	
		setControl(page);
    }
    
   

}
