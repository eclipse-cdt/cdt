package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

class BinaryInfo extends CFileInfo {

	IBinaryObject binary;
	Map hash;

	public BinaryInfo(CElement element) {
		super(element);
	}

	public boolean isBinary() {
		return true;
	}

	public ICElement[] getChildren() {
		if (hasChanged()) {
			if (hash == null) {
				hash = new HashMap();
			}
			hash.clear();
			removeChildren();
			setIsStructureKnown(true);
			IBinaryObject bin = getBinaryObject();
			ISymbol[] symbols = bin.getSymbols();
			for (int i = 0; i < symbols.length; i++) {
				switch (symbols[i].getType()) {
					case ISymbol.FUNCTION :
						addFunction(symbols[i]);
						break;

					case ISymbol.VARIABLE :
						addVariable(symbols[i]);
						break;
				}
			}
		}
		return super.getChildren();
	}

	public String getCPU() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getCPU();
		}
		return "";
	}

	public boolean isSharedLib() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getType() == IBinaryObject.SHARED;
		}
		return false;
	}

	public boolean isExecutable() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getType() == IBinaryObject.EXECUTABLE;
		}
		return false;
	}

	public boolean isObject() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getType() == IBinaryObject.OBJECT;
		}
		return false;
	}

	public boolean isCore() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getType() == IBinaryObject.CORE;
		}
		return false;
	}

	public boolean hasDebug() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.hasDebug();
		}
		return false;
	}

	public String[] getNeededSharedLibs() {
		if (isExecutable()) {
			IBinaryExecutable exec = (IBinaryExecutable) getBinaryObject();
			return exec.getNeededSharedLibs();
		}
		return new String[0];
	}

	public long getText() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getText();
		}
		return 0;
	}

	public long getData() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getData();
		}
		return 0;
	}

	public long getBSS() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.getBSS();
		}
		return 0;
	}

	public String getSoname() {
		if (isSharedLib()) {
			IBinaryShared shared = (IBinaryShared) getBinaryObject();
			return shared.getSoName();
		}
		return "";
	}

	public boolean isLittleEndian() {
		IBinaryObject bin = getBinaryObject();
		if (bin != null) {
			return bin.isLittleEndian();
		}
		return false;
	}

	IBinaryObject getBinaryObject() {
		if (binary == null) {
			IProject project = getElement().getCProject().getProject();
			IBinaryParser parser = CModelManager.getDefault().getBinaryParser(project);
			if (parser != null) {
				try {
					IFile file = (IFile) getElement().getUnderlyingResource();
					IBinaryFile bfile = parser.getBinary(file);
					if (bfile instanceof IBinaryObject) {
						binary = (IBinaryObject) bfile;
					}
				} catch (CModelException e) {
				} catch (IOException e) {
				}
			}
		}
		return binary;
	}

	private void addFunction(ISymbol symbol) {
		ICElement parent = getElement();
		String filename = filename = symbol.getFilename();
		Function function = null;

		// Addr2line returns the funny "??" when it can find the file.
		if (filename != null && !filename.equals("??")) {
			TranslationUnit tu = null;
			IPath path = new Path(filename);
			if (hash.containsKey(path)) {
				tu = (TranslationUnit) hash.get(path);
			} else {
				// A special ITranslationUnit we do not want the file to be parse.
				tu = new TranslationUnit(parent, path) {
					ArrayList array = new ArrayList(5);
					public void addChild(ICElement e) {
						array.add(e);
						array.trimToSize();
					}
						
					public ICElement [] getChildren() {
						return (ICElement[])array.toArray(new ICElement[0]);
					}
				};
				hash.put(path, tu);
				addChild(tu);
			}
			function = new Function(tu, symbol.getName());
			function.setLines(symbol.getLineNumber(), symbol.getLineNumber());
			tu.addChild(function);
		} else {
			function = new Function(parent, symbol.getName());
			addChild(function);
		}
		//		if (function != null) {
		//			if (!external) {
		//				function.getFunctionInfo().setAccessControl(IConstants.AccStatic);
		//			}
		//		}
	}

	private void addVariable(ISymbol symbol) {
		String filename = filename = symbol.getFilename();
		ICElement parent = getElement();
		Variable variable = null;
		// Addr2line returns the funny "??" when it can not find the file.
		if (filename != null && !filename.equals("??")) {
			TranslationUnit tu = null;
			IPath path = new Path(filename);
			if (hash.containsKey(path)) {
				tu = (TranslationUnit) hash.get(path);
			} else {
				tu = new TranslationUnit(parent, path);
				hash.put(path, tu);
				addChild(tu);
			}
			variable = new Variable(tu, symbol.getName());
			variable.setLines(symbol.getLineNumber(), symbol.getLineNumber());
			tu.addChild(variable);
		} else {
			variable = new Variable(parent, symbol.getName());
			addChild(variable);
		}
		//if (variable != null) {
		//	if (!external) {
		//		variable.getVariableInfo().setAccessControl(IConstants.AccStatic);
		//	}
		//}
	}

}
