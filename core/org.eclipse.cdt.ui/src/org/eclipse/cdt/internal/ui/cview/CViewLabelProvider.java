/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.Strings;

import org.eclipse.cdt.internal.ui.newui.LanguageSettingsImages;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/*
 * CViewLabelProvider
 */
public class CViewLabelProvider extends AppearanceAwareLabelProvider {

	public CViewLabelProvider(long textFlags, int imageFlags) {
		super(textFlags, imageFlags);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IncludeReferenceProxy) {
			final IIncludeReference ref = ((IncludeReferenceProxy)element).getReference();
			final IPath uriPathLocation = ref.getPath().makeAbsolute();
			final IContainer[] containers= ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(URIUtil.toURI(uriPathLocation));
			if (containers.length > 0) {
				// bug 192707, prefer the project the reference belongs to.
				final ICProject prj= ref.getCProject();
				if (prj != null) {
					for (int i = 0; i < containers.length; i++) {
						final IContainer container = containers[i];
						final IProject project = container.getProject();
						// in case the path is empty, the container is the workspace root and project is null.
						if (project != null && project.equals(prj.getProject())) {
							return container.getFullPath().makeRelative().toString();
						}
					}
				}
				IPath p = containers[0].getFullPath();
				p = (p.isRoot()) ? uriPathLocation : p.makeRelative();
				return decorateText(p.toString(), element);
			}
		} else if (element instanceof IIncludeReference) {
			IIncludeReference ref = (IIncludeReference)element;
			Object parent = ref.getParent();
			if (parent instanceof IIncludeReference) {
				IPath p = ref.getPath();
				IPath parentLocation = ((IIncludeReference)parent).getPath();
				if (parentLocation.isPrefixOf(p)) {
					p = p.setDevice(null);
					p = p.removeFirstSegments(parentLocation.segmentCount());
				}
				return decorateText(p.toString(), element);
			}
		} else if (element instanceof ITranslationUnit) {
			ITranslationUnit unit = (ITranslationUnit)element;
			Object parent = unit.getParent();
			if (parent instanceof IIncludeReference) {
				IPath p = unit.getPath();
				IPath parentLocation = ((IIncludeReference)parent).getPath();
				if (parentLocation.isPrefixOf(p)) {
					p = p.setDevice(null);
					p = p.removeFirstSegments(parentLocation.segmentCount());
				}
				return decorateText(p.toString(), element);
			}
		}
		return super.getText(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		return Strings.markLTR(new StyledString(getText(element)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		String imageKey = null;
		if (element instanceof IncludeReferenceProxy) {
			IIncludeReference reference = ((IncludeReferenceProxy)element).getReference();
			IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(reference.getPath());
			if (container != null) {
				ICProject cproject = reference.getCProject();
				IProject project = (cproject != null) ? cproject.getProject() : null;
				boolean isProjectRelative = container.getProject().equals(project);
				imageKey = LanguageSettingsImages.getImageKey(ICSettingEntry.INCLUDE_PATH, ICSettingEntry.VALUE_WORKSPACE_PATH, isProjectRelative);
			} else {
				imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER;
			}
		} else if (element instanceof IIncludeReference) {
			imageKey = CDTSharedImages.IMG_OBJS_CFOLDER;
		}

		if (imageKey != null) {
			ImageDescriptor desc = CDTSharedImages.getImageDescriptor(imageKey);
			desc = new CElementImageDescriptor(desc, 0, CElementImageProvider.SMALL_SIZE);
			return CUIPlugin.getImageDescriptorRegistry().get(desc);
		}

		return super.getImage(element);
	}
}
