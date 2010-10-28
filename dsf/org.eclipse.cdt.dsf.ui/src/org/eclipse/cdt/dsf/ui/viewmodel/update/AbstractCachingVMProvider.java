/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.internal.LoggingUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenCountUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMHasChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesUpdateStatus;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMPropertiesUpdate;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;

/**
 * Base implementation of a caching view model provider.
 * 
 * @since 1.0
 */
public class AbstractCachingVMProvider extends AbstractVMProvider 
    implements ICachingVMProvider, IElementPropertiesProvider, ICachingVMProviderExtension2 
{
    /**
     * @since 2.0
     */
    private final static String PROP_UPDATE_STATUS = "org.eclipse.cdt.dsf.ui.viewmodel.update.update_status";  //$NON-NLS-1$
    
    /**
     * @since 2.0
     */
    private final static int LENGTH_PROP_IS_CHANGED_PREFIX = PROP_IS_CHANGED_PREFIX.length();
    
	private boolean fDelayEventHandleForViewUpdate = false;
	
	// debug flag
    static boolean DEBUG_CACHE = false;

    static {
        DEBUG_CACHE = DsfUIPlugin.DEBUG && "true".equals( //$NON-NLS-1$
         Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/vm/cache")); //$NON-NLS-1$
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

        void reinsert(Entry nextEntry) {
            fPrevious.fNext = fNext;
            fNext.fPrevious = fPrevious;

            fNext  = nextEntry;
            fPrevious = nextEntry.fPrevious;
            fPrevious.fNext = this;
            fNext.fPrevious = this;
        }
    }
    
    /**
     * Entry with cached element data. 
     */
    private static class ElementDataEntry extends Entry implements ICacheEntry {
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
         * already cached.
         */
        boolean fAllChildrenKnown = false;
        
        /**
         * Map containing children of this element, keyed by child index.
         */
        Map<Integer,Object> fChildren = null;
        
        /**
         * Map containing element properties.
         * 
         * @since 2.0
         */
        Map<String, Object> fProperties = null;

        /**
         * Previous known element properties.
         * 
         * @since 2.0
         */
        Map<String, Object> fArchiveProperties = null;

		/**
		 * Ensure this cache entry has a map in which to hold the children
		 * elements. If it doesn't, create one and give it an initial capacity
		 * of 30% more than the number of children we know the parent currently
		 * has (give it some room to grow). If we don't know the child count,
		 * give the map some nominal initial capacity.
		 */
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
                ", properties=" + fProperties + //$NON-NLS-1$ 
                ", oldProperties=" + fArchiveProperties + "]"; //$NON-NLS-1$ //$NON-NLS-2$ 
        }

        public IVMNode getNode() { return ((ElementDataKey)fKey).fNode; }
        public Object getViewerInput() { return ((ElementDataKey)fKey).fViewerInput; }
        public TreePath getElementPath() { return ((ElementDataKey)fKey).fPath; }
        public boolean isDirty() { return fDirty; }
        public Boolean getHasChildren() { return fHasChildren; }
        public Integer getChildCount() { return fChildrenCount; }
        public Map<Integer, Object> getChildren() { return fChildren; }
        public Map<String, Object> getProperties() { return fProperties; }
        public java.util.Map<String,Object> getArchiveProperties() { return fArchiveProperties; }
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
        
        Collection<String> getPropertiesToFlush(ElementDataKey key, boolean isDirty) {
            if (fRootElement.equals(key.fRootElement) && fElementTester instanceof IElementUpdateTesterExtension) {
                return ((IElementUpdateTesterExtension)fElementTester).
                getPropertiesToFlush(key.fViewerInput, key.fPath, isDirty);
            } 
            return null;
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
        
    protected static String SELECTED_UPDATE_MODE = "org.eclipse.cdt.dsf.ui.viewmodel.update.selectedUpdateMode";  //$NON-NLS-1$
    /**
     * @since 1.1
     */
    protected static String SELECTED_UPDATE_SCOPE = "org.eclipse.cdt.dsf.ui.viewmodel.update.selectedUpdateScope";  //$NON-NLS-1$

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

        // Repaint the view to allow elements using the PROP_UPDATE_POLICY_ID 
        // property to repaint themselves.
        for (final IVMModelProxy proxyStrategy : getActiveModelProxies()) {
            if (!proxyStrategy.isDisposed()) {
                proxyStrategy.fireModelChanged(new  ModelDelta(proxyStrategy.getRootElement(), IModelDelta.CONTENT));
            }
        }
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
    
    public ICacheEntry getCacheEntry(IVMNode node, Object viewerInput, TreePath path) {
        ElementDataKey key = makeEntryKey(node, viewerInput, path);
        return getElementDataEntry(key, false);
    }
    
    @Override
    public void updateNode(final IVMNode node, IHasChildrenUpdate[] updates) {
        LinkedList <IHasChildrenUpdate> missUpdates = new LinkedList<IHasChildrenUpdate>();
        for(final IHasChildrenUpdate update : updates) {
            // Find or create the cache entry for the element of this update.
            ElementDataKey key = makeEntryKey(node, update);
            final ElementDataEntry entry = getElementDataEntry(key, true);
            updateRootElementMarker(key.fRootElement, node, update);
            
            // Check if the cache entry has this request result cached. 
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
                                // Update completed.  Write value to cache only if update succeeded 
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
        final ElementDataEntry entry = getElementDataEntry(key, true);
        updateRootElementMarker(key.fRootElement, node, update);
        
        // Check if the cache entry has this request result cached. 
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
                        // Update completed.  Write value to cache only if update succeeded 
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
        final ElementDataEntry entry = getElementDataEntry(key, true);
        updateRootElementMarker(key.fRootElement, node, update);
        
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
                // Note: it is possible that entry.fAllChildrenKnown == true at this point.
                // This can happen if the node's has children implementation returns true
                // while the actual children update returns with no elements.  A node 
                // may do this for optimization reasons.  I.e. sometimes it may be more
                // efficient to ask the user to expand a node to see if it has any
                // children.
            	
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
                // All children were found in cache.  Complete the update.
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
                ElementDataKey elementDataKey = (ElementDataKey)elementDataEntry.fKey;
                int updateFlags = flushKey.getUpdateFlags(elementDataKey);
                if ((updateFlags & IVMUpdatePolicy.FLUSH) != 0) {
                    if ((updateFlags & IVMUpdatePolicy.ARCHIVE) == IVMUpdatePolicy.ARCHIVE) {
                        // We are saving current data for change history, check if the data is valid.
                        // If it valid, save it for archive, if it's not valid old archive data will be used
                        // if there is any.  And if there is no old archive data, just remove the cache entry.
                        if (elementDataEntry.fProperties != null) {
                            elementDataEntry.fArchiveProperties = elementDataEntry.fProperties;
                        }
                        elementDataEntry.fProperties = null;
                        
                        // There is no archived data, which means that this entry is empty, so remove it from cache 
                        // completely.
                        if (elementDataEntry.fArchiveProperties == null) {
                            fCacheData.remove(entry.fKey);
                            entry.remove();
                        }                        
                    } else {
                        // We are not changing the archived data.  If archive data exists in the entry, leave it.
                        // Otherwise remove the whole entry.
                        if (elementDataEntry.fArchiveProperties != null) {
                            elementDataEntry.fProperties = null;
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
                } else if ((updateFlags & IVMUpdatePolicy.FLUSH_ALL_PROPERTIES) != 0) {
                	elementDataEntry.fProperties = null;
                } else if ((updateFlags & IVMUpdatePolicy.FLUSH_PARTIAL_PROPERTIES) != 0) {
                    Collection<String> propertiesToFlush = flushKey.getPropertiesToFlush(elementDataKey, elementDataEntry.fDirty);
                    if (propertiesToFlush != null && elementDataEntry.fProperties != null) {
                        elementDataEntry.fProperties.keySet().removeAll(propertiesToFlush);
                    }
                } else if ((updateFlags & IVMUpdatePolicy.DIRTY) != 0) {
                    elementDataEntry.fDirty = true;
                    if (elementDataEntry.fProperties != null) {
                        elementDataEntry.fProperties.put(PROP_CACHE_ENTRY_DIRTY, Boolean.TRUE);
                    }
                }
            }
            entry = entry.fPrevious;
        }
        
        // Insert a marker for this flush operation.
        Entry flushMarkerEntry = new Entry(flushKey);
        fCacheData.put(flushKey, flushMarkerEntry);
        flushMarkerEntry.insert(fCacheListHead);
    }

    /**
     * Listener used to detect when the viewer is finished updating itself 
     * after a model event.  The  
     */
    // Warnings for use of ITreeModelViewer.  ITreeModelViewer is an internal 
    // interface in platform, but it is more generic than the public TreeModelViewer.
    // Using ITreeModelViewer will allow us to write unit tests using the 
    // VirtualTreeModelViewer.
    @SuppressWarnings("restriction") 
    private class ViewUpdateFinishedListener implements IViewerUpdateListener, IModelChangedListener {
        private final org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer fViewer;
        private boolean fViewerChangeStarted = false;
        private RequestMonitor fRm;
        
        ViewUpdateFinishedListener(org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer viewer) {
            fViewer = viewer;
        }

        private void start(RequestMonitor rm) {
            synchronized(this) {
                fViewer.addModelChangedListener(this);
                fViewer.addViewerUpdateListener(this);
                fRm = rm;
            }
        }
        
        public synchronized void viewerUpdatesComplete() {
            done();
        }

        public void modelChanged(IModelDelta delta, IModelProxy proxy) {
            synchronized (this) {
                if (!fViewerChangeStarted) {
                    done();
                }
            }
        }
        
        public void viewerUpdatesBegin() {
            synchronized(this) {
                fViewerChangeStarted = true;
            }
        }
        
        private synchronized void done() {
            if (fRm != null) {
                fRm.done();
                fViewer.removeViewerUpdateListener(this);
                fViewer.removeModelChangedListener(this);
            }
        }
        
        public void updateStarted(IViewerUpdate update) {}
        public void updateComplete(IViewerUpdate update) {}
        
    }
    
    @Override
    protected void handleEvent(final IVMModelProxy proxyStrategy, final Object event, final RequestMonitor rm) {   
        IElementUpdateTester elementTester =  getActiveUpdatePolicy().getElementUpdateTester(event);
   
        flush(new FlushMarkerKey(proxyStrategy.getRootElement(), elementTester));
        
        if (!proxyStrategy.isDisposed()) {
            if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                trace(event, null, proxyStrategy, EventHandlerAction.processing);
            }
            proxyStrategy.createDelta(
                event, 
                new DataRequestMonitor<IModelDelta>(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                        if (DEBUG_DELTA && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                            trace(event, null, proxyStrategy, EventHandlerAction.firedDeltaFor);
                        }

                        // If we need to wait for the view to finish updating, then before posting the delta to the 
                        // viewer install a listener, which will in turn call rm.done().
                        if (fDelayEventHandleForViewUpdate) {
                            @SuppressWarnings("restriction")
                            org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer viewer = 
                                (org.eclipse.debug.internal.ui.viewers.model.ITreeModelViewer) proxyStrategy.getViewer();
                            new ViewUpdateFinishedListener(viewer).start(rm);
                        }
                        
                        proxyStrategy.fireModelChanged(getData());
                        
                        if (!fDelayEventHandleForViewUpdate) {
                            rm.done();
                        }
                    }
                    @Override public String toString() {
                        return "Result of a delta for event: '" + event.toString() + "' in VMP: '" + AbstractCachingVMProvider.this + "'" + "\n" + getData().toString();  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }   
                });
        } else {
            rm.done();
        }
    }
        
    /**
     * Override default implementation to avoid automatically removing disposed proxies from
     * list of active proxies.  The caching provider only removes a proxy after its root element
     * is no longer in the cache.
     */
    @Override
    public IModelProxy createModelProxy(Object element, IPresentationContext context) {
        // Iterate through the current active proxies to try to find a proxy with the same
        // element and re-use it if found.  Only disposed proxies can be re-used because
        // multiple viewers cannot use the same proxy.
        // 
        // Unlike in the base class, do not remove proxies just because they were disposed 
        // by the viewer.  These proxies can contain modification history for variables in 
        // their cache.  The proxies will be removed once their cache entries are emptied.  
        // See rootElementRemovedFromCache().
        IVMModelProxy proxy = null;
        for (Iterator<IVMModelProxy> itr = getActiveModelProxies().iterator(); itr.hasNext();) {
            IVMModelProxy next = itr.next();
            if (next != null && next.getRootElement().equals(element) && next.isDisposed()) {
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
	 * Called when all cache entries for the given root element have been
	 * removed from the cache. In order to properly track changed elements, the
	 * caching VM provider does not immediately forget about the proxy for a
	 * given root element when the viewer root element changes. Instead it holds
	 * on to the proxy and keeps processing deltas for that root element until
	 * the cache entries for this element are gone.
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
     * Convenience class that searches for the root element for the given
     * update and creates an element cache entry key.
     */
    private ElementDataKey makeEntryKey(IVMNode node, IViewerUpdate update) {
        return makeEntryKey(node, update.getViewerInput(), update.getElementPath());
    }
    
    /**
     * Convenience class that searches for the root element for the given
     * update and creates an element cache entry key.
     */
    private ElementDataKey makeEntryKey(IVMNode node, Object viewerInput, TreePath path) {
        Object rootElement = viewerInput;  // Default
        outer: for (IVMModelProxy proxy : getActiveModelProxies()) {
            Object proxyRoot = proxy.getRootElement();
            if (proxyRoot.equals(viewerInput)) {
                rootElement = proxyRoot;
                break;
            }
            for (int i = 0; i < path.getSegmentCount(); i++) {
                if (proxyRoot.equals(path.getSegment(i))) {
                    rootElement = proxyRoot;
                    break outer;
                }
            }
        }
        
        return new ElementDataKey(rootElement, node, viewerInput, path);
    }

    
    /**
     * This is the only method that should be used to access a cache entry.  
     * It creates a new entry if needed and it maintains the ordering in 
     * the least-recently-used linked list.   
     * @param create Create the entry if needed.
     * @return cache element entry, may be <code>null</code> if entry does 
     * not exist and the create parameter is <code>false</code>
     */
    private ElementDataEntry getElementDataEntry(ElementDataKey key, boolean create) {
        assert key != null;
        ElementDataEntry entry = (ElementDataEntry)fCacheData.get(key);
        if (entry != null) {
            // Entry exists, move it to the end of the list.
            entry.reinsert(fCacheListHead);
        } else if (create) {
            // Create a new entry and add it to the end of the list.
            entry = new ElementDataEntry(key);
            addEntry(key, entry);
        }
        return entry;
    }

    private void updateRootElementMarker(Object rootElement, IVMNode node, IViewerUpdate update) {
        boolean created = false;
        // Update the root element marker:
        // - ensure that the root marker is root markers' map,
        // - ensure that the root marker is in the cache map,
        // - and ensure that it's at the end of the cache. 
        RootElementMarkerKey rootMarker = fRootMarkers.get(rootElement);
        if (rootMarker == null) {
            rootMarker = new RootElementMarkerKey(rootElement);
            fRootMarkers.put(rootElement, rootMarker);
            created = true;
        }
        Entry rootMarkerEntry = fCacheData.get(rootMarker);
        if (rootMarkerEntry == null) {
            rootMarkerEntry = new RootElementMarkerEntry(rootMarker);
            addEntry(rootMarker, rootMarkerEntry); 
        } else if (rootMarkerEntry.fNext != fCacheListHead) {
            rootMarkerEntry.reinsert(fCacheListHead);
        }        
        
        if (created) {
            ElementDataKey rootElementDataKey = 
                new ElementDataKey(rootElement, node, update.getViewerInput(), update.getElementPath());
            ElementDataEntry entry = getElementDataEntry(rootElementDataKey, false);
            
            Object[] rootElementChildren = getActiveUpdatePolicy().getInitialRootElementChildren(rootElement);
            if (rootElementChildren != null) {
                entry.fHasChildren = rootElementChildren.length > 0;
                entry.fChildrenCount = rootElementChildren.length;
                entry.fChildren = new HashMap<Integer, Object>(entry.fChildrenCount * 4/3);
                for (int i = 0; i < rootElementChildren.length; i++) {
                    entry.fChildren.put(i, rootElementChildren[i]);
                }
                entry.fAllChildrenKnown = true;
                entry.fDirty = true;
            }
            
            Map<String, Object> rootElementProperties = getActiveUpdatePolicy().getInitialRootElementProperties(rootElement);
            
            if (rootElementProperties != null) {
                entry.fProperties = new HashMap<String, Object>((rootElementProperties.size() + 1) * 4/3);
                entry.fProperties.putAll(rootElementProperties);
                entry.fProperties.put(PROP_CACHE_ENTRY_DIRTY, true);
                entry.fDirty = true;
            }
        }
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
     * @since 2.0
     */
    public void update(IPropertiesUpdate[] updates) {
        if (updates.length == 0)
            return;

        // Optimization: if all the updates belong to the same node, avoid
        // creating any new lists/arrays.
        boolean allNodesTheSame = true;
        IVMNode firstNode = getNodeForElement(updates[0].getElement());
        for (int i = 1; i < updates.length; i++) {
            if (firstNode != getNodeForElement(updates[i].getElement())) {
                allNodesTheSame = false;
                break;
            }
        }

        if (allNodesTheSame) {
            if ( !(firstNode instanceof IElementPropertiesProvider) ) {
                for (IPropertiesUpdate update : updates) {
                    update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Element is not a VM Context or its node is not a properties provider.", null)); //$NON-NLS-1$
                    update.done();
                }
            } else {
                updateNode(firstNode, updates);
            }
        } else {
            // Sort the updates by the node.
            Map<IVMNode, List<IPropertiesUpdate>> nodeUpdatesMap = new HashMap<IVMNode, List<IPropertiesUpdate>>();
            for (IPropertiesUpdate update : updates) {
                // Get the VM Context for last element in path.
                IVMNode node = getNodeForElement(update.getElement());
                if ( node == null || !(node instanceof IElementPropertiesProvider) ) {
                    // Misdirected update.
                    update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Element is not a VM Context or its node is not a properties provider.", null)); //$NON-NLS-1$
                    update.done();
                    continue;
                }
                if (!nodeUpdatesMap.containsKey(node)) {
                    nodeUpdatesMap.put(node, new ArrayList<IPropertiesUpdate>());
                }
                nodeUpdatesMap.get(node).add(update);
            }

            // Iterate through the nodes in the sorted map.
            for (IVMNode node : nodeUpdatesMap.keySet()) {
                updateNode(node, nodeUpdatesMap.get(node).toArray(
                    new IPropertiesUpdate[nodeUpdatesMap.get(node).size()]));
            }
        }
    }
    
    /**
     * Convenience method that finds the VM node corresponding to given element.
     * It returns <code>null</code> if the element is not a VM context.
     * 
     * @param element Element to find the VM Node for.
     * @return View Model Node that this element was created by, or <code>null</code>.
     * 
     * @since 2.0
     */
    private IVMNode getNodeForElement(Object element) {
        if (element instanceof IVMContext) {
            return ((IVMContext) element).getVMNode();
        }
        return null;
    }

    
    protected void updateNode(final IVMNode node, IPropertiesUpdate[] updates) {
        LinkedList <IPropertiesUpdate> missUpdates = new LinkedList<IPropertiesUpdate>();
        for(final IPropertiesUpdate update : updates) {
            // Find or create the cache entry for the element of this update.
            ElementDataKey key = makeEntryKey(node, update);
            final ElementDataEntry entry = getElementDataEntry(key, true);
            updateRootElementMarker(key.fRootElement, node, update);
            
            // The request can be retrieved from cache if all the properties that were requested in the update are 
            // found in the map.
            if (entry.fProperties != null && entry.fProperties.keySet().containsAll(update.getProperties())) {
                // Cache Hit!  Just return the value.
                if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                    DsfUIPlugin.debug("cacheHitProperties(node = " + node + ", update = " + update + ", " + entry.fProperties + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                if (entry.fProperties.containsKey(PROP_UPDATE_POLICY_ID)) {
                    entry.fProperties.put(PROP_UPDATE_POLICY_ID, getActiveUpdatePolicy().getID());
                }
                update.setAllProperties(entry.fProperties);
                update.setStatus((IStatus)entry.fProperties.get(PROP_UPDATE_STATUS));
                update.done();
            } else {
                // Cache miss!  Check if already cached properties can be re-used. 
                Set<String> missingProperties = null;
                if (entry.fProperties != null) {
                    missingProperties = new HashSet<String>(update.getProperties().size() * 4/3);
                    missingProperties.addAll(update.getProperties());
                    missingProperties.removeAll(entry.fProperties.keySet());
                    
                    if (entry.fDirty) {
                        // Cache miss, BUT the entry is dirty already.  Determine which properties can still be updated 
                        // (if any), then request the missing properties from node, or return an error.
                        if (getActiveUpdatePolicy() instanceof IVMUpdatePolicyExtension) {
                            IVMUpdatePolicyExtension updatePolicyExt = (IVMUpdatePolicyExtension)getActiveUpdatePolicy();
                            for (Iterator<String> itr = missingProperties.iterator(); itr.hasNext();) {
                                String missingProperty = itr.next();
                                if ( !updatePolicyExt.canUpdateDirtyProperty(entry, missingProperty) ) {
                                    itr.remove();
                                    PropertiesUpdateStatus.makePropertiesStatus(update.getStatus()).setStatus(
                                        missingProperty, 
                                        DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_STATE, "Cache contains stale data.  Refresh view.", null ));//$NON-NLS-1$                                    
                                }
                            }
                        } else {
                            PropertiesUpdateStatus.makePropertiesStatus(update.getStatus()).setStatus(
                                missingProperties.toArray(new String[missingProperties.size()]), 
                                DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_STATE, "Cache contains stale data.  Refresh view.", null ));//$NON-NLS-1$                                    
                            missingProperties.clear();
                        }
                        if (missingProperties.isEmpty()) {
                            if (entry.fProperties.containsKey(PROP_UPDATE_POLICY_ID)) {
                                entry.fProperties.put(PROP_UPDATE_POLICY_ID, getActiveUpdatePolicy().getID());
                            }
                            update.setAllProperties(entry.fProperties);
                            update.done();
                            return;
                        }
                    }
                } else {
                    missingProperties = update.getProperties();
                }
                
                final Set<String> _missingProperties = missingProperties;
                // Save the flush counter of the entry and create a proxy update.
                final int flushCounter = entry.fFlushCounter;
                missUpdates.add(new VMPropertiesUpdate(
                    missingProperties,
                    update, 
                    new ViewerDataRequestMonitor<Map<String, Object>>(getExecutor(), update) {
                        @Override
                        protected void handleCompleted() {
                            PropertiesUpdateStatus missUpdateStatus = PropertiesUpdateStatus.makePropertiesStatus(getStatus());
                            Map<String, Object> cachedProperties;
                            PropertiesUpdateStatus cachedStatus;
                            if (!isCanceled() && flushCounter == entry.fFlushCounter) {
                                // We are caching the result of this update.  Copy the properties from the update
                                // to the cached properties map.
                                if (entry.fProperties == null) {
                                    entry.fProperties = new HashMap<String, Object>((getData().size() + 3) * 4/3);
                                    if (update.getProperties().contains(PROP_CACHE_ENTRY_DIRTY)) {
                                        entry.fProperties.put(PROP_CACHE_ENTRY_DIRTY, entry.fDirty);
                                    }
                                    entry.fProperties.put(PROP_UPDATE_STATUS, new PropertiesUpdateStatus());
                                } 
                                cachedProperties = entry.fProperties;
                                cachedProperties.putAll(getData());
                                
								// Make sure that all the properties that were requested by the update object are in 
                                // the cache entry's properties map. It's possible he ViewerDataRequestMonitor was able 
                                // to provide us only a subset of the requested ones. We want to prevent that from 
                                // causing future cache misses, since a cache hit requires the cache entry to contain 
                                // all requested properties. Use a null value for the missing items.
                                for (String property : _missingProperties) {
                                    if (!getData().containsKey(property)) {
                                        cachedProperties.put(property, null);
                                    }
                                }
                                
                                // Merge status from properties that came back from the node into the status that's in 
                                // the cache. 
                                cachedStatus = (PropertiesUpdateStatus)cachedProperties.get(PROP_UPDATE_STATUS);
                                cachedStatus = PropertiesUpdateStatus.mergePropertiesStatus(
                                    cachedStatus, missUpdateStatus, _missingProperties);
                                cachedProperties.put(PROP_UPDATE_STATUS, cachedStatus);
                            } else {
                                // We are not caching the result of this update, but we should still return valid data 
                                // to the client.  In case the update was canceled we can also return valid data to the 
                                // client even if the client is likely to ignore it since the cost of doing so is 
                                // relatively low.
                                // Create a temporary cached properties map and add existing cache and node update 
                                // properties to it.
                                if (entry.fProperties != null) {
                                    cachedProperties = new HashMap<String, Object>((entry.fProperties.size() + getData().size() + 3) * 4/3);
                                    cachedProperties.putAll(entry.fProperties);
                                    cachedStatus = PropertiesUpdateStatus.mergePropertiesStatus(
                                        (PropertiesUpdateStatus)cachedProperties.get(PROP_UPDATE_STATUS), 
                                        missUpdateStatus, _missingProperties);
                                } else {
                                    cachedProperties = new HashMap<String, Object>((getData().size() + 3) * 4/3);
                                    cachedStatus = missUpdateStatus;
                                }
                                cachedProperties.putAll(getData());
                                cachedProperties.put(PROP_UPDATE_STATUS, missUpdateStatus);
                                if (update.getProperties().contains(PROP_CACHE_ENTRY_DIRTY)) {
                                    cachedProperties.put(PROP_CACHE_ENTRY_DIRTY, Boolean.TRUE);
                                }
                            }
                            
                            // Refresh the update policy property.
                            if (update.getProperties().contains(PROP_UPDATE_POLICY_ID)) {
                                cachedProperties.put(PROP_UPDATE_POLICY_ID, getActiveUpdatePolicy().getID());
                            }
                            
                            // If there is archive data available, calculate the requested changed value properties.
                            // Do not calculate the changed flags if the entry has been flushed. 
                            if (entry.fArchiveProperties != null && flushCounter == entry.fFlushCounter) {
                                for (String updateProperty : update.getProperties()) {
                                    if (updateProperty.startsWith(PROP_IS_CHANGED_PREFIX)) {
                                        String changedPropertyName = updateProperty.substring(LENGTH_PROP_IS_CHANGED_PREFIX);
                                        Object newValue = cachedProperties.get(changedPropertyName);
                                        Object oldValue = entry.fArchiveProperties.get(changedPropertyName);
                                        if (oldValue != null) {
                                            cachedProperties.put(updateProperty, !oldValue.equals(newValue));
                                        }
                                    }
                                }
                            }
                            
                            if (DEBUG_CACHE && (DEBUG_PRESENTATION_ID == null || getPresentationContext().getId().equals(DEBUG_PRESENTATION_ID))) {
                                DsfUIPlugin.debug("cacheSavedProperties(node = " + node + ", update = " + update + ", " + getData() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            }
                            
                            // Fill in requested properties and status into the update.
                            for (String property : update.getProperties()) {
                                update.setProperty(property, cachedProperties.get(property));
                            }
                            PropertiesUpdateStatus updateStatus = PropertiesUpdateStatus.makePropertiesStatus(update.getStatus()); 
                            updateStatus = PropertiesUpdateStatus.mergePropertiesStatus(
                                updateStatus, cachedStatus, update.getProperties());
                            update.setStatus(updateStatus);
                            update.done();
                        }
                    }));
            }
        }
        
        // Issue all the update proxies with one call.
        if (!missUpdates.isEmpty()) {
            ((IElementPropertiesProvider)node).update(missUpdates.toArray(new IPropertiesUpdate[missUpdates.size()]));
        }
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

    /**
     * Used for tracing event handling
     * <p>
     * Note: this enum is duplicated from AbstractVMProvider.
     */
    private enum EventHandlerAction {
        received,
        queued,
        processing,
        firedDeltaFor,
        skipped,
        canceled
    }

    /**
     * Trace that we've reached a particular phase of the handling of an event
     * for a particular proxy.
     * <p>
     * Note: this method is duplicated from AbstractVMProvider.
     * 
     * @param event
     *            the event being handled
     * @param skippedOrCanceledEvent
     *            for a 'skip' or 'cancel' action, this is the event that is
     *            being dismissed. Otherwise null
     * @param proxy
     *            the target proxy; n/a (null) for a 'received' action.
     * @param action
     *            what phased of the event handling has beeb reached
     */
    private void trace(Object event, Object skippedOrCanceledEvent, IVMModelProxy proxy, EventHandlerAction action) {
        assert DEBUG_DELTA;
        StringBuilder str = new StringBuilder();
        str.append(DsfPlugin.getDebugTime());
        str.append(' ');
        if (action == EventHandlerAction.skipped || action == EventHandlerAction.canceled) {
            str.append(LoggingUtils.toString(this) + " " + action.toString() + " event " + LoggingUtils.toString(skippedOrCanceledEvent) + " because of event " + LoggingUtils.toString(event)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$          
        }
        else {
            str.append(LoggingUtils.toString(this) + " " + action.toString() + " event " + LoggingUtils.toString(event)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (action != EventHandlerAction.received) {
            str.append(" for proxy " + LoggingUtils.toString(proxy) + ", whose root is " + LoggingUtils.toString(proxy.getRootElement())); //$NON-NLS-1$ //$NON-NLS-2$
        }
        DsfUIPlugin.debug(str.toString());
    }

}
