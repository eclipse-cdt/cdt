/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 */
public interface IBinaryParser extends IAdaptable {

	/**
	 * Represents a binary file for example an ELF executable.
	 */
	interface IBinaryFile extends IAdaptable {
		/**
		 * Binary is an object, can be safely typecast to IBinaryObject
		 */
		static final int OBJECT = 0x1;

		/**
		 * Binary is an executable, can be typecast to IBinaryExectuable
		 */
		static final int EXECUTABLE = 0x02;

		/**
		 * Binary is a DLL, can be use as a IBinaryShared
		 */
		static final int SHARED = 0x04;

		/**
		 * Binary is an archive, IBinaryArchive
		 */
		static final int ARCHIVE = 0x08;

		/**
		 * Binary is a core file, an IBinaryFile
		 */
		static final int CORE = 0x10;

		/**
		 *  Filename of the binary
		 * @return the path
		 */
		IPath getPath();

		/**
		 * Binary type
		 * @return the type of the binary
		 */
		int getType();

		/**
		 * 
		 * @return the binary contents.
		 */
		InputStream getContents();

		/**
		 * Return the binary parser
		 * @return
		 */
		IBinaryParser getBinaryParser();
	}

	/**
	 * Represents an archive.
	 */
	interface IBinaryArchive extends IBinaryFile {
		IBinaryObject[] getObjects();
	}

	/**
	 * Represents a binary, for example an ELF excutable.
	 */
	interface IBinaryObject extends IBinaryFile {

		/**
		 * True if the binary contains debug information
		 * @return true if debug information
		 */
		boolean hasDebug();

		/**
		 * CPU name
		 * @return String - cpu name
		 */
		String getCPU();

		long getText();

		long getData();

		long getBSS();

		/**
		 * The endian
		 * @return boolean - true for little endian
		 */
		boolean isLittleEndian();

		/**
		 * Symbols of the object
		 * @return ISymbol[] arrays of symbols
		 */
		ISymbol[] getSymbols();

		/**
		 * Symbo at this address.
		 * @param addr
		 * @return ISymbol
		 */
		ISymbol getSymbol(long addr);

		/**
		 * The name of the object
		 * @return String
		 */
		String getName();

	}

	/**
	 * An executable.
	 */
	interface IBinaryExecutable extends IBinaryObject {

		/**
		 * Needed shared libraries for this executable
		 * @return String[] array
		 */
		String[] getNeededSharedLibs();
	}

	/**
	 * A DLL.
	 */
	interface IBinaryShared extends IBinaryExecutable {
		/**
		 * The Share Object name.
		 * @return
		 */
		String getSoName();
	}

	interface ISymbol extends Comparable {

		/**
		 * Symbol is type function.
		 */
		static final int FUNCTION = 0x01;

		/**
		 * Symbol is type variable
		 */
		static final int VARIABLE = 0x02;

		/**
		 * Name of the Symbol
		 * @return
		 */
		String getName();

		/**
		 * Address of the symbol
		 * @return
		 */
		long getAddress();

		/**
		 * Size of the symbol.
		 * @return
		 */
		long getSize();

		/**
		 * Start linenumber of the symbol in the source
		 * @return
		 */
		int getStartLine();

		/**
		 * End line number of the symbol in the source
		 * @return
		 */
		int getEndLine();

		/**
		 * Source filename of the symbol.
		 * @return
		 */
		IPath getFilename();

		/**
		 * Type of the symbol
		 * @return
		 */
		int getType();

		/**
		 * Line number corresponding to the address offset.
		 * @param offset
		 * @return
		 */
		int getLineNumber(long offset);
		
		/**
		 * Return the binary object this symbol is from.
		 * @return
		 */
		IBinaryObject getBinarObject();
	}

	/**
	 * Creates an IBinaryFile.
	 * @param hints - array byte that can be use to recognise the file.
	 *     Can be null or empty array when no hints are passed.
	 * @param path
	 * @return
	 * @throws IOException
	 */
	IBinaryFile getBinary(byte[] hints, IPath path) throws IOException;

	/**
	 * Creates an IBinaryFile.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	IBinaryFile getBinary(IPath path) throws IOException;

	/**
	 * Returns the name of the Format.
	 * @return
	 */
	String getFormat();

	/**
	 * True if the resource is a binary. 
	 * @param hints
	 * @param path
	 * @return
	 */
	boolean isBinary(byte[] hints, IPath path);

	/**
	 * Get a hint of the needed buffer size to recognise the file.
	 * @return
	 */
	int getHintBufferSize();
}
