package cn.org.atool.fluent.mybatis.entity.base;

import cn.org.atool.fluent.mybatis.annotation.FluentMybatis;
import cn.org.atool.fluent.mybatis.entity.FluentEntityInfo;
import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import com.squareup.javapoet.JavaFile;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import static com.sun.tools.javac.tree.JCTree.JCVariableDecl;

/**
 * 代码编译处理基类
 *
 * @author darui.wu
 */
public abstract class BaseProcessor extends AbstractProcessor {
    protected Filer filer;

    protected Messager messager;

    // javac 编译器相关类
    protected Trees trees;

    protected TreeMaker treeMaker;

    private boolean mIsFirstRound = true;

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!mIsFirstRound) {
            return false;
        }
        mIsFirstRound = false;
        messager.printMessage(Diagnostic.Kind.NOTE, "process fluent mybatis begin !!!");

        roundEnv.getElementsAnnotatedWith(FluentMybatis.class).stream()
            .filter(it -> it instanceof TypeElement)
            .map(it -> (TypeElement) it)
            .forEach(it -> {
                FluentEntityInfo entityInfo = null;
                try {
                    entityInfo = this.parseEntity(it);
                    List<JavaFile> javaFiles = this.generateJavaFile(it, entityInfo);
                    for (JavaFile javaFile : javaFiles) {
                        javaFile.writeTo(filer);
                    }
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                        it.getQualifiedName() + ":\nEntityInfo:" + entityInfo + "\n" + MybatisUtil.toString(e));
                    throw new RuntimeException(e);
                }
            });
        messager.printMessage(Diagnostic.Kind.NOTE, "process fluent mybatis end !!!");
        return true;
    }

    /**
     * 返回编译Entity类所在package
     *
     * @param entity
     * @return
     */
    protected String getCuPackageName(TypeElement entity) {
        TreePath treePath = trees.getPath(entity);
        JCCompilationUnit cu = (JCCompilationUnit) treePath.getCompilationUnit();
        return cu == null ? "" : cu.getPackageName().toString();
    }

    /**
     * 返回entity类定义的字段
     *
     * @param element
     * @param curTree
     * @return
     */
    public List<JCVariableDecl> translate(TypeElement element, JCTree curTree) {
        return new FieldTreeTranslator((Name) element.getSimpleName()).accept(curTree);
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.filer = env.getFiler();
        this.messager = env.getMessager();

        this.trees = Trees.instance(env);
        if (env instanceof JavacProcessingEnvironment) {
            Context ctx = ((JavacProcessingEnvironment) env).getContext();
            this.treeMaker = TreeMaker.instance(ctx);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supported = new HashSet<>();
        supported.add(FluentMybatis.class.getCanonicalName());
        return supported;
    }

    /**
     * 生成java文件
     *
     * @param curElement
     * @param fluentEntityInfo
     */
    protected abstract java.util.List<JavaFile> generateJavaFile(TypeElement curElement, FluentEntityInfo fluentEntityInfo);


    /**
     * 解析处理每一个FluentMyBatis Entity文件
     *
     * @param it
     * @return
     */
    protected abstract FluentEntityInfo parseEntity(TypeElement it);
}