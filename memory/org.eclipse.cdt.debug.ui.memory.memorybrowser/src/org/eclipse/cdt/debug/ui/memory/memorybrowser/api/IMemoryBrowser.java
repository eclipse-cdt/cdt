package org.eclipse.cdt.debug.ui.memory.memorybrowser.api;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;

/**
 * Public API for accessing the memory browser. 
 * 
 * <p>
 * All methods must be called on the UI thread, unless otherwise noted. 
 */
public interface IMemoryBrowser {
	/**
	 * Tells the memory browser to go to a new memory location. Updates the goto
	 * address bar and memory space selector (if present).
	 * 
	 * <p>
	 * This operation is a no-op if there is no active memory retrieval object.
	 * 
	 * @param expression
	 *            the expression to go to. Cannot be null or empty string.
	 *            Expression is trimmed.
	 * @param memorySpaceId
	 *            optional memory space ID. Argument is ignored if the memory
	 *            browser is not currently showing a memory space selector. If
	 *            selector is showing, this argument is interpreted as follows:
	 *            empty string means no memory space (as if the user selected
	 *            the "----" memory space), and null means use whatever memory
	 *            space is selected. Passing an ID that is not present in the
	 *            selector will result in an IllegalArgumentException
	 * @param inNewTab
	 *            if true, memory is shown in a new tab
	 * @throws CoreException
	 */
	public void go(String expression, String memorySpaceId, boolean inNewTab) throws CoreException;
	
	/**
	 * Returns the selected memory space.
	 * 
	 * <p>
	 * The memory browser exposes a memory space selector when debugging a
	 * target with multiple memory spaces. The selection provides the context
	 * for the expression when the user performs a GO action. This method will
	 * return the currently selected memory space.
	 * 
	 * @return null if the memory space selector is not shown, or if the n/a
	 *         entry is selected. Otherwise the selected memory space ID. Never
	 *         an empty string.
	 */
	public String getSelectedMemorySpace();
	
	/**
	 * Returns the active memory retrieval object, or null if none is active.
	 * 
	 * This is the retrieval object being used to obtain the memory shown in the
	 * active tab. Note that all simultaneously visible tabs use the same
	 * retrieval object. The retrieval object is obtained from the active debug
	 * context.
	 * 
	 * @return the active memory retrieval object, or null if none is active
	 */
	public IMemoryBlockRetrieval getActiveRetrieval();
}
