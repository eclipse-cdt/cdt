/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     James Blackburn - Modified patch for 149428
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Warren Paul (Nokia)
 *	   IBM Corporation (EFS Support)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.LocalProjectScope;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescription;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeChangeListener;

public class CModelManager implements IResourceChangeListener, IContentTypeChangeListener, ICProjectDescriptionListener {
	public static boolean VERBOSE = false;

	/**
	 * Unique handle onto the CModel
	 */
	final CModel cModel = new CModel();

	public static HashSet<String> OptionNames = new HashSet<String>(20);

	public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with ElementChangedEvent event masks

	/**
	 * Used to convert <code>IResourceDelta</code>s into <code>ICElementDelta</code>s.
	 */
	protected final DeltaProcessor fDeltaProcessor = new DeltaProcessor();

	/**
	 * Queue of deltas created explicitly by the C Model that
	 * have yet to be fired.
	 */
	List<ICElementDelta> fCModelDeltas = Collections.synchronizedList(new ArrayList<ICElementDelta>());

	/**
	 * Queue of reconcile deltas on working copies that have yet to be fired.
	 * This is a table form IWorkingCopy to ICElementDelta
	 */
	HashMap<IWorkingCopy, ICElementDelta> reconcileDeltas = new HashMap<IWorkingCopy, ICElementDelta>();

	/**
	 * Turns delta firing on/off. By default it is on.
	 */
	protected boolean fFire = true;

	/**
	 * Collection of listeners for C element deltas
	 */
	protected List<IElementChangedListener> fElementChangedListeners = Collections.synchronizedList(new ArrayList<IElementChangedListener>());

	/**
	 * A map from ITranslationUnit to IWorkingCopy of the shared working copies.
	 */
	private Map<IBufferFactory, Map<ITranslationUnit, WorkingCopy>> sharedWorkingCopies = new HashMap<IBufferFactory, Map<ITranslationUnit, WorkingCopy>>();
	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Map<ICElement,ICElement> elementsOutOfSynchWithBuffers = new HashMap<ICElement, ICElement>(11);

	/*
	 * Temporary cache of newly opened elements
	 */
	private ThreadLocal<Map<ICElement, CElementInfo>> temporaryCache = new ThreadLocal<Map<ICElement, CElementInfo>>();

	/**
	 * Infos cache.
	 */
	protected CModelCache cache = new CModelCache();

	/**
	 * This is a cache of the projects before any project addition/deletion has started.
	 */
	public ICProject[] cProjectsCache;

	/**
	 * The list of started BinaryRunners on projects.
	 */
	private final Map<IProject, BinaryRunner> binaryRunners = new HashMap<IProject, BinaryRunner>();

	/**
	 * Map of the binary parser for each project.
	 */
	private final Map<IProject, BinaryParserConfig[]> binaryParsersMap = Collections.synchronizedMap(new HashMap<IProject, BinaryParserConfig[]>());

	/**
	 * The lis of the SourceMappers on projects.
	 */
	private HashMap<ICProject, SourceMapper> sourceMappers = new HashMap<ICProject, SourceMapper>();

	public static final IWorkingCopy[] NoWorkingCopy = new IWorkingCopy[0];

	static volatile CModelManager factory = null;

	private CModelManager() {
	}

	public static CModelManager getDefault() {
		if (factory == null) {
			synchronized (CModelManager.class) {
				if (factory != null)
					return factory;

				factory = new CModelManager();

				// Register to the workspace;
				ResourcesPlugin.getWorkspace().addResourceChangeListener(factory,
						IResourceChangeEvent.POST_CHANGE
						| IResourceChangeEvent.PRE_DELETE
						| IResourceChangeEvent.PRE_CLOSE);

				// Register the Core Model on the Descriptor
				// Manager, it needs to know about changes.
//				CCorePlugin.getDefault().getCDescriptorManager().addDescriptorListener(factory);

				// Register as project description listener
				CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(factory,
						CProjectDescriptionEvent.APPLIED);

				// Register the Core Model on the ContentTypeManager
				// it needs to know about changes.
				Platform.getContentTypeManager().addContentTypeChangeListener(factory);
			}
		}
		return factory;
	}

	/**
	 * Returns the CModel for the given workspace, creating
	 * it if it does not yet exist.
	 */
	public ICModel getCModel(IWorkspaceRoot root) {
		return getCModel();
	}

	public CModel getCModel() {
		return cModel;
	}

	public ICElement create(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// Assume it is fullpath relative to workspace
		IResource res = root.findMember(path);
		if (res == null) {
			IPath rootPath = root.getLocation();
			if (path.equals(rootPath)) {
				return getCModel(root);
			}
			res = root.getContainerForLocation(path);
			if (res == null || !res.exists()) {
				res = root.getFileForLocation(path);
			}
			if (res != null && !res.exists()) {
				res = null;
			}
		}

		// In case this is an external resource see if we can find
		// a file for it.
		if (res == null) {
			res= ResourceLookup.selectFileForLocation(path, null);
		}

		return create(res, null);
	}

	public ICElement create(IResource resource, ICProject cproject) {
		if (resource == null) {
			return null;
		}

		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT:
				return create((IProject)resource);
			case IResource.FILE:
				return create((IFile)resource, cproject);
			case IResource.FOLDER:
				return create((IFolder)resource, cproject);
			case IResource.ROOT:
				return getCModel((IWorkspaceRoot)resource);
			default:
				return null;
		}
	}

	public ICProject create(IProject project) {
		if (project == null) {
			return null;
		}
		return cModel.getCProject(project);
	}

	public ICContainer create(IFolder folder, ICProject cproject) {
		if (folder == null) {
			return null;
		}
		if (cproject == null) {
			cproject = create(folder.getProject());
		}
		try {
			ICElement[] children = cproject.getChildren();
			for (int i = 0; i < children.length; ++i) {
				if (children[i] instanceof ISourceRoot) {
					ISourceRoot root = (ISourceRoot)children[i];
					if (root.isOnSourceEntry(folder)) {
						// Get the container
						IPath path = folder.getFullPath();
						path = path.removeFirstSegments(root.getPath().segmentCount());
						String[] segments = path.segments();
						ICContainer cfolder = root;
						for (int j = 0; j < segments.length; ++j) {
							cfolder = cfolder.getCContainer(segments[j]);
						}
						return cfolder;
					}
				} else if (children[i] instanceof ICContainer) {
					ICContainer root = (ICContainer)children[i];
					IPath rootPath = root.getPath();
					IPath path = folder.getFullPath();
					if (rootPath.isPrefixOf(path) && cproject.isOnOutputEntry(folder)) {
						path = path.removeFirstSegments(root.getPath().segmentCount());
						String[] segments = path.segments();
						ICContainer cfolder = root;
						for (int j = 0; j < segments.length; ++j) {
							cfolder = cfolder.getCContainer(segments[j]);
						}
						return cfolder;
					}
				}
			}
		} catch (CModelException e) {
			//
		}
		return null;
	}

	public ICElement create(IFile file, ICProject cproject) {
		if (file == null) {
			return null;
		}
		if (cproject == null) {
			cproject = create(file.getProject());
		}
		ICElement celement = null;
		try {
			// First look for TU's
			IPath resourcePath = file.getFullPath();
			ISourceRoot[] roots = cproject.getSourceRoots();
			for (int i = 0; i < roots.length; ++i) {
				ISourceRoot root = roots[i];
				if (root.isOnSourceEntry(file)) {
					IPath rootPath = root.getPath();
					IPath path = resourcePath.removeFirstSegments(rootPath.segmentCount());
					String fileName = path.lastSegment();
					path = path.removeLastSegments(1);
					String[] segments = path.segments();
					ICContainer cfolder = root;
					for (String segment : segments) {
						cfolder = cfolder.getCContainer(segment);
					}
					if (CoreModel.isValidTranslationUnitName(cproject.getProject(), fileName)) {
						celement = cfolder.getTranslationUnit(fileName);
					}
					break;
				}
			}

			// check for binary on output entry
			if (celement == null && cproject.isOnOutputEntry(file)) {
				IBinaryFile bin = createBinaryFile(file);
				if (bin != null)
					celement = create(file, bin, cproject);
			}
		} catch (CModelException e) {
			//
		}
		return celement;
	}

	public ICElement create(IFile file, IBinaryFile bin, ICProject cproject) {
		if (file == null) {
			return null;
		}
		if (bin == null) {
			return create(file, cproject);
		}
		if (cproject == null) {
			cproject = create(file.getProject());
		}
		ICElement celement = null;
		try {
			if (cproject.isOnOutputEntry(file)) {
				IPath resourcePath = file.getParent().getFullPath();
				ICElement cfolder = cproject.findElement(resourcePath);

				// Check if folder is a source root and use that instead
				ISourceRoot sourceRoot = cproject.findSourceRoot(resourcePath);
				if (sourceRoot != null)
					cfolder = sourceRoot;

				if (bin.getType() == IBinaryFile.ARCHIVE) {
					ArchiveContainer vlib = (ArchiveContainer)cproject.getArchiveContainer();
					celement = new Archive(cfolder, file, (IBinaryArchive)bin);
					vlib.addChild(celement);
				} else {
					BinaryContainer vbin = (BinaryContainer)cproject.getBinaryContainer();
					celement = new Binary(cfolder, file, (IBinaryObject)bin);
					vbin.addChild(celement);
				}
			}
		} catch (CModelException e) {
			//
		}
		return celement;
	}

	public ITranslationUnit createTranslationUnitFrom(ICProject cproject, IPath path) {
		if (path == null || cproject == null) {
			return null;
		}

		final IProject project= cproject.getProject();
		final String contentTypeId = CoreModel.getRegistedContentTypeId(project, path.lastSegment());

		if (path.isAbsolute()) {
			if (!Util.isNonZeroLengthFile(path)) {
				return null;
			}

			try {
				IIncludeReference[] includeReferences = cproject.getIncludeReferences();
				for (IIncludeReference includeReference : includeReferences) {
					if (includeReference.isOnIncludeEntry(path)) {
						String headerContentTypeId= contentTypeId;
						if (headerContentTypeId == null) {
							headerContentTypeId= CoreModel.hasCCNature(project) ? CCorePlugin.CONTENT_TYPE_CXXHEADER : CCorePlugin.CONTENT_TYPE_CHEADER;
						}

						// TODO:  use URI
						return new ExternalTranslationUnit(includeReference, URIUtil.toURI(path), headerContentTypeId);
					}
				}
			} catch (CModelException e) {
			}

			// if the file exists and it has a known C/C++ file extension then just create
			// an external translation unit for it.
			if (contentTypeId != null && path.toFile().exists()) {
				// TODO:  use URI
				return new ExternalTranslationUnit(cproject, URIUtil.toURI(path), contentTypeId);
			}
		} else {
			// !path.isAbsolute()
			try {
				IIncludeReference[] includeReferences = cproject.getIncludeReferences();
				for (IIncludeReference includeReference : includeReferences) {
					IPath includePath = includeReference.getPath().append(path);
					if (Util.isNonZeroLengthFile(includePath)) {
						String headerContentTypeId= contentTypeId;
						if (headerContentTypeId == null) {
							headerContentTypeId= CoreModel.hasCCNature(project) ? CCorePlugin.CONTENT_TYPE_CXXHEADER : CCorePlugin.CONTENT_TYPE_CHEADER;
						}

						// TODO:  use URI
						return new ExternalTranslationUnit(includeReference, URIUtil.toURI(includePath), headerContentTypeId);
					}
				}
			} catch (CModelException e) {
			}
		}
		return null;
	}

	/**
	 * Creates a translation unit in the given project for the given location.
	 *
	 * @param cproject
	 * @param locationURI
	 * @return ITranslationUnit
	 */
	public ITranslationUnit createTranslationUnitFrom(ICProject cproject, URI locationURI) {
		if (locationURI == null || cproject == null) {
			return null;
		}

		if(!locationURI.isAbsolute()) {
			throw new IllegalArgumentException();
		}

		final IProject project= cproject.getProject();

		IFileStore fileStore = null;
		try {
			fileStore = EFS.getStore(locationURI);
		} catch (CoreException e1) {
			CCorePlugin.log(e1);
			return null;
		}

		final String contentTypeId = CoreModel.getRegistedContentTypeId(project, fileStore.getName());
			if (!Util.isNonZeroLengthFile(locationURI)) {
				return null;
			}

			try {
				IIncludeReference[] includeReferences = cproject.getIncludeReferences();
				for (IIncludeReference includeReference : includeReferences) {
					// crecoskie
					// TODO FIXME:  include entries don't handle URIs yet
					IPath path = URIUtil.toPath(locationURI);
					if (path != null && includeReference.isOnIncludeEntry(path)) {
						String headerContentTypeId= contentTypeId;
						if (headerContentTypeId == null) {
							headerContentTypeId= CoreModel.hasCCNature(project) ? CCorePlugin.CONTENT_TYPE_CXXHEADER : CCorePlugin.CONTENT_TYPE_CHEADER;
						}

						return new ExternalTranslationUnit(includeReference, locationURI, headerContentTypeId);
					}
				}
			} catch (CModelException e) {
			}

			// if the file exists and it has a known C/C++ file extension then just create
			// an external translation unit for it.
			IFileInfo info = fileStore.fetchInfo();

			if (contentTypeId != null && info != null && info.exists()) {
				return new ExternalTranslationUnit(cproject, locationURI, contentTypeId);
			}

		return null;
	}

	public void releaseCElement(ICElement celement) {
		// Guard.
		if (celement == null)
			return;

		//System.out.println("RELEASE " + celement.getElementName());

		// Remove from the containers.
		if (celement instanceof IParent) {
			CElementInfo info = (CElementInfo)peekAtInfo(celement);
			if (info != null) {
				ICElement[] children = info.getChildren();
				for (ICElement element : children) {
					releaseCElement(element);
				}
			}

			// Make sure any object specifics not part of the children be destroy
			// For example the CProject needs to destroy the BinaryContainer and ArchiveContainer
			if (celement instanceof CElement) {
				try {
					((CElement)celement).closing(info);
				} catch (CModelException e) {
					//
				}
			}

			// If an entire folder was deleted we need to update the
			// BinaryContainer/ArchiveContainer also.
			if (celement.getElementType() == ICElement.C_CCONTAINER) {
				ICProject cproject = celement.getCProject();
				CProjectInfo pinfo = (CProjectInfo)peekAtInfo(cproject);
				ArrayList<ICElement> list = new ArrayList<ICElement>(5);
				if (pinfo != null && pinfo.vBin != null) {
					if (peekAtInfo(pinfo.vBin) != null) {
						try {
							ICElement[] bins = pinfo.vBin.getChildren();
							for (ICElement bin : bins) {
								if (celement.getPath().isPrefixOf(bin.getPath())) {
									//pinfo.vBin.removeChild(bins[i]);
									list.add(bin);
								}
							}
						} catch (CModelException e) {
							// ..
						}
					}
				}
				if (pinfo != null && pinfo.vLib != null) {
					if (peekAtInfo(pinfo.vLib) != null) {
						try {
							ICElement[] ars = pinfo.vLib.getChildren();
							for (ICElement ar : ars) {
								if (celement.getPath().isPrefixOf(ar.getPath())) {
									//pinfo.vLib.removeChild(ars[i]);
									list.add(ar);
								}
							}
						} catch (CModelException e) {
							// ..
						}
					}
				}
				// release any binary/archive that was in the path
				for (int i = 0; i < list.size(); i++) {
					ICElement b = list.get(i);
					releaseCElement(b);
				}
			}
		}

		// Remove the child from the parent list.
		//Parent parent = (Parent)celement.getParent();
		//if (parent != null && peekAtInfo(parent) != null) {
		//	parent.removeChild(celement);
		//}

		removeInfo(celement);
	}

	public BinaryParserConfig[] getBinaryParser(IProject project) {
		BinaryParserConfig[] parsers = binaryParsersMap.get(project);
		if (parsers == null) {
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project, false);
			if (desc != null) {
				ICConfigurationDescription cfgDesc = desc.getDefaultSettingConfiguration();
				if (cfgDesc != null) {
					ICConfigExtensionReference[] refs = cfgDesc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
					if (refs.length > 0) {
						ArrayList<BinaryParserConfig> list = new ArrayList<BinaryParserConfig>(refs.length);
						for (ICConfigExtensionReference ref : refs) {
							BinaryParserConfig config = new BinaryParserConfig(ref);
							list.add(config);
						}
						parsers = new BinaryParserConfig[list.size()];
						list.toArray(parsers);
					} else {
						// no parser configured
						parsers = new BinaryParserConfig[0];
					}
				}
			}
			if (parsers == null) {
				try {
					BinaryParserConfig config = new BinaryParserConfig(CCorePlugin.getDefault().getDefaultBinaryParser(), CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID);
					parsers = new BinaryParserConfig[]{config};
				} catch (CoreException e1) {
				}
			}
		}
		if (parsers != null) {
			binaryParsersMap.put(project, parsers);
			return parsers;
		}
		return new BinaryParserConfig[0];
	}

	public IBinaryFile createBinaryFile(IFile file) {
		BinaryParserConfig[] parsers = getBinaryParser(file.getProject());
		if (parsers.length == 0) {
			return null;
		}

		// Only if file has no extension, has an extension that is an integer
		// or is a binary file content type
		String ext = file.getFileExtension();
		if (ext != null && ext.length() > 0) {
			// shared libraries often have a version number
			// strip version extension: libc.so.3.2.1 -> libc.so
			IPath baseFileName = new Path(file.getName());
			outer: do {
				for (int i = 0; i < ext.length(); ++i) {
					if (!Character.isDigit(ext.charAt(i))) {
						break outer;
					}
				}
				// extension is a number -> remove it
				baseFileName = baseFileName.removeFileExtension();
				ext = baseFileName.getFileExtension();
			} while (ext != null && ext.length() > 0);

			boolean isBinary= false;
			final IContentTypeManager ctm = Platform.getContentTypeManager();
			final IContentType ctbin = ctm.getContentType(CCorePlugin.CONTENT_TYPE_BINARYFILE);
			final IContentType[] cts= ctm.findContentTypesFor(baseFileName.toString());
			for (int i=0; !isBinary && i < cts.length; i++) {
				isBinary= cts[i].isKindOf(ctbin);
			}
			if (!isBinary) {
				return null;
			}
		}

		URI fileUri = file.getLocationURI();
		//Avoid name special devices, empty files and the like
		if (!Util.isNonZeroLengthFile(fileUri)) {
			// PR:xxx the EFS does not seem to work for newly created file
			// so before bailing out give another try?
			//Avoid name special devices, empty files and the like
			if("file".equals(fileUri.getScheme())) { //$NON-NLS-1$
				File f = new File(fileUri);
				if (f.length() == 0) {
					return null;
				}
			}
			//return null;
		}

		int hints = 0;

		for (BinaryParserConfig parser2 : parsers) {
			IBinaryParser parser = null;
			try {
				parser = parser2.getBinaryParser();
				if (parser.getHintBufferSize() > hints) {
					hints = Math.max(hints, parser.getHintBufferSize());
				}
			} catch (CoreException e) {
			}
		}
		byte[] bytes = new byte[hints];
		if (hints > 0) {
			InputStream is = null;
			try {
				is = file.getContents();
				int count = 0;
				// Make sure we read up to 'hints' bytes if we possibly can
				while (count < hints) {
					int bytesRead = is.read(bytes, count, hints - count);
					if (bytesRead < 0)
						break;
					count += bytesRead;
				}
				if (count > 0 && count < bytes.length) {
					byte[] array = new byte[count];
					System.arraycopy(bytes, 0, array, 0, count);
					bytes = array;
				}
			} catch (CoreException e) {
				return null;
			} catch (IOException e) {
				return null;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		IPath location = file.getLocation();

		for (BinaryParserConfig parser2 : parsers) {
			try {
				IBinaryParser parser = parser2.getBinaryParser();
				if (parser.isBinary(bytes, location)) {
    			    IBinaryFile binFile = parser.getBinary(bytes, location);
    			    if (binFile != null) {
    			    	return binFile;
    			    }
				}
			} catch (IOException e) {
			} catch (CoreException e) {
			}
		}
		return null;
	}

	public void resetBinaryParser(IProject project) {
		if (project != null) {
			ICProject cproject = create(project);
			if (cproject != null) {
				// Let the function remove the children
				// but it has the side of effect of removing the CProject also
				// so we have to recall create again.
				try {
					cproject.close();
				} catch (CModelException e) {
					CCorePlugin.log(e);
				}
				binaryParsersMap.remove(project);

				// Fired and ICElementDelta.PARSER_CHANGED
				CElementDelta delta = new CElementDelta(getCModel());
				delta.binaryParserChanged(cproject);
				registerCModelDelta(delta);
				fire(ElementChangedEvent.POST_CHANGE);
			}
		}
	}

	public BinaryRunner getBinaryRunner(ICProject cproject) {
		BinaryRunner runner = null;
		IProject project = cproject.getProject();
		synchronized (binaryRunners) {
			runner = binaryRunners.get(project);
		}
		if (runner == null) {
			// creation of BinaryRunner must occur outside the synchronized block
			runner = new BinaryRunner(project);
			synchronized (binaryRunners) {
				if (binaryRunners.get(project) == null) {
					binaryRunners.put(project, runner);
					runner.start();
				} else {
					// another thread was faster
					runner = binaryRunners.get(project);
				}
			}
		}
		return runner;
	}

	public void removeBinaryRunner(ICProject cproject) {
		removeBinaryRunner(cproject.getProject());
	}

	public void removeBinaryRunner(IProject project) {
		BinaryRunner runner;
		synchronized (binaryRunners) {
			runner = binaryRunners.remove(project);
		}
		if (runner != null) {
			runner.stop();
		}
	}

	public SourceMapper getSourceMapper(ICProject cProject) {
		SourceMapper mapper = null;
		synchronized (sourceMappers) {
			mapper = sourceMappers.get(cProject);
			if (mapper == null) {
				mapper = new SourceMapper(cProject);
				sourceMappers.put(cProject, mapper);
			}
		}
		return mapper;
	}

	/**
	 * addElementChangedListener method comment.
	 */
	public void addElementChangedListener(IElementChangedListener listener) {
		synchronized (fElementChangedListeners) {
			if (!fElementChangedListeners.contains(listener)) {
				fElementChangedListeners.add(listener);
			}
		}
	}

	/**
	 * removeElementChangedListener method comment.
	 */
	public void removeElementChangedListener(IElementChangedListener listener) {
		synchronized (fElementChangedListeners) {
			int i = fElementChangedListeners.indexOf(listener);
			if (i != -1) {
				fElementChangedListeners.remove(i);
			}
		}
	}

	/**
	 * Registers the given delta with this manager. This API is to be
	 * used to registerd deltas that are created explicitly by the C
	 * Model. Deltas created as translations of <code>IResourceDeltas</code>
	 * are to be registered with <code>#registerResourceDelta</code>.
	 */
	public void registerCModelDelta(ICElementDelta delta) {
		fCModelDeltas.add(delta);
	}

	/**
	 * Notifies this C Model Manager that some resource changes have happened
	 * on the platform, and that the C Model should update any required
	 * internal structures such that its elements remain consistent.
	 * Translates <code>IResourceDeltas</code> into <code>ICElementDeltas</code>.
	 *
	 * @see IResourceDelta
	 * @see IResource
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IResourceDelta delta = event.getDelta();
			IResource resource = event.getResource();
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_DELETE:
				try {
					if (resource.getType() == IResource.PROJECT &&
							( ((IProject)resource).hasNature(CProjectNature.C_NATURE_ID) ||
									((IProject)resource).hasNature(CCProjectNature.CC_NATURE_ID) )){
						this.preDeleteProject((IProject) resource);}
				} catch (CoreException e) {
				}
				break;

			case IResourceChangeEvent.PRE_CLOSE:
				try {
					if (resource.getType() == IResource.PROJECT &&
					    ( ((IProject)resource).hasNature(CProjectNature.C_NATURE_ID) ||
					      ((IProject)resource).hasNature(CCProjectNature.CC_NATURE_ID) )){
						this.preCloseProject((IProject) resource);}
				} catch (CoreException e) {
				}
				break;

			case IResourceChangeEvent.POST_CHANGE:
				try {
					if (delta != null) {
						checkForProjectRename(delta);
						ICElementDelta[] translatedDeltas = fDeltaProcessor.processResourceDelta(delta);
						if (translatedDeltas.length > 0) {
							for (ICElementDelta translatedDelta : translatedDeltas) {
								registerCModelDelta(translatedDelta);
							}
						}
						fire(ElementChangedEvent.POST_CHANGE);
					}
				} catch (Exception e) {
					CCorePlugin.log(e);
				}
				break;
			}
		}
	}

	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		switch(event.getEventType()) {
		case CProjectDescriptionEvent.APPLIED:
			CProjectDescription newDes = (CProjectDescription)event.getNewCProjectDescription();
			CProjectDescription oldDes = (CProjectDescription)event.getOldCProjectDescription();
			if(oldDes != null && newDes != null) {
				ICConfigurationDescription newCfg = newDes.getDefaultSettingConfiguration();
				ICConfigurationDescription oldCfg = oldDes.getDefaultSettingConfiguration();
				int flags = 0;
				if(oldCfg != null && newCfg != null){
					if(newCfg.getId().equals(oldCfg.getId())){
						ICDescriptionDelta cfgDelta = findCfgDelta(event.getProjectDelta(), newCfg.getId());
						if(cfgDelta != null){
							flags = cfgDelta.getChangeFlags() & (ICDescriptionDelta.EXT_REF | ICDescriptionDelta.OWNER);
						}
					} else {
						flags = CProjectDescriptionManager.getInstance().calculateDescriptorFlags(newCfg, oldCfg);
					}
				}
				if ((flags & ICDescriptionDelta.EXT_REF) != 0) {
					// update binary parsers
					IProject project = newDes.getProject();
					try {
						ICConfigExtensionReference[] newExts = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(project);
						BinaryParserConfig[] currentConfigs = binaryParsersMap.get(project);
						// anything added/removed
						if (currentConfigs != null) {
							if (newExts.length != currentConfigs.length) {
								resetBinaryParser(project);
							} else { // may reorder
								for (int i = 0; i < newExts.length; i++) {
									if (!newExts[i].getID().equals(currentConfigs[i].getId())) {
										resetBinaryParser(project);
										break;
									}
								}
							}
						}
					} catch (CoreException e) {
						resetBinaryParser(project);
					}
				}
			}
			break;
		}
	}

	private ICDescriptionDelta findCfgDelta(ICDescriptionDelta delta, String id){
		if(delta == null)
			return null;
		ICDescriptionDelta children[] = delta.getChildren();
		for(int i = 0; i < children.length; i++){
			ICSettingObject s = children[i].getNewSetting();
			if(s != null && id.equals(s.getId()))
				return children[i];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeListener#contentTypeChanged()
	 */
	@Override
	public void contentTypeChanged(ContentTypeChangeEvent event) {
		ContentTypeProcessor.processContentTypeChanges(new ContentTypeChangeEvent[]{ event });
	}

	public void contentTypeChanged(ContentTypeChangeEvent[] events) {
		ContentTypeProcessor.processContentTypeChanges(events);
	}

	public void fire(int eventType) {
		fire(null, eventType);
	}

	public void fireShift(ICElement element, int offset, int size, int lines) {
		ICElementDelta delta = new CShiftData(element, offset, size, lines);
		fire(delta, ElementChangedEvent.POST_SHIFT);
	}

	/**
	 * Fire C Model deltas, flushing them after the fact.
	 * If the firing mode has been turned off, this has no effect.
	 */
	@SuppressWarnings("deprecation")
	void fire(ICElementDelta customDeltas, int eventType) {
		if (fFire) {
			ICElementDelta deltaToNotify;
			if (customDeltas == null) {
				deltaToNotify = mergeDeltas(this.fCModelDeltas);
			} else {
				deltaToNotify = customDeltas;
			}

			IElementChangedListener[] listeners;
			int listenerCount;
			int[] listenerMask;
			// Notification
			synchronized (fElementChangedListeners) {
				listeners = new IElementChangedListener[fElementChangedListeners.size()];
				fElementChangedListeners.toArray(listeners);
				listenerCount = listeners.length;
				listenerMask = null;
			}

			switch (eventType) {
			case DEFAULT_CHANGE_EVENT:
				firePreAutoBuildDelta(deltaToNotify, listeners, listenerMask, listenerCount);
				firePostChangeDelta(deltaToNotify, listeners, listenerMask, listenerCount);
				fireReconcileDelta(listeners, listenerMask, listenerCount);
				break;
			case ElementChangedEvent.PRE_AUTO_BUILD:
				firePreAutoBuildDelta(deltaToNotify, listeners, listenerMask, listenerCount);
				break;
			case ElementChangedEvent.POST_CHANGE:
				firePostChangeDelta(deltaToNotify, listeners, listenerMask, listenerCount);
				fireReconcileDelta(listeners, listenerMask, listenerCount);
				break;
			case ElementChangedEvent.POST_RECONCILE:
				fireReconcileDelta(listeners, listenerMask, listenerCount);
				break;
			case ElementChangedEvent.POST_SHIFT:
				fireShiftEvent(deltaToNotify, listeners, listenerMask, listenerCount);
				return;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void firePreAutoBuildDelta(ICElementDelta deltaToNotify,
			IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {
		if (Util.VERBOSE_DELTA) {
			System.out.println("FIRING PRE_AUTO_BUILD Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			notifyListeners(deltaToNotify, ElementChangedEvent.PRE_AUTO_BUILD, listeners, listenerMask, listenerCount);
		}
	}

	private void firePostChangeDelta(ICElementDelta deltaToNotify, IElementChangedListener[] listeners,
			int[] listenerMask, int listenerCount) {
		// post change deltas
		if (Util.VERBOSE_DELTA) {
			System.out.println("FIRING POST_CHANGE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
				// flush now so as to keep listener reactions to post their own deltas for subsequent iteration
			this.flush();
			notifyListeners(deltaToNotify, ElementChangedEvent.POST_CHANGE, listeners, listenerMask, listenerCount);
		}
	}

	private void fireReconcileDelta(IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {
		ICElementDelta deltaToNotify = mergeDeltas(this.reconcileDeltas.values());
		if (Util.VERBOSE_DELTA) {
			System.out.println("FIRING POST_RECONCILE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own deltas for subsequent iteration
			this.reconcileDeltas = new HashMap<IWorkingCopy, ICElementDelta>();
			notifyListeners(deltaToNotify, ElementChangedEvent.POST_RECONCILE, listeners, listenerMask, listenerCount);
		}
	}

	private void fireShiftEvent(ICElementDelta deltaToNotify, IElementChangedListener[] listeners,
			int[] listenerMask, int listenerCount) {
		// post change deltas
		if (Util.VERBOSE_DELTA) {
			System.out.println("FIRING POST_SHIFT event [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			this.flush();
			notifyListeners(deltaToNotify, ElementChangedEvent.POST_SHIFT, listeners, listenerMask, listenerCount);
		}
	}

	public void notifyListeners(ICElementDelta deltaToNotify, int eventType,
		IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {

		final ElementChangedEvent extraEvent = new ElementChangedEvent(deltaToNotify, eventType);
		for (int i = 0; i < listenerCount; i++) {
			if (listenerMask == null || (listenerMask[i] & eventType) != 0) {
				final IElementChangedListener listener = listeners[i];
				long start = -1;
				if (Util.VERBOSE_DELTA) {
					System.out.print("Listener #" + (i + 1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				// wrap callbacks with Safe runnable for subsequent listeners to be called when some are causing grief
				SafeRunner.run(new ISafeRunnable() {

					@Override
					public void handleException(Throwable exception) {
						//CCorePlugin.log(exception, "Exception occurred in listener of C element change notification"); //$NON-NLS-1$
						CCorePlugin.log(exception);
					}
					@Override
					public void run() throws Exception {
						listener.elementChanged(extraEvent);
					}
				});
				if (Util.VERBOSE_DELTA) {
					System.out.println(" -> " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	/**
	 * Flushes all deltas without firing them.
	 */
	protected void flush() {
		fCModelDeltas.clear();
	}

	private ICElementDelta mergeDeltas(Collection<ICElementDelta> deltas) {
		synchronized (deltas) {
			if (deltas.size() == 0)
				return null;
			if (deltas.size() == 1)
				return deltas.iterator().next();
			if (deltas.size() <= 1)
				return null;

			ICElement cRoot = getCModel();
			CElementDelta rootDelta = new CElementDelta(cRoot);
			boolean insertedTree = false;
			for (ICElementDelta delta : deltas) {
				ICElement element = delta.getElement();
				if (cRoot.equals(element)) {
					for (ICElementDelta child : delta.getAffectedChildren()) {
						CElementDelta projectDelta = (CElementDelta)child;
						rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
						insertedTree = true;
					}
					IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
					if (resourceDeltas != null) {
						for (IResourceDelta resourceDelta : resourceDeltas) {
							rootDelta.addResourceDelta(resourceDelta);
							insertedTree = true;
						}
					}
				} else {
					rootDelta.insertDeltaTree(element, (CElementDelta)delta);
					insertedTree = true;
				}
			}
			if (insertedTree) {
				return rootDelta;
			}
			return null;
		}
	}

	/**
	 * Returns the set of elements which are out of synch with their buffers.
	 */
	protected Map<ICElement,ICElement> getElementsOutOfSynchWithBuffers() {
		return this.elementsOutOfSynchWithBuffers;
	}

	/**
	 * Returns the info for the element.
	 */
	public synchronized Object getInfo(ICElement element) {
		Map<ICElement, CElementInfo> tempCache = this.temporaryCache.get();
		if (tempCache != null) {
			Object result = tempCache.get(element);
			if (result != null)
				return result;
		}
		return this.cache.getInfo(element);
	}

	/**
	 *  Returns the info for this element without
	 *  disturbing the cache ordering.
	 */
	protected synchronized Object peekAtInfo(ICElement element) {
		Map<ICElement, CElementInfo> tempCache = this.temporaryCache.get();
		if (tempCache != null) {
			Object result = tempCache.get(element);
			if (result != null) {
				return result;
			}
		}
		return this.cache.peekAtInfo(element);
	}

	/*
	 * Puts the infos in the given map (keys are ICElements and values are CElementInfos)
	 * in the C model cache in an atomic way.
	 * First checks that the info for the opened element (or one of its ancestors) has not been
	 * added to the cache. If it is the case, another thread has opened the element (or one of
	 * its ancestors). So returns without updating the cache.
	 */
	protected synchronized void putInfos(ICElement openedElement, Map<ICElement, CElementInfo> newElements) {
		// remove children
		Object existingInfo = this.cache.peekAtInfo(openedElement);
		if (openedElement instanceof IParent && existingInfo instanceof CElementInfo) {
			ICElement[] children = ((CElementInfo)existingInfo).getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				CElement child = (CElement)children[i];
				try {
					child.close();
				} catch (CModelException e) {
					// ignore
				}
			}
		}

		for (Map.Entry<ICElement, CElementInfo> element : newElements.entrySet())
			this.cache.putInfo(element.getKey(), element.getValue());
	}

	/**
	 * Removes all cached info from the C Model, including all children,
	 * but does not close this element.
	 */
	protected synchronized void removeChildrenInfo(ICElement openedElement) {
		// remove children
		Object existingInfo = this.cache.peekAtInfo(openedElement);
		if (openedElement instanceof IParent && existingInfo instanceof CElementInfo) {
			ICElement[] children = ((CElementInfo)existingInfo).getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				CElement child = (CElement)children[i];
				try {
					child.close();
				} catch (CModelException e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Removes the info of this model element.
	 */
	protected synchronized void removeInfo(ICElement element) {
		this.cache.removeInfo(element);
	}

	/*
	 * Returns the temporary cache for newly opened elements for the current thread.
	 * Creates it if not already created.
	 */
	public Map<ICElement, CElementInfo> getTemporaryCache() {
		Map<ICElement, CElementInfo> result = this.temporaryCache.get();
		if (result == null) {
			result = new HashMap<ICElement, CElementInfo>();
			this.temporaryCache.set(result);
		}
		return result;
	}

	/*
	 * Returns whether there is a temporary cache for the current thread.
	 */
	public boolean hasTemporaryCache() {
		return this.temporaryCache.get() != null;
	}

	/*
	 * Resets the temporary cache for newly created elements to null.
	 */
	public void resetTemporaryCache() {
		this.temporaryCache.set(null);
	}

	public void startup() {
		// Initialization is performed on the first getDefault()...
	}

	public void shutdown() {
		// Remove ourself from the DescriptorManager.
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
//		CCorePlugin.getDefault().getCDescriptorManager().removeDescriptorListener(factory);
		// Remove ourself from the ContentTypeManager
		Platform.getContentTypeManager().removeContentTypeChangeListener(factory);

		// Do any shutdown of services.
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(factory);

		BinaryRunner[] runners;
		synchronized (binaryRunners) {
			runners = binaryRunners.values().toArray(new BinaryRunner[0]);
		}
		for (BinaryRunner runner : runners) {
			runner.stop();
		}

		// Nullify the static factory
		factory = null;
	}

	private void checkForProjectRename(IResourceDelta delta) {
		IResourceDelta[] rem= delta.getAffectedChildren(IResourceDelta.REMOVED);
		for (IResourceDelta element : rem) {
			delta = element;
			IResource res= delta.getResource();
			if (res.getType() == IResource.PROJECT) {
				IPath movedTo= null;
				if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
					movedTo= delta.getMovedToPath();
				}
				LocalProjectScope.deletePreferences(res.getFullPath(), movedTo);
			}
		}
	}

	private void preDeleteProject(IProject project) {
		// remove binary parsers
		binaryParsersMap.remove(project);
		// stop the binary runner for this project
		removeBinaryRunner(project);
		// stop indexing jobs for this project
		CCoreInternals.getPDOMManager().preDeleteProject(create(project));
	}

	private void preCloseProject(IProject project) {
		// remove binary parsers
		binaryParsersMap.remove(project);
		// stop the binary runner for this project
		removeBinaryRunner(project);
		// stop indexing jobs for this project
		CCoreInternals.getPDOMManager().preCloseProject(create(project));
	}

	public IWorkingCopy[] getSharedWorkingCopies(IBufferFactory factory) {
		// if factory is null, default factory must be used
		if (factory == null)
			factory = BufferManager.getDefaultBufferManager().getDefaultBufferFactory();

		Map<ITranslationUnit, WorkingCopy> perFactoryWorkingCopies = sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null)
			return NoWorkingCopy;
		Collection<WorkingCopy> copies = perFactoryWorkingCopies.values();
		return copies.toArray(new IWorkingCopy[copies.size()]);
	}

	public IWorkingCopy findSharedWorkingCopy(IBufferFactory factory, ITranslationUnit tu) {
		// if factory is null, default factory must be used
		if (factory == null)
			factory = BufferManager.getDefaultBufferManager();

		Map<ITranslationUnit, WorkingCopy> perFactoryWorkingCopies = sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null)
			return null;

		return perFactoryWorkingCopies.get(tu);
	}

	public IWorkingCopy getSharedWorkingCopy(IBufferFactory factory, ITranslationUnit tu, IProblemRequestor requestor,
			IProgressMonitor monitor) throws CModelException {
		// if factory is null, default factory must be used
		if (factory == null)
			factory = BufferManager.getDefaultBufferManager();

		Map<ITranslationUnit, WorkingCopy> perFactoryWorkingCopies = sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null) {
			perFactoryWorkingCopies = new HashMap<ITranslationUnit, WorkingCopy>();
			sharedWorkingCopies.put(factory, perFactoryWorkingCopies);
		}
		WorkingCopy workingCopy = perFactoryWorkingCopies.get(tu);
		if (workingCopy != null) {
			workingCopy.useCount++;
			return workingCopy;
		}
		CreateWorkingCopyOperation op = new CreateWorkingCopyOperation(tu, perFactoryWorkingCopies,
				factory, requestor);
		op.runOperation(monitor);
		return (IWorkingCopy) op.getResultElements()[0];
	}

	public IWorkingCopy removeSharedWorkingCopy(final IBufferFactory bufferFactory,	ITranslationUnit originalElement) {
		// In order to be shared, working copies have to denote the same compilation unit
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map<ITranslationUnit, WorkingCopy> perFactoryWorkingCopies = sharedWorkingCopies.get(bufferFactory);
		if (perFactoryWorkingCopies != null) {
			return perFactoryWorkingCopies.remove(originalElement);
		}
		return null;
	}
}
