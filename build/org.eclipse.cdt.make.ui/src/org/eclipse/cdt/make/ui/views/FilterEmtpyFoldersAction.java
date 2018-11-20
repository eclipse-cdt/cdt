/*******************************************************************************
 * Copyright (c) 2011, 2013 Andrew Gvozdev.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		setDisabledImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_DTOOL_TARGET_FILTER));
		setImageDescriptor(MakeUIImages.getImageDescriptor(MakeUIImages.IMG_ETOOL_TARGET_FILTER));

		fViewer.addFilter(new ViewerFilter() {
			/**
			 * Run through the children looking for the first match that we can find that contains
			 * a make target.
			 */
			private boolean hasMakeTargets(final IContainer parentContainer) {
				final boolean[] haveTargets = new boolean[1];
				haveTargets[0] = false;

				IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
					@Override
					public boolean visit(IResourceProxy proxy) {
						if (haveTargets[0]) {
							return false; // We found what we were looking for
						}

						int rcType = proxy.getType();
						if (rcType != IResource.PROJECT && rcType != IResource.FOLDER) {
							return false; // Ignore non-containers
						}

						IContainer subFolder = (IContainer) proxy.requestResource();

						if (!(parentContainer instanceof IProject) && !subFolder.equals(parentContainer)
								&& CCorePlugin.showSourceRootsAtTopOfProject()
								&& MakeContentProvider.isSourceEntry(subFolder)) {
							return false; // Skip source folders showing up second time as regular folders
						}

						try {
							IMakeTarget[] targets = MakeCorePlugin.getDefault().getTargetManager()
									.getTargets(subFolder);
							if (targets != null && targets.length > 0) {
								haveTargets[0] = true;
								return false; // Found a target
							}
						} catch (Exception e) {
							// log any problem then ignore it
							MakeUIPlugin.log(e);
						}
						return true; // Keep looking
					}
				};

				try {
					parentContainer.accept(visitor, IResource.NONE);
				} catch (Exception e) {
					// log any problem then ignore it
					MakeUIPlugin.log(e);
				}

				return haveTargets[0];
			}

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (isChecked()) {
					IContainer container = null;
					if (element instanceof IContainer) {
						container = (IContainer) element;
						if (parentElement instanceof IProject && !(container instanceof IProject)) {
							// under subfolders do not show source roots second time (when filtered)
							if (CCorePlugin.showSourceRootsAtTopOfProject()
									&& MakeContentProvider.isSourceEntry(container))
								return false;
						}
					} else if (element instanceof TargetSourceContainer) {
						container = ((TargetSourceContainer) element).getContainer();
					}

					if (container != null) {
						return hasMakeTargets(container);
					}
				}
				return true;
			}
		});
	}

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
