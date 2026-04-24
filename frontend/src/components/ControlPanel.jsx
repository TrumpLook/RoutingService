export default function ControlPanel({
  weightType,
  routeInfo,
  isLoading,
  error,
  onWeightTypeChange,
  onReset
}) {
  return (
    <aside className="panel">
      <div className="panel-block">
        <p className="eyebrow">Routing Lab</p>
        <h1>Сравнение алгоритмов маршрутизации</h1>
        <p className="panel-text">
          Выбери тип веса и кликни две точки на карте. Маршруты Дейкстры и A* построятся сразу.
        </p>
      </div>

      <div className="panel-block">
        <label className="field">
          <span>Вес графа</span>
          <select value={weightType} onChange={(event) => onWeightTypeChange(event.target.value)}>
            <option value="TIME">TIME</option>
            <option value="DISTANCE">DISTANCE</option>
          </select>
        </label>

        <button className="reset-button" type="button" onClick={onReset}>
          Сбросить точки
        </button>
      </div>

      <div className="panel-block">
        <h2>Состояние</h2>
        <p className="status-line">{isLoading ? "Строим два маршрута..." : "Ожидание выбора точек"}</p>
        {error ? <p className="error-text">{error}</p> : null}
      </div>

      <div className="panel-block">
        <h2>Маршруты</h2>
        <div className="legend">
          <div className="legend-item">
            <span className="legend-line legend-line-red"></span>
            <span>Dijkstra</span>
          </div>
          <div className="legend-item">
            <span className="legend-line legend-line-blue"></span>
            <span>A*</span>
          </div>
        </div>
      </div>

      <div className="panel-block">
        <h2>Dijkstra</h2>
        {routeInfo?.dijkstra ? (
          <Metrics routeInfo={routeInfo.dijkstra} />
        ) : (
          <p className="panel-text">Пока нет рассчитанного маршрута.</p>
        )}
      </div>

      <div className="panel-block">
        <h2>A*</h2>
        {routeInfo?.astar ? (
          <Metrics routeInfo={routeInfo.astar} />
        ) : (
          <p className="panel-text">Пока нет рассчитанного маршрута.</p>
        )}
      </div>
    </aside>
  );
}

function Metrics({ routeInfo }) {
  return (
    <dl className="metrics">
      <div>
        <dt>Путь найден</dt>
        <dd>{routeInfo.pathFound ? "Да" : "Нет"}</dd>
      </div>
      <div>
        <dt>Общий вес</dt>
        <dd>{formatNumber(routeInfo.totalWeight)}</dd>
      </div>
      <div>
        <dt>Посещено вершин</dt>
        <dd>{formatInteger(routeInfo.visitedNodes)}</dd>
      </div>
      <div>
        <dt>Расслаблено ребер</dt>
        <dd>{formatInteger(routeInfo.relaxedEdges)}</dd>
      </div>
      <div>
        <dt>Время, нс</dt>
        <dd>{formatInteger(routeInfo.executionTimeNanos)}</dd>
      </div>
      <div>
        <dt>Точек в пути</dt>
        <dd>{formatInteger(routeInfo.points?.length ?? 0)}</dd>
      </div>
    </dl>
  );
}

function formatNumber(value) {
  if (typeof value !== "number" || Number.isNaN(value)) {
    return "-";
  }

  return new Intl.NumberFormat("ru-RU", {
    maximumFractionDigits: 2
  }).format(value);
}

function formatInteger(value) {
  if (typeof value !== "number" || Number.isNaN(value)) {
    return "-";
  }

  return new Intl.NumberFormat("ru-RU", {
    maximumFractionDigits: 0
  }).format(value);
}
