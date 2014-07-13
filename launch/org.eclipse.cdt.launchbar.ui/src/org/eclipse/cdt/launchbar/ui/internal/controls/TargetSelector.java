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
package org.eclipse.cdt.launchbar.ui.internal.controls;

import java.util.Comparator;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.IHoverProvider;
import org.eclipse.cdt.launchbar.ui.ILaunchBarUIConstants;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.cdt.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TargetSelector extends CSelector {

	private final LaunchBarUIManager uiManager;
	
	private static final String[] noTargets = new String[] { "---" };
	

	public TargetSelector(Composite parent, int style) {
		super(parent, style);

		ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
		uiManager = (LaunchBarUIManager) manager.getAdapter(LaunchBarUIManager.class);

		setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				try {
					ILaunchTarget[] targets = getManager().getLaunchTargets();
					if (targets.length > 0)
						return targets;
				} catch (CoreException e) {
					Activator.log(e.getStatus());
				}
				return noTargets;
			}
		});

		setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider labelProvider = uiManager.getLabelProvider(target);
					if (labelProvider != null) {
						return labelProvider.getImage(element);
					}
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					ILabelProvider labelProvider = uiManager.getLabelProvider(target);
					if (labelProvider != null) {
						return labelProvider.getText(element);
					}
					return target.getName();
				}
				return super.getText(element);
			}
		});

		setSorter(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				// Sort by name
				return 0;
			}
		});

		setHoverProvider(new IHoverProvider() {
			@Override
			public boolean displayHover(Object element) {
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					IHoverProvider hoverProvider = uiManager.getHoverProvider(target);
					if (hoverProvider != null) {
						return hoverProvider.displayHover(element);
					}
				}
				return false;
			}
			
			@Override
			public void dismissHover(Object element, boolean immediate) {
				if (element instanceof ILaunchTarget) {
					ILaunchTarget target = (ILaunchTarget) element;
					IHoverProvider hoverProvider = uiManager.getHoverProvider(target);
					if (hoverProvider != null) {
						hoverProvider.dismissHover(element, immediate);
					}
				}
			}
		});
	}

	@Override
	public boolean isEditable(Object element) {
		if (element instanceof ILaunchTarget) {
			ILaunchTarget target = (ILaunchTarget) element;
			return uiManager.getEditCommand(target) != null;
		}
		return false;
	}

	@Override
	public void handleEdit(Object element) {
		if (element instanceof ILaunchTarget) {
			ILaunchTarget target = (ILaunchTarget) element;
			String commandId = uiManager.getEditCommand(target);
			Activator.runCommand(commandId, ILaunchBarUIConstants.TARGET_NAME, target.getName());
		}
	}

	@Override
	public boolean hasActionArea() {
			// TODO need an add target command similar to the add configuration that allows the user
			// to select the target type.
//			return uiManager.getAddTargetCommand(getManager().getActiveLaunchDescriptor()) != null;
			return false;
	}

	@Override
	public void createActionArea(Composite parent) {
		Composite actionArea = new Composite(parent, SWT.NONE);
		GridLayout actionLayout = new GridLayout();
		actionLayout.marginWidth = actionLayout.marginHeight = 0;
		actionArea.setLayout(actionLayout);
		actionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Composite createButton = new Composite(actionArea, SWT.NONE);
		createButton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 7;
		createButton.setLayout(buttonLayout);
		createButton.setBackground(white);
		createButton.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Point size = createButton.getSize();
				GC gc = e.gc;
				gc.setForeground(outlineColor);
				gc.drawLine(0, 0, size.x, 0);
			}
		});

		final Label createLabel = new Label(createButton, SWT.None);
		createLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createLabel.setText("Add New Target...");
		createLabel.setBackground(white);

//		try {
//			final String command = uiManager.getAddTargetCommand(getManager().getActiveLaunchDescriptor());
//			MouseListener mouseListener = new MouseAdapter() {
//				public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
//					Activator.runCommand(command);
//				}
//			};
//
//			createButton.addMouseListener(mouseListener);
//			createLabel.addMouseListener(mouseListener);
//		} catch (CoreException e) {
//			Activator.log(e.getStatus());
//		}

		MouseTrackListener mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				createButton.setBackground(highlightColor);
				createLabel.setBackground(highlightColor);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				createButton.setBackground(white);
				createLabel.setBackground(white);
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
				uiManager.getManager().setActiveLaunchTarget(target);
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		}
	}
	
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(200, hHint, changed);
	}

	private ILaunchBarManager getManager() {
		return (ILaunchBarManager) getInput();
	}

	@Override
	public void setSelection(Object element) {
		if (element == null)
			element = noTargets[0];
		super.setSelection(element);
	}
	
}
