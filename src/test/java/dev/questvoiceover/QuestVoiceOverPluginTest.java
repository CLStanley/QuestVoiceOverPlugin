package dev.questvoiceover;

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Window;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class QuestVoiceOverPluginTest
{
	private static final Path BOLT_USER_HOME = Path.of(
		System.getProperty("user.home"),
		".var",
		"app",
		"com.adamcake.Bolt",
		"data",
		"bolt-launcher");

	private static final String RUNELITE_CREDENTIALS = ".runelite/credentials.properties";
	private static final String DEV_WM_CLASS = "questvoiceover-dev";
	private static final String CLIENT_WINDOW_CLASS = "net.runelite.client.ui.ContainableFrame";
	private static final long SHUTDOWN_WATCHDOG_DELAY_MILLIS = 5000L;
	private static final long FORCE_EXIT_DELAY_MILLIS = 8000L;
	private static final AtomicBoolean SHUTDOWN_WATCHDOG_STARTED = new AtomicBoolean();
	private static final Set<Window> OBSERVED_WINDOWS = Collections.newSetFromMap(new IdentityHashMap<>());

	public static void main(String[] args) throws Exception
	{
		useDevWindowClass();
		useBoltCredentialsWhenAvailable();
		installShutdownDiagnostics();
		ExternalPluginManager.loadBuiltin(QuestVoiceOverPlugin.class);
		RuneLite.main(args);
	}

	private static void useDevWindowClass()
	{
		System.setProperty("sun.awt.X11.XWMClass", DEV_WM_CLASS);
	}

	private static void useBoltCredentialsWhenAvailable()
	{
		Path currentCredentials = Path.of(System.getProperty("user.home"), RUNELITE_CREDENTIALS);
		Path boltCredentials = BOLT_USER_HOME.resolve(RUNELITE_CREDENTIALS);

		if (!Files.exists(currentCredentials) && Files.exists(boltCredentials))
		{
			System.setProperty("user.home", BOLT_USER_HOME.toString());
		}
	}

	private static void installShutdownDiagnostics()
	{
		writeDiagnosticsMarker("startup");
		Toolkit.getDefaultToolkit().addAWTEventListener(createShutdownListener(), AWTEvent.WINDOW_EVENT_MASK);
		startWindowListenerInstaller();
	}

	private static AWTEventListener createShutdownListener()
	{
		return event ->
		{
			if (!(event instanceof WindowEvent))
			{
				return;
			}

			WindowEvent windowEvent = (WindowEvent) event;
			if (windowEvent.getID() != WindowEvent.WINDOW_CLOSING)
			{
				return;
			}

			if (!isClientWindow(windowEvent.getWindow()))
			{
				return;
			}

			startShutdownWatchdog(windowEvent);
		};
	}

	private static void startShutdownWatchdog(WindowEvent event)
	{
		if (!SHUTDOWN_WATCHDOG_STARTED.compareAndSet(false, true))
		{
			return;
		}

		writeDiagnosticsMarker("shutdown-watchdog", "Window details: " + describeWindow(event.getWindow()) + '\n');
		startForcedExitFallback();

		Thread watchdog = new Thread(() ->
		{
			try
			{
				Thread.sleep(SHUTDOWN_WATCHDOG_DELAY_MILLIS);
				writeThreadDump(event);
			}
			catch (InterruptedException ignored)
			{
				Thread.currentThread().interrupt();
			}
			catch (Exception ex)
			{
				System.err.println("Failed to write shutdown diagnostics");
				ex.printStackTrace(System.err);
			}
		}, "questvoiceover-shutdown-watchdog");
		watchdog.setDaemon(false);
		watchdog.start();
	}

	private static void startForcedExitFallback()
	{
		Thread forcedExit = new Thread(() ->
		{
			try
			{
				Thread.sleep(FORCE_EXIT_DELAY_MILLIS);
				writeDiagnosticsMarker("forced-exit", "Grace period elapsed; forcing JVM halt.\n");
				System.err.println("QuestVoiceOver dev launcher forcing JVM halt after shutdown grace period");
				Runtime.getRuntime().halt(0);
			}
			catch (InterruptedException ignored)
			{
				Thread.currentThread().interrupt();
			}
			catch (Exception ex)
			{
				System.err.println("Failed to force JVM halt");
				ex.printStackTrace(System.err);
				Runtime.getRuntime().halt(1);
			}
		}, "questvoiceover-forced-exit");
		forcedExit.setDaemon(false);
		forcedExit.start();
	}

	private static void writeThreadDump(WindowEvent event) throws Exception
	{
		long pid = ProcessHandle.current().pid();
		Path dumpPath = getDiagnosticsPath("shutdown-dump-" + pid + ".log");
		StringBuilder dump = new StringBuilder();
		dump.append("Timestamp: ").append(Instant.now()).append('\n');
		dump.append("PID: ").append(pid).append('\n');
		dump.append("Window event: ").append(event.paramString()).append('\n');
		dump.append("Window details: ").append(describeWindow(event.getWindow())).append('\n');
		dump.append("JVM name: ").append(ManagementFactory.getRuntimeMXBean().getName()).append('\n');
		dump.append('\n');

		for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet())
		{
			Thread thread = entry.getKey();
			dump.append('"').append(thread.getName()).append('"')
				.append(" daemon=").append(thread.isDaemon())
				.append(" state=").append(thread.getState())
				.append(" id=").append(thread.getId())
				.append('\n');

			for (StackTraceElement frame : entry.getValue())
			{
				dump.append("\tat ").append(frame).append('\n');
			}

			dump.append('\n');
		}

		Files.writeString(
			dumpPath,
			dump.toString(),
			StandardOpenOption.CREATE,
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE);

		System.err.println("Shutdown watchdog wrote thread dump to " + dumpPath);
		System.err.print(dump);
	}

	private static void startWindowListenerInstaller()
	{
		Thread installer = new Thread(() ->
		{
			while (!SHUTDOWN_WATCHDOG_STARTED.get())
			{
				try
				{
					SwingUtilities.invokeAndWait(QuestVoiceOverPluginTest::installListenersOnCurrentWindows);
					Thread.sleep(1000L);
				}
				catch (InterruptedException ignored)
				{
					Thread.currentThread().interrupt();
					return;
				}
				catch (Exception ex)
				{
					System.err.println("Failed to install window diagnostics");
					ex.printStackTrace(System.err);
					return;
				}
			}
		}, "questvoiceover-window-listener-installer");
		installer.setDaemon(true);
		installer.start();
	}

	private static void installListenersOnCurrentWindows()
	{
		for (Window window : Window.getWindows())
		{
			if (!isClientWindow(window))
			{
				continue;
			}

			if (!OBSERVED_WINDOWS.add(window))
			{
				continue;
			}

			window.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent event)
				{
					startShutdownWatchdog(event);
				}
			});
		}
	}

	private static void writeDiagnosticsMarker(String name)
	{
		writeDiagnosticsMarker(name, "");
	}

	private static void writeDiagnosticsMarker(String name, String extraContent)
	{
		try
		{
			Path markerPath = getDiagnosticsPath(name + "-" + ProcessHandle.current().pid() + ".log");
			String marker = "Timestamp: " + Instant.now() + '\n'
				+ "PID: " + ProcessHandle.current().pid() + '\n'
				+ "tmpdir: " + System.getProperty("java.io.tmpdir") + '\n'
				+ extraContent;
			Files.writeString(
				markerPath,
				marker,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE);
			System.err.println("Diagnostics marker written to " + markerPath);
		}
		catch (Exception ex)
		{
			System.err.println("Failed to write diagnostics marker");
			ex.printStackTrace(System.err);
		}
	}

	private static Path getDiagnosticsPath(String fileName)
	{
		return Path.of(System.getProperty("java.io.tmpdir"), "questvoiceover-" + fileName);
	}

	private static boolean isClientWindow(Window window)
	{
		return window != null
			&& CLIENT_WINDOW_CLASS.equals(window.getClass().getName())
			&& window.isDisplayable();
	}

	private static String describeWindow(Window window)
	{
		if (window == null)
		{
			return "null";
		}

		String title = window instanceof Frame ? ((Frame) window).getTitle() : "";
		return window.getClass().getName()
			+ "[title=" + title
			+ ",displayable=" + window.isDisplayable()
			+ ",showing=" + window.isShowing()
			+ "]";
	}
}
