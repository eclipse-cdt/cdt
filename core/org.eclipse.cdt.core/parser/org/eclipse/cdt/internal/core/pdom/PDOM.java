/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Andrew Ferguson (Symbian)
 *    Anton Leherbauer (Wind River Systems)
 *    Sergey Prigogin (Google)
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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexMacroContainer;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.dom.Linkage;
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
import org.eclipse.cdt.internal.core.pdom.dom.MacroContainerCollector;
import org.eclipse.cdt.internal.core.pdom.dom.MacroContainerPatternCollector;
import org.eclipse.cdt.internal.core.pdom.dom.NamedNodeCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMInclude;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacroContainer;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacroReferenceName;
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
 * Database for storing semantic information for one project.
 */
public class PDOM extends PlatformObject implements IPDOM {
	private static final int BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL = 30000;
	private static final int LONG_WRITE_LOCK_REPORT_THRESHOLD = 1000;
	private static final int LONG_READ_LOCK_WAIT_REPORT_THRESHOLD = 1000;
	static boolean sDEBUG_LOCKS= false; // initialized in the PDOMManager, because IBM needs PDOM independent of runtime plugin.

	/**
	 * Identifier for PDOM format
	 * @see IIndexFragment#PROPERTY_FRAGMENT_FORMAT_ID
	 */
	public static final String FRAGMENT_PROPERTY_VALUE_FORMAT_ID= "org.eclipse.cdt.internal.core.pdom.PDOM"; //$NON-NLS-1$
	
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
	 *  57.0 - macro references (bug 156561)
	 *  58.0 - non-type parameters (bug 207840)
	 *  59.0 - changed modeling of deferred class instances (bug 229218)
	 *  60.0 - store integral values with basic types (bug 207871)
	 *  #61.0# - properly insert macro undef statements into macro-containers (bug 234591) - <<CDT 5.0>>
	 *  
	 *  CDT 6.0 development
	 *  70.0 - cleaned up templates, fixes bug 236197
	 *  71.0 - proper support for anonymous unions, bug 206450
	 *  72.0 - store project-relative paths for resources that belong to the project, bug 239472
	 *  72.1 - store flag for pure virtual methods.
	 *  73.0 - add values for variables and enumerations, bug 250788
	 *  74.0 - changes for proper template argument support, bug 242668
	 *  75.0 - support for friends, bug 250167
	 *  76.0 - support for exception specification, bug 252697
	 *  77.0 - support for parameter annotations, bug 254520
	 *  78.0 - support for updating class templates, bug 254520
	 *  79.0 - instantiation of values, bug 245027
	 *  80.0 - support for specializations of partial specializations, bug 259872
	 *  81.0 - change to c++ function types, bug 264479
	 *  82.0 - offsets for using directives, bug 270806
	 *  #83.0# - unconditionally store name in PDOMInclude, bug 272815 - <<CDT 6.0>>
	 *  #84.0# - storing free record pointers as (ptr>>3), bug 279620 - <<CDT 6.0.1>>
	 *  
	 *  CDT 7.0 development (versions not supported on the 6.0.x branch)
	 *  90.0 - support for array sizes, bug 269926
	 *  91.0 - storing unknown bindings other than unknown class types, bug 284686.
	 *  92.0 - simplification of basic types, bug 231859.
	 *  93.0 - further simplification of basic types, bug 231859.
	 *  94.0 - new model for storing types, bug 294306.
	 *  95.0 - parameter packs, bug 294730.
	 *  96.0 - storing pack expansions in the template parameter map, bug 294730.
	 *  97.0 - storing file contents hash in PDOMFile, bug 302083.
	 *  #98.0# - strongly typed enums, bug 305975.  <<CDT 7.0.0>>
	 *  99.0 - correct marshalling of basic types, bug 319186.
	 *  
	 *  CDT 8.0 development (versions not supported on the 7.0.x branch)
	 *  110.0 - update index on encoding change, bug 317435.
	 *  111.0 - correct marshalling of basic types, bug 319186.
	 *  111.1 - defaulted and deleted functions, bug 305978
	 */
	private static final int MIN_SUPPORTED_VERSION= version(111, 0);
	private static final int MAX_SUPPORTED_VERSION= version(111, Short.MAX_VALUE);
	private static final int DEFAULT_VERSION = version(111, 1);
	
	private static int version(int major, int minor) {
		return (major << 16) + minor;
	}

	/**
	 * Returns the version that shall be used when creating new databases
	 */
	public static int getDefaultVersion() {
		return DEFAULT_VERSION;
	}
	
	public static boolean isSupportedVersion(int vers) {
		return vers >= MIN_SUPPORTED_VERSION && vers <= MAX_SUPPORTED_VERSION;
	}
	public static int getMinSupportedVersion() {
		return MIN_SUPPORTED_VERSION;
	}
	public static int getMaxSupportedVersion() {
		return MAX_SUPPORTED_VERSION;
	}
	public static String versionString(int version) {
		final int major= version >> 16;
		final int minor= version & 0xffff;
		return "" + major + '.' + minor; //$NON-NLS-1$
	}
	
	public static final int LINKAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	public static final int PROPERTIES = Database.DATA_AREA + 8;
	public static final int END= Database.DATA_AREA + 12;
	static {
		assert END <= Database.CHUNK_SIZE;
	}
	
	public static class ChangeEvent {
		public Set<IIndexFileLocation> fClearedFiles= new HashSet<IIndexFileLocation>();
		public Set<IIndexFileLocation> fFilesWritten= new HashSet<IIndexFileLocation>();
		private boolean fCleared= false;
		private boolean fReloaded= false;
		private boolean fNewFiles= false;

		private void setCleared() {
			fCleared= true;
			fReloaded= false;
			fNewFiles= false;

			fClearedFiles.clear();
			fFilesWritten.clear();
		}

		public boolean isCleared() {
			return fCleared;
		}

		public void setReloaded() {
			fReloaded= true;
		}

		public boolean isReloaded() {
			return fReloaded;
		}

		public void setHasNewFiles() {
			fNewFiles = true;
		}

		public boolean hasNewFiles() {
			return fNewFiles;
		}

		public boolean isTrivial() {
			return !fCleared && !fReloaded && !fNewFiles && fClearedFiles.isEmpty() && fFilesWritten.isEmpty();
		}
	}

	public static interface IListener {
		public void handleChange(PDOM pdom, ChangeEvent event);
	}

	// Local caches
	protected Database db;
	private BTree fileIndex;
	private Map<Integer, PDOMLinkage> fLinkageIDCache = new HashMap<Integer, PDOMLinkage>();
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
		if (sDEBUG_LOCKS) {
			fLockDebugging= new HashMap<Thread, DebugLockInfo>();
			System.out.println("Debugging PDOM Locks"); //$NON-NLS-1$
		}
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
		
		clearCaches();
		db = new Database(fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());
		
		db.setLocked(lockDB);
		if (isSupportedVersion()) {
			readLinkages();
		}
		db.setLocked(lockCount != 0);
	}

	public IIndexLocationConverter getLocationConverter() {
		return locationConverter;
	}

	public boolean isSupportedVersion() throws CoreException {
		final int version = db.getVersion();
		return version >= MIN_SUPPORTED_VERSION && version <= MAX_SUPPORTED_VERSION;
	}

	private void readLinkages() throws CoreException {
		long record= getFirstLinkageRecord();
		while (record != 0) {
			String linkageID= PDOMLinkage.getLinkageID(this, record).getString();
			IPDOMLinkageFactory factory= fPDOMLinkageFactoryCache.get(linkageID);
			if (factory != null) {
				PDOMLinkage linkage= factory.getLinkage(this, record);
				fLinkageIDCache.put(linkage.getLinkageID(), linkage);
			}
			record= PDOMLinkage.getNextLinkageRecord(this, record);
		}
	}

	protected PDOMLinkage createLinkage(int linkageID) throws CoreException {
		PDOMLinkage pdomLinkage= fLinkageIDCache.get(linkageID);
		if (pdomLinkage == null) {
			final String linkageName= Linkage.getLinkageName(linkageID);
			IPDOMLinkageFactory factory= fPDOMLinkageFactoryCache.get(linkageName);			
			if (factory != null) {
				return factory.createLinkage(this);
			}
		}
		return pdomLinkage;
	}
	
	public PDOMLinkage getLinkage(int linkageID) throws CoreException {
		return fLinkageIDCache.get(linkageID);
	}

	private Collection<PDOMLinkage> getLinkageList() {
		return fLinkageIDCache.values();
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		for (PDOMLinkage linkage : getLinkageList()) {
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
		if (listeners == null || event.isTrivial())
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
		PDOMLinkage linkage= getLinkage(linkageID);
		if (linkage == null)
			return null;
		return PDOMFile.findFile(linkage, getFileIndex(), location, locationConverter);
	}
	
	public PDOMFile getFile(PDOMLinkage linkage, IIndexFileLocation location) throws CoreException {
		return PDOMFile.findFile(linkage, getFileIndex(), location, locationConverter);
	}

	public IIndexFragmentFile[] getFiles(IIndexFileLocation location) throws CoreException {
		return PDOMFile.findFiles(this, getFileIndex(), location, locationConverter);
	}

	public IIndexFragmentFile[] getAllFiles() throws CoreException {
		final List<PDOMFile> locations = new ArrayList<PDOMFile>();
		getFileIndex().accept(new IBTreeVisitor(){
			public int compare(long record) throws CoreException {
				return 0;
			}
			public boolean visit(long record) throws CoreException {
				PDOMFile file = PDOMFile.recreateFile(PDOM.this, record);
				locations.add(file);
				return true;
			}
		});
		return locations.toArray(new IIndexFragmentFile[locations.size()]);
	}
	
	protected IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location) throws CoreException {
		PDOMLinkage linkage= createLinkage(linkageID);
		IIndexFragmentFile file = getFile(linkage, location);
		if (file == null) {
			PDOMFile pdomFile = new PDOMFile(linkage, location, linkageID);
			getFileIndex().insert(pdomFile.getRecord());
			file= pdomFile;
			fEvent.setHasNewFiles();
		}
		return file;
	}

	protected void clearFileIndex() throws CoreException {
		db.putRecPtr(FILE_INDEX, 0);
		fileIndex = null;	
	}
	
	protected void clear() throws CoreException {
		assert lockCount < 0; // needs write-lock.
		
		// Clear out the database, everything is set to zero.
		int vers = getDefaultVersion();
		db.clear(vers);
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
		IBinding binding= name.resolveBinding();
		if (binding != null) {
			PDOMLinkage linkage= adaptLinkage(name.getLinkage());
			if (linkage != null) {
				return findBindingInLinkage(linkage, binding);
			}
		} else if (name.getPropertyInParent() == IASTPreprocessorStatement.MACRO_NAME) {
			PDOMLinkage linkage= adaptLinkage(name.getLinkage());
			if (linkage != null) {
				return linkage.findMacroContainer(name.getSimpleID());
			}
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
				PDOMNamedNode nnode = (PDOMNamedNode) node;
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
					// Avoid visiting unscoped enumerator items twice
					if (pattern.length == 1 && nnode instanceof ICPPEnumeration
							&& !((ICPPEnumeration) nnode).isScoped()) {
						return false;
					}
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

	public IIndexFragmentBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		// check for some easy cases
		Boolean caseSensitive= getCaseSensitive(patterns);
		if (caseSensitive != null) {
			char[][] simpleNames= extractSimpleNames(patterns);
			if (simpleNames != null) {
				if (simpleNames.length == 1) {
					return findBindings(simpleNames[0], isFullyQualified, caseSensitive, filter, monitor);
				} else if (isFullyQualified) {
					return findBindings(simpleNames, caseSensitive, filter, monitor);
				}
			}

			char[] prefix= extractPrefix(patterns);
			if (prefix != null) {
				return findBindingsForPrefix(prefix, isFullyQualified, caseSensitive, filter, monitor);
			}
		}
		
		BindingFinder finder = new BindingFinder(patterns, isFullyQualified, filter, monitor);
		for (PDOMLinkage linkage : getLinkageList()) {
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

	private Boolean getCaseSensitive(Pattern[] patterns) {
		Boolean caseSensitive= null;
		for (Pattern p : patterns) {
			switch(p.flags()) {
			case 0:
				if (caseSensitive == Boolean.FALSE) {
					return null;
				}
				caseSensitive= Boolean.TRUE;
				break;
			case Pattern.CASE_INSENSITIVE:
				if (caseSensitive == Boolean.TRUE) {
					return null;
				}
				caseSensitive= Boolean.FALSE;
				break;
			default:
				return null;
			}
		}
		return caseSensitive;
	}

	private char[][] extractSimpleNames(Pattern[] pattern) {
		char[][] result= new char[pattern.length][];
		int i= 0;
		for (Pattern p : pattern) {
			char[] input= p.pattern().toCharArray();
			for (char c : input) {
				if (!Character.isLetterOrDigit(c) && c != '_') {
					return null;
				}
			}
			result[i++]= input;
		}
		return result;
	}

	private char[] extractPrefix(Pattern[] pattern) {
		if (pattern.length != 1)
			return null;
		
		String p= pattern[0].pattern();
		if (p.endsWith(".*")) { //$NON-NLS-1$
			char[] input= p.substring(0, p.length()-2).toCharArray();
			for (char c : input) {
				if (!Character.isLetterOrDigit(c) && c != '_') {
					return null;
				}
			}
			return input;
		}
		return null;
	}
	
	public IIndexFragmentBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		
		Pattern[] patterns= new Pattern[]{pattern};
		Boolean caseSensitive= getCaseSensitive(patterns);
		if (caseSensitive != null) {
			char[][] simpleNames= extractSimpleNames(patterns);
			if (simpleNames != null && simpleNames.length == 1) {
				return findMacroContainers(simpleNames[0], false, caseSensitive, filter, monitor);
			} 
			char[] prefix= extractPrefix(patterns);
			if (prefix != null) {
				return findMacroContainers(prefix, true, caseSensitive, filter, monitor);
			}
		}

		List<IIndexFragmentBinding> result= new ArrayList<IIndexFragmentBinding>();
		for (PDOMLinkage linkage : getLinkageList()) {
			if (filter.acceptLinkage(linkage)) {
				try {
					MacroContainerPatternCollector finder = new MacroContainerPatternCollector(linkage, pattern, monitor);
					linkage.getMacroIndex().accept(finder);
					result.addAll(Arrays.asList(finder.getMacroContainers()));
				} catch (CoreException e) {
					if (e.getStatus() != Status.OK_STATUS)
						throw e;
					else
						return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
				}
			}
		}
		return  result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	public IIndexFragmentBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(names, true, filter, monitor);
	}
	
	public IIndexFragmentBinding[] findBindings(char[][] names, boolean caseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (names.length == 0) {
			return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
		}
		ArrayList<PDOMBinding> result= new ArrayList<PDOMBinding>();
		ArrayList<PDOMNamedNode> nodes= new ArrayList<PDOMNamedNode>();
		for (PDOMLinkage linkage : getLinkageList()) {
			if (filter.acceptLinkage(linkage)) {
				nodes.add(linkage);
				for (int i=0; i < names.length-1; i++) {
					char[] name= names[i];
					NamedNodeCollector collector= new NamedNodeCollector(linkage, name, false, caseSensitive);
					for (Iterator<PDOMNamedNode> in = nodes.iterator(); in.hasNext();) {
						PDOMNode node= in.next();
						node.accept(collector);
					}
					nodes.clear();
					nodes.addAll(Arrays.asList(collector.getNodes()));
				}
				char[] name= names[names.length-1];
				BindingCollector collector= new BindingCollector(linkage, name, filter, false, caseSensitive);
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

	
	private long getFirstLinkageRecord() throws CoreException {
		return db.getRecPtr(LINKAGES);
	}

	public IIndexLinkage[] getLinkages() {
		Collection<PDOMLinkage> values = getLinkageList();
		return values.toArray(new IIndexLinkage[values.size()]);
	}
	
	public PDOMLinkage[] getLinkageImpls() {
		Collection<PDOMLinkage> values = getLinkageList();
		return values.toArray(new PDOMLinkage[values.size()]);
	}

	public void insertLinkage(PDOMLinkage linkage) throws CoreException {
		linkage.setNext(db.getRecPtr(LINKAGES));
		db.putRecPtr(LINKAGES, linkage.getRecord());
		fLinkageIDCache.put(linkage.getLinkageID(), linkage);
	}

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private Object mutex = new Object();
	private int lockCount;
	private int waitingReaders;
	private long lastWriteAccess= 0;
	private long lastReadAccess= 0;
	private long timeWriteLockAcquired;

	public void acquireReadLock() throws InterruptedException {
		long t = sDEBUG_LOCKS ? System.nanoTime() : 0;
		synchronized (mutex) {
			++waitingReaders;
			try {
				while (lockCount < 0)
					mutex.wait();
			} finally {
				--waitingReaders;
			}
			++lockCount;
			db.setLocked(true);

			if (sDEBUG_LOCKS) {
				t = (System.nanoTime() - t) / 1000000;
				if (t >= LONG_READ_LOCK_WAIT_REPORT_THRESHOLD) {
					System.out.println("Acquired index read lock after " + t + " ms wait."); //$NON-NLS-1$//$NON-NLS-2$
				}
				incReadLock(fLockDebugging);
			}
		}
	}

	public void releaseReadLock() {
		boolean clearCache= false;
		synchronized (mutex) {
			assert lockCount > 0: "No lock to release"; //$NON-NLS-1$
			if (sDEBUG_LOCKS) {
				decReadLock(fLockDebugging);
			}
			
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
			if (sDEBUG_LOCKS) {
				incWriteLock(giveupReadLocks);
			}

			if (giveupReadLocks > 0) {
				// give up on read locks
				assert lockCount >= giveupReadLocks: "Not enough locks to release"; //$NON-NLS-1$
				if (lockCount < giveupReadLocks) {
					giveupReadLocks= lockCount;
				}
			} else {
				giveupReadLocks= 0;
			}

			// Let the readers go first
			long start= sDEBUG_LOCKS ? System.currentTimeMillis() : 0;
			while (lockCount > giveupReadLocks || waitingReaders > 0) {
				mutex.wait(BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL);
				if (sDEBUG_LOCKS) {
					start = reportBlockedWriteLock(start, giveupReadLocks);
				}
			}
			lockCount= -1;
			if (sDEBUG_LOCKS)
				timeWriteLockAcquired = System.currentTimeMillis();
			db.setExclusiveLock();
		}
	}

	final public void releaseWriteLock() {
		releaseWriteLock(0, true);
	}
	
	@SuppressWarnings("nls")
	public void releaseWriteLock(int establishReadLocks, boolean flush) {
		// When all locks are released we can clear the result cache.
		if (establishReadLocks == 0) {
			clearResultCache();
		}
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
			if (sDEBUG_LOCKS) {
				long timeHeld = lastWriteAccess - timeWriteLockAcquired;
				if (timeHeld >= LONG_WRITE_LOCK_REPORT_THRESHOLD) {
					System.out.println("Index write lock held for " + timeHeld + " ms");
				}
				decWriteLock(establishReadLocks);
			}

			if (lockCount < 0)
				lockCount= establishReadLocks;
			mutex.notifyAll();
			db.setLocked(lockCount != 0);
		}
		fireChange(event);
	}

	public boolean hasWaitingReaders() {
		synchronized (mutex) {
			return waitingReaders > 0;
		}
	}
	
	public long getLastWriteAccess() {
		return lastWriteAccess;
	}
	 
	public long getLastReadAccess() {
		return lastReadAccess;
	}

	protected PDOMLinkage adaptLinkage(ILinkage linkage) throws CoreException {
		return fLinkageIDCache.get(linkage.getLinkageID());
	}

	public IIndexFragmentBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null) {
			return null;
		}
		PDOMNode pdomNode= (PDOMNode) binding.getAdapter(PDOMNode.class);
		if (pdomNode instanceof IIndexFragmentBinding && pdomNode.getPDOM() == this) {
			return (IIndexFragmentBinding) pdomNode;
		}

		PDOMLinkage linkage= adaptLinkage(binding.getLinkage());
		if (linkage != null) {
			return findBindingInLinkage(linkage, binding);
		}
		return null;
	}

	private IIndexFragmentBinding findBindingInLinkage(PDOMLinkage linkage, IBinding binding) throws CoreException {
		if (binding instanceof IMacroBinding || binding instanceof IIndexMacroContainer) {
			return linkage.findMacroContainer(binding.getNameCharArray());
		}
		return linkage.adaptBinding(binding);
	}

	public IIndexFragmentBinding findBinding(IIndexFragmentName indexName) throws CoreException {
		if (indexName instanceof PDOMName) {
			PDOMName pdomName= (PDOMName) indexName;
			return pdomName.getBinding();
		}
		return null;
	}

	public IIndexFragmentName[] findNames(IBinding binding, int options) throws CoreException {
		ArrayList<IIndexFragmentName> names= new ArrayList<IIndexFragmentName>();
		IIndexFragmentBinding myBinding= adaptBinding(binding);
		if (myBinding instanceof PDOMBinding) {
			PDOMBinding pdomBinding = (PDOMBinding) myBinding;
			findNamesForMyBinding(pdomBinding, options, names);
			if ((options & SEARCH_ACROSS_LANGUAGE_BOUNDARIES) != 0) {
				PDOMBinding[] xlangBindings= getCrossLanguageBindings(binding);
				for (PDOMBinding xlangBinding : xlangBindings) {
					findNamesForMyBinding(xlangBinding, options, names);
				}
			}
		} else if (myBinding instanceof PDOMMacroContainer) {
			final PDOMMacroContainer macroContainer = (PDOMMacroContainer) myBinding;
			findNamesForMyBinding(macroContainer, options, names);
			if ((options & SEARCH_ACROSS_LANGUAGE_BOUNDARIES) != 0) {
				PDOMMacroContainer[] xlangBindings= getCrossLanguageBindings(macroContainer);
				for (PDOMMacroContainer xlangBinding : xlangBindings) {
					findNamesForMyBinding(xlangBinding, options, names);
				}
			}
		}
		return names.toArray(new IIndexFragmentName[names.size()]);
	}

	private void findNamesForMyBinding(PDOMBinding pdomBinding, int options, ArrayList<IIndexFragmentName> names)
			throws CoreException {
		PDOMName name;
		if ((options & FIND_DECLARATIONS) != 0) {
			for (name= pdomBinding.getFirstDeclaration(); name != null; name= name.getNextInBinding()) {
				if (isCommitted(name)) {
					names.add(name);
				}
			}
		}
		if ((options & FIND_DEFINITIONS) != 0) {
			for (name = pdomBinding.getFirstDefinition(); name != null; name= name.getNextInBinding()) {
				if (isCommitted(name)) {
					names.add(name);
				}
			}
		}
		if ((options & FIND_REFERENCES) != 0) {
			for (name = pdomBinding.getFirstReference(); name != null; name= name.getNextInBinding()) {
				if (isCommitted(name)) {
					names.add(name);
				}
			}
		}
	}

	private void findNamesForMyBinding(PDOMMacroContainer container, int options, ArrayList<IIndexFragmentName> names)
			throws CoreException {
		if ((options & FIND_DEFINITIONS) != 0) {
			for (PDOMMacro macro= container.getFirstDefinition(); macro != null; macro= macro.getNextInContainer()) {
				final IIndexFragmentName name = macro.getDefinition();
				if (name != null && isCommitted(macro)) {
					names.add(name);
				}
			}
		}
		if ((options & FIND_REFERENCES) != 0) {
			for (PDOMMacroReferenceName name = container.getFirstReference(); name != null; name= name.getNextInContainer()) {
				if (isCommitted(name)) {
					names.add(name);
				}
			}
		}
	}

	protected boolean isCommitted(PDOMName name) throws CoreException {
		return true;
	}

	protected boolean isCommitted(PDOMMacro name) throws CoreException {
		return true;
	}

	protected boolean isCommitted(PDOMMacroReferenceName name) throws CoreException {
		return true;
	}

	public IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file) throws CoreException {
		PDOMFile pdomFile= adaptFile(file);
		if (pdomFile != null) {
			List<PDOMInclude> result = new ArrayList<PDOMInclude>();
			for (PDOMInclude i= pdomFile.getFirstIncludedBy(); i != null; i= i.getNextInIncludedBy()) {
				if (i.getIncludedBy().getTimestamp() > 0) {
					result.add(i);
				}
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
		return findBindingsForPrefix(prefix, filescope, false, filter, monitor);
	}
	
	public IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, boolean caseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList<IIndexFragmentBinding> result= new ArrayList<IIndexFragmentBinding>();
		for (PDOMLinkage linkage : getLinkageList()) {
			if (filter.acceptLinkage(linkage)) {
				PDOMBinding[] bindings;
				BindingCollector visitor = new BindingCollector(linkage, prefix, filter, true, caseSensitive);
				visitor.setMonitor(monitor);
				try {
					linkage.accept(visitor);
					if (!filescope) {
						// Avoid adding unscoped enumerator items twice
						visitor.setSkipGlobalEnumerators(true);
						linkage.getNestedBindingsIndex().accept(visitor);
					}
				} catch (OperationCanceledException e) {
				}
				bindings= visitor.getBindings();

				for (PDOMBinding binding : bindings) {
					result.add(binding);
				}
			}
		}
		return result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	public IIndexFragmentBinding[] findBindings(char[] name, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(name, filescope, true, filter, monitor);
	}
	
	public IIndexFragmentBinding[] findBindings(char[] name, boolean filescope, boolean isCaseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList<IIndexFragmentBinding> result= new ArrayList<IIndexFragmentBinding>();
		try {
			for (PDOMLinkage linkage : getLinkageList()) {
				if (filter.acceptLinkage(linkage)) {
					if (isCaseSensitive) {
						PDOMBinding[] bindings= linkage.getBindingsViaCache(name, monitor);
						for (PDOMBinding binding : bindings) {
							if (filter.acceptBinding(binding)) {
								result.add(binding);
							}
						}
					} 
					
					if (!isCaseSensitive || !filescope) {
						BindingCollector visitor= new BindingCollector(linkage, name, filter, false, isCaseSensitive);
						visitor.setMonitor(monitor);
					
						if (!isCaseSensitive)
							linkage.accept(visitor);
						
						if (!filescope) {
							// Avoid adding unscoped enumerator items twice
							visitor.setSkipGlobalEnumerators(true);
							linkage.getNestedBindingsIndex().accept(visitor);
						}
						
						PDOMBinding[] bindings = visitor.getBindings();
						for (PDOMBinding binding : bindings) {
							result.add(binding);
						}
					}
				}
			}
		} catch (OperationCanceledException e) {
		}
		return result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	public IIndexFragmentBinding[] findMacroContainers(char[] prefix, boolean isPrefix, boolean isCaseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList<IIndexFragmentBinding> result= new ArrayList<IIndexFragmentBinding>();
		try {
			for (PDOMLinkage linkage : getLinkageList()) {
				if (filter.acceptLinkage(linkage)) {
					MacroContainerCollector visitor = new MacroContainerCollector(linkage, prefix, isPrefix, isCaseSensitive);
					visitor.setMonitor(monitor);
					linkage.getMacroIndex().accept(visitor);
					result.addAll(visitor.getMacroList());
				}
			}
		}
		catch (OperationCanceledException e) {
		}
		return result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	public IIndexMacro[] findMacros(char[] prefix, boolean isPrefix, boolean isCaseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList<IIndexMacro> result= new ArrayList<IIndexMacro>();
		try {
			for (PDOMLinkage linkage : getLinkageList()) {
				if (filter.acceptLinkage(linkage)) {
					MacroContainerCollector visitor = new MacroContainerCollector(linkage, prefix, isPrefix, isCaseSensitive);
					visitor.setMonitor(monitor);
					linkage.getMacroIndex().accept(visitor);
					for (PDOMMacroContainer mcont : visitor.getMacroList()) {
						result.addAll(Arrays.asList(mcont.getDefinitions()));
					}
				}
			}
		} catch (OperationCanceledException e) {
		}
		return result.toArray(new IIndexMacro[result.size()]);
	}
	
	public String getProperty(String propertyName) throws CoreException {
		if (IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID.equals(propertyName)) {
			return FRAGMENT_PROPERTY_VALUE_FORMAT_ID;
		}
		int version = db.getVersion();
		if (IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION.equals(propertyName)) {
			return PDOM.versionString(version);
		}
		// play it safe, properties are accessed before version checks.
		if (PDOM.isSupportedVersion(version)) {
			return new DBProperties(db, PROPERTIES).getProperty(propertyName);
		}
		if (IIndexFragment.PROPERTY_FRAGMENT_ID.equals(propertyName)) {
			return "Unknown"; //$NON-NLS-1$
		}
		return null;
	}

	public void close() throws CoreException {
		db.close();
		clearCaches();
	}

	private void clearCaches() {
		fileIndex= null;
		fLinkageIDCache.clear();
		clearResultCache();
	}

	public void clearResultCache() {
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

	public Object getCachedResult(Object key) {
		synchronized (fResultCache) {
			return fResultCache.get(key);
		}
	}

	public void putCachedResult(Object key, Object result) {
		putCachedResult(key, result, true);
	}
	
	public Object putCachedResult(Object key, Object result, boolean replace) {
		synchronized (fResultCache) {
			Object old= fResultCache.put(key, result);
			if (old != null && !replace) {
				fResultCache.put(key, old);
				return old;
			}
			return result;
		}
	}		

	public void removeCachedResult(Object key) {
		synchronized (fResultCache) {
			fResultCache.remove(key);
		}
	}		

	public String createKeyForCache(long record, char[] name) {
		return new StringBuilder(name.length + 2).append((char) (record >> 16)).append((char) record).append(name).toString();
	}

	public boolean hasLastingDefinition(PDOMBinding binding) throws CoreException {
		return binding.hasDefinition();
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

	private PDOMMacroContainer[] getCrossLanguageBindings(PDOMMacroContainer binding) throws CoreException {
		final int inputLinkage= binding.getLinkage().getLinkageID();
		if (inputLinkage == ILinkage.C_LINKAGE_ID || inputLinkage == ILinkage.CPP_LINKAGE_ID) {
			final char[] name= binding.getNameCharArray();
			for (PDOMLinkage linkage : getLinkageList()) {
				final int linkageID = linkage.getLinkageID();
				if (linkageID != inputLinkage) {
					if (linkageID == ILinkage.C_LINKAGE_ID || linkageID == ILinkage.CPP_LINKAGE_ID) {
						PDOMMacroContainer container= linkage.findMacroContainer(name);
						if (container != null) {
							return new PDOMMacroContainer[] {container};
						}
					}
				}
			}
		}
		return new PDOMMacroContainer[0];
	}

	private PDOMBinding[] getCBindingForCPP(IBinding binding) throws CoreException {
		PDOMBinding result= null;
		PDOMLinkage c= getLinkage(ILinkage.C_LINKAGE_ID);
		if (c == null) {
			return PDOMBinding.EMPTY_PDOMBINDING_ARRAY;
		}
		try {
			if (binding instanceof ICPPFunction) {
				ICPPFunction func = (ICPPFunction) binding;
				if (func.isExternC()) {
					result = FindBinding.findBinding(c.getIndex(), c,
							func.getNameCharArray(), new int[] { IIndexCBindingConstants.CFUNCTION }, 0);
				}
			} else if (binding instanceof ICPPVariable) {
				ICPPVariable var = (ICPPVariable) binding;
				if (var.isExternC()) {
					result = FindBinding.findBinding(c.getIndex(), c,
							var.getNameCharArray(), new int[] { IIndexCBindingConstants.CVARIABLE }, 0);
				}
			} else if (binding instanceof IEnumeration) {
				result= FindBinding.findBinding(c.getIndex(), c, 
						binding.getNameCharArray(), new int[] {IIndexCBindingConstants.CENUMERATION }, 0);
			} else if (binding instanceof IEnumerator) {
				result= FindBinding.findBinding(c.getIndex(), c, 
						binding.getNameCharArray(), new int[] {IIndexCBindingConstants.CENUMERATOR }, 0);
			} else if (binding instanceof ITypedef) {
				result= FindBinding.findBinding(c.getIndex(), c, 
						binding.getNameCharArray(), new int[] {IIndexCBindingConstants.CTYPEDEF }, 0);
			} else if (binding instanceof ICompositeType) {
				final int key= ((ICompositeType) binding).getKey();
				if (key == ICompositeType.k_struct || key == ICompositeType.k_union) {
					result= FindBinding.findBinding(c.getIndex(), c,
						binding.getNameCharArray(), new int[] {IIndexCBindingConstants.CSTRUCTURE }, 0);
					if (result instanceof ICompositeType && ((ICompositeType) result).getKey() != key) {
						result= null;
					}
				}
			} 
		} catch (DOMException e) {
		}
		return result == null ? PDOMBinding.EMPTY_PDOMBINDING_ARRAY : new PDOMBinding[] {result};
	}

	private PDOMBinding[] getCPPBindingForC(IBinding binding) throws CoreException {
		PDOMLinkage cpp= getLinkage(ILinkage.CPP_LINKAGE_ID);
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
		} else if (binding instanceof IEnumeration) {
			filter= new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					return binding instanceof IEnumeration;
				}
			};
		} else if (binding instanceof ITypedef) {
			filter= new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					return binding instanceof ITypedef;
				}
			};
		} else if (binding instanceof IEnumerator) {
			filter= new IndexFilter() {
				@Override
				public boolean acceptBinding(IBinding binding) {
					return binding instanceof IEnumerator;
				}
			};
		} else if (binding instanceof ICompositeType) {
			try {
				final int key = ((ICompositeType) binding).getKey();
				filter= new IndexFilter() {
					@Override
					public boolean acceptBinding(IBinding binding) {
						try {
							if (binding instanceof ICompositeType) {
								return ((ICompositeType) binding).getKey() == key;
							}
						} catch (DOMException e) {
						}
						return false;
					}
				};
			} catch (DOMException e) {
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
	
	// For debugging lock issues
	static class DebugLockInfo {
		int fReadLocks;
		int fWriteLocks;
		List<StackTraceElement[]> fTraces= new ArrayList<StackTraceElement[]>();

		public int addTrace() {
			fTraces.add(Thread.currentThread().getStackTrace());
			return fTraces.size();
		}
		@SuppressWarnings("nls")
		public void write(String threadName) {
			System.out.println("Thread: '" + threadName + "': " + fReadLocks + " readlocks, " + fWriteLocks + " writelocks");
			for (StackTraceElement[] trace : fTraces) {
				System.out.println("  Stacktrace:");
				for (StackTraceElement ste : trace) {
					System.out.println("    " + ste); 
				}
			}
		}
		public void inc(DebugLockInfo val) {
			fReadLocks+= val.fReadLocks;
			fWriteLocks+= val.fWriteLocks;
			fTraces.addAll(val.fTraces);
		}
	}

	// For debugging lock issues
	private Map<Thread, DebugLockInfo> fLockDebugging;

	// For debugging lock issues
	private static DebugLockInfo getLockInfo(Map<Thread, DebugLockInfo> lockDebugging) {
		assert sDEBUG_LOCKS;
		
		Thread key = Thread.currentThread();
		DebugLockInfo result= lockDebugging.get(key);
		if (result == null) {
			result= new DebugLockInfo();
			lockDebugging.put(key, result);
		}
		return result;
	}

	// For debugging lock issues
	static void incReadLock(Map<Thread, DebugLockInfo> lockDebugging) {
		DebugLockInfo info = getLockInfo(lockDebugging);
		info.fReadLocks++;
		if (info.addTrace() > 10) {
			outputReadLocks(lockDebugging);
		}
	}
	
	// For debugging lock issues
	@SuppressWarnings("nls")
	static void decReadLock(Map<Thread, DebugLockInfo> lockDebugging) throws AssertionError {
		DebugLockInfo info = getLockInfo(lockDebugging);
		if (info.fReadLocks <= 0) {
			outputReadLocks(lockDebugging);
			throw new AssertionError("Superfluous releaseReadLock");
		}
		if (info.fWriteLocks != 0) {
			outputReadLocks(lockDebugging);
			throw new AssertionError("Releasing readlock while holding write lock");
		}
		if (--info.fReadLocks == 0) {
			lockDebugging.remove(Thread.currentThread());
		} else {
			info.addTrace();
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private void incWriteLock(int giveupReadLocks) throws AssertionError {
		DebugLockInfo info = getLockInfo(fLockDebugging);
		if (info.fReadLocks != giveupReadLocks) {
			outputReadLocks(fLockDebugging);
			throw new AssertionError("write lock with " + giveupReadLocks + " readlocks, expected " + info.fReadLocks);
		}
		if (info.fWriteLocks != 0)
			throw new AssertionError("Duplicate write lock"); 
		info.fWriteLocks++;
	}

	// For debugging lock issues
	private void decWriteLock(int establishReadLocks) throws AssertionError {
		DebugLockInfo info = getLockInfo(fLockDebugging);
		if (info.fReadLocks != establishReadLocks)
			throw new AssertionError("release write lock with " + establishReadLocks + " readlocks, expected " + info.fReadLocks); //$NON-NLS-1$ //$NON-NLS-2$
		if (info.fWriteLocks != 1)
			throw new AssertionError("Wrong release write lock"); //$NON-NLS-1$
		info.fWriteLocks= 0;
		if (info.fReadLocks == 0) {
			fLockDebugging.remove(Thread.currentThread());
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private long reportBlockedWriteLock(long start, int giveupReadLocks) {
		long now= System.currentTimeMillis();
		if (now >= start + BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL) {
			System.out.println();
			System.out.println("Blocked writeLock");
			System.out.println("  lockcount= " + lockCount + ", giveupReadLocks=" + giveupReadLocks + ", waitingReaders=" + waitingReaders);
			outputReadLocks(fLockDebugging);
			start= now;
		}
		return start;
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private static void outputReadLocks(Map<Thread, DebugLockInfo> lockDebugging) {
		System.out.println("---------------------  Lock Debugging -------------------------");
		for (Thread th: lockDebugging.keySet()) {
			DebugLockInfo info = lockDebugging.get(th);
			info.write(th.getName());
		}
		System.out.println("---------------------------------------------------------------");
	}
	
	// For debugging lock issues
	public void adjustThreadForReadLock(Map<Thread, DebugLockInfo> lockDebugging) {
		for (Thread th : lockDebugging.keySet()) {
			DebugLockInfo val= lockDebugging.get(th);
			if (val.fReadLocks > 0) {
				DebugLockInfo myval= fLockDebugging.get(th);
				if (myval == null) {
					myval= new DebugLockInfo();
					fLockDebugging.put(th, myval);
				}
				myval.inc(val);
				for (int i = 0; i < val.fReadLocks; i++) {
					decReadLock(fLockDebugging);
				}
			}
		}
	}
}
