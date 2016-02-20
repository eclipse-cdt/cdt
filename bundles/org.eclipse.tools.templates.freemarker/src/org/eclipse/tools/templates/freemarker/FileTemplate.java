package org.eclipse.tools.templates.freemarker;

import javax.xml.bind.annotation.XmlAttribute;

public class FileTemplate {
	private String src;
	private String dest;
	private boolean open;
	private boolean show;
	private boolean copy;

	@XmlAttribute(name = "src")
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@XmlAttribute(name = "dest")
	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	@XmlAttribute(name = "open")
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@XmlAttribute(name = "show")
	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	@XmlAttribute(name = "copy")
	public boolean isCopy() {
		return copy;
	}

	public void setCopy(boolean copy) {
		this.copy = copy;
	}

}
