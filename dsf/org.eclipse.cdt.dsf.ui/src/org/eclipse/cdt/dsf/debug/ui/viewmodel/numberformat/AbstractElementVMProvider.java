/*****************************************************************
 * Copyright (c) 2014 Ericsson and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Base class for view model providers that can support individual element formatting.
 * Extending classes can override {@link #supportFormat(IVMContext)} to return false
 * if they do not want to support individual element formatting.
 */
abstract public class AbstractElementVMProvider extends AbstractDMVMProvider implements IElementFormatProvider {
	private final IElementFormatProvider fElementFormatProvider;

	public AbstractElementVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
		fElementFormatProvider = createElementNumberFormatProvider(this, getSession());
	}

	@Override
	public void dispose() {
		if (fElementFormatProvider instanceof ElementNumberFormatProvider) {
			((ElementNumberFormatProvider) fElementFormatProvider).dispose();
		}
		super.dispose();
	}

	protected IElementFormatProvider createElementNumberFormatProvider(IVMProvider provider, DsfSession session) {
		return new ElementNumberFormatProvider(provider, session);
	}

	@Override
	public boolean supportFormat(IVMContext context) {
		return fElementFormatProvider.supportFormat(context);
	}

	@Override
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput, TreePath elementPath,
			DataRequestMonitor<String> rm) {
		fElementFormatProvider.getActiveFormat(context, node, viewerInput, elementPath, rm);
	}

	@Override
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput,
			TreePath[] elementPaths, String format) {
		fElementFormatProvider.setActiveFormat(context, node, viewerInput, elementPaths, format);
	}
}
