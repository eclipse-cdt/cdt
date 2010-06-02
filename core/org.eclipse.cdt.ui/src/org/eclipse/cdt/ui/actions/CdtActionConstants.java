/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

/**
 * Action ids for standard actions, for groups in the menu bar, and
 * for actions in context menus of CDT views.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 2.0
 */
public class CdtActionConstants {

	// Navigate menu
	
	/**
	 * Navigate menu: name of standard Goto Type global action
	 * (value <code>"org.eclipse.cdt.ui.actions.GoToType"</code>).
	 */
	public static final String GOTO_TYPE= "org.eclipse.cdt.ui.actions.GoToType"; //$NON-NLS-1$
	
	/**
	 * Navigate menu: name of standard Open Super Implementation global action
	 * (value <code>"org.eclipse.cdt.ui.actions.OpenSuperImplementation"</code>).
	 */
	public static final String OPEN_SUPER_IMPLEMENTATION= "org.eclipse.cdt.ui.actions.OpenSuperImplementation"; //$NON-NLS-1$
	
    /**
     * Navigate menu: name of standard Open Declaration global action
     * (value <code>"org.eclipse.cdt.ui.actions.OpenDeclaration"</code>).
     * @since 5.0
     */
    public static final String OPEN_DECLARATION= "org.eclipse.cdt.ui.actions.OpenDeclaration"; //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Open Type Hierarchy global action
	 * (value <code>"org.eclipse.cdt.ui.actions.OpenTypeHierarchy"</code>).
	 */
	public static final String OPEN_TYPE_HIERARCHY= "org.eclipse.cdt.ui.actions.OpenTypeHierarchy"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open Call Hierarchy global action
     * (value <code>"org.eclipse.cdt.ui.actions.OpenCallHierarchy"</code>).
     * @since 3.0
     */
    public static final String OPEN_CALL_HIERARCHY= "org.eclipse.cdt.ui.actions.OpenCallHierarchy"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Open Include Browser global action
     * (value <code>"org.eclipse.cdt.ui.actions.OpenIncludeBrowser"</code>).
     * @since 4.0
     */
    public static final String OPEN_INCLUDE_BROWSER= "org.eclipse.cdt.ui.actions.OpenIncludeBrowser"; //$NON-NLS-1$

	// Edit menu

	/**
	 * Edit menu: name of standard Code Assist global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ContentAssist"</code>).
	 */
	public static final String CONTENT_ASSIST= "org.eclipse.cdt.ui.actions.ContentAssist"; //$NON-NLS-1$

	// Source menu	
	
	/**
	 * Source menu: name of standard Comment global action
	 * (value <code>"org.eclipse.cdt.ui.actions.Comment"</code>).
	 */
	public static final String COMMENT= "org.eclipse.cdt.ui.actions.Comment"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Uncomment global action
	 * (value <code>"org.eclipse.cdt.ui.actions.Uncomment"</code>).
	 */
	public static final String UNCOMMENT= "org.eclipse.cdt.ui.actions.Uncomment"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard ToggleComment global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ToggleComment"</code>).
	 * @since 3.0
	 */
	public static final String TOGGLE_COMMENT= "org.eclipse.cdt.ui.actions.ToggleComment"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Block Comment global action
	 * (value <code>"org.eclipse.cdt.ui.actions.AddBlockComment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String ADD_BLOCK_COMMENT= "org.eclipse.cdt.ui.actions.AddBlockComment"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Block Uncomment global action
	 * (value <code>"org.eclipse.cdt.ui.actions.RemoveBlockComment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String REMOVE_BLOCK_COMMENT= "org.eclipse.cdt.ui.actions.RemoveBlockComment"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Indent global action
	 * (value <code>"org.eclipse.cdt.ui.actions.Indent"</code>).
	 */
	public static final String INDENT= "org.eclipse.cdt.ui.actions.Indent"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Shift Right action
	 * (value <code>"org.eclipse.cdt.ui.actions.ShiftRight"</code>).
	 */
	public static final String SHIFT_RIGHT= "org.eclipse.cdt.ui.actions.ShiftRight"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Shift Left global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ShiftLeft"</code>).
	 */
	public static final String SHIFT_LEFT= "org.eclipse.cdt.ui.actions.ShiftLeft"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Format global action
	 * (value <code>"org.eclipse.cdt.ui.actions.Format"</code>).
	 */
	public static final String FORMAT= "org.eclipse.cdt.ui.actions.Format"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Add Include global action
	 * (value <code>"org.eclipse.cdt.ui.actions.AddInclude"</code>).
	 */
	public static final String ADD_INCLUDE= "org.eclipse.cdt.ui.actions.AddInclude"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Sort Lines global action
	 * (value <code>"org.eclipse.cdt.ui.actions.SortLines"</code>).
	 * @since 5.2
	 */
	public static final String SORT_LINES= "org.eclipse.cdt.ui.actions.SortLines"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Sort Members global action (value
	 * <code>"org.eclipse.cdt.ui.actions.SortMembers"</code>).
	 */
	public static final String SORT_MEMBERS= "org.eclipse.cdt.ui.actions.SortMembers"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Surround with try/catch block global action
	 * (value <code>"org.eclipse.cdt.ui.actions.SurroundWithTryCatch"</code>).
	 */
	public static final String SURROUND_WITH_TRY_CATCH= "org.eclipse.cdt.ui.actions.SurroundWithTryCatch"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Override Methods global action
	 * (value <code>"org.eclipse.cdt.ui.actions.OverrideMethods"</code>).
	 */
	public static final String OVERRIDE_METHODS= "org.eclipse.cdt.ui.actions.OverrideMethods"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Generate Getter and Setter global action
	 * (value <code>"org.eclipse.cdt.ui.actions.GenerateGetterSetter"</code>).
	 */
	public static final String GENERATE_GETTER_SETTER= "org.eclipse.cdt.ui.actions.GenerateGetterSetter"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard delegate methods global action (value
	 * <code>"org.eclipse.cdt.ui.actions.GenerateDelegateMethods"</code>).
	 */
	public static final String GENERATE_DELEGATE_METHODS= "org.eclipse.cdt.ui.actions.GenerateDelegateMethods"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Add Constructor From Superclass global action
	 * (value <code>"org.eclipse.cdt.ui.actions.AddConstructorFromSuperclass"</code>).
	 */
	public static final String ADD_CONSTRUCTOR_FROM_SUPERCLASS= "org.eclipse.cdt.ui.actions.AddConstructorFromSuperclass"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Generate Constructor using Fields global action
	 * (value <code>"org.eclipse.cdt.ui.actions.GenerateConstructorUsingFields"</code>).
	 */
	public static final String GENERATE_CONSTRUCTOR_USING_FIELDS= "org.eclipse.cdt.ui.actions.GenerateConstructorUsingFields"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Add Cppdoc Comment global action
	 * (value <code>"org.eclipse.cdt.ui.actions.AddCppDocComment"</code>).
	 */
	public static final String ADD_CPP_DOC_COMMENT= "org.eclipse.cdt.ui.actions.AddCppDocComment"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Find Strings to Externalize global action
	 * (value <code>"org.eclipse.cdt.ui.actions.FindStringsToExternalize"</code>).
	 */
	public static final String FIND_STRINGS_TO_EXTERNALIZE= "org.eclipse.cdt.ui.actions.FindStringsToExternalize"; //$NON-NLS-1$
	
	/**
	 * Source menu: name of standard Externalize Strings global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ExternalizeStrings"</code>).
	 */
	public static final String EXTERNALIZE_STRINGS= "org.eclipse.cdt.ui.actions.ExternalizeStrings"; //$NON-NLS-1$
	
	// Refactor menu
	
	/**
	 * Refactor menu: name of standard Self Encapsulate Field global action
	 * (value <code>"org.eclipse.cdt.ui.actions.SelfEncapsulateField"</code>).
	 */
	public static final String SELF_ENCAPSULATE_FIELD= "org.eclipse.cdt.ui.actions.SelfEncapsulateField"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Modify Parameters global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ModifyParameters"</code>).
	 */
	public static final String MODIFY_PARAMETERS= "org.eclipse.cdt.ui.actions.ModifyParameters"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Pull Up global action
	 * (value <code>"org.eclipse.cdt.ui.actions.PullUp"</code>).
	 */
	public static final String PULL_UP= "org.eclipse.cdt.ui.actions.PullUp"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Push Down global action
	 * (value <code>"org.eclipse.cdt.ui.actions.PushDown"</code>).
	 */
	public static final String PUSH_DOWN= "org.eclipse.cdt.ui.actions.PushDown"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Move Element global action
	 * (value <code>"org.eclipse.cdt.ui.actions.Move"</code>).
	 */
	public static final String MOVE= "org.eclipse.cdt.ui.actions.Move"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Rename Element global action
	 * (value <code>"org.eclipse.cdt.ui.actions.Rename"</code>).
	 */
	public static final String RENAME= "org.eclipse.cdt.ui.actions.Rename"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Extract Temp global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ExtractTemp"</code>).
	 */
	public static final String EXTRACT_TEMP= "org.eclipse.cdt.ui.actions.ExtractTemp"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Extract Constant global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ExtractConstant"</code>).
	 */
	public static final String EXTRACT_CONSTANT= "org.eclipse.cdt.ui.actions.ExtractConstant"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Extract Local Variable global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ExtractLocalVariable"</code>).
     * @since 5.1
	 */
	public static final String EXTRACT_LOCAL_VARIABLE= "org.eclipse.cdt.ui.actions.ExtractLocalVariable"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Hide Method global action
	 * (value <code>"org.eclipse.cdt.ui.actions.HideMethod"</code>).
	 */
	public static final String HIDE_METHOD= "org.eclipse.cdt.ui.actions.HideMethod"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Extract Constant global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ImplementMethod"</code>).
	 */
	public static final String IMPLEMENT_METHOD= "org.eclipse.cdt.ui.actions.ImplementMethod"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Generate Getters and Setters global action
	 * (value <code>"org.eclipse.cdt.ui.actions.GettersAndSetters"</code>).
	 */
	public static final String GETTERS_AND_SETTERS= "org.eclipse.cdt.ui.actions.GettersAndSetters"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Introduce Parameter global action
	 * (value <code>"org.eclipse.cdt.ui.actions.IntroduceParameter"</code>).
	 */
	public static final String INTRODUCE_PARAMETER= "org.eclipse.cdt.ui.actions.IntroduceParameter"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Introduce Factory global action
	 * (value <code>"org.eclipse.cdt.ui.actions.IntroduceFactory"</code>).
	 */
	public static final String INTRODUCE_FACTORY= "org.eclipse.cdt.ui.actions.IntroduceFactory"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Extract Method global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ExtractMethod"</code>).
	 */
	public static final String EXTRACT_METHOD= "org.eclipse.cdt.ui.actions.ExtractMethod"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Inline global action 
	 * (value <code>"org.eclipse.cdt.ui.actions.Inline"</code>).
	 */
	public static final String INLINE= "org.eclipse.cdt.ui.actions.Inline"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Extract Interface global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ExtractInterface"</code>).
	 */
	public static final String EXTRACT_INTERFACE= "org.eclipse.cdt.ui.actions.ExtractInterface"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Generalize Type global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ChangeType"</code>).
	 */
	public static final String CHANGE_TYPE= "org.eclipse.cdt.ui.actions.ChangeType"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard global action to convert a nested type to a top level type
	 * (value <code>"org.eclipse.cdt.ui.actions.MoveInnerToTop"</code>).
	 */
	public static final String CONVERT_NESTED_TO_TOP= "org.eclipse.cdt.ui.actions.ConvertNestedToTop"; //$NON-NLS-1$
	
	/**
	 * Refactor menu: name of standard Use Supertype global action
	 * (value <code>"org.eclipse.cdt.ui.actions.UseSupertype"</code>).
	 */
	public static final String USE_SUPERTYPE= "org.eclipse.cdt.ui.actions.UseSupertype"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard global action to convert a local
	 * variable to a field (value <code>"org.eclipse.cdt.ui.actions.ConvertLocalToField"</code>).
	 */
	public static final String CONVERT_LOCAL_TO_FIELD= "org.eclipse.cdt.ui.actions.ConvertLocalToField"; //$NON-NLS-1$

	/**
	 * Refactor menu: name of standard Covert Anonymous to Nested global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ConvertAnonymousToNested"</code>).
	 */
	public static final String CONVERT_ANONYMOUS_TO_NESTED= "org.eclipse.cdt.ui.actions.ConvertAnonymousToNested"; //$NON-NLS-1$
	
	// Search Menu
	
	/**
	 * Search menu: name of standard Find References in Workspace global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReferencesInWorkspace"</code>).
	 */
	public static final String FIND_REFERENCES_IN_WORKSPACE= "org.eclipse.cdt.ui.actions.ReferencesInWorkspace"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find References in Project global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReferencesInProject"</code>).
	 */
	public static final String FIND_REFERENCES_IN_PROJECT= "org.eclipse.cdt.ui.actions.ReferencesInProject"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find References in Hierarchy global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReferencesInHierarchy"</code>).
	 */
	public static final String FIND_REFERENCES_IN_HIERARCHY= "org.eclipse.cdt.ui.actions.ReferencesInHierarchy"; //$NON-NLS-1$
	
	/**
	 * Search menu: name of standard Find References in Working Set global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReferencesInWorkingSet"</code>).
	 */
	public static final String FIND_REFERENCES_IN_WORKING_SET= "org.eclipse.cdt.ui.actions.ReferencesInWorkingSet"; //$NON-NLS-1$



	/**
	 * Search menu: name of standard Find Declarations in Workspace global action
	 * (value <code>"org.eclipse.cdt.ui.actions.DeclarationsInWorkspace"</code>).
	 */
	public static final String FIND_DECLARATIONS_IN_WORKSPACE= "org.eclipse.cdt.ui.actions.DeclarationsInWorkspace"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Declarations in Project global action
	 * (value <code>"org.eclipse.cdt.ui.actions.DeclarationsInProject"</code>).
	 */
	public static final String FIND_DECLARATIONS_IN_PROJECT= "org.eclipse.cdt.ui.actions.DeclarationsInProject"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Declarations in Hierarchy global action
	 * (value <code>"org.eclipse.cdt.ui.actions.DeclarationsInHierarchy"</code>).
	 */
	public static final String FIND_DECLARATIONS_IN_HIERARCHY= "org.eclipse.cdt.ui.actions.DeclarationsInHierarchy"; //$NON-NLS-1$
	
	/**
	 * Search menu: name of standard Find Declarations in Working Set global action
	 * (value <code>"org.eclipse.cdt.ui.actions.DeclarationsInWorkingSet"</code>).
	 */
	public static final String FIND_DECLARATIONS_IN_WORKING_SET= "org.eclipse.cdt.ui.actions.DeclarationsInWorkingSet"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Implementors in Workspace global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ImplementorsInWorkspace"</code>).
	 */
	public static final String FIND_IMPLEMENTORS_IN_WORKSPACE= "org.eclipse.cdt.ui.actions.ImplementorsInWorkspace"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Implementors in Project global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ImplementorsInProject"</code>).
	 */
	public static final String FIND_IMPLEMENTORS_IN_PROJECT= "org.eclipse.cdt.ui.actions.ImplementorsInProject"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Implementors in Working Set global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ImplementorsInWorkingSet"</code>).
	 */
	public static final String FIND_IMPLEMENTORS_IN_WORKING_SET= "org.eclipse.cdt.ui.actions.ImplementorsInWorkingSet"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Read Access in Workspace global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReadAccessInWorkspace"</code>).
	 */
	public static final String FIND_READ_ACCESS_IN_WORKSPACE= "org.eclipse.cdt.ui.actions.ReadAccessInWorkspace"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Read Access in Project global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReadAccessInProject"</code>).
	 */
	public static final String FIND_READ_ACCESS_IN_PROJECT= "org.eclipse.cdt.ui.actions.ReadAccessInProject"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Read Access in Hierarchy global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReadAccessInHierarchy"</code>).
	 */
	public static final String FIND_READ_ACCESS_IN_HIERARCHY= "org.eclipse.cdt.ui.actions.ReadAccessInHierarchy"; //$NON-NLS-1$
	
	/**
	 * Search menu: name of standard Find Read Access in Working Set global action
	 * (value <code>"org.eclipse.cdt.ui.actions.ReadAccessInWorkingSet"</code>).
	 */
	public static final String FIND_READ_ACCESS_IN_WORKING_SET= "org.eclipse.cdt.ui.actions.ReadAccessInWorkingSet"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Write Access in Workspace global action
	 * (value <code>"org.eclipse.cdt.ui.actions.WriteAccessInWorkspace"</code>).
	 */
	public static final String FIND_WRITE_ACCESS_IN_WORKSPACE= "org.eclipse.cdt.ui.actions.WriteAccessInWorkspace"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Write Access in Project global action
	 * (value <code>"org.eclipse.cdt.ui.actions.WriteAccessInProject"</code>).
	 */
	public static final String FIND_WRITE_ACCESS_IN_PROJECT= "org.eclipse.cdt.ui.actions.WriteAccessInProject"; //$NON-NLS-1$

	/**
	 * Search menu: name of standard Find Read Access in Hierarchy global action
	 * (value <code>"org.eclipse.cdt.ui.actions.WriteAccessInHierarchy"</code>).
	 */
	public static final String FIND_WRITE_ACCESS_IN_HIERARCHY= "org.eclipse.cdt.ui.actions.WriteAccessInHierarchy"; //$NON-NLS-1$
	
	/**
	 * Search menu: name of standard Find Read Access in Working Set global action
	 * (value <code>"org.eclipse.cdt.ui.actions.WriteAccessInWorkingSet"</code>).
	 */
	public static final String FIND_WRITE_ACCESS_IN_WORKING_SET= "org.eclipse.cdt.ui.actions.WriteAccessInWorkingSet"; //$NON-NLS-1$
	
	/**
	 * Search menu: name of standard Occurrences in File global action (value
	 * <code>"org.eclipse.cdt.ui.actions.OccurrencesInFile"</code>).
	 */
	public static final String FIND_OCCURRENCES_IN_FILE= "org.eclipse.cdt.ui.actions.OccurrencesInFile"; //$NON-NLS-1$
	
	/**
	 * Search menu: name of standard Find exception occurrences global action (value
	 * <code>"org.eclipse.cdt.ui.actions.ExceptionOccurrences"</code>).
	 */
	public static final String FIND_EXCEPTION_OCCURRENCES= "org.eclipse.cdt.ui.actions.ExceptionOccurrences"; //$NON-NLS-1$		
}
