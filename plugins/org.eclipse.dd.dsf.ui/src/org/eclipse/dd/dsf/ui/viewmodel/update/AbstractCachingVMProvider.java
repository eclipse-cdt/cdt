/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMChildrenCountUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.VMHasChildrenUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;

/**
 * Base implementation of a caching view model provider.  
 */
@SuppressWarnings("restriction")
public class AbstractCachingVMProvider extends AbstractVMProvider implements ICachingVMProvider {

    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * Class representing a key to an element's data in the cache.  The main
     * components of this key are the viewer input and the path, they uniquely
     * identify an element.  The root element is used to track when a given
     * root element is no longer in the cache and can therefore be disposed.
     * The node is needed because different nodes have different lists of 
     * children for the same parent element.
     */
    private static class ElementDataKey {
        ElementDataKey(Object rootElement, IVMNode node, Object viewerInput, TreePath path) {
            fRootElement = rootElement;
            fNode = node;
            fViewerInput = viewerInput;
            fPath = path;
        }

        final Object fRootElement;
        final IVMNode fNode;
        final Object fViewerInput;
        final TreePath fPath;
        
        @Override
        public String toString() {
            return fViewerInput + "." + fPath.toString() + "(" + fNode + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ElementDataKey)) return false;
            ElementDataKey key = (ElementDataKey)obj;
            return
                (fNode == null && key.fNode == null || (fNode != null && fNode.equals(key.fNode))) &&
                (fRootElement == null && key.fRootElement == null || (fRootElement != null && fRootElement.equals(key.fRootElement))) &&
                (fViewerInput == null && key.fViewerInput == null || (fViewerInput != null && fViewerInput.equals(key.fViewerInput))) &&
                (fPath == null && key.fPath == null || (fPath != null && fPath.equals(key.fPath)));
        }
        
        @Override
        public int hashCode() {
            return 
                (fRootElement != null ? fRootElement.hashCode() : 0) +  
                (fNode != null ? fNode.hashCode() : 0) + 
                (fViewerInput != null ? fViewerInput.hashCode() : 0) + 
                (fPath != null ? fPath.hashCode() : 0);
        }
    }
    
    /**
     * A base class for the entry in the cache.  Since the cache maintains
     * a double-linked list through all the entries, the linked list references
     * are maintained in this class.
     */
    private static class Entry {
        final Object fKey;
        
        Entry fNext;
        Entry fPrevious;

        Entry(Object key) {
            fKey = key;
        }

        void insert(Entry nextEntry) {
            fNext  = nextEntry;
            fPrevious = nextEntry.fPrevious;
            fPrevious.fNext = this;
            fNext.fPrevious = this;
        }

        void remove() {
            fPrevious.fNext = fNext;
            fNext.fPrevious = fPrevious;
        }
    }
    
    /**
     * Entry with cached element data. 
     */
    static class ElementDataEntry extends Entry {
        ElementDataEntry(ElementDataKey key) {
            super(key);
        }
        
        Boolean fDirty = false;
        Boolean fHasChildren = null;
        Integer fChildrenCount = null;
        boolean fAllChildrenKnown = false;
        Map<Integer,Object> fChildren = null;
        Map<IDMContext,Object> fDataOrStatus = new HashMap<IDMContext,Object>(1);
        Map<IDMContext,IDMData> fArchiveData = new HashMap<IDMContext,IDMData>(1);;
        
        @Override
        public String toString() {
            return fKey.toString() + " = " + //$NON-NLS-1$ 
                "[hasChildren=" + fHasChildren + ", " +//$NON-NLS-1$ //$NON-NLS-2$
                "childrenCount=" + fChildrenCount + //$NON-NLS-1$
                ", children=" + fChildren + //$NON-NLS-1$ 
                ", data/status=" + fDataOrStatus + //$NON-NLS-1$ 
                ", oldData=" + fArchiveData + "]"; //$NON-NLS-1$ //$NON-NLS-2$ 
        }
    }

    /**
     * A key for a special marker entry in the cache.  This marker entry is used
     * to optimize repeated flushing of the cache.  
     * @see AbstractCachingVMProvider#flush(List)
     */
    private static class FlushMarkerKey {
        private Object fRootElement;
        private IElementUpdateTester fElementTester;
        
        FlushMarkerKey(Object rootElement, IElementUpdateTester pathTester) {
            fRootElement = rootElement;
            fElementTester = pathTester;
        }
        
        boolean includes(FlushMarkerKey key) {
            return fRootElement.equals(key.fRootElement) && 
                   fElementTester.includes(key.fElementTester);
        }
        
        int getUpdateFlags(ElementDataKey key) {
            if (fRootElement.equals(key.fRootElement)) {
                return fElementTester.getUpdateFlags(key.fViewerInput, key.fPath);
            } 
            return 0;
        }
    }
    
    /**
     * Marker used to keep track of whether any entries with the given
     * root element are present in the cache.  
     */
    private static class RootElementMarkerKey {
        
        private Object fRootElement;
        
        RootElementMarkerKey(Object rootElement) {
            fRootElement = rootElement;
        }
    }
    
    class RootElementMarkerEntry extends Entry {
        RootElementMarkerEntry(RootElementMarkerKey key) {
            super(key);
        }
        
        @Override
        void remove() {
            super.remove();
            rootElementRemovedFromCache(((RootElementMarkerKey)fKey).fRootElement);
        }
    }
        
    protected static String SELECTED_UPDATE_MODE = "org.eclipse.dd.dsf.ui.viewmodel.update.selectedUpdateMode";  //$NON-NLS-1$

    private IVMUpdatePolicy[] fAvailableUpdatePolicies;

    public Map<Object, RootElementMarkerKey> fRootMarkers = new HashMap<Object, RootElementMarkerKey>();
    
    /**
     * Hash map holding cache data.  To store the cache information, the cache uses a 
     * combination of this hash map and a double-linked list running through all 
     * the entries in the cache.  The linked list is used to organize the cache entries
     * in least recently used (LRU) order.  This ordering is then used to delete least 
     * recently used entries in the cache and keep the cache from growing indefinitely.
     * Also, the ordering is used to optimize the flushing of the cache data (see 
     * {@link FlushMarkerKey} for more details).
     */
    private final Map<Object, Entry> fCacheData = new HashMap<Object, Entry>(200, 0.75f);
    
    /**
     * Pointer to the first cache entry in the double-linked list of cache entries.
     */
    private final Entry fCacheListHead;


    public AbstractCachingVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext) {
        super(adapter, presentationContext);
        
        fCacheListHead = new Entry(null);
        fCacheListHead.fNext = fCacheListHead;
        fCacheListHead.fPrevious = fCacheListHead;
        
        fAvailableUpdatePolicies = createUpdateModes();
    }
    
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy() };
    }
    
    public IVMUpdatePolicy[] getAvailableUpdatePolicies() {
        return fAvailableUpdatePolicies;
    }

    public IVMUpdatePolicy getActiveUpdatePolicy() {
        String updateModeId = (String)getPresentationContext().getProperty(SELECTED_UPDATE_MODE);
        if (updateModeId != null) {
            for (IVMUpdatePolicy updateMode : getAvailableUpdatePolicies()) {
                if (updateMode.getID().equals(updateModeId)) {
                    return updateMode;
                }
            }
        }
        
        // Default to the first one.
        return getAvailableUpdatePolicies()[0];
    }

    public void setActiveUpdatePolicy(IVMUpdatePolicy updatePolicy) {
        getPresentationContext().setProperty(SELECTED_UPDATE_MODE, updatePolicy.getID());
    }
    
    public void refresh() {
        IElementUpdateTester elementTester =  getActiveUpdatePolicy().getElementUpdateTester(ManualUpdatePolicy.REFRESH_EVENT);
        
        List<FlushMarkerKey> flushKeys = new LinkedList<FlushMarkerKey>();

        for (final IVMModelProxy proxyStrategy : getActiveModelProxies()) {
            flushKeys.add(new FlushMarkerKey(proxyStrategy.getRootElement(), elementTester));
        }
        
        flush(flushKeys);
        
        for (final IVMModelProxy proxyStrategy : getActiveModelProxies()) {
            if (!proxyStrategy.isDisposed()) {
                proxyStrategy.fireModelChanged(new  ModelDelta(proxyStrategy.getRootElement(), IModelDelta.CONTENT));
            }
        }
    }
    
    @Override
    public void updateNode(IVMNode node, IHasChildrenUpdate[] updates) {
        LinkedList <IHasChildrenUpdate> missUpdates = new LinkedList<IHasChildrenUpdate>();
        for(final IHasChildrenUpdate update : updates) {
            ElementDataKey key = makeEntryKey(node, update);
            final ElementDataEntry entry = getElementDataEntry(key);
            if (entry.fHasChildren != null) {
                update.setHasChilren(entry.fHasChildren.booleanValue());
                update.done();
            } else {
                missUpdates.add( 
                    new VMHasChildrenUpdate(update, new DataRequestMonitor<Boolean>(getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if(isSuccess()) {
                                entry.fHasChildren = this.getData();
                                update.setHasChilren(getData());
                            } else {
                                update.setStatus(getStatus());
                            }
                            update.done();
                        }
                    }));
            }
        }
        
        if (!missUpdates.isEmpty()) {
            super.updateNode(node, missUpdates.toArray(new IHasChildrenUpdate[missUpdates.size()]));
        }
    }
    
    @Override
    public void updateNode(IVMNode node, final IChildrenCountUpdate update) {
        ElementDataKey key = makeEntryKey(node, update);
        final ElementDataEntry entry = getElementDataEntry(key);
        if(entry.fChildrenCount != null) {
            update.setChildCount(entry.fChildrenCount.intValue());
            update.done();
        } else {
            IChildrenCountUpdate updateProxy = new VMChildrenCountUpdate(update, new DataRequestMonitor<Integer>(getExecutor(), null) {
                @Override
                protected void handleCompleted() {
                    if(isSuccess()) {
                        entry.fChildrenCount = this.getData();
                        update.setChildCount(getData());
                    } else {
                        update.setStatus(getStatus());
                    }
                    update.done();
                }
            });
            super.updateNode(node, updateProxy);
        }
    }

    @Override
    public void updateNode(IVMNode node, final IChildrenUpdate update) {
        
        ElementDataKey key = makeEntryKey(node, update);
        
        final ElementDataEntry entry = getElementDataEntry(key);
        if (entry.fChildren == null || (update.getOffset() < 0 && !entry.fAllChildrenKnown)) {
            // We need to retrieve all the children if we don't have any children information.
            // Or if the client requested all children (offset = -1, length -1) and we have not
            // retrieved that before.
            IChildrenUpdate updateProxy = new VMChildrenUpdate(
                update, update.getOffset(), update.getLength(),
                new DataRequestMonitor<List<Object>>(getExecutor(), null)
                {
                    @Override
                    protected void handleCompleted()
                    {
                        // Workaround for a bug caused by an optimization in the viewer:
                        // The viewer may request more children then there are at a given level.  
                        // This causes the update to return with an error.
                        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=202109
                        // Instead of checking isSuccess(), check getData() != null.
                        if(getData() != null) {
                            // Check if the udpate retrieved all children by specifying "offset = -1, length = -1"
                            int updateOffset = update.getOffset();
                            if (updateOffset < 0) {
                                updateOffset = 0;
                                entry.fAllChildrenKnown = true;
                            }
                            
                            // Estimate size of children map.
                            Integer childrenCount = entry.fChildrenCount;
                            childrenCount = childrenCount != null ? childrenCount : 0;
                            int capacity = Math.max((childrenCount.intValue() * 4)/3, 32);
                            // Create a new map, but only if it hasn't been created yet by another update.
                            if (entry.fChildren == null) {
                                entry.fChildren = new HashMap<Integer,Object>(capacity);
                            }
                            
                            // Set the children to map and update.
                            for(int j = 0; j < getData().size(); j++) {
                                int offset = updateOffset + j;
                                Object child = getData().get(j);
                                if (child != null) {
                                    entry.fChildren.put(offset, child);
                                    update.setChild(child, offset);
                                }
                            }
                        }
                        update.done();
                    }
                });
            super.updateNode(node, updateProxy);
        } else if (update.getOffset() < 0 ) {
            // The update requested all children.  Fill in all children assuming that 
            // the children array is complete.
            
            // The following assert should never fail given the first if statement. 
            assert entry.fAllChildrenKnown;
            
            // we have all of the children in cache; return from cache
            for(int position = 0; position < entry.fChildren.size(); position++) {
                update.setChild(entry.fChildren.get(position), position);
            }
            update.done();
        } else {
            // Make the list of missing children.  If we've retrieved the 
            List<Integer> childrenMissingFromCache = new LinkedList<Integer>();
            for (int i = update.getOffset(); i < update.getOffset() + update.getLength(); i++) {
                childrenMissingFromCache.add(i);
            }

            // Fill in the known children from cache.
            for(Integer position = update.getOffset(); position < update.getOffset() + update.getLength(); position++) {
                Object child = entry.fChildren.get(position);
                if (child != null) {
                    update.setChild(entry.fChildren.get(position), position);
                    childrenMissingFromCache.remove(position);
                }
            }
            
            if (childrenMissingFromCache.size() > 0) {
                // perform a partial update; we only have some of the children of the update request

                List<IChildrenUpdate> partialUpdates = new ArrayList<IChildrenUpdate>(2);

                final CountingRequestMonitor multiRm = new ViewerCountingRequestMonitor(getExecutor(), update);
                
                while(childrenMissingFromCache.size() > 0)
                {
                    final int offset = childrenMissingFromCache.get(0);
                    childrenMissingFromCache.remove(0);
                    int length = 1;
                    while(childrenMissingFromCache.size() > 0 && childrenMissingFromCache.get(0) == offset + length)
                    {
                        length++;
                        childrenMissingFromCache.remove(0);
                    }
                    
                    partialUpdates.add(new VMChildrenUpdate(
                        update, offset, length,
                        new DataRequestMonitor<List<Object>>(getExecutor(), multiRm) {
                            @Override
                            protected void handleCompleted() {
                                if (getData() != null) {
                                    for (int i = 0; i < getData().size(); i++) {
                                        update.setChild(getData().get(i), offset + i);
                                    }
                                }
                                multiRm.done();
                            }
                        }));
                }
                
                for (IChildrenUpdate partialUpdate : partialUpdates) {
                    super.updateNode(node, partialUpdate);
                }
                multiRm.setDoneCount(partialUpdates.size());
            } else {
                // we have all of the children in cache; return from cache
                update.done();
            }
        }
        
    }
    
    /**
     * Flushes the cache with given DMC as the root element. 
     * @param dmcToFlush DM Context which is the root of the flush operation.  Entries 
     * for all DMCs that have this DMC as their ancestor will be flushed.  If this
     * parameter is null, then all entries are flushed. 
     * @param archive
     */
    private void flush(List<FlushMarkerKey> flushKeys) {
        // To flush the cache data for given context, we have to iterate through all the contexts 
        // in cache.  For each entry that has the given context as a parent, perform the flush.
        List<FlushMarkerKey> flushKeysCopy = new ArrayList<FlushMarkerKey>(flushKeys.size());
        flushKeysCopy.addAll(flushKeys);
        
        // Iterate through the cache entries backwards.  This means that we will be 
        // iterating in order of most-recently-used to least-recently-used.
        Entry entry = fCacheListHead.fPrevious;
        while (entry != fCacheListHead && flushKeysCopy.size() != 0) {
            for (Iterator<FlushMarkerKey> flushKeyItr = flushKeysCopy.iterator(); flushKeyItr.hasNext();) {
                FlushMarkerKey flushKey = flushKeyItr.next();
                
                if (entry.fKey instanceof FlushMarkerKey) {
                    FlushMarkerKey entryFlushKey = (FlushMarkerKey)entry.fKey;
                    // If the context currently being flushed includes the flush 
                    // context in current entry, remove the current entry since it will
                    // be replaced with one at the end of the list.
                    // Use special handling for null contexts, which we treat like it's an 
                    // ancestor of all other contexts.
                    if (flushKey.includes(entryFlushKey)) {
                        fCacheData.remove(entryFlushKey);
                        entry.remove();
                    }
                    
                    // If the flush context in current entry includes the current context
                    // being flushed, we can stop iterating through the cache entries
                    // now.
                    if (entryFlushKey.includes(flushKey)) {
                        flushKeyItr.remove();
                    }
                }
                else if (entry instanceof ElementDataEntry) {
                    ElementDataEntry elementDataEntry = (ElementDataEntry)entry;
                    int updateFlags = flushKey.getUpdateFlags((ElementDataKey)elementDataEntry.fKey);
                    if ((updateFlags & IVMUpdatePolicy.FLUSH) != 0) {
                        if ((updateFlags & IVMUpdatePolicy.ARCHIVE) != 0) {
                            // We are saving current data for change history, check if the data is valid. 
                            // If it valid, save it for archive, if it's not valid old archive data will be used 
                            // if there is any.  And if there is no old archive data, just remove the cache entry.
                            for (Iterator<Map.Entry<IDMContext, Object>> itr = elementDataEntry.fDataOrStatus.entrySet().iterator(); 
                                 itr.hasNext();) 
                            {
                                Map.Entry<IDMContext, Object> dataOrStatusEntry = itr.next();
                                if (dataOrStatusEntry.getValue() instanceof IDMData) {
                                    elementDataEntry.fArchiveData.put(dataOrStatusEntry.getKey(), (IDMData)dataOrStatusEntry.getValue());
                                } 
                            }
                            elementDataEntry.fDataOrStatus.clear();
                            if (elementDataEntry.fArchiveData.isEmpty()) {
                                fCacheData.remove(entry.fKey);
                                entry.remove();
                            }
                        } else {
                            // We are not changing the archived data.  If archive data exists in the entry, leave it.
                            // Otherwise remove the whole entry.
                            if (!elementDataEntry.fArchiveData.isEmpty()) {
                                elementDataEntry.fDataOrStatus.clear();
                            } else {
                                fCacheData.remove(entry.fKey);
                                entry.remove();
                            }
                        }
                        elementDataEntry.fHasChildren = null;
                        elementDataEntry.fChildrenCount = null;
                        elementDataEntry.fChildren = null;
                    } else if ((updateFlags & IVMUpdatePolicy.DIRTY) != 0) {
                        elementDataEntry.fDirty = true;
                    }
                }
            }
            entry = entry.fPrevious;
        }
        
        for (FlushMarkerKey flushKey : flushKeys) {
            // Insert a marker for this flush operation.  
            Entry flushMarkerEntry = new Entry(flushKey);
            fCacheData.put(flushKey, flushMarkerEntry);
            flushMarkerEntry.insert(fCacheListHead);
        }
    }

    @Override
    protected void handleEvent(final IVMModelProxy proxyStrategy, final Object event, RequestMonitor rm) {   
        IElementUpdateTester elementTester =  getActiveUpdatePolicy().getElementUpdateTester(event);
        
        List<FlushMarkerKey> flushKeys = new LinkedList<FlushMarkerKey>();

        flushKeys.add(new FlushMarkerKey(proxyStrategy.getRootElement(), elementTester));
        
        flush(flushKeys);
        
        super.handleEvent(proxyStrategy, event, rm);
    }
    
    /**
     * Override default implementation to avoid automatically removing disposed proxies from
     * list of active proxies.  The caching provider only removes a proxy after its root element
     * is no longer in the cache.
     */
    @Override
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        assert getExecutor().isInExecutorThread();
        
        // Iterate through the current active proxies to try to find a proxy with the same
        // element and re-use it if found.  At the same time purge proxies that are no longer
        IVMModelProxy proxy = null;
        for (Iterator<IVMModelProxy> itr = getActiveModelProxies().iterator(); itr.hasNext();) {
            IVMModelProxy next = itr.next();
            if (next == null && next.getRootElement().equals(element)) {
                proxy = next;
            }
        }
        if (proxy == null) {
            proxy = createModelProxyStrategy(element);
            getActiveModelProxies().add(proxy);
        }
        return proxy;
    }
    
    /**
     * Called when a given all cache entries for the given root element have
     * been removed from the cache.  In order to property track changed elements, 
     * the caching VM provider does not immediately remove entries for a given root
     * element, when the viewer root element changes.  Instead it keeps this root 
     * element and keeps processing deltas for that root element until the 
     * cache entries for this element are gone. 
     */
    protected void rootElementRemovedFromCache(Object rootElement) {
        fRootMarkers.remove(rootElement);
        
        for (Iterator<IVMModelProxy> proxiesItr = getActiveModelProxies().iterator(); proxiesItr.hasNext();) {
            IVMModelProxy proxy = proxiesItr.next();
            if (proxy.isDisposed() && proxy.getRootElement().equals(rootElement) ) {
                proxiesItr.remove();
            }
        }
    }

    /**
     * Convenience class that searches for teh root element for the given
     * update and creates an element cache entry key.
     */
    private ElementDataKey makeEntryKey(IVMNode node, IViewerUpdate update) {
        Object rootElement = update.getViewerInput();  // Default
        outer: for (IVMModelProxy proxy : getActiveModelProxies()) {
            Object proxyRoot = proxy.getRootElement();
            if (proxyRoot.equals(update.getViewerInput())) {
                rootElement = proxyRoot;
                break;
            }
            TreePath path = update.getElementPath();
            for (int i = 0; i < path.getSegmentCount(); i++) {
                if (proxyRoot.equals(path.getSegment(i))) {
                    rootElement = proxyRoot;
                    break outer;
                }
            }
        }
        
        return new ElementDataKey(rootElement, node, update.getElement(), update.getElementPath());
    }
    
    /**
     * This is the only method that should be used to access a cache entry.  
     * It creates a new entry if needed and it maintains the ordering in 
     * the least-recently-used linked list.   
     */
    private ElementDataEntry getElementDataEntry(ElementDataKey key) {
        assert key != null;
        ElementDataEntry entry = (ElementDataEntry)fCacheData.get(key);
        if (entry == null) {
            // Create a new entry and add it to the end of the list.
            entry = new ElementDataEntry(key);
            addEntry(key, entry);
        } else {
            // Entry exists, move it to the end of the list.
            entry.remove();
            entry.insert(fCacheListHead);
        }
        
        // Update they root element marker.
        RootElementMarkerKey rootMarker = fRootMarkers.get(key.fRootElement);
        if (rootMarker == null) {
            rootMarker = new RootElementMarkerKey(key.fRootElement);
            fRootMarkers.put(key.fRootElement, rootMarker);
        }
        Entry rootMarkerEntry = fCacheData.get(rootMarker);
        if (rootMarkerEntry == null) {
            rootMarkerEntry = new RootElementMarkerEntry(rootMarker);
            addEntry(rootMarker, rootMarkerEntry); 
        } else {
            rootMarkerEntry.remove();
            rootMarkerEntry.insert(fCacheListHead);
        }
        
        return entry;
    }

    /**
     * Convenience method used by {@link #getElementDataEntry(ElementDataKey)}
     */
    private void addEntry(Object key, Entry entry) {
        fCacheData.put(key, entry);
        entry.insert(fCacheListHead);
        // If we are at capacity in the cache, remove the entry from head.
        if (fCacheData.size() > MAX_CACHE_SIZE) {
            fCacheData.remove(fCacheListHead.fNext.fKey);
            fCacheListHead.fNext.remove();
        }
    }

    /**
     * Retrieves the deprecated IDMData object for the given IDMContext.  This 
     * method should be removed once the use of IDMData is replaced with 
     * {@link IElementPropertiesProvider}.
     */
    @Deprecated
    public void getModelData(IVMNode node, IViewerUpdate update, IDMService service, final IDMContext dmc, 
        final DataRequestMonitor rm, Executor executor)
    { 
        ElementDataKey key = makeEntryKey(node, update);
        final ElementDataEntry entry = getElementDataEntry(key);
        Object dataOrStatus = entry.fDataOrStatus.get(dmc);
        if(dataOrStatus != null) {
            if (dataOrStatus instanceof IDMData) {  
                rm.setData( (IDMData)dataOrStatus );
            } else {
                rm.setStatus((IStatus)dataOrStatus );
            }
            rm.done();
        } else {
            service.getModelData(dmc, 
                new DataRequestMonitor<IDMData>(executor, rm) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            entry.fDataOrStatus.put(dmc, getData());
                            rm.setData(getData());
                        } else {
                            entry.fDataOrStatus.put(dmc, getStatus());
                            rm.setStatus(getStatus());
                        }
                        rm.done();
                    }
                }       
            );
        }
    }
    
    /**
     * Retrieves the deprecated IDMData object for the given IDMContext.  This 
     * method should be removed once the use of IDMData is replaced with 
     * {@link IElementPropertiesProvider}.
     */
    @Deprecated
    public IDMData getArchivedModelData(IVMNode node, IViewerUpdate update, IDMContext dmc) {
        ElementDataKey key = makeEntryKey(node, update);
        final ElementDataEntry entry = getElementDataEntry(key);
        if ( entry.fArchiveData != null) {
            return entry.fArchiveData.get(dmc);
        }
        return null;
    }

}
