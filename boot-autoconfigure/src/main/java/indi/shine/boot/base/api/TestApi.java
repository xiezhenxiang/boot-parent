package indi.shine.boot.base.api;

import indi.shine.boot.base.model.api.resp.ReturnT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * @author xiezhenxiang 2020/1/17
 */
@Path("auth")
@Api("Test")
@RestController
public class TestApi {

    @ApiOperation("登陆")
    @POST
    @Path("/login")
    public ReturnT login() {
        return ReturnT.fail();
    }
}
