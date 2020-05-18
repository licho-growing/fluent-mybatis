package cn.org.atool.fluent.mybatis;

import cn.org.atool.fluent.mybatis.injector.FluentMybatisSqlInjector;
import cn.org.atool.fluent.mybatis.mapper.IMapper;
import com.mybatisplus.core.config.GlobalConfig;
import com.mybatisplus.core.injector.ISqlInjector;
import com.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.mybatisplus.extension.plugins.PaginationInterceptor;
import com.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
public class MybatisPlusBootConfiguration {
    @Bean
    @ConditionalOnMissingBean({MybatisSqlSessionFactoryBean.class, SqlSessionFactory.class})
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource, ISqlInjector sqlInjector) {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        {
            bean.setDataSource(dataSource);
            GlobalConfig gc = GlobalConfigUtils.defaults();
            gc.setSqlInjector(sqlInjector);
            gc.setSuperMapperClass(IMapper.class);
            bean.setGlobalConfig(gc);

            bean.setPlugins(paginationInterceptor());
        }
        return bean;
    }

    @Bean
    @ConditionalOnMissingBean
    public ISqlInjector sqlInjector() {
        return new FluentMybatisSqlInjector();
    }

    /**
     * 分页切面
     *
     * @return
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
