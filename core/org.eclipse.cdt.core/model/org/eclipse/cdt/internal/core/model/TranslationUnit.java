package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.internal.parser.CStructurizer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @see ITranslationUnit
 */
public class TranslationUnit extends Openable implements ITranslationUnit {

	IPath location = null;

	SourceManipulationInfo sourceManipulationInfo = null;

	public TranslationUnit(ICElement parent, IFile file) {
		super(parent, file, ICElement.C_UNIT);
	}

	public TranslationUnit(ICElement parent, IPath path) {
		super(parent, path, ICElement.C_UNIT);
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

	public ICElement getElement(String name ) {
		ICElement[] celements = getChildren();
		for (int i = 0; i < celements.length; i++) {
			if (name.equals(celements[i].getElementName())) {
				return celements[i];
			}
		}
		return null;
	}

	public IInclude getInclude(String name) {
		ICElement[] celements = getChildren();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_INCLUDE) {
				if (name.equals(celements[i].getElementName())) {
					return (IInclude)celements[i];
				}
			}
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
		ICElement[] celements = getChildren();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_USING) {
				if (name.equals(celements[i].getElementName())) {
					return (IUsing)celements[i];
				}
			}
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

	/**
	 * @see ISourceManipulation
	 */
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().copy(container, sibling, rename, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().delete(force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().move(container, sibling, rename, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void rename(String name, boolean force, IProgressMonitor monitor)
		throws CModelException {
		getSourceManipulationInfo().rename(name, force, monitor);
	}

	/**
	 * @see ISourceReference
	 */
	public String getSource() throws CModelException {
		return getSourceManipulationInfo().getSource();
	}

	/**
	 * @see ISourceReference
	 */
	public ISourceRange getSourceRange() throws CModelException {
		return getSourceManipulationInfo().getSourceRange();
	}

	protected TranslationUnitInfo getTranslationUnitInfo() {
		return (TranslationUnitInfo)getElementInfo();
	}

	protected SourceManipulationInfo getSourceManipulationInfo() {
		if (sourceManipulationInfo == null) {
			sourceManipulationInfo = new SourceManipulationInfo(this);
		}
		return sourceManipulationInfo;
	}
	protected Map parse(InputStream in) {
		try {
			removeChildren();
			if (CCorePlugin.getDefault().useNewParser()) {
				// new parser
				CModelBuilder modelBuilder = new CModelBuilder(this);
				return (modelBuilder.parse());

			} else {
				// cdt 1.0 parser
				ModelBuilder modelBuilder= new ModelBuilder(this);
				CStructurizer.getCStructurizer().parse(modelBuilder, in);
				return null;
			}
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	protected CElementInfo createElementInfo () {
		return new TranslationUnitInfo(this);
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.CFile#buildStructure(CFileInfo, IProgressMonitor)
	 */
	protected void buildStructure(OpenableInfo info, IProgressMonitor monitor) throws CModelException {
		if (monitor != null && monitor.isCanceled()) return;

		// remove existing (old) infos
		removeInfo();

		HashMap newElements = new HashMap(11);
		info.setIsStructureKnown(generateInfos(info, monitor, newElements, getResource()));
		CModelManager.getDefault().getElementsOutOfSynchWithBuffers().remove(this);
		for (Iterator iter = newElements.keySet().iterator(); iter.hasNext();) {
			ICElement key = (ICElement) iter.next();
			Object value = newElements.get(key);
			CModelManager.getDefault().putInfo(key, value);
		}
		// problem detection 
		if (monitor != null && monitor.isCanceled()) return;

		//IProblemRequestor problemRequestor = this.getProblemRequestor();
		//if (problemRequestor != null && problemRequestor.isActive()){
		//	problemRequestor.beginReporting();
		//	CompilationUnitProblemFinder.process(this, problemRequestor, monitor);
		//	problemRequestor.endReporting();
		//}
	
		// add the info for this at the end, to ensure that a getInfo cannot reply null in case the LRU cache needs
		// to be flushed. Might lead to performance issues.
		CModelManager.getDefault().putInfo(this, info);	
		
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
		return super.equals(o) && !((ITranslationUnit)o).isWorkingCopy();
	}

	/**
	 * @see IWorkingCopy#findSharedWorkingCopy(IBufferFactory)
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
	private void getNewElements(Map newElements, CElement element){
		Object info = element.getElementInfo();
		if(info != null){
			if(element instanceof IParent){
				ICElement[] children = ((CElementInfo)info).getChildren();
				int size = children.length;
				for (int i = 0; i < size; ++i) {
					CElement child = (CElement) children[i];
					getNewElements(newElements, child);		
				}		
			}
		}
		newElements.put(element, info);		
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.model.Openable#generateInfos(OpenableInfo, IProgressMonitor, Map, IResource)
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws CModelException {
		// put the info now, because getting the contents requires it
		CModelManager.getDefault().putInfo(this, info);
		TranslationUnitInfo unitInfo = (TranslationUnitInfo) info;
		
		// generate structure
		Map mapping = this.parse(false); // false since this is for working copies
		
		// this is temporary until the New Model Builder is implemented
		if(mapping == null) {
			getNewElements(newElements, this);
		} else {
			newElements.putAll(mapping);
		}
		///////////////////////////////////////////////////////////////
		
		if (isWorkingCopy()) {
			ITranslationUnit original = (ITranslationUnit) ((IWorkingCopy)this).getOriginalElement();
			// might be IResource.NULL_STAMP if original does not exist
			unitInfo.fTimestamp = ((IFile) original.getResource()).getModificationStamp();
		}
		
		return unitInfo.isStructureKnown();
	}

	/**
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

	/**
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getSharedWorkingCopy(IProgressMonitor, IBufferFactory)
	 */
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,IBufferFactory factory)
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
			workingCopy = (WorkingCopy)this.getWorkingCopy(monitor, factory);
			perFactoryWorkingCopies.put(this, workingCopy);

			// report added java delta
//			CElementDelta delta = new CElementDelta(this.getCModel());
//			delta.added(workingCopy);
//			manager.fire(delta, CModelManager.DEFAULT_CHANGE_EVENT);

			return workingCopy;
		}
	}
	/**
	 * 
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy()
	 */
	public IWorkingCopy getWorkingCopy()throws CModelException{
		return this.getWorkingCopy(null, null);
	}

	/**
	 * 
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy()
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

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#isConsistent()
	 */
	public boolean isConsistent() throws CModelException {
		return CModelManager.getDefault().getElementsOutOfSynchWithBuffers().get(this) == null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.Openable#isSourceElement()
	 */
	protected boolean isSourceElement() {
		return true;
	}
	/**
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IOpenable#makeConsistent(IProgressMonitor)
	 */
	public void makeConsistent(IProgressMonitor pm) throws CModelException {
		if (!isConsistent()) {
			// create a new info and make it the current info
			OpenableInfo info = (OpenableInfo) createElementInfo();
			buildStructure(info, pm);
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.Openable#openBuffer(IProgressMonitor)
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

	/**
	 * Parse the buffer contents of this element.
	 */
	public Map parse(boolean requireLineNumbers){
		try{
			String buf =this.getBuffer().getContents();
			if (buf != null) {
				StringBufferInputStream in = new StringBufferInputStream (buf);
				return (parse (in));
			}
			return null;

		} catch (CModelException e){
			// error getting the buffer
			return null;
		}
	}
	

	
}
