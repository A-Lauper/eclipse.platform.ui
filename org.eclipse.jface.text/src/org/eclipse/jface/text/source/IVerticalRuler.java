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

package org.eclipse.jface.text.source;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.ITextViewer;


/**
 * This interface defines a visual component which may serve text viewers as an annotation presentation 
 * area. Implementers of this interface have to define the presentation modus. This can either depend
 * on the connected viewer's view port or not. If the modus is view port dependent the ruler only shows 
 * those annotations that are attached to document regions that are visible in the view port. If independent,
 * the presented annotations can also be attached to invisible document regions.
 * 
 * This interfaces comprises three contracts:
 * <ul>
 * <li> The vertical ruler retrieves the annotations it presents from an annotation model.
 * <li> The ruler is a visual component which must be integrated in a hierarchy of SWT controls.
 * <li> The ruler provides interested clients with mapping and
 * 		interaction information. This covers the mapping between
 * 		coordinates of the ruler's control and line numbers based 
 * 		on the connected text viewer's document (<code>IVerticalRulerInfo</code>).
 * </ul>
 * Clients may implement this interface or use the default implementation provided
 * by <code>CompositeRuler</code> and <code>VerticalRuler</code>.
 *  
 * @see ITextViewer
 * @see IVerticalRulerInfo
 * @since 2.1
 */
public interface IVerticalRuler extends IVerticalRulerInfo {

	/**
	 * Associates an annotation model with this ruler.
	 * A value <code>null</code> is acceptable and clears the ruler.
	 *
	 * @param model the new annotation model, may be <code>null</code>
	 */
	void setModel(IAnnotationModel model);
		
	/**
	 * Returns the current annotation model of this ruler or <code>null</code>
	 * if the ruler has no model.
	 *
	 * @return this ruler's annotation model or <code>null</code> if there is no model
	 */
	IAnnotationModel getModel();
		
	/**
	 * Forces the vertical ruler to synchronize itself with its 
	 * annotation model and its viewer's viewport.
	 */
	void update();
	
	/**
	 * Creates the ruler's SWT control.
	 *
	 * @param parent the parent control of the ruler's control
	 * @param textViewer the text viewer to which this ruler belongs
	 * @return the ruler's SWT control
	 */
	Control createControl(Composite parent, ITextViewer textViewer);
}
