/*******************************************************************************
 * Copyright (c) 2000, 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


/**
 * Manages SWT color objects. Until the <code>dispose</code> method is called,
 * the same color object is returned for equal <code>RGB</code> values.
 * <p> This interface may be implemented by clients. </p>
 */
public interface ISharedTextColors {
		
	/**
	 * Returns the color object for the value represented by the given
	 * <code>RGB</code> object.
	 *
	 * @param rgb the rgb color specification
	 * @return the color object for the given rgb value
	 */
	Color getColor(RGB rgb);	
	
	/**
	 * Tells this color manager that the managed colors can be disposed.
	 */
	void dispose();
}
