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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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
						if (group == null) {
							group = getDefaultLaunchGroup(mode.getIdentifier());
						}
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
						if (group == null) {
							group = getDefaultLaunchGroup(mode.getIdentifier());
						}
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
			}
		});
	}

	protected ILaunchGroup getDefaultLaunchGroup(String mode) throws CoreException {
		String groupId = null;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			groupId = IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
		} else if (mode.equals(ILaunchManager.PROFILE_MODE)) {
			groupId = IDebugUIConstants.ID_PROFILE_LAUNCH_GROUP;
		} else {
			groupId = IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
		}
		if (groupId != null)
			return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(groupId);
		return null;
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
				Activator.log(e.getStatus());
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
		super.setSelection(element);
		updateLaunchButton(findLaunchButton());
	}

	private ToolItem findLaunchButton() {
		String commandId = Activator.CMD_LAUNCH;
		for (Control control : getParent().getChildren()) {
			if (control instanceof ToolBar) {
				for (ToolItem toolItem : ((ToolBar) control).getItems()) {
					if (commandId.equals(toolItem.getData("command"))) { //$NON-NLS-1$
						// found launch button
						return toolItem;
					}
				}
			}
		}
		Activator.log(new RuntimeException("Launch button is not found in toolbar")); //$NON-NLS-1$
		return null;
	}

	private void updateLaunchButton(ToolItem toolItem) {
		if (toolItem == null || isDisposed()) {
			return;
		}
		toolItem.setImage(Activator.getDefault().getImage(Activator.IMG_BUTTON_LAUNCH));
		Object selection = getSelection();
		if (selection instanceof ILaunchMode) {
			ILaunchMode lmode = (ILaunchMode) selection;
			toolItem.setToolTipText(NLS.bind(Messages.ModeSelector_ToolTip, lmode.getLabel()));
			String mode = lmode.getIdentifier();
			String iconPath = "icons/icon_" + mode + "_32x32.png"; //$NON-NLS-1$ //$NON-NLS-2$
			Image modeBigImage = Activator.getDefault().getImage(iconPath);
			if (modeBigImage == null) {
				// no icon for the mode, lets do an overlay
				Image modeImageOrig = getLabelProvider().getImage(lmode);
				if (modeImageOrig != null) {
					ImageDescriptor composite = new ReversedCenterOverlay(modeImageOrig.getImageData());
					Activator.getDefault().getImageRegistry().put(iconPath, composite);
					modeBigImage = Activator.getDefault().getImage(iconPath);
				}
			}
			if (modeBigImage != null) {
				toolItem.setImage(modeBigImage);
			}
		}
	}

	private final class ReversedCenterOverlay extends CompositeImageDescriptor {
		private ImageData small;

		public ReversedCenterOverlay(ImageData small) {
			this.small = small;
		}

		@Override
		protected Point getSize() {
			return new Point(32, 32);
		}

		@Override
		protected void drawCompositeImage(int width, int height) {
			ImageDescriptor base = Activator.getImageDescriptor("icons/launch_base_blank.png"); //$NON-NLS-1$
			ImageData baseData = base.getImageData();
			int baseColor = baseData.getPixel(16, 16);
			ImageData data = getReversed(small, baseData.palette.getRGB(baseColor));
			drawImage(baseData, 0, 0);
			drawImage(data, 8, 8);
		}

		private ImageData getReversed(ImageData imageData, RGB baseColor) {
			int width = imageData.width;
			PaletteData palette = imageData.palette;
			RGB whiteColor = new RGB(255, 255, 255);
			if (!palette.isDirect) {
				palette.colors = Arrays.copyOf(palette.colors, palette.colors.length + 2);
				palette.colors[palette.colors.length - 1] = baseColor;
				palette.colors[palette.colors.length - 2] = whiteColor;
			}
			int whitePixel = palette.getPixel(whiteColor);
			int basePixed = palette.getPixel(baseColor);
			int transPixed = imageData.transparentPixel;
			int[] lineData = new int[imageData.width];
			for (int y = 0; y < imageData.height; y++) {
				imageData.getPixels(0, y, width, lineData, 0);
				for (int x = 0; x < lineData.length; x++) {
					int pixelValue = lineData[x];
					if (pixelValue == transPixed) {
						imageData.setPixel(x, y, basePixed);
						continue;
					}
					RGB rgb = palette.getRGB(pixelValue);
					float brightness = rgb.getHSB()[2];
					if (brightness > 0.97) {
						imageData.setPixel(x, y, basePixed);
					} else {
						imageData.setPixel(x, y, whitePixel);
					}
				}
			}
			imageData.transparentPixel = -1;
			return imageData;
		}
	}
}
