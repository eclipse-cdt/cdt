/*
 * Created on Apr 2, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author alain
 */
public class LibraryReferenceShared extends Binary implements ILibraryReference {

	ILibraryEntry entry;

	public LibraryReferenceShared(ICElement parent, ILibraryEntry e, IBinaryObject bin) {
		super(parent, e.getPath(), bin);
		setElementType(ICElement.C_VCONTAINER);
		entry = e;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Binary#getModificationStamp()
	 */
	protected long getModificationStamp() {
		return getPath().toFile().lastModified();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#getPath()
	 */
	public IPath getPath() {
		return entry.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ILibraryReference#getLibraryEntry()
	 */
	public ILibraryEntry getLibraryEntry() {
		return entry;
	}

}
