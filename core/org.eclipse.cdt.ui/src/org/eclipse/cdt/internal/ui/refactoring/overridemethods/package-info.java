package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

/**
 * This package adds "Override Methods" functionality to Source context menu 
 * located both in main menu and as a pop-up menu in editor. User can select 
 * which virtual methods overrides from which classes should be generated.
 * This code generation feature is supposed to be triggered mostly in header
 * files, thats why just method declarations are generated. If user wants to
 * generate also definitions, another code generation feature "Implement
 * methods" can be triggered. Every method is added under corresponding
 * visibility label, in case when no labels are found, they are generated in
 * order set in preferences.
 * <p>
 * Code of this contribution is inspired from "Generate getters and setters"
 * code generation and "Extract constant" refactoring.
 * </p>
 * <p>
 * It is a contribution to Eclipse LTK refactoring framework, more precisely
 * to CDT refactoring framework which is a wrapper for LTK refactoring.
 * </p>
 * {@link OverrideMethodsRefactoring} is main class that controls the
 * lifecycle of refactoring (see documentation for LTK refactoring framework).
 * Other classes that are vital for functionality can be retrieved from this
 * class.
 * 
 * Steps of this refactoring are:
 * 1) Initial conditions checking. 
 * The initial conditions are satisfied when 
 * the selection (cursor) is located inside a class definition, this class has 
 * some base classes, and there is at least one virtual method to override.
 *   During this step the {@link VirtualMethodASTVisitor} traverses the AST for
 * the current file and finds the class that is selected. The binding for this
 * class is resolved and from this binding all the informations about base
 * classes and their virtual methods are gathered inside {@link VirtualMethodContainer}.
 * 
 * 2) Method selection (dialog with user).
 * {@link OverrideMethodsInputPage} represents the only <code>WizardInputPage</code>
 * that this code generation consists of. This wizard looks similar to the wizard 
 * from "Generate getters and setters" - there is a <code>CheckBoxTreeView</code> 
 * where parent nodes represent base classes and children nodes represent their 
 * virtual methods. 
 *   When one of items (virtual methods) is checked, the corresponding method
 * is saved to {@link VirtualMethodPrintData}.
 * 
 * 3) Collection of all changes.
 * This step is handled just by {@link VirtualMethodPrintData} that adds
 * selected methods inside class (rewrites the corresponding AST with the help
 * of {@link org.eclipse.cdt.internal.ui.refactoring.ClassMemberInserter}).
 * 
 * <b>Improvements for future</b>
 * There are no preferences whether user wants to generate <code>virtual</code>
 * and <code>override</code> keywords - those keywords are currently always
 * printed.
 * 
 * 
 * @author Pavel Marek 
 */