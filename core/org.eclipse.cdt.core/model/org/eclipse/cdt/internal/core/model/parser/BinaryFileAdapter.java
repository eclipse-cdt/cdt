package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

import org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.internal.core.model.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

/**
 */
public class BinaryFileAdapter extends Resource implements IFile {

	IBinaryObject object;
	IContainer parent;
	
	public BinaryFileAdapter(IContainer p, IBinaryObject o) {
		object = o;
		parent = p;
	}

	/**
	 * @see org.eclipse.core.resources.IFile#appendContents(InputStream, boolean, boolean, IProgressMonitor)
	 */
	public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		appendContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IFile#appendContents(InputStream, int, IProgressMonitor)
	 */
	public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor)
		throws CoreException {
		// FIXME: Not implemented.
	}

	/**
	 * @see org.eclipse.core.resources.IFile#create(InputStream, boolean, IProgressMonitor)
	 */
	public void create(InputStream source, boolean force,
		IProgressMonitor monitor) throws CoreException {
		create(source, (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IFile#create(InputStream, int, IProgressMonitor)
	 */
	public void create(InputStream source, int updateFlags,
		IProgressMonitor monitor) throws CoreException {
		// FIXME: Not implemented.
	}

	/**
	 * @see org.eclipse.core.resources.IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException {
		return getContents(false);
	}

	/**
	 * @see org.eclipse.core.resources.IFile#getContents(boolean)
	 */
	public InputStream getContents(boolean force) throws CoreException {
		return object.getContents();
	}

	/**
	 * @see org.eclipse.core.resources.IFile#getEncoding()
	 */
	public int getEncoding() throws CoreException {
		return ENCODING_UNKNOWN;
	}

	/**
	 * @see org.eclipse.core.resources.IFile#getHistory(IProgressMonitor)
	 */
	public IFileState[] getHistory(IProgressMonitor monitor)
		throws CoreException {
		return new IFileState[0];
	}

	/**
	 * @see org.eclipse.core.resources.IFile#move(IPath, boolean, boolean, IProgressMonitor)
	 */
	public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor)
		throws CoreException {
		move(destination, (keepHistory ? KEEP_HISTORY : IResource.NONE)
				| (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)
	 */
	public void setContents(InputStream source, boolean force, boolean keepHistory,
		IProgressMonitor monitor) throws CoreException {
		setContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE)
				| (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)
	 */
	public void setContents(IFileState source, boolean force, boolean keepHistory,
		IProgressMonitor monitor) throws CoreException {
		setContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE)
				| (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IFile#setContents(InputStream, int, IProgressMonitor)
	 */
	public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor)
		throws CoreException {
		// FIXME: Not implemented.
	}

	/**
	 * @see org.eclipse.core.resources.IFile#setContents(IFileState, int, IProgressMonitor)
	 */
	public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor)
		throws CoreException {
		// FIXME: Not implemented.
	}

	/**
	 * @see org.eclipse.core.resources.IFile#delete(boolean, boolean, IProgressMonitor)
	 */
	public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor)
		throws CoreException {
		delete((keepHistory ? KEEP_HISTORY : IResource.NONE)
				| (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#delete(int, IProgressMonitor)
	 */
	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
		// FIXME: Not implemented.
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getFullPath()
	 */
	public IPath getFullPath() {
		IFile file = object.getFile();
		if (file != null) {
			return file.getFullPath().append(object.getName());
		}
		return new Path(object.getName());
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getModificationStamp()
	 */
	public long getModificationStamp() {
		IFile file = object.getFile();
		if (file != null) {
			return file.getModificationStamp();
		}
		return 0;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getParent()
	 */
	public IContainer getParent() {
		return parent;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getType()
	 */
	public int getType() {
		return IResource.FILE;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#refreshLocal(int, IProgressMonitor)
	 */
	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
		IFile file = object.getFile();
		if (file != null) {
			file.refreshLocal(depth, monitor);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#touch(IProgressMonitor)
	 */
	public void touch(IProgressMonitor monitor) throws CoreException {
		IFile file = object.getFile();
		if (file != null) {
			file.touch(monitor);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getPersistentProperty(QualifiedName)
	 */
	public String getPersistentProperty(QualifiedName key)
		throws CoreException {
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setPersistentProperty(QualifiedName, String)
	 */
	public void setPersistentProperty(QualifiedName key, String value)
		throws CoreException {
	}

	/**
	 * @see org.eclipse.core.resources.IResource#exists()
	 */
	public boolean exists() {
		return parent.exists(new Path(getName()));
	}

}
