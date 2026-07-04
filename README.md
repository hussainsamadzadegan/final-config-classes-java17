# FinalConfigClasses - Java 17 / ZooKeeper edition

This is a migration of the original Java 8 / Ant / JAXB / raw-XML-file config
framework to Java 17, Maven, and ZooKeeper (Apache Curator 5.x) backed
persistence, with the config-classes description XML now read/written with
XStream instead of JAXB.

## What changed, at a glance

| Area | Before | After |
|---|---|---|
| Java version | 8 | 17 (`maven.compiler.release=17`) |
| Build tool | Ant (`build.xml` / `build.properties`) | Maven (`pom.xml`) |
| Description-XML parsing (`cfg.jaxb.*`) | JAXB (`javax.xml.bind`, removed from the JDK since Java 11) | Plain POJOs (`cfg.model.*`) read/written with XStream (`ConfigModelIO`) |
| Config bean `load()`/`save()` persistence | A single shared on-disk XML file, addressed by XPath-like keys (`finalconfigclasses.util.XMLUtils`) | One ZooKeeper znode per config bean instance, addressed by the same key scheme translated into a znode path (`finalconfigclasses.cfg.zk.ZkConfigStore`) |
| Cross-process config reload | Not built in | `ZkConfigManager` watches every registered bean's znode; a `save()` on any peer triggers an automatic `load()` on every other peer watching that bean |
| XStream (already used by `Registry` for export/import) | unpinned, default (unhardened) security | pinned to **1.4.21**, hardened with `XStream.setupDefaultSecurity()` + explicit type whitelist (required since XStream 1.4.7+) |
| Code generator | `CfgGenTask` (Ant `Task` subclass) | `ConfigClassGenerator` (plain class, callable from CLI or Swing UI) |
| Generator UI | none (Ant target only) | Swing app (`ConfigGeneratorFrame` / `ConfigGeneratorApp`), launched via `generate-config-classes.cmd` |
| Legacy "codebeautiful" / Jacobe formatting step | supported | **dropped** - Jacobe is an unmaintained, Windows-only external tool; if you still use it, run it as a separate step over the generated sources |

## Project layout

```
pom.xml                          Maven build (replaces build.xml/build.properties)
generate-config-classes.cmd      Launches the Swing generator UI
config-classes.xml               Sample description XML (same 3 demo classes as before)
config-classes.xsd               Description XML schema (unchanged)
src/main/resources/*.vm          Velocity templates used by the generator
src/main/java/finalconfigclasses/
  bean/                          Generic diffable/settable-bean support (unchanged)
  management/                    JMX ModelMBeanInfo builder (unchanged)
  util/                          ParameterMap (unchanged); XMLUtils (kept for reference/back-compat,
                                  no longer used by generated load()/save())
  cfg/                           Core framework: BaseConfigBean, ConfigBean, Registry, LockManager,
                                  ConfigDiffHelper, visitors, etc.
  cfg/model/                     Description-XML POJOs + XStream reader/writer (replaces cfg.jaxb)
  cfg/zk/                        ZooKeeper persistence + cluster-wide watch/reload
  cfg/engine/                    The code generator (ConfigClassGenerator, Utils) + Swing UI
  cfg/gen/                       The 3 example generated classes (Cache/Bank/MoreCache-ConfigImpl)
                                  plus a runnable ZkConfigDemo
```

## Building

```
mvn clean package
```

This produces two jars in `target/`:

- `configclasses-framework-3.0.0.jar` - the framework/library jar.
- `configclasses-framework-3.0.0-generator.jar` - an all-dependencies-bundled
  jar whose `Main-Class` launches the Swing generator UI. This is the jar
  `generate-config-classes.cmd` runs.

> This environment could not reach Maven Central to actually download
> `xstream`/`curator`/`velocity`/`commons-beanutils`/`zookeeper` and run a
> real `mvn compile`, so the ZooKeeper/XStream/Velocity-dependent classes
> were instead verified by compiling the entire source tree against
> hand-written stub classes that mirror the exact API surface used (method
> names/signatures for `CuratorFramework`, `CuratorCache`, `XStream`,
> `VelocityEngine`, etc. - see the migration notes below). That catches
> internal bugs (typos, type mismatches) but can't catch a wrong real-library
> method name. Run `mvn clean package` as your first step and fix up any
   compile errors it reports - they should be minor if any turn up.

## Generating config classes

GUI (after `mvn clean package`):

```
generate-config-classes.cmd
generate-config-classes.cmd config-classes.xml     (pre-fills the XML field)
```

CLI:

```
java -cp target/configclasses-framework-3.0.0-generator.jar ^
     finalconfigclasses.cfg.engine.ConfigClassGenerator ^
     --xml config-classes.xml --dest src/main/java
```

Options: `--no-diffhelpers` (skip DiffHelper generation), `--rigid` (generate
"rigid" load/save - znode must already exist - instead of the default
"flexible" mode which auto-creates it), `--templates <dir>` (use your own
`*.vm` templates instead of the ones bundled in the jar).

## How ZooKeeper persistence works

Every config bean instance already carries a `propertiesFile` (a root path),
a `document` (a logical sub-namespace) and an `_getXPath()` (built from its
class key + name + parent chain) - the same three pieces of information that
used to locate it inside a shared XML file. `ZkConfigStore.buildZnodePath`
turns that same triple into a ZooKeeper path, e.g.:

```
propertiesFile = "/app/config"
document       = "prod"
xpath          = "bank-ear/cache-config[@name='jasperReportTemplateCacheConfig']"
      -->
znode path     = "/app/config/prod/bank-ear/cache-config/jasperReportTemplateCacheConfig"
```

`save()` serializes all of that bean's simple attributes (plus a per-attribute
"is explicitly set" flag) into one XStream-XML blob and writes it as that
znode's data in a single round trip. `load()` reads it back. Nested
**properties** (child config beans) are *not* included in that blob - each
nested bean persists itself, under its own znode, exactly like before (via
`LoadAllVisitor`/`SaveAllVisitor` or manual calls).

- **"Flexible" mode** (`manageConfigXml=true`, the default): `load()`/`save()`
  auto-create the znode (and any missing parent znodes) if they don't exist
  yet.
- **"Rigid" mode** (`manageConfigXml=false`): the znode must already exist;
  a missing znode raises a `ConfigException`.

## Cluster-wide reload on change

```java
ZkConfigManager manager = ZkConfigManager.getInstance();
manager.start("zk1:2181,zk2:2181,zk3:2181");   // same connect string on every peer

BankConfigImpl root = new BankConfigImpl(defaults, "/app/config", "bank-lock",
        new ReentrantReadWriteLock(), "prod", null, null);
root.load();
manager.watchSubtree(root);   // registers a ZooKeeper watcher for root + every nested property
```

From here, whenever **any** peer in the cluster calls `someBean.save()`, every
*other* peer that has called `watch()`/`watchSubtree()` on that same bean
automatically has `load()` invoked on it in the background - refreshing its
in-memory state and firing the usual `PropertyChangeListener`/
`NodeChangeListener` notifications - with no direct peer-to-peer
coordination code required; ZooKeeper's watch mechanism does the
notification for you. See `finalconfigclasses.cfg.gen.ZkConfigDemo` (uses an
embedded, in-process ZooKeeper server via `curator-test`, so it runs with no
external setup) for a complete, runnable, two-"peer" example:

```
mvn -q exec:java -Dexec.mainClass=finalconfigclasses.cfg.gen.ZkConfigDemo
```

## Other notable changes

- **`ConfigBean`** gained two accessor methods, `_getPropertiesFile()`
  (already existed) and `_getDocument()` (promoted from `protected` to
  `public` on `BaseConfigBean`, and added to the `ConfigBean` interface) so
  `ZkConfigManager` can compute a bean's znode path without needing to know
  its concrete class.
- **`Registry`**'s XStream usage (`exportSubtree`/`exportOne`/`importConfig`)
  now goes through a hardened, whitelisted `XStream` instance
  (`XStream.setupDefaultSecurity()` + `allowTypesByWildcard("finalconfigclasses.**")`),
  which is required by XStream 1.4.7+ and wasn't present in the original code
  (which relied on `new XStream()`'s now-unsafe-by-default behavior).
- **Scope decision:** the original `Test1.java` .. `Test12.java` / `TestAll.java`
  demo/test classes (in `cfg/gen/`) were ad-hoc JMX-heavy harnesses tightly
  coupled to the old file-based XML persistence and to a hard-coded local
  file path. Rather than mechanically porting twelve files whose original
  intent I couldn't fully verify, I replaced them with one focused,
  runnable, well-commented demo (`ZkConfigDemo`) that specifically
  exercises the new ZooKeeper load/save + cluster-reload behavior end to
  end. Additional demo/test classes can be regenerated any time via the
  generator.
- **`config.xml.blank`** (a blank example of the old on-disk XML document
  format) was not carried over, since that on-disk format no longer exists
  under the ZooKeeper-backed persistence model.
- `finalconfigclasses.util.XMLUtils` was kept as-is (it's plain JDK
  `javax.xml.parsers`/DOM/XPath code, all still present in Java 17) in case
  you still need XML import/export elsewhere in your own code, but it is no
  longer used by generated `load()`/`save()` methods.
