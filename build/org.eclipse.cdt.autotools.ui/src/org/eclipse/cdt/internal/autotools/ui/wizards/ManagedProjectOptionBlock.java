/*******************************************************************************
 * Copyright (c) 2002, 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Red Hat - Copy from CDT 3.1.2
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import org.eclipse.cdt.internal.autotools.ui.ErrorParserBlock;
import org.eclipse.cdt.ui.dialogs.BinaryParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.newui.CDTHelpContextIds;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;


@SuppressWarnings("deprecation")
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
		errParserBlock = new ErrorParserBlock(null);
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
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getErrorParserBlock().getControl(), CDTHelpContextIds.MAN_PROJ_ERROR_PARSER);

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
