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

package org.jboss.tools.cdi.bot.test.quickfix.test;


import org.jboss.reddeer.requirements.server.ServerReqState;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerReqType;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
import org.jboss.reddeer.eclipse.ui.problems.ProblemsView;
import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.reddeer.common.wait.AbstractWait;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.workbench.impl.editor.DefaultEditor;
import org.jboss.tools.cdi.bot.test.beansxml.BeansXMLValidationTest;
import org.jboss.tools.cdi.bot.test.condition.BeanValidationErrorIsEmpty;
import org.jboss.tools.cdi.bot.test.quickfix.base.BeansXMLQuickFixTestBase;
import org.jboss.tools.cdi.reddeer.annotation.CDIWizardType;
import org.jboss.tools.cdi.reddeer.annotation.ValidationType;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test operates on quick fixes used for validation errors of beans.xml
 * 
 * @author Jaroslav Jankovic
 */
@JBossServer(state=ServerReqState.PRESENT, type=ServerReqType.AS7_1)
@OpenPerspective(JavaEEPerspective.class)
@CleanWorkspace
public class BeansXMLValidationQuickFixTest extends BeansXMLQuickFixTestBase {

	@After
	public void waitForJobs() {
		editResourceUtil.deleteInProjectExplorer(getProjectName(), 
				"WebContent", "WEB-INF", "beans.xml");
	}
	
	@BeforeClass
	public static void cleanProblemsView(){
		ProblemsView pw = new ProblemsView();
		pw.open();
		AbstractWait.sleep(TimePeriod.NORMAL);
		for(TreeItem ti: new DefaultTree().getItems()){
			ti.select();
			new ContextMenu("Delete").select();
			new DefaultShell("Delete Selected Entries");
			new PushButton("Yes").click();
			new WaitWhile(new ShellWithTextIsAvailable("Delete Selected Entries"));
			new WaitWhile(new JobIsRunning());
		}
	}
	
	@Test
	public void testNoBeanComponent() {

		String bean = "A1";
		beansHelper.createBeansXMLWithAlternative(getProjectName(), getPackageName(), bean,
				this.getClass().getResourceAsStream(BeansXMLValidationTest.BEANS_XML_WITH_ALTERNATIVE));
		
		waitForCDIValidator();
		
		new WaitWhile(new BeanValidationErrorIsEmpty(getProjectName()));
		resolveAddNewAlternative(bean, getPackageName());
		AbstractWait.sleep(TimePeriod.NORMAL);
		new WaitUntil(new BeanValidationErrorIsEmpty(getProjectName()),TimePeriod.LONG);	
	}
	
	@Test
	public void testNoStereotypeAnnotation() {

		String stereotype = "S1";
		
		beansHelper.createBeansXMLWithStereotype(getProjectName(), getPackageName(), stereotype,
				this.getClass().getResourceAsStream(BeansXMLValidationTest.BEANS_XML_WITH_STEREOTYPE));
		
		new WaitWhile(new BeanValidationErrorIsEmpty(getProjectName()));
		resolveAddNewStereotype(stereotype, getPackageName());
		AbstractWait.sleep(TimePeriod.NORMAL);
		new WaitUntil(new BeanValidationErrorIsEmpty(getProjectName()),TimePeriod.LONG);
		
	}
	
	@Test
	public void testNoAlternativeBeanComponent() {

		String bean = "B1";
		
		wizard.createCDIComponent(CDIWizardType.BEAN, bean, getPackageName(), null);
		
		beansHelper.createBeansXMLWithAlternative(getProjectName(), getPackageName(), bean,
				this.getClass().getResourceAsStream(BeansXMLValidationTest.BEANS_XML_WITH_ALTERNATIVE));
		
		new WaitWhile(new BeanValidationErrorIsEmpty(getProjectName()));
		
		resolveAddAlternativeToBean(bean);
		AbstractWait.sleep(TimePeriod.NORMAL);
		new WaitUntil(new BeanValidationErrorIsEmpty(getProjectName()),TimePeriod.LONG);
		
	}
	
	@Test
	public void testNoAlternativeStereotypeAnnotation() {

		String stereotype = "S2";
		
		wizard.createCDIComponent(CDIWizardType.STEREOTYPE, stereotype, getPackageName(), null);
		
		beansHelper.createBeansXMLWithStereotype(getProjectName(), getPackageName(), stereotype,
				this.getClass().getResourceAsStream(BeansXMLValidationTest.BEANS_XML_WITH_STEREOTYPE));
		
		new WaitWhile(new BeanValidationErrorIsEmpty(getProjectName()));
		
		resolveAddAlternativeToStereotype(stereotype);
		AbstractWait.sleep(TimePeriod.NORMAL);
		new WaitUntil(new BeanValidationErrorIsEmpty(getProjectName()),TimePeriod.LONG);
		
	}
	
	@Test
	public void testNoInterceptor() {

		String interceptor = "I1";
		beansHelper.createBeansXMLWithInterceptor(getProjectName(), getPackageName(), interceptor,
				this.getClass().getResourceAsStream(BeansXMLValidationTest.BEANS_XML_WITH_INTERCEPTOR));
		new WaitWhile(new BeanValidationErrorIsEmpty(getProjectName()));
		
		resolveAddNewInterceptor(interceptor, getPackageName());
		AbstractWait.sleep(TimePeriod.NORMAL);
		new WaitUntil(new BeanValidationErrorIsEmpty(getProjectName()),TimePeriod.LONG);
		
	}
	
	@Test
	public void testNoDecorator() {

		String decorator = "D1";
		
		beansHelper.createBeansXMLWithDecorator(getProjectName(), getPackageName(), decorator,
				this.getClass().getResourceAsStream(BeansXMLValidationTest.BEANS_XML_WITH_DECORATOR));
		
		new WaitWhile(new BeanValidationErrorIsEmpty(getProjectName()));
		resolveAddNewDecorator(decorator, getPackageName());
		AbstractWait.sleep(TimePeriod.NORMAL);
		new WaitUntil(new BeanValidationErrorIsEmpty(getProjectName()),TimePeriod.LONG);	
		
	}
	
	// https://issues.jboss.org/browse/JBIDE-14384
	@Test
	public void testNoBeansXmlPresent() {
		
		quickFixHelper.checkQuickFix(ValidationType.NO_BEANS_XML, 
				"Create File beans.xml", getProjectName(), getValidationProvider());
		new DefaultEditor("beans.xml").save();
		
	}
	
	private void waitForCDIValidator() {
		AbstractWait.sleep(TimePeriod.NORMAL);
	}
	
}
