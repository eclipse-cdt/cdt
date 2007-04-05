package org.eclipse.cdt.internal.ui.language;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;

import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.language.LanguageMapping;
import org.eclipse.cdt.internal.core.language.LanguageMappingResolver;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class FileLanguageMappingPropertyPage extends PropertyPage {

	private Map fLanguageNamesToIDsMap;
	private Combo fLanguage;
	private IContentType fContentType;
	private Composite fContents;
	
	public FileLanguageMappingPropertyPage() {
		super();
		fLanguageNamesToIDsMap = new TreeMap();
	}
	
	protected Control createContents(Composite parent) {
		IFile file = getFile();
		IProject project = file.getProject();
		fContentType = CContentTypes.getContentType(project, file.getLocation().toString());
		
		fContents = new Composite(parent, SWT.NONE);
		fContents.setLayout(new GridLayout(2, false));

		Label contentTypeLabel = new Label(fContents, SWT.NONE);
		contentTypeLabel.setText(PreferencesMessages.FileLanguagesPropertyPage_contentTypeLabel);
		contentTypeLabel.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false));
		
		Label contentTypeDescriptionLabel = new Label(fContents, SWT.NONE);
		contentTypeDescriptionLabel.setText(fContentType.getName());
		contentTypeDescriptionLabel.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		
		Label languageLabel = new Label(fContents, SWT.NONE);
		languageLabel.setText(PreferencesMessages.FileLanguagesPropertyPage_languageLabel);
		languageLabel.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false));

		fLanguage = new Combo(fContents, SWT.DROP_DOWN | SWT.READ_ONLY);
		fLanguage.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		refreshMappings();
		
		Link link = new Link(fContents, SWT.NONE);
		link.setText(PreferencesMessages.FileLanguagesPropertyPage_description);
		link.addListener(SWT.Selection, new LanguageMappingLinkListener(parent.getShell(), project) {
			protected void refresh() {
				refreshMappings();
			}
		});
		link.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false, 2, 1));
		
		return fContents;
	}

	private void refreshMappings() {
		try {
			fLanguage.setItems(getLanguages());
			findSelection();
			fContents.layout();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	private void findSelection() throws CoreException {
		IFile file = getFile();
		LanguageManager manager = LanguageManager.getInstance();
		ProjectLanguageConfiguration config = manager.getLanguageConfiguration(file.getProject());
		String languageId = config.getLanguageForFile(file);
		
		if (languageId == null) {
			// No mapping was defined so we'll choose the default.
			fLanguage.select(0);
			return;
		}
		
		ILanguage language = manager.getLanguage(languageId);
		String name = language.getName();
		
		for (int i = 1; i < fLanguage.getItemCount(); i++) {
			if (name.equals(fLanguage.getItem(i))) {
				fLanguage.select(i);
				return;
			}
		}
		
		// Couldn't find the mapping so we'll choose the default.
		fLanguage.select(0);
	}

	public boolean performOk() {
		String languageId;
		String selectedLanguageName = fLanguage.getText();
		languageId = (String) fLanguageNamesToIDsMap.get(selectedLanguageName);
		
		try {
			IFile file = getFile();
			IProject project = file.getProject();
			LanguageManager manager = LanguageManager.getInstance();
			ProjectLanguageConfiguration config = manager.getLanguageConfiguration(project);
			String oldMapping = config.getLanguageForFile(file);
			
			if (oldMapping == languageId) {
				// No changes.  We're all done.
				return true;
			}
			
			if (languageId == null) {
				config.removeFileMapping(file);
			} else {
				config.addFileMapping(file, languageId);
			}
			manager.storeLanguageMappingConfiguration(file);
			return true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}
	
	private IFile getFile() {
		return (IFile) getElement().getAdapter(IFile.class);
	}

	protected void performDefaults() {
		super.performDefaults();
	}
	
	private String[] getLanguages() throws CoreException {
		ILanguage[] languages = LanguageManager.getInstance().getRegisteredLanguages();
		String[] descriptions = new String[languages.length];
		
		IFile file = getFile();
		IProject project = file.getProject();
		LanguageMapping mappings[] = LanguageMappingResolver.computeLanguage(project, file.getProjectRelativePath().toPortableString(), fContentType.getId(), true);
		LanguageMapping inheritedMapping = mappings[0];
		
		// Skip over the file mapping because we want to know what mapping the file
		// mapping overrides.
		if (inheritedMapping.inheritedFrom == LanguageMappingResolver.FILE_MAPPING ) {
			inheritedMapping = mappings[1];
		}
		
		ILanguage inheritedLanguage = inheritedMapping.language;
		String inheritedFrom;
		switch (inheritedMapping.inheritedFrom) {
		case LanguageMappingResolver.DEFAULT_MAPPING:
			inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromSystem;
			break;
		case LanguageMappingResolver.PROJECT_MAPPING:
			inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromProject;
			break;
		case LanguageMappingResolver.WORKSPACE_MAPPING:
			inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromWorkspace;
			break;
		default:
			throw new CoreException(Util.createStatus(new IllegalArgumentException()));
		}
		int index = 0;
		descriptions[index] = Messages.format(inheritedFrom, inheritedLanguage.getName());

		index++;
		for (int i = 0; i < languages.length; i++) {
			String id = languages[i].getId();
			if (!languages[i].equals(inheritedLanguage)) {
				descriptions[index] = languages[i].getName();
				fLanguageNamesToIDsMap.put(descriptions[index], id);
				index++;
			}
		}
		return descriptions;
	}
}
