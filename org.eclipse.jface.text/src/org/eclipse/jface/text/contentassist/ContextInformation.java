package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.util.Assert;


/**
 * A default implementation of the <code>IContextInformation</code> interface.
 */
public final class ContextInformation implements IContextInformation {
	
	private String fContextDisplayString;
	private String fInformationDisplayString;
	private Image fImage;

	/**
	 * Creates a new context information without an image.
	 *
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information
	 */
	public ContextInformation(String contextDisplayString, String informationDisplayString) {
		this(null, contextDisplayString, informationDisplayString);
	}

	/**
	 * Creates a new context information with an image.
	 *
	 * @param image the image to display when presenting the context information
	 * @param contextDisplayString the string to be used when presenting the context
	 * @param informationDisplayString the string to be displayed when presenting the context information,
	 *		may not be <code>null</code>
	 */
	public ContextInformation(Image image, String contextDisplayString, String informationDisplayString) {
		
		Assert.isNotNull(informationDisplayString);
		
		fImage= image;
		fContextDisplayString= contextDisplayString;
		fInformationDisplayString= informationDisplayString;
	}

	/*
	 * @see IContextInformation#equals
	 */
	public boolean equals(Object object) {
		if (object instanceof IContextInformation) {
			IContextInformation contextInformation= (IContextInformation) object;
			boolean equals= fInformationDisplayString.equalsIgnoreCase(contextInformation.getInformationDisplayString());
			if (fContextDisplayString != null) 
				equals= equals && fContextDisplayString.equalsIgnoreCase(contextInformation.getContextDisplayString());
			return equals;
		}
		return false;
	}
	
	/*
	 * @see IContextInformation#getInformationDisplayString()
	 */
	public String getInformationDisplayString() {
		return fInformationDisplayString;
	}
	
	/*
	 * @see IContextInformation#getImage()
	 */
	public Image getImage() {
		return fImage;
	}
	
	/*
	 * @see IContextInformation#getContextDisplayString()
	 */
	public String getContextDisplayString() {
		if (fContextDisplayString != null)
			return fContextDisplayString;
		return fInformationDisplayString;
	}
}
