package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.CModelException;


/**
 */
public class TranslationUnit extends CFile implements ITranslationUnit {

	SourceManipulationInfo sourceManipulationInfo = null;

	public TranslationUnit(ICElement parent, IFile file) {
		super(parent, file);
	}

	public TranslationUnit(ICElement parent, IPath path) {
		super(parent, path);
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
	protected void parse(InputStream in) {
		getTranslationUnitInfo().parse(in);
	}

	protected CElementInfo createElementInfo () {
		return new TranslationUnitInfo(this);
	}
}
