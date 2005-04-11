/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.properties.MakePropertyPage;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.make.ui.dialogs.DiscoveryOptionsBlock;
import org.eclipse.cdt.make.ui.dialogs.SettingsBlock;
import org.eclipse.cdt.ui.dialogs.BinaryParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class MakeProjectOptionBlock extends TabFolderOptionBlock {
	private ICOptionContainer optionContainer;
	public MakeProjectOptionBlock() {
		super(true);
	}
	
	public MakeProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
		optionContainer = parent;
	}

	protected void addTabs() {
		addTab(new SettingsBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new MakeEnvironmentBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new ErrorParserBlock(MakeCorePlugin.getDefault().getPluginPreferences()));
		addTab(new BinaryParserBlock());
		addTab(new DiscoveryOptionsBlock());
	}

	public void setOptionContainer(ICOptionContainer parent) {
		super.setOptionContainer( parent );
		optionContainer = parent;
	}
	public Control createContents(Composite parent) {
		Control control = super.createContents( parent );
		
		List optionPages = getOptionPages();
		Iterator iter = optionPages.iterator();
		for( int i = 0; i < 4 && iter.hasNext(); i++ ){
			ICOptionPage page = (ICOptionPage) iter.next();
			IWorkbenchHelpSystem helpSystem = MakeUIPlugin.getDefault().getWorkbench().getHelpSystem();
			if( optionContainer != null && optionContainer instanceof MakePropertyPage )
				switch( i ){
					case 0 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_BUILDER_SETTINGS); break;
					case 1 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_ERROR_PARSER );    break;
					case 2 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_BINARY_PARSER );   break;
					case 3 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_DISCOVERY );       break;
				}
			else 
				switch( i ){
					case 0 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_BUILDER_SETTINGS);             break;
					case 1 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PREF_ERROR_PARSER );           break;
					case 2 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PREF_BINARY_PARSER );          break;
					case 3 : helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.SCANNER_CONFIG_DISCOVERY_OPTIONS ); break;
				}
		}

		return control;
	}
}
