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
package org.jboss.tools.openshift.ui.bot.test.application.importing;

import static org.junit.Assert.assertTrue;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.jface.handler.TreeViewerHandler;
import org.eclipse.reddeer.swt.condition.ControlIsEnabled;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.BackButton;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.menu.ShellMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.tree.DefaultTree;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.openshift.reddeer.utils.DatastoreOS2;
import org.jboss.tools.openshift.reddeer.utils.OpenShiftLabel;
import org.jboss.tools.openshift.reddeer.utils.v2.DeleteUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test capabilities of importing an application via menu File - Import.
 * 
 * @author mlabuda@redhat.com
 *
 */
public class ID502ImportApplicationViaMenuTest {

	private String applicationName = "diy" + System.currentTimeMillis();
	
	@Before
	public void createApplicationDeleteProject() {
		ID501ImportApplicationViaExplorerTest.createApplicationWithoutServerAdapterAndProject(applicationName);
	}
	
	@Test
	public void testImportApplicationViaMenu() {
		TreeViewerHandler treeViewerHandler = TreeViewerHandler.getInstance();
		new WorkbenchShell().setFocus();
		new ShellMenu("File", "Import...").select();
		
		new DefaultShell("Import");
		
		new DefaultTreeItem("OpenShift", "Existing OpenShift Application").select();

		new NextButton().click();
		
		new WaitUntil(new ShellIsAvailable(OpenShiftLabel.Shell.IMPORT_APPLICATION));
		new DefaultShell(OpenShiftLabel.Shell.IMPORT_APPLICATION);
		
		for (String comboItem: new DefaultCombo(0).getItems()) {
			if (comboItem.contains(DatastoreOS2.USERNAME)) {
				new DefaultCombo(0).setSelection(comboItem);
				break;
			}
		}
		
		new NextButton().click();
		
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		
		new WaitUntil(new ControlIsEnabled(new BackButton()), TimePeriod.LONG);
		
		new DefaultShell(OpenShiftLabel.Shell.IMPORT_APPLICATION);
		
		new PushButton(OpenShiftLabel.Button.BROWSE).click();
		
		new DefaultShell(OpenShiftLabel.Shell.SELECT_EXISTING_APPLICATION);
		
		treeViewerHandler.getTreeItem(new DefaultTree(), DatastoreOS2.DOMAIN, applicationName).select();
		
		new WaitUntil(new ControlIsEnabled(new OkButton()), TimePeriod.LONG);
		
		new OkButton().click();
		
		new DefaultShell(OpenShiftLabel.Shell.IMPORT_APPLICATION);
		
		new FinishButton().click();
		
		new WaitWhile(new ShellIsAvailable(OpenShiftLabel.Shell.IMPORT_APPLICATION), TimePeriod.LONG);
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		
		assertTrue("There is no project in project explorer. Importing was no successfull.",
				new ProjectExplorer().containsProject(applicationName));
	}
	
	@After
	public void deleteApplication() {
		new DeleteUtils(DatastoreOS2.USERNAME, DatastoreOS2.SERVER, DatastoreOS2.DOMAIN, applicationName, 
				applicationName).perform();
	}
}
