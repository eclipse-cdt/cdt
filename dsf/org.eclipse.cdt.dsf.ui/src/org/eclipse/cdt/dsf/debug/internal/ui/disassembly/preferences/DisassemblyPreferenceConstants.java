/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
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

/**
 * DisassemblyPreferenceConstants
 */
public class DisassemblyPreferenceConstants {

	public static final String START_ADDRESS = "disassembly.startAddress"; //$NON-NLS-1$
	public static final String END_ADDRESS = "disassembly.endAddress"; //$NON-NLS-1$
	public static final String PC_HISTORY_SIZE = "disassembly.pcHistorySize"; //$NON-NLS-1$
	public static final String SHOW_SOURCE = "disassembly.showSource"; //$NON-NLS-1$
	public static final String SHOW_SYMBOLS = "disassembly.showSymbols"; //$NON-NLS-1$
	public static final String ADDRESS_RADIX = "disassembly.addressRadix"; //$NON-NLS-1$
	public static final String OPCODE_RADIX = "disassembly.opcodeRadix"; //$NON-NLS-1$
	public static final String SHOW_ADDRESS_RADIX = "disassembly.showAddressRadix"; //$NON-NLS-1$
	public static final String ADDRESS_COLOR = "disassembly.addressColor"; //$NON-NLS-1$
	public static final String FUNCTION_OFFSETS_COLOR = "disassembly.functionOffsetsColor"; //$NON-NLS-1$
	public static final String CODE_BYTES_COLOR = "disassembly.codeBytesColor"; //$NON-NLS-1$
	public static final String AVOID_READ_BEFORE_PC = "disassembly.avoidReadBeforePC"; //$NON-NLS-1$
	public static final String TRACK_EXPRESSION = "disassembly.trackExpression"; //$NON-NLS-1$
	public static final String SYNC_ACTIVE_CONTEXT = "disassembly.syncActiveContext"; //$NON-NLS-1$
	
	/**
	 * 
	 */
	private DisassemblyPreferenceConstants() {
		// not intended to be subclassed or instantiated
	}

	/**
	 * Initialize preference default values.
	 * @param store
	 */
	public static void initializeDefaults(IPreferenceStore store) {
		store.setDefault(START_ADDRESS, 0x0L);
		store.setDefault(END_ADDRESS, "0x" + BigInteger.ONE.shiftLeft(64).toString(16)); //$NON-NLS-1$
		store.setDefault(PC_HISTORY_SIZE, 4);
		store.setDefault(SHOW_SOURCE, true);
		store.setDefault(SHOW_SYMBOLS, true);
		store.setDefault(ADDRESS_RADIX, 16);
		store.setDefault(OPCODE_RADIX, 16);
		store.setDefault(SHOW_ADDRESS_RADIX, false);
		store.setDefault(AVOID_READ_BEFORE_PC, false);
		PreferenceConverter.setDefault(store, ADDRESS_COLOR, new RGB(0, 96, 0));
		PreferenceConverter.setDefault(store, FUNCTION_OFFSETS_COLOR, new RGB(96, 0, 0));
		PreferenceConverter.setDefault(store, CODE_BYTES_COLOR, new RGB(96, 0, 0));
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
