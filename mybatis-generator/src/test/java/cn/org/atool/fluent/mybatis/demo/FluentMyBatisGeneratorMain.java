package cn.org.atool.fluent.mybatis.demo;

import cn.org.atool.fluent.mybatis.generator.MybatisGenerator;
import org.test4j.generator.mybatis.Generator;
import org.test4j.generator.mybatis.db.ColumnType;

public class FluentMyBatisGeneratorMain {
    static String url = "jdbc:mysql://localhost:3306/fluent_mybatis?useUnicode=true&characterEncoding=utf8";

    static String dao_interface = "cn.org.atool.fluent.mybatis.demo.MyCustomerInterface";

    /**
     * 使用main函数，是避免全量跑test时，误执行生成代码
     *
     * @param args
     */
    public static void main(String[] args) {
        String outputDir = System.getProperty("user.dir") + "/fluent-mybatis/src/test/java";
        MybatisGenerator.build()
            .globalConfig(config -> config.setOutputDir(outputDir, outputDir, outputDir)
                .setDataSource(url, "root", "password")
                .setBasePackage("cn.org.atool.fluent.mybatis.demo.generate"))
            .tables(config -> config
                .setTablePrefix("t_")
                .addTable("address")
                .addTable("t_user", true)
                .allTable(table -> {
                    table.setColumn("gmt_created", "gmt_modified", "is_deleted")
                        .column("is_deleted", ColumnType.BOOLEAN)
                        .addBaseDaoInterface("MyCustomerInterface<${entity}, ${query}, ${update}>", dao_interface)
                    ;
                })
            )
            .tables(config -> config
                .addTable("no_auto_id")
                .addTable("no_primary")
                .allTable(table -> table.setMapperPrefix("new"))
            )
            .execute();
    }
}