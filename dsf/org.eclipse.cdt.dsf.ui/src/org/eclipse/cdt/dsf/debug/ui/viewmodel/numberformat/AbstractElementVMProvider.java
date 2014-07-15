/*****************************************************************
 * Copyright (c) 2012, 2014 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Base class for view model providers that can support individual element formatting.
 * Extending classes should override {@link #supportFormat(IVMContext)} and {@link #getElementKey(IDMContext)}
 * to enable individual element formatting.
 */
public class AbstractElementVMProvider extends AbstractDMVMProvider implements IElementFormatProvider
{
	private static String ELEMENT_FORMAT_PERSISTABLE_PROPERTY = "org.eclipse.cdt.dsf.ui.elementFormatPersistable"; //$NON-NLS-1$

	public AbstractElementVMProvider(AbstractVMAdapter adapter,  IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider#supportFormat(org.eclipse.cdt.dsf.ui.viewmodel.IVMContext)
	 * 
	 * Keep individual element formatting off by default. Extending classes should override to enable for specific contexts.
	 */
	@Override
	public boolean supportFormat(IVMContext context) {
		return false;
	}
	
	/**
	 * Provides a key that will be used to uniquely identify the element for which formatting has been set.
	 * 
	 * @param context The element for which formatting has or is being set.
	 * @return The key to be used to retrieve the element format.
	 */
	protected String getElementKey(IVMContext context) {
		return null;
	}

	@Override
	public void getActiveFormat(IPresentationContext context, IVMNode node, Object viewerInput, TreePath elementPath,
			DataRequestMonitor<String> rm) {

		Object p = context.getProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY);
		String format = null;
		if (p instanceof ElementFormatPersistable) {
			ElementFormatPersistable persistable = (ElementFormatPersistable) p;
			Object x = elementPath.getLastSegment();
			if (x instanceof IVMContext) {
				format = persistable.getFormat(getElementKey((IVMContext)x));
			}
		}

		rm.done(format);
	}

	@Override
	public void setActiveFormat(IPresentationContext context, IVMNode[] node, Object viewerInput, TreePath[] elementPath, String format) {
		Object p = context.getProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY);
		ElementFormatPersistable persistable = null;
		if (p instanceof ElementFormatPersistable) {
			persistable = (ElementFormatPersistable) p;
		} else {
			persistable = new ElementFormatPersistable();
			context.setProperty(ELEMENT_FORMAT_PERSISTABLE_PROPERTY, persistable);
		}

		boolean changed = false;
		for (int i = 0; i < elementPath.length; i++) {
			Object x = elementPath[i].getLastSegment();
			if (x instanceof IVMContext) {
				String key = getElementKey((IVMContext)x);
				if (key != null) {
					persistable.setFormat(key, format);
					changed = true;
				}
			}
		}
		if (changed) {
			baseRefresh();
		}
	}
}
