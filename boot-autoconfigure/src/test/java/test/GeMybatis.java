package test;

import indi.fly.boot.App;
import indi.fly.boot.base.jersey.JerseyHttp;
import indi.fly.boot.base.jwt.JwtToken;
import indi.fly.boot.base.util.HttpUtils;
import indi.fly.boot.base.util.MybatisGenUtil;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class GeMybatis {

    @Autowired
    JwtToken jwtToken;
    @Autowired
    JerseyHttp jerseyHttp;

    @Test
    public void geMybatis(){
        MybatisGenUtil.genMapperAndXml();
    }

    @Test
    public void httpTest(){
        String url = "https://www.facebook.com";
        String str = HttpUtils.sendGet(url, "127.0.0.1", 1080);
        System.out.println(str);
    }

    @Test
    public void jwtTest(){
        HashMap m = Maps.newHashMap();
        m.put("id", "123");
        String str = jwtToken.createToken(m);
        jwtToken.checkToken(str);
        System.out.println(str);
    }
}
