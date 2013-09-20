/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredFileStore implements IDeferredWorkbenchAdapter {
	private final IFileStore fFileStore;
	private IFileInfo fFileInfo;
	private IFileInfo fTargetInfo;
	private ImageDescriptor fImage;
	private final boolean fExcludeHidden;

	/**
	 * @since 7.0
	 */
	public DeferredFileStore(IFileStore store, boolean exclude) {
		this(store, null, exclude);
	}

	/**
	 * @since 7.0
	 */
	public DeferredFileStore(IFileStore store, IFileInfo info, boolean exclude) {
		fFileStore = store;
		fFileInfo = info;
		fExcludeHidden = exclude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object,
	 * org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		ArrayList<DeferredFileStore> children = new ArrayList<DeferredFileStore>();
		try {
			IFileInfo[] childInfos = fFileStore.childInfos(EFS.NONE, monitor);
			for (IFileInfo info : childInfos) {
				if (!(fExcludeHidden && info.getName().startsWith("."))) { //$NON-NLS-1$
					children.add(new DeferredFileStore(fFileStore.getChild(info.getName()), info, fExcludeHidden));
				}
			}
		} catch (CoreException e) {
			// Ignore
		}
		if (children != null) {
			collector.add(children.toArray(), monitor);
		}
	}

	/**
	 * Fetch the file info for the store. If the store is a symbolic link, fetch the file info for the target as well.
	 */
	private void fetchInfo() {
		if (fFileInfo == null) {
			fFileInfo = fFileStore.fetchInfo();
		}
		if (fTargetInfo == null && fFileInfo.getAttribute(EFS.ATTRIBUTE_SYMLINK)) {
			String target = fFileInfo.getStringAttribute(EFS.ATTRIBUTE_LINK_TARGET);
			if (target != null) {
				try {
					URI targetUri = new URI(null, null, target, null); // Make sure target is escaped correctly
					URI uri = fFileStore.toURI().resolve(targetUri.getRawPath());
					IFileStore store = fFileStore.getFileSystem().getStore(uri);
					fTargetInfo = store.fetchInfo();
				} catch (URISyntaxException e) {
					RemoteUIPlugin.log(e);
				}
			}
		}
	}

	/**
	 * Return the IWorkbenchAdapter for element or the element if it is
	 * an instance of IWorkbenchAdapter. If it does not exist return
	 * null.
	 * 
	 * @param element
	 * @return IWorkbenchAdapter or <code>null</code>
	 */
	protected IWorkbenchAdapter getAdapter(Object element) {
		return (IWorkbenchAdapter) getAdapter(element, IWorkbenchAdapter.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			IFileStore[] stores = fFileStore.childStores(EFS.NONE, null);
			List<DeferredFileStore> def = new ArrayList<DeferredFileStore>();
			for (int i = 0; i < stores.length; i++) {
				if (!(fExcludeHidden && stores[i].getName().startsWith("."))) { //$NON-NLS-1$
					def.add(new DeferredFileStore(stores[i], fExcludeHidden));
				}
			}
			return def.toArray();
		} catch (CoreException e) {
			return new Object[0];
		}
	}

	/**
	 * Get the filestore backing this object
	 * 
	 * @return
	 */
	public IFileStore getFileStore() {
		return fFileStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		fetchInfo();
		if (fImage == null) {
			boolean isDir = fFileInfo.isDirectory() || (fTargetInfo != null && fTargetInfo.isDirectory());
			FileSystemElement element = new FileSystemElement(fFileStore.getName(), null, isDir);
			IWorkbenchAdapter adapter = getAdapter(element);
			if (adapter != null) {
				ImageDescriptor imageDesc = adapter.getImageDescriptor(object);
				if (fTargetInfo != null) {
					imageDesc = new OverlayImageDescriptor(imageDesc, RemoteUIImages.DESC_OVR_SYMLINK,
							OverlayImageDescriptor.BOTTOM_RIGHT);
				}
				fImage = imageDesc;
			}
		}
		return fImage;
	}

	/**
	 * If it is possible to adapt the given object to the given type, this returns the adapter. Performs the following checks:
	 * 
	 * <ol>
	 * <li>Returns <code>sourceObject</code> if it is an instance of the adapter type.</li>
	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
	 * <li>If sourceObject is not an instance of PlatformObject (which would have already done so), the adapter manager is queried
	 * for adapters</li>
	 * </ol>
	 * 
	 * Otherwise returns null.
	 * 
	 * @param sourceObject
	 *            object to adapt, or null
	 * @param adapterType
	 *            type to adapt to
	 * @return a representation of sourceObject that is assignable to the adapter type, or null if no such representation exists
	 */
	@SuppressWarnings("rawtypes")
	protected Object getAdapter(Object sourceObject, Class adapterType) {
		Assert.isNotNull(adapterType);
		if (sourceObject == null) {
			return null;
		}
		if (adapterType.isInstance(sourceObject)) {
			return sourceObject;
		}

		if (sourceObject instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) sourceObject;

			Object result = adaptable.getAdapter(adapterType);
			if (result != null) {
				// Sanity-check
				Assert.isTrue(adapterType.isInstance(result));
				return result;
			}
		}

		if (!(sourceObject instanceof PlatformObject)) {
			Object result = Platform.getAdapterManager().getAdapter(sourceObject, adapterType);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return fFileStore.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return fFileStore.getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(Object object) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		fetchInfo();
		return fFileInfo.isDirectory() || (fTargetInfo != null && fTargetInfo.isDirectory());
	}
}
