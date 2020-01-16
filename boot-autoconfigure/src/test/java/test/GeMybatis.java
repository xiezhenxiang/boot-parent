package test;

import com.google.common.collect.Maps;
import indi.shine.boot.App;
import indi.shine.boot.base.jersey.JerseyHttp;
import indi.shine.boot.base.jwt.JwtToken;
import indi.shine.boot.base.model.constant.BaseErrorCode;
import indi.shine.boot.base.util.CodeMsgUtil;
import indi.shine.boot.base.util.MybatisGenUtil;
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
    public void httpSend() {
        System.out.println(CodeMsgUtil.getMsg(BaseErrorCode.REQUEST_BAD));
    }


    @Test
    public void geMybatis(){
        MybatisGenUtil.genMapperAndXml();
    }


    @Test
    @SuppressWarnings("unchecked")
    public void jwtTest(){
        HashMap m = Maps.newHashMap();
        m.put("id", "123");
        String str = jwtToken.createToken(m);
        jwtToken.checkToken(str);
        System.out.println(str);
    }
}
