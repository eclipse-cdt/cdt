/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScriptFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class AddressText {
	private LExpressionText addressText;
	private Button fixedAddressCheck;
	private Label addressLabel;

	private String containerURI;

	private String lastEnabledText;

	public AddressText(Composite composite, FormToolkit toolkit, ILinkerScriptModel model, EReference featureReference,
			String name) {

		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		fixedAddressCheck = toolkit.createButton(composite, "Set " + name + " explicitly", SWT.CHECK);
		fixedAddressCheck.setForeground(foreground);
		fixedAddressCheck.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
		fixedAddressCheck.addListener(SWT.Selection, e -> {
			boolean isFixedAddress = fixedAddressCheck.getSelection();
			if (!isFixedAddress) {
				lastEnabledText = addressText.getText().getText();
			}

			addressText.getText().setEnabled(isFixedAddress);
			addressLabel.setEnabled(isFixedAddress);

			model.writeModel(containerURI, EObject.class, container -> {
				if (isFixedAddress) {
					LNumberLiteral literal = LinkerScriptFactory.eINSTANCE.createLNumberLiteral();
					literal.setValue(0L);
					container.eSet(featureReference, literal);
				} else {
					container.eUnset(featureReference);
				}
			});
			if (isFixedAddress && lastEnabledText != null) {
				composite.getDisplay().asyncExec(() -> addressText.getText().setText(lastEnabledText));
			}
		});

		addressLabel = toolkit.createLabel(composite, name + ":");
		addressLabel.setForeground(foreground);
		addressLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());
		addressText = new LExpressionText(composite, toolkit, model, featureReference);
		addressText.getText().setLayoutData(
				GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, GridData.CENTER).grab(true, false).create());
	}

	public void refresh() {
		addressText.refresh();

		if (addressText.getText().getText().isEmpty()) {
			addressText.getText().setEnabled(false);
			addressLabel.setEnabled(false);
			fixedAddressCheck.setSelection(false);
		} else {
			addressText.getText().setEnabled(true);
			addressLabel.setEnabled(true);
			fixedAddressCheck.setSelection(true);
		}
	}

	public void setInput(String containerURI) {
		this.containerURI = containerURI;
		addressText.setInput(containerURI);
		refresh();
	}

}
