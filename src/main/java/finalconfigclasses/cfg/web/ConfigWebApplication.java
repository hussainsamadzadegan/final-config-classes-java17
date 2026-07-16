package finalconfigclasses.cfg.web;

import finalconfigclasses.bean.BeanUpdateEvent;
import finalconfigclasses.bean.BeanUpdateFailedException;
import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.BeanUpdateRejectedException;
import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.cfg.Registry;
import finalconfigclasses.cfg.gen.BankConfigImpl;
import finalconfigclasses.cfg.gen.CacheConfigImpl;
import finalconfigclasses.cfg.misc.SaveAllVisitor;
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

        HashMap<String, Object> defValue = new HashMap<String, Object>();
        // the default value for attributes
        defValue.put("cacheSize", 100);
        defValue.put("cachePolicy", "LFU");

        HashMap<String, Object> dymaval = new HashMap<String, Object>();
        // the default value for attributes
        dymaval.put("cacheSize", Boolean.TRUE);
        dymaval.put("cachePolicy", Boolean.TRUE);

        // here I've not provide any storage(properties file or preferences node) for
        // config bean, so it would use the default values.
        CacheConfigImpl ccimpl = new CacheConfigImpl(
                defValue,
                dymaval,
                (ConfigBean) null,
                "/app/config",              // propertiesFile -> ZK root path
                "cache-config-lock",         // lockID
                new ReentrantReadWriteLock(),// propertiesLock
                "bank-ear",                      // document
                null,                        // name
                null                         // keyPrefix
        );

        CacheConfigImpl ccimpl2 = new CacheConfigImpl(
                defValue,
                dymaval,
                (ConfigBean) null,
                "/app/config",              // propertiesFile -> ZK root path
                "cache-config-lock",         // lockID
                new ReentrantReadWriteLock(),// propertiesLock
                "bank-ear",                      // document
                "null1",                        // name
                "cache-configs"                         // keyPrefix
        );


        HashMap<String, Object> defaults = new HashMap<String, Object>();
        defaults.put("tmpFolder", "c:/");
        defaults.put("descriptions", new String[]{"a", "b", "c", "d"});
        defaults.put("jasperReportTemplateCacheConfig", ccimpl);
        defaults.put("cacheConfigs", new CacheConfigImpl[] {ccimpl2});

        HashMap<String, Object> dynaProp = new HashMap<String, Object>();
        dynaProp.put("tmpFolder",Boolean.TRUE);
        dynaProp.put("descriptions", Boolean.TRUE);
        dynaProp.put("jasperReportTemplateCacheConfig", Boolean.TRUE);
//        defaults.put("cacheConfigs", new CacheConfigImpl[] {new CacheConfigImpl(), new CacheConfigImpl()});
//        defaults.put("jasperReportTemplateCacheConfig", null);
        dynaProp.put("cacheConfigs", Boolean.TRUE);
        BankConfigImpl liveConfig = new BankConfigImpl(
                defaults,
                dynaProp,
                (ConfigBean) null,
                "/app/config",              // propertiesFile -> ZK root path
                "bank-config-lock",         // lockID
                new ReentrantReadWriteLock(),// propertiesLock
                null,                      // document
                null,                        // name
                null                         // keyPrefix
        );
        liveConfig.setJasperReportTemplateCacheConfig(ccimpl);
        liveConfig.addCacheConfigs(ccimpl2);
        liveConfig.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("propertyChange: " + evt.getPropertyName() + ", oldValue: " + evt.getOldValue() + ", newValue: " + evt.getNewValue());
            }
        });
        liveConfig.addBeanUpdateListener(new BeanUpdateListener() {
            @Override
            public void rollbackUpdate(BeanUpdateEvent event) {

            }

            @Override
            public void activateUpdate(BeanUpdateEvent event) throws BeanUpdateFailedException {

            }

            @Override
            public void prepareUpdate(BeanUpdateEvent event) throws BeanUpdateRejectedException {
                System.out.println(event);
            }
        });
        liveConfig.getCacheConfigs(0).addBeanUpdateListener(new BeanUpdateListener() {
            @Override
            public void rollbackUpdate(BeanUpdateEvent event) {

            }

            @Override
            public void activateUpdate(BeanUpdateEvent event) throws BeanUpdateFailedException {

            }

            @Override
            public void prepareUpdate(BeanUpdateEvent event) throws BeanUpdateRejectedException {
                System.out.println(event);
            }
        });


//        liveConfig.load();
//
//        manager.watch(liveConfig);
        //liveConfig.accept(new SaveAllVisitor());
//        liveConfig.save();
//        CacheBusinessComponent cacheComponent = new CacheBusinessComponent();




        Registry.getInstance().putConfig("BankConfigImpl", liveConfig);
        SpringApplication.run(ConfigWebApplication.class, args);
        Thread.sleep(Long.MAX_VALUE);
    }
}
