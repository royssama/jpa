package com.example.backend.config;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.listener.logging.AbstractQueryLogEntryCreator;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;

@Configuration
public class InlinedSqlLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger("jdbc.sql");

    @Bean
    public QueryExecutionListener inlinedSqlQueryExecutionListener() {
        return new QueryExecutionListener() {
            @Override
            public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
            }

            @Override
            public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                if (!execInfo.isSuccess()) {
                    return;
                }
                for (QueryInfo qi : queryInfoList) {
                    String query = qi.getQuery();
                    List<List<ParameterSetOperation>> paramsList = qi.getParametersList();
                    if (execInfo.getStatementType() != StatementType.PREPARED
                            || paramsList == null
                            || paramsList.isEmpty()) {
                        log.info("{} ms |\n{}", execInfo.getElapsedTime(), formatSql(query));
                        continue;
                    }
                    for (List<ParameterSetOperation> ops : paramsList) {
                        log.info("{} ms |\n{}", execInfo.getElapsedTime(), formatSql(inlinePrepared(query, ops)));
                    }
                }
            }
        };
    }

    /** Hibernate {@link FormatStyle#BASIC} 로 SELECT/INSERT/UPDATE/DELETE 줄바꿈·들여쓰기 */
    private static String formatSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return sql;
        }
        try {
            return FormatStyle.BASIC.getFormatter().format(sql);
        } catch (RuntimeException ignored) {
            return sql;
        }
    }

    private String inlinePrepared(String sql, List<ParameterSetOperation> ops) {
        if (ops == null || ops.isEmpty()) {
            return sql;
        }
        SortedMap<String, String> paramMap = ParameterDisplayHelper.INSTANCE.parameterMap(ops);
        if (paramMap.isEmpty()) {
            return sql;
        }
        String result = sql;
        for (String display : paramMap.values()) {
            int q = result.indexOf('?');
            if (q < 0) {
                break;
            }
            String literal = toSqlLiteral(display);
            result = result.substring(0, q) + literal + result.substring(q + 1);
        }
        return result;
    }

    private static final Pattern NUMERIC = Pattern.compile("^-?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?$");

    private static String toSqlLiteral(String display) {
        if (display == null) {
            return "NULL";
        }
        if (display.startsWith("NULL")) {
            return "NULL";
        }
        if ("true".equalsIgnoreCase(display) || "false".equalsIgnoreCase(display)) {
            return display.toUpperCase();
        }
        if (NUMERIC.matcher(display).matches()) {
            return display;
        }
        return "'" + display.replace("'", "''") + "'";
    }

    /**
     * {@link AbstractQueryLogEntryCreator#getParametersToDisplay} 가 protected 이므로 서브클래스에서만 호출 가능하다.
     */
    private static final class ParameterDisplayHelper extends AbstractQueryLogEntryCreator {

        static final ParameterDisplayHelper INSTANCE = new ParameterDisplayHelper();

        @Override
        public String getLogEntry(
                ExecutionInfo execInfo,
                List<QueryInfo> queryInfoList,
                boolean writeDataSourceName,
                boolean writeConnectionId,
                boolean writeIsolation) {
            throw new UnsupportedOperationException();
        }

        SortedMap<String, String> parameterMap(List<ParameterSetOperation> ops) {
            return getParametersToDisplay(ops);
        }
    }
}
