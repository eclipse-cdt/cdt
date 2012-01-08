/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;

/**
 * @deprecated as of CDT 4.0. This class was used for property pages
 * for 3.X style projects.
 */
@Deprecated
class CPElementLabelProvider extends LabelProvider implements IColorProvider {

	private Color inDirect = new Color(Display.getDefault(), new RGB(170, 170, 170));

	private String fNewLabel, fCreateLabel;
	private ImageDescriptor fIncludeIcon, fMacroIcon, fLibWSrcIcon, fLibIcon;
    private ImageDescriptor fQuoteIncludeIcon, fIncludeFileIcon, fMacrosFileIcon;
	private ImageDescriptor fFolderImage, fOutputImage, fProjectImage, fContainerImage;
	private boolean bShowExported;
	private boolean bShowParentInfo;
	private ImageDescriptorRegistry fRegistry;

	public CPElementLabelProvider() {
		this(true, false);
	}

	public CPElementLabelProvider(boolean showExported, boolean showParentInfo) {
		fNewLabel = CPathEntryMessages.CPElementLabelProvider_new; 
		fCreateLabel = CPathEntryMessages.CPElementLabelProvider_willbecreated; 
		fRegistry = CUIPlugin.getImageDescriptorRegistry();

		fLibIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ARCHIVE);
		fLibWSrcIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_ARCHIVE);
		fIncludeIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER);
        fQuoteIncludeIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_QUOTE_INCLUDES_FOLDER);
        fIncludeFileIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
		fMacroIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_MACRO);
        fMacrosFileIcon = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
		fFolderImage = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_SOURCE_ROOT);
		fOutputImage = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_CONTAINER);
		fContainerImage = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_LIBRARY);

		IWorkbench workbench = CUIPlugin.getDefault().getWorkbench();

		fProjectImage = workbench.getSharedImages().getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		bShowExported = showExported;
		bShowParentInfo = showParentInfo;
	}

	/*
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		inDirect.dispose();
		inDirect= null;
		super.dispose();
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof CPElement) {
			return getCPElementText((CPElement)element);
		} else if (element instanceof CPElementAttribute) {
			return getCPElementAttributeText((CPElementAttribute)element);
		} else if (element instanceof IPathEntry) {
			return getCPElementText(CPElement.createFromExisting((IPathEntry)element, null));
		} else if (element instanceof CPElementGroup) {
			return (getCPContainerGroupText((CPElementGroup)element));
		}
		return super.getText(element);
	}

	private String getCPContainerGroupText(CPElementGroup group) {
		switch (group.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE :
				return CPathEntryMessages.CPElementLabelProvider_Includes; 
            case IPathEntry.CDT_INCLUDE_FILE :
                return CPathEntryMessages.CPElementLabelProvider_IncludeFiles; 
			case IPathEntry.CDT_MACRO :
				return CPathEntryMessages.CPElementLabelProvider_PreprocessorSymbols; 
            case IPathEntry.CDT_MACRO_FILE :
                return CPathEntryMessages.CPElementLabelProvider_MacrosFiles; 
			case IPathEntry.CDT_LIBRARY :
				return CPathEntryMessages.CPElementLabelProvider_Libraries; 
			case -1 :
				if (group.getResource().getType() == IResource.PROJECT) {
					return group.getResource().getName();
				}
				StringBuffer label = new StringBuffer(group.getResource().getProjectRelativePath().toString());
				if (!group.getResource().exists()) {
					label.append(fCreateLabel);
				}
				return label.toString();
		}
		return ""; //$NON-NLS-1$
	}

	public String getCPElementAttributeText(CPElementAttribute attrib) {
		String notAvailable = CPathEntryMessages.CPElementLabelProvider_none; 
		StringBuffer buf = new StringBuffer();
		String key = attrib.getKey();
		if (key.equals(CPElement.SOURCEATTACHMENT)) {
			buf.append(CPathEntryMessages.CPElementLabelProvider_source_attachment_label); 
			IPath path = (IPath)attrib.getValue();
			if (path != null && !path.isEmpty()) {
				buf.append(getPathString(path, path.getDevice() != null));
			} else {
				buf.append(notAvailable);
			}
		} else if (key.equals(CPElement.SOURCEATTACHMENTROOT)) {
			buf.append(CPathEntryMessages.CPElementLabelProvider_source_attachment_root_label); 
			IPath path = (IPath)attrib.getValue();
			if (path != null && !path.isEmpty()) {
				buf.append(path.toString());
			} else {
				buf.append(notAvailable);
			}
		}
		if (key.equals(CPElement.EXCLUSION)) {
			buf.append(CPathEntryMessages.CPElementLabelProvider_exclusion_filter_label); 
			IPath[] patterns = (IPath[])attrib.getValue();
			if (patterns != null && patterns.length > 0) {
				for (int i = 0; i < patterns.length; i++) {
					if (i > 0) {
						buf.append(CPathEntryMessages.CPElementLabelProvider_exclusion_filter_separator); 
					}
					buf.append(patterns[i].toString());
				}
			} else {
				buf.append(notAvailable);
			}
		}
		return buf.toString();
	}

	public String getCPElementText(CPElement cpentry) {
		IPath path = cpentry.getPath();
		switch (cpentry.getEntryKind()) {
			case IPathEntry.CDT_LIBRARY : {
				IPath libPath = (IPath)cpentry.getAttribute(CPElement.LIBRARY);
				StringBuffer str = new StringBuffer();
				addBaseString(libPath, cpentry, str);
				addExport(cpentry, str);
				addParentInfo(cpentry, str);
				return str.toString();
			}
			case IPathEntry.CDT_PROJECT :
				return path.lastSegment();
			case IPathEntry.CDT_INCLUDE : {
				IPath incPath = ((IPath)cpentry.getAttribute(CPElement.INCLUDE));
				StringBuffer str = new StringBuffer();
				addBaseString(incPath, cpentry, str);
				addExport(cpentry, str);
				addParentInfo(cpentry, str);
				return str.toString();
			}
            case IPathEntry.CDT_INCLUDE_FILE : {
                IPath incFilePath = ((IPath)cpentry.getAttribute(CPElement.INCLUDE_FILE));
                StringBuffer str = new StringBuffer();
                addBaseString(incFilePath, cpentry, str);
                addExport(cpentry, str);
                addParentInfo(cpentry, str);
                return str.toString();
            }
			case IPathEntry.CDT_MACRO : {
				StringBuffer str = new StringBuffer((String)cpentry.getAttribute(CPElement.MACRO_NAME) + "=" //$NON-NLS-1$
						+ (String)cpentry.getAttribute(CPElement.MACRO_VALUE));
				addBaseString(null, cpentry, str);
				addExport(cpentry, str);
				addParentInfo(cpentry, str);
				return str.toString();
			}
            case IPathEntry.CDT_MACRO_FILE : {
                IPath macroFilePath = ((IPath)cpentry.getAttribute(CPElement.MACROS_FILE));
                StringBuffer str = new StringBuffer();
                addBaseString(macroFilePath, cpentry, str);
                addExport(cpentry, str);
                addParentInfo(cpentry, str);
                return str.toString();
            }
			case IPathEntry.CDT_CONTAINER : {
				StringBuffer str = new StringBuffer(path.toString());
				try {
					IPathEntryContainer container = CoreModel.getPathEntryContainer(cpentry.getPath(), cpentry.getCProject());
					if (container != null) {
						str.setLength(0);
						str.append(container.getDescription());
					}
				} catch (CModelException e) {
				}
				addExport(cpentry, str);
				return str.toString();
			}
			case IPathEntry.CDT_SOURCE :
			case IPathEntry.CDT_OUTPUT : {
				StringBuffer buf = new StringBuffer(path.makeRelative().toString());
				IResource resource = cpentry.getResource();
				if (resource != null && !resource.exists()) {
					buf.append(' ');
					if (cpentry.getStatus().getSeverity() != IStatus.OK) { // only valid error for src/output would missing path...
						buf.append(fCreateLabel);
					} else {
						buf.append(fNewLabel);
					}
				}
				return buf.toString();
			}
			default :
		// pass
		}
		return CPathEntryMessages.CPElementLabelProvider_unknown_element_label; 
	}
	/**
	 * @param cpentry
	 * @param str
	 */
	private void addParentInfo(CPElement cpentry, StringBuffer str) {
		if (bShowParentInfo) {
			CPElement parent = cpentry.getParentContainer();
			if (parent != null) {
				str.append(" ["); //$NON-NLS-1$
				try {
					IPathEntryContainer container = CoreModel.getPathEntryContainer(cpentry.getPath(), cpentry.getCProject());
					if (container != null) {
						str.append(container.getDescription());
					}
				} catch (CModelException e) {
					str.append(parent.getPath());
				}
				str.append(']');
			}
		}
	}

	private void addExport(CPElement cpentry, StringBuffer str) {
		if (bShowExported && cpentry.isExported()) {
			str.append(' ');
			str.append(CPathEntryMessages.CPElementLabelProvider_export_label); 
		}
	}

	private void addBaseString(IPath endPath, CPElement cpentry, StringBuffer str) {
		IPath baseRef = (IPath)cpentry.getAttribute(CPElement.BASE_REF);
		if (!baseRef.isEmpty()) {
			if (baseRef.isAbsolute()) {
				//				str.append("From project ");
				IPath path = baseRef;
				if (endPath != null) {
					path = path.append(endPath);
				}
				str.append(path.makeRelative().toOSString());
			} else {
				//				str.append("From contribution ");
				IPathEntryContainer container;
				if (endPath != null) {
					str.append(endPath.toOSString());
				}
				str.append(" - ("); //$NON-NLS-1$
				try {
					container = CoreModel.getPathEntryContainer(baseRef, cpentry.getCProject());
					if (container != null) {
						str.append(container.getDescription());
					}
				} catch (CModelException e1) {
				}
				str.append(')');
			}
		} else {
			IPath path = (IPath)cpentry.getAttribute(CPElement.BASE);
			if (!path.isEmpty()) {
				if (endPath != null) {
					path = path.append(endPath);
				}
				str.insert(0, path.toOSString());
			} else if (endPath != null) {
				str.insert(0, endPath.toOSString());
			}
		}

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

	private ImageDescriptor getCPElementBaseImage(CPElement cpentry) {
		switch (cpentry.getEntryKind()) {
			case IPathEntry.CDT_OUTPUT :
				if (cpentry.getPath().segmentCount() == 1) {
					return fProjectImage;
				}
				return fOutputImage;
			case IPathEntry.CDT_SOURCE :
				if (cpentry.getPath().segmentCount() == 1) {
					return fProjectImage;
				}
				return fFolderImage;
			case IPathEntry.CDT_LIBRARY :
				IPath path = (IPath)cpentry.getAttribute(CPElement.SOURCEATTACHMENT);
				if (path == null || path.isEmpty()) {
					return fLibIcon;
				}
				return fLibWSrcIcon;
			case IPathEntry.CDT_PROJECT :
				return fProjectImage;
			case IPathEntry.CDT_CONTAINER :
				return fContainerImage;
			case IPathEntry.CDT_INCLUDE :
                if (((Boolean)cpentry.getAttribute(CPElement.SYSTEM_INCLUDE)).booleanValue())
				    return fIncludeIcon;
                return fQuoteIncludeIcon;
            case IPathEntry.CDT_INCLUDE_FILE :
                return fIncludeFileIcon;
			case IPathEntry.CDT_MACRO :
				return fMacroIcon;
            case IPathEntry.CDT_MACRO_FILE :
                return fMacrosFileIcon;
			default :
				return null;
		}
	}

	private static final Point SMALL_SIZE = new Point(16, 16);

	@Override
	public Image getImage(Object element) {
		if (element instanceof CPElement) {
			CPElement cpentry = (CPElement)element;
			ImageDescriptor imageDescriptor = getCPElementBaseImage(cpentry);
			if (imageDescriptor != null) {
				switch (cpentry.getStatus().getSeverity()) {
					case IStatus.WARNING :
						imageDescriptor = new CPListImageDescriptor(imageDescriptor, CPListImageDescriptor.WARNING, SMALL_SIZE);
						break;
					case IStatus.ERROR :
						imageDescriptor = new CPListImageDescriptor(imageDescriptor, CPListImageDescriptor.ERROR, SMALL_SIZE);
						break;
				}
				if (cpentry.getInherited() != null) {
					imageDescriptor = new CPListImageDescriptor(imageDescriptor, CPListImageDescriptor.PATH_INHERIT, SMALL_SIZE);
				}
				return fRegistry.get(imageDescriptor);
			}
		} else if (element instanceof CPElementAttribute) {
			String key = ((CPElementAttribute)element).getKey();
			if (key.equals(CPElement.SOURCEATTACHMENT)) {
				return fRegistry.get(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_SOURCE_ATTACH_ATTRIB));
			} else if (key.equals(CPElement.EXCLUSION)) {
				return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_EXCLUSION_FILTER_ATTRIB);
			}
		} else if (element instanceof IPathEntry) {
			return getImage(CPElement.createFromExisting((IPathEntry)element, null));
		} else if (element instanceof CPElementGroup) {
			switch ( ((CPElementGroup)element).getEntryKind()) {
				case IPathEntry.CDT_INCLUDE :
					return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDES_CONTAINER);
				case IPathEntry.CDT_MACRO :
					return fRegistry.get(fMacroIcon);
                case IPathEntry.CDT_INCLUDE_FILE :
                case IPathEntry.CDT_MACRO_FILE :
                    return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDE);
				case IPathEntry.CDT_LIBRARY :
					return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_LIBRARY);
				case -1 :
					IResource res = ((CPElementGroup)element).getResource();
					IWorkbenchAdapter adapter = (IWorkbenchAdapter)res.getAdapter(IWorkbenchAdapter.class);
					ImageDescriptor imageDescriptor = adapter.getImageDescriptor(res);
					if (!res.exists()) {
						imageDescriptor = new CPListImageDescriptor(imageDescriptor, CPListImageDescriptor.WARNING, SMALL_SIZE);
					}
					return fRegistry.get(imageDescriptor);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	@Override
	public Color getForeground(Object element) {
		if (element instanceof CPElement) {
			if ( ((CPElement)element).getInherited() != null) {
				return inDirect;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	@Override
	public Color getBackground(Object element) {
		// TODO Auto-generated method stub
		return null;
	}
}
