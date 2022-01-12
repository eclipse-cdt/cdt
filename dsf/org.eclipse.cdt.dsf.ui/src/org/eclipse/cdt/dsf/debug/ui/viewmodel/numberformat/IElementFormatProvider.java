/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Element format provider - an optional interface that provides individual element format
 * A view model provider (org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider) can optionally implement this interface.
 * If there is a requirement to persist individual format settings, this provider can
 * add an persistable (IPersistable) property to PresentationContext so that when presentation context
 * is invoked to persist its properties, the individual format settings are persisted as well.
 *
 * @since 2.2
 */
public interface IElementFormatProvider {
	/**
	 * Get active format for a given element.
	 * @param context presentation context
	 * @param node view model node
	 * @param viewerInput viewer input
	 * @param elementPath element path of the given element
	 * @param rm request monitor
	 * @return active format if available. Calls rm.setData(null) if there is no active format.
	 *         The caller will use the active format if the returned format is available,
	 *         and will use preference format if the returned format is null or not available.
	 *         Note that if caller finds that the preference format is not available from service,
	 *         it will use the first available format from service. See FormattedValueRetriever.
	 */
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput, TreePath elementPath,
			DataRequestMonitor<String> rm);

	/**
	 * Set active format for given elements. The caller will not fire any event to update view.
	 * The implementation of this method should fire proper events to refresh impacted elements.
	 * One way is to refresh the view through IVMCachingProvider.refresh but it will
	 * refresh other non-impacted elements.
	 * Another way that may be more optimal is to fire ElementFormatEvent that stores exactly the
	 * impacted elements. The view model can then handle the event more efficiently.
	 * @param context presentation context
	 * @param node view model nodes
	 * @param viewerInput viewer input
	 * @param elementPath element path of given elements
	 * @param format format
	 */
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput,
			TreePath[] elementPath, String format);

	/**
	 * Test if this provider supports individual element format for a given context
	 * @param context given context
	 * @return true if this provider supports individual element format.
	 */
	public boolean supportFormat(IVMContext context);
}
