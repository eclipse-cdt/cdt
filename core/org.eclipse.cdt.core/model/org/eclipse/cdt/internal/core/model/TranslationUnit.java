/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Anton Leherbauer (Wind River Systems)
 *     Warren Paul (Nokia) - Bug 218266
 *     James Blackburn (Broadcom Corp.)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.ParserLogService;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.internal.core.pdom.indexer.ProjectIndexerIncludeResolutionHeuristics;
import org.eclipse.cdt.internal.core.pdom.indexer.ProjectIndexerInputAdapter;
import org.eclipse.cdt.internal.core.util.ICanceler;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @see ITranslationUnit
 */
public class TranslationUnit extends Openable implements ITranslationUnit {
	private URI location = null;
	private String contentTypeId;

	/**
	 * If set, this is the problem requestor which will be used to notify problems
	 * detected during reconciling.
	 */
	protected IProblemRequestor problemRequestor;

	SourceManipulationInfo sourceManipulationInfo = null;
	private ILanguage fLanguageOfContext;

	public TranslationUnit(ICElement parent, IFile file, String idType) {
		super(parent, file, ICElement.C_UNIT);
		setContentTypeID(idType);
	}

	public TranslationUnit(ICElement parent, URI uri, String idType) {
		super(parent, (IResource) null, uri.toString(), ICElement.C_UNIT);
		location= uri;
		setContentTypeID(idType);
	}

	@Override
	public ITranslationUnit getTranslationUnit() {
		return this;
	}

	@Override
	public IInclude createInclude(String includeName, boolean isStd, ICElement sibling,
			IProgressMonitor monitor) throws CModelException {
		CreateIncludeOperation op = new CreateIncludeOperation(includeName, isStd, this);
		if (sibling != null) {
			op.createBefore(sibling);
		}
		op.runOperation(monitor);
		return getInclude(includeName);
	}

	@Override
	public IUsing createUsing(String usingName, boolean isDirective, ICElement sibling,
			IProgressMonitor monitor) throws CModelException {
		CreateIncludeOperation op = new CreateIncludeOperation(usingName, isDirective, this);
		if (sibling != null) {
			op.createBefore(sibling);
		}
		op.runOperation(monitor);
		return getUsing(usingName);
	}

	@Override
	public INamespace createNamespace(String namespace, ICElement sibling,
			IProgressMonitor monitor) throws CModelException {
		CreateNamespaceOperation op = new CreateNamespaceOperation(namespace, this);
		if (sibling != null) {
			op.createBefore(sibling);
		}
		op.runOperation(monitor);
		return getNamespace(namespace);
	}

	@Override
	public ICElement getElementAtLine(int line) throws CModelException {
		ICElement[] celements = getChildren();
		for (ICElement celement : celements) {
			ISourceRange range = ((ISourceReference)celement).getSourceRange();
			int startLine = range.getStartLine();
			int endLine = range.getEndLine();
			if (line >= startLine && line <= endLine) {
				return celement;
			}
		}
		return null;
	}

	@Override
	public ICElement getElementAtOffset(int pos) throws CModelException {
		ICElement e = getSourceElementAtOffset(pos);
		if (e == this) {
			return null;
		}
		return e;
	}

	@Override
	public ICElement[] getElementsAtOffset(int pos) throws CModelException {
		ICElement[] e = getSourceElementsAtOffset(pos);
		if (e.length == 1 && e[0] == this) {
			return CElement.NO_ELEMENTS;
		}
		return e;
	}

	@Override
	public ICElement getElement(String qname) {
		if (qname == null || qname.length() == 0) {
			return null;
		}
		try {
			ICElement[] celements = getChildren();
			for (ICElement celement : celements) {
				if (qname.equals(celement.getElementName())) {
					return celement;
				}
			}
		} catch (CModelException e) {
			//
		}

		String[] names = qname.split("::"); //$NON-NLS-1$
		ICElement current = this;
		for (String name : names) {
			if (current instanceof IParent) {
				try {
					ICElement[] celements = ((IParent) current).getChildren();
					current = null;
					for (ICElement celement : celements) {
						if (name.equals(celement.getElementName())) {
							current = celement;
							break;
						}
					}
				} catch (CModelException e) {
					current = null;
				}
			} else {
				current = null;
			}
		}
		return current;
	}

	@Override
	public IInclude getInclude(String name) {
		try {
			ICElement[] celements = getChildren();
			for (ICElement celement : celements) {
				if (celement.getElementType() == ICElement.C_INCLUDE) {
					if (name.equals(celement.getElementName())) {
						return (IInclude) celement;
					}
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public IInclude[] getIncludes() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList<ICElement> aList = new ArrayList<ICElement>();
		for (ICElement celement : celements) {
			if (celement.getElementType() == ICElement.C_INCLUDE) {
				aList.add(celement);
			}
		}
		return aList.toArray(new IInclude[0]);
	}

	@Override
	public IUsing getUsing(String name) {
		try {
			ICElement[] celements = getChildren();
			for (ICElement celement : celements) {
				if (celement.getElementType() == ICElement.C_USING) {
					if (name.equals(celement.getElementName())) {
						return (IUsing) celement;
					}
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public IUsing[] getUsings() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList<ICElement> aList = new ArrayList<ICElement>();
		for (ICElement celement : celements) {
			if (celement.getElementType() == ICElement.C_USING) {
				aList.add(celement);
			}
		}
		return aList.toArray(new IUsing[0]);
	}

	@Override
	public INamespace getNamespace(String name) {
		try {
			String[] names = name.split("::"); //$NON-NLS-1$
			ICElement current = this;
			for (int j = 0; j < names.length; ++j) {
				if (current instanceof IParent) {
					ICElement[] celements = ((IParent) current).getChildren();
					current = null;
					for (ICElement celement : celements) {
						if (celement.getElementType() == ICElement.C_NAMESPACE) {
							if (name.equals(celement.getElementName())) {
								current = celement;
								break;
							}
						}
					}
				} else {
					current = null;
				}
			}
			if (current instanceof INamespace) {
				return (INamespace) current;
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public INamespace[] getNamespaces() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList<ICElement> elementList = new ArrayList<ICElement>();
		for (ICElement celement : celements) {
			if (celement.getElementType() == ICElement.C_NAMESPACE) {
				elementList.add(celement);
			}
		}
		return elementList.toArray(new INamespace[0]);
	}

	protected void setLocationURI(URI loc) {
		location = loc;
	}

	@Override
	public IPath getLocation() {
		if (location == null) {
			IFile file = getFile();
			if (file != null) {
				return file.getLocation();
			} else {
				return null;
			}
		}
		return UNCPathConverter.toPath(location);
	}

	@Override
	public URI getLocationURI() {
		if (location == null) {
			IFile file = getFile();
			if (file != null) {
				location = file.getLocationURI();
			} else {
				return null;
			}
		}
		return location;
	}

	public IFile getFile() {
		IResource res = getResource();
		if (res instanceof IFile) {
			return (IFile) res;
		}
		return null;
	}

	@Override
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
			IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().copy(container, sibling, rename, force, monitor);
	}

	@Override
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().delete(force, monitor);
	}

	@Override
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
			IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().move(container, sibling, rename, force, monitor);
	}

	@Override
	public void rename(String name, boolean force, IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().rename(name, force, monitor);
	}

	@Override
	public String getSource() throws CModelException {
		return getSourceManipulationInfo().getSource();
	}

	@Override
	public ISourceRange getSourceRange() throws CModelException {
		return getSourceManipulationInfo().getSourceRange();
	}

	protected TranslationUnitInfo getTranslationUnitInfo() throws CModelException {
		return (TranslationUnitInfo) getElementInfo();
	}

	protected SourceManipulationInfo getSourceManipulationInfo() {
		if (sourceManipulationInfo == null) {
			sourceManipulationInfo = new SourceManipulationInfo(this);
		}
		return sourceManipulationInfo;
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new TranslationUnitInfo(this);
	}

	/**
	 * Returns true if this handle represents the same Java element
	 * as the given handle.
	 *
	 * <p>Compilation units must also check working copy state;
	 *
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ITranslationUnit)) return false;
		return super.equals(o) && !((ITranslationUnit) o).isWorkingCopy();
	}

	@Override
	public IWorkingCopy findSharedWorkingCopy() {
		return CModelManager.getDefault().findSharedWorkingCopy(null, this);
	}

	@Override
	public CElementInfo getElementInfo(IProgressMonitor monitor) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		CElementInfo info = (CElementInfo)manager.getInfo(this);
		if (info != null) {
			return info;
		}
		synchronized (this) {
			info = createElementInfo();
			openWhenClosed(info, monitor);
		}
		return info;
	}

	@Override
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm,
			Map<ICElement, CElementInfo> newElements, IResource underlyingResource) throws CModelException {
		TranslationUnitInfo unitInfo = (TranslationUnitInfo) info;

		// Generate structure
		this.parse(newElements, pm);

		// /////////////////////////////////////////////////////////////

		if (isWorkingCopy()) {
			ITranslationUnit original =  ((IWorkingCopy)this).getOriginalElement();
			// Might be IResource.NULL_STAMP if original does not exist
			IResource r = original.getResource();
			if (r != null && r instanceof IFile) {
				unitInfo.fTimestamp = ((IFile) r).getModificationStamp();
			}
		}

		return unitInfo.isStructureKnown();
	}

	@Override
	public char[] getContents() {
		try {
			IBuffer buffer = this.getBuffer();
			return buffer == null ? null : buffer.getCharacters();
		} catch (CModelException e) {
			return new char[0];
		}
	}

	@Override
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IProblemRequestor requestor)
			throws CModelException {
		return CModelManager.getDefault().getSharedWorkingCopy(null, this, requestor, monitor);
	}

	@Override
	public IWorkingCopy getWorkingCopy() throws CModelException {
		return this.getWorkingCopy(null, null);
	}

	@Override
	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor) throws CModelException {
		return getWorkingCopy(monitor, null);
	}

	@Override
	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory) throws CModelException {
		WorkingCopy workingCopy;
		IFile file= getFile();
		if (file != null) {
			workingCopy= new WorkingCopy(getParent(), file, getContentTypeId(), factory);
		} else {
			workingCopy= new WorkingCopy(getParent(), getLocationURI(), getContentTypeId(), factory);
		}
		// Open the working copy now to ensure contents are that of the current state of this element
		workingCopy.open(monitor);
		return workingCopy;
	}

	/**
	 * Returns true if this element may have an associated source buffer.
	 */
	@Override
	protected boolean hasBuffer() {
		return true;
	}

	@Override
	protected void openParent(CElementInfo childInfo, Map<ICElement, CElementInfo> newElements,
			IProgressMonitor pm) throws CModelException {
		try {
			super.openParent(childInfo, newElements, pm);
		} catch (CModelException e) {
			// allow parent to not exist for working copies defined outside
			if (!isWorkingCopy()) {
				throw e;
			}
		}
	}

	@Override
	public boolean isConsistent() throws CModelException {
		return isOpen() && CModelManager.getDefault().getElementsOutOfSynchWithBuffers().get(this) == null;
	}

	@Override
	public void makeConsistent(IProgressMonitor monitor, boolean forced) throws CModelException {
		makeConsistent(forced, monitor);
	}

	protected IASTTranslationUnit makeConsistent(boolean computeAST, IProgressMonitor monitor)
			throws CModelException {
		if (!computeAST && isConsistent()) {
			return null;
		}

		// Create a new info and make it the current info
		// (this will remove the info and its children just before storing the new infos)
		CModelManager manager = CModelManager.getDefault();
		boolean hadTemporaryCache = manager.hasTemporaryCache();
		final CElementInfo info;
		if (computeAST) {
			info= new ASTHolderTUInfo(this);
		} else {
			info= createElementInfo();
		}
		try {
			Map<ICElement, CElementInfo> newElements = manager.getTemporaryCache();
			openWhenClosed(info, monitor);
			if (newElements.get(this) == null) {
				// Close any buffer that was opened for the new elements
				Iterator<ICElement> iterator = newElements.keySet().iterator();
				while (iterator.hasNext()) {
					ICElement element = iterator.next();
					if (element instanceof Openable) {
						((Openable) element).closeBuffer();
					}
				}
				throw newNotPresentException();
			}
			if (!hadTemporaryCache) {
				manager.putInfos(this, newElements);
			}
		} finally {
			if (!hadTemporaryCache) {
				manager.resetTemporaryCache();
			}
		}
		if (info instanceof ASTHolderTUInfo) {
			final IASTTranslationUnit ast= ((ASTHolderTUInfo) info).fAST;
			((ASTHolderTUInfo) info).fAST= null;
			return ast;
		}
		return null;
	}

	@Override
	protected boolean isSourceElement() {
		return true;
	}

	@Override
	public boolean isWorkingCopy() {
		return false;
	}

	@Override
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {
		// Create buffer - translation units only use default buffer factory
		BufferManager bufManager = getBufferManager();
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null)
			return null;

		// Set the buffer source
		if (buffer.getCharacters() == null) {
			IResource resource = this.getResource();
			if (resource != null && resource.getType() == IResource.FILE) {
				buffer.setContents(Util.getResourceContentsAsCharArray((IFile)resource));
			} else {
				IPath path = this.getLocation();
				java.io.File file = path.toFile();
				if (file != null && file.isFile()) {
					InputStream stream= null;
					try {
						stream = new FileInputStream(file);
						buffer.setContents(Util.getInputStreamAsCharArray(stream, (int)file.length(), null));
					} catch (IOException e) {
						buffer.setContents(new char[0]);
					} finally {
						if (stream != null) {
							try {
								stream.close();
							} catch (IOException e) {
							}
						}
					}
				} else {
					buffer.setContents(new char[0]);
				}
			}
		}

		// Add buffer to buffer cache
		bufManager.addBuffer(buffer);

		// Listen to buffer changes
		buffer.addBufferChangedListener(this);

		return buffer;
	}

	@Override
	public Map<?, ?> parse() {
		throw new UnsupportedOperationException("Deprecated method"); //$NON-NLS-1$
	}

	/**
	 * Parse the buffer contents of this element.
	 */
	private void parse(Map<ICElement, CElementInfo> newElements, IProgressMonitor monitor) {
		boolean quickParseMode = !(CCorePlugin.getDefault().useStructuralParseMode());
		IContributedModelBuilder mb = LanguageManager.getInstance().getContributedModelBuilderFor((ITranslationUnit) this);
		if (mb == null) {
			parseUsingCModelBuilder(newElements, quickParseMode, monitor);
		} else {
			parseUsingContributedModelBuilder(mb, quickParseMode, monitor);
		}
	}

	/**
	 * Parse the buffer contents of this element.
	 * @param monitor
	 */
	private void parseUsingCModelBuilder(Map<ICElement, CElementInfo> newElements, boolean quickParseMode, IProgressMonitor monitor) {
		try {
			new CModelBuilder2(this, newElements, monitor).parse(quickParseMode);
		} catch (OperationCanceledException oce) {
			if (isWorkingCopy()) {
				throw oce;
			}
		} catch (Exception e) {
			// use the debug log for this exception.
			Util.debugLog("Exception in CModelBuilder", DebugLogConstants.MODEL);  //$NON-NLS-1$
		}
	}

	private void parseUsingContributedModelBuilder(IContributedModelBuilder mb, boolean quickParseMode,
			IProgressMonitor monitor) {
		// We did reuse the shared info cache in the internal model builder.
		// This has been fixed (bug 273471).
		// Contributed model builders cannot apply the same fix.
		// So to get by we need to remove in the LRU all the info of this handle.
		// This might result in a race condition as observed in bug 273471.
		CModelManager.getDefault().removeChildrenInfo(this);
		try {
			mb.parse(quickParseMode);
		} catch (Exception e) {
			// use the debug log for this exception.
			Util.debugLog("Exception in contributed model builder", DebugLogConstants.MODEL);  //$NON-NLS-1$
		}
	}

	public IProblemRequestor getProblemRequestor() {
		return problemRequestor;
	}

	@Override
	public boolean isHeaderUnit() {
		return isHeaderContentType(contentTypeId);
	}

	@Override
	public boolean isSourceUnit() {
		return isSourceContentType(contentTypeId);
	}

	private static boolean isHeaderContentType(String contentType) {
		return CCorePlugin.CONTENT_TYPE_CHEADER.equals(contentType)
				|| CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(contentType);
	}

	private static boolean isSourceContentType(String contentType) {
		if (isHeaderContentType(contentType))
			return false;

		return CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentType)
				|| CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentType)
				|| CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(contentType)
				|| LanguageManager.getInstance().isContributedContentType(contentType);
	}

	@Override
	public boolean isCLanguage() {
		return CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentTypeId)
				|| CCorePlugin.CONTENT_TYPE_CHEADER.equals(contentTypeId);
	}

	@Override
	public boolean isCXXLanguage() {
		return CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentTypeId)
				|| CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(contentTypeId);
	}

	@Override
	public boolean isASMLanguage() {
		return CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(contentTypeId);
	}

	@Override
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		if (location != null) {
			try {
				IFileStore fileStore = EFS.getStore(location);
				IFileInfo info = fileStore.fetchInfo();
				return info.exists();
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}

		}
		return false;
	}

	@Override
	public ILanguage getLanguage() throws CoreException {
		ILanguage language = null;

		ICProject cProject = getCProject();
		IProject project= cProject.getProject();

		ICProjectDescription description = CoreModel.getDefault().getProjectDescription(project, false);
		ICConfigurationDescription configuration;

		if (description == null) {
			// TODO: Sometimes, CoreModel returns a null ICProjectDescription
			// so for now, fall back to configuration-less language determination.
			configuration = null;
		} else {
			configuration = description.getActiveConfiguration();
		}

		IFile file= getFile();
		if (file != null) {
			language = LanguageManager.getInstance().getLanguageForFile(file, configuration, contentTypeId);
		} else {
			String filename = getElementName();
			language = LanguageManager.getInstance().getLanguageForFile(new Path(filename),
					getCProject().getProject(), configuration, contentTypeId);
		}
		return language;
	}

	@Override
	public String getContentTypeId() {
		return contentTypeId;
	}

	protected void setContentTypeID(String id) {
		contentTypeId = id;
	}

	@Override
	protected void closing(Object info) throws CModelException {
		IContentType cType = CCorePlugin.getContentType(getCProject().getProject(), getElementName());
		if (cType != null) {
			setContentTypeID(cType.getId());
		}
		super.closing(info);
	}

	/**
	 * Contributed languages' model builders need to be able to indicate whether or
	 * not the parse of a translation unit was successful without having access to
	 * the <code>CElementInfo</code> object associated with the translation unit
	 *
	 * @param wasSuccessful
	 */
	@Override
	public void setIsStructureKnown(boolean wasSuccessful) {
		try {
			this.getElementInfo().setIsStructureKnown(wasSuccessful);
		} catch (CModelException e) {
		}
	}

	@Override
	public IASTTranslationUnit getAST() throws CoreException {
		return getAST(null, 0, null);
	}

	@Override
	public IASTTranslationUnit getAST(IIndex index, int style) throws CoreException {
		return getAST(index, style, null);
	}

	public IASTTranslationUnit getAST(IIndex index, int style, IProgressMonitor monitor) throws CoreException {
		boolean incompleteIndex = index != null && !index.isFullyInitialized();
		IIndexFile[] contextToHeader = getContextToHeader(index, style);
		ITranslationUnit configureWith = getConfigureWith(contextToHeader);
		if (configureWith == this)
			contextToHeader= null;

		IScannerInfo scanInfo= configureWith.getScannerInfo((style & AST_SKIP_IF_NO_BUILD_INFO) == 0);
		if (scanInfo == null) {
			return null;
		}

		FileContent fileContent= FileContent.create(this);
		if (fileContent == null) {
			return null;
		}
		ILanguage language= configureWith.getLanguage();
		fLanguageOfContext= language;
		if (language == null) {
			return null;
		}

		IncludeFileContentProvider crf= getIncludeFileContentProvider(style, index, language.getLinkageID(), contextToHeader);
		int options= 0;
		if ((style & AST_SKIP_FUNCTION_BODIES) != 0) {
			options |= ILanguage.OPTION_SKIP_FUNCTION_BODIES;
		}
		if ((style & AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0) {
			options |= ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
		}
		if ((style & AST_PARSE_INACTIVE_CODE) != 0) {
			options |= ILanguage.OPTION_PARSE_INACTIVE_CODE;
		}
		if (isSourceUnit()) {
			options |= ILanguage.OPTION_IS_SOURCE_UNIT;
		}
		final IParserLogService log;
		if (monitor instanceof ICanceler) {
			log= new ParserLogService(DebugLogConstants.PARSER, (ICanceler) monitor);
		} else {
			log= ParserUtil.getParserLogService();
		}
		ASTTranslationUnit ast = (ASTTranslationUnit) ((AbstractLanguage) language).getASTTranslationUnit(
				fileContent, scanInfo, crf, index, options, log);
		ast.setOriginatingTranslationUnit(this);
		ast.setBasedOnIncompleteIndex(incompleteIndex);
		return ast;
	}

	private IncludeFileContentProvider getIncludeFileContentProvider(int style, IIndex index, int linkageID, IIndexFile[] contextToHeader) {
		final ICProject cprj= getCProject();
		final ProjectIndexerInputAdapter pathResolver = new ProjectIndexerInputAdapter(cprj);
		IncludeFileContentProvider fileContentsProvider;
		if ((style & AST_SKIP_NONINDEXED_HEADERS) != 0) {
			fileContentsProvider= IncludeFileContentProvider.getEmptyFilesProvider();
		} else {
			fileContentsProvider= IncludeFileContentProvider.getSavedFilesProvider();
		}

		if (index != null && (style & AST_SKIP_INDEXED_HEADERS) != 0) {
			IndexBasedFileContentProvider ibcf= new IndexBasedFileContentProvider(index, pathResolver, linkageID,
					fileContentsProvider);
			ibcf.setContextToHeaderGap(contextToHeader);
			fileContentsProvider= ibcf;
		}

		if (fileContentsProvider instanceof InternalFileContentProvider) {
			final ProjectIndexerIncludeResolutionHeuristics heuristics = new ProjectIndexerIncludeResolutionHeuristics(cprj.getProject(), pathResolver);
			((InternalFileContentProvider) fileContentsProvider).setIncludeResolutionHeuristics(heuristics);
		}

		return fileContentsProvider;
	}

	private static final int[] CTX_LINKAGES= { ILinkage.CPP_LINKAGE_ID, ILinkage.C_LINKAGE_ID };

	public IIndexFile[] getContextToHeader(IIndex index, int style) {
		if (index != null && (style & AST_CONFIGURE_USING_SOURCE_CONTEXT) != 0) {
			try {
				fLanguageOfContext= null;
				final IIndexFileLocation ifl = IndexLocationFactory.getIFL(this);
				if (ifl != null) {
					IIndexFile best = null;
					IIndexFile contextOfBest = null;
					int bestScore= -1;
					// Find file variant that has the most content and preferably was parsed in
					// context of a source file.
					for (int linkageID : CTX_LINKAGES) {
						for (IIndexFile indexFile : index.getFiles(linkageID, ifl)) {
							int score= indexFile.getMacros().length * 2;
							IIndexFile context= getParsedInContext(indexFile);
							if (isSourceFile(context, getCProject().getProject()))
								score++;
							if (score > bestScore) {
								bestScore= score;
								best= indexFile;
								contextOfBest = context;
							}
						}
					}

					if (best != null && contextOfBest != best) {
						return new IIndexFile[] { contextOfBest, best };
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}

	public static IIndexFile getParsedInContext(IIndexFile indexFile) throws CoreException {
		HashSet<IIndexFile> visited= new HashSet<IIndexFile>();
		// Bug 199412, may recurse.
		while (visited.add(indexFile)) {
			IIndexInclude include= indexFile.getParsedInContext();
			if (include == null)
				break;
			indexFile = include.getIncludedBy();
		}
		return indexFile;
	}

	/**
	 * Returns <code>true</code> if the given file is a source file.
	 * @throws CoreException
	 */
	public static boolean isSourceFile(IIndexFile indexFile, IProject project) throws CoreException {
		String path = indexFile.getLocation().getURI().getPath();
		IContentType cType = CCorePlugin.getContentType(project, path);
		if (cType == null)
			return false;

		return isSourceContentType(cType.getId());
	}

	private ITranslationUnit getConfigureWith(IIndexFile[] contextToHeader) throws CoreException {
		if (contextToHeader != null) {
			ITranslationUnit configureWith = CoreModelUtil.findTranslationUnitForLocation(
					contextToHeader[0].getLocation(), getCProject());
			if (configureWith != null)
				return configureWith;
		}
		return this;
	}

	@Override
	public IASTCompletionNode getCompletionNode(IIndex index, int style, int offset) throws CoreException {
		IIndexFile[] contextToHeader = getContextToHeader(index, style);
		ITranslationUnit configureWith = getConfigureWith(contextToHeader);
		if (configureWith == this)
			contextToHeader= null;

		IScannerInfo scanInfo = configureWith.getScannerInfo((style & ITranslationUnit.AST_SKIP_IF_NO_BUILD_INFO) == 0);
		if (scanInfo == null) {
			return null;
		}

		FileContent fileContent= FileContent.create(this);

		ILanguage language= configureWith.getLanguage();
		fLanguageOfContext= language;
		if (language != null) {
			IncludeFileContentProvider crf= getIncludeFileContentProvider(style, index, language.getLinkageID(), contextToHeader);
			IASTCompletionNode result = language.getCompletionNode(fileContent, scanInfo, crf, index,
					ParserUtil.getParserLogService(), offset);
			if (result != null) {
				final IASTTranslationUnit ast = result.getTranslationUnit();
				if (ast != null) {
					ast.setIsHeaderUnit(!isSourceUnit());
					((ASTTranslationUnit) ast).setOriginatingTranslationUnit(this);
				}
			}
			return result;
		}
		return null;
	}

	@Override
	@Deprecated
	public org.eclipse.cdt.core.parser.CodeReader getCodeReader() {
		IPath location= getLocation();
		if (location == null)
			return new org.eclipse.cdt.core.parser.CodeReader(getContents());
		if (isWorkingCopy()) {
			return new org.eclipse.cdt.core.parser.CodeReader(location.toOSString(), getContents());
		}

		IResource res= getResource();
		try {
			if (res instanceof IFile)
				return InternalParserUtil.createWorkspaceFileReader(location.toOSString(), (IFile) res, null);
			else
				return InternalParserUtil.createExternalFileReader(location.toOSString(), null);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	@Override
	public IScannerInfo getScannerInfo(boolean force) {
		IResource resource = getResource();
		ICProject project = getCProject();
		IProject rproject = project.getProject();
		IResource infoResource = resource != null ? resource : rproject;

		if (!force && CoreModel.isScannerInformationEmpty(infoResource)) {
			return null;
		}

		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(rproject);
		if (provider != null) {
			IScannerInfo scanInfo = provider.getScannerInformation(infoResource);
			if (scanInfo != null)
				return scanInfo;
		}
		if (force) {
			return new ExtendedScannerInfo();
		}
		return null;
	}

	/**
	 * Return the language of the context this file was parsed in. Works only after using
	 * {@link #getAST(IIndex, int, IProgressMonitor)} with the flag
	 * {@link ITranslationUnit#AST_CONFIGURE_USING_SOURCE_CONTEXT}.
	 */
	public ILanguage getLanguageOfContext() throws CoreException {
		final ILanguage result= fLanguageOfContext;
		return result != null ? result : getLanguage();
	}

	@Override
	public IPath getPath() {
		if (getFile() != null) {
			return super.getPath();
		}
		IPath path= getLocation();
		if (path != null) {
			return path;
		}
		return super.getPath();
	}

	@Override
	public ICElement getHandleFromMemento(String token, MementoTokenizer memento) {
		switch (token.charAt(0)) {
		case CEM_SOURCEELEMENT:
			if (!memento.hasMoreTokens()) return this;
			token= memento.nextToken();
			// element name
			final String elementName;
			if (token.charAt(0) != CEM_ELEMENTTYPE) {
				elementName= token;
				if (!memento.hasMoreTokens()) return null;
				token= memento.nextToken();
			} else {
				// anonymous
				elementName= ""; //$NON-NLS-1$
			}
			// element type
			if (token.charAt(0) != CEM_ELEMENTTYPE || !memento.hasMoreTokens()) {
				return null;
			}
			String typeString= memento.nextToken();
			int elementType;
			try {
				elementType= Integer.parseInt(typeString);
			} catch (NumberFormatException nfe) {
				CCorePlugin.log(nfe);
				return null;
			}
			token= null;
			// optional: parameters
			String[] mementoParams= {};
			if (memento.hasMoreTokens()) {
				List<String> params= new ArrayList<String>();
				do {
					token= memento.nextToken();
					if (token.charAt(0) != CEM_PARAMETER) {
						break;
					}
					if (!memento.hasMoreTokens()) {
						params.add(""); //$NON-NLS-1$
						token= null;
						break;
					}
					params.add(memento.nextToken());
					token= null;
				} while (memento.hasMoreTokens());
				mementoParams= params.toArray(new String[params.size()]);
			}
			CElement element= null;
			ICElement[] children;
			try {
				children= getChildren();
			} catch (CModelException e) {
				CCorePlugin.log(e);
				return null;
			}

			switch (elementType) {
			case ICElement.C_FUNCTION:
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_METHOD:
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			case ICElement.C_TEMPLATE_METHOD:
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
				// search for matching function
				for (ICElement element2 : children) {
					if (elementType == element2.getElementType()
							&& elementName.equals(element2.getElementName())) {
						assert element2 instanceof IFunctionDeclaration;
						String[] functionParams= ((IFunctionDeclaration)element2).getParameterTypes();
						if (Arrays.equals(functionParams, mementoParams)) {
							element= (CElement) element2;
							break;
						}
					}
				}
				break;
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_TEMPLATE_STRUCT:
			case ICElement.C_TEMPLATE_UNION:
				// search for matching template type
				for (ICElement element2 : children) {
					if (elementType == element2.getElementType()
							&& elementName.equals(element2.getElementName())) {
						assert element2 instanceof ITemplate;
						String[] templateParams= ((ITemplate)element2).getTemplateParameterTypes();
						if (Arrays.equals(templateParams, mementoParams)) {
							element= (CElement) element2;
							break;
						}
					}
				}
				break;
			default:
				// search for matching element
				for (ICElement element2 : children) {
					if (elementType == element2.getElementType()
							&& elementName.equals(element2.getElementName())) {
						element= (CElement) element2;
						break;
					}
				}
				break;
			}
			if (element != null) {
				if (token != null) {
					return element.getHandleFromMemento(token, memento);
				} else {
					return element.getHandleFromMemento(memento);
				}
			}
		}
		return null;
	}

	@Override
	public void getHandleMemento(StringBuilder buff) {
		if (getResource() == null) {
			// external translation unit
			((CElement)getCProject()).getHandleMemento(buff);
			buff.append(getHandleMementoDelimiter());
			final IPath fileLocation= getLocation();
			if (fileLocation != null) {
				escapeMementoName(buff, fileLocation.toPortableString());
			}
		} else if (getParent() instanceof ICContainer) {
			// regular case: translation unit under source container
			super.getHandleMemento(buff);
		} else {
			// translation unit below a binary
			((CElement)getCProject()).getHandleMemento(buff);
			buff.append(getHandleMementoDelimiter());
			// project relative path
			final IPath projectPath= getResource().getFullPath().removeFirstSegments(1);
			if (projectPath != null) {
				escapeMementoName(buff, projectPath.toPortableString());
			}
		}
	}

	@Override
	protected char getHandleMementoDelimiter() {
		return CElement.CEM_TRANSLATIONUNIT;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	@Deprecated
	public IWorkingCopy findSharedWorkingCopy(IBufferFactory bufferFactory) {
		return CModelManager.getDefault().findSharedWorkingCopy(bufferFactory, this);
	}

	@Override
	@Deprecated
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor requestor) throws CModelException {
		return CModelManager.getDefault().getSharedWorkingCopy(factory, this, requestor, monitor);
	}

	@Override
	@Deprecated
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory) throws CModelException {
		return CModelManager.getDefault().getSharedWorkingCopy(factory, this, null, monitor);
	}
}
