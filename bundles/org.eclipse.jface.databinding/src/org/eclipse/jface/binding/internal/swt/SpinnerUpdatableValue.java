/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.binding.internal.swt;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IChangeListener;
import org.eclipse.jface.binding.UpdatableValue;
import org.eclipse.jface.binding.swt.SWTBindingConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 3.2
 * 
 */
public class SpinnerUpdatableValue extends UpdatableValue {

	private final Spinner spinner;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param spinner
	 * @param attribute
	 */
	public SpinnerUpdatableValue(Spinner spinner, String attribute) {
		this.spinner = spinner;
		this.attribute = attribute;
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			spinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(null, IChangeEvent.CHANGE, null, null);
					}
				}
			});
		} else if (!attribute.equals(SWTBindingConstants.MIN)
				&& !attribute.equals(SWTBindingConstants.MAX)) {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void setValue(Object value, IChangeListener listenerToOmit) {
		int oldValue;
		int newValue;
		try {
			updating = true;
			newValue = ((Integer) value).intValue();
			if (attribute.equals(SWTBindingConstants.SELECTION)) {
				oldValue = spinner.getSelection();
				spinner.setSelection(newValue);
			} else if (attribute.equals(SWTBindingConstants.MIN)) {
				oldValue = spinner.getMinimum();
				spinner.setMinimum(newValue);
			} else if (attribute.equals(SWTBindingConstants.MAX)) {
				oldValue = spinner.getMaximum();
				spinner.setMaximum(newValue);
			} else {
				throw new AssertionError("invalid attribute name"); //$NON-NLS-1$
			}
			fireChangeEvent(listenerToOmit, IChangeEvent.CHANGE, new Integer(
					oldValue), new Integer(newValue));
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		int value = 0;
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			value = spinner.getSelection();
		} else if (attribute.equals(SWTBindingConstants.MIN)) {
			value = spinner.getMinimum();
		} else if (attribute.equals(SWTBindingConstants.MAX)) {
			value = spinner.getMaximum();
		}
		return new Integer(value);
	}

	public Class getValueType() {
		return Integer.class;
	}

}
