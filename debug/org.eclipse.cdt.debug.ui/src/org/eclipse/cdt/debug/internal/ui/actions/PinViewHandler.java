/*******************************************************************************
 * Copyright (c) 2010, 2012 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial implementation of run()
 *     Marc Dumais (Ericsson) - Bug 437692
 *     Raghunandana Murthappa(Advantest Europe GmbH) - Issue 1048
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.debug.internal.ui.pinclone.DebugContextPinProvider;
import org.eclipse.cdt.debug.internal.ui.pinclone.DebugEventFilterService;
import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * A Handler which handles the Pin View Command contributed to all the debug
 * related views.
 */
public class PinViewHandler extends AbstractHandler {
	private DebugContextPinProvider fProvider;
	private String fLastKnownDescription = ""; //$NON-NLS-1$
	private String fPinnedContextLabel = ""; //$NON-NLS-1$
	private IPartListener2 fPartListener;
	private static final Set<IViewPart> pinned = new HashSet<>();
	private Image image;

	public PinViewHandler() {
		createPartListener();
	}

	private void createPartListener() {
		fPartListener = new IPartListener2() {
			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (pinned == null || !pinned.contains(part))
					return;
				pinned.remove(part);
				DebugEventFilterService.getInstance().removeDebugEventFilter(part);
			}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
			}
		};
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Event trigger = (org.eclipse.swt.widgets.Event) event.getTrigger();
		ToolItem toolItem = (ToolItem) trigger.widget;
		boolean selection = toolItem.getSelection();

		IViewPart viewPart = (IViewPart) HandlerUtil.getActivePart(event);
		if (selection) {
			fProvider = DebugEventFilterService.getInstance().addDebugEventFilter(viewPart,
					getActiveDebugContext(viewPart));
			if (fProvider != null) {
				fLastKnownDescription = ((WorkbenchPart) viewPart).getContentDescription();
				fPinnedContextLabel = getPinContextLabel(fProvider);
				PinCloneUtils.setPartContentDescription(viewPart, fPinnedContextLabel);
				disposeImage();
				updatePinContextColor(fProvider, toolItem);
				pinned.add(viewPart);
				viewPart.getSite().getWorkbenchWindow().getPartService().addPartListener(fPartListener);
			}
		} else {
			fProvider = null;
			DebugEventFilterService.getInstance().removeDebugEventFilter(viewPart);
			disposeImage();
			updatePinContextColor(fProvider, toolItem);
			PinCloneUtils.setPartContentDescription(viewPart, fLastKnownDescription);
			pinned.remove(viewPart);
			viewPart.getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
		}

		return IStatus.OK;
	}

	private void disposeImage() {
		if (image != null) {
			image.dispose();
		}
	}

	protected ISelection getActiveDebugContext(IViewPart viewPart) {
		IDebugContextService contextService = DebugUITools.getDebugContextManager()
				.getContextService(viewPart.getSite().getWorkbenchWindow());
		return contextService.getActiveContext();
	}

	private String getPinContextLabel(DebugContextPinProvider provider) {
		String description = ""; //$NON-NLS-1$

		if (provider != null) {
			Set<String> labels = new HashSet<>();
			for (IPinElementHandle handle : provider.getPinHandles()) {
				String tmp = getLabel(handle);
				if (tmp != null && tmp.trim().length() != 0)
					labels.add(tmp);
			}

			for (String label : labels) {
				if (label != null) {
					if (description.length() > 0) {
						description += ", " + label; //$NON-NLS-1$
					} else {
						description = label;
					}
				}
			}
		}
		return description;
	}

	private String getLabel(IPinElementHandle handle) {
		String label = ""; //$NON-NLS-1$

		if (handle != null)
			label = handle.getLabel();

		return label;
	}

	private void updatePinContextColor(DebugContextPinProvider provider, ToolItem toolItem) {
		ImageDescriptor imageDesc = null;
		if (provider != null) {
			Set<IPinElementHandle> handles = provider.getPinHandles();

			// if handles have different toolbar icon descriptor or different pin color,
			// than use a
			// multi-pin toolbar icon
			if (useMultiPinImage(handles))
				imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_MULTI);

			if (imageDesc == null) {
				Iterator<IPinElementHandle> itr = handles.iterator();
				if (itr.hasNext()) {
					IPinElementHandle handle = itr.next();
					IPinElementColorDescriptor desc = handle.getPinElementColorDescriptor();
					if (desc != null)
						imageDesc = desc.getToolbarIconDescriptor();

					if (imageDesc == null && desc != null) {
						int overlayColor = desc.getOverlayColor() % IPinElementColorDescriptor.DEFAULT_COLOR_COUNT;

						switch (overlayColor) {
						case IPinProvider.IPinElementColorDescriptor.GREEN:
							imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_G);
							break;
						case IPinProvider.IPinElementColorDescriptor.RED:
							imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_R);
							break;
						case IPinProvider.IPinElementColorDescriptor.BLUE:
							imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION_B);
							break;
						}
					}
				}
			}
		}

		if (imageDesc == null)
			imageDesc = CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_VIEW_PIN_ACTION);
		image = imageDesc.createImage();
		toolItem.setImage(image);
	}

	private boolean useMultiPinImage(Set<IPinElementHandle> handles) {
		if (handles.size() <= 1)
			return false;

		int overlayColor = IPinElementColorDescriptor.UNDEFINED;
		ImageDescriptor imageDesc = null;
		for (IPinElementHandle handle : handles) {
			IPinElementColorDescriptor colorDesc = handle.getPinElementColorDescriptor();
			if (colorDesc != null) {
				ImageDescriptor descImageDesc = colorDesc.getToolbarIconDescriptor();
				if (imageDesc != null && !imageDesc.equals(descImageDesc))
					return true;
				imageDesc = descImageDesc;

				int descOverlayColor = colorDesc.getOverlayColor();
				if (overlayColor != IPinElementColorDescriptor.UNDEFINED && descOverlayColor != overlayColor)
					return true;
				overlayColor = descOverlayColor;
			}
		}

		return false;
	}

	@Override
	public void dispose() {
		pinned.clear();
		disposeImage();
	}

	public static Set<IViewPart> getPinnedViews() {
		return pinned;
	}
}
