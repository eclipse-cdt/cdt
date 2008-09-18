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
package org.eclipse.dd.dsf.ui.viewmodel.update;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMModelProxyExtension;
import org.eclipse.dd.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.VMHasChildrenUpdate;
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

@SuppressWarnings("restriction")
class MultiLevelUpdateHandler extends DataRequestMonitor<List<Object>> {
    private static final class UpdateLevel {
    	private final List<Object> fChildren;
    	private final TreePath fPath;
    	private int fChildIndex;
    	private UpdateLevel(TreePath path, List<Object> children) {
    		fPath = path;
    		fChildren = children;
    		fChildIndex = 0;
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
	private int fHighIndex = Integer.MAX_VALUE;
	private int fPendingUpdates;
	
	public MultiLevelUpdateHandler(Executor executor, 
			IVMModelProxyExtension modelProxy, 
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
		if (fIndex > fHighIndex) {
			fStack.clear();
		}
		if (fStack.isEmpty()) {
			fRequestMonitor.setDoneCount(fPendingUpdates);
			super.done();
			return;
		}
		UpdateLevel current = fStack.peek();

		TreePath path = current.fPath;
		Object data = current.nextChild();
		path = path.createChildPath(data);
		
		if (current.isDone()) {
			fStack.pop();
		}
		if (fIndex >= fLowIndex && fIndex <= fHighIndex) {
    		if(data instanceof IAdaptable) {
    			IElementLabelProvider labelProvider = (IElementLabelProvider) ((IAdaptable)data).getAdapter(IElementLabelProvider.class);
    			if (labelProvider != null) {
    				++fPendingUpdates;
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
				fContentProvider.update(new IChildrenUpdate[] {
						new VMChildrenUpdate(path, fViewerInput, fPresentationContext, -1, -1, this)
				});
				return;
			} else {
				// update also the hasChildren flag
				++fPendingUpdates;
				fContentProvider.update(new IHasChildrenUpdate[] {
						new VMHasChildrenUpdate(path, fViewerInput, fPresentationContext, new DataRequestMonitor<Boolean>(fExecutor, fRequestMonitor))
				});
			}
		}
		processNext();
	}
}
