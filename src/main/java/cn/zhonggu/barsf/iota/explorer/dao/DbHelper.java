package cn.zhonggu.barsf.iota.explorer.dao;


import cn.zhonggu.barsf.iota.explorer.dao.mapper.*;
import cn.zhonggu.barsf.iota.explorer.dao.mapper.base.IotaEntryBaseMapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import tk.mybatis.mapper.entity.Config;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by ZhuDH on 2018/3/28.
 */
@Component
public class DbHelper {
    @Scope(value = "singleton")
    @Bean
    public static SqlSessionFactory sessionFactory() throws IOException {
        SqlSessionFactory ssFactory = null;
        // TODO: 2018/4/2  配置文件魔法值
//        InputStream inputs = DbHelper.class.getClassLoader().getResourceAsStream("C:/Users/ZhuDH/Desktop/iota/iota-explorer/out/production/resources/mybatis/mybatis-config.xml");
        FileReader file = new FileReader(ResourceUtils.getFile("classpath:mybatis/mybatis-config.xml"));
        ssFactory = new SqlSessionFactoryBuilder().build(file);
        Configuration conf = ssFactory.getConfiguration();
        registMapper(conf);
        MapperHelper mapperHelper = new MapperHelper();
        Config config = new Config();
        mapperHelper.setConfig(config);
        // 注册通用Mapper
        mapperHelper.registerMapper(IotaEntryBaseMapper.class);
        mapperHelper.processConfiguration(ssFactory.getConfiguration());
        return ssFactory;
    }

    private static void registMapper(Configuration conf) {
        // 注册modelMapper
        conf.addMapper(TransactionMapper.class);
        conf.addMapper(TransactionTrytesMapper.class);
        conf.addMapper(TagMapper.class);
        conf.addMapper(StateDiffMapper.class);
        conf.addMapper(MilestoneMapper.class);
        conf.addMapper(BundleMapper.class);
        conf.addMapper(ApproveeMapper.class);
        conf.addMapper(AddressMapper.class);
    }

    @Bean
    public MapperScannerConfigurer getMapperScannerConfigurer(){
        MapperScannerConfigurer msc = new MapperScannerConfigurer();
        msc.setBasePackage("cn.zhonggu.barsf.iota.explorer.dao.mapper");
        return msc;
    }

    public static void closeFactory() {
        // 开了就没考虑关
    }

}
