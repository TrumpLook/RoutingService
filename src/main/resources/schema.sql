create table if not exists graph_node (
    id bigint primary key,
    lat double precision not null,
    lon double precision not null
);

create index if not exists idx_graph_node_lat on graph_node (lat);
create index if not exists idx_graph_node_lon on graph_node (lon);
