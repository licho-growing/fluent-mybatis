package cn.org.atool.fluent.mybatis.entity.generator;

import cn.org.atool.fluent.mybatis.base.impl.BaseDaoImpl;
import cn.org.atool.fluent.mybatis.entity.FluentEntityInfo;
import cn.org.atool.fluent.mybatis.entity.base.AbstractGenerator;
import cn.org.atool.fluent.mybatis.entity.base.ClassNameConst;
import cn.org.atool.fluent.mybatis.entity.base.DaoInterfaceParser;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;

import static cn.org.atool.fluent.mybatis.entity.base.ClassNameConst.Pack_BaseDao;
import static cn.org.atool.fluent.mybatis.entity.base.ClassNameConst.Suffix_BaseDao;
import static cn.org.atool.fluent.mybatis.entity.generator.MapperGenerator.getMapperName;

public class BaseDaoGenerator extends AbstractGenerator {
    public BaseDaoGenerator(TypeElement curElement, FluentEntityInfo fluentEntityInfo) {
        super(curElement, fluentEntityInfo);
        this.packageName = fluentEntityInfo.getPackageName(Pack_BaseDao);
        this.klassName = fluentEntityInfo.getNoSuffix() + Suffix_BaseDao;
    }

    @Override
    protected void build(TypeSpec.Builder builder) {
        builder.addModifiers(Modifier.ABSTRACT)
            .superclass(this.superBaseDaoImplKlass())
            .addSuperinterface(this.superMappingClass());
        for (Map.Entry<String, List<String>> daoInterface : fluentEntityInfo.getDaoInterfaces().entrySet()) {
            this.addInterface(builder, daoInterface.getKey(), daoInterface.getValue());
        }
        builder.addField(this.f_mapper())
            .addMethod(this.m_mapper())
            .addMethod(this.m_query())
            .addMethod(this.m_updater())
            .addMethod(this.m_findPkColumn());
    }

    private void addInterface(TypeSpec.Builder builder, String daoInterface, List<String> argNames) {
        List<ClassName> argClassNames = DaoInterfaceParser.getClassNames(fluentEntityInfo, argNames);
        int dot = daoInterface.lastIndexOf('.');
        String packageName = "";
        String simpleClassName = daoInterface;
        if (dot > 0) {
            packageName = daoInterface.substring(0, dot);
            simpleClassName = daoInterface.substring(dot + 1);
        }
        if (argClassNames.isEmpty()) {
            builder.addSuperinterface(ClassName.get(packageName, simpleClassName));
        } else {
            builder.addSuperinterface(parameterizedType(
                ClassName.get(packageName, simpleClassName), argClassNames.toArray(new ClassName[0])
            ));
        }
    }

    private TypeName superMappingClass() {
        return ClassName.get(MappingGenerator.getPackageName(fluentEntityInfo), MappingGenerator.getClassName(fluentEntityInfo));
    }

    private TypeName superBaseDaoImplKlass() {
        ClassName baseImpl = ClassName.get(BaseDaoImpl.class.getPackage().getName(), BaseDaoImpl.class.getSimpleName());
        ClassName entity = fluentEntityInfo.className();
        return ParameterizedTypeName.get(baseImpl, entity);
    }

    /**
     * protected EntityMapper mapper;
     *
     * @return
     */
    private FieldSpec f_mapper() {
        return FieldSpec.builder(MapperGenerator.className(fluentEntityInfo), "mapper")
            .addModifiers(Modifier.PROTECTED)
            .addAnnotation(ClassNameConst.Autowired)
            .addAnnotation(AnnotationSpec.builder(ClassNameConst.Qualifier)
                .addMember("value", "$S", getMapperName(fluentEntityInfo)).build()
            )
            .build();
    }

    /**
     * public AddressMapper mapper() {}
     *
     * @return
     */
    private MethodSpec m_mapper() {
        return MethodSpec.methodBuilder("mapper")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(MapperGenerator.className(fluentEntityInfo))
            .addStatement(super.codeBlock("return mapper"))
            .build();
    }

    /**
     * public EntityQuery query() {}
     *
     * @return
     */
    private MethodSpec m_query() {
        return MethodSpec.methodBuilder("query")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(QueryGenerator.className(fluentEntityInfo))
            .addStatement("return new $T()", QueryGenerator.className(fluentEntityInfo))
            .build();
    }

    /**
     * public AddressUpdate updater() {}
     *
     * @return
     */
    private MethodSpec m_updater() {
        return MethodSpec.methodBuilder("updater")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(UpdaterGenerator.className(fluentEntityInfo))
            .addStatement("return new $T()", UpdaterGenerator.className(fluentEntityInfo))
            .build();
    }

    /**
     * public String findPkColumn() {}
     *
     * @return
     */
    private MethodSpec m_findPkColumn() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findPkColumn")
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addJavadoc("返回实体类主键值");
        if (fluentEntityInfo.getPrimary() == null) {
            builder.addStatement("throw new $T($S)",
                RuntimeException.class, "primary key not found.");
        } else {
            builder.addStatement("return $T.$L.column",
                MappingGenerator.className(fluentEntityInfo), fluentEntityInfo.getPrimary().getProperty());
        }
        return builder.build();
    }

    @Override
    protected boolean isInterface() {
        return false;
    }
}