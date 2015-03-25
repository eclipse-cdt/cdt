/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *****************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import java.util.HashSet;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IElementFormatProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.ElementFormatEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Test view model provider that supports element format provider interface.
 * This class is used in test cases and can be extended to support other
 * optional interfaces
 */
class TestElementFormatVMProvider extends TestModelCachingVMProvider implements IElementFormatProvider {
	public int elementFormatApplyDepth = 1;
	String myPersistId = "org.eclipse.cdt.tests.dsf.vm.testElementFormatVMProvider";

	public TestElementFormatVMProvider(AbstractVMAdapter adapter,
			IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

	@Override
	public void getActiveFormat(IPresentationContext context, IVMNode node,
			Object viewerInput, TreePath elementPath, DataRequestMonitor<String> rm) {
		Object p = context.getProperty(myPersistId);
		if (p instanceof TestPersistable == false) {
			rm.setData(null);
			rm.done();
			return;
		}
		TestPersistable persistable = (TestPersistable) p;
		int end = elementPath.getSegmentCount();
		int start = elementPath.getSegmentCount() - 1;
		if (elementFormatApplyDepth == -1) {
			start = 0;
		} else if (elementFormatApplyDepth >= 1) {
			start = elementPath.getSegmentCount() - elementFormatApplyDepth;
		}
		if (start < 0)
			start = 0;
		for (int i = end; --i >= start;) {
			Object x = elementPath.getSegment(i);
			if (x instanceof TestElementVMContext) {
				String s = ((TestElementVMContext) x).getElement().getID();
				String format = persistable.getFormat(s);
				if (format != null) {
					rm.setData(format);
					rm.done();
					return;
				}
			}
		}
		rm.setData(null);
		rm.done();
	}

	@Override
	public void setActiveFormat(IPresentationContext context, IVMNode[] node,
			Object viewerInput, TreePath[] elementPath, String format) {
		Object p = context.getProperty(myPersistId);
		TestPersistable persistable = null;
		if (p instanceof TestPersistable) {
			persistable = (TestPersistable) p;
		} else {
			persistable = new TestPersistable();
			context.setProperty(myPersistId, persistable);
		}
		HashSet<Object> changed = new HashSet<Object>(elementPath.length);
		for (int i = 0; i < elementPath.length; i++) {
			Object x = elementPath[i].getLastSegment();
			if (x instanceof TestElementVMContext) {
				String s = ((TestElementVMContext) x).getElement().getID();
				persistable.setFormat(s, format);
				changed.add(x);
			}
		}
		if (changed.size() > 0) {
//			this.refresh();
			handleEvent(new ElementFormatEvent(changed, elementFormatApplyDepth));
		}
	}

	@Override
	public boolean supportFormat(IVMContext context) {
		return true;
	}
}
