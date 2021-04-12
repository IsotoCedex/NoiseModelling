package org.noise_planet.noisemodelling.jdbc;

import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SpatialResultSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.noise_planet.noisemodelling.emission.EvaluateRailwaySourceCnossos;
import org.noise_planet.noisemodelling.emission.RailWayLW;
import org.noise_planet.noisemodelling.emission.RailwayTrackParametersCnossos;
import org.noise_planet.noisemodelling.emission.RailwayVehicleParametersCnossos;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.noise_planet.noisemodelling.jdbc.MakeParallelLines.MakeParallelLine;



public class RailWayLWIterator implements Iterator<RailWayLW> {

    private Connection connection;
    private RailWayLW railWayLW;
    private RailWayLW railWayLWsum;
    private RailWayLWGeom railWayLWfinal = new RailWayLWGeom();
    private String tableTrain;
    private String tableTrack;

    public int getNbTrack() {
        return nbTrack;
    }

    public void setNbTrack(int nbTrack) {
        this.nbTrack = nbTrack;
    }

    private int nbTrack = 1;
    private LDENConfig ldenConfig;
    private SpatialResultSet spatialResultSet;
    private int currentIdSection = -1;

    public Map<String, Integer> sourceFields = null;
    GeometryFactory geometryFactory = new GeometryFactory();



    public List<LineString> getRailWayLWGeometry( double distance) {
        List<LineString> geometries = new ArrayList<>();


        boolean even = false;
        if (nbTrack % 2 == 0) even = true;

        if (nbTrack == 1) {
            geometries.addAll(railWayLWfinal.getGeometry());
            return geometries;
        }else {

            if (even) {
                for (int j=1; j <= nbTrack/2 ; j++){
                    for (LineString subGeom : railWayLWfinal.getGeometry()) {
                        geometries.add( MakeParallelLine(subGeom, (distance / 2) + distance * j));
                        geometries.add(MakeParallelLine(subGeom, -((distance / 2) + distance * j)));
                    }
                }
            } else {
                for (int j=1; j <= ((nbTrack-1)/2) ; j++) {
                    for (LineString subGeom : railWayLWfinal.getGeometry()) {
                        geometries.add( MakeParallelLine(subGeom,  distance * j));
                        geometries.add(MakeParallelLine(subGeom, -( distance * j)));
                    }
                }
                LineMerger centerLine = new LineMerger();
                centerLine.add(railWayLWfinal.getGeometry());
                geometries.addAll(centerLine.getMergedLineStrings());
            }
            return geometries;
        }
    }

    public RailWayLW getRailWayLW() {
        return railWayLWfinal.getRailWayLW();
    }

    public int getRailWayLWPK() {
        return railWayLWfinal.getPK();
    }


    public RailWayLWIterator(Connection connection, String tableTrain, String tableTrack, LDENConfig ldenConfig) {
        this.connection = connection;
        this.tableTrain = tableTrain;
        this.tableTrack = tableTrack;
        this.ldenConfig = ldenConfig;
    }

    @Override
    public boolean hasNext() {
        return railWayLW != null;
    }

    @Override
    public RailWayLWGeom next() {
        try {
            if (spatialResultSet == null) {

                spatialResultSet = connection.createStatement().executeQuery("SELECT r1.*, r2.* FROM "+tableTrain+" r1, "+tableTrack+" r2 WHERE r1.IDSECTION= R2.IDSECTION; ").unwrap(SpatialResultSet.class);
                spatialResultSet.next();
                railWayLW = getRailwayEmissionFromResultSet(spatialResultSet, "DAY");
                railWayLWsum = railWayLW;
                currentIdSection = spatialResultSet.getInt("PK");
            }
            while (spatialResultSet.next()) {
                if (currentIdSection == spatialResultSet.getInt("PK")) {
                    railWayLWsum = RailWayLW.sumRailWayLW(railWayLWsum,railWayLW);
                } else {
                    railWayLWfinal.setRailWayLW(railWayLWsum);
                    Geometry inputgeometry = spatialResultSet.getGeometry();
                    List<LineString> inputLineStrings = new ArrayList<>();
                    for (int id = 0; id < inputgeometry.getNumGeometries(); id++) {
                        Geometry subGeom = inputgeometry.getGeometryN(id);
                        if (subGeom instanceof LineString) {
                            inputLineStrings.add((LineString) subGeom);
                        }
                    }
                    railWayLWfinal.setGeometry(inputLineStrings);
                    railWayLWfinal.setPK(spatialResultSet.getInt("PK"));
                    railWayLW = getRailwayEmissionFromResultSet(spatialResultSet, "DAY");
                    railWayLWsum = railWayLW;
                    currentIdSection = spatialResultSet.getInt("PK");
                    return railWayLWfinal;
                }

            }
        } catch (SQLException | IOException throwables) {
            throw new NoSuchElementException(throwables.getMessage());
        }
        return null;
    }

    /**
     * @param rs     result set of source
     * @param period D or E or N
     * @return Emission spectrum in dB
     */
    public RailWayLW getRailwayEmissionFromResultSet(ResultSet rs, String period) throws SQLException, IOException {

        if (sourceFields == null) {
            sourceFields = new HashMap<>();
            int fieldId = 1;
            for (String fieldName : JDBCUtilities.getFieldNames(rs.getMetaData())) {
                sourceFields.put(fieldName.toUpperCase(), fieldId++);
            }
        }
        double[] lvl = new double[ldenConfig.propagationProcessPathData.freq_lvl.size()];

        String typeTrain = "FRET";
        double vehicleSpeed = 160;
        int vehiclePerHour = 1;
        int rollingCondition = 0;
        double idlingTime = 0;
        int trackTransfer = 4;
        int impactNoise = 0;
        int bridgeTransfert = 0;
        int curvature = 0;
        int railRoughness = 4;
        double vMaxInfra = 160;
        double vehicleCommercial = 160;
        boolean isTunnel = false;

        // Read fields
        if (sourceFields.containsKey("SPEEDVEHIC")) {
            vehicleSpeed = rs.getDouble(sourceFields.get("SPEEDVEHIC"));
        }
        if (sourceFields.containsKey("T" + period)) {
            vehiclePerHour = rs.getInt(sourceFields.get("T" + period));
        }
        if (sourceFields.containsKey("ROLLINGCONDITION")) {
            rollingCondition = rs.getInt(sourceFields.get("ROLLINGCONDITION"));
        }
        if (sourceFields.containsKey("IDLINGTIME")) {
            idlingTime = rs.getDouble(sourceFields.get("IDLINGTIME"));
        }
        if (sourceFields.containsKey("TRACKTRANS")) {
            trackTransfer = rs.getInt(sourceFields.get("TRACKTRANS"));
        }
        if (sourceFields.containsKey("RAILROUGHN")) {
            railRoughness = rs.getInt(sourceFields.get("RAILROUGHN"));
        }

        if (sourceFields.containsKey("IMPACTNOIS")) {
            impactNoise = rs.getInt(sourceFields.get("IMPACTNOIS"));
        }
        if (sourceFields.containsKey("BRIDGETRAN")) {
            bridgeTransfert = rs.getInt(sourceFields.get("BRIDGETRAN"));
        }
        if (sourceFields.containsKey("CURVATURE")) {
            curvature = rs.getInt(sourceFields.get("CURVATURE"));
        }

        if (sourceFields.containsKey("SPEEDTRACK")) {
            vMaxInfra = rs.getDouble(sourceFields.get("SPEEDTRACK"));
        }
        if (sourceFields.containsKey("SPEEDCOMME")) {
            vehicleCommercial = rs.getDouble(sourceFields.get("SPEEDCOMME"));
        }
        if (sourceFields.containsKey("TYPETRAIN")) {
            typeTrain = rs.getString(sourceFields.get("TYPETRAIN"));
        }

        if (sourceFields.containsKey("ISTUNNEL")) {
            isTunnel = rs.getBoolean(sourceFields.get("ISTUNNEL"));
        }

        if (sourceFields.containsKey("NTRACK")) {
            nbTrack = rs.getInt(sourceFields.get("NTRACK"));
            this.nbTrack = nbTrack;
        }


        EvaluateRailwaySourceCnossos evaluateRailwaySourceCnossos = new EvaluateRailwaySourceCnossos();

        RailWayLW  lWRailWay = new RailWayLW();

        RailwayTrackParametersCnossos trackParameters = new RailwayTrackParametersCnossos(vMaxInfra, trackTransfer, railRoughness,
                impactNoise, bridgeTransfert, curvature, vehicleCommercial, isTunnel, nbTrack);

        Map<String, Integer> vehicles = evaluateRailwaySourceCnossos.getVehicleFromTrain(typeTrain);

        if (vehicles!=null){
            int i = 0;
            for (Map.Entry<String,Integer> entry : vehicles.entrySet()){
                typeTrain = entry.getKey();
                vehiclePerHour = vehiclePerHour * entry.getValue();
                RailwayVehicleParametersCnossos vehicleParameters = new RailwayVehicleParametersCnossos(typeTrain, vehicleSpeed,
                        vehiclePerHour/nbTrack, rollingCondition, idlingTime);

                if (i==0){
                    lWRailWay = evaluateRailwaySourceCnossos.evaluate(vehicleParameters, trackParameters);
                }
                else {
                    lWRailWay = RailWayLW.sumRailWayLW(lWRailWay, evaluateRailwaySourceCnossos.evaluate(vehicleParameters, trackParameters));
                }
                i++;
            }

        }else if (evaluateRailwaySourceCnossos.isInVehicleList(typeTrain)){
            RailwayVehicleParametersCnossos vehicleParameters = new RailwayVehicleParametersCnossos(typeTrain, vehicleSpeed,
                    vehiclePerHour/nbTrack, rollingCondition, idlingTime);
             lWRailWay = evaluateRailwaySourceCnossos.evaluate(vehicleParameters, trackParameters);
        }

        return lWRailWay;
    }
}


class RailWayLWGeom extends RailWayLW {
    private RailWayLW railWayLW;
    private List<LineString> geometry;
    private int pk;

    public RailWayLW getRailWayLW() {
        return railWayLW;
    }

    public void setRailWayLW(RailWayLW railWayLW) {
        this.railWayLW = railWayLW;
    }

    public List<LineString> getGeometry() {
        return  geometry;
    }


    public int getPK() {
        return pk;
    }

    public int setPK(int pk) {
        return this.pk;
    }

    public void setGeometry(List<LineString> geometry) {
        this.geometry = geometry;
    }
}
