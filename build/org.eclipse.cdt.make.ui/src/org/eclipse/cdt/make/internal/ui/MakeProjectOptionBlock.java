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
import org.eclipse.cdt.make.ui.BuildPathInfoBlock;
import org.eclipse.cdt.make.ui.SettingsBlock;
import org.eclipse.cdt.ui.TabFolderOptionBlock;
import org.eclipse.cdt.ui.BinaryParserBlock;
import org.eclipse.cdt.ui.ICOptionContainer;
import org.eclipse.swt.widgets.TabItem;

public class MakeProjectOptionBlock extends TabFolderOptionBlock {

	public MakeProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
	}

	protected TabItem addTabs() {
		TabItem item = addTab(new SettingsBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new BinaryParserBlock(MakeCorePlugin.getDefault().getPluginPreferences()));
		addTab(new BuildPathInfoBlock());
		return item;
	}

}
