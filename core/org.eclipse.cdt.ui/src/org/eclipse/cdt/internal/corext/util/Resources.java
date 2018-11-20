/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.corext.CorextMessages;
import org.eclipse.cdt.internal.ui.CUIStatus;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

public class Resources {

	private Resources() {
	}

	/**
	 * Checks if the given resource is in sync with the underlying file system.
	 *
	 * @param resource the resource to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 * isOK()</code> returns <code>true</code> then the resource is in sync
	 */
	public static IStatus checkInSync(IResource resource) {
		return checkInSync(new IResource[] { resource });
	}

	/**
	 * Checks if the given resources are in sync with the underlying file
	 * system.
	 *
	 * @param resources the resources to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 *  isOK() </code> returns <code>true</code> then the resources are in sync
	 */
	public static IStatus checkInSync(IResource[] resources) {
		IStatus result = null;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!resource.isSynchronized(IResource.DEPTH_INFINITE)) {
				result = addOutOfSync(result, resource);
			}
		}
		if (result != null)
			return result;
		return new Status(IStatus.OK, CUIPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
	}

	/**
	 * Makes the given resource committable. Committable means that it is
	 * writeable and that its content hasn't changed by calling
	 * <code>validateEdit</code> for the given resource on <tt>IWorkspace</tt>.
	 *
	 * @param resource the resource to be checked
	 * @param context the context passed to <code>validateEdit</code>
	 * @return status describing the method's result. If <code>status.isOK()</code> returns <code>true</code> then the resources are committable.
	 *
	 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 */
	public static IStatus makeCommittable(IResource resource, Object context) {
		return makeCommittable(new IResource[] { resource }, context);
	}

	/**
	 * Makes the given resources committable. Committable means that all
	 * resources are writeable and that the content of the resources hasn't
	 * changed by calling <code>validateEdit</code> for a given file on
	 * <tt>IWorkspace</tt>.
	 *
	 * @param resources the resources to be checked
	 * @param context the context passed to <code>validateEdit</code>
	 * @return IStatus status describing the method's result. If <code>status.
	 * isOK()</code> returns <code>true</code> then the add resources are
	 * committable
	 *
	 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 */
	public static IStatus makeCommittable(IResource[] resources, Object context) {
		List<IResource> readOnlyFiles = new ArrayList<>();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE) {
				ResourceAttributes attributes = resource.getResourceAttributes();
				if (attributes != null && attributes.isReadOnly()) {
					readOnlyFiles.add(resource);
				}
			}
		}
		if (readOnlyFiles.size() == 0)
			return new Status(IStatus.OK, CUIPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$

		Map<IFile, Long> oldTimeStamps = createModificationStampMap(readOnlyFiles);
		IStatus status = ResourcesPlugin.getWorkspace()
				.validateEdit(readOnlyFiles.toArray(new IFile[readOnlyFiles.size()]), context);
		if (!status.isOK())
			return status;

		IStatus modified = null;
		Map<IFile, Long> newTimeStamps = createModificationStampMap(readOnlyFiles);
		for (Iterator<IFile> iter = oldTimeStamps.keySet().iterator(); iter.hasNext();) {
			IFile file = iter.next();
			if (!oldTimeStamps.get(file).equals(newTimeStamps.get(file)))
				modified = addModified(modified, file);
		}
		if (modified != null)
			return modified;
		return new Status(IStatus.OK, CUIPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
	}

	private static Map<IFile, Long> createModificationStampMap(List<IResource> files) {
		Map<IFile, Long> map = new HashMap<>();
		for (Iterator<IResource> iter = files.iterator(); iter.hasNext();) {
			IFile file = (IFile) iter.next();
			map.put(file, Long.valueOf(file.getModificationStamp()));
		}
		return map;
	}

	private static IStatus addModified(IStatus status, IFile file) {
		IStatus entry = CUIStatus.createError(ICStatusConstants.VALIDATE_EDIT_CHANGED_CONTENT,
				NLS.bind(CorextMessages.Resources_fileModified, file.getFullPath().toString()), null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus) status).add(entry);
			return status;
		} else {
			MultiStatus result = new MultiStatus(CUIPlugin.getPluginId(),
					ICStatusConstants.VALIDATE_EDIT_CHANGED_CONTENT, CorextMessages.Resources_modifiedResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}

	private static IStatus addOutOfSync(IStatus status, IResource resource) {
		IStatus entry = new Status(IStatus.ERROR, ResourcesPlugin.getPlugin().getBundle().getSymbolicName(),
				IResourceStatus.OUT_OF_SYNC_LOCAL,
				NLS.bind(CorextMessages.Resources_outOfSync, resource.getFullPath().toString()), null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus) status).add(entry);
			return status;
		} else {
			MultiStatus result = new MultiStatus(ResourcesPlugin.getPlugin().getBundle().getSymbolicName(),
					IResourceStatus.OUT_OF_SYNC_LOCAL, CorextMessages.Resources_outOfSyncResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}

	public static String[] getLocationOSStrings(IResource[] resources) {
		List<String> result = new ArrayList<>(resources.length);
		for (int i = 0; i < resources.length; i++) {
			IPath location = resources[i].getLocation();
			if (location != null)
				result.add(location.toOSString());
		}
		return result.toArray(new String[result.size()]);
	}
}
