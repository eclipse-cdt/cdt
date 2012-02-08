/*******************************************************************************
 * Copyright (c) 2012 Doug Schaefer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.wizards.ProjectTypePage;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

/**
 * @since 8.1
 * 
 */
public class ToolChainSelectionPage extends WizardPage implements ProjectTypePage {

	private IWizardPage nextPage;
	private String[] toolChainIds;
	private String selectedToolChainId;
	private List toolChainList;
	
	public ToolChainSelectionPage() {
		super("ToolChainSelectionPage"); //$NON-NLS-1$
		setTitle(Messages.ToolChainSelectionPage_Title);
		setDescription(Messages.ToolChainSelectionPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		
		toolChainList = new List(comp, SWT.BORDER | SWT.SINGLE);
		toolChainList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		for (String toolChainId : toolChainIds) {
			IToolChain toolChain = ManagedBuildManager.getExtensionToolChain(toolChainId);
			if (toolChain != null)
				toolChainList.add(toolChain.getName());
		}
		
		toolChainList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (toolChainList.getSelectionCount() == 0)
					selectedToolChainId = null;
				else
					selectedToolChainId = toolChainIds[toolChainList.getSelectionIndex()];
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		setControl(comp);
	}

	@Override
	public boolean init(Template template, IWizard wizard, IWizardPage nextPage) {
		this.nextPage = nextPage;
		setWizard(wizard);
		toolChainIds = template.getTemplateInfo().getToolChainIds();
		
		// only need this page if there are multiple toolChains to select from.
		return toolChainIds != null && toolChainIds.length > 1;
	}

	@Override
	public IWizardPage getNextPage() {
		if (nextPage != null)
			return nextPage;
		return super.getNextPage();
	}

	@Override
	public boolean isPageComplete() {
		return selectedToolChainId != null;
	}
	
}
