/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.IPositionTrackerManager;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class PositionTrackerManager implements IPositionTrackerManager, IFileBufferListener {
    private static final int HASHMAP_ENTRY_SIZE = 56;
    private static final int MAX_MEMORY= 1024*512; // 512 kbytes
    private static final int MAX_MEMORY_AFTER_CLEANUP= (MAX_MEMORY * 7) / 10; // 70% of MAX_MEMORY
    
    private static PositionTrackerManager sManager= new PositionTrackerManager();
    public  static PositionTrackerManager getInstance() {
        return sManager;
    }

    private int fMemoryCounter= 0;
    private int fInstalled= 0;
    private HashMap fPositionTrackerMap;
    
    private PositionTrackerManager() {
        fPositionTrackerMap= new HashMap();
    }

    public synchronized void install() {
        if (++fInstalled == 1) {
            ITextFileBufferManager mgr= FileBuffers.getTextFileBufferManager();
            mgr.addFileBufferListener(this);
        }
    }
    
    public synchronized void uninstall() {
        if (--fInstalled == 0) {
            FileBuffers.getTextFileBufferManager().removeFileBufferListener(this);
            fPositionTrackerMap.clear();
            fMemoryCounter= 0;
        }
    }

    public void bufferCreated(IFileBuffer buffer) {
        if (buffer instanceof ITextFileBuffer) {
            createCheckpoint((ITextFileBuffer) buffer);
        }
    }

    public void bufferDisposed(IFileBuffer buffer) {
        if (buffer instanceof ITextFileBuffer) {
            resetToLastCheckpoint((ITextFileBuffer) buffer);
        }
    }

    public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
        if (!isDirty && buffer instanceof ITextFileBuffer) {
            createCheckpoint((ITextFileBuffer) buffer);
        }
    }

    public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
        if (isStateValidated && !buffer.isDirty()) {
            bufferCreated(buffer);
        }
    }
    
    public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {}
    public void bufferContentReplaced(IFileBuffer buffer) {}
    public void underlyingFileMoved(IFileBuffer buffer, IPath path) {}
    public void underlyingFileDeleted(IFileBuffer buffer) {}
    public void stateChangeFailed(IFileBuffer buffer) {}
    public void stateChanging(IFileBuffer buffer) {}

    private synchronized void createCheckpoint(ITextFileBuffer buffer) {
        PositionTrackerChain chain= getChain(buffer);
        if (chain == null) {
            chain = new PositionTrackerChain(buffer.getModificationStamp());
            fPositionTrackerMap.put(buffer.getLocation(), chain);
            fMemoryCounter+= PositionTrackerChain.MEMORY_SIZE + HASHMAP_ENTRY_SIZE;
        }
        else {
            chain.stopTracking();
            fMemoryCounter+= chain.createCheckpoint(buffer.getModificationStamp());
        }
        chain.startTracking(buffer.getDocument());

        if (fMemoryCounter > MAX_MEMORY) {
            runCleanup();
        }
    }
    
    private synchronized PositionTrackerChain getChain(ITextFileBuffer buffer) {
        return (PositionTrackerChain) fPositionTrackerMap.get(buffer.getLocation());
    }

    private synchronized void resetToLastCheckpoint(ITextFileBuffer buffer) {
        PositionTrackerChain chain= getChain(buffer);
        if (chain != null) {
            chain.stopTracking();
            chain.getActiveTracker().clear();

            if (!chain.isModified()) {
                fPositionTrackerMap.remove(buffer.getLocation());
                chain.dispose();
            }
        }
    }
    
    private synchronized void runCleanup() {
        fMemoryCounter= 0;
        for (Iterator iter = fPositionTrackerMap.values().iterator(); iter.hasNext();) {
            PositionTrackerChain chain= (PositionTrackerChain) iter.next();
            fMemoryCounter+= HASHMAP_ENTRY_SIZE;
            fMemoryCounter+= chain.getMemorySize();
        }
        if (fMemoryCounter > MAX_MEMORY_AFTER_CLEANUP) {
            SortedMap map= new TreeMap();
            for (Iterator iter = fPositionTrackerMap.values().iterator(); iter.hasNext();) {
                PositionTrackerChain chain = (PositionTrackerChain) iter.next();
                addChain(map, chain);
            }
            while (!map.isEmpty()) {
                Long key= (Long) map.firstKey();
                List list= (List) map.remove(key);
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    PositionTrackerChain chain = (PositionTrackerChain) iter.next();
                    fMemoryCounter+= chain.removeOldest();
                    addChain(map, chain);
                }
                if (fMemoryCounter <= MAX_MEMORY_AFTER_CLEANUP) {
                    break;
                }
            }
        }        
    }

    private synchronized void addChain(SortedMap map, PositionTrackerChain chain) {
        long or= chain.getOldestRetirement();
        if (or != Long.MAX_VALUE) {
            Long lor= new Long(or);
            List list= (List) map.get(lor);
            if (list == null) {
                list= new LinkedList();
                map.put(lor, list);
            }
            list.add(chain);
        }
    }

    public synchronized IPositionConverter findPositionConverter(IFile file, long timestamp) {
        PositionTrackerChain chain= (PositionTrackerChain) fPositionTrackerMap.get(file.getFullPath());
        if (chain != null) {
            return chain.findTrackerAt(timestamp);
        }
        return null;
    }
}
