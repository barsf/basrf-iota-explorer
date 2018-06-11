package cn.zhonggu.barsf.iota.explorer;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;

/**
 * Created by ZhuDH on 2018/4/2.
 */
@Controller
@SpringBootApplication(exclude ={DataSourceAutoConfiguration.class} )
public class IotaExplorerStartUp extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(IotaExplorerStartUp.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    // 内置tomcat配置
    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(80);
        container.setDisplayName("iota-explorer");
    }


//    // 使用外置tomcat
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(IotaExplorerStartUp.class);
//    }
}
