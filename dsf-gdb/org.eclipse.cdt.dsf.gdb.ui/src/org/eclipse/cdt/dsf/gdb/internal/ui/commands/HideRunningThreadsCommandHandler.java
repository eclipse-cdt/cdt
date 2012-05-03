/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/** 
 * Command handler to enable the hiding of running threads.
 * 
 * EXPERIMENTAL: We expect to remove this command and replace it with filtering.
 *
 * @since 2.3
 */
public class HideRunningThreadsCommandHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		boolean current = store.getBoolean(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS);
		store.setValue(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS, !current);

		return null;
	}
	
    @Override
	public void updateElement(UIElement element,
                              @SuppressWarnings("rawtypes") Map parameters) {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		boolean current = store.getBoolean(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS);
    	element.setChecked(current);
   }
}
