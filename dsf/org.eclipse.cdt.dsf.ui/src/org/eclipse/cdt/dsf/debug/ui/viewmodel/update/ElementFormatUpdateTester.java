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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import java.util.Collection;
import java.util.Set;

import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTesterExtension;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.jface.viewers.TreePath;

/**
 * An update tester for element format event (ElementFormatEvent). This tester
 * flush partial properties for elements that are affected by a change of
 * element format.
 * 
 * @since 2.2
 */
public class ElementFormatUpdateTester implements IElementUpdateTesterExtension {
	protected Set<String> propertiesWithPrefixes;
	protected ElementFormatEvent formatEvent;

	public ElementFormatUpdateTester(ElementFormatEvent event, Set<String> propertiesWithPrefixes) {
		formatEvent = event;
		this.propertiesWithPrefixes = propertiesWithPrefixes;
	}

	@Override
	public int getUpdateFlags(Object viewerInput, TreePath path) {
		Set<Object> elements = formatEvent.getElements();
		if (elements.contains(viewerInput)) {
			return IVMUpdatePolicy.FLUSH_PARTIAL_PROPERTIES;
		}
		int applyDepth = formatEvent.getApplyDepth();
		if (applyDepth == -1) {
			for (int i = 0; i < path.getSegmentCount(); i++) {
				if (elements.contains(path.getSegment(i))) {
					return IVMUpdatePolicy.FLUSH_PARTIAL_PROPERTIES;
				}
			}
		} else if (applyDepth >= 1) {
			int start = path.getSegmentCount() - applyDepth;
			if (start < 0)
				start = 0;
			for (int i = start; i < path.getSegmentCount(); i++) {
				if (elements.contains(path.getSegment(i))) {
					return IVMUpdatePolicy.FLUSH_PARTIAL_PROPERTIES;
				}
			}
		}
		return 0;
	}

	@Override
	public Collection<String> getPropertiesToFlush(Object viewerInput,
			TreePath path, boolean isDirty) {
		return propertiesWithPrefixes;
	}

	@Override
	public boolean includes(IElementUpdateTester tester) {
		if (tester.equals(this)) {
			return true;
		}
		if (tester instanceof ElementFormatUpdateTester) {
			return formatEvent.getElements().containsAll(
					((ElementFormatUpdateTester) tester).formatEvent.getElements())
					&& propertiesWithPrefixes.containsAll(((ElementFormatUpdateTester) tester).propertiesWithPrefixes)
					&& formatEvent.getApplyDepth() == ((ElementFormatUpdateTester) tester).formatEvent.getApplyDepth();
		}
		return false;
	}

	@Override
	public String toString() {
		return "Manual (refresh = false) update tester for an element format event"; //$NON-NLS-1$
	}
}
