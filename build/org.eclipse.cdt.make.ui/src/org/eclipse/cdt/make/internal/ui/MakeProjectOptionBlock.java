/*
 * Created on 5-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui;

import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.ui.dialogs.DiscoveryOptionsBlock;
import org.eclipse.cdt.make.ui.dialogs.SettingsBlock;
import org.eclipse.cdt.ui.dialogs.BinaryParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;

public class MakeProjectOptionBlock extends TabFolderOptionBlock {

	public MakeProjectOptionBlock() {
		super(true);
	}
	
	public MakeProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
	}

	protected void addTabs() {
		addTab(new SettingsBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new ErrorParserBlock(MakeCorePlugin.getDefault().getPluginPreferences()));
		addTab(new BinaryParserBlock());
		addTab(new DiscoveryOptionsBlock());
	}

}
