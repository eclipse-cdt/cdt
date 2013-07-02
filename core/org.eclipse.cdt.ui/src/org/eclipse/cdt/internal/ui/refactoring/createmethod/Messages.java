package org.eclipse.cdt.internal.ui.refactoring.createmethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String CreateMethodRefactoring_ClassNotFound;
	public static String CreateMethodRefactoring_MethodCannotBeCreated;
	public static String CreateMethodRefactoringWizard_PageTitle;
	public static String CreateMethodRefactoringWizardPage_ParameterNamesLabel;
	public static String CreateMethodRefactoringWizardPage_ParameterNameUsedMultipleTimes;
	public static String Helpers_DefaultMethodName;
	public static String Helpers_DefaultParameterName;
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}