package org.jboss.tools.openshift.ui.bot.test.application.basic;

import static org.junit.Assert.fail;

import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.core.condition.ShellWithTextIsAvailable;
import org.jboss.reddeer.jface.exception.JFaceLayerException;
import org.jboss.reddeer.jface.viewer.handler.TreeViewerHandler;
import org.jboss.reddeer.swt.impl.button.CancelButton;
import org.jboss.reddeer.swt.impl.text.DefaultText;
import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
import org.jboss.tools.openshift.ui.utils.Datastore;
import org.jboss.tools.openshift.ui.utils.OpenShiftLabel;
import org.jboss.tools.openshift.ui.wizard.application.OpenNewApplicationWizard;
import org.junit.Test;

/**
 * Test capabilities of content assist for cartridges in New application wizard.
 * 
 * @author mlabuda@redhat.com
 *
 */
public class ID305CartridgeContentAssistTest {

	@Test
	public void testCartridgeContentAssist() {
		TreeViewerHandler treeViewerHandler = TreeViewerHandler.getInstance();
		OpenNewApplicationWizard.openWizardFromExplorer(Datastore.USERNAME, Datastore.DOMAIN);
		
		new DefaultText(1).setText("diy");
		
		try {
			treeViewerHandler.getTreeItem(new DefaultTreeItem("Basic Cartridges"), 
				OpenShiftLabel.Cartridge.DIY);
			// pass
		} catch (JFaceLayerException ex) {
			fail("Content assist don't work. Cartridge was not found by it's shortened name");
		}
		
		new DefaultText(1).setText("Enterprise");
		
		try {
			treeViewerHandler.getTreeItem(new DefaultTreeItem("Basic Cartridges"), 
				OpenShiftLabel.Cartridge.JBOSS_EAP);
			// pass
		} catch (JFaceLayerException ex) {
			fail("Content assist don't work. Cartridge was not found by it's name");
		}
		
		new DefaultText(1).setText("unsupported");
		
		try {
			treeViewerHandler.getTreeItem(new DefaultTreeItem("Basic Cartridges"), 
				OpenShiftLabel.Cartridge.DIY);
			// pass
		} catch (JFaceLayerException ex) {
			fail("Content assist don't work. Cartridge was not found by it's details");
		}
		
		new CancelButton().click();
		
		new WaitWhile(new ShellWithTextIsAvailable(OpenShiftLabel.Shell.NEW_APP_WIZARD),
				TimePeriod.LONG);
		
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}
}
