/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.IPositionTrackerManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.URIUtil;
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
    /**
     * as the key in the map we use:
     * the full path for resources,
     * the location as path for local non-workspace files,
     * the location as URI for non-local non-workspace files.
     */
    private HashMap<Object, PositionTrackerChain> fPositionTrackerMap;

    private PositionTrackerManager() {
        fPositionTrackerMap= new HashMap<Object, PositionTrackerChain>();
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

    @Override
	public void bufferCreated(IFileBuffer buffer) {
        if (buffer instanceof ITextFileBuffer) {
            createCheckpoint((ITextFileBuffer) buffer);
        }
    }

    @Override
	public void bufferDisposed(IFileBuffer buffer) {
        if (buffer instanceof ITextFileBuffer) {
            resetToLastCheckpoint((ITextFileBuffer) buffer);
        }
    }

    @Override
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
        if (!isDirty && buffer instanceof ITextFileBuffer) {
            createCheckpoint((ITextFileBuffer) buffer);
        }
    }

    @Override
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
        if (isStateValidated && !buffer.isDirty()) {
            bufferCreated(buffer);
        }
    }

    @Override
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {}
    @Override
	public void bufferContentReplaced(IFileBuffer buffer) {}
    @Override
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {}
    @Override
	public void underlyingFileDeleted(IFileBuffer buffer) {}
    @Override
	public void stateChangeFailed(IFileBuffer buffer) {}
    @Override
	public void stateChanging(IFileBuffer buffer) {}

    private synchronized void createCheckpoint(ITextFileBuffer buffer) {
    	final Object bufferKey= getKey(buffer);
        PositionTrackerChain chain= fPositionTrackerMap.get(bufferKey);
        if (chain == null) {
            chain = new PositionTrackerChain(buffer.getModificationStamp());
            fPositionTrackerMap.put(bufferKey, chain);
            fMemoryCounter+= PositionTrackerChain.MEMORY_SIZE + HASHMAP_ENTRY_SIZE;
        } else {
            chain.stopTracking();
            fMemoryCounter+= chain.createCheckpoint(buffer.getModificationStamp());
        }
        chain.startTracking(buffer.getDocument());

        if (fMemoryCounter > MAX_MEMORY) {
            runCleanup();
        }
    }

	private Object getKey(ITextFileBuffer buffer) {
		Object key= buffer.getLocation();
		if (key == null) {
			URI uri= buffer.getFileStore().toURI();
			key= URIUtil.toPath(uri);
			if (key == null) {
				key= uri;
			}
		}
		return key;
	}

    private synchronized void resetToLastCheckpoint(ITextFileBuffer buffer) {
    	final Object bufferKey= getKey(buffer);
        PositionTrackerChain chain= fPositionTrackerMap.get(bufferKey);
        if (chain != null) {
            chain.stopTracking();
            chain.getActiveTracker().clear();

            if (!chain.isModified()) {
                fPositionTrackerMap.remove(bufferKey);
                chain.dispose();
            }
        }
    }

    private synchronized void runCleanup() {
        fMemoryCounter= 0;
        for (PositionTrackerChain chain : fPositionTrackerMap.values()) {
            fMemoryCounter+= HASHMAP_ENTRY_SIZE;
            fMemoryCounter+= chain.getMemorySize();
        }
        if (fMemoryCounter > MAX_MEMORY_AFTER_CLEANUP) {
            SortedMap<Long, List<PositionTrackerChain>> map= new TreeMap<Long, List<PositionTrackerChain>>();
            for (Iterator<PositionTrackerChain> iter = fPositionTrackerMap.values().iterator(); iter.hasNext();) {
                PositionTrackerChain chain = iter.next();
                addChain(map, chain);
            }
            while (!map.isEmpty()) {
                Long key= map.firstKey();
                List<PositionTrackerChain> list= map.remove(key);
                for (Iterator<PositionTrackerChain> iter = list.iterator(); iter.hasNext();) {
                    PositionTrackerChain chain = iter.next();
                    fMemoryCounter+= chain.removeOldest();
                    addChain(map, chain);
                }
                if (fMemoryCounter <= MAX_MEMORY_AFTER_CLEANUP) {
                    break;
                }
            }
        }
    }

    private synchronized void addChain(SortedMap<Long, List<PositionTrackerChain>> map, PositionTrackerChain chain) {
        long or= chain.getOldestRetirement();
        if (or != Long.MAX_VALUE) {
            Long lor= new Long(or);
            List<PositionTrackerChain> list= map.get(lor);
            if (list == null) {
                list= new LinkedList<PositionTrackerChain>();
                map.put(lor, list);
            }
            list.add(chain);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public synchronized IPositionConverter findPositionConverter(IFile file, long timestamp) {
        PositionTrackerChain chain= fPositionTrackerMap.get(file.getFullPath());
        if (chain != null) {
            return chain.findTrackerAt(timestamp);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public synchronized IPositionConverter findPositionConverter(IPath externalLocation, long timestamp) {
        PositionTrackerChain chain= fPositionTrackerMap.get(externalLocation);
        if (chain != null) {
            return chain.findTrackerAt(timestamp);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public synchronized IPositionConverter findPositionConverter(ITranslationUnit tu, long timestamp) {
    	IFile file= (IFile) tu.getResource();
    	if (file != null) {
    		return findPositionConverter(file, timestamp);
    	}
    	IPath location= tu.getLocation();
    	if (location != null) {
    		return findPositionConverter(location, timestamp);
    	}

    	URI locationURI = tu.getLocationURI();
    	if (locationURI != null) {
    		return findPositionConverter(locationURI, timestamp);
    	}

    	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public synchronized IPositionConverter findPositionConverter(URI locationURI, long timestamp) {
    	PositionTrackerChain chain= fPositionTrackerMap.get(locationURI);
    	if (chain == null) {
            IPath path= URIUtil.toPath(locationURI);
            if (path != null) {
            	chain= fPositionTrackerMap.get(path);
            }
    	}
        if (chain != null) {
            return chain.findTrackerAt(timestamp);
        }
        return null;
    }
}
