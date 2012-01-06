/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.newui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.ACPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Helper class to provide unified images for {@link ICLanguageSettingEntry}.
 */
public class LanguageSettingsImages {
	/**
	 * @return the base key for the image.
	 */
	public static String getImageKey(int kind, int flag, boolean isProjectRelative) {
		String imageKey = null;
	
		boolean isWorkspacePath = (flag & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		boolean isBuiltin = (flag & ICSettingEntry.BUILTIN) != 0;
		boolean isFramework = (flag & ICSettingEntry.FRAMEWORKS_MAC) != 0;
	
		switch (kind) {
		case ICSettingEntry.INCLUDE_PATH:
			if (isWorkspacePath) {
				if (isProjectRelative) {
					imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER_PROJECT;
				} else {
					imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER_WORKSPACE;
				}
			} else if (isFramework) {
				imageKey = CDTSharedImages.IMG_OBJS_FRAMEWORKS_FOLDER;
			} else if (isBuiltin) {
				imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER_SYSTEM;
			} else {
				imageKey = CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER;
			}
			break;
		case ICSettingEntry.INCLUDE_FILE:
			imageKey = CDTSharedImages.IMG_OBJS_TUNIT_HEADER;
			break;
		case ICSettingEntry.MACRO:
			imageKey = CDTSharedImages.IMG_OBJS_MACRO;
			break;
		case ICSettingEntry.MACRO_FILE:
			imageKey = CDTSharedImages.IMG_OBJS_MACROS_FILE;
			break;
		case ICSettingEntry.LIBRARY_PATH:
			imageKey = CDTSharedImages.IMG_OBJS_LIBRARY_FOLDER;
			break;
		case ICSettingEntry.LIBRARY_FILE:
			imageKey = CDTSharedImages.IMG_OBJS_LIBRARY;
			break;
		}
		if (imageKey == null)
			imageKey = CDTSharedImages.IMG_OBJS_UNKNOWN_TYPE;
		return imageKey;
	}

	/**
	 * Returns image for the given entry from internally managed repository including
	 * necessary overlays.
	 *
	 * @param entry - language settings entry to get an image for.
	 * @param projectName - pass project name if available. That lets put "project" metaphor
	 *    on the image. Pass {@code null} if no project name is available.
	 * @param cfgDescription - configuration description of the entry.
	 * @return the image for the entry with appropriate overlays.
	 */
	public static Image getImage(ICLanguageSettingEntry entry, String projectName, ICConfigurationDescription cfgDescription) {
		int kind = entry.getKind();
		int flags = entry.getFlags();
		boolean isWorkspacePath = (flags & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		String path = entry.getName();
		boolean isProjectRelative = (projectName != null) && isWorkspacePath && path.startsWith(IPath.SEPARATOR + projectName + IPath.SEPARATOR);

		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey != null) {
			if ((flags & ICSettingEntry.UNDEFINED) != 0) {
				return CDTSharedImages.getImageOverlaid(imageKey, CDTSharedImages.IMG_OVR_INACTIVE, IDecoration.BOTTOM_LEFT);
			}

			if (entry instanceof ACPathEntry) {
				String overlayKey=null;
				IStatus status = getStatus(entry, cfgDescription);
				switch (status.getSeverity()) {
				case IStatus.ERROR:
					overlayKey = CDTSharedImages.IMG_OVR_ERROR;
					break;
				case IStatus.WARNING:
					overlayKey = CDTSharedImages.IMG_OVR_WARNING;
					break;
				case IStatus.INFO:
					overlayKey = CDTSharedImages.IMG_OVR_WARNING;
					break;
				}
				return CDTSharedImages.getImageOverlaid(imageKey, overlayKey, IDecoration.BOTTOM_LEFT);
			}
			return CDTSharedImages.getImage(imageKey);
		}
		return null;
	}

	/**
	 * Checking if the entry points to existing or accessible location.
	 * @param entry - resolved entry
	 */
	private static boolean isLocationOk(ACPathEntry entry) {
		boolean exists = true;
		boolean isWorkspacePath = (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		if (isWorkspacePath) {
			IPath path = new Path(entry.getValue());
			IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			exists = (rc !=null) && rc.isAccessible();
		} else {
			String pathname = entry.getName();
			java.io.File file = new java.io.File(pathname);
			exists = file.exists();
		}
		return exists;
	}

	/**
	 * Defines status object for the status message line.
	 *
	 * @param entry - the entry to check status on.
	 * @param cfgDescription - configuration description of the entry.
	 * @return a status object defining severity and message.
	 */
	public static IStatus getStatus(ICLanguageSettingEntry entry, ICConfigurationDescription cfgDescription) {
		if (entry instanceof ACPathEntry) {
			if (!entry.isResolved()) {
				ICLanguageSettingEntry[] entries = CDataUtil.resolveEntries(new ICLanguageSettingEntry[] {entry}, cfgDescription);
				if (entries != null && entries.length > 0) {
					entry = entries[0];
				}
			}

			ACPathEntry acEntry = (ACPathEntry)entry;
			String acEntryName = acEntry.getName();
			IPath path = new Path(acEntryName);
			if (!path.isAbsolute()) {
				return new Status(IStatus.INFO, CUIPlugin.PLUGIN_ID, Messages.LanguageSettingsImages_UsingRelativePathsNotRecommended);
			}
			if (!isLocationOk(acEntry)) {
				if (acEntry.isFile()) {
					return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, Messages.LanguageSettingsImages_FileDoesNotExist);
				} else {
					return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, Messages.LanguageSettingsImages_FolderDoesNotExist);
				}
			}

		}
		return Status.OK_STATUS;
	}

}
