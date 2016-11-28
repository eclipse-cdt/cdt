/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.cdt.linkerscript.linkerScript.Assignment;
import org.eclipse.cdt.linkerscript.linkerScript.AssignmentHidden;
import org.eclipse.cdt.linkerscript.linkerScript.AssignmentProvide;
import org.eclipse.cdt.linkerscript.linkerScript.AssignmentProvideHidden;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptPackage;
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class StatementAssignmentDetailsPage extends AbstractDetailsPage implements IDetailsPage {

	protected static final LinkerScriptFactory factory = LinkerScriptFactory.eINSTANCE;
	private Text symbolNameText;
	private LExpressionText expressionText;
	private Button useProvide;
	private Button useHidden;

	public StatementAssignmentDetailsPage(ILinkerScriptModel model) {
		super(model, "Assignment Details", "Set the properties of the assignment.", 0);
	}

	@Override
	protected void doUpdate() {
		String symbolName = getModel().readModel(getInputURI(), StatementAssignment.class, "", assignment -> {
			if (assignment.getAssignment() == null) {
				return "";
			}
			return assignment.getAssignment().getName();
		});
		symbolNameText.setText(symbolName);

		String assignmentExpUri = getModel().readModel(getInputURI(), StatementAssignment.class, null, assignment -> {
			if (assignment.getAssignment() == null) {
				return null;
			}
			Resource resource = assignment.eResource();
			return resource.getURIFragment(assignment.getAssignment());
		});
		expressionText.setInput(assignmentExpUri);

		boolean provide = getModel().readModel(getInputURI(), StatementAssignment.class, false, assignment -> {
			if (assignment.getAssignment() instanceof AssignmentProvide
					|| assignment.getAssignment() instanceof AssignmentProvideHidden) {
				return true;
			}
			return false;
		});
		useProvide.setSelection(provide);

		boolean hidden = getModel().readModel(getInputURI(), StatementAssignment.class, false, assignment -> {
			if (assignment.getAssignment() instanceof AssignmentHidden
					|| assignment.getAssignment() instanceof AssignmentProvideHidden) {
				return true;
			}
			return false;
		});
		useHidden.setSelection(hidden);

	}

	@Override
	public void doCreateContents(Composite composite) {
		FormToolkit toolkit = getManagedForm().getToolkit();
		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		Label symbolNameLabel = toolkit.createLabel(composite, "Symbol Name:");
		symbolNameLabel.setForeground(foreground);
		symbolNameLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());

		symbolNameText = toolkit.createText(composite, "Initial Name", SWT.BORDER);
		symbolNameText.setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		symbolNameText.addModifyListener(
				e -> getModel().writeModel(getInputURI(), StatementAssignment.class, assignmentStmt -> {
					String newName = symbolNameText.getText();
					Assignment assignment = assignmentStmt.getAssignment();
					if (!newName.equals(assignment.getName())) {
						assignment.setName(newName);
					}
				}));

		Label assignmentValueLabel = toolkit.createLabel(composite, "Value:");
		assignmentValueLabel.setToolTipText("Any valid linker expression can be assigned to a symbol");
		assignmentValueLabel.setForeground(foreground);
		assignmentValueLabel
				.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());

		expressionText = new LExpressionText(composite, toolkit, getModel(),
				LinkerScriptPackage.Literals.ASSIGNMENT__EXP);
		expressionText.getText().setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());

		useProvide = toolkit.createButton(composite, "Use PROVIDE keyword", SWT.CHECK);
		useProvide.setToolTipText(
				"The PROVIDE keyword defines a symbol only if it is referenced but not otherwise defined.");
		useProvide.setForeground(foreground);
		useProvide.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		useProvide.addListener(SWT.Selection, this::provideHiddenChanged);

		useHidden = toolkit.createButton(composite, "Use HIDDEN keyword", SWT.CHECK);
		useHidden.setToolTipText("The HIDDEN keyword defines a symbol that will be hidden and will not be exported.");
		useHidden.setForeground(foreground);
		useHidden.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		useHidden.addListener(SWT.Selection, this::provideHiddenChanged);
	}

	private void provideHiddenChanged(Event e) {
		getModel().writeModel(getInputURI(), StatementAssignment.class, assignmentStmt -> {
			boolean newProvide = useProvide.getSelection();
			boolean newHidden = useHidden.getSelection();
			Assignment wasAssignment = assignmentStmt.getAssignment();
			boolean wasProvide = wasAssignment instanceof AssignmentProvide
					|| wasAssignment instanceof AssignmentProvideHidden;
			boolean wasHidden = wasAssignment instanceof AssignmentHidden
					|| wasAssignment instanceof AssignmentProvideHidden;

			if (newProvide != wasProvide || newHidden != wasHidden) {
				Assignment newAssignment;
				if (newProvide && newHidden) {
					newAssignment = factory.createAssignmentProvideHidden();
				} else if (newProvide) {
					newAssignment = factory.createAssignmentProvide();
				} else if (newHidden) {
					newAssignment = factory.createAssignmentHidden();
				} else {
					newAssignment = factory.createAssignment();
				}

				newAssignment.setName(wasAssignment.getName());
				newAssignment.setFeature(wasAssignment.getFeature());
				newAssignment.setExp(wasAssignment.getExp());
				assignmentStmt.setAssignment(newAssignment);
			}
		});
	}

}
