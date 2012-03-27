/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import org.eclipse.cdt.debug.core.DebugCoreMessages;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.internal.ui.CDebugModelPresentation;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.model.elements.BreakpointLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.icu.text.MessageFormat;

/**
 * Factory for event breakpoint label provider
 */
public class CEventBreakpointsLabelProviderFactory implements IAdapterFactory {
	private static ILabelProvider fLabelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof ICEventBreakpoint) {
				try {
					ICEventBreakpoint breakpoint = (ICEventBreakpoint) element;

					ICBreakpointsUIContribution bscs[] = CBreakpointUIContributionFactory.getInstance()
							.getBreakpointUIContributions(breakpoint);

					if (bscs.length == 0)
						return null;
					StringBuffer buffer = new StringBuffer();

					for (ICBreakpointsUIContribution con : bscs) {
						Object attValue = breakpoint.getMarker().getAttribute(con.getId());

						if (con.getId().equals(ICEventBreakpoint.EVENT_TYPE_ID)) {
							buffer.append(con.getLabelForValue((String) attValue));
							continue;
						}
						if (attValue != null && attValue.toString().length() > 0) {
							buffer.append(" ["); //$NON-NLS-1$
							buffer.append(con.getLabel());
							buffer.append(": "); //$NON-NLS-1$
							if (attValue instanceof String)
								buffer.append(con.getLabelForValue((String) attValue));
							else
								buffer.append(attValue);
							buffer.append(']');
						}
					}
					appendIgnoreCount(breakpoint, buffer);
					appendCondition(breakpoint, buffer);
					return buffer.toString();

				} catch (CoreException e) {
					CDebugUIPlugin.log(e);
				}
			}
			return null;
		}

		/**
		 * Returns null. We do not provide the image because it would require
		 * duplicating centralized code in {@link CDebugModelPresentation},
		 * particularly the code that determines the proper overlays. This
		 * adapter is actually only called from within CDebugModelPresentation
		 * and we know it will do the right thing for an event breakpoint if we
		 * return null here. 
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object element) {
 
			return null;
		}
	};

	protected static StringBuffer appendIgnoreCount(ICBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int ignoreCount = breakpoint.getIgnoreCount();
		if (ignoreCount > 0) {
			label.append(' ');
			label.append(MessageFormat.format(
					DebugCoreMessages.getString("CDebugUtils.3"), new Object[] { Integer.toString(ignoreCount) })); //$NON-NLS-1$
		}
		return label;
	}

	protected static void appendCondition(ICBreakpoint breakpoint, StringBuffer buffer) throws CoreException {
		String condition = breakpoint.getCondition();
		if (condition != null && condition.length() > 0) {
			buffer.append(' ');
			buffer.append(MessageFormat
					.format(DebugCoreMessages.getString("CDebugUtils.4"), new Object[] { condition })); //$NON-NLS-1$
		}
	}

	private static IElementLabelProvider fElementLabelProvider = new BreakpointLabelProvider();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IElementLabelProvider.class)) {
			if (adaptableObject instanceof ICEventBreakpoint) {
				return fElementLabelProvider;
			}
		}
		if (adapterType.equals(ILabelProvider.class)) {
			if (adaptableObject instanceof ICEventBreakpoint) {
				return fLabelProvider;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IElementLabelProvider.class, ILabelProvider.class };
	}
}
