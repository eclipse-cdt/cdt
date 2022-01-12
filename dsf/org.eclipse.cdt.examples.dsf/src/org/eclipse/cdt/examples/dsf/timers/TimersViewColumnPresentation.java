/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import org.eclipse.cdt.examples.dsf.DsfExamplesPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 *
 */
@SuppressWarnings("restriction")
public class TimersViewColumnPresentation implements IColumnPresentation {

	public static final String ID = DsfExamplesPlugin.PLUGIN_ID + ".TIMER_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$
	public static final String COL_ID = ID + ".COL_ID"; //$NON-NLS-1$
	public static final String COL_VALUE = ID + ".COL_VALUE"; //$NON-NLS-1$

	@Override
	public void init(IPresentationContext context) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public String[] getAvailableColumns() {
		return new String[] { COL_ID, COL_VALUE };
	}

	@Override
	public String getHeader(String id) {
		if (COL_ID.equals(id)) {
			return "ID"; //$NON-NLS-1$
		} else if (COL_VALUE.equals(id)) {
			return "Value"; //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String id) {
		return null;
	}

	@Override
	public String[] getInitialColumns() {
		return getAvailableColumns();
	}

	@Override
	public boolean isOptional() {
		return true;
	}

}
