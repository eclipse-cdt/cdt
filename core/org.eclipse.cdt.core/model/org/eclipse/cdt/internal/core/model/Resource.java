package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.QualifiedName;

/**
 */
public abstract class Resource extends PlatformObject implements IResource {

	ArrayList markers;
	Map sessionProperties;
	boolean readOnly;
	boolean derived;
	boolean teamPrivate;
	long stamp;
	boolean local;
	

	/**
	 * @see org.eclipse.core.resources.IResource#accept(IResourceVisitor, int, boolean)
	 */
	public void accept(IResourceVisitor visitor, int depth,
		boolean includePhantoms) throws CoreException {
		accept(visitor, depth, includePhantoms ? IContainer.INCLUDE_PHANTOMS : IResource.NONE);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#accept(IResourceVisitor)
	 */
	public void accept(IResourceVisitor visitor) throws CoreException {
		accept(visitor, IResource.NONE, IContainer.DEPTH_INFINITE);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#accept(IResourceVisitor, int, int)
	 */
	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException  {
		final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
		final boolean includeTeamPrivateMember =
			(memberFlags & IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS) != 0;

		// The INCLUDE_PHANTOMS flag is not specified and this resource does not exist. 
		if (!includePhantoms && !exists()) {
			throw new CoreException(new CModelStatus(ICModelStatus.ERROR));
		}

			// The INCLUDE_PHANTOMS flag is not specified and this resource is
		// a project that is not open. 
		if (!includePhantoms && !getProject().isOpen()) {
			throw new CoreException(new CModelStatus(ICModelStatus.ERROR));
		}

		if ((!isPhantom() && !isTeamPrivateMember()) ||
			(includePhantoms && isPhantom()) ||
			(includeTeamPrivateMember && isTeamPrivateMember())) {
			// If the visitor returns false, this resource's members are not visited. 
			if (!visitor.visit(this)) {
				return;
			}
		}

		// Full stop here.
		if (depth == DEPTH_ZERO) {
			return;
		}

		// Bail out here if not a container.
		if (getType() == IResource.FILE) {
			return;
		}

		// Advance.
		if (depth == DEPTH_ONE) {
			depth = DEPTH_ZERO;
		}

		IResource[] members = ((IContainer)this).members(memberFlags);
		for (int i = 0; i < members.length; i++) {
			members[i].accept(visitor, depth, memberFlags);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#copy(IPath, boolean, IProgressMonitor)
	 */
	public void copy(IPath destination, boolean force,
		IProgressMonitor monitor) throws CoreException {
		copy(destination, (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#copy(IProjectDescription, boolean, IProgressMonitor)
	 */
	public void copy(IProjectDescription description, boolean force,
		IProgressMonitor monitor) throws CoreException {
		copy(description, (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#copy(IProjectDescription, int, IProgressMonitor)
	 */
	public void copy(IProjectDescription description, int updateFlags,
		IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject destProject = root.getProject(description.getName());
		IPath destination = destProject.getFullPath();
		copy(destination, updateFlags, monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#copy(IPath, int, IProgressMonitor)
	 */
	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		int type = getType();
		if (type == IResource.FILE) {
			IFile oFile = (IFile)this;
			IFile nFile = root.getFile(destination);
			nFile.create(oFile.getContents(), updateFlags, monitor);
		} else if (type == IResource.FOLDER) {
			IFolder oFolder = (IFolder) this;
			IFolder nFolder = root.getFolder(destination);
			nFolder.create(updateFlags, true, monitor);
			IResource[] children = oFolder.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				child.copy(destination.append(child.getName()), updateFlags, monitor);
			}

		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#delete(boolean, IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		delete(force ? FORCE : IResource.NONE, monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#move(IPath, boolean, IProgressMonitor)
	 */
	public void move(IPath destination, boolean force, IProgressMonitor monitor)
		throws CoreException {
		move(destination, force ? FORCE : IResource.NONE, monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#move(IProjectDescription, boolean, boolean, IProgressMonitor)
	 */
	public void move(IProjectDescription description, boolean force, boolean keepHistory,
		IProgressMonitor monitor) throws CoreException {
		move(description, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#move(IProjectDescription, int, IProgressMonitor)
	 */
	public void move(IProjectDescription description, int updateFlags,
		IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject destProject = root.getProject(description.getName());
		IPath destination = destProject.getFullPath();
		move(destination, updateFlags, monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#move(IPath, int, IProgressMonitor)
	 */
	public void move(IPath destination, int updateFlags, IProgressMonitor monitor)
	throws CoreException {
		// FIXME: this is not atomic.
		copy(destination, updateFlags, monitor);
		delete(updateFlags, monitor);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#createMarker(String)
	 */
	public IMarker createMarker(String type) throws CoreException {
		IMarker marker = new Marker(this, type);
		if (markers == null) {
			markers = new ArrayList();
		}
		markers.add(marker);
		return marker;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#deleteMarkers(String, boolean, int)
	 */
	public void deleteMarkers(String type, boolean includeSubtypes, int depth)
		throws CoreException {
		if (markers != null) {
			markers.clear();
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#findMarker(long)
	 */
	public IMarker findMarker(long id) throws CoreException {
		if (markers != null) {
			for (int i = 0; i < markers.size(); i++) {
				IMarker marker = (IMarker)markers.get(i);
				if (marker.getId() == id) {
					return marker;  
				}
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#findMarkers(String, boolean, int)
	 */
	public IMarker[] findMarkers(String type, boolean includeSubtypes,
		int depth) throws CoreException {
		if (markers != null) {
			ArrayList aList = new ArrayList();
			for (int i = 0; i < markers.size(); i++) {
				IMarker marker = (IMarker)markers.get(i);
				if (marker.getType().equals(type)) {
					aList.add(marker);
				}
			}

			return (IMarker[]) aList.toArray(new IMarker[0]);
		}
		return new IMarker[0];
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getMarker(long)
	 */
	public IMarker getMarker(long id) {
		try {
			IMarker marker = findMarker(id);
			if (marker != null) {
				return marker;
			}
		} catch (CoreException e) {
		}
		Marker marker = new Marker(this, "");
		marker.setId(id);
		return marker;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getSessionProperty(QualifiedName)
	 */
	public Object getSessionProperty(QualifiedName key) throws CoreException {
		if (sessionProperties != null) {
			return sessionProperties.get(key);
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setSessionProperty(QualifiedName, Object)
	 */
	public void setSessionProperty(QualifiedName key, Object value)
		throws CoreException {
		if (sessionProperties == null) {
			sessionProperties = new HashMap(5);
		}
		sessionProperties.put(key, value);
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getFileExtension()
	 */
	public String getFileExtension() {
		return getFullPath().getFileExtension();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getLocation()
	 */
	public IPath getLocation() {
		return getProject().getLocation().append(getFullPath());
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getName()
	 */
	public String getName() {
		return getFullPath().lastSegment();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getProject()
	 */
	public IProject getProject() {
		return getParent().getProject();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getProjectRelativePath()
	 */
	public IPath getProjectRelativePath() {
		return getFullPath().removeFirstSegments(getProject().getFullPath().segmentCount());
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getWorkspace()
	 */
	public IWorkspace getWorkspace() {
		return getProject().getWorkspace();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#clearHistory(IProgressMonitor)
	 */
	public void clearHistory(IProgressMonitor monitor) throws CoreException {
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isAccessible()
	 */
	public boolean isAccessible() {
		int type = getType();
		if (type == IResource.FILE || type == IResource.FOLDER) {
			return exists();
		} else if (type == IResource.PROJECT) {
			IProject project = (IProject)this;
			return exists()  && project.isOpen();
		} else if (type == IResource.ROOT) {
			return true;
		}
		return false;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isDerived()
	 */
	public boolean isDerived() {
		return derived;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isLocal(int)
	 */
	public boolean isLocal(int depth) {
		return !isPhantom();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isPhantom()
	 */
	public boolean isPhantom() {
		return !exists();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isReadOnly()
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isSynchronized(int)
	 */
	public boolean isSynchronized(int depth) {
		if (getType() == IResource.ROOT) {
			if (depth == IResource.DEPTH_ZERO) {
				return true;
			}

			if (depth == IResource.DEPTH_ONE) {
				depth = IResource.DEPTH_ZERO;
			}

			IProject[] projects = ((IWorkspaceRoot)this).getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].isSynchronized(depth))
					return false;
			}
			return true;

		}
		return exists();
	}

	/**
	 * @see org.eclipse.core.resources.IResource#isTeamPrivateMember()
	 */
	public boolean isTeamPrivateMember() {
		return teamPrivate;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setDerived(boolean)
	 */
	public void setDerived(boolean isDerived) throws CoreException {
		derived = isDerived;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setLocal(boolean, int, IProgressMonitor)
	 */
	public void setLocal(boolean flag, int depth, IProgressMonitor monitor)
		throws CoreException {
		local = flag;
		if (getType() == IResource.FILE || depth == IResource.DEPTH_ZERO) {
			return;
		}
		if (depth == IResource.DEPTH_ONE)
			depth = IResource.DEPTH_ZERO;
		IResource[] children = ((IContainer) this).members();
		for (int i = 0; i < children.length; i++) {
			children[i].setLocal(flag, depth, monitor);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean read) {
		this.readOnly = read;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#setTeamPrivateMember(boolean)
	 */
	public void setTeamPrivateMember(boolean isTeamPrivate)
		throws CoreException {
		teamPrivate = isTeamPrivate;
	}

	/**
	 * @see org.eclipse.core.resources.IResource#exists()
	 */
	public abstract boolean exists();

	/**
	 * @see org.eclipse.core.resources.IResource#refreshLocal(int, IProgressMonitor)
	 */
	public abstract void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException;

	/**
	 * @see org.eclipse.core.resources.IResource#getFullPath()
	 */
	public abstract IPath getFullPath();

	/**
	 * @see org.eclipse.core.resources.IResource#getParent()
	 */
	public abstract IContainer getParent();

	/**
	 * @see org.eclipse.core.resources.IResource#getType()
	 */
	public abstract int getType();

	/**
	 * @see org.eclipse.core.resources.IResource#getModificationStamp()
	 */
	public abstract long getModificationStamp();

	/**
	 * @see org.eclipse.core.resources.IResource#delete(int, IProgressMonitor)
	 */
	public abstract void delete(int updateFlags, IProgressMonitor monitor) throws CoreException;

	/**
	 * @see org.eclipse.core.resources.IResource#touch(IProgressMonitor)
	 */
	public abstract void touch(IProgressMonitor monitor) throws CoreException;

	/**
	 * @see org.eclipse.core.resources.IResource#setPersistentProperty(QualifiedName, String)
	 */
	public abstract void setPersistentProperty(QualifiedName key, String value) throws CoreException;

	/**
	 * @see org.eclipse.core.resources.IResource#getPersistentProperty(QualifiedName)
	 */
	public abstract String getPersistentProperty(QualifiedName key) throws CoreException;


	/**
	 * @see org.eclipse.core.resources.IResource#isLinked()
	 */
	public boolean isLinked() {
		return false; // If this changes to true then need to change getRawLocation below
	}

	/**
	 * @see org.eclipse.core.resources.IResource#getRawLocation()
	 */
	public IPath getRawLocation() {
		return getLocation();
	}

}
