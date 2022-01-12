/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals;

import java.util.Arrays;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;

/**
 * Label provider for <code>ICSignal</code>s.
 */
public class SignalLabelProvider extends ElementLabelProvider {

	@Override
	protected String getLabel(TreePath elementPath, IPresentationContext presentationContext, String columnId)
			throws CoreException {
		Object element = elementPath.getLastSegment();
		String[] columns = presentationContext.getColumns();
		int columnIdx = columns != null ? Arrays.asList(columns).indexOf(columnId) : 0;
		String label = getColumnText(element, columnIdx);
		return label != null ? label : ""; //$NON-NLS-1$
	}

	@Override
	protected ImageDescriptor getImageDescriptor(TreePath elementPath, IPresentationContext presentationContext,
			String columnId) throws CoreException {
		Object element = elementPath.getLastSegment();
		if (element instanceof ICSignal) {
			String[] columns = presentationContext.getColumns();
			int columnIdx = columns != null ? Arrays.asList(columns).indexOf(columnId) : 0;
			if (columnIdx == 0) {
				return CDebugImages.DESC_OBJS_SIGNAL;
			}
		}
		return super.getImageDescriptor(elementPath, presentationContext, columnId);
	}

	private String getColumnText(Object element, int columnIndex) {
		if (element instanceof ICSignal) {
			try {
				switch (columnIndex) {
				case 0:
					return ((ICSignal) element).getName();
				case 1:
					return (((ICSignal) element).isPassEnabled()) ? SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
				case 2:
					return (((ICSignal) element).isStopEnabled()) ? SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
				case 3:
					return ((ICSignal) element).getDescription();
				}
			} catch (DebugException e) {
			}
		}
		return null;
	}

}
