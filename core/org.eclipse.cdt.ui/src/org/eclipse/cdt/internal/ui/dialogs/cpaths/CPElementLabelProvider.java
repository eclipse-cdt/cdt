/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

class CPElementLabelProvider extends LabelProvider {

	private String fNewLabel, fClassLabel, fCreateLabel;
	private ImageDescriptor fIncludeIcon, fMacroIcon, fLibWSrcIcon, fLibIcon, fExtLibIcon, fExtLibWSrcIcon;
	private ImageDescriptor fFolderImage, fOutputImage, fProjectImage, fContainerImage;

	private ImageDescriptorRegistry fRegistry;

	public CPElementLabelProvider() {
		fNewLabel = CPathEntryMessages.getString("CPListLabelProvider.new"); //$NON-NLS-1$
		fClassLabel = CPathEntryMessages.getString("CPListLabelProvider.classcontainer"); //$NON-NLS-1$
		fCreateLabel = CPathEntryMessages.getString("CPListLabelProvider.willbecreated"); //$NON-NLS-1$
		fRegistry = CUIPlugin.getImageDescriptorRegistry();

		fLibIcon = CPluginImages.DESC_OBJS_ARCHIVE;
		fLibWSrcIcon = CPluginImages.DESC_OBJS_ARCHIVE_WSRC;
		fIncludeIcon = CPluginImages.DESC_OBJS_INCLUDES_FOLDER;
		fMacroIcon = CPluginImages.DESC_OBJS_MACRO;
		fFolderImage = CPluginImages.DESC_OBJS_SOURCE_ROOT;
		fOutputImage = CPluginImages.DESC_OBJS_CONTAINER;
		fContainerImage = CPluginImages.DESC_OBJS_LIBRARY;

		IWorkbench workbench = CUIPlugin.getDefault().getWorkbench();

		fProjectImage = workbench.getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
	}

	public String getText(Object element) {
		if (element instanceof CPElement) {
			return getCPListElementText((CPElement) element);
		} else if (element instanceof CPElementAttribute) {
			return getCPListElementAttributeText((CPElementAttribute) element);
		} else if (element instanceof IPathEntry) {
			return getCPListElementText(CPElement.createFromExisting((IPathEntry) element, null));
		}
		return super.getText(element);
	}

	public String getCPListElementAttributeText(CPElementAttribute attrib) {
		String notAvailable = CPathEntryMessages.getString("CPListLabelProvider.none"); //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		String key = attrib.getKey();
		if (key.equals(CPElement.SOURCEATTACHMENT)) {
			buf.append(CPathEntryMessages.getString("CPListLabelProvider.source_attachment.label")); //$NON-NLS-1$
			IPath path = (IPath) attrib.getValue();
			if (path != null && !path.isEmpty()) {
				buf.append(getPathString(path, path.getDevice() != null));
			} else {
				buf.append(notAvailable);
			}
		} else if (key.equals(CPElement.SOURCEATTACHMENTROOT)) {
			buf.append(CPathEntryMessages.getString("CPListLabelProvider.source_attachment_root.label")); //$NON-NLS-1$
			IPath path = (IPath) attrib.getValue();
			if (path != null && !path.isEmpty()) {
				buf.append(path.toString());
			} else {
				buf.append(notAvailable);
			}
		}
		if (key.equals(CPElement.EXCLUSION)) {
			buf.append(CPathEntryMessages.getString("CPListLabelProvider.exclusion_filter.label")); //$NON-NLS-1$
			IPath[] patterns = (IPath[]) attrib.getValue();
			if (patterns != null && patterns.length > 0) {
				for (int i = 0; i < patterns.length; i++) {
					if (i > 0) {
						buf.append(CPathEntryMessages.getString("CPListLabelProvider.exclusion_filter_separator")); //$NON-NLS-1$
					}
					buf.append(patterns[i].toString());
				}
			} else {
				buf.append(notAvailable);
			}
		}
		return buf.toString();
	}

	public String getCPListElementText(CPElement cpentry) {
		IPath path = cpentry.getPath();
		switch (cpentry.getEntryKind()) {
			case IPathEntry.CDT_LIBRARY:
				{
					IResource resource = cpentry.getResource();
					if (resource instanceof IContainer) {
						StringBuffer buf = new StringBuffer(path.makeRelative().toString());
						buf.append(' ');
						buf.append(fClassLabel);
						if (!resource.exists()) {
							buf.append(' ');
							if (cpentry.isMissing()) {
								buf.append(fCreateLabel);
							} else {
								buf.append(fNewLabel);
							}
						}
						return buf.toString();
					}
					// should not come here
					return path.makeRelative().toString();
				}
			case IPathEntry.CDT_PROJECT:
				return path.lastSegment();
			case IPathEntry.CDT_INCLUDE:
				{
					StringBuffer str = new StringBuffer(((IPath) cpentry.getAttribute(CPElement.INCLUDE)).toOSString());
					IPath base = (IPath) cpentry.getAttribute(CPElement.BASE_REF);
					if (!base.isEmpty()) {
						str.append(" - ("); //$NON-NLS-1$
						str.append(base);
						str.append(')');
					} else {
						path = ((IPath) cpentry.getAttribute(CPElement.BASE)).addTrailingSeparator();
						str.insert(0, path.toOSString());
					}
					return str.toString();
				}
			case IPathEntry.CDT_MACRO:
				{
					StringBuffer str = new StringBuffer((String) cpentry.getAttribute(CPElement.MACRO_NAME) + "=" //$NON-NLS-1$
							+ (String) cpentry.getAttribute(CPElement.MACRO_VALUE));
					IPath base = (IPath) cpentry.getAttribute(CPElement.BASE_REF);
					if (!base.isEmpty()) {
						str.append('(');
						str.append(base);
						str.append(')');
					}
					return str.toString();
				}
			case IPathEntry.CDT_CONTAINER:
				try {
					IPathEntryContainer container = CoreModel.getPathEntryContainer(cpentry.getPath(), cpentry.getCProject());
					if (container != null) {
						return container.getDescription();
					}
				} catch (CModelException e) {
				}
				return path.toString();
			case IPathEntry.CDT_SOURCE:
			case IPathEntry.CDT_OUTPUT:
				{
					StringBuffer buf = new StringBuffer(path.makeRelative().toString());
					IResource resource = cpentry.getResource();
					if (resource != null && !resource.exists()) {
						buf.append(' ');
						if (cpentry.isMissing()) {
							buf.append(fCreateLabel);
						} else {
							buf.append(fNewLabel);
						}
					}
					return buf.toString();
				}
			default:
		// pass
		}
		return CPathEntryMessages.getString("CPListLabelProvider.unknown_element.label"); //$NON-NLS-1$
	}

	private String getPathString(IPath path, boolean isExternal) {
		//		if (ArchiveFileFilter.isArchivePath(path)) {
		//			IPath appendedPath = path.removeLastSegments(1);
		//			String appended = isExternal ? appendedPath.toOSString() :
		// appendedPath.makeRelative().toString();
		//			return
		// CPathEntryMessages.getFormattedString("CPListLabelProvider.twopart",
		// //$NON-NLS-1$
		//					new String[] { path.lastSegment(), appended});
		//		} else {
		return isExternal ? path.toOSString() : path.makeRelative().toString();
		//		}
	}

	private ImageDescriptor getCPListElementBaseImage(CPElement cpentry) {
		switch (cpentry.getEntryKind()) {
			case IPathEntry.CDT_OUTPUT:
				if (cpentry.getPath().segmentCount() == 1) {
					return fProjectImage;
				} else {
					return fOutputImage;
				}
			case IPathEntry.CDT_SOURCE:
				if (cpentry.getPath().segmentCount() == 1) {
					return fProjectImage;
				} else {
					return fFolderImage;
				}
			case IPathEntry.CDT_LIBRARY:
				IResource res = cpentry.getResource();
				IPath path = (IPath) cpentry.getAttribute(CPElement.SOURCEATTACHMENT);
				if (res == null) {
					if (path == null || path.isEmpty()) {
						return fExtLibIcon;
					} else {
						return fExtLibWSrcIcon;
					}
				} else if (res instanceof IFile) {
					if (path == null || path.isEmpty()) {
						return fLibIcon;
					} else {
						return fLibWSrcIcon;
					}
				} else {
					return fFolderImage;
				}
			case IPathEntry.CDT_PROJECT:
				return fProjectImage;
			case IPathEntry.CDT_CONTAINER:
				return fContainerImage;
			case IPathEntry.CDT_INCLUDE:
				return fIncludeIcon;
			case IPathEntry.CDT_MACRO:
				return fMacroIcon;
			default:
				return null;
		}
	}

	private static final Point SMALL_SIZE = new Point(16, 16);

	public Image getImage(Object element) {
		if (element instanceof CPElement) {
			CPElement cpentry = (CPElement) element;
			ImageDescriptor imageDescriptor = getCPListElementBaseImage(cpentry);
			if (imageDescriptor != null) {
				if (cpentry.isMissing()) {
					imageDescriptor = new CElementImageDescriptor(imageDescriptor, CElementImageDescriptor.WARNING, SMALL_SIZE);
				}
				return fRegistry.get(imageDescriptor);
			}
		} else if (element instanceof CPElementAttribute) {
			String key = ((CPElementAttribute) element).getKey();
			if (key.equals(CPElement.SOURCEATTACHMENT)) {
				//				return
				// fRegistry.get(CPluginImages.DESC_OBJS_SOURCE_ATTACH_ATTRIB);
			} else if (key.equals(CPElement.EXCLUSION)) {
				return CPluginImages.get(CPluginImages.IMG_OBJS_EXCLUDSION_FILTER_ATTRIB);
			}
		} else if (element instanceof IPathEntry) {
			return getImage(CPElement.createFromExisting((IPathEntry) element, null));
		}
		return null;
	}
}