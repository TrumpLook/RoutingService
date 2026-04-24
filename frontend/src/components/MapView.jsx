import L from "leaflet";
import { MapContainer, Marker, Polyline, Popup, TileLayer, useMapEvents } from "react-leaflet";

const blockedRoadIcon = L.divIcon({
  className: "blocked-road-marker",
  html: '<div class="blocked-road-dot"></div>',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
  popupAnchor: [0, -8]
});

function ClickHandler({ onMapClick }) {
  useMapEvents({
    click(event) {
      onMapClick(event.latlng);
    }
  });

  return null;
}

export default function MapView({ startPoint, endPoint, routeInfo, activeEvents, onMapClick }) {
  const dijkstraPositions = (routeInfo?.dijkstra?.points ?? []).map((point) => [point.lat, point.lon]);
  const aStarPositions = (routeInfo?.astar?.points ?? []).map((point) => [point.lat, point.lon]);

  return (
    <div className="map-shell">
      <MapContainer center={[55.751244, 37.618423]} zoom={13} className="map">
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickHandler onMapClick={onMapClick} />
        {startPoint ? <Marker position={[startPoint.lat, startPoint.lon]} /> : null}
        {endPoint ? <Marker position={[endPoint.lat, endPoint.lon]} /> : null}
        {activeEvents.map((event) => (
          <Marker
            key={`${event.eventId}-${event.nodeId}`}
            position={[event.lat, event.lon]}
            icon={blockedRoadIcon}
          >
            <Popup>
              <div className="event-popup">
                <strong>{event.eventType}</strong>
                <div>node: {event.nodeId}</div>
              </div>
            </Popup>
          </Marker>
        ))}
        {dijkstraPositions.length > 1 ? (
          <Polyline positions={dijkstraPositions} color="#de5b31" weight={6} opacity={0.95} />
        ) : null}
        {aStarPositions.length > 1 ? (
          <Polyline
            positions={aStarPositions}
            color="#2f7df6"
            weight={4}
            opacity={1}
            dashArray="10 10"
            lineCap="round"
          />
        ) : null}
      </MapContainer>
    </div>
  );
}
