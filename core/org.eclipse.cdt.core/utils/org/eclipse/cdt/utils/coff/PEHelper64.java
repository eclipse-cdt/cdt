/*******************************************************************************
 * Copyright (c) 2000, 2023 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation of ElfHelper
 *     John Dallaway - Initial PEHelper64 implementation (#652)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;

import java.io.IOException;

import org.eclipse.cdt.utils.coff.Coff64.FileHeader;
import org.eclipse.cdt.utils.coff.Coff64.SectionHeader;

/**
 * <code>PEHelper64</code> is a wrapper class for the <code>PE64</code> class
 * to provide higher level API for sorting/searching the COFF data.
 *
 * @see PE64
 * @since 8.4
 */
public final class PEHelper64 {

	public static record Sizes(long text, long data, long bss) {

		public long total() {
			return text + data + bss;
		}

	}

	private final PE64 pe64;

	public PEHelper64(PE64 pe64) {
		this.pe64 = pe64;
	}

	public Sizes getSizes() throws IOException {
		long bss = 0;
		long data = 0;
		long text = 0;

		boolean isExecutable = (pe64.getFileHeader().f_flags & FileHeader.F_EXEC) != 0;
		for (SectionHeader section : pe64.getSectionHeaders()) {
			if ((section.s_flags & SectionHeader.STYP_MEM_DISCARDABLE) != 0) {
				continue; // ignore discardable section
			}
			long size = isExecutable ? section.s_paddr : section.s_size;
			if ((section.s_flags & SectionHeader.STYP_BSS) != 0) {
				bss += size;
			} else if ((section.s_flags & SectionHeader.STYP_DATA) != 0) {
				if ((section.s_flags & SectionHeader.STYP_MEM_WRITE) != 0) {
					data += size; // treat writable data section as data
				} else {
					text += size; // treat non-writable data section as text
				}
			} else if ((section.s_flags & SectionHeader.STYP_TEXT) != 0) {
				text += size;
			}
		}
		return new Sizes(text, data, bss);
	}

}
