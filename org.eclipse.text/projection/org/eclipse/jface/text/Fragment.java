/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;

import org.eclipse.jface.text.Position;

/**
 * Fragment.java
 */
public class Fragment extends Position {
		
	private Position fOrigin;

	public Fragment(int offset, int length, Position origin) {
		super(offset, length);
		fOrigin= origin;
	}
	
//	public Fragment(int offset, int length) {
//		super(offset, length);
//	}
	
	/**
	 * Returns the fOrigin.
	 * @return Position
	 */
	public Position getOrigin() {
		return fOrigin;
	}
	
	public void setOrigin(Position origin) {
		fOrigin= origin;
	}
}
