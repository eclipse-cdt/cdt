/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Markus Schorn - initial API and implementation 
 ******************************************************************************/ 
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class CRefactoringMatchStore {
    private Map<IFile, IPath> fFileToPathMap= new HashMap<IFile, IPath>();
    private Map<IPath, SortedMap<CRefactoringMatch, CRefactoringMatch>> fPathToMatches= new HashMap<IPath, SortedMap<CRefactoringMatch, CRefactoringMatch>>();
    private Comparator<CRefactoringMatch> fOffsetComparator;

    public CRefactoringMatchStore() {
        fOffsetComparator= new Comparator<CRefactoringMatch>() {
            @Override
			public int compare(CRefactoringMatch o1, CRefactoringMatch o2) {
                return o1.getOffset() - o2.getOffset();
            }
        };
    }
    
    public void addMatch(CRefactoringMatch match) {
        IPath path= resolvePath(match.getFile());
        if (path != null) {
            Map<CRefactoringMatch, CRefactoringMatch> matchesForPath= getMapForPath(path, true);
            matchesForPath.put(match, match);
        }
    }
        
    private Map<CRefactoringMatch, CRefactoringMatch> getMapForPath(IPath path, boolean create) {
        SortedMap<CRefactoringMatch, CRefactoringMatch> map= fPathToMatches.get(path);
        if (map == null && create) {
            map= new TreeMap<CRefactoringMatch, CRefactoringMatch>(fOffsetComparator);
            fPathToMatches.put(path, map);
        }
        return map;
    }

    private IPath resolvePath(IFile file) {
        IPath path= fFileToPathMap.get(file);
        if (path == null) {
            path= file.getLocation();
            if (path == null) {
                path= file.getFullPath();
            }
            fFileToPathMap.put(file, path);
        }
        return path;
    }

    public int getFileCount() {
        return fFileToPathMap.size();
    }

    public List<IFile> getFileList() {
        return new ArrayList<IFile>(fFileToPathMap.keySet());
    }

    public boolean contains(IResource file) {
        return fFileToPathMap.containsKey(file);
    }

    public Collection<CRefactoringMatch> getMatchesForFile(IResource file) {
        return getMatchesForPath(fFileToPathMap.get(file));
    }

    public Collection<CRefactoringMatch> getMatchesForPath(IPath path) {
        if (path != null) {
            Map<CRefactoringMatch, CRefactoringMatch> map= fPathToMatches.get(path);
            if (map != null) {
                return map.keySet();
            }
        }
        return Collections.emptySet();
    }

    public CRefactoringMatch findMatch(IPath path, int nodeOffset) {
        Map<CRefactoringMatch, CRefactoringMatch> map= fPathToMatches.get(path);
        if (map != null) {
            return map.get(new CRefactoringMatch(null, nodeOffset, 0, 0));
        }
        return null;
    }

    public void removePath(IPath path) {
        Map<CRefactoringMatch, CRefactoringMatch> map= fPathToMatches.remove(path);
        if (map != null && !map.isEmpty()) {
            IFile file= (map.values().iterator().next()).getFile();
            fFileToPathMap.remove(file);
        }
    }

    public Collection<CRefactoringMatch> findMatchesInRange(Path path, int offset, int end) {
        if (path != null) {
            SortedMap<CRefactoringMatch, CRefactoringMatch> map= fPathToMatches.get(path);
            if (map != null) {
                return map.subMap(new CRefactoringMatch(null, offset, 0, 0),
                        new CRefactoringMatch(null, end, 0, 0)).keySet();
            }
        }
        return Collections.emptySet();
    }
}
