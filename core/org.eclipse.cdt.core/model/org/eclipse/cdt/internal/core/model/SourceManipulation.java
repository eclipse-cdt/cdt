package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICOpenable;
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
		getCRoot().copy(elements, containers, siblings, renamings, force, monitor);
	}

	/**
	 * @see ISourceManipulation
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		ICElement[] elements = new ICElement[] {this};
		getCRoot().delete(elements, force, monitor);
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
		getCRoot().move(elements, containers, siblings, renamings, force, monitor);
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
		getCRoot().rename(elements, dests, renamings, force, monitor);
	}

	/**
	 * @see IMember
	 */
	public ITranslationUnit getTranslationUnit() {
		return getSourceManipulationInfo().getTranslationUnit();
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
	 * ICOpenable.
	 */
	public ICOpenable getOpenableParent() {
		ICElement current = getParent();
		while (current != null){
			if (current instanceof ICOpenable){
				return (ICOpenable) current;
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
	public IResource getUnderlyingResource() throws CModelException {
		return getParent().getUnderlyingResource();
	}

	public IResource getResource() throws CModelException {
		return null;
	}
	
	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}

	protected SourceManipulationInfo getSourceManipulationInfo() {
		return (SourceManipulationInfo)getElementInfo();
	}
}
