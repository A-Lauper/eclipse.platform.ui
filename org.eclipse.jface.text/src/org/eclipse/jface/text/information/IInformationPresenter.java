/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.information;


import org.eclipse.jface.text.ITextViewer;


/**
 * An information presenter shows information available at the text viewer's
 * current document position. An <code>IInformationPresenter</code> is a 
 * <code>ITextViewer</code> add-on.<p>
 * An information presenters has a list of  <code>IInformationProvider</code> objects 
 * each of which is registered for a  particular document content type. 
 * The presenter uses the strategy objects to retrieve the information to present.<p>
 * The interface can be implemented by clients. By default, clients use
 * <code>InformationPresenter</code> as the standard implementer of this interface. 
 *
 * @see ITextViewer
 * @see IInformationProvider
 * @since 2.0
 */
public interface IInformationPresenter {
	
	/**
	 * Installs the information presenter on the given text viewer. After this method has been
	 * finished, the presenter is operational. I.e., the method <code>showInformation</code>
	 * can be called until <code>uninstall</code> is called.
	 * 
	 * @param textViewer the viewer on which the presenter is installed
	 */
	void install(ITextViewer textViewer);
	
	/**
	 * Removes the information presenter from the text viewer it has previously been
	 * installed on. 
	 */
	void uninstall();
	
	/**
	 * Shows information related to the cursor position of the text viewer
	 * this information presenter is installed on.
	 *
	 * @return an optional error message if 
	 */
	void showInformation();
	
	/**
	 * Returns the information provider to be used for the given content type.
	 *
	 * @param contentType the type of the content for which information will be requested
	 * @return an information provider or
	 *         <code>null</code> if none exists for the specified content type
	 */
	IInformationProvider getInformationProvider(String contentType);
}

