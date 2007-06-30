/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.ui.viewmodel.update;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

public abstract class VMCache  
{
	protected HashMap<Object, Integer> fChildrenCounts = new HashMap<Object, Integer>();
	
	class ChildData
	{
		Object child;
		int offset;
		
		public ChildData(Object child, int offset)
		{
			this.child = child;
			this.offset = offset;
		}
	}
	
	protected HashMap<Object, ChildData[]> fChildren = new HashMap<Object, ChildData[]>();
	
	protected HashMap<Object, Boolean> fHasChildren = new HashMap<Object, Boolean>();
	
	protected HashMap<IDMContext<?>, IDMData> fData = new HashMap<IDMContext<?>, IDMData>();
	
	protected HashMap<IDMContext<?>, IDMData> fDataArchive = fData;
	
	protected void flush(boolean archive)
	{
		if(archive)
			fDataArchive = fData;
		fData = new HashMap<IDMContext<?>, IDMData>();
		fChildrenCounts.clear();
		fChildren.clear();
		fChildrenCounts.clear();
	}
	
	protected boolean useCache()
	{
		return true;
	}
	
	public IHasChildrenUpdate[] update(IHasChildrenUpdate[] updates) {
    	Vector<IHasChildrenUpdate> missVector = new Vector<IHasChildrenUpdate>();
    	for(IHasChildrenUpdate update : updates)
    	{
    		if(fHasChildren.containsKey(update.getElement()) && useCache())
    		{
    			update.setHasChilren(fHasChildren.get(update.getElement()).booleanValue());
    			update.done();
    		}
    		else
    		{
    			missVector.addElement(update);
    		}
    	}
    	
    	updates = new IHasChildrenUpdate[missVector.size()];
    	for(int i = 0; i < updates.length; i++)
    	{
    		final IHasChildrenUpdate update = missVector.elementAt(i);
    		updates[i] = new IHasChildrenUpdate()
    		{
    			private boolean fIsHasChildren;
    			
    			public void setHasChilren(boolean hasChildren) {
    				fIsHasChildren = hasChildren;
					update.setHasChilren(hasChildren);
				}
    			
				public void cancel() {
					update.cancel();
				}

				public void done() {
					fHasChildren.put(getElement(), Boolean.valueOf(fIsHasChildren));
					update.done();
				}

				public IStatus getStatus() {
					return update.getStatus();
				}

				public boolean isCanceled() {
					return update.isCanceled();
				}

				public void setStatus(IStatus status) {
					update.setStatus(status);
				}

				public Object getElement() {
					return update.getElement();
				}

				public TreePath getElementPath() {
					return update.getElementPath();
				}

				public IPresentationContext getPresentationContext() {
					return update.getPresentationContext();
				}
    			
    		};
    	}
    	
    	return updates;
    }
    
    public IChildrenCountUpdate[] update(IChildrenCountUpdate[] updates) 
    {
    	Vector<IChildrenCountUpdate> missVector = new Vector<IChildrenCountUpdate>();
    	for(IChildrenCountUpdate update : updates)
    	{
    		if(fChildrenCounts.containsKey(update.getElement()) && useCache())
    		{
    			update.setChildCount(fChildrenCounts.get(update.getElement()));
    			update.done();
    		}
    		else
    		{
    			missVector.addElement(update);
    		}
    	}
    	
    	updates = new IChildrenCountUpdate[missVector.size()];
    	for(int i = 0; i < updates.length; i++)
    	{
    		final IChildrenCountUpdate update = missVector.elementAt(i);
    		updates[i] = new IChildrenCountUpdate()
    		{
    			private int fChildCount;
    			
				public void cancel() {
					update.cancel();
				}

				public void done() {
					fChildrenCounts.put(getElement(), fChildCount);
					update.done();
				}

				public IStatus getStatus() {
					return update.getStatus();
				}

				public boolean isCanceled() {
					return update.isCanceled();
				}

				public void setStatus(IStatus status) {
					update.setStatus(status);
				}

				public void setChildCount(int numChildren) {
					fChildCount = numChildren;
					update.setChildCount(numChildren);
				}

				public Object getElement() {
					return update.getElement();
				}

				public TreePath getElementPath() {
					return update.getElementPath();
				}

				public IPresentationContext getPresentationContext() {
					return update.getPresentationContext();
				}
    			
    		};
    	}
    	
    	return updates;
    }
    
    public IChildrenUpdate[] update(IChildrenUpdate[] updates) {
    	Vector<IChildrenUpdate> missVector = new Vector<IChildrenUpdate>();
    	for(IChildrenUpdate update : updates)
    	{
    		if(fChildren.containsKey(update.getElement()) && useCache())
    		{
    			ChildData childData[] = fChildren.get(update.getElement());
    			for(ChildData data : childData)
    				update.setChild(data.child, data.offset);
    			update.done();
    		}
    		else
    		{
    			missVector.addElement(update);
    		}
    	}
    	
    	updates = new IChildrenUpdate[missVector.size()];
    	for(int i = 0; i < updates.length; i++)
    	{
    		final IChildrenUpdate update = missVector.elementAt(i);
    		updates[i] = new IChildrenUpdate()
    		{
    			Vector<ChildData> fChilds = new Vector<ChildData>();
    			
    			public int getLength() {
					return update.getLength();
				}

				public int getOffset() {
					return update.getOffset();
				}

				public void setChild(Object child, int offset) {
					fChilds.addElement(new ChildData(child, offset));
					update.setChild(child, offset);
				}
    			
				public void cancel() {
					update.cancel();
				}

				public void done() {
					// FIXME synchronize with events?
					fChildren.put(getElement(), fChilds.toArray(new ChildData[fChilds.size()]));
					update.done();
				}

				public IStatus getStatus() {
					return update.getStatus();
				}

				public boolean isCanceled() {
					return update.isCanceled();
				}

				public void setStatus(IStatus status) {
					update.setStatus(status);
				}

				public Object getElement() {
					return update.getElement();
				}

				public TreePath getElementPath() {
					return update.getElementPath();
				}

				public IPresentationContext getPresentationContext() {
					return update.getPresentationContext();
				}
    			
    		};
    	}
    	
    	return updates;
    }
    
    @SuppressWarnings("unchecked") 
    public void getModelData(IDMService service, final IDMContext dmc, final DataRequestMonitor rm, DsfExecutor executor)
    { 
    	if(fData.containsKey(dmc) && useCache())
    	{ 
    		rm.setData(  fData.get(dmc));
    		rm.done();
    	}
    	else
    	{
	    	service.getModelData(dmc, 
	    		new DataRequestMonitor<IDMData>(executor, null) {
		            @Override
		            public void handleCompleted() {
		            	fData.put(dmc, getData());
		            	rm.setData(getData());
		            	rm.done();
		            }
		        }		
	    	);
    	}
    }
    
    @SuppressWarnings("unchecked") 
    public IDMData getArchivedModelData(IDMContext dmc)
    {
    	return fDataArchive.get(dmc);
    }
    
    public abstract void handleEvent(IDMEvent event);

}


