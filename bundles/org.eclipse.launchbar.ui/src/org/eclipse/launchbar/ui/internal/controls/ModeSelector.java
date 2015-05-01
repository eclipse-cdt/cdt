/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.controls;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class ModeSelector extends CSelector {

	private static final String[] noModes = new String[] { "---" }; //$NON-NLS-1$

	private final LaunchBarManager manager = Activator.getDefault().getLaunchBarUIManager().getManager();

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
					Activator.log(e.getStatus());
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
					try {
						ILaunchGroup group = getLaunchGroup(mode.getIdentifier());
						if (group != null) {
							ImageDescriptor imageDesc = group.getImageDescriptor();
							Image image = images.get(imageDesc);
							if (image == null) {
								image = imageDesc.createImage();
								images.put(imageDesc, image);
							}
							return image;
						}
					} catch (CoreException e) {
						Activator.log(e.getStatus());
					}
				}
				return super.getImage(element);
			}
			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchMode) {
					ILaunchMode mode = (ILaunchMode) element;
					try {
						ILaunchGroup group = getLaunchGroup(mode.getIdentifier());
						if (group != null) {
							return group.getLabel().replace("&", ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} catch (CoreException e) {
						Activator.log(e.getStatus());
					}
				}
				return super.getText(element);
			}
		});

		setSorter(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof ILaunchMode && o2 instanceof ILaunchMode) {
					String mode1 = ((ILaunchMode)o1).getIdentifier();
					String mode2 = ((ILaunchMode)o2).getIdentifier();
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
			}
		});
	}


	protected ILaunchGroup getLaunchGroup(String mode) throws CoreException {
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(manager.getActiveLaunchDescriptor(), manager.getActiveLaunchTarget());
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
				Activator.log(e.getStatus());
			} catch (Exception e) {
				// manager can throw illegal state exception hopefully we never get it
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
		super.setSelection(element);
	}

}
