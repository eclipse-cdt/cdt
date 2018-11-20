/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - Added support for AbsoluteSourceContainer(159833)
 * Ken Ryall (Nokia) - Added support for CSourceNotFoundElement (167305)
 * Ken Ryall (Nokia) - Option to open disassembly view when no source (81353)
 * James Blackburn (Broadcom Corp.) - Linked Resources / Nested Projects (247948)
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.cdt.debug.internal.core.ListenerList;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * A source lookup participant that searches for C/C++ source code.
 */
public class CSourceLookupParticipant extends AbstractSourceLookupParticipant {

	static class NoSourceElement {
	}

	private static final NoSourceElement gfNoSource = new NoSourceElement();

	private ListenerList fListeners;

	private Map<Object, Object[]> fCachedResults = Collections.synchronizedMap(new HashMap<Object, Object[]>());

	/**
	 * Constructor for CSourceLookupParticipant.
	 */
	public CSourceLookupParticipant() {
		super();
		fListeners = new ListenerList(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
	@Override
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof String) {
			return (String) object;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#findSourceElements(java.lang.Object)
	 */
	@Override
	public Object[] findSourceElements(Object object) throws CoreException {
		// Check the cache
		Object[] results = fCachedResults.get(object);
		if (results != null)
			return results;

		// Workaround for cases when the stack frame doesn't contain the source file name
		String name = null;
		IBreakpoint breakpoint = null;
		if (object instanceof String) {
			name = (String) object;
		}

		// Actually query the source containers for the requested resource
		Object[] foundElements = super.findSourceElements(object);

		// If none found, invoke the absolute path container directly
		if (foundElements.length == 0 && (object instanceof IDebugElement)) {
			// debugger could have resolved it itself and "name" is an absolute path
			if (new File(name).exists()) {
				foundElements = new AbsolutePathSourceContainer().findSourceElements(name);
			} else {
				foundElements = new Object[] { new CSourceNotFoundElement((IDebugElement) object,
						((IDebugElement) object).getLaunch().getLaunchConfiguration(), name) };
			}
		}

		// Source lookup participant order is preserved where possible except for one case:
		//   - If we've stopped at a breakpoint the user has made on an IResource, we definitely want to show
		//     that IResource before others
		if (breakpoint != null && breakpoint.getMarker() != null && breakpoint.getMarker().getResource() != null) {
			IResource breakpointResource = breakpoint.getMarker().getResource();
			for (int i = 0; i < foundElements.length; i++) {
				if (foundElements[i].equals(breakpointResource)) {
					Object temp = foundElements[0];
					foundElements[0] = foundElements[i];
					foundElements[i] = temp;
					break;
				}
			}
		}
		fCachedResults.put(object, foundElements);
		return foundElements;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#dispose()
	 */
	@Override
	public void dispose() {
		fListeners.removeAll();
		super.dispose();
	}

	public void addSourceLookupChangeListener(ISourceLookupChangeListener listener) {
		fListeners.add(listener);
	}

	public void removeSourceLookupChangeListener(ISourceLookupChangeListener listener) {
		fListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.core.sourcelookup.ISourceLookupDirector)
	 */
	@Override
	public void sourceContainersChanged(ISourceLookupDirector director) {
		// clear the cache
		fCachedResults.clear();

		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((ISourceLookupChangeListener) listeners[i]).sourceContainersChanged(director);
		super.sourceContainersChanged(director);
	}
}
