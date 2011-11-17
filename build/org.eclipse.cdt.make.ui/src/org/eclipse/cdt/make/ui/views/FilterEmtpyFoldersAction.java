/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - Initial implementation: extracted from MakeView
 *******************************************************************************/

package org.eclipse.cdt.make.ui.views;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.MakeContentProvider;
import org.eclipse.cdt.make.ui.TargetSourceContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter to filter out empty folders (with no make targets or other folders)
 * in Make Targets view.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @since 7.1
 */
public class FilterEmtpyFoldersAction extends Action {
	private static final String FILTER_EMPTY_FOLDERS = "FilterEmptyFolders"; //$NON-NLS-1$

	private TreeViewer fViewer;

	/**
	 * Constructor.
	 *
	 * @param viewer - viewer to filter out empty folders.
	 */
	public FilterEmtpyFoldersAction(TreeViewer viewer) {
		super(MakeUIPlugin.getResourceString("FilterEmptyFolderAction.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
		fViewer = viewer;

		setToolTipText(MakeUIPlugin.getResourceString("FilterEmptyFolderAction.tooltip")); //$NON-NLS-1$
		setChecked(getSettings().getBoolean(FILTER_EMPTY_FOLDERS));
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_FILTER); //$NON-NLS-1$
		fViewer.addFilter(new ViewerFilter() {
			//Check the make targets of the specified container, and if they don't exist, run
			//through the children looking for the first match that we can find that contains
			//a make target.
			private boolean hasMakeTargets(IContainer container) throws CoreException {
				IMakeTarget [] targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(container);
				if(targets != null && targets.length > 0) {
					return true;
				}

				final boolean [] haveTargets = new boolean[1];
				haveTargets[0] = false;

				IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
					@Override
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if(haveTargets[0]) {
							return false;	//We found what we were looking for
						}

						if(proxy.getType() != IResource.FOLDER) {
							return true;	//We only look at folders for content
						}

						IContainer folder = (IContainer) proxy.requestResource();
						IMakeTarget [] targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(folder);
						if(targets != null && targets.length > 0) {
							haveTargets[0] = true;
							return false;
						}
						return true;		//Keep looking
					}
				};
				container.accept(visitor, IResource.NONE);

				return haveTargets[0];
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (isChecked()) {
					IContainer container = null;
					if (element instanceof IContainer) {
						container = (IContainer)element;
						if (!(container instanceof IProject)) {
							// under subfolders do not show source roots second time (when filtered)
							if (CCorePlugin.showSourceRootsAtTopOfProject() && MakeContentProvider.isSourceEntry(container))
								return false;
						}
					} else if (element instanceof TargetSourceContainer) {
						container = ((TargetSourceContainer) element).getContainer();
					}

					if (container!=null) {
						try {
							return hasMakeTargets(container);
						} catch(Exception ex) {
							return false;
						}
					}
				}
				return true;
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		fViewer.refresh();
		getSettings().put(FILTER_EMPTY_FOLDERS, isChecked());
	}

	/**
	 * Returns setting for this control.
	 *
	 * @return Settings.
	 */
	private IDialogSettings getSettings() {
		final String sectionName = "org.eclipse.cdt.internal.ui.MakeView"; //$NON-NLS-1$
		IDialogSettings settings = MakeUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null) {
			settings = MakeUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
		}
		return settings;
	}
}
