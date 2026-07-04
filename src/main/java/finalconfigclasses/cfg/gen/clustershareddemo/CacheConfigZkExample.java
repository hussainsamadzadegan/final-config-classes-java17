package finalconfigclasses.cfg.gen.clustershareddemo;

import finalconfigclasses.cfg.zk.ZkConfigManager;
import finalconfigclasses.cfg.gen.BankConfigImpl;
import finalconfigclasses.cfg.gen.CacheConfigImpl;

import java.util.HashMap;

public class CacheConfigZkExample {

    public static void main(String[] args) throws Exception {
        String zkConnect = "localhost:2181";
        String propertiesFile = "/app/config";

        ZkConfigManager.getInstance().start(zkConnect);

        System.out.println("Starting 3-node cluster demo...");

        new Thread(() -> runNode("node1", propertiesFile), "Node-1").start();
        Thread.sleep(1000);
        new Thread(() -> runNode("node2", propertiesFile), "Node-2").start();
        Thread.sleep(1000);
        new Thread(() -> runNode("node3", propertiesFile), "Node-3").start();
    }

    private static void runNode(String nodeId, String propertiesFile) {
        try {
            System.out.println("\n=== Node " + nodeId + " starting ===");

            // Create BankConfig
            BankConfigImpl bank = createBankConfig(nodeId, propertiesFile);

            // Register listener BEFORE load/watch
            bank.addPropertyChangeListener(evt ->
                    System.out.printf("[%s] 🔥 Bank PropertyChange: %s = %s -> %s%n",
                            nodeId, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));

            // Create CacheConfig (child)
            CacheConfigImpl cache = createCacheConfig(nodeId, bank, propertiesFile);
            bank.addCacheConfigs(cache);   // assuming this method exists in BankConfigImpl

            // Register cache listener
            cache.addPropertyChangeListener(evt ->
                    System.out.printf("[%s] 🔥 Cache PropertyChange: %s = %s -> %s%n",
                            nodeId, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));

            // Watch the whole subtree (this ensures paths exist + sets watchers)
            ZkConfigManager.getInstance().watchSubtree(bank);

            // IMPORTANT: Load after watching
            bank.save();
            cache.save();
            cache.load();
            bank.load();

            System.out.println("[" + nodeId + "] Initial bank.tmpFolder = " + bank.getTmpFolder());
            System.out.println("[" + nodeId + "] Initial cacheSize = " + cache.getCacheSize());

            // Simulate local change (optional)
            // cache.setCacheSize(5000 + (int)(Math.random() * 2000));
            // cache.save();

            System.out.println("[" + nodeId + "] Ready. Change 'tmpFolder' via ZooKeeper Admin UI / zkCli to test propagation.");

            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BankConfigImpl createBankConfig(String nodeId, String propertiesFile) {
        HashMap<String, Object> defs = new HashMap<>();
        defs.put("tmpFolder", "home");
        defs.put("descriptions", null);

        return new BankConfigImpl(defs, propertiesFile, null, null,
                nodeId + "-d", null, null); // document isolation per node
    }

    private static CacheConfigImpl createCacheConfig(String nodeId, BankConfigImpl parent, String propertiesFile) {
        HashMap<String, Object> defs = new HashMap<>();
        defs.put("cacheSize", 1000);
        defs.put("cachePolicy", "LRU");

        return new CacheConfigImpl(defs, parent, propertiesFile, null, null,
                nodeId + "-d", "cached", null);
    }
}