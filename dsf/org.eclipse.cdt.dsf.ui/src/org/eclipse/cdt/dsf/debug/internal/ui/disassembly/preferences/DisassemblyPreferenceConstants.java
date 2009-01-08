/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.preferences;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;

/**
 * DisassemblyPreferenceConstants
 */
public class DisassemblyPreferenceConstants {

	public static final String START_ADDRESS = "disassembly.startAddress"; //$NON-NLS-1$
	public static final String END_ADDRESS = "disassembly.endAddress"; //$NON-NLS-1$
	public static final String PC_HISTORY_SIZE = "disassembly.pcHistorySize"; //$NON-NLS-1$
	public static final String SHOW_SOURCE = "disassembly.showSource"; //$NON-NLS-1$
	public static final String SHOW_LABELS = "disassembly.showLabels"; //$NON-NLS-1$
	public static final String SHOW_SYMBOLS = "disassembly.showSymbols"; //$NON-NLS-1$
	public static final String SIMPLIFIED = "disassembly.simplified"; //$NON-NLS-1$
	public static final String INSTRUCTION_RADIX = "disassembly.instructionRadix"; //$NON-NLS-1$
	public static final String ADDRESS_RADIX = "disassembly.addressRadix"; //$NON-NLS-1$
	public static final String SHOW_ADDRESS_RADIX = "disassembly.showAddressRadix"; //$NON-NLS-1$
	public static final String SHOW_ADDRESS_RULER = "disassembly.showAddressRuler"; //$NON-NLS-1$
	public static final String ADDRESS_COLOR = "disassembly.addressColor"; //$NON-NLS-1$
	public static final String SHOW_FUNCTION_OFFSETS = "disassembly.showFunctionOffsetRuler"; //$NON-NLS-1$
	public static final String OPCODE_COLOR = "disassembly.opcodeColor"; //$NON-NLS-1$
	public static final String USE_SOURCE_ONLY_MODE = "disassembly.useSourceOnlyMode"; //$NON-NLS-1$
	public static final String AVOID_READ_BEFORE_PC = "disassembly.avoidReadBeforePC"; //$NON-NLS-1$

	/**
	 * 
	 */
	private DisassemblyPreferenceConstants() {
		// not intended to be subclassed or instatiated
	}

	/**
	 * Initialize preference default values.
	 * @param store
	 */
	public static void initializeDefaults(IPreferenceStore store) {
		TextEditorPreferenceConstants.initializeDefaultValues(store);
		store.setDefault(START_ADDRESS, 0x0L);
		store.setDefault(END_ADDRESS, "0x" + BigInteger.ONE.shiftLeft(64).toString(16)); //$NON-NLS-1$
		store.setDefault(PC_HISTORY_SIZE, 4);
		store.setDefault(SHOW_SOURCE, true);
		store.setDefault(SHOW_FUNCTION_OFFSETS, false);
		store.setDefault(SHOW_LABELS, true);
		store.setDefault(SHOW_SYMBOLS, true);
		store.setDefault(SIMPLIFIED, true);
		store.setDefault(INSTRUCTION_RADIX, 16);
		store.setDefault(ADDRESS_RADIX, 16);
		store.setDefault(SHOW_ADDRESS_RADIX, false);
		store.setDefault(SHOW_ADDRESS_RULER, true);
		store.setDefault(AVOID_READ_BEFORE_PC, false);
		store.setDefault(USE_SOURCE_ONLY_MODE, false);
		PreferenceConverter.setDefault(store, ADDRESS_COLOR, new RGB(0, 96, 0));
		PreferenceConverter.setDefault(store, OPCODE_COLOR, new RGB(96, 0, 0));
	}

	public static class Initializer extends AbstractPreferenceInitializer {
		@Override
		public void initializeDefaultPreferences() {
			IPreferenceStore store = DsfUIPlugin.getDefault().getPreferenceStore();
			initializeDefaults(store);
			EditorsUI.useAnnotationsPreferencePage(store);
		}
	}
}
