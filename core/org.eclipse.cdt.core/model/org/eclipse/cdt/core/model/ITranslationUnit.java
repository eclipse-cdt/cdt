package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents an entire C translation unit (<code>.c</code> source file).
 * The children are of type <code>IStructureElement</code>,
 * <code>IInclude</code>, etc..
 * and appear in the order in which they are declared in the source.
 * If a <code>.c</code> file cannot be parsed, its structure remains unknown.
 * Use <code>ICElement.isStructureKnown</code> to determine whether this is 
 * the case.
 */
public interface ITranslationUnit extends ICFile , ISourceReference, ISourceManipulation {
	/**
	 * Creates and returns an include declaration in this translation unit
	 * with the given name.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the element will be inserted
	 * as the last import declaration in this translation unit.
	 * <p>
	 * If the translation unit already includes the specified include declaration,
	 * the import is not generated (it does not generate duplicates).
	 *
	 * @param name the name of the include declaration to add (For example: <code>"stdio.h"</code> or
	 *  <code>"sys/types.h"</code>)
	 * @param sibling the existing element which the include declaration will be inserted immediately before (if
	 *	<code> null </code>, then this include will be inserted as the last include declaration.
	 * @param monitor the progress monitor to notify
	 * @return the newly inserted include declaration (or the previously existing one in case attempting to create a duplicate)
	 *
	 * @exception CModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This C element does not exist or the specified sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The specified sibling is not a child of this translation unit (INVALID_SIBLING)
	 * <li> The name is not a valid import name (INVALID_NAME)
	 * </ul>
	 */
	IInclude createInclude(String name, ICElement sibling, IProgressMonitor monitor) throws CModelException;

	/**
	 * Creates and returns a namesapce declaration in this translation unit
	 * with the given package name.
	 *
	 * <p>If the translation unit already includes the specified package declaration,
	 * it is not generated (it does not generate duplicates).
	 *
	 * @param name the name of the namespace declaration to add (For example, <code>"std"</code>)
	 * @param monitor the progress monitor to notify
	 * @return the newly inserted namespace declaration (or the previously existing one in case attempting to create a duplicate)
	 *
	 * @exception CModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li>This C element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating an underlying resource
	 * <li> The name is not a valid package name (INVALID_NAME)
	 * </ul>
	 */
	IUsing createUsing (String name, IProgressMonitor monitor) throws CModelException;   

	/**
	 * Returns the smallest element within this translation unit that 
	 * includes the given source position (that is, a method, field, etc.), or
	 * <code>null</code> if there is no element other than the translation
	 * unit itself at the given position, or if the given position is not
	 * within the source range of this translation unit.
	 *
	 * @param position a source position inside the translation unit
	 * @return the innermost C element enclosing a given source position or <code>null</code>
	 *	if none (excluding the translation unit).
	 * @exception CModelException if the translation unit does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	ICElement getElementAtLine(int line) throws CModelException;

	ICElement getElement(String name) throws CModelException;

	/**
	 * Returns the include declaration in this translation unit with the given name.
	 *
	 * @param the name of the include to find (For example: <code>"stdio.h"</code> 
	 * 	or <code>"sys/types.h"</code>)
	 * @return a handle onto the corresponding include declaration. The include declaration may or may not exist.
	 */
	IInclude getInclude(String name) ;

	/**
	 * Returns the include declarations in this translation unit
	 * in the order in which they appear in the source.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	IInclude[] getIncludes() throws CModelException;

	/**
	 * Returns the first namespace declaration in this translation unit with the given package name
	 * This is a handle-only method. The namespace declaration may or may not exist.
	 *
	 * @param name the name of the namespace declaration (For example, <code>"std"</code>)
	 */
	IUsing getUsing(String name);

	/**
	 * Returns the namespace declarations in this translation unit
	 * in the order in which they appear in the source.
	 *
	 * @return an array of namespace declaration (normally of size one)
	 *
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	IUsing[] getUsings() throws CModelException;
}
