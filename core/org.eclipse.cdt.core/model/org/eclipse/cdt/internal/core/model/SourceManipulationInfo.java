package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;

/** 
 * Element info for ISourceReference elements. 
 */
/* package */
class SourceManipulationInfo extends CElementInfo {

	int modifiers;

	protected SourceManipulationInfo(CElement element) {
		super(element);
		setIsStructureKnown(true);
		modifiers = 0;
	}

	protected ISourceRange getSourceRange() {
		return new SourceRange(getElement().getStartPos(),
						getElement().getLength(),
						getElement().getIdStartPos(),
						getElement().getIdLength(), 
						getElement().getStartLine(),
						getElement().getEndLine());
	}

	/**
	 * @see ISourceReference
	 */
	public String getSource() throws CModelException {
		ITranslationUnit unit = getTranslationUnit();
		IBuffer buffer = unit.getBuffer();
		if (buffer == null) {
			return null;
		}
		int offset = getElement().getStartPos();
		int length = getElement().getLength();
		if (offset == -1 || length == 0 ) {
			return null;
		}
		try {
			return buffer.getText(offset, length);
		} catch(RuntimeException e) {
			return null;
		}

//		ITranslationUnit tu = getTranslationUnit();
//		if (tu != null) {
//			try {
//				IResource res = tu.getResource();
//				if (res != null && res instanceof IFile) {
//					StringBuffer buffer = Util.getContent((IFile)res);
//					return  buffer.substring(getElement().getStartPos(),
//							getElement().getStartPos() + getElement().getLength());
//				}
//			} catch (IOException e) {
//				throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
//			} catch (StringIndexOutOfBoundsException bound) {
//				// This is not good we screwed up the offset some how
//				throw new CModelException(bound, ICModelStatusConstants.INDEX_OUT_OF_BOUNDS);
//			}
//		}
//		return ""; //$NON-NLS-1$
	}

	/**
	 * @see IMember
	 */
	public ITranslationUnit getTranslationUnit() {
		ICElement celem = getElement();
		for (; celem != null; celem = celem.getParent()) {
			if (celem instanceof ITranslationUnit)
				return (ITranslationUnit)celem;
		}
		return null;
	}

	/**
	 * @see ISourceManipulation
	 */
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		if (container == null) {
			throw new IllegalArgumentException("operation.nullContainer"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {getElement()};
		ICElement[] containers= new ICElement[] {container};
		ICElement[] siblings= null;
		if (sibling != null) {
			siblings= new ICElement[] {sibling};
		}
		String[] renamings= null;
		if (rename != null) {
			renamings= new String[] {rename};
		}
		getElement().getCModel().copy(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		ICElement[] elements = new ICElement[] {getElement()};
		getElement().getCModel().delete(elements, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		if (container == null) {
			throw new IllegalArgumentException("operation.nullContainer"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {getElement()};
		ICElement[] containers= new ICElement[] {container};
		ICElement[] siblings= null;
		if (sibling != null) {
			siblings= new ICElement[] {sibling};
		}
		String[] renamings= null;
		if (rename != null) {
			renamings= new String[] {rename};
		}
		getElement().getCModel().move(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void rename(String name, boolean force, IProgressMonitor monitor) throws CModelException {
		if (name == null) {
			throw new IllegalArgumentException("element.nullName"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {getElement()};
		ICElement[] dests= new ICElement[] {getElement().getParent()};
		String[] renamings= new String[] {name};
		getElement().getCModel().rename(elements, dests, renamings, force, monitor);
	}
	
	/**
	 * return the element modifiers
	 * @return int
	 */
	public int getModifiers(){
		return modifiers;
	}
	
	/**
	 *  subclasses  should override
	 */
	public boolean hasSameContentsAs( SourceManipulationInfo otherInfo){
		return (this.element.fType == otherInfo.element.fType);
	}
	
}
