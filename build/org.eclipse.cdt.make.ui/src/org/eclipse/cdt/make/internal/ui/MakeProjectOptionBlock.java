/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

/**
 * @deprecated as of CDT 4.0. This option block was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
public class MakeProjectOptionBlock extends TabFolderOptionBlock {
	private ICOptionContainer optionContainer;

	public MakeProjectOptionBlock() {
		super(true);
	}

	public MakeProjectOptionBlock(ICOptionContainer parent) {
		super(parent);
		optionContainer = parent;
	}

	@Override
	protected void addTabs() {
		addTab(new SettingsBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new MakeEnvironmentBlock(MakeCorePlugin.getDefault().getPluginPreferences(), MakeBuilder.BUILDER_ID));
		addTab(new ErrorParserBlock(MakeCorePlugin.getDefault().getPluginPreferences()));
		addTab(new BinaryParserBlock());
		addTab(new DiscoveryOptionsBlock());
	}

	@Override
	public void setOptionContainer(ICOptionContainer parent) {
		super.setOptionContainer(parent);
		optionContainer = parent;
	}

	@Override
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);

		List<ICOptionPage> optionPages = getOptionPages();
		Iterator<ICOptionPage> iter = optionPages.iterator();
		for (int i = 0; i < 4 && iter.hasNext(); i++) {
			ICOptionPage page = iter.next();
			IWorkbenchHelpSystem helpSystem = MakeUIPlugin.getDefault().getWorkbench().getHelpSystem();
			if (optionContainer != null && optionContainer instanceof MakePropertyPage)
				switch (i) {
				case 0:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_BUILDER_SETTINGS);
					break;
				case 1:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_ERROR_PARSER);
					break;
				case 2:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_BINARY_PARSER);
					break;
				case 3:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PROP_DISCOVERY);
					break;
				}
			else
				switch (i) {
				case 0:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_BUILDER_SETTINGS);
					break;
				case 1:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PREF_ERROR_PARSER);
					break;
				case 2:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.MAKE_PREF_BINARY_PARSER);
					break;
				case 3:
					helpSystem.setHelp(page.getControl(), IMakeHelpContextIds.SCANNER_CONFIG_DISCOVERY_OPTIONS);
					break;
				}
		}

		return control;
	}
}
