package finalconfigclasses.cfg.web;

import finalconfigclasses.cfg.model.ConfigClasses;
import finalconfigclasses.cfg.model.ConfigModelIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serves config-classes.xml's structure (not its runtime values - see
 * {@link finalconfigclasses.cfg.zk.ZkConfigStore} for that) to the React
 * data-entry app as resolved JSON metadata, ready to drive a generic
 * recursive form component with no per-class frontend code.
 */
@RestController
@RequestMapping("/api/config-model")
public class ConfigModelController {

    private static final Logger log = LoggerFactory.getLogger(ConfigModelController.class);

    private final String configClassesXmlPath;

    public ConfigModelController(@Value("${app.config-classes-xml-path}") String configClassesXmlPath) {
        this.configClassesXmlPath = configClassesXmlPath;
    }

    /**
     * All declared config-classes, fully resolved (extends flattened, types
     * normalized) and keyed by class name - what the React app fetches once
     * on startup so nested {@code property} references can be looked up
     * client-side without another round trip.
     */
    @GetMapping
    public Map<String, ConfigModelResolver.ResolvedConfigClass> getAll() {
        ConfigClasses classes = readConfigClasses();
        List<ConfigModelResolver.ResolvedConfigClass> resolved = ConfigModelResolver.resolveAll(classes);
        return resolved.stream().collect(Collectors.toMap(rc -> rc.name, rc -> rc));
    }

    @GetMapping("/{name}")
    public ConfigModelResolver.ResolvedConfigClass getOne(@PathVariable String name) {
        ConfigClasses classes = readConfigClasses();
        return ConfigModelResolver.resolveAll(classes).stream()
                .filter(rc -> rc.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unknown config-class: " + name));
    }

    private ConfigClasses readConfigClasses() {
        try {
            return ConfigModelIO.read(new File(configClassesXmlPath));
        } catch (Exception e) {
            log.error("Failed to read config-classes.xml from {}", configClassesXmlPath, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read config-classes.xml", e);
        }
    }
}
