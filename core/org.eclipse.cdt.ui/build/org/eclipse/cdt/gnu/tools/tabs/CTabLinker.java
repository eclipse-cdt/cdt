/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools.tabs;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.cdt.core.builder.model.CBuildVariable;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.cdt.core.builder.model.ICBuildVariable;
import org.eclipse.cdt.core.builder.model.ICPosixBuildConstants;
import org.eclipse.cdt.core.builder.model.ICToolchain;
import org.eclipse.cdt.ui.builder.ACToolTab;
import org.eclipse.cdt.ui.builder.internal.CBuildVariableDialog;
import org.eclipse.cdt.ui.builder.internal.CLibFileDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * The control for editing and viewing linker options.
 */
public class CTabLinker extends ACToolTab {
	
	private Text fOutputFileName;
	private Text fCmdLine;
	private Button btnStatic;
	private Button btnSharedObject;
	private Button btnStripSybmols;
	private List fLibPaths;
	private List fLibs;
	private List fLibFileList; // Used for validation.
	private Button btnAddLib;	// this set of buttons controls the lib of libraries
	private Button btnRemoveLib;
	private Button btnMoveUp;
	private Button btnMoveDown;
	private Button btnRemove;	// set of buttons for the library paths

	class addLibraryPath extends SelectionAdapter 
	{
		public void widgetSelected(SelectionEvent sel) 
		{
			CBuildVariableDialog selectFolder = new CBuildVariableDialog(getShell(), ("Select_Library_Path_1")); //$NON-NLS-1$
			if (selectFolder.open() == selectFolder.OK) {
				ICBuildVariable result = selectFolder.getSelection();
				fLibPaths.add(result.toString());
			}
		}
	};

	class addLibraryFile extends SelectionAdapter 
	{
		public void widgetSelected(SelectionEvent sel) 
		{
			
			// create list of item to send to the library picker
			ICBuildVariable[] libPaths = new CBuildVariable[fLibPaths.getItemCount()];
			for (int nIndex = 0; nIndex < fLibPaths.getItemCount(); nIndex++) {
				String item = fLibPaths.getItem(nIndex);
				int nCloseBracePos = item.indexOf("]");//$NON-NLS-1$
				String name = item.substring(1, nCloseBracePos);
				String val = item.substring(nCloseBracePos + 2);
				libPaths[nIndex] = new CBuildVariable(name, val, "");// fLibPaths.getItem(nIndex));
			}
			
			fLibFileList = fLibs;				
			CLibFileDialog selectFile = new CLibFileDialog(getShell(),
														 (ICToolchain) null /* getToolchain() */,
														 libPaths,
														 fLibFileList);
			if (selectFile.open() == selectFile.OK) {
				fLibs.add(selectFile.getSelection().getName());
			}
			
			// Update the move down button.
			if (fLibs.getItemCount() > 1) {
				btnMoveDown.setEnabled(true);
			}
		}
	};
			
	class delLibraryPath extends SelectionAdapter 
	{
		public void widgetSelected(SelectionEvent sel) 
		{
			int[] nSelItems = fLibPaths.getSelectionIndices();
			
			if (nSelItems != null) {						
				fLibPaths.remove(nSelItems);
			}
		}
	};

	class delLibraryFile extends SelectionAdapter 
	{
		public void widgetSelected(SelectionEvent sel) 
		{
			int[] nSelItems = fLibs.getSelectionIndices();
			
			if (nSelItems != null) {						
				fLibs.remove(nSelItems);
			}
			
			// Added to make sure that if nothing is selected the move up/down buttons are appropriately enabled or 
			// disabled.
			nSelItems = fLibs.getSelectionIndices();
			if ((nSelItems != null) && (nSelItems.length == 0)) {
				btnMoveUp.setEnabled(false);
				btnMoveDown.setEnabled(false);
			}
		}
	};

	class moveLibs extends SelectionAdapter
	{
		int m_nDelta;
		
		public moveLibs(int delta) {
			Assert.isTrue(delta == -1 || delta == 1, ("Delta_must_be_1_or_-1_2")); //$NON-NLS-1$
			m_nDelta = delta;
		}
		
		private boolean CanMove(int selections, int delta ) {
			boolean canMove = true;
			
			// First of all check if we can even move the selected lib names up or down.
			if ((selections == 0) && (delta == -1)) {
				btnMoveUp.setEnabled(false);
				
				if (fLibs.getItemCount() > 1) {
					btnMoveDown.setEnabled(true);
				}
				canMove = false;
			}
			if ((selections == (fLibs.getItemCount() - 1)) && (delta == 1)) {
				btnMoveDown.setEnabled(false);
				
				if (fLibs.getItemCount() > 1) {
					btnMoveUp.setEnabled(true);
				}
				canMove = false;
			}
			
			return canMove;
		}
		
		public void widgetSelected(SelectionEvent sel) {
			
			int[] selections = fLibs.getSelectionIndices();
			if ((selections != null) && (m_nDelta != 0)) {
				int nStart = (m_nDelta < 0) ? 0 : selections.length - 1;
				int nEnd = (m_nDelta < 0) ? selections.length : -1;
				String strTemp;

				boolean bCanMove = CanMove(selections[nStart], m_nDelta);
				// this loop moves group of items based on the delta and moves them
				for (int nIndex = nStart ; nIndex != nEnd && bCanMove; nIndex += (m_nDelta * -1)) {										
					// remove and re-add the list box
					strTemp = fLibs.getItem(selections[nIndex]);
					fLibs.remove(selections[nIndex]);
					fLibs.add(strTemp, selections[nIndex] + m_nDelta);
					// adjust the index in the selection list so we can re-highlight the block
					selections[nIndex] = selections[nIndex] + m_nDelta;
					
					bCanMove = CanMove(selections[nIndex], m_nDelta);
				}
				fLibs.select(selections);
			}
		}
	};
	
	
	// decl these after defining the vars
	addLibraryPath m_addLibraryPath = new addLibraryPath();
	delLibraryPath m_delLibraryPath = new delLibraryPath();
	addLibraryFile m_addLibraryFile = new addLibraryFile();
	delLibraryFile m_delLibraryFile	= new delLibraryFile();
			
	public CTabLinker() {
		super();
	}

	class TextWidth {
		private int width_ = 0;
		public TextWidth(Composite composite, String[] labels) {
			GC gc = new GC(composite);		
			gc.setFont(composite.getFont());
			for (int i = 0; i < labels.length; i++) {
				int newWidth = gc.textExtent(labels[i]).x + 10; //$NON-NLS-1$
				if (newWidth > width_) {
					width_ = newWidth;
				}
			}
		}
		public int getWidth() {
			return width_;
		}
	}
	
	/**
	 * Helper function for creating a grid layout to spec.
	 * 
	 * @param columns		Number of columns in layout.
	 * @param equalWidth	True if columns are of equal width.
	 * @param marginHeight	Margin height for layout.
	 * @param marginWidth	Margin width for layout.
	 * 
	 * @return Newly created GridLayout with the specified properties.
	 */
	private GridLayout createGridLayout(int columns, boolean equalWidth, int marginHeight, int marginWidth) {
		GridLayout layout = new GridLayout(columns, equalWidth);
		layout.marginHeight = marginHeight;
		layout.marginWidth 	= marginWidth;
		return layout;
	}
	
	/** 
	 * helper routine for putting the values in the list box
	 * 
	 * @param lstTarget	the control to fill with values
	 * @param data 		linker properties data 
	 * @param data			parameter name to fetch from data
	 * 
	 */
	private void setList(org.eclipse.swt.widgets.List target, java.util.List list)
	{
		target.removeAll();
		if (null != list) {
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				 target.add((String) iter.next());
			}
		}
	}
		
	/**
	 * helper routine for getting data from a list control
	 * @param control contol to get data from
	 */		
	private java.util.List getList(List control) 
	{
		String[] items = fLibs.getItems();
		Vector vec = new Vector();
		for (int i = 0; i < items.length; i++) {
			vec.add(items[i]);
		}
		return vec;
	}

	/**
	 * helper routine for getting data from a text control
	 * @param control contol to get data from
	 */		
	private String readText(Text control) 
	{
		String str = control.getText();
		if (str != null) {
			str.trim();	
		} else {
			str = "";
		}
		return str;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		final String[]	BUTTON_LABELS = {	
			("Add..._5"), //$NON-NLS-1$
			("Remove_7"), //$NON-NLS-1$
			("Add_12"), //$NON-NLS-1$
			("Remove_13"), //$NON-NLS-1$
			("Move_Up_14"), //$NON-NLS-1$
			("Move_Down_15") //$NON-NLS-1$
		};

		Composite ths = new Composite(parent, SWT.NONE);
		int BUTTON_WIDTH = new TextWidth(ths, BUTTON_LABELS).getWidth();
		
		// Panel

		ths.setLayout(new GridLayout(2, true));
		ths.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH));
		fLibFileList = new List(parent, SWT.NONE);
							
		// --------------------------------------------------
		// Left column
		// --------------------------------------------------

		Composite cmpLeft = new Composite(ths, SWT.NONE);

		cmpLeft.setLayout(createGridLayout(1, true, 2, 2));
		cmpLeft.setLayoutData(new GridData(GridData.FILL_BOTH));

		// row 1
		new Label(cmpLeft, SWT.NULL).setText(("Output_File_Name_3")); //$NON-NLS-1$

		// row 2
		fOutputFileName = new Text(cmpLeft, SWT.LEFT | SWT.BORDER);
		fOutputFileName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

		// row 3
		btnStatic = new Button(cmpLeft, SWT.CHECK | SWT.LEFT);
		btnStatic.setText(("Perform_Static_Linking_6")); //$NON-NLS-1$
		btnStatic.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));

		// row 4
		btnSharedObject = new Button(cmpLeft, SWT.CHECK | SWT.LEFT);
		btnSharedObject.setText(("Create_Shared_Object_8")); //$NON-NLS-1$
		btnSharedObject.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

		// row 5			
		btnStripSybmols = new Button(cmpLeft, SWT.CHECK | SWT.LEFT);
		btnStripSybmols.setText(("Strip_Symbols_9")); //$NON-NLS-1$
		btnStripSybmols.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

		// row 6
		new Label(cmpLeft, SWT.NULL).setText(("Additional_Options_11")); //$NON-NLS-1$

		// row 7
		fCmdLine = new Text(cmpLeft, SWT.LEFT | SWT.BORDER);
		fCmdLine.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));


		// --------------------------------------------------
		// Right column
		// --------------------------------------------------

		Composite cmpRight = new Composite(ths, SWT.NONE);

		cmpRight.setLayout(createGridLayout(1, true, 2, 2));
		cmpRight.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Lib paths

		Group grpLibPaths = new Group(cmpRight, SWT.SHADOW_NONE);
		grpLibPaths.setLayout(createGridLayout(1, true, 2, 2));
		grpLibPaths.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpLibPaths.setText(("Library_Paths__4")); //$NON-NLS-1$

		Composite cmpLibPaths = new Composite(grpLibPaths, SWT.NONE);
		cmpLibPaths.setLayout(createGridLayout(1, true, 2, 2));
		cmpLibPaths.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpPathCtrls = new Composite(cmpLibPaths, SWT.NONE);
		cmpPathCtrls.setLayout(createGridLayout(2, false, 2, 2));
		cmpPathCtrls.setLayoutData(new GridData(GridData.FILL_BOTH));

		fLibPaths = new List(cmpPathCtrls, SWT.LEFT | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL); 
		fLibPaths.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpPathButtons = new Composite(cmpPathCtrls, SWT.NONE);
		cmpPathButtons.setLayout(createGridLayout(1, true, 2, 2));
		GridData gdPathBtns = new GridData();
		gdPathBtns.widthHint = BUTTON_WIDTH;
		cmpPathButtons.setLayoutData(gdPathBtns);

		Button btnNew = new Button(cmpPathButtons, SWT.NULL); 
		btnNew.setText(("Add..._5")); //$NON-NLS-1$
		btnNew.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnRemove = new Button(cmpPathButtons, SWT.NULL);
		btnRemove.setText(("Remove_7")); //$NON-NLS-1$
		btnRemove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Libs

		Group grpLibs = new Group(cmpRight, SWT.SHADOW_NONE);
		grpLibs.setLayout(createGridLayout(1, true, 2, 2));
		grpLibs.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpLibs.setText(("Libraries_10")); //$NON-NLS-1$

		Composite cmpLibs = new Composite(grpLibs, SWT.NONE);
		cmpLibs.setLayout(createGridLayout(1, true, 2, 2));
		cmpLibs.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpLibCtrls = new Composite(cmpLibs, SWT.NONE);
		cmpLibCtrls.setLayout(createGridLayout(2, false, 2, 2));
		cmpLibCtrls.setLayoutData(new GridData(GridData.FILL_BOTH));

		fLibs = new List(cmpLibCtrls, SWT.LEFT | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		fLibs.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpLibButtons = new Composite(cmpLibCtrls, SWT.NONE);
		cmpLibButtons.setLayout(createGridLayout(1, true, 2, 2));
		GridData gdLibBtns = new GridData();
		gdLibBtns.widthHint = BUTTON_WIDTH;
		cmpLibButtons.setLayoutData(gdLibBtns);

		btnAddLib = new Button(cmpLibButtons, SWT.NULL);
		btnAddLib.setText(("Add_12")); //$NON-NLS-1$
		btnAddLib.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnRemoveLib = new Button(cmpLibButtons, SWT.NULL);
		btnRemoveLib.setText(("Remove_13")); //$NON-NLS-1$
		btnRemoveLib.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnMoveUp = new Button(cmpLibButtons, SWT.NULL);
		btnMoveUp.setText(("Move_Up_14")); //$NON-NLS-1$
		btnMoveUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btnMoveDown = new Button(cmpLibButtons, SWT.NULL);
		btnMoveDown.setText(("Move_Down_15")); //$NON-NLS-1$
		btnMoveDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*
		 * create the listener events the controls
		 * 
		 */
		btnNew.addSelectionListener(m_addLibraryPath);
		btnRemove.addSelectionListener(m_delLibraryPath);
		
		fLibPaths.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) {
					int[] selection = fLibPaths.getSelectionIndices();
					btnRemove.setEnabled(selection.length > 0);						
				}

			}
		);
		
//			fLibPaths.addKeyListener(new KeyAdapter()
//				{
//					public void keyPressed(KeyEvent key) {}
//					public void keyReleased(KeyEvent key) {}
//					public void keyTyped(KeyEvent key)
//					{
//						if (key.keyCode == SWT.DEL) {
//							m_delLibraryPath.widgetSelected(null);
//						}
//						else if (key.keyCode == SWT.INSERT) {
//							m_addLibraryPath.widgetSelected(null);
//						}
//					}
//					
//				}
//			);
		
		btnAddLib.addSelectionListener(m_addLibraryFile);
		btnRemoveLib.addSelectionListener(m_delLibraryFile);
		
		fLibs.addFocusListener(new FocusAdapter() 
			{
				public void focusLost(FocusEvent e) {
					// btnMoveDown.setEnabled(false);
					// btnMoveUp.setEnabled(false);
					super.focusLost(e);
				}
			}
		);
		
		fLibs.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) {
					
					int[] selections = fLibs.getSelectionIndices();
					if (selections.length > 0) {
						// see if we should enable the up button
						btnMoveUp.setEnabled(selections[0] > 0);
						btnMoveDown.setEnabled(selections[selections.length - 1] < fLibs.getItemCount() - 1);
						btnRemoveLib.setEnabled(true);
					}
					else {
						// don't enable the buttons if the user hasn't made any selections
						btnMoveUp.setEnabled(false);
						btnMoveDown.setEnabled(false);
						btnRemoveLib.setEnabled(false);
					}
					
					super.widgetSelected(e);
				}
			}
		);
		
		btnMoveDown.addSelectionListener(new moveLibs(1));
		btnMoveUp.addSelectionListener(new moveLibs(-1));
		
		// the user hasn't made selections yet, so turn these buttons off
		btnMoveDown.setEnabled(false);
		btnMoveUp.setEnabled(false);
		btnRemoveLib.setEnabled(false);
		btnRemove.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#initializeFrom(ICBuildConfig)
	 */
	public void initializeFrom(ICBuildConfig config) {
		String output = "";
		String cmdLine = "";
		boolean isStatic = false;
		boolean isShared = false;
		boolean isStrip = false;
		java.util.List libs = null;
		java.util.List libPaths = null;

		try {
			output = config.getAttribute(ICPosixBuildConstants.LD_OUTPUT, "");
			cmdLine = config.getAttribute(ICPosixBuildConstants.LD_USER_ARGS, "");
			isStatic = config.getAttribute(ICPosixBuildConstants.LD_LINK_STATIC, false);
			isShared = config.getAttribute(ICPosixBuildConstants.LD_LINK_AS_SHARED, false);
			isStrip = config.getAttribute(ICPosixBuildConstants.LD_STRIP, false);
			libs = config.getAttribute(ICPosixBuildConstants.LD_LIBS, libs);
			libPaths = config.getAttribute(ICPosixBuildConstants.LD_LIBPATHS, libPaths);
		} catch (CoreException e) {
		}

		fOutputFileName.setText(output);
		fCmdLine.setText(cmdLine);
		btnStatic.setSelection(isStatic);
		btnSharedObject.setSelection(isShared);
		btnStripSybmols.setSelection(isStrip);
		setList(fLibs, libs);
		setList(fLibs, libPaths);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#performApply(ICBuildConfigWorkingCopy)
	 */
	public void performApply(ICBuildConfigWorkingCopy config) {
		config.setAttribute(ICPosixBuildConstants.LD_OUTPUT, readText(fOutputFileName));
		config.setAttribute(ICPosixBuildConstants.LD_USER_ARGS, readText(fCmdLine));
		config.setAttribute(ICPosixBuildConstants.LD_LIBS, getList(fLibs));
		config.setAttribute(ICPosixBuildConstants.LD_LIBPATHS, getList(fLibPaths));
		config.setAttribute(ICPosixBuildConstants.LD_LINK_STATIC, btnStatic.getSelection());
		config.setAttribute(ICPosixBuildConstants.LD_STRIP, btnStripSybmols.getSelection());

		if (btnSharedObject.getSelection()) {
			config.setAttribute(ICPosixBuildConstants.LD_LINK_AS_SHARED, true);
			config.setAttribute(ICPosixBuildConstants.LD_LINK_AS_ARCHIVE, false);
			config.setAttribute(ICPosixBuildConstants.LD_LINK_AS_PROGRAM, false);
		} else {
			config.setAttribute(ICPosixBuildConstants.LD_LINK_AS_SHARED, false);
			config.setAttribute(ICPosixBuildConstants.LD_LINK_AS_ARCHIVE, false);
			config.setAttribute(ICPosixBuildConstants.LD_LINK_AS_PROGRAM, true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#setDefaults(ICBuildConfigWorkingCopy)
	 */
	public void setDefaults(ICBuildConfigWorkingCopy configuration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#isValid(ICBuildConfigWorkingCopy)
	 */
	public boolean isValid(ICBuildConfigWorkingCopy config) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.builder.ICToolTab#getName()
	 */
	public String getName() {
		return "Linker";
	}
}