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
package org.eclipse.jface.text;


/**
 * Extension interface for <code>ITextViewer</code>. Extends <code>ITextViewer</code> with the explicit
 * concept of model and widget coordinates.  For example, a selection returned by the text viewer's control is
 * a widget selection. A widget selection always maps to a certain range of the viewer's document. This
 * range is considered the model selection.<p> 
 * This general concepts replaces the notion of <code>visible region</code>.
 * 
 * @since 2.1
 * @deprecated completely replaced by <code>ITextViewerExtension5</code>
 */
public interface ITextViewerExtension3 {
	
	
	/**
	 * Returns the minimal region of the viewer's document that completely comprises everything that is
	 * visible in the viewer's widget.
	 * 
	 * @return the minimal region of the viewer's document comprising the contents of the viewer's widget
	 */
	IRegion getModelCoverage();
	
	
	/**
	 * Returns the widget line that corresponds to the given line of the viewer's document or <code>-1</code> if there is no such line.
	 * 
	 * @param modelLine the line of the viewer's document
	 * @return the corresponding widget line or <code>-1</code>
	 */
	int modelLine2WidgetLine(int modelLine);

	/**
	 * Returns the widget offset that corresponds to the given offset in the viewer's document
	 * or <code>-1</code> if there is no such offset
	 * 
	 * @param modelOffset the offset in the viewer's document
	 * @return the corresponding widget offset or <code>-1</code>
	 */
	int modelOffset2WidgetOffset(int modelOffset);

	/**
	 * Returns the minimal region of the viewer's widget that completely comprises the given region of the
	 * viewer's document or <code>null</code> if there is no such region.
	 * 
	 * @param modelRange the region of the viewer's document
	 * @return the minimal region of the widget comprising <code>modelRange</code> or <code>null</code>
	 */
	IRegion modelRange2WidgetRange(IRegion modelRange);


	/**
	 * Returns the offset of the viewer's document that corresponds to the given widget offset
	 * or <code>-1</code> if there is no such offset
	 * 
	 * @param widgetOffset the widget offset
	 * @return the corresponding offset in the viewer's document or <code>-1</code>
	 */
	int widgetOffset2ModelOffset(int widgetOffset);
	
	/**
	 * Returns the minimal region of the viewer's document that completely comprises the given widget region
	 * or <code>null</code> if there is no such region.
	 * 
	 * @param widgetRange the widget region
	 * @return the minimal region of the viewer's document comprising <code>widgetlRange</code> or <code>null</code>
	 */
	IRegion widgetRange2ModelRange(IRegion widgetRange);

	/**
	 * Returns the line of the viewer's document that corresponds to the given widget line or <code>-1</code> if there is no such line.
	 * 
	 * @param widgetLine the widget line
	 * @return the corresponding line of the viewer's document or <code>-1</code>
	 */
	int widgetlLine2ModelLine(int widgetLine);
	
	/**
	 * Returns the widget line of the given widget offset.
	 * 
	 * @param widgetOffset the widget offset
	 * @return the widget line of the widget offset
	 */
	int widgetLineOfWidgetOffset(int widgetOffset);
}
