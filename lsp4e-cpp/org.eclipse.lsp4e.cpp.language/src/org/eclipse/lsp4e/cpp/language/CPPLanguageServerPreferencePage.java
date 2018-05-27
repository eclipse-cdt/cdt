package org.eclipse.lsp4e.cpp.language;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
//import pluginClass;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class CPPLanguageServerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private DirectoryFieldEditor direc;
	private BooleanFieldEditor chk;
	private RadioGroupFieldEditor radio;
	private StringFieldEditor text;

//	public IPreferenceStore prefs = getPreferenceStore();

	public CPPLanguageServerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A demonstration of a preference page implementation");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		direc = new DirectoryFieldEditor(PreferenceConstants.P_PATH,
				"&Directory preference:", getFieldEditorParent());
		addField(direc);
		chk = new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent());
		addField(chk);

		radio = new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent());
		addField(radio);
		text = new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent());
		addField(text);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		}
}