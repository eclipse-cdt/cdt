package org.eclipse.cdt.utils.elf.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.internal.core.model.parser.ElfBinaryArchive;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 */
public class GNUElfParser extends AbstractCExtension implements IBinaryParser {

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getBinary(IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		if (path == null) {
			path = new Path("");
		}
		IBinaryFile binary = null;
		try {
			Elf.Attribute attribute = Elf.getAttributes(path.toOSString());
			if (attribute != null) {
				switch (attribute.getType()) {
					case Attribute.ELF_TYPE_EXE :
						binary = new BinaryExecutable(path);
						break;

					case Attribute.ELF_TYPE_SHLIB :
						binary = new BinaryShared(path);
						break;

					case Attribute.ELF_TYPE_OBJ :
						binary = new BinaryObject(path);
						break;

					case Attribute.ELF_TYPE_CORE :
						BinaryObject obj = new BinaryObject(path);
						obj.setType(IBinaryFile.CORE);
						binary = obj;
						break;
				}
			}
		} catch (IOException e) {
			binary = new ElfBinaryArchive(path);
		}
		return binary;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return "ELF";
	}

	String getAddr2LinePath() {
		ICExtensionReference ref = getExtensionReference();
		return ref.getExtensionData("addr2line");
	}

	String getCPPFiltPath() {
		ICExtensionReference ref = getExtensionReference();
		return ref.getExtensionData("c++filt");
	}
}
