/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

/**
 * Abstract page to be used as base for preference pages
 */
public abstract class AbstractPrefPage extends AbstractPage 
				implements IWorkbenchPreferencePage {

	public Label titleLabel;
	
	@Override
	protected Control createContents(Composite parent) {
		//	Create the container we return to the property page editor
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 1;
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout( compositeLayout );
		
		String s = getHeader();
		if (s != null) {
			Group configGroup = ControlFactory.createGroup(composite, EMPTY_STR, 1);
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.grabExcessHorizontalSpace = true;
			configGroup.setLayoutData(gd);
			titleLabel = ControlFactory.createLabel(configGroup, s); 
		}
		createWidgets(composite);
    	return composite;
	}

	@Override
	protected boolean checkElement() { return true; } 
	@Override
	public boolean isForPrefs()    { return true; }
	public void init(IWorkbench workbench) {}
	@Override
	public ICResourceDescription getResDesc() { return null; }
	@Override
	public void performApply() { performOk(); }
	@Override
	public boolean performOk() { 
		forEach(ICPropertyTab.OK, null);
		return true; 
	}
	abstract protected String getHeader(); 
}
