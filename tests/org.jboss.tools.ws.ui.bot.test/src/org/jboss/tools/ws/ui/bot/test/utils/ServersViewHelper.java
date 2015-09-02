package org.jboss.tools.ws.ui.bot.test.utils;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.reddeer.common.condition.WaitCondition;
import org.jboss.reddeer.common.wait.AbstractWait;
import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.condition.JobIsRunning;
import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
import org.jboss.reddeer.eclipse.wst.server.ui.view.Server;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServerModule;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerPublishState;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerState;
import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewException;
import org.jboss.reddeer.eclipse.wst.server.ui.wizard.ModifyModulesDialog;
import org.jboss.reddeer.eclipse.wst.server.ui.wizard.ModifyModulesPage;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.menu.ShellMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.tools.common.reddeer.label.IDELabel;

/**
 * 
 * @author Radoslav Rabara
 *
 */
public class ServersViewHelper {

	private static final Logger LOGGER = Logger
			.getLogger(ServersViewHelper.class.getName());
	
	private ServersViewHelper() {};

	/**
	 * Removes the specified <var>project</var> from the configured server.
	 * 
	 * @param project project to be removed from the server
	 */
	public static void removeProjectFromServer(String project, String serverName) {
		ServersView serversView = new ServersView();
		serversView.open();
		Server server = serversView.getServer(serverName);

		ServerModule serverModule = null;
		try {
			serverModule = server.getModule(project);
		} catch (EclipseLayerException e) {
			LOGGER.info("Project " + project + " was not found on the server");
			return;
		}
		if (serverModule != null) {
			serverModule.remove();
		}
	}

	/**
	 * Removes all projects from the specified server.
	 */
	public static void removeAllProjectsFromServer(String serverName) {
		ServersView serversView = new ServersView();
		serversView.open();
		
		Server server = null;
		try {
			serversView.activate();
			server = serversView.getServer(serverName);
		} catch (EclipseLayerException e) {
			LOGGER.warning("Server " + serverName + "not found, retrying");
			serversView.activate();
			server = serversView.getServer(serverName);
			
		}
		List<ServerModule> modules = server.getModules();
		
		if (modules == null || modules.isEmpty())
			return;
		
		for (ServerModule module : modules) {
			if (module != null) {
				AbstractWait.sleep(TimePeriod.SHORT);
				module.remove();
			}
		}
	}

	/**
	 * Method runs project on the configured server
	 */
	public static void runProjectOnServer(String projectName) {
		new ProjectExplorer().getProject(projectName).select();
		new ShellMenu(org.hamcrest.core.Is.is(IDELabel.Menu.RUN), org.hamcrest.core.Is.is(IDELabel.Menu.RUN_AS),
				org.hamcrest.core.StringContains.containsString("Run on Server")).select();
		new DefaultShell("Run On Server");
		new PushButton(IDELabel.Button.FINISH).click();
		new WaitUntil(new JobIsRunning(), TimePeriod.getCustom(5), false);
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}

	/**
	 * Adds the specified project to the specified server
	 */
	public static void addProjectToServer(String projectName, String serverName) {
		ServersView serversView = new ServersView();
		serversView.open();
		Server server = serversView.getServer(serverName);
		ModifyModulesDialog dialog = server.addAndRemoveModules();
		ModifyModulesPage page = new ModifyModulesPage();
		page.add(projectName);
		dialog.finish();
	}

	public static void serverClean(String serverName) {
		ServersView serversView = new ServersView();
		serversView.open();
		Server server = null;
		try {
			server = serversView.getServer(serverName);
		} catch (EclipseLayerException e) {
			LOGGER.warning("Server " + serverName + "not found, retrying");
			server = serversView.getServer(serverName);
			
		}
		AbstractWait.sleep(TimePeriod.SHORT);
		server.clean();
	}
	
	public static void startServer(String serverName) {
		ServersView view = new ServersView();
		view.open();
		Server server = view.getServer(serverName);
		try {
			server.start();
			new WaitUntil(new ServerStateCondition(server, ServerState.STARTED), TimePeriod.LONG);
		} catch (ServersViewException ex) {
			LOGGER.info("Server " + serverName + " is already running");
		}
	}
	
	public static void waitForPublish(String serverName) {
		ServersView view = new ServersView();
		view.open();
		Server server = view.getServer(serverName);
		new WaitUntil(new ServerStateCondition(server, ServerState.STARTED), TimePeriod.LONG);
		new WaitUntil(new ServerPublishStateCondition(server, ServerPublishState.SYNCHRONIZED), TimePeriod.LONG);
	}

	private static class ServerStateCondition implements WaitCondition {

		private ServerState expectedState;
		private Server server;

		private ServerStateCondition(Server server, ServerState expectedState) {
			this.expectedState = expectedState;
			this.server = server;
		}

		@Override
		public boolean test() {
			return expectedState.equals(server.getLabel().getState());
		}

		@Override
		public String description() {
			return "server's state is: " + expectedState.getText();
		}
	}
	
	private static class ServerPublishStateCondition implements WaitCondition {

		private ServerPublishState expectedState;
		private Server server;

		private ServerPublishStateCondition(Server server, ServerPublishState expectedState) {
			this.expectedState = expectedState;
			this.server = server;
		}

		@Override
		public boolean test() {
			return expectedState.equals(server.getLabel().getPublishState());
		}

		@Override
		public String description() {
			return "server's publish state is " + expectedState.getText();
		}
	}
}
