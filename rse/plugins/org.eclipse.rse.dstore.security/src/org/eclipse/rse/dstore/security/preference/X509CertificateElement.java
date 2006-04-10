/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.dstore.security.preference;


import java.security.cert.X509Certificate;
import java.util.Date;


public class X509CertificateElement extends Element
{
	public static int CERT_NAME = 0;
	public static int CERT_UNIT = 1;
	public static int CERT_ORGANIZATION = 2;
	public static int CERT_CITY = 3;
	public static int CERT_PROVINCE = 4;
	public static int CERT_COUNTRY = 5;
		
	
	private X509Certificate _cert;
	public X509CertificateElement(String alias, String value, X509Certificate cert)
	{
		super(alias, value);
		_cert = cert;
	}
				
	public String getType()
	{
		return _cert.getType();
	}
	
	public String getVersion()
	{
		return "V." + _cert.getVersion();
	}
	
	private String[] parse(String full)
	{
		StringBuffer result = new StringBuffer();
		char[] chars = full.toCharArray();
		boolean inQuotes = false;
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (c == '\"')
			{
				inQuotes = !inQuotes;
			}
			else
			{
				if (c == ',')
				{
					if (!inQuotes)
					{
						c = ';';
					}
				}
			}
			result.append(c);			
		}
		return result.toString().split(";");
	}
	
	private String extract(String full, int index)
	{
		String[] pairs = parse(full);
		String match = pairs[index].split("=")[1];
		return match;
	}
	
	
	public String getIssuerDN()
	{
		String full = _cert.getIssuerDN().getName();
		return full;
	}
	
	public String getIssuerName()
	{
		String full = _cert.getIssuerDN().getName();
		return extract(full, CERT_NAME);
	}
	
	public String getIssuerUnit()
	{
		String full = _cert.getIssuerDN().getName();
		return extract(full, CERT_UNIT);
	}
	
	public String getIssuerOrg()
	{
		String full = _cert.getIssuerDN().getName();
		return extract(full, CERT_ORGANIZATION);
	}
	
	public String getIssuerCity()
	{
		String full = _cert.getIssuerDN().getName();
		return extract(full, CERT_CITY);
	}
	
	public String getIssuerProvince()
	{
		String full = _cert.getIssuerDN().getName();
		return extract(full, CERT_PROVINCE);
	}
	
	public String getIssuerCountry()
	{
		String full = _cert.getIssuerDN().getName();
		return extract(full, CERT_COUNTRY);
	}
	
	public String getSubjectDN()
	{
		String full = _cert.getSubjectDN().getName();
		return full;
	}
	
	public String getSubjectName()
	{
		String full = _cert.getSubjectDN().getName();
		return extract(full, CERT_NAME);
	}
	
	public String getSubjectUnit()
	{
		String full = _cert.getSubjectDN().getName();
		return extract(full, CERT_UNIT);
	}
	
	public String getSubjectOrg()
	{
		String full = _cert.getSubjectDN().getName();
		return extract(full, CERT_ORGANIZATION);
	}
	
	public String getSubjectCity()
	{
		String full = _cert.getSubjectDN().getName();
		return extract(full, CERT_CITY);
	}
	
	public String getSubjectProvince()
	{
		String full = _cert.getSubjectDN().getName();
		return extract(full, CERT_PROVINCE);
	}
	
	public String getSubjectCountry()
	{
		String full = _cert.getSubjectDN().getName();
		return extract(full, CERT_COUNTRY);
	}
	

	

	
	public String getNotBefore()
	{
		return _cert.getNotBefore().toString();
	}
	
	public String getNotAfter()
	{
		return _cert.getNotAfter().toString();
	}
	
	public String getExpirationDate()
	{
		Date date = _cert.getNotAfter();
		return date.toString();
	}
	
	
	public String getSigAlgName()
	{
		return _cert.getSigAlgName();
	}
	
	public String getSerialNumber()
	{
		return _cert.getSerialNumber().toString();
	}
	
	public String getAlgorithm()
	{
		return _cert.getPublicKey().getAlgorithm();
	}
	
	public String getFormat()
	{
		return _cert.getPublicKey().getFormat();
	}
	
	public Object getCert()
	{
		return _cert;
	}
}