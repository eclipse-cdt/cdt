package org.eclipse.rse.services.dstore.util;

import org.eclipse.dstore.core.model.DataElement;

public class FileSystemMessageUtil 
{

	/**
	 * Returns the source message (first part of the source attribute) for this element.
	 *
	 * @return the source message
	 */
	public static String getSourceMessage(DataElement element)
	{
		String source = element.getSource();
		if (source == null) return null;
		if (source.equals("")) return "";
		int sepIndex = source.indexOf("|");
		if (sepIndex == -1) return source;
		else return source.substring(0, sepIndex);
	}

	/**
	 * Returns the source location (second part of the source attribute) for this element.
	 *
	 * @return the source location
	 */
	public static String getSourceLocation(DataElement element)
	{
		String source = element.getSource();
		if (source == null) return null;
		if (source.equals("")) return "";
		int sepIndex = source.indexOf("|");
		if (sepIndex == -1) return "";
		else return source.substring(sepIndex+1);
	}
}
