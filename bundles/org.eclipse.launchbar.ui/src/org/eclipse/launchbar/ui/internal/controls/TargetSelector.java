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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.launchbar.core.internal.LaunchBarManager;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.RemoteConnectionsLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

public class TargetSelector extends CSelector {

	private final LaunchBarUIManager uiManager = Activator.getDefault().getLaunchBarUIManager();

	private static final String[] noTargets = new String[] { "---" }; //$NON-NLS-1$

	public TargetSelector(Composite parent, int style) {
		super(parent, style);

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
				try {
					List<IRemoteConnection> targets = manager.getLaunchTargets(manager.getActiveLaunchDescriptor());
					if (!targets.isEmpty()) {
						return targets.toArray();
					}
				} catch (CoreException e) {
					Activator.log(e);
				}
				return noTargets;
			}
		});

		setLabelProvider(new RemoteConnectionsLabelProvider());

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
		// TODO
		return false;
	}

	@Override
	public void handleEdit(Object element) {
		// TODO
	}

	@Override
	public boolean hasActionArea() {
		return true;
	}

	@Override
	public void createActionArea(final Composite parent) {
		Composite actionArea = new Composite(parent, SWT.NONE);
		GridLayout actionLayout = new GridLayout();
		actionLayout.marginWidth = actionLayout.marginHeight = 0;
		actionArea.setLayout(actionLayout);
		actionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Composite createButton = new Composite(actionArea, SWT.NONE);
		createButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = buttonLayout.marginHeight = 7;
		createButton.setLayout(buttonLayout);
		createButton.setBackground(backgroundColor);
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
		createLabel.setText(Messages.TargetSelector_CreateNewTarget);
		createLabel.setBackground(backgroundColor);

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseUp(org.eclipse.swt.events.MouseEvent event) {
				try {
					ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
					Command newConnectionCmd = commandService.getCommand(IRemoteUIConnectionService.NEW_CONNECTION_COMMAND);
					newConnectionCmd.executeWithChecks(new ExecutionEvent());
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
					Activator.log(e);
				}
			}
		};

		createButton.addMouseListener(mouseListener);
		createLabel.addMouseListener(mouseListener);

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
		if (selection instanceof IRemoteConnection) {
			IRemoteConnection target = (IRemoteConnection) selection;
			try {
				uiManager.getManager().setActiveLaunchTarget(target);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(200, hHint, changed);
	}

	@Override
	public void setSelection(Object element) {
		if (element == null)
			element = noTargets[0];
		super.setSelection(element);
	}

}
