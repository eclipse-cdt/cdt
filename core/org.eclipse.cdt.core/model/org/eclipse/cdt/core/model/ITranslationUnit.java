/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
/**
 * Represents an entire C translation unit (<code>.c</code> source file).
 * The children are of type <code>IStructureElement</code>,
 * <code>IInclude</code>, etc..
 * and appear in the order in which they are declared in the source.
 * If a <code>.c</code> file cannot be parsed, its structure remains unknown.
 * Use <code>ICElement.isStructureKnown</code> to determine whether this is 
 * the case.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITranslationUnit extends ICElement, IParent, IOpenable, ISourceReference, ISourceManipulation {
	
	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Meaning: Skip function and method bodies.
	 * @since 4.0
	 */
	public static final int AST_SKIP_FUNCTION_BODIES= 0x1;

	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Meaning: Skip over headers that are found in the index, parse all others.
	 * Macro definitions and bindings are taken from index for skipped files.
	 */
	public static final int AST_SKIP_INDEXED_HEADERS = 0x2;

	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Meaning: Skip headers even if they are not found in the index. 
	 * Makes practically only sense in combination with {@link #AST_SKIP_INDEXED_HEADERS}.
	 */
	public static final int AST_SKIP_NONINDEXED_HEADERS = 0x4;

	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * A combination of {@link #AST_SKIP_INDEXED_HEADERS} and {@link #AST_SKIP_NONINDEXED_HEADERS}.
	 * Meaning: Don't parse header files at all, be they indexed or not. 
	 * Macro definitions and bindings are taken from the index if available.
	 */
	public static final int AST_SKIP_ALL_HEADERS = AST_SKIP_INDEXED_HEADERS | AST_SKIP_NONINDEXED_HEADERS;

	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Meaning: Don't parse the file if there is no build information for it.
	 */
	public static final int AST_SKIP_IF_NO_BUILD_INFO = 0x8;

	/**
	 * @deprecated The option has no effect.
	 * @since 4.0
	 */
	@Deprecated
	public static final int AST_CREATE_COMMENT_NODES = 0x10;
	
	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Meaning: Configure the parser with language and build-information taken from a source file
	 * that directly or indirectly includes this file. If no suitable file is found in the index,
	 * the flag is ignored.
	 * @since 4.0
	 */
	public static final int AST_CONFIGURE_USING_SOURCE_CONTEXT= 0x20;

	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Instructs the parser not to create ast nodes for expressions within aggregate initializers
	 * when they do not contain names.
	 * @since 5.1
	 */
	public final static int AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS= 0x40;

	/**
	 * Style constant for {@link #getAST(IIndex, int)}. 
	 * Instructs the parser to make an attempt to create ast nodes for inactive code branches. The parser
	 * makes its best effort to create ast for the inactive code branches but may decide to skip parts
	 * of the inactive code (e.g. function bodies, entire code branches, etc.).
	 * <p>
	 * The inactive nodes can be accessed via {@link IASTDeclarationListOwner#getDeclarations(boolean)} or
	 * by using a visitor with {@link ASTVisitor#includeInactiveNodes} set to <code>true</code>.
	 * 
	 * @since 5.1
	 */
	public final static int AST_PARSE_INACTIVE_CODE= 0x80;

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
	 * Returns the shared working copy for this element, using the default <code>IBuffer</code> factory, or
	 * <code>null</code>, if no working copy has been created for this element.
	 * <p>
	 * Users of this method must not destroy the resulting working copy.
	 * 
	 * @param bufferFactory
	 *            the given <code>IBuffer</code> factory
	 * @return the found shared working copy for this element, or <code>null</code> if none
	 * @see IBufferFactory
	 * @since 5.1
	 */
	IWorkingCopy findSharedWorkingCopy();

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
	 * Returns a shared working copy on this element using the given factory to create the buffer, or this
	 * element if this element is already a working copy. This API can only answer an already existing working
	 * copy if it is based on the same original translation unit AND was using the same buffer factory (i.e.
	 * as defined by <code>Object#equals</code>).
	 * <p>
	 * The life time of a shared working copy is as follows:
	 * <ul>
	 * <li>The first call to <code>getSharedWorkingCopy(...)</code> creates a new working copy for this
	 * element</li>
	 * <li>Subsequent calls increment an internal counter.</li>
	 * <li>A call to <code>destroy()</code> decrements the internal counter.</li>
	 * <li>When this counter is 0, the working copy is destroyed.
	 * </ul>
	 * So users of this method must destroy exactly once the working copy.
	 * <p>
	 * Note that the buffer factory will be used for the life time of this working copy, i.e. if the working
	 * copy is closed then reopened, this factory will be used. The buffer will be automatically initialized
	 * with the original's compilation unit content upon creation.
	 * <p>
	 * When the shared working copy instance is created, an ADDED ICElementDelta is reported on this working
	 * copy.
	 * 
	 * @param monitor
	 *            a progress monitor used to report progress while opening this compilation unit or
	 *            <code>null</code> if no progress should be reported
	 * @param requestor
	 *            a requestor which will get notified of problems detected during reconciling as they are
	 *            discovered. The requestor can be set to <code>null</code> indicating that the client is not
	 *            interested in problems.
	 * @exception CModelException
	 *                if the contents of this element can not be determined. Reasons include:
	 *                <ul>
	 *                <li> This C element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 *                </ul>
	 * @return a shared working copy on this element using the given factory to create the buffer, or this
	 *         element if this element is already a working copy
	 * @see IBufferFactory
	 * @see IProblemRequestor
	 * @since 5.1
	 */
	IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IProblemRequestor requestor) throws CModelException;

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
	 * Returns <code>true</code> if the code is C
	 */
	boolean isCLanguage();

	/**
	 * Returns <code>true</code> if the code is C++
	 */
	boolean isCXXLanguage();

	/**
	 * Returns <code>true</code> if the code is assembly
	 */
	boolean isASMLanguage();

	/**
	 * Returns a new working copy for the Translation Unit.
	 * @return IWorkingCopy
	 */
	IWorkingCopy getWorkingCopy() throws CModelException;

	/**
	 * Returns a new working copy for the Translation Unit.
	 * @since 5.1
	 */
	IWorkingCopy getWorkingCopy(IProgressMonitor monitor) throws CModelException;

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
	 * Return the language for this translation unit.
	 */
	ILanguage getLanguage() throws CoreException;
	
	/**
	 * Used by contributed languages' model builders to indicate whether or
	 * not the parse of a translation unit was successful.
	 * 
	 * @param wasSuccessful
	 * 
	 * TODO (DS) I'm not sure it's a good idea to put a setter in this
	 * interface. We should revisit this.
	 * 
	 */
	public void setIsStructureKnown(boolean wasSuccessful);
	
	/**
	 * Returns the absolute path of the location of the translation unit. May be <code>null</code>, in 
	 * case the location does not exist.
	 * @return an absolute path to the location, or <code>null</code>
	 * @since 4.0
	 */
	public IPath getLocation();

	/**
	 * Returns the scanner info associated with this translation unit. May return <code>null</code> if no 
	 * configuration is available.
	 * @param force if <code>true</code> a default info is returned, even if nothing is configured for this
	 * translation unit
	 * @return a scanner info for parsing the translation unit or <code>null</code> if none is configured
	 * @since 4.0
	 */
	public IScannerInfo getScannerInfo(boolean force);

	/**
	 * Creates the full AST for this translation unit. May return <code>null</code> if the language of this
	 * translation unit does not support ASTs.
	 * @return the AST for the translation unit or <code>null</code>
	 * @throws CoreException
	 * @since 4.0
	 */
	public IASTTranslationUnit getAST() throws CoreException;

	/**
	 * Creates an AST based on the requested style. May return <code>null</code> if the language of this
	 * translation unit does not support ASTs.
	 * @param index	index to back up the parsing of the AST, may be <code>null</code>
	 * @param style <code>0</code> or a combination of {@link #AST_SKIP_ALL_HEADERS}, 
	 * {@link #AST_SKIP_IF_NO_BUILD_INFO}, {@link #AST_SKIP_INDEXED_HEADERS}
	 * and {@link #AST_CONFIGURE_USING_SOURCE_CONTEXT}.
	 * @return the AST requested or <code>null</code>
	 * @throws CoreException
	 * @since 4.0
	 */
	public IASTTranslationUnit getAST(IIndex index, int style) throws CoreException;
	
	/**
	 * Return the completion node using the given index and parsing style at the given offset.
	 */
	public IASTCompletionNode getCompletionNode(IIndex index, int style, int offset) throws CoreException;	
	
	
	/**
	 * @deprecated use {@link #getSharedWorkingCopy(IProgressMonitor, IProblemRequestor)}, 
	 * or CDTUITools.getWorkingCopyManager() instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory) throws CModelException;
	/**
	 * @deprecated use {@link #getSharedWorkingCopy(IProgressMonitor, IProblemRequestor)}, 
	 * or CDTUITools.getWorkingCopyManager() instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor, IBufferFactory factory, IProblemRequestor requestor) throws CModelException;
	/**
	 * @deprecated use {@link #findSharedWorkingCopy()}, 
	 * or CDTUITools.getWorkingCopyManager() instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated IWorkingCopy findSharedWorkingCopy(IBufferFactory bufferFactory);
	/**
	 * @deprecated use {@link #getWorkingCopy(IProgressMonitor)}, 
	 * or CDTUITools.getWorkingCopyManager() instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated IWorkingCopy getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory) throws CModelException;
	/**
	 * @deprecated don't use this method.
	 */
	@Deprecated	Map<?,?> parse();
	/**
	 * @deprecated, use {@link FileContent#create(ITranslationUnit)}, instead.
	 */
	@Deprecated
	org.eclipse.cdt.core.parser.CodeReader getCodeReader();

	/**
	 * Returns the path to the file that should be used by the parser to access the file contents.
	 * For local translation units, this will return the equivalent to <code>getLocation().toOSString()</code>
	 * 
	 * @since 5.2
	 * @return String representing the path that should be used to obtain the file content.
	 * @see FileContent
	 */
	String getPathForFileContent();


}
