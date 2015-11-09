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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetListener;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.launchbar.core.target.TargetStatus.Code;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;

public class TargetSelector extends CSelector implements ILaunchTargetListener {

	private final LaunchBarUIManager uiManager = Activator.getDefault().getLaunchBarUIManager();
	private final ILaunchTargetUIManager targetUIManager = Activator.getService(ILaunchTargetUIManager.class);
	private final ILaunchTargetManager targetManager = Activator.getService(ILaunchTargetManager.class);

	private static final ILaunchTarget[] noTargets = new ILaunchTarget[] { ILaunchTarget.NULL_TARGET };

	public TargetSelector(Composite parent, int style) {
		super(parent, style);

		targetManager.addListener(this);

		setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				LaunchBarManager manager = uiManager.getManager();
				List<ILaunchTarget> targets = manager.getLaunchTargets(manager.getActiveLaunchDescriptor());
				if (!targets.isEmpty()) {
					return targets.toArray();
				}
				return noTargets;
			}
		});

		setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider provider = targetUIManager.getLabelProvider(target);
					return provider != null ? provider.getText(target) : target.getName();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element == ILaunchTarget.NULL_TARGET) {
					return null;
				}
				if (element instanceof ILaunchTarget) {
					// TODO apply a status overlay
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider provider = targetUIManager.getLabelProvider(target);
					if (provider != null) {
						final Image baseImage = provider.getImage(target);
						final TargetStatus status = targetManager.getStatus(target);
						if (status.getCode() == Code.OK) {
							return baseImage;
						} else {
							String compId = target.getTypeId()
									+ (status.getCode() == Code.ERROR ? ".error" : ".warning"); //$NON-NLS-1$ //$NON-NLS-2$
							Image image = Activator.getDefault().getImageRegistry().get(compId);
							if (image == null) {
								ImageDescriptor desc = new CompositeImageDescriptor() {
									@Override
									protected Point getSize() {
										Rectangle bounds = baseImage.getBounds();
										return new Point(bounds.width, bounds.height);
									}

									@Override
									protected void drawCompositeImage(int width, int height) {
										Image overlay = PlatformUI.getWorkbench().getSharedImages()
												.getImage(status.getCode() == Code.ERROR
														? ISharedImages.IMG_DEC_FIELD_ERROR
														: ISharedImages.IMG_DEC_FIELD_WARNING);
										drawImage(baseImage.getImageData(), 0, 0);
										int y = baseImage.getBounds().height - overlay.getBounds().height;
										drawImage(overlay.getImageData(), 0, y);
									}
								};
								image = desc.createImage();
								Activator.getDefault().getImageRegistry().put(compId, image);
							}
							return image;
						}
					}
				}
				return super.getImage(element);
			}
		});

		setSorter(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				// Sort by name
				String s1 = String.valueOf(o1);
				String s2 = String.valueOf(o2);
				return s1.compareTo(s2);
			}
		});
	}

	@Override
	public boolean isEditable(Object element) {
		return true;
	}

	private ISelectionProvider getSelectionProvider() {
		return new ISelectionProvider() {
			@Override
			public void setSelection(ISelection selection) {
				// ignore
			}

			@Override
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				// ignore
			}

			@Override
			public ISelection getSelection() {
				return new StructuredSelection(TargetSelector.this.getSelection());
			}

			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				// ignore
			}
		};
	}

	@Override
	public void handleEdit(Object element) {
		// opens property dialog on a selected target
		new PropertyDialogAction(new SameShellProvider(getShell()), getSelectionProvider()).run();
	}

	@Override
	protected void fireSelectionChanged() {
		Object selection = getSelection();
		if (selection instanceof ILaunchTarget) {
			ILaunchTarget target = (ILaunchTarget) selection;
			try {
				uiManager.getManager().setActiveLaunchTarget(target);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	public void setToolTipText(ILaunchTarget target) {
		String text = Messages.TargetSelector_ToolTipPrefix + ": " + targetManager.getStatus(target).getMessage(); //$NON-NLS-1$
		setToolTipText(text);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(200, hHint, changed);
	}

	@Override
	public void setSelection(Object element) {
		if (element == null) {
			element = noTargets[0];
		} else if (element instanceof ILaunchTarget) {
			setToolTipText((ILaunchTarget) element);
		}
		super.setSelection(element);
	}

	@Override
	public void dispose() {
		super.dispose();
		targetManager.removeListener(this);
	}

	@Override
	public void update(Object element) {
		super.update(element);
		if (element != null && element instanceof ILaunchTarget) {
			setToolTipText((ILaunchTarget) element);
		} else {
			setToolTipText(Messages.TargetSelector_ToolTipPrefix);
		}
	}

	@Override
	public void launchTargetStatusChanged(ILaunchTarget target) {
		if (target.equals(uiManager.getManager().getActiveLaunchTarget())) {
			refresh();
		}
	}

}
