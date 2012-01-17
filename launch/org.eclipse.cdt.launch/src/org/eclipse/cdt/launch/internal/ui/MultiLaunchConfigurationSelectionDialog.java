/*******************************************************************************
 *  Copyright (c) 2009, 2010 QNX Software Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *      Freescale Semiconductor
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate;
import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate.LaunchElement;
import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate.LaunchElement.EPostLaunchAction;
import org.eclipse.cdt.launch.ui.ComboControlledStackComposite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;



/**
 * Dialog to select launch configuration(s)
 */
public class MultiLaunchConfigurationSelectionDialog extends TitleAreaDialog implements ISelectionChangedListener {
	private ViewerFilter[] fFilters = null;
	private ISelection fSelection;
	private ILaunchGroup[] launchGroups;
	private String mode;
	private EPostLaunchAction action = EPostLaunchAction.NONE;
	private Object actionParam;
	private boolean isDefaultMode;
	private ViewerFilter emptyTypeFilter;
	private IStructuredSelection fInitialSelection;
	private ComboControlledStackComposite fStackComposite;
	private Label fDelayAmountLabel;
	private Text fDelayAmountWidget; // in seconds
	private boolean fForEditing; // true if dialog was opened to edit an entry, otherwise it was opened to add one
	
	public MultiLaunchConfigurationSelectionDialog(Shell shell, String initMode, boolean forEditing) {
		super(shell);
		LaunchConfigurationManager manager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		launchGroups = manager.getLaunchGroups();
		mode = initMode;
		fForEditing = forEditing;
		fFilters = null;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		emptyTypeFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof ILaunchConfigurationType) {
					try {
						ILaunchConfigurationType type = (ILaunchConfigurationType) element;
						return getLaunchManager().getLaunchConfigurations(type).length > 0;
					} catch (CoreException e) {
						return false;
					}
				} else if (element instanceof ILaunchConfiguration) {
					return MultiLaunchConfigurationDelegate.isValidLaunchReference((ILaunchConfiguration) element);
				}
				return true;
			}
		};
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control x = super.createContents(parent);
		validate();
		setErrorMessage(null);
		return x;
	}

	@Override
	protected Control createDialogArea(Composite parent2) {
		Composite comp = (Composite) super.createDialogArea(parent2);
		
		// title bar 
		getShell().setText(fForEditing ?
				LaunchMessages.MultiLaunchConfigurationSelectionDialog_13 :
				LaunchMessages.MultiLaunchConfigurationSelectionDialog_12);
		
		// dialog message area (not title bar)
		setTitle(fForEditing ?
				LaunchMessages.MultiLaunchConfigurationSelectionDialog_15 :
				LaunchMessages.MultiLaunchConfigurationSelectionDialog_14);
		
		fStackComposite = new ComboControlledStackComposite(comp, SWT.NONE);
		HashMap<String, ILaunchGroup> modes = new HashMap<String, ILaunchGroup>();
		for (ILaunchGroup launchGroup : launchGroups) {
			if (!modes.containsKey(launchGroup.getMode())) {
				modes.put(launchGroup.getMode(), launchGroup);
			}
		}
		if (this.mode.equals(MultiLaunchConfigurationDelegate.DEFAULT_MODE)) {
			try {
				this.mode = "run"; //$NON-NLS-1$
				ILaunchConfiguration[] configs = getSelectedLaunchConfigurations();
				if (configs.length > 0) {
					// we care only about the first selected element
					for (Iterator<String> iterator = modes.keySet().iterator(); iterator.hasNext();) {
						String mode = iterator.next();
						if (configs[0].supportsMode(mode)) {
							this.mode = mode;
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		} 
		for (Iterator<String> iterator = modes.keySet().iterator(); iterator.hasNext();) {
			String mode = iterator.next();
			ILaunchGroup launchGroup = modes.get(mode);
			LaunchConfigurationFilteredTree fTree = new LaunchConfigurationFilteredTree(fStackComposite.getStackParent(), SWT.MULTI
			        | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), launchGroup, fFilters);
			String label = mode;
			fStackComposite.addItem(label, fTree);
			fTree.createViewControl();
			ViewerFilter[] filters = fTree.getViewer().getFilters();
			for (ViewerFilter viewerFilter : filters) {
				if (viewerFilter instanceof LaunchGroupFilter) {
					fTree.getViewer().removeFilter(viewerFilter);
				}
			}
			fTree.getViewer().addFilter(emptyTypeFilter);
			fTree.getViewer().addSelectionChangedListener(this);
			if (launchGroup.getMode().equals(this.mode)) {
				fStackComposite.setSelection(label);
			}
			if (fInitialSelection!=null) {
				
				fTree.getViewer().setSelection(fInitialSelection, true);
			}
		}
		fStackComposite.setLabelText(LaunchMessages.MultiLaunchConfigurationSelectionDialog_4); 
		fStackComposite.pack();
		Rectangle bounds = fStackComposite.getBounds();
		// adjust size
		GridData data = ((GridData) fStackComposite.getLayoutData());
		if (data == null) {
			data = new GridData(GridData.FILL_BOTH);
			fStackComposite.setLayoutData(data);
		}
		data.heightHint = Math.max(convertHeightInCharsToPixels(15), bounds.height);
		data.widthHint = Math.max(convertWidthInCharsToPixels(40), bounds.width);
		fStackComposite.getCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mode = ((Combo) e.widget).getText();
			}
		});
		// "Use default mode" checkbox. Use a parent composite to provide consistent left-side padding
		Composite checkboxComp = new Composite(comp, SWT.NONE);
		checkboxComp.setLayout(new GridLayout(1, false));
		checkboxComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button checkBox = new Button(checkboxComp, SWT.CHECK);
		checkBox.setText(LaunchMessages.MultiLaunchConfigurationSelectionDialog_5); 
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isDefaultMode = ((Button) e.widget).getSelection();
			}
		});
		checkBox.setSelection(isDefaultMode);
		
		createPostLaunchControl(comp);
		return comp;
	}

	private void createPostLaunchControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(4, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(comp, SWT.NONE);
		label.setText(LaunchMessages.MultiLaunchConfigurationSelectionDialog_8); 
		Combo combo = new Combo(comp, SWT.READ_ONLY);
		combo.add(LaunchElement.actionEnumToStr(EPostLaunchAction.NONE));
		combo.add(LaunchElement.actionEnumToStr(EPostLaunchAction.WAIT_FOR_TERMINATION));
		combo.add(LaunchElement.actionEnumToStr(EPostLaunchAction.DELAY));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String actionStr = ((Combo) e.widget).getText();
				action = MultiLaunchConfigurationDelegate.LaunchElement.strToActionEnum(actionStr);
				showHideDelayAmountWidgets();
				validate();
			}
		});
		combo.setText(MultiLaunchConfigurationDelegate.LaunchElement.actionEnumToStr(action));
		
		fDelayAmountLabel = new Label(comp, SWT.NONE);
		fDelayAmountLabel.setText(LaunchMessages.MultiLaunchConfigurationSelectionDialog_9); 
		
		fDelayAmountWidget = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = convertWidthInCharsToPixels(8);
		fDelayAmountWidget.setLayoutData(gridData);
		fDelayAmountWidget.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				String text = ((Text)e.widget).getText();
				try {
					actionParam = new Integer(Integer.parseInt(text));
				}
				catch (NumberFormatException exc) {
					actionParam = null;
				}
				validate();
			}
		});
		if (actionParam instanceof Integer) {
			fDelayAmountWidget.setText(((Integer)actionParam).toString());	
		}
		
		showHideDelayAmountWidgets();
	}

	private void showHideDelayAmountWidgets() {
		final boolean visible = action == EPostLaunchAction.DELAY;
		fDelayAmountLabel.setVisible(visible);
		fDelayAmountWidget.setVisible(visible);
	}

	public ILaunchConfiguration[] getSelectedLaunchConfigurations() {
		List<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>(); 
		if (fSelection != null && !fSelection.isEmpty()) {
			for (Iterator<?> iter = ((IStructuredSelection)fSelection).iterator(); iter.hasNext();) {
				Object selection = iter.next();
				if (selection instanceof ILaunchConfiguration) {
					configs.add((ILaunchConfiguration)selection);
				}
			}
		}
		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}

	public String getMode() {
		return isDefaultMode ? MultiLaunchConfigurationDelegate.DEFAULT_MODE : mode;
	}
	
	public EPostLaunchAction getAction(){
		return action;
	}

	public Object getActionParam(){
		return actionParam;
	}

	public static MultiLaunchConfigurationSelectionDialog createDialog(Shell shell, String groupId, boolean forEditing) {
		return new MultiLaunchConfigurationSelectionDialog(shell, groupId, forEditing);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		
		// This listener gets called for a selection change in the launch
		// configuration viewer embedded in the dialog. Problem is, there are
		// numerous viewers--one for each platform debug ILaunchGroup (run,
		// debug, profile). These viewers are stacked, so only one is ever
		// visible to the user. During initialization, we get a selection change
		// notification for every viewer. We need to ignore all but the one that
		// matters--the visible one.
		
		Tree topTree = null;
		final Control topControl = fStackComposite.getTopControl();
		if (topControl instanceof FilteredTree) {
			final TreeViewer viewer = ((FilteredTree)topControl).getViewer();
			if (viewer != null) {
				topTree = viewer.getTree();
			}
		}
		if (topTree == null) {
			return;
		}
		
		boolean selectionIsForVisibleViewer = false;
		final Object src = event.getSource();
		if (src instanceof Viewer) {
			final Control viewerControl = ((Viewer)src).getControl();
			if (viewerControl == topTree) {
				selectionIsForVisibleViewer = true;
			}
		}
		
		if (!selectionIsForVisibleViewer) {
			return;
		}
		
		fSelection = event.getSelection();
		validate();
	}

	protected void validate() {
		Button ok_button = getButton(IDialogConstants.OK_ID);
		boolean isValid = true;
		if (getSelectedLaunchConfigurations().length < 1) {
			setErrorMessage(LaunchMessages.MultiLaunchConfigurationSelectionDialog_7); 
			isValid = false;
		} else {
			setErrorMessage(null);
		}
		
		if (isValid) {
			if (fForEditing) {
				// must have only one selection
				if (getSelectedLaunchConfigurations().length > 1) {
					setErrorMessage(LaunchMessages.MultiLaunchConfigurationSelectionDialog_11); 
					isValid = false;
				}
			}
		}

		if (isValid) {
			if (action == EPostLaunchAction.DELAY) {
				isValid = (actionParam instanceof Integer) && ((Integer)actionParam > 0);
				setErrorMessage(isValid ? null : LaunchMessages.MultiLaunchConfigurationSelectionDialog_10); 
			}
		}
		
		if (ok_button != null) 
			ok_button.setEnabled(isValid);
	}

	public void setInitialSelection(LaunchElement el) {
		action = el.action;
		actionParam = el.actionParam;
		isDefaultMode = el.mode.equals(MultiLaunchConfigurationDelegate.DEFAULT_MODE);
	    fInitialSelection = new StructuredSelection(el.data);   
	    fSelection = fInitialSelection;
    }
}
