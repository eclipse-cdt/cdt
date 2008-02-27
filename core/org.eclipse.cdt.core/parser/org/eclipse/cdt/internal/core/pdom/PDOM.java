/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentInclude;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.DBProperties;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.FindBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.MacroCollector;
import org.eclipse.cdt.internal.core.pdom.dom.NamedNodeCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;

/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOM extends PlatformObject implements IPDOM {
	/**
	 * Identifier for PDOM format
	 * @see IIndexFragment#PROPERTY_FRAGMENT_FORMAT_ID
	 */
	public static final String FRAGMENT_PROPERTY_VALUE_FORMAT_ID= "org.eclipse.cdt.internal.core.pdom.PDOM"; //$NON-NLS-1$
	
	public static final int CURRENT_VERSION = 56; 
	public static final int MIN_SUPPORTED_VERSION= CURRENT_VERSION;
	
	/**
	 * The earliest PDOM version that the CURRENT_VERSION can be read as. For example,
	 * versions 37,38 and 39 may be safely read by code from the version of CDT (4.0.0)
	 * released at PDOM version 36.
	 * <p>
	 * Ideally this would always be CURRENT_VERSION on the basis that CURRENT_VERSION is
	 * not incrementing.
	 */
	public static final int EARLIEST_FORWARD_COMPATIBLE_VERSION= CURRENT_VERSION; 
	
	/* 
	 * PDOM internal format history
	 * 
	 *    #x# = the version was used in an official release
	 * 
	 *   0 - the beginning of it all
	 *   1 - first change to kick off upgrades
	 *   2 - added file inclusions  
	 *   3 - added macros and change string implementation
	 *   4 - added parameters in C++
	 *   5 - added types and restructured nodes a bit
	 *   6 - function style macros	 
	 * # 7#- class key - <<CDT 3.1>>
	 *   8 - enumerators
	 *   9 - base classes
	 *  10 - typedefs, types on C++ variables
	 * #11#- changed how members work - <<CDT 3.1.1>>, <<CDT 3.1.2>>
	 *  12 - one more change for members (is-a list -> has-a list)
	 *  13 - CV-qualifiers, storage class specifiers, function/method annotations
	 *  14 - added timestamps for files (bug 149571)
	 *  15 - fixed offsets for pointer types and qualifier types and PDOMCPPVariable (bug 160540). 
	 *  16 - have PDOMCPPField store type information, and PDOMCPPNamespaceAlias store what it is aliasing
	 *  17 - use single linked list for names in file, adds a link to enclosing definition name.
	 *  18 - distinction between c-unions and c-structs.
	 *  19 - alter representation of paths in the PDOM (162172)
	 *  20 - add pointer to member types, array types, return types for functions
	 *  21 - change representation of paths in the PDOM (167549)
	 *  22 - fix inheritance relations (167396)
	 *  23 - types on c-variables, return types on c-functions
	 *  24 - file local scopes (161216)
	 *  25 - change ordering of bindings (175275)
	 *  26 - add properties storage
	 *  27 - templates: classes, functions, limited nesting support, only template type parameters
	 *  28 - templates: class instance/specialization base classes
	 *  29 - includes: fixed modeling of unresolved includes (180159)
	 *  30 - templates: method/constructor templates, typedef specializations
	 *  31 - macros: added file locations
	 *  32 - support stand-alone function types (181936)
	 *  33 - templates: constructor instances
	 *  34 - fix for base classes represented by qualified names (183843)
	 *  35 - add scanner configuration hash-code (62366)
	 * #36#- changed chunk size back to 4K (184892) - <<CDT 4.0>>
	 * #37#- added index for nested bindings (189811), compatible with version 36 - <<CDT 4.0.1>>
	 *  38 - added b-tree for macros (193056), compatible with version 36 and 37
	 * #39#- added flag for function-style macros (208558), compatible with version 36,37,38 - <<CDT 4.0.2>>
	 *  
	 *  50 - support for complex, imaginary and long long (bug 209049).
	 *  51 - modeling extern "C" (bug 191989)
	 *  52 - files per linkage (bug 191989)
	 *  53 - polymorphic method calls (bug 156691)
	 *  54 - optimization of database size (bug 210392)
	 *  55 - generalization of local bindings (bug 215783)
	 *  56 - using directives (bug 216527)
	 */
	
	public static final int LINKAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	public static final int PROPERTIES = Database.DATA_AREA + 8;
	public static final int MACRO_BTREE= Database.DATA_AREA + 12; 
	public static final int END= Database.DATA_AREA + 16;
	static {
		assert END <= Database.CHUNK_SIZE;
	}
	
	public static class ChangeEvent {
		public Set<IIndexFileLocation> fClearedFiles= new HashSet<IIndexFileLocation>();
		public Set<IIndexFileLocation> fFilesWritten= new HashSet<IIndexFileLocation>();
		public boolean fCleared= false;
		public boolean fReloaded= false;

		private void setCleared() {
			fReloaded= false;
			fCleared= true;
			fClearedFiles.clear();
			fFilesWritten.clear();
		}

		public void clear() {
			fReloaded= fCleared= false;
			fClearedFiles.clear();
			fFilesWritten.clear();
		}
	}
	public static interface IListener {
		public void handleChange(PDOM pdom, ChangeEvent event);
	}


	// Local caches
	protected Database db;
	private BTree fileIndex;
	private BTree fMacroIndex= null;
	private Map<String, PDOMLinkage> fLinkageIDCache = new HashMap<String, PDOMLinkage>();
	private File fPath;
	private IIndexLocationConverter locationConverter;
	private Map<String, IPDOMLinkageFactory> fPDOMLinkageFactoryCache;
	private HashMap<Object, Object> fResultCache= new HashMap<Object, Object>();
	private List<IListener> listeners;
	protected ChangeEvent fEvent= new ChangeEvent();

	public PDOM(File dbPath, IIndexLocationConverter locationConverter, Map<String, IPDOMLinkageFactory> linkageFactoryMappings) throws CoreException {
		this(dbPath, locationConverter, ChunkCache.getSharedInstance(), linkageFactoryMappings);
	}
	
	public PDOM(File dbPath, IIndexLocationConverter locationConverter, ChunkCache cache, Map<String, IPDOMLinkageFactory> linkageFactoryMappings) throws CoreException {
		fPDOMLinkageFactoryCache = linkageFactoryMappings;
		loadDatabase(dbPath, cache);
		this.locationConverter = locationConverter;
	}
	
	/**
	 * Returns whether this PDOM can never be written to. Writable subclasses should return false.
	 */
	protected boolean isPermanentlyReadOnly() {
		return true;
	}  

	private void loadDatabase(File dbPath, ChunkCache cache) throws CoreException {
		fPath= dbPath;
		final boolean lockDB= db == null || lockCount != 0;
		
		db = new Database(fPath, cache, CURRENT_VERSION, isPermanentlyReadOnly());
		fileIndex= null;	// holds on to the database, so clear it.
		fMacroIndex= null;  // same here 
		
		db.setLocked(lockDB);
		int version= db.getVersion();
		if (version >= MIN_SUPPORTED_VERSION) {
			readLinkages();
		}
		db.setLocked(lockCount != 0);
	}

	public IIndexLocationConverter getLocationConverter() {
		return locationConverter;
	}

	public boolean isCurrentVersion() throws CoreException {
		return db.getVersion() == CURRENT_VERSION;
	}

	public boolean isSupportedVersion() throws CoreException {
		return db.getVersion() >= MIN_SUPPORTED_VERSION;
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		for (Iterator<PDOMLinkage> iter = fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage = iter.next();
			linkage.accept(visitor);
		}
	}

	public void addListener(IListener listener) {
		if (listeners == null)
			listeners = new LinkedList<IListener>();
		listeners.add(listener);
	}

	public void removeListener(IListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}

	private void fireChange(ChangeEvent event) {
		if (listeners == null)
			return;
		Iterator<IListener> i = listeners.iterator();
		while (i.hasNext())
			i.next().handleChange(this, event);
	}

	public Database getDB() {
		return db;
	}

	public BTree getFileIndex() throws CoreException {
		if (fileIndex == null)
			fileIndex = new BTree(getDB(), FILE_INDEX, new PDOMFile.Comparator(getDB()));
		return fileIndex;
	}

	public PDOMFile getFile(int linkageID, IIndexFileLocation location) throws CoreException {
		return PDOMFile.findFile(this, getFileIndex(), location, linkageID, locationConverter);
	}

	public IIndexFragmentFile[] getFiles(IIndexFileLocation location) throws CoreException {
		return PDOMFile.findFiles(this, getFileIndex(), location, locationConverter);
	}

	public IIndexFragmentFile[] getAllFiles() throws CoreException {
		final List<PDOMFile> locations = new ArrayList<PDOMFile>();
		getFileIndex().accept(new IBTreeVisitor(){
			public int compare(int record) throws CoreException {
				return 0;
			}
			public boolean visit(int record) throws CoreException {
				PDOMFile file = new PDOMFile(PDOM.this, record);
				locations.add(file);
				return true;
			}
		});
		return locations.toArray(new IIndexFragmentFile[locations.size()]);
	}
	
	protected IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location) throws CoreException {
		IIndexFragmentFile file = getFile(linkageID, location);
		if (file == null) {
			PDOMFile pdomFile = new PDOMFile(this, location, linkageID);
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
		assert lockCount < 0; // needs write-lock.
		
		// Clear out the database, everything is set to zero.
		db.clear(CURRENT_VERSION);
		clearCaches();
		fEvent.setCleared();
	}
	
	void reloadFromFile(File file) throws CoreException {
		assert lockCount < 0;	// must have write lock.
		File oldFile= fPath;
		clearCaches();
		try {
			db.close();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		loadDatabase(file, db.getChunkCache());
		db.setExclusiveLock();
		oldFile.delete();
		fEvent.fReloaded= true;
	}		

	public boolean isEmpty() throws CoreException {
		return getFirstLinkageRecord() == 0;
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

		private final ArrayList<PDOMNamedNode> currentPath= new ArrayList<PDOMNamedNode>();
		private final ArrayList<BitSet> matchStack= new ArrayList<BitSet>();
		private List<PDOMNamedNode> bindings = new ArrayList<PDOMNamedNode>();
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

			if (node instanceof PDOMNamedNode) {
				PDOMNamedNode nnode = (PDOMNamedNode)node;
				String name = new String(nnode.getNameCharArray());

				// check if we have a complete match.
				final int lastIdx = pattern.length-1;
				if (matchesUpToLevel.get(lastIdx) && pattern[lastIdx].matcher(name).matches()) {
					if (nnode instanceof IBinding && filter.acceptBinding((IBinding) nnode)) {
						bindings.add(nnode);
					}
				}

				// check if we have a partial match
				if (nnode.mayHaveChildren()) {
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
						currentPath.add(nnode);
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
				matchesUpToLevel= matchStack.remove(matchStack.size()-1);
			}
		}

		public IIndexFragmentBinding[] getBindings() {
			return bindings.toArray(new IIndexFragmentBinding[bindings.size()]);
		}
	}

	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(new Pattern[] { pattern }, isFullyQualified, filter, monitor);
	}

	public IIndexFragmentBinding[] findBindings(Pattern[] pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		BindingFinder finder = new BindingFinder(pattern, isFullyQualified, filter, monitor);
		for (Iterator<PDOMLinkage> iter = fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage = iter.next();
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
		if (names.length == 0) {
			return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
		}
		ArrayList<PDOMBinding> result= new ArrayList<PDOMBinding>();
		ArrayList<PDOMNamedNode> nodes= new ArrayList<PDOMNamedNode>();
		for (Iterator<PDOMLinkage> iter = fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage = iter.next();
			if (filter.acceptLinkage(linkage)) {
				nodes.add(linkage);
				for (int i=0; i < names.length-1; i++) {
					char[] name= names[i];
					NamedNodeCollector collector= new NamedNodeCollector(linkage, name, false, true);
					for (Iterator<PDOMNamedNode> in = nodes.iterator(); in.hasNext();) {
						PDOMNode node= in.next();
						node.accept(collector);
					}
					nodes.clear();
					nodes.addAll(Arrays.asList(collector.getNodes()));
				}
				char[] name= names[names.length-1];
				BindingCollector collector= new BindingCollector(linkage, name, filter, false, true);
				for (Iterator<PDOMNamedNode> in = nodes.iterator(); in.hasNext();) {
					PDOMNode node= in.next();
					node.accept(collector);
				}
				nodes.clear();
				result.addAll(Arrays.asList(collector.getBindings()));
			}
		}
		return result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	private void readLinkages() throws CoreException {
		// populate the linkage cache
		int record= getFirstLinkageRecord();
		while (record != 0) {
			String linkageID= PDOMLinkage.getId(this, record).getString();
			IPDOMLinkageFactory factory= fPDOMLinkageFactoryCache.get(linkageID);
			if (factory != null) {
				PDOMLinkage linkage= factory.getLinkage(this, record);
				fLinkageIDCache.put(linkageID, linkage);
			}
			record= PDOMLinkage.getNextLinkageRecord(this, record);
		}
	}

	public PDOMLinkage getLinkage(String linkageID) {
		return fLinkageIDCache.get(linkageID);
	}

	public PDOMLinkage createLinkage(String linkageID) throws CoreException {
		PDOMLinkage pdomLinkage= fLinkageIDCache.get(linkageID);
		if (pdomLinkage == null) {
			// Need to create it
			IPDOMLinkageFactory factory= fPDOMLinkageFactoryCache.get(linkageID);			
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
		Iterator<PDOMLinkage> i = fLinkageIDCache.values().iterator();
		while (i.hasNext()) {
			PDOMLinkage linkage = i.next();
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
		Collection<PDOMLinkage> values = fLinkageIDCache.values();
		return values.toArray(new IIndexLinkage[values.size()]);
	}
	
	public PDOMLinkage[] getLinkageImpls() {
		Collection<PDOMLinkage> values = fLinkageIDCache.values();
		return values.toArray(new PDOMLinkage[values.size()]);
	}

	public void insertLinkage(PDOMLinkage linkage) throws CoreException {
		linkage.setNext(db.getInt(LINKAGES));
		db.putInt(LINKAGES, linkage.getRecord());
		fLinkageIDCache.put(linkage.getLinkageName(), linkage);
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
			try {
				while (lockCount < 0)
					mutex.wait();
			}
			finally {
				--waitingReaders;
			}
			++lockCount;
			db.setLocked(true);
		}
	}

	public void releaseReadLock() {
		boolean clearCache= false;
		synchronized (mutex) {
			assert lockCount > 0: "No lock to release"; //$NON-NLS-1$
			lastReadAccess= System.currentTimeMillis();
			if (lockCount > 0)
				--lockCount;
			mutex.notifyAll();
			clearCache= lockCount == 0;
			db.setLocked(lockCount != 0);
		}
		if (clearCache) {
			clearResultCache();
		}
	}

	/**
	 * Acquire a write lock on this PDOM. Blocks until any existing read/write locks are released.
	 * @throws InterruptedException
	 * @throws IllegalStateException if this PDOM is not writable
	 */
	public void acquireWriteLock() throws InterruptedException {
		acquireWriteLock(0);
	}

	/**
	 * Acquire a write lock on this PDOM, giving up the specified number of read locks first. Blocks
	 * until any existing read/write locks are released.
	 * @throws InterruptedException
	 * @throws IllegalStateException if this PDOM is not writable
	 */
	public void acquireWriteLock(int giveupReadLocks) throws InterruptedException {
		assert !isPermanentlyReadOnly();
		synchronized (mutex) {
			if (giveupReadLocks > 0) {
				// give up on read locks
				assert lockCount >= giveupReadLocks: "Not enough locks to release"; //$NON-NLS-1$
				if (lockCount < giveupReadLocks) {
					giveupReadLocks= lockCount;
				}
			}
			else {
				giveupReadLocks= 0;
			}

			// Let the readers go first
			while (lockCount > giveupReadLocks || waitingReaders > 0)
				mutex.wait();
			lockCount= -1;
			db.setExclusiveLock();
		}
	}

	final public void releaseWriteLock() {
		releaseWriteLock(0, true);
	}
	
	public void releaseWriteLock(int establishReadLocks, boolean flush) {
		clearResultCache();
		try {
			db.giveUpExclusiveLock(flush);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		assert lockCount == -1;
		lastWriteAccess= System.currentTimeMillis();
		final ChangeEvent event= fEvent;
		fEvent= new ChangeEvent();
		synchronized (mutex) {
			if (lockCount < 0)
				lockCount= establishReadLocks;
			mutex.notifyAll();
			db.setLocked(lockCount != 0);
		}
		fireChange(event);
	}


	public long getLastWriteAccess() {
		return lastWriteAccess;
	}
	 
	public long getLastReadAccess() {
		return lastReadAccess;
	}

	protected PDOMLinkage adaptLinkage(ILinkage linkage) throws CoreException {
		return fLinkageIDCache.get(linkage.getLinkageName());
	}

	public IIndexFragmentBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null) {
			return null;
		}
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

	public IIndexFragmentBinding findBinding(IIndexFragmentName indexName) throws CoreException {
		if (indexName instanceof PDOMName) {
			PDOMName pdomName= (PDOMName) indexName;
			return pdomName.getBinding();
		}
		return null;
	}

	public IIndexFragmentName[] findNames(IBinding binding, int options) throws CoreException {
		ArrayList<PDOMName> names= new ArrayList<PDOMName>();
		PDOMBinding pdomBinding = (PDOMBinding) adaptBinding(binding);
		if (pdomBinding != null) {
			names= new ArrayList<PDOMName>();
			findNamesForMyBinding(pdomBinding, options, names);
		}
		if ((options & SEARCH_ACCROSS_LANGUAGE_BOUNDARIES) != 0) {
			PDOMBinding[] xlangBindings= getCrossLanguageBindings(binding);
			for (int j = 0; j < xlangBindings.length; j++) {
				findNamesForMyBinding(xlangBindings[j], options, names);
			}
		}
		return names.toArray(new IIndexFragmentName[names.size()]);
	}

	private void findNamesForMyBinding(PDOMBinding pdomBinding, int options, ArrayList<PDOMName> names)
			throws CoreException {
		PDOMName name;
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
	}

	public IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file) throws CoreException {
		PDOMFile pdomFile= adaptFile(file);
		if (pdomFile != null) {
			List<PDOMInclude> result = new ArrayList<PDOMInclude>();
			for (PDOMInclude i= pdomFile.getFirstIncludedBy(); i != null; i= i.getNextInIncludedBy()) {
				result.add(i);
			}
			return result.toArray(new PDOMInclude[result.size()]);
		}
		return new PDOMInclude[0];
	}

	private PDOMFile adaptFile(IIndexFragmentFile file) throws CoreException {
		if (file.getIndexFragment() == this && file instanceof PDOMFile) {
			return (PDOMFile) file;
		}

		return getFile(file.getLinkageID(), file.getLocation());
	}

	public File getPath() {
		return fPath;
	}
	
	public IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList<IIndexFragmentBinding> result= new ArrayList<IIndexFragmentBinding>();
		for (Iterator<PDOMLinkage> iter= fLinkageIDCache.values().iterator(); iter.hasNext();) {
			PDOMLinkage linkage= iter.next();
			if (filter.acceptLinkage(linkage)) {
				PDOMBinding[] bindings;
				BindingCollector visitor = new BindingCollector(linkage, prefix, filter, true, false);
				visitor.setMonitor(monitor);
				try {
					linkage.accept(visitor);
					if (!filescope) {
						linkage.getNestedBindingsIndex().accept(visitor);
					}
				}
				catch (OperationCanceledException e) {
				}
				bindings= visitor.getBindings();

				for (int j = 0; j < bindings.length; j++) {
					result.add(bindings[j]);
				}
			}
		}
		return result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	public IIndexMacro[] findMacros(char[] prefix, boolean isPrefix, boolean isCaseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList<IIndexMacro> result= new ArrayList<IIndexMacro>();
		MacroCollector visitor = new MacroCollector(this, prefix, isPrefix, isCaseSensitive);
		visitor.setMonitor(monitor);
		try {
			getMacroIndex().accept(visitor);
			result.addAll(visitor.getMacroList());
		}
		catch (OperationCanceledException e) {
		}
		return result.toArray(new IIndexMacro[result.size()]);
	}

	private BTree getMacroIndex() {
		if (fMacroIndex == null) {
			fMacroIndex= new BTree(db, MACRO_BTREE, new FindBinding.MacroBTreeComparator(this));
		}
		return fMacroIndex;
	}
	
	public void afterAddMacro(PDOMMacro macro) throws CoreException {
		getMacroIndex().insert(macro.getRecord());
	}

	public void beforeRemoveMacro(PDOMMacro macro) throws CoreException {
		getMacroIndex().delete(macro.getRecord());
	}

	public String getProperty(String propertyName) throws CoreException {
		if(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID.equals(propertyName)) {
			return FRAGMENT_PROPERTY_VALUE_FORMAT_ID;
		}
		if(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION.equals(propertyName)) {
			return ""+db.getVersion(); //$NON-NLS-1$
		}
		return new DBProperties(db, PROPERTIES).getProperty(propertyName);
	}

	public void close() throws CoreException {
		db.close();
		clearCaches();
	}

	private void clearCaches() {
		fileIndex= null;
		fMacroIndex= null;
		fLinkageIDCache.clear();
		clearResultCache();
	}

	private void clearResultCache() {
		synchronized (fResultCache) {
			fResultCache.clear();
		}
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

	protected void flush() throws CoreException {
		db.flush();
	}

	public Object getCachedResult(Object binding) {
		synchronized(fResultCache) {
			return fResultCache.get(binding);
		}
	}

	public void putCachedResult(Object key, Object result) {
		synchronized(fResultCache) {
			fResultCache.put(key, result);
		}
	}		
	
	public String createKeyForCache(int record, char[] name) {
		return new StringBuffer(name.length+2).append((char) (record >> 16)).append((char) record).append(name).toString();
	}
	
	private PDOMBinding[] getCrossLanguageBindings(IBinding binding) throws CoreException {
		switch(binding.getLinkage().getLinkageID()) {
		case ILinkage.C_LINKAGE_ID:
			return getCPPBindingForC(binding);
		case ILinkage.CPP_LINKAGE_ID:
			return getCBindingForCPP(binding);
		}
		return PDOMBinding.EMPTY_PDOMBINDING_ARRAY;
	}

	private PDOMBinding[] getCBindingForCPP(IBinding binding) throws CoreException {
		PDOMBinding result= null;
		PDOMLinkage c= getLinkage(ILinkage.C_LINKAGE_NAME);
		if (c == null) {
			return PDOMBinding.EMPTY_PDOMBINDING_ARRAY;
		}
		try {
			if (binding instanceof ICPPFunction) {
				ICPPFunction func = (ICPPFunction) binding;
				if (func.isExternC()) {
					result = FindBinding.findBinding(c.getIndex(), this, func.getNameCharArray(),
							new int[] { IIndexCBindingConstants.CFUNCTION }, 0);
				}
			} else if (binding instanceof ICPPVariable) {
				ICPPVariable var = (ICPPVariable) binding;
				if (var.isExternC()) {
					result = FindBinding.findBinding(c.getIndex(), this, var.getNameCharArray(),
							new int[] { IIndexCBindingConstants.CVARIABLE }, 0);
				}
			}
		} catch (DOMException e) {
		}
		return result == null ? PDOMBinding.EMPTY_PDOMBINDING_ARRAY : new PDOMBinding[] {result};
	}

	private PDOMBinding[] getCPPBindingForC(IBinding binding) throws CoreException {
		PDOMLinkage cpp= getLinkage(ILinkage.CPP_LINKAGE_NAME);
		if (cpp == null) {
			return PDOMBinding.EMPTY_PDOMBINDING_ARRAY;
		}
		IndexFilter filter= null;
		if (binding instanceof IFunction) {
			filter= new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					try {
						if (binding instanceof ICPPFunction) {
							return ((ICPPFunction) binding).isExternC();
						}
					} catch (DOMException e) {
					}
					return false;
				}
			};
		} else if (binding instanceof IVariable) {
			if (!(binding instanceof IField) && !(binding instanceof IParameter)) {
				filter= new IndexFilter() {
					@Override
					public boolean acceptBinding(IBinding binding) {
						try {
							if (binding instanceof ICPPVariable) {
								return ((ICPPVariable) binding).isExternC();
							}
						} catch (DOMException e) {
						}
						return false;
					}
				};
			}
		}
		if (filter != null) {
			BindingCollector collector= new BindingCollector(cpp, binding.getNameCharArray(), filter, false, true);
			cpp.accept(collector);
			return collector.getBindings();
		}
		return PDOMBinding.EMPTY_PDOMBINDING_ARRAY;
	}

	public IIndexFragmentFileSet createFileSet() {
		return new PDOMFileSet();
	}
}