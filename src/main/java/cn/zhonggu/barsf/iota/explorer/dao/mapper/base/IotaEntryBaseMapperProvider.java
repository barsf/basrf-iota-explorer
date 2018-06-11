package cn.zhonggu.barsf.iota.explorer.dao.mapper.base;

import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.annotation.Version;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.*;
import tk.mybatis.mapper.version.VersionException;

import java.util.Set;

/**
 * Created by ZhuDH on 2018/3/29.
 */
public class IotaEntryBaseMapperProvider extends MapperTemplate {

    public IotaEntryBaseMapperProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    public String latest(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        setResultType(ms, entityClass);
        String tableName = tableName(entityClass);
        return new SQL() {{
            SELECT(" * ");
            FROM(tableName);
            for (EntityColumn entityColumn : EntityHelper.getPKColumns(entityClass)) {
                ORDER_BY(entityColumn.getColumn() + " desc");
            }
        }} + " limit 1";
    }

    public String first(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        setResultType(ms, entityClass);
        String tableName = tableName(entityClass);
        return new SQL() {{
            SELECT(" * ");
            FROM(tableName);
            for (EntityColumn entityColumn : EntityHelper.getPKColumns(entityClass)) {
                ORDER_BY(entityColumn.getColumn());
            }
        }} + " limit 1";
    }

    public String next(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        setResultType(ms, entityClass);
        String tableName = tableName(entityClass);
        return new SQL() {
            {
                SELECT("*");
                FROM(tableName);
                EntityHelper.getPKColumns(entityClass).stream().findFirst().ifPresent(entityColumn -> WHERE(entityColumn.getColumn() + " <![CDATA[ > ]]> #{pk}"));
                for (EntityColumn entityColumn : EntityHelper.getPKColumns(entityClass)) {
                    ORDER_BY(entityColumn.getColumn());
                }
            }
        }.toString() + " limit 1";
    }

    public String previous(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        setResultType(ms, entityClass);
        String tableName = tableName(entityClass);
        return new SQL() {
            {
                SELECT("*");
                FROM(tableName);
                EntityHelper.getPKColumns(entityClass).stream().findFirst().ifPresent(entityColumn -> WHERE(entityColumn.getColumn() + " <![CDATA[ < ]]> #{pk}"));
                for (EntityColumn entityColumn : EntityHelper.getPKColumns(entityClass)) {
                    ORDER_BY(entityColumn.getColumn() + "desc");
                }
            }
        }.toString() + " limit 1";
    }

    public String count(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        String tableName = tableName(entityClass);
        return new SQL() {{
            SELECT("count(*)");
            FROM(tableName);
        }}.toString();
    }


    public String keysStartingWith(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        String tableName = tableName(entityClass);
        return new SQL() {{
            SELECT(EntityHelper.getPKColumns(entityClass).stream().findFirst().get().getColumn());
            FROM(tableName);
            EntityHelper.getPKColumns(entityClass).stream().findFirst().ifPresent(entityColumn -> WHERE(entityColumn.getColumn() + " >= #{pk}"));
        }}.toString();
    }

    public String insertOrUpdate(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        //获取全部列
        Set<EntityColumn> columnList = EntityHelper.getColumns(entityClass);
        processKey(sql, entityClass, ms, columnList);
        sql.append(SqlHelper.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.insertColumns(entityClass, false, false, false));
        sql.append("<trim prefix=\"VALUES(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            //优先使用传入的属性值,当原属性property!=null时，用原属性
            //自增的情况下,如果默认有值,就会备份到property_cache中,所以这里需要先判断备份的值是否存在
            if (column.isIdentity()) {
                sql.append(SqlHelper.getIfCacheNotNull(column, column.getColumnHolder(null, "_cache", ",")));
            } else {
                //其他情况值仍然存在原property中
                sql.append(SqlHelper.getIfNotNull(column, column.getColumnHolder(null, null, ","), isNotEmpty()));
            }
            //当属性为null时，如果存在主键策略，会自动获取值，如果不存在，则使用null
            if (column.isIdentity()) {
                sql.append(SqlHelper.getIfCacheIsNull(column, column.getColumnHolder() + ","));
            } else {
                //当null的时候，如果不指定jdbcType，oracle可能会报异常，指定VARCHAR不影响其他
                sql.append(SqlHelper.getIfIsNull(column, column.getColumnHolder(null, null, ","), isNotEmpty()));
            }
        }
        sql.append("</trim>");
        sql.append(" ON DUPLICATE KEY UPDATE ");

        //获取全部列
        Set<EntityColumn> columnSet = EntityHelper.getColumns(entityClass);
        //对乐观锁的支持
        EntityColumn versionColumn = null;
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (versionColumn != null) {
                    throw new VersionException(entityClass.getCanonicalName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                versionColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column == versionColumn) {
                    Version version = versionColumn.getEntityField().getAnnotation(Version.class);
                    String versionClass = version.nextVersion().getCanonicalName();
                    //version = ${@tk.mybatis.mapper.version@nextVersionClass("versionClass", version)}
                    sql.append(column.getColumn())
                            .append(" = ${@tk.mybatis.mapper.version.VersionUtil@nextVersion(\"")
                            .append(versionClass).append("\", ")
                            .append(column.getProperty()).append(")},");
                } else {
                    sql.append(column.getColumnEqualsHolder(null) + ",");
                }
            } else if (column.isId()) {
                //set id = id,
                sql.append(column.getColumn()).append(" = ").append(column.getColumn()).append(",");
            }
        }
        sql.deleteCharAt(sql.length() - 1);

        return sql.toString();
    }

    private void processKey(StringBuilder sql, Class<?> entityClass, MappedStatement ms, Set<EntityColumn> columnList) {
        //Identity列只能有一个
        Boolean hasIdentityKey = false;
        //先处理cache或bind节点
        for (EntityColumn column : columnList) {
            if (column.isIdentity()) {
                //这种情况下,如果原先的字段有值,需要先缓存起来,否则就一定会使用自动增长
                //这是一个bind节点
                sql.append(SqlHelper.getBindCache(column));
                //如果是Identity列，就需要插入selectKey
                //如果已经存在Identity列，抛出异常
                if (hasIdentityKey) {
                    //jdbc类型只需要添加一次
                    if (column.getGenerator() != null && column.getGenerator().equals("JDBC")) {
                        continue;
                    }
                    throw new MapperException(ms.getId() + "对应的实体类" + entityClass.getCanonicalName() + "中包含多个MySql的自动增长列,最多只能有一个!");
                }
                //插入selectKey
                SelectKeyHelper.newSelectKeyMappedStatement(ms, column, entityClass, isBEFORE(), getIDENTITY(column));
                hasIdentityKey = true;
            }
        }
    }
}
