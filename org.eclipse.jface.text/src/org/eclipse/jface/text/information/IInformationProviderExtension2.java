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

import org.eclipse.jface.text.IInformationControlCreator;

/**
 * Extension interface for <code>IInformationProvider</code> to provide
 * its own information control creator.
 * 
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.information.IInformationProvider
 * @since 3.0
 */
public interface IInformationProviderExtension2 {
	
	/**
	 * Returns the information control creator of this information provider.
	 * 
	 * @return the information control creator
	 */
	IInformationControlCreator getInformationPresenterControlCreator();
}
