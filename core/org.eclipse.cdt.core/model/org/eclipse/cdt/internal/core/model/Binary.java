package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class Binary extends Openable implements IBinary {

	public Binary(ICElement parent, IFile file) {
		this(parent, file.getLocation());
	}

	public Binary(ICElement parent, IPath path) {
		super (parent, path, ICElement.C_BINARY);
	}

	public Binary(ICElement parent, IFile file, String name) {
		super(parent, file, name, ICElement.C_BINARY);
	}

	public boolean hasDebug () {
		return ((BinaryInfo)getElementInfo()).hasDebug();
	}

	public boolean isExecutable() {
		return ((BinaryInfo)getElementInfo()).isExecutable();
	}

	public boolean isObject() {
		return ((BinaryInfo)getElementInfo()).isObject();
	}

	public boolean isSharedLib() {
		return ((BinaryInfo)getElementInfo()).isSharedLib();
	}

	public boolean isCore() {
		return ((BinaryInfo)getElementInfo()).isCore();
	}

	public String [] getNeededSharedLibs() {
		return ((BinaryInfo)getElementInfo()).getNeededSharedLibs();
	}

	public String getCPU() {
		return ((BinaryInfo)getElementInfo()).getCPU();
	}

	public long getText() {
		return ((BinaryInfo)getElementInfo()).getText();
	}

	public long getData() {
		return ((BinaryInfo)getElementInfo()).getData();
	}

	public long getBSS() {
		return ((BinaryInfo)getElementInfo()).getBSS();
	}

	public String getSoname() {
		return ((BinaryInfo)getElementInfo()).getSoname();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinary#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		return  ((BinaryInfo)getElementInfo()).isLittleEndian();
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
		IBinaryObject bin = getBinaryObject(res);
		if (bin != null) {
			Map hash = new HashMap();
			ISymbol[] symbols = bin.getSymbols();
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
			if (info instanceof BinaryInfo) {
				((BinaryInfo)info).loadInfo(bin);
			}
		} else {
			return false;
		}
		return true;
	}

	protected IBinaryObject getBinaryObject(IResource res) {
		IBinaryObject binary = null;
		IBinaryParser parser = null;
		IProject project = null;
		if (res != null) {
			project = res.getProject();
		}
		if (project != null) {
			parser = CModelManager.getDefault().getBinaryParser(project);
		}
		if (parser != null) {
			try {
				IPath path = res.getLocation();
				IBinaryFile bfile = parser.getBinary(path);
				if (bfile instanceof IBinaryObject) {
					binary = (IBinaryObject) bfile;
				}
			} catch (IOException e) {
			}
		}
		return binary;
	}

	private void addFunction(OpenableInfo info, ISymbol symbol, Map hash) {
		ICElement parent = this;
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

					protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm,
						Map newElements, IResource underlyingResource) throws CModelException {
							return true;
					}
				};
				hash.put(path, tu);
				info.addChild(tu);
			}
			function = new Function(tu, symbol.getName());
			tu.addChild(function);
		} else {
			function = new Function(parent, symbol.getName());
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
		ICElement parent = this;
		Variable variable = null;
		// Addr2line returns the funny "??" when it can not find the file.
		if (filename != null && !filename.equals("??")) {
			TranslationUnit tu = null;
			IPath path = new Path(filename);
			if (hash.containsKey(path)) {
				tu = (TranslationUnit) hash.get(path);
			} else {
				tu = new TranslationUnit(parent, path) {
					ArrayList array = new ArrayList(5);
					public void addChild(ICElement e) {
						array.add(e);
						array.trimToSize();
					}
						
					public ICElement [] getChildren() {
						return (ICElement[])array.toArray(new ICElement[0]);
					}

					protected boolean generateInfos(OpenableInfo info, IProgressMonitor pm,
						Map newElements, IResource underlyingResource) throws CModelException {
							return true;
					}
				};
				hash.put(path, tu);
				info.addChild(tu);
			}
			variable = new Variable(tu, symbol.getName());
			tu.addChild(variable);
		} else {
			variable = new Variable(parent, symbol.getName());
			info.addChild(variable);
		}
		//if (variable != null) {
		//	if (!external) {
		//		variable.getVariableInfo().setAccessControl(IConstants.AccStatic);
		//	}
		//}
	}

}
