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

package org.eclipse.text.tests;

import org.eclipse.text.tests.link.LinkTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * 
 * @since 3.0
 */
public class EclipseTextTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Suite for for org.eclipse.text"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTest(LineTrackerTest4.suite());
		suite.addTest(DocumentExtensionTest.suite());
		suite.addTest(LineTrackerTest3.suite());
		suite.addTest(DocumentTest.suite());
		suite.addTest(PositionUpdatingCornerCasesTest.suite());
		suite.addTest(TextEditTests.suite());
		suite.addTest(GapTextTest.suite());
		suite.addTest(ChildDocumentTest.suite());
		//$JUnit-END$
		
		suite.addTest(ProjectionTestSuite.suite());
		suite.addTest(LinkTestSuite.suite());
		
		return suite;
	}
}
