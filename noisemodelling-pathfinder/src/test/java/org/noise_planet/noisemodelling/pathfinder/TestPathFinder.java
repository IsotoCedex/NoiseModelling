/**
 * NoiseModelling is a library capable of producing noise maps. It can be freely used either for research and education, as well as by experts in a professional use.
 * <p>
 * NoiseModelling is distributed under GPL 3 license. You can read a copy of this License in the file LICENCE provided with this software.
 * <p>
 * Official webpage : http://noise-planet.org/noisemodelling.html
 * Contact: contact@noise-planet.org
 */

package org.noise_planet.noisemodelling.pathfinder;


import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.noise_planet.noisemodelling.pathfinder.cnossos.CnossosPath;
import org.noise_planet.noisemodelling.pathfinder.path.Scene;
import org.noise_planet.noisemodelling.pathfinder.profilebuilder.CutProfile;
import org.noise_planet.noisemodelling.pathfinder.profilebuilder.ProfileBuilder;
import org.noise_planet.noisemodelling.pathfinder.utils.geometry.JTSUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.noise_planet.noisemodelling.pathfinder.PathFinder.splitLineStringIntoPoints;


public class TestPathFinder {
    //private static final Logger LOGGER = LoggerFactory.getLogger(TestComputeCnossosRays.class);
    //private static final double EPSILON = 1e0;


    @Test
    public void testMeanPlane() {
        Coordinate sGround = new Coordinate(10, 10, 0);
        Coordinate rGround = new Coordinate(200, 50, 10);
        LineSegment segBottom = new LineSegment(new Coordinate(120, -20, 0),
                new Coordinate(120, 80, 0));
        LineSegment segTop = new LineSegment(new Coordinate(185, -5, 10),
                new Coordinate(185, 75, 10));
        LineSegment SgroundRGround = new LineSegment(sGround,
                rGround);

        Coordinate O1 = segBottom.lineIntersection(SgroundRGround);
        O1.z = segBottom.p0.z;
        Coordinate O2 = segTop.lineIntersection(SgroundRGround);
        O2.z = segTop.p0.z;
        List<Coordinate> uv = new ArrayList<>();
        uv.add(new Coordinate(sGround.distance(sGround), sGround.z));
        uv.add(new Coordinate(sGround.distance(O1), O1.z));
        uv.add(new Coordinate(sGround.distance(O2), O2.z));
        uv.add(new Coordinate(sGround.distance(rGround), rGround.z));

        double[] ab = JTSUtility.getMeanPlaneCoefficients(uv.toArray(new Coordinate[uv.size()]));
        double slope = ab[0];
        double intercept = ab[1];

        assertEquals(0.05, slope, 0.01);
        assertEquals(-2.83, intercept, 0.01);

        uv = new ArrayList<>();
        uv.add(new Coordinate(sGround.distance(sGround), sGround.z));
        uv.add(new Coordinate(sGround.distance(O1), O1.z));
        uv.add(new Coordinate(sGround.distance(O2), O2.z));

        ab = JTSUtility.getMeanPlaneCoefficients(uv.toArray(new Coordinate[uv.size()]));
        slope = ab[0];
        intercept = ab[1];
        assertEquals(0.05, slope, 0.01);
        assertEquals(-2.33, intercept, 0.01);
    }

    /**
     * Test vertical edge diffraction ray computation
     *
     * @throws ParseException
     */
    @Test
    public void TestcomputeVerticalEdgeDiffraction() throws ParseException {
        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();
        profileBuilder.addBuilding(wktReader.read("POLYGON((5 6, 6 5, 7 5, 7 8, 6 8, 5 7, 5 6))"), 4, -1);
        profileBuilder.addBuilding(wktReader.read("POLYGON((9 7, 11 7, 11 11, 9 11, 9 7))"), 4, -1);
        profileBuilder.addBuilding(wktReader.read("POLYGON((12 8, 13 8, 13 10, 12 10, 12 8))"), 4, -1);
        profileBuilder.addBuilding(wktReader.read("POLYGON((10 4, 11 4, 11 6, 10 6, 10 4))"), 4, -1);
        profileBuilder.finishFeeding();

        PathFinder computeRays = new PathFinder(new Scene(profileBuilder));
        Coordinate p1 = new Coordinate(2, 6.5, 1.6);
        Coordinate p2 = new Coordinate(14, 6.5, 1.6);

        List<Coordinate> ray = computeRays.computeSideHull(true, p1, p2, profileBuilder);
        int i = 0;
        assertEquals(0, p1.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(9, 11).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(11, 11).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(13, 10).distance(ray.get(i++)), 0.02);
        assertEquals(0, p2.distance(ray.get(i)), 0.02);

        ray = computeRays.computeSideHull(false, p1, p2, profileBuilder);
        i = 0;
        assertEquals(0, p1.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(6, 5).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(10, 4).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(11, 4).distance(ray.get(i++)), 0.02);
        assertEquals(0, p2.distance(ray.get(i)), 0.02);

        ray = computeRays.computeSideHull(false, p2, p1, profileBuilder);
        i = 0;
        assertEquals(0, p2.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(13, 10).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(11, 11).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(9, 11).distance(ray.get(i++)), 0.02);
        assertEquals(0, p1.distance(ray.get(i)), 0.02);

        ray = computeRays.computeSideHull(true, p2, p1, profileBuilder);
        i = 0;
        assertEquals(0, p2.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(11, 4).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(10, 4).distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(6, 5).distance(ray.get(i++)), 0.02);
        assertEquals(0, p1.distance(ray.get(i)), 0.02);
    }

    @Test
    public void TestSplitLineSourceIntoPoints() {
        GeometryFactory factory = new GeometryFactory();
        List<Coordinate> sourcePoints = new ArrayList<>();
        // source line is split in 3 parts of 2.5 meters
        // This is because minimal receiver-source distance is equal to 5 meters
        // The constrain is distance / 2.0 so 2.5 meters
        // The source length is equals to 5 meters
        // It can be equally split in 2 segments of 2.5 meters each, for each segment the nearest point is retained
        LineString geom = factory.createLineString(new Coordinate[]{new Coordinate(1,2,0),
                new Coordinate(4,2,0), new Coordinate(4, 0, 0)});
        Coordinate receiverCoord = new Coordinate(-4, 2, 0);
        Coordinate nearestPoint = JTSUtility.getNearestPoint(receiverCoord, geom);
        double segmentSizeConstraint = Math.max(1, receiverCoord.distance3D(nearestPoint) / 2.0);
        assertEquals(2.5, splitLineStringIntoPoints(geom , segmentSizeConstraint, sourcePoints), 1e-6);
        assertEquals(2, sourcePoints.size());
        assertEquals(0, new Coordinate(2.25, 2, 0).distance3D(sourcePoints.get(0)), 1e-6);
        assertEquals(0, new Coordinate(4, 1.25, 0).distance3D(sourcePoints.get(1)), 1e-6);
    }

    @Test
    public void TestSplitRegression() throws ParseException {
        LineString geom = (LineString)new WKTReader().read("LINESTRING (26.3 175.5 0.0000034909259558, 111.9 90.9 0, 123 -70.9 0, 345.2 -137.8 0)");
        double constraint = 82.98581729762442;
        List<Coordinate> pts = new ArrayList<>();
        splitLineStringIntoPoints(geom, constraint, pts);
        for(Coordinate pt : pts) {
            assertNotNull(pt);
        }
        assertEquals(7, pts.size());
    }

    /**
     * Test vertical edge diffraction ray computation
     *
     * @throws ParseException
     */
    @Test
    public void TestComputeHorizontalEdgeDiffraction() throws ParseException {
        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);
        //Scene dimension
        Envelope cellEnvelope = new Envelope(new Coordinate(316705, 6706347, 0.), new Coordinate(316828, 6706469, 0.));
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316759.81 6706397.4101739, 316759.81 6706403.99033324, 316765.7002829955 6706404.19000385, 316778.88539775345 6706404.489665548, 316777.49275745824 6706400.809116197, 316765.598667453 6706399.209910818, 316765.5966749492 6706399.209431015, 316765.594822107 6706399.208555082, 316765.59318675095 6706399.207319812, 316765.5918375706 6706399.205777088, 316765.59083123534 6706399.203991711, 316765.59021001414 6706399.202038671, 316765.58999999997 6706399.2, 316765.58999999997 6706397.509829072, 316759.81 6706397.4101739))"), 16.68046);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316755.91050631634 6706412.408966506, 316756.3094447798 6706419.689593465, 316765.78984906914 6706419.290418547, 316765.6900012205 6706412.900156232, 316765.69 6706412.9, 316765.69 6706412.20970156, 316762.3996971088 6706412.109995412, 316762.39766060974 6706412.109722513, 316762.3957228751 6706412.109039148, 316762.3939657122 6706412.107974169, 316762.39246330503 6706412.106572536, 316762.3912790822 6706412.104893423, 316762.3904630394 6706412.10300772, 316762.3900496281 6706412.100995037, 316762.1910047661 6706410.110546417, 316758.81 6706410.30942905, 316758.81 6706412.1, 316758.8097809892 6706412.102081406, 316758.80913354986 6706412.104071641, 316758.8080860413 6706412.10588353, 316758.8066843467 6706412.107437708, 316758.8049898632 6706412.108666099, 316758.8030768129 6706412.109514894, 316758.8010289915 6706412.109946918, 316755.91050631634 6706412.408966506))"), 16.73458);
        cellEnvelope.expandBy(200);
        profileBuilder.finishFeeding();

        CutProfile profile = profileBuilder.getProfile(
                new Coordinate(316876.05185368325, 6706318.789634008, 22.089050196052437),
                new Coordinate(316747.10402055364, 6706422.950335046, 12.808121783800553));
        CnossosPath propa = new PathFinder(new Scene(profileBuilder)).computeHEdgeDiffraction(profile, false);
        assertEquals(3, propa.getPointList().size());
    }

    /**
     * Regression test for hull points in intersection with buildings
     */
    //@Test
    /*public void TestComputeDiffractionRaysComplex() throws Exception {
        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);
        //Scene dimension
        Envelope cellEnvelope = new Envelope();
        Coordinate p1 = new Coordinate(316886.8727665055, 6703857.739385221, 8.581142709966494);
        Coordinate p2 = new Coordinate(316876.05185368325, 6703918.789634008, 13.667755518192145);
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();

        profileBuilder.addBuilding(wktReader.read("POLYGON ((316901.11218957935 6703895.907300464, 316907.20238788123 6703894.409710717, 316907.20420580526 6703894.409072553, 316907.2058676055 6703894.408097605, 316907.2073115943 6703894.406822067, 316907.20848416915 6703894.405293286, 316907.20934180304 6703894.403568014, 316907.20985265967 6703894.401710292, 316907.20999777544 6703894.399789084, 316907.20977176365 6703894.397875704, 316906.711831376 6703892.10734992, 316913.4929281489 6703890.412075727, 316914.09035236185 6703892.602631174, 316914.0910465184 6703892.604453669, 316914.09208120627 6703892.606106775, 316914.0934170728 6703892.60752762, 316914.09500331036 6703892.608662164, 316914.0967795891 6703892.609467257, 316914.098678351 6703892.609912277, 316914.1006273796 6703892.6099803, 316914.10255254694 6703892.609668738, 316926.5927824364 6703889.312248047, 316928.7878607862 6703897.992785159, 316916.29752967303 6703901.190309923, 316903.30721426976 6703904.487851526, 316901.11218957935 6703895.907300464))"), 11.915885805791621);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316886.41232341167 6703903.607226911, 316897.992788823 6703900.7121105585, 316899.7878130121 6703907.692760183, 316888.3070780876 6703910.587902033, 316886.41232341167 6703903.607226911))"), 13.143551238469575);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316888.1122565511 6703867.407367232, 316896.10231072427 6703865.509729366, 316896.1023971443 6703865.509708434, 316904.19264749985 6703863.512115754, 316905.7879625209 6703870.092790215, 316897.6974903785 6703872.190320031, 316897.69565429183 6703872.190993624, 316897.6939844259 6703872.192011705, 316897.69254465215 6703872.193335333, 316897.6913900411 6703872.194913881, 316897.6905647558 6703872.19668697, 316897.6901003631 6703872.198586781, 316897.6900146256 6703872.200540646, 316897.6903108229 6703872.202473832, 316898.88782459043 6703876.8927360885, 316896.5070718511 6703877.487924273, 316895.9096476382 6703875.297368825, 316895.9089607805 6703875.295561034, 316895.9079388003 6703875.293919256, 316895.9066199183 6703875.292504889, 316895.9050534592 6703875.291370831, 316895.9032980068 6703875.290559494, 316895.9014192128 6703875.29010122, 316895.8994873419 6703875.290013149, 316895.8975746438 6703875.290298575, 316892.70774903445 6703876.087754977, 316892.20980580675 6703873.598038838, 316892.2091870762 6703873.596050616, 316892.2081700073 6703873.594233633, 316892.2067986987 6703873.592666672, 316892.20513260836 6703873.591417673, 316892.2032439755 6703873.590540791, 316892.20121468866 6703873.590074047, 316892.1991327346 6703873.590037678, 316892.19708838384 6703873.59043326, 316889.9070482306 6703874.287402003, 316888.1122565511 6703867.407367232))"), 12.447629178041673);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316874.71224225214 6703907.007098008, 316884.49264060217 6703904.4122984465, 316885.88800615596 6703910.292767566, 316882.7975012198 6703911.090317226, 316882.79569543176 6703911.090973888, 316882.79404857155 6703911.091963801, 316882.7926214425 6703911.093250415, 316882.7914667354 6703911.09478623, 316882.7906270828 6703911.096514541, 316882.79013348534 6703911.098371537, 316882.79000416707 6703911.100288658, 316882.7902439024 6703911.102195121, 316883.68809233804 6703915.092632613, 316877.207229554 6703916.687921914, 316875.10968736705 6703908.497519089, 316875.1096623494 6703908.497423373, 316874.71224225214 6703907.007098008))"), 13.675755295119075);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316909.9120555793 6703863.50729387, 316912.69263484754 6703862.8121490525, 316915.7878816859 6703875.692369767, 316913.0074268087 6703876.288181527, 316909.9120555793 6703863.50729387))"), 10.563372307530404);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316864.71217490366 6703906.807692843, 316867.59278650914 6703906.2117042355, 316868.69034157164 6703910.302591286, 316868.691015592 6703910.304390946, 316868.6920214139 6703910.306028446, 316868.69332189154 6703910.3074433105, 316868.69486899726 6703910.308583287, 316868.6966055951 6703910.309406275, 316868.6984675512 6703910.309881882, 316868.70038610185 6703910.309992543, 316868.70229039335 6703910.309734171, 316872.1022903933 6703909.509734171, 316872.1040185817 6703909.50915702, 316872.10561336356 6703909.508275878, 316872.1070217964 6703909.507119998, 316872.1081971238 6703909.505727754, 316872.10910032806 6703909.504145362, 316872.10970142495 6703909.502425356, 316872.4082232103 6703908.308338215, 316874.09833733283 6703907.910664304, 316875.09133194154 6703908.506461069, 316877.18779957696 6703916.692668026, 316870.6072353747 6703918.287956317, 316869.60968663864 6703914.397516247, 316869.6090061109 6703914.395653741, 316869.607971459 6703914.393962133, 316869.6066233658 6703914.392507936, 316869.6050148389 6703914.39134833, 316869.60320912633 6703914.390528913, 316869.60127722955 6703914.390081901, 316869.59929511155 6703914.390024874, 316869.59734071 6703914.390360074, 316866.70739797025 6703915.187585657, 316865.80974391196 6703911.297751404, 316865.8097139901 6703911.297625469, 316864.71217490366 6703906.807692843))"), 13.227635299718834);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316878.5120586163 6703875.207141824, 316886.39273419575 6703873.112278695, 316888.5878448322 6703881.992498998, 316883.2977917435 6703883.190246867, 316883.29591523844 6703883.190872311, 316883.29419854766 6703883.19185487, 316883.2927088358 6703883.193156104, 316883.29150438716 6703883.194725101, 316883.2906323251 6703883.196500476, 316883.29012676864 6703883.198412769, 316883.2900074975 6703883.200387161, 316883.2902791779 6703883.202346406, 316883.9902791779 6703886.102346405, 316883.99093748967 6703886.104227399, 316883.99195572 6703886.105940501, 316883.99329342984 6703886.107417676, 316883.99489749194 6703886.108600256, 316883.99670420075 6703886.109441277, 316883.9986418026 6703886.109907336, 316884.0006333454 6703886.109979923, 316884.00259973475 6703886.109656157, 316886.59276802506 6703885.412303156, 316887.1878184773 6703887.792504964, 316881.90736221685 6703888.988079967, 316878.5120586163 6703875.207141824))"), 12.286729118682786);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316882.81188369356 6703911.1072608605, 316885.89284142124 6703910.312174995, 316887.08777371957 6703914.892748807, 316883.90655846545 6703915.68805262, 316883.70964576973 6703915.097314532, 316882.81188369356 6703911.1072608605))"), 9.484434135999829);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316881.91189041897 6703889.007560945, 316887.19214319426 6703887.812032015, 316887.78838154906 6703890.892596849, 316882.60756777594 6703892.187800292, 316881.91189041897 6703889.007560945))"), 10.568596285589448);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316882.61290170497 6703892.207082339, 316887.79261431424 6703890.9121541865, 316888.2880030182 6703892.992786742, 316883.30653076805 6703894.287969527, 316882.61290170497 6703892.207082339))"), 9.10452432502548);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316905.8121263122 6703864.407576221, 316909.8925868034 6703863.511865381, 316911.4878736878 6703870.092423779, 316907.40741319663 6703870.988134619, 316905.8121263122 6703864.407576221))"), 11.07579981877306);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316856.5121665223 6703915.707105044, 316858.00720681547 6703921.487927511, 316864.4879847223 6703919.8926591035, 316863.5902691971 6703916.1023046635, 316863.5900090117 6703916.100424443, 316863.59010879387 6703916.098528932, 316863.5905649486 6703916.096686421, 316863.591361041 6703916.094963295, 316863.5924683885 6703916.093421639, 316863.5938470941 6703916.092116996, 316863.59544748423 6703916.091096371, 316863.597211898 6703916.090396537, 316866.6881294962 6703915.193033365, 316865.79253813525 6703911.312137467, 316858.91274731315 6703913.007158394, 316859.5095358266 6703914.896988687, 316859.50994610385 6703914.89896317, 316859.50995188195 6703914.90097982, 316859.50955292606 6703914.902956621, 316859.50876546133 6703914.904813179, 316859.5076215132 6703914.90647399, 316859.5061676051 6703914.907871509, 316859.5044628661 6703914.908948901, 316859.5025766265 6703914.90966235, 316856.5121665223 6703915.707105044))"), 13.50627531988858);

        cellEnvelope.expandToInclude(p1);
        cellEnvelope.expandToInclude(p2);
        cellEnvelope.expandBy(100);

        profileBuilder.finishFeeding();

        PathParameters processData = new PathParameters(profileBuilder);
        //new ArrayList<>(), manager, sourcesIndex, srclst, new ArrayList<>(), new ArrayList<>(), 0, 99, 1000,1000,0,0,new double[0],0,0,new EmptyProgressVisitor(), new ArrayList<>(), true
        CnossosPaths computeRays = new CnossosPaths(processData);

        Assert.assertFalse(computeRays.computeFreeField(profileBuilder.getProfile(p1, p2), processData, false).getSegmentList().isEmpty());

        List<Coordinate> pts = computeRays.computeSideHull(true, p1, p2, profileBuilder);
        assertEquals(5, pts.size());
        for (int i = 0; i < pts.size() - 1; i++) {
            Assert.assertTrue(computeRays.computeFreeField(profileBuilder.getProfile(pts.get(i), pts.get(i + 1)), processData, false).getSegmentList().isEmpty());
        }

        pts = computeRays.computeSideHull(false, p1, p2, profileBuilder);
        assertEquals(5, pts.size());
        for (int i = 0; i < pts.size() - 1; i++) {
            Assert.assertTrue(computeRays.computeFreeField(profileBuilder.getProfile(pts.get(i), pts.get(i + 1)), processData, false).getSegmentList().isEmpty());
        }

        PathParameters data = new PathParameters(profileBuilder);
        data.setComputeHorizontalDiffraction(true);
        data.setComputeVerticalDiffraction(true);
        List<PropagationPath> prop = computeRays.directPath(p2, -1, null, p1, -1,
        data.isComputeHEdgeDiffraction(), data.isComputeVEdgeDiffraction(), false);
        // 3 paths
        // 1 over the building
        assertEquals(3, prop.size());
    }*/

   /* @Test
    public void testPropagationPathSerialization() throws IOException {
        List<PropagationPath> expected = new ArrayList<>();
        expected.add(new PropagationPath(true,
                Arrays.asList(new PointPath(
                        new Coordinate(1,2,3), 15.0, Collections.nCopies(8, 0.23), 8,
                        PointPath.POINT_TYPE.RECV)),
                Arrays.asList(new SegmentPath(0.15,
                        new org.locationtech.jts.math.Vector3D(1,1,1),
                        new Coordinate(1.5,2.5,3.5))),
                Arrays.asList(new SegmentPath(0.35,
                        new org.locationtech.jts.math.Vector3D(2,2,3),
                        new Coordinate(4.5,5.5,8.5)), new SegmentPath(0.15,
                        new org.locationtech.jts.math.Vector3D(1,1,1),
                        new Coordinate(1.5,2.5,3.5)))));
        expected.add(new PropagationPath(true,
                Arrays.asList(new PointPath(
                        new Coordinate(2,7,1), 1.0, Collections.nCopies(8,0.4), 1,
                        PointPath.POINT_TYPE.DIFV)),
                Arrays.asList(new SegmentPath(0.115,
                        new org.locationtech.jts.math.Vector3D(11,13,14),
                        new Coordinate(1.5,21.5,13.5))),
                null));
        expected.get(0).setIdReceiver(5) ;
        expected.get(0).setIdSource(10);
        expected.get(0).setIdReceiver(6) ;
        expected.get(0).setIdSource(18);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PropagationPath.writePropagationPathListStream(new DataOutputStream(byteArrayOutputStream), expected);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ArrayList<PropagationPath> got = new ArrayList<>();
        PropagationPath.readPropagationPathListStream(new DataInputStream(byteArrayInputStream), got);

        assertEquals(2, got.size());
        assertEquals(expected.get(0).getPointList().size(), got.get(0).getPointList().size());
        assertEquals(expected.get(0).getPointList().get(0).coordinate, got.get(0).getPointList().get(0).coordinate);
        assertEquals(1, expected.get(1).getPointList().size());
        assertEquals(PointPath.POINT_TYPE.DIFV, expected.get(1).getPointList().get(0).type);
        assertNotNull(expected.get(1).getSRSegment());
        assertEquals(expected.get(0).getIdReceiver(), got.get(0).getIdReceiver());
        assertEquals(expected.get(0).getIdSource(), got.get(0).getIdSource());
        assertEquals(expected.get(1).getIdReceiver(), got.get(1).getIdReceiver());
        assertEquals(expected.get(1).getIdSource(), got.get(1).getIdSource());
    }
*/
/*
    @Test
    public void testPropagationPathSerialization2() throws ParseException, IOException  {

        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);

        Coordinate p1 = new Coordinate(316914.1, 6703907.5, 4);
        Coordinate p2 = new Coordinate(316913.4, 6703879, 4);
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();

        profileBuilder.addBuilding(wktReader.read("POLYGON ((316925.36 6703889.64, 316914.1 6703892.61, 316914.09 6703892.61, 316914.09 6703892.6, 316913.49 6703890.41, 316906.71 6703892.11, 316907.21 6703894.4, 316907.21 6703894.41, 316907.2 6703894.41, 316901.11 6703895.91, 316903.31 6703904.49, 316916.3 6703901.19, 316925.36 6703898.87, 316925.36 6703889.64)) "), 11.915885805791621);
        profileBuilder.addBuilding(wktReader.read("POLYGON ((316886.41 6703903.61, 316888.31 6703910.59, 316899.79 6703907.69, 316897.99 6703900.71, 316886.41 6703903.61))"), 13.143551238469575);

        profileBuilder.finishFeeding();
        

        PathParameters processData = new PathParameters(profileBuilder);

        processData.addReceiver(p1);
        processData.addSource(factory.createPoint(p2));
        CnossosPaths computeRays = new CnossosPaths(processData);

        computeRays.setThreadCount(1);

        CnossosPaths computeRaysOut = new CnossosPaths(true, processData);

        computeRays.run(computeRaysOut);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PropagationPath.writePropagationPathListStream(new DataOutputStream(byteArrayOutputStream), computeRaysOut.propagationPaths);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ArrayList<PropagationPath> got = new ArrayList<>();
        PropagationPath.readPropagationPathListStream(new DataInputStream(byteArrayInputStream), got);

        Assert.assertEquals(computeRaysOut.propagationPaths.size(), got.size());
    }
*/
    /**
     * Test vertical edge diffraction ray computation
     *
     */
    @Test
    public void TestcomputeVerticalEdgeDiffractionRayOverBuilding() throws ParseException {
        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);
        //Scene dimension
        //Envelope cellEnvelope = new Envelope(new Coordinate(0, 0, 0.), new Coordinate(20, 15, 0.));
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();
        profileBuilder.addBuilding(wktReader.read("POLYGON((5 5, 7 5, 7 6, 8 6, 8 8, 5 8, 5 5))"), 4.3);
        profileBuilder.addBuilding(wktReader.read("POLYGON((9 7, 10 7, 10 9, 9 9, 9 7))"), 4.3);
        profileBuilder.finishFeeding();

        Scene processData = new Scene(profileBuilder);
        PathFinder computeRays = new PathFinder(processData);
        Coordinate p1 = new Coordinate(4, 3, 3);
        Coordinate p2 = new Coordinate(13, 10, 6.7);

        // Check the computation of convex corners of a building
        List<Coordinate> b1OffsetRoof = profileBuilder.getWideAnglePointsByBuilding(1, Math.PI * (1 + 1 / 16.0), Math.PI * (2 - (1 / 16.)));
        int i = 0;
        assertEquals(0, new Coordinate(5, 5).distance(b1OffsetRoof.get(i++)), 2 * ProfileBuilder.wideAngleTranslationEpsilon);
        assertEquals(0, new Coordinate(7, 5).distance(b1OffsetRoof.get(i++)), 2 * ProfileBuilder.wideAngleTranslationEpsilon);
        assertEquals(0, new Coordinate(8, 6).distance(b1OffsetRoof.get(i++)), 2 * ProfileBuilder.wideAngleTranslationEpsilon);
        assertEquals(0, new Coordinate(8, 8).distance(b1OffsetRoof.get(i++)), 2 * ProfileBuilder.wideAngleTranslationEpsilon);
        assertEquals(0, new Coordinate(5, 8).distance(b1OffsetRoof.get(i++)), 2 * ProfileBuilder.wideAngleTranslationEpsilon);
        assertEquals(0, new Coordinate(5, 5).distance(b1OffsetRoof.get(i++)), 2 * ProfileBuilder.wideAngleTranslationEpsilon);


        List<Coordinate> ray = computeRays.computeSideHull(true, p1, p2, profileBuilder);
        i = 0;
        assertEquals(0, p1.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(5, 8).distance(ray.get(i++)), 0.02);
        assertEquals(0, p2.distance(ray.get(i++)), 0.02);


        ray = computeRays.computeSideHull(false, p1, p2, profileBuilder);
        i = 0;
        assertEquals(0, p1.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(7, 5).distance(ray.get(i++)), 0.02);
        assertEquals(0, p2.distance(ray.get(i++)), 0.02);


        ray = computeRays.computeSideHull(false, p2, p1, profileBuilder);
        i = 0;
        assertEquals(0, p2.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(5, 8).distance(ray.get(i++)), 0.02);
        assertEquals(0, p1.distance(ray.get(i++)), 0.02);

        ray = computeRays.computeSideHull(true, p2, p1, profileBuilder);
        i = 0;
        assertEquals(0, p2.distance(ray.get(i++)), 0.02);
        assertEquals(0, new Coordinate(7, 5).distance(ray.get(i++)), 0.02);
        assertEquals(0, p1.distance(ray.get(i++)), 0.02);
    }

    /**
     * Test vertical edge diffraction ray computation with receiver in concave building
     * This configuration is not supported currently, so it must return no rays.
     *
     * @throws ParseException
     */
    @Test
    public void TestConcaveVerticalEdgeDiffraction() throws ParseException {
        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);
        //Scene dimension
        //Envelope cellEnvelope = new Envelope(new Coordinate(0, 0, 0.), new Coordinate(20, 15, 0.));
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();
        profileBuilder.addBuilding(wktReader.read("POLYGON((5 6, 4 5, 7 5, 7 8, 4 8, 5 7, 5 6))"), 4);
        profileBuilder.addBuilding(wktReader.read("POLYGON((9 7, 11 7, 11 11, 9 11, 9 7))"), 4);
        profileBuilder.addBuilding(wktReader.read("POLYGON((12 8, 13 8, 13 10, 12 10, 12 8))"), 4);
        profileBuilder.addBuilding(wktReader.read("POLYGON((10 4, 11 4, 11 6, 10 6, 10 4))"), 4);
        profileBuilder.finishFeeding();

        Scene processData = new Scene(profileBuilder);
        PathFinder computeRays = new PathFinder(processData);
        Coordinate p1 = new Coordinate(4.5, 6.5, 1.6);
        Coordinate p2 = new Coordinate(14, 6.5, 1.6);

        List<Coordinate> ray = computeRays.computeSideHull(true, p1, p2, profileBuilder);
        Assert.assertTrue(ray.isEmpty());
        ray = computeRays.computeSideHull(false, p1, p2, profileBuilder);
        Assert.assertTrue(ray.isEmpty());
        ray = computeRays.computeSideHull(false, p2, p1, profileBuilder);
        Assert.assertTrue(ray.isEmpty());
        ray = computeRays.computeSideHull(true, p2, p1, profileBuilder);
        Assert.assertTrue(ray.isEmpty());
    }

    /**
     * Test TC05 -- Reduced receiver height to include diffraction in some frequency bands
     */
    //@Test
    public void TC05()  throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create a profile builder object
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add topographic points
        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -20, 80)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -20, 80)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -20, 80)), 0.2);

        profileBuilder.finishFeeding();

        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);
    }

    /**
     * Test TC06 -- Reduced receiver height to include diffraction in some frequency bands
     * This test
     */
    //@Test
    public void TC06()  throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add topographic points
        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -20, 80)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -20, 80)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -20, 80)), 0.2);

        profileBuilder.finishFeeding();

        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 11.5));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * Test TC07 -- Flat ground with spatially varying acoustic properties and long barrier
     */
    //@Test
    public void TC07()  throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(100, 240, 0),
                new Coordinate(100.1, 240, 0),
                new Coordinate(265.1, -180, 0),
                new Coordinate(265, -180, 0),
                new Coordinate(100, 240, 0)}), 6, -1);

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -250, 250)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -250, 250)), 0.2);

        profileBuilder.finishFeeding();

        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 4));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * Test TC08 -- Flat ground with spatially varying acoustic properties and short barrier
     */
    //@Test
    public void TC08()  throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(175, 50, 0),
                new Coordinate(175.01, 50, 0),
                new Coordinate(190.01, 10, 0),
                new Coordinate(190, 10, 0),
                new Coordinate(175, 50, 0)}), 6, -1);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -250, 250)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -250, 250)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 4));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * Test TC09 -- Ground with spatially varying heights and and acoustic properties and short barrier
     */
    //@Test
    public void TC09()  throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(175, 50, 0),
                new Coordinate(175.01, 50, 0),
                new Coordinate(190.01, 10, 0),
                new Coordinate(190, 10, 0),
                new Coordinate(175, 50, 0)}), 6, -1);

        // Add topographic points
        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -250, 250)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -250, 250)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        rayData.setGs(0.9);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }




    /**
     * Test TC10 -- Flat ground with homogeneous acoustic properties and cubic building – receiver
     * at low height
     */
    //@Test
    public void TC10()  throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(55, 5, 0),
                new Coordinate(65, 5, 0),
                new Coordinate(65, 15, 0),
                new Coordinate(55, 15, 0),
                new Coordinate(55, 5, 0)}), 10, -1);

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -250, 250)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -250, 250)), 0.2);

        profileBuilder.finishFeeding();

        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(70, 10, 4));
        rayData.addSource(factory.createPoint(new Coordinate(50, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true, null);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }
    /**
     * Test TC11 -- Flat ground with homogeneous acoustic properties and cubic building – receiver
     * at large height
     */
    //@Test
    public void TC11() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(55, 5,0),
                new Coordinate(65, 5,0),
                new Coordinate(65, 15,0),
                new Coordinate(55, 15,0),
                new Coordinate(55, 5,0)}), 10, -1);

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -250, 250)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -250, 250)), 0.2);

        profileBuilder.finishFeeding();

        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(70, 10, 15));
        rayData.addSource(factory.createPoint(new Coordinate(50, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * Test TC12 -- Flat ground with homogeneous acoustic properties and polygonal building –
     * receiver at low height
     */
    //@Test
    public void TC12() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(11., 15.5, 0),
                new Coordinate(12., 13, 0),
                new Coordinate(14.5, 12, 0),
                new Coordinate(17.0, 13, 0),
                new Coordinate(18.0, 15.5, 0),
                new Coordinate(17.0, 18, 0),
                new Coordinate(14.5, 19, 0),
                new Coordinate(12.0, 18, 0),
                new Coordinate(11, 15.5, 0)}), 10, -1);
        
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.5);

        profileBuilder.finishFeeding();

        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(30, 20, 6));
        rayData.addSource(factory.createPoint(new Coordinate(0, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);


    }

    /**
     * Test TC13 -- Ground with spatially varying heights and acoustic properties and polygonal
     * building
     */
    //@Test
    public void TC13() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(169.4, 41.0, 0),
                new Coordinate(172.5, 33.5, 0),
                new Coordinate(180.0, 30.4, 0),
                new Coordinate(187.5, 33.5, 0),
                new Coordinate(190.6, 41.0, 0),
                new Coordinate(187.5, 48.5, 0),
                new Coordinate(180.0, 51.6, 0),
                new Coordinate(172.5, 48.5, 0),
                new Coordinate(169.4, 41.0, 0)}), 20, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -250, 250)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -250, 250)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -250, 250)), 0.2);

        profileBuilder.finishFeeding();
        
        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 28.5));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }
    /**
     * Test TC14 -- Flat ground with homogeneous acoustic properties and polygonal building –
     * receiver at large height
     */
    //@Test
    public void TC14() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(11., 15.5, 0),
                new Coordinate(12., 13, 0),
                new Coordinate(14.5, 12, 0),
                new Coordinate(17.0, 13, 0),
                new Coordinate(18.0, 15.5, 0),
                new Coordinate(17.0, 18, 0),
                new Coordinate(14.5, 19, 0),
                new Coordinate(12.0, 18, 0),
                new Coordinate(11, 15.5, 0)}), 10, -1);

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(-300, 300, -300, 300)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(25, 20, 23));
        rayData.addSource(factory.createPoint(new Coordinate(8, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }
    /**
     * Test TC15 -- Flat ground with homogeneous acoustic properties and four buildings
     */
    //@Test
    public void TC15() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(55.0, 5.0, 0),
                new Coordinate(65.0, 5.0, 0),
                new Coordinate(65.0, 15.0, 0),
                new Coordinate(55.0, 15.0, 0),
                new Coordinate(55.0, 5.0, 0)}), 8, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(70, 14.5, 0),
                new Coordinate(80.0, 10.2, 0),
                new Coordinate(80.0, 20.2, 0),
                new Coordinate(70, 14.5, 0)}), 12, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(90.1, 19.5, 0),
                new Coordinate(93.3, 17.8, 0),
                new Coordinate(87.3, 6.6, 0),
                new Coordinate(84.1, 8.3, 0),
                new Coordinate(90.1, 19.5, 0)}), 10, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(94.9, 14.1, 0),
                new Coordinate(98.02, 12.37, 0),
                new Coordinate(92.03, 1.2, 0),
                new Coordinate(88.86, 2.9, 0),
                new Coordinate(94.9, 14.1, 0)}), 10, -1);

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(-250, 250, -250, 250)), 0.5);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(100, 15, 5));
        rayData.addSource(factory.createPoint(new Coordinate(50, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
         PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }



    /**
     * Reflecting barrier on ground with spatially varying heights and acoustic properties
     */
    //@Test
    public void TC16() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114, 52, 0),
                new Coordinate(170, 60, 0),
                new Coordinate(170, 62, 0),
                new Coordinate(114, 54, 0),
                new Coordinate(114, 52, 0)}), 15, -1);


        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * Reflecting two barrier on ground with spatially varying heights and acoustic properties
     */
    //@Test
    public void TC16b() throws IOException  {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114, 52, 0),
                new Coordinate(170, 60, 0),
                new Coordinate(170, 62, 0),
                new Coordinate(114, 54, 0),
                new Coordinate(114, 52, 0)}), 20, -1);

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114, 12, 0),
                new Coordinate(170, 30, 0),
                new Coordinate(170, 32, 0),
                new Coordinate(114, 14, 0),
                new Coordinate(114, 12, 0)}), 20, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 15));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * TC17 - Reflecting barrier on ground with spatially varying heights and acoustic properties
     * reduced receiver height
     */
    //@Test
    public void TC17() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114, 52, 0),
                new Coordinate(170, 60, 0),
                new Coordinate(170, 62, 0),
                new Coordinate(114, 54, 0),
                new Coordinate(114, 52, 0)}), 15, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 11.5));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * TC18 - Screening and reflecting barrier on ground with spatially varying heights and
     * acoustic properties
     */
    //@Test
    public void TC18() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114, 52),
                new Coordinate(170, 60),
                new Coordinate(170, 61),
                new Coordinate(114, 53),
                new Coordinate(114, 52)}), 15, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(87, 50),
                new Coordinate(92, 32),
                new Coordinate(92, 33),
                new Coordinate(87, 51),
                new Coordinate(87, 50)}), 12, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 12));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * TC18b - Screening and reflecting barrier on ground with spatially varying heights and
     * acoustic properties
     */
    //@Test
    public void TC18b() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114, 52),
                new Coordinate(170, 60),
                new Coordinate(170, 61),
                new Coordinate(114, 53),
                new Coordinate(114, 52)}), 15, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(87, 50),
                new Coordinate(92, 32),
                new Coordinate(92, 33),
                new Coordinate(87, 51),
                new Coordinate(87, 50)}), 12, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 50, 21.7));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }



     /**
     * TC19 - Complex object and 2 barriers on ground with spatially varying heights and
     * acoustic properties
     */
    //@Test
    public void TC19() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();



        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(156, 28),
                new Coordinate(145, 7),
                new Coordinate(145, 8),
                new Coordinate(156, 29),
                new Coordinate(156, 28)}), 14, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(175, 35),
                new Coordinate(188, 19),
                new Coordinate(188, 20),
                new Coordinate(175, 36),
                new Coordinate(175, 35)}), 14.5, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(100, 24),
                new Coordinate(118, 24),
                new Coordinate(118, 30),
                new Coordinate(100, 30),
                new Coordinate(100, 24)}), 12, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(100, 15.1),
                new Coordinate(118, 15.1),
                new Coordinate(118, 23.9),
                new Coordinate(100, 23.9),
                new Coordinate(100, 15.1)}), 7, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(100, 9),
                new Coordinate(118, 9),
                new Coordinate(118, 15),
                new Coordinate(100, 15),
                new Coordinate(100, 9)}), 12, -1);


        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 30, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * TC20 -Ground with spatially varying heights and acoustic properties
     */
    //@Test
    public void TC20() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(167.2, 39.5),
                new Coordinate(151.6, 48.5),
                new Coordinate(141.1, 30.3),
                new Coordinate(156.7, 21.3),
                new Coordinate(159.7, 26.5),
                new Coordinate(151.0, 31.5),
                new Coordinate(155.5, 39.3),
                new Coordinate(164.2, 34.3),
                new Coordinate(167.2, 39.5)}), 0, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 25, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));

        rayData.setComputeHorizontalDiffraction(false);
        rayData.setComputeVerticalDiffraction(false);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * TC21 - Building on ground with spatially varying heights and acoustic properties
     */
    //@Test
    public void TC21() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(167.2, 39.5),
                new Coordinate(151.6, 48.5),
                new Coordinate(141.1, 30.3),
                new Coordinate(156.7, 21.3),
                new Coordinate(159.7, 26.5),
                new Coordinate(151.0, 31.5),
                new Coordinate(155.5, 39.3),
                new Coordinate(164.2, 34.3),
                new Coordinate(167.2, 39.5)}), 11.5, -1);

        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(200, 25, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


    /**
     * TC22 - Building with receiver backside on ground with spatially varying heights and
     * acoustic properties
     */
    //@Test
    public void TC22() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(197, 36.0, 0),
                new Coordinate(179, 36, 0),
                new Coordinate(179, 15, 0),
                new Coordinate(197, 15, 0),
                new Coordinate(197, 21, 0),
                new Coordinate(187, 21, 0),
                new Coordinate(187, 30, 0),
                new Coordinate(197, 30, 0),
                new Coordinate(197, 36, 0)}), 20, -1);


        //x1
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        //x2
        profileBuilder.addTopographicPoint(new Coordinate(225, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(225, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, -20, 0));
        profileBuilder.addTopographicPoint(new Coordinate(0, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(120, 80, 0));
        profileBuilder.addTopographicPoint(new Coordinate(205, -5, 10));
        profileBuilder.addTopographicPoint(new Coordinate(205, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, 75, 10));
        profileBuilder.addTopographicPoint(new Coordinate(185, -5, 10));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -100, 100)), 0.9);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -100, 100)), 0.5);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(150, 225, -100, 100)), 0.2);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(187.05, 25, 14));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);
        rayData.setComputeHorizontalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }


     /**
     * TC23 – Two buildings behind an earth-berm on flat ground with homogeneous acoustic
     * properties
     */
    //@Test
    public void TC23() throws IOException {
       GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();


        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(75, 34, 0),
                new Coordinate(110, 34, 0),
                new Coordinate(110, 26, 0),
                new Coordinate(75, 26, 0),
                new Coordinate(75, 34, 0)}), 9, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(83, 18, 0),
                new Coordinate(118, 18, 0),
                new Coordinate(118, 10, 0),
                new Coordinate(83, 10, 0),
                new Coordinate(83, 18, 0)}), 8, -1);

        // Ground Surface

        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(30, -14, 0), // 1
                new Coordinate(122, -14, 0),// 1 - 2
                new Coordinate(122, 45, 0), // 2 - 3
                new Coordinate(30, 45, 0),  // 3 - 4
                new Coordinate(30, -14, 0) // 4
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(59.6, -9.87, 0), // 5
                new Coordinate(76.84, -5.28, 0), // 5-6
                new Coordinate(63.71, 41.16, 0), // 6-7
                new Coordinate(46.27, 36.28, 0), // 7-8
                new Coordinate(59.6, -9.87, 0) // 8
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(46.27, 36.28, 0), // 9
                new Coordinate(54.68, 37.59, 5), // 9-10
                new Coordinate(55.93, 37.93, 5), // 10-11
                new Coordinate(63.71, 41.16, 0) // 11
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(59.6, -9.87, 0), // 12
                new Coordinate(67.35, -6.83, 5), // 12-13
                new Coordinate(68.68, -6.49, 5), // 13-14
                new Coordinate(76.84, -5.28, 0) // 14
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(54.68, 37.59, 5), //15
                new Coordinate(67.35, -6.83, 5)
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(55.93, 37.93, 5), //16
                new Coordinate(68.68, -6.49, 5)
        }));

        // Create porus surface as defined by the test:
        // The surface of the earth berm is porous (G = 1).
        profileBuilder.addGroundEffect(factory.createPolygon(new Coordinate[]{
                new Coordinate(59.6, -9.87, 0), // 5
                new Coordinate(76.84, -5.28, 0), // 5-6
                new Coordinate(63.71, 41.16, 0), // 6-7
                new Coordinate(46.27, 36.28, 0), // 7-8
                new Coordinate(59.6, -9.87, 0) // 8
        }), 1.);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(107, 25.95, 4));
        rayData.addSource(factory.createPoint(new Coordinate(38, 14, 1)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);

        rayData.setGs(0.);


        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * – Two buildings behind an earth-berm on flat ground with homogeneous acoustic properties – receiver position modified
     * @throws IOException
     */
    //@Test
    public void TC24() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();


        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(75, 34, 0),
                new Coordinate(110, 34, 0),
                new Coordinate(110, 26, 0),
                new Coordinate(75, 26, 0),
                new Coordinate(75, 34, 0)}), 9, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(83, 18, 0),
                new Coordinate(118, 18, 0),
                new Coordinate(118, 10, 0),
                new Coordinate(83, 10, 0),
                new Coordinate(83, 18, 0)}), 8, -1);

        // Ground Surface

        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(30, -14, 0), // 1
                new Coordinate(122, -14, 0),// 1 - 2
                new Coordinate(122, 45, 0), // 2 - 3
                new Coordinate(30, 45, 0),  // 3 - 4
                new Coordinate(30, -14, 0) // 4
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(59.6, -9.87, 0), // 5
                new Coordinate(76.84, -5.28, 0), // 5-6
                new Coordinate(63.71, 41.16, 0), // 6-7
                new Coordinate(46.27, 36.28, 0), // 7-8
                new Coordinate(59.6, -9.87, 0) // 8
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(46.27, 36.28, 0), // 9
                new Coordinate(54.68, 37.59, 5), // 9-10
                new Coordinate(55.93, 37.93, 5), // 10-11
                new Coordinate(63.71, 41.16, 0) // 11
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(59.6, -9.87, 0), // 12
                new Coordinate(67.35, -6.83, 5), // 12-13
                new Coordinate(68.68, -6.49, 5), // 13-14
                new Coordinate(76.84, -5.28, 0) // 14
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(54.68, 37.59, 5), //15
                new Coordinate(67.35, -6.83, 5)
        }));
        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(55.93, 37.93, 5), //16
                new Coordinate(68.68, -6.49, 5)
        }));

        // Create porus surface as defined by the test:
        // The surface of the earth berm is porous (G = 1).
        profileBuilder.addGroundEffect(factory.createPolygon(new Coordinate[]{
                new Coordinate(59.6, -9.87, 0), // 5
                new Coordinate(76.84, -5.28, 0), // 5-6
                new Coordinate(63.71, 41.16, 0), // 6-7
                new Coordinate(46.27, 36.28, 0), // 7-8
                new Coordinate(59.6, -9.87, 0) // 8
        }), 1.);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(106, 18.5, 4));
        rayData.addSource(factory.createPoint(new Coordinate(38, 14, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setReflexionOrder(1);

        rayData.setGs(0.);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * – Replacement of the earth-berm by a barrier
     * @throws IOException
     */
    //@Test
    public void TC25() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();


        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(75, 34, 0),
                new Coordinate(110, 34, 0),
                new Coordinate(110, 26, 0),
                new Coordinate(75, 26, 0),
                new Coordinate(75, 34, 0)}), 9, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(83, 18, 0),
                new Coordinate(118, 18, 0),
                new Coordinate(118, 10, 0),
                new Coordinate(83, 10, 0),
                new Coordinate(83, 18, 0)}), 8, -1);

        // screen
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(59.19, 24.47, 0),
                new Coordinate(64.17, 6.95, 0),
                new Coordinate(64.171, 6.951, 0),
                new Coordinate(59.191, 24.471, 0),
                new Coordinate(59.19, 24.47, 0)}), 5, -1);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(106, 18.5, 4));
        rayData.addSource(factory.createPoint(new Coordinate(38, 14, 1)));
        rayData.setComputeHorizontalDiffraction(true);

        rayData.setComputeVerticalDiffraction(true);
        rayData.maxSrcDist = 1500;
        rayData.setReflexionOrder(1);

        rayData.setGs(0.);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }



    /**
     * TC26 – Road source with influence of retrodiffraction
     * @throws IOException
     * */
    //@Test
    public void TC26() throws IOException {


        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        // screen
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(74.0, 52.0, 0),
                new Coordinate(130.0, 60.0, 0),
                new Coordinate(130.01, 60.01, 0),
                new Coordinate(74.01, 52.01, 0),
                new Coordinate(74.0, 52.0, 0)}), 7, -1); // not exacly the same

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(0, 50, -10, 100)), 0.0);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(50, 150, -10, 100)), 0.5);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(120, 50, 8));
        rayData.addSource(factory.createPoint(new Coordinate(10, 10, 0.05)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * TC27 – Road source with influence of retrodiffraction
     * @throws IOException
     * */
    //@Test
    public void TC27() throws IOException {
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        // screen
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(114.0, 52.0, 0),
                new Coordinate(170.0, 60.0, 0),
                new Coordinate(170.01, 60.01, 0),
                new Coordinate(114.01, 52.01, 0),
                new Coordinate(114.0, 52.0, 0)}), 4, -1); // not exacly the same


        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(-200, -200, -0.5), // 5
                new Coordinate(110, -200, -0.5), // 5-6
                new Coordinate(110, 200, -0.5), // 6-7
                new Coordinate(-200, 200, -0.5), // 7-8
                new Coordinate(-200, -200, -0.5) // 8
        }));

        profileBuilder.addTopographicLine(factory.createLineString(new Coordinate[]{
                new Coordinate(111, -200, 0), // 5
                new Coordinate(200, -200, 0), // 5-6
                new Coordinate(200, 200, 0), // 6-7
                new Coordinate(111, 200, 0), // 7-8
                new Coordinate(111, -200, 0) // 8
        }));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(80, 110, 20, 80)), 0.0);
        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(110, 215, 20, 80)), 1.0);


        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);

        rayData.setComputeHorizontalDiffraction(true);

        rayData.addReceiver(new Coordinate(200, 50, 4));
        rayData.addSource(factory.createPoint(new Coordinate(105, 35, -0.45)));
        rayData.setComputeVerticalDiffraction(true);
        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /**
     * TC28 Propagation over a large distance with many buildings between source and
     * receiver
     */
    //@Test
    public void TC28() throws IOException {
        double upKml = 100.;
        GeometryFactory factory = new GeometryFactory();

        //Create profile builder
        ProfileBuilder profileBuilder = new ProfileBuilder();

        // Add building
        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(113, 10, 0),
                new Coordinate(127, 16, 0),
                new Coordinate(102, 70, 0),
                new Coordinate(88, 64, 0),
                new Coordinate(113, 10, 0)}), 6, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(176, 19, 0),
                new Coordinate(164, 88, 0),
                new Coordinate(184, 91, 0),
                new Coordinate(196, 22, 0),
                new Coordinate(176, 19, 0)}), 10, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(250, 70, 0),
                new Coordinate(250, 180, 0),
                new Coordinate(270, 180, 0),
                new Coordinate(270, 70, 0),
                new Coordinate(250, 70, 0)}), 14, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(332, 32, 0),
                new Coordinate(348, 126, 0),
                new Coordinate(361, 108, 0),
                new Coordinate(349, 44, 0),
                new Coordinate(332, 32, 0)}), 10, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(400, 5, 0),
                new Coordinate(400, 85, 0),
                new Coordinate(415, 85, 0),
                new Coordinate(415, 5, 0),
                new Coordinate(400, 5, 0)}), 9, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(444, 47, 0),
                new Coordinate(436, 136, 0),
                new Coordinate(516, 143, 0),
                new Coordinate(521, 89, 0),
                new Coordinate(506, 87, 0),
                new Coordinate(502, 127, 0),
                new Coordinate(452, 123, 0),
                new Coordinate(459, 48, 0),
                new Coordinate(444, 47, 0)}), 12, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(773, 12, 0),
                new Coordinate(728, 90, 0),
                new Coordinate(741, 98, 0),
                new Coordinate(786, 20, 0),
                new Coordinate(773, 12, 0)}), 14, -1);

        profileBuilder.addBuilding(factory.createPolygon(new Coordinate[]{
                new Coordinate(972, 82, 0),
                new Coordinate(979, 121, 0),
                new Coordinate(993, 118, 0),
                new Coordinate(986, 79, 0),
                new Coordinate(972, 82, 0)}), 8, -1);

        //x2
        profileBuilder.addTopographicPoint(new Coordinate(-1300, -1300, 0+upKml));
        profileBuilder.addTopographicPoint(new Coordinate(1300, 1300, 0+upKml));
        profileBuilder.addTopographicPoint(new Coordinate(-1300, 1300, 0+upKml));
        profileBuilder.addTopographicPoint(new Coordinate(1300, -1300, 0+upKml));

        profileBuilder.addGroundEffect(factory.toGeometry(new Envelope(-11, 1011, -300, 300)), 0.5);

        profileBuilder.finishFeeding();


        Scene rayData = new Scene(profileBuilder);
        rayData.addReceiver(new Coordinate(1000, 100, 1+upKml));
        rayData.addSource(factory.createPoint(new Coordinate(0, 50, 4+upKml)));
        rayData.setComputeHorizontalDiffraction(true);
        rayData.maxSrcDist = 1500;
        rayData.setComputeVerticalDiffraction(true);

        PathFinderVisitor propDataOut = new PathFinderVisitor(true);
        PathFinder computeRays = new PathFinder(rayData);
        computeRays.setThreadCount(1);
        computeRays.run(propDataOut);

    }

    /*private static Geometry addGround(ProfileBuilder profileBuilder) {
        List<LineSegment> lineSegments = new ArrayList<>();
        lineSegments.add(new LineSegment(new Coordinate(0, 80, 0), new Coordinate(225, 80, 0)));
        lineSegments.add(new LineSegment(new Coordinate(225, 80, 0), new Coordinate(225, -20, 0)));
        lineSegments.add(new LineSegment(new Coordinate(225, -20, 0 ), new Coordinate(0, -20, 0)));
        lineSegments.add(new LineSegment(new Coordinate(0, -20, 0), new Coordinate(0, 80, 0)));
        lineSegments.add(new LineSegment(new Coordinate(120, -20, 0), new Coordinate(120, 80, 0)));
        lineSegments.add(new LineSegment(new Coordinate(185, -15, 10), new Coordinate(205, -15, 10)));
        lineSegments.add(new LineSegment(new Coordinate(205,-15, 10), new Coordinate(205, 75, 10)));
        lineSegments.add(new LineSegment(new Coordinate(205, 75, 10), new Coordinate(185, 75, 10)));
        lineSegments.add(new LineSegment(new Coordinate(185, 75, 10), new Coordinate(185, -15, 10)));
        lineSegments.add(new LineSegment(new Coordinate(120, 80, 0), new Coordinate(185, 75, 10)));
        lineSegments.add(new LineSegment(new Coordinate(120,-20 ,0), new Coordinate(185, -15, 10)));
        lineSegments.add(new LineSegment(new Coordinate(205, 75, 10), new Coordinate(225, 80, 0)));
        lineSegments.add(new LineSegment(new Coordinate(205, -15, 10), new Coordinate(225, -20, 0)));

        GeometryFactory factory = new GeometryFactory();
        LineString[] segments = new LineString[lineSegments.size()];
        int i = 0;
        for(LineSegment segment : lineSegments) {
            segments[i++] = factory.createLineString(new Coordinate[]{segment.p0, segment.p1});
        }
        Geometry geo = factory.createMultiLineString(segments);
        geo = geo.union();
        geo = Densifier3D.densify(geo, 4);
        for(Coordinate pt : geo.getCoordinates()) {
            profileBuilder.addTopographicPoint(pt);
        }
        return geo;
    }*/

    /**
     * Test vertical edge diffraction ray computation.
     * If the diffraction plane goes under the ground, reject the path
     * @throws ParseException
     */
    @Test
    public void TestVerticalEdgeDiffractionAirplaneSource() throws ParseException {
        GeometryFactory factory = new GeometryFactory();
        WKTReader wktReader = new WKTReader(factory);
        //Scene dimension
        Envelope cellEnvelope = new Envelope();
        Coordinate source = new Coordinate(223512.78, 6757739.7, 500.0);
        Coordinate receiver = new Coordinate(223392.04632028608, 6757724.944483406, 2.0);
        //Create obstruction test object
        ProfileBuilder profileBuilder = new ProfileBuilder();
        profileBuilder.addBuilding(wktReader.read("POLYGON ((223393 6757706, 223402 6757696, 223409 6757703, 223411 6757705, 223414 6757702, 223417 6757704, 223421 6757709, 223423 6757712, 223437 6757725, 223435 6757728, 223441 6757735, 223448 6757741, 223439 6757751, 223433 6757745, 223432 6757745, 223430 6757747, 223417 6757734, 223402 6757720, 223404 6757717, 223393 6757706)) "), 13);

        cellEnvelope.expandToInclude(source);
        cellEnvelope.expandToInclude(receiver);
        cellEnvelope.expandBy(1200);

        profileBuilder.finishFeeding();

        Scene processData = new Scene(profileBuilder);
        // new ArrayList<>(), manager, sourcesIndex, srclst, new ArrayList<>(), new ArrayList<>(), 0, 99, 1000,1000,0,0,new double[0],0,0,new EmptyProgressVisitor(), new ArrayList<>(), true

        PathFinder computeRays = new PathFinder(processData);

        List<Coordinate> ray = computeRays.computeSideHull(false, receiver, source, profileBuilder);
        Assert.assertTrue(ray.isEmpty());

    }
}