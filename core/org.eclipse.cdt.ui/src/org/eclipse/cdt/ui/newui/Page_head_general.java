/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.DocCommentOwnerBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Page_head_general extends PropertyPage implements ICOptionContainer {
	private DocCommentOwnerBlock fDocBlock;
	private boolean isProjectLevel;

	@Override
	protected Control createContents(Composite parent) {
		isProjectLevel= getProject() != null;
		if(isProjectLevel) {
			fDocBlock= new DocCommentOwnerBlock();
			fDocBlock.setContainer(this);
			fDocBlock.createControl(parent);
		}
		noDefaultAndApplyButton();
		return parent;
	}

	@Override
	protected void performDefaults() {
		if(isProjectLevel) {
			fDocBlock.performDefaults();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if(isProjectLevel) {
			try {
				fDocBlock.performApply(new NullProgressMonitor());
			} catch(CoreException ce) {
				CUIPlugin.log(ce);
			}
		}
		return true;
	}

	@Override
	public IProject getProject(){
		IProject project= null;
		IAdaptable elem = getElement();
		if (elem instanceof IProject) {
			project= (IProject) elem;
		} else if (elem != null) {
			project= (IProject) elem.getAdapter(IProject.class);
		}
		return project;
	}

	@Override
	public Preferences getPreferences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateContainer() {
	}
}
