package cn.crane4j.mybatis.plus.extension.example;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author huangchengxing
 */
@Mapper
public interface FooMapper extends BaseMapper<Foo> {
}
