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
	public interface IBinaryFile extends IAdaptable {
		public int OBJECT = 0x1;
		public int EXECUTABLE = 0x02;
		public int SHARED = 0x04;
		public int ARCHIVE = 0x08;
		public int CORE = 0x10;

		public IPath getPath();
		public int getType();
		public InputStream getContents();
	}

	/**
	 * Represents an archive.
	 */
	public interface IBinaryArchive extends IBinaryFile {
		public IBinaryObject[] getObjects();
	}

	/**
	 * Represents a binary, for example an ELF excutable.
	 */
	public interface IBinaryObject extends IBinaryFile {

		public boolean hasDebug();

		public String getCPU();

		public long getText();

		public long getData();

		public long getBSS();
        
		public boolean isLittleEndian();

		public ISymbol[] getSymbols();
		
		public String getName();

	}

	/**
	 * An executable.
	 */
	public interface IBinaryExecutable extends IBinaryObject {
		public String[] getNeededSharedLibs();
	}

	/**
	 * A DLL.
	 */
	public interface IBinaryShared extends IBinaryExecutable {
		public String getSoName();
	}

	public interface ISymbol {
		public int FUNCTION = 0x01;
		public int VARIABLE = 0x02;
	
		public String getName();
		public int getLineNumber();
		public String getFilename();
		public int getType();
	}

	public IBinaryFile getBinary(IPath path) throws IOException;
	
	public String getFormat();
}
