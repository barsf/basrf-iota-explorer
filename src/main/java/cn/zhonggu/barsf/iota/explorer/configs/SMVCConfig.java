package cn.zhonggu.barsf.iota.explorer.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by ZhuDH on 2018/4/2.
 */
@Configuration
public class SMVCConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        //将 /static/开头的请求,重定位到,方便静态查看
//        registry.addResourceHandler("static/js/**")
//                .addResourceLocations("classpath:/static/js/");
//        registry.addResourceHandler("static/css/**")
//                .addResourceLocations("classpath:/static/css/");
//        registry.addResourceHandler("static/images/**")
//                .addResourceLocations("classpath:/static/images/");
//        super.addResourceHandlers(registry);
    }

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setQueueCapacity(100);
        executor.setMaxPoolSize(25);
        return executor;
    }
}
