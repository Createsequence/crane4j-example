package cn.crane4j.mybatis.plus.extension.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.mybatis.plus.MpMethodContainerProvider;
import cn.crane4j.springboot.support.OperateTemplate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * <p>使用MP插件进行字段填充的示例
 *
 * <p>执行测试用例前，务必确保数据源已有 foo 表及相应数据
 * <pre type = "sql">{@code
 * -- ----------------------------
 * -- Table structure for foo
 * -- ----------------------------
 * DROP TABLE IF EXISTS `foo`;
 * CREATE TABLE `foo`  (
 *   `id` int(11) NOT NULL AUTO_INCREMENT,
 *   `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '',
 *   `age` int(3) NULL DEFAULT NULL,
 *   `sex` int(1) NULL DEFAULT NULL,
 *   PRIMARY KEY (`id`) USING BTREE
 * ) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
 *
 * -- ----------------------------
 * -- Records of foo
 * -- ----------------------------
 * INSERT INTO `foo` VALUES (1, '小明', 18, 1);
 * INSERT INTO `foo` VALUES (2, '小红', 18, 0);
 * INSERT INTO `foo` VALUES (3, '小刚', 17, 1);
 * INSERT INTO `foo` VALUES (4, '小李', 19, 0);
 *
 * SET FOREIGN_KEY_CHECKS = 1;
 * }</pre>
 *
 * @author huangchengxing
 */
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jMybatisPlusExampleApplication.class})
public class MpExtensionExample {

    @Autowired
    private OperateTemplate operateTemplate;

    /**
     * 默认的主键字段，默认查询全部字段，
     * 等同于 select * from foo where id in ?，
     * 根据id查询，并将查询结果按id分组，然后对应到目标对象
     */
    @Test
    public void testPrimaryKeyAndAllColumns() {
        List<FooVO> fooList = Arrays.asList(new FooVO().setId(1), new FooVO().setId(2));
        operateTemplate.executeIfMatchAnyGroups(fooList, "testPrimaryKeyAndAllColumns");

        FooVO foo1 = fooList.get(0);
        checkFoo(foo1, 1, "小明", 18, 1);
        FooVO foo2 = fooList.get(1);
        checkFoo(foo2, 2, "小红", 18, 0);
    }

    /**
     * 默认的主键字段，指定的全部字段，
     * 等同于 select id, name from foo where id in ?，
     * 根据id查询，并将查询结果按id分组，然后对应到目标对象
     */
    @Test
    public void testPrimaryKeyAndCustomColumns() {
        List<FooVO> fooList = Arrays.asList(new FooVO().setId(1), new FooVO().setId(2));
        operateTemplate.executeIfMatchAnyGroups(fooList, "testPrimaryKeyAndCustomColumns");

        FooVO foo1 = fooList.get(0);
        checkFoo(foo1, 1, "小明", null, null);
        FooVO foo2 = fooList.get(1);
        checkFoo(foo2, 2, "小红", null, null);
    }

    /**
     * 指定的key字段，默认查询全部字段，
     * 等同于 select * from foo where name in ?，
     * 根据name查询，并将查询结果按name分组，然后对应到目标对象
     */
    @Test
    public void testCustomKeyAndAllColumns() {
        List<FooVO> fooList = Arrays.asList(new FooVO().setUserName("小明"), new FooVO().setUserName("小红"));
        operateTemplate.executeIfMatchAnyGroups(fooList, "testCustomKeyAndAllColumns");

        FooVO foo1 = fooList.get(0);
        checkFoo(foo1, 1, "小明", 18, 1);
        FooVO foo2 = fooList.get(1);
        checkFoo(foo2, 2, "小红", 18, 0);
    }

    /**
     * 指定的key字段与查询字段，
     * 等同于 select name, age from foo where name in ?，
     * 根据name查询，并将查询结果按name分组，然后对应到目标对象
     */
    @Test
    public void testCustomKeyAndCustomColumns() {
        List<FooVO> fooList = Arrays.asList(new FooVO().setUserName("小明"), new FooVO().setUserName("小红"));
        operateTemplate.executeIfMatchAnyGroups(fooList, "testCustomKeyAndCustomColumns");

        FooVO foo1 = fooList.get(0);
        checkFoo(foo1, null, "小明", 18, null);
        FooVO foo2 = fooList.get(1);
        checkFoo(foo2, null, "小红", 18, null);
    }

    private static void checkFoo(FooVO foo, Integer id, String name, Integer age, Integer sex) {
        Assert.assertNotNull(foo);
        Assert.assertEquals(id, foo.getId());
        Assert.assertEquals(name, foo.getUserName());
        Assert.assertEquals(age, foo.getUserAge());
        Assert.assertEquals(sex, foo.getUserSex());
    }

    // 配置映射模板，上面直接引用模板字段
    @MappingTemplate({
        @Mapping(src = "name", ref = "userName"),
        @Mapping(src = "age", ref = "userAge"),
        @Mapping(src = "sex", ref = "userSex"),
        @Mapping(src = "id", ref = "id")
    })
    @RequiredArgsConstructor
    @Accessors(chain = true)
    @Data
    private static class FooVO {
        // 声明两个不同组的操作，都基于id字段值进行
        @Assemble(
            container = "container('fooMapper')",
            groups = "testPrimaryKeyAndAllColumns",
            containerProvider = MpMethodContainerProvider.class,
            propTemplates = FooVO.class
        )
        @Assemble(container = "container('fooMapper', {'name'})",
            groups = "testPrimaryKeyAndCustomColumns",
            containerProvider = MpMethodContainerProvider.class,
            propTemplates = FooVO.class
        )
        private Integer id;

        // 声明两个不同组的操作，都基于name字段值进行
        @Assemble(container = "container('fooMapper', 'name')",
            groups = "testCustomKeyAndAllColumns",
            containerProvider = MpMethodContainerProvider.class,
            propTemplates = FooVO.class
        )
        @Assemble(
            container = "container('fooMapper', 'name', {'age'})",
            groups = "testCustomKeyAndCustomColumns",
            containerProvider = MpMethodContainerProvider.class,
            propTemplates = FooVO.class
        )
        private String userName;
        private Integer userAge;
        private Integer userSex;
    }

}
