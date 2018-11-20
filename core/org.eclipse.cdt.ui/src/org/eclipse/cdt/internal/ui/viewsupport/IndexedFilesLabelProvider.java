/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A label provider that marks all translation units that are currently part of the index.
 */
public class IndexedFilesLabelProvider implements ILightweightLabelDecorator {
	private static final ImageDescriptor INDEXED = AbstractUIPlugin.imageDescriptorFromPlugin(CUIPlugin.PLUGIN_ID,
			"$nl$/icons/ovr16/indexedFile.gif"); //$NON-NLS-1$

	public IndexedFilesLabelProvider() {
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * Adds the linked resource overlay if the given element is a linked
	 * resource.
	 *
	 * @param element element to decorate
	 * @param decoration  The decoration we are adding to
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(Object, IDecoration)
	 */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		IIndexFileLocation ifl = null;
		IProject project = null;
		if (element instanceof IFile) {
			final IFile file = (IFile) element;
			project = file.getProject();

			try {
				if (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID)) {
					ifl = IndexLocationFactory.getWorkspaceIFL(file);
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}

		} else if (element instanceof ITranslationUnit) {
			final ITranslationUnit tu = (ITranslationUnit) element;
			ifl = IndexLocationFactory.getIFL(tu);
			project = tu.getCProject().getProject();
		}
		if (ifl != null && isIndexed(project, ifl)) {
			decoration.addOverlay(INDEXED, IDecoration.TOP_LEFT);
		}
	}

	private boolean isIndexed(IProject project, IIndexFileLocation ifl) {
		if (project == null || ifl == null) {
			return false;
		}

		return IndexedFilesCache.getInstance().isIndexed(project, ifl);
	}
}
