/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.cdt.linkerscript.linkerScript.InputSection;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSectionTypeNoLoad;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class OutputSectionDetailsPage extends AbstractFormPart implements IDetailsPage {

	private ILinkerScriptModel model;
	private String inputURI;
	private Text nameText;
	private Button keepCheck;
	private ILinkerScriptModelListener modelListener = this::asyncUpdate;
	private Display display;
	private Button noLoadCheck;
	private AddressText vmaExpressionText;
	private AddressText lmaExpressionText;
	private MemoryRegionText vmaRegionText;
	private MemoryRegionText lmaRegionText;
	private LinkerCommandDetails details;

	public OutputSectionDetailsPage(ILinkerScriptModel model) {
		this.model = model;

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

	private Stream<InputSection> getInputSections(OutputSection outputSection) {
		return outputSection.getStatements().stream().filter(t -> t instanceof StatementInputSection)
				.map(t -> ((StatementInputSection) t).getSpec());
	}

	private void update() {
		if (inputURI == null || nameText.isDisposed()) {
			return;
		}


		nameText.setText(model.readModel(inputURI, OutputSection.class, "", outputSection -> {
			return outputSection.getName();
		}));

		List<Boolean> keeps = model.readModel(inputURI, OutputSection.class, Collections.emptyList(), outputSection -> {
			return getInputSections(outputSection).map(InputSection::isKeep).collect(Collectors.toList());
		});

		boolean anyKeep = keeps.stream().anyMatch(t -> t);
		boolean anyNotKeep = keeps.stream().anyMatch(t -> !t);

		if (anyKeep && anyNotKeep) {
			keepCheck.setGrayed(true);
			keepCheck.setSelection(true);
		} else if (!anyKeep) {
			keepCheck.setGrayed(false);
			keepCheck.setSelection(false);
		} else {
			keepCheck.setGrayed(false);
			keepCheck.setSelection(true);
		}

		boolean noLoad = model.readModel(inputURI, OutputSection.class, false, outputSection -> {
			return outputSection.getType() instanceof OutputSectionTypeNoLoad;
		});
		noLoadCheck.setSelection(noLoad);

		vmaExpressionText.setInput(inputURI);
		vmaRegionText.setInput(inputURI);
		lmaExpressionText.setInput(inputURI);
		lmaRegionText.setInput(inputURI);
		details.setInput(inputURI);

	}

	@Override
	public void createContents(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		FormToolkit toolkit = getManagedForm().getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		section.setText("Output Section Details");
		section.setDescription("Set the properties of the output section.");
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		Composite composite = toolkit.createComposite(section);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		section.setClient(composite);

		Label nameLabel = toolkit.createLabel(composite, "Output Section Name:");
		nameLabel.setForeground(foreground);
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());

		nameText = toolkit.createText(composite, "Initial Name", SWT.BORDER);
		nameText.setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		nameText.addModifyListener(e -> model.writeModel(inputURI, OutputSection.class, sec -> {
			String newName = nameText.getText();
			if (!newName.equals(sec.getName())) {
				sec.setName(newName);
			}
		}));

		vmaExpressionText = new AddressText(composite, toolkit, model,
				LinkerScriptPackage.Literals.OUTPUT_SECTION__ADDRESS, "Virtual Memory Address (VMA)");

		vmaRegionText = new MemoryRegionText(composite, toolkit, model,
				LinkerScriptPackage.Literals.OUTPUT_SECTION__MEMORY, "Virtual Memory Region");

		lmaExpressionText = new AddressText(composite, toolkit, model, LinkerScriptPackage.Literals.OUTPUT_SECTION__AT,
				"Load Memory Address (LMA)");

		lmaRegionText = new MemoryRegionText(composite, toolkit, model,
				LinkerScriptPackage.Literals.OUTPUT_SECTION__AT_MEMORY, "Load Memory Region");

		keepCheck = toolkit.createButton(composite, "KEEP (Group will not be omitted even if unreferenced)", SWT.CHECK);
		keepCheck.setForeground(foreground);
		keepCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		keepCheck.addListener(SWT.Selection, e -> {
			/* any change turns off the gray state */
			keepCheck.setGrayed(false);
		});
		keepCheck.addListener(SWT.Selection, e -> {
			model.writeModel(inputURI, OutputSection.class, outputSection -> {
				boolean newKeep = keepCheck.getSelection();
				Stream<InputSection> inputSections = getInputSections(outputSection);
				inputSections.forEach(inputSection -> {
					if (inputSection.isKeep() != newKeep) {
						inputSection.setKeep(newKeep);
					}
				});
			});
		});

		noLoadCheck = toolkit.createButton(composite, "Mark as \"NOLOAD\" (Group will not be loaded at runtime)",
				SWT.CHECK);
		noLoadCheck.setForeground(foreground);
		noLoadCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		noLoadCheck.addListener(SWT.Selection, e -> {
			model.writeModel(inputURI, OutputSection.class, outputSection -> {
				boolean newNoLoad = noLoadCheck.getSelection();
				outputSection.setType(newNoLoad ? LinkerScriptFactory.eINSTANCE.createOutputSectionTypeNoLoad() : null);
			});
		});

		details = new LinkerCommandDetails(composite, toolkit, model, 0);

		display = parent.getDisplay();
		model.addModelListener(modelListener);
		update();
	}

}
