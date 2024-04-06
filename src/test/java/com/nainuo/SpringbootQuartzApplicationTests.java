package com.nainuo;

import com.nainuo.VO.TimestampUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootQuartzApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(TimestampUtils.convertToUtilDate(1712301165));
    }

}
