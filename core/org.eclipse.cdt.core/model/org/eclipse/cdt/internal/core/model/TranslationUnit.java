package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @see ITranslationUnit
 */
public class TranslationUnit extends Openable implements ITranslationUnit {

	IPath location = null;

	/**
	 * If set, this is the problem requestor which will be used to notify problems
	 * detected during reconciling.
	 */
	protected IProblemRequestor problemRequestor;


	SourceManipulationInfo sourceManipulationInfo = null;

	public TranslationUnit(ICElement parent, IFile file) {
		super(parent, file, ICElement.C_UNIT);
	}

	public TranslationUnit(ICElement parent, IPath path) {
		super(parent, path, ICElement.C_UNIT);
	}

	public TranslationUnit(ICElement parent, IResource res, String name) {
		super(parent, res, name, ICElement.C_UNIT);
	}

	public ITranslationUnit getTranslationUnit () {
		return this;
	}

	public IInclude createInclude(String name, ICElement sibling, IProgressMonitor monitor)
		throws CModelException {
		return null;
	}

	public IUsing createUsing(String name, IProgressMonitor monitor) throws CModelException {
		return null;
	}

	public ICElement getElementAtLine(int line) throws CModelException {
		ICElement[] celements = getChildren();
		for (int i = 0; i < celements.length; i++) {
			ISourceRange range = ((ISourceReference)celements[i]).getSourceRange();
			int startLine = range.getStartLine();
			int endLine = range.getEndLine();
			if (line >= startLine && line <= endLine) {
				return celements[i];
			}
		}
		return null;
	}

	public ICElement getElementAtOffset(int pos) throws CModelException {
		ICElement e= getSourceElementAtOffset(pos);
		if (e == this) {
			return null;
		}
		return e;
	}

	public ICElement[] getElementsAtOffset(int pos) throws CModelException {
		ICElement[] e= getSourceElementsAtOffset(pos);
		if (e.length == 1 && e[0] == this) {
			return CElement.NO_ELEMENTS;
		}
		return e;		
	}

	public ICElement getElement(String name ) {
		try {
			ICElement[] celements = getChildren();
			for (int i = 0; i < celements.length; i++) {
				if (name.equals(celements[i].getElementName())) {
					return celements[i];
				}
			}
		} catch (CModelException e) {		
		}
		return null;
	}

	public IInclude getInclude(String name) {
		try {
			ICElement[] celements = getChildren();
			for (int i = 0; i < celements.length; i++) {
				if (celements[i].getElementType() == ICElement.C_INCLUDE) {
					if (name.equals(celements[i].getElementName())) {
						return (IInclude)celements[i];
					}
				}
			}
		} catch (CModelException e) {		
		}
		return null;
	}

	public IInclude[] getIncludes() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList aList = new ArrayList();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_INCLUDE) {
				aList.add(celements[i]);
			}
		}
		return (IInclude[])aList.toArray(new IInclude[0]);
	}

	public IUsing getUsing(String name) {
		try {
			ICElement[] celements = getChildren();
			for (int i = 0; i < celements.length; i++) {
				if (celements[i].getElementType() == ICElement.C_USING) {
					if (name.equals(celements[i].getElementName())) {
						return (IUsing)celements[i];
					}
				}
			}
		} catch (CModelException e) {		
		}		
		return null;
	}

	public IUsing[] getUsings() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList aList = new ArrayList();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_USING) {
				aList.add(celements[i]);
			}
		}
		return (IUsing[])aList.toArray(new IUsing[0]);
	}

	public void setLocation(IPath loc) {
		location = loc;
	}

	public IPath getLocation() {
		if (location == null) {
			IFile file = getFile();
			if (file != null) {
				location = file.getLocation();
			} else {
				return getPath();
			}
		}
		return location;
	}

	protected IFile getFile() {
		IResource res = getResource();
		if (res instanceof IFile) {
			return (IFile)res;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#copy(org.eclipse.cdt.core.model.ICElement, org.eclipse.cdt.core.model.ICElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().copy(container, sibling, rename, force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#delete(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().delete(force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#move(org.eclipse.cdt.core.model.ICElement, org.eclipse.cdt.core.model.ICElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().move(container, sibling, rename, force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#rename(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void rename(String name, boolean force, IProgressMonitor monitor)
		throws CModelException {
		getSourceManipulationInfo().rename(name, force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getSource()
	 */
	public String getSource() throws CModelException {
		return getSourceManipulationInfo().getSource();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getSourceRange()
	 */
	public ISourceRange getSourceRange() throws CModelException {
		return getSourceManipulationInfo().getSourceRange();
	}

	protected TranslationUnitInfo getTranslationUnitInfo() throws CModelException {
		return (TranslationUnitInfo)getElementInfo();
	}

	protected SourceManipulationInfo getSourceManipulationInfo() {
		if (sourceManipulationInfo == null) {
			sourceManipulationInfo = new SourceManipulationInfo(this);
		}
		return sourceManipulationInfo;
	}

	protected CElementInfo createElementInfo () {
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
	public boolean equals(Object o) {
		if (!(o instanceof ITranslationUnit)) return false;
		return super.equals(o) && !((ITranslationUnit)o).isWorkingCopy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#findSharedWorkingCopy(org.eclipse.cdt.internal.core.model.IBufferFactory)
	 */
	public IWorkingCopy findSharedWorkingCopy(IBufferFactory factory) {

		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager();

		// In order to be shared, working copies have to denote the same translation unit 
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = CModelManager.getDefault().sharedWorkingCopies;
	
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null) return null;
		return (WorkingCopy)perFactoryWorkingCopies.get(this);
	}

	/**
	 * To be removed with the new model builder in place
	 * @param newElements
	 * @param element
	 */
	private void getNewElements(Map mapping, CElement element){
		Object info = null;
		try {
			info = element.getElementInfo();
		} catch (CModelException e) {
		}
		if(info != null){
			if(element instanceof IParent){
				ICElement[] children = ((CElementInfo)info).getChildren();
				int size = children.length;
				for (int i = 0; i < size; ++i) {
					CElement child = (CElement) children[i];
					getNewElements(mapping, child);		
				}		
			}
		}
		mapping.put(element, info);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws CModelException {
		TranslationUnitInfo unitInfo = (TranslationUnitInfo) info;

		// We reuse the general info cache in the CModelBuilder, We should not do this
		// and instead create the info explicitely(see JDT).
		// So to get by we need to remove in the LRU all the info of this handle
		CModelManager.getDefault().removeChildrenInfo(this);

		// generate structure
		this.parse(newElements); 
		
		///////////////////////////////////////////////////////////////
		
		if (isWorkingCopy()) {
			ITranslationUnit original =  ((IWorkingCopy)this).getOriginalElement();
			// might be IResource.NULL_STAMP if original does not exist
			IResource r = original.getResource();
			if (r != null && r instanceof  IFile) {
				unitInfo.fTimestamp = ((IFile) r).getModificationStamp();
			}
		}
		
		return unitInfo.isStructureKnown();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getContents()
	 */
	public char[] getContents() {
		try {
			IBuffer buffer = this.getBuffer();
			return buffer == null ? null : buffer.getCharacters();
		} catch (CModelException e) {
			return new char[0];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getSharedWorkingCopy(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.cdt.internal.core.model.IBufferFactory)
	 */
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,IBufferFactory factory)
		throws CModelException {
		return getSharedWorkingCopy(monitor, factory, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getSharedWorkingCopy(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.cdt.internal.core.model.IBufferFactory, org.eclipse.cdt.core.model.IProblemRequestor)
	 */
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,IBufferFactory factory, IProblemRequestor requestor)
		throws CModelException {
	
		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager();

		CModelManager manager = CModelManager.getDefault();
	
		// In order to be shared, working copies have to denote the same translation unit 
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = manager.sharedWorkingCopies;
	
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null){
			perFactoryWorkingCopies = new HashMap();
			sharedWorkingCopies.put(factory, perFactoryWorkingCopies);
		}
		WorkingCopy workingCopy = (WorkingCopy)perFactoryWorkingCopies.get(this);
		if (workingCopy != null) {
			workingCopy.useCount++;
			return workingCopy;

		} else {
			CreateWorkingCopyOperation op = new CreateWorkingCopyOperation(this, perFactoryWorkingCopies, factory, requestor);
			runOperation(op, monitor);
			return (IWorkingCopy)op.getResultElements()[0];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy()
	 */
	public IWorkingCopy getWorkingCopy()throws CModelException{
		return this.getWorkingCopy(null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.cdt.internal.core.model.IBufferFactory)
	 */
	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory)throws CModelException{
		WorkingCopy workingCopy = new WorkingCopy(getParent(), getFile(), factory);
		// open the working copy now to ensure contents are that of the current state of this element
		workingCopy.open(monitor);
		return workingCopy;
	}

	/**
	 * Returns true if this element may have an associated source buffer.
	 */
	protected boolean hasBuffer() {
		return true;
	}

	/*
	 * @see Openable#openParent
	 */
	protected void openParent(Object childInfo, Map newElements, IProgressMonitor pm) throws CModelException {
		try {
			super.openParent(childInfo, newElements, pm);
		} catch(CModelException e){
			// allow parent to not exist for working copies defined outside
			if (!isWorkingCopy()){ 
				throw e;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IOpenable#isConsistent()
	 */
	public boolean isConsistent() throws CModelException {
		return CModelManager.getDefault().getElementsOutOfSynchWithBuffers().get(this) == null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#isSourceElement()
	 */
	protected boolean isSourceElement() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#openBuffer(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {

		// create buffer -  translation units only use default buffer factory
		BufferManager bufManager = getBufferManager();		
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null) 
			return null;
	
		// set the buffer source
		if (buffer.getCharacters() == null){
			IResource file = this.getResource();
			if (file != null && file.getType() == IResource.FILE) {
				buffer.setContents(Util.getResourceContentsAsCharArray((IFile)file));
			}
		}

		// add buffer to buffer cache
		bufManager.addBuffer(buffer);
			
		// listen to buffer changes
		buffer.addBufferChangedListener(this);
	
		return buffer;
	}

	public Map parse() {
		Map map = new HashMap();
		try {
			getNewElements(map, this);
		} catch (Exception e) {
		}
		return map;
	}

	/**
	 * Parse the buffer contents of this element.
	 */
	private void parse(Map newElements){
		try {
			CModelBuilder modelBuilder = new CModelBuilder(this, newElements);
			boolean quickParseMode = ! (CCorePlugin.getDefault().useStructuralParseMode());
			modelBuilder.parse(quickParseMode);
		} catch (Exception e) {
			// use the debug log for this exception.
			Util.debugLog( "Exception in CModelBuilder", IDebugLogConstants.MODEL);  //$NON-NLS-1$
		}							
	}
	
	public IProblemRequestor getProblemRequestor() {
		return problemRequestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isHeaderUnit()
	 */
	public boolean isHeaderUnit() {
		IProject project = getCProject().getProject();
		return CoreModel.isValidHeaderUnitName(project, getPath().lastSegment());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isSourceUnit()
	 */
	public boolean isSourceUnit() {
		IProject project = getCProject().getProject();
		return CoreModel.isValidSourceUnitName(project, getPath().lastSegment());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isCLanguage()
	 */
	public boolean isCLanguage() {
		IProject project = getCProject().getProject();
		ICFileType type = CCorePlugin.getDefault().getFileType(project, getPath().lastSegment());
		String lid = type.getLanguage().getId();
		return lid != null && lid.equals(ICFileTypeConstants.LANG_C);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isCXXLanguage()
	 */
	public boolean isCXXLanguage() {
		IProject project = getCProject().getProject();
		ICFileType type = CCorePlugin.getDefault().getFileType(project, getPath().lastSegment());
		String lid = type.getLanguage().getId();
		return lid != null && lid.equals(ICFileTypeConstants.LANG_CXX);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isASMLanguage()
	 */
	public boolean isASMLanguage() {
		IProject project = getCProject().getProject();
		ICFileType type = CCorePlugin.getDefault().getFileType(project, getPath().lastSegment());
		String lid = type.getLanguage().getId();
		return lid != null && lid.equals(ICFileTypeConstants.LANG_ASM);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}
	
}
