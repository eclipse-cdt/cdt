/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Aaron Luchko, aluchko@redhat.com - 105926 [Formatter] Exporting Unnamed profile fails silently
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.internal.ui.util.Messages;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.Profile;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.util.SWTUtil;

import org.osgi.service.prefs.BackingStoreException;


/**
 * The code formatter preference page. 
 */
public class CodeFormatterConfigurationBlock {
    
    private static final String DIALOGSTORE_LASTLOADPATH= CUIPlugin.PLUGIN_ID + ".codeformatter.loadpath"; //$NON-NLS-1$
	private static final String DIALOGSTORE_LASTSAVEPATH= CUIPlugin.PLUGIN_ID + ".codeformatter.savepath"; //$NON-NLS-1$
	
	private class StoreUpdater implements Observer {
		
		public StoreUpdater() {
			fProfileManager.addObserver(this);
		}

		public void update(Observable o, Object arg) {
			final int value= ((Integer)arg).intValue();
			switch (value) {
			case ProfileManager.PROFILE_DELETED_EVENT:
			case ProfileManager.PROFILE_RENAMED_EVENT:
			case ProfileManager.PROFILE_CREATED_EVENT:
			case ProfileManager.SETTINGS_CHANGED_EVENT:
				try {
					ProfileStore.writeProfiles(fProfileManager.getSortedProfiles(), fInstanceScope); // update profile store
					fProfileManager.commitChanges(fCurrContext); // update formatter settings with curently selected profile 
				} catch (CoreException x) {
					CUIPlugin.getDefault().log(x);
				}
				break;
			case ProfileManager.SELECTION_CHANGED_EVENT:
				fProfileManager.commitChanges(fCurrContext); // update formatter settings with curently selected profile
				break;
			}
		}
	}

	private class ProfileComboController implements Observer, SelectionListener {
		
		private final List fSortedProfiles;
		
		public ProfileComboController() {
			fSortedProfiles= fProfileManager.getSortedProfiles();
			fProfileCombo.addSelectionListener(this);
			fProfileManager.addObserver(this);
			updateProfiles();
			updateSelection();
		}
		
		public void widgetSelected(SelectionEvent e) {
			final int index= fProfileCombo.getSelectionIndex();
			fProfileManager.setSelected((Profile)fSortedProfiles.get(index));
		}

		public void widgetDefaultSelected(SelectionEvent e) {}

		public void update(Observable o, Object arg) {
			if (arg == null) return;
			final int value= ((Integer)arg).intValue();
			switch (value) {
				case ProfileManager.PROFILE_CREATED_EVENT:
				case ProfileManager.PROFILE_DELETED_EVENT:
				case ProfileManager.PROFILE_RENAMED_EVENT:
					updateProfiles();
					updateSelection();
					break;
				case ProfileManager.SELECTION_CHANGED_EVENT:
					updateSelection();
					break;
			}
		}
		
		private void updateProfiles() {
			fProfileCombo.setItems(fProfileManager.getSortedDisplayNames());
		}

		private void updateSelection() {
			fProfileCombo.setText(fProfileManager.getSelected().getName());
		}
	}
	
	private class ButtonController implements Observer, SelectionListener {
		
		public ButtonController() {
			fProfileManager.addObserver(this);
			fNewButton.addSelectionListener(this);
			fRenameButton.addSelectionListener(this);
			fEditButton.addSelectionListener(this);
			fDeleteButton.addSelectionListener(this);
			fSaveButton.addSelectionListener(this);
			fLoadButton.addSelectionListener(this);
			update(fProfileManager, null);
		}

		public void update(Observable o, Object arg) {
			Profile selected= ((ProfileManager)o).getSelected();
			final boolean notBuiltIn= !selected.isBuiltInProfile();
			fEditButton.setText(notBuiltIn ? FormatterMessages.CodingStyleConfigurationBlock_edit_button_desc
			    : FormatterMessages.CodingStyleConfigurationBlock_show_button_desc); 
			fDeleteButton.setEnabled(notBuiltIn);
			fSaveButton.setEnabled(notBuiltIn);
			fRenameButton.setEnabled(notBuiltIn);
		}

		public void widgetSelected(SelectionEvent e) {
			final Button button= (Button)e.widget;
			if (button == fSaveButton)
				saveButtonPressed();
			else if (button == fEditButton)
				modifyButtonPressed();
			else if (button == fDeleteButton) 
				deleteButtonPressed();
			else if (button == fNewButton)
				newButtonPressed();
			else if (button == fLoadButton)
				loadButtonPressed();
			else if (button == fRenameButton) 
				renameButtonPressed();
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		
		private void renameButtonPressed() {
			if (fProfileManager.getSelected().isBuiltInProfile())
				return;
			final CustomProfile profile= (CustomProfile) fProfileManager.getSelected();
			final RenameProfileDialog renameDialog= new RenameProfileDialog(fComposite.getShell(), profile, fProfileManager);
			if (renameDialog.open() == Window.OK) {
				fProfileManager.setSelected(renameDialog.getRenamedProfile());
			}
		}
		
		private void modifyButtonPressed() {
			final ModifyDialog modifyDialog= new ModifyDialog(fComposite.getShell(), fProfileManager.getSelected(), fProfileManager, false);
			modifyDialog.open();
		}
		
		private void deleteButtonPressed() {
			if (MessageDialog.openQuestion(
				fComposite.getShell(), 
				FormatterMessages.CodingStyleConfigurationBlock_delete_confirmation_title, 
				Messages.format(FormatterMessages.CodingStyleConfigurationBlock_delete_confirmation_question, fProfileManager.getSelected().getName()))) { 
				fProfileManager.deleteSelected();
			}
		}
		
		private void newButtonPressed() {
			final CreateProfileDialog p= new CreateProfileDialog(fComposite.getShell(), fProfileManager);
			if (p.open() != Window.OK) 
				return;
			if (!p.openEditDialog()) 
				return;
			final ModifyDialog modifyDialog= new ModifyDialog(fComposite.getShell(), p.getCreatedProfile(), fProfileManager, true);
			modifyDialog.open();
		}
		
		private void saveButtonPressed() {
			Profile selected= fProfileManager.getSelected();
			if (selected.isSharedProfile()) {
				final RenameProfileDialog renameDialog= new RenameProfileDialog(fComposite.getShell(), selected, fProfileManager);
				if (renameDialog.open() != Window.OK) {
					return;
				}
					
				selected= renameDialog.getRenamedProfile();
				fProfileManager.setSelected(selected);
			}
			
			final FileDialog dialog= new FileDialog(fComposite.getShell(), SWT.SAVE);
			dialog.setText(FormatterMessages.CodingStyleConfigurationBlock_save_profile_dialog_title); 
			dialog.setFilterExtensions(new String [] {"*.xml"}); //$NON-NLS-1$
			
			final String lastPath= CUIPlugin.getDefault().getDialogSettings().get(DIALOGSTORE_LASTSAVEPATH);
			if (lastPath != null) {
				dialog.setFilterPath(lastPath);
			}
			final String path= dialog.open();
			if (path == null) 
				return;
			
			CUIPlugin.getDefault().getDialogSettings().put(DIALOGSTORE_LASTSAVEPATH, dialog.getFilterPath());
			
			final File file= new File(path);
			if (file.exists() && !MessageDialog.openQuestion(fComposite.getShell(), FormatterMessages.CodingStyleConfigurationBlock_save_profile_overwrite_title, Messages.format(FormatterMessages.CodingStyleConfigurationBlock_save_profile_overwrite_message, path))) { 
				return;
			}
			
			final Collection profiles= new ArrayList();

			profiles.add(selected);
			try {
				ProfileStore.writeProfilesToFile(profiles, file);
			} catch (CoreException e) {
				final String title= FormatterMessages.CodingStyleConfigurationBlock_save_profile_error_title; 
				final String message= FormatterMessages.CodingStyleConfigurationBlock_save_profile_error_message; 
				ExceptionHandler.handle(e, fComposite.getShell(), title, message);
			}
		}
		
		private void loadButtonPressed() {
			final FileDialog dialog= new FileDialog(fComposite.getShell(), SWT.OPEN);
			dialog.setText(FormatterMessages.CodingStyleConfigurationBlock_load_profile_dialog_title); 
			dialog.setFilterExtensions(new String [] {"*.xml"}); //$NON-NLS-1$
			final String lastPath= CUIPlugin.getDefault().getDialogSettings().get(DIALOGSTORE_LASTLOADPATH);
			if (lastPath != null) {
				dialog.setFilterPath(lastPath);
			}
			final String path= dialog.open();
			if (path == null) 
				return;
			CUIPlugin.getDefault().getDialogSettings().put(DIALOGSTORE_LASTLOADPATH, dialog.getFilterPath());
			
			final File file= new File(path);
			Collection profiles= null;
			try {
				profiles= ProfileStore.readProfilesFromFile(file);
			} catch (CoreException e) {
				final String title= FormatterMessages.CodingStyleConfigurationBlock_load_profile_error_title; 
				final String message= FormatterMessages.CodingStyleConfigurationBlock_load_profile_error_message; 
				ExceptionHandler.handle(e, fComposite.getShell(), title, message);
			}
			if (profiles == null || profiles.isEmpty())
				return;
			
			final CustomProfile profile= (CustomProfile)profiles.iterator().next();
			
			if (ProfileVersioner.getVersionStatus(profile) > 0) {
				final String title= FormatterMessages.CodingStyleConfigurationBlock_load_profile_error_too_new_title; 
				final String message= FormatterMessages.CodingStyleConfigurationBlock_load_profile_error_too_new_message; 
			    MessageDialog.openWarning(fComposite.getShell(), title, message);
			}
			
			if (fProfileManager.containsName(profile.getName())) {
				final AlreadyExistsDialog aeDialog= new AlreadyExistsDialog(fComposite.getShell(), profile, fProfileManager);
				if (aeDialog.open() != Window.OK) 
					return;
			}
			ProfileVersioner.updateAndComplete(profile);
			fProfileManager.addProfile(profile);
		}
	}
	
//	private class PreviewController implements Observer {
//
//		public PreviewController() {
//			fProfileManager.addObserver(this);
//			fCodeStylePreview.setWorkingValues(fProfileManager.getSelected().getSettings());
//			fCodeStylePreview.update();
//		}
//		
//		public void update(Observable o, Object arg) {
//			final int value= ((Integer)arg).intValue();
//			switch (value) {
//				case ProfileManager.PROFILE_CREATED_EVENT:
//				case ProfileManager.PROFILE_DELETED_EVENT:
//				case ProfileManager.SELECTION_CHANGED_EVENT:
//				case ProfileManager.SETTINGS_CHANGED_EVENT:
//					fCodeStylePreview.setWorkingValues(((ProfileManager)o).getSelected().getSettings());
//					fCodeStylePreview.update();
//			}
//		}
//	}
	
//	/**
//	 * Some C source code used for preview.
//	 */
//	private final static String PREVIEW=
//		"/*\n* " + //$NON-NLS-1$
//		FormatterMessages.CodingStyleConfigurationBlock_preview_title + 
//		"\n*/\n\n" + //$NON-NLS-1$
//		"#include <math.h>\n" + //$NON-NLS-1$
//		"class Point {" +  //$NON-NLS-1$
//		"public:" +  //$NON-NLS-1$
//		"Point(double xc, double yc) : x(xc), y(yc) {}" + //$NON-NLS-1$ 
//		"double distance(const Point& other) const;" + //$NON-NLS-1$
//		"double x;" +  //$NON-NLS-1$
//		"double y;" +  //$NON-NLS-1$
//		"};" +  //$NON-NLS-1$
//		"float Point::distance(const Point& other) const {" + //$NON-NLS-1$
//		"double dx = x - other.x;" + //$NON-NLS-1$
//		"double dy = y - other.y;" + //$NON-NLS-1$
//		"return sqrt(dx * dx + dy * dy);" + //$NON-NLS-1$
//		"}"; //$NON-NLS-1$

	/**
	 * The GUI controls
	 */
	protected Composite fComposite;
	protected Combo fProfileCombo;
	protected Button fEditButton;
	protected Button fRenameButton;
	protected Button fDeleteButton;
	protected Button fNewButton;
	protected Button fLoadButton;
	protected Button fSaveButton;
	
	/**
	 * The ProfileManager, the model of this page.
	 */
	protected final ProfileManager fProfileManager;
	private CustomCodeFormatterBlock fCustomCodeFormatterBlock;
	
	/**
	 * The CPreview.
	 */
//	protected TranslationUnitPreview fCodeStylePreview;
	private PixelConverter fPixConv;

	private IScopeContext fCurrContext;
	private IScopeContext fInstanceScope;
	
	/**
	 * Create a new <code>CodeFormatterConfigurationBlock</code>.
	 */
	public CodeFormatterConfigurationBlock(IProject project, PreferencesAccess access) {
		fInstanceScope= access.getInstanceScope();
		List profiles= null;
		try {
		    profiles= ProfileStore.readProfiles(fInstanceScope);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		if (profiles == null) {
			try {
				// bug 129427
			    profiles= ProfileStore.readProfilesFromPreferences(new DefaultScope());
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		
		if (profiles == null) 
		    profiles= new ArrayList();
		
		if (project != null) {
			fCurrContext= access.getProjectScope(project);
		} else {
			fCurrContext= fInstanceScope;
		}
		
		fProfileManager= new ProfileManager(profiles, fCurrContext, access);
		fCustomCodeFormatterBlock= new CustomCodeFormatterBlock(CUIPlugin.getDefault().getPluginPreferences());

		new StoreUpdater();
	}

	/**
	 * Create the contents
	 * @param parent Parent composite
	 * @return Created control
	 */
	public Composite createContents(Composite parent) {

		final int numColumns = 5;
		
		fPixConv = new PixelConverter(parent);
		fComposite = createComposite(parent, numColumns);

		fProfileCombo= createProfileCombo(fComposite, numColumns - 3, fPixConv.convertWidthInCharsToPixels(20));
		fEditButton= createButton(fComposite, FormatterMessages.CodingStyleConfigurationBlock_edit_button_desc, GridData.HORIZONTAL_ALIGN_BEGINNING); 
		fRenameButton= createButton(fComposite, FormatterMessages.CodingStyleConfigurationBlock_rename_button_desc, GridData.HORIZONTAL_ALIGN_BEGINNING); 
		fDeleteButton= createButton(fComposite, FormatterMessages.CodingStyleConfigurationBlock_remove_button_desc, GridData.HORIZONTAL_ALIGN_BEGINNING); 

		final Composite group= createComposite(fComposite, 4);
		final GridData groupData= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		groupData.horizontalSpan= numColumns;
		group.setLayoutData(groupData);

		fNewButton= createButton(group, FormatterMessages.CodingStyleConfigurationBlock_new_button_desc, GridData.HORIZONTAL_ALIGN_BEGINNING); 
		((GridData)createLabel(group, "", 1).getLayoutData()).grabExcessHorizontalSpace= true; //$NON-NLS-1$
		fLoadButton= createButton(group, FormatterMessages.CodingStyleConfigurationBlock_load_button_desc, GridData.HORIZONTAL_ALIGN_END); 
		fSaveButton= createButton(group, FormatterMessages.CodingStyleConfigurationBlock_save_button_desc, GridData.HORIZONTAL_ALIGN_END); 

		fCustomCodeFormatterBlock.createContents(fComposite);
		
//		createLabel(fComposite, FormatterMessages.CodingStyleConfigurationBlock_preview_label_text, numColumns); 
//		configurePreview(fComposite, numColumns);
		
		new ButtonController();
		new ProfileComboController();
//		new PreviewController();
		
		return fComposite;
	}

	
	private static Button createButton(Composite composite, String text, final int style) {
		final Button button= new Button(composite, SWT.PUSH);
		button.setFont(composite.getFont());
		button.setText(text);

		final GridData gd= new GridData(style);
		gd.widthHint= SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);
		return button;
	}
	
	private static Combo createProfileCombo(Composite composite, int span, int widthHint) {
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		gd.widthHint= widthHint;

		final Combo combo= new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setFont(composite.getFont());
		combo.setLayoutData(gd);
		return combo;
	}
	
	private Label createLabel(Composite composite, String text, int numColumns) {
		final GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = numColumns;
		gd.widthHint= 0;

		final Label label = new Label(composite, SWT.WRAP);
		label.setFont(composite.getFont());
		label.setText(text);
		label.setLayoutData(gd);
		return label;		
	}
	
	private Composite createComposite(Composite parent, int numColumns) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		final GridLayout layout = new GridLayout(numColumns, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		return composite;
	}
	
//	private void configurePreview(Composite composite, int numColumns) {
//		fCodeStylePreview= new TranslationUnitPreview(fProfileManager.getSelected().getSettings(), composite);
//		fCodeStylePreview.setPreviewText(PREVIEW);
//	    
//		final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
//		gd.horizontalSpan = numColumns;
//		gd.verticalSpan= 7;
//		gd.widthHint = 0;
//		gd.heightHint = 0;
//		fCodeStylePreview.getControl().setLayoutData(gd);
//	}

	public final boolean hasProjectSpecificOptions(IProject project) {
		if (project != null) {
			return ProfileManager.hasProjectSpecificSettings(new ProjectScope(project));
		}
		return false;
	}
	
	public boolean performOk() {
		fCustomCodeFormatterBlock.performOk();
		return true;
	}
	
	public void performApply() {
		try {
			fCurrContext.getNode(CUIPlugin.PLUGIN_ID).flush();
			fCurrContext.getNode(CCorePlugin.PLUGIN_ID).flush();
			if (fCurrContext != fInstanceScope) {
				fInstanceScope.getNode(CUIPlugin.PLUGIN_ID).flush();
				fInstanceScope.getNode(CCorePlugin.PLUGIN_ID).flush();
			}
			fCustomCodeFormatterBlock.performOk();
		} catch (BackingStoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	public void performDefaults() {
		Profile profile= fProfileManager.getProfile(ProfileManager.DEFAULT_PROFILE);
		if (profile != null) {
			int defaultIndex= fProfileManager.getSortedProfiles().indexOf(profile);
			if (defaultIndex != -1) {
				fProfileManager.setSelected(profile);
			}
		}
		fCustomCodeFormatterBlock.performDefaults();
	}
	
	public void dispose() {
	}

	public void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		if (useProjectSpecificSettings) {
			fProfileManager.commitChanges(fCurrContext);
		} else {
			fProfileManager.clearAllSettings(fCurrContext);
		}
	}
}
