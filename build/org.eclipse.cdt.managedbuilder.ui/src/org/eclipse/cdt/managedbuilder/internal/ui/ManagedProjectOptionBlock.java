/**********************************************************************
 * Copyright (c) 2002,2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.ui.dialogs.BinaryParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.help.WorkbenchHelp;

public class ManagedProjectOptionBlock extends TabFolderOptionBlock {

	private ErrorParserBlock errParserBlock;
	private BinaryParserBlock binaryParserBlock;
	
	/**
	 * @param parent
	 */
	public ManagedProjectOptionBlock(ICOptionContainer parent) {
		super(parent, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
	 */
	protected void addTabs() {
		errParserBlock = new ErrorParserBlock();
		addTab(errParserBlock);
		addTab(binaryParserBlock = new BinaryParserBlock());
	}

	public BinaryParserBlock getBinaryParserBlock() {
		return binaryParserBlock;
	}
	
	public ErrorParserBlock getErrorParserBlock() {
		return errParserBlock;
	}
	
	public Control createContents(Composite parent) {
		Control control = super.createContents( parent );
		((GridLayout)((Composite)control).getLayout()).marginWidth = 1;
		GridData gd = new GridData(GridData.FILL_BOTH);
		((Composite)control).setLayoutData(gd);

		if (getErrorParserBlock()!= null)
			WorkbenchHelp.setHelp(getErrorParserBlock().getControl(), ManagedBuilderHelpContextIds.MAN_PROJ_ERROR_PARSER);

		return control;
	}	

	public void updateValues() {
		if (getErrorParserBlock()!= null) {
			getErrorParserBlock().updateValues();
		}
		if (getBinaryParserBlock()!= null) {
			// TODO
			//getBinaryParserBlock().updateValues();
		}
	}

	public void update() {
		super.update();
	}
}
