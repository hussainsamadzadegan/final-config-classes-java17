package finalconfigclasses.cfg.web;

import finalconfigclasses.cfg.Registry;
import finalconfigclasses.cfg.gen.BankConfigImpl;
import finalconfigclasses.cfg.zk.ZkConfigManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Runs the config-classes.xml metadata API used by the React data-entry app.
 *
 * Run with:
 *   mvn -q exec:java -Dexec.mainClass=finalconfigclasses.cfg.web.ConfigWebApplication
 * or build/run the jar normally once packaged.
 *
 * Reads app.config-classes-xml-path from application.properties (defaults
 * to config-classes.xml at the project root, next to pom.xml).
 */
@SpringBootApplication
public class ConfigWebApplication {
    public static void main(String[] args) throws Exception {
        String connectString = "127.0.0.1:2181";

        ZkConfigManager.getInstance().start(connectString);


        HashMap<String, Object> defaults = new HashMap<String, Object>();
        defaults.put("tmpFolder", "c:/");
        defaults.put("descriptions", new String[]{"a", "b", "c", "d"});

        BankConfigImpl liveConfig = new BankConfigImpl(
                defaults,
                "/app/config",              // propertiesFile -> ZK root path
                "cache-config-lock",         // lockID
                new ReentrantReadWriteLock(),// propertiesLock
                "prodw",                      // document
                null,                        // name
                null                         // keyPrefix
        );
        liveConfig.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("propertyChange: " + evt.getPropertyName() + ", oldValue: " + evt.getOldValue() + ", newValue: " + evt.getNewValue());
            }
        });
//        liveConfig.load();
//
//        manager.watch(liveConfig);
        liveConfig.save();
//        CacheBusinessComponent cacheComponent = new CacheBusinessComponent();




        Registry.getInstance().putConfig("BankConfigImpl", liveConfig);
        SpringApplication.run(ConfigWebApplication.class, args);
        Thread.sleep(Long.MAX_VALUE);
    }
}
