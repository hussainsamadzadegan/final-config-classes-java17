package finalconfigclasses.cfg.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the Vite dev server (default http://localhost:5173) to call this
 * API directly. The React app's own vite.config.ts proxies /api instead
 * (so no browser CORS request is actually made in the normal `npm run dev`
 * flow), but this stays as a fallback for hitting the API directly (e.g.
 * from a REST client, or a frontend build that isn't proxied).
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
