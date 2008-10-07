/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dd.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMService;
import org.eclipse.dd.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.dd.dsf.ui.concurrent.SimpleDisplayExecutor;
import org.eclipse.dd.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.dd.dsf.ui.viewmodel.IVMModelProxyExtension;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Base implementation of a caching view model provider.  
 */
@SuppressWarnings("restriction")
public class AbstractCachingVMProvider extends AbstractVMProvider implements ICachingVMProvider, ICachingVMProviderExtension {

	private boolean fDelayEventHandleForViewUpdate = false;
	
	// debug flag
    static boolean DEBUG_CACHE = false;

    static {
        DEBUG_CACHE = DsfUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
         Platform.getDebugOption("org.eclipse.dd.dsf.ui/debug/vm/cache")); //$NON-NLS-1$
    }   

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
            return fNode.toString() + " " +  //$NON-NLS-1$
                (fPath.getSegmentCount() == 0 ? fViewerInput.toString() : fPath.getLastSegment().toString());
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
        
        /**
         * Counter of flush operations performed on this entry.  It is used
         * by caching update operations to make sure that an update which
         * was issued for a given entry is still valid for that entry when
         * it is completed by the node. 
         */
        int fFlushCounter = 0;
        
        /**
         * Indicates that the data in this cache entry is out of date with
         * the data on the target.
         */
        Boolean fDirty = false;
        
        /** 
         * Cached {@link IHasChildrenUpdate} result. 
         */
        Boolean fHasChildren = null;

        /** 
         * Cached {@link IChildrenCountUpdate} result. 
         */
        Integer fChildrenCount = null;
        
        /**
         * Flag indicating that all the children of the given element are 
         * alredy cached.
         */
        boolean fAllChildrenKnown = false;
        
        /**
         * Map containing children of this element, keyed by child index.
         */
        Map<Integer,Object> fChildren = null;
        
        /**
         * Map of IDMData objects, keyed by the DM context.
         */
        Map<IDMContext,Object> fDataOrStatus = new HashMap<IDMContext,Object>(1);
        
        /**
         * Previous known value of the DM data objects.
         */
        Map<IDMContext,IDMData> fArchiveData = new HashMap<IDMContext,IDMData>(1);;
        
        void ensureChildrenMap() {
            if (fChildren == null) {
                Integer childrenCount = fChildrenCount;
                childrenCount = childrenCount != null ? childrenCount : 0;
                int capacity = Math.max((childrenCount.intValue() * 4)/3, 32);
                fChildren = new HashMap<Integer,Object>(capacity);
            }
        }
        
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
        
        @Override
        public String toString() {
            return fElementTester.toString() + " " + fRootElement.toString(); //$NON-NLS-1$
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
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof RootElementMarkerKey && ((RootElementMarkerKey)obj).fRootElement.equals(fRootElement);
        }
        
        @Override
        public int hashCode() {
            return fRootElement.hashCode();
        }

        @Override
        public String toString() {
            return fRootElement.toString();
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
        
        @Override
        public String toString() {
            return "ROOT MARKER " + fKey;  //$NON-NLS-1$
        }
    }
        
    protected static String SELECTED_UPDATE_MODE = "org.eclipse.dd.dsf.ui.viewmodel.update.selectedUpdateMode";  //$NON-NLS-1$
    /**
     * @since 1.1
     */
    protected static String SELECTED_UPDATE_SCOPE = "org.eclipse.dd.dsf.ui.viewmodel.update.selectedUpdateScope";  //$NON-NLS-1$

    private IVMUpdatePolicy[] fAvailableUpdatePolicies;
    private IVMUpdateScope[] fAvailableUpdateScopes;

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
    private final Map<Object, Entry> fCacheData = Collections.synchronizedMap(new HashMap<Object, Entry>(200, 0.75f));
    
    /**
     * Pointer to the first cache entry in the double-linked list of cache entries.
     */
    private final Entry fCacheListHead;


    public AbstractCachingVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext) {
        super(adapter, presentationContext);
        
        fCacheListHead = new Entry(null) {
            @Override
            public String toString() {
                return "HEAD"; //$NON-NLS-1$
            }
        };
        fCacheListHead.fNext = fCacheListHead;
        fCacheListHead.fPrevious = fCacheListHead;
        
        fAvailableUpdatePolicies = createUpdateModes();
        
        fAvailableUpdateScopes = createUpdateScopes();
        setActiveUpdateScope(new VisibleUpdateScope());
    }
    
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy() };
    }
    
    /**
     * @since 1.1
     */
    protected IVMUpdateScope[] createUpdateScopes() {
        return new IVMUpdateScope[] { new VisibleUpdateScope(), new AllUpdateScope() };
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
        
        for (final IVMModelProxy proxyStrategy : getActiveModelProxies()) {
            flush(new FlushMarkerKey(proxyStrategy.getRootElement(), elementTester));
        }
        
        for (final IVMModelProxy proxyStrategy : getActiveModelProxies()) {
            if (!proxyStrategy.isDisposed()) {
                proxyStrategy.fireModelChanged(new  ModelDelta(proxyStrategy.getRootElement(), IModelDelta.CONTENT));
            }
        }
    }
    
    @Override
    public void updateNode(final IVMNode node, IHasChildrenUpdate[] updates) {
        LinkedList <IHasChildrenUpdate> missUpdates = new LinkedList<IHasChildrenUpdate>();
        for(final IHasChildrenUpdate update : updates) {
            // Find or create the cache entry for the element of this update.
            ElementDataKey key = makeEntryKey(node, update);
            final ElementDataEntry entry = getElementDataEntry(key);
            
            if (entry.fHasChildren != null) {
                // Cache Hit!  Just return the value.
                if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                    DsfUIPlugin.debug("cacheHitHasChildren(node = " + node + ", update = " + update + ", " + entry.fHasChildren + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                update.setHasChilren(entry.fHasChildren.booleanValue());
                update.done();
            } else {
                // Cache miss!  Save the flush counter of the entry and create a proxy update.
                final int flushCounter = entry.fFlushCounter;
                missUpdates.add( 
                    new VMHasChildrenUpdate(
                        update, 
                        new ViewerDataRequestMonitor<Boolean>(getExecutor(), update) {
                            @Override
                            protected void handleCompleted() {
                                // Update completed.  Write value to cache only if update successed 
                                // and the cache entry wasn't flushed in the mean time. 
                                if(isSuccess()) {
                                    if (flushCounter == entry.fFlushCounter) {
                                        if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                                            DsfUIPlugin.debug("cacheSavedHasChildren(node = " + node + ", update = " + update + ", " + getData() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                        }
                                        entry.fHasChildren = this.getData();
                                    }
                                    update.setHasChilren(getData());
                                } else {
                                    update.setStatus(getStatus());
                                }
                                update.done();
                            }
                        }));
            }
        }
        
        // Issue all the update proxies with one call.
        if (!missUpdates.isEmpty()) {
            super.updateNode(node, missUpdates.toArray(new IHasChildrenUpdate[missUpdates.size()]));
        }
    }
    
    @Override
    public void updateNode(final IVMNode node, final IChildrenCountUpdate update) {
        // Find or create the cache entry for the element of this update.
        ElementDataKey key = makeEntryKey(node, update);
        final ElementDataEntry entry = getElementDataEntry(key);
        
        if(entry.fChildrenCount != null) {
            // Cache Hit!  Just return the value.
            if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                DsfUIPlugin.debug("cacheHitChildrenCount(node = " + node + ", update = " + update + ", " + entry.fChildrenCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            update.setChildCount(entry.fChildrenCount.intValue());
            update.done();
        } else {
            // Cache miss!  Save the flush counter of the entry and create a proxy update.
            final int flushCounter = entry.fFlushCounter;
            IChildrenCountUpdate updateProxy = new VMChildrenCountUpdate(
                update, 
                new ViewerDataRequestMonitor<Integer>(getExecutor(), update) {
                    @Override
                    protected void handleCompleted() {
                        // Update completed.  Write value to cache only if update successed 
                        // and the cache entry wasn't flushed in the mean time. 
                        if(isSuccess()) {
                            if (flushCounter == entry.fFlushCounter) {
                                if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                                    DsfUIPlugin.debug("cacheSavedChildrenCount(node = " + node + ", update = " + update + ", " + getData() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                }
                                entry.fChildrenCount = this.getData();
                            }
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
    public void updateNode(final IVMNode node, final IChildrenUpdate update) {
        // Find or create the cache entry for the element of this update.
        ElementDataKey key = makeEntryKey(node, update);
        final ElementDataEntry entry = getElementDataEntry(key);
        
        final int flushCounter = entry.fFlushCounter;
        if (entry.fChildren == null || (update.getOffset() < 0 && !entry.fAllChildrenKnown)) {
            // Need to retrieve all the children if there is no children information yet.
            // Or if the client requested all children (offset = -1, length -1) and all 
            // the children are not yet known.
            IChildrenUpdate updateProxy = new VMChildrenUpdate(
                update, update.getOffset(), update.getLength(),
                new ViewerDataRequestMonitor<List<Object>>(getExecutor(), update){
                    @Override
                    protected void handleSuccess() {
                        // Check if the update retrieved all children by specifying "offset = -1, length = -1"
                        int updateOffset = update.getOffset();
                        if (updateOffset < 0) 
                        {
                            updateOffset = 0;
                            if (entry.fFlushCounter == flushCounter) {
                                entry.fAllChildrenKnown = true;
                            }
                        }

                        if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                            DsfUIPlugin.debug("cacheSavedChildren(node = " + node + ", update = " + update + ", children = {" + updateOffset + "->" + (updateOffset + getData().size()) + "})"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                        }

                        if (flushCounter == entry.fFlushCounter) {
                            entry.ensureChildrenMap();
                        }
                        
                        // Set the children to map and update.
                        for(int j = 0; j < getData().size(); j++) {
                            int offset = updateOffset + j;
                            Object child = getData().get(j);
                            if (child != null) {
                                if (flushCounter == entry.fFlushCounter) {
                                    entry.fChildren.put(offset, child);
                                }
                                update.setChild(child, offset);
                            }
                        }
                        update.done();
                    }
                    
                    @Override
                    protected void handleCancel() {
                        if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                            DsfUIPlugin.debug("cacheCanceledChildren(node = " + node + ", update = " + update + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
                        }
                        super.handleCancel();
                    }
                });
            super.updateNode(node, updateProxy);
        } else if (update.getOffset() < 0 ) {
            // The update requested all children.  Fill in all children assuming that 
            // the children array is complete.

            if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                DsfUIPlugin.debug("cacheHitChildren(node = " + node + ", update = " + update + ", children = " + entry.fChildren.keySet() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
            }

            // The following assert should never fail given the first if statement. 
            assert entry.fAllChildrenKnown;
            
            // we have all of the children in cache; return from cache
            for(int position = 0; position < entry.fChildren.size(); position++) {
                update.setChild(entry.fChildren.get(position), position);
            }
            update.done();
        } else {
            // Update for a partial list of children was requested.
            // Iterate through the known children and make a list of missing 
            // indexes.   
            List<Integer> childrenMissingFromCache = new LinkedList<Integer>();
            for (int i = update.getOffset(); i < update.getOffset() + update.getLength(); i++) {
                childrenMissingFromCache.add(i);
            }

            // Write known children from cache into the update.
            for(Integer position = update.getOffset(); position < update.getOffset() + update.getLength(); position++) {
                Object child = entry.fChildren.get(position);
                if (child != null) {
                    update.setChild(entry.fChildren.get(position), position);
                    childrenMissingFromCache.remove(position);
                }
            }
            
            if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                DsfUIPlugin.debug("cachePartialHitChildren(node = " + node + ", update = " + update + ", missing = " + childrenMissingFromCache + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
            }
            
            if (childrenMissingFromCache.size() > 0) {
                // Some children were not found in the cache, create separate 
                // proxy updates for the continuous ranges of missing children.
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
                            protected void handleSuccess() {
                                // Only save the children to the cahce if the entry wasn't flushed.
                                if (flushCounter == entry.fFlushCounter) {
                                    if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                                        DsfUIPlugin.debug("cachePartialSaveChildren(node = " + node + ", update = " + update + ", saved = {" + offset + "->" + (offset + getData().size()) + "})"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
                                    }
                                    entry.ensureChildrenMap();
                                }
                                
                                for (int i = 0; i < getData().size(); i++) {
                                    if (getData().get(i) != null) {
                                        update.setChild(getData().get(i), offset + i);
                                        if (flushCounter == entry.fFlushCounter) {
                                            // Only save the children to the cahce if the entry wasn't flushed.
                                            entry.fChildren.put(offset + i, getData().get(i));
                                        }
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
                // All children were found in cache.  Compelte the update.
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
    private void flush(FlushMarkerKey flushKey) {
        if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
            DsfUIPlugin.debug("cacheFlushing(" + flushKey + ")"); //$NON-NLS-1$ //$NON-NLS-2$  
        }
        // For each entry that has the given context as a parent, perform the flush.
        // Iterate through the cache entries backwards.  This means that we will be
        // iterating in order of most-recently-used to least-recently-used.
        Entry entry = fCacheListHead.fPrevious;
        while (entry != fCacheListHead) {
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
                    break;

                }
            }
            else if (entry instanceof ElementDataEntry) {
                ElementDataEntry elementDataEntry = (ElementDataEntry)entry;
                int updateFlags = flushKey.getUpdateFlags((ElementDataKey)elementDataEntry.fKey);
                if ((updateFlags & IVMUpdatePolicy.FLUSH) != 0) {
                    if ((updateFlags & IVMUpdatePolicy.ARCHIVE) == IVMUpdatePolicy.ARCHIVE) {
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
                    elementDataEntry.fFlushCounter++;                    
                    elementDataEntry.fHasChildren = null;
                    elementDataEntry.fChildrenCount = null;
                    elementDataEntry.fChildren = null;
                    elementDataEntry.fAllChildrenKnown = false;
                    elementDataEntry.fDirty = false;
                } else if ((updateFlags & IVMUpdatePolicy.DIRTY) != 0) {
                    elementDataEntry.fDirty = true;
                }
            }
            entry = entry.fPrevious;
        }
        
        // Insert a marker for this flush operation.
        Entry flushMarkerEntry = new Entry(flushKey);
        fCacheData.put(flushKey, flushMarkerEntry);
        flushMarkerEntry.insert(fCacheListHead);
    }

    @Override
    protected void handleEvent(final IVMModelProxy proxyStrategy, final Object event, final RequestMonitor rm) {   
        IElementUpdateTester elementTester =  getActiveUpdatePolicy().getElementUpdateTester(event);
   
        flush(new FlushMarkerKey(proxyStrategy.getRootElement(), elementTester));
        
        if (proxyStrategy instanceof IVMModelProxyExtension) {
            IVMModelProxyExtension proxyStrategyExtension = (IVMModelProxyExtension)proxyStrategy;
            if(fDelayEventHandleForViewUpdate) {
    	        if(this.getActiveUpdateScope().getID().equals(AllUpdateScope.ALL_UPDATE_SCOPE_ID)) {
    	            CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);
    	            countingRm.setDoneCount(2);
  	        	    new MultiLevelUpdateHandler(getExecutor(), proxyStrategyExtension, getPresentationContext(), this, countingRm).
  	        	        startUpdate();
                    AbstractCachingVMProvider.super.handleEvent(proxyStrategy, event, countingRm);
    	        } else {
                    // block updating only the viewport
                    CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);
                    countingRm.setDoneCount(2);
    	        	
    	        	TreeViewer viewer = (TreeViewer) proxyStrategyExtension.getViewer();
    	        	Tree tree = viewer.getTree();
    	        	int count = tree.getSize().y / tree.getItemHeight();
    	        	
    	        	TreeItem topItem = tree.getTopItem();
    	        	int index = computeTreeIndex(topItem);
    	        	
    	        	MultiLevelUpdateHandler handler = new MultiLevelUpdateHandler(
    	        			getExecutor(), proxyStrategyExtension, getPresentationContext(), this, countingRm);
    	        	handler.setRange(index, index + count);
					handler.startUpdate();
                    AbstractCachingVMProvider.super.handleEvent(proxyStrategy, event, countingRm);
    	        }
            } else {
            	if(this.getActiveUpdateScope().getID().equals(AllUpdateScope.ALL_UPDATE_SCOPE_ID))
    	        {
            		final CountingRequestMonitor multiRm = new CountingRequestMonitor(getExecutor(), rm);
                    multiRm.setDoneCount(2);
            		
    	        	MultiLevelUpdateHandler handler = new MultiLevelUpdateHandler(
    	        			getExecutor(), proxyStrategyExtension, getPresentationContext(), this, multiRm);
					handler.startUpdate();

					super.handleEvent(proxyStrategy, event, multiRm);
    	        } else {
    	        	super.handleEvent(proxyStrategy, event, rm);
    	        }
            }
        } else {
            super.handleEvent(proxyStrategy, event, rm);
        }
    }
        
	private static int computeTreeIndex(TreeItem child) {
		if (child != null) {
			if(child.getParentItem() != null) {
				int previous = 0;
				int index = child.getParentItem().indexOf(child);
				while (--index >= 0) {
					previous += computeTreeExtent(child.getParentItem().getItem(index));
				}
				return computeTreeIndex(child.getParentItem()) + previous;
			} else {
				int previous = 0;
				int index = child.getParent().indexOf(child);
				while (--index >= 0) {
					previous += computeTreeExtent(child.getParent().getItem(index));
				}
				return previous;
			}
		}
		return 0;
	}

	private static int computeTreeExtent(TreeItem item) {
		int extent = 1;
		if (item.getExpanded()) {
			for (TreeItem i : item.getItems()) {
				extent += computeTreeExtent(i);
			}
		}
		return extent;
	}
	
    /**
     * Override default implementation to avoid automatically removing disposed proxies from
     * list of active proxies.  The caching provider only removes a proxy after its root element
     * is no longer in the cache.
     */
    @Override
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        // Iterate through the current active proxies to try to find a proxy with the same
        // element and re-use it if found.  At the same time purge proxies that are no longer
        IVMModelProxy proxy = null;
        for (Iterator<IVMModelProxy> itr = getActiveModelProxies().iterator(); itr.hasNext();) {
            IVMModelProxy next = itr.next();
            if (next != null && next.getRootElement().equals(element)) {
                proxy = next;
                break;
            }
        }
        if (proxy == null) {
            proxy = createModelProxyStrategy(element);
            getActiveModelProxies().add(proxy);
        } else if (proxy.isDisposed()) {
            // DSF is capable of re-using old proxies which were previously 
            // disposed.  However, the viewer which installs a proxy using
            // a background job to install the proxy calls 
            // IModelProxy.isDisposed(), to check whether the proxy was disposed
            // before it could be installed.  We need to clear the disposed flag
            // of the re-used proxy here, otherwise the proxy will never get used.
            // Calling init here will cause the init() method to be called twice
            // so the IVMModelProxy needs to be prepared for that.
            // See bug 241024.
            proxy.init(context);
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
        
        return new ElementDataKey(rootElement, node, update.getViewerInput(), update.getElementPath());
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
        
        // Update the root element marker:
        // - ensure that the root marker is root markers' map,
        // - ensure that the root marker is in the cache map,
        // - and ensure that it's at the end of the cache. 
        RootElementMarkerKey rootMarker = fRootMarkers.get(key.fRootElement);
        if (rootMarker == null) {
            rootMarker = new RootElementMarkerKey(key.fRootElement);
            fRootMarkers.put(key.fRootElement, rootMarker);
        }
        Entry rootMarkerEntry = fCacheData.get(rootMarker);
        if (rootMarkerEntry == null) {
            rootMarkerEntry = new RootElementMarkerEntry(rootMarker);
            addEntry(rootMarker, rootMarkerEntry); 
        } else if (rootMarkerEntry.fNext != fCacheListHead) {
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
    public void getModelData(final IVMNode node, final IViewerUpdate update, final IDMService service, final IDMContext dmc, 
        final DataRequestMonitor rm, final Executor executor)
    {
    	// Determine if this request is being issues on the a VM executor thread. If so
    	// then we do not need to create a new one to insure data integrity.
    	Executor vmExecutor = getExecutor();
    	if ( vmExecutor instanceof SimpleDisplayExecutor && 
    	     Display.getDefault().getThread() == Thread.currentThread() ) 
    	{
    		getCacheModelData(node, update, service, dmc, rm, executor );
    	} else {
    		vmExecutor.execute(new DsfRunnable() {
    			public void run() {
    				getCacheModelData(node, update, service, dmc, rm, executor );
    			}
    		});
    	}
    }
    
    private void getCacheModelData(final IVMNode node, final IViewerUpdate update, final IDMService service, final IDMContext dmc, 
            final DataRequestMonitor rm, final Executor executor)
    {
    	ElementDataKey key = makeEntryKey(node, update);
    	final ElementDataEntry entry = getElementDataEntry(key);
    	/*if (entry.fDirty) {
            rm.setStatus(Status.CANCEL_STATUS);
            rm.done();
        } else */{
        	Object dataOrStatus = entry.fDataOrStatus.get(dmc);
        	if(dataOrStatus != null) {
        		if (dataOrStatus instanceof IDMData) {
        			rm.setData( dataOrStatus );
        		} else {
        			rm.setStatus((IStatus)dataOrStatus );
        		}
        		rm.done();
        	} else {
        		// Determine if we are already running on a DSF executor thread. if so then
        		// we do not need to create a new one to issue the request to the service.
        		DsfExecutor dsfExecutor = service.getExecutor();
        		if ( dsfExecutor.isInExecutorThread() ) {
        			getModelDataFromService(node, update, service, dmc, rm, executor, entry );
        		}
        		else {
        			dsfExecutor.execute(new DsfRunnable() {
        				public void run() {
        					getModelDataFromService(node, update, service, dmc, rm, executor, entry );
        				}
        			});
        		}
        	}
        }
    }
    
    private void getModelDataFromService(final IVMNode node, final IViewerUpdate update, final IDMService service, final IDMContext dmc, 
            final DataRequestMonitor rm, final Executor executor, final ElementDataEntry entry)
    {
    	service.getModelData(
    		dmc,
			new ViewerDataRequestMonitor<IDMData>(executor, update) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					entry.fDataOrStatus.put(dmc, getData());
					rm.setData(getData());
				} else {
					if (!isCanceled()) {
						entry.fDataOrStatus.put(dmc, getStatus());
					}
					rm.setStatus(getStatus());
				}
				rm.done();
			}
		});
    }
    
    /**
     * Retrieves the deprecated IDMData object for the given IDMContext.  This 
     * method should be removed once the use of IDMData is replaced with 
     * {@link IElementPropertiesProvider}.
     */
    @Deprecated
    public IDMData getArchivedModelData(IVMNode node, IViewerUpdate update, IDMContext dmc) {
        ElementDataKey key = makeEntryKey(node, update);
        final Entry entry = fCacheData.get(key);
        if ( entry instanceof ElementDataEntry) {
            Map<IDMContext,IDMData> archiveData = ((ElementDataEntry)entry).fArchiveData; 
            if (archiveData != null) {
                return archiveData.get(dmc);
            }
        }
        return null;
    }

    /**
     * @since 1.1
     */
    public IVMUpdateScope[] getAvailableUpdateScopes() {
        return fAvailableUpdateScopes;
    }

    /**
     * @since 1.1
     */
    public IVMUpdateScope getActiveUpdateScope() {
        String updateScopeId = (String)getPresentationContext().getProperty(SELECTED_UPDATE_SCOPE);
        if (updateScopeId != null) {
            for (IVMUpdateScope updateScope : getAvailableUpdateScopes()) {
                if (updateScope.getID().equals(updateScopeId)) {
                    return updateScope;
                }
            }
        }
        
        // Default to the first one.
        return getAvailableUpdateScopes()[0];
    }

    /**
     * @since 1.1
     */
    public void setActiveUpdateScope(IVMUpdateScope updateScope) {
        getPresentationContext().setProperty(SELECTED_UPDATE_SCOPE, updateScope.getID());
    }

    @Override
    public boolean shouldWaitHandleEventToComplete() {
        return fDelayEventHandleForViewUpdate;
    }

    /**
     * @since 1.1
     */
	protected void setDelayEventHandleForViewUpdate(boolean on) {
		fDelayEventHandleForViewUpdate = on;
	}

}
