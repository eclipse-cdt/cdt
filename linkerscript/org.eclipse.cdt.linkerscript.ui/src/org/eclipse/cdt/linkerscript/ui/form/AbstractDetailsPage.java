/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public abstract class AbstractDetailsPage extends AbstractFormPart implements IDetailsPage {

	private ILinkerScriptModel model;
	private String inputURI;
	private LinkerCommandDetails details;
	private ILinkerScriptModelListener modelListener = this::asyncUpdate;
	private Display display;
	private String title;
	private String description;
	private int displayContainerCount;

	public AbstractDetailsPage(ILinkerScriptModel model, String title, String description, int displayContainerCount) {
		this.model = model;
		this.title = title;
		this.description = description;
		this.displayContainerCount = displayContainerCount;
	}

	@Override
	public void dispose() {
		model.removeModelListener(modelListener);
		super.dispose();
	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			inputURI = (String) ssel.getFirstElement();
		} else {
			inputURI = null;
		}

		update();
	}

	private void asyncUpdate() {
		if (!display.isDisposed()) {
			display.asyncExec(this::update);
		}
	}

	private void update() {
		if (inputURI == null || details.getControl().isDisposed()) {
			return;
		}

		doUpdate();

		details.setInput(inputURI);
	}

	protected ILinkerScriptModel getModel() {
		return model;
	}

	protected String getInputURI() {
		return inputURI;
	}

	protected abstract void doUpdate();

	protected abstract void doCreateContents(Composite composite);

	@Override
	public void createContents(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		FormToolkit toolkit = getManagedForm().getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		section.setText(title);
		section.setDescription(description);

		Composite composite = toolkit.createComposite(section);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		section.setClient(composite);

		doCreateContents(composite);

		details = new LinkerCommandDetails(composite, toolkit, model, displayContainerCount);
		display = parent.getDisplay();
		model.addModelListener(modelListener);
		update();
	}

}
