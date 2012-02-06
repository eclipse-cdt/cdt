/*****************************************************************
 * Copyright (c) 2010, 2012 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 300053)
 *     Patrick Chuong (Texas Instruments) - Bug 369998
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * <p>
 * This interface provides location information for a breakpoint to
 * determine its visual annotation position in the disassembly viewer document.
 * If a breakpoint adapts to this interface, its position in the viewer is
 * determined by the information provided by the location provider.
 * </p>
 * 
 * <p>
 * Breakpoints implementing either {@link ICAddressBreakpoint} or {@link ILineBreakpoint}
 * need not provide a location provider but may do so in order to override default
 * location retrieval.
 * </p> 
 * 
 * <p>
 * The annotation position will be determined with the following ordering:
 * <ol>
 * <li>If there is source info, than source marker will be use by the viewer.</li>
 * <li>If there is label info, than label marker will be use by the viewer.</li>
 * <li>If there is address info, than address marker will be use by the viewer.</li>
 * <li>Otherwise, nothing will be created.</li>
 * </ol>
 * </p>
 * <br>
 * @since 2.1
 */
public interface IBreakpointLocationProvider {
	/**
	 * Returns the line number of the breakpoint or -1 if no line number is
	 * available.
	 * 
	 * @param breakpoint the breakpoint
	 * @param debugContext the debug context of the view
	 * @return the line number or -1
	 */
	int getLineNumber(IBreakpoint breakpoint, IAdaptable debugContext);

	/**
	 * Returns the source file path of the breakpoint or <code>null</code> if no
	 * source file is associated with this breakpoint.
	 * 
	 * @param breakpoint the breakpoint
	 * @param debugContext the debug context of the view
	 * @return the file path, can be <code>null</code>
	 */
	String getSourceFile(IBreakpoint breakpoint, IAdaptable debugContext);

	/**
	 * Returns the label address of the breakpoint or <code>null</code> if no
	 * label is associated with this breakpoint.
	 * 
	 * @param breakpoint the breakpoint
	 * @param debugContext the debug context of the view
	 * @return the label address, can be <code>null</code>
	 */
	IAddress getLabelAddress(IBreakpoint breakpoint, IAdaptable debugContext);

	/**
	 * Returns the addresses of the breakpoint.
	 * 
	 * <p>
	 * <i>Currently there can only be one annotation per breakpoint. Therefore
	 * an annotation is created only for the first valid address. Support for
	 * multiple annotations per breakpoint is up for future enhancements. </i>
	 * </p>
	 * 
	 * @param breakpoint the breakpoint
	 * @param debugContext the debug context of the view
	 * @return the addresses, can be <code>null</code>
	 */
	IAddress[] getAddresses(IBreakpoint breakpoint, IAdaptable debugContext);
}
