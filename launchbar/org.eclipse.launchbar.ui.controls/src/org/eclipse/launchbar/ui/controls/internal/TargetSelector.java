/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetListener;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.launchbar.core.target.TargetStatus.Code;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.launchbar.ui.target.NewLaunchTargetWizardAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class TargetSelector extends CSelector implements ILaunchTargetListener {

	private final ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
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
				try {
					return manager.getLaunchTargets(manager.getActiveLaunchDescriptor());
				} catch (CoreException e) {
					Activator.log(e);
					return noTargets;
				}
			}
		});

		setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider provider = targetUIManager.getLabelProvider(target);
					return provider != null ? provider.getText(target) : target.getId();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element == ILaunchTarget.NULL_TARGET) {
					return null;
				}
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider provider = targetUIManager.getLabelProvider(target);
					if (provider != null) {
						final Image baseImage = provider.getImage(target);
						final TargetStatus status = targetManager.getStatus(target);
						if (status.getCode() == Code.OK) {
							return baseImage;
						} else {
							String compId = String.format("%s.%s.%s", target.getTypeId(), target.getId(), //$NON-NLS-1$
									status.getCode());
							Image image = Activator.getDefault().getImageRegistry().get(compId);
							if (image == null && baseImage != null) {
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

		setSorter((o1, o2) -> {
			// Sort by name
			String s1 = String.valueOf(o1);
			String s2 = String.valueOf(o2);
			return s1.compareTo(s2);
		});
	}

	@Override
	public boolean isEditable(Object element) {
		return true;
	}

	@Override
	public void handleEdit(Object element) {
		targetUIManager.editLaunchTarget((ILaunchTarget) getSelection());
	}

	@Override
	public boolean hasActionArea() {
		return true;
	}

	@Override
	public void createActionArea(final Composite parent) {
		final Composite createButton = new Composite(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(createButton);
		GridLayoutFactory.fillDefaults().margins(7, 7).applyTo(createButton);
		createButton.setBackground(getBackground());

		final Label createLabel = new Label(createButton, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(createLabel);
		createLabel.setBackground(getBackground());
		createLabel.setText(Messages.TargetSelector_CreateNewTarget);

		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent event) {
				new NewLaunchTargetWizardAction().run();
			}
		};

		createButton.addMouseListener(mouseListener);
		createLabel.addMouseListener(mouseListener);

		MouseTrackListener mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				Color highlightColor = getHighlightColor();
				createButton.setBackground(highlightColor);
				createLabel.setBackground(highlightColor);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				Color backgroundColor = getBackground();
				createButton.setBackground(backgroundColor);
				createLabel.setBackground(backgroundColor);
			}
		};
		createButton.addMouseTrackListener(mouseTrackListener);
		createLabel.addMouseTrackListener(mouseTrackListener);
	}

	@Override
	protected void fireSelectionChanged() {
		Object selection = getSelection();
		if (selection instanceof ILaunchTarget) {
			ILaunchTarget target = (ILaunchTarget) selection;
			try {
				manager.setActiveLaunchTarget(target);
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
		if (isDisposed())
			return;
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
		try {
			if (target.equals(manager.getActiveLaunchTarget())) {
				refresh();
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

}
