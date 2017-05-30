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
package org.jboss.tools.openshift.ui.bot.test.application.v3.advanced;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.eclipse.condition.ProjectExists;
import org.eclipse.reddeer.eclipse.core.resources.Project;
import org.eclipse.reddeer.eclipse.ui.browser.BrowserEditor;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.swt.api.Browser;
import org.eclipse.reddeer.swt.condition.ControlIsEnabled;
import org.eclipse.reddeer.swt.condition.PageIsLoaded;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.condition.TableContainsItem;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.button.BackButton;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.button.YesButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.swt.impl.menu.ShellMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.DefaultText;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.handler.EditorHandler;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;
import org.hamcrest.core.StringContains;
import org.jboss.tools.openshift.reddeer.condition.AmountOfResourcesExists;
import org.jboss.tools.openshift.reddeer.condition.BrowserContainsText;
import org.jboss.tools.openshift.reddeer.condition.OpenShiftProjectExists;
import org.jboss.tools.openshift.reddeer.condition.OpenShiftResourceExists;
import org.jboss.tools.openshift.reddeer.enums.Resource;
import org.jboss.tools.openshift.reddeer.enums.ResourceState;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement.RequiredBasicConnection;
import org.jboss.tools.openshift.reddeer.utils.DatastoreOS3;
import org.jboss.tools.openshift.reddeer.utils.OpenShiftLabel;
import org.jboss.tools.openshift.reddeer.utils.TestUtils;
import org.jboss.tools.openshift.reddeer.utils.v3.OpenShift3NativeProjectUtils;
import org.jboss.tools.openshift.reddeer.view.OpenShiftExplorerView;
import org.jboss.tools.openshift.reddeer.view.resources.OpenShift3Connection;
import org.jboss.tools.openshift.reddeer.view.resources.OpenShiftResource;
import org.jboss.tools.openshift.reddeer.wizard.v3.NewOpenShift3ApplicationWizard;
import org.jboss.tools.openshift.ui.bot.test.application.v3.basic.TemplateParametersTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@RequiredBasicConnection
public class DeployEclipseProjectTest {

	private static String HTTPS_REPO = "https://github.com/mlabuda/jboss-eap-application.git";
	private static String GIT_NAME = "jboss-eap-application";
	private static String PROJECT_NAME = "jboss-javaee6-webapp";
	
	@InjectRequirement
	private OpenShiftConnectionRequirement connectionReq;
	
	@BeforeClass
	public static void importEAPProjectAndStashChanges() {
		EditorHandler.getInstance().closeAll(false);
		TestUtils.cleanupGitFolder(GIT_NAME);
		
		importProject();
	
		commitChanges();
	}
	
	@Before
	public void setUp() {
		OpenShift3NativeProjectUtils.getOrCreateProject(DatastoreOS3.PROJECT1,
				DatastoreOS3.PROJECT1_DISPLAYED_NAME, StringUtils.EMPTY, connectionReq.getConnection());
	}
	
	private static void importProject() {
		new ShellMenu("File", "Import...").select();
		
		new DefaultShell("Import");
		new DefaultTreeItem("Git", "Projects from Git (with smart import)").select();
	
		nextWizardPage();
				
		new DefaultTreeItem("Clone URI").select();
		
		nextWizardPage();
		
		new LabeledText("URI:").setText(HTTPS_REPO);
		
		nextWizardPage();
		nextWizardPage();
		nextWizardPage();
		
		new WaitUntil(new ControlIsEnabled(new FinishButton()));
		
		new FinishButton().click();
	
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		new WaitUntil(new ProjectExists(PROJECT_NAME), TimePeriod.LONG);
	}
	
	private static void nextWizardPage() {
		new WaitUntil(new ControlIsEnabled(new NextButton()));
		
		new NextButton().click();
		
		new WaitUntil(new ControlIsEnabled(new BackButton()));
	}
	
	private static void commitChanges() {
		ProjectExplorer explorer = new ProjectExplorer();
		explorer.open();
		
		Project project = explorer.getProject(PROJECT_NAME);
		project.refresh();
		project.select();
		new ContextMenu(OpenShiftLabel.ContextMenu.GIT_COMMIT).select();
		
		try {
			new DefaultShell("No files to commit");
			new YesButton().click();
		} catch (RedDeerException ex) {
			// do nothing
		}
		
		new WorkbenchView("Git Staging").activate();
		new DefaultStyledText().setText("Commit from IDE");
		new PushButton(OpenShiftLabel.Button.COMMIT).click();
		
		try {
			// no files were changed
			new DefaultShell("Committing is not possible");
			new OkButton().click();
		} catch (RedDeerException ex) {
			// do nothing
		}
		
		new WaitWhile(new JobIsRunning());
	}
	
	@Test
	public void testSelectionOfGitBasedProjectForDeploymentViaBrowsing() {
		NewOpenShift3ApplicationWizard wizard = new NewOpenShift3ApplicationWizard();
		wizard.openWizardFromExplorer();

		new PushButton(OpenShiftLabel.Button.BROWSE).click();
		
		new DefaultShell("Select Existing Project");
		assertTrue("There should be listed git based project of EAP application in the table.",
				new TableContainsItem(new DefaultTable(), PROJECT_NAME, 0).test());
		
		new DefaultTable().select(PROJECT_NAME);
		new OkButton().click();
		
		new WaitWhile(new ShellIsAvailable("Select Existing Project"));
		
		new DefaultShell(OpenShiftLabel.Shell.NEW_APP_WIZARD);
		new DefaultTreeItem(OpenShiftLabel.Others.EAP_TEMPLATE).select();
		
		try {
			new WaitUntil(new ControlIsEnabled(new NextButton()), TimePeriod.getCustom(5));
		} catch (WaitTimeoutExpiredException ex) {
			fail("Next button should be enabled if git based project and template are selected");
		}
	}
	
	
	@Test
	public void testSelectionOfGitBasedProjectForDeploymentByTyping() {
		NewOpenShift3ApplicationWizard wizard = new NewOpenShift3ApplicationWizard();
		wizard.openWizardFromExplorer();
		
		new DefaultTreeItem(OpenShiftLabel.Others.EAP_TEMPLATE).select();
		new DefaultText().setText(PROJECT_NAME);
		
		try {
			new WaitUntil(new ControlIsEnabled(new NextButton()), TimePeriod.getCustom(5));
		} catch (WaitTimeoutExpiredException ex) {
			fail("Next button should be enabled if git based project and template are selected");
		}
	}
	
	@Test
	public void testCorrectTemplateParamValuesWhenTemplateIsSelectedFirst() {
		NewOpenShift3ApplicationWizard wizard = new NewOpenShift3ApplicationWizard();
		wizard.openWizardFromExplorer();
		
		new DefaultTreeItem(OpenShiftLabel.Others.EAP_TEMPLATE).select();
		new DefaultText().setText(PROJECT_NAME);
		
		wizard.next();
		
		assertTrue("Wrong source repository URL is shown for eclipse project being deployed on "
				+ "OpenShift based on eap template. Could be failing because of https://issues.jboss.org/browse/JBIDE-23639.",
				new DefaultTable().getItem(TemplateParametersTest.SOURCE_REPOSITORY_URL).getText(1).equals(HTTPS_REPO));
		assertTrue("Wrong source repository REF is shown for eclipse project being deployed on "
				+ "OpenShift based on eap template. Could be failing because of https://issues.jboss.org/browse/JBIDE-23639.",
				new DefaultTable().getItem(TemplateParametersTest.SOURCE_REPOSITORY_REF).getText(1).equals("master"));
		
		wizard.cancel();
	}
	
	@Test
	public void testCorrectTemplateParamsValuesWhenEclipseProjectIsSelectedFirst() {
		NewOpenShift3ApplicationWizard wizard = new NewOpenShift3ApplicationWizard();
		wizard.openWizardFromExplorer();
		
		new DefaultText().setText(PROJECT_NAME);
		new DefaultTreeItem(OpenShiftLabel.Others.EAP_TEMPLATE).select();
		
		wizard.next();
		
		assertTrue("Wrong source repository URL is shown for eclipse project being deployed on "
				+ "OpenShift based on eap template.",
				new DefaultTable().getItem(TemplateParametersTest.SOURCE_REPOSITORY_URL).getText(1).equals(HTTPS_REPO));
		assertTrue("Wrong source repository REF is shown for eclipse project being deployed on "
				+ "OpenShift based on eap template.",
				new DefaultTable().getItem(TemplateParametersTest.SOURCE_REPOSITORY_REF).getText(1).equals("master"));
		
		wizard.cancel();
	}
	
	@Test
	public void testDeployExistingEclipseProjectToOpenShiftSelectProjectFirst() {
		NewOpenShift3ApplicationWizard wizard = new NewOpenShift3ApplicationWizard();
		wizard.openWizardFromExplorer();
		
		new DefaultTreeItem(OpenShiftLabel.Others.EAP_TEMPLATE).select();
		new DefaultText().setText(PROJECT_NAME);
		
		wizard.next();
		wizard.next();
		wizard.finish();
		
		new WaitUntil(new ShellIsAvailable(OpenShiftLabel.Shell.APPLICATION_SUMMARY), TimePeriod.LONG);
		
		new DefaultShell(OpenShiftLabel.Shell.APPLICATION_SUMMARY);
		new OkButton().click();
		
		new WaitWhile(new ShellIsAvailable(OpenShiftLabel.Shell.NEW_APP_WIZARD), TimePeriod.LONG);
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		
		new WaitUntil(new OpenShiftResourceExists(Resource.BUILD, "eap-app-1", ResourceState.COMPLETE), 
				TimePeriod.getCustom(1000));
		new WaitUntil(new AmountOfResourcesExists(Resource.POD, 2), TimePeriod.VERY_LONG);
		
		OpenShiftResource route = new OpenShiftExplorerView().getOpenShift3Connection().
				getProject().getOpenShiftResources(Resource.ROUTE).get(0);
		String url = "http://" + route.getPropertyValue("Misc", "URI") + "/" + PROJECT_NAME;
		route.select();
		new ContextMenu(OpenShiftLabel.ContextMenu.SHOW_IN_WEB_BROWSER).select();

		Browser browser= new InternalBrowser();
		
		new WaitUntil(new PageIsLoaded(browser), TimePeriod.LONG);
		
		try {
			new WaitUntil(new BrowserContainsText(url, "Welcome to JBoss!"), TimePeriod.getCustom(600));
		} catch (WaitTimeoutExpiredException ex) {
			fail("Application was not successfully deployed. Its content was not shown correctly in browser");
		}		
	}

	@After
	public void closeAllShells() {
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
	}
	
	@AfterClass
	public static void tearDown() {
		OpenShiftExplorerView explorer = new OpenShiftExplorerView();
		explorer.reopen();
		
		OpenShift3Connection connection  = explorer.getOpenShift3Connection();
		connection.getProject().delete();
		
		try {
			new WaitWhile(new OpenShiftProjectExists());
		} catch (WaitTimeoutExpiredException ex) {
			connection.refresh();
		
			new WaitWhile(new OpenShiftProjectExists(), TimePeriod.getCustom(5));
		}
		
		connection.createNewProject();
		
		ProjectExplorer projectExplorer = new ProjectExplorer();
		if (projectExplorer.containsProject(PROJECT_NAME)) {
			projectExplorer.getProject(PROJECT_NAME).delete(true);
		}	
			
		BrowserEditor browser = new BrowserEditor(new StringContains("javaee6"));
		browser.close();
	}
}
