/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ISymbolReader;

public class CodeViewReader implements ISymbolReader {

	RandomAccessFile file;
	int cvData;
	boolean isLe;
	List fileList;
	String[] files = null;
	boolean parsed = false;
	
	public CodeViewReader(RandomAccessFile accessFile, int dataOffset, boolean littleEndian) {
		file = accessFile;
		cvData = dataOffset;
		isLe = littleEndian;
		
		fileList = new ArrayList();
	}

	public String[] getSourceFiles() {
		if (!parsed) {
			try {
				parse();				
			} catch (IOException e) {
			}

			parsed = true;

			files = new String[fileList.size()];
			for (int i = 0; i < fileList.size(); i++) {
				files[i] = (String)fileList.get(i);
			}
		}

		return files;
	}

	private int getInt(int value) {
		if (isLe) {
			int tmp = 0;

			for (int i = 0; i < 4; i++) {
				tmp <<= 8;
				tmp |= value & 0xFF;
				value >>= 8;
			}
			
			return tmp;
		} else {
			return value;
		}
	}
	
	private short getShort(short value) {
		if (isLe) {
			short tmp = value;

			tmp &= 0xFF;
			tmp <<= 8;
			tmp |= (value >> 8) & 0xFF;
			
			return tmp;
		} else {
			return value;
		}
	}

	private void parse() throws IOException {
		if (cvData <= 0)
			return;
		
		// seek to the start of the CodeView data
		file.seek(cvData);
		
		// skip the next four bytes - signature "NB11"
		file.skipBytes(4);

		// get the offset to the subsection directory
		int subsectionDirOffset = getInt(file.readInt());

		// seek to the start of the subsection directory
		file.seek(cvData + subsectionDirOffset);

		// skip the header length (2) and directory entry length (2)
		file.skipBytes(4);

		// loop through the directories looking for source files
		int directoryCount = getInt(file.readInt());

		// skip the rest of the header
		file.skipBytes(8);

		// save the file offset to the base of the directories
		long directoryOffset = file.getFilePointer();

		for (int i = 0; i < directoryCount; i++) {
			// seek to the next directory
			file.seek(directoryOffset + i*12);

			// get the type of the subsection.  we only care about source modules
			short subsectionType = getShort(file.readShort());
			if (0x127 == subsectionType) {
				
				// skip the module index
				file.skipBytes(2);
				
				// get the offset from the base address
				int subsectionOffset = getInt(file.readInt());
				
				// seek to the start of the source module section
				file.seek(cvData + subsectionOffset);
				
				// get the number of source files
				short fileCount = getShort(file.readShort());
				
				// skip the number of segments
				file.skipBytes(2);
				
				// save the file offset to the array of base offsets
				long arrayOffset = file.getFilePointer();
				
				// loop through the files and add them to our list
				for (int j = 0; j < fileCount; j++) {
					// seek to the correct array entry
					file.seek(arrayOffset + j*4);

					// get the offset to the first entry and seek to it
					int offset = getInt(file.readInt());
					file.seek(cvData + subsectionOffset + offset);
					
					// get the number of segments
					short segments = getShort(file.readShort());
					
					// now skip to the name length
					file.skipBytes(2 + segments*4 + segments*8);
					byte nameLength = file.readByte();

					// now extract the filename and add it to our list
					// if it's not already there
					byte[] nameBuffer = new byte[nameLength];
					file.readFully(nameBuffer);
					String name = new String(nameBuffer);

					if (!fileList.contains(name))
						fileList.add(name);
				}
			}
		}
	}
}
