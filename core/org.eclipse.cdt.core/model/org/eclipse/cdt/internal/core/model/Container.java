package org.eclipse.cdt.internal.core.model;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class Container extends Resource implements IContainer {

	/**
	 * @see IContainer#findMember(String)
	 */
	public IResource findMember(String name) {
		return findMember(name, false);
	}

	/**
	 * @see IContainer#findMember(String, boolean)
	 */
	public IResource findMember(String name, boolean phantom) {
		return findMember(new Path(name), phantom);
	}

	/**
	 * @see IContainer#findMember(IPath)
	 */
	public IResource findMember(IPath path) {
		return findMember(path, false);
	}

	/**
	 * @see IContainer#findMember(IPath)
	 */
	public IResource findMember(IPath path, boolean phantom) {
		path = getFullPath().append(path);
		try {
			IResource[] resources = members(phantom);
			for (int i = 0; i < resources.length; i++) {
				if (resources[i].getFullPath().equals(path)) {
					return resources[i];
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}

	/** 
	 * @see IContainer#getFile(String)
	 */
	public IFile getFile(String name) {
		return getFile(new Path(name));
	}

	/** 
	 * @see IContainer#getFolder(String)
	 */
	public IFolder getFolder(String name) {
		return getFolder(new Path(name));
	}

	/**
	 * @see IContainer#members
	 */
	public IResource[] members() throws CoreException {
		return members(IResource.NONE);
	}

	/**
	 * @see IContainer#members(boolean)
	 */
	public IResource[] members(boolean phantom) throws CoreException {
		return members(phantom ? INCLUDE_PHANTOMS : IResource.NONE);
	}

	/*
	 * @see IContainer#members(int)
	 */
	public abstract IResource[] members(int memberFlags) throws CoreException;

	/** 
	 * @see IContainer#exits(IPath)
	 */
	public abstract boolean exists(IPath path);

	/** 
	 * @see IContainer#getFile(IPath)
	 */
	public abstract IFile getFile(IPath path);

	/** 
	 * @see IContainer#getFolder(IPath)
	 */
	public abstract IFolder getFolder(IPath path);

}
