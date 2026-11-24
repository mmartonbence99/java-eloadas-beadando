package org.example.ea_beadando;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

@Component
public class BrowserLauncher {

    private static final Logger logger = LoggerFactory.getLogger(BrowserLauncher.class);

    private final Environment environment;
    private final ApplicationContext applicationContext;

    public BrowserLauncher(Environment environment, ApplicationContext applicationContext) {
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        if (!environment.getProperty("app.browser.auto-open", Boolean.class, true)
                || !isRunningFromJar()) {
            return;
        }

        if (!(applicationContext instanceof WebServerApplicationContext webContext)) {
            logger.warn("Nem sikerült meghatározni a webkiszolgáló portját; a böngésző nem nyitható meg.");
            return;
        }

        String contextPath = environment.getProperty("server.servlet.context-path", "");
        URI applicationUri = URI.create("http://127.0.0.1:"
                + webContext.getWebServer().getPort() + contextPath);
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(applicationUri);
                return;
            }
        } catch (IOException | UnsupportedOperationException e) {
            logger.debug("A Java Desktop API nem tudta megnyitni a böngészőt.", e);
        }

        try {
            new ProcessBuilder(nativeBrowserCommand(applicationUri)).start();
            logger.info("A böngésző megnyitása: {}", applicationUri);
        } catch (IOException | UnsupportedOperationException e) {
            logger.warn("A böngésző automatikus megnyitása nem sikerült. Nyisd meg ezt a címet: {}",
                    applicationUri, e);
        }
    }

    private List<String> nativeBrowserCommand(URI uri) {
        String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (operatingSystem.contains("win")) {
            return List.of("rundll32.exe", "url.dll,FileProtocolHandler", uri.toString());
        }
        if (operatingSystem.contains("mac")) {
            return List.of("open", uri.toString());
        }
        return List.of("xdg-open", uri.toString());
    }

    private boolean isRunningFromJar() {
        File source = new ApplicationHome(BrowserLauncher.class).getSource();
        return source != null && source.isFile() && source.getName().endsWith(".jar");
    }
}
