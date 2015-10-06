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

package org.jboss.tools.ws.ui.bot.test.webservice;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.text.MessageFormat;

import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.exception.CoreLayerException;
import org.jboss.reddeer.jface.wizard.WizardDialog;
import org.jboss.reddeer.swt.api.Button;
import org.jboss.reddeer.swt.api.Shell;
import org.jboss.reddeer.swt.api.StyledText;
import org.jboss.reddeer.swt.condition.WidgetIsEnabled;
import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.impl.button.NextButton;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.reddeer.workbench.impl.editor.DefaultEditor;
import org.jboss.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.tools.common.reddeer.label.IDELabel;
import org.jboss.tools.ws.reddeer.ui.wizards.wst.WebServiceFirstWizardPage;
import org.jboss.tools.ws.reddeer.ui.wizards.wst.WebServiceFirstWizardPage.ServiceType;
import org.jboss.tools.ws.reddeer.ui.wizards.wst.WebServiceSecondWizardPage;
import org.jboss.tools.ws.reddeer.ui.wizards.wst.WebServiceWizard;
import org.jboss.tools.ws.reddeer.ui.wizards.wst.WebServiceWizardPageBase.SliderLevel;
import org.jboss.tools.ws.ui.bot.test.soap.SOAPTestBase;
import org.jboss.tools.ws.ui.bot.test.utils.ProjectHelper;
import org.jboss.tools.ws.ui.bot.test.utils.ResourceHelper;
import org.junit.Assert;

/**
 * Base class for Web Service wizard tests
 * 
 * @author jjankovi
 *
 */
public abstract class WebServiceTestBase extends SOAPTestBase {

	protected abstract String getWsPackage();

	protected abstract String getWsName();

	protected void bottomUpWS(InputStream input, WebServiceRuntime serviceRuntime) {
		String source = ResourceHelper.readStream(input);
		String src = MessageFormat.format(source, getWsPackage(), getWsName());
		createService(ServiceType.BOTTOM_UP, getWsPackage() + "." + getWsName(), getLevel(), null, src, serviceRuntime);
	}

	protected void topDownWS(InputStream input, WebServiceRuntime serviceRuntime, String pkg) {
		String s = ResourceHelper.readStream(input);
		String[] tns = getWsPackage().split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int i = tns.length - 1; i > 0; i--) {
			sb.append(tns[i]);
			sb.append(".");
		}
		sb.append(tns[0]);
		String src = MessageFormat.format(s, sb.toString(), getWsName());
		createService(ServiceType.TOP_DOWN, "/" + getWsProjectName() + "/src/" + getWsName() + ".wsdl", getLevel(), pkg,
				src, serviceRuntime);
	}

	private void createService(ServiceType type, String source, SliderLevel level, String pkg, String code,
			WebServiceRuntime serviceRuntime) {
		// create ws source - java class or wsdl
		switch (type) {
		case BOTTOM_UP:
			TextEditor editor = ProjectHelper.createClass(getWsProjectName(), getWsPackage(), getWsName());
			assertNotNull(editor);

			// replace default content of java class w/ code
			editor.setText(code);
			editor.save();
			editor.close();
			break;
		case TOP_DOWN:
			DefaultEditor ed = ProjectHelper.createWsdl(getWsProjectName(), getWsName());
			assertNotNull(ed);
			StyledText text = new DefaultStyledText();
			assertNotNull(text);

			text.setText(code);
			ed.save();
			ed.close();
			break;
		}

		// clean projects and wait for eclipse to build the workspace
		ProjectHelper.cleanAllProjects();
		
		// create a web service
		WebServiceWizard wizard = new WebServiceWizard();
		wizard.open();

		WebServiceFirstWizardPage page = new WebServiceFirstWizardPage();
		new WaitWhile(new JobIsRunning(), TimePeriod.NORMAL);
		page.setServiceType(type);
		page.setSource(source);
		page.setServerRuntime(getConfiguredServerName());
		new WaitWhile(new JobIsRunning(), TimePeriod.getCustom(5), false);
		page.setWebServiceRuntime(serviceRuntime.getName());
		try {
			page.setServiceProject(getWsProjectName());
			page.setServiceEARProject(getEarProjectName());
		} catch (CoreLayerException ex) {
			LOGGER.warning("Cannot find project settings, trying with default");
		}

		new WaitWhile(new JobIsRunning(), TimePeriod.getCustom(5), false);
		page.setServiceSlider(level);
		if (page.isClientEnabled()) {
			page.setClientSlider(SliderLevel.NO_CLIENT);
		}

		new WaitUntil(new WidgetIsEnabled(new NextButton()), TimePeriod.getCustom(5), false);
		wizard.next();

		checkErrorDialog(wizard);

		if (pkg != null && pkg.trim().length() > 0) {
			WebServiceSecondWizardPage page2 = new WebServiceSecondWizardPage();
			page2.setPackageName(pkg);
			Button finishButton = new PushButton(IDELabel.Button.FINISH);
			wizard.next();
			new WaitUntil(new WidgetIsEnabled(finishButton));
		}

		new WaitWhile(new JobIsRunning(), TimePeriod.getCustom(5), false);
		wizard.finish();

		// let's fail if there's some error in the wizard,
		// and close error dialog and the wizard so other tests
		// can continue
		if (new DefaultShell() != null) {
			if (new DefaultShell().getText().contains("Error")) {
				String msg = new DefaultText().getText();
				new PushButton(0).click();
				wizard.cancel();
				Assert.fail(msg);
			}
		}
	}

	private void checkErrorDialog(WizardDialog openedWizard) {
		Shell shell = new DefaultShell();
		String text = shell.getText();
		if (text.contains("Error")) {
			String msg = "<no text>";
			try {
				msg = new DefaultText().getText();
			} catch (SWTLayerException e) {

			}
			new PushButton(0).click();
			openedWizard.cancel();
			Assert.fail(text + msg);
		}
	}
}
