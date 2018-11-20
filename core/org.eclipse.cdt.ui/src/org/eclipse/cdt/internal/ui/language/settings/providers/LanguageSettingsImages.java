/*******************************************************************************
 * Copyright (c) 2010, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.settings.model.ACPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

/**
 * Helper class to provide unified images for {@link ICLanguageSettingEntry}.
 */
public class LanguageSettingsImages {
	// evaluates to "/${ProjName)/"
	private static final String PROJ_NAME_PREFIX = '/'
			+ CdtVariableResolver.createVariableReference(CdtVariableResolver.VAR_PROJ_NAME) + '/';

	/**
	 * Check if the language settings entry should be presented as "project-relative" in UI.
	 *
	 * @param entry - language settings entry to check.
	 * @return {@code true} if the entry should be displayed as "project-relative", {@code false} otherwise.
	 */
	public static boolean isProjectRelative(ICLanguageSettingEntry entry) {
		if (entry instanceof ACPathEntry) {
			String path = entry.getName();
			return ((ACPathEntry) entry).isValueWorkspacePath() && path.startsWith(PROJ_NAME_PREFIX);
		}
		return false;
	}

	/**
	 * Convert path used by {@link ICLanguageSettingEntry} to label representing project-relative portion.
	 *
	 * @param path - path to convert to label in project-relative format.
	 * @return label to be used to display the path in UI.
	 */
	public static String toProjectRelative(String path) {
		if (path.startsWith(LanguageSettingsImages.PROJ_NAME_PREFIX)) {
			return path.substring(LanguageSettingsImages.PROJ_NAME_PREFIX.length());
		}
		return path;
	}

	/**
	 * Convert label for project-relative path back to path representation carried by {@link ICLanguageSettingEntry}.
	 *
	 * @param label - label in project-relative format.
	 * @return path to be used by {@link ICLanguageSettingEntry}.
	 */
	public static String fromProjectRelative(String label) {
		return LanguageSettingsImages.PROJ_NAME_PREFIX + label;
	}

	/**
	 * Returns image for the given {@link ICLanguageSettingEntry} from internally managed repository including
	 * necessary overlays for given configuration description.
	 *
	 * @param kind - kind of {@link ICLanguageSettingEntry}, i.e. {@link ICSettingEntry#INCLUDE_PATH} etc.
	 * @param flags - flags of {@link ICSettingEntry}.
	 * @param isProjectRelative specifies if the image should present "project-relative" icon.
	 * @return the image for the entry with appropriate overlays.
	 */
	public static Image getImage(int kind, int flags, boolean isProjectRelative) {
		String imageKey = getImageKey(kind, flags, isProjectRelative);
		if (imageKey != null) {
			return CDTSharedImages.getImage(imageKey);
		}
		return null;
	}

	/**
	 * Returns image for the given entry from internally managed repository including
	 * necessary overlays for given configuration description.
	 *
	 * @param entry - language settings entry to get an image for.
	 * @param cfgDescription - configuration description of the entry.
	 * @return the image for the entry with appropriate overlays.
	 */
	public static Image getImage(ICLanguageSettingEntry entry, ICConfigurationDescription cfgDescription) {
		int kind = entry.getKind();
		int flags = entry.getFlags();
		boolean isProjectRelative = isProjectRelative(entry);

		String imageKey = getImageKey(kind, flags, isProjectRelative);
		Image image = null;
		if (imageKey != null) {
			String[] overlayKeys = new String[5];

			if ((flags & ICSettingEntry.UNDEFINED) != 0) {
				image = CDTSharedImages.getImageOverlaid(imageKey, CDTSharedImages.IMG_OVR_INACTIVE,
						IDecoration.BOTTOM_LEFT);
			} else {
				String overlayKeyStatus = null;
				IStatus status = getStatus(entry, cfgDescription);
				switch (status.getSeverity()) {
				case IStatus.ERROR:
					overlayKeyStatus = CDTSharedImages.IMG_OVR_ERROR;
					break;
				case IStatus.WARNING:
					overlayKeyStatus = CDTSharedImages.IMG_OVR_WARNING;
					break;
				case IStatus.INFO:
					overlayKeyStatus = CDTSharedImages.IMG_OVR_WARNING;
					break;
				}
				if (overlayKeyStatus != null) {
					overlayKeys[IDecoration.BOTTOM_LEFT] = overlayKeyStatus;
				}

				if ((flags & ICSettingEntry.EXPORTED) != 0) {
					overlayKeys[IDecoration.BOTTOM_RIGHT] = CDTSharedImages.IMG_OVR_EXPORTED;
				}

				image = CDTSharedImages.getImageOverlaid(imageKey, overlayKeys);
			}
		}
		return image;
	}

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
	 * Checking if the entry points to existing or accessible location.
	 * @param entry - resolved entry
	 */
	private static boolean isLocationOk(ACPathEntry entry) {
		boolean exists = true;
		boolean isWorkspacePath = (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
		if (isWorkspacePath) {
			IPath path = new Path(entry.getValue());
			IResource rc = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			exists = (rc != null) && rc.isAccessible();
		} else if (UNCPathConverter.isUNC(entry.getName())) {
			return true;
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
				ICLanguageSettingEntry[] entries = CDataUtil.resolveEntries(new ICLanguageSettingEntry[] { entry },
						cfgDescription);
				if (entries != null && entries.length > 0) {
					entry = entries[0];
				}
			}

			ACPathEntry acEntry = (ACPathEntry) entry;
			String acEntryName = acEntry.getName();
			IPath path = new Path(acEntryName);
			if (!path.isAbsolute()) {
				return new Status(IStatus.INFO, CUIPlugin.PLUGIN_ID,
						Messages.LanguageSettingsImages_UsingRelativePathsNotRecommended);
			}
			if (!isLocationOk(acEntry)) {
				if (acEntry.isFile()) {
					return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
							Messages.LanguageSettingsImages_FileDoesNotExist);
				} else {
					return new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
							Messages.LanguageSettingsImages_FolderDoesNotExist);
				}
			}

		}
		return Status.OK_STATUS;
	}

}
