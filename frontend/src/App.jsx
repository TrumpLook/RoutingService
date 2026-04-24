import { useEffect, useState } from "react";
import ControlPanel from "./components/ControlPanel";
import MapView from "./components/MapView";
import { buildRoute, getActiveEvents } from "./api";

export default function App() {
  const [weightType, setWeightType] = useState("TIME");
  const [startPoint, setStartPoint] = useState(null);
  const [endPoint, setEndPoint] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [activeEvents, setActiveEvents] = useState([]);

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

        const [dijkstra, astar] = await Promise.all([
          buildRoute({ ...requestPayload, algorithm: "DIJKSTRA" }),
          buildRoute({ ...requestPayload, algorithm: "ASTAR" })
        ]);

        if (!isCancelled) {
          setRouteInfo({ dijkstra, astar });
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
  }, [endPoint, startPoint, weightType]);

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

  function handleMapClick(latlng) {
    setError("");
    setRouteInfo(null);

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
      />
      <section className="canvas">
        <MapView
          startPoint={startPoint}
          endPoint={endPoint}
          routeInfo={routeInfo}
          activeEvents={activeEvents}
          onMapClick={handleMapClick}
        />
      </section>
    </main>
  );
}
