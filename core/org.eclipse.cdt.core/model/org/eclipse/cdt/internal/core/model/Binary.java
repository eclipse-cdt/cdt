package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class Binary extends Openable implements IBinary {

	IBinaryFile binaryFile;

	public Binary(ICElement parent, IFile file, IBinaryFile bin) {
		super(parent, file, ICElement.C_BINARY);
		binaryFile = bin;
	}

	public Binary(ICElement parent, IPath path, IBinaryFile bin) {
		super (parent, path, ICElement.C_BINARY);
		binaryFile = bin;
	}


	public boolean isSharedLib() {
		if (binaryFile != null) {
			return binaryFile.getType() == IBinaryObject.SHARED;
		}
		return false;
	}

	public boolean isExecutable() {
		if (binaryFile != null) {
			return binaryFile.getType() == IBinaryObject.EXECUTABLE;
		}
		return false;
	}

	public boolean isObject() {
		if (binaryFile != null) {
			return binaryFile.getType() == IBinaryObject.OBJECT;
		}
		return false;
	}

	public boolean isCore() {
		if (binaryFile != null) {
			return binaryFile.getType() == IBinaryObject.CORE;
		}
		return false;
	}

	public boolean hasDebug() {
		if (isObject() || isExecutable() || isSharedLib()) {
			return ((IBinaryObject)binaryFile).hasDebug();
		}
		return false;
	}

	public String getCPU() {
		if (isObject() || isExecutable() || isSharedLib() || isCore()) {
			return ((IBinaryObject)binaryFile).getCPU();
		}
		return "";
	}

	public String[] getNeededSharedLibs() {
		if (isExecutable()) {
			return ((IBinaryExecutable)binaryFile).getNeededSharedLibs();
		}
		return new String[0];
	}

	public long getText() {
		if (isObject() || isExecutable() || isSharedLib()) {
			return ((IBinaryObject)binaryFile).getText();
		}
		return 0;
	}

	public long getData() {
		if (isObject() || isExecutable() || isSharedLib()) {
			return ((IBinaryObject)binaryFile).getData();
		}
		return 0;
	}

	public long getBSS() {
		if (isObject() || isExecutable() || isSharedLib()) {
			return ((IBinaryObject)binaryFile).getBSS();
		}
		return 0;
	}

	public String getSoname() {
		if (isSharedLib()) {
			return ((IBinaryShared)binaryFile).getSoName();
		}
		return "";
	}

	public boolean isLittleEndian() {
		if (isObject() || isExecutable() || isSharedLib() || isCore()) {
			return ((IBinaryObject)binaryFile).isLittleEndian();
		}
		return false;
	}

	public CElementInfo createElementInfo() {
		return new BinaryInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#generateInfos(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		CModelManager.getDefault().putInfo(this, info);
		return computeChildren(info, underlyingResource);
	}


	boolean computeChildren(OpenableInfo info, IResource res) {
		if (isObject() || isExecutable() || isSharedLib()) {
			Map hash = new HashMap();
			ISymbol[] symbols = ((IBinaryObject)binaryFile).getSymbols();
			for (int i = 0; i < symbols.length; i++) {
				switch (symbols[i].getType()) {
					case ISymbol.FUNCTION :
						addFunction(info, symbols[i], hash);
					break;

					case ISymbol.VARIABLE :
						addVariable(info, symbols[i], hash);
					break;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private void addFunction(OpenableInfo info, ISymbol symbol, Map hash) {
		String filename = filename = symbol.getFilename();
		BinaryFunction function = null;

		// Addr2line returns the funny "??" when it can find the file.
		if (filename != null && !filename.equals("??")) {
			BinaryModule module = null;
			IPath path = new Path(filename);
			if (hash.containsKey(path)) {
				module = (BinaryModule)hash.get(path);
			} else {
				// A special container we do not want the file to be parse.
				module = new BinaryModule(this, path);
				hash.put(path, module);
				info.addChild(module);
			}
			function = new BinaryFunction(module, symbol.getName(), symbol.getAddress());
			function.setLines(symbol.getStartLine(), symbol.getEndLine());
			module.addChild(function);
		} else {
			//function = new Function(parent, symbol.getName());
			function = new BinaryFunction(this, symbol.getName(), symbol.getAddress());
			function.setLines(symbol.getStartLine(), symbol.getEndLine());
			info.addChild(function);
		}
		//		if (function != null) {
		//			if (!external) {
		//				function.getFunctionInfo().setAccessControl(IConstants.AccStatic);
		//			}
		//		}
	}

	private void addVariable(OpenableInfo info, ISymbol symbol, Map hash) {
		String filename = filename = symbol.getFilename();
		BinaryVariable variable = null;
		// Addr2line returns the funny "??" when it can not find the file.
		if (filename != null && !filename.equals("??")) {
			BinaryModule module = null;
			IPath path = new Path(filename);
			if (hash.containsKey(path)) {
				module = (BinaryModule)hash.get(path);
			} else {
				module = new BinaryModule(this, path);
				hash.put(path, module);
				info.addChild(module);
			}
			variable = new BinaryVariable(module, symbol.getName(), symbol.getAddress());
			variable.setLines(symbol.getStartLine(), symbol.getEndLine());
			module.addChild(variable);
		} else {
			variable = new BinaryVariable(this, symbol.getName(), symbol.getAddress());
			variable.setLines(symbol.getStartLine(), symbol.getEndLine());
			info.addChild(variable);
		}
		
		//if (variable != null) {
		//	if (!external) {
		//		variable.getVariableInfo().setAccessControl(IConstants.AccStatic);
		//	}
		//}
	}

}
