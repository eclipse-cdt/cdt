/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredCWorkbenchAdapter extends CWorkbenchAdapter
    implements IDeferredWorkbenchAdapter {

	private static boolean fSerializeFetching = false;
	private static boolean fBatchFetchedChildren = true;
    
	final ISchedulingRule mutexRule = new ISchedulingRule() {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == mutexRule;
		}
		public boolean contains(ISchedulingRule rule) {
			return rule == mutexRule;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object,
	 *           org.eclipse.jface.progress.IElementCollector,
	 *           org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
	    if (object instanceof IParent) {
			if (fBatchFetchedChildren) {
				Object[] children = getChildren(object);
				if (children.length > 0)
				    collector.add(children, monitor);
			} else {
			    // TODO right now there is no advantage to this
			    // over the batched case above, but in the future
			    // we could have another method of progressively
			    // iterating over an ICElement's children
				Object[] children = getChildren(object);
				for (int i = 0; i < children.length; i++) {
					if (monitor.isCanceled()) {
						return;
					}
					collector.add(children[i], monitor);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(final Object object) {
	    if (fSerializeFetching) {
	        // only one ICElement parent can fetch children at a time
	        return mutexRule;
	    } 
	    // allow several ICElement parents to fetch children concurrently
	    return null;
	}
}
