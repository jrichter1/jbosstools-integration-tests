/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.ws.ui.bot.test.sample;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.jboss.reddeer.common.wait.AbstractWait;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.tools.ws.reddeer.ui.wizards.ws.ui.SampleWebServiceWizard;
import org.jboss.tools.ws.reddeer.ui.wizards.ws.ui.SimpleWebServiceWizard;
import org.jboss.tools.ws.ui.bot.test.WSTestBase;
import org.jboss.tools.ws.ui.bot.test.utils.Asserts;
import org.jboss.tools.ws.ui.bot.test.wsclient.WSClient;

/**
 * Test operates on Sample Web Service wizard
 * @author jjankovi
 *
 */
public class SampleSoapTestBase extends WSTestBase {

	protected void createSampleSOAPWS(String project, String name, String pkg, String cls) {
		createSampleService(project, name, pkg, cls, null);
	}

	protected void createSimpleWS(String project, String name, String pkg, String cls) {
		createSimpleService(project, name, pkg, cls, null);
	}
   
	protected void checkSOAPService(String project, String svcName, String svcPkg, String svcClass, String msgContent) {
		checkSoapService(project, svcName, svcPkg, svcClass, msgContent, null);
	}

	protected static final String SOAP_REQUEST = getSoapRequest("<ns1:sayHello xmlns:ns1=\"http://{0}/\"><arg0>{1}</arg0></ns1:sayHello>");
	protected static final String SERVER_URL = "localhost:8080";

	protected IProject getProject(String project) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(project);
	}

	protected IFile getDD(String project) {
		return getProject(project).getFile("WebContent/WEB-INF/web.xml");
	}

	protected void createSampleService(String project, String name, String pkg, String cls, String appCls) {
		SampleWebServiceWizard w = new SampleWebServiceWizard();
		w.open();
		w.setProjectName(project);
		w.setServiceName(name);
		w.setPackageName(pkg);
		w.setClassName(cls);
		w.finish();
    }

	protected void createSimpleService(String project, String name, String pkg, String cls, String appCls) {
		SimpleWebServiceWizard w = new SimpleWebServiceWizard();
		w.open();
		w.setProjectName(project);
		w.setServiceName(name);
		w.setPackageName(pkg);
		w.setClassName(cls);
		w.finish();
	}

	protected void checkSoapService(String project, String svcName, String svcPkg, String svcClass, String msgContent, String appCls) {
		TextEditor editor = new TextEditor(svcClass + ".java");
		editor.activate();
		String code = editor.getText();
		Asserts.assertContain(code, "package " + svcPkg + ";");
		String dd = resourceHelper.readFile(getDD(project));
		Asserts.assertContain(dd, "<servlet-name>" + svcName + "</servlet-name>");
		serversViewHelper.removeProjectFromServer(project, getConfiguredServerName());
		serversViewHelper.runProjectOnServer(project);
		AbstractWait.sleep(TimePeriod.getCustom(5));
		try {
			WSClient c = new WSClient(new URL("http://" + SERVER_URL + "/" + project + "/" + svcName),
							new QName("http://" + svcPkg + "/", svcClass + "Service"),
							new QName("http://" + svcPkg + "/", svcClass + "Port"));
			Asserts.assertContain(c.callService(MessageFormat.format(SOAP_REQUEST, svcPkg, msgContent)),
					"Hello " + msgContent + "!");
		} catch (MalformedURLException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
    }
}
