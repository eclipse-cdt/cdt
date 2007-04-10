/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentInclude;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.DBProperties;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.ApplyVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;

/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOM extends PlatformObject implements IIndexFragment, IPDOM {
	protected Database db;

	public static final int VERSION = 29;
	// 0 - the beginning of it all
	// 1 - first change to kick off upgrades
	// 2 - added file inclusions
	// 3 - added macros and change string implementation
	// 4 - added parameters in C++
	// 5 - added types and restructured nodes a bit
	// 6 - function style macros.
	// 7 - class key
	// 8 - enumerators
	// 9 - base classes
	// 10 - typedefs, types on C++ variables
	// 11 - changed how members work
	// 12 - one more change for members (is-a list -> has-a list)
	// 13 - CV-qualifiers, storage class specifiers, function/method annotations
	// 14 - added timestamps for files (bug 149571)
	// 15 - fixed offsets for pointer types and qualifier types and PDOMCPPVariable (bug 160540). 
	// 16 - have PDOMCPPField store type information, and PDOMCPPNamespaceAlias store what it is aliasing
	// 17 - use single linked list for names in file, adds a link to enclosing defintion name.
	// 18 - distinction between c-unions and c-structs.
    // 19 - alter representation of paths in the pdom (162172)
	// 20 - add pointer to member types, array types, return types for functions
	// 21 - change representation of paths in the pdom (167549)
	// 22 - fix inheritance relations (167396)
	// 23 - types on c-variables, return types on c-functions
	// 24 - file local scopes (161216)
	// 25 - change ordering of bindings (175275)
	// 26 - add properties storage
	// 27 - templates: classes, functions, limited nesting support, only template type parameters
	// 28 - templates: class instance/specialization base classes
	// 29 - includes: fixed modelling of unresolved includes (180159)

	public static final int LINKAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	public static final int PROPERTIES = Database.DATA_AREA + 8;

	// Local caches
	private BTree fileIndex;
	private Map fLinkageIDCache = new HashMap();
	private File fPath;
	private IIndexLocationConverter locationConverter;
	private Map fPDOMLinkageFactoryCache;
	
	public PDOM(File dbPath, IIndexLocationConverter locationConverter, Map linkageFactoryMappings) throws CoreException {
		this(dbPath, locationConverter, ChunkCache.getSharedInstance(), linkageFactoryMappings);
	}
	
	public PDOM(File dbPath, IIndexLocationConverter locationConverter, ChunkCache cache, Map linkageFactoryMappings) throws CoreException {
		fPDOMLinkageFactoryCache = linkageFactoryMappings;
		loadDatabase(dbPath, cache);
		this.locationConverter = locationConverter;
	}

	private void loadDatabase(File dbPath, ChunkCache cache) throws CoreException {
		fPath= dbPath;
		db = new Database(fPath, cache, VERSION);
		fileIndex= null;	// holds on to the database, so clear it.

		int version= db.getVersion();
		if (version == VERSION) {
			readLinkages();
		}
	}

	public IIndexLocationConverter getLocationConverter() {
		return locationConverter;
	}

	public boolean versionMismatch() throws CoreException {
		if (db.getVersion() != VERSION) {
			return true;
		} else
			return false;
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		for (Iterator iter = fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage = (PDOMLinkage) iter.next();
			linkage.accept(visitor);
		}
	}

	public static interface IListener {
		public void handleChange(PDOM pdom);
	}

	private List listeners;

	public void addListener(IListener listener) {
		if (listeners == null)
			listeners = new LinkedList();
		listeners.add(listener);
	}

	public void removeListener(IListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}

	private void fireChange() {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext())
			((IListener)i.next()).handleChange(this);
	}

	public Database getDB() {
		return db;
	}

	public BTree getFileIndex() throws CoreException {
		if (fileIndex == null)
			fileIndex = new BTree(getDB(), FILE_INDEX, new PDOMFile.Comparator(getDB()));
		return fileIndex;
	}

	public IIndexFragmentFile getFile(IIndexFileLocation location) throws CoreException {
		return PDOMFile.findFile(this, getFileIndex(), location, locationConverter);
	}

	public List/*<IIndexFileLocation>*/ getAllFileLocations() throws CoreException {
		final List locations = new ArrayList();
		getFileIndex().accept(new IBTreeVisitor(){
			public int compare(int record) throws CoreException {
				return 0;
			}
			public boolean visit(int record) throws CoreException {
				PDOMFile file = new PDOMFile(PDOM.this, record);
				locations.add(file.getLocation());
				return true;
			}
		});
		return locations;
	}
	
	protected IIndexFragmentFile addFile(IIndexFileLocation location) throws CoreException {
		IIndexFragmentFile file = getFile(location);
		if (file == null) {
			PDOMFile pdomFile = new PDOMFile(this, location);
			getFileIndex().insert(pdomFile.getRecord());
			file= pdomFile;
		}
		return file;		
	}

	protected void clearFileIndex() throws CoreException {
		db.putInt(FILE_INDEX, 0);
		fileIndex = null;	
	}
	
	protected void clear() throws CoreException {
		Database db = getDB();
		// Clear out the database
		db.clear(1);

		// Zero out the File Index and Linkages
		clearFileIndex();
		
		db.setVersion(VERSION);
		db.putInt(PROPERTIES, 0);
		db.putInt(LINKAGES, 0);
		fLinkageIDCache.clear();
	}
	
	void reloadFromFile(File file) throws CoreException {
		File oldFile= fPath;
		fLinkageIDCache.clear();
		try {
			db.close();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		loadDatabase(file, db.getChunkCache());
		oldFile.delete();
	}		

	public boolean isEmpty() throws CoreException {
		return getFirstLinkageRecord() == 0;
	}

	/**
	 * @deprecated use findDefinitions() instead
	 */
	public IName[] getDefinitions(IBinding binding) throws CoreException {
		if (binding instanceof PDOMBinding) {
			List names = new ArrayList();
			for (PDOMName name = ((PDOMBinding)binding).getFirstDefinition();
			name != null;
			name = name.getNextInBinding())
				names.add(name);
			return (IName[]) names.toArray(new IIndexName[names.size()]);
		}
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	/**
	 * @deprecated use findReferences() instead
	 */
	public IName[] getReferences(IBinding binding) throws CoreException {
		if (binding instanceof PDOMBinding) {
			List names = new ArrayList();
			for (PDOMName name = ((PDOMBinding)binding).getFirstReference();
			name != null;
			name = name.getNextInBinding())
				names.add(name);
			return (IName[]) names.toArray(new IIndexName[names.size()]);
		}
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexFragmentBinding findBinding(IASTName name) throws CoreException {
		PDOMLinkage linkage= adaptLinkage(name.getLinkage());
		if (linkage != null) {
			return linkage.resolveBinding(name);
		}
		return null;
	}

	private static class BindingFinder implements IPDOMVisitor {
		private final Pattern[] pattern;
		private final IProgressMonitor monitor;

		private final ArrayList currentPath= new ArrayList();
		private final ArrayList matchStack= new ArrayList();
		private List bindings = new ArrayList();
		private boolean isFullyQualified;
		private BitSet matchesUpToLevel;
		private IndexFilter filter;

		public BindingFinder(Pattern[] pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) {
			this.pattern = pattern;
			this.monitor = monitor;
			this.isFullyQualified= isFullyQualified;
			this.filter= filter;
			matchesUpToLevel= new BitSet();
			matchesUpToLevel.set(0);
			matchStack.add(matchesUpToLevel);
		}

		public boolean visit(IPDOMNode node) throws CoreException {
			if (monitor.isCanceled())
				throw new CoreException(Status.OK_STATUS);

			if (node instanceof PDOMBinding) {
				PDOMBinding binding = (PDOMBinding)node;
				String name = binding.getName();

				// check if we have a complete match.
				final int lastIdx = pattern.length-1;
				if (matchesUpToLevel.get(lastIdx) && pattern[lastIdx].matcher(name).matches()) {
					if (filter.acceptImplicitMethods() || !(binding instanceof ICPPMethod) ||
							!((ICPPMethod)binding).isImplicit()) {
						if (filter.acceptBinding(binding)) {
							bindings.add(binding);
						}
					}
				}

				// check if we have a partial match
				if (binding.mayHaveChildren()) {
					boolean visitNextLevel= false;
					BitSet updatedMatchesUpToLevel= new BitSet();
					if (!isFullyQualified) {
						updatedMatchesUpToLevel.set(0);
						visitNextLevel= true;
					}
					for (int i=0; i < lastIdx; i++) {
						if (matchesUpToLevel.get(i) && pattern[i].matcher(name).matches()) {
							updatedMatchesUpToLevel.set(i+1);
							visitNextLevel= true;
						}
					}
					if (visitNextLevel) {
						matchStack.add(matchesUpToLevel);
						matchesUpToLevel= updatedMatchesUpToLevel;
						currentPath.add(binding);
						return true;
					}
				}
				return false;
			}
			return false;
		}

		public void leave(IPDOMNode node) throws CoreException {
			final int idx= currentPath.size()-1;
			if (idx >= 0 && currentPath.get(idx) == node) {
				currentPath.remove(idx);
				matchesUpToLevel= (BitSet) matchStack.remove(matchStack.size()-1);
			}
		}

		public IIndexFragmentBinding[] getBindings() {
			return (IIndexFragmentBinding[])bindings.toArray(new IIndexFragmentBinding[bindings.size()]);
		}
	}

	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(new Pattern[] { pattern }, isFullyQualified, filter, monitor);
	}

	public IIndexFragmentBinding[] findBindings(Pattern[] pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		BindingFinder finder = new BindingFinder(pattern, isFullyQualified, filter, monitor);
		for (Iterator iter = fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage = (PDOMLinkage) iter.next();
			if (filter.acceptLinkage(linkage)) {
				try {
					linkage.accept(finder);
				} catch (CoreException e) {
					if (e.getStatus() != Status.OK_STATUS)
						throw e;
					else
						return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
				}
			}
		}
		return finder.getBindings();
	}

	public IIndexFragmentBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList result= new ArrayList();
		for (Iterator iter = fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage = (PDOMLinkage) iter.next();
			if (filter.acceptLinkage(linkage)) {
				ArrayList bindings= new ArrayList();
				bindings.add(linkage);
				for (int i=0; i < names.length; i++) {
					char[] name= names[i];
					IndexFilter levelFilter= i==names.length-1 ? filter : IndexFilter.ALL;
					BindingCollector collector= new BindingCollector(linkage, name, levelFilter, false, true);
					for (Iterator in = bindings.iterator(); in.hasNext();) {
						PDOMNode node= (PDOMNode) in.next();
						node.accept(collector);
					}
					bindings.clear();
					bindings.addAll(Arrays.asList(collector.getBindings()));
				}
				result.addAll(bindings);
			}
		}
		return (IIndexFragmentBinding[]) result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	private void readLinkages() throws CoreException {
		// populate the linkage cache
		int record= getFirstLinkageRecord();
		while (record != 0) {
			String linkageID= PDOMLinkage.getId(this, record).getString();
			IPDOMLinkageFactory factory= (IPDOMLinkageFactory) fPDOMLinkageFactoryCache.get(linkageID);
			if (factory != null) {
				PDOMLinkage linkage= factory.getLinkage(this, record);
				fLinkageIDCache.put(linkageID, linkage);
			}
			record= PDOMLinkage.getNextLinkageRecord(this, record);
		}
	}

	public PDOMLinkage getLinkage(String linkageID) throws CoreException {
		return (PDOMLinkage) fLinkageIDCache.get(linkageID);
	}

	public PDOMLinkage createLinkage(String linkageID) throws CoreException {
		PDOMLinkage pdomLinkage= (PDOMLinkage) fLinkageIDCache.get(linkageID);
		if (pdomLinkage == null) {
			// Need to create it
			IPDOMLinkageFactory factory= (IPDOMLinkageFactory) fPDOMLinkageFactoryCache.get(linkageID);			
			if (factory != null) {
				return factory.createLinkage(this);
			}
		}
		return pdomLinkage;
	}

	public PDOMLinkage getLinkage(int record) throws CoreException {
		if (record == 0)
			return null;

		// First check the cache. We do a linear search since there will be very few linkages
		// in a given database.
		Iterator i = fLinkageIDCache.values().iterator();
		while (i.hasNext()) {
			PDOMLinkage linkage = (PDOMLinkage)i.next();
			if (linkage.getRecord() == record)
				return linkage;
		}

		String id = PDOMLinkage.getId(this, record).getString();
		return createLinkage(id);
	}

	private int getFirstLinkageRecord() throws CoreException {
		return db.getInt(LINKAGES);
	}

	public IIndexLinkage[] getLinkages() {
		Collection values = fLinkageIDCache.values();
		return (IIndexLinkage[]) values.toArray(new IIndexLinkage[values.size()]);
	}
	
	public PDOMLinkage[] getLinkageImpls() {
		Collection values = fLinkageIDCache.values();
		return (PDOMLinkage[]) values.toArray(new PDOMLinkage[values.size()]);
	}

	public void insertLinkage(PDOMLinkage linkage) throws CoreException {
		linkage.setNext(db.getInt(LINKAGES));
		db.putInt(LINKAGES, linkage.getRecord());
		fLinkageIDCache.put(linkage.getID(), linkage);
	}

	public PDOMBinding getBinding(int record) throws CoreException {
		if (record == 0)
			return null;
		else {
			PDOMNode node = PDOMNode.getLinkage(this, record).getNode(record);
			return node instanceof PDOMBinding ? (PDOMBinding)node : null;
		}
	}

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private Object mutex = new Object();
	private int lockCount;
	private int waitingReaders;
	private long lastWriteAccess= 0;
	private long lastReadAccess= 0;

	public void acquireReadLock() throws InterruptedException {
		synchronized (mutex) {
			++waitingReaders;
			while (lockCount < 0)
				mutex.wait();
			--waitingReaders;
			++lockCount;
		}
	}

	public void releaseReadLock() {
		synchronized (mutex) {
			assert lockCount > 0: "No lock to release"; //$NON-NLS-1$
			lastReadAccess= System.currentTimeMillis();
			if (lockCount > 0)
				--lockCount;
			mutex.notifyAll();
		}
	}

	public void acquireWriteLock() throws InterruptedException {
		acquireWriteLock(0);
	}

	public void acquireWriteLock(int giveupReadLocks) throws InterruptedException {
		synchronized (mutex) {
			if (giveupReadLocks > 0) {
				// giveup on read locks
				assert lockCount >= giveupReadLocks: "Not enough locks to release"; //$NON-NLS-1$
				if (lockCount >= giveupReadLocks) {
					lockCount-= giveupReadLocks;
				}
				else if (lockCount >= 0) {
					lockCount= 0;
				}
			}

			// Let the readers go first
			while (lockCount != 0 || waitingReaders > 0)
				mutex.wait();
			--lockCount;
			db.setWritable();
		}
	}

	final public void releaseWriteLock() {
		releaseWriteLock(0);
	}

	public void releaseWriteLock(int establishReadLocks) {
		try {
			db.setReadOnly();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		assert lockCount == -1;
		lastWriteAccess= System.currentTimeMillis();
		synchronized (mutex) {
			if (lockCount < 0)
				lockCount= establishReadLocks;
			mutex.notifyAll();
		}
		fireChange();
	}


	public long getLastWriteAccess() {
		return lastWriteAccess;
	}
	 
	public long getLastReadAccess() {
		return lastReadAccess;
	}

	protected PDOMLinkage adaptLinkage(ILinkage linkage) throws CoreException {
		return (PDOMLinkage) fLinkageIDCache.get(linkage.getID());
	}

	public IIndexFragmentBinding adaptBinding(IBinding binding) throws CoreException {
		PDOMBinding pdomBinding= (PDOMBinding) binding.getAdapter(PDOMBinding.class);
		if (pdomBinding != null && pdomBinding.getPDOM() == this) {
			return pdomBinding;
		}

		PDOMLinkage linkage= adaptLinkage(binding.getLinkage());
		if (linkage != null) {
			return linkage.adaptBinding(binding);
		}
		return null;
	}

	public IIndexFragmentBinding adaptBinding(IIndexFragmentBinding binding) throws CoreException {
		if (binding != null) {
			return adaptBinding((IBinding) binding);
		}
		return null;
	}

	public IIndexFragmentBinding findBinding(IIndexFragmentName indexName) throws CoreException {
		if (indexName instanceof PDOMName) {
			PDOMName pdomName= (PDOMName) indexName;
			return pdomName.getPDOMBinding();
		}
		return null;
	}

	public IIndexFragmentName[] findNames(IBinding binding, int options) throws CoreException {
		IIndexFragmentBinding proxyBinding= adaptBinding(binding);
		if (proxyBinding != null) {
			return findNames(proxyBinding, options);
		}
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexFragmentName[] findNames(IIndexFragmentBinding binding, int options) throws CoreException {
		PDOMBinding pdomBinding = (PDOMBinding) adaptBinding(binding);

		if (pdomBinding != null) {
			PDOMName name;
			List names = new ArrayList();
			if ((options & FIND_DECLARATIONS) != 0) {
				for (name= pdomBinding.getFirstDeclaration(); name != null; name= name.getNextInBinding()) {
					names.add(name);
				}
			}
			if ((options & FIND_DEFINITIONS) != 0) {
				for (name = pdomBinding.getFirstDefinition(); name != null; name= name.getNextInBinding()) {
					names.add(name);
				}
			}
			if ((options & FIND_REFERENCES) != 0) {
				for (name = pdomBinding.getFirstReference(); name != null; name= name.getNextInBinding()) {
					names.add(name);
				}
			}
			return (IIndexFragmentName[]) names.toArray(new IIndexFragmentName[names.size()]);
		}
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file) throws CoreException {
		PDOMFile pdomFile= adaptFile(file);
		if (pdomFile != null) {
			List result = new ArrayList();
			for (PDOMInclude i= pdomFile.getFirstIncludedBy(); i != null; i= i.getNextInIncludedBy()) {
				result.add(i);
			}
			return (IIndexFragmentInclude[]) result.toArray(new PDOMInclude[result.size()]);
		}
		return new PDOMInclude[0];
	}

	private PDOMFile adaptFile(IIndexFragmentFile file) throws CoreException {
		if (file.getIndexFragment() == this && file instanceof PDOMFile) {
			return (PDOMFile) file;
		}

		return (PDOMFile) getFile(file.getLocation());
	}

	public File getPath() {
		return fPath;
	}
	
	public IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter) throws CoreException {
		ArrayList result= new ArrayList();
		for (Iterator iter= fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage= (PDOMLinkage) iter.next();
			if (filter.acceptLinkage(linkage)) {
				IBinding[] bindings;
				if(filescope) {
					BindingCollector visitor = new BindingCollector(linkage, prefix, filter, true, false);
					linkage.accept(visitor);
					bindings= visitor.getBindings();
				} else {
					BindingCollector visitor = new BindingCollector(linkage, prefix, filter, true, false);
					linkage.accept(visitor);
					linkage.accept(new ApplyVisitor(linkage, visitor));
					bindings= visitor.getBindings();
				}
				for (int j = 0; j < bindings.length; j++) {
					result.add(bindings[j]);
				}
			}
		}
		return (IIndexFragmentBinding[]) result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	public String getProperty(String propertyName) throws CoreException {
		return new DBProperties(db, PROPERTIES).getProperty(propertyName);
	}

	public void close() throws CoreException {
		fLinkageIDCache.clear();
		db.close();
	}

	public long getCacheHits() {
		return db.getCacheHits();
	}

	public long getCacheMisses() {
		return db.getCacheMisses();
	}

	public void resetCacheCounters() {
		db.resetCacheCounters();
	}		
}