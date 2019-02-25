package test;

import com.boot.App;
import com.boot.base.jwt.JwtToken;
import com.boot.base.util.MybatisGenUtil;
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

    @Test
    public void jwtTest(){
        MybatisGenUtil.genMapperAndXml();


        HashMap m = Maps.newHashMap();
        m.put("id", "123");
        String str = jwtToken.createToken(m);
        jwtToken.checkToken(str);
        System.out.println(str);
    }
}
