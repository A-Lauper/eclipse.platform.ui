/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * @since 2.1
 */
public interface IOverviewRuler extends IVerticalRuler {
	
	/**
	 * Returns whether there is an annotation an the given y coordinate. This
	 * method takes the compression factor of the vertical ruler into account.
	 * 
	 * @param y
	 * @return <code>true</code> if there is an annotation, <code>false</code>
	 * 	otherwise
	 */
	boolean hasAnnotation(int y);
	
	/**
	 * Returns the height of the visual presentation of an annotation in this
	 * overview ruler. Assumes that all annotations are represented using the
	 * same height.
	 * 
	 * @return int the visual height of an annotation
	 */
	int getAnnotationHeight();
	
	/**
	 * Sets the color for the given annotation type in this overview ruler.
	 * @param annotationType the annotation type
	 * @param color the color
	 */
	void setAnnotationTypeColor(Object annotationType, Color color);
	
	/**
	 * Sets the drawing layer for the given annotation type in this overview
	 * ruler.
	 * @param annotationType the annotation type
	 * @param layer the drawing layer
	 */
	void setAnnotationTypeLayer(Object annotationType, int layer);
	
	/**
	 * Adds the given annotation type to this overview ruler. Starting with this
	 * call, annotations of the given type are shown in the overview ruler.
	 * @param annotationType the annotation type
	 */
	void addAnnotationType(Object annotationType);
	
	/**
	 * Removes the given annotation type from this overview ruler. Annotations
	 * of the given type are no longer shown in the overview ruler.
	 */
	void removeAnnotationType(Object annotationType);
	
	/**
	 * Adds the given annotation type to the header of this ruler. Starting with
	 * this call, the presence of annotations is tracked and the header is drawn
	 * in the configured color.
	 * 
	 * @param annotationType the annotation type to be tracked
	 */
	void addHeaderAnnotationType(Object annotationType);
	
	/**
	 * Removes the given annotation type from the header of this ruler. The
	 * presence of annotations of the given type is no longer tracked and the
	 * header is drawn in the default color, depending on the other configured
	 * configured annotation types.
	 * 
	 * @param annotationType the annotation type to be removed
	 */
	void removeHeaderAnnotationType(Object annotationType);
	
	/**
	 * Sets the color of the header. The header is drawn in this color if there
	 * are annotations of any of the annotation types added to the header.
	 * 
	 * @param color
	 */
	void setHeaderColor(Color color);
	
	/**
	 * Returns this rulers header control. This is the little area between the
	 * top of the text widget and the top of this overview ruler.
	 * @return the header control of this overview ruler.
 	 */
	Control getHeaderControl();
}
