package org.eclipse.cdt.ui.wizards;

import org.eclipse.cdt.core.build.standard.StandardBuildManager;
import org.eclipse.cdt.core.resources.IStandardBuildInfo;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

public class BuildPathInfoBlock implements IWizardTab {
	private static final String PREFIX = "BuildPathInfoBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String PATHS = PREFIX + ".paths"; //$NON-NLS-1$
	private static final String SYMBOLS = PREFIX + ".symbols"; //$NON-NLS-1$
	private static final String BROWSE = PREFIX + ".browse"; //$NON-NLS-1$
	private static final String PATH_TITLE = BROWSE + ".path"; //$NON-NLS-1$
	private static final String PATH_LABEL = BROWSE + ".path.label"; //$NON-NLS-1$
	private static final String SYMBOL_TITLE = BROWSE + ".symbol"; //$NON-NLS-1$
	private static final String SYMBOL_LABEL = BROWSE + ".symbol.label"; //$NON-NLS-1$
	private static final String NEW = "BuildPropertyCommon.label.new"; //$NON-NLS-1$
	private static final String REMOVE = "BuildPropertyCommon.label.remove"; //$NON-NLS-1$
	private static final String UP = "BuildPropertyCommon.label.up"; //$NON-NLS-1$
	private static final String DOWN = "BuildPropertyCommon.label.down"; //$NON-NLS-1$

	private IProject project;
	private IValidation page;
	private List pathList;
	private List symbolList;
	private Composite pathButtonComp;
	private Button addPath;
	private Button removePath;
	private Button pathUp;
	private Button pathDown;
	private Composite symbolButtonComp;
	private Button addSymbol;
	private Button removeSymbol;
	private Button symbolUp;
	private Button symbolDown;
	private Shell shell;
	

	/**
	 * @param valid
	 */
	public BuildPathInfoBlock(IValidation valid) {
		this(valid, null);
	}

	/**
	 * @param page
	 * @param project
	 */
	public BuildPathInfoBlock(IValidation valid, IProject project) {
		this.page = valid;
		this.project = project;
	}

	/**
	 * @param composite
	 */
	private void createPathListButtons(Composite parent) {
		// Create a composite for the buttons
		pathButtonComp = ControlFactory.createComposite(parent, 1);
		GridData gd = new GridData(GridData.FILL_BOTH);
		pathButtonComp.setFont(parent.getFont());
		
		// Add the buttons
		addPath = ControlFactory.createPushButton(pathButtonComp, CUIPlugin.getResourceString(NEW));
		addPath.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleAddPath();
			}
		});
		addPath.setEnabled(true);
		addPath.setFont(parent.getFont());
		addPath.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(addPath);
		
		removePath = ControlFactory.createPushButton(pathButtonComp, CUIPlugin.getResourceString(REMOVE));
		removePath.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleRemovePath();
			}
		});
		removePath.setFont(parent.getFont());
		removePath.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(removePath);
		
		pathUp = ControlFactory.createPushButton(pathButtonComp, CUIPlugin.getResourceString(UP));
		pathUp.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handlePathUp();
			}
		});
		pathUp.setFont(parent.getFont());
		pathUp.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(pathUp);
		
		pathDown = ControlFactory.createPushButton(pathButtonComp, CUIPlugin.getResourceString(DOWN));
		pathDown.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handlePathDown();
			}
		});
		pathDown.setFont(parent.getFont());
		pathDown.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(pathDown);
		
	}

	/**
	 * @param composite
	 */
	private void createPathListControl(Composite parent, int numColumns) {
		// Create the list
		pathList = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		pathList.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				enablePathButtons();
			}
		});
		pathList.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				editPathListItem();			
			}

			public void mouseDown(MouseEvent e) {
				// Handled by the selection listener
			}

			public void mouseUp(MouseEvent e) {
				// Handled by the selection listener
			}
		
		});
		
		// Make it occupy the first 2 columns
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = numColumns - 1;
		pathList.setLayoutData(gd);
		pathList.setFont(parent.getFont());
	}

	/**
	 * @param composite
	 */
	private void createSymbolListButtons(Composite parent) {
		// Create a composite for the buttons
		symbolButtonComp = ControlFactory.createComposite(parent, 1);
		GridData gd = new GridData(GridData.FILL_BOTH);
		symbolButtonComp.setFont(parent.getFont());
		
		// Add the buttons
		addSymbol = ControlFactory.createPushButton(symbolButtonComp, CUIPlugin.getResourceString(NEW));
		addSymbol.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleAddSymbol();
			}
		});
		addSymbol.setEnabled(true);
		addSymbol.setFont(parent.getFont());
		addSymbol.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(addSymbol);
		
		removeSymbol = ControlFactory.createPushButton(symbolButtonComp, CUIPlugin.getResourceString(REMOVE));
		removeSymbol.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleRemoveSymbol();
			}
		});
		removeSymbol.setFont(parent.getFont());
		removeSymbol.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(removeSymbol);

		symbolUp = ControlFactory.createPushButton(symbolButtonComp, CUIPlugin.getResourceString(UP));
		symbolUp.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleSymbolUp();
			}
		});
		symbolUp.setFont(parent.getFont());
		symbolUp.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(symbolUp);

		symbolDown = ControlFactory.createPushButton(symbolButtonComp, CUIPlugin.getResourceString(DOWN));
		symbolDown.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleSymbolDown();
			}
		});
		symbolDown.setFont(parent.getFont());
		symbolDown.setLayoutData(new GridData());
		SWTUtil.setButtonDimensionHint(symbolDown);
	}

	/**
	 * @param composite
	 */
	private void createSymbolListControl(Composite parent, int numColumns) {
		// Create the list
		symbolList = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		symbolList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableSymbolButtons();
			}
		});
		symbolList.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				editSymbolListItem();
			}

			public void mouseDown(MouseEvent e) {
				// Handled by the selection event
			}

			public void mouseUp(MouseEvent e) {
				// Handled by the selection event
			}
		});
		
		// Make it occupy the first n-1 columns
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = numColumns - 1;
		symbolList.setLayoutData(gd);
		symbolList.setFont(parent.getFont());
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#doRun(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doRun(IProject project, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (project != null) {
			try {
				// Store the paths and symbols 
				monitor.beginTask("Setting Include Paths", 1);
				StandardBuildManager.setIncludePaths(project, getPathListContents());
	
				monitor.beginTask("Setting Defined Symbols", 1);
				StandardBuildManager.setPreprocessorSymbols(project, getSymbolListContents());
			
				StandardBuildManager.saveBuildInfo(project);
			} catch (CoreException e) {
				// Should probably tell someone
			}
		}
	}

	/*
	 * Double-click handler to allow edit of path information
	 */
	protected void editPathListItem() {
		// Edit the selection index
		int index = pathList.getSelectionIndex();
		if (index != -1) {
			String selItem = pathList.getItem(index);
			if (selItem != null) {
				InputDialog dialog = new InputDialog(shell, CUIPlugin.getResourceString(PATH_TITLE), CUIPlugin.getResourceString(PATH_LABEL), selItem, null);
				String newItem = null;
				if (dialog.open() == InputDialog.OK) {
					newItem = dialog.getValue();
					if (newItem != null && !newItem.equals(selItem)) {
						pathList.setItem(index, newItem);
					}
				}
			}
		}
	}

	/*
	 * Double-click handler to allow edit of symbol information
	 */
	protected void editSymbolListItem() {
		// Edit the selection index
		int index = symbolList.getSelectionIndex();
		if (index != -1) {
			String selItem = symbolList.getItem(index);
			if (selItem != null) {
				InputDialog dialog = new InputDialog(shell, CUIPlugin.getResourceString(SYMBOL_TITLE), CUIPlugin.getResourceString(SYMBOL_LABEL), selItem, null);
				String newItem = null;
				if (dialog.open() == InputDialog.OK) {
					newItem = dialog.getValue();
					if (newItem != null && !newItem.equals(selItem)) {
						symbolList.setItem(index, newItem);
					}
				}
			}
		}
	}

	/*
	 * Enables the buttons on the path control if the right conditions are met
	 */
	private void enablePathButtons() {
		// Enable the remove button if there is at least 1 item in the list
		int items = pathList.getItemCount();
		if (items > 0){
			removePath.setEnabled(true);
			// Enable the up/down buttons depending on what item is selected
			int index = pathList.getSelectionIndex();
			pathUp.setEnabled(items > 1 && index > 0);
			pathDown.setEnabled(items > 1 && index < (items - 1));
		} else {
			removePath.setEnabled(false);
			pathUp.setEnabled(false);
			pathDown.setEnabled(false);
		}
	}
	
	private void enableSymbolButtons() {
		// Enable the remove button if there is at least 1 item in the list
		int items = symbolList.getItemCount();
		if (items > 0){
			removeSymbol.setEnabled(true);
			// Enable the up/down buttons depending on what item is selected
			int index = symbolList.getSelectionIndex();
			symbolUp.setEnabled(items > 1 && index > 0);
			symbolDown.setEnabled(items > 1 && index < (items - 1));
		} else {
			removeSymbol.setEnabled(false);
			symbolUp.setEnabled(false);
			symbolDown.setEnabled(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public Composite getControl(Composite parent) {
		this.shell = parent.getShell();

		// Create the composite control for the tab
		int tabColumns = 3;
		Font font = parent.getFont();
		Composite composite = ControlFactory.createComposite(parent, tabColumns);
		composite.setFont(font);
		GridData gd;
		
		// Create a label for the include paths control
		Label paths = ControlFactory.createLabel(composite, CUIPlugin.getResourceString(PATHS));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = tabColumns;
		paths.setLayoutData(gd);
		paths.setFont(font);
		
		//Create the list and button controls
		createPathListControl(composite, tabColumns);
		createPathListButtons(composite);
		enablePathButtons();

		// Create a label for the symbols control
		Label symbols = ControlFactory.createLabel(composite, CUIPlugin.getResourceString(SYMBOLS));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = tabColumns;
		symbols.setLayoutData(gd);
		symbols.setFont(font);
		
		// Create list and button controls for symbols
		createSymbolListControl(composite, tabColumns);
		createSymbolListButtons(composite);
		enableSymbolButtons();

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getImage()
	 */
	public Image getImage() {
		// We don't decorate the tabs for the wizard or property page
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#getLabel()
	 */
	public String getLabel() {
		// Return the label defined in the CPluginProperties.resource file
		return CUIPlugin.getResourceString(LABEL);
	}

	/**
	 * @return
	 */
	private String[] getPathListContents() {
		return pathList.getItems();
	}

	/**
	 * @return
	 */
	private String[] getSymbolListContents() {
		return symbolList.getItems();
	}

	/**
	 * 
	 */
	protected void handleAddPath() {
		// Popup an entry dialog
		InputDialog dialog = new InputDialog(shell, CUIPlugin.getResourceString(PATH_TITLE), CUIPlugin.getResourceString(PATH_LABEL), "", null);
		String path = null;
		if (dialog.open() == InputDialog.OK) {
			path = dialog.getValue();
		}
		if (path != null && path.length() > 0) {
			pathList.add(path);
			pathList.select(pathList.getItemCount() - 1);
			enablePathButtons();
		}
	}

	/**
	 * 
	 */
	protected void handleAddSymbol() {
		// Popup an entry dialog
		InputDialog dialog = new InputDialog(shell, CUIPlugin.getResourceString(SYMBOL_TITLE), CUIPlugin.getResourceString(SYMBOL_LABEL), "", null);
		String symbol = null;
		if (dialog.open() == InputDialog.OK) {
			symbol = dialog.getValue();
		}
		if (symbol != null && symbol.length() > 0) {
			symbolList.add(symbol);
			symbolList.select(symbolList.getItemCount() - 1);
			enableSymbolButtons();
		}
	}

	/**
	 * 
	 */
	protected void handlePathDown() {
		// Get the selection index
		int index = pathList.getSelectionIndex();
		int items = pathList.getItemCount();
		if (index == -1 || index == items - 1) {
			return;		
		}
		// Swap the items in the list
		String selItem = pathList.getItem(index);
		pathList.remove(index);
		if (index + 1 == items) {
			pathList.add(selItem);
		} else {
			pathList.add(selItem, ++index);
		}
		
		// Keep the swapped item selected
		pathList.select(index);
		enablePathButtons();
	}

	/**
	 * 
	 */
	protected void handlePathUp() {
		// Get the selection index
		int index = pathList.getSelectionIndex();
		if (index == -1 || index == 0) {
			return;		
		}
		// Swap the items in the list
		String selItem = pathList.getItem(index);
		pathList.remove(index);
		pathList.add(selItem, --index);
		
		// Keep the index selected
		pathList.select(index);
		enablePathButtons();
	}

	/**
	 * 
	 */
	protected void handleRemovePath() {
		// Get the selection index
		int index = pathList.getSelectionIndex();
		if (index == -1) {
			return;		
		}
		
		// Remove the element at that index
		pathList.remove(index);
		index = index - 1 < 0 ? 0 : index -1;
		pathList.select(index);
		
		// Check if the buttons should still be enabled
		enablePathButtons();
	}

	/**
	 * 
	 */
	protected void handleRemoveSymbol() {
		// Get the selection index
		int index = symbolList.getSelectionIndex();
		if (index == -1) {
			return;
		}
		// Remove the item at that index
		symbolList.remove(index);
		index = index - 1 < 0 ? 0 : index -1;
		symbolList.select(index);
		// Check if the button state should be toggled
		enableSymbolButtons();
	}

	/**
	 * 
	 */
	protected void handleSymbolDown() {
		// Get the selection index
		int index = symbolList.getSelectionIndex();
		int items = symbolList.getItemCount();
		if (index == -1 || index == items - 1) {
			return;		
		}
		// Swap the items in the list
		String selItem = symbolList.getItem(index);
		symbolList.remove(index);
		if (index + 1 == items) {
			symbolList.add(selItem);
		} else {
			symbolList.add(selItem, ++index);
		}
		
		// Keep the swapped item selected
		symbolList.select(index);
		enableSymbolButtons();
	}

	/**
	 * 
	 */
	protected void handleSymbolUp() {
		// Get the selection index
		int index = symbolList.getSelectionIndex();
		if (index == -1 || index == 0) {
			return;		
		}
		// Swap the items in the list
		String selItem = symbolList.getItem(index);
		symbolList.remove(index);
		symbolList.add(selItem, --index);
		
		// Keep the index selected
		symbolList.select(index);
		enableSymbolButtons();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#isValid()
	 */
	public boolean isValid() {
		// Info on this page is not critical
		return true;
	}

	private void setPathListContents() {
		if (project != null) {
			try {
				IStandardBuildInfo info = StandardBuildManager.getBuildInfo(project);
				pathList.setItems(info.getIncludePaths());
			} catch (CoreException e) {
			}
		}
	}
	
	private void setSymbolListContents() {
		if (project != null) {
			try {
				IStandardBuildInfo info = StandardBuildManager.getBuildInfo(project);
				symbolList.setItems(info.getPreprocessorSymbols());
			} catch (CoreException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardTab#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		// Set the content from the project
		setPathListContents();
		pathList.select(0);
		enablePathButtons();
		setSymbolListContents();
		symbolList.select(0);
		enableSymbolButtons();
	}
}
