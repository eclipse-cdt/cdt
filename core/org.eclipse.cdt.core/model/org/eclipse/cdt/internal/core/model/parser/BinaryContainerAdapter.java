package org.eclipse.cdt.internal.core.model.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.internal.core.model.Container;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

/**
 */
public class BinaryContainerAdapter extends Container implements IFolder {

	IBinaryArchive archive;
	long timestamp;
	ArrayList children;
	ArrayList phantomResources;
	
	public BinaryContainerAdapter (IBinaryArchive ar) {
		archive = ar;
		phantomResources = new ArrayList(0);
		children = new ArrayList(5);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#refreshLocal(int, IProgressMonitor)
	 */
	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
		IFile file = archive.getFile();
		if (file != null) {
			file.refreshLocal(depth, monitor);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getFullPath()
	 */
	public IPath getFullPath() {
		IFile file = archive.getFile();
		if (file != null) {
			return file.getFullPath();
		}
		return new Path("");
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getParent()
	 */
	public IContainer getParent() {
		IFile file = archive.getFile();
		if (file != null) {
			return file.getParent();
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getType()
	 */
	public int getType() {
		return IResource.FOLDER;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getModificationStamp()
	 */
	public long getModificationStamp() {
		IFile file = archive.getFile();
		if (file != null) {
			return file.getModificationStamp();
		}
		return 0;
	}

	/**
	 * @see org.eclipse.core.resources.IFolder#delete(boolean, boolean, IProgressMonitor)
	 */
	public void delete(boolean force, boolean keepHistory,
		IProgressMonitor monitor) throws CoreException {
		delete((keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#delete(int, IProgressMonitor)
	 */
	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
		IFile file = archive.getFile();
		if (file != null) {
			file.delete(updateFlags, monitor);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#touch(IProgressMonitor)
	 */
	public void touch(IProgressMonitor monitor) throws CoreException {
		IFile file = archive.getFile();
		if (file != null) {
			file.touch(monitor);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IFolder#create(boolean, boolean, IProgressMonitor)
	 */
	public void create(boolean force, boolean local, IProgressMonitor monitor)
		throws CoreException {
		create((force ? FORCE : IResource.NONE), local, monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IFolder#create(int, boolean, IProgressMonitor)
	 */
	public void create(int updateFlags, boolean local,
		IProgressMonitor monitor) throws CoreException {
		// FIXME: Not implemented.
	}

	/**
	 * @see org.eclipse.core.resources.IFolder#getFile(String)
	 */
	public IFile getFile(IPath path) {
		IFile f = (IFile)findMember(path);
		if (f == null) {
			// Pass it to parent to create a fake/phantom if the object
			// is not in the archive.
			f = getParent().getFile(path);
			// Add it to the list of phantoms
			if (! phantomResources.contains(f)) {
				phantomResources.add(f);
				phantomResources.trimToSize();
			}
		}
		return f;
	}

	/**
	 * @see org.eclipse.core.resources.IFolder#move(IPath, boolean, boolean, IProgressMonitor)
	 */
	public void move(IPath destination, boolean force, boolean keepHistory,
		IProgressMonitor monitor) throws CoreException {
		move(destination, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IContainer#exists(IPath)
	 */
	public boolean exists(IPath path) {
		// Check if it is the archive.
		if (getFullPath().equals(path)) {
			IFile file = archive.getFile();
			if (file != null) {
				return file.exists();
			}
			return false;
		}
		return findMember(path) != null;		
	}

	/**
	 * @see org.eclipse.core.resources.IContainer#getFolder(IPath)
	 */
	public IFolder getFolder(IPath path) {
		// Only Files in the archive pass this to the parent
		// to create a phatom resource
		IFolder f = getParent().getFolder(path);
		if (!phantomResources.contains(f)) {
			phantomResources.add(f);
			phantomResources.trimToSize();
		}
		return f;
	}

	/**
	 * @see org.eclipse.core.resources.IContainer#members(int)
	 */
	public IResource[] members(int memberFlags) throws CoreException {
		final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
		final boolean includeTeamPrivateMember =
			(memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) != 0;

		ArrayList aList = new ArrayList();

		if (hasChanged()) {		
			children.clear();
			IBinaryObject[] objects = archive.getObjects();
			for (int i = 0; i < objects.length; i++) {
				children.add(new BinaryFileAdapter(this, objects[i]));
			}
		}

		for (int i = 0; i < children.size(); i++) {
			IResource child = (IResource)children.get(i);
			if (includeTeamPrivateMember && child.isTeamPrivateMember() || !child.isTeamPrivateMember()) {
		    	aList.add(child);
		    }	    
		}

		if (includePhantoms) {
			aList.addAll(phantomResources);
		}
		return (IResource[])aList.toArray(new IResource[0]);
	}

	/**
	 * @see org.eclipse.core.resources.IContainer#findDeletedMembersWithHistory(int, IProgressMonitor)
	 */
	public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor)
		throws CoreException {
		return new IFile[0];
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getPersistentProperty(QualifiedName)
	 */
	public String getPersistentProperty(QualifiedName key) throws CoreException {
		IFile file = archive.getFile();
		if (file != null) {
			return file.getPersistentProperty(key);
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setPersistentProperty(QualifiedName, String)
	 */
	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
		IFile file = archive.getFile();
		if (file != null) {
			file.setPersistentProperty(key, value);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#exists()
	 */
	public boolean exists() {
		IFile file = archive.getFile();
		if (file != null) {
			return file.exists();
		}
		return false;
	}

	boolean hasChanged() {
		boolean changed;
		long modif = getModificationStamp();
		changed = modif != timestamp;
		timestamp = modif;
		return changed;
	}
	/**
	 * @see org.eclipse.core.resources.IFolder#createLink(org.eclipse.core.runtime.IPath, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void createLink(
		IPath localLocation,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
	}

}
