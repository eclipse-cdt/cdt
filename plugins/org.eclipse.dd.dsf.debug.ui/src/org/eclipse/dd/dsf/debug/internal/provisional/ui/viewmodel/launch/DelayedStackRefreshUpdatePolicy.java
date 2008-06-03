/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.jface.viewers.TreePath;

/**
 * Automatic update strategy specialized for delayed full stack frame updates. The
 * strategy can operate in lazy mode where only the top stack frame is
 * invalidated, while in non-lazy mode everything is invalidated.
 */
public class DelayedStackRefreshUpdatePolicy extends AutomaticUpdatePolicy {

	private static final class DelayedStackRefreshUpdateTester implements IElementUpdateTester {

		private final boolean fLazyMode;
		DelayedStackRefreshUpdateTester(boolean lazyMode) {
			fLazyMode= lazyMode;
		}
		public int getUpdateFlags(Object viewerInput, TreePath path) {
			if (fLazyMode) {
	            Object element = path.getSegmentCount() != 0 ? path.getLastSegment() : viewerInput;
				if (element instanceof IDMVMContext) {
					IDMContext dmc= ((IDMVMContext) element).getDMContext();
					if (dmc instanceof IFrameDMContext) {
						if (((IFrameDMContext) dmc).getLevel() > 0) {
							// mark as dirty only
							return DIRTY;
						} else {
							return FLUSH;
						}
					} else if (dmc instanceof IExecutionDMContext) {
						return DIRTY;
					}
				}
			}
			return FLUSH;
		}

		public boolean includes(IElementUpdateTester tester) {
		    // A non-lazy tester includes a lazy tester, but not vice versa.  
		    // This allows entries that were marked as dirty by a flush with
		    // the lazy mode to be superceded by a non-lazy update which 
		    // actually clears the entries that were marked as dirty.
			if (tester instanceof DelayedStackRefreshUpdateTester) {
			    DelayedStackRefreshUpdateTester sfTester = (DelayedStackRefreshUpdateTester)tester;
			    if (fLazyMode) {
			        return sfTester.fLazyMode;
			    } else {
			        return false;
			    }
			}
			return false;
		}
	}

	public static String DELAYED_UPDATE_POLICY_ID= "org.eclipse.dd.dsf.ui.viewmodel.update.lazyUpdatePolicy"; //$NON-NLS-1$

	private static final DelayedStackRefreshUpdateTester fgLazyUpdateTester= new DelayedStackRefreshUpdateTester(true);
	private static final DelayedStackRefreshUpdateTester fgNonLazyUpdateTester= new DelayedStackRefreshUpdateTester(false);

	@Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
		if (event instanceof ISuspendedDMEvent) {
			return fgLazyUpdateTester;
		} else if (event instanceof FullStackRefreshEvent) {
			return fgNonLazyUpdateTester;
		} else {
			return super.getElementUpdateTester(event);
		}
	}

	/*
	 * @see org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy#getID()
	 */
	@Override
	public String getID() {
		return DELAYED_UPDATE_POLICY_ID;
	}

	/*
	 * @see org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy#getName()
	 */
	@Override
	public String getName() {
		return "Delayed Stack Refresh"; //$NON-NLS-1$
	}

}
