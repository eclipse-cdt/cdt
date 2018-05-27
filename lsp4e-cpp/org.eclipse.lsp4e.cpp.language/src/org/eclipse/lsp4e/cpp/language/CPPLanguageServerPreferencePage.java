package org.eclipse.lsp4e.cpp.language;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class CPPLanguageServerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private FileFieldEditor file;
	private RadioGroupFieldEditor radio;
	private StringFieldEditor options;

	public CPPLanguageServerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for the C++ Language Server\n\n");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

		radio = new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE,
				"Please select the C++ Language Server you want to use in Eclipse :", 1,
				new String[][] { { "ClangD", "clangd" }, { "CQuery", "cquery" } }, getFieldEditorParent());
		addField(radio);

		file = new FileFieldEditor(PreferenceConstants.P_PATH, "Browse path to the server executable",
				getFieldEditorParent());
		addField(file);

		options = new StringFieldEditor(PreferenceConstants.P_FLAGS, "Enter any command-line options for the server",
				getFieldEditorParent());
		addField(options);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}