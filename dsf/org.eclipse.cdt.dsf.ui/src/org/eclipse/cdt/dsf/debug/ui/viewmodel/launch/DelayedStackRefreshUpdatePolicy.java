/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.Map;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.UpdatePolicyDecorator;
import org.eclipse.jface.viewers.TreePath;

/**
 * An update strategy decorator specialized for delayed stack frame refresh. The
 * strategy flushes only the cached top stack frame in case of an normal {@link ISuspendedDMEvent},
 * while in in case of a special {@link FullStackRefreshEvent} everything is invalidated.
 * 
 * <p>
 * The underlying base update policy is considered for container contexts only.
 * In other cases the cache data is always flushed.
 * </p>
 * 
 * @since 1.1
 */
public class DelayedStackRefreshUpdatePolicy extends UpdatePolicyDecorator {

	private static final class DelayedStackRefreshUpdateTester implements IElementUpdateTester {

	    private final IElementUpdateTester fBaseTester;
	    
        /** Indicates whether only the top stack frame should be updated */
        private final boolean fLazyStackFrameMode;

		DelayedStackRefreshUpdateTester(IElementUpdateTester baseTester, boolean lazyStackFrameMode) {
		    fBaseTester = baseTester;
		    fLazyStackFrameMode = lazyStackFrameMode;
		}
		public int getUpdateFlags(Object viewerInput, TreePath path) {
            Object element = path.getSegmentCount() != 0 ? path.getLastSegment() : viewerInput;
			if (element instanceof IDMVMContext) {
				IDMContext dmc = ((IDMVMContext) element).getDMContext();
				if (fLazyStackFrameMode) {
					if (dmc instanceof IFrameDMContext) {
						if (((IFrameDMContext) dmc).getLevel() == 0) {
							return FLUSH;
						}
					} else if (dmc instanceof IExecutionDMContext) {
						return fBaseTester.getUpdateFlags(viewerInput, path);
					}
					return DIRTY;
				} else if (dmc instanceof IContainerDMContext) {
					return fBaseTester.getUpdateFlags(viewerInput, path);
				}
			}
			return FLUSH;
		}

		public boolean includes(IElementUpdateTester tester) {
		    // A non-lazy tester includes a lazy tester, but not vice versa.  
		    // This allows entries that were marked as dirty by a flush with
		    // the lazy mode to be superseded by a non-lazy update which 
		    // actually clears the entries that were marked as dirty.
			if (tester instanceof DelayedStackRefreshUpdateTester) {
			    DelayedStackRefreshUpdateTester sfTester = (DelayedStackRefreshUpdateTester)tester;
			    if (fLazyStackFrameMode) {
			    	if (sfTester.fLazyStackFrameMode) {
			    	    return fBaseTester.includes(sfTester.fBaseTester);
			    	}
			    } else {
			    	if (!sfTester.fLazyStackFrameMode) {
			    	    return fBaseTester.includes(sfTester.fBaseTester);
			    	}
			    	// non-lazy includes lazy
			        return true;
			    }
			}
			return false;
		}
		
        @Override
        public String toString() {
            return "Delayed stack refresh (lazy = " + fLazyStackFrameMode + ", base = " + fBaseTester + ") update tester"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

	}

	private static final class ThreadsUpdateTester implements IElementUpdateTester {

        private final IElementUpdateTester fBaseTester;
        
        private final boolean fRefreshAll;
        
        ThreadsUpdateTester(IElementUpdateTester baseTester, boolean refreshAll) {
            fBaseTester = baseTester;
            fRefreshAll = refreshAll;
        }

        public int getUpdateFlags(Object viewerInput, TreePath path) {
            Object element = path.getSegmentCount() != 0 ? path.getLastSegment() : viewerInput;
            
            if (!fRefreshAll && element instanceof IDMVMContext) {
                IDMContext dmc = ((IDMVMContext) element).getDMContext();
                if (dmc instanceof IContainerDMContext) {
                    return fBaseTester.getUpdateFlags(viewerInput, path);
                }
            }
            
            // If the element is not a container or if the flush all flag is set, 
            // always flush it.
            return FLUSH;
        }
        
        public boolean includes(IElementUpdateTester tester) {
            // A refresh-all tester includes a non-refresh-all tester, but not 
            // vice versa. This allows entries that were marked as dirty by 
            // a flush with
            // the non-refresh-all to be superseded by a refresh-all update which 
            // actually clears the entries that were marked as dirty.
            if (tester instanceof ThreadsUpdateTester) {
                ThreadsUpdateTester threadsTester = (ThreadsUpdateTester)tester;
                if (fRefreshAll) {
                    if (threadsTester.fRefreshAll) {
                        return fBaseTester.includes(threadsTester.fBaseTester);
                    }
                    // refresh-all includes the non-refresh-all
                    return true;
                } else {
                    if (!threadsTester.fRefreshAll) {
                        return fBaseTester.includes(threadsTester.fBaseTester);
                    }
                }
            }
            return false;
        }
        
        @Override
        public String toString() {
            return "Threads update tester (base = " + fBaseTester + ") update tester"; //$NON-NLS-1$ //$NON-NLS-2$ 
        }
	}

	
	public DelayedStackRefreshUpdatePolicy(IVMUpdatePolicy base) {
		super(base);
	}

	@Override
	public IElementUpdateTester getElementUpdateTester(Object event) {
		if (event instanceof ISuspendedDMEvent) {
			return new DelayedStackRefreshUpdateTester(getBaseUpdatePolicy().getElementUpdateTester(event), true);
		} else if (event instanceof FullStackRefreshEvent) {
            return new DelayedStackRefreshUpdateTester(getBaseUpdatePolicy().getElementUpdateTester(event), false);
		} else if (event instanceof IExitedDMEvent &&
		           ((IExitedDMEvent)event).getDMContext() instanceof IContainerDMContext) 
		{
            // container exit should always trigger a refresh
            return new ThreadsUpdateTester(super.getElementUpdateTester(event), true);
		} else {
		    return new ThreadsUpdateTester(super.getElementUpdateTester(event), false);
		}
	}
	
	public Object[] getInitialRootElementChildren(Object rootElement) {
	    return null;
	}

    public Map<String, Object> getInitialRootElementProperties(Object rootElement) {
        return null;
    }
}
