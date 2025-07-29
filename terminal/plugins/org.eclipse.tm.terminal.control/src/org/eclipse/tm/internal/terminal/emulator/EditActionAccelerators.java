/*******************************************************************************
 * Copyright (c) 2013, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tm.internal.terminal.emulator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

class EditActionAccelerators {
	private static final String COPY_COMMAND_ID = "org.eclipse.tm.terminal.copy"; //$NON-NLS-1$
	private static final String PASTE_COMMAND_ID = "org.eclipse.tm.terminal.paste"; //$NON-NLS-1$

	private final Map<Integer, String> commandIdsByAccelerator = new HashMap<>();

	private void load() {
		addAccelerator(COPY_COMMAND_ID);
		addAccelerator(PASTE_COMMAND_ID);
	}

	private void addAccelerator(String commandId) {
		TriggerSequence[] bindings = bindingsFor(commandId);
		for (int i = 0; i < bindings.length; ++i) {
			if (bindings[i] instanceof KeySequence) {
				KeyStroke[] keyStrokes = ((KeySequence) bindings[i]).getKeyStrokes();
				if (keyStrokes.length != 0) {
					int accelerator = SWTKeySupport.convertKeyStrokeToAccelerator(keyStrokes[0]);
					commandIdsByAccelerator.put(Integer.valueOf(accelerator), commandId);
				}
			}
		}
	}

	private static TriggerSequence[] bindingsFor(String commandId) {
		IBindingService bindingService = bindingService();
		return bindingService.getActiveBindingsFor(commandId);
	}

	private static IBindingService bindingService() {
		return PlatformUI.getWorkbench().getAdapter(IBindingService.class);
	}

	boolean isCopyAction(int accelerator) {
		return isMatchingAction(accelerator, COPY_COMMAND_ID);
	}

	boolean isPasteAction(int accelerator) {
		return isMatchingAction(accelerator, PASTE_COMMAND_ID);
	}

	private boolean isMatchingAction(int accelerator, String commandId) {
		if (commandIdsByAccelerator.isEmpty()) {
			load();
		}
		return commandId.equals(commandIdsByAccelerator.get(Integer.valueOf(accelerator)));
	}
}
