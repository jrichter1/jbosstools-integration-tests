/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.ws.ui.bot.test.wsclient;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.jws.WebService;

import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.PackageExplorer;
import org.jboss.reddeer.eclipse.core.resources.Project;
import org.jboss.reddeer.eclipse.core.resources.ProjectItem;
import org.jboss.reddeer.eclipse.ui.views.navigator.ResourceNavigator;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.tools.ws.reddeer.ui.wizards.wst.WebServiceWizardPageBase.SliderLevel;
import org.jboss.tools.ws.ui.bot.test.WSTestBase;
import org.jboss.tools.ws.ui.bot.test.webservice.WebServiceRuntime;
import org.junit.After;
import org.junit.Test;

/**
 * Test template operates on Web Service Client Wizard
 * 
 * @author jlukas
 * @author Radoslav Rabara
 */
@WebService(targetNamespace = "http://wsclient.test.bot.ui.ws.tools.jboss.org/", portName = "WSClientTestTemplatePort", serviceName = "WSClientTestTemplateService")
public class WSClientTestTemplate extends WSTestBase {

	protected final WebServiceRuntime serviceRuntime;

	public WSClientTestTemplate(WebServiceRuntime serviceRuntime) {
		this.serviceRuntime = serviceRuntime;
	}

	@Override
	protected String getWsProjectName() {
		return "client";
	}

	@Override
	protected String getWsPackage() {
		return "client." + getLevel().toString().toLowerCase();
	}

	@Override
	protected String getEarProjectName() {
		return "clientEAR";
	}
	
	protected String getSampleClientFileName() {
		return "clientsample/ClientSample.java";
	}
	
	/**
	 * Fails because the created client is not deployed to the server
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=428982
	 */
	@Test
	public void testDeployClient() {
		setLevel(SliderLevel.DEPLOY);
		clientTest(getWsPackage());
	}

	@Test
	public void testAssembleClient() {
		setLevel(SliderLevel.ASSEMBLE);
		clientTest(getWsPackage());
	}

	@Test
	public void testDevelopClient() {
		setLevel(SliderLevel.DEVELOP);
		clientTest(getWsPackage());
	}

	@Test
	public void testInstallClient() {
		setLevel(SliderLevel.INSTALL);
		clientTest(getWsPackage());
	}

	@Test
	public void testStartClient() {
		setLevel(SliderLevel.START);
		clientTest(getWsPackage());
	}

	@Test
	public void testTestClient() {
		setLevel(SliderLevel.TEST);
		clientTest(getWsPackage());
	}

	@Test
	public void testDefaultPkg() {
		setLevel(SliderLevel.ASSEMBLE);
		clientTest(null);
	}

	@After
	@Override
	public void cleanup() {
		deleteAllPackages();
		super.cleanup();
	}

	protected void clientTest(String targetPkg) {
		clientHelper.createClient(
				getConfiguredServerName(),
				 "http://soaptest.parasoft.com/calculator.wsdl",
				serviceRuntime,
				getWsProjectName(),
				getEarProjectName(),
				getLevel(),
				targetPkg);
		
		assertThatExpectedFilesExists(targetPkg);
		
		assertThatEARProjectIsDeployed();
	}
	
	private void assertThatExpectedFilesExists(String targetPkg) {
		ResourceNavigator navigator = new ResourceNavigator();
		navigator.open();
		Project project = navigator.getProject(getWsProjectName());
		String pkg = (targetPkg != null && !"".equals(targetPkg.trim())) ? getWsPackage() :
			"com.parasoft.wsdl.calculator";
		String src = "src/" + pkg.replace('.', '/') + "/";
		String[] expectedFiles = {
				src + "ICalculator.java",
				src + "Add.java",
				src + "AddResponse.java",
				src + "Divide.java",
				src + "DivideResponse.java",
				src + "Multiply.java",
				src + "MultiplyResponse.java",
				src + "Subtract.java",
				src + "SubtractResponse.java",
				src + getSampleClientFileName()};
		for(String file : expectedFiles) {
			assertTrue("File " + file + " was not created", project.containsItem(file.split("/")));
		}
	}
	
	private void assertThatEARProjectIsDeployed() {
		switch (getLevel()) {
		case TEST:
		case START:
		case INSTALL:
		case DEPLOY:
			if(!clientHelper.projectIsDeployed(getConfiguredServerName(), getEarProjectName())) {
				fail("Project was not found on the server.");
			}
		default:
			break;
		}
	}

	private void deleteAllPackages() {
		PackageExplorer pe = new PackageExplorer();
		pe.open();
		Project p = pe.getProject(getWsProjectName());
		ProjectItem src = p.getProjectItem("src");
		try {
			for(ProjectItem pkg: src.getChildren()) {
				pkg.select();
				pkg.delete();
			}
		} catch(SWTLayerException e) {
			pe.open();
			src = p.getProjectItem("src");
			List<ProjectItem> pkgs = src.getChildren();
			for(ProjectItem pkg: pkgs) {
				pkg.select();
				pkg.delete();
			}
		}
		
	}
}

