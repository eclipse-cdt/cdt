package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ISourceManipulation;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstract class for C elements which implement ISourceReference.
 */

public class SourceManipulation extends Parent implements ISourceManipulation, ISourceReference {

	public SourceManipulation(ICElement parent, String name, int type) {
		super(parent, name, type);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		if (container == null) {
			throw new IllegalArgumentException("operation.nullContainer"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {this};
		ICElement[] containers= new ICElement[] {container};
		ICElement[] siblings= null;
		if (sibling != null) {
			siblings= new ICElement[] {sibling};
		}
		String[] renamings= null;
		if (rename != null) {
			renamings= new String[] {rename};
		}
		getCModel().copy(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		ICElement[] elements = new ICElement[] {this};
		getCModel().delete(elements, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		if (container == null) {
			throw new IllegalArgumentException("operation.nullContainer"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {this};
		ICElement[] containers= new ICElement[] {container};
		ICElement[] siblings= null;
		if (sibling != null) {
			siblings= new ICElement[] {sibling};
		}
		String[] renamings= null;
		if (rename != null) {
			renamings= new String[] {rename};
		}
		getCModel().move(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void rename(String name, boolean force, IProgressMonitor monitor) throws CModelException {
		if (name == null) {
			throw new IllegalArgumentException("element.nullName"); //$NON-NLS-1$
		}
		ICElement[] elements= new ICElement[] {this};
		ICElement[] dests= new ICElement[] {this.getParent()};
		String[] renamings= new String[] {name};
		getCModel().rename(elements, dests, renamings, force, monitor);
	}

	/**
	 * @see IMember
	 */
	public ITranslationUnit getTranslationUnit() {
		try {
			return getSourceManipulationInfo().getTranslationUnit();
		} catch (CModelException e) {
			return null;
		}
	}

	/**
	 * Elements within compilation units and class files have no
	 * corresponding resource.
	 *
	 * @see ICElement
	 */
	public IResource getCorrespondingResource() throws CModelException {
		return null;
	}

	/**
	 * Returns the first parent of the element that is an instance of
	 * IOpenable.
	 */
	public IOpenable getOpenableParent() {
		ICElement current = getParent();
		while (current != null){
			if (current instanceof IOpenable){
				return (IOpenable) current;
			}
			current = current.getParent();
		}
		return null;
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

	/**
	 * @see ICElement
	 */
	public IResource getUnderlyingResource() {
		return getParent().getUnderlyingResource();
	}

	public IResource getResource() {
		return null;
	}
	
	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}

	protected SourceManipulationInfo getSourceManipulationInfo() throws CModelException {
		return (SourceManipulationInfo)getElementInfo();
	}
	
	public boolean isIdentical(SourceManipulation other) throws CModelException{
		return (this.equals(other) 
		&& (this.getSourceManipulationInfo().hasSameContentsAs(other.getSourceManipulationInfo())));
	}

	/*
	 * @see JavaElement#generateInfos
	 */
	protected void generateInfos(Object info, Map newElements, IProgressMonitor pm) throws CModelException {
		Openable openableParent = (Openable)getOpenableParent();
		if (openableParent == null) {
			return;
		}
		
		CElementInfo openableParentInfo = (CElementInfo) CModelManager.getDefault().getInfo(openableParent);
		if (openableParentInfo == null) {
			openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
		}
		newElements.put(this, info);
	}

}
