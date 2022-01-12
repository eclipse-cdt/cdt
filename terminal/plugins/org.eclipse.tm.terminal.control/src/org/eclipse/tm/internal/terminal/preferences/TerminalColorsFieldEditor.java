/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

import static org.eclipse.tm.terminal.model.TerminalColor.BACKGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_RED;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_YELLOW;
import static org.eclipse.tm.terminal.model.TerminalColor.CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.FOREGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.RED;
import static org.eclipse.tm.terminal.model.TerminalColor.SELECTION_BACKGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.SELECTION_FOREGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.YELLOW;

import java.util.EnumMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.terminal.control.impl.TerminalMessages;
import org.eclipse.tm.terminal.model.TerminalColor;

/**
 * A field editor that can be used for editing terminal colors.
 *
 * @since 5.0
 */
public class TerminalColorsFieldEditor extends FieldEditor {

	private EnumMap<TerminalColor, ColorSelector> colorSelectors;
	private Composite controls;
	private Font boldFont;

	/**
	 * Creates a field editor for editing colors of {@link TerminalColor}.
	 * The preference names used are as they are returned from {@link TerminalColor#getPreferenceName()}
	 * @param labelText
	 * @param parent
	 */
	public TerminalColorsFieldEditor(Composite parent) {
		super("", "", parent); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) controls.getLayoutData();
		gd.horizontalSpan = numColumns;
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		colorSelectors = new EnumMap<>(TerminalColor.class);
		controls = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(controls);
		GridLayoutFactory.fillDefaults().applyTo(controls);

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.BOLD);
		boldFont = boldDescriptor.createFont(parent.getDisplay());

		Group general = new Group(controls, SWT.SHADOW_NONE);
		general.setText(TerminalMessages.TerminalColorsFieldEditor_GeneralColors);
		general.setFont(boldFont);
		GridDataFactory.fillDefaults().applyTo(general);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(general);

		createLabelledSelector(general, FOREGROUND, TerminalMessages.TerminalColorsFieldEditor_TextColor);
		createLabelledSelector(general, BACKGROUND, TerminalMessages.TerminalColorsFieldEditor_Background);
		createLabelledSelector(general, SELECTION_BACKGROUND, TerminalMessages.TerminalColorsFieldEditor_Selection);
		createLabelledSelector(general, SELECTION_FOREGROUND, TerminalMessages.TerminalColorsFieldEditor_SelectedText);

		Group palette = new Group(controls, SWT.SHADOW_NONE);
		palette.setText(TerminalMessages.TerminalColorsFieldEditor_PaletteColors);
		palette.setFont(boldFont);
		GridDataFactory.fillDefaults().applyTo(palette);
		GridLayoutFactory.swtDefaults().numColumns(8).applyTo(palette);

		createSelector(palette, BLACK, TerminalMessages.TerminalColorsFieldEditor_Black);
		createSelector(palette, RED, TerminalMessages.TerminalColorsFieldEditor_Red);
		createSelector(palette, GREEN, TerminalMessages.TerminalColorsFieldEditor_Green);
		createSelector(palette, YELLOW, TerminalMessages.TerminalColorsFieldEditor_Yellow);
		createSelector(palette, BLUE, TerminalMessages.TerminalColorsFieldEditor_Blue);
		createSelector(palette, MAGENTA, TerminalMessages.TerminalColorsFieldEditor_Magenta);
		createSelector(palette, CYAN, TerminalMessages.TerminalColorsFieldEditor_Cyan);
		createSelector(palette, WHITE, TerminalMessages.TerminalColorsFieldEditor_White);

		createSelector(palette, BRIGHT_BLACK, TerminalMessages.TerminalColorsFieldEditor_BrightBlack);
		createSelector(palette, BRIGHT_RED, TerminalMessages.TerminalColorsFieldEditor_BrightRed);
		createSelector(palette, BRIGHT_GREEN, TerminalMessages.TerminalColorsFieldEditor_BrightGreen);
		createSelector(palette, BRIGHT_YELLOW, TerminalMessages.TerminalColorsFieldEditor_BrightYellow);
		createSelector(palette, BRIGHT_BLUE, TerminalMessages.TerminalColorsFieldEditor_BrightBlue);
		createSelector(palette, BRIGHT_MAGENTA, TerminalMessages.TerminalColorsFieldEditor_BrightMagenta);
		createSelector(palette, BRIGHT_CYAN, TerminalMessages.TerminalColorsFieldEditor_BrightCyan);
		createSelector(palette, BRIGHT_WHITE, TerminalMessages.TerminalColorsFieldEditor_BrightWhite);

		Group presets = new Group(controls, SWT.SHADOW_NONE);
		presets.setText(TerminalMessages.TerminalColorsFieldEditor_Presets);
		presets.setFont(boldFont);
		GridDataFactory.fillDefaults().applyTo(presets);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(presets);
		Combo presetsCombo = new Combo(presets, SWT.DROP_DOWN | SWT.READ_ONLY);
		presetsCombo.add(TerminalMessages.TerminalColorsFieldEditor_LoadPresets);
		TerminalColorPresets colorPresets = TerminalColorPresets.INSTANCE;
		colorPresets.getPresets().forEach(presetsCombo::add);
		presetsCombo.addListener(SWT.Selection, e -> {
			int selectionIndex = presetsCombo.getSelectionIndex();
			if (selectionIndex > 0) {
				int selectedPresetIndex = selectionIndex - 1; // account for "Load Presets..." entry
				colorSelectors.forEach((terminalColor, colorSelector) -> colorSelector
						.setColorValue(colorPresets.getPreset(selectedPresetIndex).getRGB(terminalColor)));

			}
		});
		presetsCombo.select(0);
	}

	@Override
	public void dispose() {
		if (boldFont != null) {
			boldFont.dispose();
		}
	}

	private void createLabelledSelector(Composite parent, TerminalColor color, String label) {
		Label labelControl = new Label(parent, SWT.LEFT);
		labelControl.setText(label);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelControl);
		createSelector(parent, color, label);
	}

	private void createSelector(Composite parent, TerminalColor color, String label) {
		ColorSelector colorSelector = new ColorSelector(parent);
		colorSelector.getButton().setToolTipText(label);
		GridDataFactory.fillDefaults().applyTo(colorSelector.getButton());
		colorSelectors.put(color, colorSelector);
	}

	@Override
	protected void doLoad() {
		IPreferenceStore store = getPreferenceStore();
		colorSelectors.forEach((terminalColor, colorSelector) -> colorSelector.setColorValue(
				PreferenceConverter.getColor(store, ITerminalConstants.getPrefForTerminalColor(terminalColor))));
	}

	@Override
	protected void doLoadDefault() {
		IPreferenceStore store = getPreferenceStore();
		colorSelectors.forEach((terminalColor, colorSelector) -> colorSelector.setColorValue(
				PreferenceConverter.getDefaultColor(store, ITerminalConstants.getPrefForTerminalColor(terminalColor))));
	}

	@Override
	public void store() {
		IPreferenceStore store = getPreferenceStore();
		if (store == null) {
			return;
		}

		if (presentsDefaultValue()) {
			doStoreDefault(store);
		} else {
			doStore();
		}
	}

	/**
	 * Stores the default preference value from this field editor into
	 * the preference store.
	 */
	protected void doStoreDefault(IPreferenceStore store) {
		colorSelectors.forEach((terminalColor, colorSelector) -> store
				.setToDefault(ITerminalConstants.getPrefForTerminalColor(terminalColor)));
	}

	@Override
	protected void doStore() {
		IPreferenceStore store = getPreferenceStore();
		colorSelectors.forEach((terminalColor, colorSelector) -> PreferenceConverter.setValue(store,
				ITerminalConstants.getPrefForTerminalColor(terminalColor), colorSelector.getColorValue()));
	}

	@Override
	public String getPreferenceName() {
		throw new IllegalArgumentException(
				"preference name should not be accessed as this class represent multiple preferences"); //$NON-NLS-1$
	}

	@Override
	public String getLabelText() {
		throw new IllegalArgumentException(
				"label text should not be accessed as this class represent multiple preferences"); //$NON-NLS-1$
	}

}
