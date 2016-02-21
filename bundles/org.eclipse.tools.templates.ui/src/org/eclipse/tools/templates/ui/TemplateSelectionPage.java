/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tools.templates.ui.internal.Activator;
import org.eclipse.tools.templates.ui.internal.Tag;
import org.eclipse.tools.templates.ui.internal.TagListViewer;
import org.eclipse.tools.templates.ui.internal.Template;
import org.eclipse.tools.templates.ui.internal.TemplateExtension;
import org.eclipse.tools.templates.ui.internal.TemplateTable;
import org.eclipse.ui.IWorkbenchWizard;

public class TemplateSelectionPage extends WizardPage {

	private final String[] requestedTags;

	private ListViewer tagList;
	private TemplateTable templateTable;

	public TemplateSelectionPage(String pageName, String... tags) {
		super(pageName);
		this.requestedTags = tags;
	}

	@Override
	public void createControl(Composite parent) {
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		setControl(form);

		tagList = new TagListViewer(form, SWT.BORDER);
		tagList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});

		templateTable = new TemplateTable(form, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		templateTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(templateTable.getSelectedTemplate() != null);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				getContainer().showPage(getNextPage());
			}
		});

		TemplateExtension templateExtension = Activator.getTemplateExtension();
		List<Template> templates = new ArrayList<>();
		for (Template template : templateExtension.getTemplates()) {
			for (String requestedTag : requestedTags) {
				if (template.hasTag(requestedTag)) {
					templates.add(template);
					break;
				}
			}
		}

		Set<Tag> tags = new HashSet<>();
		for (Template template : templates) {
			tags.addAll(template.getTags());
		}

		templateTable.setTemplates(templates);
		tagList.setInput(tags);
		tagList.getList().select(0);

		form.setWeights(new int[] { 20, 80 });
	}

	@Override
	public IWizardPage getNextPage() {
		Template template = templateTable.getSelectedTemplate();
		if (template != null) {
			try {
				IWorkbenchWizard nextWizard = template.getWizard();
				nextWizard.addPages();
				return nextWizard.getPages()[0];
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return super.getNextPage();
	}

}
