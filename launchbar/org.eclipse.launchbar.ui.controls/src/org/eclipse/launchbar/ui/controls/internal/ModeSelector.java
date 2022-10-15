/*******************************************************************************
 * Copyright (c) 2014, 2022 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.ui.ILaunchBarUIConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ModeSelector extends CSelector {
	private static final String[] noModes = new String[] { "---" }; //$NON-NLS-1$
	private final ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);

	public ModeSelector(Composite parent, int style) {
		super(parent, style);
		setToolTipText(Messages.ModeSelector_0);
		setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				try {
					ILaunchMode[] modes = manager.getLaunchModes();
					if (modes.length > 0)
						return modes;
				} catch (CoreException e) {
					Activator.log(e);
				}
				return noModes;
			}
		});
		setLabelProvider(new LabelProvider() {
			private Map<ImageDescriptor, Image> images = new HashMap<>();

			@Override
			public void dispose() {
				super.dispose();
				for (Image image : images.values()) {
					image.dispose();
				}
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof ILaunchMode) {
					ILaunchMode mode = (ILaunchMode) element;
					ILaunchGroup group = getLaunchGroup(mode);
					if (group != null) {
						ImageDescriptor imageDesc = group.getImageDescriptor();
						if (imageDesc == null)
							return null;
						Image image = images.get(imageDesc);
						if (image == null) {
							image = imageDesc.createImage();
							images.put(imageDesc, image);
						}
						return image;
					}
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchMode) {
					ILaunchMode mode = (ILaunchMode) element;
					ILaunchGroup group = getLaunchGroup(mode);
					if (group != null) {
						return group.getLabel().replace("&", ""); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				return super.getText(element);
			}
		});
		setSorter((o1, o2) -> {
			if (o1 instanceof ILaunchMode && o2 instanceof ILaunchMode) {
				String mode1 = ((ILaunchMode) o1).getIdentifier();
				String mode2 = ((ILaunchMode) o2).getIdentifier();
				// run comes first, then debug, then the rest
				if (mode1.equals("run")) { //$NON-NLS-1$
					if (mode2.equals("run")) //$NON-NLS-1$
						return 0;
					else
						return -1;
				}
				if (mode2.equals("run")) //$NON-NLS-1$
					return 1;
				if (mode1.equals("debug")) { //$NON-NLS-1$
					if (mode2.equals("debug")) //$NON-NLS-1$
						return 0;
					else
						return -1;
				}
				if (mode2.equals("debug")) //$NON-NLS-1$
					return 1;
			}
			return 0;
		});
	}

	protected ILaunchGroup getDefaultLaunchGroup(String mode) {
		String groupId;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			groupId = IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
		} else if (mode.equals(ILaunchManager.PROFILE_MODE)) {
			groupId = IDebugUIConstants.ID_PROFILE_LAUNCH_GROUP;
		} else {
			groupId = IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
		}
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(groupId);
	}

	protected ILaunchGroup getLaunchGroup(String mode) throws CoreException {
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(manager.getActiveLaunchDescriptor(),
				manager.getActiveLaunchTarget());
		if (type == null)
			return null;
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(type, mode);
	}

	@Override
	protected void fireSelectionChanged() {
		Object selected = getSelection();
		if (selected instanceof ILaunchMode) {
			ILaunchMode mode = (ILaunchMode) selected;
			try {
				manager.setActiveLaunchMode(mode);
			} catch (CoreException e) {
				Activator.log(e);
			} catch (Exception e) {
				// manager can throw illegal state exception hopefully we never
				// get it
				Activator.log(e);
			}
		}
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(150, hHint, changed);
	}

	@Override
	public void setSelection(Object element) {
		if (element == null)
			element = noModes[0];
		if (isDisposed())
			return;
		super.setSelection(element);
		updateLaunchButton(findLaunchButton());
	}

	private CLaunchButton findLaunchButton() {
		String commandId = ILaunchBarUIConstants.CMD_LAUNCH;
		// Launchbar buttons are grouped in a composite that is a sibling to this
		for (Control control : getParent().getChildren()) {
			if (control instanceof Composite comp) {
				for (Control button : comp.getChildren()) {
					if (commandId.equals(button.getData("command"))) { //$NON-NLS-1$
						// found launch button
						return (CLaunchButton) button;
					}
				}
			}
		}
		Activator.log(new RuntimeException("Launch button is not found in toolbar")); //$NON-NLS-1$
		return null;
	}

	private void updateLaunchButton(CLaunchButton button) {
		if (button == null || isDisposed()) {
			return;
		}
		Object selection = getSelection();
		if (selection instanceof ILaunchMode mode) {
			button.setToolTipText(NLS.bind(Messages.ModeSelector_ToolTip, mode.getLabel()));
			Image image = getLabelProvider().getImage(mode);
			button.setImage(image);
		}
	}

	public ILaunchGroup getLaunchGroup(ILaunchMode mode) {
		ILaunchGroup group = null;
		try {
			group = getLaunchGroup(mode.getIdentifier());
		} catch (CoreException e) {
			Activator.log(e);
		}
		if (group == null) {
			group = getDefaultLaunchGroup(mode.getIdentifier());
		}
		return group;
	}
}
