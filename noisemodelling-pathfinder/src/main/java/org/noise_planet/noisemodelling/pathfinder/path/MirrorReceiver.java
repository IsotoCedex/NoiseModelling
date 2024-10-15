/**
 * NoiseModelling is a library capable of producing noise maps. It can be freely used either for research and education, as well as by experts in a professional use.
 * <p>
 * NoiseModelling is distributed under GPL 3 license. You can read a copy of this License in the file LICENCE provided with this software.
 * <p>
 * Official webpage : http://noise-planet.org/noisemodelling.html
 * Contact: contact@noise-planet.org
 */

package org.noise_planet.noisemodelling.pathfinder.path;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.noise_planet.noisemodelling.pathfinder.profilebuilder.ProfileBuilder;
import org.noise_planet.noisemodelling.pathfinder.profilebuilder.Wall;

import java.util.Objects;

/**
 *  Information for Receiver image.
 * @author Nicolas Fortin
 */
public class MirrorReceiver {


    private Coordinate receiverPos;
    private final MirrorReceiver parentMirror;
    private final Wall wall;
    private final int buildingId; // building that belongs to this wall
    private final ProfileBuilder.IntersectionType type;
    /**
     * This data is not stored in the RTREE as it is not used after the creation of the index
     */
    Polygon imageReceiverVisibilityCone;

    /**
     * @return coordinate of mirrored receiver
     */
    public Coordinate getReceiverPos() {
        return receiverPos;
    }

    public void setReceiverPos(Coordinate receiverPos) {
        this.receiverPos = receiverPos;
    }

    public MirrorReceiver copyWithoutCone() {
        return new MirrorReceiver(receiverPos, parentMirror == null ? null : parentMirror.copyWithoutCone(),
                wall, buildingId, type);
    }
    /**
     * @return Other MirrorReceiver index, -1 for the first reflexion
     */
    public MirrorReceiver getParentMirror() {
        return parentMirror;
    }

    /**
     * @return Wall index of the last mirrored processed
     */
    public Wall getWall() {
        return wall;
    }

    /**
     * @return building that belongs to this wall
     */
    public int getBuildingId() {
        return buildingId;
    }

    /**
     * @param receiverPos coordinate of mirrored receiver
     * @param mirrorResultId Other MirrorReceiver index, -1 for the first reflexion
     * @param wallId Wall index of the last mirrored processed
     * @param buildingId building that belongs to this wall
     */
    public MirrorReceiver(Coordinate receiverPos, MirrorReceiver parentMirror, Wall wall, int buildingId,
                          ProfileBuilder.IntersectionType type) {
        this.receiverPos = receiverPos;
        this.parentMirror = parentMirror;
        this.wall = wall;
        this.buildingId = buildingId;
        this.type = type;
    }

    public Polygon getImageReceiverVisibilityCone() {
        return imageReceiverVisibilityCone;
    }

    public void setImageReceiverVisibilityCone(Polygon imageReceiverVisibilityCone) {
        this.imageReceiverVisibilityCone = imageReceiverVisibilityCone;
    }

    /**
     * Copy constructor
     * @param cpy ref
     */
    public MirrorReceiver(MirrorReceiver cpy) {
        this.receiverPos = new Coordinate(cpy.receiverPos);
        this.parentMirror = cpy.parentMirror;
        this.wall = cpy.wall;
        this.buildingId = cpy.buildingId;
        this.type = cpy.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MirrorReceiver that = (MirrorReceiver) o;
        return wall == that.wall && buildingId == that.buildingId && receiverPos.equals(that.receiverPos) && Objects.equals(parentMirror, that.parentMirror);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiverPos, parentMirror, wall, buildingId);
    }

    public ProfileBuilder.IntersectionType getType() {
        return type;
    }
}
