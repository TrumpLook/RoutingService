package com.example.demo.services;

import com.example.demo.models.GraphNodeEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import java.util.List;
import java.util.Optional;

@Repository
public class GraphNodeRepository {
    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    public GraphNodeRepository(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(GraphNodeEntity graphNode) {
        jdbcClient.sql("""
                insert into graph_node (id, lat, lon)
                values (:id, :lat, :lon)
                on conflict (id) do nothing
                """)
                .param("id", graphNode.id())
                .param("lat", graphNode.lat())
                .param("lon", graphNode.lon())
                .update();
    }

    public void saveAll(List<GraphNodeEntity> graphNodes) {
        final int batchSize = 1000;
        for (int start = 0; start < graphNodes.size(); start += batchSize) {
            int end = Math.min(start + batchSize, graphNodes.size());
            List<GraphNodeEntity> batch = graphNodes.subList(start, end);
            jdbcTemplate.batchUpdate(
                    """
                    insert into graph_node (id, lat, lon)
                    values (?, ?, ?)
                    on conflict (id) do nothing
                    """,
                    batch,
                    batch.size(),
                    (ps, graphNode) -> {
                        ps.setLong(1, graphNode.id());
                        ps.setDouble(2, graphNode.lat());
                        ps.setDouble(3, graphNode.lon());
                    }
            );
        }
    }

    public Optional<GraphNodeEntity> findById(long id) {
        return jdbcClient.sql("""
                select id, lat, lon
                from graph_node
                where id = :id
                """)
                .param("id", id)
                .query((rs, rowNum) -> new GraphNodeEntity(
                        rs.getLong("id"),
                        rs.getDouble("lat"),
                        rs.getDouble("lon")
                ))
                .optional();
    }

    public Optional<Long> findNearestId(double lat, double lon) {
        double[] radiuses = {0.002, 0.01, 0.05, 0.2, 1.0};

        for (double radius : radiuses) {
            Optional<Long> candidate = findNearestIdInRadius(lat, lon, radius);
            if (candidate.isPresent()) {
                return candidate;
            }
        }

        return jdbcClient.sql("""
                select id
                from graph_node
                order by power(lat - :lat, 2) + power(lon - :lon, 2)
                limit 1
                """)
                .param("lat", lat)
                .param("lon", lon)
                .query(Long.class)
                .optional();
    }

    private Optional<Long> findNearestIdInRadius(double lat, double lon, double radius) {
        return jdbcClient.sql("""
                select id
                from graph_node
                where lat between :minLat and :maxLat
                  and lon between :minLon and :maxLon
                order by power(lat - :lat, 2) + power(lon - :lon, 2)
                limit 1
                """)
                .param("minLat", lat - radius)
                .param("maxLat", lat + radius)
                .param("minLon", lon - radius)
                .param("maxLon", lon + radius)
                .param("lat", lat)
                .param("lon", lon)
                .query(Long.class)
                .optional();
    }
}
