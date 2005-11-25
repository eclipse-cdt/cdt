/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import java.util.Map;

import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.core.runtime.CoreException;
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
public interface ITranslationUnit extends ICElement, IParent, IOpenable, ISourceReference, ISourceManipulation {
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
	IInclude createInclude(String name, boolean isStd, ICElement sibling, IProgressMonitor monitor) throws CModelException;

	/**
	 * Creates and returns a using declaration/directive in this translation unit
	 *
	 *
	 * @param name the name of the using
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
	IUsing createUsing (String name, boolean isDirective, ICElement sibling, IProgressMonitor monitor) throws CModelException;   

	/**
	 * Creates and returns a namespace in this translation unit
	 *
	 * @param name the name of the namespace
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
	INamespace createNamespace (String namespace, ICElement sibling, IProgressMonitor monitor) throws CModelException;   

	/**
	 * Finds the shared working copy for this element, given a <code>IBuffer</code> factory. 
	 * If no working copy has been created for this element associated with this
	 * buffer factory, returns <code>null</code>.
	 * <p>
	 * Users of this method must not destroy the resulting working copy. 
	 * 
	 * @param bufferFactory the given <code>IBuffer</code> factory
	 * @return the found shared working copy for this element, <code>null</code> if none
	 * @see IBufferFactory
	 * @since 2.0
	 */
	IWorkingCopy findSharedWorkingCopy(IBufferFactory bufferFactory);

	/**
	 * Returns the contents of a translation unit as a char[]
	 * @return char[]
	 */
	char[] getContents();

	/**
	 * Returns the smallest element within this translation unit that 
	 * includes the given source position (that is, a method, field, etc.), or
	 * <code>null</code> if there is no element other than the translation
	 * unit itself at the given position, or if the given position is not
	 * within the source range of this translation unit.
	 *
	 * @param line a position inside the translation unit
	 * @return the innermost C element enclosing a given source position or <code>null</code>
	 *	if none (excluding the translation unit).
	 * @exception CModelException if the translation unit does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	ICElement getElementAtLine(int line) throws CModelException;

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
	ICElement getElementAtOffset(int offset) throws CModelException;

	/**
	 * Returns the elements within this translation unit that 
	 * includes the given source position (that is, a method, field, etc.), or
	 * an empty array if there are no elements other than the translation
	 * unit itself at the given position, or if the given position is not
	 * within the source range of this translation unit.
	 * You have this behavior when at expansion of a macro.
	 *
	 * @param position a source position inside the translation unit
	 * @return the innermost C element enclosing a given source position or <code>null</code>
	 *	if none (excluding the translation unit).
	 * @exception CModelException if the translation unit does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	ICElement[] getElementsAtOffset(int offset) throws CModelException;

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
	 * Returns a shared working copy on this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy.
	 * This API can only answer an already existing working copy if it is based on the same
	 * original translation unit AND was using the same buffer factory (i.e. as
	 * defined by <code>Object#equals</code>).
	 * <p>
	 * The life time of a shared working copy is as follows:
	 * <ul>
	 * <li>The first call to <code>getSharedWorkingCopy(...)</code> creates a new working copy for this
	 *     element</li>
	 * <li>Subsequent calls increment an internal counter.</li>
	 * <li>A call to <code>destroy()</code> decrements the internal counter.</li>
	 * <li>When this counter is 0, the working copy is destroyed.
	 * </ul>
	 * So users of this method must destroy exactly once the working copy.
	 * <p>
	 * Note that the buffer factory will be used for the life time of this working copy, i.e. if the 
	 * working copy is closed then reopened, this factory will be used.
	 * The buffer will be automatically initialized with the original's compilation unit content
	 * upon creation.
	 * <p>
	 * When the shared working copy instance is created, an ADDED ICElementDelta is reported on this
	 * working copy.
	 *
	 * @param monitor a progress monitor used to report progress while opening this compilation unit
	 *                 or <code>null</code> if no progress should be reported 
	 * @param factory the factory that creates a buffer that is used to get the content of the working copy
	 *                 or <code>null</code> if the internal factory should be used
	 * @param problemRequestor a requestor which will get notified of problems detected during
	 * 	reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
	 * 	that the client is not interested in problems.
	 * @exception CModelException if the contents of this element can   not be
	 * determined. Reasons include:
	 * <ul>
	 * <li> This C element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @return a shared working copy on this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy
	 * @see IBufferFactory
	 * @see IProblemRequestor
	 * @since 2.0
	 */
	
	IWorkingCopy getSharedWorkingCopy(
		IProgressMonitor monitor,
		IBufferFactory factory)
		throws CModelException;

	/**
	 * Returns a shared working copy on this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy.
	 * This API can only answer an already existing working copy if it is based on the same
	 * original translation unit AND was using the same buffer factory (i.e. as
	 * defined by <code>Object#equals</code>).
	 * <p>
	 * The life time of a shared working copy is as follows:
	 * <ul>
	 * <li>The first call to <code>getSharedWorkingCopy(...)</code> creates a new working copy for this
	 *     element</li>
	 * <li>Subsequent calls increment an internal counter.</li>
	 * <li>A call to <code>destroy()</code> decrements the internal counter.</li>
	 * <li>When this counter is 0, the working copy is destroyed.
	 * </ul>
	 * So users of this method must destroy exactly once the working copy.
	 * <p>
	 * Note that the buffer factory will be used for the life time of this working copy, i.e. if the 
	 * working copy is closed then reopened, this factory will be used.
	 * The buffer will be automatically initialized with the original's compilation unit content
	 * upon creation.
	 * <p>
	 * When the shared working copy instance is created, an ADDED ICElementDelta is reported on this
	 * working copy.
	 *
	 * @param monitor a progress monitor used to report progress while opening this compilation unit
	 *                 or <code>null</code> if no progress should be reported 
	 * @param factory the factory that creates a buffer that is used to get the content of the working copy
	 *                 or <code>null</code> if the internal factory should be used
	 * @param problemRequestor a requestor which will get notified of problems detected during
	 * 	reconciling as they are discovered. The requestor can be set to <code>null</code> indicating
	 * 	that the client is not interested in problems.
	 * @exception CModelException if the contents of this element can   not be
	 * determined. Reasons include:
	 * <ul>
	 * <li> This C element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * </ul>
	 * @return a shared working copy on this element using the given factory to create
	 * the buffer, or this element if this element is already a working copy
	 * @see IBufferFactory
	 * @see IProblemRequestor
	 * @since 2.0
	 */
	IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor requestor) throws CModelException;

	/**
	 * Returns the first using in this translation unit with the name
	 * This is a handle-only method. The namespace declaration may or may not exist.
	 *
	 * @param name the name of the namespace declaration (For example, <code>"std"</code>)
	 */
	IUsing getUsing(String name);

	/**
	 * Returns the usings in this translation unit
	 * in the order in which they appear in the source.
	 *
	 * @return an array of namespace declaration (normally of size one)
	 *
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	IUsing[] getUsings() throws CModelException;

	/**
	 * Returns the first namespace declaration in this translation unit with the given name
	 * This is a handle-only method. The namespace declaration may or may not exist.
	 *
	 * @param name the name of the namespace declaration (For example, <code>"std"</code>)
	 */
	INamespace getNamespace(String name);

	/**
	 * Returns the namespace declarations in this translation unit
	 * in the order in which they appear in the source.
	 *
	 * @return an array of namespace declaration (normally of size one)
	 *
	 * @exception CModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	INamespace[] getNamespaces() throws CModelException;

	/**
	 * True if its a header.
	 * @return boolean
	 */
	boolean isHeaderUnit();

	/**
	 * True it is a source file.
	 * @return boolean
	 */
	boolean isSourceUnit();

	/**
	 * True if the code is C
	 * @return
	 */
	boolean isCLanguage();

	/**
	 * True if the code is C++
	 * 
	 * @return
	 */
	boolean isCXXLanguage();

	/**
	 * True if assembly
	 * 
	 * @return
	 */
	boolean isASMLanguage();

	/**
	 * Returns a new working copy for the Translation Unit.
	 * @return IWorkingCopy
	 */
	IWorkingCopy getWorkingCopy() throws CModelException;

	/**
	 * Returns a new working copy for the Translation Unit.
	 * @return IWorkingCopy
	 */
	IWorkingCopy getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory) throws CModelException;

	/**
	 * Return the contentType id for this file.
	 * @return String - contentType id
	 */
	String getContentTypeId();

	/**
	 * Checks if this is a working copy.
	 * @return boolean
	 */
	boolean isWorkingCopy();

	/**
	 * parse()
	 * returns a map of all new elements and their element info
	 * @deprecated this is currently only used by the core tests. It should
	 * be removed from the interface.
	 */
	Map parse();

	/**
	 * Return the language for this translation unit.
	 * 
	 * @return
	 */
	ILanguage getLanguage() throws CoreException;
}
