/*******************************************************************************
 * Copyright (c) 2008, 2020 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Marco Stornelli <marco.stornelli@gmail.com> - Bug 333134
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 333134, Bug 559193
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.options.OptionMetadata;
import org.eclipse.cdt.core.options.OptionStorage;
import org.eclipse.cdt.doxygen.DoxygenMetadata;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * <em>This class is not intended for use outside of CDT</em>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DocCommentOwnerComposite extends Composite {
	protected DocCommentOwnerCombo fDocCombo;
	protected Label desc, comboLabel;
	protected Group group;

	private final Map<OptionMetadata<Boolean>, Button> buttons;

	public DocCommentOwnerComposite(Composite parent, IDocCommentOwner initialOwner, String description, String label) {
		super(parent, SWT.NONE);
		buttons = new LinkedHashMap<>();
		GridLayout gl = new GridLayout();
		gl.marginHeight = gl.marginWidth = 0;
		setLayout(gl);

		group = ControlFactory.createGroup(this, DialogsMessages.DocCommentOwnerComposite_DocumentationToolGroupTitle,
				2);
		group.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		desc = new Label(group, SWT.WRAP);
		GridData gd = GridDataFactory.fillDefaults().grab(false, false).span(2, 1).create();
		gd.widthHint = 150;
		desc.setText(description);
		desc.setLayoutData(gd);

		comboLabel = new Label(group, SWT.NONE);
		comboLabel.setText(label);

		fDocCombo = new DocCommentOwnerCombo(group, SWT.NONE, initialOwner) {
		};
		gd = GridDataFactory.fillDefaults().grab(true, false).create();
		fDocCombo.setLayoutData(gd);
		fDocCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> recheckButtons()));
	}

	/**
	 * Creates widgets required to represent doxygen options, extracted from constructor to keep it unchanged.
	 * Needs to be invoked only once just after the constructor
	 *
	 * @param metadata the doxygen metadata to use for checkbox creation
	 *
	 * @since 6.7
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setDoxygenMetadata(DoxygenMetadata metadata) {
		metadata.booleanOptions().forEach(o -> createCheckBox(group, o));
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

	public IDocCommentOwner getSelectedDocCommentOwner() {
		return fDocCombo.getSelectedDocCommentOwner();
	}

	/**
	 * Initializes widget values from the given option storage instance
	 *
	 * @param storage the option storage to initialize from
	 *
	 * @since 6.7
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void initialize(OptionStorage storage) {
		buttons.entrySet().stream().forEach(e -> e.getValue().setSelection(storage.load(e.getKey())));
	}

	/**
	 * Apply widget values to the given option storage instance
	 *
	 * @param storage the option storage to apply to
	 *
	 * @since 6.7
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void apply(OptionStorage storage) {
		buttons.entrySet().stream().forEach(e -> storage.save(e.getValue().getSelection(), e.getKey()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		desc.setEnabled(enabled);
		comboLabel.setEnabled(enabled);
		fDocCombo.setEnabled(enabled);
		group.setEnabled(enabled);
		recheckButtons();
	}

	void recheckButtons() {
		boolean doxygenEnabled = fDocCombo.isEnabled() && DocCommentOwnerManager.DOXYGEN_CDT_DOC_ONWER_ID
				.equals(fDocCombo.getSelectedDocCommentOwner().getID());
		buttons.values().forEach(b -> b.setEnabled(doxygenEnabled));
	}

}
