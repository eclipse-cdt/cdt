package org.eclipse.cdt.linkerscript.ui.form;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.cdt.linkerscript.linkerScript.Memory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.xtext.EcoreUtil2;

public class MemoryRegionText {
	private Text regionText;
	private Button browseMemoryButton;

	private ILinkerScriptModel model;
	private String containerURI;
	private EAttribute featureAttribute;

	public MemoryRegionText(Composite composite, FormToolkit toolkit, ILinkerScriptModel model,
			EAttribute featureAttribute, String name) {
		this.model = model;
		this.featureAttribute = featureAttribute;

		Color foreground = toolkit.getColors().getColor(IFormColors.TITLE);

		Label regionLabel = toolkit.createLabel(composite, name + ":");
		regionLabel.setForeground(foreground);
		regionLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, GridData.CENTER).create());
		regionText = toolkit.createText(composite, "Initial Region");
		regionText.setLayoutData(
				GridDataFactory.fillDefaults().align(SWT.FILL, GridData.CENTER).grab(true, false).create());
		regionText.addModifyListener(e -> model.writeModel(containerURI, EObject.class, sec -> {
			String newRegion = regionText.getText();
			if (newRegion.isEmpty()) {
				newRegion = null;
			}
			Object region = sec.eGet(featureAttribute);
			if (!Objects.equals(region, newRegion)) {
				sec.eSet(featureAttribute, newRegion);
			}
		}));

		browseMemoryButton = toolkit.createButton(composite, "Browse...", SWT.PUSH);
		browseMemoryButton.addListener(SWT.Selection, this::browse);
	}

	private void browse(Event event) {
		String[] memNames = model.readModel("/", EObject.class, null, root -> {
			List<Memory> allMemories = EcoreUtil2.getAllContentsOfType(root, Memory.class);
			Stream<String> memNamesStream = allMemories.stream().map(Memory::getName);
			return memNamesStream.toArray(size -> new String[size]);
		});

		SingleStringSelectionStatusDialog dialog = new SingleStringSelectionStatusDialog(browseMemoryButton.getShell(), memNames);
		dialog.setTitle("Select Memory");
		dialog.setMessage("Select a memory:");
		if (dialog.open() == Window.OK) {
			String firstResult = (String) dialog.getFirstResult();
			regionText.setText(firstResult);
		}
	}

	public void refresh() {
		Object region = model.readModel(containerURI, EObject.class, null,
				outputSection -> outputSection.eGet(featureAttribute));
		String regionStr;
		if (region instanceof String) {
			regionStr = (String) region;
		} else {
			regionStr = "";
		}
		regionText.setText(regionStr);
	}

	public void setInput(String containerURI) {
		this.containerURI = containerURI;
		refresh();
	}
}
