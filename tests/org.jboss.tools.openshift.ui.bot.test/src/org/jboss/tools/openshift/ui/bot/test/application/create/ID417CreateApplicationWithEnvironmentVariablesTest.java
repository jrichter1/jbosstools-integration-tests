/*******************************************************************************
 * Copyright (c) 2007-2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.ui.bot.test.application.create;

import static org.junit.Assert.fail;

import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.tools.openshift.reddeer.utils.DatastoreOS2;
import org.jboss.tools.openshift.reddeer.utils.OpenShiftLabel;
import org.jboss.tools.openshift.reddeer.utils.v2.DeleteUtils;
import org.jboss.tools.openshift.reddeer.view.OpenShiftExplorerView;
import org.jboss.tools.openshift.reddeer.wizard.v2.NewOpenShift2ApplicationWizard;
import org.junit.After;
import org.junit.Test;

/**
 * Test capabilities of creating an application with environment variables.
 * 
 * @author mlabuda@redhat.com
 *
 */
public class ID417CreateApplicationWithEnvironmentVariablesTest {

	private String applicationName = "diy" + System.currentTimeMillis();
	
	@Test
	public void testCreateApplicationWithEnvironmentVariables() {
		NewOpenShift2ApplicationWizard wizard = new NewOpenShift2ApplicationWizard(DatastoreOS2.USERNAME,
				DatastoreOS2.SERVER, DatastoreOS2.DOMAIN);
		wizard.openWizardFromExplorer();
		wizard.createNewApplicationOnBasicCartridge(
				OpenShiftLabel.Cartridge.DIY, applicationName, false,
				true, true, false, null, null, true, null, null, null, (String[]) null);
		
		wizard.postCreateSteps(true);
		
		OpenShiftExplorerView explorer = new OpenShiftExplorerView();
		explorer.getOpenShift2Connection(DatastoreOS2.USERNAME, DatastoreOS2.SERVER).getDomain(DatastoreOS2.DOMAIN).
			getApplication(applicationName).select();
		
		new ContextMenu(OpenShiftLabel.ContextMenu.SHOW_ENV_VARS).select();
		
		try {
			new WaitUntil(new ConsoleHasText("varname=varvalue"), TimePeriod.LONG);
		} catch (WaitTimeoutExpiredException ex) {
			fail("Environment variable has not been created correctly while application"
					+ "was creating. There is no user defined environment variable.");
		}
	}
	
	@After
	public void deleteApplication() {
		new DeleteUtils(DatastoreOS2.USERNAME, DatastoreOS2.SERVER, DatastoreOS2.DOMAIN, applicationName,
				applicationName).perform();
	}
}
