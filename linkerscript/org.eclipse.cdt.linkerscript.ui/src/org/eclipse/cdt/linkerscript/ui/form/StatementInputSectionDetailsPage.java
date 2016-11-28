/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.cdt.linkerscript.linkerScript.InputSection;
import org.eclipse.cdt.linkerscript.linkerScript.InputSectionFile;
import org.eclipse.cdt.linkerscript.linkerScript.InputSectionWild;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.eclipse.cdt.linkerscript.linkerScript.Wildcard;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public class StatementInputSectionDetailsPage extends AbstractDetailsPage implements IDetailsPage {
	protected static final Object[] NOOBJECTS = new Object[0];
	protected static final LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;

	private Button useAllFiles;
	private Text fileNameText;
	private Button keepCheck;
	private TableViewer sectionsTableViewer;
	private Button addSectionButton;
	private Button removeSectionButton;
	private Button editSectionButton;

	private class WildcardRuleContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return getModel().readModel(getInputURI(), StatementInputSection.class, NOOBJECTS, stmt -> {
				Resource resource = stmt.eResource();
				InputSection spec = stmt.getSpec();
				if (spec instanceof InputSectionWild) {
					InputSectionWild inputSectionWild = (InputSectionWild) spec;
					return inputSectionWild.getSections().stream().map(resource::getURIFragment).toArray();

				}
				return NOOBJECTS;

			});
		}
	}

	private class WildcardRuleColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return getModel().readModel(element, EObject.class, "", t -> {
				return NodeModelUtils.getTokenText(NodeModelUtils.getNode(t));
			});
		}

	}

	public StatementInputSectionDetailsPage(ILinkerScriptModel model) {
		super(model, "Input Section Details", "Set the properties of the input section.", 1);
	}

	@Override
	protected void doUpdate() {
		fileNameText.setText(getModel().readModel(getInputURI(), StatementInputSection.class, "", stmt -> {
			InputSection spec = stmt.getSpec();
			if (spec instanceof InputSectionFile) {
				InputSectionFile fileSpec = (InputSectionFile) spec;
				return fileSpec.getFile();
			} else if (spec instanceof InputSectionWild) {
				InputSectionWild wildSpec = (InputSectionWild) spec;
				return wildSpec.getWildFile().getName();
			} else {
				return "";
			}
		}));

		useAllFiles.setSelection("*".equals(fileNameText.getText()));

		keepCheck.setSelection(getModel().readModel(getInputURI(), StatementInputSection.class, false, stmt -> {
			InputSection spec = stmt.getSpec();
			if (spec instanceof InputSectionFile) {
				return false;
			} else if (spec instanceof InputSectionWild) {
				InputSectionWild wildSpec = (InputSectionWild) spec;
				return wildSpec.isKeep();
			} else {
				return false;
			}
		}));

		sectionsTableViewer.refresh();
	}

	@Override
	public void doCreateContents(Composite composite) {
		FormToolkit toolkit = getManagedForm().getToolkit();
		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		useAllFiles = toolkit.createButton(composite, "Include all file names (use * wildcard)", SWT.CHECK);
		useAllFiles.setToolTipText(
				"Use the * wildcard to match any file name and so include all file names for this input section.");
		useAllFiles.setForeground(foreground);
		useAllFiles.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		useAllFiles.addListener(SWT.Selection, e -> {
			String newName = useAllFiles.getSelection() ? "*" : "inputname.o";
			fileNameText.setText(newName);
		});

		Label fileNameLabel = toolkit.createLabel(composite, "File Name:");
		fileNameLabel.setForeground(foreground);
		fileNameLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());

		fileNameText = toolkit.createText(composite, "Initial Name", SWT.BORDER);
		fileNameText.setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		fileNameText.addModifyListener(e -> getModel().writeModel(getInputURI(), StatementInputSection.class, stmt -> {
			InputSection spec = stmt.getSpec();
			String newName = fileNameText.getText();
			if (spec instanceof InputSectionFile) {
				InputSectionFile fileSpec = (InputSectionFile) spec;
				if (!newName.equals(fileSpec.getFile())) {
					fileSpec.setFile(newName);
				}
			} else if (spec instanceof InputSectionWild) {
				InputSectionWild wildSpec = (InputSectionWild) spec;
				if (!newName.equals(wildSpec.getWildFile().getName())) {
					wildSpec.getWildFile().setName(newName);
				}
			}
		}));

		// create sections
		Label sectionNamesLabel = toolkit.createLabel(composite, "Section Names:");
		sectionNamesLabel.setForeground(foreground);
		sectionNamesLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());

		Table table = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		table.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		sectionsTableViewer = new TableViewer(table);
		sectionsTableViewer.setContentProvider(new WildcardRuleContentProvider());
		sectionsTableViewer.setLabelProvider(new WildcardRuleColumnLabelProvider());
		sectionsTableViewer.setInput(getModel());

		Composite secButtonComp = toolkit.createComposite(composite);
		secButtonComp.setLayout(GridLayoutFactory.fillDefaults().create());
		secButtonComp.setLayoutData(GridDataFactory.fillDefaults().create());
		addSectionButton = toolkit.createButton(secButtonComp, "Add...", SWT.NONE);
		editSectionButton = toolkit.createButton(secButtonComp, "Edit...", SWT.NONE);
		removeSectionButton = toolkit.createButton(secButtonComp, "Remove", SWT.NONE);
		addSectionButton.setLayoutData(GridDataFactory.fillDefaults().create());
		editSectionButton.setLayoutData(GridDataFactory.fillDefaults().create());
		removeSectionButton.setLayoutData(GridDataFactory.fillDefaults().create());
		addSectionButton.addListener(SWT.Selection, e -> add());
		editSectionButton.addListener(SWT.Selection, e -> edit());
		removeSectionButton.addListener(SWT.Selection, e -> remove());

		keepCheck = toolkit.createButton(composite, "KEEP (Group will not be omitted even if unreferenced)", SWT.CHECK);
		keepCheck.setForeground(foreground);
		keepCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		keepCheck.addListener(SWT.Selection,
				e -> getModel().writeModel(getInputURI(), StatementInputSection.class, stmt -> {
					InputSection spec = stmt.getSpec();
					boolean newKeep = keepCheck.getSelection();
					if (spec instanceof InputSectionFile) {
						InputSectionFile fileSpec = (InputSectionFile) spec;
						if (newKeep) {
							// convert to InputSectionWild
							InputSectionWild wildSpec = factory.createInputSectionWild();
							Wildcard wildFile = factory.createWildcard();
							wildFile.setName(fileSpec.getFile());
							wildSpec.setWildFile(wildFile);
							Wildcard wildSection = factory.createWildcard();
							wildSection.setName("*");
							wildSpec.getSections().add(wildSection);
							wildSpec.setKeep(true);
							stmt.setSpec(wildSpec);
						} else {
							// nothing to do, InputSectionFile doesn't support
							// keep so nothing to disable
						}
					} else if (spec instanceof InputSectionWild) {
						InputSectionWild wildSpec = (InputSectionWild) spec;
						if (newKeep != wildSpec.isKeep()) {
							wildSpec.setKeep(newKeep);
						}
					}
				}));

	}

	private void add() {
		InputSectionDialog dialog = new InputSectionDialog(getManagedForm().getForm().getShell());
		if (dialog.open() == Dialog.OK) {
			getModel().writeModel(getInputURI(), StatementInputSection.class, stmt -> {
				InputSection spec = stmt.getSpec();
				String newName = dialog.getSectionName();

				if (spec instanceof InputSectionFile) {
					InputSectionFile fileSpec = (InputSectionFile) spec;
					// convert to InputSectionWild
					InputSectionWild wildSpec = factory.createInputSectionWild();
					Wildcard wildFile = factory.createWildcard();
					wildFile.setName(fileSpec.getFile());
					wildSpec.setWildFile(wildFile);
					Wildcard wildSection = factory.createWildcard();
					wildSection.setName(newName);
					wildSpec.getSections().add(wildSection);
					stmt.setSpec(wildSpec);
				} else if (spec instanceof InputSectionWild) {
					InputSectionWild wildSpec = (InputSectionWild) spec;
					Wildcard wildSection = factory.createWildcard();
					wildSection.setName(newName);
					wildSpec.getSections().add(wildSection);
				}
			});
		}
	}

	private void edit() {
		if (sectionsTableViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sectionsTableViewer.getSelection();
			Object selectedURI = selection.getFirstElement();

			String name = getModel().readModel(selectedURI, Wildcard.class, "", Wildcard::getName);
			InputSectionDialog dialog = new InputSectionDialog(getManagedForm().getForm().getShell(), name, "sort");
			if (dialog.open() == Dialog.OK) {
				getModel().writeModel(selectedURI, Wildcard.class, wildSection -> {
					String newName = dialog.getSectionName();
					wildSection.setName(newName);
				});
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void remove() {
		if (sectionsTableViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sectionsTableViewer.getSelection();
			getModel().writeModel(getInputURI(), StatementInputSection.class, stmt -> {
				Resource resource = stmt.eResource();
				selection.iterator().forEachRemaining(sel -> {
					EObject selObj = resource.getEObject((String) sel);
					EcoreUtil2.delete(selObj);
				});
			});
		}

	}

}
