package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 */
public interface IBinaryParser {

	/**
	 * Represents a binary file for example an ELF executable.
	 */
	interface IBinaryFile extends IAdaptable {
		static final int OBJECT = 0x1;
		static final int EXECUTABLE = 0x02;
		static final int SHARED = 0x04;
		static final int ARCHIVE = 0x08;
		static final int CORE = 0x10;

		IPath getPath();
		int getType();
		InputStream getContents();
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

		boolean hasDebug();

		String getCPU();

		long getText();

		long getData();

		long getBSS();
        
		boolean isLittleEndian();

		ISymbol[] getSymbols();

		ISymbol getSymbol(long addr);

		String getName();

	}

	/**
	 * An executable.
	 */
	interface IBinaryExecutable extends IBinaryObject {
		String[] getNeededSharedLibs();
	}

	/**
	 * A DLL.
	 */
	interface IBinaryShared extends IBinaryExecutable {
		String getSoName();
	}

	interface ISymbol extends Comparable {
		static final int FUNCTION = 0x01;
		static final int VARIABLE = 0x02;
	
		String getName();
		long getAddress();
		long getSize();
		int getStartLine();
		int getEndLine();
		IPath getFilename();
		int getType();
		int getLineNumber(long offset);
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
