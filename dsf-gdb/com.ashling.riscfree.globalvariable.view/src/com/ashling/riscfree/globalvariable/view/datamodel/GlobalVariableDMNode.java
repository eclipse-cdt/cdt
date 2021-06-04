/**
 * 
 */
package com.ashling.riscfree.globalvariable.view.datamodel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 * @author vinod
 *
 */
public class GlobalVariableDMNode implements IAdaptable, IGlobalVariableDescriptor, Comparable<GlobalVariableDMNode> {

	private String fileName = null;
	private String fullname = null;
	private int line;
	private String name = null;
	private String type = null;
	private String description = null;

	public GlobalVariableDMNode(String fileName, String fullname, int line, String name, String type,
			String description) {
		this.fileName = fileName;
		this.fullname = fullname;
		this.line = line;
		this.name = name;
		this.type = type;
		this.description = description;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFullname() {
		return fullname;
	}

	public int getLine() {
		return line;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public int compareTo(GlobalVariableDMNode o) {
		if (this.getName().equalsIgnoreCase(o.getName()) && this.getFullname().equalsIgnoreCase(o.getFullname())) {
			return 0;
		}
		return this.getFullname().compareTo(o.getFullname());
	}

	@Override
	public IPath getPath() {
		return null;
	}

}
