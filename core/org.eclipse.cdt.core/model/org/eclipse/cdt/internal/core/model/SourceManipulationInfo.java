package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.CModelException;

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
		ITranslationUnit tu = getTranslationUnit();
		if (tu != null) {
			try {
				IFile file = ((CFile)tu).getFile();
				StringBuffer buffer = Util.getContent(file);
				return  buffer.substring(getElement().getStartPos(),
						getElement().getStartPos() + getElement().getLength());
			} catch (IOException e) {
				throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
			}
		}
		return "";
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
		return true;
	}
	
}
