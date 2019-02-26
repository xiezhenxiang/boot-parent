package indi.fly.boot.base.api;

import indi.fly.boot.base.jersey.JerseySwaggerProperties;
import indi.fly.boot.base.util.CommonHelper;
import org.glassfish.jersey.server.mvc.Template;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
@Path("/")
@Produces({"text/html"})
public class SwaggerView {
    @Autowired
    private JerseySwaggerProperties jerseySwaggerProperties;

    public SwaggerView() {
    }

    @Path("/Swagger.html")
    @GET
    @Template(
            name = "/index"
    )
    public Map<String, Object> indexView() {

        Map<String, Object> map = new HashMap();
        if (Objects.nonNull(this.jerseySwaggerProperties.getHost())) {
            map.put("host", this.jerseySwaggerProperties.getHost());
        } else {
            map.put("host", this.jerseySwaggerProperties.getIp() + ":" + this.jerseySwaggerProperties.getPort());
        }

        map.put("path", CommonHelper.parsePath(this.jerseySwaggerProperties.getBasePath()));
        map.put("cdn", this.jerseySwaggerProperties.getCdn());
        return map;
    }
}
