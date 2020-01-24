/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.options.OptionMetadata;
import org.eclipse.cdt.core.options.OptionStorage;
import org.eclipse.cdt.doxygen.DoxygenMetadata;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class DocCommentOwnerArea {

	private final Label desc;
	private final Label comboLabel;
	private final Group group;

	private final Combo combo;
	private final IDocCommentOwner owners[];

	private final Map<OptionMetadata<Boolean>, Button> buttons;

	public DocCommentOwnerArea(Composite pane, DoxygenMetadata metadata, String descriptionText,
			String comboLabelText) {
		owners = getNontestOwners();
		buttons = new LinkedHashMap<>();
		group = ControlFactory.createGroup(pane, Messages.DocCommentOwnerArea_group_doctool, 2);
		group.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		desc = new Label(group, SWT.WRAP);
		desc.setText(descriptionText);
		desc.setLayoutData(
				GridDataFactory.fillDefaults().grab(false, false).span(2, 1).hint(150, SWT.DEFAULT).create());
		comboLabel = new Label(group, SWT.NONE);
		comboLabel.setText(comboLabelText);
		combo = createCombo(group);
		combo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> recheckButtons()));
		metadata.booleanOptions().forEach(o -> createCheckBox(group, o));
	}

	private Combo createCombo(Composite parent) {
		String[] items = new String[owners.length + 1];
		items[0] = Messages.DocCommentOwnerArea_doctool_none;
		for (int i = 0; i < owners.length; i++) {
			items[i + 1] = owners[i].getName();
		}
		Combo created = ControlFactory.createSelectCombo(parent, items, Messages.DocCommentOwnerArea_doctool_none);
		return created;
	}

	private Button createCheckBox(Composite parent, OptionMetadata<Boolean> option) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(option.name());
		checkBox.setToolTipText(option.description());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 0;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		buttons.put(option, checkBox);
		return checkBox;
	}

	/**
	 * @return the array of registered doc-comment owners, filtering out those from the
	 * test plug-in.
	 */
	private IDocCommentOwner[] getNontestOwners() {
		List<IDocCommentOwner> result = new ArrayList<>();
		for (IDocCommentOwner owner : DocCommentOwnerManager.getInstance().getRegisteredOwners()) {
			if (owner.getID().indexOf(".test.") == -1) //$NON-NLS-1$
				result.add(owner);
		}
		return result.toArray(new IDocCommentOwner[result.size()]);
	}

	public void initialize(IDocCommentOwner initial, OptionStorage storage) {
		selectDocumentOwner(initial, combo);
		buttons.entrySet().stream().forEach(e -> e.getValue().setSelection(storage.load(e.getKey())));
	}

	private void selectDocumentOwner(IDocCommentOwner owner, Combo created) {
		for (int i = 0; i < owners.length; i++) {
			if (owners[i].getID().equals(owner.getID())) {
				created.select(i + 1);
				return;
			}
		}
		created.select(0);
		//no selection event here for some reason, need to force re-check
		recheckButtons();
	}

	public void setEnabled(boolean enabled) {
		desc.setEnabled(enabled);
		comboLabel.setEnabled(enabled);
		combo.setEnabled(enabled);
		group.setEnabled(enabled);
		recheckButtons();
	}

	void recheckButtons() {
		boolean doxygenEnabled = combo.isEnabled()
				&& DocCommentOwnerManager.DOXYGEN_CDT_DOC_ONWER_ID.equals(getSelectedDocCommentOwner().getID());
		buttons.values().forEach(b -> b.setEnabled(doxygenEnabled));
	}

	public IDocCommentOwner getSelectedDocCommentOwner() {
		int index = combo.getSelectionIndex();
		return index == 0 ? NullDocCommentOwner.INSTANCE : owners[index - 1];
	}

	public void apply(OptionStorage storage) {
		buttons.entrySet().stream().forEach(e -> storage.save(e.getValue().getSelection(), e.getKey()));
	}

}
