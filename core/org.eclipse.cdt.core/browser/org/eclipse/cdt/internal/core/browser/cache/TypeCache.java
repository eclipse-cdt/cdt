/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.browser.TypeInfo;
import org.eclipse.core.runtime.IPath;

public class TypeCache {

	private static final int INITIAL_FILE_COUNT= 100;
	private static final int INITIAL_TYPE_COUNT= INITIAL_FILE_COUNT*20;
	private final Map fFileMap= new HashMap(INITIAL_FILE_COUNT);
	private final Map fTypeMap= new HashMap(INITIAL_TYPE_COUNT);
	private boolean fIsDirty= true;
	
	private static final class TypeReference {
		private TypeInfo fRealInfo= null;
		private Set fPaths= new HashSet(1);

		public TypeReference(TypeInfo info, IPath path) {
			fRealInfo= info;
			fPaths.add(path);
		}
		
		public TypeInfo getInfo() {
			return fRealInfo;
		}
		
		public void addPath(IPath path) {
			fPaths.add(path);
		}

		public void removePath(IPath path) {
			fPaths.remove(path);
		}
		
		public Collection getPaths() {
			return fPaths;
		}
	};

	public TypeCache() {
	}

	public synchronized void markAsDirty(boolean dirty) {
		fIsDirty= dirty;
	}
	
	public synchronized boolean isDirty() {
		return fIsDirty;
	}

	public synchronized Set getAllFiles() {
		return fFileMap.keySet();
	}

	public synchronized Set getAllTypes() {
		return fTypeMap.keySet();
	}
		
	private TypeInfo addTypeReference(TypeInfo info, IPath path) {
		// we use info as a key here. the actual value found in
		// the map corresponds to the 'real' TypeInfo object with
		// the same hashCode
		TypeReference typeRef= (TypeReference) fTypeMap.get(info);
		if (typeRef == null) {
			// add this type to cache
			typeRef= new TypeReference(info, path);
			fTypeMap.put(info, typeRef);
		} else if (typeRef.getInfo() != info) {
			typeRef.addPath(path);
		}
		return typeRef.getInfo();
	}

	private void removeTypeReference(TypeInfo info, IPath path) {
		// we use info as a key here. the actual value found in
		// the map corresponds to the 'real' TypeInfo object with
		// the same hashCode
		TypeReference typeRef= (TypeReference) fTypeMap.get(info);
		if (typeRef == null)
			return;
		
		typeRef.removePath(path);
		for (Iterator i= typeRef.getPaths().iterator(); i.hasNext(); ) {
			IPath p= (IPath) i.next();
			fFileMap.remove(p);
		}
		fTypeMap.remove(info);
	}

	public synchronized void insert(IPath path, Collection types) {
		Collection typeSet= (Collection) fFileMap.get(path);
		if (typeSet == null)
			typeSet= new ArrayList(types.size());
		for (Iterator typesIter= types.iterator(); typesIter.hasNext(); ) {
			TypeInfo info= (TypeInfo)typesIter.next();
			TypeInfo newType= addTypeReference(info, path);
			typeSet.add(newType);
		}
		fFileMap.put(path, typeSet);
	}
	
	public synchronized boolean contains(IPath path) {
		return fFileMap.containsKey(path);
	}

	public synchronized void flush(IPath path) {
		Collection typeSet= (Collection) fFileMap.get(path);
		if (typeSet != null) {
			for (Iterator typesIter= typeSet.iterator(); typesIter.hasNext(); ) {
				TypeInfo info= (TypeInfo)typesIter.next();
				removeTypeReference(info, path);
			}
			fFileMap.remove(path);
		}
		fIsDirty= true;
	}
	
	public synchronized void flush(Set paths) {
		if (paths != null) {
			// flush paths from cache
			for (Iterator i= paths.iterator(); i.hasNext(); ) {
				IPath path= (IPath) i.next();
				flush(path);
			}
		}
	}

	public synchronized void flushAll() {
		// flush the entire cache
		fFileMap.clear();
		fTypeMap.clear();
		fIsDirty= true;
	}
}
