/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core.model;

import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryElement;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceManipulation;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 */
public class BinaryElement extends CElement implements IBinaryElement, ISourceManipulation, ISourceReference {

	long addr;

	public BinaryElement(ICElement parent, String name, int type, long a) {
		super(parent, name, type);
		addr = a;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#copy(org.eclipse.cdt.core.model.ICElement, org.eclipse.cdt.core.model.ICElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void copy(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor)
		throws CModelException {
		throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#delete(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#move(org.eclipse.cdt.core.model.ICElement, org.eclipse.cdt.core.model.ICElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void move(ICElement container, ICElement sibling, String rename, boolean replace, IProgressMonitor monitor)
		throws CModelException {
		throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#rename(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void rename(String name, boolean replace, IProgressMonitor monitor) throws CModelException {
		throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, this));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getSource()
	 */
	public String getSource() throws CModelException {
		ITranslationUnit tu = getTranslationUnit();
		if (tu != null) {
			try {
				IResource res = tu.getResource();
				if (res != null && res instanceof IFile) {
					StringBuffer buffer = Util.getContent((IFile)res);
					return  buffer.substring(getStartPos(),
							getStartPos() + getLength());
				}
			} catch (IOException e) {
				throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
			}
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getSourceRange()
	 */
	public ISourceRange getSourceRange() throws CModelException {
		return new SourceRange(getStartPos(),
						getLength(),
						getIdStartPos(),
						getIdLength(), 
						getStartLine(),
						getEndLine());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getTranslationUnit()
	 */
	public ITranslationUnit getTranslationUnit()  {
		ITranslationUnit tu = null;
		CModelManager mgr = CModelManager.getDefault();
		ICElement parent = getParent();
		if (parent != null) {
			IPath path = parent.getPath();
			if (path != null && path.isAbsolute()) {
				IResource res = mgr.getCModel().getWorkspace().getRoot().getFileForLocation(path);
				if (res != null && res.exists() && res.getType() == IResource.FILE) {
					ICElement e = CModelManager.getDefault().create(res, null);
					if (e instanceof ITranslationUnit) {
						tu = (ITranslationUnit)e;
					}
				}
			} else {
				// TODO-model: handle non-absolute paths when finding source files
				// ??? assert()
				path = new Path(""); //$NON-NLS-1$
			}
			// Fall back to the project sourcemapper.
			if (tu == null) {
				ICProject cproject = getCProject();
				SourceMapper mapper = mgr.getSourceMapper(cproject);
				if (mapper != null) {
					String lastSegment = path.lastSegment();
					if (lastSegment != null) {
						tu = mapper.findTranslationUnit(lastSegment);
					}
				}
			}
		}
		return tu;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#createElementInfo()
	 */
	protected CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryElement#getAddress()
	 */
	public long getAddress() throws CModelException {
		return addr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IBinaryElement#getBinary()
	 */
	public IBinary getBinary() {
		ICElement current = this;
		do {
			if (current instanceof IBinary) {
				return (IBinary) current;
			}
		} while ((current = current.getParent()) != null);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#generateInfos(java.lang.Object, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void generateInfos(Object info, Map newElements, IProgressMonitor monitor) throws CModelException {
	}

}
