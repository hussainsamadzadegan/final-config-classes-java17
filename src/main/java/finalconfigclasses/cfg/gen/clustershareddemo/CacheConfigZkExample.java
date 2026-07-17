// CacheConfigZkExample.java
package finalconfigclasses.cfg.gen.clustershareddemo;

import finalconfigclasses.cfg.ConfigBean;
import finalconfigclasses.bean.BeanUpdateEvent;
import finalconfigclasses.bean.BeanUpdateFailedException;
import finalconfigclasses.bean.BeanUpdateListener;
import finalconfigclasses.bean.BeanUpdateRejectedException;
import finalconfigclasses.cfg.gen.BankConfigImpl;   // or whatever your root is

public class CacheConfigZkExample {

    private BankConfigImpl bankConfig;

    public void init(BankConfigImpl liveConfig) {
        this.bankConfig = liveConfig;

        System.out.println("[" + getNodeId() + "] Registering BeanUpdateListener on BankConfigImpl");

        // Register listener on root
        liveConfig.addBeanUpdateListener(new BeanUpdateListener() {
            @Override
            public void prepareUpdate(BeanUpdateEvent event) throws BeanUpdateRejectedException {
                System.out.println("[" + getNodeId() + "] prepareUpdate → " + event.getSourceBean());
                // Add your logic: validation, cache refresh prep, etc.
            }

            @Override
            public void activateUpdate(BeanUpdateEvent event) throws BeanUpdateFailedException {
                System.out.println("[" + getNodeId() + "] activateUpdate → Changes applied");
                onConfigChanged(event);
            }

            @Override
            public void rollbackUpdate(BeanUpdateEvent event) {
                System.out.println("[" + getNodeId() + "] rollbackUpdate");
            }
        });

        // Optionally register on important children too
        // liveConfig.getJasperReportTemplateCacheConfig()...
    }

    private void onConfigChanged(BeanUpdateEvent event) {
        // This runs on EVERY node when config changes (via ZooKeeper)
        // Refresh caches, reload connections, notify internal components, etc.
        System.out.println("[" + getNodeId() + "] Config changed - refreshing local state...");
    }

    private String getNodeId() {
        // Optional: help distinguish logs from different nodes
        return System.getProperty("node.id", "node-" + System.currentTimeMillis() % 10000);
    }

    public void shutdown() {
        if (bankConfig != null) {
            // remove listeners if needed
        }
    }
}