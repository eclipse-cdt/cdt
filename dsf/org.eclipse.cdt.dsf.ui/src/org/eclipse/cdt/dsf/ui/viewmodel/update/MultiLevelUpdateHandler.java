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
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMHasChildrenUpdate;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 1.1
 */
class MultiLevelUpdateHandler extends DataRequestMonitor<List<Object>> {
	
	private static final boolean DEBUG = Boolean.parseBoolean(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/atomicUpdate")); //$NON-NLS-1$ //;

    private static final class UpdateLevel {
    	private final List<Object> fChildren;
    	private final TreePath fPath;
    	private int fChildIndex;
    	private UpdateLevel(TreePath path, List<Object> children) {
    		fPath = path;
    		fChildren = children;
    		fChildIndex = 0;
    		assert !isDone();
    	}
    	private boolean isDone() {
    		return fChildIndex == fChildren.size();
    	}
    	private Object nextChild() {
    		if (isDone()) {
    			return null;
    		}
    		return fChildren.get(fChildIndex++);
    	}
    }
	private final class DummyLabelUpdate implements ILabelUpdate {
		private final RequestMonitor fMonitor;
		private final Object fData;
		private final TreePath fPath;
	
		private DummyLabelUpdate(Object data, TreePath path, RequestMonitor rm) {
			fMonitor= rm;
			fData= data;
			fPath= path;
		}
		public Object getElement() {
			return fData;
		}
		public TreePath getElementPath() {
			return fPath.createChildPath(fData);
		}
		public IPresentationContext getPresentationContext() {
			return fPresentationContext;
		}
		public Object getViewerInput() {
			return fViewerInput;
		}
		public void cancel() { }
		public IStatus getStatus() {
			return null;
		}
		public boolean isCanceled() {
			return false;
		}
		public void setStatus(IStatus status) { }
		public String[] getColumnIds() {
			return fColumns;
		}
		public void setBackground(RGB arg0, int arg1) { }
		public void setFontData(FontData arg0, int arg1) { }
		public void setForeground(RGB arg0, int arg1) { }
		public void setImageDescriptor(ImageDescriptor arg0, int arg1) { }
		public void setLabel(String arg0, int arg1) {
		}
		public void done() {
			fMonitor.done();
		}
	}
	
	private final Executor fExecutor;
	private final IElementContentProvider fContentProvider;
	private final IPresentationContext fPresentationContext;
	private final String[] fColumns;
	private final Viewer fViewer;
	private final Object fViewerInput;
	private final Stack<UpdateLevel> fStack = new Stack<UpdateLevel>();
	private final CountingRequestMonitor fRequestMonitor;

	private int fIndex = 0;
	private TreePath fCurrentPath;
	private int fLowIndex = 0;
	private int fHighIndex = Integer.MAX_VALUE - 1;
	private int fPendingUpdates;
	
	public MultiLevelUpdateHandler(Executor executor, 
			IVMModelProxy modelProxy, 
			IPresentationContext presentationContext, 
			IElementContentProvider contentProvider,
			RequestMonitor parentRequestMonitor) {
		super(executor, null);
		fExecutor = executor;
		fViewer = modelProxy.getViewer();
		fViewerInput = modelProxy.getViewerInput();
		fCurrentPath = modelProxy.getRootPath();
		fPresentationContext = presentationContext;
		fColumns = presentationContext.getColumns();
		fContentProvider = contentProvider;
		
		fRequestMonitor = new CountingRequestMonitor(fExecutor, parentRequestMonitor);
	}
	void startUpdate() {
		if (DEBUG) System.out.println("[MultiLevelUpdateHandler] startUpdate " + fLowIndex + '-' + fHighIndex); //$NON-NLS-1$
		fContentProvider.update(new IChildrenUpdate[] {
				new VMChildrenUpdate(fCurrentPath, fViewerInput, fPresentationContext, -1, -1, this)
		});
	}
	void setRange(int low, int high) {
		fLowIndex = low;
		fHighIndex = high;
	}
	boolean isDone() {
		return fStack.isEmpty();
	}
	@Override
	public synchronized void done() {
        try {
            fExecutor.execute(new DsfRunnable() {
                public void run() {
            		final List<Object> data= getData();
            		if (data != null && !data.isEmpty()) {
        				if (DEBUG) System.out.println("[MultiLevelUpdateHandler] gotChildUpdate " + data.size()); //$NON-NLS-1$
						fStack.push(new UpdateLevel(fCurrentPath, data));
            		}
                	processNext();
                }
                @Override
                public String toString() {
                    return "Completed: " + MultiLevelUpdateHandler.this.toString(); //$NON-NLS-1$
                }
            });
        } catch (RejectedExecutionException e) {
            handleRejectedExecutionException();
        }
	}
	protected void processNext() {
		while (true) {
			if (fIndex > fHighIndex) {
				fStack.clear();
			}
			if (fStack.isEmpty()) {
				fRequestMonitor.setDoneCount(fPendingUpdates);
				super.done();
				return;
			}
			UpdateLevel current = fStack.peek();
			assert !current.isDone();
	
			TreePath path = current.fPath;
			Object data = current.nextChild();
			if (current.isDone()) {
				fStack.pop();
			}
			if (data == null) {
				// consider null children - http://bugs.eclipse.org/250309
				++fIndex;
				continue;
			}
			path = path.createChildPath(data);
			
			if (fIndex >= fLowIndex && fIndex <= fHighIndex) {
	    		if(data instanceof IAdaptable) {
	    			IElementLabelProvider labelProvider = (IElementLabelProvider) ((IAdaptable)data).getAdapter(IElementLabelProvider.class);
	    			if (labelProvider != null) {
	    				++fPendingUpdates;
	    				if (DEBUG) System.out.println("[MultiLevelUpdateHandler] labelUpdate " + data); //$NON-NLS-1$
	        			labelProvider.update(new ILabelUpdate[] {
	        					new DummyLabelUpdate(data, path, fRequestMonitor)
	        			});
	    			}
	    		}
			}
			fIndex++;
			if (fViewer instanceof TreeViewer) {
				TreeViewer treeViewer = (TreeViewer) fViewer;
				if (treeViewer.getExpandedState(data)) {
					fCurrentPath = path;
    				if (DEBUG) System.out.println("[MultiLevelUpdateHandler] childrenUpdate " + data); //$NON-NLS-1$
					fContentProvider.update(new IChildrenUpdate[] {
							new VMChildrenUpdate(path, fViewerInput, fPresentationContext, -1, -1, this)
					});
					return;
				} else if (fIndex >= fLowIndex) {
					// update also the hasChildren flag
					++fPendingUpdates;
    				if (DEBUG) System.out.println("[MultiLevelUpdateHandler] hasChildUpdate " + data); //$NON-NLS-1$
					fContentProvider.update(new IHasChildrenUpdate[] {
							new VMHasChildrenUpdate(path, fViewerInput, fPresentationContext, new DataRequestMonitor<Boolean>(fExecutor, fRequestMonitor))
					});
				}
			}
		}
	}
}
