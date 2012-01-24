/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;

/**
 * @author Dad
 * @since 5.4
 */
public class TemplateSelectionPage extends WizardPage {

	public TemplateSelectionPage() {
		super("templateSelection"); //$NON-NLS-1$
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		
		List templateList = new List(comp, SWT.BORDER);
		templateList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Template[] templates = TemplateEngineUI.getDefault().getTemplates();
		for (Template template : templates) {
			templateList.add(template.getLabel());
		}
		
		setControl(comp);
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}
	
}
