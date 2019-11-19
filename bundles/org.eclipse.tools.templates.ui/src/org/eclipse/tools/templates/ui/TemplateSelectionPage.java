/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
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

public class TemplateSelectionPage extends WizardPage {

	private final String[] requestedTags;
	private final List<Template> templates;

	private TagListViewer tagList;
	private TemplateTable templateTable;

	public TemplateSelectionPage(String pageName, String... tags) {
		super(pageName);
		this.requestedTags = tags;
		TemplateExtension templateExtension = Activator.getTemplateExtension();
		templates = new ArrayList<>();
		for (Template template : templateExtension.getTemplates()) {
			for (String requestedTag : requestedTags) {
				if (template.hasTag(requestedTag)) {
					templates.add(template);
					break;
				}
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		setControl(form);

		tagList = new TagListViewer(form, SWT.BORDER);
		tagList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Collection<Tag> selectedTags = tagList.getSelectedTags();
				List<Template> selectedTemplates = new ArrayList<>();
				for (Template template : templates) {
					for (Tag tag : selectedTags) {
						if (template.hasTag(tag.getId())) {
							selectedTemplates.add(template);
							break;
						}
					}
				}

				Template selected = templateTable.getSelectedTemplate();
				templateTable.setTemplates(selectedTemplates);
				templateTable.selectTemplate(selected);

				updateButtons();
			}
		});

		templateTable = new TemplateTable(form, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		templateTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				getContainer().showPage(getNextPage());
			}
		});

		Set<Tag> tags = new HashSet<>();
		for (Template template : templates) {
			tags.addAll(template.getTags());
		}

		if (requestedTags.length == 1) {
			// Implies that the requested tag is actually the same as All.
			// We can safely remove it.
			for (Tag tag : tags) {
				if (tag.getId().equals(requestedTags[0])) {
					tags.remove(tag);
					break;
				}
			}
		}

		templateTable.setTemplates(templates);
		tagList.setInput(tags);
		tagList.getList().select(0); // All

		form.setWeights(new int[] { 20, 80 });
	}

	public void selectTemplate(String id) {
		if (templateTable != null) {
			for (Template template : templates) {
				if (template.getId().equals(id)) {
					templateTable.selectTemplate(template);
					updateButtons();
					break;
				}
			}
		}
	}

	private void updateButtons() {
		setPageComplete(templateTable.getSelectedTemplate() != null);
		getContainer().updateButtons();
	}

	@Override
	public IWizardPage getNextPage() {
		Template template = templateTable.getSelectedTemplate();
		if (template != null) {
			try {
				NewWizard oldWizard = (NewWizard) getWizard();
				TemplateWizard nextWizard = (TemplateWizard) template.getWizard();
				oldWizard.initialize(nextWizard);
				nextWizard.addPages();
				return nextWizard.getPages()[0];
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return super.getNextPage();
	}

}
