/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/** 
 * Base implementation for View Model Adapters.  The implementation uses
 * its own single-thread executor for communicating with providers and 
 * layout nodes. 
 */
@ThreadSafe
@SuppressWarnings("restriction")
abstract public class AbstractVMAdapter implements IVMAdapter
{
 
	/**
	 * Interface for a viewer update which can be "monitored".
	 */
	interface IMonitoredUpdate extends IViewerUpdate {
 		boolean isDone();
 		void setMonitor(RequestMonitor monitor);
	}

	/**
	 * Wraps an IViewerUpdate to add a request monitor.
	 */
	abstract static class MonitoredUpdate implements IMonitoredUpdate {

		protected IViewerUpdate fDelegate;
		private boolean fIsDone;
		private RequestMonitor fMonitor;

		MonitoredUpdate(IViewerUpdate update) {
			fDelegate = update;
		}

		public boolean isDone() {
			return fIsDone;
		}

		public void setMonitor(RequestMonitor monitor) {
			fMonitor = monitor;
			if (fIsDone) {
				monitor.done();
			}
		}

		public Object getElement() {
			return fDelegate.getElement();
		}

		public TreePath getElementPath() {
			return fDelegate.getElementPath();
		}

		public IPresentationContext getPresentationContext() {
			return fDelegate.getPresentationContext();
		}

		public Object getViewerInput() {
			return fDelegate.getViewerInput();
		}

		public void cancel() {
			fDelegate.cancel();
			if (!fIsDone) {
				fIsDone = true;
				if (fMonitor != null) {
					fMonitor.done();
				}
			}
		}

		public void done() {
			fDelegate.done();
			if (!fIsDone) {
				fIsDone = true;
				if (fMonitor != null) {
					fMonitor.done();
				}
			}
		}

		public IStatus getStatus() {
			return fDelegate.getStatus();
		}

		public boolean isCanceled() {
			return fDelegate.isCanceled();
		}

		public void setStatus(IStatus status) {
			fDelegate.setStatus(status);
		}

	}

	static class MonitoredChildrenUpdate extends MonitoredUpdate implements IChildrenUpdate {
		public MonitoredChildrenUpdate(IChildrenUpdate update) {
			super(update);
		}

		public int getLength() {
			return ((IChildrenUpdate)fDelegate).getLength();
		}

		public int getOffset() {
			return ((IChildrenUpdate)fDelegate).getOffset();
		}

		public void setChild(Object child, int offset) {
			((IChildrenUpdate)fDelegate).setChild(child, offset);
		}
		
	}

	static class MonitoredHasChildrenUpdate extends MonitoredUpdate implements IHasChildrenUpdate {
		public MonitoredHasChildrenUpdate(IHasChildrenUpdate update) {
			super(update);
		}

		public void setHasChilren(boolean hasChildren) {
			((IHasChildrenUpdate)fDelegate).setHasChilren(hasChildren);
		}

	}

	static class MonitoredChildrenCountUpdate extends MonitoredUpdate implements IChildrenCountUpdate {
		public MonitoredChildrenCountUpdate(IChildrenCountUpdate update) {
			super(update);
		}

		public void setChildCount(int numChildren) {
			((IChildrenCountUpdate)fDelegate).setChildCount(numChildren);
		}

	}


	private boolean fDisposed;

    private final Map<IPresentationContext, IVMProvider> fViewModelProviders = 
        Collections.synchronizedMap( new HashMap<IPresentationContext, IVMProvider>() );

	/**
	 * List of IViewerUpdates pending after processing an event.
	 */
	private final List<IMonitoredUpdate> fPendingUpdates = new ArrayList<IMonitoredUpdate>();

    /**
     * Constructor for the View Model session.  It is tempting to have the 
     * adapter register itself here with the session as the model adapter, but
     * that would mean that the adapter might get accessed on another thread
     * even before the deriving class is fully constructed.  So it it better
     * to have the owner of this object register it with the session.
     * @param session
     */
    public AbstractVMAdapter() {
    }    

    @ThreadSafe
    public IVMProvider getVMProvider(IPresentationContext context) {
        synchronized(fViewModelProviders) {
            if (fDisposed) return null;

            IVMProvider provider = fViewModelProviders.get(context);
            if (provider == null) {
                provider = createViewModelProvider(context);
                if (provider != null) {
                    fViewModelProviders.put(context, provider);
                }
            }
            return provider;
        }
    }

    /**
     * Enumerate the VM providers.
     * @return An instance of {@link Iterable<IVMProvider>}
     * 
	 * @since 1.1
	 */
    protected Iterable<IVMProvider> getVMProviderIterable() {
    	return fViewModelProviders.values();
    }

    public void dispose() {
        IVMProvider[] providers = new IVMProvider[0]; 
        synchronized(fViewModelProviders) {
            providers = fViewModelProviders.values().toArray(new IVMProvider[fViewModelProviders.size()]);
            fViewModelProviders.clear();            
            fDisposed = true;
        }
        
        for (final IVMProvider provider : providers) {
            try {
                provider.getExecutor().execute(new Runnable() {
                    public void run() {
                        provider.dispose();
                    }
                });
            } catch (RejectedExecutionException e) {
                // Not much we can do at this point.
            }            
        }
    }
    
    /**
	 * @return whether this VM adapter is disposed.
	 * 
     * @since 1.1
	 */
	public boolean isDisposed() {
		return fDisposed;
	}

    public void update(IHasChildrenUpdate[] updates) {
    	handleUpdate(updates);
    }
    
    public void update(IChildrenCountUpdate[] updates) {
    	handleUpdate(updates);
    }
    
    public void update(final IChildrenUpdate[] updates) {
    	handleUpdate(updates);
    }
    
    private void handleUpdate(IViewerUpdate[] updates) {
    	updates = wrapUpdates(updates);
        IVMProvider provider = getVMProvider(updates[0].getPresentationContext());
        if (provider != null) {
            updateProvider(provider, updates);
        } else {
            for (IViewerUpdate update : updates) {
                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, 
                    "No model provider for update " + update, null)); //$NON-NLS-1$
                update.done();
            }
        }
    }

    private void updateProvider(final IVMProvider provider, final IViewerUpdate[] updates) {
        try {
            provider.getExecutor().execute(new Runnable() {
                public void run() {
                    if (updates instanceof IHasChildrenUpdate[]) {
                        provider.update((IHasChildrenUpdate[])updates);
                    } else if (updates instanceof IChildrenCountUpdate[]) {
                        provider.update((IChildrenCountUpdate[])updates);
                    } else if (updates instanceof IChildrenUpdate[]) {
                        provider.update((IChildrenUpdate[])updates);
                    }  
                }
            });
        } catch (RejectedExecutionException e) {
            for (IViewerUpdate update : updates) {
                update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, 
                    "Display is disposed, cannot complete update " + update, null)); //$NON-NLS-1$
                update.done();
            }                
        }            
    }
    
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        IVMProvider provider = getVMProvider(context);
        if (provider != null) {
            return provider.createModelProxy(element, context);
        }
        return null;
    }
    
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        final IVMProvider provider = getVMProvider(context);
        if (provider != null) {
            return provider.createColumnPresentation(context, element);
        }
        return null;
    }
    
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        final IVMProvider provider = getVMProvider(context);
        if (provider != null) {
            return provider.getColumnPresentationId(context, element);
        }
        return null;
    }


    public void update(IViewerInputUpdate update) {
        final IVMProvider provider = getVMProvider(update.getPresentationContext());
        if (provider != null) {
            provider.update(update);
        }
    }

    /**
     * Creates a new View Model Provider for given presentation context.  Returns null
     * if the presentation context is not supported.
     */
    @ThreadSafe    
    abstract protected IVMProvider createViewModelProvider(IPresentationContext context);
    
	/**
	 * Dispatch given event to VM providers interested in events.
	 * 
	 * @since 1.1
	 */
	protected final void handleEvent(final Object event) {
    	final List<IVMEventListener> eventListeners = new ArrayList<IVMEventListener>();

    	aboutToHandleEvent(event);
    	
		for (IVMProvider vmProvider : getVMProviderIterable()) {
			if (vmProvider instanceof IVMEventListener) {
				eventListeners.add((IVMEventListener)vmProvider);
			}
		}
	
		if (!eventListeners.isEmpty()) {
			synchronized (fPendingUpdates) {
				fPendingUpdates.clear();
			}
			// TODO which executor to use?
			final Executor executor= eventListeners.get(0).getExecutor();
			final CountingRequestMonitor crm = new CountingRequestMonitor(executor, null) {
				@Override
				protected void handleCompleted() {
					if (isDisposed()) {
						return;
					}
					// The event listeners have completed processing the event.
					// Now monitor completion of viewer updates issued while dispatching the event
					final CountingRequestMonitor updatesMonitor = new CountingRequestMonitor(executor, null) {
						@Override
						protected void handleCompleted() {
							if (isDisposed()) {
								return;
							}
							doneHandleEvent(event);
						}
					};
					synchronized (fPendingUpdates) {
						int pending = fPendingUpdates.size();
						updatesMonitor.setDoneCount(pending);
						for (IMonitoredUpdate update : fPendingUpdates) {
							update.setMonitor(updatesMonitor);
						}
						fPendingUpdates.clear();
					}
				}
			};
			crm.setDoneCount(eventListeners.size());
	        
			for (final IVMEventListener vmEventListener : eventListeners) {
				vmEventListener.getExecutor().execute(new DsfRunnable() {
					public void run() {
						vmEventListener.handleEvent(event, crm);
					}});
			}
		}
	}

	private IViewerUpdate[] wrapUpdates(IViewerUpdate[] updates) {
		if (updates.length == 0) {
			return updates;
		}
		int i = 0;
		synchronized (fPendingUpdates) {
			for (IViewerUpdate update : updates) {
				IMonitoredUpdate wrap= createMonitoredUpdate(update);
				updates[i++] = wrap;
				fPendingUpdates.add(wrap);
			}
		}
		return updates;
	}

	private IMonitoredUpdate createMonitoredUpdate(IViewerUpdate update) {
		if (update instanceof IChildrenCountUpdate) {
			return new MonitoredChildrenCountUpdate(((IChildrenCountUpdate)update));
		} else if (update instanceof IHasChildrenUpdate) {
			return new MonitoredHasChildrenUpdate(((IHasChildrenUpdate)update));
		} else if (update instanceof IChildrenUpdate) {
			return new MonitoredChildrenUpdate(((IChildrenUpdate)update));
		}
		return null;
	}

    /**
     * Given event is about to be handled.
     * 
     * @param event
     * 
     * @since 1.1
     */
    protected void aboutToHandleEvent(final Object event) {
    }

    /**
     * Given event has been processed by all VM event listeners.
     * 
     * @param event
     * 
     * @since 1.1
     */
    protected void doneHandleEvent(final Object event) {
    }
}
