/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Aaron Luchko, aluchko@redhat.com - 105926 [Formatter] Exporting Unnamed profile fails silently
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.Profile;

/**
 * The code formatter preference page. 
 */
public class CodeFormatterConfigurationBlock extends ProfileConfigurationBlock {
    
    private static final String FORMATTER_DIALOG_PREFERENCE_KEY= "formatter_page"; //$NON-NLS-1$

    private static final String DIALOGSTORE_LASTSAVELOADPATH= CUIPlugin.PLUGIN_ID + ".codeformatter.savepath"; //$NON-NLS-1$

	private class PreviewController implements Observer {

		public PreviewController(ProfileManager profileManager) {
			profileManager.addObserver(this);
			fCustomCodeFormatterBlock.addObserver(this);
			fCodeStylePreview.setWorkingValues(profileManager.getSelected().getSettings());
			fCodeStylePreview.update();
		}
		
		public void update(Observable o, Object arg) {
			if (o == fCustomCodeFormatterBlock) {
				fCodeStylePreview.setFormatterId((String)arg);
				fCodeStylePreview.update();
				return;
			}
			final int value= ((Integer)arg).intValue();
			switch (value) {
				case ProfileManager.PROFILE_CREATED_EVENT:
				case ProfileManager.PROFILE_DELETED_EVENT:
				case ProfileManager.SELECTION_CHANGED_EVENT:
				case ProfileManager.SETTINGS_CHANGED_EVENT:
					fCodeStylePreview.setWorkingValues(((ProfileManager)o).getSelected().getSettings());
					fCodeStylePreview.update();
			}
		}
	}
	
	/**
	 * Some C++ source code used for preview.
	 */
	private final static String PREVIEW=
		"/*\n* " + //$NON-NLS-1$
		FormatterMessages.CodingStyleConfigurationBlock_preview_title + 
		"\n*/\n" + //$NON-NLS-1$
		"#include <math.h>\n\n" + //$NON-NLS-1$
		"class Point {" +  //$NON-NLS-1$
		"public:" +  //$NON-NLS-1$
		"Point(double xc, double yc) : x(xc), y(yc) {}" + //$NON-NLS-1$ 
		"double distance(const Point& other) const;" + //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"double x;" +  //$NON-NLS-1$
		"double y;" +  //$NON-NLS-1$
		"};" +  //$NON-NLS-1$
		"\n\n" +  //$NON-NLS-1$
		"double Point::distance(const Point& other) const {" + //$NON-NLS-1$
		"double dx = x - other.x;" + //$NON-NLS-1$
		"double dy = y - other.y;" + //$NON-NLS-1$
		"return sqrt(dx * dx + dy * dy);" + //$NON-NLS-1$
		"}"; //$NON-NLS-1$

	/**
	 * The CPreview.
	 */
	protected TranslationUnitPreview fCodeStylePreview;
	
	protected CustomCodeFormatterBlock fCustomCodeFormatterBlock;
	/**
	 * Create a new <code>CodeFormatterConfigurationBlock</code>.
	 */
	public CodeFormatterConfigurationBlock(IProject project, PreferencesAccess access) {
		super(project, access, DIALOGSTORE_LASTSAVELOADPATH);
		if (project == null) {
			//TLETODO formatter customizable on project level?
			fCustomCodeFormatterBlock= new CustomCodeFormatterBlock(access);
		}
	}

	protected IProfileVersioner createProfileVersioner() {
	    return new ProfileVersioner();
    }
	
	protected ProfileStore createProfileStore(IProfileVersioner versioner) {
	    return new FormatterProfileStore(versioner);
    }
	
	protected ProfileManager createProfileManager(List profiles, IScopeContext context, PreferencesAccess access, IProfileVersioner profileVersioner) {
	    return new FormatterProfileManager(profiles, context, access, profileVersioner);
    }
	
	
	protected void configurePreview(Composite composite, int numColumns, ProfileManager profileManager) {
		if (fCustomCodeFormatterBlock != null) {
			fCustomCodeFormatterBlock.createContents(composite);
		}
		
		createLabel(composite, FormatterMessages.CodingStyleConfigurationBlock_preview_label_text, numColumns);
		TranslationUnitPreview result= new TranslationUnitPreview(profileManager.getSelected().getSettings(), composite);
		result.setFormatterId(fCustomCodeFormatterBlock.getFormatterId());
        result.setPreviewText(PREVIEW);
        fCodeStylePreview= result;

		final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = numColumns;
		gd.verticalSpan= 7;
		gd.widthHint = 0;
		gd.heightHint = 0;
		fCodeStylePreview.getControl().setLayoutData(gd);
		
		new PreviewController(profileManager);
	}

    
    protected ModifyDialog createModifyDialog(Shell shell, Profile profile, ProfileManager profileManager, ProfileStore profileStore, boolean newProfile) {
        return new FormatterModifyDialog(shell, profile, profileManager, profileStore, newProfile, FORMATTER_DIALOG_PREFERENCE_KEY, DIALOGSTORE_LASTSAVELOADPATH);
    }

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.formatter.ProfileConfigurationBlock#performApply()
	 */
	public void performApply() {
		if (fCustomCodeFormatterBlock != null) {
			fCustomCodeFormatterBlock.performOk();
		}
		super.performApply();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.formatter.ProfileConfigurationBlock#performDefaults()
	 */
	public void performDefaults() {
		if (fCustomCodeFormatterBlock != null) {
			fCustomCodeFormatterBlock.performDefaults();
		}
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.formatter.ProfileConfigurationBlock#performOk()
	 */
	public boolean performOk() {
		if (fCustomCodeFormatterBlock != null) {
			fCustomCodeFormatterBlock.performOk();
		}
		return super.performOk();
	}
    
}
