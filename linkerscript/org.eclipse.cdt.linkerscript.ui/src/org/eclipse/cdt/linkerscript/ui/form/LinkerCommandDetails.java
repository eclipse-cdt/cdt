/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public class LinkerCommandDetails {

	private Text rawText;
	private ILinkerScriptModel model;
	private String inputURI;
	private int displayContainerCount;

	public LinkerCommandDetails(Composite composite, FormToolkit toolkit, ILinkerScriptModel model,
			int displayContainerCount) {
		this.model = model;
		this.displayContainerCount = displayContainerCount;
		Section scriptSection = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR);
		scriptSection.clientVerticalSpacing = 6;
		scriptSection.setLayout(GridLayoutFactory.fillDefaults().create());
		scriptSection.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(3, 1).create());

		Composite client = toolkit.createComposite(scriptSection);
		client.setLayout(GridLayoutFactory.fillDefaults().create());

		scriptSection.setText("Linker Script Commands Preview:");

		rawText = toolkit.createText(client, "", SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		rawText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rawText.setToolTipText(
				"This is a read-only preview, edit using widget controls or directly in the linker script file.");
		toolkit.paintBordersFor(client);

		scriptSection.setClient(client);
	}

	public void refresh() {
		rawText.setText(model.readModel(inputURI, EObject.class, "", obj -> {
			for (int i = 0; i < displayContainerCount; i++) {
				EObject container = obj.eContainer();
				if (container != null) {
					obj = container;
				}
			}
			String text = NodeModelUtils.getNode(obj).getText();
			String lines[] = text.split("\\r?\\n");
			int shortestPrefix = Integer.MAX_VALUE;
			for (String line : lines) {
				for (int i = 0; i < line.length(); i++) {
					if (!Character.isWhitespace(line.charAt(i))) {
						shortestPrefix = Math.min(shortestPrefix, i);
						break;
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			for (String line : lines) {
				if (shortestPrefix > 0 && shortestPrefix < line.length()) {
					line = line.substring(shortestPrefix);
				}
				if (sb.length() > 0) {
					sb.append(line);
					sb.append(System.lineSeparator());
				} else if (line.length() > 0) {
					sb.append(line);
					sb.append(System.lineSeparator());
				}
			}
			return sb.toString();
		}));

	}

	public void setInput(String inputURI) {
		this.inputURI = inputURI;
		refresh();
	}

	public Widget getControl() {
		return rawText;
	}
}
