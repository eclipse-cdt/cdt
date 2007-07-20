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
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.ui.viewmodel.VMElementsCountUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.VMElementsUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.VMHasElementsUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;

@SuppressWarnings("restriction")
public abstract class VMCache  
{
	protected Executor fExecutor = new DefaultDsfExecutor();
	
	protected HashMap<Object, Integer> fChildrenCounts = new HashMap<Object, Integer>();
	
	protected HashMap<Object, HashMap<Integer,Object>> fChildren = new HashMap<Object, HashMap<Integer,Object>>();
	
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
		fHasChildren.clear();
	}
	
	protected boolean isCacheReadEnabled()
	{
		return true;
	}
	
	protected boolean isCacheWriteEnabled()
	{
		return true;
	}
	
	@SuppressWarnings("restriction")
	public IHasChildrenUpdate[] update(IHasChildrenUpdate[] updates) {
		if(!isCacheReadEnabled())
			return updates;
		
    	Vector<IHasChildrenUpdate> missVector = new Vector<IHasChildrenUpdate>();
    	for(IHasChildrenUpdate update : updates)
    	{
    		if(fHasChildren.containsKey(update.getElement()) && isCacheReadEnabled())
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
    		updates[i] = new VMHasElementsUpdate(update, new DataRequestMonitor<Boolean>(fExecutor, null)
			{
    			@Override
    			protected void handleCompleted()
    			{
                    if(getStatus().isOK())
                    {
                        if(isCacheWriteEnabled())
                            fHasChildren.put(update.getElement(), this.getData());
                        update.setHasChilren(getData());
                    }
    				update.done();
    			}
			});
    	}
    	
    	return updates;
    }
    
	@SuppressWarnings("restriction")
    public IChildrenCountUpdate[] update(IChildrenCountUpdate[] updates) 
    {
		if(!isCacheReadEnabled())
			return updates;
		
    	Vector<IChildrenCountUpdate> missVector = new Vector<IChildrenCountUpdate>();
    	for(IChildrenCountUpdate update : updates)
    	{
    		if(fChildrenCounts.containsKey(update.getElement()) && isCacheReadEnabled())
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
    		updates[i] = new VMElementsCountUpdate(update, new DataRequestMonitor<Integer>(fExecutor, null)
			{
    			@Override
				protected void handleCompleted() {
    				if(getStatus().isOK())
    				{
    					if(isCacheWriteEnabled())
        					fChildrenCounts.put(update.getElement(), this.getData());
        				update.setChildCount(this.getData());
    				}
    				update.done();
				}
			});
    	}
    	
    	return updates;
    }
    
	@SuppressWarnings("restriction")
    public IChildrenUpdate[] update(IChildrenUpdate[] updates) {
		if(!isCacheReadEnabled())
			return updates;
		
    	Vector<IChildrenUpdate> updatesEntirelyMissingFromCache = new Vector<IChildrenUpdate>();
    	for(final IChildrenUpdate update : updates)
    	{
    		if(fChildren.containsKey(update.getElement()) && isCacheReadEnabled())
    		{
    			Vector<Integer> childrenMissingFromCache = new Vector<Integer>();
    			for(int i = update.getOffset(); i < update.getOffset() + update.getLength(); i++)
    				childrenMissingFromCache.addElement(i);
    			childrenMissingFromCache.removeAll(fChildren.get(update.getElement()).keySet());
    			
    			if(childrenMissingFromCache.size() > 0)
    			{
    				// perform a partial update; we only have some of the children of the update request
    			
    				final HashMap<DataRequestMonitor<List<Object>>,IChildrenUpdate> associationsRequestMonitorToChildUpdate
    					= new HashMap<DataRequestMonitor<List<Object>>,IChildrenUpdate>();
    				
	    			final MultiRequestMonitor<DataRequestMonitor<List<Object>>> childrenMultiRequestMon = 
	                    new MultiRequestMonitor<DataRequestMonitor<List<Object>>>(fExecutor, null) { 
	                        @Override
	                        protected void handleCompleted() {
	                            // Status is OK, only if all request monitors are OK. 
	                            if (getStatus().isOK()) 
	                            { 
	                                for (DataRequestMonitor<List<Object>> monitor : getRequestMonitors()) 
	                                {
                                		int offset = associationsRequestMonitorToChildUpdate.get(monitor).getOffset();
                                		for(Object child : monitor.getData())
                                			update.setChild(child, offset++);
	                                }
	                            } else {
	                                update.setStatus(getStatus());
	                            }
	                            update.done();
	                        }
	                    };
	                    
	    			while(childrenMissingFromCache.size() > 0)
	    			{
	    				int offset = childrenMissingFromCache.elementAt(0);
	    				childrenMissingFromCache.removeElementAt(0);
	    				int length = 1;
	    				while(childrenMissingFromCache.size() > 0 && childrenMissingFromCache.elementAt(0) == offset + length)
	    				{
	    					length++;
	    					childrenMissingFromCache.removeElementAt(0);
	    				}
	    				
	    				DataRequestMonitor<List<Object>> partialUpdateMonitor = new DataRequestMonitor<List<Object>>(fExecutor, null)
						{
			    			@Override
			    			protected void handleCompleted()
			    			{
			    				
			    				childrenMultiRequestMon.requestMonitorDone(this);
			    			}
						};
						
	    				final IChildrenUpdate partialUpdate = new VMElementsUpdate(update, offset, length,
	    						childrenMultiRequestMon.add(partialUpdateMonitor));
	    				
	    				associationsRequestMonitorToChildUpdate.put(partialUpdateMonitor, partialUpdate);
								
	    				updatesEntirelyMissingFromCache.add(partialUpdate);
	    				
	    			}
    			}
    			else
    			{
    				// we have all of the children in cache; return from cache
    				for(int position = update.getOffset(); position < update.getOffset() + update.getLength(); position++)
    					update.setChild(fChildren.get(update.getElement()).get(position), position);
    				update.done();
    			}
    		}
    		else
    		{
    			updatesEntirelyMissingFromCache.addElement(update);
    		}
    	}
    	
    	updates = new IChildrenUpdate[updatesEntirelyMissingFromCache.size()];
    	for(int i = 0; i < updates.length; i++)
    	{
    		final IChildrenUpdate update = updatesEntirelyMissingFromCache.elementAt(i);
    		updates[i] = new VMElementsUpdate(update, update.getOffset(), update.getLength(),
    			new DataRequestMonitor<List<Object>>(fExecutor, null)
			{
    			@Override
    			protected void handleCompleted()
    			{
    				if(getStatus().isOK())
    				{
	    				for(int j = 0; j < update.getLength(); j++)
	    				{
	    					if(isCacheWriteEnabled())
	    					{
		    					if(!fChildren.containsKey(update.getElement()))
		    						fChildren.put(update.getElement(), new HashMap<Integer,Object>());
		    					
		    					fChildren.get(update.getElement()).put(update.getOffset() + j, getData().get(j));
	    					}
	    					
	    					update.setChild(getData().get(j), update.getOffset() + j);
	    				}
    				}
    				update.done();
				}
			});
    	}

    	return updates;
    }
    
    @SuppressWarnings("unchecked") 
    @ConfinedToDsfExecutor("DsfSession.getSession(dmc.getSessionId()).getExecutor()")
    public void getModelData(IDMService service, final IDMContext dmc, final DataRequestMonitor rm, DsfExecutor executor)
    { 
    	if(fData.containsKey(dmc) && isCacheReadEnabled())
    	{ 
    		rm.setData(  fData.get(dmc));
    		rm.done();
    	}
    	else
    	{
	    	service.getModelData(dmc, 
	    		new DataRequestMonitor<IDMData>(executor, null) {
		            @Override
					protected void handleCompleted() {
		            	if(getStatus().isOK())
		            	{
		            		if(isCacheWriteEnabled())
			            		fData.put(dmc, getData());
			            	rm.setData(getData());
		            	}
	    				rm.done();
					}

					@Override
					public synchronized void setCanceled(boolean canceled) {
						rm.setCanceled(canceled);
						super.setCanceled(canceled);
					}

					@Override
					public void setMultiStatus(String pluginId, int code,
							String message, IStatus subStatus) {
						rm.setMultiStatus(pluginId, code, message, subStatus);
						super.setMultiStatus(pluginId, code, message, subStatus);
					}

					@Override
					public synchronized void setStatus(IStatus status) {
						rm.setStatus(status);
						super.setStatus(status);
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
    
    @SuppressWarnings("unchecked")
    public abstract void handleEvent(IDMEvent event);

}


