import { useEffect, useRef, useState } from "react";
import ControlPanel from "./components/ControlPanel";
import MapView from "./components/MapView";
import { buildRoute, getActiveEvents } from "./api";

const MIN_EVENT_ROUTE_DISTANCE_METERS = 50;
const EARTH_RADIUS_METERS = 6371000;

export default function App() {
  const [weightType, setWeightType] = useState("TIME");
  const [startPoint, setStartPoint] = useState(null);
  const [endPoint, setEndPoint] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [activeEvents, setActiveEvents] = useState([]);
  const [rerouteVersion, setRerouteVersion] = useState(0);
  const [showAlternative, setShowAlternative] = useState(false);
  const processedEventIdsRef = useRef(new Set());

  useEffect(() => {
    if (!startPoint || !endPoint) {
      return;
    }

    let isCancelled = false;

    async function requestRoute() {
      setIsLoading(true);
      setError("");

      try {
        const requestPayload = {
          startLat: startPoint.lat,
          startLon: startPoint.lon,
          endLat: endPoint.lat,
          endLon: endPoint.lon,
          weightType
        };

        const [dijkstra, astar, alternative] = await Promise.all([
          buildRoute({ ...requestPayload, algorithm: "DIJKSTRA", alternative: false }),
          buildRoute({ ...requestPayload, algorithm: "ASTAR", alternative: false }),
          buildRoute({ ...requestPayload, algorithm: "ASTAR", alternative: true })
        ]);

        if (!isCancelled) {
          setRouteInfo({ dijkstra, astar, alternative });
        }
      } catch (requestError) {
        if (!isCancelled) {
          setRouteInfo(null);
          setError(requestError.message);
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    requestRoute();

    return () => {
      isCancelled = true;
    };
  }, [endPoint, rerouteVersion, startPoint, weightType]);

  useEffect(() => {
    let isCancelled = false;

    async function loadEvents() {
      try {
        const response = await getActiveEvents();
        if (!isCancelled) {
          setActiveEvents(response);
        }
      } catch {
        if (!isCancelled) {
          setActiveEvents([]);
        }
      }
    }

    loadEvents();
    const intervalId = window.setInterval(loadEvents, 3000);

    return () => {
      isCancelled = true;
      window.clearInterval(intervalId);
    };
  }, []);

  useEffect(() => {
    if (!startPoint || !endPoint || !routeInfo || isLoading) {
      return;
    }

    const currentEventIds = new Set(activeEvents.map((event) => event.eventId));
    for (const eventId of processedEventIdsRef.current) {
      if (!currentEventIds.has(eventId)) {
        processedEventIdsRef.current.delete(eventId);
      }
    }

    const routeBlockingEvent = activeEvents.find((event) => {
      if (processedEventIdsRef.current.has(event.eventId)) {
        return false;
      }

      return isEventOnCurrentRoute(event, routeInfo, showAlternative);
    });

    if (!routeBlockingEvent) {
      return;
    }

    processedEventIdsRef.current.add(routeBlockingEvent.eventId);
    setRerouteVersion((currentVersion) => currentVersion + 1);
  }, [activeEvents, endPoint, isLoading, routeInfo, startPoint, showAlternative]);

  function handleMapClick(latlng) {
    setError("");
    setRouteInfo(null);
    processedEventIdsRef.current.clear();

    if (!startPoint) {
      setStartPoint({ lat: latlng.lat, lon: latlng.lng });
      return;
    }

    if (!endPoint) {
      setEndPoint({ lat: latlng.lat, lon: latlng.lng });
      return;
    }

    setStartPoint({ lat: latlng.lat, lon: latlng.lng });
    setEndPoint(null);
  }

  function handleReset() {
    setStartPoint(null);
    setEndPoint(null);
    setRouteInfo(null);
    setError("");
    processedEventIdsRef.current.clear();
  }

  return (
    <main className="app-layout">
      <ControlPanel
        weightType={weightType}
        routeInfo={routeInfo}
        isLoading={isLoading}
        error={error}
        onWeightTypeChange={setWeightType}
        onReset={handleReset}
        showAlternative={showAlternative}
        onShowAlternativeChange={setShowAlternative}
      />
      <section className="canvas">
        <MapView
          startPoint={startPoint}
          endPoint={endPoint}
          routeInfo={routeInfo}
          activeEvents={activeEvents}
          onMapClick={handleMapClick}
          showAlternative={showAlternative}
        />
      </section>
    </main>
  );
}

function isEventOnCurrentRoute(event, routeInfo, showAlternative) {
  const onPrimary = isEventOnRoutePoints(event, routeInfo?.dijkstra?.points)
    || isEventOnRoutePoints(event, routeInfo?.astar?.points);
  if (showAlternative && routeInfo?.alternative) {
    return onPrimary || isEventOnRoutePoints(event, routeInfo.alternative.points);
  }
  return onPrimary;
}

function isEventOnRoutePoints(event, points) {
  if (!points || points.length < 2) {
    return false;
  }

  const thresholdMeters = Math.max(
    Number(event.radiusMeters) || 0,
    MIN_EVENT_ROUTE_DISTANCE_METERS
  );

  for (let index = 0; index < points.length - 1; index++) {
    const distanceMeters = distanceToSegmentMeters(
      event.lat,
      event.lon,
      points[index],
      points[index + 1]
    );

    if (distanceMeters <= thresholdMeters) {
      return true;
    }
  }

  return false;
}

function distanceToSegmentMeters(lat, lon, start, end) {
  const eventPoint = toProjectedPoint(lat, lon, lat);
  const startPoint = toProjectedPoint(start.lat, start.lon, lat);
  const endPoint = toProjectedPoint(end.lat, end.lon, lat);

  const segmentX = endPoint.x - startPoint.x;
  const segmentY = endPoint.y - startPoint.y;
  const segmentLengthSquared = segmentX * segmentX + segmentY * segmentY;

  if (segmentLengthSquared === 0) {
    return distanceMeters(eventPoint, startPoint);
  }

  const projection = (
    (eventPoint.x - startPoint.x) * segmentX
    + (eventPoint.y - startPoint.y) * segmentY
  ) / segmentLengthSquared;
  const clampedProjection = Math.max(0, Math.min(1, projection));

  const closestPoint = {
    x: startPoint.x + clampedProjection * segmentX,
    y: startPoint.y + clampedProjection * segmentY
  };

  return distanceMeters(eventPoint, closestPoint);
}

function toProjectedPoint(lat, lon, referenceLat) {
  const referenceLatRadians = degreesToRadians(referenceLat);

  return {
    x: EARTH_RADIUS_METERS * degreesToRadians(lon) * Math.cos(referenceLatRadians),
    y: EARTH_RADIUS_METERS * degreesToRadians(lat)
  };
}

function distanceMeters(firstPoint, secondPoint) {
  const dx = firstPoint.x - secondPoint.x;
  const dy = firstPoint.y - secondPoint.y;

  return Math.sqrt(dx * dx + dy * dy);
}

function degreesToRadians(value) {
  return value * Math.PI / 180;
}
